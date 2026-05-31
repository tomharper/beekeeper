from typing import Optional

from beanie import Document

from .base import TimestampMixin


class Notification(Document, TimestampMixin):
    # Identity — plain str UUID to match the rest of the schema
    id: str  # type: ignore[assignment]
    user_id: str  # the RECIPIENT of the notification (User.id)
    type: str  # e.g. "inspection", "task", "event"
    title: str
    message: str = ""
    is_read: bool = False

    # Optional reference to the record that triggered the notification
    ref_type: Optional[str] = None
    ref_id: Optional[str] = None

    class Settings:
        name = "notifications"
        indexes = [
            # Recipient inbox: unread filter, newest first
            [("user_id", 1), ("is_read", 1), ("created_at", -1)],
        ]
