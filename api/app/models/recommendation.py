from enum import Enum as PyEnum
from sqlalchemy import String, ForeignKey, Enum, Text
from sqlalchemy.orm import Mapped, mapped_column, relationship

from .base import Base, TimestampMixin


class RecommendationType(str, PyEnum):
    POSITIVE = "POSITIVE"
    WARNING = "WARNING"
    ACTION_REQUIRED = "ACTION_REQUIRED"
    INFO = "INFO"


class Priority(str, PyEnum):
    HIGH = "HIGH"
    MEDIUM = "MEDIUM"
    LOW = "LOW"


class Recommendation(Base, TimestampMixin):
    __tablename__ = "recommendations"

    id: Mapped[str] = mapped_column(String, primary_key=True)
    hive_id: Mapped[str] = mapped_column(
        String, ForeignKey("hives.id"), nullable=False
    )
    type: Mapped[RecommendationType] = mapped_column(
        Enum(RecommendationType), nullable=False
    )
    title: Mapped[str] = mapped_column(String, nullable=False)
    description: Mapped[str] = mapped_column(Text, nullable=False)
    priority: Mapped[Priority] = mapped_column(
        Enum(Priority), default=Priority.MEDIUM, nullable=False
    )

    # Relationships
    hive: Mapped["Hive"] = relationship("Hive", back_populates="recommendations")
