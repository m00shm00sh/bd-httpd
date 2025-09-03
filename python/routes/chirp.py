from typing import Annotated
from uuid import UUID

from fastapi import APIRouter, HTTPException, Path, Response

from deps import auth_jwtuser_dep, db_dep
from models import ChirpRequest, ChirpEntry, ChirpResponse
import sql.chirp_queries as queries
from ._util import require_not_none

router = APIRouter()

@router.post("/api/chirps", status_code=201)
async def create_chirp(ch: ChirpRequest, user: auth_jwtuser_dep, db: db_dep) -> ChirpResponse:
    # we can't do this in the pydantic model because it will throw 422 because the request can't be processed
    # due to validation failure
    if len(ch.body) > 140:
        raise HTTPException(400, 'Chirp too long')
    res = await queries.create_chirp(db, user, ChirpEntry.from_request(ch))
    return res

@router.get("/api/chirps")
async def get_chirps(*, author_id: UUID | None = None, sort: str = "asc", db: db_dep) -> list[ChirpResponse]:
    res = await queries.get_chirps(db, author_id)
    match sort:
        case "asc":
            res.sort(key=lambda k: k.created_at)
        case "desc":
            res.sort(key=lambda k: k.created_at, reverse=True)
        case _:
            raise ValueError('bad sort direction')
    return res

@router.get("/api/chirps/{cid}")
async def get_chirp_by_id(cid: Annotated[UUID, Path(title="chirp ID")], db: db_dep) -> ChirpResponse:
    res = require_not_none(await queries.get_chirp_by_id(db, cid),
                           HTTPException, 404, 'no such chirp')
    return res

@router.delete("/api/chirps/{cid}", status_code=204)
async def delete_chirp(user: auth_jwtuser_dep, cid: UUID, db: db_dep):
    c = require_not_none(await queries.get_chirp_by_id(db, cid),
                         HTTPException, 404, 'no such chirp')
    print(c.user_id, user)
    if c.user_id != user:
        raise HTTPException(403, 'cannot delete someone else\'s chirp')
    await queries.delete_chirp(db, cid)

