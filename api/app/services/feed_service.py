from typing import List, Optional
from datetime import datetime
from beanie.operators import In

from app.models import User
from app.repositories import InspectionRepository
from app.services.follow_service import FollowService
from app.schemas.feed import FeedItemResponse
from app.schemas.follow import UserSummary
from app.schemas.inspection import InspectionResponse


class FeedService:
    def __init__(self):
        self.inspections = InspectionRepository()
        self.follows = FollowService()

    async def get_feed(
        self, user_id: str, limit: int = 20, before: Optional[datetime] = None
    ) -> List[FeedItemResponse]:
        followed_ids = await self.follows.get_following_ids(user_id)
        if not followed_ids:
            return []

        inspections = await self.inspections.get_feed(followed_ids, limit, before)
        if not inspections:
            return []

        # Denormalize author names in one batch query
        author_ids = list({i.user_id for i in inspections})
        users = await User.find(In(User.id, author_ids)).to_list()
        names = {u.id: u.full_name for u in users}

        return [
            FeedItemResponse(
                type="inspection",
                author=UserSummary(
                    id=insp.user_id, full_name=names.get(insp.user_id, "Unknown")
                ),
                occurred_at=insp.inspection_date,
                inspection=InspectionResponse.model_validate(insp),
            )
            for insp in inspections
        ]
