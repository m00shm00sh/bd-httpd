package util

import AppConfig
import doModule

import com.zaxxer.hikari.HikariDataSource
import io.ktor.client.HttpClient
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation

internal fun withServer(block: suspend HttpClient.(AppConfig.App) -> Unit) =
    testApplication {
        val config = AppConfig.App(
            db = HikariDataSource(),
            jwt = AppConfig.Jwt(issuer = "test:mem"),
            polka = AppConfig.Polka("f271c81ff7084ee5b99a5091b42d486e"),
            platform = "dev"
        )
        application {
            val db = getDatabase()
            doModule(db, config)
        }
        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }
        client.block(config)
    }