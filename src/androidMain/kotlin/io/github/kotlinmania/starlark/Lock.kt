package io.github.kotlinmania.starlark

actual class ReentrantLock actual constructor() {
    private val lock = java.util.concurrent.locks.ReentrantLock()

    actual fun lock() = lock.lock()
    actual fun unlock() = lock.unlock()
}
