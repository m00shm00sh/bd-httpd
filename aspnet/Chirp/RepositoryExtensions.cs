using Microsoft.EntityFrameworkCore;
using aspnet.Db;

namespace aspnet.Chirp;

internal static class RepositoryExtensions
{
    public static async Task<Response> CreateChirp(this BdChirpyContext ctx, Request reqChirp, Guid userId,
        CancellationToken ct)
    {
        var dbChirp = new Db.Chirp
        {
            Body = reqChirp.Body,
            UserId = userId
        };
        await ctx.Chirps.AddAsync(dbChirp, ct);
        await ctx.SaveChangesAsync(ct);

        return new Response(
            Id: dbChirp.Id, 
            CreatedAt: new DateTimeOffset(dbChirp.CreatedAt),
            UpdatedAt: new DateTimeOffset(dbChirp.UpdatedAt),
            Body: dbChirp.Body,
            UserId: userId
        );
    }

    public static async Task<List<Response>> GetAllChirps(this BdChirpyContext ctx, Guid? userId, CancellationToken ct,
        bool reverseOrder = false)
    {
        var q = ctx.Chirps.AsNoTracking();
        if (userId.HasValue)
            q = q.Where(x => x.UserId == userId.Value);
        q = !reverseOrder ? q.OrderBy(e => e.CreatedAt) : q.OrderByDescending(e => e.CreatedAt);
        var list = new List<Response>();
        // the monstrosity in the foreach is taken from EF's IQueryable<TSource>.ToListAsync(ct)
        await foreach (var chirp in q.AsAsyncEnumerable().WithCancellation(ct).ConfigureAwait(false))
        {
            list.Add(new Response(
                Id: chirp.Id,
                CreatedAt: new DateTimeOffset(chirp.CreatedAt),
                UpdatedAt: new DateTimeOffset(chirp.UpdatedAt),
                Body: chirp.Body,
                UserId: chirp.UserId
            ));
        }
        return list;
    }

    public static async Task<Response?> FindChirpById(this BdChirpyContext ctx, Guid chirpId, CancellationToken ct)
    {
        var row = await ctx.Chirps.SingleOrDefaultAsync(x => x.Id == chirpId, ct);
        if (row is null)
            return null;
        return new Response(
            Id: row.Id,
            CreatedAt: new DateTimeOffset(row.CreatedAt),
            UpdatedAt: new DateTimeOffset(row.UpdatedAt),
            Body: row.Body,
            UserId: row.UserId
        );
    }

    public enum DeleteResult
    {
        Ok,
        NotFound,
        NotAllowed
    }

    public static async Task<DeleteResult> DeleteChirp(this BdChirpyContext ctx, Guid chirpId, Guid userId,
        CancellationToken ct)
    {
        // do the authentication check in the repository layer because we need the entity row for the delete.
        var chirp = await ctx.Chirps.FindAsync([chirpId], ct);
        if (chirp is null)
            return DeleteResult.NotFound;
        if (chirp.UserId != userId)
            return DeleteResult.NotAllowed;
        ctx.Chirps.Remove(chirp);
        await ctx.SaveChangesAsync(ct);
        return DeleteResult.Ok;
    }
}