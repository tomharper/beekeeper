from fastapi import APIRouter, status, Query
from typing import List

from app.services import RecommendationService
from app.schemas import (
    RecommendationCreate,
    RecommendationUpdate,
    RecommendationResponse,
)

router = APIRouter(prefix="/recommendations", tags=["recommendations"])


@router.get("", response_model=List[RecommendationResponse])
async def get_recommendations(hive_id: str = Query(...)):
    """Get all recommendations for a specific hive"""
    service = RecommendationService()
    return await service.get_recommendations_by_hive(hive_id)


@router.post(
    "", response_model=RecommendationResponse, status_code=status.HTTP_201_CREATED
)
async def create_recommendation(recommendation: RecommendationCreate):
    """Create a new recommendation"""
    import uuid

    recommendation_id = str(uuid.uuid4())
    service = RecommendationService()
    return await service.create_recommendation(recommendation, recommendation_id)


@router.put("/{recommendation_id}", response_model=RecommendationResponse)
async def update_recommendation(
    recommendation_id: str, recommendation: RecommendationUpdate
):
    """Update a recommendation"""
    service = RecommendationService()
    return await service.update_recommendation(recommendation_id, recommendation)


@router.delete("/{recommendation_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_recommendation(recommendation_id: str):
    """Delete a recommendation"""
    service = RecommendationService()
    await service.delete_recommendation(recommendation_id)
