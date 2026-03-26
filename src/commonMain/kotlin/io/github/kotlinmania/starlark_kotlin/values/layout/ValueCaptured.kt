// port-lint: source src/values/layout/value_captured.rs
package io.github.kotlinmania.starlark_kotlin.values.layout

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

/**
 * Special value which holds a reference to actual value.
 * This is used to implement variable capture by nested functions.
 *
 * [Value] holding [ValueCaptured] is equivalent to `Box<Option<Value>>`.
 */

import io.github.kotlinmania.starlark_kotlin.values.Freeze
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.Trace
import io.github.kotlinmania.starlark_kotlin.values.Tracer
import io.github.kotlinmania.starlark_kotlin.values.freeze_error.FreezeResult

// #[derive(Debug, Trace, ProvidesStaticType, Display, NoSerialize, Allocative)]
// #[display("{:?}", self)] // This type should never be user visible
// #[repr(transparent)]
// #[allocative(skip)]
// pub(crate) struct ValueCaptured<'v>(Cell<Option<Value<'v>>>);
internal class ValueCaptured private constructor(
    @Volatile private var payload: Value?,
) : StarlarkValue, Trace, Freeze<FrozenValueCaptured> {

    override val TYPE: String get() = "value_captured"

    // #[display("{:?}", self)]
    override fun toString(): String = "ValueCaptured($payload)"

    // impl Trace for ValueCaptured
    override fun trace(tracer: Tracer) {
        payload?.let { value ->
            payload = tracer.trace(value)
        }
    }

    // impl ValueCaptured
    companion object {
        // pub(crate) fn new(payload: Option<Value<'v>>) -> ValueCaptured<'v>
        internal fun new(payload: Value?): ValueCaptured {
            if (payload != null) {
                assert(payload.downcastRef<ValueCaptured>() == null)
                assert(payload.downcastRef<FrozenValueCaptured>() == null)
            }
            return ValueCaptured(payload)
        }
    }

    // impl ValueCaptured
    // pub(crate) fn set(&self, value: Value<'v>)
    internal fun set(value: Value) {
        assert(value.downcastRef<ValueCaptured>() == null)
        assert(value.downcastRef<FrozenValueCaptured>() == null)
        this.payload = value
    }

    // Cell::get equivalent
    internal fun get(): Value? {
        return payload
    }

    // impl Freeze for ValueCaptured
    // type Frozen = FrozenValueCaptured;
    // fn freeze(self, freezer: &Freezer) -> FreezeResult<FrozenValueCaptured>
    override fun freeze(freezer: Freezer): FreezeResult<FrozenValueCaptured> {
        val frozenPayload: FrozenValue? = if (payload != null) {
            val result = payload!!.freeze(freezer)
            if (result.isFailure) {
                @Suppress("UNCHECKED_CAST")
                return result as FreezeResult<FrozenValueCaptured>
            }
            result.getOrThrow()
        } else {
            null
        }
        return Result.success(FrozenValueCaptured(frozenPayload))
    }
}

// #[derive(Debug, ProvidesStaticType, Display, NoSerialize, Allocative)]
// #[display("{:?}", self)] // Type is not user visible
// #[repr(transparent)]
// pub(crate) struct FrozenValueCaptured(Option<FrozenValue>);
internal class FrozenValueCaptured(
    private val payload: FrozenValue?,
) : StarlarkValue {

    override val TYPE: String get() = "value_captured"

    // #[display("{:?}", self)]
    override fun toString(): String = "FrozenValueCaptured($payload)"

    internal fun get(): FrozenValue? {
        return payload
    }
}

// #[starlark_value(type = "value_captured")]
// impl<'v> StarlarkValue<'v> for ValueCaptured<'v> {}

// #[starlark_value(type = "value_captured")]
// impl<'v> StarlarkValue<'v> for FrozenValueCaptured {
//     type Canonical = ValueCaptured<'v>;
// }

// pub(crate) fn value_captured_get<'v>(value_captured: Value<'v>) -> Option<Value<'v>>
internal fun valueCapturedGet(valueCaptured: Value): Value? {
    val frozen = valueCaptured.unpackFrozen()
    if (frozen != null) {
        val frozenCaptured = frozen.downcastRef<FrozenValueCaptured>()
            ?: error("not a ValueCaptured")
        return frozenCaptured.get()?.toValue()
    } else {
        val captured = valueCaptured.downcastRef<ValueCaptured>()
            ?: error("not a ValueCaptured")
        return captured.get()
    }
}
