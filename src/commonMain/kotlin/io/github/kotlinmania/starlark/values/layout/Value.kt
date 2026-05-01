// port-lint: source values/layout/value.rs
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

// Possible optimisations:
// Avoid the wrapper duplication
// Encode Int in the pointer too

// We import pointer tagging on the bottom two bits:
// 00 => this Value pointer is actually a FrozenValue pointer
// 01 => this is a real Value pointer
// 11 => this is a bool (next bit: 1 => true, 0 => false)
// 10 => this is a None
//
// We do not use pointer tagging for Int (although we would like to), because
// our getRef requires a pointer to the value. We need to put that pointer
// somewhere. The solution is to have a separate value storage vs vtable.

import io.github.kotlinmania.starlarkmap.Hashed
import io.github.kotlinmania.starlarkmap.StarlarkHashValue
import io.github.kotlinmania.starlarkmap.StarlarkHasher
import io.github.kotlinmania.starlark.docs.DocItem
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.typing.TyCallable
import io.github.kotlinmania.starlark.typing.ParamIsRequired
import io.github.kotlinmania.starlark.typing.ParamSpec
import io.github.kotlinmania.starlark.values.FrozenRef
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.ValueError
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.layout.heap.AValueOrForwardUnpack
import io.github.kotlinmania.starlark.values.layout.heap.AValueHeader
import io.github.kotlinmania.starlark.values.layout.heap.AValueRepr
import io.github.kotlinmania.starlark.values.layout.heap.Tracer
import io.github.kotlinmania.starlark.values.layout.AValue
import io.github.kotlinmania.starlark.values.layout.AValueImpl
import io.github.kotlinmania.starlark.values.layout.FrozenPointer
import io.github.kotlinmania.starlark.values.layout.Pointer
import io.github.kotlinmania.starlark.values.layout.RawPointer
import io.github.kotlinmania.starlark.values.layout.ValueLifetimeless
import io.github.kotlinmania.starlark.values.layout.typed.FrozenStringValue
import io.github.kotlinmania.starlark.eval.runtime.Evaluator
import io.github.kotlinmania.starlark.eval.runtime.Arguments
import io.github.kotlinmania.starlark.values.layout.typed.StringValue
import io.github.kotlinmania.starlark.values.types.string.StarlarkStr
import io.github.kotlinmania.starlark.values.types.int.PointerI32
import io.github.kotlinmania.starlark.values.types.int.InlineInt
import io.github.kotlinmania.starlark.values.types.num.NumRef
import io.github.kotlinmania.starlark.values.types.int.StarlarkIntRef
import io.github.kotlinmania.starlark.values.starlarktypeid.StarlarkTypeId
import io.github.kotlinmania.starlark.values.layout.Freezer
import io.github.kotlinmania.starlark.values.StarlarkIterator
import io.github.kotlinmania.starlark.values.stackGuard
import io.github.kotlinmania.starlark.values.reprStackPush
import io.github.kotlinmania.starlark.values.jsonStackPush
import io.github.kotlinmania.starlark.values.types.FUNCTION_TYPE
import io.github.kotlinmania.starlark.values.demand.requestValueImpl
import io.github.kotlinmania.starlark.eval.compiler.DefGen
import io.github.kotlinmania.starlark.eval.runtime.FrameSpan
import io.github.kotlinmania.starlark.eval.runtime.ArgumentsFull
import io.github.kotlinmania.starlark.eval.runtime.params.spec.ParametersSpec
import io.github.kotlinmania.starlark.eval.runtime.params.spec.asValue
import io.github.kotlinmania.starlark.collections.symbol.Symbol
import io.github.kotlinmania.starlark.values.types.NativeFunction
import io.github.kotlinmania.starlark.values.types.BoundMethodGen
import io.github.kotlinmania.starlark.values.types.list.FrozenListData
import io.github.kotlinmania.starlark.values.types.dict.FrozenDictRef
import io.github.kotlinmania.starlark.values.types.tuple.TupleGen
import io.github.kotlinmania.starlark.values.types.range.Range
import io.github.kotlinmania.starlark.values.types.record.RecordGen
import io.github.kotlinmania.starlark.values.types.record.recordtype.RecordTypeGen
import io.github.kotlinmania.starlark.values.types.enumeration.enumtype.EnumTypeGen
import io.github.kotlinmania.starlark.values.types.enumeration.value.EnumValueGen
import io.github.kotlinmania.starlark.values.types.structs.StructGen
import io.github.kotlinmania.starlark.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark.values.layout.typed.StringValueLike
import io.github.kotlinmania.starlark.CoerceKey
import io.github.kotlinmania.starlark.values.Trace
import io.github.kotlinmania.starlark.util.ArcStr
import io.github.kotlinmania.starlark.values.types.float.StarlarkFloat
import io.github.kotlinmania.starlark.values.types.none.VALUE_NONE
import io.github.kotlinmania.starlark.values.types.none.NoneType
import io.github.kotlinmania.starlark.values.types.bool.VALUE_FALSE_TRUE
import io.github.kotlinmania.starlark.values.layout.avalues.str.allocStrConcat
// VALUE_EMPTY_STRING is in the same package (values.layout) via StaticString.kt
import io.github.kotlinmania.starlark.values.types.tuple.VALUE_EMPTY_TUPLE
import io.github.kotlinmania.starlark.values.types.list.VALUE_EMPTY_FROZEN_LIST
import io.github.kotlinmania.starlark.values.types.dict.VALUE_EMPTY_FROZEN_DICT
import io.github.kotlinmania.starlark.values.types.dict.DictGen
import io.github.kotlinmania.starlark.values.types.dict.DictLike
import io.github.kotlinmania.starlark.values.types.list.ListGen
import io.github.kotlinmania.starlark.values.types.list.ListLike
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

// We already import another `ValueError`, hence the odd name.
private class ValueValueError {
    class WrongType(val expectedType: String, val actualType: String) :
        Exception("Expected value of type `$expectedType` but got `$actualType`")
}

/**
 * Integer value is too big to fit in the target type.
 *
 * @property integerType The name of the target integer type.
 * @property value The string representation of the value that was too big.
 */
class IntegerTooBigError(
    val integerType: String,
    val value: String,
) : Exception("Integer value is too big to fit in $integerType: $value")

/**
 * Cycle detected when serializing value to JSON.
 */
private class ToJsonCycleError(val typeName: String) :
    Exception("Cycle detected when serializing value of type `$typeName` to JSON")

private fun debugValue(typ: String, v: Value): String {
    // When value is being moved during GC or freeze,
    // `Value` pointee is not a proper value, but a GC-related information.
    // Regular operations like `.toRepr()` crash, but `Debug` should work.
    val ptrOpt = v.ptr.unpackPtrOpt()
    if (ptrOpt != null) {
        try {
            AValueHeader.fromIndex(ptrOpt)
        } catch (_: Exception) {
            // Pointer is not a valid header (could be forward during GC)
            return "$typ(Forward($ptrOpt))"
        }
    }
    return "$typ(${v.getRef().asDebug()})"
}

/**
 * A Starlark value, stored on a [Heap].
 *
 * Many of the methods simply forward to the underlying [StarlarkValue].
 * The [toString] method is equivalent to the `repr()` function in Starlark.
 */
class Value internal constructor(
    internal var ptr: Pointer,
) : ValueLike<Value> {
    companion object {
        /**
         * Create a new [Value] from an [AValueHeader] pointer.
         */
        internal fun newPtr(x: AValueHeader, isStr: Boolean): Value {
            return Value(Pointer.newUnfrozen(x.index, isStr))
        }

        /**
         * Create a new [Value] from an [AValueHeader], querying whether it is a string.
         */
        internal fun newPtrQueryIsStr(x: AValueHeader): Value {
            val isString = x.vtable.isStr
            return newPtr(x, isString)
        }

        /**
         * Create a new [Value] from an [AValueRepr].
         */
        internal fun <T : AValue> newRepr(x: AValueRepr<AValueImpl<T>>): Value {
            return newPtr(x.header, x.header.vtable.isStr)
        }

        internal fun newPtrUsizeWithStrTag(x: Long): Value {
            return Value(Pointer.newUnfrozenUsizeWithStrTag(x))
        }

        /**
         * Create a new int for testing purposes.
         */
        fun testingNewInt(x: Int): Value {
            return FrozenValue.testingNewInt(x).toValue()
        }

        /**
         * Create a new `None` value.
         */
        fun newNone(): Value {
            return FrozenValue.newNone().toValue()
        }

        /**
         * Create a new boolean value.
         */
        fun newBool(x: Boolean): Value {
            return FrozenValue.newBool(x).toValue()
        }

        /**
         * Create a new integer value.
         */
        internal fun newInt(x: InlineInt): Value {
            return FrozenValue.newInt(x).toValue()
        }

        /**
         * Create a new blank string value.
         */
        internal fun newEmptyString(): Value {
            return FrozenValue.newEmptyString().toValue()
        }

        /**
         * Create a new empty tuple value.
         */
        internal fun newEmptyTuple(): Value {
            return FrozenValue.newEmptyTuple().toValue()
        }

        /**
         * Turn a [FrozenValue] into a [Value]. See the safety warnings on
         * [OwnedFrozenValue].
         */
        fun newFrozen(x: FrozenValue): Value {
            // Safe if every FrozenValue must have had a reference added to its heap first.
            // That property is NOT statically checked.
            return Value(x.ptr.toPointer())
        }

        /**
         * Convert from [FrozenValue] (ValueLike factory method).
         */
        fun fromFrozenValue(v: FrozenValue): Value = v.toValue()
    }

    internal fun castLifetime(): Value = Value(this.ptr.castLifetime())

    /**
     * Produce a [Value] regardless of the type you are starting with.
     * For [Value], simply returns itself.
     */
    override fun toValue(): Value = this

    /**
     * Obtain the underlying [FrozenValue] from inside the [Value], if it is one.
     */
    fun unpackFrozen(): FrozenValue? {
        return if (ptr.isUnfrozen()) {
            null
        } else {
            // SAFETY: We have just checked the value is frozen.
            unpackFrozenUnchecked()
        }
    }

    private fun unpackFrozenUnchecked(): FrozenValue {
        return FrozenValue(ptr.castLifetime().toFrozenPointerUnchecked())
    }

    /**
     * Is this value `None`.
     */
    fun isNone(): Boolean {
        return ptrEq(newNone())
    }

    /**
     * Obtain the underlying numerical value, if it is one.
     */
    internal fun unpackNum(): NumRef? {
        val int = StarlarkIntRef.unpack(this)
        if (int != null) {
            return NumRef.Int(int)
        }
        val float = downcastRef<StarlarkFloat>()
        if (float != null) {
            return NumRef.Float(float)
        }
        return null
    }

    /**
     * Unpack this value as an integer of type [Long], or return an error if it is too big.
     * Returns `null` if the value is not an integer at all.
     */
    internal fun unpackInteger(): Result<Long?> {
        val num = StarlarkIntRef.unpackValueOpt(this) ?: return Result.success(null)
        return when (num) {
            is StarlarkIntRef.Small -> {
                val i32 = num.toI32()
                if (i32 != null) {
                    Result.success(i32.toLong())
                } else {
                    // Small value that does not fit in i32 should not normally happen,
                    // but fall through to error.
                    Result.failure(IntegerTooBigError(
                        integerType = "Long",
                        value = num.toString(),
                    ))
                }
            }
            is StarlarkIntRef.Big -> {
                return try {
                    Result.success(num.value.get().longValue(exactRequired = true))
                } catch (_: Exception) {
                    Result.failure(IntegerTooBigError(
                        integerType = "Long",
                        value = num.toString(),
                    ))
                }
            }
        }
    }

    /**
     * Obtain the underlying `bool` if it is a boolean.
     */
    fun unpackBool(): Boolean? {
        return if (ptrEq(newBool(true))) {
            true
        } else if (ptrEq(newBool(false))) {
            false
        } else {
            null
        }
    }

    /**
     * Obtain the underlying integer if it fits in an `Int`.
     * Note floats are not considered integers, i.e. `unpackI32` for `1.0` will return `null`.
     */
    fun unpackI32(): Int? {
        return if (InlineInt.smallerThanI32()) {
            StarlarkIntRef.unpack(this)?.toI32()
        } else {
            unpackInlineInt()?.toI32()
        }
    }

    /**
     * Unpack inline integer value.
     */
    internal fun unpackInlineInt(): InlineInt? {
        return ptr.unpackInt()?.let { InlineInt(it) }
    }

    /**
     * Unpack int value as a FrozenValueTyped PointerI32.
     */
    internal fun unpackIntValue(): FrozenValueTyped<PointerI32>? {
        return if (unpackInlineInt() != null) {
            // SAFETY: We have just checked the value is an int.
            FrozenValueTyped.newUnchecked(unpackFrozenUnchecked())
        } else {
            null
        }
    }

    /**
     * Check if this value is a string.
     */
    internal fun isStr(): Boolean {
        return ptr.isStr()
    }

    /**
     * Like [unpackStr], but returns the wrapping [StarlarkStr] object instead of the
     * underlying [String]. Mostly useful for callers that need to dispatch on
     * [StarlarkValue] methods through the Starlark string wrapper.
     *
     * Unstable and likely to be removed in future, as the presence of the wrapping
     * [StarlarkStr] is not a guaranteed part of the API.
     */
    fun unpackStarlarkStr(): StarlarkStr? {
        return if (isStr()) {
            getRef().downcastRef<StarlarkStr>()
        } else {
            null
        }
    }

    /**
     * Obtain the underlying `str` if it is a string.
     */
    fun unpackStr(): String? {
        return unpackStarlarkStr()?.asStr()
    }

    /**
     * Obtain the underlying `str` if it is a string, otherwise return an error for users.
     */
    fun unpackStrErr(): Result<String> = unpackStr()?.let { Result.success(it) }
        ?: Result.failure(IllegalArgumentException("Expected value of type `string` but got `${toStringForTypeError()}`"))

    /**
     * Get a pointer to an [AValue].
     */
    @PublishedApi
    internal fun getRef(): AValueDyn {
        return if (ptr.unpackIsInt()) {
            val intVal = ptr.unpackIntValue()
            AValueDyn(
                StarlarkValueRawPtr(PointerI32(intVal)),
                PointerI32.vtable(),
            )
        } else {
            val ptrIndex = ptr.unpackPtr()
            val header = AValueHeader.fromIndex(ptrIndex)
            header.unpack()
        }
    }

    /**
     * Get the full reference including the value itself.
     */
    private fun getRefFull(): AValueDynFull {
        return AValueDynFull(getRef(), this)
    }

    /**
     * Get the vtable for this value.
     */
    internal fun vtable(): AValueVTable {
        return if (ptr.unpackIsInt()) {
            PointerI32.vtable()
        } else {
            val ptrIndex = ptr.unpackPtr()
            AValueHeader.fromIndex(ptrIndex).vtable
        }
    }

    /**
     * Get the raw underlying pointer from the AValueDyn of this value.
     * Used by inline functions that need to access the underlying object
     * from a different package (e.g. ValueOf.unpackValueImpl).
     */
    @PublishedApi
    internal fun getUnderlyingPtr(): Any {
        return getRef().value.ptr
    }

    /**
     * Downcast without checking the value type.
     */
    internal inline fun <reified T : StarlarkValue> downcastRefUnchecked(): T {
        assert(getRef().downcastRef<T>() != null)
        if (PointerI32.typeIsPointerI32<T>()) {
            val pi32 = PointerI32(ptr.unpackIntValue())
            check(pi32 is T)
            return pi32
        }
        return getRef().downcastRef<T>()!!
    }

    /**
     * Get the hash value for this value.
     */
    internal fun getHash(): Result<StarlarkHashValue> {
        return getRef().getHash()
    }

    /**
     * Are two [Value]s equal, looking at only their underlying pointer. This function is
     * low-level and provides two guarantees.
     *
     * 1. It is _reflexive_, the same [Value] passed as both arguments will result in `true`.
     * 2. If this function is `true`, then [Value.equals] will also consider them equal.
     *
     * Note that other properties are not guaranteed, and the result is not considered part of the API.
     * The result can be impacted by optimisations such as hash-consing, copy-on-write, partial
     * evaluation etc.
     */
    fun ptrEq(other: Value): Boolean {
        return ptr.ptrEq(other.ptr)
    }

    /**
     * Returns an identity for this [Value], derived from its pointer. This function is
     * low-level and provides two guarantees. Those are valid until the next GC:
     *
     * 1. Calling it multiple times on the same [Value] will return [ValueIdentity] that
     *    compare equal.
     * 2. If two [Value]s have [ValueIdentity] that compare equal, then [Value.ptrEq] and
     *    [Value.equals] will also consider them to be equal.
     */
    fun identity(): ValueIdentity {
        return ValueIdentity.new(this)
    }

    /**
     * Get the underlying pointer.
     * Should be done sparingly as it slightly breaks the abstraction.
     * Most useful as a hash key based on pointer.
     * For external users, [Value.identity] returns an opaque [ValueIdentity] that makes fewer
     * guarantees.
     */
    internal fun ptrValue(): RawPointer {
        return ptr.raw()
    }

    /**
     * `type(x)`.
     */
    fun getType(): String {
        return vtable().typeName
    }

    /**
     * `bool(x)`.
     */
    fun toBool(): Boolean {
        // Fast path for the common case
        val b = unpackBool()
        return if (b != null) {
            b
        } else {
            getRef().toBool()
        }
    }

    /**
     * `x[index]`.
     */
    fun at(index: Value, heap: Heap): Result<Value> {
        return getRef().at(index, heap)
    }

    /**
     * `x[start:stop:stride]`.
     */
    fun slice(start: Value?, stop: Value?, stride: Value?, heap: Heap): Result<Value> {
        return getRef().slice(start, stop, stride, heap)
    }

    /**
     * `len(x)`.
     */
    fun length(): Result<Int> {
        return getRef().length()
    }

    /**
     * `other in x`.
     */
    fun isIn(other: Value): Result<Boolean> {
        return getRef().isIn(other)
    }

    /**
     * `+x`.
     */
    fun plus(heap: Heap): Result<Value> {
        return getRef().plus(heap)
    }

    /**
     * `-x`.
     */
    fun minus(heap: Heap): Result<Value> {
        return getRef().minus(heap)
    }

    /**
     * `x - other`.
     */
    fun sub(other: Value, heap: Heap): Result<Value> {
        return getRef().sub(other, heap)
    }

    /**
     * `x * other`.
     */
    fun mul(other: Value, heap: Heap): Result<Value> {
        return when (val result = getRef().mul(other, heap)) {
            null -> when (val rresult = other.getRef().rmul(this, heap)) {
                null -> ValueError.unsupportedOwned(getType(), "*", other.getType())
                else -> rresult
            }
            else -> result
        }
    }

    /**
     * `x % other`.
     */
    fun percent(other: Value, heap: Heap): Result<Value> {
        return getRef().percent(other, heap)
    }

    /**
     * `x / other`.
     */
    fun div(other: Value, heap: Heap): Result<Value> {
        return getRef().div(other, heap)
    }

    /**
     * `x // other`.
     */
    fun floorDiv(other: Value, heap: Heap): Result<Value> {
        return getRef().floorDiv(other, heap)
    }

    /**
     * `x & other`.
     */
    fun bitAnd(other: Value, heap: Heap): Result<Value> {
        return getRef().bitAnd(other, heap)
    }

    /**
     * `x | other`.
     */
    fun bitOr(other: Value, heap: Heap): Result<Value> {
        return getRef().bitOr(other, heap)
    }

    /**
     * `x ^ other`.
     */
    fun bitXor(other: Value, heap: Heap): Result<Value> {
        return getRef().bitXor(other, heap)
    }

    /**
     * `~x`.
     */
    fun bitNot(heap: Heap): Result<Value> {
        return getRef().bitNot(heap)
    }

    /**
     * `x << other`.
     */
    fun leftShift(other: Value, heap: Heap): Result<Value> {
        return getRef().leftShift(other, heap)
    }

    /**
     * `x >> other`.
     */
    fun rightShift(other: Value, heap: Heap): Result<Value> {
        return getRef().rightShift(other, heap)
    }

    /**
     * Invoke with a call stack location.
     */
    internal fun invokeWithLoc(
        location: FrozenRef<FrameSpan>?,
        args: Arguments,
        eval: Evaluator,
    ): Result<Value> {
        return eval.withCallStack(this, location) { e ->
            getRefFull().invoke(args, e)
        }
    }

    /**
     * Callable parameters if known.
     *
     * For now it only returns parameter spec for `def` and `lambda`.
     */
    fun parametersSpec(): ParametersSpec<Value>? {
        val def = downcastRef<DefGen<Value>>()
        if (def != null) {
            return def.parameters
        }
        val frozenDef = downcastRef<DefGen<FrozenValue>>()
        if (frozenDef != null) {
            return frozenDef.parameters.asValue()
        }
        return null
    }

    /**
     * Invoke self with given arguments.
     */
    override fun invoke(
        args: Arguments,
        eval: Evaluator,
    ): Result<Value> {
        return invokeWithLoc(null, args, eval)
    }

    /**
     * Invoke a function with only positional arguments.
     */
    internal fun invokePos(
        pos: List<Value>,
        eval: Evaluator,
    ): Result<Value> {
        val params = Arguments(ArgumentsFull<Symbol>(pos = pos))
        return invoke(params, eval)
    }

    /**
     * Check if this value is callable.
     */
    private fun checkCallable(): Result<Unit> {
        if (!vtable().hasInvoke) {
            return Result.failure(
                IllegalStateException("Value is not callable: ${toStringForTypeError()}")
            )
        }
        return Result.success(Unit)
    }

    /**
     * Check this value can be "called" with given parameter types, and provided return type.
     *
     * This check is done optimistically: when it is not known
     * whether the value is compatible with given arguments, return `Ok(())`.
     *
     * This operation is expensive.
     */
    fun checkCallableWith(
        pos: Iterable<Ty>,
        named: Iterable<Pair<String, Ty>>,
        args: Ty?,
        kwargs: Ty?,
        ret: Ty,
    ): Result<Unit> {
        val posList = pos.toList()
        val namedList = named.toList()
        return checkCallableWithImpl(posList, namedList, args, kwargs, ret)
    }

    private fun checkCallableWithImpl(
        pos: List<Ty>,
        named: List<Pair<String, Ty>>,
        args: Ty?,
        kwargs: Ty?,
        ret: Ty,
    ): Result<Unit> {
        // First, provide a good error message when the value is not callable
        // without invoking a typechecker.
        try {
            checkCallable().getOrThrow()
        } catch (e: Exception) {
            return Result.failure(e)
        }

        val paramSpec = try {
            ParamSpec.newParts(
                pos.map { ty -> Pair(ParamIsRequired.Yes, ty) },
                emptyList(),
                args,
                named.map { (n, ty) -> Triple(n, ParamIsRequired.Yes, ty) },
                kwargs,
            )
        } catch (e: Exception) {
            return Result.failure(e)
        }

        val sig = TyCallable.new(paramSpec, ret)

        // Ty.ofValue: import typechecker type if available, else type repr
        val ty = getRef().typecheckerTy() ?: getTypeStarlarkRepr()
        if (!ty.checkCall(
                pos,
                named.map { (n, ty) -> Pair(n, ty) },
                args,
                kwargs,
                ret,
            )
        ) {
            return Result.failure(
                IllegalStateException(
                    "Value `${toStringForTypeError()}` is not compatible with the signature `$sig`"
                )
            )
        }

        return Result.success(Unit)
    }

    /**
     * `type(x)` as a [FrozenStringValue].
     */
    fun getTypeValue(): FrozenStringValue {
        return vtable().typeValue()
    }

    /**
     * See documentation of [StarlarkTypeId].
     */
    internal fun starlarkTypeId(): StarlarkTypeId {
        return vtable().starlarkTypeId
    }

    /**
     * The literal string that a user would need to import this in type annotations.
     */
    internal fun getTypeStarlarkRepr(): Ty {
        return vtable().typeStarlarkRepr()
    }

    /**
     * Add two [Value]s together. Will first try using `add`,
     * before falling back to `radd`.
     */
    fun add(other: Value, heap: Heap): Result<Value> {
        // Fast special case for ints.
        val ls = unpackInlineInt()
        if (ls != null) {
            val rs = other.unpackInlineInt()
            if (rs != null) {
                // On overflow take the slow path below.
                val sum = ls.checkedAdd(rs)
                if (sum != null) {
                    return Result.success(newInt(sum))
                }
            }
        }

        // Addition of string is super common and pretty cheap, so have a special case for it.
        val lStr = unpackStr()
        if (lStr != null) {
            val rStr = other.unpackStr()
            if (rStr != null) {
                return if (lStr.isEmpty()) {
                    Result.success(other)
                } else if (rStr.isEmpty()) {
                    Result.success(this)
                } else {
                    Result.success(heap.allocStrConcat(lStr, rStr).toValue())
                }
            }
        }

        return when (val result = getRef().add(other, heap)) {
            null -> when (val rresult = other.getRef().radd(this, heap)) {
                null -> ValueError.unsupportedOwned(getType(), "+", other.getType())
                else -> rresult
            }
            else -> result
        }
    }

    /**
     * Convert a value to a [FrozenValue] using a supplied [Freezer].
     */
    override fun freeze(freezer: Freezer): Result<FrozenValue> {
        return freezer.freeze(this)
    }

    override fun trace(tracer: Tracer) {
        tracer.trace(this)
    }

    override val staticType: KClass<*> get() = Value::class

    // ValueLike implementation

    override fun fromFrozenValue(v: FrozenValue): Value = v.toValue()

    /**
     * Implement the `str()` function - converts a string value to itself,
     * otherwise uses `repr()`.
     */
    fun toStr(): String {
        return unpackStr() ?: toRepr()
    }

    /**
     * Implement the `repr()` function.
     */
    fun toRepr(): String {
        val s = StringBuilder()
        collectRepr(s)
        return s.toString()
    }

    /**
     * Name to import when displaying this value in the call stack.
     */
    internal fun nameForCallStack(): String {
        return getRef().nameForCallStack(this)
    }

    /**
     * Convert the value to JSON.
     *
     * Return an error if the value or any contained value does not support conversion to JSON.
     */
    fun toJson(): Result<String> {
        return toJsonValue().map { it.toString() }
    }

    /**
     * Convert the value to a JSON value object.
     */
    fun toJsonValue(): Result<JsonElement> {
        return serializeJsonElementImpl()
    }

    internal fun serializeJsonElementImpl(): Result<JsonElement> {
        val guard = jsonStackPush(this)
        return if (guard.isSuccess) {
            try {
                serializeToJsonElement()
            } finally {
                guard.getOrThrow().close()
            }
        } else {
            Result.failure(ToJsonCycleError(getType()))
        }
    }

    private fun serializeToJsonElement(): Result<JsonElement> {
        if (isNone()) return Result.success(JsonNull)
        unpackBool()?.let { return Result.success(JsonPrimitive(it)) }
        unpackStr()?.let { return Result.success(JsonPrimitive(it)) }

        val int = StarlarkIntRef.unpackValueOpt(this)
        if (int != null) {
            return when (int) {
                is StarlarkIntRef.Small -> Result.success(JsonPrimitive(int.value.toI32()))
                is StarlarkIntRef.Big -> {
                    val serialized = int.value.serialize()
                    when (serialized) {
                        is Long -> Result.success(JsonPrimitive(serialized))
                        is ULong -> Result.success(JsonPrimitive(serialized.toString()))
                        is String -> Result.success(JsonPrimitive(serialized))
                        else -> Result.failure(
                            IllegalStateException("Unsupported bigint JSON serialization: ${serialized::class.simpleName}")
                        )
                    }
                }
            }
        }

        val num = unpackNum()
        if (num != null) {
            return when (num) {
                is NumRef.Float -> Result.success(JsonPrimitive(num.value.value))
                // Int handled above.
                else -> Result.failure(IllegalStateException("Unsupported numeric JSON serialization: $num"))
            }
        }

        val rawPtr = getRef().value.ptr
        if (rawPtr is ListGen<*>) {
            val listLike = rawPtr.data as? ListLike
                ?: return Result.failure(IllegalStateException("Unsupported list backing for JSON: ${rawPtr.data::class.simpleName}"))
            val out = mutableListOf<JsonElement>()
            for (v in listLike.content()) {
                val el = v.toJsonValue().getOrElse { return Result.failure(it) }
                out.add(el)
            }
            return Result.success(JsonArray(out))
        }

        if (rawPtr is DictGen<*>) {
            val dictLike = rawPtr.inner as? DictLike
                ?: return Result.failure(IllegalStateException("Unsupported dict backing for JSON: ${rawPtr.inner::class.simpleName}"))
            val out = mutableMapOf<String, JsonElement>()
            for ((k, v) in dictLike.content().iter()) {
                val key = k.unpackStr()
                    ?: return Result.failure(IllegalStateException("JSON object keys must be strings, got `${k.toStringForTypeError()}`"))
                val el = v.toJsonValue().getOrElse { return Result.failure(it) }
                out[key] = el
            }
            return Result.success(JsonObject(out))
        }

        return Result.failure(IllegalStateException("Unsupported JSON serialization for `${toStringForTypeError()}`"))
    }

    /**
     * Forwards to [StarlarkValue.setAttr].
     */
    fun setAttr(attribute: String, allocValue: Value): Result<Unit> {
        return getRef().setAttr(attribute, allocValue)
    }

    /**
     * Forwards to [StarlarkValue.setAt].
     */
    fun setAt(index: Value, allocValue: Value): Result<Unit> {
        return getRef().setAt(index, allocValue)
    }

    /**
     * Forwards to [StarlarkValue.documentation].
     */
    fun documentation(): DocItem {
        return getRef().documentation()
    }

    /**
     * Produce an iterable from a value.
     */
    fun iterate(heap: Heap): Result<StarlarkIterator> {
        return getRef().iterate(this, heap).map { iter ->
            StarlarkIterator.new(iter, heap)
        }
    }

    /**
     * Get the [Hashed] version of this [Value].
     */
    override fun getHashed(): Result<Hashed<Value>> = super.getHashed()

    /**
     * Are two values equal. If the values are of different types it will
     * return `false`. It will only error if there is excessive recursion.
     */
    override fun equals(other: Value): Result<Boolean> {
        return if (ptrEq(other)) {
            Result.success(true)
        } else {
            equalsNotPtrEq(other)
        }
    }

    private fun equalsNotPtrEq(other: Value): Result<Boolean> {
        try {
            stackGuard()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        return getRef().equals(other)
    }

    /**
     * How are two values comparable. For values of different types will return error.
     */
    override fun compare(other: Value): Result<Int> {
        try {
            stackGuard()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        return getRef().compare(other)
    }

    /**
     * Describe the value, in order to get its metadata in a way that could be used
     * to generate prototypes, help information or whatever other descriptive text
     * is required.
     * Plan is to make this return a data type at some point in the future, possibly
     * move on to [StarlarkValue] and include data from members.
     */
    fun describe(name: String): String {
        return if (getType() == FUNCTION_TYPE) {
            "def ${toRepr().replace(" = ...", " = None")}: pass"
        } else {
            "# $name = ${toRepr()}"
        }
    }

    /**
     * Call `exportAs` on the underlying value, but only if the type is mutable.
     * Otherwise, does nothing.
     */
    fun exportAs(variableName: String, eval: Evaluator): Result<Unit> {
        return getRef().exportAs(variableName, eval)
    }

    /**
     * Return the attribute with the given name.
     */
    fun getAttr(attribute: String, heap: Heap): Result<Value?> {
        val aref = getRef()
        val methods = aref.vtable().methods()
        return if (methods != null) {
            val hashedAttr = Hashed.new(attribute)
            val v = methods.getHashed(hashedAttr)
            if (v != null) {
                v.bind(this, heap).map { it }
            } else {
                Result.success(aref.getAttrHashed(hashedAttr, heap))
            }
        } else {
            Result.success(aref.getAttr(attribute, heap))
        }
    }

    /**
     * Like `getAttr` but return an error if the attribute is not available.
     */
    fun getAttrError(attribute: String, heap: Heap): Result<Value> {
        val v = try {
            getAttr(attribute, heap).getOrThrow()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        return if (v == null) {
            ValueError.unsupportedOwned(getType(), ".$attribute", null)
        } else {
            Result.success(v)
        }
    }

    /**
     * Query whether an attribute exists on a type. Should be equivalent to whether
     * [getAttr] succeeds, but potentially more efficient.
     */
    fun hasAttr(attribute: String, heap: Heap): Boolean {
        val aref = getRef()
        val methods = aref.vtable().methods()
        if (methods != null) {
            if (methods.get(attribute) != null) {
                return true
            }
        }
        return aref.hasAttr(attribute, heap)
    }

    /**
     * Get a list of all the attributes this function supports, used to implement the
     * `dir()` function.
     */
    fun dirAttr(): List<String> {
        val aref = getRef()
        val methods = aref.vtable().methods()
        val result = if (methods != null) {
            val res = methods.names().toMutableList()
            res.addAll(aref.dirAttr())
            res
        } else {
            aref.dirAttr().toMutableList()
        }
        result.sort()
        return result
    }

    /**
     * Request a value provided by [StarlarkValue.provide].
     */
    inline fun <reified T : Any> requestValue(): T? {
        return requestValueImpl(this)
    }

    /**
     * Return a string usable for error messages.
     *
     * If the value is too large, it may be truncated.
     */
    fun toStringForTypeError(): String {
        return displayForTypeError().toString()
    }

    private fun displayForTypeError(): DisplayWithTypeImpl {
        return DisplayWithTypeImpl(this)
    }

    private class DisplayWithTypeImpl(
        private val value: Value,
    ) {
        fun fmt(): String {
            fun splitAtSafe(s: String, index: Int): Pair<String, String> {
                var i = index.coerceIn(0, s.length)
                while (i < s.length) {
                    return Pair(s.substring(0, i), s.substring(i))
                }
                return Pair(s, "")
            }

            var repr = value.toRepr()
            val maxLen = 60

            if (repr.length > maxLen && repr.toList().size > maxLen) {
                val truncated = "<<...>>"

                // 1/3 from back, 2/3 from front, because front is usually more interesting.
                val takeFromBack = maxOf(0, maxLen - truncated.length) / 3
                val takeFromFront = takeFromBack * 2

                // Resulting repr is approximately `maxLen` long.
                repr = buildString {
                    append(splitAtSafe(repr, takeFromFront).first)
                    append(truncated)
                    append(splitAtSafe(repr, maxOf(0, repr.length - takeFromBack)).second)
                }
            }

            return "${value.getType()} (repr: $repr)"
        }

        override fun toString(): String = fmt()
    }

    /**
     * Downcast to a specific [StarlarkValue] type.
     */
    // Note: internal because this inline function accesses internal members.
    // All callers are within the same module.
    internal inline fun <reified T : StarlarkValue> downcastRef(): T? {
        if (T::class == StarlarkStr::class) {
            return if (isStr()) {
                // SAFETY: we just checked this is string, and requested type is string.
                downcastRefUnchecked<T>()
            } else {
                null
            }
        }
        if (PointerI32.typeIsPointerI32<T>()) {
            return if (unpackInlineInt() != null) {
                // SAFETY: we just checked this is int, and requested type is int.
                downcastRefUnchecked<T>()
            } else {
                null
            }
        }
        return getRef().downcastRef<T>()
    }

    // ValueLike interface requires non-reified KClass version
    override fun <T : StarlarkValue> downcastRef(clazz: KClass<T>): T? {
        if (clazz == StarlarkStr::class) {
            if (!isStr()) return null
        }
        if (clazz == PointerI32::class) {
            return if (unpackInlineInt() != null) {
                clazz.safeCast(PointerI32(ptr.unpackIntValue()))
            } else {
                null
            }
        }
        return getRef().downcastRef(clazz)
    }

    /**
     * Collect repr into a collector, handling cycles.
     */
    override fun collectRepr(collector: StringBuilder) {
        val guard = reprStackPush(this)
        if (guard.isSuccess) {
            try {
                getRef().collectRepr(collector)
            } finally {
                guard.getOrThrow().close()
            }
        } else {
            getRef().collectReprCycle(collector)
        }
    }

    /**
     * Write hash value.
     */
    override fun writeHash(hasher: StarlarkHasher): Result<Unit> {
        return getRef().writeHash(hasher)
    }

    /**
     * Collect str into a collector.
     */
    override fun collectStr(collector: StringBuilder) {
        val s = unpackStr()
        if (s != null) {
            collector.append(s)
        } else {
            collectRepr(collector)
        }
    }

    // This is equivalent to toRepr().
    override fun toString(): String = toRepr()

    /**
     * Equivalent of Rust `Display::fmt` for [Value].
     *
     * Writes the Starlark `repr()` form into [collector], including cycle handling.
     */
    fun fmt(collector: StringBuilder) {
        collectRepr(collector)
    }

    /**
     * Equivalent of Rust `Debug::fmt` for [Value].
     */
    fun fmt(collector: StringBuilder, debug: Boolean) {
        if (debug) {
            collector.append(debugValue("Value", this))
        } else {
            fmt(collector)
        }
    }

    /**
     * Equivalent of Rust `PartialEq::eq` for [Value].
     */
    fun eq(other: Value): Boolean {
        return equals(other).getOrDefault(false)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Value) return false
        return equals(other).getOrDefault(false)
    }

    override fun hashCode(): Int {
        return ptr.raw().hashCode()
    }

    fun debug(): String {
        return debugValue("Value", this)
    }
}

fun Value.Companion.default(): Value = Value.newNone()

fun FrozenValue.Companion.default(): FrozenValue = FrozenValue.newNone()

fun Value.equivalent(key: FrozenValue): Boolean {
    return key.equals(this).getOrThrow()
}

fun FrozenValue.equivalent(key: Value): Boolean {
    return this.equals(key).getOrThrow()
}

/**
 * A [Value] that can never be changed. Can be converted back to a [Value] with [toValue].
 *
 * A [FrozenValue] exists on a [FrozenHeap], which in turn can be kept
 * alive by a [FrozenHeapRef]. If the frozen heap gets dropped
 * while a [FrozenValue] from it still exists, the program will probably crash, so be careful
 * when working directly with [FrozenValue]s. See the type [OwnedFrozenValue]
 * for a little bit more safety.
 */
class FrozenValue internal constructor(
    internal val ptr: FrozenPointer,
) : ValueLike<FrozenValue> {
    companion object {
        /**
         * Create a new [FrozenValue] from an [AValueHeader] pointer.
         */
        internal fun newPtr(x: AValueHeader, isStr: Boolean): FrozenValue {
            return FrozenValue(FrozenPointer.newFrozen(x.index, isStr))
        }

        /**
         * Create a new [FrozenValue] from an [AValueHeader], querying whether it is a string.
         */
        internal fun newPtrQueryIsStr(x: AValueHeader): FrozenValue {
            val isString = x.vtable.isStr
            return newPtr(x, isString)
        }

        internal fun newPtrUsizeWithStrTag(x: Long): FrozenValue {
            return FrozenValue(FrozenPointer.newFrozenUsizeWithStrTag(x))
        }

        /**
         * Create a new value representing `None` in Starlark.
         */
        fun newNone(): FrozenValue {
            return VALUE_NONE.toFrozenValue()
        }

        /**
         * Create a new boolean in Starlark.
         */
        fun newBool(x: Boolean): FrozenValue {
            // Implemented by indexing into a static so that
            // the compiler makes this function branchless.
            return VALUE_FALSE_TRUE[if (x) 1 else 0].toFrozenValue()
        }

        /**
         * Create a new int in Starlark.
         */
        internal fun newInt(x: InlineInt): FrozenValue {
            return FrozenValue(FrozenPointer.newInt(x.toI32()))
        }

        /**
         * Create a new int for testing purposes.
         */
        internal fun testingNewInt(x: Int): FrozenValue {
            return newInt(InlineInt.tryFrom(x).getOrThrow())
        }

        /**
         * Create a new empty string.
         */
        internal fun newEmptyString(): FrozenValue {
            return VALUE_EMPTY_STRING.unpack()
        }

        /**
         * Create a new empty tuple.
         */
        internal fun newEmptyTuple(): FrozenValue {
            return VALUE_EMPTY_TUPLE.toFrozenValue()
        }

        /**
         * Create a new empty list.
         */
        fun newEmptyList(): FrozenValue {
            return VALUE_EMPTY_FROZEN_LIST.toFrozenValue()
        }

        /**
         * Create a new empty dict.
         */
        fun newEmptyDict(): FrozenValue {
            return VALUE_EMPTY_FROZEN_DICT.toFrozenValue()
        }

        /**
         * Convert from [FrozenValue] (ValueLike factory method).
         * For FrozenValue, simply returns itself.
         */
        fun fromFrozenValue(v: FrozenValue): FrozenValue = v
    }

    /**
     * Get the underlying raw pointer.
     */
    internal fun ptrValue(): RawPointer {
        return ptr.raw()
    }

    /**
     * Is a value a Starlark `None`.
     */
    fun isNone(): Boolean {
        return toValue().isNone()
    }

    /**
     * Return the `bool` if the value is a boolean, otherwise `null`.
     */
    fun unpackBool(): Boolean? {
        return toValue().unpackBool()
    }

    /**
     * Obtain the underlying integer if it fits in an `Int`.
     * Note floats are not considered integers, i.e. `unpackI32` for `1.0` will return `null`.
     */
    fun unpackI32(): Int? {
        return toValue().unpackI32()
    }

    /**
     * Unpack inline integer value.
     */
    internal fun unpackInlineInt(): InlineInt? {
        return toValue().unpackInlineInt()
    }

    /**
     * Check if this value is a string.
     */
    internal fun isStr(): Boolean {
        return toValue().isStr()
    }

    internal fun unpackStr(): String? {
        return toValue().unpackStr()
    }

    /**
     * Convert a [FrozenValue] back to a [Value].
     */
    override fun toValue(): Value {
        return Value.newFrozen(this)
    }

    /**
     * Is this type builtin? We perform certain optimizations only on builtin types
     * because we know they have well defined semantics.
     */
    internal fun isBuiltin(): Boolean {
        // The list is not comprehensive, this is fine.
        // If some type is not listed here, some optimizations will not work for this type.
        return isNone()
            || isStr()
            || unpackBool() != null
            || NumRef.unpackValue(toValue()).getOrNull()?.let { true } ?: false
            || FrozenListData.fromFrozenValue(this) != null
            || FrozenDictRef.fromFrozenValue(this) != null
            || FrozenValueTyped.new<TupleGen<FrozenValue>>(this) != null
            || FrozenValueTyped.new<Range>(this) != null
            || FrozenValueTyped.new<DefGen<FrozenValue>>(this) != null
            || FrozenValueTyped.new<NativeFunction>(this) != null
            || FrozenValueTyped.new<StructGen<FrozenValue>>(this) != null
            || FrozenValueTyped.new<RecordTypeGen>(this) != null
            || FrozenValueTyped.new<RecordGen>(this) != null
            || FrozenValueTyped.new<EnumTypeGen>(this) != null
            || FrozenValueTyped.new<EnumValueGen>(this) != null
    }

    /**
     * Can `invoke` be called on this object speculatively?
     * (E.g. at compiled time when all the arguments are known.)
     */
    internal fun speculativeExecSafe(): Boolean {
        val nf = FrozenValueTyped.new<NativeFunction>(this)
        if (nf != null) {
            return nf.asRef().speculativeExecSafe
        }
        val bm = FrozenValueTyped.new<BoundMethodGen<FrozenValue>>(this)
        if (bm != null) {
            return bm.asRef().method.asRef().speculativeExecSafe
        }
        return false
    }

    /**
     * `self == b` is `ptrEq`.
     */
    internal fun eqIsPtrEq(): Boolean {
        // Note `int` is not `ptrEq` because `int` can be equal to `float`.

        // If a value does not override equality, it is `ptrEq`.
        if (!toValue().getRef().vtable().hasEquals) {
            return true
        }
        // Strings of length <= 1 are statically allocated.
        val str = unpackStr()
        if (str != null && str.length <= 1) {
            return true
        }
        // Empty tuple is statically allocated.
        val tuple = TupleGen.fromValue(toValue())
        if (tuple != null && tuple.len() == 0) {
            return true
        }
        return false
    }

    /**
     * Downcast to given type.
     */
    internal inline fun <reified T : StarlarkValue> downcastFrozenRef(): FrozenRef<T>? {
        val value = toValue().downcastRef<T>() ?: return null
        return FrozenRef(value)
    }

    /**
     * Downcast to string.
     */
    fun downcastFrozenStr(): FrozenRef<String>? {
        val value = toValue().unpackStr() ?: return null
        return FrozenRef(value)
    }

    /**
     * Note: see docs about `Value.unpackStarlarkStr` about instability.
     */
    fun downcastFrozenStarlarkStr(): FrozenRef<StarlarkStr>? {
        val value = toValue().unpackStarlarkStr() ?: return null
        return FrozenRef(value)
    }

    /**
     * Compare this frozen value with a mutable value for equality.
     */
    override fun equals(other: Value): Result<Boolean> {
        return toValue().equals(other)
    }

    /**
     * Collect repr into a collector, handling cycles.
     */
    override fun collectRepr(collector: StringBuilder) {
        toValue().collectRepr(collector)
    }

    /**
     * Collect str into a collector.
     */
    override fun collectStr(collector: StringBuilder) {
        toValue().collectStr(collector)
    }

    /**
     * Write hash value.
     */
    override fun writeHash(hasher: StarlarkHasher): Result<Unit> {
        return toValue().writeHash(hasher)
    }

    /**
     * How are two values comparable. For values of different types will return error.
     */
    override fun compare(other: Value): Result<Int> {
        return toValue().compare(other)
    }

    /**
     * Equivalent of Rust `Display::fmt` for [FrozenValue].
     */
    fun fmt(collector: StringBuilder) {
        toValue().fmt(collector)
    }

    /**
     * Equivalent of Rust `Debug::fmt` for [FrozenValue].
     */
    fun fmt(collector: StringBuilder, debug: Boolean) {
        if (debug) {
            collector.append(debugValue("FrozenValue", Value.newFrozen(this)))
        } else {
            fmt(collector)
        }
    }

    /**
     * Equivalent of Rust `PartialEq::eq` for [FrozenValue].
     */
    fun eq(other: FrozenValue): Boolean {
        return toValue().eq(other.toValue())
    }

    override fun toString(): String {
        return toValue().toString()
    }

    fun debug(): String {
        return debugValue("FrozenValue", Value.newFrozen(this))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FrozenValue) return false
        return toValue() == other.toValue()
    }

    override fun hashCode(): Int {
        return ptr.raw().hashCode()
    }

    // ValueLike implementation

    override fun fromFrozenValue(v: FrozenValue): FrozenValue = v

    override fun freeze(freezer: Freezer): Result<FrozenValue> {
        return Result.success(this)
    }

    override fun trace(tracer: Tracer) {
    }

    override val staticType: KClass<*> get() = FrozenValue::class

    // ValueLike interface requires non-reified KClass version
    override fun <T : StarlarkValue> downcastRef(clazz: KClass<T>): T? {
        return toValue().downcastRef(clazz)
    }

    /**
     * Downcast to a specific [StarlarkValue] type.
     */
    internal inline fun <reified T : StarlarkValue> downcastRef(): T? {
        return toValue().downcastRef<T>()
    }
}

// Delegates to [toJson], which routes through [Value.serializeJsonElementImpl] for cycle detection.
fun Value.serialize(): Result<String> = toJson()

fun FrozenValue.serialize(): Result<String> = toValue().serialize()

object ValueStarlarkTypeRepr : StarlarkTypeRepr {
    override fun starlarkTypeRepr(): Ty = FrozenValueStarlarkTypeRepr.starlarkTypeRepr()
}

object FrozenValueStarlarkTypeRepr : StarlarkTypeRepr {
    override fun starlarkTypeRepr(): Ty = Ty.any()
}

/**
 * Abstract over [Value] and [FrozenValue].
 *
 * The methods on this interface are those required to implement containers,
 * allowing implementations of [ComplexValue] to be agnostic of their contained type.
 * For details about each function, see the documentation for [Value],
 * which provides the same functions (and more).
 */
interface ValueLike<Self : ValueLike<Self>> :
    ValueLifetimeless,
    Trace,
    CoerceKey<Value>,
    ProvidesStaticType {
    /**
     * Produce a [Value] regardless of the type you are starting with.
     */
    fun toValue(): Value

    /**
     * Convert from [FrozenValue].
     */
    fun fromFrozenValue(v: FrozenValue): Self

    /**
     * Call this value as a function with given arguments.
     */
    fun invoke(args: Arguments, eval: Evaluator): Result<Value> {
        return toValue().invoke(args, eval)
    }

    /**
     * Hash the value.
     */
    fun writeHash(hasher: StarlarkHasher): Result<Unit>

    /**
     * Get hash value.
     */
    fun getHashed(): Result<Hashed<Self>> {
        val hash = toValue().unpackStarlarkStr()?.getHash()
            ?: toValue().getHash().getOrElse { return Result.failure(it) }
        return Result.success(Hashed.newUnchecked(hash, this as Self))
    }

    /**
     * `repr(x)`.
     */
    fun collectRepr(collector: StringBuilder)

    /**
     * `str(x)`.
     */
    fun collectStr(collector: StringBuilder) {
        val s = toValue().unpackStr()
        if (s != null) {
            collector.append(s)
        } else {
            collectRepr(collector)
        }
    }

    /**
     * `x == other`.
     *
     * This operation can only return error on stack overflow.
     */
    fun equals(other: Value): Result<Boolean>

    /**
     * `x <=> other`.
     */
    fun compare(other: Value): Result<Int>

    /**
     * Get a reference to underlying data or `null`
     * if contained object has different type than requested.
     */
    fun <T : StarlarkValue> downcastRef(clazz: KClass<T>): T?

    /**
     * Get a reference to underlying data or error
     * if contained object has different type than requested.
     */
    fun <T : StarlarkValue> downcastRefErr(clazz: KClass<T>): Result<T> {
        val v = downcastRef(clazz)
        return if (v != null) {
            Result.success(v)
        } else {
            Result.failure(ValueValueError.WrongType(
                clazz.simpleName ?: "Unknown",
                toValue().toStringForTypeError(),
            ))
        }
    }
}

private fun testSendSync() {
}
