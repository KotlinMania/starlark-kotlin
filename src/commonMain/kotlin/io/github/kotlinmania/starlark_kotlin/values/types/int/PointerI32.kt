// port-lint: source src/values/types/int/pointer_i32.rs
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

import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.kotlinmania.starlark_kotlin.collections.StarlarkHashValue
import io.github.kotlinmania.starlark_kotlin.collections.StarlarkHasher
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.typing.TyBasic
import io.github.kotlinmania.starlark_kotlin.typing.TypingBinOp
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.ValueError
import io.github.kotlinmania.starlark_kotlin.values.layout.AValueDyn
import io.github.kotlinmania.starlark_kotlin.values.layout.AValueVTable
import io.github.kotlinmania.starlark_kotlin.values.layout.StarlarkValueRawPtr
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.const_type_id.ConstTypeId
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.pointer.RawPointer
import io.github.kotlinmania.starlark_kotlin.values.starlark_type_id.StarlarkTypeId
import io.github.kotlinmania.starlark_kotlin.values.types.bigint.StarlarkBigInt
import io.github.kotlinmania.starlark_kotlin.values.types.num.NumRef
import io.github.kotlinmania.starlark_kotlin.values.types.num.NumTy
import io.github.kotlinmania.starlark_kotlin.values.types.num.typecheckNumBinOp

/** The result of calling `type()` on integers. */
// Rust: pub const INT_TYPE: &str = "int";
const val INT_TYPE: String = "int"

/**
 * Integer value stored inline using pointer tagging.
 *
 * In Rust, this is not a real type -- a pointer to it is secretly an i32.
 * In Kotlin, we maintain the semantic interface while adapting to platform
 * constraints (no raw pointer manipulation).
 *
 * The canonical type for int is [StarlarkBigInt]; this type shares
 * the same Starlark `"int"` type name.
 */
// Rust: pub(crate) struct PointerI32 { _private: () }
internal class PointerI32 internal constructor(
    /** The inline integer value this pointer represents. */
    private val value: InlineInt,
) {

    /** Construct from a raw i32 value (used by layout code). */
    internal constructor(rawI32: Int) : this(InlineInt.newUnchecked(rawI32))

    companion object {
        /**
         * Creates a [PointerI32] from a [RawPointer].
         *
         * In Rust, this reinterprets the raw pointer as a reference. In Kotlin,
         * we extract the inline int from the tagged pointer.
         */
        // Rust: pub(crate) unsafe fn from_raw_pointer_unchecked(raw_pointer: RawPointer) -> &'static PointerI32
        internal fun fromRawPointerUnchecked(rawPointer: RawPointer): PointerI32 {
            require(rawPointer.isInt()) { "RawPointer must be an int" }
            return PointerI32(InlineInt.newUnchecked(rawPointer.unpackInt()!!))
        }

        // Rust: pub(crate) fn vtable() -> &'static AValueVTable
        // Rust: AValueVTable::new::<AValueBasic<PointerI32>>()
        internal val VTABLE: AValueVTable by lazy {
            AValueVTable(
                staticTypeOfValue = ConstTypeId.of<PointerI32>(),
                starlarkTypeId = StarlarkTypeId.fromTypeId(ConstTypeId.of<PointerI32>()),
                typeName = INT_TYPE,
                isStr = false,
                memorySizeFn = { _ -> error("PointerI32 has no heap allocation") },
                heapFreezeFn = { _, _ -> error("PointerI32 cannot be frozen") },
                heapCopyFn = { _, _ -> error("PointerI32 cannot be heap-copied") },
                starlarkValue = PointerI32StarlarkValueAdapter,
            )
        }

        internal fun vtable(): AValueVTable = VTABLE

        // Rust: pub(crate) fn type_is_pointer_i32<'v, T: StarlarkValue<'v>>() -> bool
        internal inline fun <reified T : StarlarkValue> typeIsPointerI32(): Boolean {
            return T::class == PointerI32::class
        }
    }

    // Rust: pub(crate) fn get(&self) -> InlineInt
    internal fun get(): InlineInt = value

    // Rust: pub(crate) fn as_avalue_dyn(&'static self) -> AValueDyn<'static>
    internal fun asAvalueDyn(): AValueDyn {
        return AValueDyn(StarlarkValueRawPtr.newPointerI32(this), vtable())
    }

    /** This operation is expensive, use only if you have to. */
    // Rust: fn to_bigint(&self) -> BigInt
    private fun toBigInt(): BigInteger {
        return get().toBigInt()
    }

    // --- PartialEq: pointer identity in Rust ---
    // Rust: fn eq(&self, other: &Self) -> bool { ptr::eq(self, other) }
    override fun equals(other: Any?): Boolean {
        return this === other
    }

    override fun hashCode(): Int = value.hashCode()

    // --- Display ---
    // Rust: fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result { write!(f, "{}", self.get()) }
    override fun toString(): String = get().toString()

    // --- StarlarkValue implementation ---
    // Rust: #[starlark_value(type = INT_TYPE)]
    // Rust: impl<'v> StarlarkValue<'v> for PointerI32
    // Rust: type Canonical = StarlarkBigInt;

    // Rust: fn is_special(_: Private) -> bool { true }
    fun isSpecial(): Boolean = true

    // Rust: fn equals(&self, other: Value) -> crate::Result<bool>
    fun equals(other: Value): Result<Boolean> {
        return Result.success(
            NumRef.Int(StarlarkIntRef.Small(get())) == other.unpackNum()
        )
    }

    // Rust: fn to_bool(&self) -> bool
    fun toBool(): Boolean = get().toI32() != 0

    // Rust: fn write_hash(&self, hasher: &mut StarlarkHasher) -> crate::Result<()>
    fun writeHash(hasher: StarlarkHasher): Result<Unit> {
        hasher.write(NumRef.Int(StarlarkIntRef.Small(get())).getHash64())
        return Result.success(Unit)
    }

    // Rust: fn get_hash(&self, _private: Private) -> crate::Result<StarlarkHashValue>
    fun getHash(): Result<StarlarkHashValue> {
        return Result.success(NumRef.Int(StarlarkIntRef.Small(get())).getHash())
    }

    // Rust: fn plus(&self, _heap: Heap<'v>) -> crate::Result<Value<'v>>
    fun plus(@Suppress("UNUSED_PARAMETER") heap: Heap): Result<Value> {
        return Result.success(Value.newInt(get()))
    }

    // Rust: fn minus(&self, heap: Heap<'v>) -> crate::Result<Value<'v>>
    fun minus(heap: Heap): Result<Value> {
        return Result.success(heap.alloc(-StarlarkIntRef.Small(get())))
    }

    // Rust: fn add(&self, other: Value<'v>, heap: Heap<'v>) -> Option<crate::Result<Value<'v>>>
    fun add(other: Value, heap: Heap): Result<Value>? {
        val otherNum = other.unpackNum() ?: return null
        return Result.success(heap.alloc(NumRef.Int(StarlarkIntRef.Small(get())) + otherNum))
    }

    // Rust: fn sub(&self, other: Value<'v>, heap: Heap<'v>) -> crate::Result<Value<'v>>
    fun sub(other: Value, heap: Heap): Result<Value> {
        val otherNum = other.unpackNum()
            ?: return ValueError.unsupportedWith(INT_TYPE, "-", other)
        return Result.success(heap.alloc(NumRef.Int(StarlarkIntRef.Small(get())) - otherNum))
    }

    // Rust: fn mul(&self, other: Value<'v>, heap: Heap<'v>) -> Option<crate::Result<Value<'v>>>
    fun mul(other: Value, heap: Heap): Result<Value>? {
        val otherNum = other.unpackNum() ?: return null
        return Result.success(heap.alloc(NumRef.Int(StarlarkIntRef.Small(get())) * otherNum))
    }

    // Rust: fn div(&self, other: Value<'v>, heap: Heap<'v>) -> crate::Result<Value<'v>>
    fun div(other: Value, heap: Heap): Result<Value> {
        val otherNum = other.unpackNum()
            ?: return ValueError.unsupportedWith(INT_TYPE, "/", other)
        return NumRef.Int(StarlarkIntRef.Small(get())).div(otherNum).map { heap.alloc(it) }
    }

    // Rust: fn percent(&self, other: Value<'v>, heap: Heap<'v>) -> crate::Result<Value<'v>>
    fun percent(other: Value, heap: Heap): Result<Value> {
        val otherNum = other.unpackNum()
            ?: return ValueError.unsupportedWith(INT_TYPE, "%", other)
        return NumRef.Int(StarlarkIntRef.Small(get())).percent(otherNum).map { heap.alloc(it) }
    }

    // Rust: fn floor_div(&self, other: Value<'v>, heap: Heap<'v>) -> crate::Result<Value<'v>>
    fun floorDiv(other: Value, heap: Heap): Result<Value> {
        val otherNum = other.unpackNum()
            ?: return ValueError.unsupportedWith(INT_TYPE, "//", other)
        return NumRef.Int(StarlarkIntRef.Small(get())).floorDiv(otherNum).map { heap.alloc(it) }
    }

    // Rust: fn compare(&self, other: Value) -> crate::Result<Ordering>
    fun compare(other: Value): Result<Int> {
        val otherNum = other.unpackNum()
            ?: return ValueError.unsupportedWith(INT_TYPE, "compare", other)
        return Result.success(NumRef.Int(StarlarkIntRef.Small(get())).compareTo(otherNum))
    }

    // Rust: fn bit_and(&self, other: Value, heap: Heap<'v>) -> crate::Result<Value<'v>>
    fun bitAnd(other: Value, heap: Heap): Result<Value> {
        return when (val rhs = StarlarkIntRef.unpack(other)) {
            null -> ValueError.unsupportedWith(INT_TYPE, "&", other)
            is StarlarkIntRef.Small -> Result.success(Value.newInt(get() and rhs.value))
            is StarlarkIntRef.Big -> Result.success(
                heap.alloc(StarlarkInt.from(toBigInt() and rhs.value.get()))
            )
        }
    }

    // Rust: fn bit_or(&self, other: Value, heap: Heap<'v>) -> crate::Result<Value<'v>>
    fun bitOr(other: Value, heap: Heap): Result<Value> {
        return when (val rhs = StarlarkIntRef.unpack(other)) {
            null -> ValueError.unsupportedWith(INT_TYPE, "|", other)
            is StarlarkIntRef.Small -> Result.success(Value.newInt(get() or rhs.value))
            is StarlarkIntRef.Big -> Result.success(
                heap.alloc(StarlarkInt.from(toBigInt() or rhs.value.get()))
            )
        }
    }

    // Rust: fn bit_xor(&self, other: Value, heap: Heap<'v>) -> crate::Result<Value<'v>>
    fun bitXor(other: Value, heap: Heap): Result<Value> {
        return when (val rhs = StarlarkIntRef.unpack(other)) {
            null -> ValueError.unsupportedWith(INT_TYPE, "^", other)
            is StarlarkIntRef.Small -> Result.success(Value.newInt(get() xor rhs.value))
            is StarlarkIntRef.Big -> Result.success(
                heap.alloc(StarlarkInt.from(toBigInt() xor rhs.value.get()))
            )
        }
    }

    // Rust: fn bit_not(&self, _heap: Heap<'v>) -> crate::Result<Value<'v>>
    fun bitNot(@Suppress("UNUSED_PARAMETER") heap: Heap): Result<Value> {
        return Result.success(Value.newInt(!get()))
    }

    // Rust: fn left_shift(&self, other: Value, heap: Heap<'v>) -> crate::Result<Value<'v>>
    fun leftShift(other: Value, heap: Heap): Result<Value> {
        val rhs = StarlarkIntRef.unpack(other)
            ?: return ValueError.unsupportedWith(INT_TYPE, "<<", other)
        return StarlarkIntRef.Small(get()).leftShift(rhs).map { heap.alloc(it) }
    }

    // Rust: fn right_shift(&self, other: Value, heap: Heap<'v>) -> crate::Result<Value<'v>>
    fun rightShift(other: Value, heap: Heap): Result<Value> {
        val rhs = StarlarkIntRef.unpack(other)
            ?: return ValueError.unsupportedWith(INT_TYPE, ">>", other)
        return StarlarkIntRef.Small(get()).rightShift(rhs).map { heap.alloc(it) }
    }

    /**
     * Type-checks a binary operation.
     * This is dead code because the canonical int type is [StarlarkBigInt],
     * but kept for consistency.
     */
    // Rust: fn bin_op_ty(op: TypingBinOp, rhs: &TyBasic) -> Option<Ty>
    fun binOpTy(op: TypingBinOp, rhs: TyBasic): Ty? {
        return typecheckNumBinOp(NumTy.Int, op, rhs)
    }

    // Rust: fn typechecker_ty(&self) -> Option<Ty>
    fun typecheckerTy(): Ty = Ty.int()
}

/**
 * Adapter providing [StarlarkValue] interface for the [PointerI32] vtable.
 *
 * [PointerI32] does not directly implement [StarlarkValue] (matching Rust where
 * the `#[starlark_value]` proc macro generates the impl), so this adapter
 * supplies the required type metadata for the vtable.
 */
private object PointerI32StarlarkValueAdapter : StarlarkValue {
    override val TYPE: String get() = INT_TYPE
    override fun isSpecial(): Boolean = true
    override fun typecheckerTy(): Ty = Ty.int()
}
