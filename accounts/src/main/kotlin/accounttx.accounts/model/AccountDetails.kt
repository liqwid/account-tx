package accounttx.accounts.model

import java.math.BigDecimal
import java.util.Currency

interface AccountDetails {
    val currency: Currency
    var balance: BigDecimal
}