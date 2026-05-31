from .apiary import Apiary, ApiaryStatus
from .hive import (
    Hive,
    HiveStatus,
    ColonyStrength,
    QueenStatus,
    Temperament,
    HoneyStores,
)
from .alert import Alert, AlertType, AlertSeverity
from .recommendation import Recommendation, RecommendationType, Priority
from .user import User
from .follow import Follow
from .task import (
    Task,
    TaskType,
    TaskStatus,
    TaskPriority,
    RecurrenceFrequency,
)
from .inspection import (
    Inspection,
    QueenCellStatus,
    BroodPattern,
    ColonyTemperament,
    ColonyPopulation,
    HealthStatus,
    ResourceLevel,
)


# Registered with init_beanie() at startup.
DOCUMENT_MODELS = [
    Apiary,
    Hive,
    Inspection,
    Task,
    User,
    Follow,
    Alert,
    Recommendation,
]


__all__ = [
    "DOCUMENT_MODELS",
    "Apiary",
    "ApiaryStatus",
    "Hive",
    "HiveStatus",
    "ColonyStrength",
    "QueenStatus",
    "Temperament",
    "HoneyStores",
    "Alert",
    "AlertType",
    "AlertSeverity",
    "Recommendation",
    "RecommendationType",
    "Priority",
    "User",
    "Follow",
    "Task",
    "TaskType",
    "TaskStatus",
    "TaskPriority",
    "RecurrenceFrequency",
    "Inspection",
    "QueenCellStatus",
    "BroodPattern",
    "ColonyTemperament",
    "ColonyPopulation",
    "HealthStatus",
    "ResourceLevel",
]
