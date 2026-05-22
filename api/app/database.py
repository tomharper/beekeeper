import os
from motor.motor_asyncio import AsyncIOMotorClient
from beanie import init_beanie


MONGODB_URI = os.getenv("MONGODB_URI", "mongodb://localhost:27017")
MONGODB_DB = os.getenv("MONGODB_DB", "beekeeper")

_client: AsyncIOMotorClient | None = None


def get_client() -> AsyncIOMotorClient:
    global _client
    if _client is None:
        _client = AsyncIOMotorClient(MONGODB_URI)
    return _client


async def init_db() -> None:
    """Initialise Beanie with all document models. Call from FastAPI lifespan."""
    from app.models import DOCUMENT_MODELS

    client = get_client()
    await init_beanie(database=client[MONGODB_DB], document_models=DOCUMENT_MODELS)


async def close_db() -> None:
    global _client
    if _client is not None:
        _client.close()
        _client = None
