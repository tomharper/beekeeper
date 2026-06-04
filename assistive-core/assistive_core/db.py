"""Database wiring for assistive-core.

Multi-DB Beanie init:
  - ``User`` is bound to the SHARED identity DB (env IDENTITY_DB) so one login
    works across every vertical.
  - The core social documents (Follow, Notification, Event) plus each vertical's
    domain documents are bound to the PER-VERTICAL DB (env MONGODB_DB).

``init_core`` is the single entrypoint a vertical's FastAPI lifespan calls. It
also registers the vertical's feed sources into the feed registry.
"""
from typing import List, Optional

from beanie import init_beanie
from motor.motor_asyncio import AsyncIOMotorClient

from .auth.models import User
from .calendar.models import Event
from .feed.registry import FeedSource, register as register_feed_source
from .follow.models import Follow
from .notifications.models import Notification
from .settings import settings, JWT_SECRET_PLACEHOLDER

# Core social documents that live in the per-vertical DB alongside domain docs.
CORE_SOCIAL_DOCUMENTS: list = [Follow, Notification, Event]

_client: Optional[AsyncIOMotorClient] = None


def get_client() -> AsyncIOMotorClient:
    global _client
    if _client is None:
        _client = AsyncIOMotorClient(settings.MONGODB_URI)
    return _client


async def init_core(
    *,
    vertical_documents: list,
    feed_sources: List[FeedSource],
) -> None:
    """Initialise Beanie across both databases and register feed sources.

    Args:
        vertical_documents: the calling vertical's Beanie Document classes
            (e.g. Apiary, Hive, Inspection, Task for beekeeper). Bound to the
            per-vertical DB.
        feed_sources: FeedSource instances describing which of those documents
            appear in the social feed / drive notification fan-out.
    """
    # Fail fast in production on the insecure default SSO signing key.
    if settings.ENV == "production" and settings.JWT_SECRET_KEY == JWT_SECRET_PLACEHOLDER:
        raise RuntimeError(
            "JWT_SECRET_KEY is the insecure default. Set a real JWT_SECRET_KEY "
            "in production — it signs SSO tokens shared across all assistive verticals."
        )

    client = get_client()

    # Identity DB: shared users collection (SSO).
    await init_beanie(
        database=client[settings.IDENTITY_DB],
        document_models=[User],
    )

    # Vertical DB: core social docs + the vertical's own domain docs.
    await init_beanie(
        database=client[settings.MONGODB_DB],
        document_models=[*CORE_SOCIAL_DOCUMENTS, *vertical_documents],
    )

    # Feed-source registry: each vertical declares its public record types.
    for source in feed_sources:
        register_feed_source(source)


async def close_core() -> None:
    global _client
    if _client is not None:
        _client.close()
        _client = None
