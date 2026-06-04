"""Beekeeper's feed-source registrations for assistive-core.

The shared feed/notification substrate does not hardcode record types; each
vertical declares which of its documents are broadcastable. Beekeeper opts in
two record kinds, mirroring the prior FeedService:

  - inspection -> ordered by ``inspection_date``
  - task       -> ordered by ``due_date``

Each ``to_item`` projection reproduces the payload the old
``schemas.feed.FeedItemResponse`` carried (a full InspectionResponse / TaskResponse,
camelCase-aliased), now nested under the generic ``payload`` field.
"""
from assistive_core import FeedSource

from app.models import Inspection, Task
from app.schemas import InspectionResponse, TaskResponse

INSPECTION_FEED_SOURCE = FeedSource(
    type="inspection",
    document=Inspection,
    occurred_at=lambda doc: doc.inspection_date,
    occurred_at_field="inspection_date",  # DB-side sort/cursor
    to_item=lambda doc: InspectionResponse.model_validate(doc),
    notify=lambda doc: {"title": "New hive inspection", "ref_id": doc.id},
)

TASK_FEED_SOURCE = FeedSource(
    type="task",
    document=Task,
    occurred_at=lambda doc: doc.due_date,
    occurred_at_field="due_date",  # DB-side sort/cursor
    to_item=lambda doc: TaskResponse.model_validate(doc),
    notify=lambda doc: {"title": f"New task: {doc.title}", "ref_id": doc.id},
)

FEED_SOURCES = [INSPECTION_FEED_SOURCE, TASK_FEED_SOURCE]
