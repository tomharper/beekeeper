from typing import List, Optional
from datetime import datetime
from beanie.operators import In
from app.models import Inspection


class InspectionRepository:
    async def get_all(self) -> List[Inspection]:
        return await Inspection.find_all().sort(-Inspection.inspection_date).to_list()

    async def get_by_id(self, inspection_id: str) -> Optional[Inspection]:
        return await Inspection.get(inspection_id)

    async def get_by_user_id(self, user_id: str) -> List[Inspection]:
        return (
            await Inspection.find(Inspection.user_id == user_id)
            .sort(-Inspection.inspection_date)
            .to_list()
        )

    async def get_by_hive_id(self, hive_id: str) -> List[Inspection]:
        return (
            await Inspection.find(Inspection.hive_id == hive_id)
            .sort(-Inspection.inspection_date)
            .to_list()
        )

    async def get_by_hive_and_user(self, hive_id: str, user_id: str) -> List[Inspection]:
        return (
            await Inspection.find(
                Inspection.hive_id == hive_id,
                Inspection.user_id == user_id,
            )
            .sort(-Inspection.inspection_date)
            .to_list()
        )

    async def get_latest_for_hive(self, hive_id: str) -> Optional[Inspection]:
        return await Inspection.find_one(
            Inspection.hive_id == hive_id,
            sort=[(Inspection.inspection_date, -1)],
        )

    async def get_recent(self, user_id: str, limit: int = 10) -> List[Inspection]:
        return (
            await Inspection.find(Inspection.user_id == user_id)
            .sort(-Inspection.inspection_date)
            .limit(limit)
            .to_list()
        )

    async def get_feed(
        self, user_ids: List[str], limit: int = 20, before: Optional[datetime] = None
    ) -> List[Inspection]:
        """Public inspections from the given users, newest first (for the follow feed)."""
        conditions = [In(Inspection.user_id, user_ids), Inspection.is_public == True]  # noqa: E712
        if before is not None:
            conditions.append(Inspection.inspection_date < before)
        return (
            await Inspection.find(*conditions)
            .sort(-Inspection.inspection_date)
            .limit(limit)
            .to_list()
        )

    async def create(self, inspection: Inspection) -> Inspection:
        await inspection.insert()
        return inspection

    async def update(self, inspection: Inspection) -> Inspection:
        await inspection.save()
        return inspection

    async def delete(self, inspection: Inspection) -> None:
        await inspection.delete()
