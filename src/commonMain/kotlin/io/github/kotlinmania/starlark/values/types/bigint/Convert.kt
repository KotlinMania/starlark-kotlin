// port-lint: source src/values/types/bigint/convert.rs
package io.github.kotlinmania.starlark.values.types.bigint

/*
 * Copyright 2019 The Starlark in Rust Authors.
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

/**
 * Conversion adapters for Kotlin numeric types to/from Starlark values.
 *
 * for StarlarkTypeRepr, AllocValue, AllocFrozenValue, and UnpackValue.
 * In Kotlin, we cannot implement interfaces on primitives, so we provide
 * extension functions and converter objects.
 */

import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.AllocValue
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.types.int.StarlarkInt
import io.github.kotlinmania.starlark.values.types.int.StarlarkIntRef
import io.github.kotlinmania.starlark.values.types.int.allocFrozenValue
import io.github.kotlinmania.starlark.values.types.int.allocValue

/**
 * Starlark type repr for all integer types.
 * Canonical type is i32's repr.
 */
private fun intStarlarkTypeRepr(): Ty = IntTypeReprCanonical.starlarkTypeRepr()

/** Canonical integer type repr reference. */
internal object IntTypeReprCanonical : StarlarkTypeRepr {
    override fun starlarkTypeRepr(): Ty = Ty.int()
}

// --- UInt conversions ---

object UIntTypeRepr : StarlarkTypeRepr {
    override fun starlarkTypeRepr(): Ty = intStarlarkTypeRepr()
}

object UIntAllocValue : AllocValue {
    override fun starlarkTypeRepr(): Ty = intStarlarkTypeRepr()
    override fun allocValue(heap: Heap): Value = StarlarkInt.from(0u).allocValue(heap)
}

/** Allocate a UInt on a Starlark heap. */
fun UInt.allocValue(heap: Heap): Value = StarlarkInt.from(this).allocValue(heap)

/** Allocate a UInt on a frozen Starlark heap. */
fun UInt.allocFrozenValue(heap: FrozenHeap): FrozenValue = StarlarkInt.from(this).allocFrozenValue(heap)

// --- ULong conversions ---

object ULongTypeRepr : StarlarkTypeRepr {
    override fun starlarkTypeRepr(): Ty = intStarlarkTypeRepr()
}

/** Allocate a ULong on a Starlark heap. */
fun ULong.allocValue(heap: Heap): Value = StarlarkInt.from(this).allocValue(heap)

/** Allocate a ULong on a frozen Starlark heap. */
fun ULong.allocFrozenValue(heap: FrozenHeap): FrozenValue = StarlarkInt.from(this).allocFrozenValue(heap)

// --- Long conversions ---

object LongTypeRepr : StarlarkTypeRepr {
    override fun starlarkTypeRepr(): Ty = intStarlarkTypeRepr()
}

/** Allocate a Long on a Starlark heap. */
fun Long.allocValue(heap: Heap): Value = StarlarkInt.from(this).allocValue(heap)

/** Allocate a Long on a frozen Starlark heap. */
fun Long.allocFrozenValue(heap: FrozenHeap): FrozenValue = StarlarkInt.from(this).allocFrozenValue(heap)

// --- Int conversions ---

/** Allocate an Int on a Starlark heap. */
fun Int.allocValue(heap: Heap): Value = StarlarkInt.from(this).allocValue(heap)

/** Allocate an Int on a frozen Starlark heap. */
fun Int.allocFrozenValue(heap: FrozenHeap): FrozenValue = StarlarkInt.from(this).allocFrozenValue(heap)

// --- BigInteger conversions ---

object BigIntegerTypeRepr : StarlarkTypeRepr {
    override fun starlarkTypeRepr(): Ty = intStarlarkTypeRepr()
}

/** Allocate a BigInteger on a Starlark heap. */
fun BigInteger.allocValue(heap: Heap): Value = StarlarkInt.from(this).allocValue(heap)

/** Allocate a BigInteger on a frozen Starlark heap. */
fun BigInteger.allocFrozenValue(heap: FrozenHeap): FrozenValue = StarlarkInt.from(this).allocFrozenValue(heap)

// --- UnpackValue implementations ---

/**
 * Unpack a UInt from a Starlark Value.
 */
fun Value.unpackUInt(): Result<UInt?> = unpackInteger().map { it?.toUInt() }

/**
 * Unpack a ULong from a Starlark Value.
 */
fun Value.unpackULong(): Result<ULong?> = unpackInteger().map { it?.toULong() }

/**
 * Unpack a Long from a Starlark Value.
 */
fun Value.unpackLong(): Result<Long?> = unpackInteger()

/**
 * Unpack an Int from a Starlark Value.
 */
fun Value.unpackInt(): Result<Int?> = unpackInteger().map { it?.toInt() }

/**
 * Unpack a BigInteger from a Starlark Value.
 */
fun Value.unpackBigInteger(): Result<BigInteger?> {
    val intRef = StarlarkIntRef.unpackValueOpt(this) ?: return Result.success(null)
    return Result.success(
        when (intRef) {
            is StarlarkIntRef.Small -> BigInteger.fromInt(intRef.value.toI32())
            is StarlarkIntRef.Big -> intRef.value.get()
        }
    )
}
