// port-lint: source values/recursive_repr_or_json_guard.rs
package io.github.kotlinmania.starlark.values

/*
 * Copyright 2018 The Starlark in Rust Authors.
 * Copyright (c) Facebook, Inc. and its affiliates.
 * Copyright (c) 2025 Sydney Renee, The Solace Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not import this file except in compliance with the License.
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

/** Detect recursion when doing `repr` or `toJson`. */

import io.github.kotlinmania.starlarkmap.smallset.SmallSet
import io.github.kotlinmania.starlark.values.layout.RawPointer
import io.github.kotlinmania.starlark.values.layout.Value

/** Pop the stack on drop. */
internal class ReprStackGuard : AutoCloseable {
    override fun close() {
        val popped = reprStack.pop()
        check(popped != null)
    }
}

/** Pop the stack on drop. */
internal class JsonStackGuard : AutoCloseable {
    override fun close() {
        val popped = jsonStack.pop()
        check(popped != null)
    }
}

/** Returned when `repr` is called recursively and a cycle is detected. */
internal class ReprCycle

/** Returned when `toJson` is called recursively and a cycle is detected. */
internal class JsonCycle

private val reprStack = SmallSet<RawPointer>()

private val jsonStack = SmallSet<RawPointer>()

/** Push a value to the stack, return error if it is already on the stack. */
internal fun reprStackPush(value: Value): Result<ReprStackGuard> {
    if (!reprStack.insert(value.ptrValue())) {
        return Result.failure(Exception(ReprCycle().toString()))
    }
    return Result.success(ReprStackGuard())
}

/** Push a value to the stack, return error if it is already on the stack. */
internal fun jsonStackPush(value: Value): Result<JsonStackGuard> {
    if (!jsonStack.insert(value.ptrValue())) {
        return Result.failure(Exception(JsonCycle().toString()))
    }
    return Result.success(JsonStackGuard())
}
