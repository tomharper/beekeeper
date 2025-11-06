# Beekeeper FastAPI Backend

Backend API for the Beekeeper app with AI-powered image analysis.

## Setup

1. Create a virtual environment:
```bash
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate
```

2. Install dependencies:
```bash
pip install -r requirements.txt
```

3. Create `.env` file:
```bash
cp .env.example .env
# Edit .env with your API keys
```

4. Run the development server:
```bash
python main.py
# Or use uvicorn directly:
uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

## API Documentation

Once running, visit:
- Swagger UI: http://localhost:8000/docs
- ReDoc: http://localhost:8000/redoc

## Endpoints

### Hive Management
- `GET /api/hives` - List all hives
- `POST /api/hives` - Create a new hive
- `GET /api/hives/{id}` - Get hive details
- `PUT /api/hives/{id}` - Update hive
- `DELETE /api/hives/{id}` - Delete hive

### Inspection Management
- `GET /api/inspections` - List inspections (filter by hive_id)
- `POST /api/inspections` - Create inspection
- `GET /api/inspections/{id}` - Get inspection details

### Task Management
- `GET /api/tasks` - List tasks (filter by hive_id, completed)
- `POST /api/tasks` - Create task
- `PUT /api/tasks/{id}` - Update task
- `DELETE /api/tasks/{id}` - Delete task

### AI Image Analysis
- `POST /api/analyze-image` - Analyze bee/hive images
- `POST /api/upload-image` - Upload images

### Weather
- `GET /api/weather` - Get weather data for location

## TODO

- [ ] Implement database persistence (SQLite/PostgreSQL)
- [ ] Integrate Claude Vision API for image analysis
- [ ] Add authentication and user management
- [ ] Implement file storage (local/S3)
- [ ] Add weather API integration
- [ ] Implement notification system
- [ ] Add data export functionality
