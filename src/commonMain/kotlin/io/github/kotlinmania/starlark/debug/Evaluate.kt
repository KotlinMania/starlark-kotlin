// port-lint: source src/debug/evaluate.rs
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
import io.github.kotlinmania.starlark.eval.evalModule
import io.github.kotlinmania.starlark.eval.runtime.LocalSlotIdCapturedOrNot
import io.github.kotlinmania.starlark.syntax.AstModule
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.typed.FrozenStringValue
import io.github.kotlinmania.starlark.eval.runtime.Evaluator
import kotlin.text.get
import kotlin.text.iterator

/**
 * Evaluate statements in the existing context. This function is designed for debugging,
 * not production use.
 *
 * There are lots of health warnings on this code. Might not work with frozen modules,
 * unassigned variables, nested definitions etc. It would be a bad idea to rely on the
 * results of continued execution after evaluating stuff randomly.
 */
fun Evaluator.evalStatements(statements: AstModule): Result<Value> {
    // We are doing a lot of funky stuff here. It's amazing anything works, so let's not push our luck with GC.
    disableGc()

    // Everything must be evaluated with the current heap (or we'll lose memory), which means
    // the current module (eval.module_env).
    // We also want access to the module variables (fine), the locals (need to move them over),
    // and the frozen variables (move them over).
    // Afterwards, we want to put everything back - locals can move back to locals, modules
    // can stay where they are, but frozen values are discarded.

    // We want all the local variables to be available to the module, so we capture
    // everything before, shove the local variables into the module, and then revert after
    val originalModule: SmallMap<FrozenStringValue, Value?> = SmallMap.new<FrozenStringValue, Value?>().also { map ->
        for ((name, slot) in moduleEnv.mutableNames().allNamesAndSlots()) {
            map.insert(name, moduleEnv.slots().getSlot(slot))
        }
    }

    // Push all the frozen variables into the module
    val frozen = topFrameDefFrozenModule(true)
    if (frozen != null) {
        for ((name, slot) in frozen.value.names.symbols()) {
            val value = frozen.value.getSlot(slot)
            if (value != null) {
                moduleEnv.set(name.asStr(), value.toValue())
            }
        }
    }

    // Push all local variables into the module
    val locals = callStack.toFunctionValues()
        .reversed()
        .firstNotNullOfOrNull { toScopeNamesByLocalSlotId(it) }
    if (locals != null) {
        for ((slot, name) in locals.withIndex()) {
            val value = currentFrame.getSlotSlow(LocalSlotIdCapturedOrNot(slot.toUInt()))
            if (value != null) {
                moduleEnv.set(name.asStr(), value)
            }
        }
    }

    val globals = topFrameDefInfoForDebugger().globals
    val res = evalModule(statements, globals.asRef())

    // Now put the Module back how it was before we started, as best we can
    // and move things into locals if that makes sense
    if (locals != null) {
        for ((slot, name) in locals.withIndex()) {
            val value = moduleEnv.get(name.asStr())
            if (value != null) {
                currentFrame.setSlotSlow(LocalSlotIdCapturedOrNot(slot.toUInt()), value)
            }
        }
        for ((name, slot) in moduleEnv.mutableNames().allNamesAndSlots()) {
            val nameStr = name.asStr()
            val originalIdx = originalModule.getIndexOf(name)
            when {
                originalIdx == null -> {
                    moduleEnv.mutableNames().hideName(nameStr)
                }
                else -> {
                    val original = originalModule.get(name)
                    if (original != null) {
                        moduleEnv.slots().setSlot(slot, original)
                    }
                    // else: No way to unassign a previously assigned value yet
                }
            }
        }
    }

    return res
}
