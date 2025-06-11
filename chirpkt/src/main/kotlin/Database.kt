import com.zaxxer.hikari.*
import kotlinx.coroutines.*
import org.jetbrains.annotations.Blocking
import org.jooq.*
import org.jooq.exception.*
import org.jooq.impl.*
import org.jooq.kotlin.coroutines.transactionCoroutine
import java.util.function.Consumer

internal class Database(dataSource: HikariDataSource) {
    private val ioDisp = Dispatchers.IO
    private val context = ioDisp + CoroutineName("JooqIO")
    private val dsl: DSLContext =
        DSL.using(
            DefaultConfiguration().apply {
                set(DataSourceConnectionProvider(dataSource))
                set(inferDialectFromJdbcUrl(dataSource))
                set(ExecutorProvider(ioDisp::asExecutor))
            }
        )

    /** Suspend blocking operation with context onto current continuation.
     *
     * @see suspendedBlockingIO for non-result version
     * @see withContext
     */
    suspend fun <T : Record, R> ResultQuery<T>.suspendedBlockingFetch(block: ResultQuery<T>.() -> R): R {
        return withContext(context) {
            runInterruptible {
                block()
            }
        }
    }

    /** Suspend blocking operation with context onto current continuation.
     *
     * @see suspendedBlockingFetch for result version
     * @see withContext
     */
    suspend fun <R> Query.suspendedBlockingIO(block: Query.() -> R): R {
        return withContext(context) {
            runInterruptible {
                block()
            }
        }
    }

    /** Do something with a [DSLContext] provided as receiver. */
    fun <R> withDsl(block: DSLContext.() -> R): R =
        dsl.block()

    /** Evaluate [block], translating SQL exceptions.
     * @throws IllegalArgumentException if executing the SQL statement caused an integrity constraint violation
     * @throws IllegalStateException if executing the SQL statement caused any other kind of error
     * @throws CancellationException if coroutine was cancelled or otherwise interrupted
     */
    suspend fun <R> op(block: suspend Database.() -> R): R {
        try {
            return block()
        } catch (ex: DataAccessException) {
            throw translateJooqException(ex)
        }
    }

    suspend fun <R> transaction(block: DSLContext.() -> R): R =
        dsl.transactionCoroutine(context) { c -> c.dsl().block() }

}

@Blocking
internal fun <T : Record> ResultQuery<T>.forEachLazy(action: (T) -> Unit) =
    forEach(Consumer(action))

private fun inferDialectFromJdbcUrl(hikariConfig: HikariConfig) =
    hikariConfig.jdbcUrl.run {
        when {
            /* Other DB engines are supported by Jooq but may require changing the DDL in the migration scripts, which
             * is undesirable.
             * We will only concern ourselves with the immediate one and a possible successor should scaling to
             * concurrent writes (i.e. multiple users) become necessary.
             * It is not a design priority to support R2DBC.
             */
            startsWith("jdbc:sqlite:") -> SQLDialect.SQLITE
            startsWith("jdbc:postgres") -> SQLDialect.POSTGRES
            else -> throw IllegalArgumentException("unexpected jdbc url $this")
        }
    }

private fun translateJooqException(t: DataAccessException): Throwable {
    if (t is IntegrityConstraintViolationException)
        return IllegalArgumentException("Database exception: ${t.message}", t.cause)
    return IllegalStateException("Database exception: ${t.message}", t.cause)
}
