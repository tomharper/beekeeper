from fastapi import APIRouter, HTTPException, Depends
from sqlalchemy.orm import Session
from pydantic import BaseModel
from anthropic import Anthropic
import os
import uuid
from datetime import datetime
from typing import List

from app.database import get_db
from app.services import AIAdvisorService
from app.schemas import AlertResponse

router = APIRouter()

# Initialize Anthropic client
anthropic_client = None


def get_anthropic_client():
    """Get or create Anthropic client"""
    global anthropic_client
    if anthropic_client is None:
        api_key = os.getenv("ANTHROPIC_API_KEY")
        if not api_key:
            raise HTTPException(
                status_code=500,
                detail="ANTHROPIC_API_KEY not configured. Please add it to your .env file.",
            )
        anthropic_client = Anthropic(api_key=api_key)
    return anthropic_client


class ChatRequest(BaseModel):
    message: str


class ChatMessage(BaseModel):
    id: str
    content: str
    role: str
    timestamp: str


@router.get("/advisor/alerts", response_model=List[AlertResponse])
async def get_advisor_alerts(db: Session = Depends(get_db)):
    """
    Get intelligent alerts and recommendations from AI Advisor.

    Analyzes hive data to generate actionable alerts for:
    - Overdue tasks
    - Health issues detected in inspections
    - Pest/disease warnings
    - Queen problems
    - Low resource levels
    - Seasonal reminders
    """
    try:
        advisor_service = AIAdvisorService(db)
        alerts = advisor_service.generate_alerts()
        return alerts
    except Exception as e:
        print(f"Error generating alerts: {str(e)}")
        raise HTTPException(
            status_code=500,
            detail=f"Failed to generate alerts: {str(e)}",
        )


@router.post("/chat", response_model=ChatMessage)
async def chat(request: ChatRequest):
    """
    Chat with AI beekeeping advisor powered by Claude.

    Provides expert guidance on:
    - Hive management and inspection
    - Colony health assessment
    - Pest and disease identification
    - Seasonal beekeeping tasks
    - Equipment and best practices
    """
    try:
        client = get_anthropic_client()

        # Call Claude API with beekeeping expertise
        message = client.messages.create(
            model="claude-3-5-sonnet-20241022",
            max_tokens=1024,
            system="""You are an expert beekeeping advisor with decades of experience.
You provide clear, practical advice on all aspects of beekeeping including:
- Hive inspections and what to look for
- Colony health, queen management, and brood patterns
- Pest control (varroa mites, small hive beetles, wax moths, etc.)
- Disease prevention and treatment (American/European foulbrood, nosema, etc.)
- Seasonal management (spring buildup, summer flow, fall prep, winter survival)
- Equipment selection and hive configurations
- Honey harvesting and extraction
- Swarm prevention and management
- Best practices for sustainable beekeeping

Always provide specific, actionable guidance. When discussing treatments or interventions,
mention safety considerations. Be encouraging but realistic about challenges.""",
            messages=[
                {"role": "user", "content": request.message}
            ],
        )

        # Extract response text
        response_text = message.content[0].text

        # Return formatted response
        return ChatMessage(
            id=str(uuid.uuid4()),
            content=response_text,
            role="assistant",
            timestamp=datetime.now().isoformat(),
        )

    except Exception as e:
        # Log the error (in production, use proper logging)
        print(f"Chat error: {str(e)}")
        raise HTTPException(
            status_code=500,
            detail=f"Failed to get AI response: {str(e)}",
        )
