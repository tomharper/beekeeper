from enum import Enum as PyEnum

from beanie import Document

from .base import TimestampMixin


class RecommendationType(str, PyEnum):
    POSITIVE = "POSITIVE"
    WARNING = "WARNING"
    ACTION_REQUIRED = "ACTION_REQUIRED"
    INFO = "INFO"


class Priority(str, PyEnum):
    HIGH = "HIGH"
    MEDIUM = "MEDIUM"
    LOW = "LOW"


class Recommendation(Document, TimestampMixin):
    id: str  # type: ignore[assignment]
    hive_id: str
    type: RecommendationType
    title: str
    description: str
    priority: Priority = Priority.MEDIUM

    class Settings:
        name = "recommendations"
        indexes = ["hive_id"]
