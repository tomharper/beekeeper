# Beekeeper API

FastAPI backend for the Beekeeper application with proper layered architecture.

## Architecture

The API follows a clean layered architecture:

```
app/
├── models/          # SQLAlchemy domain models (database layer)
├── schemas/         # Pydantic view models/DTOs (API layer)
├── repositories/    # Data access layer
├── services/        # Business logic layer
├── routers/         # API routes/controllers
├── database.py      # Database configuration
├── seed_data.py     # Mock data initialization
└── main.py          # FastAPI application
```

## Features

### API Endpoints

**Apiaries**
- `GET /api/apiaries` - List all apiaries
- `GET /api/apiaries/{id}` - Get apiary by ID
- `POST /api/apiaries` - Create new apiary
- `PUT /api/apiaries/{id}` - Update apiary
- `DELETE /api/apiaries/{id}` - Delete apiary

**Hives**
- `GET /api/hives` - List all hives (optional `?apiary_id=` filter)
- `GET /api/hives/{id}` - Get hive by ID
- `POST /api/hives` - Create new hive
- `PUT /api/hives/{id}` - Update hive
- `DELETE /api/hives/{id}` - Delete hive

**Alerts**
- `GET /api/alerts` - List all alerts
- `GET /api/alerts/active` - List active (non-dismissed) alerts
- `GET /api/alerts/{id}` - Get alert by ID
- `POST /api/alerts` - Create new alert
- `PATCH /api/alerts/{id}` - Update alert (e.g., dismiss)

**Recommendations**
- `GET /api/recommendations?hive_id={id}` - List recommendations for a hive
- `POST /api/recommendations` - Create new recommendation
- `PUT /api/recommendations/{id}` - Update recommendation
- `DELETE /api/recommendations/{id}` - Delete recommendation

**Weather**
- `GET /api/weather` - Get current weather conditions

## Getting Started

### Prerequisites

- Python 3.11+
- pip or poetry

### Installation

#### Using pip

```bash
cd api
pip install -r requirements.txt
```

#### Using poetry

```bash
cd api
poetry install
```

### Configuration

Copy `.env.example` to `.env` and configure:

```bash
cp .env.example .env
```

Edit `.env`:
```
DATABASE_URL=sqlite:///./beekeeper.db
DEBUG=True
API_HOST=0.0.0.0
API_PORT=2020
CORS_ORIGINS=http://localhost:2000
```

### Running the API

#### Development mode

```bash
uvicorn app.main:app --reload --host 0.0.0.0 --port 2020
```

Or with poetry:

```bash
poetry run uvicorn app.main:app --reload --host 0.0.0.0 --port 2020
```

The API will be available at:
- API: http://localhost:2020
- Interactive docs: http://localhost:2020/docs
- Alternative docs: http://localhost:2020/redoc

### Mock Data

The database is automatically seeded with mock data on first startup:
- 3 apiaries (Backyard Garden, Hillside Meadow, Riverbend Apiary)
- 9 hives across the apiaries
- 3 alerts
- 3 recommendations

## Development

### Run tests

```bash
pytest
```

### Code formatting

```bash
black app/
```

### Linting

```bash
ruff check app/
```

### Type checking

```bash
mypy app/
```

## Database

The API uses SQLAlchemy with SQLite by default. To use PostgreSQL or another database, update the `DATABASE_URL` in `.env`.

### Reset database

```bash
rm beekeeper.db
```

The database will be recreated and reseeded on next startup.

## Deployment

For production deployment:

1. Set `DEBUG=False` in `.env`
2. Use a production database (PostgreSQL recommended)
3. Use a production ASGI server like Gunicorn with Uvicorn workers:

```bash
gunicorn app.main:app -w 4 -k uvicorn.workers.UvicornWorker --bind 0.0.0.0:2020
```

## API Documentation

Once running, visit:
- Swagger UI: http://localhost:2020/docs
- ReDoc: http://localhost:2020/redoc
