from typing import List
from fastapi import HTTPException, status

from app.models import Inspection
from app.repositories import InspectionRepository
from app.schemas import InspectionCreate, InspectionUpdate, InspectionResponse


class InspectionService:
    def __init__(self):
        self.repository = InspectionRepository()

    async def get_all_inspections(self, user_id: str) -> List[InspectionResponse]:
        inspections = await self.repository.get_by_user_id(user_id)
        return [InspectionResponse.model_validate(inspection) for inspection in inspections]

    async def get_inspection(self, inspection_id: str, user_id: str) -> InspectionResponse:
        inspection = await self.repository.get_by_id(inspection_id)
        if not inspection:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND, detail="Inspection not found"
            )
        if inspection.user_id != user_id:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Not authorized to access this inspection",
            )
        return InspectionResponse.model_validate(inspection)

    async def get_inspections_by_hive(self, hive_id: str, user_id: str) -> List[InspectionResponse]:
        inspections = await self.repository.get_by_hive_and_user(hive_id, user_id)
        return [InspectionResponse.model_validate(inspection) for inspection in inspections]

    async def get_latest_for_hive(self, hive_id: str, user_id: str) -> InspectionResponse:
        inspection = await self.repository.get_latest_for_hive(hive_id)
        if not inspection:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="No inspections found for this hive",
            )
        if inspection.user_id != user_id:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Not authorized to access this inspection",
            )
        return InspectionResponse.model_validate(inspection)

    async def get_recent_inspections(self, user_id: str, limit: int = 10) -> List[InspectionResponse]:
        inspections = await self.repository.get_recent(user_id, limit)
        return [InspectionResponse.model_validate(inspection) for inspection in inspections]

    async def create_inspection(
        self, inspection_data: InspectionCreate, inspection_id: str, user_id: str
    ) -> InspectionResponse:
        inspection = Inspection(
            id=inspection_id,
            user_id=user_id,
            **inspection_data.model_dump(),
        )
        created_inspection = await self.repository.create(inspection)
        return InspectionResponse.model_validate(created_inspection)

    async def update_inspection(
        self, inspection_id: str, inspection_data: InspectionUpdate, user_id: str
    ) -> InspectionResponse:
        inspection = await self.repository.get_by_id(inspection_id)
        if not inspection:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND, detail="Inspection not found"
            )
        if inspection.user_id != user_id:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Not authorized to modify this inspection",
            )

        update_data = inspection_data.model_dump(exclude_unset=True)
        for key, value in update_data.items():
            setattr(inspection, key, value)

        updated_inspection = await self.repository.update(inspection)
        return InspectionResponse.model_validate(updated_inspection)

    async def delete_inspection(self, inspection_id: str, user_id: str) -> None:
        inspection = await self.repository.get_by_id(inspection_id)
        if not inspection:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND, detail="Inspection not found"
            )
        if inspection.user_id != user_id:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Not authorized to delete this inspection",
            )
        await self.repository.delete(inspection)
