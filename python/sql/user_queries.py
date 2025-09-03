from uuid import UUID

from sqlalchemy import select, insert, update, delete
from sqlalchemy.ext.asyncio import AsyncConnection

from uuid_utils import uuid7

from models import UserEntry, UserResponse
from ._util import coerce_utc
from .tables import Users

# pylint: disable=redefined-builtin
async def create_user(c: AsyncConnection, u: UserEntry) -> UserResponse:
    q = (insert(Users)
         .values({"id": uuid7(), "email": u.email, "pass": u.password})
         .returning(Users.c.id, Users.c.created_at, Users.c.is_chirpy_red))
    id, created, red = (await c.execute(q)).one()
    created = coerce_utc(created)
    return UserResponse(id=id, created_at=created, updated_at=created, email=u.email, is_chirpy_red=red)

# pylint: disable=redefined-builtin
async def get_user_by_email(c: AsyncConnection, email: str) -> tuple[UserEntry, UserResponse] | None:
    q = (select(Users.c['id', 'pass', 'created_at', 'updated_at', 'is_chirpy_red'])
         .where(Users.c.email == email))
    row = (await c.execute(q)).one_or_none()
    if row is None:
        return None
    id, pass_, created, updated, red = row
    created = coerce_utc(created)
    updated = coerce_utc(updated)
    return UserEntry(email, pass_), UserResponse(id=id, created_at=created, updated_at=updated, is_chirpy_red=red)

async def does_user_exist(c: AsyncConnection, id: UUID) -> bool:
    q = (select(Users.c.id).where(Users.c.id == id))
    return (await c.execute(q)).one_or_none() is not None

# pylint: disable=redefined-builtin
async def update_user(c: AsyncConnection, id: UUID, u: UserEntry) -> UserResponse | None:
    q1 = (update(Users)
          .values({"email": u.email, "pass": u.password, "updated_at": None})
          .where(Users.c.id == id))
    q2 = (select(Users.c['created_at', 'updated_at', 'is_chirpy_red'])
          .where(Users.c.id == id))
    if (await c.execute(q1)).rowcount != 1:
        return None
    created, updated, red = (await c.execute(q2)).one()
    created = coerce_utc(created)
    updated = coerce_utc(updated)
    return UserResponse(id=id, created_at=created, updated_at=updated, email=u.email, is_chirpy_red=red)

# pylint: disable=redefined-builtin
async def upgrade_user_to_red(c: AsyncConnection, id: UUID) -> bool:
    q = (update(Users)
         .values(is_chirpy_red = True, updated_at = None)
         .where(Users.c.id == id))
    return (await c.execute(q)).rowcount > 0

async def delete_all_users(c: AsyncConnection):
    q = delete(Users)
    await c.execute(q)
