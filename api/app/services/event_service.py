from typing import List
from datetime import datetime
from fastapi import HTTPException, status

from app.models import Event
from app.repositories.event_repository import EventRepository
from app.services.follow_service import FollowService
from app.services.notification_service import NotificationService
from app.schemas.event import EventCreate, EventUpdate, EventResponse


class EventService:
    def __init__(self):
        self.repository = EventRepository()
        self.follows = FollowService()
        self.notifications = NotificationService()

    async def get_user_events(self, user_id: str) -> List[EventResponse]:
        events = await self.repository.get_by_user_id(user_id)
        return [EventResponse.model_validate(e) for e in events]

    async def get_event(self, event_id: str, user_id: str) -> EventResponse:
        event = await self.repository.get_by_id(event_id)
        if not event:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND, detail="Event not found"
            )
        if event.user_id != user_id:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Not authorized to access this event",
            )
        return EventResponse.model_validate(event)

    async def get_calendar(
        self, user_id: str, start: datetime, end: datetime
    ) -> List[EventResponse]:
        """Own + followed users' PUBLIC events within [start, end]."""
        user_ids = await self.follows.get_following_ids(user_id)
        user_ids.append(user_id)
        events = await self.repository.get_calendar(user_ids, start, end)
        return [EventResponse.model_validate(e) for e in events]

    async def create_event(
        self, event_data: EventCreate, event_id: str, user_id: str
    ) -> EventResponse:
        event = Event(id=event_id, user_id=user_id, **event_data.model_dump())
        created = await self.repository.create(event)

        # Best-effort fan-out to followers; never fail the create on notification error.
        if created.is_public:
            try:
                await self.notifications.create_for_followers(
                    actor_id=user_id,
                    type="event",
                    title=f"New event: {created.title}",
                    ref_type="event",
                    ref_id=created.id,
                )
            except Exception:
                pass

        return EventResponse.model_validate(created)

    async def update_event(
        self, event_id: str, event_data: EventUpdate, user_id: str
    ) -> EventResponse:
        event = await self.repository.get_by_id(event_id)
        if not event:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND, detail="Event not found"
            )
        if event.user_id != user_id:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Not authorized to modify this event",
            )

        update_data = event_data.model_dump(exclude_unset=True)
        for key, value in update_data.items():
            setattr(event, key, value)

        updated = await self.repository.update(event)
        return EventResponse.model_validate(updated)

    async def delete_event(self, event_id: str, user_id: str) -> None:
        event = await self.repository.get_by_id(event_id)
        if not event:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND, detail="Event not found"
            )
        if event.user_id != user_id:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Not authorized to delete this event",
            )
        await self.repository.delete(event)
