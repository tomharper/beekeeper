from sqlalchemy.orm import Session
from typing import List, Optional
from app.models import Hive


class HiveRepository:
    def __init__(self, db: Session):
        self.db = db

    def get_all(self) -> List[Hive]:
        return self.db.query(Hive).all()

    def get_by_id(self, hive_id: str) -> Optional[Hive]:
        return self.db.query(Hive).filter(Hive.id == hive_id).first()

    def get_by_apiary_id(self, apiary_id: str) -> List[Hive]:
        return self.db.query(Hive).filter(Hive.apiary_id == apiary_id).all()

    def create(self, hive: Hive) -> Hive:
        self.db.add(hive)
        self.db.commit()
        self.db.refresh(hive)
        return hive

    def update(self, hive: Hive) -> Hive:
        self.db.commit()
        self.db.refresh(hive)
        return hive

    def delete(self, hive: Hive) -> None:
        self.db.delete(hive)
        self.db.commit()
