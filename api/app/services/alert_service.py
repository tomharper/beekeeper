from sqlalchemy.orm import Session
from typing import List
from fastapi import HTTPException, status

from app.models import Alert
from app.repositories import AlertRepository
from app.schemas import AlertCreate, AlertUpdate, AlertResponse


class AlertService:
    def __init__(self, db: Session):
        self.repository = AlertRepository(db)

    def get_all_alerts(self) -> List[AlertResponse]:
        alerts = self.repository.get_all()
        return [self._to_response(alert) for alert in alerts]

    def get_active_alerts(self) -> List[AlertResponse]:
        alerts = self.repository.get_active()
        return [self._to_response(alert) for alert in alerts]

    def get_alert(self, alert_id: str) -> AlertResponse:
        alert = self.repository.get_by_id(alert_id)
        if not alert:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND, detail="Alert not found"
            )
        return self._to_response(alert)

    def create_alert(self, alert_data: AlertCreate, alert_id: str) -> AlertResponse:
        hive_ids_str = (
            ",".join(alert_data.hive_ids) if alert_data.hive_ids else None
        )
        alert = Alert(
            id=alert_id,
            type=alert_data.type,
            title=alert_data.title,
            message=alert_data.message,
            severity=alert_data.severity,
            timestamp=alert_data.timestamp,
            hive_ids=hive_ids_str,
            dismissed=False,
        )
        created_alert = self.repository.create(alert)
        return self._to_response(created_alert)

    def update_alert(self, alert_id: str, alert_data: AlertUpdate) -> AlertResponse:
        alert = self.repository.get_by_id(alert_id)
        if not alert:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND, detail="Alert not found"
            )

        update_data = alert_data.model_dump(exclude_unset=True)
        for key, value in update_data.items():
            setattr(alert, key, value)

        updated_alert = self.repository.update(alert)
        return self._to_response(updated_alert)

    def _to_response(self, alert: Alert) -> AlertResponse:
        hive_ids = alert.hive_ids.split(",") if alert.hive_ids else None
        return AlertResponse(
            id=alert.id,
            type=alert.type,
            title=alert.title,
            message=alert.message,
            severity=alert.severity,
            timestamp=alert.timestamp,
            hive_ids=hive_ids,
            dismissed=alert.dismissed,
        )
