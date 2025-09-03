from datetime import datetime, timedelta, UTC
from typing import Annotated, Callable
from uuid import UUID

from fastapi import Depends, Header, HTTPException
import jwt
from jwt import InvalidTokenError

from .config import config_dep
from .db import db_dep
from sql.user_queries import does_user_exist

def _auth_header(scheme: str):
    def do_get(authorization: Annotated[list[str] | None, Header()] = None) -> str:
        if authorization is None:
            raise HTTPException(status_code=401)
        buf = [toks[1] for h in authorization
               if (toks := h.split(None, maxsplit=1))
               and len(toks) == 2 and toks[0] == scheme]
        return buf[0]
    return do_get

auth_bearer_dep = Annotated[str, Depends(_auth_header("Bearer"))]
auth_apikey_dep = Annotated[str, Depends(_auth_header("ApiKey"))]

_JWT_ALG = "HS256"

TokenCreator = Callable[[UUID], str]

def _jwt_create_token(c: config_dep) -> TokenCreator:
    def do_create(user: UUID):
        exp = datetime.now(UTC) + c.jwt_exp
        return (jwt.encode(
            {
                "sub": user.hex, "exp": exp, "iss": c.jwt_issuer
            },
            c.jwt_secret, algorithm=_JWT_ALG)
    )
    return do_create

async def _jwt_get_user(bearer: auth_bearer_dep, c: config_dep, db_conn: db_dep) -> UUID:
    _401 = HTTPException(status_code=401)
    try:
        payload = jwt.decode(bearer, c.jwt_secret, algorithms=[_JWT_ALG])
        username = payload.get("sub")
        if username is None:
            raise _401
    except (InvalidTokenError, ValueError):
        raise _401
    uuid = UUID(username)
    if not (await does_user_exist(db_conn, uuid)):
        raise _401
    return uuid

auth_jwtcreate_dep = Annotated[TokenCreator, Depends(_jwt_create_token)]
auth_jwtuser_dep = Annotated[UUID, Depends(_jwt_get_user)]