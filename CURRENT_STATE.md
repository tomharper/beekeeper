# Beekeeper App - Current State

**Last Updated:** May 22, 2026

## Project Layout

The repo's active layout sits at the top level (older `beekeeper/` and `beekeeperApp/` directories remain as legacy scaffolding):

```
beekeeper/
├── api/             # FastAPI backend (Python)
├── web/             # React + TypeScript web app
├── composeApp/      # Kotlin Multiplatform shared/mobile (Android + iOS targets)
├── iosApp/          # iOS host app (SwiftUI shell around Compose)
├── images/          # Shared image assets
├── beekeeper/       # Legacy: earlier react-app + fastapi-backend scaffold
└── beekeeperApp/    # Legacy: earlier KMP scaffold
```

## Backend — `api/`

FastAPI + async stack. Data on **MongoDB** via motor + Beanie (Pydantic ODM). Photo storage on **Bunny.net** CDN.

The shared data plane is the **cinefiller MongoDB cluster** (per-app databases — `beekeeper` here) and the **`hyperlocal` Bunny zone** (per-app folder namespace). VRUsafety uses the same cluster/zone for its own services; no shared API service sits between them.

**Routers** (`api/app/routers/`):
- `auth` — registration / login (JWT)
- `apiaries`, `hives`, `inspections`, `tasks` — core CRUD
- `photos` — image upload (Bunny.net storage)
- `chat` — Claude-backed AI chat
- `recommendations`, `alerts`, `weather`

**Models** (`api/app/models/`): Beanie `Document` classes — `Apiary`, `Hive`, `Inspection`, `Task`, `User`, `Alert`, `Recommendation`. Ids are plain string UUIDs (not `PydanticObjectId`). Enums serialised as plain strings.

**Repositories** (`api/app/repositories/`): thin async wrappers around Beanie `Document.find / get / insert / save / delete`. No DB session passed around — each repo just calls the model directly.

**Services** (`api/app/services/`):
- `auth_service`, `apiary_service`, `hive_service`, `inspection_service`, `task_service`
- `ai_analysis_service` — Claude Vision photo analysis
- `bunny_storage_service` — image upload/CDN
- `recommendation_service`, `alert_service`, `weather_service` (latter is currently a stub)

`seed_data.py` populates demo apiaries/hives/alerts/recommendations on first boot; self-skips if the apiaries collection is non-empty.

**Run:**
```bash
cd api
pip install -r requirements.txt
# set MONGODB_URI, MONGODB_DB (default "beekeeper"), ANTHROPIC_API_KEY, BUNNY_* env vars
uvicorn app.main:app --reload
```

## Web — `web/`

React + TypeScript + Vite + Tailwind.

**Pages** (`web/src/pages/`): `LoginPage`, `RegisterPage`, `ApiaryListPage`, `ApiaryDashboardPage`, `HiveDetailsPage`, `InspectionsPage`, `TasksPage`, `AIAdvisorPage`, `ProfilePage`.

**Other:** `components/BottomNav.tsx`, `context/`, `api/`, `types/`, `utils/`.

**Run:**
```bash
cd web
npm install
npm run dev
```

## Mobile — `composeApp/` + `iosApp/`

Kotlin Multiplatform with Android + iOS targets. ~39 Kotlin files under `commonMain`.

**Domain models** (`domain/model/`): `Apiary`, `Hive`, `Inspection`, `Task`, `Alert`, `Recommendation`, `Weather`, `ChatMessage`.

**Screens** (`ui/screens/`):
- `dashboard/ApiaryDashboardScreen`
- `apiary/ApiaryListScreen`
- `hive/HiveDetailsScreen`
- `inspections/InspectionsScreen`, `CreateInspectionScreen`
- `tasks/TasksScreen`
- `advisor/AIAdvisorScreen`, `AIAdvisorChatScreen`

**ViewModels:** `InspectionsViewModel`, `TasksViewModel`, `AIAdvisorViewModel`.

**Data layer:**
- `data/api/` — `ApiClient`, `ApiConfig`, `ApiModels`
- `data/repository/` — `InspectionRepository`, `TaskRepository`, `AIAdvisorRepository`
- `data/database/` — `Database`, `DatabaseDriverFactory`, mappers for Hive/Apiary/Task/Inspection
- `data/MockDataRepository.kt`

**SQLDelight schema** (`commonMain/sqldelight/com/beekeeper/app/database/`): `Apiary.sq`, `Hive.sq`, `Inspection.sq`, `Task.sq`.

**iOS host** (`iosApp/iOSApp.swift`): minimal SwiftUI shell that mounts `MainViewController` from Compose. See `iOS_SETUP.md`.

## Recent History

Recent commits on `main`:
- **Backend migrated** SQLAlchemy/SQLite → motor/Beanie/MongoDB (commit `6c8d039`). Two non-mechanical shape changes: `Alert.hive_ids` (csv string → `list[str]`) and `Inspection.photos` (json-text → `list[str]`). `Apiary.hive_count` now computed in the service layer (was a SQLAlchemy relationship property).
- PR #7 — backend AI chat endpoint with Claude
- iOS app setup + `iOS_SETUP.md`
- PR #6 — Android API connectivity + AI chat
- Offline support (SQLDelight) + `CreateInspectionScreen` UI
- CameraX dependencies for photo capture
- Web bottom navigation + mobile bottom nav / FABs
- AI Vision Integration (frontend + backend, Bunny.net storage)

## Known Gaps / Open Areas

These aren't observable as completed in the tree — worth confirming before assuming "done":
- **Mobile + web still need to be re-pointed at the new backend.** They'll keep working against the old camelCase JSON shape (unchanged), but if env vars or build configs reference any legacy DB shape they may need updates.
- **Customer/follower-facing surface not built yet.** The current routes are all operator-side; the read-only "what is the commercial beekeeper doing" view that other smaller beekeepers would consume is still TBD.
- **Local-first sync.** Mobile SQLDelight tables exist on-device, but there's no sync layer wiring local writes up to Mongo. This needs design before it gets built.
- **Harvest** — present in legacy models, no screen/router visible in the current `api/` or `composeApp/`.
- **Notifications / task reminders** — no platform notification handlers visible.
- **Charts / analytics, data export (CSV/PDF), educational content** — not present.
- **iOS host** is minimal; verify camera, storage, and notifications work end-to-end on iOS, not just Android.
- **Legacy `beekeeper/` and `beekeeperApp/` dirs** — decide whether to delete or keep as reference; they'll confuse contributors.

## Working Tree Status (snapshot)

- Branch `main`, ahead of `origin/main` by 1 commit (Mongo migration, not yet pushed).
- Other uncommitted noise: `.DS_Store` modification and untracked `web/.env` (correctly ignored from staging).

## Reference Docs

- `README.md` — top-level overview
- `DESIGN_ANALYSIS.md` — design notes
- `iOS_SETUP.md` — iOS build setup
- `TESTING.md` — testing notes
- `api/CHAT_SETUP.md` — AI chat configuration
