// port-lint: source src/debug/inspect.rs
package io.github.kotlinmania.starlark_kotlin.debug

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

import io.github.kotlinmania.starlark_kotlin.assert.Assert
import io.github.kotlinmania.starlark_kotlin.collections.SmallMap
import io.github.kotlinmania.starlark_kotlin.environment.GlobalsBuilder
import io.github.kotlinmania.starlark_kotlin.eval.compiler.def.Def
import io.github.kotlinmania.starlark_kotlin.eval.compiler.def.FrozenDef
import io.github.kotlinmania.starlark_kotlin.values.layout.typed.FrozenStringValue
import io.github.kotlinmania.starlark_kotlin.values.types.string.ValueLike
import io.github.kotlinmania.starlark_kotlin.values.types.string.Evaluator
import io.github.kotlinmania.starlark_kotlin.eval.runtime.LocalSlotIdCapturedOrNot
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.typed.FrozenStringValue
import io.github.kotlinmania.starlark_kotlin.values.types.string.moduleEnv
import io.github.kotlinmania.starlark_kotlin.values.types.string.allocStr
import io.github.kotlinmania.starlark_kotlin.util.asStr
import io.github.kotlinmania.starlark_kotlin.values.types.tuple.unpackFrozen
import io.github.kotlinmania.starlark_kotlin.values.types.tuple.it
import io.github.kotlinmania.starlark_kotlin.eval.runtime.currentFrame
import io.github.kotlinmania.starlark_kotlin.eval.runtime.callStack
import io.github.kotlinmania.starlark_kotlin.coerce
import io.github.kotlinmania.starlark_kotlin.any.downcastRef
import io.github.kotlinmania.starlark_kotlin.values.types.string.Dict
import io.github.kotlinmania.starlark_kotlin.stdlib.new

internal fun toScopeNamesByLocalSlotId(x: Value): List<FrozenStringValue>? {
    if (x.unpackFrozen() != null) {
        return x.downcastRef<FrozenDef>()?.defInfo?.used
    } else {
        return x.downcastRef<Def>()?.defInfo?.used
    }
}

/**
 * Obtain the local variables currently in scope. When at top-level these will be
 * [Module][io.github.kotlinmania.starlark_kotlin.environment.Module] variables, otherwise local
 * definitions. The precise number of variables may change over time due to optimisation. The only
 * legitimate use of this function is for debugging.
 */
fun Evaluator.localVariables(): SmallMap<String, Value> {
    return inspectLocalVariables(this) ?: inspectModuleVariables(this)
}

private fun inspectLocalVariables(eval: Evaluator): SmallMap<String, Value>? {
    // First we find the first entry on the call_stack which contains a Def (and thus has locals)
    val xs = eval.callStack.toFunctionValues()
    val names = xs.reversed().firstNotNullOfOrNull { toScopeNamesByLocalSlotId(it) }
        ?: return null
    val res = SmallMap<String, Value>()
    for ((slot, name) in names.withIndex()) {
        // correctly handle captured.
        val v = eval.currentFrame.getSlotSlow(LocalSlotIdCapturedOrNot(slot.toUInt()))
        if (v != null) {
            res.insert(name.asStr(), v)
        }
    }
    return res
}

private fun inspectModuleVariables(eval: Evaluator): SmallMap<String, Value> {
    val res = SmallMap<String, Value>()
    for ((name, slot) in eval.moduleEnv.mutableNames().allNamesAndSlots()) {
        val v = eval.moduleEnv.slots().getSlot(slot)
        if (v != null) {
            res.insert(name.asStr(), v)
        }
    }
    return res
}

// Tests

private fun debuggerFunctions(builder: GlobalsBuilder) {
    builder.setFunction("debug_inspect_stack") { eval: Evaluator ->
        Result.success(eval.callStack().intoFrames().map { it.toString() })
    }

    builder.setFunction("debug_inspect_variables") { eval: Evaluator ->
        val sm = SmallMap<Any, Value>()
        for ((k, v) in eval.localVariables()) {
            sm.insertHashed(eval.heap().allocStr(k).getHashed(), v)
        }
        Result.success(Dict.new(coerce(sm)))
    }
}

internal fun testDebugStack() {
    val a = Assert()
    a.globalsAdd(::debuggerFunctions)
    a.pass(
        """
def assert_stack(want):
    stack = debug_inspect_stack()
    assert_eq([x.split(' ')[0] for x in stack[:-2]], want)

assert_stack([])

def f(): assert_stack(["g", "f"])
def g(): f()
g()
""",
    )
}

internal fun testDebugVariables() {
    val a = Assert()
    a.globalsAdd(::debuggerFunctions)
    a.pass(
        """
root = 12
_ignore = [x for x in [True]]
def f(x = 1, y = "test"):
    z = x + 5
    for _magic in [False, True]:
        continue
    assert_eq(debug_inspect_variables(), {"x": 1, "y": "hello", "z": 6, "_magic": True})
f(y = "hello")
assert_eq(debug_inspect_variables(), {"root": 12, "f": f, "_ignore": [True]})
""",
    )
}
