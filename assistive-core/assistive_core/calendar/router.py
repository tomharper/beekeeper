"""Event router (/events/*). Ported from beekeeper api/app/routers/event.py.

NOTE: GET /calendar is declared BEFORE GET /{event_id} so the static path is
not shadowed by the path parameter.
"""
import uuid
from datetime import datetime
from typing import List

from fastapi import APIRouter, Depends, Query, status

from ..auth.deps import get_current_user
from ..auth.models import User
from .schemas import EventCreate, EventResponse, EventUpdate
from .service import EventService

router = APIRouter(prefix="/events", tags=["events"])


@router.get("", response_model=List[EventResponse])
async def get_events(current_user: User = Depends(get_current_user)):
    """List the current user's own events."""
    return await EventService().get_user_events(current_user.id)


@router.get("/calendar", response_model=List[EventResponse])
async def get_calendar(
    start: datetime = Query(..., description="Range start (inclusive)"),
    end: datetime = Query(..., description="Range end (inclusive)"),
    current_user: User = Depends(get_current_user),
):
    """Own + followed users' public events within [start, end]."""
    return await EventService().get_calendar(current_user.id, start, end)


@router.get("/{event_id}", response_model=EventResponse)
async def get_event(event_id: str, current_user: User = Depends(get_current_user)):
    """Get one of the current user's events by ID."""
    return await EventService().get_event(event_id, current_user.id)


@router.post("", response_model=EventResponse, status_code=status.HTTP_201_CREATED)
async def create_event(
    event: EventCreate, current_user: User = Depends(get_current_user)
):
    """Create a new calendar event."""
    event_id = str(uuid.uuid4())
    return await EventService().create_event(event, event_id, current_user.id)


@router.put("/{event_id}", response_model=EventResponse)
async def update_event(
    event_id: str,
    event: EventUpdate,
    current_user: User = Depends(get_current_user),
):
    """Update an existing event."""
    return await EventService().update_event(event_id, event, current_user.id)


@router.delete("/{event_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_event(event_id: str, current_user: User = Depends(get_current_user)):
    """Delete an event."""
    await EventService().delete_event(event_id, current_user.id)
