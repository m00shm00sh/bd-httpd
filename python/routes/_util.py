from typing import Type, TypeVar
T = TypeVar('T')
def require_not_none(x: T | None, et: Type[Exception], *ea, **ekw) -> T:
    if x is None:
        raise et(*ea, **ekw)
    return x