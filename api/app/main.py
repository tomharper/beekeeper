from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from contextlib import asynccontextmanager
import os

from app.database import init_db, get_db
from app.seed_data import seed_database
from app.routers import (
    apiaries_router,
    hives_router,
    alerts_router,
    recommendations_router,
    weather_router,
)


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Lifecycle manager for the application"""
    # Startup
    print("Starting up...")
    init_db()

    # Seed database with initial data
    db = next(get_db())
    try:
        seed_database(db)
    finally:
        db.close()

    yield

    # Shutdown
    print("Shutting down...")


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

# Include routers
app.include_router(apiaries_router, prefix="/api")
app.include_router(hives_router, prefix="/api")
app.include_router(alerts_router, prefix="/api")
app.include_router(recommendations_router, prefix="/api")
app.include_router(weather_router, prefix="/api")


@app.get("/")
def root():
    """Root endpoint"""
    return {"message": "Welcome to Beekeeper API", "version": "1.0.0"}


@app.get("/health")
def health():
    """Health check endpoint"""
    return {"status": "healthy"}
