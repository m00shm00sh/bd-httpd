from dataclasses import dataclass
from datetime import datetime
from typing import Self
from uuid import UUID

from passlib.context import CryptContext
from pydantic import AwareDatetime, BaseModel, SecretStr


class UserRequest(BaseModel):
    email: str
    password: SecretStr


_password_crypter = CryptContext(schemes=["bcrypt"])


@dataclass
class UserEntry:  # no-pydantic
    """
    UserRequest with password represented in a form that is safe for storage
    """
    email: str
    password: str

    @classmethod
    def from_request(cls, u: UserRequest) -> Self:
        return cls(u.email, _password_crypter.hash(u.password.get_secret_value()))

    def test_password(self, test_password: str):
        return _password_crypter.verify(test_password, self.password)


class UserResponse(BaseModel):
    id: UUID
    created_at: AwareDatetime
    updated_at: AwareDatetime
    email: str | None = None
    is_chirpy_red: bool


class UserResponseWithToken(BaseModel):
    id: UUID
    created_at: AwareDatetime
    updated_at: AwareDatetime
    token: str
    refresh_token: str
    email: str
    is_chirpy_red: bool

    @classmethod
    def from_response(cls, u: UserResponse, access_token: str, refresh_token: str) -> Self:
        return cls(**u.model_dump(), token=access_token, refresh_token=refresh_token)
