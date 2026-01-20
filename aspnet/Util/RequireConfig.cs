namespace aspnet.Util;

internal static class ConfigExtensions
{
    public static string GetFromEnvironmentOrConfig(this IConfiguration config, string envName, string cfgName)
        => Environment.GetEnvironmentVariable(envName)
            ?? config[cfgName]
            ?? throw new ArgumentNullException(null,
                    $"The environment variable {envName} does not exist and neither does the config item {cfgName}."
                );
}