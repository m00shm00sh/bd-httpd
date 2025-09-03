from atomics.base import AtomicInt

from starlette.responses import Response
from starlette.staticfiles import StaticFiles
from starlette.types import Scope

_c = AtomicInt(width = 4)

class HitcountStaticFiles(StaticFiles):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs, html = True)

    async def get_response(self, path: str, scope: Scope) -> Response:
        response = await super().get_response(path, scope)
        _c.inc()
        return response

def hit_count() -> int:
    return _c.load()
