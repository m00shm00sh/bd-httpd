from fastapi import APIRouter, HTTPException

from models.polka_hook import PolkaHook
from deps import auth_apikey_dep, config_dep, db_dep
from sql.user_queries import upgrade_user_to_red

router = APIRouter()

@router.post("/api/polka/webhooks", status_code=204)
async def polka_webhook(body: PolkaHook, c: config_dep, apikey: auth_apikey_dep, db: db_dep):
    if apikey != c.polka_key:
        raise HTTPException(401)
    if body.event == "user.upgraded":
        if not await upgrade_user_to_red(db, body.data.user_id):
            raise HTTPException(404)
