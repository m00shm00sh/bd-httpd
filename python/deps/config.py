from typing import Annotated

from fastapi import Depends

from config import Config, get_config

config_dep = Annotated[Config, Depends(get_config)]
