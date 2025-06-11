package chirp

import kotlinx.datetime.Instant
import kotlinx.serialization.*
import java.util.UUID

@Serializable
internal data class ChirpResponse(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    @SerialName("created_at")
    val created: Instant,
    @SerialName("updated_at")
    val updated: Instant,
    val body: String,
    @Serializable(with = UUIDSerializer::class)
    val userId: UUID,
)