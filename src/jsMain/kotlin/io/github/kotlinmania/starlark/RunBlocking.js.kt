package io.github.kotlinmania.starlark

import kotlinx.coroutines.CoroutineScope

actual fun <T> runBlocking(block: suspend CoroutineScope.() -> T): T = throw UnsupportedOperationException("runBlocking is not supported on JS")
