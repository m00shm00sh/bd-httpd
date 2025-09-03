package webhook

import AuthenticationFailure
import io.ktor.http.*
import io.ktor.http.content.OutgoingContent
import io.ktor.resources.Resource
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import post
import user.UserService

@Resource("polka")
internal class PolkaRoute {
    @Resource("webhooks")
    internal class Webhooks(val parent: PolkaRoute = PolkaRoute())
}

private class ApikeyPrincipal(val key: String)

// derived from https://github.com/LukasForst/ktor-plugins:apikey
private class ApikeyAuthProvider(c: Config): AuthenticationProvider(c) {
    private val scheme = "ApiKey"
    private val authenticateFn = c.authenticateFn
    private val challengeFn = c.challengeFn

    override suspend fun onAuthenticate(context: AuthenticationContext) {
        var key: String? = null
        context.call.request
            .headers.getAll(HttpHeaders.Authorization)
            ?.let {
                for (line in it) {
                    val tokens = line.split(' ', limit = 2)
                    if (tokens.size == 2 && tokens[0] == "ApiKey") {
                        key = tokens[1]
                        break
                    }
                }
            }
        val principal = key?.let { authenticateFn(context.call, it) }
        val cause = when {
            key == null -> AuthenticationFailedCause.NoCredentials
            principal == null -> AuthenticationFailedCause.InvalidCredentials
            else -> null
        }

        if (cause != null) {
            context.challenge(scheme, cause) { challenge, call ->
                challengeFn(call)
                challenge.complete()
            }
        }

        if (principal != null)
            context.principal("ApiKey", principal)
    }

    class Config(): AuthenticationProvider.Config("ApiKey") {
        lateinit var authenticateFn: suspend ApplicationCall.(String) -> Any?
        var challengeFn: suspend (ApplicationCall) -> Unit = { call ->
            call.respond(HttpStatusCode.Unauthorized)
        }
        fun validate(body: suspend ApplicationCall.(String) -> Any?) {
            authenticateFn = body
        }
        fun challenge(body: suspend (ApplicationCall) -> Unit) {
            challengeFn = body
        }
    }
}

private fun AuthenticationConfig.apiKey(configure: ApikeyAuthProvider.Config.() -> Unit) {
    val provider = ApikeyAuthProvider(ApikeyAuthProvider.Config().apply(configure))
    register(provider)
}


internal fun AuthenticationConfig.configurePolkaAuth(polka: AppConfig.Polka) {
    apiKey {
        validate {
            it.takeIf { it == polka.key }?.let { _ -> ApikeyPrincipal(it) }
        }
    }
}

internal fun Route.polkaWebhook(userService: UserService) {
    authenticate("ApiKey") {
        post<PolkaRoute.Webhooks, PolkaRequest, Unit>(HttpStatusCode.NoContent) { _, req ->
            if (req.event == "user.upgraded" &&
                !userService.upgradeUser(req.data.userId)
            )
                throw NoSuchElementException()
        }
    }
}
