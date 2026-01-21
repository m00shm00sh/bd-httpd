using Microsoft.AspNetCore.Http.HttpResults;

using aspnet.Auth;
using aspnet.Db;
using aspnet.User;

namespace aspnet.Webhooks;

internal static class RoutingExtensions
{
    extension(WebApplication app)
    {
        public void AddWebhookApiRoutes()
        {
            app.MapPost("/api/polka/webhooks",
                async Task<Results<NoContent, NotFound>> (PolkaRequest req, BdChirpyContext repo,
                    CancellationToken ct) =>
                {
                    return req.Event switch
                    {
                        "user.upgraded" =>
                            await repo.UpgradeUserToRed(req.Data.UserId, ct)
                                ? TypedResults.NoContent()
                                : TypedResults.NotFound(),
                        _ => TypedResults.NoContent()
                    };
                })
                .AddEndpointFilter(new ApikeyEndpointFilter(app.Configuration,
                    "POLKA_KEY", "Apikeys:Polka"))
                .WithName("PolkaWebhook");
        }
    }
}