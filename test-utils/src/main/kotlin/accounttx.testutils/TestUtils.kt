package accounttx.testutils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext
import kotlin.system.measureTimeMillis

private val computationExecutor: Executor = Executors.newFixedThreadPool(
    Runtime.getRuntime().availableProcessors()
)

private val dispatcher = object : CoroutineDispatcher() {
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        computationExecutor.execute(block)
    }
}

fun massiveRun(action: suspend (index: Int) -> Unit) = runBlocking(dispatcher) {
    val n = 100 // number of coroutines to launch
    val k = 1000 // times an action is repeated by each coroutine
    val time = measureTimeMillis {
        val jobs = List(n) { jobIndex ->
            launch {
                repeat(k) { action(it + k * jobIndex) }
            }
        }
        jobs.forEach { it.join() }
    }
    println("Completed ${n * k} actions in $time ms")
}
