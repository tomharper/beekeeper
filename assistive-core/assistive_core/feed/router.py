"""Feed router (/feed). Ported from beekeeper api/app/routers/feed.py.

Registry-driven: the items returned span every registered FeedSource, so this
router stays vertical-agnostic (no record-kind imports)."""
from datetime import datetime
from typing import List, Optional

from fastapi import APIRouter, Depends, Query

from ..auth.deps import get_current_user
from ..auth.models import User
from .registry import FeedItemResponse
from .service import FeedService

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
