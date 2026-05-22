from typing import List
from fastapi import HTTPException, status

from app.models import Hive, HiveStatus
from app.repositories import HiveRepository
from app.schemas import HiveCreate, HiveUpdate, HiveResponse


class HiveService:
    def __init__(self):
        self.repository = HiveRepository()

    async def get_all_hives(self) -> List[HiveResponse]:
        hives = await self.repository.get_all()
        return [HiveResponse.model_validate(hive) for hive in hives]

    async def get_hive(self, hive_id: str) -> HiveResponse:
        hive = await self.repository.get_by_id(hive_id)
        if not hive:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND, detail="Hive not found"
            )
        return HiveResponse.model_validate(hive)

    async def get_hives_by_apiary(self, apiary_id: str) -> List[HiveResponse]:
        hives = await self.repository.get_by_apiary_id(apiary_id)
        return [HiveResponse.model_validate(hive) for hive in hives]

    async def create_hive(self, hive_data: HiveCreate, hive_id: str) -> HiveResponse:
        hive = Hive(id=hive_id, status=HiveStatus.STRONG, **hive_data.model_dump())
        created_hive = await self.repository.create(hive)
        return HiveResponse.model_validate(created_hive)

    async def update_hive(self, hive_id: str, hive_data: HiveUpdate) -> HiveResponse:
        hive = await self.repository.get_by_id(hive_id)
        if not hive:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND, detail="Hive not found"
            )

        update_data = hive_data.model_dump(exclude_unset=True)
        for key, value in update_data.items():
            setattr(hive, key, value)

        updated_hive = await self.repository.update(hive)
        return HiveResponse.model_validate(updated_hive)

    async def delete_hive(self, hive_id: str) -> None:
        hive = await self.repository.get_by_id(hive_id)
        if not hive:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND, detail="Hive not found"
            )
        await self.repository.delete(hive)
