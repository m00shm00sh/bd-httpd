from datetime import datetime, timedelta, UTC
from secrets import token_hex
from uuid import UUID

from sqlalchemy import insert, select, update
from sqlalchemy.ext.asyncio import AsyncConnection

from .tables import Refresh_tokens

# pylint: disable=redefined-builtin
async def create_refresh_token(c: AsyncConnection, id: UUID) -> str:
    token = token_hex(32)
    exp = datetime.now(UTC) + timedelta(days = 60)
    q = (insert(Refresh_tokens)
         .values({"token": token, "user_id": id , "expires_at": exp}))
    await c.execute(q)
    return token

async def get_user_by_refresh(c: AsyncConnection, refresh: str) -> UUID | None:
    exp = datetime.now(UTC)
    q = (select(Refresh_tokens.c.user_id)
         .where(Refresh_tokens.c.token == refresh)
         .where(Refresh_tokens.c.revoked_at == None) # pylint: disable=singleton-comparison
         .where(Refresh_tokens.c.expires_at > exp))
    print(q)
    row = (await c.execute(q)).one_or_none()
    if row is None:
        return None
    return row[0]

async def revoke_token(c: AsyncConnection, refresh: str) -> None:
    now = datetime.now(UTC)
    q = (update(Refresh_tokens).values(revoked_at=now).where(Refresh_tokens.c.token == refresh))
    print(q)
    await c.execute(q)
