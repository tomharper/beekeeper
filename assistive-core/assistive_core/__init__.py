"""assistive-core: shared social-broadcast substrate for assistive vertical apps.

This module pre-declares the FULL intended public API by importing from every
planned submodule. The parallel module authors only create files INSIDE their
own subpackage (and the submodule __init__ re-exports the new names) - they never
edit this file. See CONVENTIONS.md for the exact names each submodule must provide.

Multi-DB design:
  - User lives in the SHARED identity DB (SSO across verticals).
  - Follow / Notification / Event + each vertical's domain docs live in the
    per-vertical DB. ``init_core`` wires both and registers feed sources.
"""

# --- settings ---
from .settings import settings, Settings

# --- db / lifecycle ---
from .db import init_core, close_core, get_client, CORE_SOCIAL_DOCUMENTS

# --- shared model primitives ---
from .base import TimestampMixin, utcnow

# --- auth (shared identity / SSO) ---
from .auth import (
    User,
    auth_service,
    auth_router,
    get_current_user,
    get_current_user_optional,
    Token,
    UserCreate,
    UserLogin,
    UserResponse,
)

# --- follow (social graph) ---
from .follow import (
    Follow,
    FollowRepository,
    FollowService,
    follow_service,
    follow_router,
    UserSummary,
)

# --- feed (registry-driven aggregation) ---
from .feed import (
    FeedSource,
    register,
    registered,
    FeedItemResponse,
    FeedAuthor,
    FeedService,
    feed_service,
    feed_router,
    announce,
)

# --- notifications (fan-out inbox) ---
from .notifications import (
    Notification,
    NotificationResponse,
    NotificationRepository,
    NotificationService,
    notification_service,
    notification_router,
)

# --- calendar (events) ---
from .calendar import (
    Event,
    EventCreate,
    EventUpdate,
    EventResponse,
    EventRepository,
    EventService,
    event_service,
    event_router,
)

# --- external-service clients ---
from .clients import (
    BunnyStorageService,
    bunny_storage,
    WeatherClient,
    weather_client,
)

__all__ = [
    # settings
    "settings",
    "Settings",
    # db
    "init_core",
    "close_core",
    "get_client",
    "CORE_SOCIAL_DOCUMENTS",
    # base
    "TimestampMixin",
    "utcnow",
    # auth
    "User",
    "auth_service",
    "auth_router",
    "get_current_user",
    "get_current_user_optional",
    "Token",
    "UserCreate",
    "UserLogin",
    "UserResponse",
    # follow
    "Follow",
    "FollowRepository",
    "FollowService",
    "follow_service",
    "follow_router",
    "UserSummary",
    # feed
    "FeedSource",
    "register",
    "registered",
    "FeedItemResponse",
    "FeedAuthor",
    "FeedService",
    "feed_service",
    "feed_router",
    "announce",
    # notifications
    "Notification",
    "NotificationResponse",
    "NotificationRepository",
    "NotificationService",
    "notification_service",
    "notification_router",
    # calendar
    "Event",
    "EventCreate",
    "EventUpdate",
    "EventResponse",
    "EventRepository",
    "EventService",
    "event_service",
    "event_router",
    # clients
    "BunnyStorageService",
    "bunny_storage",
    "WeatherClient",
    "weather_client",
]
