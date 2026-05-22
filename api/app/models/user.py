from beanie import Document
from pydantic import EmailStr
from pymongo import IndexModel


class User(Document):
    id: str  # type: ignore[assignment]
    email: EmailStr
    hashed_password: str
    full_name: str

    class Settings:
        name = "users"
        indexes = [
            IndexModel("email", unique=True),
        ]
