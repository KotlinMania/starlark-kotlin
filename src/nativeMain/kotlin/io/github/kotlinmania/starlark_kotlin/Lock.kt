package io.github.kotlinmania.starlark_kotlin

import kotlin.concurrent.AtomicInt

actual class ReentrantLock actual constructor() {
    private val locked = AtomicInt(0)

    actual fun lock() {
        while (!locked.compareAndSet(0, 1)) {
            // spin
        }
    }

    actual fun unlock() {
        locked.store(0)
    }
}
