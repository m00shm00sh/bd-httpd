from fastapi import APIRouter, HTTPException

from deps import auth_jwtcreate_dep, auth_jwtuser_dep, db_dep
from models import UserRequest, UserResponse, UserResponseWithToken, UserEntry
from sql import user_queries as uq
from sql.refresh_queries import create_refresh_token

from ._util import require_not_none

router = APIRouter()

@router.post("/api/users", status_code=201)
async def register_user(u: UserRequest, db: db_dep) -> UserResponse:
    user = await uq.create_user(db, UserEntry.from_request(u))
    return user

@router.put("/api/users")
async def update_user(u: UserRequest, uid: auth_jwtuser_dep, db: db_dep) -> UserResponse:
    resp = await uq.update_user(db, uid, UserEntry.from_request(u))
    return resp

@router.post("/api/login")
async def login_user(u: UserRequest, db: db_dep, gen_jwt: auth_jwtcreate_dep) -> UserResponseWithToken:
    u_loginentry, u_resp = require_not_none(await uq.get_user_by_email(db, u.email),
                                            HTTPException, 401, 'bad login')
    if not u_loginentry.test_password(u.password.get_secret_value()):
        raise HTTPException(401, 'bad login')
    u_resp.email = u.email
    access = gen_jwt(u_resp.id)
    refresh = await create_refresh_token(db, u_resp.id)
    return UserResponseWithToken.from_response(u_resp, access, refresh)

