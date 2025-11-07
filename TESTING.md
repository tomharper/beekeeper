# Testing Guide

This guide explains how to test the Beekeeper application.

## Prerequisites

Make sure you have:
- Python 3.11+ installed
- Node.js 18+ installed
- All dependencies installed

## Backend Testing

### Install Test Dependencies

```bash
cd api
pip install -r requirements.txt
```

### Run Backend Tests

```bash
cd api
pytest
```

For verbose output:
```bash
pytest -v
```

For coverage report:
```bash
pytest --cov=app
```

### Verify API is Running

First, start the API server:
```bash
cd api
uvicorn app.main:app --reload --host 0.0.0.0 --port 2020
```

Then in another terminal, run the verification script:
```bash
cd api
python verify_api.py
```

This script will:
- Test all API endpoints
- Show you the data returned
- Confirm CORS is configured correctly
- Display sample apiaries, hives, alerts, etc.

### Manual API Testing

You can also test the API manually:

1. Start the server: `uvicorn app.main:app --reload --port 2020`
2. Visit http://localhost:2020/docs for interactive Swagger UI
3. Try out different endpoints directly in the browser

## Frontend Testing

### Run React App

```bash
cd web
npm install
npm run dev
```

The app will be available at http://localhost:2000

### Test React with Backend

1. **Start the Backend**:
   ```bash
   cd api
   uvicorn app.main:app --reload --port 2020
   ```

2. **Start the Frontend**:
   ```bash
   cd web
   npm run dev
   ```

3. **Verify Connection**:
   - Open http://localhost:2000 in your browser
   - Check browser console for any CORS errors
   - Verify data is loading (currently using mock data)

## Full Stack Integration Testing

### Step 1: Start Both Servers

Terminal 1 (Backend):
```bash
cd api
uvicorn app.main:app --reload --port 2020
```

Terminal 2 (Frontend):
```bash
cd web
npm run dev
```

### Step 2: Verify Backend is Working

Terminal 3:
```bash
cd api
python verify_api.py
```

You should see:
```
✓ Root endpoint: 200
✓ Health check: 200
✓ GET /api/apiaries: 200
ℹ Found 3 apiaries
  - Backyard Garden (Sunnyvale, CA) - 5 hives
  - Hillside Meadow (45.123, -122.456) - 8 hives
  - Riverbend Apiary (Cloverdale, OR) - 3 hives
...
```

### Step 3: Test Frontend

1. Open http://localhost:2000
2. You should see the apiary list
3. Click on an apiary to see hives
4. Click on a hive to see details

### Common Issues

**CORS Errors**:
- Make sure API is running on port 2020
- Check CORS_ORIGINS in api/.env includes http://localhost:2000

**Connection Refused**:
- Verify API is running: `curl http://localhost:2020/health`
- Check ports are correct (web: 2000, api: 2020)

**404 Errors**:
- API endpoints should start with `/api/`
- Example: `http://localhost:2020/api/apiaries`

## Test Coverage

### Backend Tests Cover:

- ✅ Root and health endpoints
- ✅ Apiaries CRUD operations
- ✅ Hives CRUD operations
- ✅ Filtering hives by apiary
- ✅ Alerts (get, dismiss)
- ✅ Recommendations by hive
- ✅ Weather endpoint

### Frontend Tests (To Be Added):

- Component unit tests
- Integration tests with API
- E2E tests with Playwright/Cypress

## Next Steps

After verifying everything works:

1. Connect React to FastAPI (replace mock data with API calls)
2. Add loading states and error handling
3. Add more comprehensive tests
4. Set up CI/CD pipeline
