from sqlalchemy.orm import Session
from typing import List, Optional
from fastapi import HTTPException, status
from datetime import datetime

from app.models import Task, TaskStatus
from app.repositories import TaskRepository
from app.schemas import TaskCreate, TaskUpdate, TaskResponse


class TaskService:
    def __init__(self, db: Session):
        self.repository = TaskRepository(db)

    def get_all_tasks(self, user_id: str) -> List[TaskResponse]:
        """Get all tasks for a user"""
        tasks = self.repository.get_by_user_id(user_id)
        return [TaskResponse.model_validate(task) for task in tasks]

    def get_task(self, task_id: str, user_id: str) -> TaskResponse:
        """Get a specific task by ID"""
        task = self.repository.get_by_id(task_id)
        if not task:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND, detail="Task not found"
            )

        # Verify the task belongs to the user
        if task.user_id != user_id:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Not authorized to access this task",
            )

        return TaskResponse.model_validate(task)

    def get_tasks_by_status(
        self, user_id: str, task_status: TaskStatus
    ) -> List[TaskResponse]:
        """Get all tasks for a user with a specific status"""
        tasks = self.repository.get_by_status(user_id, task_status)
        return [TaskResponse.model_validate(task) for task in tasks]

    def get_pending_tasks(self, user_id: str) -> List[TaskResponse]:
        """Get all pending and in-progress tasks for a user"""
        tasks = self.repository.get_pending_and_overdue(user_id)
        return [TaskResponse.model_validate(task) for task in tasks]

    def get_upcoming_tasks(self, user_id: str, days: int = 7) -> List[TaskResponse]:
        """Get upcoming tasks in the next X days"""
        tasks = self.repository.get_upcoming(user_id, days)
        return [TaskResponse.model_validate(task) for task in tasks]

    def get_overdue_tasks(self, user_id: str) -> List[TaskResponse]:
        """Get all overdue tasks for a user"""
        tasks = self.repository.get_overdue(user_id)
        return [TaskResponse.model_validate(task) for task in tasks]

    def get_tasks_by_hive(self, hive_id: str, user_id: str) -> List[TaskResponse]:
        """Get all tasks for a specific hive"""
        tasks = self.repository.get_by_hive_id(hive_id)
        # Filter by user_id to ensure user can only see their own tasks
        user_tasks = [task for task in tasks if task.user_id == user_id]
        return [TaskResponse.model_validate(task) for task in user_tasks]

    def get_tasks_by_apiary(self, apiary_id: str, user_id: str) -> List[TaskResponse]:
        """Get all tasks for a specific apiary"""
        tasks = self.repository.get_by_apiary_id(apiary_id)
        # Filter by user_id to ensure user can only see their own tasks
        user_tasks = [task for task in tasks if task.user_id == user_id]
        return [TaskResponse.model_validate(task) for task in user_tasks]

    def create_task(
        self, task_data: TaskCreate, task_id: str, user_id: str
    ) -> TaskResponse:
        """Create a new task"""
        task = Task(
            id=task_id,
            user_id=user_id,
            status=TaskStatus.PENDING,
            **task_data.model_dump()
        )
        created_task = self.repository.create(task)
        return TaskResponse.model_validate(created_task)

    def update_task(
        self, task_id: str, task_data: TaskUpdate, user_id: str
    ) -> TaskResponse:
        """Update an existing task"""
        task = self.repository.get_by_id(task_id)
        if not task:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND, detail="Task not found"
            )

        # Verify the task belongs to the user
        if task.user_id != user_id:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Not authorized to modify this task",
            )

        update_data = task_data.model_dump(exclude_unset=True)
        for key, value in update_data.items():
            setattr(task, key, value)

        updated_task = self.repository.update(task)
        return TaskResponse.model_validate(updated_task)

    def complete_task(self, task_id: str, user_id: str) -> TaskResponse:
        """Mark a task as completed"""
        task = self.repository.get_by_id(task_id)
        if not task:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND, detail="Task not found"
            )

        # Verify the task belongs to the user
        if task.user_id != user_id:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Not authorized to modify this task",
            )

        completed_task = self.repository.mark_as_completed(task)
        return TaskResponse.model_validate(completed_task)

    def delete_task(self, task_id: str, user_id: str) -> None:
        """Delete a task"""
        task = self.repository.get_by_id(task_id)
        if not task:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND, detail="Task not found"
            )

        # Verify the task belongs to the user
        if task.user_id != user_id:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Not authorized to delete this task",
            )

        self.repository.delete(task)

    def mark_overdue_tasks(self, user_id: str) -> int:
        """Mark all past-due pending tasks as overdue"""
        return self.repository.mark_overdue_tasks(user_id)
