// port-lint: source src/stdlib/call_stack.rs
package io.github.kotlinmania.starlark_kotlin.stdlib.call_stack

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

//! Implementation of `call_stack` function.

import io.github.kotlinmania.starlark_kotlin.environment.GlobalsBuilder
import io.github.kotlinmania.starlark_kotlin.environment.Methods
import io.github.kotlinmania.starlark_kotlin.environment.MethodsBuilder
import io.github.kotlinmania.starlark_kotlin.environment.MethodsStatic
import io.github.kotlinmania.starlark_kotlin.values.AllocValue
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.types.string.Evaluator
import io.github.kotlinmania.starlark_kotlin.values.types.list.NoneOr
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.types.function
import io.github.kotlinmania.starlark_kotlin.eval.runtime.positional
import io.github.kotlinmania.starlark_kotlin.docs.name
import io.github.kotlinmania.starlark_kotlin.analysis.Other
import io.github.kotlinmania.starlark_kotlin.values.types.namespace.attribute
import io.github.kotlinmania.starlark_kotlin.values.owned.downcast
import io.github.kotlinmania.starlark_kotlin.eval.runtime.callStack
import io.github.kotlinmania.starlark_kotlin.analysis.unused_loads.file
import io.github.kotlinmania.starlark_kotlin.analysis.location
import io.github.kotlinmania.starlark_kotlin.codemap.FileSpan

// #[derive(ProvidesStaticType, Trace, Allocative, Debug, NoSerialize, Clone)]
/// A frame of the call-stack.
// struct StackFrame {
//     name: String,
//     location: Option<FileSpan>,
// }
internal data class StackFrame(
    /// The name of the entry on the call-stack.
    val name: String,
    /// The location of the definition, or null for native functions.
    val location: FileSpan?,
) : StarlarkValue, AllocValue {

    // #[starlark_value(type = "StackFrame", StarlarkTypeRepr, UnpackValue)]
    // impl StarlarkValue for StackFrame

    // fn get_methods() -> Option<&'static Methods>
    fun getMethods(): Methods? {
        return stackFrameMethodsStatic.methods(::stackFrameMethods)
    }

    // impl AllocValue for StackFrame
    // fn alloc_value(self, heap: Heap<'v>) -> Value<'v>
    override fun allocValue(heap: Heap): Value {
        return heap.allocComplexNoFreeze(this)
    }

    // impl Display for StackFrame
    override fun toString(): String = "<StackFrame ...>"

    companion object {
        const val TYPE: String = "StackFrame"
    }
}

// static RES: MethodsStatic = MethodsStatic::new();
private val stackFrameMethodsStatic = MethodsStatic()

// #[starlark_module]
// fn stack_frame_methods(builder: &mut MethodsBuilder)
private fun stackFrameMethods(builder: MethodsBuilder) {
    /// Returns the name of the entry on the call-stack.
    // #[starlark(attribute)]
    // fn func_name(this: &StackFrame) -> starlark::Result<String>
    builder.attribute("func_name") { thisValue, _ ->
        val this = thisValue.downcast<StackFrame>()!!
        this.name
    }

    /// Returns a path of the module from which the entry was called, or None for native functions.
    // #[starlark(attribute)]
    // fn module_path(this: &StackFrame) -> starlark::Result<NoneOr<String>>
    builder.attribute("module_path") { thisValue, _ ->
        val this = thisValue.downcast<StackFrame>()!!
        when (val loc = this.location) {
            null -> NoneOr.None
            else -> NoneOr.Other(loc.file.filename())
        }
    }
}

// #[starlark_module]
// pub(crate) fn global(builder: &mut GlobalsBuilder)
internal fun global(builder: GlobalsBuilder) {
    /// Get a textual representation of the call stack.
    ///
    /// strip_frames will pop N frames from the top of the call stack.
    // fn call_stack(#[starlark(require=named, default = 0)] strip_frames: u32, eval: &mut Evaluator) -> anyhow::Result<String>
    builder.function("call_stack") { args, eval ->
        val stripFrames = args.namedOptional<Int>("strip_frames") ?: 0
        val stack = eval.callStack()
        val frameCount = stack.frames.size
        val truncateTo = maxOf(0, frameCount - stripFrames)
        stack.frames.subList(truncateTo, frameCount).clear()
        stack.toString()
    }

    /// Get a structural representation of the n-th call stack frame.
    ///
    /// With `n=0` returns `call_stack_frame` itself.
    /// Returns `None` if `n` is greater than or equal to the stack size.
    // fn call_stack_frame(#[starlark(require = pos)] n: u32, eval: &mut Evaluator) -> anyhow::Result<NoneOr<StackFrame>>
    builder.function("call_stack_frame") { args, eval ->
        val n = args.positional<Int>(0)
        val stack = eval.callStack()
        if (n >= stack.frames.size) {
            return@function NoneOr.None
        }
        val frame = stack.frames.getOrNull(stack.frames.size - n - 1)
        when (frame) {
            null -> NoneOr.None
            else -> NoneOr.Other(
                StackFrame(
                    name = frame.name,
                    location = frame.location,
                )
            )
        }
    }
}

// #[cfg(test)] mod tests
// Tests are in commonTest, not here.
