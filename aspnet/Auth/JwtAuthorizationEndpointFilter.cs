namespace aspnet.Auth;

internal class JwtAuthorizationEndpointFilter(ILogger<JwtAuthorizationEndpointFilter> logger) : IEndpointFilter
{
    public async ValueTask<object?> InvokeAsync(EndpointFilterInvocationContext context, EndpointFilterDelegate next)
    {
        var httpContext = context.HttpContext;

        var sub = httpContext.User.GetSubjectId();
        logger.LogDebug("JWT User: {0}", sub);
        if (sub == null)
            return Results.Unauthorized();
        return await next(context);
    }
}