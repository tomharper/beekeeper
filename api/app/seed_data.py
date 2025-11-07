from datetime import datetime, timedelta
from sqlalchemy.orm import Session

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


def seed_database(db: Session):
    """Seed the database with initial mock data"""

    # Check if data already exists
    existing_apiaries = db.query(Apiary).count()
    if existing_apiaries > 0:
        print("Database already seeded, skipping...")
        return

    print("Seeding database...")

    # Create Apiaries
    apiary1 = Apiary(
        id="1",
        name="Backyard Garden",
        location="Sunnyvale, CA",
        latitude=37.3688,
        longitude=-122.0363,
        status=ApiaryStatus.HEALTHY,
    )
    apiary2 = Apiary(
        id="2",
        name="Hillside Meadow",
        location="45.123, -122.456",
        latitude=45.123,
        longitude=-122.456,
        status=ApiaryStatus.WARNING,
    )
    apiary3 = Apiary(
        id="3",
        name="Riverbend Apiary",
        location="Cloverdale, OR",
        latitude=45.2271,
        longitude=-123.4023,
        status=ApiaryStatus.ALERT,
    )

    db.add_all([apiary1, apiary2, apiary3])

    # Create Hives for Backyard Garden
    hive1 = Hive(
        id="h1",
        name="Hive A-01",
        apiary_id="1",
        status=HiveStatus.STRONG,
        last_inspected=datetime.now() - timedelta(days=3),
        colony_strength=ColonyStrength.STRONG,
        queen_status=QueenStatus.LAYING,
        temperament=Temperament.CALM,
        honey_stores=HoneyStores.FULL,
    )
    hive2 = Hive(
        id="h2",
        name="Hive A-02",
        apiary_id="1",
        status=HiveStatus.NEEDS_INSPECTION,
        last_inspected=datetime.now() - timedelta(days=14),
        colony_strength=ColonyStrength.MODERATE,
        queen_status=QueenStatus.LAYING,
        temperament=Temperament.MODERATE,
        honey_stores=HoneyStores.ADEQUATE,
    )
    hive3 = Hive(
        id="h3",
        name="Hive B-01",
        apiary_id="1",
        status=HiveStatus.STRONG,
        last_inspected=datetime.now() - timedelta(days=5),
        colony_strength=ColonyStrength.STRONG,
        queen_status=QueenStatus.LAYING,
        temperament=Temperament.CALM,
        honey_stores=HoneyStores.FULL,
    )
    hive4 = Hive(
        id="h4",
        name="Hive B-02",
        apiary_id="1",
        status=HiveStatus.ALERT,
        last_inspected=datetime.now() - timedelta(days=2),
        colony_strength=ColonyStrength.WEAK,
        queen_status=QueenStatus.NOT_LAYING,
        temperament=Temperament.DEFENSIVE,
        honey_stores=HoneyStores.LOW,
    )

    # Create Hives for Hillside Meadow
    hive5 = Hive(
        id="h5",
        name="Hive 01",
        apiary_id="2",
        status=HiveStatus.STRONG,
        last_inspected=datetime.now() - timedelta(days=1),
        colony_strength=ColonyStrength.STRONG,
        queen_status=QueenStatus.LAYING,
        temperament=Temperament.CALM,
        honey_stores=HoneyStores.FULL,
    )
    hive6 = Hive(
        id="h6",
        name="Hive 02",
        apiary_id="2",
        status=HiveStatus.STRONG,
        last_inspected=datetime.now() - timedelta(days=2),
        colony_strength=ColonyStrength.STRONG,
        queen_status=QueenStatus.LAYING,
        temperament=Temperament.CALM,
        honey_stores=HoneyStores.ADEQUATE,
    )
    hive7 = Hive(
        id="h7",
        name="Hive 03",
        apiary_id="2",
        status=HiveStatus.NEEDS_INSPECTION,
        last_inspected=datetime.now() - timedelta(days=9),
        colony_strength=ColonyStrength.MODERATE,
        queen_status=QueenStatus.LAYING,
        temperament=Temperament.MODERATE,
        honey_stores=HoneyStores.ADEQUATE,
    )
    hive8 = Hive(
        id="h8",
        name="Hive 04",
        apiary_id="2",
        status=HiveStatus.ALERT,
        last_inspected=datetime.now() - timedelta(days=4),
        colony_strength=ColonyStrength.MODERATE,
        queen_status=QueenStatus.LAYING,
        temperament=Temperament.CALM,
        honey_stores=HoneyStores.ADEQUATE,
    )

    # Create Hive for Riverbend
    hive9 = Hive(
        id="h9",
        name="Hive 01",
        apiary_id="3",
        status=HiveStatus.WEAK,
        last_inspected=datetime.now() - timedelta(days=7),
        colony_strength=ColonyStrength.WEAK,
        queen_status=QueenStatus.MISSING,
        temperament=Temperament.DEFENSIVE,
        honey_stores=HoneyStores.LOW,
    )

    db.add_all([hive1, hive2, hive3, hive4, hive5, hive6, hive7, hive8, hive9])

    # Create Alerts
    alert1 = Alert(
        id="a1",
        type=AlertType.SWARM_WARNING,
        title="Swarm Warning",
        message="AI Alert: High Swarm Probability in this area. Check hives A-01 and C-04.",
        severity=AlertSeverity.WARNING,
        timestamp=datetime.now(),
        hive_ids="h1,h4",
        dismissed=False,
    )
    alert2 = Alert(
        id="a2",
        type=AlertType.TREATMENT_DUE,
        title="Mite treatment due",
        message="For Hive 02",
        severity=AlertSeverity.WARNING,
        timestamp=datetime.now(),
        hive_ids="h2",
        dismissed=False,
    )
    alert3 = Alert(
        id="a3",
        type=AlertType.WEATHER_WARNING,
        title="High humidity overnight",
        message="Ensure hive ventilation",
        severity=AlertSeverity.INFO,
        timestamp=datetime.now(),
        dismissed=False,
    )

    db.add_all([alert1, alert2, alert3])

    # Create Recommendations
    rec1 = Recommendation(
        id="r1",
        hive_id="h1",
        type=RecommendationType.POSITIVE,
        title="Hive is Thriving",
        description="Excellent honey stores and strong population. Consider adding a super soon to prevent swarming.",
        priority=Priority.LOW,
    )
    rec2 = Recommendation(
        id="r2",
        hive_id="h1",
        type=RecommendationType.WARNING,
        title="Monitor Varroa Mites",
        description="Your last mite count was 3 weeks ago. It's recommended to perform a new count within the next 7 days.",
        priority=Priority.MEDIUM,
    )
    rec3 = Recommendation(
        id="r3",
        hive_id="h4",
        type=RecommendationType.ACTION_REQUIRED,
        title="Queen Issue Detected",
        description="No laying pattern observed. Consider requeening within 2 weeks.",
        priority=Priority.HIGH,
    )

    db.add_all([rec1, rec2, rec3])

    db.commit()
    print("Database seeded successfully!")
