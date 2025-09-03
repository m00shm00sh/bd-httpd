from dataclasses import dataclass
from datetime import datetime
from typing import Self
from uuid import UUID

from pydantic import BaseModel, Field

class ChirpRequest(BaseModel):
    body: str

_profane = frozenset(["kerfuffle", "sharbert", "fornax"])
def _clean_text(s: str) -> str:
    words = [("****" if w.lower() in _profane else w) for w in s.split()]
    return " ".join(words)

@dataclass
class ChirpEntry:
    body: str

    @classmethod
    def from_request(cls, ch: ChirpRequest) -> Self:
        return cls(body=_clean_text(ch.body))

class ChirpResponse(BaseModel):
    id: UUID
    created_at: datetime
    updated_at: datetime
    body: str
    user_id: UUID
