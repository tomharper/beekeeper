from pydantic import BaseModel, ConfigDict
from typing import Optional
from app.models import ApiaryStatus


class ApiaryBase(BaseModel):
    name: str
    location: str
    latitude: Optional[float] = None
    longitude: Optional[float] = None


class ApiaryCreate(ApiaryBase):
    pass


class ApiaryUpdate(BaseModel):
    name: Optional[str] = None
    location: Optional[str] = None
    latitude: Optional[float] = None
    longitude: Optional[float] = None
    status: Optional[ApiaryStatus] = None


class ApiaryResponse(ApiaryBase):
    id: str
    status: ApiaryStatus
    hive_count: int

    model_config = ConfigDict(from_attributes=True)
