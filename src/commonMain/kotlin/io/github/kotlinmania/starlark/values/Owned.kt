// port-lint: source src/values/owned.rs
package io.github.kotlinmania.starlark.values.owned

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

import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.AllocFrozenValue
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.FrozenValueTyped
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeapRef
import io.github.kotlinmania.starlark.values.ownedfrozenref.OwnedFrozenRef
import io.github.kotlinmania.starlark.values.ownedfrozenref.OwnedRefFrozenRef

internal sealed class OwnedError(
    override val message: String,
) : Exception(message) {
    class WrongType(
        typeName: String,
        actual: String,
    ) : OwnedError(
            "Expected value of type `$typeName` but got `$actual`",
        )
}

/**
 * A [FrozenValue] along with a [FrozenHeapRef] that ensures it is kept alive.
 * Obtained from `FrozenModule.get` or [OwnedFrozenValue.alloc].
 *
 * While it is possible to obtain the underlying [FrozenValue] with
 * [uncheckedFrozenValue], that approach is strongly discouraged.
 * See the other methods which unpack the code, access it as a
 * [Value] (which has a suitable lifetime) or add references to other heaps.
 */
public class OwnedFrozenValue(
    @PublishedApi internal val owner: FrozenHeapRef,
    // Invariant: this FrozenValue must be kept alive by the `owner` field.
    @PublishedApi internal val value: FrozenValue,
) : AutoCloseable {
    override fun close() {
        owner.close()
    }

    companion object {
        /** Create an [OwnedFrozenValue] in a new heap. */
        fun alloc(x: AllocFrozenValue): OwnedFrozenValue {
            val heap = FrozenHeap.new()
            val value = heap.alloc(x)
            return OwnedFrozenValue(heap.intoRef(), value)
        }

        fun default(): OwnedFrozenValue = alloc(NoneAllocFrozenValue)
    }

    override fun toString(): String = value.toString()

    /** Unpack the boolean contained in the underlying value, or null if it is not a boolean. */
    fun unpackBool(): Boolean? = value.unpackBool()

    /**
     * Obtain the underlying integer if it fits in an Int.
     * Note floats are not considered integers, i.e. `unpackI32` for `1.0` will return null.
     */
    fun unpackI32(): Int? = value.unpackI32()

    /** Unpack the string contained in the underlying value, or null if it is not a string. */
    fun unpackStr(): String? = value.unpackStr()

    /** Check if `self` references a value of type [T]. */
    internal inline fun <reified T : StarlarkValue> downcast(): Result<OwnedFrozenValueTyped<T>> {
        val typed = FrozenValueTyped.new<T>(value)
        return if (typed != null) {
            Result.success(OwnedFrozenValueTyped(owner.clone(), typed))
        } else {
            Result.failure(
                OwnedError.WrongType(
                    T::class.simpleName ?: "unknown",
                    value.toString(),
                ),
            )
        }
    }

    /** Obtain the [Value] stored inside. */
    fun value(): Value = Value.newFrozen(value)

    /** Extract a [Value] by passing the [FrozenHeap] which will promise to keep it alive. */
    fun ownedValue(heap: FrozenHeap): Value = ownedFrozenValue(heap).toValue()

    /**
     * Operate on the [FrozenValue] stored inside.
     * Safe provided you don't store the argument [FrozenValue] after the closure has returned.
     */
    fun map(f: (FrozenValue) -> FrozenValue): OwnedFrozenValue = OwnedFrozenValue(owner.clone(), f(value))

    /** Same as [map] above but with [Result]. */
    fun <E : Throwable> tryMap(f: (FrozenValue) -> Result<FrozenValue>): Result<OwnedFrozenValue> = f(value).map { OwnedFrozenValue(owner.clone(), it) }

    /** Obtain a reference to the FrozenHeap that owns this value. */
    fun owner(): FrozenHeapRef = owner

    /**
     * Obtain direct access to the [FrozenValue] that lives inside.
     * If you drop all references to the [FrozenHeap] keeping it alive,
     * any code using the [FrozenValue] may fail.
     */
    fun uncheckedFrozenValue(): FrozenValue = value

    /** Extract a [FrozenValue] by passing the [FrozenHeap] which will keep it alive. */
    fun ownedFrozenValue(heap: FrozenHeap): FrozenValue {
        heap.addReference(owner)
        return value
    }
}

/** Same as [OwnedFrozenValue] but it is known to contain [T]. */
internal class OwnedFrozenValueTyped<T : StarlarkValue>(
    private val owner: FrozenHeapRef,
    private val value: FrozenValueTyped<T>,
) : AutoCloseable {
    override fun close() {
        owner.close()
    }

    /** Access the underlying value. */
    fun asRef(): T = value.asRef()

    /** Create an [OwnedFrozenValueTyped] from an owner and typed value. */
    companion object {
        fun <T : StarlarkValue> new(owner: FrozenHeapRef, value: FrozenValueTyped<T>): OwnedFrozenValueTyped<T> = OwnedFrozenValueTyped(owner, value)
    }

    /** Erase the type. */
    fun toFrozenValue(): FrozenValue = value.toFrozenValue()

    /** Get a value reference. */
    fun toValue(): Value = toFrozenValue().toValue()

    /** Erase the type. */
    fun toOwnedFrozenValue(): OwnedFrozenValue = OwnedFrozenValue(owner.clone(), value.toFrozenValue())

    /** Convert to borrowed ref. */
    fun asOwnedRefFrozenRef(): OwnedRefFrozenRef<T> = OwnedRefFrozenRef.newUnchecked(value.asRef(), owner.clone())

    /** Convert to an owned ref. */
    fun intoOwnedFrozenRef(): OwnedFrozenRef<T> = OwnedFrozenRef.newUnchecked(value.asRef(), owner.clone())

    /** Obtain a reference to the FrozenHeap that owns this value. */
    fun owner(): FrozenHeapRef = owner

    /** Obtain a reference to the value. */
    fun valueTyped(): FrozenValueTyped<T> = value

    /** Extract a [FrozenValueTyped] by passing the [FrozenHeap] which will keep it alive. */
    fun ownedFrozenValueTyped(heap: FrozenHeap): FrozenValueTyped<T> {
        heap.addReference(owner)
        return value
    }

    /** Extract a [FrozenValue] by passing the [FrozenHeap] which will keep it alive. */
    fun ownedFrozenValue(heap: FrozenHeap): FrozenValue = ownedFrozenValueTyped(heap).toFrozenValue()

    /** Extract a [Value] by passing the [FrozenHeap] which will promise to keep it alive. */
    fun ownedValue(heap: FrozenHeap): Value = ownedFrozenValue(heap).toValue()

    /** Extract a reference by passing the [FrozenHeap] which will promise to keep it alive. */
    fun ownedAsRef(heap: FrozenHeap): T {
        // Keep the reference.
        ownedValue(heap)
        return asRef()
    }

    /** Operate on the [FrozenValue] stored inside. */
    fun <U : StarlarkValue> map(f: (FrozenValueTyped<T>) -> FrozenValueTyped<U>): OwnedFrozenValueTyped<U> = OwnedFrozenValueTyped(owner.clone(), f(value))

    /** Same as [map] above but with [Result]. */
    fun <U : StarlarkValue> tryMap(f: (FrozenValueTyped<T>) -> Result<FrozenValueTyped<U>>): Result<OwnedFrozenValueTyped<U>> = f(value).map { OwnedFrozenValueTyped(owner.clone(), it) }

    /** Same as [map] above but with nullable. */
    fun <U : StarlarkValue> maybeMap(f: (FrozenValueTyped<T>) -> FrozenValueTyped<U>?): OwnedFrozenValueTyped<U>? {
        val result = f(value) ?: return null
        return OwnedFrozenValueTyped(owner.clone(), result)
    }
}

// Placeholder for NoneType alloc — will be replaced when NoneType is fully ported.
private object NoneAllocFrozenValue : AllocFrozenValue {
    override fun allocFrozenValue(heap: FrozenHeap): FrozenValue = FrozenValue.newNone()

    override fun starlarkTypeRepr(): Ty = Ty.none()
}
