"""Notifications subpackage.

OWNED FILES (this agent created the foundational document):
  - models.py -> COMPLETE. Notification document (per-vertical DB).

TO BE CREATED BY THE NOTIFICATIONS MODULE AGENT:
  - schemas.py    -> NotificationResponse (port beekeeper schemas/notification.py,
                     camelCase aliasing).
  - repository.py -> class NotificationRepository: get_by_id, get_by_user_id,
                     create, insert_many, update, mark_all_read
                     (port beekeeper repositories/notification_repository.py).
  - service.py    -> class NotificationService with:
        async def create_for_followers(self, actor_id, type, title, message="",
                                       ref_type=None, ref_id=None) -> None
        async def list_for_user(self, user_id) -> list[NotificationResponse]
        async def mark_read(self, notification_id, user_id) -> NotificationResponse
        async def mark_all_read(self, user_id) -> None
      (port beekeeper services/notification_service.py; depends on FollowRepository
       for fan-out, resolved from assistive_core.follow).
  - router.py     -> APIRouter(prefix="/notifications"): GET "" list,
        POST "/{id}/read", POST "/read-all". Export as `notification_router`.

This __init__ must continue to export: Notification, NotificationResponse,
NotificationRepository, NotificationService, notification_service,
notification_router.
"""
from .models import Notification

try:
    from .schemas import NotificationResponse  # type: ignore
except Exception:  # pragma: no cover
    NotificationResponse = None  # type: ignore

try:
    from .repository import NotificationRepository  # type: ignore
except Exception:  # pragma: no cover
    NotificationRepository = None  # type: ignore

try:
    from .service import NotificationService  # type: ignore

    notification_service = NotificationService()
except Exception:  # pragma: no cover
    NotificationService = None  # type: ignore
    notification_service = None

try:
    from .router import router as notification_router  # type: ignore
except Exception:  # pragma: no cover
    notification_router = None

__all__ = [
    "Notification",
    "NotificationResponse",
    "NotificationRepository",
    "NotificationService",
    "notification_service",
    "notification_router",
]
