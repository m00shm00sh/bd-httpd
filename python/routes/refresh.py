from fastapi import APIRouter, HTTPException, Path, Query

from deps import auth_bearer_dep, auth_jwtcreate_dep, db_dep
from models import RefreshResponse
import sql.refresh_queries as queries
from ._util import require_not_none

router = APIRouter()

@router.post("/api/refresh")
async def refresh_token(bearer: auth_bearer_dep, gen_jwt: auth_jwtcreate_dep, db: db_dep) -> RefreshResponse:
    uid = require_not_none(await queries.get_user_by_refresh(db, bearer),
                           HTTPException, 401, 'invalid token')
    access = gen_jwt(uid)
    res = RefreshResponse(token=access)
    return res

@router.post("/api/revoke", status_code=204)
async def revoke_token(bearer: auth_bearer_dep, db: db_dep) -> None:
    await queries.revoke_token(db, bearer)
