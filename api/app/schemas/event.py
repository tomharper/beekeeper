from datetime import datetime
from typing import Optional
from pydantic import BaseModel, ConfigDict


def to_camel(string: str) -> str:
    """Convert snake_case to camelCase"""
    words = string.split('_')
    return words[0] + ''.join(word.capitalize() for word in words[1:])


class EventBase(BaseModel):
    title: str
    description: str = ""
    event_date: datetime
    end_date: Optional[datetime] = None
    location: Optional[str] = None
    all_day: bool = False
    is_public: bool = True

    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)


class EventCreate(EventBase):
    pass


class EventUpdate(BaseModel):
    title: Optional[str] = None
    description: Optional[str] = None
    event_date: Optional[datetime] = None
    end_date: Optional[datetime] = None
    location: Optional[str] = None
    all_day: Optional[bool] = None
    is_public: Optional[bool] = None

    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)


class EventResponse(EventBase):
    id: str
    user_id: str

    model_config = ConfigDict(
        from_attributes=True, alias_generator=to_camel, populate_by_name=True
    )
