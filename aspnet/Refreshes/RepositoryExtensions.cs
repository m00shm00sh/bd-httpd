using System.Security.Cryptography;
using aspnet.Db;
using Microsoft.EntityFrameworkCore;

namespace aspnet.Refreshes;

internal static class RepositoryExtensions
{
    public static async Task<string> CreateRefreshToken(this BdChirpyContext ctx, Guid id, CancellationToken ct)
    {
        // TODO: can we use EF's table introspection to get the char length of [refreshes].[token]?
        var tokenBytes = RandomNumberGenerator.GetBytes(64/2);
        var token = Convert.ToHexString(tokenBytes);
        var row = new RefreshToken
        {
            Token = token,
            UserId = id,
            ExpiresAt = DateTime.UtcNow.AddDays(60).ToLocalTime(),
        };
        await ctx.RefreshTokens.AddAsync(row, ct);
        await ctx.SaveChangesAsync(ct);
        return token;
    }

    public static async Task<Guid?> FindUserByRefreshToken(this BdChirpyContext ctx, string token, CancellationToken ct)
    {
        var now = DateTime.UtcNow.ToLocalTime();
        var row = await ctx.RefreshTokens.FirstOrDefaultAsync(
            r => r.Token == token && r.ExpiresAt > now && r.RevokedAt == null, ct);
        return row?.UserId;
    }

    public static async Task RevokeRefreshToken(this BdChirpyContext ctx, string token, CancellationToken ct)
    {
        var now = DateTime.UtcNow.ToLocalTime();
        var row = await ctx.RefreshTokens.FirstOrDefaultAsync(r => r.Token == token, ct);
        if (row is null)
            return;
        row.RevokedAt = now;
        await ctx.SaveChangesAsync(ct);
    }
}