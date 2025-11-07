import os
import httpx
from typing import Optional
from datetime import datetime


class BunnyStorageService:
    """Service for uploading photos to Bunny.net storage"""

    def __init__(self):
        self.storage_zone = os.getenv("BUNNY_STORAGE_ZONE", "")
        self.api_key = os.getenv("BUNNY_API_KEY", "")
        self.cdn_url = os.getenv("BUNNY_CDN_URL", "")
        self.storage_url = f"https://storage.bunnycdn.com/{self.storage_zone}"

    async def upload_photo(
        self, file_content: bytes, filename: str, folder: str = "inspections"
    ) -> Optional[str]:
        """
        Upload a photo to Bunny.net storage

        Args:
            file_content: The binary content of the file
            filename: The name of the file
            folder: The folder to store the file in (default: "inspections")

        Returns:
            The CDN URL of the uploaded file, or None if upload failed
        """
        if not self.api_key or not self.storage_zone:
            raise ValueError("Bunny.net credentials not configured")

        # Generate unique filename with timestamp
        timestamp = datetime.utcnow().strftime("%Y%m%d_%H%M%S")
        unique_filename = f"{timestamp}_{filename}"
        file_path = f"{folder}/{unique_filename}"

        # Upload to Bunny.net
        upload_url = f"{self.storage_url}/{file_path}"
        headers = {
            "AccessKey": self.api_key,
            "Content-Type": "application/octet-stream",
        }

        async with httpx.AsyncClient() as client:
            try:
                response = await client.put(
                    upload_url, content=file_content, headers=headers, timeout=30.0
                )
                response.raise_for_status()

                # Return CDN URL
                cdn_url = f"{self.cdn_url}/{file_path}"
                return cdn_url

            except httpx.HTTPError as e:
                print(f"Failed to upload to Bunny.net: {e}")
                return None

    async def delete_photo(self, file_path: str) -> bool:
        """
        Delete a photo from Bunny.net storage

        Args:
            file_path: The path of the file to delete (e.g., "inspections/20240101_120000_photo.jpg")

        Returns:
            True if deletion was successful, False otherwise
        """
        if not self.api_key or not self.storage_zone:
            raise ValueError("Bunny.net credentials not configured")

        delete_url = f"{self.storage_url}/{file_path}"
        headers = {"AccessKey": self.api_key}

        async with httpx.AsyncClient() as client:
            try:
                response = await client.delete(delete_url, headers=headers, timeout=10.0)
                response.raise_for_status()
                return True
            except httpx.HTTPError as e:
                print(f"Failed to delete from Bunny.net: {e}")
                return False


# Singleton instance
bunny_storage = BunnyStorageService()
