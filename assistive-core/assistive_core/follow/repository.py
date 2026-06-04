"""Follow repository. Ported from beekeeper api/app/repositories/follow_repository.py."""
from typing import List, Optional

from .models import Follow


class FollowRepository:
    async def get(self, follower_id: str, followed_id: str) -> Optional[Follow]:
        return await Follow.find_one(
            Follow.follower_id == follower_id,
            Follow.followed_id == followed_id,
        )

    async def get_following(self, follower_id: str) -> List[Follow]:
        return await Follow.find(Follow.follower_id == follower_id).to_list()

    async def get_followers(self, followed_id: str) -> List[Follow]:
        return await Follow.find(Follow.followed_id == followed_id).to_list()

    async def create(self, follow: Follow) -> Follow:
        await follow.insert()
        return follow

    async def delete(self, follow: Follow) -> None:
        await follow.delete()
