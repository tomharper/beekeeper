from fastapi import APIRouter, Depends, status, Query
from sqlalchemy.orm import Session
from typing import List, Optional

from app.database import get_db
from app.services import InspectionService
from app.schemas import InspectionCreate, InspectionUpdate, InspectionResponse
from app.models import User
from app.routers.auth import get_current_user

router = APIRouter(prefix="/inspections", tags=["inspections"])


@router.get("", response_model=List[InspectionResponse])
def get_inspections(
    hive_id: Optional[str] = Query(None, description="Filter by hive ID"),
    limit: Optional[int] = Query(None, description="Limit number of recent inspections"),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """
    Get inspections with optional filters:
    - hive_id: Get inspections for a specific hive
    - limit: Get the most recent N inspections
    """
    service = InspectionService(db)

    if hive_id:
        return service.get_inspections_by_hive(hive_id, current_user.id)
    elif limit is not None:
        return service.get_recent_inspections(current_user.id, limit)
    else:
        return service.get_all_inspections(current_user.id)


@router.get("/recent", response_model=List[InspectionResponse])
def get_recent_inspections(
    limit: int = Query(10, description="Number of recent inspections to return"),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """Get the most recent inspections"""
    service = InspectionService(db)
    return service.get_recent_inspections(current_user.id, limit)


@router.get("/hive/{hive_id}/latest", response_model=InspectionResponse)
def get_latest_hive_inspection(
    hive_id: str,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """Get the most recent inspection for a specific hive"""
    service = InspectionService(db)
    return service.get_latest_for_hive(hive_id, current_user.id)


@router.get("/{inspection_id}", response_model=InspectionResponse)
def get_inspection(
    inspection_id: str,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """Get a specific inspection by ID"""
    service = InspectionService(db)
    return service.get_inspection(inspection_id, current_user.id)


@router.post("", response_model=InspectionResponse, status_code=status.HTTP_201_CREATED)
def create_inspection(
    inspection: InspectionCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """Create a new inspection"""
    import uuid

    inspection_id = str(uuid.uuid4())
    service = InspectionService(db)
    return service.create_inspection(inspection, inspection_id, current_user.id)


@router.put("/{inspection_id}", response_model=InspectionResponse)
def update_inspection(
    inspection_id: str,
    inspection: InspectionUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """Update an existing inspection"""
    service = InspectionService(db)
    return service.update_inspection(inspection_id, inspection, current_user.id)


@router.delete("/{inspection_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_inspection(
    inspection_id: str,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """Delete an inspection"""
    service = InspectionService(db)
    service.delete_inspection(inspection_id, current_user.id)
