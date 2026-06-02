
// port-lint: source src/values/layout/value_captured.rs
package io.github.kotlinmania.starlark.values.layout

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

import io.github.kotlinmania.starlark.values.ComplexValue
import io.github.kotlinmania.starlark.values.Freeze
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.Trace
import io.github.kotlinmania.starlark.values.layout.heap.Tracer
import io.github.kotlinmania.starlark.values.layout.heap.ValueHolder
import kotlin.concurrent.Volatile

internal class ValueCaptured private constructor(
    @Volatile private var payload: Value?,
) : ComplexValue,
    Trace,
    Freeze<FrozenValueCaptured> {
    override val TYPE: String get() = "value_captured"

    override fun toString(): String = "ValueCaptured($payload)"

    override fun trace(tracer: Tracer) {
        payload?.let { value ->
            val holder = ValueHolder(value)
            tracer.trace(holder)
            payload = holder.value
        }
    }

    companion object {
        internal fun new(payload: Value?): ValueCaptured {
            if (payload != null) {
                check(payload.downcastRef<ValueCaptured>() == null)
                check(payload.downcastRef<FrozenValueCaptured>() == null)
            }
            return ValueCaptured(payload)
        }
    }

    internal fun set(value: Value) {
        check(value.downcastRef<ValueCaptured>() == null)
        check(value.downcastRef<FrozenValueCaptured>() == null)
        this.payload = value
    }

    internal fun get(): Value? = payload

    override fun freeze(freezer: Freezer): Result<FrozenValueCaptured> {
        val frozenPayload: FrozenValue? =
            if (payload != null) {
                val result = payload!!.freeze(freezer)
                if (result.isFailure) {
                    @Suppress("UNCHECKED_CAST")
                    return result as Result<FrozenValueCaptured>
                }
                result.getOrThrow()
            } else {
                null
            }
        return Result.success(FrozenValueCaptured(frozenPayload))
    }
}

internal class FrozenValueCaptured(
    private val payload: FrozenValue?,
) : StarlarkValue {
    override val TYPE: String get() = "value_captured"

    override fun toString(): String = "FrozenValueCaptured($payload)"

    internal fun get(): FrozenValue? = payload
}


internal fun valueCapturedGet(valueCaptured: Value): Value? {
    val frozen = valueCaptured.unpackFrozen()
    if (frozen != null) {
        val frozenCaptured =
            frozen.downcastRef<FrozenValueCaptured>()
                ?: error("not a ValueCaptured")
        return frozenCaptured.get()?.toValue()
    } else {
        val captured =
            valueCaptured.downcastRef<ValueCaptured>()
                ?: error("not a ValueCaptured")
        return captured.get()
    }
}
