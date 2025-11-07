from pydantic import BaseModel, ConfigDict, Field
from typing import Optional
from app.models import ApiaryStatus


def to_camel(string: str) -> str:
    """Convert snake_case to camelCase"""
    words = string.split('_')
    return words[0] + ''.join(word.capitalize() for word in words[1:])


class ApiaryBase(BaseModel):
    name: str
    location: str
    latitude: Optional[float] = None
    longitude: Optional[float] = None

    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)


class ApiaryCreate(ApiaryBase):
    pass


class ApiaryUpdate(BaseModel):
    name: Optional[str] = None
    location: Optional[str] = None
    latitude: Optional[float] = None
    longitude: Optional[float] = None
    status: Optional[ApiaryStatus] = None

    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)


class ApiaryResponse(ApiaryBase):
    id: str
    status: ApiaryStatus
    hive_count: int = Field(..., serialization_alias="hiveCount")

    model_config = ConfigDict(from_attributes=True, alias_generator=to_camel, populate_by_name=True)
