from sqlalchemy.orm import Session
from typing import List, Optional
from fastapi import HTTPException, status

from app.models import Apiary
from app.repositories import ApiaryRepository
from app.schemas import ApiaryCreate, ApiaryUpdate, ApiaryResponse


class ApiaryService:
    def __init__(self, db: Session):
        self.repository = ApiaryRepository(db)

    def get_all_apiaries(self) -> List[ApiaryResponse]:
        apiaries = self.repository.get_all()
        return [ApiaryResponse.model_validate(apiary) for apiary in apiaries]

    def get_apiary(self, apiary_id: str) -> ApiaryResponse:
        apiary = self.repository.get_by_id(apiary_id)
        if not apiary:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND, detail="Apiary not found"
            )
        return ApiaryResponse.model_validate(apiary)

    def create_apiary(self, apiary_data: ApiaryCreate, apiary_id: str) -> ApiaryResponse:
        apiary = Apiary(
            id=apiary_id,
            **apiary_data.model_dump()
        )
        created_apiary = self.repository.create(apiary)
        return ApiaryResponse.model_validate(created_apiary)

    def update_apiary(self, apiary_id: str, apiary_data: ApiaryUpdate) -> ApiaryResponse:
        apiary = self.repository.get_by_id(apiary_id)
        if not apiary:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND, detail="Apiary not found"
            )

        update_data = apiary_data.model_dump(exclude_unset=True)
        for key, value in update_data.items():
            setattr(apiary, key, value)

        updated_apiary = self.repository.update(apiary)
        return ApiaryResponse.model_validate(updated_apiary)

    def delete_apiary(self, apiary_id: str) -> None:
        apiary = self.repository.get_by_id(apiary_id)
        if not apiary:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND, detail="Apiary not found"
            )
        self.repository.delete(apiary)
