"""Shared model primitives. Ported from beekeeper api/app/models/base.py."""
from datetime import datetime, timezone

from pydantic import BaseModel, Field


def utcnow() -> datetime:
    return datetime.now(timezone.utc)


class TimestampMixin(BaseModel):
    """Adds created_at / updated_at to a Beanie Document."""

    created_at: datetime = Field(default_factory=utcnow)
    updated_at: datetime = Field(default_factory=utcnow)
