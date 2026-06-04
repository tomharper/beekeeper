"""Calendar subpackage.

OWNED FILES (this agent created the foundational document):
  - models.py -> COMPLETE. Event document (per-vertical DB).

TO BE CREATED BY THE CALENDAR MODULE AGENT:
  - schemas.py    -> EventBase, EventCreate, EventUpdate, EventResponse
                     (port beekeeper schemas/event.py, camelCase aliasing).
  - repository.py -> class EventRepository: get_by_id, get_by_user_id,
                     get_calendar(user_ids, start, end), create, update, delete
                     (port beekeeper repositories/event_repository.py).
  - service.py    -> class EventService with:
        async def get_user_events(self, user_id) -> list[EventResponse]
        async def get_event(self, event_id, user_id) -> EventResponse
        async def get_calendar(self, user_id, start, end) -> list[EventResponse]
        async def create_event(self, event_data, event_id, user_id) -> EventResponse
        async def update_event(self, event_id, event_data, user_id) -> EventResponse
        async def delete_event(self, event_id, user_id) -> None
      (port beekeeper services/event_service.py; uses follow_service for the
       calendar fan-in and notification_service for public-create fan-out).
  - router.py     -> APIRouter(prefix="/events"): GET "", GET "/calendar",
        GET "/{id}", POST "", PUT "/{id}", DELETE "/{id}".
        Export as `event_router`.

This __init__ must continue to export: Event, EventCreate, EventUpdate,
EventResponse, EventRepository, EventService, event_service, event_router.
"""
from .models import Event

try:
    from .schemas import EventCreate, EventResponse, EventUpdate  # type: ignore
except Exception:  # pragma: no cover
    EventCreate = EventUpdate = EventResponse = None  # type: ignore

try:
    from .repository import EventRepository  # type: ignore
except Exception:  # pragma: no cover
    EventRepository = None  # type: ignore

try:
    from .service import EventService  # type: ignore

    event_service = EventService()
except Exception:  # pragma: no cover
    EventService = None  # type: ignore
    event_service = None

try:
    from .router import router as event_router  # type: ignore
except Exception:  # pragma: no cover
    event_router = None

__all__ = [
    "Event",
    "EventCreate",
    "EventUpdate",
    "EventResponse",
    "EventRepository",
    "EventService",
    "event_service",
    "event_router",
]
