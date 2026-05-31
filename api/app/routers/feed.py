from fastapi import APIRouter, Depends, Query
from typing import List, Optional
from datetime import datetime

from app.services.feed_service import FeedService
from app.schemas.feed import FeedItemResponse
from app.models import User
from app.routers.auth import get_current_user

router = APIRouter(prefix="/feed", tags=["feed"])


@router.get("", response_model=List[FeedItemResponse])
async def get_feed(
    limit: int = Query(20, ge=1, le=100, description="Max items to return"),
    before: Optional[datetime] = Query(
        None, description="Cursor: occurredAt of the last item seen (for pagination)"
    ),
    current_user: User = Depends(get_current_user),
):
    """Public activity from the users you follow, newest first."""
    return await FeedService().get_feed(current_user.id, limit, before)
