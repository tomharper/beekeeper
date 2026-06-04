from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from contextlib import asynccontextmanager
import os

from assistive_core import (
    init_core,
    close_core,
    auth_router,
    follow_router,
    feed_router,
    notification_router,
    event_router,
)

from app.models import DOMAIN_DOCUMENTS
from app.feed_sources import FEED_SOURCES
from app.seed_data import seed_database
from app.routers import (
    apiaries_router,
    hives_router,
    alerts_router,
    recommendations_router,
    weather_router,
    tasks_router,
    inspections_router,
    photos_router,
    chat_router,
)


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Lifecycle manager for the application"""
    print("Starting up...")
    # Wire both databases (shared identity + per-vertical) and register the
    # beekeeper feed sources via assistive-core.
    await init_core(
        vertical_documents=DOMAIN_DOCUMENTS,
        feed_sources=FEED_SOURCES,
    )
    await seed_database()
    yield
    print("Shutting down...")
    await close_core()


app = FastAPI(
    title="Beekeeper API",
    description="API for managing apiaries, hives, and beekeeping operations",
    version="1.0.0",
    lifespan=lifespan,
)

# Configure CORS
origins = os.getenv("CORS_ORIGINS", "http://localhost:2000").split(",")

app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Shared social substrate routers (auth/SSO, follow, feed, notifications, calendar).
app.include_router(auth_router, prefix="/api")
app.include_router(follow_router, prefix="/api")
app.include_router(feed_router, prefix="/api")
app.include_router(notification_router, prefix="/api")
app.include_router(event_router, prefix="/api")

# Beekeeper domain routers.
app.include_router(apiaries_router, prefix="/api")
app.include_router(hives_router, prefix="/api")
app.include_router(alerts_router, prefix="/api")
app.include_router(recommendations_router, prefix="/api")
app.include_router(weather_router, prefix="/api")
app.include_router(tasks_router, prefix="/api")
app.include_router(inspections_router, prefix="/api")
app.include_router(photos_router, prefix="/api")
app.include_router(chat_router, prefix="/api")


@app.get("/")
def root():
    return {"message": "Welcome to Beekeeper API", "version": "1.0.0"}


@app.get("/health")
def health():
    return {"status": "healthy"}
