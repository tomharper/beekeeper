from .apiaries import router as apiaries_router
from .hives import router as hives_router
from .alerts import router as alerts_router
from .recommendations import router as recommendations_router
from .weather import router as weather_router
from .tasks import router as tasks_router
from .inspections import router as inspections_router
from .photos import router as photos_router
from .chat import router as chat_router
from .follow import router as follow_router
from .feed import router as feed_router
from .event import router as event_router
from .notification import router as notification_router

__all__ = [
    "event_router",
    "notification_router",
    "apiaries_router",
    "hives_router",
    "alerts_router",
    "recommendations_router",
    "weather_router",
    "tasks_router",
    "inspections_router",
    "photos_router",
    "chat_router",
    "follow_router",
    "feed_router",
]
