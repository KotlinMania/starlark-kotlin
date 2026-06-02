package io.github.kotlinmania.starlark

import kotlinx.coroutines.CoroutineScope

/**
 * Multiplatform `runBlocking` wrapper.
 *
 * On JVM and Native, delegates to `kotlinx.coroutines.runBlocking`.
 * On JS and WasmJS, throws [UnsupportedOperationException] since blocking
 * the single-threaded event loop is not possible.
 */
expect fun <T> runBlocking(block: suspend CoroutineScope.() -> T): T
