"""
Beekeeper App - FastAPI Backend
Main application entry point
"""
from fastapi import FastAPI, HTTPException, UploadFile, File
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from datetime import datetime
from typing import List, Optional
import uvicorn

app = FastAPI(
    title="Beekeeper API",
    description="Backend API for beekeeping management and AI analysis",
    version="1.0.0"
)

# CORS middleware for React frontend
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:3000", "http://localhost:5173"],  # React dev servers
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ============================================================================
# Data Models
# ============================================================================

class Hive(BaseModel):
    id: Optional[int] = None
    name: str
    location: str
    installation_date: datetime
    hive_type: str  # Langstroth, Top Bar, Warre, etc.
    notes: Optional[str] = None

class Inspection(BaseModel):
    id: Optional[int] = None
    hive_id: int
    date: datetime
    queen_seen: bool
    brood_pattern: str  # excellent, good, fair, poor
    temperament: str  # calm, moderate, aggressive
    pest_issues: Optional[str] = None
    health_status: str  # healthy, concerning, needs attention
    notes: Optional[str] = None
    photos: Optional[List[str]] = None

class Task(BaseModel):
    id: Optional[int] = None
    hive_id: Optional[int] = None  # None for general tasks
    task_type: str  # inspection, feeding, treatment, harvest, etc.
    title: str
    description: Optional[str] = None
    due_date: datetime
    completed: bool = False
    completed_date: Optional[datetime] = None

class ImageAnalysisRequest(BaseModel):
    image_url: str
    analysis_type: str  # hive_health, bee_count, pest_detection, brood_pattern

class ImageAnalysisResponse(BaseModel):
    analysis_type: str
    findings: dict
    recommendations: List[str]
    confidence: float

# ============================================================================
# In-memory storage (replace with database in production)
# ============================================================================

hives_db = []
inspections_db = []
tasks_db = []

# ============================================================================
# API Endpoints
# ============================================================================

@app.get("/")
async def root():
    return {
        "message": "Beekeeper API",
        "version": "1.0.0",
        "status": "running"
    }

@app.get("/health")
async def health_check():
    return {"status": "healthy"}

# Hive Management
@app.get("/api/hives", response_model=List[Hive])
async def get_hives():
    return hives_db

@app.post("/api/hives", response_model=Hive)
async def create_hive(hive: Hive):
    hive.id = len(hives_db) + 1
    hives_db.append(hive)
    return hive

@app.get("/api/hives/{hive_id}", response_model=Hive)
async def get_hive(hive_id: int):
    for hive in hives_db:
        if hive.id == hive_id:
            return hive
    raise HTTPException(status_code=404, detail="Hive not found")

@app.put("/api/hives/{hive_id}", response_model=Hive)
async def update_hive(hive_id: int, hive: Hive):
    for i, h in enumerate(hives_db):
        if h.id == hive_id:
            hive.id = hive_id
            hives_db[i] = hive
            return hive
    raise HTTPException(status_code=404, detail="Hive not found")

@app.delete("/api/hives/{hive_id}")
async def delete_hive(hive_id: int):
    for i, h in enumerate(hives_db):
        if h.id == hive_id:
            hives_db.pop(i)
            return {"message": "Hive deleted"}
    raise HTTPException(status_code=404, detail="Hive not found")

# Inspection Management
@app.get("/api/inspections", response_model=List[Inspection])
async def get_inspections(hive_id: Optional[int] = None):
    if hive_id:
        return [i for i in inspections_db if i.hive_id == hive_id]
    return inspections_db

@app.post("/api/inspections", response_model=Inspection)
async def create_inspection(inspection: Inspection):
    inspection.id = len(inspections_db) + 1
    inspections_db.append(inspection)
    return inspection

@app.get("/api/inspections/{inspection_id}", response_model=Inspection)
async def get_inspection(inspection_id: int):
    for inspection in inspections_db:
        if inspection.id == inspection_id:
            return inspection
    raise HTTPException(status_code=404, detail="Inspection not found")

# Task Management
@app.get("/api/tasks", response_model=List[Task])
async def get_tasks(hive_id: Optional[int] = None, completed: Optional[bool] = None):
    tasks = tasks_db
    if hive_id is not None:
        tasks = [t for t in tasks if t.hive_id == hive_id]
    if completed is not None:
        tasks = [t for t in tasks if t.completed == completed]
    return tasks

@app.post("/api/tasks", response_model=Task)
async def create_task(task: Task):
    task.id = len(tasks_db) + 1
    tasks_db.append(task)
    return task

@app.put("/api/tasks/{task_id}", response_model=Task)
async def update_task(task_id: int, task: Task):
    for i, t in enumerate(tasks_db):
        if t.id == task_id:
            task.id = task_id
            tasks_db[i] = task
            return task
    raise HTTPException(status_code=404, detail="Task not found")

@app.delete("/api/tasks/{task_id}")
async def delete_task(task_id: int):
    for i, t in enumerate(tasks_db):
        if t.id == task_id:
            tasks_db.pop(i)
            return {"message": "Task deleted"}
    raise HTTPException(status_code=404, detail="Task not found")

# Image Analysis
@app.post("/api/analyze-image", response_model=ImageAnalysisResponse)
async def analyze_image(request: ImageAnalysisRequest):
    """
    Analyze bee/hive images using AI
    TODO: Integrate with Claude Vision API or similar
    """
    # Placeholder implementation
    return ImageAnalysisResponse(
        analysis_type=request.analysis_type,
        findings={
            "status": "analysis_pending",
            "message": "AI vision integration coming soon"
        },
        recommendations=["Connect AI vision service"],
        confidence=0.0
    )

@app.post("/api/upload-image")
async def upload_image(file: UploadFile = File(...)):
    """
    Upload and store hive/bee images
    TODO: Implement file storage (local or cloud)
    """
    # Placeholder implementation
    return {
        "filename": file.filename,
        "content_type": file.content_type,
        "message": "Image upload endpoint - storage implementation pending"
    }

# Weather Integration
@app.get("/api/weather")
async def get_weather(latitude: float, longitude: float):
    """
    Get weather data for optimal inspection timing
    TODO: Integrate with weather API
    """
    return {
        "message": "Weather API integration pending",
        "location": {"lat": latitude, "lon": longitude}
    }

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000, reload=True)
