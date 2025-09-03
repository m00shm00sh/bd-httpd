"""init

Revision ID: 271712636952
Revises: 
Create Date: 2025-06-30 23:33:56.730823

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa


# revision identifiers, used by Alembic.
revision: str = '271712636952'
down_revision: Union[str, Sequence[str], None] = None
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None

_sqlite_subsec_now = "datetime('subsec')"
_sqlite_subsec_now_t = sa.text(_sqlite_subsec_now)

def upgrade() -> None:
    triggers = []
    from sqlalchemy import Column, ForeignKey, \
        BOOLEAN, TEXT, TIMESTAMP, UUID, VARCHAR
    op.create_table('Users',
        Column('id', UUID, primary_key=True),
        Column('created_at', TIMESTAMP, nullable=False, server_default=_sqlite_subsec_now_t),
        Column('updated_at', TIMESTAMP, server_default=_sqlite_subsec_now_t),
        Column('email', TEXT, nullable=False, unique=True),
        Column('pass', TEXT, nullable=False, server_default=""),
        Column('is_chirpy_red', BOOLEAN, nullable=False, server_default=sa.text('false')),
    )
    triggers.append(f""" 
        CREATE TRIGGER users_set_mtime AFTER UPDATE ON Users
        FOR EACH ROW WHEN NEW.updated_at IS NULL
        BEGIN
            UPDATE Users SET updated_at = {_sqlite_subsec_now} WHERE updated_at IS NULL;
        END;
    """)
    op.create_table('Chirps',
        Column('id', UUID, primary_key=True),
        Column('created_at', TIMESTAMP, nullable=False, server_default=_sqlite_subsec_now_t),
        Column('updated_at', TIMESTAMP, server_default=_sqlite_subsec_now_t),
        Column('body', TEXT, nullable=False),
        Column('user_id', UUID, ForeignKey('Users.id', ondelete='cascade'))
    )
    triggers.append(f"""
        CREATE TRIGGER chirps_set_mtime AFTER UPDATE ON Chirps
        FOR EACH ROW WHEN NEW.updated_at IS NULL
        BEGIN
            UPDATE Chirps SET updated_at = {_sqlite_subsec_now} WHERE updated_at IS NULL;
        END;
    """)

    op.create_table('Refresh_tokens',
        Column('token', VARCHAR, primary_key=True),
        Column('created_at', TIMESTAMP, nullable=False, server_default=_sqlite_subsec_now_t),
        Column('updated_at', TIMESTAMP, server_default=_sqlite_subsec_now_t),
        Column('user_id', UUID, ForeignKey('Users.id', ondelete='cascade')),
        Column('expires_at', TIMESTAMP, nullable=False),
        Column('revoked_at', TIMESTAMP)
    )
    triggers.append(f"""
    CREATE TRIGGER refreshes_set_mtime AFTER UPDATE ON Refresh_tokens
    FOR EACH ROW WHEN NEW.updated_at IS NULL
    BEGIN
        UPDATE Refresh_tokens SET updated_at = {_sqlite_subsec_now} WHERE updated_at IS NULL;
    END;
    """)
    for t in triggers:
        op.execute(sa.text(t))

def downgrade() -> None:
    triggers = ['users_set_mtime', 'chirps_set_mtime', 'refreshes_set_mtime']
    for t in triggers:
        op.execute(f"DROP TRIGGER {t}")
    tables = ['Users', 'Chirps', 'Refresh_tokens']
    for t in tables[::-1]:
        op.drop_table(t)