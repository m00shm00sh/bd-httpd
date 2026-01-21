using System.Security.Claims;
using Microsoft.AspNetCore.Http.HttpResults;
using Microsoft.AspNetCore.Mvc;

using aspnet.Auth;
using aspnet.Db;
using aspnet.User;

namespace aspnet.Chirp;

internal static class RoutingExtensions
{
    extension(WebApplication app)
    {
        public void AddChirpApiRoutes()
        {
            app.MapPost("/api/chirps",
                async Task<Results<Created<Response>, BadRequest<string>, UnauthorizedHttpResult>> (
                    Request req, ClaimsPrincipal user, BdChirpyContext repo, CancellationToken ct) =>
                {
                    var userId = user.GetSubjectId();
                    if (userId is null)
                    {
                        return TypedResults.Unauthorized();
                    }

                    var isUserRed = await repo.IsUserRed(userId.Value, ct);

                    try
                    {
                        var cleanChirp = req.WithCleanBody(isUserRed ? 200 : 140);
                        var response = await repo.CreateChirp(cleanChirp, userId.Value, ct);
                        return TypedResults.Created((Uri?)null, response);
                    }
                    catch (ArgumentOutOfRangeException e)
                    {
                        return TypedResults.BadRequest(e.Message);
                    }
                })
                .AddEndpointFilter<JwtAuthorizationEndpointFilter>()
                .WithName("CreateChirp");
        
            app.MapGet("/api/chirps",
                    async Task<List<Response>> (
                        BdChirpyContext repo, CancellationToken ct, [FromQuery(Name="author_id")] Guid? authorId = null,
                        string sort = "asc") =>
                    {
                        var reverseDirection = sort == "desc";
                        return await repo.GetAllChirps(authorId, ct, reverseDirection);
                    })
                .WithName("GetAllChirps");
        
            app.MapGet("/api/chirps/{id:guid}",
                async Task<Results<Ok<Response>, NotFound>> (Guid id, BdChirpyContext repo, CancellationToken ct) =>
                {
                    var chirp = await repo.FindChirpById(id, ct);
                    return chirp switch
                    {
                        null => TypedResults.NotFound(),
                        _ => TypedResults.Ok(chirp)
                    };
                })
                .WithName("GetChirpById");
        
            app.MapDelete("/api/chirps/{id:guid}",
                async Task<Results<NoContent, UnauthorizedHttpResult, ForbidHttpResult, NotFound>> (
                    Guid id, ClaimsPrincipal user, BdChirpyContext repo, CancellationToken ct) =>
                {
                    var userId = user.GetSubjectId();
                    if (userId is null)
                    {
                        return TypedResults.Unauthorized();
                    }
                    return await repo.DeleteChirp(id, userId.Value, ct) switch
                    {
                        RepositoryExtensions.DeleteResult.NotFound => TypedResults.NotFound(),
                        RepositoryExtensions.DeleteResult.NotAllowed => TypedResults.Forbid(),
                        RepositoryExtensions.DeleteResult.Ok => TypedResults.NoContent(),
                        _ => throw new SystemException("unexpected result of delete")
                    };
                })
                .AddEndpointFilter<JwtAuthorizationEndpointFilter>()
                .WithName("DeleteChirp");
        }
    }
}