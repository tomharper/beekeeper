from fastapi import APIRouter, Depends, status, Query
from typing import List, Optional

from app.services import InspectionService
from app.schemas import InspectionCreate, InspectionUpdate, InspectionResponse
from assistive_core import User, get_current_user

router = APIRouter(prefix="/inspections", tags=["inspections"])


@router.get("", response_model=List[InspectionResponse])
async def get_inspections(
    hive_id: Optional[str] = Query(None, description="Filter by hive ID"),
    limit: Optional[int] = Query(None, description="Limit number of recent inspections"),
    current_user: User = Depends(get_current_user),
):
    service = InspectionService()
    if hive_id:
        return await service.get_inspections_by_hive(hive_id, current_user.id)
    elif limit is not None:
        return await service.get_recent_inspections(current_user.id, limit)
    else:
        return await service.get_all_inspections(current_user.id)


@router.get("/recent", response_model=List[InspectionResponse])
async def get_recent_inspections(
    limit: int = Query(10, description="Number of recent inspections to return"),
    current_user: User = Depends(get_current_user),
):
    service = InspectionService()
    return await service.get_recent_inspections(current_user.id, limit)


@router.get("/hive/{hive_id}/latest", response_model=InspectionResponse)
async def get_latest_hive_inspection(
    hive_id: str,
    current_user: User = Depends(get_current_user),
):
    service = InspectionService()
    return await service.get_latest_for_hive(hive_id, current_user.id)


@router.get("/{inspection_id}", response_model=InspectionResponse)
async def get_inspection(
    inspection_id: str,
    current_user: User = Depends(get_current_user),
):
    service = InspectionService()
    return await service.get_inspection(inspection_id, current_user.id)


@router.post("", response_model=InspectionResponse, status_code=status.HTTP_201_CREATED)
async def create_inspection(
    inspection: InspectionCreate,
    current_user: User = Depends(get_current_user),
):
    import uuid

    inspection_id = str(uuid.uuid4())
    service = InspectionService()
    return await service.create_inspection(inspection, inspection_id, current_user.id)


@router.put("/{inspection_id}", response_model=InspectionResponse)
async def update_inspection(
    inspection_id: str,
    inspection: InspectionUpdate,
    current_user: User = Depends(get_current_user),
):
    service = InspectionService()
    return await service.update_inspection(inspection_id, inspection, current_user.id)


@router.delete("/{inspection_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_inspection(
    inspection_id: str,
    current_user: User = Depends(get_current_user),
):
    service = InspectionService()
    await service.delete_inspection(inspection_id, current_user.id)
