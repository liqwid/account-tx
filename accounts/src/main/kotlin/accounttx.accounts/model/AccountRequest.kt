package accounttx.accounts.model

import java.math.BigDecimal
import java.util.Currency

data class AccountRequest(
    override val currency: Currency,
    override var balance: BigDecimal
) : AccountDetails