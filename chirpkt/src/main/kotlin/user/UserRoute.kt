package user

import AuthenticationFailure
import endpoint
import refresh.RefreshService
import tokens.JwtService

import io.ktor.http.HttpMethod
import io.ktor.resources.Resource
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route

@Resource("/users")
internal class UserRoute {
}
@Resource("/login")
internal class LoginRoute {
}

internal fun Route.userRoutes(userService: UserService, refreshService: RefreshService, tokenService: JwtService) {
    // create user
    endpoint<UserRoute, UserRequest, UserResponseWithToken>(HttpMethod.Post) { _, req ->
        val result = userService.createUser(req.toUserEntry())
        val refreshToken = refreshService.createRefreshTokenForUser(result.id!!)
        val accessToken = tokenService.createToken(result.id)
        result.withTokens(refreshToken, accessToken)
    }
    // login user
    endpoint<LoginRoute, UserRequest, UserResponseWithToken>(HttpMethod.Post) { _, req ->
        val (user, userResp) = userService.getUserByEmail(req.email)
            ?: throw AuthenticationFailure("bad login")
        if (!user.password.test(req.password))
            throw AuthenticationFailure("bad login")
        val refreshToken = refreshService.createRefreshTokenForUser(userResp.id!!)
        val accessToken = tokenService.createToken(userResp.id)
        userResp.withTokens(refreshToken, accessToken)
    }
    authenticate {
        // update user
        endpoint<UserRoute, UserRequest, UserResponseWithToken>(HttpMethod.Put) { _, req ->
            val user = JwtService.getUser(call) ?: throw AuthenticationFailure("")
            val result = userService.updateUser(req.toUserEntry(), user)
                ?: throw NoSuchElementException("no user")
            val refreshToken = refreshService.createRefreshTokenForUser(user)
            val accessToken = tokenService.createToken(user)
            result
                .copy(id = user, email = req.email,)
                .withTokens(accessToken, refreshToken)
        }
    }

}