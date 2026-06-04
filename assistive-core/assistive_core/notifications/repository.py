"""Notification repository.
Ported from beekeeper api/app/repositories/notification_repository.py."""
from typing import List, Optional

from .models import Notification


class NotificationRepository:
    async def get_by_id(self, notification_id: str) -> Optional[Notification]:
        return await Notification.get(notification_id)

    async def get_by_user_id(self, user_id: str) -> List[Notification]:
        return (
            await Notification.find(Notification.user_id == user_id)
            .sort(-Notification.created_at)
            .to_list()
        )

    async def create(self, notification: Notification) -> Notification:
        await notification.insert()
        return notification

    async def insert_many(self, notifications: List[Notification]) -> None:
        await Notification.insert_many(notifications)

    async def update(self, notification: Notification) -> Notification:
        await notification.save()
        return notification

    async def mark_all_read(self, user_id: str) -> None:
        await Notification.find(
            Notification.user_id == user_id,
            Notification.is_read == False,  # noqa: E712
        ).update({"$set": {"is_read": True}})
