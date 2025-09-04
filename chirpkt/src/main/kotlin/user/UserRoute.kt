package user

import put
import post
import AuthenticationFailure
import refresh.RefreshService
import tokens.JwtService

import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import org.koin.ktor.ext.inject
import kotlin.getValue

@Resource("users")
internal class UserRoute

@Resource("login")
internal class LoginRoute

internal fun Route.userRoutes() {
    val userService by inject<UserService>()
    val refreshService by inject<RefreshService>()
    val tokenService by inject<JwtService>()

    // create user
    post<UserRoute, UserRequest, UserResponse>(HttpStatusCode.Created) { _, req ->
        val result = userService.createUser(req.toUserEntry())
        result
    }
    // login user
    post<LoginRoute, UserRequest, UserResponseWithToken> { _, req ->
        val (user, userResp) = userService.getUserByEmail(req.email)
            ?: throw AuthenticationFailure("bad login")
        if (!user.password.test(req.password))
            throw AuthenticationFailure("bad login")
        val refreshToken = refreshService.createRefreshTokenForUser(userResp.id!!)
        val accessToken = tokenService.createToken(userResp.id)
        userResp.withTokens(accessToken, refreshToken)
    }
    authenticate("access") {
        // update user
        put<UserRoute, UserRequest, UserResponse> { _, req ->
            val user = tokenService.getUser(call) ?: throw AuthenticationFailure("")
            val result = userService.updateUser(req.toUserEntry(), user)
                ?: throw NoSuchElementException("no user")
            result.copy(id = user, email = req.email,)
        }
    }

}