from fastapi import APIRouter, Depends, File, UploadFile, HTTPException, status, Form
from sqlalchemy.orm import Session
from typing import Optional

from app.database import get_db
from app.models import User
from app.routers.auth import get_current_user
from app.services.bunny_storage_service import bunny_storage
from app.services.ai_analysis_service import ai_analysis_service

router = APIRouter(prefix="/photos", tags=["photos"])


@router.post("/upload", status_code=status.HTTP_201_CREATED)
async def upload_photo(
    file: UploadFile = File(...),
    folder: str = Form("inspections"),
    current_user: User = Depends(get_current_user),
):
    """
    Upload a photo to Bunny.net storage

    Args:
        file: The image file to upload
        folder: The folder to store the file in (default: "inspections")

    Returns:
        Dictionary with the CDN URL of the uploaded photo
    """
    # Validate file type
    allowed_types = ["image/jpeg", "image/jpg", "image/png", "image/webp"]
    if file.content_type not in allowed_types:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Invalid file type. Allowed: {', '.join(allowed_types)}",
        )

    # Validate file size (max 10MB)
    file_content = await file.read()
    if len(file_content) > 10 * 1024 * 1024:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="File size exceeds 10MB limit",
        )

    # Upload to Bunny.net
    try:
        cdn_url = await bunny_storage.upload_photo(
            file_content=file_content, filename=file.filename or "photo.jpg", folder=folder
        )

        if not cdn_url:
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail="Failed to upload photo to storage",
            )

        return {
            "success": True,
            "url": cdn_url,
            "filename": file.filename,
            "size": len(file_content),
        }

    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail=str(e)
        )


@router.post("/analyze")
async def analyze_photo(
    image_url: str = Form(...),
    analysis_type: str = Form("general"),
    current_user: User = Depends(get_current_user),
):
    """
    Analyze a photo using Claude Vision AI

    Args:
        image_url: URL of the image to analyze
        analysis_type: Type of analysis (general, queen, brood, pests, health)

    Returns:
        Dictionary with AI analysis results including findings and recommendations
    """
    valid_types = ["general", "queen", "brood", "pests", "health"]
    if analysis_type not in valid_types:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Invalid analysis type. Allowed: {', '.join(valid_types)}",
        )

    try:
        analysis_result = await ai_analysis_service.analyze_hive_photo(
            image_url=image_url, analysis_type=analysis_type
        )

        if not analysis_result:
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail="Failed to analyze photo with AI",
            )

        return {
            "success": True,
            "image_url": image_url,
            "analysis": analysis_result,
        }

    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail=str(e)
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Unexpected error during analysis: {str(e)}",
        )


@router.delete("/delete")
async def delete_photo(
    file_path: str = Form(...),
    current_user: User = Depends(get_current_user),
):
    """
    Delete a photo from Bunny.net storage

    Args:
        file_path: The path of the file to delete (e.g., "inspections/20240101_120000_photo.jpg")

    Returns:
        Success message
    """
    try:
        success = await bunny_storage.delete_photo(file_path)

        if not success:
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail="Failed to delete photo from storage",
            )

        return {"success": True, "message": "Photo deleted successfully"}

    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail=str(e)
        )
