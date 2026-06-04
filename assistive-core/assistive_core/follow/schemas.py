"""Follow schemas. Ported from beekeeper api/app/schemas/follow.py."""
from pydantic import BaseModel, ConfigDict


def to_camel(string: str) -> str:
    """Convert snake_case to camelCase."""
    words = string.split("_")
    return words[0] + "".join(word.capitalize() for word in words[1:])


class UserSummary(BaseModel):
    """Minimal public view of a user - id + name only, no email/PII."""

    id: str
    full_name: str

    model_config = ConfigDict(
        from_attributes=True, alias_generator=to_camel, populate_by_name=True
    )
