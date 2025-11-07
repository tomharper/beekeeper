from fastapi import APIRouter, Depends, status
from sqlalchemy.orm import Session
from typing import List

from app.database import get_db
from app.services import ApiaryService
from app.schemas import ApiaryCreate, ApiaryUpdate, ApiaryResponse

router = APIRouter(prefix="/apiaries", tags=["apiaries"])


@router.get("", response_model=List[ApiaryResponse])
def get_apiaries(db: Session = Depends(get_db)):
    """Get all apiaries"""
    service = ApiaryService(db)
    return service.get_all_apiaries()


@router.get("/{apiary_id}", response_model=ApiaryResponse)
def get_apiary(apiary_id: str, db: Session = Depends(get_db)):
    """Get a specific apiary by ID"""
    service = ApiaryService(db)
    return service.get_apiary(apiary_id)


@router.post("", response_model=ApiaryResponse, status_code=status.HTTP_201_CREATED)
def create_apiary(apiary: ApiaryCreate, db: Session = Depends(get_db)):
    """Create a new apiary"""
    import uuid

    apiary_id = str(uuid.uuid4())
    service = ApiaryService(db)
    return service.create_apiary(apiary, apiary_id)


@router.put("/{apiary_id}", response_model=ApiaryResponse)
def update_apiary(
    apiary_id: str, apiary: ApiaryUpdate, db: Session = Depends(get_db)
):
    """Update an existing apiary"""
    service = ApiaryService(db)
    return service.update_apiary(apiary_id, apiary)


@router.delete("/{apiary_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_apiary(apiary_id: str, db: Session = Depends(get_db)):
    """Delete an apiary"""
    service = ApiaryService(db)
    service.delete_apiary(apiary_id)
