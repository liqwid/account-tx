package accounttx.accounts.model

import java.math.BigDecimal
import java.util.UUID

data class TransferRequest(
    val from: UUID,
    val to: UUID,
    val amount: BigDecimal
)