"""FastAPI auth dependencies. Ported from beekeeper api/app/routers/auth.py
(the get_current_user / get_current_user_optional deps).

Canonical import path for all verticals and core modules:
    from assistive_core import get_current_user, get_current_user_optional
"""
from fastapi import Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer

from . import service as auth_service
from .models import User

oauth2_scheme = OAuth2PasswordBearer(tokenUrl="/api/auth/login", auto_error=False)


async def get_current_user(token: str = Depends(oauth2_scheme)) -> User:
    """Get the current authenticated user from the token (resolved against the
    shared identity DB)."""
    if not token:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Not authenticated",
            headers={"WWW-Authenticate": "Bearer"},
        )

    email = auth_service.verify_token(token)
    if email is None:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid authentication credentials",
            headers={"WWW-Authenticate": "Bearer"},
        )

    user = await auth_service.get_user_by_email(email)
    if user is None:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="User not found",
            headers={"WWW-Authenticate": "Bearer"},
        )

    return user


async def get_current_user_optional(token: str = Depends(oauth2_scheme)) -> User | None:
    """Get the current user if authenticated, otherwise return None."""
    if not token:
        return None
    email = auth_service.verify_token(token)
    if email is None:
        return None
    return await auth_service.get_user_by_email(email)
