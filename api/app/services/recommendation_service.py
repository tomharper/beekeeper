from sqlalchemy.orm import Session
from typing import List
from fastapi import HTTPException, status

from app.models import Recommendation
from app.repositories import RecommendationRepository
from app.schemas import RecommendationCreate, RecommendationUpdate, RecommendationResponse


class RecommendationService:
    def __init__(self, db: Session):
        self.repository = RecommendationRepository(db)

    def get_recommendations_by_hive(self, hive_id: str) -> List[RecommendationResponse]:
        recommendations = self.repository.get_by_hive_id(hive_id)
        return [
            RecommendationResponse.model_validate(rec) for rec in recommendations
        ]

    def create_recommendation(
        self, recommendation_data: RecommendationCreate, recommendation_id: str
    ) -> RecommendationResponse:
        recommendation = Recommendation(
            id=recommendation_id, **recommendation_data.model_dump()
        )
        created_recommendation = self.repository.create(recommendation)
        return RecommendationResponse.model_validate(created_recommendation)

    def update_recommendation(
        self, recommendation_id: str, recommendation_data: RecommendationUpdate
    ) -> RecommendationResponse:
        recommendation = self.repository.get_by_id(recommendation_id)
        if not recommendation:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Recommendation not found",
            )

        update_data = recommendation_data.model_dump(exclude_unset=True)
        for key, value in update_data.items():
            setattr(recommendation, key, value)

        updated_recommendation = self.repository.update(recommendation)
        return RecommendationResponse.model_validate(updated_recommendation)

    def delete_recommendation(self, recommendation_id: str) -> None:
        recommendation = self.repository.get_by_id(recommendation_id)
        if not recommendation:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Recommendation not found",
            )
        self.repository.delete(recommendation)
