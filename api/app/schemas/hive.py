from pydantic import BaseModel, ConfigDict
from datetime import datetime
from typing import Optional
from app.models import (
    HiveStatus,
    ColonyStrength,
    QueenStatus,
    Temperament,
    HoneyStores,
)


def to_camel(string: str) -> str:
    """Convert snake_case to camelCase"""
    words = string.split('_')
    return words[0] + ''.join(word.capitalize() for word in words[1:])


class HiveBase(BaseModel):
    name: str
    apiary_id: str

    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)


class HiveCreate(HiveBase):
    last_inspected: datetime
    image_url: Optional[str] = None
    colony_strength: ColonyStrength = ColonyStrength.MODERATE
    queen_status: QueenStatus = QueenStatus.UNKNOWN
    temperament: Temperament = Temperament.MODERATE
    honey_stores: HoneyStores = HoneyStores.ADEQUATE


class HiveUpdate(BaseModel):
    name: Optional[str] = None
    status: Optional[HiveStatus] = None
    last_inspected: Optional[datetime] = None
    image_url: Optional[str] = None
    colony_strength: Optional[ColonyStrength] = None
    queen_status: Optional[QueenStatus] = None
    temperament: Optional[Temperament] = None
    honey_stores: Optional[HoneyStores] = None

    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)


class HiveResponse(HiveBase):
    id: str
    status: HiveStatus
    last_inspected: datetime
    image_url: Optional[str] = None
    colony_strength: ColonyStrength
    queen_status: QueenStatus
    temperament: Temperament
    honey_stores: HoneyStores

    model_config = ConfigDict(from_attributes=True, alias_generator=to_camel, populate_by_name=True)
