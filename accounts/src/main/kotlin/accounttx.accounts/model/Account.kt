package accounttx.accounts.model

import accounttx.repo.Entity
import java.math.BigDecimal
import java.util.Currency
import java.util.UUID
import java.util.UUID.randomUUID

data class Account(
    override val id: UUID = randomUUID(),
    override val currency: Currency,
    override var balance: BigDecimal
) : Entity<UUID>, AccountDetails