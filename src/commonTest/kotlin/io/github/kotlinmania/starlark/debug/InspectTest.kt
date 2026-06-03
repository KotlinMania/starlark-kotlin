// port-lint: tests src/debug/inspect.rs
package io.github.kotlinmania.starlark.debug

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

import io.github.kotlinmania.starlark.assert.Assert
import io.github.kotlinmania.starlark.collections.Hashed
import io.github.kotlinmania.starlark.collections.SmallMap
import io.github.kotlinmania.starlark.environment.GlobalsBuilder
import io.github.kotlinmania.starlark.eval.runtime.Arguments
import io.github.kotlinmania.starlark.eval.runtime.Evaluator
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.typed.StringValue
import io.github.kotlinmania.starlark.values.types.dict.Dict
import kotlin.test.Test

private fun debuggerFunctions(builder: GlobalsBuilder) {
    builder.setFunction("debug_inspect_stack") { _: Arguments, eval: Evaluator ->
        Result.success(eval.callStack().intoFrames().map { it.toString() })
    }

    builder.setFunction("debug_inspect_variables") { _: Arguments, eval: Evaluator ->
        val sm = SmallMap.new<Value, Value>()
        for ((k, v) in eval.localVariables()) {
            val sv = StringValue.newUnchecked(eval.heap().allocStr(k))
            val hashedValue = Hashed.newUnchecked(sv.getHash(), sv.toValue())
            sm.insertHashed(hashedValue, v)
        }
        Result.success(Dict.new(sm))
    }
}

internal class InspectTest {
    @Test
    fun testDebugStack() {
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

    @Test
    fun testDebugVariables() {
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
}
