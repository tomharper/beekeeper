from fastapi import APIRouter, status, Query
from typing import List, Optional

from app.services import HiveService
from app.schemas import HiveCreate, HiveUpdate, HiveResponse

router = APIRouter(prefix="/hives", tags=["hives"])


@router.get("", response_model=List[HiveResponse])
async def get_hives(apiary_id: Optional[str] = Query(None)):
    """Get all hives, optionally filtered by apiary_id"""
    service = HiveService()
    if apiary_id:
        return await service.get_hives_by_apiary(apiary_id)
    return await service.get_all_hives()


@router.get("/{hive_id}", response_model=HiveResponse)
async def get_hive(hive_id: str):
    """Get a specific hive by ID"""
    service = HiveService()
    return await service.get_hive(hive_id)


@router.post("", response_model=HiveResponse, status_code=status.HTTP_201_CREATED)
async def create_hive(hive: HiveCreate):
    """Create a new hive"""
    import uuid

    hive_id = str(uuid.uuid4())
    service = HiveService()
    return await service.create_hive(hive, hive_id)


@router.put("/{hive_id}", response_model=HiveResponse)
async def update_hive(hive_id: str, hive: HiveUpdate):
    """Update an existing hive"""
    service = HiveService()
    return await service.update_hive(hive_id, hive)


@router.delete("/{hive_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_hive(hive_id: str):
    """Delete a hive"""
    service = HiveService()
    await service.delete_hive(hive_id)
