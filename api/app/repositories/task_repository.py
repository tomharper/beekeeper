from typing import List, Optional
from datetime import datetime, timedelta, timezone

from beanie.operators import In, Or, Set

from app.models import Task, TaskStatus


def _utcnow() -> datetime:
    return datetime.now(timezone.utc)


class TaskRepository:
    async def get_all(self) -> List[Task]:
        return await Task.find_all().to_list()

    async def get_by_id(self, task_id: str) -> Optional[Task]:
        return await Task.get(task_id)

    async def get_by_user_id(self, user_id: str) -> List[Task]:
        return await Task.find(Task.user_id == user_id).to_list()

    async def get_by_hive_id(self, hive_id: str) -> List[Task]:
        return await Task.find(Task.hive_id == hive_id).to_list()

    async def get_by_apiary_id(self, apiary_id: str) -> List[Task]:
        return await Task.find(Task.apiary_id == apiary_id).to_list()

    async def get_by_status(self, user_id: str, status: TaskStatus) -> List[Task]:
        return await Task.find(
            Task.user_id == user_id,
            Task.status == status,
        ).to_list()

    async def get_pending_and_overdue(self, user_id: str) -> List[Task]:
        return (
            await Task.find(
                Task.user_id == user_id,
                In(
                    Task.status,
                    [TaskStatus.PENDING, TaskStatus.OVERDUE, TaskStatus.IN_PROGRESS],
                ),
            )
            .sort(Task.due_date)
            .to_list()
        )

    async def get_upcoming(self, user_id: str, days: int = 7) -> List[Task]:
        end_date = _utcnow() + timedelta(days=days)
        return (
            await Task.find(
                Task.user_id == user_id,
                Task.due_date <= end_date,
                In(Task.status, [TaskStatus.PENDING, TaskStatus.IN_PROGRESS]),
            )
            .sort(Task.due_date)
            .to_list()
        )

    async def get_overdue(self, user_id: str) -> List[Task]:
        now = _utcnow()
        return (
            await Task.find(
                Task.user_id == user_id,
                Task.due_date < now,
                In(Task.status, [TaskStatus.PENDING, TaskStatus.IN_PROGRESS]),
            )
            .sort(Task.due_date)
            .to_list()
        )

    async def get_feed(
        self, user_ids: List[str], limit: int = 20, before: Optional[datetime] = None
    ) -> List[Task]:
        """Public tasks from the given users, newest first (for the follow feed)."""
        conditions = [In(Task.user_id, user_ids), Task.is_public == True]  # noqa: E712
        if before is not None:
            conditions.append(Task.due_date < before)
        return (
            await Task.find(*conditions)
            .sort(-Task.due_date)
            .limit(limit)
            .to_list()
        )

    async def create(self, task: Task) -> Task:
        await task.insert()
        return task

    async def update(self, task: Task) -> Task:
        await task.save()
        return task

    async def delete(self, task: Task) -> None:
        await task.delete()

    async def mark_as_completed(self, task: Task) -> Task:
        task.status = TaskStatus.COMPLETED
        task.completed_date = _utcnow()
        return await self.update(task)

    async def mark_overdue_tasks(self, user_id: str) -> int:
        """Mark all past-due pending tasks as overdue. Returns modified count."""
        now = _utcnow()
        result = await Task.find(
            Task.user_id == user_id,
            Task.due_date < now,
            Task.status == TaskStatus.PENDING,
        ).update(Set({Task.status: TaskStatus.OVERDUE}))
        return result.modified_count if result else 0
