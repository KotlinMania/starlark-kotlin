// port-lint: source src/values/value_of.rs
package io.github.kotlinmania.starlark_kotlin.values.value_of

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

import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.values.AllocValue
import io.github.kotlinmania.starlark_kotlin.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark_kotlin.values.ValueOfUncheckedGeneric
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.Value

/**
 * A wrapper that keeps the original value on the heap for use elsewhere,
 * and also, when unpacked, unpacks the value to validate it is of
 * the correct type. Has an `UnpackValue` instance, so often used as
 * an argument to `@starlark_module` defined functions.
 */
class ValueOf<T>(
    /** The original [Value] on the same heap. */
    val value: Value,
    /** The value that was unpacked. */
    val typed: T,
) : StarlarkTypeRepr, AllocValue {

    /** Convert to `ValueOfUnchecked`. */
    @Suppress("UNCHECKED_CAST")
    fun asUnchecked(): ValueOfUncheckedGeneric<Value, StarlarkTypeRepr> {
        return ValueOfUncheckedGeneric.new(value)
    }

    override fun starlarkTypeRepr(): Ty {
        val t = typed
        return if (t is StarlarkTypeRepr) {
            t.starlarkTypeRepr()
        } else {
            Ty.any()
        }
    }

    override fun allocValue(heap: Heap): Value = value

    override fun toString(): String = typed.toString()

    companion object {
        @Suppress("UNCHECKED_CAST")
        @PublishedApi
        internal inline fun <reified T : Any> unpackValueImpl(value: Value): ValueOf<T>? {
            val typed: T = when (T::class) {
                Int::class -> value.unpackI32() as? T ?: return null
                Boolean::class -> value.unpackBool() as? T ?: return null
                String::class -> value.unpackStr() as? T ?: return null
                else -> {
                    val underlying: Any = value.getUnderlyingPtr()
                    underlying as? T ?: return null
                }
            }
            return ValueOf(value, typed)
        }
    }
}
