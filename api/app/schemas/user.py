from pydantic import BaseModel, EmailStr, Field, ConfigDict


def to_camel(string: str) -> str:
    """Convert snake_case to camelCase"""
    words = string.split('_')
    return words[0] + ''.join(word.capitalize() for word in words[1:])


class UserBase(BaseModel):
    email: EmailStr
    full_name: str = Field(..., serialization_alias="fullName", validation_alias="fullName")


class UserCreate(UserBase):
    password: str
    model_config = ConfigDict(
        populate_by_name=True
    )


class UserLogin(BaseModel):
    email: EmailStr
    password: str


class UserResponse(UserBase):
    id: str
    model_config = ConfigDict(
        from_attributes=True,
        alias_generator=to_camel,
        populate_by_name=True
    )


class Token(BaseModel):
    access_token: str = Field(..., serialization_alias="accessToken")
    token_type: str = Field(..., serialization_alias="tokenType")
    user: UserResponse
    model_config = ConfigDict(
        alias_generator=to_camel,
        populate_by_name=True
    )
