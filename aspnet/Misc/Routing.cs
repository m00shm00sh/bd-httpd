using Microsoft.Extensions.FileProviders;

using aspnet.Db;
using aspnet.RazorSlices;
using aspnet.User;

namespace aspnet.Misc;

internal static class Hitcount
{
    private static int _hitCount;

    internal static void IncrementHitCount()
    {
        Interlocked.Increment(ref _hitCount);
    }
    
    internal static int HitCount => _hitCount;

    internal static void ResetHitCount()
    {
        Interlocked.Exchange(ref _hitCount, 0);
    }
}

internal static class Routing
{
    extension(WebApplication app)
    {
        public void AddMiscApiRoutes()
        {
            app.MapGet("/api/healthz",
                () => "OK")
                .WithName("Healthz");
        
            app.MapGet("/admin/metrics",
                () => 
                    Results.Extensions.RazorSlice<Metrics, object>(Hitcount.HitCount)
                )
                .WithName("Metrics");
        
            if (app.Environment.IsDevelopment())
            {
                app.MapPost("/admin/reset",
                    async (BdChirpyContext db, CancellationToken ct) =>
                    {
                        await db.DeleteAllUsers(ct);
                        Hitcount.ResetHitCount();
                        return TypedResults.Ok();
                    })
                    .WithName("ResetDb");
            }
            else
            {
                app.MapPost("/admin/reset",
                    () => TypedResults.Forbid()
                    )
                    .WithName("ResetDb");
            }

            app.UseStaticFiles(new StaticFileOptions
            {
            
                OnPrepareResponse = _ =>
                {
                    Hitcount.IncrementHitCount();
                },
                FileProvider = new PhysicalFileProvider(
                    Path.Combine(app.Environment.ContentRootPath, "../static")
                ),
                RequestPath = "/app"
            });
            // hack to get around configuring UseDefaultFiles()
            app.MapGet("/app/",
                () => TypedResults.Redirect("/app/index.html")
            );
        }
    }
}