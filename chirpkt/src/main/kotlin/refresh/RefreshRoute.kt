package refresh

import AuthenticationFailure
import post

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.AuthenticationConfig
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.bearer
import io.ktor.server.auth.principal
import io.ktor.server.resources.post
import io.ktor.server.routing.Route
import tokens.JwtService

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

internal fun Route.refreshRoutes(refreshService: RefreshService, tokenService: JwtService) {
    authenticate("refresh-token") {
        // login via refresh
        post<RefreshRoute, RefreshResponse>{ _ ->
            val token = call.principal<RefreshPrincipal>()?.token
                ?: throw AuthenticationFailure("")
            // this is done here instead of in authentication handler to avoid needless GET in revoke
            val user = refreshService.getUserByRefresh(token)
            if (user == null)
                throw AuthenticationFailure("");
            val accessToken = tokenService.createToken(user)
            RefreshResponse(accessToken)
        }
        // revoke
        post<RevokeRoute, _> {
            val token = call.principal<RefreshPrincipal>()?.token
                ?: throw AuthenticationFailure("")
            refreshService.revokeToken(token)
            return@post HttpStatusCode.NoContent
        }
    }
}
