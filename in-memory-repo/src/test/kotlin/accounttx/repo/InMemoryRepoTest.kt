package accounttx.repo

import accounttx.testutils.massiveRun
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.IllegalStateException
import java.util.UUID
import kotlin.test.assertEquals

data class TestEntity(
    override val id: UUID,
    var counter: Int = 0
) : Entity<UUID>

class TestRepo : InMemoryRepo<UUID, TestEntity>() {
    override fun TestEntity.copy(): TestEntity = copy()
}

internal class InMemoryRepoTest {
    private lateinit var repo: TestRepo

    @BeforeEach
    fun initRepo() {
        repo = TestRepo()
    }

    @Test
    internal fun `it should handle multiple inserts`() {
        massiveRun {
            repo.insert(TestEntity(UUID.randomUUID()))
        }
        println("repo size is ${repo.count()}")
        assertEquals(100000, repo.count())
    }

    @Test
    internal fun `it handles combined locks modification`() {
        val id1 = UUID.randomUUID()
        val id2 = UUID.randomUUID()
        repo.insert(TestEntity(id1))
        repo.insert(TestEntity(id2))
        massiveRun {
            repo.withLockedPair(id1, id2) { entity1, entity2 ->
                entity1.counter++
                entity2.counter++
            }
        }
        val result1 = repo[id1]
        val result2 = repo[id2]
        assertEquals(100000, result1?.counter)
        assertEquals(100000, result2?.counter)
    }

    @Test
    internal fun `withLockedPair throws IllegalStateException if no item is found`() {
        assertThrows<IllegalStateException> {
            repo.withLockedPair(UUID.randomUUID(), UUID.randomUUID()) { _, _ -> }
        }
    }
}