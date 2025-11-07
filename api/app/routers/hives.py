from fastapi import APIRouter, Depends, status, Query
from sqlalchemy.orm import Session
from typing import List, Optional

from app.database import get_db
from app.services import HiveService
from app.schemas import HiveCreate, HiveUpdate, HiveResponse

router = APIRouter(prefix="/hives", tags=["hives"])


@router.get("", response_model=List[HiveResponse])
def get_hives(
    apiary_id: Optional[str] = Query(None), db: Session = Depends(get_db)
):
    """Get all hives, optionally filtered by apiary_id"""
    service = HiveService(db)
    if apiary_id:
        return service.get_hives_by_apiary(apiary_id)
    return service.get_all_hives()


@router.get("/{hive_id}", response_model=HiveResponse)
def get_hive(hive_id: str, db: Session = Depends(get_db)):
    """Get a specific hive by ID"""
    service = HiveService(db)
    return service.get_hive(hive_id)


@router.post("", response_model=HiveResponse, status_code=status.HTTP_201_CREATED)
def create_hive(hive: HiveCreate, db: Session = Depends(get_db)):
    """Create a new hive"""
    import uuid

    hive_id = str(uuid.uuid4())
    service = HiveService(db)
    return service.create_hive(hive, hive_id)


@router.put("/{hive_id}", response_model=HiveResponse)
def update_hive(hive_id: str, hive: HiveUpdate, db: Session = Depends(get_db)):
    """Update an existing hive"""
    service = HiveService(db)
    return service.update_hive(hive_id, hive)


@router.delete("/{hive_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_hive(hive_id: str, db: Session = Depends(get_db)):
    """Delete a hive"""
    service = HiveService(db)
    service.delete_hive(hive_id)
