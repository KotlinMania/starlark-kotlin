// port-lint: source src/debug/inspect.rs
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

import io.github.kotlinmania.starlark.collections.SmallMap
import io.github.kotlinmania.starlark.eval.compiler.Def
import io.github.kotlinmania.starlark.eval.compiler.FrozenDef
import io.github.kotlinmania.starlark.values.layout.typed.FrozenStringValue
import io.github.kotlinmania.starlark.eval.runtime.Evaluator
import io.github.kotlinmania.starlark.eval.runtime.LocalSlotIdCapturedOrNot
import io.github.kotlinmania.starlark.values.layout.Value

internal fun toScopeNamesByLocalSlotId(x: Value): List<FrozenStringValue>? {
    if (x.unpackFrozen() != null) {
        return x.downcastRef<FrozenDef>()?.defInfo?.used
    } else {
        return x.downcastRef<Def>()?.defInfo?.used
    }
}

/**
 * Obtain the local variables currently in scope. When at top-level these will be
 * [Module][io.github.kotlinmania.starlark.environment.Module] variables, otherwise local
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
    val res = SmallMap.new<String, Value>()
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
    val res = SmallMap.new<String, Value>()
    for ((name, slot) in eval.moduleEnv.mutableNames().allNamesAndSlots()) {
        val v = eval.moduleEnv.slots().getSlot(slot)
        if (v != null) {
            res.insert(name.asStr(), v)
        }
    }
    return res
}
