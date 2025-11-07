from pydantic import BaseModel, ConfigDict
from app.models import RecommendationType, Priority


def to_camel(string: str) -> str:
    """Convert snake_case to camelCase"""
    words = string.split('_')
    return words[0] + ''.join(word.capitalize() for word in words[1:])


class RecommendationBase(BaseModel):
    hive_id: str
    type: RecommendationType
    title: str
    description: str
    priority: Priority

    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)


class RecommendationCreate(RecommendationBase):
    pass


class RecommendationUpdate(BaseModel):
    type: RecommendationType | None = None
    title: str | None = None
    description: str | None = None
    priority: Priority | None = None

    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)


class RecommendationResponse(RecommendationBase):
    id: str

    model_config = ConfigDict(from_attributes=True, alias_generator=to_camel, populate_by_name=True)
