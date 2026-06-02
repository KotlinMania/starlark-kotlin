package io.github.kotlinmania.starlark

/**
 * Multiplatform ReentrantLock abstraction.
 * On threaded platforms uses a Kotlin atomic spin lock.
 * On JS/WasmJS is a no-op (single-threaded).
 */
expect class ReentrantLock() {
    fun lock()

    fun unlock()
}

inline fun <T> ReentrantLock.withLock(action: () -> T): T {
    lock()
    try {
        return action()
    } finally {
        unlock()
    }
}
