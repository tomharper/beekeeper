from sqlalchemy.orm import Session
from typing import List
from datetime import datetime, timedelta
import uuid

from app.models import Alert, AlertType, AlertSeverity, Task, Inspection, TaskStatus, HealthStatus
from app.repositories import AlertRepository, TaskRepository, InspectionRepository
from app.schemas import AlertResponse


class AIAdvisorService:
    """
    Service for generating intelligent alerts and recommendations
    based on hive inspection data, task status, and beekeeping best practices.
    """

    def __init__(self, db: Session):
        self.db = db
        self.alert_repo = AlertRepository(db)
        self.task_repo = TaskRepository(db)
        self.inspection_repo = InspectionRepository(db)

    def generate_alerts(self) -> List[AlertResponse]:
        """
        Analyze hive data and generate actionable alerts.
        Returns both existing active alerts and newly generated ones.
        """
        alerts = []

        # Get existing active alerts
        existing_alerts = self.alert_repo.get_active()
        for alert in existing_alerts:
            alerts.append(self._to_response(alert))

        # Generate new alerts based on data analysis
        alerts.extend(self._check_overdue_tasks())
        alerts.extend(self._check_inspection_issues())
        alerts.extend(self._check_overdue_inspections())
        alerts.extend(self._check_seasonal_reminders())

        return alerts

    def _check_overdue_tasks(self) -> List[AlertResponse]:
        """Check for overdue tasks"""
        alerts = []
        now = datetime.now()

        # Get all pending tasks
        tasks = self.task_repo.get_all()
        overdue_tasks = [
            t
            for t in tasks
            if t.status == TaskStatus.PENDING and t.due_date < now
        ]

        if overdue_tasks:
            # Group by hive
            hive_groups = {}
            for task in overdue_tasks:
                hive_id = task.hive_id or "general"
                if hive_id not in hive_groups:
                    hive_groups[hive_id] = []
                hive_groups[hive_id].append(task)

            for hive_id, tasks in hive_groups.items():
                days_overdue = (now - min(t.due_date for t in tasks)).days
                hive_ids = [hive_id] if hive_id != "general" else None

                alert = self._create_alert(
                    alert_type=AlertType.TASK,
                    title=f"{len(tasks)} Overdue Task{'s' if len(tasks) > 1 else ''}",
                    message=f"You have {len(tasks)} overdue task(s). Oldest is {days_overdue} days overdue.",
                    severity=AlertSeverity.WARNING if days_overdue < 7 else AlertSeverity.CRITICAL,
                    hive_ids=hive_ids,
                )
                alerts.append(alert)

        return alerts

    def _check_inspection_issues(self) -> List[AlertResponse]:
        """Check recent inspections for health issues"""
        alerts = []

        # Get recent inspections (last 30 days)
        inspections = self.inspection_repo.get_all()
        recent_inspections = [
            i
            for i in inspections
            if (datetime.now() - i.inspection_date).days <= 30
        ]

        for inspection in recent_inspections:
            # Check for varroa mites
            if inspection.varroa_mites_detected:
                alert = self._create_alert(
                    alert_type=AlertType.PEST,
                    title="Varroa Mites Detected",
                    message=f"Varroa mites detected in Hive {inspection.hive_id}. Consider treatment options.",
                    severity=AlertSeverity.CRITICAL,
                    hive_ids=[inspection.hive_id],
                )
                alerts.append(alert)

            # Check for diseases
            if inspection.disease_detected:
                alert = self._create_alert(
                    alert_type=AlertType.DISEASE,
                    title="Disease Detected",
                    message=f"Disease detected in Hive {inspection.hive_id}: {inspection.disease_description or 'Unknown disease'}",
                    severity=AlertSeverity.CRITICAL,
                    hive_ids=[inspection.hive_id],
                )
                alerts.append(alert)

            # Check for queen issues
            if not inspection.queen_seen and (datetime.now() - inspection.inspection_date).days >= 14:
                alert = self._create_alert(
                    alert_type=AlertType.HIVE_HEALTH,
                    title="Queen Not Seen",
                    message=f"Queen not seen in Hive {inspection.hive_id} for over 2 weeks. Verify queen presence.",
                    severity=AlertSeverity.WARNING,
                    hive_ids=[inspection.hive_id],
                )
                alerts.append(alert)

            # Check for poor health status
            if inspection.health_status in [HealthStatus.CONCERNING, HealthStatus.CRITICAL]:
                alert = self._create_alert(
                    alert_type=AlertType.HIVE_HEALTH,
                    title=f"Hive Health: {inspection.health_status.value}",
                    message=f"Hive {inspection.hive_id} requires attention. Inspect and address issues promptly.",
                    severity=AlertSeverity.CRITICAL if inspection.health_status == HealthStatus.CRITICAL else AlertSeverity.WARNING,
                    hive_ids=[inspection.hive_id],
                )
                alerts.append(alert)

            # Check for low resources
            if inspection.honey_stores.value == "EMPTY" or inspection.honey_stores.value == "LOW":
                alert = self._create_alert(
                    alert_type=AlertType.HIVE_HEALTH,
                    title="Low Honey Stores",
                    message=f"Hive {inspection.hive_id} has low honey stores. Consider feeding.",
                    severity=AlertSeverity.WARNING,
                    hive_ids=[inspection.hive_id],
                )
                alerts.append(alert)

        return alerts

    def _check_overdue_inspections(self) -> List[AlertResponse]:
        """Check for hives that need inspection"""
        alerts = []

        # Simplified: Just check if there are any hives at all
        # In production, you'd query hives and check their last inspection date
        inspections = self.inspection_repo.get_all()
        if inspections:
            # Group by hive and find last inspection
            hive_last_inspection = {}
            for insp in inspections:
                if insp.hive_id not in hive_last_inspection:
                    hive_last_inspection[insp.hive_id] = insp.inspection_date
                else:
                    if insp.inspection_date > hive_last_inspection[insp.hive_id]:
                        hive_last_inspection[insp.hive_id] = insp.inspection_date

            # Check for hives overdue for inspection (>14 days)
            now = datetime.now()
            for hive_id, last_date in hive_last_inspection.items():
                days_since = (now - last_date).days
                if days_since > 14:
                    alert = self._create_alert(
                        alert_type=AlertType.HIVE_HEALTH,
                        title="Inspection Overdue",
                        message=f"Hive {hive_id} hasn't been inspected in {days_since} days. Schedule an inspection.",
                        severity=AlertSeverity.WARNING if days_since < 30 else AlertSeverity.CRITICAL,
                        hive_ids=[hive_id],
                    )
                    alerts.append(alert)

        return alerts

    def _check_seasonal_reminders(self) -> List[AlertResponse]:
        """Generate seasonal beekeeping reminders"""
        alerts = []
        now = datetime.now()
        month = now.month

        # Spring (March-May): Swarm season
        if month in [3, 4, 5]:
            alert = self._create_alert(
                alert_type=AlertType.SEASONAL,
                title="Swarm Season Alert",
                message="Spring swarm season is here. Check for queen cells and add supers if needed.",
                severity=AlertSeverity.INFO,
            )
            alerts.append(alert)

        # Early Spring (March): Spring inspection
        if month == 3:
            alert = self._create_alert(
                alert_type=AlertType.SEASONAL,
                title="Spring Inspection Time",
                message="Perform spring inspections to assess winter survival and colony strength.",
                severity=AlertSeverity.INFO,
            )
            alerts.append(alert)

        # Fall (September-October): Winter prep
        if month in [9, 10]:
            alert = self._create_alert(
                alert_type=AlertType.SEASONAL,
                title="Winter Preparation",
                message="Prepare hives for winter: check food stores, reduce entrances, and treat for mites.",
                severity=AlertSeverity.WARNING,
            )
            alerts.append(alert)

        # Summer (July-August): Nectar flow / varroa treatment
        if month in [7, 8]:
            alert = self._create_alert(
                alert_type=AlertType.PEST,
                title="Varroa Treatment Window",
                message="Mid-summer is ideal for varroa mite treatment before fall buildup.",
                severity=AlertSeverity.INFO,
            )
            alerts.append(alert)

        return alerts

    def _create_alert(
        self,
        alert_type: AlertType,
        title: str,
        message: str,
        severity: AlertSeverity,
        hive_ids: List[str] = None,
    ) -> AlertResponse:
        """Create and save a new alert"""
        alert_id = str(uuid.uuid4())
        hive_ids_str = ",".join(hive_ids) if hive_ids else None

        alert = Alert(
            id=alert_id,
            type=alert_type,
            title=title,
            message=message,
            severity=severity,
            timestamp=datetime.now(),
            hive_ids=hive_ids_str,
            dismissed=False,
        )

        # Save to database
        created_alert = self.alert_repo.create(alert)
        return self._to_response(created_alert)

    def _to_response(self, alert: Alert) -> AlertResponse:
        """Convert Alert model to AlertResponse schema"""
        hive_ids = alert.hive_ids.split(",") if alert.hive_ids else None
        return AlertResponse(
            id=alert.id,
            type=alert.type,
            title=alert.title,
            message=alert.message,
            severity=alert.severity,
            timestamp=alert.timestamp,
            hive_ids=hive_ids,
            dismissed=alert.dismissed,
        )
