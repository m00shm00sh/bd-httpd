using aspnet.Auth;
using aspnet.Db;
using aspnet.User;
using Microsoft.AspNetCore.Http.HttpResults;

namespace aspnet.Webhooks;

internal static class RoutingExtensions
{
    public static void AddWebhookApiRoutes(this WebApplication app)
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
                "POLKA_KEY", "Apikeys:Polka")
            )
            .WithName("PolkaWebhook");
    }
}