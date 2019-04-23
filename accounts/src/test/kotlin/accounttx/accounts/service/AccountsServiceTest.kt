package accounttx.accounts.service

import accounttx.accounts.model.AccountRequest
import accounttx.testutils.massiveRun
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.util.Currency
import kotlin.test.assertEquals

internal class AccountsServiceTest {
    private val service = AccountsService()
    @Test
    internal fun `should perform multiple parallel account operations correctly`() {
        val account1 = AccountRequest(
            currency = Currency.getInstance("USD"),
            balance = BigDecimal("100000")
        ).let(service::createAccount)
        val account2 = AccountRequest(
            currency = Currency.getInstance("USD"),
            balance = BigDecimal("200000")
        ).let(service::createAccount)
        val account3 = AccountRequest(
            currency = Currency.getInstance("USD"),
            balance = BigDecimal("300000")
        ).let(service::createAccount)
        massiveRun {
            service.transfer(account1.id, account2.id, BigDecimal("1"))
            service.transfer(account2.id, account3.id, BigDecimal("2"))
            service.transfer(account3.id, account1.id, BigDecimal("3"))
        }
        assertEquals("300000", service.getAccount(account1.id)?.balance?.toString())
        assertEquals("100000", service.getAccount(account2.id)?.balance?.toString())
        assertEquals("200000", service.getAccount(account3.id)?.balance?.toString())
    }

    @Test
    internal fun `should perform multiple parallel account operations on many accounts correctly`() {
        massiveRun {
            AccountRequest(
                currency = Currency.getInstance("USD"),
                balance = BigDecimal("10")
            ).let(service::createAccount)
        }
        val ids = service.listAccounts().map { it.id }.toMutableList()
        ids.add(ids.first())
        massiveRun { index ->
            service.transfer(ids[index], ids[index + 1], BigDecimal("3"))
        }
        ids.forEach {
            assertEquals("10", service.getAccount(it)?.balance?.toString())
        }
    }

    @Test
    internal fun `should sync multiple parallel operations with a transaction`() {
        val account1 = AccountRequest(
            currency = Currency.getInstance("USD"),
            balance = BigDecimal("100000")
        ).let(service::createAccount)
        val account2 = AccountRequest(
            currency = Currency.getInstance("USD"),
            balance = BigDecimal("200000")
        ).let(service::createAccount)
        val account3 = AccountRequest(
            currency = Currency.getInstance("USD"),
            balance = BigDecimal("300000")
        ).let(service::createAccount)
        massiveRun {
            service.inTransaction {
                service.transfer(account1.id, account2.id, BigDecimal("100000"))
                service.transfer(account2.id, account3.id, BigDecimal("100000"))
                service.transfer(account3.id, account1.id, BigDecimal("100000"))
            }
        }
        assertEquals("100000", service.getAccount(account1.id)?.balance?.toString())
        assertEquals("200000", service.getAccount(account2.id)?.balance?.toString())
        assertEquals("300000", service.getAccount(account3.id)?.balance?.toString())
    }

    @Test
    internal fun `should throw IllegalArgumentException if trying to transfer between different currencies`() {
        val account1 = AccountRequest(
            currency = Currency.getInstance("USD"),
            balance = BigDecimal("100000")
        ).let(service::createAccount)
        val account2 = AccountRequest(
            currency = Currency.getInstance("EUR"),
            balance = BigDecimal("100000")
        ).let(service::createAccount)

        assertThrows<IllegalArgumentException>("Unsupported convertion pair USD-EUR") {
            service.transfer(account1.id, account2.id, BigDecimal("100000"))
        }
    }

    @Test
    internal fun `should throw IllegalArgumentException if trying to transfer more than balance`() {
        val account1 = AccountRequest(
            currency = Currency.getInstance("USD"),
            balance = BigDecimal("100000")
        ).let(service::createAccount)
        val account2 = AccountRequest(
            currency = Currency.getInstance("USD"),
            balance = BigDecimal("100000")
        ).let(service::createAccount)

        assertThrows<IllegalArgumentException>("200000 USD is larger than current balance of account ${account1.id}") {
            service.transfer(account1.id, account2.id, BigDecimal("200000"))
        }
    }
}
