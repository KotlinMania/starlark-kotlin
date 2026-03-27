// port-lint: source src/values/types/tuple/refs.rs
package io.github.kotlinmania.starlark_kotlin.values.types.tuple

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

// Placeholder types referenced from other modules
// These will be replaced with real imports as the port progresses
// TODO: stub - Value needs real import
class Value {
    fun unpackFrozen(): FrozenValue? = null
    companion object
}
// TODO: stub - FrozenValue needs real import
class FrozenValue {
    fun toValue(): Value = Value()
    fun <T : Any> downcastRef(): T? = null
    companion object
}
class Tuple {
    fun content(): List<Value> = emptyList()
    companion object {
        fun fromValue(value: Value): Tuple? = null
    }
}
class FrozenTuple {
    fun content(): List<FrozenValue> = emptyList()
    companion object {
        const val TYPE: String = "tuple"
    }
}
// TODO: stub - Ty needs real import
class Ty {
    companion object {
        fun anyTuple(): Ty = Ty()
    }
}

/// Reference to tuple data in Starlark heap.
class TupleRef(
    private val contents: List<Value>,
) {
    /// Number of elements.
    fun len(): Int = contents.size

    /// Tuple elements.
    fun content(): List<Value> = contents

    /// Iterate over the contents.
    fun iter(): Iterator<Value> = contents.iterator()

    companion object {
        /// `type(())`, which is `"tuple"`.
        const val TYPE: String = FrozenTuple.TYPE

        private fun new(slice: List<Value>): TupleRef = TupleRef(slice)

        /// Downcast a value to a tuple.
        fun fromValue(value: Value): TupleRef? {
            val tuple = Tuple.fromValue(value) ?: return null
            return new(tuple.content())
        }

        /// Downcast a value to a tuple.
        fun fromFrozenValue(value: FrozenValue): TupleRef? {
            return fromValue(value.toValue())
        }

        fun starlarkTypeRepr(): Ty = Ty.anyTuple()
    }
}

/// Reference to tuple data in frozen Starlark heap.
class FrozenTupleRef(
    private val contents: List<FrozenValue>,
) {
    /// Number of elements.
    fun len(): Int = contents.size

    /// Tuple elements.
    fun content(): List<FrozenValue> = contents

    /// Iterate over contents.
    fun iter(): Iterator<FrozenValue> = contents.iterator()

    companion object {
        /// `type(())`, which is `"tuple"`.
        const val TYPE: String = FrozenTuple.TYPE

        private fun new(slice: List<FrozenValue>): FrozenTupleRef = FrozenTupleRef(slice)

        /// Downcast a value to a tuple.
        fun fromFrozenValue(value: FrozenValue): FrozenTupleRef? {
            val tuple = value.downcastRef<FrozenTuple>() ?: return null
            return new(tuple.content())
        }

        fun starlarkTypeRepr(): Ty = Ty.anyTuple()
    }
}

/// Unpack a TupleRef from a Value.
fun unpackTupleRef(value: Value): TupleRef? {
    return TupleRef.fromValue(value)
}

/// Unpack a FrozenTupleRef from a Value.
fun unpackFrozenTupleRef(value: Value): FrozenTupleRef? {
    val frozen = value.unpackFrozen() ?: return null
    return FrozenTupleRef.fromFrozenValue(frozen)
}
