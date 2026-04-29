// port-lint: source values/types/int/pointer_i32.rs
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

import com.ionspin.kotlin.bignum.integer.BigInteger
import starlarkmap.StarlarkHashValue
import starlarkmap.StarlarkHasher
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.typing.TyBasic
import io.github.kotlinmania.starlark.typing.TypingBinOp
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.ValueError
import io.github.kotlinmania.starlark.values.layout.AValueDyn
import io.github.kotlinmania.starlark.values.layout.AValueVTable
import io.github.kotlinmania.starlark.values.layout.ConstTypeId
import io.github.kotlinmania.starlark.values.layout.StarlarkValueRawPtr
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.layout.RawPointer
import io.github.kotlinmania.starlark.values.starlarktypeid.StarlarkTypeId
import io.github.kotlinmania.starlark.values.types.bigint.StarlarkBigInt
import io.github.kotlinmania.starlark.values.types.num.Num
import io.github.kotlinmania.starlark.values.types.num.NumRef
import io.github.kotlinmania.starlark.values.types.num.NumTy
import io.github.kotlinmania.starlark.values.types.num.typecheckNumBinOp
import io.github.kotlinmania.starlark.typing.oracle.TypingBinOp as OracleTypingBinOp

/** The result of calling `type()` on integers. */
const val INT_TYPE: String = "int"

/**
 * Integer value stored inline using pointer tagging.
 *
 * In Kotlin, we maintain the semantic interface while adapting to platform
 * constraints (no raw pointer manipulation).
 *
 * The canonical type for int is [StarlarkBigInt]; this type shares
 * the same Starlark `"int"` type name.
 */
internal class PointerI32 internal constructor(
    /** The inline integer value this pointer represents. */
    private val value: InlineInt,
) : StarlarkValue {

    override val TYPE: String get() = INT_TYPE

    /** Construct from a raw i32 value (used by layout code). */
    internal constructor(rawI32: Int) : this(InlineInt.newUnchecked(rawI32))

    companion object {
        /**
         * Creates a [PointerI32] from a [RawPointer].
         *
         * we extract the inline int from the tagged pointer.
         */
        internal fun fromRawPointerUnchecked(rawPointer: RawPointer): PointerI32 {
            require(rawPointer.isInt()) { "RawPointer must be an int" }
            return PointerI32(InlineInt.newUnchecked(rawPointer.unpackInt()!!))
        }

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

        internal inline fun <reified T : StarlarkValue> typeIsPointerI32(): Boolean {
            return T::class == PointerI32::class
        }
    }

    internal fun get(): InlineInt = value

    internal fun asAvalueDyn(): AValueDyn {
        return AValueDyn(StarlarkValueRawPtr.newPointerI32(this), vtable())
    }

    /** This operation is expensive, import only if you have to. */
    private fun toBigint(): BigInteger {
        return get().toBigint()
    }

    // --- PartialEq: pointer identity in Rust ---
    override fun equals(other: Any?): Boolean {
        return this === other
    }

    override fun hashCode(): Int = value.hashCode()

    // --- Display ---
    override fun toString(): String = get().toString()

    // --- StarlarkValue implementation ---
    // Canonical type is StarlarkBigInt.

    override fun isSpecial(): Boolean = true

    override fun equals(other: Value): Result<Boolean> {
        return Result.success(
            NumRef.Int(StarlarkIntRef.Small(get())) == other.unpackNum()
        )
    }

    override fun toBool(): Boolean = get().toI32() != 0

    override fun writeHash(hasher: StarlarkHasher): Result<Unit> {
        hasher.writeU64(NumRef.Int(StarlarkIntRef.Small(get())).getHash64())
        return Result.success(Unit)
    }

    override fun getHash(): Result<StarlarkHashValue> {
        return Result.success(NumRef.Int(StarlarkIntRef.Small(get())).getHash())
    }

    override fun plus(heap: Heap): Result<Value> {
        return Result.success(Value.newInt(get()))
    }

    override fun minus(heap: Heap): Result<Value> {
        return Result.success(Num.Int(-StarlarkIntRef.Small(get())).allocValue(heap))
    }

    override fun add(other: Value, heap: Heap): Result<Value>? {
        val otherNum = other.unpackNum() ?: return null
        return Result.success((NumRef.Int(StarlarkIntRef.Small(get())) + otherNum).allocValue(heap))
    }

    override fun sub(other: Value, heap: Heap): Result<Value> {
        val otherNum = other.unpackNum()
            ?: return ValueError.unsupportedWith(INT_TYPE, "-", other)
        return Result.success((NumRef.Int(StarlarkIntRef.Small(get())) - otherNum).allocValue(heap))
    }

    override fun mul(other: Value, heap: Heap): Result<Value>? {
        val otherNum = other.unpackNum() ?: return null
        return Result.success((NumRef.Int(StarlarkIntRef.Small(get())) * otherNum).allocValue(heap))
    }

    override fun div(other: Value, heap: Heap): Result<Value> {
        val otherNum = other.unpackNum()
            ?: return ValueError.unsupportedWith(INT_TYPE, "/", other)
        return NumRef.Int(StarlarkIntRef.Small(get())).div(otherNum).map { Num.Float(it).allocValue(heap) }
    }

    override fun percent(other: Value, heap: Heap): Result<Value> {
        val otherNum = other.unpackNum()
            ?: return ValueError.unsupportedWith(INT_TYPE, "%", other)
        return NumRef.Int(StarlarkIntRef.Small(get())).percent(otherNum).map { it.allocValue(heap) }
    }

    override fun floorDiv(other: Value, heap: Heap): Result<Value> {
        val otherNum = other.unpackNum()
            ?: return ValueError.unsupportedWith(INT_TYPE, "//", other)
        return NumRef.Int(StarlarkIntRef.Small(get())).floorDiv(otherNum).map { it.allocValue(heap) }
    }

    override fun compare(other: Value): Result<Int> {
        val otherNum = other.unpackNum()
            ?: return ValueError.unsupportedWith(INT_TYPE, "compare", other)
        return Result.success(NumRef.Int(StarlarkIntRef.Small(get())).compareTo(otherNum))
    }

    override fun bitAnd(other: Value, heap: Heap): Result<Value> {
        return when (val rhs = StarlarkIntRef.unpack(other)) {
            null -> ValueError.unsupportedWith(INT_TYPE, "&", other)
            is StarlarkIntRef.Small -> Result.success(Value.newInt(get() and rhs.value))
            is StarlarkIntRef.Big -> Result.success(
                Num.Int(StarlarkInt.from(toBigint() and rhs.value.get())).allocValue(heap)
            )
        }
    }

    override fun bitOr(other: Value, heap: Heap): Result<Value> {
        return when (val rhs = StarlarkIntRef.unpack(other)) {
            null -> ValueError.unsupportedWith(INT_TYPE, "|", other)
            is StarlarkIntRef.Small -> Result.success(Value.newInt(get() or rhs.value))
            is StarlarkIntRef.Big -> Result.success(
                Num.Int(StarlarkInt.from(toBigint() or rhs.value.get())).allocValue(heap)
            )
        }
    }

    override fun bitXor(other: Value, heap: Heap): Result<Value> {
        return when (val rhs = StarlarkIntRef.unpack(other)) {
            null -> ValueError.unsupportedWith(INT_TYPE, "^", other)
            is StarlarkIntRef.Small -> Result.success(Value.newInt(get() xor rhs.value))
            is StarlarkIntRef.Big -> Result.success(
                Num.Int(StarlarkInt.from(toBigint() xor rhs.value.get())).allocValue(heap)
            )
        }
    }

    override fun bitNot(heap: Heap): Result<Value> {
        return Result.success(Value.newInt(!get()))
    }

    override fun leftShift(other: Value, heap: Heap): Result<Value> {
        val rhs = StarlarkIntRef.unpack(other)
            ?: return ValueError.unsupportedWith(INT_TYPE, "<<", other)
        return StarlarkIntRef.Small(get()).leftShift(rhs).map { Num.Int(it).allocValue(heap) }
    }

    override fun rightShift(other: Value, heap: Heap): Result<Value> {
        val rhs = StarlarkIntRef.unpack(other)
            ?: return ValueError.unsupportedWith(INT_TYPE, ">>", other)
        return StarlarkIntRef.Small(get()).rightShift(rhs).map { Num.Int(it).allocValue(heap) }
    }

    /**
     * Type-checks a binary operation.
     * This is dead code because the canonical int type is [StarlarkBigInt],
     * but kept for consistency.
     */
    override fun binOpTy(op: TypingBinOp, rhs: TyBasic): Ty? {
        val oracleOp = when (op) {
            TypingBinOp.Add -> OracleTypingBinOp.ADD
            TypingBinOp.Sub -> OracleTypingBinOp.SUB
            TypingBinOp.Mul -> OracleTypingBinOp.MUL
            TypingBinOp.Div -> OracleTypingBinOp.DIV
            TypingBinOp.FloorDiv -> OracleTypingBinOp.FLOOR_DIV
            TypingBinOp.Percent -> OracleTypingBinOp.PERCENT
            TypingBinOp.BitAnd -> OracleTypingBinOp.BIT_AND
            TypingBinOp.BitOr -> OracleTypingBinOp.BIT_OR
            TypingBinOp.BitXor -> OracleTypingBinOp.BIT_XOR
            TypingBinOp.LeftShift -> OracleTypingBinOp.LEFT_SHIFT
            TypingBinOp.RightShift -> OracleTypingBinOp.RIGHT_SHIFT
            TypingBinOp.In -> OracleTypingBinOp.IN
            TypingBinOp.Less -> OracleTypingBinOp.LESS
        }
        return typecheckNumBinOp(NumTy.Int, oracleOp, rhs)
    }

    override fun typecheckerTy(): Ty = Ty.int()
}

/**
 * Adapter providing [StarlarkValue] interface for the [PointerI32] vtable.
 *
 * [PointerI32] does not directly implement [StarlarkValue] (matching Rust where
 * the `(starlarkValue)` proc macro generates the implementation), so this adapter
 * supplies the required type metadata for the vtable.
 */
private object PointerI32StarlarkValueAdapter : StarlarkValue {
    override val TYPE: String get() = INT_TYPE
    override fun isSpecial(): Boolean = true
    override fun typecheckerTy(): Ty? = Ty.int()
}
