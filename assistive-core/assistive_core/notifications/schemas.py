"""Notification response schema. Ported from beekeeper schemas/notification.py."""
from datetime import datetime
from typing import Optional

from pydantic import BaseModel, ConfigDict


def to_camel(string: str) -> str:
    """Convert snake_case to camelCase (ports beekeeper's to_camel)."""
    words = string.split("_")
    return words[0] + "".join(word.capitalize() for word in words[1:])


class NotificationResponse(BaseModel):
    id: str
    user_id: str
    type: str
    title: str
    message: str = ""
    is_read: bool = False
    ref_type: Optional[str] = None
    ref_id: Optional[str] = None
    created_at: datetime

    model_config = ConfigDict(
        from_attributes=True, alias_generator=to_camel, populate_by_name=True
    )
