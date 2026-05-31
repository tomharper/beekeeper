from beanie import Document
from pymongo import IndexModel

from .base import TimestampMixin


class Follow(Document, TimestampMixin):
    # Identity — plain str UUID to match the rest of the schema
    id: str  # type: ignore[assignment]
    follower_id: str  # the user doing the following (User.id)
    followed_id: str  # the user being followed

    class Settings:
        name = "follows"
        indexes = [
            # No duplicate follow edges
            IndexModel([("follower_id", 1), ("followed_id", 1)], unique=True),
            # Reverse lookup: who follows me
            "followed_id",
        ]
