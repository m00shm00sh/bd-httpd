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
import io.ktor.server.html.respondHtml
import io.ktor.server.http.content.staticFileSystem
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.resources.*
import io.ktor.server.resources.handle
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.html.body
import kotlinx.html.h1
import kotlinx.html.p
import kotlinx.serialization.serializer
import java.util.concurrent.atomic.AtomicLong

private val hitCount = AtomicLong()

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
            get("healthz") {
                call.respondText("OK")
            }
        }
        route("admin") {
            get("metrics") {
                call.respondHtml {
                    body {
                        h1 {
                            +"Welcome, Chirpy Admin"
                        }
                        p {
                          +"Chirpy has been visited ${hitCount.get()} times!"
                        }
                    }
                }
            }
            post("reset") {
                if (!isDev)
                    throw UnsupportedOperationException()
                userService.deleteAllUsers()
                hitCount.set(0)
                call.respondText("", status = HttpStatusCode.OK)
            }
        }
        // Static plugin. Try to access `/static/index.html`
        staticFileSystem("/app", "../static") {
            modify { _, _ ->
                hitCount.incrementAndGet()
            }
        }

    }
}

internal class AuthenticationFailure(s: String): RuntimeException(s)
