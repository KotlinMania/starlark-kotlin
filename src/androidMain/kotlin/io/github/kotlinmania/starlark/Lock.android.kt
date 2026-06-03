package io.github.kotlinmania.starlark

import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
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
