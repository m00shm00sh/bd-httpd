using aspnet.Db;
using Microsoft.EntityFrameworkCore;

namespace aspnet.User;

internal static class RepositoryExtensions
{
    public static async Task<Response> CreateUser(this BdChirpyContext ctx, Request reqUser, CancellationToken ct)
    {
        var user = await reqUser.ToStorage();
        var dbUser = new Db.User
        {
            Email = user.Email,
            Pass = user.Password.Value
        };
        await ctx.Users.AddAsync(dbUser, ct);
        await ctx.SaveChangesAsync(ct);

        return new Response(
            Id: dbUser.Id, 
            CreatedAt: new DateTimeOffset(dbUser.CreatedAt), UpdatedAt: new DateTimeOffset(dbUser.UpdatedAt),
            Email: user.Email,
            IsChirpyRed: dbUser.IsChirpyRed > 0
        );
    }

    public static async Task<Tuple<Response, HashedPassword>?> FindUserByEmail(this BdChirpyContext ctx, 
        Request reqUser, CancellationToken ct)
    {
        var row = await ctx.Users.AsNoTracking().SingleOrDefaultAsync(u => u.Email == reqUser.Email, ct);
        if (row is null)
            return null;
        return Tuple.Create(
            new Response(
                Id: row.Id,
                CreatedAt: new DateTimeOffset(row.CreatedAt), UpdatedAt: new DateTimeOffset(row.UpdatedAt),
                Email: row.Email, IsChirpyRed: row.IsChirpyRed > 0
            ),
            HashedPassword.FromHashed(row.Pass)
        );
    }

    public static async Task<Response?> UpdateUser(this BdChirpyContext ctx, Request reqUser, Guid id, 
        CancellationToken ct)
    {
        var row = await ctx.Users.FindAsync([id], cancellationToken: ct);
        if (row is null)
            return null;
        var user = await reqUser.ToStorage();
        row.Email = user.Email;
        row.Pass = user.Password.Value;
        await ctx.SaveChangesAsync(ct);

        return new Response(
            Id: row.Id,
            CreatedAt: new DateTimeOffset(row.CreatedAt), UpdatedAt: new DateTimeOffset(row.UpdatedAt),
            Email: row.Email, IsChirpyRed: row.IsChirpyRed > 0
        );
    }

    public static async Task<bool> UpgradeUserToRed(this BdChirpyContext ctx, Guid id, CancellationToken ct)
    {
        var row = await ctx.Users.FindAsync([id], ct);
        if (row is null)
            return false;
        row.IsChirpyRed = 1;
        await ctx.SaveChangesAsync(ct);
        return true;
    }

    public static async Task DeleteAllUsers(this BdChirpyContext ctx, CancellationToken ct)
    {
        await ctx.Users.ExecuteDeleteAsync(ct);
    }
}
