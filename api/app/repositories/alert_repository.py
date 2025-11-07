from sqlalchemy.orm import Session
from typing import List, Optional
from app.models import Alert


class AlertRepository:
    def __init__(self, db: Session):
        self.db = db

    def get_all(self) -> List[Alert]:
        return self.db.query(Alert).all()

    def get_active(self) -> List[Alert]:
        return self.db.query(Alert).filter(Alert.dismissed == False).all()

    def get_by_id(self, alert_id: str) -> Optional[Alert]:
        return self.db.query(Alert).filter(Alert.id == alert_id).first()

    def create(self, alert: Alert) -> Alert:
        self.db.add(alert)
        self.db.commit()
        self.db.refresh(alert)
        return alert

    def update(self, alert: Alert) -> Alert:
        self.db.commit()
        self.db.refresh(alert)
        return alert

    def delete(self, alert: Alert) -> None:
        self.db.delete(alert)
        self.db.commit()
