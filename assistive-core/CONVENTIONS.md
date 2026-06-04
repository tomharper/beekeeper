# assistive-core conventions (for parallel module authors)

`assistive_core/__init__.py` already imports the FULL public API from every
submodule. **Do not edit `__init__.py`.** Each module author only creates files
inside their own subpackage and re-exports the required names from that
subpackage's `__init__.py` (the stub `try/except` re-exports are already wired).

General rules:
- Python >=3.12, Pydantic v2, Beanie/Motor.
- camelCase response aliasing via a local `to_camel` (matches beekeeper).
- Document `id: str` (UUID4 string), not ObjectId.
- Config comes from `assistive_core.settings.settings`, never `os.getenv` inline.
- `User` lives in the identity DB; everything else in the per-vertical DB.
- Auth dependency import path (canonical): `from assistive_core import get_current_user`
  (or `from assistive_core.auth.deps import get_current_user`).
- Cross-module reuse: `from assistive_core.follow import follow_service, FollowRepository`.

## Already COMPLETE (foundational, authored together)

- `settings.py` -> `settings`, `Settings`
- `base.py` -> `TimestampMixin`, `utcnow`
- `db.py` -> `init_core(*, vertical_documents, feed_sources)`, `close_core()`,
  `get_client()`, `CORE_SOCIAL_DOCUMENTS`
- `auth/` -> `User`, `auth_service`, `auth_router`, `get_current_user`,
  `get_current_user_optional`, `Token`, `UserCreate`, `UserLogin`, `UserResponse`
- `follow/` -> `Follow`, `FollowRepository`, `FollowService`, `follow_service`,
  `follow_router`, `UserSummary`
- `feed/registry.py` -> `FeedSource`, `register`, `registered`, `clear`,
  `FeedItemResponse`, `FeedAuthor`, `to_camel`
- `notifications/models.py` -> `Notification` document
- `calendar/models.py` -> `Event` document

## feed module agent -> create `feed/service.py`, `feed/router.py`

- `service.py`: `class FeedService` with
  `async def get_feed(self, user_id: str, limit: int = 20, before: datetime | None = None) -> list[FeedItemResponse]`.
  Iterate `assistive_core.feed.registry.registered()`; for each `FeedSource`
  query `src.document` for `(user_id IN followed_ids AND is_public == True)`,
  apply the `before` cursor against `src.occurred_at`, merge all sources, sort by
  `occurred_at` desc, slice to `limit`. Build each item as
  `FeedItemResponse(type=src.type, author=FeedAuthor(id=..., full_name=...), occurred_at=src.occurred_at(doc), payload=src.to_item(doc))`.
  Resolve author names with one batched `User.find(In(User.id, author_ids))`.
  Use `follow_service.get_following_ids`.
- `router.py`: `APIRouter(prefix="/feed")`; `GET ""` -> `list[FeedItemResponse]`,
  query params `limit` (ge=1, le=100, default 20) and `before` (datetime cursor),
  `Depends(get_current_user)`. Export router as `feed_router`.

## notifications module agent -> create `schemas.py`, `repository.py`, `service.py`, `router.py`

Port from beekeeper `schemas/notification.py`, `repositories/notification_repository.py`,
`services/notification_service.py`, `routers/notification.py`.
- `schemas.py`: `NotificationResponse`.
- `repository.py`: `NotificationRepository` (`get_by_id`, `get_by_user_id`,
  `create`, `insert_many`, `update`, `mark_all_read`).
- `service.py`: `NotificationService` (`create_for_followers`, `list_for_user`,
  `mark_read`, `mark_all_read`). Fan-out uses `FollowRepository` from
  `assistive_core.follow`.
- `router.py`: `APIRouter(prefix="/notifications")` -> `GET ""`,
  `POST "/{id}/read"`, `POST "/read-all"`. Export as `notification_router`.

## calendar module agent -> create `schemas.py`, `repository.py`, `service.py`, `router.py`

Port from beekeeper `schemas/event.py`, `repositories/event_repository.py`,
`services/event_service.py`, `routers/event.py`.
- `schemas.py`: `EventBase`, `EventCreate`, `EventUpdate`, `EventResponse`.
- `repository.py`: `EventRepository` (`get_by_id`, `get_by_user_id`,
  `get_calendar`, `create`, `update`, `delete`).
- `service.py`: `EventService` (`get_user_events`, `get_event`, `get_calendar`,
  `create_event`, `update_event`, `delete_event`). Uses `follow_service` for the
  calendar fan-in and `notification_service` for public-create fan-out.
- `router.py`: `APIRouter(prefix="/events")` -> `GET ""`, `GET "/calendar"`,
  `GET "/{id}"`, `POST ""`, `PUT "/{id}"`, `DELETE "/{id}"`. Export as `event_router`.

## clients module agent -> create `bunny.py`, `weather.py`

Port Bunny from beekeeper `services/bunny_storage_service.py`; read config from
`settings`. Weather hits the sibling VRUsafety service at
`settings.WEATHER_SERVICE_URL` via httpx (beekeeper's was mock).
- `bunny.py`: `BunnyStorageService` (`upload_photo`, `delete_photo`) + module
  instance `bunny_storage`.
- `weather.py`: `WeatherClient` (`get_current_weather`) + module instance
  `weather_client`.
