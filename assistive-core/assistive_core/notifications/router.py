"""Notification router.
Ported from beekeeper api/app/routers/notification.py."""
from typing import List

from fastapi import APIRouter, Depends, status

from ..auth import User, get_current_user
from .schemas import NotificationResponse
from .service import NotificationService

router = APIRouter(prefix="/notifications", tags=["notifications"])


@router.get("", response_model=List[NotificationResponse])
async def get_notifications(current_user: User = Depends(get_current_user)):
    """List the current user's notifications, newest first."""
    return await NotificationService().list_for_user(current_user.id)


@router.post("/{notification_id}/read", response_model=NotificationResponse)
async def mark_notification_read(
    notification_id: str,
    current_user: User = Depends(get_current_user),
):
    """Mark a single notification as read."""
    return await NotificationService().mark_read(notification_id, current_user.id)


@router.post("/read-all", status_code=status.HTTP_204_NO_CONTENT)
async def mark_all_read(current_user: User = Depends(get_current_user)):
    """Mark all of the current user's notifications as read."""
    await NotificationService().mark_all_read(current_user.id)
