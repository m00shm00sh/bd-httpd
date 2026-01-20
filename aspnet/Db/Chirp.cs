namespace aspnet.Db;

public partial class Chirp
{
    public Guid Id { get; set; }

    public DateTime CreatedAt { get; set; }

    public DateTime UpdatedAt { get; set; }

    public string Body { get; set; } = null!;

    public Guid UserId { get; set; }

    public virtual User User { get; set; } = null!;
}
