package accounttx.accounts.service

import accounttx.accounts.model.Account
import accounttx.accounts.model.AccountRequest
import accounttx.accounts.repository.AccountRepository
import java.lang.IllegalArgumentException
import java.math.BigDecimal
import java.util.UUID
import java.util.Currency

class AccountsService {
    private val repo = AccountRepository()

    fun listAccounts(): List<Account> = repo.list()

    fun getAccount(id: UUID): Account? = repo[id]

    @Throws(IllegalStateException::class)
    fun createAccount(accountRequest: AccountRequest): Account {
        val id = UUID.randomUUID()
        val account = Account(
            id = id,
            currency = accountRequest.currency,
            balance = accountRequest.balance
        )
        return inTransaction {
            if (repo[id] != null)
                throw IllegalStateException("Failed to create account")

            repo.insert(account)
            repo[id]!!
        }
    }

    @Throws(IllegalArgumentException::class)
    fun transfer(from: UUID, to: UUID, creditAmount: BigDecimal) {
        repo.withLockedPair(from, to) { fromAccount, toAccount ->
            val debitAmount = convert(fromAccount.currency, toAccount.currency, creditAmount)

            if (fromAccount.balance.compareTo(creditAmount) == -1)
                throw IllegalArgumentException("$creditAmount ${fromAccount.currency} is larger than current balance of account ${fromAccount.id}")

            fromAccount.balance = fromAccount.balance.subtract(creditAmount)
            toAccount.balance = toAccount.balance.add(debitAmount)
        }
    }

    fun <T> inTransaction(block: () -> T): T = repo.inTransaction(block)

    private fun convert(fromCurrency: Currency, toCurrency: Currency, amount: BigDecimal): BigDecimal {
        if (fromCurrency.currencyCode == toCurrency.currencyCode) return amount
        throw IllegalArgumentException("Unsupported convertion pair ${fromCurrency.currencyCode}-${toCurrency.currencyCode}")
    }
}