from fastapi import APIRouter, Depends, status, Query
from sqlalchemy.orm import Session
from typing import List

from app.database import get_db
from app.services import RecommendationService
from app.schemas import (
    RecommendationCreate,
    RecommendationUpdate,
    RecommendationResponse,
)

router = APIRouter(prefix="/recommendations", tags=["recommendations"])


@router.get("", response_model=List[RecommendationResponse])
def get_recommendations(hive_id: str = Query(...), db: Session = Depends(get_db)):
    """Get all recommendations for a specific hive"""
    service = RecommendationService(db)
    return service.get_recommendations_by_hive(hive_id)


@router.post(
    "", response_model=RecommendationResponse, status_code=status.HTTP_201_CREATED
)
def create_recommendation(
    recommendation: RecommendationCreate, db: Session = Depends(get_db)
):
    """Create a new recommendation"""
    import uuid

    recommendation_id = str(uuid.uuid4())
    service = RecommendationService(db)
    return service.create_recommendation(recommendation, recommendation_id)


@router.put("/{recommendation_id}", response_model=RecommendationResponse)
def update_recommendation(
    recommendation_id: str,
    recommendation: RecommendationUpdate,
    db: Session = Depends(get_db),
):
    """Update a recommendation"""
    service = RecommendationService(db)
    return service.update_recommendation(recommendation_id, recommendation)


@router.delete("/{recommendation_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_recommendation(recommendation_id: str, db: Session = Depends(get_db)):
    """Delete a recommendation"""
    service = RecommendationService(db)
    service.delete_recommendation(recommendation_id)
