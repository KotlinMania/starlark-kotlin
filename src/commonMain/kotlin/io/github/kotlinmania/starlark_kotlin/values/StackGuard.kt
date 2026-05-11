<<<<<<< HEAD:src/commonMain/kotlin/io/github/kotlinmania/starlark/values/StackGuard.kt
// port-lint: source values/stack_guard.rs
package io.github.kotlinmania.starlark.values
=======
// port-lint: source src/values/stack_guard.rs
package io.github.kotlinmania.starlark_kotlin.values
>>>>>>> origin/main:src/commonMain/kotlin/io/github/kotlinmania/starlark_kotlin/values/StackGuard.kt

/*
 * Copyright 2019 The Starlark in Rust Authors.
 * Copyright (c) Facebook, Inc. and its affiliates.
 * Copyright (c) 2025 Sydney Renee, The Solace Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/** Guard to check we don't recurse too deeply with nested operations like Equals. */

<<<<<<< HEAD:src/commonMain/kotlin/io/github/kotlinmania/starlark/values/StackGuard.kt
import io.github.kotlinmania.starlark.unlikely
import io.github.kotlinmania.threadlocal.ThreadLocal
=======
import io.github.kotlinmania.starlark_kotlin.unlikely
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
>>>>>>> origin/main:src/commonMain/kotlin/io/github/kotlinmania/starlark_kotlin/values/StackGuard.kt

// Maximum recursion level for comparison
private const val MAX_RECURSION: Int = 3000

// A thread-local counter is used to detect too deep recursion.
//
// Thread-local is chosen instead of explicit function "recursion" parameter
// for two reasons:
// * It's possible to propagate stack depth across external functions like
//   `Display.toString` where passing a stack depth parameter is hard
// * We need to guarantee that stack depth is not lost in complex invocation
//   chains like function calls compare which calls native function which calls
//   starlark function which calls to_str. We could change all evaluation stack
//   signatures to accept some "context" parameters, but passing it as
//   thread-local is easier.
private class StackDepth(
    var value: Int,
)

private val STACK_DEPTH: ThreadLocal<StackDepth> = ThreadLocal()

private fun stackDepth(): StackDepth = STACK_DEPTH.getOr { StackDepth(value = 0) }

/**
 * Stored previous stack depth before calling [stackGuard].
 *
 * Stores the previous stack depth back to thread-local on [close].
 */
class StackGuard internal constructor(
    private val prevDepth: Int,
) : AutoCloseable {
    // impl Drop for StackGuard
    override fun close() {
        stackDepth().value = prevDepth
    }
}

/** Increment stack depth. */
private fun inc(): StackGuard {
    val depth = stackDepth()
    val prevDepth = depth.value
    depth.value = prevDepth + 1
    return StackGuard(prevDepth = prevDepth)
}

/** Check stack depth does not exceed configured max stack depth. */
private fun check() {
    if (unlikely(stackDepth().value >= MAX_RECURSION)) {
        throw ControlError.TooManyRecursionLevel
    }
}

/**
 * Try increment stack depth.
 *
 * Return opaque object which resets stack to previous value
 * on [AutoCloseable.close].
 *
 * If stack depth exceeds configured limit, throws error.
 */
internal fun stackGuard(): StackGuard {
    check()
    return inc()
}
