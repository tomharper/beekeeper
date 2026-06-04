"""Auth subpackage: shared identity (SSO), JWT, FastAPI deps, and router."""
from . import service as auth_service
from .deps import get_current_user, get_current_user_optional
from .models import User
from .router import router as auth_router
from .schemas import Token, UserCreate, UserLogin, UserResponse

__all__ = [
    "User",
    "auth_service",
    "auth_router",
    "get_current_user",
    "get_current_user_optional",
    "Token",
    "UserCreate",
    "UserLogin",
    "UserResponse",
]
