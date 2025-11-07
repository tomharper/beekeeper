from sqlalchemy.orm import Session
from sqlalchemy import and_, desc
from typing import List, Optional
from app.models import Inspection


class InspectionRepository:
    def __init__(self, db: Session):
        self.db = db

    def get_all(self) -> List[Inspection]:
        return self.db.query(Inspection).order_by(desc(Inspection.inspection_date)).all()

    def get_by_id(self, inspection_id: str) -> Optional[Inspection]:
        return self.db.query(Inspection).filter(Inspection.id == inspection_id).first()

    def get_by_user_id(self, user_id: str) -> List[Inspection]:
        """Get all inspections for a specific user"""
        return (
            self.db.query(Inspection)
            .filter(Inspection.user_id == user_id)
            .order_by(desc(Inspection.inspection_date))
            .all()
        )

    def get_by_hive_id(self, hive_id: str) -> List[Inspection]:
        """Get all inspections for a specific hive"""
        return (
            self.db.query(Inspection)
            .filter(Inspection.hive_id == hive_id)
            .order_by(desc(Inspection.inspection_date))
            .all()
        )

    def get_by_hive_and_user(self, hive_id: str, user_id: str) -> List[Inspection]:
        """Get all inspections for a specific hive filtered by user"""
        return (
            self.db.query(Inspection)
            .filter(
                and_(
                    Inspection.hive_id == hive_id,
                    Inspection.user_id == user_id
                )
            )
            .order_by(desc(Inspection.inspection_date))
            .all()
        )

    def get_latest_for_hive(self, hive_id: str) -> Optional[Inspection]:
        """Get the most recent inspection for a hive"""
        return (
            self.db.query(Inspection)
            .filter(Inspection.hive_id == hive_id)
            .order_by(desc(Inspection.inspection_date))
            .first()
        )

    def get_recent(self, user_id: str, limit: int = 10) -> List[Inspection]:
        """Get the most recent inspections for a user"""
        return (
            self.db.query(Inspection)
            .filter(Inspection.user_id == user_id)
            .order_by(desc(Inspection.inspection_date))
            .limit(limit)
            .all()
        )

    def create(self, inspection: Inspection) -> Inspection:
        self.db.add(inspection)
        self.db.commit()
        self.db.refresh(inspection)
        return inspection

    def update(self, inspection: Inspection) -> Inspection:
        self.db.commit()
        self.db.refresh(inspection)
        return inspection

    def delete(self, inspection: Inspection) -> None:
        self.db.delete(inspection)
        self.db.commit()
