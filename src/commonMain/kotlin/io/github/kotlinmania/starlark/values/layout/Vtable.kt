// port-lint: source values/layout/vtable.rs
package io.github.kotlinmania.starlark.values.layout

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

import io.github.kotlinmania.starlarkmap.Hashed
import io.github.kotlinmania.starlarkmap.StarlarkHashValue
import io.github.kotlinmania.starlarkmap.StarlarkHasher
import io.github.kotlinmania.starlark.docs.DocItem
import io.github.kotlinmania.starlark.environment.Methods
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.layout.Freezer
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.demand.Demand
import io.github.kotlinmania.starlark.values.layout.ValueAllocSize
import io.github.kotlinmania.starlark.values.starlarktypeid.StarlarkTypeId
import io.github.kotlinmania.starlark.values.types.int.PointerI32
import io.github.kotlinmania.starlark.values.layout.typed.FrozenStringValue
import io.github.kotlinmania.starlark.eval.runtime.Evaluator
import io.github.kotlinmania.starlark.eval.runtime.Arguments
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.layout.heap.Tracer
import io.github.kotlinmania.starlark.values.layout.heap.AValueHeader
import io.github.kotlinmania.starlark.values.layout.heap.AValueRepr
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

/**
 * Untyped raw pointer to a [StarlarkValue] without an attached vtable.
 */
class StarlarkValueRawPtr(
    /** The underlying value reference. */
    val ptr: Any,
) {
    companion object {
        fun newHeader(header: AValueHeader): StarlarkValueRawPtr {
            check(header.index % AValueHeader.ALIGN.toLong() == 0L)
            return StarlarkValueRawPtr(header)
        }

        internal fun newPointerI32(ptr: PointerI32): StarlarkValueRawPtr {
            val ptr = ptr as PointerI32
            val ptrAny = ptr as Any
            return StarlarkValueRawPtr(ptrAny)
        }
    }

    /**
     * Pointer to the typed payload.
     */
    internal inline fun <reified T : Any> valuePtr(): T {
        check(
            AValueRepr.paddingAfterHeader<PointerI32>() == 0,
        ) {
            "There is no header for PointerI32, but following code should work"
        }

        val ptr = ptr
        val paddingAfterHeader = AValueRepr.paddingAfterHeader<T>()
        return if (ptr is AValueHeader) {
            val repr = ptr.asRepr<T>()
            check(paddingAfterHeader == AValueRepr.paddingAfterHeader<T>())
            repr.payload
        } else {
            ptr as T
        }
    }

    /** Reference to the typed payload; equivalent to [valuePtr]. */
    internal inline fun <reified T : Any> valueRef(): T = valuePtr()

    /** Non-reified counterpart of [valueRef] / [valuePtr] using runtime [KClass] lookup. */
    fun <T : Any> valueRef(clazz: KClass<T>): T =
        clazz.safeCast(ptr) ?: error("StarlarkValueRawPtr cannot be viewed as ${clazz.simpleName}")
}

/**
 * VTable for [AValue] operations.
 *
 * This struct contains function pointers for all operations on a Starlark value,
 * allowing dynamic dispatch.
 */
class AValueVTable(
    // Common AValue fields.
    val staticTypeOfValue: ConstTypeId,
    val starlarkTypeId: StarlarkTypeId,
    val typeName: String,
    /** Cache `typeName` here to avoid computing hash (mirrors Rust `typeAsAllocativeKey`). */
    val typeAsAllocativeKey: String = typeName,

    // AValue
    val isStr: Boolean,
    internal val memorySizeFn: (StarlarkValueRawPtr) -> ValueAllocSize,
    internal val heapFreezeFn: (StarlarkValueRawPtr, Freezer) -> Result<FrozenValue>,
    internal val heapCopyFn: (StarlarkValueRawPtr, Tracer) -> Value,

    // StarlarkValue dispatch
    internal val starlarkValue: StarlarkValue,

    // Drop
    private val dropInPlaceFn: (StarlarkValueRawPtr) -> Unit = { value ->
        val p = value.ptr
        if (p is AutoCloseable) {
            p.close()
        }
    },

    /**
     * Capability flags indicating which [StarlarkValue] interface methods are
     * meaningfully implemented for this vtable.
     */
    val hasInvoke: Boolean = false,
    val hasEvalType: Boolean = false,
    val hasIterate: Boolean = false,
    val hasEquals: Boolean = false,

    // Display/Debug
    private val displayFn: (StarlarkValueRawPtr) -> Any = { it.ptr },
    private val debugFn: (StarlarkValueRawPtr) -> Any = { it.ptr },
    private val erasedSerdeSerializeFn: (StarlarkValueRawPtr) -> Any = { error("unreachable") },
    private val allocativeFn: (StarlarkValueRawPtr) -> Any = { it.ptr },
    private val totalMemoryForProfileFn: (StarlarkValueRawPtr) -> UInt = { p ->
        memorySizeFn(p).bytes()
    },
) {
    companion object {
        fun newBlackHole(): AValueVTable {
            val BLACKHOLE_ALLOCATIVE_KEY = "BlackHole"
            val BLACKHOLE_TYPE_ID = ConstTypeId.of<BlackHole>()
            val BLACKHOLE_STARLARK_TYPE_ID = StarlarkTypeId.fromTypeId(BLACKHOLE_TYPE_ID)
            return AValueVTable(
                staticTypeOfValue = BLACKHOLE_TYPE_ID,
                starlarkTypeId = BLACKHOLE_STARLARK_TYPE_ID,
                typeName = "BlackHole",
                typeAsAllocativeKey = BLACKHOLE_ALLOCATIVE_KEY,
                isStr = false,
                dropInPlaceFn = { _ -> },
                memorySizeFn = { p ->
                    val thisValue = p.valueRef<BlackHole>()
                    thisValue.size
                },
                heapFreezeFn = { _, _ -> error("BlackHole") },
                heapCopyFn = { _, _ -> error("BlackHole") },
                starlarkValue = object : StarlarkValue {
                    override val TYPE: String get() = "BlackHole"
                },
                displayFn = { thisPtr -> thisPtr.valueRef<BlackHole>() },
                debugFn = { thisPtr -> thisPtr.valueRef<BlackHole>() },
                erasedSerdeSerializeFn = { _this -> error("unreachable") },
                allocativeFn = { thisPtr -> thisPtr.valueRef<BlackHole>() },
                totalMemoryForProfileFn = { thisPtr ->
                    val p = thisPtr.valueRef<BlackHole>()
                    p.size.bytes()
                },
            )
        }

        /**
         * Public for use by simple-frozen vtable registration in doctests.
         * Hidden from docs and uses a private [AValue] bound to prevent direct external use.
         */
        internal inline fun <reified T> new(): AValueVTable where T : StarlarkValue, T : AValue {
            val typeId = ConstTypeId.of<T>()
            val starlarkTypeId = StarlarkTypeId.fromTypeId(typeId)
            val typeName = T::class.simpleName ?: T::class.toString()
            val TYPE_ID = typeId
            val STARLARK_TYPE_ID = starlarkTypeId
            val ALLOCATIVE_KEY = typeName

            val dropInPlace: (StarlarkValueRawPtr) -> Unit = { p ->
                val p0 = p.valuePtr<T>()
                AValueRepr.fromPayloadPtrMut(p0)
                Unit
            }
            val memorySize = { p: StarlarkValueRawPtr ->
                val p0 = p.valueRef<T>()
                p0.allocSizeForExtraLen(p0.extraLen(p0))
            }
            val heapFreeze = { p: StarlarkValueRawPtr, freezer: Freezer ->
                val p0 = p.valueRef<T>()
                AValueRepr.fromPayloadPtrMut(p0)
                p0.heapFreeze(freezer)
            }
            val heapCopy = { p: StarlarkValueRawPtr, tracer: Tracer ->
                val p0 = p.valueRef<T>()
                AValueRepr.fromPayloadPtrMut(p0)
                p0.heapCopy(tracer)
            }
            val display = { thisPtr: StarlarkValueRawPtr ->
                val thisValue = thisPtr.valuePtr<T>()
                thisValue
            }
            val debug = { thisPtr: StarlarkValueRawPtr ->
                val thisValue = thisPtr.valuePtr<T>()
                thisValue
            }
            val erasedSerdeSerialize = { thisPtr: StarlarkValueRawPtr ->
                val thisValue = thisPtr.valuePtr<T>()
                thisValue
            }
            val allocative = { thisPtr: StarlarkValueRawPtr ->
                val thisValue = thisPtr.valuePtr<T>()
                thisValue
            }
            val totalMemoryForProfile = { thisPtr: StarlarkValueRawPtr ->
                val p = thisPtr.valueRef<T>()
                p.totalMemoryForProfile(p).toUInt()
            }
            return AValueVTable(
                staticTypeOfValue = TYPE_ID,
                starlarkTypeId = STARLARK_TYPE_ID,
                typeName = typeName,
                typeAsAllocativeKey = ALLOCATIVE_KEY,
                isStr = false,
                dropInPlaceFn = dropInPlace,
                memorySizeFn = memorySize,
                heapFreezeFn = heapFreeze,
                heapCopyFn = heapCopy,
                starlarkValue = object : StarlarkValue {
                    override val TYPE: String get() = typeName
                },
                displayFn = display,
                debugFn = debug,
                erasedSerdeSerializeFn = erasedSerdeSerialize,
                allocativeFn = allocative,
                totalMemoryForProfileFn = totalMemoryForProfile,
            )
        }

        /**
         * Kotlin-only extension: build an [AValueVTable] for a runtime [KClass].
         * Has no Rust counterpart — Rust uses the const-generic `new<T>()` above.
         * Used by the pagable vtable registry, which reconstructs vtables from
         * a deserialized type id rather than a static type parameter.
         */
        fun forType(type: KClass<*>): AValueVTable {
            val typeId = ConstTypeId.of(type)
            val typeName = type.simpleName ?: type.toString()
            return AValueVTable(
                staticTypeOfValue = typeId,
                starlarkTypeId = StarlarkTypeId.fromTypeId(typeId),
                typeName = typeName,
                typeAsAllocativeKey = typeName,
                isStr = false,
                memorySizeFn = { _ -> ValueAllocSize.new(AlignedSize.newBytes(16)) },
                heapFreezeFn = { _, _ -> error("forType: heapFreeze not supported for $typeName") },
                heapCopyFn = { _, _ -> error("forType: heapCopy not supported for $typeName") },
                starlarkValue = object : StarlarkValue {
                    override val TYPE: String get() = typeName
                },
            )
        }
    }

    fun typeValue(): FrozenStringValue {
        return starlarkValue.getTypeValueStatic()
    }

    fun typeStarlarkRepr(): Ty {
        return starlarkValue.getTypeStarlarkRepr()
    }

    fun methods(): Methods? {
        return starlarkValue.getMethods()
    }

    /**
     * Drop the value in-place, mirroring Rust's `AValueVTable::dropInPlace`.
     */
    fun dropInPlace(value: StarlarkValueRawPtr) {
        dropInPlaceFn(value)
    }

    /**
     * Create an AValueDyn from this vtable.
     * Used by AValueHeader.unpack() to create a dynamic dispatch reference.
     */
    internal fun unpackDyn(): AValueDyn {
        return AValueDyn(StarlarkValueRawPtr(starlarkValue), this)
    }
}

/**
 * A dynamically dispatched reference to a Starlark value paired with its vtable.
 *
 * Wraps a [StarlarkValue] reference and its vtable metadata, providing forwarding
 * methods that delegate to the vtable or to the [StarlarkValue] interface.
 */
internal class AValueDyn(
    internal val value: StarlarkValueRawPtr,
    private val _vtable: AValueVTable,
) {
    companion object {
        fun new(value: StarlarkValueRawPtr, vtable: AValueVTable): AValueDyn {
            return AValueDyn(value = value, _vtable = vtable)
        }
    }

    fun vtable(): AValueVTable = _vtable

    fun memorySize(): ValueAllocSize {
        return _vtable.memorySizeFn(value)
    }

    /**
     * Allocative-trait reference. Kotlin has no equivalent trait, so the
     * underlying value is returned for callers to inspect via [kotlin.reflect].
     */
    fun asAllocative(): Any {
        val value = this.value
        return value.valueRef<StarlarkValue>()
    }

    /**
     * Total bytes attributed to this value when building a heap profile.
     * Defers to the [memorySize] accessor and converts to bytes.
     */
    fun totalMemoryForProfile(): UInt = _vtable.memorySizeFn(value).bytes()

    /**
     * Serializable view. Returns the underlying value for kotlinx.serialization
     * to dispatch on at the JSON path.
     */
    fun asSerialize(): Any {
        val value = this.value
        return value.valueRef<StarlarkValue>()
    }

    fun heapFreeze(freezer: Freezer): Result<FrozenValue> {
        return _vtable.heapFreezeFn(value, freezer)
    }

    fun heapCopy(tracer: Tracer): Value {
        return _vtable.heapCopyFn(value, tracer)
    }

    fun documentation(): DocItem {
        return starlarkValue().documentation()
    }

    fun typecheckerTy(): Ty? {
        return starlarkValue().typecheckerTy()
    }

    fun evalType(): Ty? {
        return starlarkValue().evalType()
    }

    fun at(index: Value, heap: Heap): Result<Value> {
        return starlarkValue().at(index, heap)
    }

    fun at2(index0: Value, index1: Value, heap: Heap): Result<Value> {
        return starlarkValue().at2(index0, index1, heap)
    }

    fun isIn(collection: Value): Result<Boolean> {
        return starlarkValue().isIn(collection)
    }

    fun slice(start: Value?, stop: Value?, step: Value?, heap: Heap): Result<Value> {
        return starlarkValue().slice(start, stop, step, heap)
    }

    fun getAttr(name: String, heap: Heap): Value? {
        return starlarkValue().getAttr(name, heap)
    }

    fun getAttrHashed(name: Hashed<String>, heap: Heap): Value? {
        return starlarkValue().getAttrHashed(name, heap)
    }

    fun hasAttr(name: String, heap: Heap): Boolean {
        return starlarkValue().hasAttr(name, heap)
    }

    fun dirAttr(): List<String> {
        return starlarkValue().dirAttr()
    }

    fun bitAnd(other: Value, heap: Heap): Result<Value> {
        return starlarkValue().bitAnd(other, heap)
    }

    fun bitOr(other: Value, heap: Heap): Result<Value> {
        return starlarkValue().bitOr(other, heap)
    }

    fun bitXor(other: Value, heap: Heap): Result<Value> {
        return starlarkValue().bitXor(other, heap)
    }

    fun bitNot(heap: Heap): Result<Value> {
        return starlarkValue().bitNot(heap)
    }

    fun toBool(): Boolean {
        return starlarkValue().toBool()
    }

    fun length(): Result<Int> {
        return starlarkValue().length()
    }

    fun iterate(me: Value, heap: Heap): Result<Value> {
        return starlarkValue().iterate(me, heap)
    }

    fun iterNext(index: Int, heap: Heap): Value? {
        return starlarkValue().iterNext(index, heap)
    }

    fun iterSizeHint(index: Int): Pair<Int, Int?> {
        return starlarkValue().iterSizeHint(index)
    }

    fun iterStop() {
        starlarkValue().iterStop()
    }

    fun getHash(): Result<StarlarkHashValue> {
        return starlarkValue().getHash()
    }

    fun plus(heap: Heap): Result<Value> {
        return starlarkValue().plus(heap)
    }

    fun minus(heap: Heap): Result<Value> {
        return starlarkValue().minus(heap)
    }

    fun add(other: Value, heap: Heap): Result<Value>? {
        return starlarkValue().add(other, heap)
    }

    fun radd(other: Value, heap: Heap): Result<Value>? {
        return starlarkValue().radd(other, heap)
    }

    fun sub(other: Value, heap: Heap): Result<Value> {
        return starlarkValue().sub(other, heap)
    }

    fun mul(other: Value, heap: Heap): Result<Value>? {
        return starlarkValue().mul(other, heap)
    }

    fun rmul(other: Value, heap: Heap): Result<Value>? {
        return starlarkValue().rmul(other, heap)
    }

    fun div(other: Value, heap: Heap): Result<Value> {
        return starlarkValue().div(other, heap)
    }

    fun floorDiv(other: Value, heap: Heap): Result<Value> {
        return starlarkValue().floorDiv(other, heap)
    }

    fun percent(other: Value, heap: Heap): Result<Value> {
        return starlarkValue().percent(other, heap)
    }

    fun leftShift(other: Value, heap: Heap): Result<Value> {
        return starlarkValue().leftShift(other, heap)
    }

    fun rightShift(other: Value, heap: Heap): Result<Value> {
        return starlarkValue().rightShift(other, heap)
    }

    fun collectRepr(collector: StringBuilder) {
        starlarkValue().collectRepr(collector)
    }

    fun collectReprCycle(collector: StringBuilder) {
        starlarkValue().collectReprCycle(collector)
    }

    inline fun <reified T : StarlarkValue> downcastRef(): T? {
        val expectedTypeId = ConstTypeId.of<T>()
        if (_vtable.staticTypeOfValue != expectedTypeId) {
            return null
        }
        return value.valueRef()
    }

    fun <T : StarlarkValue> downcastRef(clazz: kotlin.reflect.KClass<T>): T? {
        val expectedTypeId = ConstTypeId.of(clazz)
        if (_vtable.staticTypeOfValue != expectedTypeId) {
            return null
        }
        return clazz.safeCast(value.ptr)
    }

    fun equals(other: Value): Result<Boolean> {
        return starlarkValue().equals(other)
    }

    fun compare(other: Value): Result<Int> {
        return starlarkValue().compare(other)
    }

    fun nameForCallStack(me: Value): String {
        return starlarkValue().nameForCallStack(me)
    }

    fun exportAs(variableName: String, eval: Evaluator): Result<Unit> {
        return starlarkValue().exportAs(variableName, eval)
    }

    fun setAt(index: Value, newValue: Value): Result<Unit> {
        return starlarkValue().setAt(index, newValue)
    }

    fun setAttr(attribute: String, newValue: Value): Result<Unit> {
        return starlarkValue().setAttr(attribute, newValue)
    }

    fun writeHash(hasher: StarlarkHasher): Result<Unit> {
        return starlarkValue().writeHash(hasher)
    }

    fun typeMatchesValue(value: Value): Boolean {
        return starlarkValue().typeMatchesValue(value)
    }

    fun asDisplay(): Any {
        val value = this.value
        return value.valueRef<StarlarkValue>()
    }

    fun asDebug(): Any {
        val value = this.value
        return value.valueRef<StarlarkValue>()
    }

    fun provide(demand: Demand) {
        starlarkValue().provide(demand)
    }

    private fun starlarkValue(): StarlarkValue {
        return value.valueRef()
    }

    fun fmt(): String {
        val debugStruct = "AValueDyn"
        return "$debugStruct { .. }"
    }

    override fun toString(): String = fmt()
}

/** Raw pointer, vtable and [Value]. */
internal class AValueDynFull(
    private val avalue: AValueDyn,
    val value: Value,
) {
    companion object {
        fun new(avalue: AValueDyn, value: Value): AValueDynFull {
            return AValueDynFull(avalue = avalue, value = value)
        }
    }

    fun invoke(
        args: Arguments,
        eval: Evaluator,
    ): Result<Value> {
        val sv: StarlarkValue = avalue.value.valueRef()
        return sv.invoke(value, args, eval)
    }
}

// BlackHole is defined in Avalue.kt (same package)
