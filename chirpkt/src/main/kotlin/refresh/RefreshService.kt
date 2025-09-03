package refresh

import Database
import cryptoRand
import db.generated.tables.references.REFRESH_TOKENS
import java.util.UUID
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlinx.datetime.*

internal class RefreshService(private val db: Database) {

    suspend fun createRefreshTokenForUser(uid: UUID): String {
        val token = hexString()
        val exp = Clock.System.now().plus(60.toDuration(DurationUnit.DAYS))
        db.op {
            withDsl {
                insertInto(REFRESH_TOKENS,
                    REFRESH_TOKENS.TOKEN, REFRESH_TOKENS.USER_ID, REFRESH_TOKENS.EXPIRES_AT)
                    .values(token, uid, exp.toLocalDateTime(TimeZone.UTC).toJavaLocalDateTime())
            }.suspendedBlockingIO {
                execute()
            }
        }
        return token
    }

    suspend fun getUserByRefresh(refresh: String): UUID? =
        db.op {
            withDsl {
                val exp = Clock.System.now()
                select(REFRESH_TOKENS.USER_ID)
                    .from(REFRESH_TOKENS)
                    .where(REFRESH_TOKENS.TOKEN.eq(refresh)
                        .and(REFRESH_TOKENS.REVOKED_AT.isNull)
                        .and(REFRESH_TOKENS.EXPIRES_AT.greaterThan(exp.toLocalDateTime(TimeZone.UTC).toJavaLocalDateTime())
                        ))
            }.suspendedBlockingFetch {
                fetchOne()?.let { (uid) ->
                    checkNotNull(uid)
                    uid
                }
            }
        }

    suspend fun revokeToken(refresh: String): Unit =
        db.op {
            withDsl {
                val exp = Clock.System.now()
                update(REFRESH_TOKENS)
                    .set(REFRESH_TOKENS.REVOKED_AT, exp.toLocalDateTime(TimeZone.UTC).toJavaLocalDateTime())
                    .where(REFRESH_TOKENS.TOKEN.eq(refresh))
            }.suspendedBlockingIO {
                execute()
            }
        }

    companion object {
        fun hexString(): String {
            val k = ByteArray(32)
            cryptoRand.nextBytes(k)
            @OptIn(ExperimentalStdlibApi::class)
            val token = k.toHexString()
            return token
        }
    }

}