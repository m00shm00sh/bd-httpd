namespace aspnet.Chirp;

internal record Request(string Body)
{
    public Request WithCleanBody(int maxBodyLength)
    {
        if (Body.Length > maxBodyLength)
            throw new ArgumentOutOfRangeException("Body", "too long");
        return new(
            Body.Split(" ")
            .Select(w => _profanities.Contains(w.ToLower()) ? "****" : w)
            .Aggregate((a, b) => $"{a} {b}")
        );
    }

    private static readonly HashSet<string> _profanities = ["kerfuffle", "sharbert", "fornax"];
}

internal record Response(
    Guid Id,
    DateTimeOffset CreatedAt,
    DateTimeOffset UpdatedAt,
    string Body,
    Guid UserId
);
