from fastapi import APIRouter, Depends, status
from sqlalchemy.orm import Session
from typing import List

from app.database import get_db
from app.services import AlertService
from app.schemas import AlertCreate, AlertUpdate, AlertResponse

router = APIRouter(prefix="/alerts", tags=["alerts"])


@router.get("", response_model=List[AlertResponse])
def get_alerts(db: Session = Depends(get_db)):
    """Get all alerts"""
    service = AlertService(db)
    return service.get_all_alerts()


@router.get("/active", response_model=List[AlertResponse])
def get_active_alerts(db: Session = Depends(get_db)):
    """Get all active (non-dismissed) alerts"""
    service = AlertService(db)
    return service.get_active_alerts()


@router.get("/{alert_id}", response_model=AlertResponse)
def get_alert(alert_id: str, db: Session = Depends(get_db)):
    """Get a specific alert by ID"""
    service = AlertService(db)
    return service.get_alert(alert_id)


@router.post("", response_model=AlertResponse, status_code=status.HTTP_201_CREATED)
def create_alert(alert: AlertCreate, db: Session = Depends(get_db)):
    """Create a new alert"""
    import uuid

    alert_id = str(uuid.uuid4())
    service = AlertService(db)
    return service.create_alert(alert, alert_id)


@router.patch("/{alert_id}", response_model=AlertResponse)
def update_alert(alert_id: str, alert: AlertUpdate, db: Session = Depends(get_db)):
    """Update an alert (e.g., dismiss it)"""
    service = AlertService(db)
    return service.update_alert(alert_id, alert)
