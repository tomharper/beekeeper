import uuid
from typing import List
from fastapi import HTTPException, status
from beanie.operators import In

from app.models import Follow, User
from app.repositories.follow_repository import FollowRepository
from app.schemas.follow import UserSummary


class FollowService:
    def __init__(self):
        self.repository = FollowRepository()

    async def follow(self, follower_id: str, followed_id: str) -> None:
        if follower_id == followed_id:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="You cannot follow yourself",
            )
        target = await User.get(followed_id)
        if not target:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND, detail="User not found"
            )
        # Idempotent — following an already-followed user is a no-op
        existing = await self.repository.get(follower_id, followed_id)
        if existing:
            return
        await self.repository.create(
            Follow(id=str(uuid.uuid4()), follower_id=follower_id, followed_id=followed_id)
        )

    async def unfollow(self, follower_id: str, followed_id: str) -> None:
        existing = await self.repository.get(follower_id, followed_id)
        if existing:
            await self.repository.delete(existing)

    async def get_following_ids(self, follower_id: str) -> List[str]:
        follows = await self.repository.get_following(follower_id)
        return [f.followed_id for f in follows]

    async def get_following(self, follower_id: str) -> List[UserSummary]:
        return await self._summaries(await self.get_following_ids(follower_id))

    async def get_followers(self, followed_id: str) -> List[UserSummary]:
        follows = await self.repository.get_followers(followed_id)
        return await self._summaries([f.follower_id for f in follows])

    async def search_users(self, query: str, exclude_id: str) -> List[UserSummary]:
        if not query.strip():
            return []
        # Case-insensitive name match; never exposes email/PII (UserSummary is id+name)
        users = await User.find(
            {"full_name": {"$regex": query, "$options": "i"}}
        ).limit(20).to_list()
        return [
            UserSummary(id=u.id, full_name=u.full_name)
            for u in users
            if u.id != exclude_id
        ]

    async def _summaries(self, ids: List[str]) -> List[UserSummary]:
        if not ids:
            return []
        users = await User.find(In(User.id, ids)).to_list()
        return [UserSummary(id=u.id, full_name=u.full_name) for u in users]
