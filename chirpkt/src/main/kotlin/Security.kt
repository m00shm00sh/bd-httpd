import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import refresh.configureBearerRefresh
import tokens.JwtService
import user.UserService
import webhook.configurePolkaAuth

internal fun Application.configureSecurity(userService: UserService, jwtService: JwtService, polka: AppConfig.Polka) {
    authentication {
        jwt("access") {
            verifier(jwtService.verifier)
            validate { credential ->
                jwtService.validator(credential, userService)
            }
            challenge { scheme, realm ->
                // not inside a route so shouldn't throw AuthenticationFailure()
                call.respond(HttpStatusCode.Unauthorized, "invalid token")
            }
        }
        configureBearerRefresh()
        configurePolkaAuth(polka)
    }
}
