from sqlalchemy.orm import Session
from sqlalchemy import and_, or_
from typing import List, Optional
from datetime import datetime
from app.models import Task, TaskStatus


class TaskRepository:
    def __init__(self, db: Session):
        self.db = db

    def get_all(self) -> List[Task]:
        return self.db.query(Task).all()

    def get_by_id(self, task_id: str) -> Optional[Task]:
        return self.db.query(Task).filter(Task.id == task_id).first()

    def get_by_user_id(self, user_id: str) -> List[Task]:
        """Get all tasks for a specific user"""
        return self.db.query(Task).filter(Task.user_id == user_id).all()

    def get_by_hive_id(self, hive_id: str) -> List[Task]:
        """Get all tasks for a specific hive"""
        return self.db.query(Task).filter(Task.hive_id == hive_id).all()

    def get_by_apiary_id(self, apiary_id: str) -> List[Task]:
        """Get all tasks for a specific apiary"""
        return self.db.query(Task).filter(Task.apiary_id == apiary_id).all()

    def get_by_status(self, user_id: str, status: TaskStatus) -> List[Task]:
        """Get all tasks for a user with a specific status"""
        return (
            self.db.query(Task)
            .filter(and_(Task.user_id == user_id, Task.status == status))
            .all()
        )

    def get_pending_and_overdue(self, user_id: str) -> List[Task]:
        """Get all pending and overdue tasks for a user"""
        return (
            self.db.query(Task)
            .filter(
                and_(
                    Task.user_id == user_id,
                    or_(
                        Task.status == TaskStatus.PENDING,
                        Task.status == TaskStatus.OVERDUE,
                        Task.status == TaskStatus.IN_PROGRESS,
                    ),
                )
            )
            .order_by(Task.due_date)
            .all()
        )

    def get_upcoming(self, user_id: str, days: int = 7) -> List[Task]:
        """Get upcoming tasks in the next X days"""
        from datetime import timedelta

        end_date = datetime.utcnow() + timedelta(days=days)
        return (
            self.db.query(Task)
            .filter(
                and_(
                    Task.user_id == user_id,
                    Task.due_date <= end_date,
                    or_(
                        Task.status == TaskStatus.PENDING,
                        Task.status == TaskStatus.IN_PROGRESS,
                    ),
                )
            )
            .order_by(Task.due_date)
            .all()
        )

    def get_overdue(self, user_id: str) -> List[Task]:
        """Get all overdue tasks for a user"""
        now = datetime.utcnow()
        return (
            self.db.query(Task)
            .filter(
                and_(
                    Task.user_id == user_id,
                    Task.due_date < now,
                    Task.status.in_([TaskStatus.PENDING, TaskStatus.IN_PROGRESS]),
                )
            )
            .order_by(Task.due_date)
            .all()
        )

    def create(self, task: Task) -> Task:
        self.db.add(task)
        self.db.commit()
        self.db.refresh(task)
        return task

    def update(self, task: Task) -> Task:
        self.db.commit()
        self.db.refresh(task)
        return task

    def delete(self, task: Task) -> None:
        self.db.delete(task)
        self.db.commit()

    def mark_as_completed(self, task: Task) -> Task:
        """Mark a task as completed with the completion timestamp"""
        task.status = TaskStatus.COMPLETED
        task.completed_date = datetime.utcnow()
        return self.update(task)

    def mark_overdue_tasks(self, user_id: str) -> int:
        """Mark all past-due pending tasks as overdue"""
        now = datetime.utcnow()
        result = (
            self.db.query(Task)
            .filter(
                and_(
                    Task.user_id == user_id,
                    Task.due_date < now,
                    Task.status == TaskStatus.PENDING,
                )
            )
            .update({Task.status: TaskStatus.OVERDUE})
        )
        self.db.commit()
        return result
