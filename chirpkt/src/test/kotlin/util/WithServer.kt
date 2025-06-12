package util

import AppConfig
import Database
import doModule
import refresh.RefreshService
import tokens.JwtService
import user.UserService
import chirp.ChirpService

import com.zaxxer.hikari.HikariDataSource
import io.ktor.client.HttpClient
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.getAllRoutes
import io.ktor.server.routing.routing
import kotlin.test.assertEquals

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
            doModule(
                config,
                UserService(db),
                RefreshService(db),
                JwtService(config.jwt),
                ChirpService(db),
            )
        }
        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }
        client.block(config)
    }