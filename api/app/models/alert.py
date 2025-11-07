from enum import Enum as PyEnum
from datetime import datetime
from sqlalchemy import String, DateTime, Boolean, Enum, Text
from sqlalchemy.orm import Mapped, mapped_column
from typing import Optional

from .base import Base, TimestampMixin


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


class Alert(Base, TimestampMixin):
    __tablename__ = "alerts"

    id: Mapped[str] = mapped_column(String, primary_key=True)
    type: Mapped[AlertType] = mapped_column(Enum(AlertType), nullable=False)
    title: Mapped[str] = mapped_column(String, nullable=False)
    message: Mapped[str] = mapped_column(Text, nullable=False)
    severity: Mapped[AlertSeverity] = mapped_column(
        Enum(AlertSeverity), default=AlertSeverity.INFO, nullable=False
    )
    timestamp: Mapped[datetime] = mapped_column(DateTime, nullable=False)
    hive_ids: Mapped[Optional[str]] = mapped_column(
        String, nullable=True
    )  # Comma-separated hive IDs
    dismissed: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)
