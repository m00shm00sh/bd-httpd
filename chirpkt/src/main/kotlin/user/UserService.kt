package user

import Database
import db.generated.tables.references.USERS
import toKotlinInstant
import uuidGenerator
import java.util.UUID

internal class UserService(private val db: Database) {
    suspend fun createUser(user: UserEntry): UserResponse =
        uuidGenerator.generate().let { userId ->
            db.op {
                withDsl {
                    insertInto(USERS, USERS.ID, USERS.EMAIL,  USERS.PASS)
                        .values(userId, user.email, user.password.value)
                        .returningResult(USERS.CREATED_AT, USERS.UPDATED_AT)
                }.suspendedBlockingFetch {
                    fetchSingle().let { (created, updated) ->
                        checkNotNull(created)
                        checkNotNull(updated)
                        val created = created.toKotlinInstant()
                        val updated = updated.toKotlinInstant()
                        UserResponse(userId, created, updated, user.email, false)
                    }
                }
            }
        }

    suspend fun getUserByEmail(email: String): Pair<UserEntry, UserResponse>? {
        var user = UserEntry(email, NO_PASSWORD)
        lateinit var response: UserResponse
        db.op {
            withDsl {
                select(USERS.ID, USERS.PASS,
                    USERS.CREATED_AT, USERS.UPDATED_AT,
                    USERS.IS_CHIRPY_RED
                )
                    .from(USERS)
                    .where(USERS.EMAIL.eq(email))
            }.suspendedBlockingFetch {
                fetchOne()?.let { (id, pass, created, updated, red) ->
                    checkNotNull(id)
                    checkNotNull(pass)
                    checkNotNull(created)
                    checkNotNull(updated)
                    checkNotNull(red)
                    user = user.copy(password = PasswordString(pass))
                    response = UserResponse(id,
                        created.toKotlinInstant(),
                        updated.toKotlinInstant(),
                        email, red
                    )
                }
            }
        }
        if (user.password == NO_PASSWORD)
            return null
        return user to response
    }

    suspend fun existsUserForId(id: UUID): Boolean =
        db.op {
            withDsl {
                select(USERS.ID)
                    .from(USERS)
                    .where(USERS.ID.eq(id))
            }.suspendedBlockingFetch {
                fetchOne() != null
            }
        }

    suspend fun updateUser(newDetails: UserEntry, userId: UUID): UserResponse? =
        db.op {
            // For SQLite, need to do update then select for the post-insert trigger to replace values correctly
            val (q1, q2) =
            withDsl {
                val q1 = update(USERS)
                    .set(USERS.EMAIL, newDetails.email)
                    .set(USERS.PASS, newDetails.password.value)
                    .set(USERS.UPDATED_AT, null as java.time.LocalDateTime?) // to be filled by trigger
                    .where(USERS.ID.eq(userId))
                val q2 = select(USERS.CREATED_AT, USERS.UPDATED_AT, USERS.IS_CHIRPY_RED)
                        .from(USERS)
                        .where(USERS.ID.eq(userId))
                q1 to q2
            }
            if (!q1.suspendedBlockingIO { execute() == 1 })
                return@op null
            q2.suspendedBlockingFetch {
                fetchOne()?.let { (created, updated, red) ->
                    checkNotNull(created)
                    checkNotNull(updated)
                    checkNotNull(red)
                    return@let UserResponse(null,
                        created.toKotlinInstant(),
                        updated.toKotlinInstant(),
                        null, red
                    )
                } ?: return@suspendedBlockingFetch null
            }
        }

    suspend fun upgradeUser(user: UUID): Boolean =
        db.op {
            withDsl {
                update(USERS)
                    .set(USERS.IS_CHIRPY_RED, true)
                    .where(USERS.ID.eq(user))
            }.suspendedBlockingIO {
                return@suspendedBlockingIO execute() > 0
            }
        }

    suspend fun deleteAllUsers() {
        db.op {
            withDsl {
                deleteFrom(USERS)
            }.suspendedBlockingIO {
                execute()
            }
        }
    }
}
