import refresh.RefreshService

import com.sksamuel.hoplite.*
import com.typesafe.config.ConfigException
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.*
import kotlin.time.*

internal object AppConfig {

    data class Polka(
        val key: String
    )

    data class Jwt(
        val issuer: String = "http://chirp:mem/",
        val secret: Secret = Secret(RefreshService.hexString()),
        val timeout: Duration = 1.toDuration(DurationUnit.HOURS)
    )

    data class App(
        val db: HikariDataSource,
        val jwt: Jwt,
        val polka: Polka,
        val platform: String
    )

    fun config(with: Map<String, Any>) =
        ConfigLoaderBuilder.default()
            .addMapSource(with)
            .build()
            .loadConfigOrThrow<App>()
}

/* We can't use ApplicationConfig.toMap() because it provides a recursive structure for keys instead of a
 * property map, meaning
 *      x { y = z }
 * produces
 *      mapOf("x" to mapOf("y" to "z"))
 * not
 *      mapOf("x.y" to "z")
 * Hoplite's property map handler requires the second form to behave as desired.
 * So we provide a thin wrapper that converts an ApplicationConfig to a Map<String, Union<String, List<String>>>
 *
 * Methods that are unused by Hoplite but exist to satisfy the Map interface all throw UOE.
 */
internal fun ApplicationConfig.asPropertyMap(): Map<String, Any /* String | List<String> */> =
    object : Map<String, Any> {
        private val _keys = this@asPropertyMap.keys()
        private val _entryMap by lazy {
            _keys.associateWith { get(it)!! }
        }
        override val size = _keys.size
        override val keys: Set<String> = _keys
        override val values: Collection<Any>
            get() = throw UnsupportedOperationException()
        override val entries: Set<Map.Entry<String, Any>> by lazy {
            _entryMap.entries
        }

        override fun isEmpty() = _keys.isEmpty()

        override fun containsKey(key: String): Boolean = _keys.contains(key)

        override fun containsValue(value: Any): Boolean = throw UnsupportedOperationException()

        override fun get(key: String): Any? /* String | List<String> | null */ {
            val v = propertyOrNull(key) ?: return null
            return try {
                v.getString()
            } catch (_: ConfigException.WrongType) {
                v.getList()
            }
        }
    }
