from typing import List, Optional
from app.models import Apiary


class ApiaryRepository:
    async def get_all(self) -> List[Apiary]:
        return await Apiary.find_all().to_list()

    async def get_by_id(self, apiary_id: str) -> Optional[Apiary]:
        return await Apiary.get(apiary_id)

    async def create(self, apiary: Apiary) -> Apiary:
        await apiary.insert()
        return apiary

    async def update(self, apiary: Apiary) -> Apiary:
        await apiary.save()
        return apiary

    async def delete(self, apiary: Apiary) -> None:
        await apiary.delete()
