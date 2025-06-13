import chirp.ChirpService
import chirp.chirpRoutes
import refresh.RefreshService
import refresh.refreshRoutes
import tokens.JwtService
import user.UserService
import user.userRoutes
import webhook.polkaWebhook

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.*
import io.ktor.server.resources.*
import io.ktor.server.routing.route
import io.ktor.server.routing.routing


internal fun Application.configureRouting(
    userService: UserService,
    refreshService: RefreshService,
    jwtService: JwtService,
    chirpService: ChirpService,
    isDev: Boolean
) {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause" , status = HttpStatusCode.InternalServerError)
        }
        exception<NoSuchElementException> { call, cause  ->
            call.respondText("404: $cause", status = HttpStatusCode.NotFound)
        }
        exception<UnsupportedOperationException> { call, cause ->
            call.respondText("403: $cause", status = HttpStatusCode.Forbidden)
        }
        exception<AuthenticationFailure> { call, cause ->
            call.respondText("401: $cause", status = HttpStatusCode.Unauthorized)
        }
    }
    install(Resources)

    routing {
        route("api") {
            userRoutes(userService, refreshService, jwtService)
            chirpRoutes(chirpService, jwtService)
            refreshRoutes(refreshService, jwtService)
            polkaWebhook(userService)
            miscApiRoutes()
        }
        miscRoutes(isDev, userService)
    }
}

internal class AuthenticationFailure(s: String): RuntimeException(s)
