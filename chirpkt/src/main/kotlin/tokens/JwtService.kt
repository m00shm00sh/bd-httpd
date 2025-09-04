package tokens

import AppConfig
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.JWTVerifier
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTCredential
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import user.UserService
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.toJavaInstant

internal class JwtService(private val config: AppConfig.Jwt) {
    val verifier: JWTVerifier = JWT
        .require(Algorithm.HMAC256(config.secret.value))
        .withIssuer(config.issuer)
        .build()

    fun createToken(user: UUID): String {
        @OptIn(ExperimentalTime::class)
        return JWT.create()
            .withIssuer(config.issuer)
            .withClaim(CLAIM_USER, user.toString())
            .withExpiresAt((Clock.System.now() + config.timeout).toJavaInstant())
            .sign(Algorithm.HMAC256(config.secret.value))
    }

    suspend fun validator(credential: JWTCredential, userService: UserService): JWTPrincipal? {
        val user = credential.payload
            .getClaim(CLAIM_USER).asString()
            .let(UUID::fromString)
            ?: return null
        userService.existsUserForId(user).takeIf { it } ?: return null
        return JWTPrincipal(credential.payload)
    }

    // non-companion this so we can call it through DI proxy
    fun getUser(call: ApplicationCall): UUID? =
        call.principal<JWTPrincipal>()?.payload
            ?.getClaim(CLAIM_USER)?.asString()
            ?.let(UUID::fromString)

    companion object {
        private const val CLAIM_USER = "user"


    }

}
