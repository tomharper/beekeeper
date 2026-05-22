from enum import Enum as PyEnum
from datetime import datetime
from typing import Optional

from beanie import Document

from .base import TimestampMixin


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


class Task(Document, TimestampMixin):
    id: str  # type: ignore[assignment]
    title: str
    description: str = ""
    task_type: TaskType = TaskType.GENERAL

    # Scheduling
    due_date: datetime
    reminder_date: Optional[datetime] = None

    # Recurrence
    recurrence_frequency: Optional[RecurrenceFrequency] = None
    recurrence_interval: Optional[int] = 1
    recurrence_end_date: Optional[datetime] = None
    recurrence_count: Optional[int] = None

    # Association
    hive_id: Optional[str] = None
    apiary_id: Optional[str] = None
    user_id: str

    # Status
    status: TaskStatus = TaskStatus.PENDING
    priority: TaskPriority = TaskPriority.MEDIUM
    completed_date: Optional[datetime] = None

    # Additional info
    estimated_duration_minutes: Optional[int] = None
    weather_dependent: bool = False
    minimum_temperature: Optional[float] = None
    notes: str = ""

    class Settings:
        name = "tasks"
        indexes = [
            "hive_id",
            "apiary_id",
            [("user_id", 1), ("due_date", 1)],
            [("user_id", 1), ("status", 1)],
        ]
