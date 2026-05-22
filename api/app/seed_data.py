from datetime import datetime, timedelta, timezone

from app.models import (
    Apiary,
    ApiaryStatus,
    Hive,
    HiveStatus,
    ColonyStrength,
    QueenStatus,
    Temperament,
    HoneyStores,
    Alert,
    AlertType,
    AlertSeverity,
    Recommendation,
    RecommendationType,
    Priority,
)


def _now() -> datetime:
    return datetime.now(timezone.utc)


async def seed_database() -> None:
    """Seed Mongo with initial demo data if the apiaries collection is empty."""
    if await Apiary.find_all().count() > 0:
        print("Database already seeded, skipping...")
        return

    print("Seeding database...")
    now = _now()

    apiaries = [
        Apiary(
            id="1",
            name="Backyard Garden",
            location="Sunnyvale, CA",
            latitude=37.3688,
            longitude=-122.0363,
            status=ApiaryStatus.HEALTHY,
        ),
        Apiary(
            id="2",
            name="Hillside Meadow",
            location="45.123, -122.456",
            latitude=45.123,
            longitude=-122.456,
            status=ApiaryStatus.WARNING,
        ),
        Apiary(
            id="3",
            name="Riverbend Apiary",
            location="Cloverdale, OR",
            latitude=45.2271,
            longitude=-123.4023,
            status=ApiaryStatus.ALERT,
        ),
    ]
    await Apiary.insert_many(apiaries)

    hives = [
        # Backyard Garden
        Hive(
            id="h1", name="Hive A-01", apiary_id="1",
            status=HiveStatus.STRONG, last_inspected=now - timedelta(days=3),
            colony_strength=ColonyStrength.STRONG, queen_status=QueenStatus.LAYING,
            temperament=Temperament.CALM, honey_stores=HoneyStores.FULL,
        ),
        Hive(
            id="h2", name="Hive A-02", apiary_id="1",
            status=HiveStatus.NEEDS_INSPECTION, last_inspected=now - timedelta(days=14),
            colony_strength=ColonyStrength.MODERATE, queen_status=QueenStatus.LAYING,
            temperament=Temperament.MODERATE, honey_stores=HoneyStores.ADEQUATE,
        ),
        Hive(
            id="h3", name="Hive B-01", apiary_id="1",
            status=HiveStatus.STRONG, last_inspected=now - timedelta(days=5),
            colony_strength=ColonyStrength.STRONG, queen_status=QueenStatus.LAYING,
            temperament=Temperament.CALM, honey_stores=HoneyStores.FULL,
        ),
        Hive(
            id="h4", name="Hive B-02", apiary_id="1",
            status=HiveStatus.ALERT, last_inspected=now - timedelta(days=2),
            colony_strength=ColonyStrength.WEAK, queen_status=QueenStatus.NOT_LAYING,
            temperament=Temperament.DEFENSIVE, honey_stores=HoneyStores.LOW,
        ),
        # Hillside Meadow
        Hive(
            id="h5", name="Hive 01", apiary_id="2",
            status=HiveStatus.STRONG, last_inspected=now - timedelta(days=1),
            colony_strength=ColonyStrength.STRONG, queen_status=QueenStatus.LAYING,
            temperament=Temperament.CALM, honey_stores=HoneyStores.FULL,
        ),
        Hive(
            id="h6", name="Hive 02", apiary_id="2",
            status=HiveStatus.STRONG, last_inspected=now - timedelta(days=2),
            colony_strength=ColonyStrength.STRONG, queen_status=QueenStatus.LAYING,
            temperament=Temperament.CALM, honey_stores=HoneyStores.ADEQUATE,
        ),
        Hive(
            id="h7", name="Hive 03", apiary_id="2",
            status=HiveStatus.NEEDS_INSPECTION, last_inspected=now - timedelta(days=9),
            colony_strength=ColonyStrength.MODERATE, queen_status=QueenStatus.LAYING,
            temperament=Temperament.MODERATE, honey_stores=HoneyStores.ADEQUATE,
        ),
        Hive(
            id="h8", name="Hive 04", apiary_id="2",
            status=HiveStatus.ALERT, last_inspected=now - timedelta(days=4),
            colony_strength=ColonyStrength.MODERATE, queen_status=QueenStatus.LAYING,
            temperament=Temperament.CALM, honey_stores=HoneyStores.ADEQUATE,
        ),
        # Riverbend
        Hive(
            id="h9", name="Hive 01", apiary_id="3",
            status=HiveStatus.WEAK, last_inspected=now - timedelta(days=7),
            colony_strength=ColonyStrength.WEAK, queen_status=QueenStatus.MISSING,
            temperament=Temperament.DEFENSIVE, honey_stores=HoneyStores.LOW,
        ),
    ]
    await Hive.insert_many(hives)

    alerts = [
        Alert(
            id="a1",
            type=AlertType.SWARM_WARNING,
            title="Swarm Warning",
            message="AI Alert: High Swarm Probability in this area. Check hives A-01 and C-04.",
            severity=AlertSeverity.WARNING,
            timestamp=now,
            hive_ids=["h1", "h4"],
            dismissed=False,
        ),
        Alert(
            id="a2",
            type=AlertType.TREATMENT_DUE,
            title="Mite treatment due",
            message="For Hive 02",
            severity=AlertSeverity.WARNING,
            timestamp=now,
            hive_ids=["h2"],
            dismissed=False,
        ),
        Alert(
            id="a3",
            type=AlertType.WEATHER_WARNING,
            title="High humidity overnight",
            message="Ensure hive ventilation",
            severity=AlertSeverity.INFO,
            timestamp=now,
            hive_ids=[],
            dismissed=False,
        ),
    ]
    await Alert.insert_many(alerts)

    recommendations = [
        Recommendation(
            id="r1",
            hive_id="h1",
            type=RecommendationType.POSITIVE,
            title="Hive is Thriving",
            description="Excellent honey stores and strong population. Consider adding a super soon to prevent swarming.",
            priority=Priority.LOW,
        ),
        Recommendation(
            id="r2",
            hive_id="h1",
            type=RecommendationType.WARNING,
            title="Monitor Varroa Mites",
            description="Your last mite count was 3 weeks ago. It's recommended to perform a new count within the next 7 days.",
            priority=Priority.MEDIUM,
        ),
        Recommendation(
            id="r3",
            hive_id="h4",
            type=RecommendationType.ACTION_REQUIRED,
            title="Queen Issue Detected",
            description="No laying pattern observed. Consider requeening within 2 weeks.",
            priority=Priority.HIGH,
        ),
    ]
    await Recommendation.insert_many(recommendations)

    print("Database seeded successfully!")
