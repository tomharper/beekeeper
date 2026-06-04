"""Follow router (/follows/*, /users/search).
Ported from beekeeper api/app/routers/follow.py."""
from typing import List

from fastapi import APIRouter, Depends, Query, status

from ..auth.deps import get_current_user
from ..auth.models import User
from .schemas import UserSummary
from .service import FollowService

router = APIRouter(tags=["follow"])


@router.get("/users/search", response_model=List[UserSummary])
async def search_users(
    q: str = Query(..., min_length=1, description="Name to search for"),
    current_user: User = Depends(get_current_user),
):
    """Find other users by name (returns id + name only)."""
    return await FollowService().search_users(q, current_user.id)


@router.get("/follows/following", response_model=List[UserSummary])
async def list_following(current_user: User = Depends(get_current_user)):
    """List the users the current user follows."""
    return await FollowService().get_following(current_user.id)


@router.get("/follows/followers", response_model=List[UserSummary])
async def list_followers(current_user: User = Depends(get_current_user)):
    """List the users who follow the current user."""
    return await FollowService().get_followers(current_user.id)


@router.post("/follows/{user_id}", status_code=status.HTTP_204_NO_CONTENT)
async def follow_user(user_id: str, current_user: User = Depends(get_current_user)):
    """Follow another user. Idempotent."""
    await FollowService().follow(current_user.id, user_id)


@router.delete("/follows/{user_id}", status_code=status.HTTP_204_NO_CONTENT)
async def unfollow_user(user_id: str, current_user: User = Depends(get_current_user)):
    """Unfollow a user. No-op if not currently followed."""
    await FollowService().unfollow(current_user.id, user_id)
