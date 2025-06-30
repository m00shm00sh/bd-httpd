package util

import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.statement.HttpResponse
import io.ktor.http.*
import io.ktor.http.contentType

internal suspend fun HttpClient.postJson(urlString: String, block: HttpRequestBuilder.() -> Unit) =
    post(urlString) {
        contentType(ContentType.Application.Json)
        block()
    }

internal suspend fun HttpClient.patchJson(urlString: String, block: HttpRequestBuilder.() -> Unit) =
    patch(urlString) {
        contentType(ContentType.Application.Json)
        block()
    }

internal suspend fun HttpClient.putJson(urlString: String, block: HttpRequestBuilder.() -> Unit) =
    put(urlString) {
        contentType(ContentType.Application.Json)
        block()
    }
