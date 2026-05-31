from datetime import datetime
from typing import Optional
from pydantic import BaseModel, ConfigDict

from app.schemas.follow import UserSummary, to_camel
from app.schemas.inspection import InspectionResponse


class FeedItemResponse(BaseModel):
    """A single activity item from a followed user.

    The record itself is the content (no separate 'post' abstraction). For now
    only inspections flow into the feed; `type` discriminates as Task etc. are added.
    """
    type: str  # "inspection"
    author: UserSummary
    occurred_at: datetime
    inspection: Optional[InspectionResponse] = None

    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)
