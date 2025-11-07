from pydantic import BaseModel, ConfigDict
from app.models import RecommendationType, Priority


class RecommendationBase(BaseModel):
    hive_id: str
    type: RecommendationType
    title: str
    description: str
    priority: Priority


class RecommendationCreate(RecommendationBase):
    pass


class RecommendationUpdate(BaseModel):
    type: RecommendationType | None = None
    title: str | None = None
    description: str | None = None
    priority: Priority | None = None


class RecommendationResponse(RecommendationBase):
    id: str

    model_config = ConfigDict(from_attributes=True)
