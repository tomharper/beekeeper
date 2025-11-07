from pydantic import BaseModel, ConfigDict
from datetime import datetime
from typing import Optional
from app.models import TaskType, TaskStatus, TaskPriority, RecurrenceFrequency


def to_camel(string: str) -> str:
    """Convert snake_case to camelCase"""
    words = string.split('_')
    return words[0] + ''.join(word.capitalize() for word in words[1:])


class RecurrenceData(BaseModel):
    """Recurrence pattern data"""
    frequency: RecurrenceFrequency
    interval: int = 1
    end_date: Optional[datetime] = None
    count: Optional[int] = None

    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)


class TaskBase(BaseModel):
    title: str
    description: str = ""
    task_type: TaskType = TaskType.GENERAL
    due_date: datetime
    reminder_date: Optional[datetime] = None
    hive_id: Optional[str] = None
    apiary_id: Optional[str] = None
    priority: TaskPriority = TaskPriority.MEDIUM
    estimated_duration_minutes: Optional[int] = None
    weather_dependent: bool = False
    minimum_temperature: Optional[float] = None
    notes: str = ""

    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)


class TaskCreate(TaskBase):
    """Schema for creating a new task"""
    recurrence_frequency: Optional[RecurrenceFrequency] = None
    recurrence_interval: Optional[int] = 1
    recurrence_end_date: Optional[datetime] = None
    recurrence_count: Optional[int] = None


class TaskUpdate(BaseModel):
    """Schema for updating a task"""
    title: Optional[str] = None
    description: Optional[str] = None
    task_type: Optional[TaskType] = None
    due_date: Optional[datetime] = None
    reminder_date: Optional[datetime] = None
    hive_id: Optional[str] = None
    apiary_id: Optional[str] = None
    status: Optional[TaskStatus] = None
    priority: Optional[TaskPriority] = None
    completed_date: Optional[datetime] = None
    estimated_duration_minutes: Optional[int] = None
    weather_dependent: Optional[bool] = None
    minimum_temperature: Optional[float] = None
    notes: Optional[str] = None
    recurrence_frequency: Optional[RecurrenceFrequency] = None
    recurrence_interval: Optional[int] = None
    recurrence_end_date: Optional[datetime] = None
    recurrence_count: Optional[int] = None

    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)


class TaskResponse(TaskBase):
    """Schema for task responses"""
    id: str
    status: TaskStatus
    completed_date: Optional[datetime] = None
    recurrence_frequency: Optional[RecurrenceFrequency] = None
    recurrence_interval: Optional[int] = None
    recurrence_end_date: Optional[datetime] = None
    recurrence_count: Optional[int] = None
    user_id: str
    created_at: datetime
    updated_at: datetime

    model_config = ConfigDict(from_attributes=True, alias_generator=to_camel, populate_by_name=True)
