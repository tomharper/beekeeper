from datetime import datetime, timezone
from pydantic import BaseModel, Field


def utcnow() -> datetime:
    return datetime.now(timezone.utc)


class TimestampMixin(BaseModel):
    """Mixin to add created_at and updated_at timestamps to a Beanie Document."""

    created_at: datetime = Field(default_factory=utcnow)
    updated_at: datetime = Field(default_factory=utcnow)
