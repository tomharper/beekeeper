from fastapi import APIRouter, status
from typing import List

from app.services import AlertService
from app.schemas import AlertCreate, AlertUpdate, AlertResponse

router = APIRouter(prefix="/alerts", tags=["alerts"])


@router.get("", response_model=List[AlertResponse])
async def get_alerts():
    """Get all alerts"""
    service = AlertService()
    return await service.get_all_alerts()


@router.get("/active", response_model=List[AlertResponse])
async def get_active_alerts():
    """Get all active (non-dismissed) alerts"""
    service = AlertService()
    return await service.get_active_alerts()


@router.get("/{alert_id}", response_model=AlertResponse)
async def get_alert(alert_id: str):
    """Get a specific alert by ID"""
    service = AlertService()
    return await service.get_alert(alert_id)


@router.post("", response_model=AlertResponse, status_code=status.HTTP_201_CREATED)
async def create_alert(alert: AlertCreate):
    """Create a new alert"""
    import uuid

    alert_id = str(uuid.uuid4())
    service = AlertService()
    return await service.create_alert(alert, alert_id)


@router.patch("/{alert_id}", response_model=AlertResponse)
async def update_alert(alert_id: str, alert: AlertUpdate):
    """Update an alert (e.g., dismiss it)"""
    service = AlertService()
    return await service.update_alert(alert_id, alert)
