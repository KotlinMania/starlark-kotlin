// port-lint: source values/layout/heap/call_enter_exit.rs
package io.github.kotlinmania.starlark.values.layout.heap

/*
 * Copyright 2019 The Starlark in Rust Authors.
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

// Marker objects to track allocations.

import io.github.kotlinmania.starlark.eval.runtime.profile.ProfilerInstant
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.Trace
import io.github.kotlinmania.starlark.values.layout.heap.Tracer
import io.github.kotlinmania.starlark.values.layout.Value

/** A type which is either drop or non-drop. */
internal interface MaybeDrop

/** Type which has `Drop`. */
internal class NeedsDrop : MaybeDrop, AutoCloseable {
    //     // Just make this type `Drop`.
    //     // Note `mem::needsDrop()` would return `true` for this type,
    //     // even if `drop` is optimized away.
    override fun close() {
        // Just make this type have a finalizer.
    }
}

/** Type which doesn't have `Drop`. */
internal class NoDrop : MaybeDrop

internal class CallEnter<D : MaybeDrop>(
    var function: Value,
    val time: ProfilerInstant,
    val maybeDrop: D,
) : StarlarkValue, Trace {
    override val TYPE: String get() = "call_enter"

    override fun trace(tracer: Tracer) {
        val holder = ValueHolder(function)
        tracer.trace(holder)
        function = holder.value
    }

    override fun toString(): String = "CallEnter"
}

internal class CallExit<D : MaybeDrop>(
    val time: ProfilerInstant,
    val maybeDrop: D,
) : StarlarkValue {
    override val TYPE: String get() = "call_exit"

    override fun toString(): String = "CallExit"
}
