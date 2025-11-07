from enum import Enum as PyEnum
from datetime import datetime
from sqlalchemy import String, DateTime, ForeignKey, Enum
from sqlalchemy.orm import Mapped, mapped_column, relationship
from typing import Optional, List

from .base import Base, TimestampMixin


class HiveStatus(str, PyEnum):
    STRONG = "STRONG"
    ALERT = "ALERT"
    NEEDS_INSPECTION = "NEEDS_INSPECTION"
    WEAK = "WEAK"


class ColonyStrength(str, PyEnum):
    STRONG = "STRONG"
    MODERATE = "MODERATE"
    WEAK = "WEAK"


class QueenStatus(str, PyEnum):
    LAYING = "LAYING"
    NOT_LAYING = "NOT_LAYING"
    MISSING = "MISSING"
    UNKNOWN = "UNKNOWN"


class Temperament(str, PyEnum):
    CALM = "CALM"
    MODERATE = "MODERATE"
    DEFENSIVE = "DEFENSIVE"


class HoneyStores(str, PyEnum):
    FULL = "FULL"
    ADEQUATE = "ADEQUATE"
    LOW = "LOW"
    EMPTY = "EMPTY"


class Hive(Base, TimestampMixin):
    __tablename__ = "hives"

    id: Mapped[str] = mapped_column(String, primary_key=True)
    name: Mapped[str] = mapped_column(String, nullable=False)
    apiary_id: Mapped[str] = mapped_column(
        String, ForeignKey("apiaries.id"), nullable=False
    )
    status: Mapped[HiveStatus] = mapped_column(
        Enum(HiveStatus), default=HiveStatus.STRONG, nullable=False
    )
    last_inspected: Mapped[datetime] = mapped_column(DateTime, nullable=False)
    image_url: Mapped[Optional[str]] = mapped_column(String, nullable=True)
    colony_strength: Mapped[ColonyStrength] = mapped_column(
        Enum(ColonyStrength), default=ColonyStrength.MODERATE, nullable=False
    )
    queen_status: Mapped[QueenStatus] = mapped_column(
        Enum(QueenStatus), default=QueenStatus.UNKNOWN, nullable=False
    )
    temperament: Mapped[Temperament] = mapped_column(
        Enum(Temperament), default=Temperament.MODERATE, nullable=False
    )
    honey_stores: Mapped[HoneyStores] = mapped_column(
        Enum(HoneyStores), default=HoneyStores.ADEQUATE, nullable=False
    )

    # Relationships
    apiary: Mapped["Apiary"] = relationship("Apiary", back_populates="hives")
    recommendations: Mapped[List["Recommendation"]] = relationship(
        "Recommendation", back_populates="hive", cascade="all, delete-orphan"
    )
