using aspnet.Auth;
using aspnet.Db;
using Microsoft.AspNetCore.Http.HttpResults;

namespace aspnet.Refreshes;

internal static class RoutingExtensions
{
    public static void AddRefreshRoutes(this WebApplication app)
    {
        app.MapPost("/api/refresh",
            async Task<Results<Ok<Response>, UnauthorizedHttpResult>> (
                HttpRequest req, TokenService tokSvc, BdChirpyContext repo, CancellationToken ct) =>
            {
                var bearer = req._getAuthBearer();
                if (bearer is null)
                    return TypedResults.Unauthorized();
                var userId = await repo.FindUserByRefreshToken(bearer, ct);
                if (userId is null)
                    return TypedResults.Unauthorized();
                var token = tokSvc.GenerateToken(userId.Value);
                return TypedResults.Ok(new Response(token));
            })
            .WithName("LoginByRefreshToken");
        
        app.MapPost("/api/revoke",
            async (HttpRequest req, BdChirpyContext repo, CancellationToken ct) =>
            {
                var bearer = req._getAuthBearer();
                if (bearer is null)
                    return TypedResults.NoContent();
                await repo.RevokeRefreshToken(bearer, ct);
                return TypedResults.NoContent();
            })
            .WithName("RevokeRefreshToken");
    }

    private static string? _getAuthBearer(this HttpRequest req)
    {
        string? bearer = null;
        
        foreach (var s in req.Headers.Authorization)
        {
            if (string.IsNullOrWhiteSpace(s))
                continue;
            var toks = s.Split(' ', 2);
            if (toks is not ["Bearer", _]) continue;
            bearer = toks[1];
            break;
        }
        
        return bearer;
    }
}