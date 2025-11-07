from enum import Enum as PyEnum
from sqlalchemy import String, Integer, Float, Enum
from sqlalchemy.orm import Mapped, mapped_column, relationship
from typing import List, Optional

from .base import Base, TimestampMixin


class ApiaryStatus(str, PyEnum):
    HEALTHY = "HEALTHY"
    WARNING = "WARNING"
    ALERT = "ALERT"


class Apiary(Base, TimestampMixin):
    __tablename__ = "apiaries"

    id: Mapped[str] = mapped_column(String, primary_key=True)
    name: Mapped[str] = mapped_column(String, nullable=False)
    location: Mapped[str] = mapped_column(String, nullable=False)
    latitude: Mapped[Optional[float]] = mapped_column(Float, nullable=True)
    longitude: Mapped[Optional[float]] = mapped_column(Float, nullable=True)
    status: Mapped[ApiaryStatus] = mapped_column(
        Enum(ApiaryStatus), default=ApiaryStatus.HEALTHY, nullable=False
    )

    # Relationships
    hives: Mapped[List["Hive"]] = relationship(
        "Hive", back_populates="apiary", cascade="all, delete-orphan"
    )

    @property
    def hive_count(self) -> int:
        return len(self.hives) if self.hives else 0
