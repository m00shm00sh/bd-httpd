from typing import Annotated

from fastapi import Depends
from sqlalchemy.ext.asyncio import AsyncConnection

from sql.connection import get_db_conn

db_dep = Annotated[AsyncConnection, Depends(get_db_conn)]
