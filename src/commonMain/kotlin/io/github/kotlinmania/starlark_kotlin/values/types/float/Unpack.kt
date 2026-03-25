// port-lint: source src/values/types/float/unpack.rs
package io.github.kotlinmania.starlark_kotlin.values.types.float

import io.github.kotlinmania.starlark_kotlin.values.layout.value
import io.github.kotlinmania.starlark_kotlin.values.value_of.unpackValueImpl
import io.github.kotlinmania.starlark_kotlin.values.types.num.asFloat
import io.github.kotlinmania.starlark_kotlin.tests.assert
import io.github.kotlinmania.starlark_kotlin.values.unpack_and_discard.unpackValueImpl

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
 * Unpack `int` or `float` into `f64`.
 */
@JvmInline
value class UnpackFloat(val value: Double)

// Extension functions implementing trait-like behavior for UnpackFloat

/**
 * StarlarkTypeRepr implementation for UnpackFloat.
 * Canonical type is Num::Canonical, delegates to Num's type representation.
 */
internal fun UnpackFloat.Companion.starlarkTypeRepr(): Ty =
    Num.Companion.canonical().starlarkTypeRepr()

/**
 * UnpackValue implementation for UnpackFloat.
 * Unpacks NumRef and converts to f64 via as_float().
 */
internal fun UnpackFloat.Companion.unpackValueImpl(value: Value<*>): Result<UnpackFloat?> {
    val num = NumRef.Companion.unpackValueImpl(value).getOrNull()
        ?: return Result.success(null)
    return Result.success(num?.let { UnpackFloat(it.asFloat()) })
}

// Placeholder types for dependencies that will be ported later
internal class Ty private constructor()
internal class Value<V> private constructor() {
    companion object {
        fun testingNewInt(i: Int): Value<*> = Value()
    }
}
internal class NumRef<V> private constructor() {
    fun asFloat(): Double = 0.0
    companion object {
        fun unpackValueImpl(value: Value<*>): Result<NumRef<*>?> = Result.success(null)
    }
}
internal class Num private constructor() {
    companion object {
        fun canonical(): StarlarkTypeRepr = object : StarlarkTypeRepr {
            override fun starlarkTypeRepr(): Ty = Ty()
        }
    }
}
internal interface StarlarkTypeRepr {
    fun starlarkTypeRepr(): Ty
}
internal class Heap private constructor() {
    fun alloc(value: Double): Value<*> = Value()
    companion object {
        fun <T> temp(block: (Heap) -> T): T = block(Heap())
    }
}

// Tests
internal fun testUnpackFloat() {
    Heap.temp { heap ->
        val test1 = UnpackFloat.unpackValueImpl(Value.testingNewInt(1))
            .getOrNull()
        assert(test1?.value == 1.0)

        val test2 = UnpackFloat.unpackValueImpl(heap.alloc(1.0))
            .getOrNull()
        assert(test2?.value == 1.0)
    }
}
