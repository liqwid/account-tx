package accounttx.accounts.handlers

import accounttx.accounts.model.AccountRequest
import accounttx.accounts.model.TransferRequest
import accounttx.accounts.service.AccountsService
import accounttx.common.bodyAsJson
import accounttx.common.gson
import io.vertx.ext.web.RoutingContext
import java.util.UUID

class AccountsHandlers {
    private val service = AccountsService()

    fun handleGetAccount(ctx: RoutingContext) {
        val id = UUID.fromString(ctx.pathParam("id"))
        val account = service.getAccount(id)
            ?: ctx.fail(404)

        ctx.response().end(gson.toJson(account))
    }

    fun handleListAccounts(ctx: RoutingContext) {
        val accounts = service.listAccounts()

        ctx.response().end(gson.toJson(accounts))
    }

    fun handleCreateAccount(ctx: RoutingContext) {
        val accountRequest = ctx.bodyAsJson<AccountRequest>()

        val account = service.createAccount(accountRequest)

        ctx.response().end(gson.toJson(account))
    }

    fun handleTransfer(ctx: RoutingContext) {
        val transferRequest = ctx.bodyAsJson<TransferRequest>()

        service.transfer(transferRequest.from, transferRequest.to, transferRequest.amount)
        ctx.response().end()
    }
}