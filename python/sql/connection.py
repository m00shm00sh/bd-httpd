from contextlib import asynccontextmanager
from typing import Any, AsyncIterator

from sqlalchemy.ext.asyncio import AsyncConnection, create_async_engine

from config import get_config

class _ConnectionManager:
    def __init__(self, url: str, **sqlalchemy_kwargs: Any):
        self._engine = create_async_engine(url, **sqlalchemy_kwargs)

    async def close(self):
        self._require_open()
        await self._engine.dispose()
        self._engine = None

    @asynccontextmanager
    async def connect(self) -> AsyncIterator[AsyncConnection]:
        self._require_open()
        async with self._engine.begin() as connection:
            try:
                yield connection
            finally:
                await connection.close()

    def _require_open(self):
        if self._engine is None:
            raise RuntimeError("closed connection pool")

conn_manager = _ConnectionManager(get_config().db_url, echo="debug")

async def get_db_conn():
    async with conn_manager.connect() as c:
        await c.exec_driver_sql('pragma foreign_keys=ON')
        yield c
        await c.commit()
