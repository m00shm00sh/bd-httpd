import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import org.koin.ktor.ext.inject
import refresh.configureBearerRefresh
import tokens.JwtService
import user.UserService
import webhook.configurePolkaAuth
import kotlin.getValue

internal fun Application.configureSecurity(polka: AppConfig.Polka) {
    val userService by inject<UserService>()
    val jwtService by inject<JwtService>()

    authentication {
        jwt("access") {
            verifier(jwtService.verifier)
            validate { credential ->
                jwtService.validator(credential, userService)
            }
            challenge { _, _ ->
                // not inside a route so shouldn't throw AuthenticationFailure()
                call.respond(HttpStatusCode.Unauthorized, "invalid token")
            }
        }
        configureBearerRefresh()
        configurePolkaAuth(polka)
    }
}
