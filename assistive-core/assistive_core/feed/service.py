"""Feed service. Registry-driven port of beekeeper's feed_service.

Beekeeper's FeedService hardcoded the inspection + task record kinds. Here the
record kinds are NOT hardcoded: ``feed.registry.registered()`` supplies the set
of public record types each vertical opted into. For every registered
``FeedSource`` we query its document for ``(user_id IN followed AND is_public)``,
apply the ``before`` cursor and ordering against the source's ``occurred_at``,
merge all sources, sort by ``occurred_at`` desc, and slice to ``limit``.

Author names are denormalised in a SINGLE batched ``User.find`` across every
item from every source (User lives in the shared identity DB).
"""
from __future__ import annotations

from datetime import datetime
from typing import List, Optional

from beanie.operators import In

from ..auth.models import User
from ..follow import follow_service
from . import registry
from .registry import FeedAuthor, FeedItemResponse


class FeedService:
    async def get_feed(
        self, user_id: str, limit: int = 20, before: Optional[datetime] = None
    ) -> List[FeedItemResponse]:
        """Public activity from the users you follow, newest first."""
        followed_ids = await follow_service.get_following_ids(user_id)
        if not followed_ids:
            return []

        sources = registry.registered()
        if not sources:
            return []

        # (FeedSource, doc, occurred_at) gathered across every registered source.
        gathered = []
        for src in sources:
            # Query field names come from the source contract (defaulting to
            # user_id / is_public), so a source whose document names them
            # differently still works without editing the core.
            query = {
                src.user_field: {"$in": followed_ids},
                src.is_public_field: True,
            }
            if before is not None and src.occurred_at_field:
                query[src.occurred_at_field] = {"$lt": before}

            finder = src.document.find(query)
            if src.occurred_at_field:
                # Push ordering + cursor + per-source limit down into the DB.
                finder = finder.sort("-" + src.occurred_at_field).limit(limit)
            docs = await finder.to_list()

            for doc in docs:
                occurred = src.occurred_at(doc)
                # Apply the cursor in Python only for sources without a stored
                # occurred_at field (the DB already filtered the others).
                if (
                    before is not None
                    and not src.occurred_at_field
                    and not (occurred < before)
                ):
                    continue
                gathered.append((src, doc, occurred))

        if not gathered:
            return []

        gathered.sort(key=lambda g: g[2], reverse=True)
        gathered = gathered[:limit]

        # Denormalise author names in one batched lookup across all items.
        author_ids = list({src.user_id(doc) for src, doc, _ in gathered})
        users = await User.find(In(User.id, author_ids)).to_list()
        names = {u.id: u.full_name for u in users}

        return [
            FeedItemResponse(
                type=src.type,
                author=FeedAuthor(
                    id=src.user_id(doc),
                    full_name=names.get(src.user_id(doc), "Unknown"),
                ),
                occurred_at=occurred,
                payload=src.to_item(doc),
            )
            for src, doc, occurred in gathered
        ]


async def announce(record) -> None:
    """Registry-driven follower fan-out for a freshly-created public record.

    A vertical calls this once after creating any record; the matching
    ``FeedSource.notify`` descriptor supplies the notification content, so no
    per-vertical notification code is needed. Best-effort: never raises — a
    notification failure must not fail the caller's create.
    """
    try:
        for src in registry.registered():
            if not isinstance(record, src.document):
                continue
            if src.notify is None:
                return
            if not getattr(record, src.is_public_field, False):
                return
            desc = src.notify(record)
            if not desc:
                return
            # Imported lazily to avoid any import-order coupling with notifications.
            from ..notifications import notification_service

            await notification_service.create_for_followers(
                actor_id=src.user_id(record),
                type=src.type,
                title=desc.get("title", ""),
                message=desc.get("message", ""),
                ref_type=desc.get("ref_type", src.type),
                ref_id=desc.get("ref_id", getattr(record, "id", None)),
            )
            return
    except Exception:
        pass
