// port-lint: source eval/compiler/defInline/local_as_value.rs
package io.github.kotlinmania.starlark.eval.compiler.definline.localasvalue

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

/**
 * Local slot id as value.
 *
 * To be able to propagate the local slot number through parameter binding machinery.
 */

import io.github.kotlinmania.starlark.eval.runtime.LocalSlotId
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.layout.FrozenValueTyped
import io.github.kotlinmania.starlark.values.layout.avalues.simple.allocSimpleTyped

/**
 * Local slot id as `FrozenValue`. This object is only used during compilation
 * and never appears in the executed program.
 */
internal class LocalAsValue(
    val local: LocalSlotId,
) : StarlarkValue {

    override val TYPE: String get() = "LocalAsValue"

    override fun toString(): String = "LocalAsValue(local=$local)"
}

/**
 * Create a value which represents a reference to local slot id during optimization.
 *
 * Pre-allocates up to 100 slots (practically enough for any function).
 */
internal fun localAsValue(local: LocalSlotId): FrozenValueTyped<LocalAsValue>? {
    // 100 is practically enough.
    return LOCALS.getOrNull(local.index.toInt())
}

private val LOCALS: List<FrozenValueTyped<LocalAsValue>> by lazy {
    val heap = FrozenHeap()
    List(100) { i ->
        heap.allocSimpleTyped(LocalAsValue(LocalSlotId(i.toUInt())))
    }
}
