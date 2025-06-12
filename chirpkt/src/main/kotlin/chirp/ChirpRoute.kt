package chirp

import AuthenticationFailure
import endpoint
import io.ktor.client.request.HttpRequest
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import tokens.JwtService
import user.UserRequest
import user.UserResponseWithToken
import user.UserService
import user.toUserEntry
import user.withTokens
import java.util.UUID

@Resource("/chirps")
internal class ChirpRoute(
    val sort: String? = "asc",
    @SerialName("author_id")
    @Serializable(with = UUIDSerializer::class)
    val authorId: UUID? = null,
) {
    @Resource("{id}")
    internal class ById(
        val parent: ChirpRoute = ChirpRoute(),
        @Serializable(with = UUIDSerializer::class)
        val id: UUID
    )
}

internal fun Route.chirpRoutes(chirpService: ChirpService, tokenService: JwtService) {
    authenticate("access") {
        // create chirp
        endpoint<ChirpRoute, ChirpRequest, ChirpResponse>(HttpMethod.Post) { _, req ->
            val user = JwtService.getUser(call)
            checkNotNull(user)
            call.response.status(HttpStatusCode.Created)
            chirpService.createChirp(req, user)
        }
    }

    // get all chirps
    endpoint<ChirpRoute, Unit, List<ChirpResponse>>(HttpMethod.Get) { res, _ ->
        val aid = res.authorId
        val sort = res.sort ?: "asc"
        val chirps = chirpService.getChirps(aid)
        return@endpoint (
            if (sort == "desc")
                chirps.reversed()
            else
                chirps
        )
    }

    suspend fun getChirpByIdOrThrow404(id: UUID) =
        chirpService.getChirpById(id) ?: throw NoSuchElementException("no such chirp")

    // get one chirp
    endpoint<ChirpRoute.ById, Unit, ChirpResponse>(HttpMethod.Get) { res, _ ->
        getChirpByIdOrThrow404(res.id)
    }

    authenticate("access") {
        // delete one chirp
        endpoint<ChirpRoute.ById, Unit, Unit>(HttpMethod.Delete) { res, _ ->
            val user = JwtService.getUser(call)
            val chirp = getChirpByIdOrThrow404(res.id)
            if (chirp.userId != user)
                throw UnsupportedOperationException("can't delete someone else's chirp")
            chirpService.deleteChirp(chirp.id)
        }
    }

}