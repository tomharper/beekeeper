from typing import List, Optional
from app.models import Alert


class AlertRepository:
    async def get_all(self) -> List[Alert]:
        return await Alert.find_all().to_list()

    async def get_active(self) -> List[Alert]:
        return await Alert.find(Alert.dismissed == False).to_list()  # noqa: E712

    async def get_by_id(self, alert_id: str) -> Optional[Alert]:
        return await Alert.get(alert_id)

    async def create(self, alert: Alert) -> Alert:
        await alert.insert()
        return alert

    async def update(self, alert: Alert) -> Alert:
        await alert.save()
        return alert

    async def delete(self, alert: Alert) -> None:
        await alert.delete()
