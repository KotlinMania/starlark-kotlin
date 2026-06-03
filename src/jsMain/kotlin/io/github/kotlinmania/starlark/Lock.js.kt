package io.github.kotlinmania.starlark

// JS is single-threaded; no-op lock.
actual class ReentrantLock actual constructor() {
    actual fun lock() {}

    actual fun unlock() {}
}
