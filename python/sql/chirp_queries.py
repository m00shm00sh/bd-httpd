from uuid import UUID

from sqlalchemy import select, insert, delete
from sqlalchemy.ext.asyncio import AsyncConnection

from uuid_utils import uuid7

from models import ChirpEntry, ChirpResponse
from ._util import coerce_utc
from .tables import Chirps

# pylint: disable=redefined-builtin
async def create_chirp(c: AsyncConnection, uid: UUID, ch: ChirpEntry) -> ChirpResponse:
    q = (insert(Chirps)
         .values({"id": uuid7(), "body": ch.body, "user_id": uid})
         .returning(Chirps.c.id, Chirps.c.created_at))
    id, created = (await c.execute(q)).one()
    created = coerce_utc(created)
    return ChirpResponse(id=id, created_at=created, updated_at=created, body=ch.body, user_id=uid)

# pylint: disable=redefined-builtin
async def get_chirps(c: AsyncConnection, uid: UUID | None) -> list[ChirpResponse]:
    q = (select(Chirps.c['id', 'created_at', 'updated_at', 'body', 'user_id'])
         .where(Chirps.c.updated_at is not None))
    if uid is not None:
        q = q.where(Chirps.c.user_id == uid)
    rows = (await c.execute(q)).all()
    return [
        ChirpResponse(id=id, created_at=coerce_utc(ca), updated_at=coerce_utc(ua), body=b, user_id=uid)
        for (id, ca, ua, b, uid) in rows
    ]

# pylint: disable=redefined-builtin
async def get_chirp_by_id(c: AsyncConnection, id: UUID) -> ChirpResponse | None:
    q = (select(Chirps.c['id', 'created_at', 'updated_at', 'body', 'user_id'])
         .where(Chirps.c.id == id))
    chirp = (await c.execute(q)).one_or_none()
    if chirp is None:
        return None
    id, ca, ua, b, uid = chirp
    ca = coerce_utc(ca)
    ua = coerce_utc(ua)
    return ChirpResponse(id=id, created_at=ca, updated_at=ua, body=b, user_id=uid)

# pylint: disable=redefined-builtin
async def delete_chirp(c: AsyncConnection, id: UUID):
    q = (delete(Chirps).where(Chirps.c.id == id))
    await c.execute(q)
