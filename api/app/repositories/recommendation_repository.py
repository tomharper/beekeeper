from typing import List, Optional
from app.models import Recommendation


class RecommendationRepository:
    async def get_all(self) -> List[Recommendation]:
        return await Recommendation.find_all().to_list()

    async def get_by_hive_id(self, hive_id: str) -> List[Recommendation]:
        return await Recommendation.find(Recommendation.hive_id == hive_id).to_list()

    async def get_by_id(self, recommendation_id: str) -> Optional[Recommendation]:
        return await Recommendation.get(recommendation_id)

    async def create(self, recommendation: Recommendation) -> Recommendation:
        await recommendation.insert()
        return recommendation

    async def update(self, recommendation: Recommendation) -> Recommendation:
        await recommendation.save()
        return recommendation

    async def delete(self, recommendation: Recommendation) -> None:
        await recommendation.delete()
