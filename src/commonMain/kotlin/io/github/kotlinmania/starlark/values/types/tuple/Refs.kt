// port-lint: source values/types/tuple/refs.rs
package io.github.kotlinmania.starlark.values.types.tuple

/*
 * Copyright 2018 The Starlark in Rust Authors.
 * Copyright (c) Facebook, Inc. and its affiliates.
 * Copyright (c) 2025 Sydney Renee, The Solace Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not import this file except in compliance with the License.
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

import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.Value

/** Reference to tuple data in Starlark heap. */
class TupleRef(
    private val contents: List<Value>,
) {

    /** `type(())`, which is `"tuple"`. */

    /** Number of elements. */
    fun len(): Int = contents.size

    /** Tuple elements. */
    fun content(): List<Value> = contents

    /** Iterate over the contents. */
    fun iter(): Iterator<Value> = contents.iterator()

    companion object {
        const val TYPE: String = TupleGen.TYPE

        private fun new(slice: List<Value>): TupleRef = TupleRef(slice)

        /** Downcast a value to a tuple. */
        fun fromValue(value: Value): TupleRef? {
            val tuple = TupleGen.fromValue(value) ?: return null
            return new(tuple.content())
        }

        /** Downcast a value to a tuple. */
        fun fromFrozenValue(value: FrozenValue): TupleRef? {
            return fromValue(value.toValue())
        }

        fun starlarkTypeRepr(): Ty = Ty.anyTuple()
    }
}

/** Reference to tuple data in frozen Starlark heap. */
//     contents: [FrozenValue],
class FrozenTupleRef(
    private val contents: List<FrozenValue>,
) {

    /** Number of elements. */
    fun len(): Int = contents.size

    /** Tuple elements. */
    fun content(): List<FrozenValue> = contents

    /** Iterate over contents. */
    fun iter(): Iterator<FrozenValue> = contents.iterator()

    companion object {
        /** `type(())`, which is `"tuple"`. */
        const val TYPE: String = TupleGen.TYPE

        private fun new(slice: List<FrozenValue>): FrozenTupleRef = FrozenTupleRef(slice)

        /** Downcast a value to a tuple. */
        fun fromFrozenValue(value: FrozenValue): FrozenTupleRef? {
            val tuple = value.downcastRef<TupleGen<FrozenValue>>() ?: return null
            return new(tuple.content())
        }

        fun starlarkTypeRepr(): Ty = Ty.anyTuple()
    }
}

fun unpackTupleRef(value: Value): TupleRef? {
    return TupleRef.fromValue(value)
}

fun unpackFrozenTupleRef(value: Value): FrozenTupleRef? {
    val frozen = value.unpackFrozen() ?: return null
    return FrozenTupleRef.fromFrozenValue(frozen)
}
