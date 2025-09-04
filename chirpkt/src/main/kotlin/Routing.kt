import chirp.chirpRoutes
import refresh.refreshRoutes
import user.userRoutes
import webhook.polkaWebhook

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.*
import io.ktor.server.resources.*
import io.ktor.server.routing.*


internal fun Application.configureRouting(isDev: Boolean) {
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
        exception<IllegalArgumentException> { call, cause ->
            call.respondText("400: $cause", status = HttpStatusCode.BadRequest)
        }
    }
    install(Resources)

    routing {
        route("api") {
            userRoutes()
            chirpRoutes()
            refreshRoutes()
            polkaWebhook()
            miscApiRoutes()
        }
        miscRoutes(isDev)
    }
}

internal class AuthenticationFailure(s: String): RuntimeException(s)
