package util

import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java
import io.ktor.serialization.kotlinx.json.json
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import kotlinx.coroutines.test.runTest

internal fun withClient(block: suspend HttpClient.() -> Unit) =
    runTest {
        val client = HttpClient(Java) {
            install(ContentNegotiation) {
                json()
            }
        }
        client.block()
    }