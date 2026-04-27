// port-lint: source src/values/types/int/i32.rs
package io.github.kotlinmania.starlark.values.types.int

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

import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.AllocFrozenValue
import io.github.kotlinmania.starlark.values.AllocValue
import io.github.kotlinmania.starlark.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark.values.UnpackValue
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.IntegerTooBigError
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.layout.heap.Heap

/**
 *
 * the wrapped [Int] onto the heap.
 */
class I32AllocValue(val value: Int) : AllocValue {
    override fun starlarkTypeRepr(): Ty = Ty.int()
    override fun allocValue(heap: Heap): Value {
        return heap.alloc(StarlarkInt.from(value))
    }
}

/**
 *
 * from the wrapped [Int] onto the frozen heap.
 */
class I32AllocFrozenValue(val value: Int) : AllocFrozenValue {
    override fun starlarkTypeRepr(): Ty = Ty.int()
    override fun allocFrozenValue(heap: FrozenHeap): FrozenValue {
        return heap.alloc(StarlarkInt.from(value))
    }
}

/**
 *
 * `type Canonical = <StarlarkInt as StarlarkTypeRepr>::Canonical`.
 *
 * by `PointerI32`, whose canonical type is `StarlarkBigInt`.
 */
object I32TypeRepr : StarlarkTypeRepr {
    override fun starlarkTypeRepr(): Ty = Ty.int()
}

/**
 *
 * `type Error = crate::Error`.
 *
 * Note this does not use `Value::unpackInteger()` because unlike other call sites,
 * we know that `i32` is `InlineInt` on 64-bit platforms and never `BigInt`,
 * so this is faster. If the value isn't an inline `i32` but is some other
 * `StarlarkIntRef`, surface [IntegerTooBigError]; otherwise return `null`.
 */
object I32Unpack : UnpackValue<Int> {
    override fun starlarkTypeRepr(): Ty = Ty.int()

    override fun unpackValueImpl(value: Value): Result<Int?> {
        val v = value.unpackI32()
        if (v != null) {
            return Result.success(v)
        }
        val int = StarlarkIntRef.unpack(value)
        if (int != null) {
            return Result.failure(
                IntegerTooBigError(
                    integerType = "Int",
                    value = int.toString(),
                )
            )
        }
        return Result.success(null)
    }
}
