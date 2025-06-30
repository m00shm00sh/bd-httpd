package webhook

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
internal data class PolkaRequest(
    val event: String,
    val data: PolkaData
) {
    @Serializable
    data class PolkaData(
        @Serializable(with = UUIDSerializer::class)
        @SerialName("user_id")
        val userId: UUID
    )
}