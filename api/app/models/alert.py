from enum import Enum as PyEnum
from datetime import datetime

from beanie import Document
from pydantic import Field

from .base import TimestampMixin


class AlertType(str, PyEnum):
    SWARM_WARNING = "SWARM_WARNING"
    VARROA_MITE = "VARROA_MITE"
    INSPECTION_DUE = "INSPECTION_DUE"
    TREATMENT_DUE = "TREATMENT_DUE"
    WEATHER_WARNING = "WEATHER_WARNING"
    HONEY_FLOW = "HONEY_FLOW"
    GENERAL = "GENERAL"


class AlertSeverity(str, PyEnum):
    INFO = "INFO"
    WARNING = "WARNING"
    CRITICAL = "CRITICAL"


class Alert(Document, TimestampMixin):
    id: str  # type: ignore[assignment]
    type: AlertType
    title: str
    message: str
    severity: AlertSeverity = AlertSeverity.INFO
    timestamp: datetime
    # Was comma-separated string in SQLAlchemy; real array in Mongo.
    hive_ids: list[str] = Field(default_factory=list)
    dismissed: bool = False

    class Settings:
        name = "alerts"
        indexes = [
            [("dismissed", 1), ("timestamp", -1)],
        ]
