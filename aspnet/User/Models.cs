using System.Text.Json.Serialization;
using static Soenneker.Hashing.Argon2.Argon2HashingUtil;

namespace aspnet.User;

internal record Request(
    string Email,
    string Password
);

internal readonly struct HashedPassword
{
    public string Value { get; }

    private HashedPassword(string hashed)
    {
        Value = hashed;
    }

    public static async Task<HashedPassword> FromPlaintext(Request request)
        => new(await Hash(request.Password));

    public static HashedPassword FromHashed(string hashedPassword)
        => new(hashedPassword);

    public async Task<bool> VerifyPassword(string plaintextPassword)
        => await Verify(plaintextPassword, Value);
}

internal record StorageRequest(
    string Email,
    HashedPassword Password
);

internal static class HashingSupport
{
    public static async Task<StorageRequest> ToStorage(this Request req)
        => new(
            Email: req.Email,
            Password: await HashedPassword.FromPlaintext(req)
        );
}


internal record Response(
    Guid Id,
    DateTimeOffset CreatedAt,
    DateTimeOffset UpdatedAt,
    string Email,
    bool IsChirpyRed
);

internal record LoginResponse(
    Guid Id,
    DateTimeOffset CreatedAt,
    DateTimeOffset UpdatedAt,
    string Email,
    bool IsChirpyRed,
    [property: JsonPropertyName("token")] string AccessToken,
    string RefreshToken
);

internal static class LoginResponseExtensions
{
    public static LoginResponse ToLoginResponse(this Response r, string accessToken, string refreshToken)
    {
        return new LoginResponse(
            Id: r.Id,
            CreatedAt: r.CreatedAt,
            UpdatedAt: r.UpdatedAt,
            Email: r.Email,
            IsChirpyRed: r.IsChirpyRed,
            AccessToken: accessToken,
            RefreshToken: refreshToken
        );
    }
}
