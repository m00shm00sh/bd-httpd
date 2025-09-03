from datetime import timedelta
from functools import lru_cache
from pydantic_settings import BaseSettings, SettingsConfigDict

class Config(BaseSettings):
    db_url: str
    is_dev: bool = False
    jwt_secret: str
    jwt_issuer: str
    polka_key: str
    jwt_exp: timedelta = timedelta(hours=1)
    refresh_exp: timedelta = timedelta(days=60)

    model_config = SettingsConfigDict(env_file=".env")

@lru_cache
def get_config():
    return Config()
