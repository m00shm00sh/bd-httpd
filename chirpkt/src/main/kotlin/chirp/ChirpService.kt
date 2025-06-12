package chirp

import Database
import db.generated.tables.references.CHIRPS
import forEachLazy
import toKotlinInstant
import uuidGenerator
import java.util.UUID


internal class ChirpService(private val db: Database) {
    suspend fun createChirp(request: ChirpRequest, userId: UUID): ChirpResponse =
        uuidGenerator.generate().let { chirpId ->
            db.op {
                withDsl {
                    insertInto(CHIRPS, CHIRPS.ID, CHIRPS.BODY, CHIRPS.USER_ID)
                        .values(chirpId, request.body, userId)
                        .returningResult(CHIRPS.CREATED_AT, CHIRPS.UPDATED_AT)
                }.suspendedBlockingFetch {
                    fetchSingle().let { (created, updated) ->
                        checkNotNull(created)
                        checkNotNull(updated)
                        val created = created.toKotlinInstant()
                        val updated = updated.toKotlinInstant()
                        ChirpResponse(chirpId, created, updated, request.body, userId)
                    }
                }
            }
        }

    suspend fun getChirps(userId: UUID? = null): List<ChirpResponse> =
        db.op {
            withDsl {
                select(CHIRPS.ID,
                    CHIRPS.CREATED_AT, CHIRPS.UPDATED_AT,
                    CHIRPS.BODY, CHIRPS.USER_ID
                )
                    .from(CHIRPS)
                    .apply {
                        if (userId != null)
                            where(CHIRPS.USER_ID.eq(userId))
                    }
                    .orderBy(CHIRPS.CREATED_AT)
            }.suspendedBlockingFetch {
                buildList {
                    forEachLazy { (chirpId, created, updated, body, userId) ->
                        checkNotNull(chirpId)
                        checkNotNull(created)
                        // is this a race condition that can actually occur or is the lock held during
                        // trigger propagation on (post-)update enough to make this impossible?
                        if (updated == null)
                            return@forEachLazy
                        checkNotNull(body)
                        checkNotNull(userId)
                        val created = created.toKotlinInstant()
                        val updated = updated.toKotlinInstant()
                        add(ChirpResponse(chirpId, created, updated, body, userId))
                    }
                }
            }
        }

    suspend fun getChirpById(chirpId: UUID): ChirpResponse? =
        db.op {
            withDsl {
                select(CHIRPS.ID,
                    CHIRPS.CREATED_AT, CHIRPS.UPDATED_AT,
                    CHIRPS.BODY, CHIRPS.USER_ID
                )
                    .from(CHIRPS)
                    .where(CHIRPS.ID.eq(chirpId))
            }.suspendedBlockingFetch {
                fetchOne()?.let { (chirpId, created, updated, body, userId) ->
                    checkNotNull(chirpId)
                    checkNotNull(created)
                    if (updated == null)
                        return@suspendedBlockingFetch null
                    checkNotNull(body)
                    checkNotNull(userId)
                    val created = created.toKotlinInstant()
                    val updated = updated.toKotlinInstant()
                    return@suspendedBlockingFetch ChirpResponse(chirpId, created, updated, body, userId)
                }
            }
        }

    suspend fun deleteChirp(chirpId: UUID) =
        db.op {
            withDsl {
                deleteFrom(CHIRPS)
                    .where(CHIRPS.ID.eq(chirpId))
            }.suspendedBlockingIO {
                return@suspendedBlockingIO execute() == 1
            }
        }
}
