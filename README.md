# Beekeeper App

A beekeeping management application — hive/apiary tracking, inspections, tasks, and AI-powered photo analysis — that doubles as a **peer knowledge-sharing network**: a commercial beekeeper's operations (inspections, feedings, treatments) are broadcast to nearby smaller beekeepers who follow them, so they can see what to do and when.

It is the first **vertical** built on `assistive-core` (below).

## Shared core: `assistive-core`

The auth, follow/subscribe graph, activity feed, notifications, and shared calendar are **not beekeeping-specific** — they are provided by a shared package, **`assistive-core`**, intended to back several "assistive" vertical apps (beekeeper, plus future event-sharing, social-weather, and road-safety apps) on **one shared infrastructure** (a MongoDB cluster + Bunny CDN + a weather service).

**It currently lives vendored inside this repo at [`assistive-core/`](assistive-core/)**, consumed by the Python backend (`api/`) via an editable install (`-e ../assistive-core` in `api/requirements.txt`). This is a **temporary home** — it will be split into its own repository later (history preservable via `git subtree split --prefix=assistive-core`). Until then, treat it as a standalone package and keep it **domain-agnostic**: changes there affect every vertical that will consume it.

Key pieces (`assistive-core/assistive_core/`):
- `auth/` — shared **SSO** identity (users live in a shared identity DB; one login spans verticals)
- `follow/` — the follow/subscribe graph
- `feed/` — a vertical-agnostic **FeedSource registry**: a vertical registers its record types to appear in the merged feed and drive notification fan-out (`assistive_core.announce(record)`), with no edits to the core
- `notifications/`, `calendar/`, `clients/` (Bunny media + weather)

The beekeeper backend registers its inspection/task feed sources in `api/app/feed_sources.py` and wires the package in via `assistive_core.init_core(...)` in `api/app/main.py`.

## Project Structure

```
beekeeper/
├── api/                 # FastAPI backend (Python) — Motor/Beanie on MongoDB
├── assistive-core/      # Shared platform package, vendored (see above)
├── web/                 # React + TypeScript + Vite web app
├── composeApp/          # Kotlin Multiplatform shared + mobile code (Android + iOS)
├── iosApp/              # iOS host (SwiftUI shell around Compose)
├── images/              # Shared image assets
├── beekeeper/           # Legacy scaffold (superseded by api/ + web/)
└── beekeeperApp/        # Legacy KMP scaffold (superseded by composeApp/)
```

## Features

### Operator (beekeeper-facing)
- **Apiary & hive management** — track multiple yards and hives
- **Inspection tracking** — detailed records (queen, brood, health, resources) with photos; feedings & treatments are captured on the inspection
- **Task scheduling** — inspections, feeding, treatments, harvests
- **AI photo analysis** — Claude Vision for bee health, pests (varroa, etc.), queen presence, brood pattern, colony strength
- **AI advisor** — Claude-backed chat
- **Weather** — conditions for inspection timing

### Social (the follow network — via `assistive-core`)
- **SSO identity** — one account across assistive apps
- **Follow/subscribe** — follow other beekeepers
- **Activity feed** — public inspections & tasks from people you follow, newest first, cursor-paginated
- **Notifications** — fan-out when someone you follow posts public activity
- **Shared calendar** — your events plus followed users' public events
- Per-record `isPublic` visibility (default public; only new records flow to the feed)

## Technology Stack

### Backend (`api/` + `assistive-core/`)
- **FastAPI** + **Uvicorn** (ASGI)
- **MongoDB** via **Motor** (async) + **Beanie** (ODM) — shared "cinefiller" cluster, per-vertical database + a shared identity DB for SSO
- **Pydantic v2** — validation / camelCase response aliasing
- **python-jose** — JWT auth (shared SSO signing key)
- **Anthropic Claude** — vision + chat
- **Bunny.net** — media/CDN storage

### Web (`web/`)
- **React 18** + **TypeScript** + **Vite** + **Tailwind CSS**
- **React Router**, **lucide-react**, **date-fns**

### Mobile (`composeApp/` + `iosApp/`)
- **Kotlin Multiplatform** + **Compose Multiplatform** (Android + iOS)
- **Ktor** (HTTP), **SQLDelight** (offline cache), **Koin** (DI), Kotlinx Serialization/DateTime

## Getting Started

### Prerequisites
- Python 3.12 (backend)
- Node.js 18+ (web)
- A MongoDB connection (local `mongod`, or the shared Atlas cluster)
- JDK + Android SDK for mobile (note: **no Gradle wrapper** — use a system `gradle`)
- API keys: `ANTHROPIC_API_KEY`, and `BUNNY_*` if using uploads

### 1. Backend API (`api/`)

```bash
cd api
python -m venv venv && source venv/bin/activate
pip install -r requirements.txt          # also installs ../assistive-core editable

cp .env.example .env                      # then fill in the values below
# Required: MONGODB_URI, IDENTITY_DB, MONGODB_DB, JWT_SECRET_KEY, ANTHROPIC_API_KEY
# Optional: BUNNY_*, WEATHER_SERVICE_URL

uvicorn app.main:app --reload --port 2020
# API at http://localhost:2020 · docs at http://localhost:2020/docs
```

Notes:
- Users authenticate against the shared **identity DB** (`IDENTITY_DB`); domain + social data live in the **vertical DB** (`MONGODB_DB`).
- `init_core` fails fast if `ENV=production` and `JWT_SECRET_KEY` is still the placeholder.

### 2. Web (`web/`)

```bash
cd web
npm install
cp .env.example .env          # VITE_API_URL defaults to http://localhost:2020/api
npm run dev
# App at http://localhost:2000
```

### 3. Mobile (`composeApp/`)

```bash
# No gradlew in this repo — use a system gradle + a local.properties with sdk.dir
gradle :composeApp:assembleDebug        # Android
# iOS: open iosApp/ in Xcode and run
```

## Architecture

### Data flow

```
Web / Mobile  ──►  FastAPI (api/)  ──►  assistive-core ──►  MongoDB (identity DB + vertical DB)
                        │                               └─►  follow / feed registry / notifications / calendar
                        ├──►  Anthropic Claude (vision + chat)
                        ├──►  Bunny.net CDN (media)
                        └──►  weather service (shared)
```

### Backend layering (`api/app/`)
```
routers/  →  services/  →  repositories/  →  models/  (Beanie documents, MongoDB)
            (domain: apiaries, hives, inspections, tasks)
assistive_core provides: auth(SSO), follow, feed, notifications, calendar
app/feed_sources.py registers inspection + task as FeedSources
```

### Mobile layering
```
Compose UI  →  ViewModel (StateFlow)  →  Repository (API-first + SQLDelight cache)  →  Ktor / SQLDelight
```

## Roadmap

### Done
- [x] Backend on MongoDB (Motor/Beanie), migrated off SQLAlchemy/SQLite
- [x] Apiary/hive/inspection/task management; AI vision + advisor
- [x] `assistive-core` extracted: SSO auth, follow graph, feed registry, notifications, calendar
- [x] Web "Following" surface (feed, search, follow/unfollow, pagination)
- [x] Mobile feed/follow feature (KMP)

### Next
- [ ] Stand up a **social-weather** vertical on `assistive-core` (proves the boundary)
- [ ] Reconcile the mobile `Inspection` model with the backend schema (field-name divergence)
- [ ] Event fan-out via Mongo change streams (replace synchronous fan-out)
- [ ] Split `assistive-core` into its own repository
- [ ] Migrate hypster (events) and VRU-safety onto `assistive-core`

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes (keep `assistive-core` domain-agnostic)
4. Submit a pull request

## License

[Add your license here]

## Acknowledgments

- Built with FastAPI, MongoDB/Beanie, React, and Kotlin/Compose Multiplatform
- AI powered by Anthropic Claude
