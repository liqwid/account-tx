package accounttx.common

import com.google.gson.Gson
import io.vertx.ext.web.RoutingContext
import java.lang.IllegalArgumentException

val gson = Gson()

inline fun <reified T> fromJson(body: String): T =
    gson.fromJson(body, T::class.java)

inline fun <reified T> RoutingContext.bodyAsJson(): T {
    val body = bodyAsString
    try {
        return fromJson(body)
    } catch (ex: Throwable) {
        throw IllegalArgumentException("Failed to parse request body $body")
    }
}
