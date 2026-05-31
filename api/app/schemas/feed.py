from datetime import datetime
from typing import Optional
from pydantic import BaseModel, ConfigDict

from app.schemas.follow import UserSummary, to_camel
from app.schemas.inspection import InspectionResponse
from app.schemas.task import TaskResponse


class FeedItemResponse(BaseModel):
    """A single activity item from a followed user.

    The record itself is the content (no separate 'post' abstraction).
    `type` discriminates which record kind is carried (inspection or task).
    """
    type: str  # "inspection" | "task"
    author: UserSummary
    occurred_at: datetime
    inspection: Optional[InspectionResponse] = None
    task: Optional[TaskResponse] = None

    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)
