from pydantic import BaseModel, ConfigDict
from datetime import datetime
from typing import Optional
from app.models import (
    QueenCellStatus,
    BroodPattern,
    ColonyTemperament,
    ColonyPopulation,
    HealthStatus,
    ResourceLevel,
)


def to_camel(string: str) -> str:
    """Convert snake_case to camelCase"""
    words = string.split('_')
    return words[0] + ''.join(word.capitalize() for word in words[1:])


class InspectionBase(BaseModel):
    hive_id: str
    inspection_date: datetime
    duration_minutes: Optional[int] = None

    # Weather
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
    photos: str = ""  # JSON array string or comma-separated URLs
    notes: str = ""
    next_inspection_date: Optional[datetime] = None

    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)


class InspectionCreate(InspectionBase):
    """Schema for creating a new inspection"""
    pass


class InspectionUpdate(BaseModel):
    """Schema for updating an inspection"""
    inspection_date: Optional[datetime] = None
    duration_minutes: Optional[int] = None
    weather_temp: Optional[float] = None
    weather_conditions: Optional[str] = None
    queen_seen: Optional[bool] = None
    queen_marked: Optional[bool] = None
    queen_cells: Optional[QueenCellStatus] = None
    brood_pattern: Optional[BroodPattern] = None
    eggs_seen: Optional[bool] = None
    larvae_seen: Optional[bool] = None
    capped_brood_seen: Optional[bool] = None
    estimated_brood_frames: Optional[int] = None
    temperament: Optional[ColonyTemperament] = None
    population: Optional[ColonyPopulation] = None
    estimated_frames_covered: Optional[int] = None
    health_status: Optional[HealthStatus] = None
    varroa_mites_detected: Optional[bool] = None
    pests_notes: Optional[str] = None
    disease_signs: Optional[str] = None
    honey_stores: Optional[ResourceLevel] = None
    pollen_stores: Optional[ResourceLevel] = None
    capped_honey: Optional[bool] = None
    actions_taken: Optional[str] = None
    feeding_done: Optional[bool] = None
    feeding_notes: Optional[str] = None
    treatment_applied: Optional[bool] = None
    treatment_notes: Optional[str] = None
    photos: Optional[str] = None
    notes: Optional[str] = None
    next_inspection_date: Optional[datetime] = None

    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)


class InspectionResponse(InspectionBase):
    """Schema for inspection responses"""
    id: str
    user_id: str
    created_at: datetime
    updated_at: datetime

    model_config = ConfigDict(from_attributes=True, alias_generator=to_camel, populate_by_name=True)
