package accounttx

import io.vertx.core.Vertx
import io.vertx.kotlin.core.deployVerticleAwait
import kotlinx.coroutines.runBlocking

const val PORT = 3003

fun main() {
    runBlocking {
        val vertx = Vertx.vertx()
        try {
            val server = Server(PORT)
            vertx.deployVerticleAwait(server)
            println("Server started on port $PORT")
        } catch (exception: Throwable) {
            println("Error starting server")
            exception.printStackTrace()
        }
    }
}