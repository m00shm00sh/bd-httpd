from uuid import UUID

from pydantic import BaseModel

class PolkaData(BaseModel):
    user_id: UUID

class PolkaHook(BaseModel):
    event: str
    data: PolkaData
