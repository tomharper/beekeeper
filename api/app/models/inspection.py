from enum import Enum as PyEnum
from datetime import datetime
from sqlalchemy import String, DateTime, ForeignKey, Enum, Integer, Float, Boolean, Text
from sqlalchemy.orm import Mapped, mapped_column, relationship
from typing import Optional

from .base import Base, TimestampMixin


class QueenCellStatus(str, PyEnum):
    NONE = "NONE"
    QUEEN_CUPS = "QUEEN_CUPS"
    CHARGED_CELLS = "CHARGED_CELLS"
    CAPPED_CELLS = "CAPPED_CELLS"
    SUPERSEDURE_CELLS = "SUPERSEDURE_CELLS"
    SWARM_CELLS = "SWARM_CELLS"


class BroodPattern(str, PyEnum):
    EXCELLENT = "EXCELLENT"
    GOOD = "GOOD"
    SPOTTY = "SPOTTY"
    POOR = "POOR"
    NONE = "NONE"


class ColonyTemperament(str, PyEnum):
    VERY_CALM = "VERY_CALM"
    CALM = "CALM"
    MODERATE = "MODERATE"
    DEFENSIVE = "DEFENSIVE"
    AGGRESSIVE = "AGGRESSIVE"
    VERY_AGGRESSIVE = "VERY_AGGRESSIVE"


class ColonyPopulation(str, PyEnum):
    VERY_WEAK = "VERY_WEAK"
    WEAK = "WEAK"
    MEDIUM = "MEDIUM"
    STRONG = "STRONG"
    VERY_STRONG = "VERY_STRONG"


class HealthStatus(str, PyEnum):
    EXCELLENT = "EXCELLENT"
    HEALTHY = "HEALTHY"
    CONCERNING = "CONCERNING"
    NEEDS_ATTENTION = "NEEDS_ATTENTION"
    CRITICAL = "CRITICAL"


class ResourceLevel(str, PyEnum):
    NONE = "NONE"
    VERY_LOW = "VERY_LOW"
    LOW = "LOW"
    ADEQUATE = "ADEQUATE"
    GOOD = "GOOD"
    EXCELLENT = "EXCELLENT"


class Inspection(Base, TimestampMixin):
    __tablename__ = "inspections"

    id: Mapped[str] = mapped_column(String, primary_key=True)
    hive_id: Mapped[str] = mapped_column(
        String, ForeignKey("hives.id", ondelete="CASCADE"), nullable=False
    )
    user_id: Mapped[str] = mapped_column(
        String, ForeignKey("users.id", ondelete="CASCADE"), nullable=False
    )

    # Basic info
    inspection_date: Mapped[datetime] = mapped_column(DateTime, nullable=False)
    duration_minutes: Mapped[Optional[int]] = mapped_column(Integer, nullable=True)

    # Weather (simple fields for now)
    weather_temp: Mapped[Optional[float]] = mapped_column(Float, nullable=True)
    weather_conditions: Mapped[Optional[str]] = mapped_column(String, nullable=True)

    # Queen observations
    queen_seen: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)
    queen_marked: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)
    queen_cells: Mapped[QueenCellStatus] = mapped_column(
        Enum(QueenCellStatus), default=QueenCellStatus.NONE, nullable=False
    )

    # Brood observations
    brood_pattern: Mapped[BroodPattern] = mapped_column(
        Enum(BroodPattern), default=BroodPattern.GOOD, nullable=False
    )
    eggs_seen: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)
    larvae_seen: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)
    capped_brood_seen: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)
    estimated_brood_frames: Mapped[Optional[int]] = mapped_column(Integer, nullable=True)

    # Colony observations
    temperament: Mapped[ColonyTemperament] = mapped_column(
        Enum(ColonyTemperament), default=ColonyTemperament.CALM, nullable=False
    )
    population: Mapped[ColonyPopulation] = mapped_column(
        Enum(ColonyPopulation), default=ColonyPopulation.MEDIUM, nullable=False
    )
    estimated_frames_covered: Mapped[Optional[int]] = mapped_column(Integer, nullable=True)

    # Health observations
    health_status: Mapped[HealthStatus] = mapped_column(
        Enum(HealthStatus), default=HealthStatus.HEALTHY, nullable=False
    )
    varroa_mites_detected: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)
    pests_notes: Mapped[str] = mapped_column(Text, default="", nullable=False)
    disease_signs: Mapped[str] = mapped_column(Text, default="", nullable=False)

    # Resources
    honey_stores: Mapped[ResourceLevel] = mapped_column(
        Enum(ResourceLevel), default=ResourceLevel.ADEQUATE, nullable=False
    )
    pollen_stores: Mapped[ResourceLevel] = mapped_column(
        Enum(ResourceLevel), default=ResourceLevel.ADEQUATE, nullable=False
    )
    capped_honey: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)

    # Actions taken
    actions_taken: Mapped[str] = mapped_column(Text, default="", nullable=False)  # JSON or comma-separated
    feeding_done: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)
    feeding_notes: Mapped[str] = mapped_column(Text, default="", nullable=False)
    treatment_applied: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)
    treatment_notes: Mapped[str] = mapped_column(Text, default="", nullable=False)

    # Media and notes
    photos: Mapped[str] = mapped_column(Text, default="", nullable=False)  # JSON array of URLs
    notes: Mapped[str] = mapped_column(Text, default="", nullable=False)
    next_inspection_date: Mapped[Optional[datetime]] = mapped_column(DateTime, nullable=True)

    # Relationships
    hive: Mapped["Hive"] = relationship("Hive", foreign_keys=[hive_id])
    user: Mapped["User"] = relationship("User", back_populates="inspections")
