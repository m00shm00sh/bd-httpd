using System.IdentityModel.Tokens.Jwt;
using System.Text.Json;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.AspNetCore.OpenApi;
using Microsoft.EntityFrameworkCore;

using aspnet.Auth;
using aspnet.Chirp;
using aspnet.Db;
using aspnet.Misc;
using aspnet.Refreshes;
using aspnet.User;
using aspnet.Util;
using aspnet.Webhooks;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddDbContext<BdChirpyContext>(options =>
    options.UseNpgsql(builder.Configuration.GetFromEnvironmentOrConfig(
        "DB_URL", "ConnectionStrings:DbUrl")
    )
);
builder.Services.AddOpenApi(options =>
    options.CreateSchemaReferenceId = t =>
    {
        var fn = t.Type.FullName;
        return fn?.StartsWith("aspnet.") == true
            ? fn["aspnet.".Length..]
            : OpenApiOptions.CreateDefaultSchemaReferenceId(t);
    }
);
builder.Services.AddScoped<TokenService>();
JwtSecurityTokenHandler.DefaultMapInboundClaims = false;
builder.Services.AddAuthentication(JwtBearerDefaults.AuthenticationScheme)
    .AddJwtBearer(options =>
    {
        options.TokenValidationParameters = TokenService.MakeJwtValidationParameters(builder.Configuration);
    });
builder.Services.Configure<Microsoft.AspNetCore.Http.Json.JsonOptions>(options =>
{
    options.SerializerOptions.PropertyNamingPolicy = JsonNamingPolicy.SnakeCaseLower;
});
builder.Services.AddHttpLogging(_ =>
{
});

builder.Logging.AddConsole();
builder.Logging.AddDebug();
var app = builder.Build();

app.UseAuthentication();

// Configure the HTTP request pipeline.
if (app.Environment.IsDevelopment())
{
    app.MapOpenApi();
}

app.UseHttpsRedirection();
app.UseHttpLogging();

app.AddMiscApiRoutes();
app.AddUserApiRoutes();
app.AddRefreshRoutes();
app.AddWebhookApiRoutes();
app.AddChirpApiRoutes();
app.Run();
