package refresh

import AuthenticationFailure
import post

import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.server.auth.*
import io.ktor.server.routing.Route
import org.koin.ktor.ext.inject
import tokens.JwtService
import kotlin.getValue

@Resource("refresh")
internal class RefreshRoute()
@Resource("revoke")
internal class RevokeRoute

private class RefreshPrincipal(val token: String)

internal fun AuthenticationConfig.configureBearerRefresh() {
    bearer("refresh-token") {
        authenticate {
            RefreshPrincipal(it.token)
        }
    }
}

internal fun Route.refreshRoutes() {
    val refreshService by inject<RefreshService>()
    val tokenService by inject<JwtService>()

    authenticate("refresh-token") {
        // login via refresh
        post<RefreshRoute, RefreshResponse>{ _ ->
            val token = call.principal<RefreshPrincipal>()?.token
                ?: throw AuthenticationFailure("")
            // this is done here instead of in authentication handler to avoid needless GET in revoke
            val user = refreshService.getUserByRefresh(token)
                ?: throw AuthenticationFailure("")
            val accessToken = tokenService.createToken(user)
            RefreshResponse(accessToken)
        }
        // revoke
        post<RevokeRoute, Unit>(HttpStatusCode.NoContent) {
            val token = call.principal<RefreshPrincipal>()?.token
                ?: throw AuthenticationFailure("")
            refreshService.revokeToken(token)
        }
    }
}
