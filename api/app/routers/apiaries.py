from fastapi import APIRouter, status
from typing import List

from app.services import ApiaryService
from app.schemas import ApiaryCreate, ApiaryUpdate, ApiaryResponse

router = APIRouter(prefix="/apiaries", tags=["apiaries"])


@router.get("", response_model=List[ApiaryResponse])
async def get_apiaries():
    """Get all apiaries"""
    service = ApiaryService()
    return await service.get_all_apiaries()


@router.get("/{apiary_id}", response_model=ApiaryResponse)
async def get_apiary(apiary_id: str):
    """Get a specific apiary by ID"""
    service = ApiaryService()
    return await service.get_apiary(apiary_id)


@router.post("", response_model=ApiaryResponse, status_code=status.HTTP_201_CREATED)
async def create_apiary(apiary: ApiaryCreate):
    """Create a new apiary"""
    import uuid

    apiary_id = str(uuid.uuid4())
    service = ApiaryService()
    return await service.create_apiary(apiary, apiary_id)


@router.put("/{apiary_id}", response_model=ApiaryResponse)
async def update_apiary(apiary_id: str, apiary: ApiaryUpdate):
    """Update an existing apiary"""
    service = ApiaryService()
    return await service.update_apiary(apiary_id, apiary)


@router.delete("/{apiary_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_apiary(apiary_id: str):
    """Delete an apiary"""
    service = ApiaryService()
    await service.delete_apiary(apiary_id)
