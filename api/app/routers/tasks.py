from fastapi import APIRouter, Depends, status, Query
from typing import List, Optional

from app.services import TaskService
from app.schemas import TaskCreate, TaskUpdate, TaskResponse
from app.models import TaskStatus
from assistive_core import User, get_current_user

router = APIRouter(prefix="/tasks", tags=["tasks"])


@router.get("", response_model=List[TaskResponse])
async def get_tasks(
    task_status: Optional[TaskStatus] = Query(None, description="Filter by task status"),
    hive_id: Optional[str] = Query(None, description="Filter by hive ID"),
    apiary_id: Optional[str] = Query(None, description="Filter by apiary ID"),
    upcoming_days: Optional[int] = Query(None, description="Get tasks due in next X days"),
    current_user: User = Depends(get_current_user),
):
    service = TaskService()
    if hive_id:
        return await service.get_tasks_by_hive(hive_id, current_user.id)
    elif apiary_id:
        return await service.get_tasks_by_apiary(apiary_id, current_user.id)
    elif task_status:
        return await service.get_tasks_by_status(current_user.id, task_status)
    elif upcoming_days is not None:
        return await service.get_upcoming_tasks(current_user.id, upcoming_days)
    else:
        return await service.get_all_tasks(current_user.id)


@router.get("/pending", response_model=List[TaskResponse])
async def get_pending_tasks(current_user: User = Depends(get_current_user)):
    service = TaskService()
    return await service.get_pending_tasks(current_user.id)


@router.get("/overdue", response_model=List[TaskResponse])
async def get_overdue_tasks(current_user: User = Depends(get_current_user)):
    service = TaskService()
    return await service.get_overdue_tasks(current_user.id)


@router.get("/{task_id}", response_model=TaskResponse)
async def get_task(task_id: str, current_user: User = Depends(get_current_user)):
    service = TaskService()
    return await service.get_task(task_id, current_user.id)


@router.post("", response_model=TaskResponse, status_code=status.HTTP_201_CREATED)
async def create_task(task: TaskCreate, current_user: User = Depends(get_current_user)):
    import uuid

    task_id = str(uuid.uuid4())
    service = TaskService()
    return await service.create_task(task, task_id, current_user.id)


@router.put("/{task_id}", response_model=TaskResponse)
async def update_task(
    task_id: str,
    task: TaskUpdate,
    current_user: User = Depends(get_current_user),
):
    service = TaskService()
    return await service.update_task(task_id, task, current_user.id)


@router.post("/{task_id}/complete", response_model=TaskResponse)
async def complete_task(task_id: str, current_user: User = Depends(get_current_user)):
    service = TaskService()
    return await service.complete_task(task_id, current_user.id)


@router.delete("/{task_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_task(task_id: str, current_user: User = Depends(get_current_user)):
    service = TaskService()
    await service.delete_task(task_id, current_user.id)


@router.post("/mark-overdue", status_code=status.HTTP_200_OK)
async def mark_overdue(current_user: User = Depends(get_current_user)):
    service = TaskService()
    count = await service.mark_overdue_tasks(current_user.id)
    return {"message": f"Marked {count} tasks as overdue"}
