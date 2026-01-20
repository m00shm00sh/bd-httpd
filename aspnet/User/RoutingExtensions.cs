using System.Security.Claims;
using aspnet.Auth;
using aspnet.Db;
using aspnet.Refreshes;
using Microsoft.AspNetCore.Http.HttpResults;

namespace aspnet.User;

internal static class RoutingExtensions
{
    public static void AddUserApiRoutes(this WebApplication app)
    {
        app.MapPost("/api/users", 
                    async (Request req, BdChirpyContext repo, CancellationToken ct) =>
            {
                var res = await repo.CreateUser(req, ct);
                return TypedResults.Created((Uri?)null, res);
            })
            .WithName("CreateUser");
        app.MapPost("/api/login",
            async Task<Results<Ok<LoginResponse>, UnauthorizedHttpResult>>(
                Request req, BdChirpyContext repo, TokenService tokSvc, CancellationToken ct
            ) =>
            {
                var row = await repo.FindUserByEmail(req, ct);
                if (row is null)
                    return TypedResults.Unauthorized();
                var (details, pass) = row;
                if (!await pass.VerifyPassword(req.Password))
                    return TypedResults.Unauthorized();
                var jwt = tokSvc.GenerateToken(details.Id);
                var refresh = await repo.CreateRefreshToken(details.Id, ct);
                var r = details.ToLoginResponse(jwt, refresh);
                return TypedResults.Ok(r);
            })
            .WithName("LoginUser");
        app.MapPut("/api/users",
            async Task<Results<Ok<Response>, UnauthorizedHttpResult>> (
                Request req, ClaimsPrincipal user, BdChirpyContext repo, CancellationToken ct) =>
            {
                var userId = user.GetSubjectId();
                if (userId is null)
                {
                    return TypedResults.Unauthorized();
                }
                var r = await repo.UpdateUser(req, userId.Value, ct);
                return r != null ? TypedResults.Ok(r) : TypedResults.Unauthorized();
            })
            .AddEndpointFilter<JwtAuthorizationEndpointFilter>()
            .WithName("UpdateUser");
    }
}