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


# Beekeeper's own domain documents. Passed to assistive_core.init_core() as
# `vertical_documents`; the shared social docs (Follow, Notification, Event) and
# the identity User are wired by init_core itself.
DOMAIN_DOCUMENTS = [
    Apiary,
    Hive,
    Inspection,
    Task,
    Alert,
    Recommendation,
]


__all__ = [
    "DOMAIN_DOCUMENTS",
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
