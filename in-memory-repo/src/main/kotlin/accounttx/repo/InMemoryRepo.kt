package accounttx.repo

import java.lang.IllegalStateException
import java.util.concurrent.ConcurrentHashMap

abstract class InMemoryRepo<K, E : Entity<K>> {
    private val map = ConcurrentHashMap<K, E>()

    abstract fun E.copy(): E

    operator fun get(id: K): E? {
        return map[id]?.copy()
    }

    fun list(): List<E> {
        return map.values.map { it.copy() }.toList()
    }

    fun count(): Int {
        return map.size
    }

    fun insert(entity: E) {
        val id = entity.id
        val inserted = entity.copy()
        map.putIfAbsent(id, inserted)
    }

    @Throws(IllegalStateException::class)
    fun withLockedPair(id1: K, id2: K, block: (entity1: E, entity2: E) -> Unit) {
        val entity1 = map[id1]
            ?: throw IllegalStateException("No entity with id $id1")
        val entity2 = map[id2]
            ?: throw IllegalStateException("No entity with id $id2")

        synchronized(map) {
            block(entity1, entity2)
        }
    }

    fun <T> inTransaction(block: () -> T): T {
        synchronized(map) {
            return block()
        }
    }
}