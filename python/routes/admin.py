from fastapi import APIRouter, HTTPException, Response
from starlette.responses import HTMLResponse

import hitcount
from deps import config_dep, db_dep
from sql.user_queries import delete_all_users

router = APIRouter()

@router.get("/admin/metrics")
async def get_metrics() -> HTMLResponse:
    c = hitcount.hit_count()
    return HTMLResponse(f"""
        <html>
            <body>
                <h1>Welcome, Chirpy Admin</h1>
                <p>Chirpy has been visited {c} times!</p>
            </body>
        </html>
    """)

@router.post("/admin/reset")
async def do_reset(c: config_dep, db: db_dep):
    if not c.is_dev:
        raise HTTPException(403, 'not in dev mode')
    await delete_all_users(db)
    return Response("", media_type="text/plain")
