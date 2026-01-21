using aspnet.Util;

namespace aspnet.Auth;

internal class ApikeyEndpointFilter(IConfiguration configuration, string envName, string cfgName)
    : IEndpointFilter
{
    private readonly string _filterApiKey = configuration.GetFromEnvironmentOrConfig(envName, cfgName);

    public async ValueTask<object?> InvokeAsync(EndpointFilterInvocationContext context, EndpointFilterDelegate next)
    {
        var httpContext = context.HttpContext;
        string? apiKey = null;
        
        foreach (var s in httpContext.Request.Headers.Authorization)
        {
            if (string.IsNullOrWhiteSpace(s))
                continue;
            var toks = s.Split(' ', 2);
            if (toks is not ["ApiKey", _]) continue;
            apiKey = toks[1];
            break;
        }
        
        if (apiKey != _filterApiKey)
            return Results.Unauthorized();
        
        return await next(context);
    }
}