from fastapi import FastAPI
from fastapi.responses import PlainTextResponse

import routes.admin
import routes.user
import routes.refresh
import routes.chirp
import routes.polka

from hitcount import HitcountStaticFiles

app = FastAPI()

app.mount("/app/", HitcountStaticFiles(directory="../static"))

@app.get("/api/healthz", response_class=PlainTextResponse)
async def healthz():
    return "OK"

app.include_router(routes.admin.router)
app.include_router(routes.user.router)
app.include_router(routes.refresh.router)
app.include_router(routes.polka.router)
app.include_router(routes.chirp.router)

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, port=8080)
