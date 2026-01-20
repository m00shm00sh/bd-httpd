namespace aspnet.Webhooks;

internal record PolkaRequest(string Event, PolkaData Data);

internal record PolkaData(Guid UserId);