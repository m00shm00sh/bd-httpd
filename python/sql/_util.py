from datetime import datetime, timezone

def coerce_utc(dt: datetime) -> datetime:
    return dt.replace(tzinfo=timezone.utc)
