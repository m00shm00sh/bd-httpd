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
import io.ktor.server.routing.*
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

internal inline fun <reified RoutingResource: Any, reified Body: Any, reified Response: Any>
Route.endpoint(
    method: HttpMethod,
    noinline body: suspend RoutingContext.(RoutingResource, Body) -> Response
): Route {
    lateinit var route: Route
    resource<RoutingResource> {
        route = method(method) {
            val serializer = serializer<RoutingResource>()
            handle(serializer) { resource ->
                val input = if (Body::class == Unit::class) Unit as Body else call.receive()
                val result = body(resource, input)
                if (Response::class == Unit::class)
                    call.respondText("", status = HttpStatusCode.NoContent)
                else
                    call.respond(result)
            }
        }
    }
    return route
}
