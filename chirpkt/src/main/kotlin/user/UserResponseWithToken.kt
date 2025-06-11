package user

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
internal data class UserResponseWithToken(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    @SerialName("created_at")
    val created: Instant,
    @SerialName("updated_at")
    val updated: Instant,
    @SerialName("token")
    val accessToken: String,
    @SerialName("refresh_token")
    val refreshToken: String,
    val email: String,
    @SerialName("is_chirpy_red")
    val isRed: Boolean
)
internal fun UserResponse.withTokens(accessToken: String, refreshToken: String) =
    UserResponseWithToken(
        // if id or email are null, we forgot to hydrate the fields with request data
        checkNotNull(id), created, updated,
        accessToken, refreshToken,
        checkNotNull(email), isRed
    )