package refresh

import kotlinx.serialization.Serializable

@Serializable
internal data class RefreshResponse(
    val token: String
)