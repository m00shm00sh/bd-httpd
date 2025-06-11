package user

import kotlinx.datetime.Instant
import kotlinx.serialization.*
import java.util.UUID

@Serializable
internal data class UserResponse(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID?,
    @SerialName("created_at")
    val created: Instant,
    @SerialName("updated_at")
    val updated: Instant,
    val email: String?,
    @SerialName("is_chirpy_red")
    val isRed: Boolean
)