from datetime import datetime
from typing import Optional

from beanie import Document

from .base import TimestampMixin


class Event(Document, TimestampMixin):
    # Identity — plain str UUID to match the rest of the schema
    id: str  # type: ignore[assignment]
    user_id: str
    title: str
    description: str = ""
    event_date: datetime
    end_date: Optional[datetime] = None
    location: Optional[str] = None
    all_day: bool = False

    # Visibility — public events flow to followers' shared calendar.
    is_public: bool = True

    class Settings:
        name = "events"
        indexes = [
            [("user_id", 1), ("event_date", 1)],
            # Calendar query: public events from followed users in a date range
            [("is_public", 1), ("event_date", 1)],
        ]
