// port-lint: source src/values/recursive_repr_or_json_guard.rs
package io.github.kotlinmania.starlark_kotlin.values

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

//! Detect recursion when doing `repr` or `to_json`.

import io.github.kotlinmania.starlark_kotlin.collections.SmallSet
import io.github.kotlinmania.starlark_kotlin.values.layout.RawPointer

/// Pop the stack on drop.
// pub(crate) struct ReprStackGuard;
internal class ReprStackGuard : AutoCloseable {
    // impl Drop for ReprStackGuard
    override fun close() {
        val popped = reprStack.pop()
        check(popped != null)
    }
}

/// Pop the stack on drop.
// pub(crate) struct JsonStackGuard;
internal class JsonStackGuard : AutoCloseable {
    // impl Drop for JsonStackGuard
    override fun close() {
        val popped = jsonStack.pop()
        check(popped != null)
    }
}

/// Returned when `repr` is called recursively and a cycle is detected.
// pub(crate) struct ReprCycle;
internal class ReprCycle

/// Returned when `to_json` is called recursively and a cycle is detected.
// pub(crate) struct JsonCycle;
internal class JsonCycle

// thread_local! { static REPR_STACK: Cell<SmallSet<RawPointer>> }
// In Kotlin Multiplatform, Starlark evaluation is single-threaded per evaluator,
// so a simple mutable set suffices.
private val reprStack = SmallSet<RawPointer>()

// thread_local! { static JSON_STACK: Cell<SmallSet<RawPointer>> }
private val jsonStack = SmallSet<RawPointer>()

/// Push a value to the stack, return error if it is already on the stack.
// pub(crate) fn repr_stack_push(value: Value) -> Result<ReprStackGuard, ReprCycle>
internal fun reprStackPush(value: Value): Result<ReprStackGuard> {
    if (!reprStack.insert(value.ptrValue())) {
        return Result.failure(Exception(ReprCycle().toString()))
    }
    return Result.success(ReprStackGuard())
}

/// Push a value to the stack, return error if it is already on the stack.
// pub(crate) fn json_stack_push(value: Value) -> Result<JsonStackGuard, JsonCycle>
internal fun jsonStackPush(value: Value): Result<JsonStackGuard> {
    if (!jsonStack.insert(value.ptrValue())) {
        return Result.failure(Exception(JsonCycle().toString()))
    }
    return Result.success(JsonStackGuard())
}
