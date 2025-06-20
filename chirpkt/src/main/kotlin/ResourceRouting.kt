import io.ktor.http.HttpStatusCode
import io.ktor.server.application.isHandled
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingContext

internal suspend inline fun <reified O : Any> RoutingContext.handleResponseBody(result: O) {
    if (call.isHandled)
        return
    when (O::class) {
        HttpStatusCode::class -> call.respondText(result.toString(), status = result as HttpStatusCode)
        Unit::class -> call.respondText("", status = HttpStatusCode.OK)
        else -> call.respond(result)
    }
}
@JvmName("getReturningObject")
internal inline fun <reified T : Any, reified O : Any>
Route.get(crossinline body: suspend RoutingContext.(T) -> O): Route =
    get<T> { handleResponseBody(body(it)) }

// (+) request body; (+) response body
@JvmName("postReturningObject")
internal inline fun <reified T : Any, reified R : Any, reified O : Any>
Route.post(crossinline body: suspend RoutingContext.(T, R) -> O): Route =
    post<T, R> { resource, response -> handleResponseBody(body(resource, response)) }

// (-) request body; (+) response body
@JvmName("postWithoutRequestBodyReturningObject")
internal inline fun <reified T : Any, reified O: Any>
Route.post(crossinline body: suspend RoutingContext.(T) -> O): Route =
    post<T> { handleResponseBody(body(it)) }

// (+) request body; (+) response body
@JvmName("putReturningObject")
internal inline fun <reified T : Any, reified R : Any, reified O : Any>
Route.put(crossinline body: suspend RoutingContext.(T, R) -> O): Route =
    put<T, R> { resource, response -> handleResponseBody(body(resource, response)) }

// (-) request body; (+) response body
@JvmName("putWithoutRequestBodyReturningObject")
internal inline fun <reified T : Any, reified O: Any>
Route.put(crossinline body: suspend RoutingContext.(T) -> O): Route =
    put<T> { handleResponseBody(body(it)) }

@JvmName("deleteReturningObject")
internal inline fun <reified T : Any, reified O : Any>
Route.delete(crossinline body: suspend RoutingContext.(T) -> O): Route =
    delete<T> { handleResponseBody(body(it)) }

// (+) request body; (+) response body
@JvmName("patchReturningObject")
internal inline fun <reified T : Any, reified R : Any, reified O : Any>
Route.patch(crossinline body: suspend RoutingContext.(T, R) -> O): Route =
    patch<T, R> { resource, response -> handleResponseBody(body(resource, response)) }

// (-) request body; (+) response body
@JvmName("patchWithoutRequestBodyReturningObject")
internal inline fun <reified T : Any, reified O : Any>
Route.patch(crossinline body: suspend RoutingContext.(T) -> O): Route =
    patch<T> { handleResponseBody(body(it)) }


