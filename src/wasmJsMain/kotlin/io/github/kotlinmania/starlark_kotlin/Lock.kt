package io.github.kotlinmania.starlark_kotlin

// WasmJS is single-threaded; no-op lock.
actual class ReentrantLock actual constructor() {
    actual fun lock() {}
    actual fun unlock() {}
}
