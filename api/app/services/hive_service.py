from sqlalchemy.orm import Session
from typing import List
from fastapi import HTTPException, status

from app.models import Hive, HiveStatus
from app.repositories import HiveRepository
from app.schemas import HiveCreate, HiveUpdate, HiveResponse


class HiveService:
    def __init__(self, db: Session):
        self.repository = HiveRepository(db)

    def get_all_hives(self) -> List[HiveResponse]:
        hives = self.repository.get_all()
        return [HiveResponse.model_validate(hive) for hive in hives]

    def get_hive(self, hive_id: str) -> HiveResponse:
        hive = self.repository.get_by_id(hive_id)
        if not hive:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND, detail="Hive not found"
            )
        return HiveResponse.model_validate(hive)

    def get_hives_by_apiary(self, apiary_id: str) -> List[HiveResponse]:
        hives = self.repository.get_by_apiary_id(apiary_id)
        return [HiveResponse.model_validate(hive) for hive in hives]

    def create_hive(self, hive_data: HiveCreate, hive_id: str) -> HiveResponse:
        hive = Hive(
            id=hive_id,
            status=HiveStatus.STRONG,
            **hive_data.model_dump()
        )
        created_hive = self.repository.create(hive)
        return HiveResponse.model_validate(created_hive)

    def update_hive(self, hive_id: str, hive_data: HiveUpdate) -> HiveResponse:
        hive = self.repository.get_by_id(hive_id)
        if not hive:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND, detail="Hive not found"
            )

        update_data = hive_data.model_dump(exclude_unset=True)
        for key, value in update_data.items():
            setattr(hive, key, value)

        updated_hive = self.repository.update(hive)
        return HiveResponse.model_validate(updated_hive)

    def delete_hive(self, hive_id: str) -> None:
        hive = self.repository.get_by_id(hive_id)
        if not hive:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND, detail="Hive not found"
            )
        self.repository.delete(hive)
