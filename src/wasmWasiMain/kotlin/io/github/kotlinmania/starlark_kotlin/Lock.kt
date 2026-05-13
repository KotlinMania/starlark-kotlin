package io.github.kotlinmania.starlark_kotlin

// WasmWasi currently runs single-threaded; no-op lock.
actual class ReentrantLock actual constructor() {
    actual fun lock() {}
    actual fun unlock() {}
}

