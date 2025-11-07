import os
import base64
import httpx
from typing import Dict, List, Optional
from anthropic import Anthropic


class AIAnalysisService:
    """Service for analyzing hive photos using Claude Vision API"""

    def __init__(self):
        self.api_key = os.getenv("ANTHROPIC_API_KEY", "")
        self.client = Anthropic(api_key=self.api_key) if self.api_key else None

    async def analyze_hive_photo(
        self, image_url: str, analysis_type: str = "general"
    ) -> Optional[Dict]:
        """
        Analyze a hive photo using Claude Vision

        Args:
            image_url: URL of the image to analyze
            analysis_type: Type of analysis (general, queen, brood, pests, health)

        Returns:
            Dictionary with analysis results including findings and recommendations
        """
        if not self.client:
            raise ValueError("Anthropic API key not configured")

        # Download image from URL
        async with httpx.AsyncClient() as http_client:
            try:
                response = await http_client.get(image_url, timeout=30.0)
                response.raise_for_status()
                image_data = response.content
            except httpx.HTTPError as e:
                print(f"Failed to download image: {e}")
                return None

        # Encode image to base64
        image_base64 = base64.standard_b64encode(image_data).decode("utf-8")

        # Determine media type
        media_type = "image/jpeg"
        if image_url.lower().endswith(".png"):
            media_type = "image/png"
        elif image_url.lower().endswith(".webp"):
            media_type = "image/webp"
        elif image_url.lower().endswith(".gif"):
            media_type = "image/gif"

        # Create analysis prompt based on type
        prompt = self._get_analysis_prompt(analysis_type)

        try:
            # Call Claude Vision API
            message = self.client.messages.create(
                model="claude-3-5-sonnet-20241022",
                max_tokens=1024,
                messages=[
                    {
                        "role": "user",
                        "content": [
                            {
                                "type": "image",
                                "source": {
                                    "type": "base64",
                                    "media_type": media_type,
                                    "data": image_base64,
                                },
                            },
                            {"type": "text", "text": prompt},
                        ],
                    }
                ],
            )

            # Parse response
            analysis_text = message.content[0].text

            # Structure the response
            result = {
                "analysis_type": analysis_type,
                "findings": self._parse_findings(analysis_text),
                "recommendations": self._parse_recommendations(analysis_text),
                "full_analysis": analysis_text,
                "confidence": "high",  # Claude doesn't return confidence, so we default
            }

            return result

        except Exception as e:
            print(f"Failed to analyze image with Claude: {e}")
            return None

    def _get_analysis_prompt(self, analysis_type: str) -> str:
        """Get appropriate prompt based on analysis type"""

        prompts = {
            "general": """
Analyze this beehive inspection photo. Provide a detailed assessment including:

1. **Hive Health**: Overall health status and any visible concerns
2. **Queen Detection**: Whether a queen bee is visible (marked or unmarked)
3. **Brood Pattern**: Quality of brood pattern (excellent, good, spotty, poor, or none visible)
4. **Colony Population**: Estimated bee population strength (very weak to very strong)
5. **Pests & Diseases**: Any visible pests (varroa mites, beetles, moths) or disease signs
6. **Resources**: Visible honey or pollen stores
7. **Temperament Indicators**: Any visual cues about colony temperament

Please structure your response with clear sections and specific observations.
Then provide 2-3 actionable recommendations for the beekeeper.
""",
            "queen": """
Analyze this photo specifically for queen bee detection:

1. Is there a queen bee visible in this image?
2. If yes, is she marked? What color is the mark?
3. Describe her location and appearance
4. Are there any queen cells visible?
5. Any signs of supersedure or swarm preparation?

Provide specific recommendations related to queen management.
""",
            "brood": """
Analyze the brood pattern in this photo:

1. **Brood Pattern Quality**: Rate as excellent, good, spotty, or poor
2. **Brood Stages**: Are eggs, larvae, or capped brood visible?
3. **Pattern Distribution**: Is the pattern solid or scattered?
4. **Concerns**: Any unusual cells, dead brood, or abnormalities?
5. **Population Estimate**: Based on brood, estimate future colony strength

Provide recommendations for brood management.
""",
            "pests": """
Analyze this photo for pest detection:

1. **Varroa Mites**: Any visible mites on bees or cells?
2. **Small Hive Beetles**: Any beetles visible?
3. **Wax Moths**: Any signs of webbing or larvae?
4. **Other Pests**: Ants, wasps, mice droppings, etc.?
5. **Severity**: Rate pest pressure (none, low, moderate, high, severe)

Provide immediate and long-term pest management recommendations.
""",
            "health": """
Analyze this photo for colony health indicators:

1. **Overall Health Status**: Excellent, healthy, concerning, needs attention, or critical
2. **Disease Signs**: Any signs of foulbrood, chalkbrood, nosema, or viruses?
3. **Deformed Wings**: Any bees with deformed wings (virus indicator)?
4. **Dead Bees**: Unusual number of dead bees visible?
5. **Hygiene**: Do bees appear clean and well-groomed?

Provide health management and treatment recommendations.
""",
        }

        return prompts.get(analysis_type, prompts["general"])

    def _parse_findings(self, analysis_text: str) -> Dict[str, str]:
        """Extract key findings from analysis text"""
        # Simple parsing - in production, you'd want more sophisticated NLP
        findings = {}

        # Look for common patterns
        if "queen" in analysis_text.lower():
            if "visible" in analysis_text.lower() or "seen" in analysis_text.lower():
                findings["queen_detected"] = "yes"
            else:
                findings["queen_detected"] = "no"

        if "excellent" in analysis_text.lower():
            findings["brood_pattern"] = "excellent"
        elif "good" in analysis_text.lower():
            findings["brood_pattern"] = "good"
        elif "spotty" in analysis_text.lower():
            findings["brood_pattern"] = "spotty"
        elif "poor" in analysis_text.lower():
            findings["brood_pattern"] = "poor"

        if "mite" in analysis_text.lower() or "varroa" in analysis_text.lower():
            findings["varroa_concern"] = "detected or mentioned"

        findings["summary"] = analysis_text[:200] + "..." if len(analysis_text) > 200 else analysis_text

        return findings

    def _parse_recommendations(self, analysis_text: str) -> List[str]:
        """Extract recommendations from analysis text"""
        recommendations = []

        # Look for recommendation sections
        lines = analysis_text.split("\n")
        in_recommendations = False

        for line in lines:
            line = line.strip()
            if not line:
                continue

            # Detect recommendation section
            if "recommendation" in line.lower():
                in_recommendations = True
                continue

            # Extract numbered or bulleted recommendations
            if in_recommendations and (
                line.startswith("-")
                or line.startswith("•")
                or line.startswith("*")
                or (len(line) > 2 and line[0].isdigit() and line[1] in [".", ")"])
            ):
                # Clean up the recommendation text
                rec = line.lstrip("-•*0123456789.) ").strip()
                if rec:
                    recommendations.append(rec)

        # If no recommendations found, add a default
        if not recommendations:
            recommendations.append("Continue regular monitoring and inspections")

        return recommendations[:5]  # Return max 5 recommendations


# Singleton instance
ai_analysis_service = AIAnalysisService()
