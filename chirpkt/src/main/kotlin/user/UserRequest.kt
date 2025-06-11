package user

import kotlinx.serialization.Serializable

@Serializable
internal data class UserRequest(
    val email: String,
    val password: String
)