// port-lint: source src/values/layout/vtable.rs
package io.github.kotlinmania.starlark.values.layout

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

import io.github.kotlinmania.starlark.collections.Hashed
import io.github.kotlinmania.starlark.collections.StarlarkHashValue
import io.github.kotlinmania.starlark.collections.StarlarkHasher
import io.github.kotlinmania.starlark.docs.DocItem
import io.github.kotlinmania.starlark.environment.Methods
import io.github.kotlinmania.starlark.eval.runtime.Arguments
import io.github.kotlinmania.starlark.eval.runtime.Evaluator
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.demand.Demand
import io.github.kotlinmania.starlark.values.layout.heap.AValueRepr
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.layout.heap.Tracer
import io.github.kotlinmania.starlark.values.layout.typed.FrozenStringValue
import io.github.kotlinmania.starlark.values.starlarktypeid.StarlarkTypeId
import io.github.kotlinmania.starlark.values.types.int.PointerI32
import kotlin.reflect.KClass

/**
 * Untyped raw pointer to StarlarkValue without vtable.
 *
 * In Kotlin, this wraps a strongly typed value reference instead of a raw pointer.
 */
class StarlarkValueRawPtr(
    /** The underlying value reference. */
    private val ptr: StarlarkValue,
) {
    companion object {
        fun newHeader(header: StarlarkValue): StarlarkValueRawPtr = StarlarkValueRawPtr(header)

        internal fun newPointerI32(ptr: PointerI32): StarlarkValueRawPtr = StarlarkValueRawPtr(ptr)
    }

    @PublishedApi
    internal fun starlarkValue(): StarlarkValue = ptr
}

/**
 * VTable for AValue operations.
 *
 * In Kotlin, instead of function pointers for dynamic dispatch, we hold
 * type metadata and delegate to the StarlarkValue interface directly.
 * This struct contains metadata and dispatch shims for all operations on a Starlark value.
 */
class AValueVTable(
    // Common AValue fields.
    val staticTypeOfValue: ConstTypeId,
    val starlarkTypeId: StarlarkTypeId,
    val typeName: String,
    // AValue
    val isStr: Boolean,
    internal val memorySizeFn: (StarlarkValueRawPtr) -> ValueAllocSize,
    internal val heapFreezeFn: (AValueRepr<*>, StarlarkValueRawPtr, Freezer) -> Result<FrozenValue>,
    internal val heapCopyFn: (StarlarkValueRawPtr, Tracer) -> Value,
    // StarlarkValue dispatch
    internal val starlarkValue: StarlarkValue,
    /**
     * Capability flags mirroring Rust's StarlarkValueVTable HAS_* constants.
     * These indicate which StarlarkValue trait methods are meaningfully implemented.
     */
    val hasInvoke: Boolean = false,
    val hasEvalType: Boolean = false,
    val hasIterate: Boolean = false,
    val hasEquals: Boolean = false,
    // Display/Debug
    private val displayFn: (StarlarkValueRawPtr) -> String = { it.starlarkValue().toString() },
    private val debugFn: (StarlarkValueRawPtr) -> String = { it.starlarkValue().toString() },
) {
    companion object {
        internal fun newBlackHole(blackHole: BlackHole = BlackHole(ValueAllocSize(AlignedSize(0u)))): AValueVTable =
            AValueVTable(
                staticTypeOfValue = ConstTypeId.of<BlackHole>(),
                starlarkTypeId = StarlarkTypeId.fromTypeId(ConstTypeId.of<BlackHole>()),
                typeName = "BlackHole",
                isStr = false,
                memorySizeFn = { _ ->
                    blackHole.size
                },
                heapFreezeFn = { _, _, _ -> error("BlackHole") },
                heapCopyFn = { _, _ -> error("BlackHole") },
                starlarkValue =
                    object : StarlarkValue {
                        override val TYPE: String get() = "BlackHole"
                    },
                displayFn = { "BlackHole" },
                debugFn = { "BlackHole" },
            )

        /**
         * Build an [AValueVTable] from a [KClass].
         *
         * This mirrors the Rust `AValueVTable::new::<T>()` which creates
         * a static vtable from the type parameter at compile time.
         * Used by the vtable registry for deserialization support.
         */
        inline fun <reified T : Any> new(): AValueVTable = forType(T::class)

        /**
         * Build an [AValueVTable] for the given type class.
         *
         * Constructs a vtable with type metadata derived from the [KClass].
         * The vtable uses stub implementations for heap operations since
         * deserialization reconstructs values through the pagable subsystem
         * rather than through these vtable functions.
         */
        fun forType(type: KClass<*>): AValueVTable {
            val typeId = ConstTypeId.of(type)
            val typeName = type.simpleName ?: type.toString()
            return AValueVTable(
                staticTypeOfValue = typeId,
                starlarkTypeId = StarlarkTypeId.fromTypeId(typeId),
                typeName = typeName,
                isStr = false,
                memorySizeFn = { _ -> ValueAllocSize.new(AlignedSize.newBytes(16)) },
                heapFreezeFn = { _, _, _ -> error("forType: heapFreeze not supported for $typeName") },
                heapCopyFn = { _, _ -> error("forType: heapCopy not supported for $typeName") },
                starlarkValue =
                    object : StarlarkValue {
                        override val TYPE: String get() = typeName
                    },
            )
        }
    }

    fun typeValue(): FrozenStringValue = starlarkValue.getTypeValueStatic()

    fun typeStarlarkRepr(): Ty = starlarkValue.getTypeStarlarkRepr()

    fun methods(): Methods? = starlarkValue.getMethods()

    /**
     * Create an AValueDyn from this vtable.
     * Used by AValueHeader.unpack() to create a dynamic dispatch reference.
     */
    internal fun unpackDyn(): AValueDyn = AValueDyn(StarlarkValueRawPtr(starlarkValue), this)
}

/**
 * A dynamically dispatched reference to a Starlark value with its vtable.
 *
 * In Kotlin, this wraps a StarlarkValue reference and its vtable metadata,
 * providing forwarding methods that delegate to the vtable or the StarlarkValue interface.
 */
internal class AValueDyn(
    internal val value: StarlarkValueRawPtr,
    private val _vtable: AValueVTable,
) {
    fun vtable(): AValueVTable = _vtable

    fun memorySize(): ValueAllocSize = _vtable.memorySizeFn(value)

    fun heapFreeze(
        repr: AValueRepr<*>,
        freezer: Freezer,
    ): Result<FrozenValue> = _vtable.heapFreezeFn(repr, value, freezer)

    fun heapCopy(tracer: Tracer): Value = _vtable.heapCopyFn(value, tracer)

    fun documentation(): DocItem = starlarkValue().documentation()

    fun typecheckerTy(): Ty? = starlarkValue().typecheckerTy()

    fun evalType(): Ty? = starlarkValue().evalType()

    fun at(index: Value, heap: Heap): Result<Value> = starlarkValue().at(index, heap)

    fun at2(index0: Value, index1: Value, heap: Heap): Result<Value> = starlarkValue().at2(index0, index1, heap)

    fun isIn(collection: Value): Result<Boolean> = starlarkValue().isIn(collection)

    fun slice(start: Value?, stop: Value?, step: Value?, heap: Heap): Result<Value> = starlarkValue().slice(start, stop, step, heap)

    fun getAttr(name: String, heap: Heap): Value? = starlarkValue().getAttr(name, heap)

    fun getAttrHashed(name: Hashed<String>, heap: Heap): Value? = starlarkValue().getAttrHashed(name, heap)

    fun hasAttr(name: String, heap: Heap): Boolean = starlarkValue().hasAttr(name, heap)

    fun dirAttr(): List<String> = starlarkValue().dirAttr()

    fun bitAnd(other: Value, heap: Heap): Result<Value> = starlarkValue().bitAnd(other, heap)

    fun bitOr(other: Value, heap: Heap): Result<Value> = starlarkValue().bitOr(other, heap)

    fun bitXor(other: Value, heap: Heap): Result<Value> = starlarkValue().bitXor(other, heap)

    fun bitNot(heap: Heap): Result<Value> = starlarkValue().bitNot(heap)

    fun toBool(): Boolean = starlarkValue().toBool()

    fun length(): Result<Int> = starlarkValue().length()

    fun iterate(me: Value, heap: Heap): Result<Value> = starlarkValue().iterate(me, heap)

    fun iterNext(index: Int, heap: Heap): Value? = starlarkValue().iterNext(index, heap)

    fun iterSizeHint(index: Int): Pair<Int, Int?> = starlarkValue().iterSizeHint(index)

    fun iterStop() {
        starlarkValue().iterStop()
    }

    fun getHash(): Result<StarlarkHashValue> = starlarkValue().getHash()

    fun plus(heap: Heap): Result<Value> = starlarkValue().plus(heap)

    fun minus(heap: Heap): Result<Value> = starlarkValue().minus(heap)

    fun add(other: Value, heap: Heap): Result<Value>? = starlarkValue().add(other, heap)

    fun radd(other: Value, heap: Heap): Result<Value>? = starlarkValue().radd(other, heap)

    fun sub(other: Value, heap: Heap): Result<Value> = starlarkValue().sub(other, heap)

    fun mul(other: Value, heap: Heap): Result<Value>? = starlarkValue().mul(other, heap)

    fun rmul(other: Value, heap: Heap): Result<Value>? = starlarkValue().rmul(other, heap)

    fun div(other: Value, heap: Heap): Result<Value> = starlarkValue().div(other, heap)

    fun floorDiv(other: Value, heap: Heap): Result<Value> = starlarkValue().floorDiv(other, heap)

    fun percent(other: Value, heap: Heap): Result<Value> = starlarkValue().percent(other, heap)

    fun leftShift(other: Value, heap: Heap): Result<Value> = starlarkValue().leftShift(other, heap)

    fun rightShift(other: Value, heap: Heap): Result<Value> = starlarkValue().rightShift(other, heap)

    fun collectRepr(collector: StringBuilder) {
        starlarkValue().collectRepr(collector)
    }

    fun collectReprCycle(collector: StringBuilder) {
        starlarkValue().collectReprCycle(collector)
    }

    inline fun <reified T : StarlarkValue> downcastRef(): T? {
        val sv = starlarkValue()
        return sv as? T
    }

    fun equals(other: Value): Result<Boolean> = starlarkValue().equals(other)

    fun compare(other: Value): Result<Int> = starlarkValue().compare(other)

    fun nameForCallStack(me: Value): String = starlarkValue().nameForCallStack(me)

    fun exportAs(variableName: String, eval: Evaluator): Result<Unit> = starlarkValue().exportAs(variableName, eval)

    fun setAt(index: Value, newValue: Value): Result<Unit> = starlarkValue().setAt(index, newValue)

    fun setAttr(attribute: String, newValue: Value): Result<Unit> = starlarkValue().setAttr(attribute, newValue)

    fun writeHash(hasher: StarlarkHasher): Result<Unit> = starlarkValue().writeHash(hasher)

    fun typeMatchesValue(value: Value): Boolean = starlarkValue().typeMatchesValue(value)

    fun asDisplay(): Any = starlarkValue()

    fun asDebug(): Any = starlarkValue()

    fun provide(demand: Demand) {
        starlarkValue().provide(demand)
    }

    internal fun starlarkValue(): StarlarkValue = value.starlarkValue()

    override fun toString(): String = "AValueDyn(..)"
}

/** Raw pointer, vtable and Value. */
internal class AValueDynFull(
    private val avalue: AValueDyn,
    val value: Value,
) {
    fun invoke(
        args: Arguments,
        eval: Evaluator,
    ): Result<Value> {
        val sv: StarlarkValue = avalue.value.starlarkValue()
        return try {
            sv.invoke(value, args, eval)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// BlackHole is defined in Avalue.kt (same package)
