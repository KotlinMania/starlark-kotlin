// port-lint: source src/values/layout/value_captured.rs
package io.github.kotlinmania.starlark_kotlin.values.layout.value_captured

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

import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.types.tuple.unpackFrozen
import io.github.kotlinmania.starlark_kotlin.any.downcastRef

/// Special value which holds a reference to actual value.
/// This is used to implement variable capture by nested functions.
///
/// `Value` holding `ValueCaptured` is equivalent to `Box<Option<Value>>`.

// #[derive(Debug, Trace, ProvidesStaticType, Display, NoSerialize, Allocative)]
// pub(crate) struct ValueCaptured<'v>(Cell<Option<Value<'v>>>);
// Kotlin: single class, no lifetime. Mutable var for Cell semantics.
internal class ValueCaptured(
    private var payload: Value?,
) : StarlarkValue {

    companion object {
        // pub(crate) fn new(payload: Option<Value<'v>>) -> ValueCaptured<'v>
        fun new(payload: Value?): ValueCaptured {
            return ValueCaptured(payload)
        }
    }

    // pub(crate) fn set(&self, value: Value<'v>)
    fun set(value: Value) {
        payload = value
    }

    // Cell::get equivalent
    fun get(): Value? {
        return payload
    }

    // #[starlark_value(type = "value_captured")]
    // impl StarlarkValue for ValueCaptured

    // #[display("{:?}", self)]
    override fun toString(): String {
        return "ValueCaptured($payload)"
    }
}

// #[derive(Debug, ProvidesStaticType, Display, NoSerialize, Allocative)]
// pub(crate) struct FrozenValueCaptured(Option<FrozenValue>);
internal class FrozenValueCaptured(
    internal val inner: FrozenValue?,
) : StarlarkValue {

    // #[starlark_value(type = "value_captured")]
    // impl StarlarkValue for FrozenValueCaptured

    // #[display("{:?}", self)]
    override fun toString(): String {
        return "FrozenValueCaptured($inner)"
    }
}

// pub(crate) fn value_captured_get<'v>(value_captured: Value<'v>) -> Option<Value<'v>>
internal fun valueCapturedGet(valueCaptured: Value): Value? {
    val frozen = valueCaptured.unpackFrozen()
    if (frozen != null) {
        val frozenCaptured = frozen.downcastRef<FrozenValueCaptured>()
            ?: error("not a ValueCaptured")
        return frozenCaptured.inner?.toValue()
    } else {
        val captured = valueCaptured.downcastRef<ValueCaptured>()
            ?: error("not a ValueCaptured")
        return captured.get()
    }
}
