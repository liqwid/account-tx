package accounttx

import accounttx.accounts.handlers.AccountsHandlers
import io.vertx.core.Vertx
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

suspend fun buildRouter(vertx: Vertx): Router {
    val router = Router.router(vertx)
    val handlers = AccountsHandlers()
    router.route().handler(BodyHandler.create())

    router.get("/accounts/:id").coroutineHandler { handlers.handleGetAccount(it) }
    router.get("/accounts").coroutineHandler { handlers.handleListAccounts(it) }
    router.post("/accounts").coroutineHandler { handlers.handleCreateAccount(it) }
    router.post("/accounts/transfer").coroutineHandler { handlers.handleTransfer(it) }

    return router
}

private suspend fun Route.coroutineHandler(fn: suspend (RoutingContext) -> Unit) {
    handler { ctx ->
        GlobalScope.launch(ctx.vertx().dispatcher()) {
            try {
                fn(ctx)
            } catch (ex: IllegalArgumentException) {
                ctx.fail(400, ex)
            } catch (ex: Throwable) {
                LoggerFactory.getLogger(this@coroutineHandler::class.java).error(ex)
                ctx.fail(500)
            }
        }
    }
}