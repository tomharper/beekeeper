from sqlalchemy.orm import Session
from typing import List
from fastapi import HTTPException, status

from app.models import Inspection
from app.repositories import InspectionRepository
from app.schemas import InspectionCreate, InspectionUpdate, InspectionResponse


class InspectionService:
    def __init__(self, db: Session):
        self.repository = InspectionRepository(db)

    def get_all_inspections(self, user_id: str) -> List[InspectionResponse]:
        """Get all inspections for a user"""
        inspections = self.repository.get_by_user_id(user_id)
        return [InspectionResponse.model_validate(inspection) for inspection in inspections]

    def get_inspection(self, inspection_id: str, user_id: str) -> InspectionResponse:
        """Get a specific inspection by ID"""
        inspection = self.repository.get_by_id(inspection_id)
        if not inspection:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND, detail="Inspection not found"
            )

        # Verify the inspection belongs to the user
        if inspection.user_id != user_id:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Not authorized to access this inspection",
            )

        return InspectionResponse.model_validate(inspection)

    def get_inspections_by_hive(self, hive_id: str, user_id: str) -> List[InspectionResponse]:
        """Get all inspections for a specific hive"""
        inspections = self.repository.get_by_hive_and_user(hive_id, user_id)
        return [InspectionResponse.model_validate(inspection) for inspection in inspections]

    def get_latest_for_hive(self, hive_id: str, user_id: str) -> InspectionResponse:
        """Get the most recent inspection for a hive"""
        inspection = self.repository.get_latest_for_hive(hive_id)
        if not inspection:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="No inspections found for this hive",
            )

        # Verify the inspection belongs to the user
        if inspection.user_id != user_id:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Not authorized to access this inspection",
            )

        return InspectionResponse.model_validate(inspection)

    def get_recent_inspections(self, user_id: str, limit: int = 10) -> List[InspectionResponse]:
        """Get the most recent inspections for a user"""
        inspections = self.repository.get_recent(user_id, limit)
        return [InspectionResponse.model_validate(inspection) for inspection in inspections]

    def create_inspection(
        self, inspection_data: InspectionCreate, inspection_id: str, user_id: str
    ) -> InspectionResponse:
        """Create a new inspection"""
        inspection = Inspection(
            id=inspection_id,
            user_id=user_id,
            **inspection_data.model_dump()
        )
        created_inspection = self.repository.create(inspection)
        return InspectionResponse.model_validate(created_inspection)

    def update_inspection(
        self, inspection_id: str, inspection_data: InspectionUpdate, user_id: str
    ) -> InspectionResponse:
        """Update an existing inspection"""
        inspection = self.repository.get_by_id(inspection_id)
        if not inspection:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND, detail="Inspection not found"
            )

        # Verify the inspection belongs to the user
        if inspection.user_id != user_id:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Not authorized to modify this inspection",
            )

        update_data = inspection_data.model_dump(exclude_unset=True)
        for key, value in update_data.items():
            setattr(inspection, key, value)

        updated_inspection = self.repository.update(inspection)
        return InspectionResponse.model_validate(updated_inspection)

    def delete_inspection(self, inspection_id: str, user_id: str) -> None:
        """Delete an inspection"""
        inspection = self.repository.get_by_id(inspection_id)
        if not inspection:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND, detail="Inspection not found"
            )

        # Verify the inspection belongs to the user
        if inspection.user_id != user_id:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Not authorized to delete this inspection",
            )

        self.repository.delete(inspection)
