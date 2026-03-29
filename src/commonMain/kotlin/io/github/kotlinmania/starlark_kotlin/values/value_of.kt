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
 * the correct type. Has an [UnpackValue] instance, so often used as
 * an argument to `#[starlark_module]` defined functions.
 */
class ValueOf<T>(
    /** The original [Value] on the same heap. */
    val value: Value,
    /** The value that was unpacked. */
    val typed: T,
) : StarlarkTypeRepr, AllocValue {

    // impl ValueOf

    /** Convert to [ValueOfUnchecked]. */
    // pub fn as_unchecked(&self) -> ValueOfUnchecked<'v, T>
    @Suppress("UNCHECKED_CAST")
    fun asUnchecked(): ValueOfUncheckedGeneric<Value, StarlarkTypeRepr> {
        // In Rust, T: UnpackValue which implies StarlarkTypeRepr,
        // so ValueOfUnchecked<T> is always valid. Kotlin lacks the
        // trait bound on T, so we return ValueOfUnchecked<StarlarkTypeRepr>
        // as the erased equivalent.
        return ValueOfUncheckedGeneric.new(value)
    }

    // impl Deref for ValueOf
    // fn deref(&self) -> &Value
    // Kotlin: direct access via .value field.

    // impl StarlarkTypeRepr for ValueOf
    // fn starlark_type_repr() -> Ty
    // In Rust: T::starlark_type_repr() where T: UnpackValue (extends StarlarkTypeRepr).
    override fun starlarkTypeRepr(): Ty {
        val t = typed
        return if (t is StarlarkTypeRepr) {
            t.starlarkTypeRepr()
        } else {
            // Fallback: T does not implement StarlarkTypeRepr in Kotlin.
            // In Rust this is unreachable due to the trait bound.
            Ty.any()
        }
    }

    // impl AllocValue for ValueOf
    // fn alloc_value(self, _heap: Heap<'v>) -> Value<'v>
    override fun allocValue(heap: Heap): Value = value

    // impl Display for ValueOf
    override fun toString(): String = typed.toString()

    companion object {
        // impl UnpackValue for ValueOf
        // fn unpack_value_impl(value: Value<'v>) -> Result<Option<Self>, Self::Error>
        //
        // Rust: let Some(typed) = T::unpack_value_impl(value)? ...
        // Kotlin: With reified T, attempt to extract T from the Value's
        // underlying StarlarkValue via runtime type check. For primitive
        // Kotlin types (Int, String, Boolean) that map to Starlark types,
        // use the Value's dedicated unpack methods.
        @Suppress("UNCHECKED_CAST")
        @PublishedApi
        internal inline fun <reified T : Any> unpackValueImpl(value: Value): ValueOf<T>? {
            val typed: T = when (T::class) {
                Int::class -> value.unpackI32() as? T ?: return null
                Boolean::class -> value.unpackBool() as? T ?: return null
                String::class -> value.unpackStr() as? T ?: return null
                else -> {
                    // For StarlarkValue subtypes and other types, extract
                    // the underlying object from the Value and attempt a cast.
                    // In Rust, T::unpack_value_impl(value) dispatches statically
                    // via the UnpackValue trait; in Kotlin we rely on reified T
                    // and runtime type checking.
                    val underlying: Any = value.getUnderlyingPtr()
                    underlying as? T ?: return null
                }
            }
            return ValueOf(value, typed)
        }
    }
}
