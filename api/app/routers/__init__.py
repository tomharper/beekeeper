from .apiaries import router as apiaries_router
from .hives import router as hives_router
from .alerts import router as alerts_router
from .recommendations import router as recommendations_router
from .weather import router as weather_router
from .tasks import router as tasks_router
from .inspections import router as inspections_router

__all__ = [
    "apiaries_router",
    "hives_router",
    "alerts_router",
    "recommendations_router",
    "weather_router",
    "tasks_router",
    "inspections_router",
]
