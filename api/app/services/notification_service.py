import uuid
from typing import List, Optional
from fastapi import HTTPException, status

from app.models import Notification
from app.repositories.notification_repository import NotificationRepository
from app.repositories.follow_repository import FollowRepository
from app.schemas.notification import NotificationResponse


class NotificationService:
    def __init__(self):
        self.repository = NotificationRepository()
        self.follows = FollowRepository()

    async def create_for_followers(
        self,
        actor_id: str,
        type: str,
        title: str,
        message: str = "",
        ref_type: Optional[str] = None,
        ref_id: Optional[str] = None,
    ) -> None:
        """Fan out a notification to everyone who follows the actor."""
        followers = await self.follows.get_followers(actor_id)
        if not followers:
            return
        notifications = [
            Notification(
                id=str(uuid.uuid4()),
                user_id=f.follower_id,
                type=type,
                title=title,
                message=message,
                ref_type=ref_type,
                ref_id=ref_id,
            )
            for f in followers
        ]
        await self.repository.insert_many(notifications)

    async def list_for_user(self, user_id: str) -> List[NotificationResponse]:
        notifications = await self.repository.get_by_user_id(user_id)
        return [NotificationResponse.model_validate(n) for n in notifications]

    async def mark_read(self, notification_id: str, user_id: str) -> NotificationResponse:
        notification = await self.repository.get_by_id(notification_id)
        if not notification:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND, detail="Notification not found"
            )
        if notification.user_id != user_id:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Not authorized to modify this notification",
            )
        notification.is_read = True
        updated = await self.repository.update(notification)
        return NotificationResponse.model_validate(updated)

    async def mark_all_read(self, user_id: str) -> None:
        await self.repository.mark_all_read(user_id)
