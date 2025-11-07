from enum import Enum as PyEnum
from datetime import datetime
from sqlalchemy import String, DateTime, ForeignKey, Enum, Integer, Float, Boolean, Text
from sqlalchemy.orm import Mapped, mapped_column, relationship
from typing import Optional

from .base import Base, TimestampMixin


class TaskType(str, PyEnum):
    # Regular maintenance
    INSPECTION = "INSPECTION"
    FEEDING = "FEEDING"
    WATER_CHECK = "WATER_CHECK"

    # Seasonal
    SPRING_INSPECTION = "SPRING_INSPECTION"
    SUMMER_INSPECTION = "SUMMER_INSPECTION"
    FALL_PREPARATION = "FALL_PREPARATION"
    WINTER_CHECK = "WINTER_CHECK"

    # Health
    PEST_TREATMENT = "PEST_TREATMENT"
    DISEASE_TREATMENT = "DISEASE_TREATMENT"
    MEDICATION = "MEDICATION"

    # Production
    HARVEST_HONEY = "HARVEST_HONEY"
    EXTRACT_HONEY = "EXTRACT_HONEY"
    BOTTLE_HONEY = "BOTTLE_HONEY"
    HARVEST_WAX = "HARVEST_WAX"
    HARVEST_PROPOLIS = "HARVEST_PROPOLIS"

    # Colony management
    SPLIT_HIVE = "SPLIT_HIVE"
    COMBINE_HIVES = "COMBINE_HIVES"
    REQUEEN = "REQUEEN"
    SWARM_PREVENTION = "SWARM_PREVENTION"
    SWARM_COLLECTION = "SWARM_COLLECTION"

    # Equipment
    ADD_BOXES = "ADD_BOXES"
    REMOVE_BOXES = "REMOVE_BOXES"
    CLEAN_EQUIPMENT = "CLEAN_EQUIPMENT"
    REPAIR_EQUIPMENT = "REPAIR_EQUIPMENT"
    BUILD_FRAMES = "BUILD_FRAMES"

    # Other
    EDUCATION = "EDUCATION"
    RECORD_KEEPING = "RECORD_KEEPING"
    ORDER_SUPPLIES = "ORDER_SUPPLIES"
    GENERAL = "GENERAL"
    OTHER = "OTHER"


class TaskStatus(str, PyEnum):
    PENDING = "PENDING"
    IN_PROGRESS = "IN_PROGRESS"
    COMPLETED = "COMPLETED"
    CANCELLED = "CANCELLED"
    OVERDUE = "OVERDUE"


class TaskPriority(str, PyEnum):
    LOW = "LOW"
    MEDIUM = "MEDIUM"
    HIGH = "HIGH"
    URGENT = "URGENT"


class RecurrenceFrequency(str, PyEnum):
    DAILY = "DAILY"
    WEEKLY = "WEEKLY"
    BIWEEKLY = "BIWEEKLY"
    MONTHLY = "MONTHLY"
    QUARTERLY = "QUARTERLY"
    YEARLY = "YEARLY"


class Task(Base, TimestampMixin):
    __tablename__ = "tasks"

    id: Mapped[str] = mapped_column(String, primary_key=True)
    title: Mapped[str] = mapped_column(String, nullable=False)
    description: Mapped[str] = mapped_column(Text, default="", nullable=False)
    task_type: Mapped[TaskType] = mapped_column(
        Enum(TaskType), default=TaskType.GENERAL, nullable=False
    )

    # Scheduling
    due_date: Mapped[datetime] = mapped_column(DateTime, nullable=False)
    reminder_date: Mapped[Optional[datetime]] = mapped_column(DateTime, nullable=True)

    # Recurrence (stored as JSON-like fields)
    recurrence_frequency: Mapped[Optional[RecurrenceFrequency]] = mapped_column(
        Enum(RecurrenceFrequency), nullable=True
    )
    recurrence_interval: Mapped[Optional[int]] = mapped_column(Integer, nullable=True, default=1)
    recurrence_end_date: Mapped[Optional[datetime]] = mapped_column(DateTime, nullable=True)
    recurrence_count: Mapped[Optional[int]] = mapped_column(Integer, nullable=True)

    # Association
    hive_id: Mapped[Optional[str]] = mapped_column(
        String, ForeignKey("hives.id", ondelete="CASCADE"), nullable=True
    )
    apiary_id: Mapped[Optional[str]] = mapped_column(
        String, ForeignKey("apiaries.id", ondelete="CASCADE"), nullable=True
    )
    user_id: Mapped[str] = mapped_column(
        String, ForeignKey("users.id", ondelete="CASCADE"), nullable=False
    )

    # Status
    status: Mapped[TaskStatus] = mapped_column(
        Enum(TaskStatus), default=TaskStatus.PENDING, nullable=False
    )
    priority: Mapped[TaskPriority] = mapped_column(
        Enum(TaskPriority), default=TaskPriority.MEDIUM, nullable=False
    )
    completed_date: Mapped[Optional[datetime]] = mapped_column(DateTime, nullable=True)

    # Additional info
    estimated_duration_minutes: Mapped[Optional[int]] = mapped_column(Integer, nullable=True)
    weather_dependent: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)
    minimum_temperature: Mapped[Optional[float]] = mapped_column(Float, nullable=True)
    notes: Mapped[str] = mapped_column(Text, default="", nullable=False)

    # Relationships
    hive: Mapped[Optional["Hive"]] = relationship("Hive", foreign_keys=[hive_id])
    apiary: Mapped[Optional["Apiary"]] = relationship("Apiary", foreign_keys=[apiary_id])
    user: Mapped["User"] = relationship("User", back_populates="tasks")
