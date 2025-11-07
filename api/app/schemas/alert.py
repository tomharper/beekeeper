from pydantic import BaseModel, ConfigDict
from datetime import datetime
from typing import Optional, List
from app.models import AlertType, AlertSeverity


def to_camel(string: str) -> str:
    """Convert snake_case to camelCase"""
    words = string.split('_')
    return words[0] + ''.join(word.capitalize() for word in words[1:])


class AlertBase(BaseModel):
    type: AlertType
    title: str
    message: str
    severity: AlertSeverity

    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)


class AlertCreate(AlertBase):
    timestamp: datetime
    hive_ids: Optional[List[str]] = None


class AlertUpdate(BaseModel):
    dismissed: Optional[bool] = None

    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)


class AlertResponse(AlertBase):
    id: str
    timestamp: datetime
    hive_ids: Optional[List[str]] = None
    dismissed: bool

    model_config = ConfigDict(from_attributes=True, alias_generator=to_camel, populate_by_name=True)
