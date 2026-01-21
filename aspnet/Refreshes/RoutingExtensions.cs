using Microsoft.AspNetCore.Http.HttpResults;
using Microsoft.AspNetCore.Mvc;

using aspnet.Auth;
using aspnet.Db;

namespace aspnet.Refreshes;

internal static class RoutingExtensions
{
    extension(WebApplication app)
    {
        public void AddRefreshRoutes()
        {
            app.MapPost("/api/refresh",
                async Task<Results<Ok<Response>, UnauthorizedHttpResult>> (
                    [FromHeader(Name = "Authorization")] string[] auth, TokenService tokSvc, BdChirpyContext repo,
                    CancellationToken ct) =>
                {
                    var bearer = auth._getBearer();
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
                async ([FromHeader(Name = "Authorization")] string[] auth, BdChirpyContext repo, 
                    CancellationToken ct) =>
                {
                    var bearer = auth._getBearer();
                    if (bearer is null)
                        return TypedResults.NoContent();
                    await repo.RevokeRefreshToken(bearer, ct);
                    return TypedResults.NoContent();
                })
                .WithName("RevokeRefreshToken");
        }
    }

    private static string? _getBearer(this string[] auth)
    {
        string? bearer = null;
        
        foreach (var s in auth)
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