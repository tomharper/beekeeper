from collections import Counter
from typing import List
from fastapi import HTTPException, status

from app.models import Apiary, Hive
from app.repositories import ApiaryRepository
from app.schemas import ApiaryCreate, ApiaryUpdate, ApiaryResponse


def _to_response(apiary: Apiary, hive_count: int) -> ApiaryResponse:
    return ApiaryResponse.model_validate(
        {**apiary.model_dump(), "hive_count": hive_count}
    )


class ApiaryService:
    def __init__(self):
        self.repository = ApiaryRepository()

    async def get_all_apiaries(self) -> List[ApiaryResponse]:
        apiaries = await self.repository.get_all()
        # One query for all hives; group by apiary_id locally.
        hives = await Hive.find_all().to_list()
        counts = Counter(h.apiary_id for h in hives)
        return [_to_response(a, counts.get(a.id, 0)) for a in apiaries]

    async def get_apiary(self, apiary_id: str) -> ApiaryResponse:
        apiary = await self.repository.get_by_id(apiary_id)
        if not apiary:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND, detail="Apiary not found"
            )
        count = await Hive.find(Hive.apiary_id == apiary_id).count()
        return _to_response(apiary, count)

    async def create_apiary(self, apiary_data: ApiaryCreate, apiary_id: str) -> ApiaryResponse:
        apiary = Apiary(id=apiary_id, **apiary_data.model_dump())
        created_apiary = await self.repository.create(apiary)
        return _to_response(created_apiary, 0)

    async def update_apiary(self, apiary_id: str, apiary_data: ApiaryUpdate) -> ApiaryResponse:
        apiary = await self.repository.get_by_id(apiary_id)
        if not apiary:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND, detail="Apiary not found"
            )

        update_data = apiary_data.model_dump(exclude_unset=True)
        for key, value in update_data.items():
            setattr(apiary, key, value)

        updated_apiary = await self.repository.update(apiary)
        count = await Hive.find(Hive.apiary_id == apiary_id).count()
        return _to_response(updated_apiary, count)

    async def delete_apiary(self, apiary_id: str) -> None:
        apiary = await self.repository.get_by_id(apiary_id)
        if not apiary:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND, detail="Apiary not found"
            )
        await self.repository.delete(apiary)
