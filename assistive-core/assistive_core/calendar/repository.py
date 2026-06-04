"""Event repository. Ported from beekeeper api/app/repositories/event_repository.py."""
from datetime import datetime
from typing import List, Optional

from beanie.operators import In

from .models import Event


class EventRepository:
    async def get_by_id(self, event_id: str) -> Optional[Event]:
        return await Event.get(event_id)

    async def get_by_user_id(self, user_id: str) -> List[Event]:
        return (
            await Event.find(Event.user_id == user_id)
            .sort(+Event.event_date)
            .to_list()
        )

    async def get_calendar(
        self, user_ids: List[str], start: datetime, end: datetime
    ) -> List[Event]:
        """Public events from the given users within [start, end], earliest first."""
        return (
            await Event.find(
                In(Event.user_id, user_ids),
                Event.is_public == True,  # noqa: E712
                Event.event_date >= start,
                Event.event_date <= end,
            )
            .sort(+Event.event_date)
            .to_list()
        )

    async def create(self, event: Event) -> Event:
        await event.insert()
        return event

    async def update(self, event: Event) -> Event:
        await event.save()
        return event

    async def delete(self, event: Event) -> None:
        await event.delete()
