from enum import Enum as PyEnum
from datetime import datetime
from typing import Optional

from beanie import Document

from .base import TimestampMixin


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


class Hive(Document, TimestampMixin):
    id: str  # type: ignore[assignment]
    name: str
    apiary_id: str
    status: HiveStatus = HiveStatus.STRONG
    last_inspected: datetime
    image_url: Optional[str] = None
    colony_strength: ColonyStrength = ColonyStrength.MODERATE
    queen_status: QueenStatus = QueenStatus.UNKNOWN
    temperament: Temperament = Temperament.MODERATE
    honey_stores: HoneyStores = HoneyStores.ADEQUATE

    class Settings:
        name = "hives"
        indexes = ["apiary_id"]
