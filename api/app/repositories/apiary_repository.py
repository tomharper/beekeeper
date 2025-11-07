from sqlalchemy.orm import Session
from typing import List, Optional
from app.models import Apiary


class ApiaryRepository:
    def __init__(self, db: Session):
        self.db = db

    def get_all(self) -> List[Apiary]:
        return self.db.query(Apiary).all()

    def get_by_id(self, apiary_id: str) -> Optional[Apiary]:
        return self.db.query(Apiary).filter(Apiary.id == apiary_id).first()

    def create(self, apiary: Apiary) -> Apiary:
        self.db.add(apiary)
        self.db.commit()
        self.db.refresh(apiary)
        return apiary

    def update(self, apiary: Apiary) -> Apiary:
        self.db.commit()
        self.db.refresh(apiary)
        return apiary

    def delete(self, apiary: Apiary) -> None:
        self.db.delete(apiary)
        self.db.commit()
