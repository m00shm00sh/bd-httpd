using System.IdentityModel.Tokens.Jwt;
using System.Text.Json;
using aspnet.Auth;
using aspnet.Chirp;
using aspnet.Db;
using aspnet.Misc;
using aspnet.Refreshes;
using aspnet.User;
using aspnet.Util;
using aspnet.Webhooks;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.EntityFrameworkCore;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddDbContext<BdChirpyContext>(options =>
    options.UseNpgsql(builder.Configuration.GetFromEnvironmentOrConfig(
        "DB_URL", "ConnectionStrings:DbUrl")
    )
);
builder.Services.AddOpenApi();
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
