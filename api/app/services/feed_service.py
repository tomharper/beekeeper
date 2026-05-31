from typing import List, Optional
from datetime import datetime
from beanie.operators import In

from app.models import User
from app.repositories import InspectionRepository, TaskRepository
from app.services.follow_service import FollowService
from app.schemas.feed import FeedItemResponse
from app.schemas.follow import UserSummary
from app.schemas.inspection import InspectionResponse
from app.schemas.task import TaskResponse


class FeedService:
    def __init__(self):
        self.inspections = InspectionRepository()
        self.tasks = TaskRepository()
        self.follows = FollowService()

    async def get_feed(
        self, user_id: str, limit: int = 20, before: Optional[datetime] = None
    ) -> List[FeedItemResponse]:
        followed_ids = await self.follows.get_following_ids(user_id)
        if not followed_ids:
            return []

        inspections = await self.inspections.get_feed(followed_ids, limit, before)
        tasks = await self.tasks.get_feed(followed_ids, limit, before)
        if not inspections and not tasks:
            return []

        # Denormalize author names in one batch query across both record kinds
        author_ids = list(
            {i.user_id for i in inspections} | {t.user_id for t in tasks}
        )
        users = await User.find(In(User.id, author_ids)).to_list()
        names = {u.id: u.full_name for u in users}

        def author(uid: str) -> UserSummary:
            return UserSummary(id=uid, full_name=names.get(uid, "Unknown"))

        items: List[FeedItemResponse] = [
            FeedItemResponse(
                type="inspection",
                author=author(insp.user_id),
                occurred_at=insp.inspection_date,
                inspection=InspectionResponse.model_validate(insp),
            )
            for insp in inspections
        ]
        items += [
            FeedItemResponse(
                type="task",
                author=author(task.user_id),
                occurred_at=task.due_date,
                task=TaskResponse.model_validate(task),
            )
            for task in tasks
        ]

        items.sort(key=lambda item: item.occurred_at, reverse=True)
        return items[:limit]
