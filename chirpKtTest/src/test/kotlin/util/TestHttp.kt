package util

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import org.junit.jupiter.api.*
import kotlin.test.assertEquals

internal suspend inline fun <reified ReqBody, reified RespBody> HttpClient.testEndpoint(
    method: Method,
    endpoint: String,
    crossinline reqBuilder: HttpRequestBuilder.() -> Unit = {},
    reqBody: ReqBody = Unit as ReqBody,
    responseCode: HttpStatusCode = HttpStatusCode.OK,
    message: String = "$method $endpoint",
    acceptAdditional: List<ContentType> = emptyList(),
    crossinline testResponse: RespBody.() -> Unit = {},
) {
    val expandedMessage = message.replace("{}", "$method $endpoint")

    (method.method)(endpoint) {
        reqBuilder()
        acceptAdditional.forEach { accept(it) }
        if (ReqBody::class != Unit::class)
            setBody(reqBody)
    }.apply {
        assertEquals(responseCode, status, "$expandedMessage response code")
        if (RespBody::class == Unit::class)
            return
        val body = assertDoesNotThrow("$expandedMessage response body type") {
            if (RespBody::class != String::class)
                body<RespBody>()
            else
                bodyAsText() as RespBody
        }
        /* we expect testResponse to have one or more assertXxx calls whose message param we have no control over
         * so use assertAll to wrap failures with message context
         */
        assertAll("$expandedMessage response body value", { body.testResponse() })
    }
}

internal enum class Method(val method: suspend HttpClient.(String, HttpRequestBuilder.() -> Unit) -> HttpResponse) {
    PostJson(HttpClient::postJson),
    Post(HttpClient::post),
    PutJson(HttpClient::putJson),
    PatchJson(HttpClient::patchJson),
    GetJson(HttpClient::get), // inside withServer
    Get(HttpClient::get), // outside withServer
    Delete(HttpClient::delete)
}
