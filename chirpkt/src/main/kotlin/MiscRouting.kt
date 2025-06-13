import chirp.ChirpService
import chirp.chirpRoutes
import refresh.RefreshService
import refresh.refreshRoutes
import tokens.JwtService
import user.UserService
import user.userRoutes
import webhook.polkaWebhook

import io.ktor.http.*
import io.ktor.resources.Resource
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

@Resource("healthz")
internal class Healthz
@Resource("admin")
internal class Admin {
    @Resource("metrics")
    internal class Metrics(val parent: Admin)

    @Resource("reset")
    internal class Reset(val parent: Admin)
}

internal fun Route.miscApiRoutes() {
    get<Healthz, String> {
        "OK"
    }
}

private val hitCount = AtomicLong()

internal fun Route.miscRoutes(isDev: Boolean, userService: UserService) {
    get<Admin.Metrics> {
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
    post<Admin.Reset, String> {
        if (!isDev)
            throw UnsupportedOperationException()
        userService.deleteAllUsers()
        hitCount.set(0)
        ""
    }

    // Static plugin. Try to access `/static/index.html`
    staticFileSystem("/app", "../static") {
        modify { _, _ ->
            hitCount.incrementAndGet()
        }
    }
}