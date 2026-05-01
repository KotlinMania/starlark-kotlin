// port-lint: source environment/module_dump.rs
package io.github.kotlinmania.starlark.environment

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

import io.github.kotlinmania.starlark.eval.compiler.DefGen
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeapRef
import io.github.kotlinmania.starlark.values.layout.FrozenValueTyped

private class Write {
    var w: String = ""
    val writeln: (String) -> Unit = { line -> w += (line + "\n") }
    val pushStr: (String) -> Unit = { s -> w += s }
}

/** Print a lot of module internals for debugging. */
fun FrozenModule.dumpDebug(): String {
    val w = Write()

    val evalDurationAsSecsF64Rounded3 = (((evalDuration.inWholeNanoseconds / 1_000_000_000.0) * 1000).toLong() / 1000.0)
    w.writeln("Eval duration: ${evalDurationAsSecsF64Rounded3}s")
    w.writeln("Heap stats:")
    w.pushStr(frozenHeap().dumpDebug())

    for ((name, value) in allItems()) {
        // Note (nga): this prints public, private and imported symbols.
        //   We only care about public and private symbols, but no imported.
        w.writeln("")
        w.writeln("$name = $value")
        val def = FrozenValueTyped.new<DefGen<FrozenValue>>(value)
        if (def != null) {
            def.asRef().dumpDebug().lines().forEach { line -> w.writeln("  $line") }
        }
    }
    return w.w
}

internal fun FrozenHeapRef.dumpDebug(): String {
    val w = Write()
    w.writeln("Allocated bytes: ${allocatedBytes()}")
    w.writeln("Available bytes: ${availableBytes()}")
    return w.w
}
