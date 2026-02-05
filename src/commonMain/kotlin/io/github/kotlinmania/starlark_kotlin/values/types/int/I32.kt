// port-lint: source src/values/types/int/i32.rs
package io.github.kotlinmania.starlark_kotlin.values.types.int

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

// Placeholder types - will be removed when actual implementations are ported
internal class Ty private constructor()
internal class Heap<V> private constructor() { fun alloc(value: StarlarkInt): Value<V> = Value() }
internal class FrozenHeap private constructor() { fun alloc(value: StarlarkInt): FrozenValue = FrozenValue() }
internal class Value<V> private constructor() { fun unpackI32(): Int? = null }
internal class FrozenValue private constructor()
internal class StarlarkInt private constructor() { companion object { fun from(value: Int): StarlarkInt = StarlarkInt() } }
internal class StarlarkIntRef private constructor() {
    companion object { fun unpack(value: Value<*>): StarlarkIntRef? = null }
    override fun toString(): String = ""
}
internal class IntegerTooBigError(val value: String, val integerType: String) :
    Exception("Integer value is too big to fit in $integerType: $value")
internal interface StarlarkTypeRepr { fun starlarkTypeRepr(): Ty }
internal object PointerI32 { fun starlarkTypeRepr(): Ty = Ty() }

// impl<V_> AllocValue<V_> for i32
internal inline fun <V> Int.allocValue(heap: Heap<V>): Value<V> {
    return heap.alloc(StarlarkInt.from(this))
}

// impl AllocFrozenValue for i32
internal inline fun Int.allocFrozenValue(heap: FrozenHeap): FrozenValue {
    return heap.alloc(StarlarkInt.from(this))
}

// impl StarlarkTypeRepr for i32
internal object IntStarlarkTypeRepr : StarlarkTypeRepr {
    override fun starlarkTypeRepr(): Ty {
        return PointerI32.starlarkTypeRepr()
    }
}

// impl UnpackValue<'_> for i32
internal fun Int.Companion.unpackValueImpl(value: Value<*>): Result<Int?> {
    // Note this does not use `Value::unpack_integer()`
    // because we unlike other call sites,
    // we know that `i32` is `InlineInt` on 64-bit platforms and never `BigInt`,
    // so this is faster.
    val v = value.unpackI32()
    if (v != null) {
        return Result.success(v)
    } else {
        val int = StarlarkIntRef.unpack(value)
        if (int != null) {
            return Result.failure(
                IntegerTooBigError(
                    value = int.toString(),
                    integerType = "kotlin.Int"
                )
            )
        } else {
            return Result.success(null)
        }
    }
}
