"""Environment configuration for assistive-core.

A single ``settings`` instance is exported; submodules import it and read
attributes. Values are read from the environment at import time (process env),
mirroring beekeeper's ``os.getenv`` approach but centralised.
"""
import os

# Sentinel default for JWT_SECRET_KEY. init_core() fails fast on this value when
# ENV=production, since it signs SSO tokens shared across every assistive vertical.
JWT_SECRET_PLACEHOLDER = "your-secret-key-here-change-in-production"


class Settings:
    # --- Mongo / identity ---
    # One Mongo deployment; identity DB is shared across all verticals (SSO),
    # everything else lives in the per-vertical DB.
    MONGODB_URI: str = os.getenv("MONGODB_URI", "mongodb://localhost:27017")
    IDENTITY_DB: str = os.getenv("IDENTITY_DB", "assistive_identity")
    MONGODB_DB: str = os.getenv("MONGODB_DB", "beekeeper")

    # --- JWT ---
    JWT_SECRET_KEY: str = os.getenv("JWT_SECRET_KEY", JWT_SECRET_PLACEHOLDER)
    JWT_ALGORITHM: str = os.getenv("JWT_ALGORITHM", "HS256")
    ACCESS_TOKEN_EXPIRE_MINUTES: int = int(
        os.getenv("ACCESS_TOKEN_EXPIRE_MINUTES", "30")
    )

    # Deployment environment; "production" makes init_core require a real JWT secret.
    ENV: str = os.getenv("ASSISTIVE_ENV", os.getenv("ENV", "dev"))

    # --- Bunny.net storage (photos/media) ---
    BUNNY_STORAGE_ZONE: str = os.getenv("BUNNY_STORAGE_ZONE", "")
    BUNNY_API_KEY: str = os.getenv("BUNNY_API_KEY", "")
    BUNNY_CDN_URL: str = os.getenv("BUNNY_CDN_URL", "")

    # --- External services (sibling VRUsafety repo) ---
    WEATHER_SERVICE_URL: str = os.getenv("WEATHER_SERVICE_URL", "")


settings = Settings()
