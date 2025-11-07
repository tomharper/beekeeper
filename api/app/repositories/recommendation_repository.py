from sqlalchemy.orm import Session
from typing import List, Optional
from app.models import Recommendation


class RecommendationRepository:
    def __init__(self, db: Session):
        self.db = db

    def get_all(self) -> List[Recommendation]:
        return self.db.query(Recommendation).all()

    def get_by_hive_id(self, hive_id: str) -> List[Recommendation]:
        return (
            self.db.query(Recommendation)
            .filter(Recommendation.hive_id == hive_id)
            .all()
        )

    def get_by_id(self, recommendation_id: str) -> Optional[Recommendation]:
        return (
            self.db.query(Recommendation)
            .filter(Recommendation.id == recommendation_id)
            .first()
        )

    def create(self, recommendation: Recommendation) -> Recommendation:
        self.db.add(recommendation)
        self.db.commit()
        self.db.refresh(recommendation)
        return recommendation

    def update(self, recommendation: Recommendation) -> Recommendation:
        self.db.commit()
        self.db.refresh(recommendation)
        return recommendation

    def delete(self, recommendation: Recommendation) -> None:
        self.db.delete(recommendation)
        self.db.commit()
