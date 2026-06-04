"""Feed subpackage.

OWNED FILES (this agent created the foundational registry):
  - registry.py  -> COMPLETE. FeedSource, register(), registered(), clear(),
                    FeedItemResponse, FeedAuthor, to_camel.

TO BE CREATED BY THE FEED MODULE AGENT (do not touch registry.py shapes):
  - service.py   -> class FeedService with:
        async def get_feed(self, user_id: str, limit: int = 20,
                           before: datetime | None = None) -> list[FeedItemResponse]
        It must iterate feed.registry.registered(), query each FeedSource.document
        for (user_id IN followed_ids AND is_public True), apply the `before`
        cursor on FeedSource.occurred_at, merge, sort by occurred_at desc, slice
        to `limit`, and build FeedItemResponse(type=src.type,
        author=FeedAuthor(...), occurred_at=src.occurred_at(doc),
        payload=src.to_item(doc)). Author names come from a single batched
        User.find(In(User.id, author_ids)) query. Uses follow_service for
        get_following_ids.
  - router.py    -> APIRouter(prefix="/feed"); GET "" -> list[FeedItemResponse],
        params limit (1..100, default 20) and before (datetime cursor),
        Depends(get_current_user). Export as `feed_router`.

This __init__ must continue to export: FeedSource, register, registered,
FeedItemResponse, FeedService, feed_service, feed_router.
"""
from .registry import (
    FeedAuthor,
    FeedItemResponse,
    FeedSource,
    clear,
    register,
    registered,
    to_camel,
)

try:  # service.py is authored by the feed module agent.
    from .service import FeedService, announce  # type: ignore

    feed_service = FeedService()
except Exception:  # pragma: no cover - stub fallback until module is authored
    FeedService = None  # type: ignore
    feed_service = None
    announce = None  # type: ignore

try:  # router.py is authored by the feed module agent.
    from .router import router as feed_router  # type: ignore
except Exception:  # pragma: no cover
    feed_router = None

__all__ = [
    "FeedSource",
    "register",
    "registered",
    "clear",
    "FeedItemResponse",
    "FeedAuthor",
    "to_camel",
    "FeedService",
    "feed_service",
    "feed_router",
    "announce",
]
