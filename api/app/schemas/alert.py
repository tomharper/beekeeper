from pydantic import BaseModel, ConfigDict
from datetime import datetime
from typing import Optional, List
from app.models import AlertType, AlertSeverity


class AlertBase(BaseModel):
    type: AlertType
    title: str
    message: str
    severity: AlertSeverity


class AlertCreate(AlertBase):
    timestamp: datetime
    hive_ids: Optional[List[str]] = None


class AlertUpdate(BaseModel):
    dismissed: Optional[bool] = None


class AlertResponse(AlertBase):
    id: str
    timestamp: datetime
    hive_ids: Optional[List[str]] = None
    dismissed: bool

    model_config = ConfigDict(from_attributes=True)
