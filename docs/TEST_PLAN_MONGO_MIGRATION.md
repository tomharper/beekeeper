# Beekeeper — MongoDB Migration Test Plan

Scope: validate the SQLAlchemy/SQLite → Motor/Beanie/MongoDB migration of the
`beekeeper` backend (`api/`). The migration commit was `6c8d039`; HEAD (`d4af175`)
has since moved DB wiring into the shared **assistive-core** package. **All tests
must target HEAD wiring**, not the migration commit.

This document is the single source of truth for scaffolders. Every test case to
write is listed below, per file, with its source-under-test and priority.

---

## 1. Strategy

The migration changed the persistence layer end to end while keeping the HTTP API
shape. The highest-value risks are not the HTTP routes (mostly unchanged) but the
new persistence behaviors:

1. **Serialization round-trips** — fields whose representation changed:
   - `Alert.hive_ids`: was comma-separated string → now real `list[str]`.
   - `Inspection.photos`: was JSON-encoded text → now real `list[str]`.
   - All enums (`str, Enum`) must round-trip as their string values through Beanie ↔ BSON ↔ JSON.
   - `datetime` fields are timezone-aware UTC (`base.utcnow`) and must survive BSON round-trip.
   - `id` is a UUID **string** (from `str(uuid.uuid4())` in routers), NOT an ObjectId. `Apiary.get(uuid_str)` must work and the API must never leak an ObjectId.
   - `created_at` / `updated_at` auto-populate via `TimestampMixin` default_factory.
2. **Repository query correctness** — equality filters, `In(...)` operators, datetime range filters, sort order, and `limit`/`before` cursor pagination.
3. **Index bootstrap** — Beanie creates the declared `Settings.indexes` on init.
4. **Bulk mutation** — `TaskRepository.mark_overdue_tasks` (`.update(Set(...))`, returns `modified_count`).
5. **Not-found / empty paths** — `get_by_id` → `None`, empty result lists, 404 routes.

Test layers:
- **Repository/unit (async, P0)** — direct `await Repo().method(...)` against a live test Mongo. This is where the migration logic actually lives; most existing tests never touch it.
- **Serialization/model (async, P0)** — insert a Document, re-fetch, assert field types and values round-trip.
- **Index (async, P1)** — assert declared indexes exist via `Model.get_motor_collection().index_information()`.
- **HTTP/route (sync TestClient, P1)** — rewrite of the existing route tests so they run inside the lifespan (and therefore inside an initialized Beanie + seeded data).

### Existing-test conventions to match
- Route tests use a **module-level** `client = TestClient(app)` and plain
  synchronous `def test_*` functions hitting `/api/...` (see `tests/test_apiaries.py`).
- `tests/__init__.py` exists; there is **no** `conftest.py` yet.
- `pytest-asyncio==0.24.0` is installed but unconfigured. New async tests need
  `@pytest.mark.asyncio` (or `asyncio_mode=auto`) — add this config in `conftest.py`
  / `pytest.ini` as part of P0 infra.
- New async repository tests are a NEW convention this migration requires (the
  existing suite has none). Keep them isolated in their own files and keep the
  route files synchronous, matching the current style.

---

## 2. How to run

```
cd /Users/tomharper/projects/assistive/beekeeper/api && python -m pytest
```

Notes / gotchas:
- Use the venv interpreter — system anaconda may shadow it:
  `cd /Users/tomharper/projects/assistive/beekeeper/api && venv/bin/python -m pytest`
- A MongoDB must be reachable at `MONGODB_URI` (default `mongodb://localhost:27017`).
  `mongomock-motor` is **NOT installed** (`pip list` shows only beanie/motor/pytest/pytest-asyncio).
  The conftest therefore targets a **real Mongo** and **skips** the whole async
  suite when Mongo is unreachable (see §3). If a maintainer later adds
  `mongomock-motor`, the conftest fixture can swap the client with no test changes.
- `assistive_core.settings.Settings` reads env **at import time**, so test DB
  overrides (`MONGODB_DB`, `IDENTITY_DB`, `MONGODB_URI`) must be set **before**
  `assistive_core` is imported — set them at the very top of `conftest.py`
  (before any `from app...` / `from assistive_core...`) or via `pytest.ini` `env`.

---

## 3. Mongo test approach (conftest design — P0, build first)

`assistive_core.db` memoizes a **module-global** `_client`
(`get_client()` / `close_core()`). This is the central isolation hazard: a client
created against the dev DB leaks across tests. The conftest must own its lifecycle.

Required `tests/conftest.py` responsibilities:
1. **Env override BEFORE imports**: set `MONGODB_DB="beekeeper_test"`,
   `IDENTITY_DB="assistive_identity_test"`, ensure `ENV`/`ASSISTIVE_ENV` is NOT
   `production` (so `init_core` doesn't fail-fast on the placeholder JWT key),
   and read `MONGODB_URI` (default localhost).
2. **asyncio config**: set `asyncio_mode=auto` (pytest.ini) or mark fixtures;
   define an `event_loop` policy compatible with `pytest-asyncio==0.24.0`.
3. **Mongo-reachability guard**: attempt a `ping`; if it fails, `pytest.skip`
   (collection-level) the entire async suite so CI without Mongo stays green.
4. **`init_core` fixture** (session or module scope): call
   `await init_core(vertical_documents=DOMAIN_DOCUMENTS, feed_sources=FEED_SOURCES)`
   exactly as `app/main.py` lifespan does. Import `DOMAIN_DOCUMENTS` from
   `app.models` and `FEED_SOURCES` from `app.feed_sources`.
5. **Per-test cleanup** (function scope, autouse): after each test, drop/clear
   the beekeeper-test collections (apiaries, hives, alerts, recommendations,
   tasks, inspections) AND the identity-test DB, so tests don't see each other's
   data. Prefer dropping collections over dropping the DB (keeps indexes; or
   re-init if dropped).
6. **Teardown**: `await close_core()` and reset the global `_client`; drop the
   two test databases at session end.
7. **Factory helpers** (optional but recommended): small builders that create a
   valid `Apiary`/`Hive`/`Inspection`/`Task`/`Alert`/`Recommendation` with a
   `str(uuid.uuid4())` id and required fields filled, to keep test cases short.

The conftest is a hard dependency of every P0/P1 file below. Build it first.

Also as P0 infra: **delete or rewrite `tests/test_main.py`** — it imports
`sqlalchemy`, `app.database.get_db`, `app.models.Base`, none of which exist
post-migration, and it **aborts collection** (`ModuleNotFoundError: sqlalchemy`),
blocking the other 16 tests from even running.

---

## 4. Prioritized test files to add

Priority key: **P0** = migration-correctness-critical (serialization, query logic,
infra), **P1** = important (indexes, route rewrites, service aggregation), **P2** = later.

---

### 4.1 `tests/conftest.py` — **P0**
Source under test: `assistive_core/db.py` (`init_core`/`close_core`/`get_client`),
`assistive_core/settings.py`, `app/models.DOMAIN_DOCUMENTS`, `app/feed_sources.FEED_SOURCES`.
Purpose: test infrastructure — async Mongo fixtures, env override, skip guard, per-test cleanup, factory helpers. No test cases itself; see §3 for required behaviors.

---

### 4.2 `tests/test_main.py` (REWRITE existing) — **P0**
Source under test: `app/main.py` (root/health routes, lifespan wiring).
Purpose: remove all SQLAlchemy imports; verify app boots under the real lifespan.
Cases:
- DELETE the SQLAlchemy `engine`/`override_get_db`/`Base.metadata` setup entirely.
- `test_root_endpoint`: `GET /` → 200, body `{"message": "Welcome to Beekeeper API", "version": "1.0.0"}`.
- `test_health_endpoint`: `GET /health` → 200, `{"status": "healthy"}`.
- `test_app_boots_with_lifespan`: use `with TestClient(app) as client:` so the
  lifespan runs `init_core` + `seed_database`; assert a basic route returns 200
  (confirms Beanie init does not raise on startup).

---

### 4.3 `tests/test_serialization.py` — **P0**  (HIGHEST migration risk)
Source under test: all `app/models/*.py` Document classes + `app/models/base.py`.
Purpose: assert each migrated field round-trips through insert → re-fetch with correct type/value.
Cases:
- `test_apiary_id_is_uuid_string`: insert `Apiary(id=str(uuid4()), ...)`; assert
  `await Apiary.get(that_str)` returns it and `.id` is the same `str` (NOT an ObjectId).
- `test_timestamps_autopopulate`: insert without setting `created_at`/`updated_at`;
  assert both are non-null timezone-aware UTC datetimes (`TimestampMixin`).
- `test_alert_hive_ids_roundtrips_as_list`: insert `Alert` with
  `hive_ids=["h1","h2","h3"]`; re-fetch; assert it is a `list[str]` equal to input
  (NOT a comma-joined string). Also test empty `[]` default.
- `test_inspection_photos_roundtrips_as_list`: insert `Inspection` with
  `photos=["a.jpg","b.jpg"]`; re-fetch; assert `list[str]` equal (NOT JSON text).
  Also test empty `[]` default.
- `test_apiary_status_enum_roundtrip`: each `ApiaryStatus` value persists/loads as the enum (stored as its string value).
- `test_hive_enums_roundtrip`: `HiveStatus`, `ColonyStrength`, `QueenStatus`, `Temperament`, `HoneyStores` round-trip.
- `test_alert_enums_roundtrip`: `AlertType`, `AlertSeverity` round-trip; `dismissed: bool` round-trips.
- `test_recommendation_enums_roundtrip`: `RecommendationType`, `Priority`.
- `test_task_enums_roundtrip`: sample across the large `TaskType` set plus `TaskStatus`, `TaskPriority`, `RecurrenceFrequency`; `is_public: bool=True` default.
- `test_inspection_enums_roundtrip`: all 7 enums (`QueenCellStatus`, `BroodPattern`, `ColonyTemperament`, `ColonyPopulation`, `HealthStatus`, `ResourceLevel` ×2 for honey/pollen) round-trip; `is_public` default True.
- `test_datetime_fields_roundtrip`: `Hive.last_inspected`, `Inspection.inspection_date`/`next_inspection_date`, `Task.due_date`/`completed_date` persist and re-load as tz-aware datetimes (assert equality to microsecond/BSON-millis tolerance).
- `test_optional_fields_default_none`: optional fields (e.g. `Inspection.duration_minutes`, `weather_temp`) are `None` when unset and round-trip as `None`.

---

### 4.4 `tests/test_inspection_repository.py` — **P0**
Source under test: `app/repositories/inspection_repository.py`.
Purpose: cover the most query-heavy DAO — filters, sort, pagination cursor, feed `In`+`is_public`.
Cases:
- `test_create_and_get_by_id`: `create` then `get_by_id` returns it.
- `test_get_by_id_missing_returns_none`: unknown id → `None`.
- `test_get_all_sorted_desc`: insert 3 with different `inspection_date`; `get_all` returns newest-first.
- `test_get_by_user_id_filters_and_sorts`: only matching `user_id`, newest-first.
- `test_get_by_hive_id_filters_and_sorts`: only matching `hive_id`, newest-first.
- `test_get_by_hive_and_user`: both filters AND-ed.
- `test_get_latest_for_hive`: `find_one` + sort returns the single newest for that hive; `None` when none.
- `test_get_recent_limit`: `get_recent(user_id, limit=2)` returns exactly 2 newest of N>2.
- `test_get_recent_default_limit_10`: default caps at 10.
- `test_get_feed_only_public`: feed excludes `is_public=False` records.
- `test_get_feed_in_user_ids`: only inspections from the supplied `user_ids` list (`In`).
- `test_get_feed_limit`: respects `limit`.
- `test_get_feed_before_cursor`: with `before=<ts>`, returns only records strictly older than the cursor (`inspection_date < before`); pagination across two pages yields no overlap and no gap.
- `test_update_persists`: mutate a field, `update`, re-fetch shows change.
- `test_delete_removes`: `delete` then `get_by_id` → `None`.

---

### 4.5 `tests/test_task_repository.py` — **P0**
Source under test: `app/repositories/task_repository.py`.
Purpose: cover status/`In` filters, datetime ranges, feed cursor, and the bulk `mark_overdue_tasks` mutation.
Cases:
- `test_create_get_update_delete`: full CRUD round-trip; `get_by_id` missing → `None`.
- `test_get_by_user_id` / `test_get_by_hive_id` / `test_get_by_apiary_id`: equality filters return only matches.
- `test_get_by_status`: filters by `user_id` AND a given `TaskStatus`.
- `test_get_pending_and_overdue`: returns only `PENDING`/`OVERDUE`/`IN_PROGRESS` (`In`), excludes `COMPLETED`/others, sorted by `due_date` asc.
- `test_get_upcoming_window`: `get_upcoming(user_id, days=7)` returns tasks with `due_date <= now+7d` and status in {PENDING, IN_PROGRESS}; excludes ones beyond the window and excludes COMPLETED.
- `test_get_upcoming_custom_days`: `days=1` narrows the window.
- `test_get_overdue`: returns tasks with `due_date < now` and status in {PENDING, IN_PROGRESS}, sorted by `due_date`; excludes future/COMPLETED.
- `test_get_feed_public_in_users_limit_before`: mirror inspection feed cases — `is_public` filter, `In(user_ids)`, `limit`, and `before` cursor (`due_date < before`), newest-first.
- `test_mark_as_completed`: sets `status=COMPLETED` and stamps `completed_date` (non-null tz-aware); persists.
- `test_mark_overdue_tasks_modifies_matching`: seed past-due `PENDING` tasks for a user; call returns `modified_count` == number flipped; re-fetch shows `OVERDUE`.
- `test_mark_overdue_tasks_ignores_non_pending_and_future`: future-due or non-PENDING tasks are untouched.
- `test_mark_overdue_tasks_returns_zero_when_none`: no matches → returns `0`, nothing mutated.
- `test_mark_overdue_tasks_scoped_to_user`: another user's past-due tasks are NOT flipped.

---

### 4.6 `tests/test_apiary_hive_repositories.py` — **P0**
Source under test: `app/repositories/apiary_repository.py`, `app/repositories/hive_repository.py`.
Purpose: CRUD + the `Hive.get_by_apiary_id` equality filter (apiary↔hive relationship is core to the app).
Cases:
- `test_apiary_crud`: `create` → `get_by_id` → `get_all` (contains it) → `update` → `delete` → `get_by_id` is `None`.
- `test_apiary_get_by_id_missing_returns_none`.
- `test_hive_crud`: same lifecycle for `Hive`.
- `test_hive_get_by_id_missing_returns_none`.
- `test_hive_get_by_apiary_id_filters`: insert hives across two apiaries; `get_by_apiary_id` returns only the matching apiary's hives.
- `test_hive_get_by_apiary_id_empty`: unknown apiary id → `[]`.
- `test_hive_get_all`: returns all inserted hives regardless of apiary.

---

### 4.7 `tests/test_alert_recommendation_repositories.py` — **P0**
Source under test: `app/repositories/alert_repository.py`, `app/repositories/recommendation_repository.py`.
Purpose: CRUD + `Alert.get_active` (`dismissed == False`) and `Recommendation.get_by_hive_id`.
Cases:
- `test_alert_crud`: create/get/update/delete; missing id → `None`.
- `test_alert_get_active_excludes_dismissed`: seed dismissed + active; `get_active` returns only `dismissed == False`.
- `test_alert_get_all_includes_dismissed`: `get_all` returns both.
- `test_alert_hive_ids_persisted_via_repo`: create via repo with `hive_ids=[...]`; re-fetch shows the list (repo-level guard complementing §4.3).
- `test_recommendation_crud`: create/get/update/delete; missing id → `None`.
- `test_recommendation_get_by_hive_id_filters`: returns only matching `hive_id`; unknown id → `[]`.

---

### 4.8 `tests/test_indexes.py` — **P1**
Source under test: each model's `Settings.indexes` + `init_core` index bootstrap.
Purpose: assert Beanie created the declared indexes on init (catches typos / missing index migration).
Cases (use `await Model.get_motor_collection().index_information()`):
- `test_hive_index_on_apiary_id`: `hives` has an index keyed on `apiary_id`.
- `test_alert_compound_index`: `alerts` has compound `(dismissed:1, timestamp:-1)`.
- `test_recommendation_index_on_hive_id`.
- `test_task_indexes`: `tasks` has `hive_id`, `apiary_id`, compound `(user_id:1, due_date:1)`, compound `(user_id:1, status:1)`.
- `test_inspection_indexes`: `inspections` has `hive_id`, `user_id`, `(hive_id:1, inspection_date:-1)`, `(user_id:1, inspection_date:-1)`, `(user_id:1, is_public:1, inspection_date:-1)`.
- `test_apiary_has_only_default_index`: `apiaries` declares no custom indexes (only `_id`).
- Note: none of the models declare a `unique=True` index, so there is no uniqueness to assert beyond the default `_id`. (Documented here so scaffolders don't invent uniqueness tests.)

---

### 4.9 `tests/test_apiary_service.py` — **P1**
Source under test: `app/services/apiary_service.py`.
Purpose: cover `hive_count` aggregation (`Counter` over all hives in `get_all_apiaries`; `.count()` in `get_apiary`) and 404 paths — pure migration-affected service logic.
Cases:
- `test_get_all_apiaries_hive_counts`: two apiaries with differing hive counts; assert each `ApiaryResponse.hive_count` is correct (and 0 for an apiary with no hives).
- `test_get_apiary_counts_hives`: `get_apiary` returns correct `hive_count` via `.count()`.
- `test_get_apiary_not_found_raises_404`: unknown id raises `HTTPException` 404.
- `test_update_apiary_not_found_raises_404`.
- `test_delete_apiary_not_found_raises_404`.
- `test_create_apiary_starts_with_zero_hives`: freshly created apiary reports `hive_count == 0`.

---

### 4.10 Route tests (REWRITE) — **P1**
Source under test: `app/routers/*.py` via `TestClient(app)`.
Purpose: make the existing HTTP tests actually run inside an initialized DB and stop silently passing on empty data.
Apply to existing files: `tests/test_apiaries.py`, `tests/test_hives.py`,
`tests/test_recommendations.py`, `tests/test_alerts_weather.py`.
Cases / changes (shared pattern across all four):
- Replace module-level `client = TestClient(app)` usage so requests run **inside
  the lifespan** (e.g. a `client` fixture using `with TestClient(app) as client:`),
  so Beanie is initialized and `seed_database` has run. Point it at the test DB
  via the conftest env override.
- Remove the conditional `if apiaries:` / `if hives:` guards that let tests pass
  on empty data — assert on data the test itself creates.
- `test_apiaries.py`: keep root CRUD cases but make create→get→update→delete
  self-contained (don't rely on seed ordering); assert `hive_count` present and
  correct after adding a hive.
- `test_hives.py`: create apiary then hive under it; assert filter-by-apiary route returns it; CRUD + 404 on unknown hive update/delete.
- `test_recommendations.py`: create recommendation; list/by-hive route returns it; 404 on unknown.
- `test_alerts_weather.py`: assert `hive_ids` survives as a JSON array through the route; active-vs-dismissed filtering at the route; keep weather cases as-is if they don't hit Mongo.
- Add the currently-missing route 404 coverage: update/delete of unknown hive, recommendation, task, inspection → 404.

---

### 4.11 `tests/test_task_inspection_routes.py` — **P2**
Source under test: `app/routers/tasks.py`, `app/routers/inspections.py`.
Purpose: end-to-end HTTP coverage for the two models that currently have NO route tests at all, exercising the pagination/feed/overdue endpoints through the API.
Cases:
- Task: create → list-by-user → mark-complete endpoint → overdue/upcoming endpoints return expected sets.
- Inspection: create → get-by-hive → latest-for-hive → recent/feed endpoints honor `limit`/`before` query params.
- 404s on unknown task/inspection get/update/delete.
- Confirm `photos` array and enum fields serialize correctly in the JSON response (route-level complement to §4.3).

---

## 5. Build order

1. **P0 infra**: `conftest.py` (§4.1) + delete/rewrite `test_main.py` (§4.2) — nothing else runs until collection succeeds.
2. **P0 correctness**: `test_serialization.py` (§4.3), then the four repository files (§4.4–4.7).
3. **P1**: `test_indexes.py` (§4.8), `test_apiary_service.py` (§4.9), route rewrites (§4.10).
4. **P2**: `test_task_inspection_routes.py` (§4.11).
