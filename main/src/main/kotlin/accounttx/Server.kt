package accounttx

import io.vertx.core.http.HttpServer
import io.vertx.kotlin.core.http.listenAwait
import io.vertx.kotlin.coroutines.CoroutineVerticle

class Server(private val port: Int) : CoroutineVerticle() {
    private lateinit var server: HttpServer

    override suspend fun start() {

        val router = buildRouter(vertx)

        server = vertx.createHttpServer()
            .requestHandler(router)
            .listenAwait(port)
    }

    override suspend fun stop() {
        super.stop()

        server.close()
    }
}
