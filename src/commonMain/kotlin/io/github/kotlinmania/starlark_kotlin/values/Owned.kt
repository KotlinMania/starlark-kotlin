// port-lint: source src/values/owned.rs
package io.github.kotlinmania.starlark_kotlin.values.owned

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

import io.github.kotlinmania.starlark_kotlin.values.AllocFrozenValue
import io.github.kotlinmania.starlark_kotlin.values.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.owned_frozen_ref.OwnedFrozenRef
import io.github.kotlinmania.starlark_kotlin.values.owned_frozen_ref.OwnedRefFrozenRef
import io.github.kotlinmania.starlark_kotlin.values.FrozenHeapRef
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.pointer.newFrozen
import io.github.kotlinmania.starlark_kotlin.any.downcastRef

private sealed class OwnedError(override val message: String) : Exception(message) {
    class WrongType(typeName: String, actual: String) : OwnedError(
        "Expected value of type `$typeName` but got `$actual`"
    )
}

/// A [FrozenValue] along with a [FrozenHeapRef] that ensures it is kept alive.
/// Obtained from `FrozenModule.get` or [OwnedFrozenValue.alloc].
///
/// While it is possible to obtain the underlying [FrozenValue] with
/// [uncheckedFrozenValue], that approach is strongly discouraged.
/// See the other methods which unpack the code, access it as a
/// [Value] (which has a suitable lifetime) or add references to other heaps.
class OwnedFrozenValue(
    private val owner: FrozenHeapRef,
    // Invariant: this FrozenValue must be kept alive by the `owner` field.
    private val value: FrozenValue,
) {

    companion object {
        /// Create an [OwnedFrozenValue] in a new heap.
        fun alloc(x: AllocFrozenValue): OwnedFrozenValue {
            val heap = FrozenHeap.new()
            val value = heap.alloc(x)
            return OwnedFrozenValue(heap.intoRef(), value)
        }

        fun default(): OwnedFrozenValue = alloc(NoneAllocFrozenValue)
    }

    override fun toString(): String = value.toString()

    /// Unpack the boolean contained in the underlying value, or null if it is not a boolean.
    fun unpackBool(): Boolean? = value.unpackBool()

    /// Obtain the underlying integer if it fits in an Int.
    /// Note floats are not considered integers, i.e. `unpackI32` for `1.0` will return null.
    fun unpackI32(): Int? = value.unpackI32()

    /// Unpack the string contained in the underlying value, or null if it is not a string.
    fun unpackStr(): String? = value.unpackStr()

    /// Check if `self` references a value of type [T].
    inline fun <reified T : StarlarkValue> downcast(): Result<OwnedFrozenValueTyped<T>> {
        val typed = FrozenValueTyped.new<T>(value)
        return if (typed != null) {
            Result.success(OwnedFrozenValueTyped(owner, typed))
        } else {
            Result.failure(OwnedError.WrongType(
                T::class.simpleName ?: "unknown",
                value.toString()
            ))
        }
    }

    /// Obtain the [Value] stored inside.
    fun value(): Value = Value.newFrozen(value)

    /// Extract a [Value] by passing the [FrozenHeap] which will promise to keep it alive.
    fun ownedValue(heap: FrozenHeap): Value {
        return ownedFrozenValue(heap).toValue()
    }

    /// Operate on the [FrozenValue] stored inside.
    /// Safe provided you don't store the argument [FrozenValue] after the closure has returned.
    fun map(f: (FrozenValue) -> FrozenValue): OwnedFrozenValue {
        return OwnedFrozenValue(owner, f(value))
    }

    /// Same as [map] above but with [Result].
    fun <E : Throwable> tryMap(f: (FrozenValue) -> Result<FrozenValue>): Result<OwnedFrozenValue> {
        return f(value).map { OwnedFrozenValue(owner, it) }
    }

    /// Obtain a reference to the FrozenHeap that owns this value.
    fun owner(): FrozenHeapRef = owner

    /// Obtain direct access to the [FrozenValue] that lives inside.
    /// If you drop all references to the [FrozenHeap] keeping it alive,
    /// any code using the [FrozenValue] may fail.
    fun uncheckedFrozenValue(): FrozenValue = value

    /// Extract a [FrozenValue] by passing the [FrozenHeap] which will keep it alive.
    fun ownedFrozenValue(heap: FrozenHeap): FrozenValue {
        heap.addReference(owner)
        return value
    }
}

/// Same as [OwnedFrozenValue] but it is known to contain [T].
class OwnedFrozenValueTyped<T : StarlarkValue>(
    private val owner: FrozenHeapRef,
    private val value: FrozenValueTyped<T>,
) {

    /// Access the underlying value.
    fun asRef(): T = value.asRef()

    /// Create an [OwnedFrozenValueTyped] from an owner and typed value.
    companion object {
        fun <T : StarlarkValue> new(owner: FrozenHeapRef, value: FrozenValueTyped<T>): OwnedFrozenValueTyped<T> {
            return OwnedFrozenValueTyped(owner, value)
        }
    }

    /// Erase the type.
    fun toFrozenValue(): FrozenValue = value.toFrozenValue()

    /// Get a value reference.
    fun toValue(): Value = toFrozenValue().toValue()

    /// Erase the type.
    fun toOwnedFrozenValue(): OwnedFrozenValue {
        return OwnedFrozenValue(owner, value.toFrozenValue())
    }

    /// Convert to borrowed ref.
    fun asOwnedRefFrozenRef(): OwnedRefFrozenRef<T> {
        return OwnedRefFrozenRef.newUnchecked(value.asRef(), owner)
    }

    /// Convert to an owned ref.
    fun intoOwnedFrozenRef(): OwnedFrozenRef<T> {
        return OwnedFrozenRef.newUnchecked(value.asRef(), owner)
    }

    /// Obtain a reference to the FrozenHeap that owns this value.
    fun owner(): FrozenHeapRef = owner

    /// Obtain a reference to the value.
    fun valueTyped(): FrozenValueTyped<T> = value

    /// Extract a [FrozenValueTyped] by passing the [FrozenHeap] which will keep it alive.
    fun ownedFrozenValueTyped(heap: FrozenHeap): FrozenValueTyped<T> {
        heap.addReference(owner)
        return value
    }

    /// Extract a [FrozenValue] by passing the [FrozenHeap] which will keep it alive.
    fun ownedFrozenValue(heap: FrozenHeap): FrozenValue {
        return ownedFrozenValueTyped(heap).toFrozenValue()
    }

    /// Extract a [Value] by passing the [FrozenHeap] which will promise to keep it alive.
    fun ownedValue(heap: FrozenHeap): Value {
        return ownedFrozenValue(heap).toValue()
    }

    /// Extract a reference by passing the [FrozenHeap] which will promise to keep it alive.
    fun ownedAsRef(heap: FrozenHeap): T {
        // Keep the reference.
        ownedValue(heap)
        return asRef()
    }

    /// Operate on the [FrozenValue] stored inside.
    fun <U : StarlarkValue> map(f: (FrozenValueTyped<T>) -> FrozenValueTyped<U>): OwnedFrozenValueTyped<U> {
        return OwnedFrozenValueTyped(owner, f(value))
    }

    /// Same as [map] above but with [Result].
    fun <U : StarlarkValue> tryMap(f: (FrozenValueTyped<T>) -> Result<FrozenValueTyped<U>>): Result<OwnedFrozenValueTyped<U>> {
        return f(value).map { OwnedFrozenValueTyped(owner, it) }
    }

    /// Same as [map] above but with nullable.
    fun <U : StarlarkValue> maybeMap(f: (FrozenValueTyped<T>) -> FrozenValueTyped<U>?): OwnedFrozenValueTyped<U>? {
        val result = f(value) ?: return null
        return OwnedFrozenValueTyped(owner, result)
    }
}

// Placeholder for NoneType alloc — will be replaced when NoneType is fully ported.
private object NoneAllocFrozenValue : AllocFrozenValue {
    override fun allocFrozenValue(heap: FrozenHeap): FrozenValue = FrozenValue.newNone()
}

// Placeholder for FrozenValueTyped — represents a typed wrapper around FrozenValue.
// Will be replaced with the real implementation from values/layout.
class FrozenValueTyped<T : StarlarkValue>(
    private val frozenValue: FrozenValue,
    private val ref: T,
) {
    companion object {
        inline fun <reified T : StarlarkValue> new(value: FrozenValue): FrozenValueTyped<T>? {
            val ref = value.downcastRef<T>() ?: return null
            return FrozenValueTyped(value, ref)
        }
    }

    fun toFrozenValue(): FrozenValue = frozenValue
    fun toValue(): Value = frozenValue.toValue()
    fun asRef(): T = ref
}
