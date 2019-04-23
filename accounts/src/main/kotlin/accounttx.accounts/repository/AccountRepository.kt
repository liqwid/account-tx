package accounttx.accounts.repository

import accounttx.accounts.model.Account
import accounttx.repo.InMemoryRepo
import java.util.UUID

class AccountRepository : InMemoryRepo<UUID, Account>() {
    override fun Account.copy(): Account = copy()
}