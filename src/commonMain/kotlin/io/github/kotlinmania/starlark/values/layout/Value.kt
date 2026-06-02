// port-lint: source src/values/layout/value.rs
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

// Possible optimisations:
// Avoid the Box duplication
// Encode Int in the pointer too

// We use pointer tagging on the bottom two bits:
// 00 => this Value pointer is actually a FrozenValue pointer
// 01 => this is a real Value pointer
// 11 => this is a bool (next bit: 1 => true, 0 => false)
// 10 => this is a None
//
// We don't use pointer tagging for Int (although we'd like to), because
// our val_ref requires a pointer to the value. We need to put that pointer
// somewhere. The solution is to have a separate value storage vs vtable.

import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.kotlinmania.starlark.Error
import io.github.kotlinmania.starlark.collections.Hashed
import io.github.kotlinmania.starlark.collections.StarlarkHashValue
import io.github.kotlinmania.starlark.collections.StarlarkHasher
import io.github.kotlinmania.starlark.collections.symbol.Symbol
import io.github.kotlinmania.starlark.docs.DocItem
import io.github.kotlinmania.starlark.eval.compiler.Def
import io.github.kotlinmania.starlark.eval.compiler.FrozenDef
import io.github.kotlinmania.starlark.eval.runtime.Arguments
import io.github.kotlinmania.starlark.eval.runtime.ArgumentsFull
import io.github.kotlinmania.starlark.eval.runtime.Evaluator
import io.github.kotlinmania.starlark.eval.runtime.FrameSpan
import io.github.kotlinmania.starlark.eval.runtime.params.spec.ParametersSpec
import io.github.kotlinmania.starlark.typing.ParamIsRequired
import io.github.kotlinmania.starlark.typing.ParamSpec
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.typing.TyCallable
import io.github.kotlinmania.starlark.values.FrozenRef
import io.github.kotlinmania.starlark.values.StarlarkIterator
import io.github.kotlinmania.starlark.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.ValueError
import io.github.kotlinmania.starlark.values.demand.requestValueImpl
import io.github.kotlinmania.starlark.values.jsonStackPush
import io.github.kotlinmania.starlark.values.layout.avalues.str.allocStrConcat
import io.github.kotlinmania.starlark.values.layout.heap.AValueHeader
import io.github.kotlinmania.starlark.values.layout.heap.AValueRepr
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.layout.typed.FrozenStringValue
import io.github.kotlinmania.starlark.values.layout.typed.StarlarkStr
import io.github.kotlinmania.starlark.values.reprStackPush
import io.github.kotlinmania.starlark.values.stackGuard
import io.github.kotlinmania.starlark.values.starlarktypeid.StarlarkTypeId
import io.github.kotlinmania.starlark.values.types.FUNCTION_TYPE
import io.github.kotlinmania.starlark.values.types.FrozenBoundMethod
import io.github.kotlinmania.starlark.values.types.NativeFunction
import io.github.kotlinmania.starlark.values.types.bool.VALUE_FALSE_TRUE
import io.github.kotlinmania.starlark.values.types.dict.FrozenDictRef
import io.github.kotlinmania.starlark.values.types.dict.VALUE_EMPTY_FROZEN_DICT
import io.github.kotlinmania.starlark.values.types.enumeration.enumtype.EnumType
import io.github.kotlinmania.starlark.values.types.enumeration.value.FrozenEnumValue
import io.github.kotlinmania.starlark.values.types.float.StarlarkFloat
import io.github.kotlinmania.starlark.values.types.int.InlineInt
import io.github.kotlinmania.starlark.values.types.int.PointerI32
import io.github.kotlinmania.starlark.values.types.int.StarlarkIntRef
import io.github.kotlinmania.starlark.values.types.list.FrozenListData
import io.github.kotlinmania.starlark.values.types.list.VALUE_EMPTY_FROZEN_LIST
import io.github.kotlinmania.starlark.values.types.none.VALUE_NONE
import io.github.kotlinmania.starlark.values.types.num.NumRef
import io.github.kotlinmania.starlark.values.types.range.Range
import io.github.kotlinmania.starlark.values.types.record.FrozenRecord
import io.github.kotlinmania.starlark.values.types.record.recordtype.RecordTypeGen
import io.github.kotlinmania.starlark.values.types.structs.FrozenStruct
import io.github.kotlinmania.starlark.values.types.tuple.FrozenTuple
import io.github.kotlinmania.starlark.values.types.tuple.Tuple
import io.github.kotlinmania.starlark.values.types.tuple.VALUE_EMPTY_TUPLE
import io.github.kotlinmania.starlark.values.types.tuple.fromValue
import kotlin.reflect.KClass

// We already import another `ValueError`, hence the odd name.
private class ValueValueError {
    class WrongType(
        val expectedType: String,
        val actualType: String,
    ) : Exception("Expected value of type `$expectedType` but got `$actualType`")
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
private class ToJsonCycleError(
    val typeName: String,
) : Exception("Cycle detected when serializing value of type `$typeName` to JSON")

private fun debugValue(typ: String, v: Value): String {
    // When value is being moved during GC or freeze,
    // `Value` pointee is not a proper value, but a GC-related information.
    // Regular operations like `.toRepr()` crash, but `Debug` should work.
    // In Rust:
    //   if let Some(x) = v.0.unpack_ptr() {
    //       if let AValueOrForwardUnpack::Forward(fwd) = x.unpack() {
    //           return f.debug_tuple(typ).field(&fwd).finish();
    //   f.debug_tuple(typ).field(v.get_ref().as_debug()).finish()
    val ptrOpt = v.ptr.unpackPtrOpt()
    if (ptrOpt != null) {
        try {
            AValueHeader.fromIndex(ptrOpt)
        } catch (_: Exception) {
            // Pointer is not a valid header (could be forward during GC)
            return "$typ(Forward($ptrOpt))"
        }
    }
    val listGen = v.downcastRef<io.github.kotlinmania.starlark.values.types.list.ListGen<*>>()
    if (listGen != null) {
        val content = (listGen.data as io.github.kotlinmania.starlark.values.types.list.ListLike).content()
        val contentStr = content.joinToString(", ") { debugValue("Value", it) }
        return "$typ(ListGen(ListData { content: Cell { value: ValueTyped(Value(Array { len: ${content.size}, capacity: ${content.size}, iter_count: 0, content: [$contentStr] })) } }))"
    }
    return "$typ(${v.getRef().asDebug()})"
}

/**
 * A Starlark value. The lifetime argument `'v` in Rust corresponds to the [Heap] it is stored on.
 * In Kotlin, lifetime management is handled by the garbage collector.
 *
 * Many of the methods simply forward to the underlying [StarlarkValue].
 * The [toString] method is equivalent to the `repr()` function in Starlark.
 */
class Value internal constructor(
    internal val ptr: Pointer,
) : ValueLike {
    companion object {
        /**
         * Create a new [Value] from an [AValueHeader] pointer.
         */
        internal fun newPtr(x: AValueHeader, isStr: Boolean): Value = Value(Pointer.newUnfrozen(x.index, isStr))

        /**
         * Create a new [Value] from an [AValueHeader], querying whether it's a string.
         */
        internal fun newPtrQueryIsStr(x: AValueHeader): Value {
            val isString = x.vtable.isStr
            return newPtr(x, isString)
        }

        /**
         * Create a new [Value] from an [AValueRepr].
         */
        internal fun <T : AValue> newRepr(x: AValueRepr<AValueImpl<T>>): Value = newPtr(x.header, x.header.vtable.isStr)

        /**
         * Create a new [Value] from a raw usize with string tag.
         */
        internal fun newPtrUsizeWithStrTag(x: Long): Value = Value(Pointer.newUnfrozenUsizeWithStrTag(x))

        /**
         * Create a new int for testing purposes.
         */
        internal fun testingNewInt(x: Int): Value = FrozenValue.testingNewInt(x).toValue()

        /**
         * Create a new `None` value.
         */
        fun newNone(): Value = FrozenValue.newNone().toValue()

        /**
         * Create a new boolean value.
         */
        fun newBool(x: Boolean): Value = FrozenValue.newBool(x).toValue()

        /**
         * Create a new integer value.
         */
        internal fun newInt(x: InlineInt): Value = FrozenValue.newInt(x).toValue()

        /**
         * Create a new blank string value.
         */
        internal fun newEmptyString(): Value = FrozenValue.newEmptyString().toValue()

        /**
         * Create a new empty tuple value.
         */
        internal fun newEmptyTuple(): Value = FrozenValue.newEmptyTuple().toValue()

        /**
         * Turn a [FrozenValue] into a [Value]. See the safety warnings on
         * `OwnedFrozenValue`.
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

    /**
     * Cast the lifetime of this value. In Kotlin there are no lifetimes,
     * so this is effectively a no-op identity function.
     */
    @Suppress("NOTHING_TO_INLINE")
    internal inline fun castLifetime(): Value = this

    /**
     * Produce a [Value] regardless of the type you are starting with.
     * For [Value], simply returns itself.
     */
    override fun toValue(): Value = this

    /**
     * Obtain the underlying [FrozenValue] from inside the [Value], if it is one.
     */
    fun unpackFrozen(): FrozenValue? =
        if (ptr.isUnfrozen()) {
            null
        } else {
            // SAFETY: We've just checked the value is frozen.
            unpackFrozenUnchecked()
        }

    private fun unpackFrozenUnchecked(): FrozenValue = FrozenValue(ptr.castLifetime().toFrozenPointerUnchecked())

    /**
     * Is this value `None`.
     */
    fun isNone(): Boolean = ptrEq(newNone())

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
     * Unpack this value as an integer of type [Long], or return an error if it's too big.
     * Returns `null` if the value is not an integer at all.
     */
    internal fun <I> unpackIntegerImpl(
        integerType: String,
        tryFromI32: (Int) -> I?,
        tryFromBigInt: (BigInteger) -> I?,
    ): Result<I?> {
        val num = StarlarkIntRef.unpackValueOpt(this) ?: return Result.success(null)

        val option =
            when (num) {
                is StarlarkIntRef.Small -> {
                    val i32 = num.toI32()
                    if (i32 != null) {
                        tryFromI32(i32)
                    } else {
                        null
                    }
                }
                is StarlarkIntRef.Big -> {
                    tryFromBigInt(num.value.get())
                }
            }

        return if (option != null) {
            Result.success(option)
        } else {
            Result.failure(
                Error.newValue(
                    IntegerTooBigError(
                        integerType = integerType,
                        value = num.toString(),
                    ),
                ),
            )
        }
    }

    internal fun unpackInteger(): Result<Long?> =
        unpackIntegerImpl(
            integerType = "i64",
            tryFromI32 = { i32 -> i32.toLong() },
            tryFromBigInt = { bigInt ->
                try {
                    bigInt.longValue(exactRequired = true)
                } catch (_: ArithmeticException) {
                    null
                }
            },
        )

    /**
     * Obtain the underlying `bool` if it is a boolean.
     */
    fun unpackBool(): Boolean? =
        if (ptrEq(newBool(true))) {
            true
        } else if (ptrEq(newBool(false))) {
            false
        } else {
            null
        }

    /**
     * Obtain the underlying integer if it fits in an `Int`.
     * Note floats are not considered integers, i.e. `unpackI32` for `1.0` will return `null`.
     */
    fun unpackI32(): Int? =
        if (InlineInt.smallerThanI32()) {
            StarlarkIntRef.unpack(this)?.toI32()
        } else {
            unpackInlineInt()?.toI32()
        }

    /**
     * Unpack inline integer value.
     */
    internal fun unpackInlineInt(): InlineInt? = ptr.unpackInt()?.let { InlineInt(it) }

    /**
     * Unpack int value as a FrozenValueTyped PointerI32.
     */
    internal fun unpackIntValue(): FrozenValueTyped<PointerI32>? =
        if (unpackInlineInt() != null) {
            // SAFETY: We've just checked the value is an int.
            FrozenValueTyped.newUnchecked(unpackFrozenUnchecked())
        } else {
            null
        }

    /**
     * Check if this value is a string.
     */
    internal fun isStr(): Boolean = ptr.isStr()

    /**
     * Like [unpackStr], but gives a pointer to a boxed [StarlarkStr].
     * Mostly useful for when you want to convert the string to a `dyn` trait, but can't
     * form a `dyn` of an unsized type.
     *
     * Unstable and likely to be removed in future, as the presence of the `Box` is
     * not a guaranteed part of the API.
     */
    fun unpackStarlarkStr(): StarlarkStr? =
        if (isStr()) {
            getRef().downcastRef<StarlarkStr>()
        } else {
            null
        }

    /**
     * Obtain the underlying `str` if it is a string.
     */
    fun unpackStr(): String? = unpackStarlarkStr()?.asStr()

    /**
     * Obtain the underlying `str` if it is a string, otherwise return an error for users.
     */
    fun unpackStrErr(): Result<String> {
        val s = unpackStr()
        return if (s != null) {
            Result.success(s)
        } else {
            Result.failure(
                Error.newValue(
                    ValueValueError.WrongType("string", toStringForTypeError()),
                ),
            )
        }
    }

    /**
     * Get a pointer to an [AValue].
     */
    @PublishedApi
    internal fun getRef(): AValueDyn =
        if (ptr.unpackIsInt()) {
            val intVal = ptr.unpackIntValue()
            AValueDyn(
                StarlarkValueRawPtr(PointerI32.fromRawInt(intVal)),
                PointerI32.vtable(),
            )
        } else {
            val ptrIndex = ptr.unpackPtr()
            val header = AValueHeader.fromIndex(ptrIndex)
            header.unpack()
        }

    /**
     * Get the full reference including the value itself.
     */
    private fun getRefFull(): AValueDynFull = AValueDynFull(getRef(), this)

    /**
     * Get the vtable for this value.
     */
    internal fun vtable(): AValueVTable =
        if (ptr.unpackIsInt()) {
            PointerI32.vtable()
        } else {
            val ptrIndex = ptr.unpackPtr()
            AValueHeader.fromIndex(ptrIndex).vtable
        }

    /**
     * Get the raw underlying pointer from this value's AValueDyn.
     * Used by inline functions that need to access the underlying object
     * from a different package (e.g. ValueOf.unpackValueImpl).
     */
    @PublishedApi
    internal fun getUnderlyingPtr(): StarlarkValue = getRef().value.starlarkValue()

    /**
     * Downcast without checking the value type.
     */
    @Suppress("UNCHECKED_CAST")
    internal inline fun <reified T : StarlarkValue> downcastRefUnchecked(): T {
        if (PointerI32.typeIsPointerI32<T>()) {
            return PointerI32.fromRawInt(ptr.unpackIntValue()) as T
        }
        return getRef().downcastRef<T>()!!
    }

    /**
     * Get the hash value for this value.
     */
    internal fun getHash(): Result<StarlarkHashValue> = getRef().getHash()

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
    fun ptrEq(other: Value): Boolean = ptr.ptrEq(other.ptr)

    /**
     * Returns an identity for this [Value], derived from its pointer. This function is
     * low-level and provides two guarantees. Those are valid until the next GC:
     *
     * 1. Calling it multiple times on the same [Value] will return [ValueIdentity] that
     *    compare equal.
     * 2. If two [Value]s have [ValueIdentity] that compare equal, then [Value.ptrEq] and
     *    [Value.equals] will also consider them to be equal.
     */
    fun identity(): ValueIdentity = ValueIdentity.new(this)

    /**
     * Get the underlying pointer.
     * Should be done sparingly as it slightly breaks the abstraction.
     * Most useful as a hash key based on pointer.
     * For external users, `Value.identity` returns an opaque `ValueIdentity` that makes fewer
     * guarantees.
     */
    internal fun ptrValue(): RawPointer = ptr.raw()

    /**
     * `type(x)`.
     */
    fun getType(): String = vtable().typeName

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
    fun at(index: Value, heap: Heap): Result<Value> = getRef().at(index, heap)

    /**
     * `x[start:stop:stride]`.
     */
    fun slice(start: Value?, stop: Value?, stride: Value?, heap: Heap): Result<Value> = getRef().slice(start, stop, stride, heap)

    /**
     * `len(x)`.
     */
    fun length(): Result<Int> = getRef().length()

    /**
     * `other in x`.
     */
    fun isIn(other: Value): Result<Boolean> = getRef().isIn(other)

    /**
     * `+x`.
     */
    fun plus(heap: Heap): Result<Value> = getRef().plus(heap)

    /**
     * `-x`.
     */
    fun minus(heap: Heap): Result<Value> = getRef().minus(heap)

    /**
     * `x - other`.
     */
    fun sub(other: Value, heap: Heap): Result<Value> = getRef().sub(other, heap)

    /**
     * `x * other`.
     */
    fun mul(other: Value, heap: Heap): Result<Value> =
        when (val result = getRef().mul(other, heap)) {
            null ->
                when (val rresult = other.getRef().rmul(this, heap)) {
                    null -> ValueError.unsupportedOwned(getType(), "*", other.getType())
                    else -> rresult
                }
            else -> result
        }

    /**
     * `x % other`.
     */
    fun percent(other: Value, heap: Heap): Result<Value> = getRef().percent(other, heap)

    /**
     * `x / other`.
     */
    fun div(other: Value, heap: Heap): Result<Value> = getRef().div(other, heap)

    /**
     * `x // other`.
     */
    fun floorDiv(other: Value, heap: Heap): Result<Value> = getRef().floorDiv(other, heap)

    /**
     * `x & other`.
     */
    fun bitAnd(other: Value, heap: Heap): Result<Value> = getRef().bitAnd(other, heap)

    /**
     * `x | other`.
     */
    fun bitOr(other: Value, heap: Heap): Result<Value> = getRef().bitOr(other, heap)

    /**
     * `x ^ other`.
     */
    fun bitXor(other: Value, heap: Heap): Result<Value> = getRef().bitXor(other, heap)

    /**
     * `~x`.
     */
    fun bitNot(heap: Heap): Result<Value> = getRef().bitNot(heap)

    /**
     * `x << other`.
     */
    fun leftShift(other: Value, heap: Heap): Result<Value> = getRef().leftShift(other, heap)

    /**
     * `x >> other`.
     */
    fun rightShift(other: Value, heap: Heap): Result<Value> = getRef().rightShift(other, heap)

    /**
     * Invoke with a call stack location.
     */
    internal fun invokeWithLoc(
        location: FrozenRef<FrameSpan>?,
        args: Arguments,
        eval: Evaluator,
    ): Result<Value> =
        eval.withCallStack(this, location) { e ->
            getRefFull().invoke(args, e)
        }

    /**
     * Callable parameters if known.
     *
     * For now it only returns parameter spec for `def` and `lambda`.
     */
    @Suppress("UNCHECKED_CAST")
    fun parametersSpec(): ParametersSpec<Value>? {
        val def = downcastRef<Def>()
        if (def != null) {
            return def.parameters
        }
        val frozenDef = downcastRef<FrozenDef>()
        if (frozenDef != null) {
            // In Rust this is a transmute from ParametersSpec<FrozenValue> to ParametersSpec<Value>.
            // In Kotlin with type erasure, an unchecked cast is equivalent.
            return frozenDef.parameters as ParametersSpec<Value>
        }
        return null
    }

    /**
     * Invoke self with given arguments.
     */
    override fun invoke(
        args: Arguments,
        eval: Evaluator,
    ): Result<Value> = invokeWithLoc(null, args, eval)

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
                IllegalStateException("Value is not callable: ${toStringForTypeError()}"),
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

        val paramSpec =
            try {
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

        // Ty.ofValue: use typechecker type if available, else type repr
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
                    "Value `${toStringForTypeError()}` is not compatible with the signature `$sig`",
                ),
            )
        }

        return Result.success(Unit)
    }

    /**
     * `type(x)` as a [FrozenStringValue].
     */
    fun getTypeValue(): FrozenStringValue = vtable().typeValue()

    /**
     * See documentation of [StarlarkTypeId].
     */
    internal fun starlarkTypeId(): StarlarkTypeId = vtable().starlarkTypeId

    /**
     * The literal string that a user would need to use this in type annotations.
     */
    internal fun getTypeStarlarkRepr(): Ty = vtable().typeStarlarkRepr()

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
            null ->
                when (val rresult = other.getRef().radd(this, heap)) {
                    null -> ValueError.unsupportedOwned(getType(), "+", other.getType())
                    else -> rresult
                }
            else -> result
        }
    }

    /**
     * Convert a value to a [FrozenValue] using a supplied [Freezer].
     */
    override fun freeze(freezer: Freezer): Result<FrozenValue> = freezer.freeze(this)

    // ValueLike impl

    override fun fromFrozenValue(v: FrozenValue): ValueLike = v.toValue()

    /**
     * Implement the `str()` function - converts a string value to itself,
     * otherwise uses `repr()`.
     */
    fun toStr(): String = unpackStr() ?: toRepr()

    /**
     * Implement the `repr()` function.
     */
    fun toRepr(): String {
        val s = StringBuilder()
        collectRepr(s)
        return s.toString()
    }

    /**
     * Name to use when displaying this value in the call stack.
     */
    internal fun nameForCallStack(): String = getRef().nameForCallStack(this)

    /**
     * Convert the value to JSON.
     *
     * Return an error if the value or any contained value does not support conversion to JSON.
     */
    fun toJson(): Result<String> {
        // In Rust: serde_json::to_string(&self).map_err(|e| anyhow::anyhow!(e))
        return serializeImpl()
    }

    /**
     * Convert the value to a JSON value object.
     */
    fun toJsonValue(): Result<Any> {
        // In Rust: serde_json::to_value(self).map_err(|e| anyhow::anyhow!(e))
        return serializeImpl()
    }

    internal fun serializeImpl(): Result<String> = toJsonStringImpl()

    private fun escapeJsonString(s: String): String {
        val sb = StringBuilder()
        sb.append('"')
        for (i in 0 until s.length) {
            val c = s[i]
            when (c) {
                '\\' -> sb.append("\\\\")
                '"' -> sb.append("\\\"")
                '\b' -> sb.append("\\b")
                '\u000c' -> sb.append("\\f")
                '\n' -> sb.append("\\n")
                '\r' -> sb.append("\\r")
                '\t' -> sb.append("\\t")
                else -> {
                    val code = c.code
                    if (code < 0x20) {
                        sb.append("\\u")
                        val hex = code.toString(16)
                        for (j in 0 until (4 - hex.length)) {
                            sb.append('0')
                        }
                        sb.append(hex)
                    } else {
                        sb.append(c)
                    }
                }
            }
        }
        sb.append('"')
        return sb.toString()
    }

    private fun toJsonStringImpl(): Result<String> {
        if (isNone()) {
            return Result.success("null")
        }
        val b = unpackBool()
        if (b != null) {
            return Result.success(b.toString())
        }
        val num = unpackNum()
        if (num != null) {
            return when (num) {
                is NumRef.Int -> Result.success(num.value.toString())
                is NumRef.Float -> {
                    val d = num.value.value
                    if (d.isInfinite() || d.isNaN()) {
                        Result.failure(Exception("Cannot serialize non-finite float to JSON"))
                    } else {
                        Result.success(d.toString())
                    }
                }
            }
        }
        val s = unpackStr()
        if (s != null) {
            return Result.success(escapeJsonString(s))
        }

        // Cycle check guard for container types
        val guard = jsonStackPush(this)
        if (!guard.isSuccess) {
            return Result.failure(ToJsonCycleError(getType()))
        }

        try {
            // Check list
            val listGen = downcastRef<io.github.kotlinmania.starlark.values.types.list.ListGen<*>>()
            if (listGen != null) {
                val listLike = listGen.data as? io.github.kotlinmania.starlark.values.types.list.ListLike
                val content = listLike?.content() ?: emptyList()
                val sb = StringBuilder()
                sb.append('[')
                for (i in content.indices) {
                    if (i > 0) sb.append(',')
                    val serialized = content[i].toJsonStringImpl().getOrElse { return Result.failure(it) }
                    sb.append(serialized)
                }
                sb.append(']')
                return Result.success(sb.toString())
            }

            // Check tuple
            val tupleGen = downcastRef<io.github.kotlinmania.starlark.values.types.tuple.TupleGen<*>>()
            if (tupleGen != null) {
                val content = tupleGen.content()
                val sb = StringBuilder()
                sb.append('[')
                for (i in content.indices) {
                    if (i > 0) sb.append(',')
                    val item = (content[i] as? Value) ?: (content[i] as? FrozenValue)?.toValue()
                    if (item == null) {
                        return Result.failure(Exception("Unsupported tuple item type"))
                    }
                    val serialized = item.toJsonStringImpl().getOrElse { return Result.failure(it) }
                    sb.append(serialized)
                }
                sb.append(']')
                return Result.success(sb.toString())
            }

            // Check Dict
            val dictGen = downcastRef<io.github.kotlinmania.starlark.values.types.dict.DictGen<*>>()
            if (dictGen != null) {
                val entries =
                    when (val inner = dictGen.inner) {
                        is io.github.kotlinmania.starlark.values.types.dict.FrozenDictData -> {
                            inner.content.iter().map { Pair(it.first.toValue(), it.second.toValue()) }
                        }
                        is io.github.kotlinmania.starlark.values.types.dict.AtomicRef<*> -> {
                            val dict = inner.value as io.github.kotlinmania.starlark.values.types.dict.Dict
                            dict.content.iter().map { Pair(it.first, it.second) }
                        }
                        else -> emptySequence()
                    }
                val sb = StringBuilder()
                sb.append('{')
                var first = true
                for ((k, v) in entries) {
                    if (!first) sb.append(',')
                    first = false
                    val keyStr = k.unpackStr() ?: return Result.failure(Exception("Dict keys must be strings for JSON serialization"))
                    sb.append(escapeJsonString(keyStr))
                    sb.append(':')
                    val serialized = v.toJsonStringImpl().getOrElse { return Result.failure(it) }
                    sb.append(serialized)
                }
                sb.append('}')
                return Result.success(sb.toString())
            }

            // Check Struct
            val structVal = downcastRef<io.github.kotlinmania.starlark.values.types.structs.StructGen<Value>>()
            if (structVal != null) {
                val sb = StringBuilder()
                sb.append('{')
                var first = true
                for ((k, v) in structVal.fields.iter()) {
                    if (!first) sb.append(',')
                    first = false
                    sb.append(escapeJsonString(k))
                    sb.append(':')
                    val serialized = v.toJsonStringImpl().getOrElse { return Result.failure(it) }
                    sb.append(serialized)
                }
                sb.append('}')
                return Result.success(sb.toString())
            }
            val structFrozen = downcastRef<io.github.kotlinmania.starlark.values.types.structs.StructGen<FrozenValue>>()
            if (structFrozen != null) {
                val sb = StringBuilder()
                sb.append('{')
                var first = true
                for ((k, v) in structFrozen.fields.iter()) {
                    if (!first) sb.append(',')
                    first = false
                    sb.append(escapeJsonString(k))
                    sb.append(':')
                    val serialized = v.toValue().toJsonStringImpl().getOrElse { return Result.failure(it) }
                    sb.append(serialized)
                }
                sb.append('}')
                return Result.success(sb.toString())
            }

            // Check Namespace
            val namespaceVal = downcastRef<io.github.kotlinmania.starlark.values.types.namespace.NamespaceGen<Value>>()
            if (namespaceVal != null) {
                val sb = StringBuilder()
                sb.append('{')
                var first = true
                for ((k, v) in namespaceVal.fields.iter()) {
                    if (!first) sb.append(',')
                    first = false
                    sb.append(escapeJsonString(k))
                    sb.append(':')
                    val serialized = v.value.toJsonStringImpl().getOrElse { return Result.failure(it) }
                    sb.append(serialized)
                }
                sb.append('}')
                return Result.success(sb.toString())
            }
            val namespaceFrozen = downcastRef<io.github.kotlinmania.starlark.values.types.namespace.NamespaceGen<FrozenValue>>()
            if (namespaceFrozen != null) {
                val sb = StringBuilder()
                sb.append('{')
                var first = true
                for ((k, v) in namespaceFrozen.fields.iter()) {
                    if (!first) sb.append(',')
                    first = false
                    sb.append(escapeJsonString(k))
                    sb.append(':')
                    val serialized =
                        v.value
                            .toValue()
                            .toJsonStringImpl()
                            .getOrElse { return Result.failure(it) }
                    sb.append(serialized)
                }
                sb.append('}')
                return Result.success(sb.toString())
            }

            // Check Record
            val record =
                io.github.kotlinmania.starlark.values.types.record.RecordGen
                    .fromValue(this)
            if (record != null) {
                val sb = StringBuilder()
                sb.append('{')
                var first = true
                for ((k, v) in record.iter()) {
                    if (!first) sb.append(',')
                    first = false
                    sb.append(escapeJsonString(k))
                    sb.append(':')
                    val serialized = v.toJsonStringImpl().getOrElse { return Result.failure(it) }
                    sb.append(serialized)
                }
                sb.append('}')
                return Result.success(sb.toString())
            }

            // Check Enum value
            val enumValue = downcastRef<io.github.kotlinmania.starlark.values.types.enumeration.value.EnumValueGen>()
            if (enumValue != null) {
                return enumValue.value.toJsonStringImpl()
            }

            // Check Set
            val setVal = downcastRef<io.github.kotlinmania.starlark.values.types.set.SetGen<io.github.kotlinmania.starlark.values.types.set.RefCell>>()
            if (setVal != null) {
                val sb = StringBuilder()
                sb.append('[')
                var first = true
                val elements =
                    setVal.inner
                        .borrow()
                        .data.content
                        .iter()
                for (item in elements) {
                    if (!first) sb.append(',')
                    first = false
                    val serialized = item.toJsonStringImpl().getOrElse { return Result.failure(it) }
                    sb.append(serialized)
                }
                sb.append(']')
                return Result.success(sb.toString())
            }
            val setFrozen = downcastRef<io.github.kotlinmania.starlark.values.types.set.SetGen<io.github.kotlinmania.starlark.values.types.set.FrozenSetData>>()
            if (setFrozen != null) {
                val sb = StringBuilder()
                sb.append('[')
                var first = true
                val elements = setFrozen.inner.content.iter()
                for (item in elements) {
                    if (!first) sb.append(',')
                    first = false
                    val serialized = item.toValue().toJsonStringImpl().getOrElse { return Result.failure(it) }
                    sb.append(serialized)
                }
                sb.append(']')
                return Result.success(sb.toString())
            }

            // If none of the known types, throw error
            return Result.failure(Exception("Value of type `${getType()}` is not serializable to JSON"))
        } finally {
            guard.getOrThrow().close()
        }
    }

    /**
     * Forwards to [StarlarkValue.setAttr].
     */
    fun setAttr(attribute: String, allocValue: Value): Result<Unit> = getRef().setAttr(attribute, allocValue)

    /**
     * Forwards to [StarlarkValue.setAt].
     */
    fun setAt(index: Value, allocValue: Value): Result<Unit> = getRef().setAt(index, allocValue)

    /**
     * Forwards to [StarlarkValue.documentation].
     */
    fun documentation(): DocItem = getRef().documentation()

    /**
     * Produce an iterable from a value.
     */
    fun iterate(heap: Heap): Result<StarlarkIterator> =
        getRef().iterate(this, heap).map { iter ->
            StarlarkIterator.new(iter, heap)
        }

    /**
     * Get the [Hashed] version of this [Value].
     */
    override fun getHashed(): Result<Hashed<Value>> {
        val str = unpackStarlarkStr()
        val hash =
            try {
                if (str != null) {
                    str.getHash().getOrThrow()
                } else {
                    getHash().getOrThrow()
                }
            } catch (e: Exception) {
                return Result.failure(e)
            }
        return Result.success(Hashed.newUnchecked(hash, this))
    }

    /**
     * Are two values equal. If the values are of different types it will
     * return `false`. It will only error if there is excessive recursion.
     */
    override fun equals(other: Value): Result<Boolean> =
        if (ptrEq(other)) {
            Result.success(true)
        } else {
            equalsNotPtrEq(other)
        }

    private fun equalsNotPtrEq(other: Value): Result<Boolean> {
        val guard =
            try {
                stackGuard()
            } catch (e: Exception) {
                return Result.failure(e)
            }
        try {
            return getRef().equals(other)
        } finally {
            guard.close()
        }
    }

    /**
     * How are two values comparable. For values of different types will return error.
     */
    override fun compare(other: Value): Result<Int> {
        val guard =
            try {
                stackGuard()
            } catch (e: Exception) {
                return Result.failure(e)
            }
        try {
            return getRef().compare(other)
        } finally {
            guard.close()
        }
    }

    /**
     * Describe the value, in order to get its metadata in a way that could be used
     * to generate prototypes, help information or whatever other descriptive text
     * is required.
     * Plan is to make this return a data type at some point in the future, possibly
     * move on to `StarlarkValue` and include data from members.
     */
    fun describe(name: String): String =
        if (getType() == FUNCTION_TYPE) {
            "def ${toRepr().replace(" = ...", " = None")}: pass"
        } else {
            "# $name = ${toRepr()}"
        }

    /**
     * Call `exportAs` on the underlying value, but only if the type is mutable.
     * Otherwise, does nothing.
     */
    fun exportAs(variableName: String, eval: Evaluator): Result<Unit> = getRef().exportAs(variableName, eval)

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
        val v =
            try {
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
        val result =
            if (methods != null) {
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
    inline fun <reified T : Any> requestValue(): T? = requestValueImpl(this)

    /**
     * Return a string usable for error messages.
     *
     * If the value is too large, it may be truncated.
     */
    fun toStringForTypeError(): String = displayForTypeError()

    private fun displayForTypeError(): String {
        fun splitAtSafe(s: String, index: Int): Pair<String, String> {
            // In Kotlin strings are always valid character sequences
            val safeIndex = index.coerceIn(0, s.length)
            return Pair(s.substring(0, safeIndex), s.substring(safeIndex))
        }

        var repr = toRepr()
        val maxLen = 60

        if (repr.length > maxLen && repr.toList().size > maxLen) {
            val truncated = "<<...>>"

            // 1/3 from back, 2/3 from front, because front is usually more interesting.
            val takeFromBack = maxOf(0, maxLen - truncated.length) / 3
            val takeFromFront = takeFromBack * 2

            // Resulting repr is approximately `maxLen` long.
            repr =
                buildString {
                    append(splitAtSafe(repr, takeFromFront).first)
                    append(truncated)
                    append(splitAtSafe(repr, maxOf(0, repr.length - takeFromBack)).second)
                }
        }

        return "${getType()} (repr: $repr)"
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
                @Suppress("UNCHECKED_CAST")
                getRef().downcastRef<StarlarkStr>() as? T
            } else {
                null
            }
        }
        if (PointerI32.typeIsPointerI32<T>()) {
            return if (unpackInlineInt() != null) {
                @Suppress("UNCHECKED_CAST")
                PointerI32.fromRawInt(ptr.unpackIntValue()) as? T
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
                @Suppress("UNCHECKED_CAST")
                PointerI32.fromRawInt(ptr.unpackIntValue()) as? T
            } else {
                null
            }
        }
        val ref = getRef()
        return if (clazz.isInstance(ref)) {
            @Suppress("UNCHECKED_CAST")
            ref as Any as T
        } else {
            null
        }
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
    override fun writeHash(hasher: StarlarkHasher): Result<Unit> = getRef().writeHash(hasher)

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

    // In Rust, Display for Value reuses repr (strings display with quotes).
    // This is equivalent to toRepr().
    override fun toString(): String = toRepr()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Value) return false
        return equals(other).getOrDefault(false)
    }

    override fun hashCode(): Int = ptr.raw().hashCode()

    fun debug(): String = debugValue("Value", this)
}

// Kotlin: Coerce/CoerceKey traits not needed; type safety is handled differently.

fun Value.Companion.default(): Value = Value.newNone()

fun FrozenValue.Companion.default(): FrozenValue = FrozenValue.newNone()

fun Value.equivalent(key: FrozenValue): Boolean = key.equals(this).getOrThrow()

fun FrozenValue.equivalent(key: Value): Boolean = this.equals(key).getOrThrow()

/**
 * A [Value] that can never be changed. Can be converted back to a [Value] with [toValue].
 *
 * A [FrozenValue] exists on a `FrozenHeap`, which in turn can be kept
 * alive by a `FrozenHeapRef`. If the frozen heap gets dropped
 * while a [FrozenValue] from it still exists, the program will probably crash, so be careful
 * when working directly with [FrozenValue]s. See the type `OwnedFrozenValue`
 * for a little bit more safety.
 */
class FrozenValue internal constructor(
    internal val ptr: FrozenPointer,
) : ValueLike {
    companion object {
        /**
         * Create a new [FrozenValue] from an [AValueHeader] pointer.
         */
        internal fun newPtr(x: AValueHeader, isStr: Boolean): FrozenValue = FrozenValue(FrozenPointer.newFrozen(x.index, isStr))

        /**
         * Create a new [FrozenValue] from an [AValueHeader], querying whether it's a string.
         */
        internal fun newPtrQueryIsStr(x: AValueHeader): FrozenValue {
            val isString = x.vtable.isStr
            return newPtr(x, isString)
        }

        /**
         * Create a new [FrozenValue] from a raw usize with string tag.
         */
        internal fun newPtrUsizeWithStrTag(x: Long): FrozenValue = FrozenValue(FrozenPointer.newFrozenUsizeWithStrTag(x))

        /**
         * Create a new value representing `None` in Starlark.
         */
        fun newNone(): FrozenValue = VALUE_NONE.toFrozenValue()

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
        internal fun newInt(x: InlineInt): FrozenValue = FrozenValue(FrozenPointer.newInt(x.toI32()))

        /**
         * Create a new int for testing purposes.
         */
        internal fun testingNewInt(x: Int): FrozenValue = newInt(InlineInt.tryFrom(x).getOrThrow())

        /**
         * Create a new empty string.
         */
        internal fun newEmptyString(): FrozenValue = VALUE_EMPTY_STRING.unpack()

        /**
         * Create a new empty tuple.
         */
        internal fun newEmptyTuple(): FrozenValue = VALUE_EMPTY_TUPLE.toFrozenValue()

        /**
         * Create a new empty list.
         */
        fun newEmptyList(): FrozenValue = VALUE_EMPTY_FROZEN_LIST.toFrozenValue()

        /**
         * Create a new empty dict.
         */
        fun newEmptyDict(): FrozenValue = VALUE_EMPTY_FROZEN_DICT.toFrozenValue()

        /**
         * Convert from [FrozenValue] (ValueLike factory method).
         * For FrozenValue, simply returns itself.
         */
        fun fromFrozenValue(v: FrozenValue): FrozenValue = v
    }

    /**
     * Get the underlying raw pointer.
     */
    internal fun ptrValue(): RawPointer = ptr.raw()

    /**
     * Is a value a Starlark `None`.
     */
    fun isNone(): Boolean = toValue().isNone()

    /**
     * Return the `bool` if the value is a boolean, otherwise `null`.
     */
    fun unpackBool(): Boolean? = toValue().unpackBool()

    /**
     * Obtain the underlying integer if it fits in an `Int`.
     * Note floats are not considered integers, i.e. `unpackI32` for `1.0` will return `null`.
     */
    fun unpackI32(): Int? = toValue().unpackI32()

    /**
     * Unpack inline integer value.
     */
    internal fun unpackInlineInt(): InlineInt? = toValue().unpackInlineInt()

    /**
     * Check if this value is a string.
     */
    internal fun isStr(): Boolean = toValue().isStr()

    /**
     * The resulting `str` is alive as long as the `FrozenHeap` is,
     * but we don't have that lifetime available to us. Therefore,
     * we cheat a little, and use the lifetime of the `FrozenValue`.
     * Because of this cheating, we don't expose it outside Starlark.
     */
    internal fun unpackStr(): String? = toValue().unpackStr()

    /**
     * Convert a [FrozenValue] back to a [Value].
     */
    override fun toValue(): Value = Value.newFrozen(this)

    /**
     * Is this type builtin? We perform certain optimizations only on builtin types
     * because we know they have well defined semantics.
     */
    internal fun isBuiltin(): Boolean {
        // The list is not comprehensive, this is fine.
        // If some type is not listed here, some optimizations won't work for this type.
        return isNone() ||
            isStr() ||
            unpackBool() != null ||
            NumRef.unpackValue(toValue()).getOrNull()?.let { true } ?: false ||
            FrozenListData.fromFrozenValue(this) != null ||
            FrozenDictRef.fromFrozenValue(this) != null ||
            FrozenValueTyped.new<FrozenTuple>(this) != null ||
            FrozenValueTyped.new<Range>(this) != null ||
            FrozenValueTyped.new<FrozenDef>(this) != null ||
            FrozenValueTyped.new<NativeFunction>(this) != null ||
            FrozenValueTyped.new<FrozenStruct>(this) != null ||
            FrozenValueTyped.new<RecordTypeGen>(this) != null ||
            FrozenValueTyped.new<FrozenRecord>(this) != null ||
            FrozenValueTyped.new<EnumType>(this) != null ||
            FrozenValueTyped.new<FrozenEnumValue>(this) != null
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
        val bm = FrozenValueTyped.new<FrozenBoundMethod>(this)
        if (bm != null) {
            return bm
                .asRef()
                .method
                .asRef()
                .speculativeExecSafe
        }
        return false
    }

    /**
     * `self == b` is `ptrEq`.
     */
    internal fun eqIsPtrEq(): Boolean {
        // Note `int` is not `ptr_eq` because `int` can be equal to `float`.

        // If a value does not override equality, it is `ptr_eq`.
        if (!toValue().getRef().vtable().hasEquals) {
            return true
        }
        // Strings of length <= 1 are statically allocated.
        val str = unpackStr()
        if (str != null && str.length <= 1) {
            return true
        }
        // Empty tuple is statically allocated.
        val tuple = Tuple.fromValue(toValue())
        return tuple != null && tuple.len() == 0
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
    override fun equals(other: Value): Result<Boolean> = toValue().equals(other)

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
    override fun writeHash(hasher: StarlarkHasher): Result<Unit> = toValue().writeHash(hasher)

    /**
     * How are two values comparable. For values of different types will return error.
     */
    override fun compare(other: Value): Result<Int> = toValue().compare(other)

    override fun toString(): String = toValue().toString()

    fun debug(): String = debugValue("FrozenValue", Value.newFrozen(this))

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FrozenValue) return false
        return toValue() == other.toValue()
    }

    override fun hashCode(): Int = ptr.raw().hashCode()

    // ValueLike impl

    override fun fromFrozenValue(v: FrozenValue): ValueLike = v

    override fun freeze(
        @Suppress("unused") freezer: Freezer,
    ): Result<FrozenValue> = Result.success(this)

    // ValueLike interface requires non-reified KClass version
    override fun <T : StarlarkValue> downcastRef(clazz: KClass<T>): T? = toValue().downcastRef(clazz)

    /**
     * Downcast to a specific [StarlarkValue] type.
     */
    internal inline fun <reified T : StarlarkValue> downcastRef(): T? = toValue().downcastRef<T>()
}

// then delegates to erased_serde::serialize(self.get_ref().as_serialize(), s).
// The cycle detection logic is in Value.serializeImpl().
fun Value.serialize(): Result<String> = serializeImpl()

// Rust: self.to_value().serialize(s)
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
 * The methods on this trait are those required to implement containers,
 * allowing implementations of `ComplexValue` to be agnostic of their contained type.
 * For details about each function, see the documentation for [Value],
 * which provides the same functions (and more).
 */
interface ValueLike : ValueLifetimeless {
    /**
     * `StringValue` or `FrozenStringValue`.
     */

    /**
     * Produce a [Value] regardless of the type you are starting with.
     */
    fun toValue(): Value

    /**
     * Convert from [FrozenValue].
     */
    fun fromFrozenValue(v: FrozenValue): ValueLike

    /**
     * Call this value as a function with given arguments.
     */
    fun invoke(args: Arguments, eval: Evaluator): Result<Value> = toValue().invoke(args, eval)

    /**
     * Hash the value.
     */
    fun writeHash(hasher: StarlarkHasher): Result<Unit>

    /**
     * Get hash value.
     */
    fun getHashed(): Result<Hashed<ValueLike>> {
        val v = toValue()
        val str = v.unpackStarlarkStr()
        val hash =
            try {
                if (str != null) {
                    str.getHash().getOrThrow()
                } else {
                    v.getHash().getOrThrow()
                }
            } catch (e: Exception) {
                return Result.failure(e)
            }
        return Result.success(Hashed.newUnchecked(hash, this))
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
            Result.failure(
                ValueValueError.WrongType(
                    clazz.simpleName ?: "Unknown",
                    toValue().toStringForTypeError(),
                ),
            )
        }
    }
}

// Kotlin: Sealed/marker traits not needed; implemented via interface inheritance.

// Static value references are imported from their defining modules.
// See: VALUE_NONE (NoneType.kt), VALUE_FALSE_TRUE (bool/Value.kt),
// VALUE_EMPTY_STRING (StaticString.kt), VALUE_EMPTY_TUPLE (tuple/Value.kt),
// VALUE_EMPTY_FROZEN_LIST (list/Value.kt), VALUE_EMPTY_FROZEN_DICT (dict/Value.kt)
