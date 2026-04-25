// port-lint: source src/environment/module_dump.rs
package io.github.kotlinmania.starlark_kotlin.environment

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

import io.github.kotlinmania.starlark_kotlin.eval.compiler.DefGen
import io.github.kotlinmania.starlark_kotlin.values.layout.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.FrozenHeapRef
import io.github.kotlinmania.starlark_kotlin.values.types.string.format
import io.github.kotlinmania.starlark_kotlin.values.layout.FrozenValueTyped

// impl FrozenModule

/** Print a lot of module internals for debugging. */
fun FrozenModule.dumpDebug(): String {
    return buildString {
        val secs = evalDuration.inWholeMilliseconds / 1000.0
        appendLine("Eval duration: ${((secs * 1000).toLong() / 1000.0)}s")
        appendLine("Heap stats:")
        append(frozenHeap().dumpDebug())

        for ((name, value) in allItems()) {
            appendLine()
            appendLine("$name = $value")
            val def = FrozenValueTyped.new<DefGen<FrozenValue>>(value)
            if (def != null) {
                def.asRef().dumpDebug()
                    .lines()
                    .forEach { line -> appendLine("  $line") }
            }
        }
    }
}

// impl FrozenHeapRef

private fun FrozenHeapRef.dumpDebug(): String {
    return buildString {
        appendLine("Allocated bytes: ${allocatedBytes()}")
        appendLine("Available bytes: ${availableBytes()}")
    }
}
