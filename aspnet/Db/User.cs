namespace aspnet.Db;

public partial class User
{
    public Guid Id { get; set; }

    public DateTime CreatedAt { get; set; }

    public DateTime UpdatedAt { get; set; }

    public string Email { get; set; } = null!;

    public string Pass { get; set; } = null!;

    public int IsChirpyRed { get; set; }

    public virtual ICollection<Chirp> Chirps { get; set; } = new List<Chirp>();

    public virtual ICollection<RefreshToken> RefreshTokens { get; set; } = new List<RefreshToken>();
}
