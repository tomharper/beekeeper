from fastapi import APIRouter, Depends, status, Query
from sqlalchemy.orm import Session
from typing import List, Optional

from app.database import get_db
from app.services import TaskService
from app.schemas import TaskCreate, TaskUpdate, TaskResponse
from app.models import TaskStatus, User
from app.routers.auth import get_current_user

router = APIRouter(prefix="/tasks", tags=["tasks"])


@router.get("", response_model=List[TaskResponse])
def get_tasks(
    task_status: Optional[TaskStatus] = Query(None, description="Filter by task status"),
    hive_id: Optional[str] = Query(None, description="Filter by hive ID"),
    apiary_id: Optional[str] = Query(None, description="Filter by apiary ID"),
    upcoming_days: Optional[int] = Query(
        None, description="Get tasks due in next X days"
    ),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """
    Get tasks with optional filters:
    - task_status: Filter by status (PENDING, IN_PROGRESS, COMPLETED, CANCELLED, OVERDUE)
    - hive_id: Get tasks for a specific hive
    - apiary_id: Get tasks for a specific apiary
    - upcoming_days: Get tasks due in the next X days
    """
    service = TaskService(db)

    if hive_id:
        return service.get_tasks_by_hive(hive_id, current_user.id)
    elif apiary_id:
        return service.get_tasks_by_apiary(apiary_id, current_user.id)
    elif task_status:
        return service.get_tasks_by_status(current_user.id, task_status)
    elif upcoming_days is not None:
        return service.get_upcoming_tasks(current_user.id, upcoming_days)
    else:
        return service.get_all_tasks(current_user.id)


@router.get("/pending", response_model=List[TaskResponse])
def get_pending_tasks(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """Get all pending and in-progress tasks"""
    service = TaskService(db)
    return service.get_pending_tasks(current_user.id)


@router.get("/overdue", response_model=List[TaskResponse])
def get_overdue_tasks(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """Get all overdue tasks"""
    service = TaskService(db)
    return service.get_overdue_tasks(current_user.id)


@router.get("/{task_id}", response_model=TaskResponse)
def get_task(
    task_id: str,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """Get a specific task by ID"""
    service = TaskService(db)
    return service.get_task(task_id, current_user.id)


@router.post("", response_model=TaskResponse, status_code=status.HTTP_201_CREATED)
def create_task(
    task: TaskCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """Create a new task"""
    import uuid

    task_id = str(uuid.uuid4())
    service = TaskService(db)
    return service.create_task(task, task_id, current_user.id)


@router.put("/{task_id}", response_model=TaskResponse)
def update_task(
    task_id: str,
    task: TaskUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """Update an existing task"""
    service = TaskService(db)
    return service.update_task(task_id, task, current_user.id)


@router.post("/{task_id}/complete", response_model=TaskResponse)
def complete_task(
    task_id: str,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """Mark a task as completed"""
    service = TaskService(db)
    return service.complete_task(task_id, current_user.id)


@router.delete("/{task_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_task(
    task_id: str,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """Delete a task"""
    service = TaskService(db)
    service.delete_task(task_id, current_user.id)


@router.post("/mark-overdue", status_code=status.HTTP_200_OK)
def mark_overdue(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """Mark all past-due pending tasks as overdue"""
    service = TaskService(db)
    count = service.mark_overdue_tasks(current_user.id)
    return {"message": f"Marked {count} tasks as overdue"}
