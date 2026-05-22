from typing import List
from fastapi import HTTPException, status

from app.models import Recommendation
from app.repositories import RecommendationRepository
from app.schemas import RecommendationCreate, RecommendationUpdate, RecommendationResponse


class RecommendationService:
    def __init__(self):
        self.repository = RecommendationRepository()

    async def get_recommendations_by_hive(self, hive_id: str) -> List[RecommendationResponse]:
        recommendations = await self.repository.get_by_hive_id(hive_id)
        return [RecommendationResponse.model_validate(rec) for rec in recommendations]

    async def create_recommendation(
        self, recommendation_data: RecommendationCreate, recommendation_id: str
    ) -> RecommendationResponse:
        recommendation = Recommendation(
            id=recommendation_id, **recommendation_data.model_dump()
        )
        created_recommendation = await self.repository.create(recommendation)
        return RecommendationResponse.model_validate(created_recommendation)

    async def update_recommendation(
        self, recommendation_id: str, recommendation_data: RecommendationUpdate
    ) -> RecommendationResponse:
        recommendation = await self.repository.get_by_id(recommendation_id)
        if not recommendation:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Recommendation not found",
            )

        update_data = recommendation_data.model_dump(exclude_unset=True)
        for key, value in update_data.items():
            setattr(recommendation, key, value)

        updated_recommendation = await self.repository.update(recommendation)
        return RecommendationResponse.model_validate(updated_recommendation)

    async def delete_recommendation(self, recommendation_id: str) -> None:
        recommendation = await self.repository.get_by_id(recommendation_id)
        if not recommendation:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Recommendation not found",
            )
        await self.repository.delete(recommendation)
