package chirp

import kotlinx.serialization.Serializable

@Serializable
internal data class ChirpRequest(
    val body: String
)