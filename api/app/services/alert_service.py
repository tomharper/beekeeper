from typing import List
from fastapi import HTTPException, status

from app.models import Alert
from app.repositories import AlertRepository
from app.schemas import AlertCreate, AlertUpdate, AlertResponse


class AlertService:
    def __init__(self):
        self.repository = AlertRepository()

    async def get_all_alerts(self) -> List[AlertResponse]:
        alerts = await self.repository.get_all()
        return [AlertResponse.model_validate(alert) for alert in alerts]

    async def get_active_alerts(self) -> List[AlertResponse]:
        alerts = await self.repository.get_active()
        return [AlertResponse.model_validate(alert) for alert in alerts]

    async def get_alert(self, alert_id: str) -> AlertResponse:
        alert = await self.repository.get_by_id(alert_id)
        if not alert:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND, detail="Alert not found"
            )
        return AlertResponse.model_validate(alert)

    async def create_alert(self, alert_data: AlertCreate, alert_id: str) -> AlertResponse:
        alert = Alert(
            id=alert_id,
            type=alert_data.type,
            title=alert_data.title,
            message=alert_data.message,
            severity=alert_data.severity,
            timestamp=alert_data.timestamp,
            hive_ids=alert_data.hive_ids or [],
            dismissed=False,
        )
        created_alert = await self.repository.create(alert)
        return AlertResponse.model_validate(created_alert)

    async def update_alert(self, alert_id: str, alert_data: AlertUpdate) -> AlertResponse:
        alert = await self.repository.get_by_id(alert_id)
        if not alert:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND, detail="Alert not found"
            )

        update_data = alert_data.model_dump(exclude_unset=True)
        for key, value in update_data.items():
            setattr(alert, key, value)

        updated_alert = await self.repository.update(alert)
        return AlertResponse.model_validate(updated_alert)
