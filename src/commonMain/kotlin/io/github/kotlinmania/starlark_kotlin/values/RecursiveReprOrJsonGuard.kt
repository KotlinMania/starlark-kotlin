<<<<<<< HEAD:src/commonMain/kotlin/io/github/kotlinmania/starlark/values/RecursiveReprOrJsonGuard.kt
// port-lint: source values/recursive_repr_or_json_guard.rs
package io.github.kotlinmania.starlark.values
=======
// port-lint: source src/values/recursive_repr_or_json_guard.rs
package io.github.kotlinmania.starlark_kotlin.values
>>>>>>> origin/main:src/commonMain/kotlin/io/github/kotlinmania/starlark_kotlin/values/RecursiveReprOrJsonGuard.kt

/*
 * Copyright 2018 The Starlark in Rust Authors.
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

/** Detect recursion when doing `repr` or `to_json`. */

<<<<<<< HEAD:src/commonMain/kotlin/io/github/kotlinmania/starlark/values/RecursiveReprOrJsonGuard.kt
import io.github.kotlinmania.starlarkmap.smallset.SmallSet
import io.github.kotlinmania.threadlocal.ThreadLocal
import io.github.kotlinmania.starlark.values.layout.RawPointer
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.unlikely
=======
import io.github.kotlinmania.starlark_kotlin.collections.small_set.SmallSet
import io.github.kotlinmania.starlark_kotlin.values.layout.RawPointer
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
>>>>>>> origin/main:src/commonMain/kotlin/io/github/kotlinmania/starlark_kotlin/values/RecursiveReprOrJsonGuard.kt

/** Pop the stack on drop. */
// pub(crate) struct ReprStackGuard;
internal class ReprStackGuard : AutoCloseable {
    // impl Drop for ReprStackGuard
    override fun close() {
        val stack = reprStack()
        val popped = stack.pop()
        check(popped != null)
    }
}

/** Pop the stack on drop. */
// pub(crate) struct JsonStackGuard;
internal class JsonStackGuard : AutoCloseable {
    // impl Drop for JsonStackGuard
    override fun close() {
        val stack = jsonStack()
        val popped = stack.pop()
        check(popped != null)
    }
}

/** Returned when `repr` is called recursively and a cycle is detected. */
<<<<<<< HEAD:src/commonMain/kotlin/io/github/kotlinmania/starlark/values/RecursiveReprOrJsonGuard.kt
internal class ReprCycle : Exception()

/** Returned when `toJson` is called recursively and a cycle is detected. */
internal class JsonCycle : Exception()

private val REPR_STACK: ThreadLocal<SmallSet<RawPointer>> = ThreadLocal()

private val JSON_STACK: ThreadLocal<SmallSet<RawPointer>> = ThreadLocal()

private fun reprStack(): SmallSet<RawPointer> = REPR_STACK.getOr { SmallSet() }

private fun jsonStack(): SmallSet<RawPointer> = JSON_STACK.getOr { SmallSet() }
=======
// pub(crate) struct ReprCycle;
internal class ReprCycle

/** Returned when `to_json` is called recursively and a cycle is detected. */
// pub(crate) struct JsonCycle;
internal class JsonCycle

// thread_local! { static REPR_STACK: Cell<SmallSet<RawPointer>> }
// In Kotlin Multiplatform, Starlark evaluation is single-threaded per evaluator,
// so a simple mutable set suffices.
private val reprStack = SmallSet<RawPointer>()

// thread_local! { static JSON_STACK: Cell<SmallSet<RawPointer>> }
private val jsonStack = SmallSet<RawPointer>()
>>>>>>> origin/main:src/commonMain/kotlin/io/github/kotlinmania/starlark_kotlin/values/RecursiveReprOrJsonGuard.kt

/** Push a value to the stack, return error if it is already on the stack. */
// pub(crate) fn repr_stack_push(value: Value) -> Result<ReprStackGuard, ReprCycle>
internal fun reprStackPush(value: Value): Result<ReprStackGuard> {
    val stack = reprStack()
    return if (unlikely(!stack.insert(value.ptrValue()))) {
        Result.failure(ReprCycle())
    } else {
        Result.success(ReprStackGuard())
    }
}

/** Push a value to the stack, return error if it is already on the stack. */
// pub(crate) fn json_stack_push(value: Value) -> Result<JsonStackGuard, JsonCycle>
internal fun jsonStackPush(value: Value): Result<JsonStackGuard> {
    val stack = jsonStack()
    return if (unlikely(!stack.insert(value.ptrValue()))) {
        Result.failure(JsonCycle())
    } else {
        Result.success(JsonStackGuard())
    }
}
