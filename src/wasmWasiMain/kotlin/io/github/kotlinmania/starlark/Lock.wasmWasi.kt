package io.github.kotlinmania.starlark

// WasmWasi currently runs single-threaded; no-op lock.
actual class ReentrantLock actual constructor() {
    actual fun lock() {}

    actual fun unlock() {}
}
