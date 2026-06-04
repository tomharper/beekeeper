"""Follow subpackage: social graph (per-vertical DB)."""
from .models import Follow
from .repository import FollowRepository
from .router import router as follow_router
from .schemas import UserSummary
from .service import FollowService

# Module-level singleton-style instance for convenient reuse by other services.
follow_service = FollowService()

__all__ = [
    "Follow",
    "FollowRepository",
    "FollowService",
    "follow_service",
    "follow_router",
    "UserSummary",
]
