// port-lint: source src/values/types/unbound.rs
package io.github.kotlinmania.starlark_kotlin.values.types

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

import io.github.kotlinmania.starlark_kotlin.values.FrozenRef
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.eval.runtime.Evaluator
import io.github.kotlinmania.starlark_kotlin.eval.runtime.Arguments
import io.github.kotlinmania.starlark_kotlin.eval.runtime.FrameSpan
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.toValue
import io.github.kotlinmania.starlark_kotlin.values.layout.FrozenValueTyped
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.allocComplex

/** A value or an unbound method or unbound attribute. */
// #[derive(Clone)]
// pub(crate) enum UnboundValue
internal sealed class UnboundValue {
    /** A method with `this` unbound. */
    // Method(FrozenValueTyped<'static, NativeMethod>)
    class Method(val method: FrozenValueTyped<NativeMethod>) : UnboundValue()

    /** An attribute with `this` unbound. */
    // Attr(FrozenValueTyped<'static, NativeAttribute>)
    class Attr(val attr: FrozenValueTyped<NativeAttribute>) : UnboundValue()

    // impl Debug for UnboundValue
    override fun toString(): String = "MaybeUnboundValue(..)"

    // pub(crate) fn to_frozen_value(&self) -> FrozenValue
    fun toFrozenValue(): FrozenValue {
        return when (this) {
            is Method -> method.toFrozenValue()
            is Attr -> attr.toFrozenValue()
        }
    }

    /** Bind this object to given `this` value. */
    // pub(crate) fn bind<'v>(&self, this: Value<'v>, heap: Heap<'v>) -> crate::Result<Value<'v>>
    fun bind(thisValue: Value, heap: Heap): Result<Value> {
        return when (this) {
            is Method -> Result.success(heap.allocComplex(BoundMethodGen(method, thisValue.toValue())))
            is Attr -> attr.asRef().invoke(thisValue, heap)
        }
    }

    // pub(crate) fn invoke_method<'v>(...)
    fun invokeMethod(
        thisValue: Value,
        span: FrozenRef<FrameSpan>,
        args: Arguments,
        eval: Evaluator,
    ): Result<Value> {
        return eval.withCallStack(
            toFrozenValue().toValue(),
            span,
        ) { eval ->
            when (this) {
                is Method -> method.asRef().function.invoke(eval, thisValue, args)
                is Attr -> attr.asRef().invoke(thisValue, eval.heap())
            }
        }
    }
}
