from sqlalchemy import MetaData, Table, Column, ForeignKey, \
    BOOLEAN, TEXT, TIMESTAMP, UUID, VARCHAR

metadata = MetaData()

_triggers = []

Users = Table(
    'Users', metadata,
    Column('id', UUID, primary_key=True),
    Column('created_at', TIMESTAMP, nullable=False, server_default='current_timestamp'),
    Column('updated_at', TIMESTAMP, server_default='current_timestamp'),
    Column('email', TEXT, nullable=False, unique=True),
    Column('pass', TEXT, nullable=False, server_default=""),
    Column('is_chirpy_red', BOOLEAN, nullable=False, server_default='False'),
)
_triggers.append("""
    CREATE TRIGGER users_set_mtime AFTER UPDATE ON Users
    FOR EACH ROW WHEN NEW.updated_at IS NULL
    BEGIN
        UPDATE Users SET updated_at = CURRENT_TIMESTAMP WHERE updated_at IS NULL;
    END;
""")

Chirps = Table(
    'Chirps', metadata,
    Column('id', UUID, primary_key=True),
    Column('created_at', TIMESTAMP, nullable=False, server_default='current_timestamp'),
    Column('updated_at', TIMESTAMP, server_default='current_timestamp'),
    Column('body', TEXT, nullable=False),
    Column('user_id', ForeignKey(Users.c.id))
)
_triggers.append("""
    CREATE TRIGGER chirps_set_mtime AFTER UPDATE ON Chirps
    FOR EACH ROW WHEN NEW.updated_at IS NULL
    BEGIN
        UPDATE Chirps SET updated_at = CURRENT_TIMESTAMP WHERE updated_at IS NULL;
    END;
""")

Refresh_tokens = Table(
    'Refresh_tokens', metadata,
    Column('token', VARCHAR, primary_key=True),
    Column('created_at', TIMESTAMP, nullable=False, server_default="datetime('now', 'subsec')"),
    Column('updated_at', TIMESTAMP, server_default="datetime('now', 'subsec')"),
    Column('user_id', UUID, ForeignKey(Users.c.id)),
    Column('expires_at', TIMESTAMP, nullable=False),
    Column('revoked_at', TIMESTAMP)
)
_triggers.append("""
CREATE TRIGGER refreshes_set_mtime AFTER UPDATE ON Refresh_tokens
FOR EACH ROW WHEN NEW.updated_at IS NULL
BEGIN
    UPDATE Refresh_tokens SET updated_at = CURRENT_TIMESTAMP WHERE updated_at IS NULL;
END;
""")

triggers = (*_triggers,)

