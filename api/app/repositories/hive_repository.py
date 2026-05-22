from typing import List, Optional
from app.models import Hive


class HiveRepository:
    async def get_all(self) -> List[Hive]:
        return await Hive.find_all().to_list()

    async def get_by_id(self, hive_id: str) -> Optional[Hive]:
        return await Hive.get(hive_id)

    async def get_by_apiary_id(self, apiary_id: str) -> List[Hive]:
        return await Hive.find(Hive.apiary_id == apiary_id).to_list()

    async def create(self, hive: Hive) -> Hive:
        await hive.insert()
        return hive

    async def update(self, hive: Hive) -> Hive:
        await hive.save()
        return hive

    async def delete(self, hive: Hive) -> None:
        await hive.delete()
