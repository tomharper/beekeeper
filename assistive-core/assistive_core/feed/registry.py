"""Feed-source registry.

The feed must NOT hardcode record types. Each vertical registers its public
record types as ``FeedSource`` instances; ``feed_service`` then queries every
registered source for ``(user_id IN followed AND is_public)``, merges by
``occurred_at`` desc, and cursor-paginates. The same registry drives
notification fan-out on public creates.
"""
from __future__ import annotations

from dataclasses import dataclass, field
from datetime import datetime
from typing import Any, Callable, List, Optional

from beanie import Document
from pydantic import BaseModel, ConfigDict


def to_camel(string: str) -> str:
    """Convert snake_case to camelCase (ports beekeeper's to_camel)."""
    words = string.split("_")
    return words[0] + "".join(word.capitalize() for word in words[1:])


# A doc -> datetime accessor, a doc -> str (user id) accessor, a doc -> payload.
OccurredAt = Callable[[Any], datetime]
UserIdAccessor = Callable[[Any], str]
ToItem = Callable[[Any], Any]


@dataclass
class FeedSource:
    """One registered record kind that can appear in the social feed.

    - ``type``: string discriminator carried on each feed item (e.g. "inspection").
    - ``document``: the Beanie ``Document`` subclass to query.
    - ``occurred_at``: callable mapping a doc to the datetime used for ordering.
    - ``user_id``: callable mapping a doc to its author's user id
      (default ``lambda d: d.user_id``).
    - ``to_item``: callable mapping a doc to its feed payload (dict or
      Pydantic model) carried under the ``type`` key of the feed item.
    """

    type: str
    document: type[Document]
    occurred_at: OccurredAt
    to_item: ToItem
    user_id: UserIdAccessor = field(default=lambda d: d.user_id)
    # Stored field names used for DB-side querying. A source whose document
    # names these differently just overrides them — the feed query is no longer
    # hardcoded to "user_id"/"is_public".
    user_field: str = "user_id"
    is_public_field: str = "is_public"
    # Stored datetime field for DB-side sort + cursor. When set, the feed pushes
    # ordering/cursor/limit into each per-source query instead of loading every
    # public doc and sorting in Python. Leave None to keep the Python fallback.
    occurred_at_field: Optional[str] = None
    # Optional follower-notification descriptor: doc -> dict(title, message?,
    # ref_type?, ref_id?) or None. When set, ``assistive_core.announce(record)``
    # fans out to followers with no per-vertical notification code.
    notify: Optional[Callable[[Any], Optional[dict]]] = None


# Module-level registry. Populated by init_core() via register().
_REGISTRY: List[FeedSource] = []


def register(source: FeedSource) -> None:
    """Register a feed source. Idempotent per ``type`` (re-registering a type
    replaces the prior entry, so re-init in tests is safe)."""
    global _REGISTRY
    _REGISTRY = [s for s in _REGISTRY if s.type != source.type]
    _REGISTRY.append(source)


def registered() -> List[FeedSource]:
    """Return all currently-registered feed sources."""
    return list(_REGISTRY)


def clear() -> None:
    """Clear the registry (test helper)."""
    _REGISTRY.clear()


class FeedItemResponse(BaseModel):
    """A single activity item from a followed user.

    The record itself is the content (no separate 'post' abstraction).
    ``type`` discriminates which record kind is carried; ``payload`` holds the
    source-specific body produced by ``FeedSource.to_item``.
    """

    type: str
    author: "FeedAuthor"
    occurred_at: datetime
    payload: Any = None

    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)


class FeedAuthor(BaseModel):
    """Minimal author identity on a feed item (id + name only, no PII)."""

    id: str
    full_name: str

    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)


FeedItemResponse.model_rebuild()
