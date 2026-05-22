from typing import List
from fastapi import HTTPException, status

from app.models import Task, TaskStatus
from app.repositories import TaskRepository
from app.schemas import TaskCreate, TaskUpdate, TaskResponse


class TaskService:
    def __init__(self):
        self.repository = TaskRepository()

    async def get_all_tasks(self, user_id: str) -> List[TaskResponse]:
        tasks = await self.repository.get_by_user_id(user_id)
        return [TaskResponse.model_validate(task) for task in tasks]

    async def get_task(self, task_id: str, user_id: str) -> TaskResponse:
        task = await self.repository.get_by_id(task_id)
        if not task:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND, detail="Task not found"
            )
        if task.user_id != user_id:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Not authorized to access this task",
            )
        return TaskResponse.model_validate(task)

    async def get_tasks_by_status(
        self, user_id: str, task_status: TaskStatus
    ) -> List[TaskResponse]:
        tasks = await self.repository.get_by_status(user_id, task_status)
        return [TaskResponse.model_validate(task) for task in tasks]

    async def get_pending_tasks(self, user_id: str) -> List[TaskResponse]:
        tasks = await self.repository.get_pending_and_overdue(user_id)
        return [TaskResponse.model_validate(task) for task in tasks]

    async def get_upcoming_tasks(self, user_id: str, days: int = 7) -> List[TaskResponse]:
        tasks = await self.repository.get_upcoming(user_id, days)
        return [TaskResponse.model_validate(task) for task in tasks]

    async def get_overdue_tasks(self, user_id: str) -> List[TaskResponse]:
        tasks = await self.repository.get_overdue(user_id)
        return [TaskResponse.model_validate(task) for task in tasks]

    async def get_tasks_by_hive(self, hive_id: str, user_id: str) -> List[TaskResponse]:
        tasks = await self.repository.get_by_hive_id(hive_id)
        user_tasks = [task for task in tasks if task.user_id == user_id]
        return [TaskResponse.model_validate(task) for task in user_tasks]

    async def get_tasks_by_apiary(self, apiary_id: str, user_id: str) -> List[TaskResponse]:
        tasks = await self.repository.get_by_apiary_id(apiary_id)
        user_tasks = [task for task in tasks if task.user_id == user_id]
        return [TaskResponse.model_validate(task) for task in user_tasks]

    async def create_task(
        self, task_data: TaskCreate, task_id: str, user_id: str
    ) -> TaskResponse:
        task = Task(
            id=task_id,
            user_id=user_id,
            status=TaskStatus.PENDING,
            **task_data.model_dump(),
        )
        created_task = await self.repository.create(task)
        return TaskResponse.model_validate(created_task)

    async def update_task(
        self, task_id: str, task_data: TaskUpdate, user_id: str
    ) -> TaskResponse:
        task = await self.repository.get_by_id(task_id)
        if not task:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND, detail="Task not found"
            )
        if task.user_id != user_id:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Not authorized to modify this task",
            )

        update_data = task_data.model_dump(exclude_unset=True)
        for key, value in update_data.items():
            setattr(task, key, value)

        updated_task = await self.repository.update(task)
        return TaskResponse.model_validate(updated_task)

    async def complete_task(self, task_id: str, user_id: str) -> TaskResponse:
        task = await self.repository.get_by_id(task_id)
        if not task:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND, detail="Task not found"
            )
        if task.user_id != user_id:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Not authorized to modify this task",
            )
        completed_task = await self.repository.mark_as_completed(task)
        return TaskResponse.model_validate(completed_task)

    async def delete_task(self, task_id: str, user_id: str) -> None:
        task = await self.repository.get_by_id(task_id)
        if not task:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND, detail="Task not found"
            )
        if task.user_id != user_id:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Not authorized to delete this task",
            )
        await self.repository.delete(task)

    async def mark_overdue_tasks(self, user_id: str) -> int:
        return await self.repository.mark_overdue_tasks(user_id)
