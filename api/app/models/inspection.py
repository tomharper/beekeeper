from enum import Enum as PyEnum
from datetime import datetime
from typing import Optional

from beanie import Document
from pydantic import Field

from .base import TimestampMixin, utcnow


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


class Inspection(Document, TimestampMixin):
    # Identity — keep as plain str (UUID-style) to match existing schema/routers
    id: str  # type: ignore[assignment]
    hive_id: str
    user_id: str

    # Basic info
    inspection_date: datetime
    duration_minutes: Optional[int] = None

    # Weather (simple fields for now)
    weather_temp: Optional[float] = None
    weather_conditions: Optional[str] = None

    # Queen observations
    queen_seen: bool = False
    queen_marked: bool = False
    queen_cells: QueenCellStatus = QueenCellStatus.NONE

    # Brood observations
    brood_pattern: BroodPattern = BroodPattern.GOOD
    eggs_seen: bool = False
    larvae_seen: bool = False
    capped_brood_seen: bool = False
    estimated_brood_frames: Optional[int] = None

    # Colony observations
    temperament: ColonyTemperament = ColonyTemperament.CALM
    population: ColonyPopulation = ColonyPopulation.MEDIUM
    estimated_frames_covered: Optional[int] = None

    # Health observations
    health_status: HealthStatus = HealthStatus.HEALTHY
    varroa_mites_detected: bool = False
    pests_notes: str = ""
    disease_signs: str = ""

    # Resources
    honey_stores: ResourceLevel = ResourceLevel.ADEQUATE
    pollen_stores: ResourceLevel = ResourceLevel.ADEQUATE
    capped_honey: bool = False

    # Actions taken
    actions_taken: str = ""
    feeding_done: bool = False
    feeding_notes: str = ""
    treatment_applied: bool = False
    treatment_notes: str = ""

    # Media and notes
    photos: list[str] = Field(default_factory=list)
    notes: str = ""
    next_inspection_date: Optional[datetime] = None

    class Settings:
        name = "inspections"
        indexes = [
            "hive_id",
            "user_id",
            [("hive_id", 1), ("inspection_date", -1)],
            [("user_id", 1), ("inspection_date", -1)],
        ]
