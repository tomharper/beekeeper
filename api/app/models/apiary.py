from enum import Enum as PyEnum
from typing import Optional

from beanie import Document

from .base import TimestampMixin


class ApiaryStatus(str, PyEnum):
    HEALTHY = "HEALTHY"
    WARNING = "WARNING"
    ALERT = "ALERT"


class Apiary(Document, TimestampMixin):
    id: str  # type: ignore[assignment]
    name: str
    location: str
    latitude: Optional[float] = None
    longitude: Optional[float] = None
    status: ApiaryStatus = ApiaryStatus.HEALTHY

    class Settings:
        name = "apiaries"
