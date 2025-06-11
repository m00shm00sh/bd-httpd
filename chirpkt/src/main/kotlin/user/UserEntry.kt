package user

import at.favre.lib.crypto.bcrypt.BCrypt

internal data class UserEntry(
    val email: String,
    val password: PasswordString
)

@JvmInline
internal value class PasswordString(
    val value: String
) {
    fun test(plaintext: String): Boolean =
        BCrypt.verifyer().verify(plaintext.toCharArray(), value).verified

    companion object {
        fun fromPlaintext(s: String): PasswordString {
            val bytes = s.toCharArray()
            return PasswordString(BCrypt.withDefaults().hashToString(10, bytes))
        }
    }
}

internal val NO_PASSWORD = PasswordString("")

internal fun UserRequest.toUserEntry() =
    UserEntry(email, PasswordString.fromPlaintext(password))