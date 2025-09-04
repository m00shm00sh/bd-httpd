import user.UserService

import io.ktor.resources.Resource
import io.ktor.server.html.respondHtml
import io.ktor.server.http.content.staticFileSystem
import io.ktor.server.resources.*
import io.ktor.server.routing.Route
import kotlinx.html.*
import org.koin.ktor.ext.inject
import java.util.concurrent.atomic.AtomicLong
import kotlin.getValue

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

internal fun Route.miscRoutes(isDev: Boolean) {
    val userService by inject<UserService>()

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