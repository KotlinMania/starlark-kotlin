// port-lint: source src/values/types/unbound.rs
package io.github.kotlinmania.starlark.values.types

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

/** Handle special "unbound" globals: methods or attributes. */

import io.github.kotlinmania.starlark.eval.runtime.Arguments
import io.github.kotlinmania.starlark.eval.runtime.Evaluator
import io.github.kotlinmania.starlark.eval.runtime.FrameSpan
import io.github.kotlinmania.starlark.values.FrozenRef
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.FrozenValueTyped
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.avalues.allocComplex
import io.github.kotlinmania.starlark.values.layout.heap.Heap

/** A value or an unbound method or unbound attribute. */
internal sealed class UnboundValue {
    /** A method with `this` unbound. */
    class Method(
        val method: FrozenValueTyped<NativeMethod>,
    ) : UnboundValue()

    /** An attribute with `this` unbound. */
    class Attr(
        val attr: FrozenValueTyped<NativeAttribute>,
    ) : UnboundValue()

    override fun toString(): String = "MaybeUnboundValue(..)"

    fun toFrozenValue(): FrozenValue =
        when (this) {
            is Method -> method.toFrozenValue()
            is Attr -> attr.toFrozenValue()
        }

    /** Bind this object to given `this` value. */
    fun bind(thisValue: Value, heap: Heap): Result<Value> =
        when (this) {
            is Method -> Result.success(heap.allocComplex(BoundMethodGen(method, thisValue.toValue())))
            is Attr -> attr.asRef().invoke(thisValue, heap)
        }

    fun invokeMethod(
        thisValue: Value,
        span: FrozenRef<FrameSpan>,
        args: Arguments,
        eval: Evaluator,
    ): Result<Value> =
        eval.withCallStack(
            toFrozenValue().toValue(),
            span,
        ) { eval ->
            when (this) {
                is Method -> method.asRef().function.invoke(eval, thisValue, args)
                is Attr -> attr.asRef().invoke(thisValue, eval.heap())
            }
        }
}
