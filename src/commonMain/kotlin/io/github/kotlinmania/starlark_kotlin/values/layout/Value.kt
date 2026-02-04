// port-lint: source src/values/layout/value.rs
package io.github.kotlinmania.starlark_kotlin.values.layout

import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.Heap
import io.github.kotlinmania.starlark_kotlin.values.Freezer
import io.github.kotlinmania.starlark_kotlin.values.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.collections.StarlarkHashValue
import io.github.kotlinmania.starlark_kotlin.collections.StarlarkHasher
import io.github.kotlinmania.starlark_kotlin.collections.Hashed
import io.github.kotlinmania.starlark_kotlin.eval.Arguments
import io.github.kotlinmania.starlark_kotlin.eval.Evaluator
import io.github.kotlinmania.starlark_kotlin.typing.Ty

/**
 * Possible optimizations:
 * - Avoid duplicated object allocations
 * - Use inline classes for small value types
 *
 * We use pointer tagging on the bottom two bits (Rust approach):
 * - 00 => this Value pointer is actually a FrozenValue pointer
 * - 01 => this is a real Value pointer
 * - 11 => this is a bool (next bit: 1 => true, 0 => false)
 * - 10 => this is a None
 *
 * Note: In Kotlin, we can't use actual pointer tagging, so we'll use sealed classes
 * or similar approaches to represent tagged values.
 */

/**
 * A Starlark value. The lifetime concept from Rust is managed in Kotlin through
 * the garbage collector and heap references.
 *
 * Many of the methods forward to the underlying [StarlarkValue] implementation.
 * The [toString] method is equivalent to the `repr()` function in Starlark.
 */
sealed class Value {
    /**
     * Get a reference to the underlying StarlarkValue implementation.
     */
    abstract fun getRef(): StarlarkValue

    /**
     * Get the type name of this value.
     */
    abstract fun getTypeName(): String

    /**
     * Check if this value equals another value using Starlark equality semantics.
     */
    abstract fun equals(other: Value): Result<Boolean>

    /**
     * Compare this value to another value using Starlark comparison semantics.
     * Returns null if the values are not comparable.
     */
    abstract fun compare(other: Value): Result<Int?>

    /**
     * Get the hash value for this value.
     */
    abstract fun getHash(): Result<StarlarkHashValue>

    /**
     * Convert this value to a string representation (Starlark repr).
     */
    abstract override fun toString(): String

    /**
     * Check if this value is truthy in Starlark semantics.
     */
    abstract fun isTruthy(): Boolean

    companion object {
        /**
         * Create a new None value.
         */
        fun newNone(): Value = NoneValue

        /**
         * Create a new boolean value.
         */
        fun newBool(value: Boolean): Value = if (value) BoolValue.TRUE else BoolValue.FALSE

        /**
         * Create a new integer value.
         */
        fun newInt(value: Int): Value = IntValue(value)

        /**
         * Create a new empty string.
         */
        fun newEmptyString(): Value = StringValue("")

        /**
         * Create a new empty tuple.
         */
        fun newEmptyTuple(): Value = TupleValue(emptyList())
    }
}

/**
 * Represents the None value in Starlark.
 */
object NoneValue : Value() {
    override fun getRef(): StarlarkValue = NoneStarlarkValue
    override fun getTypeName(): String = "NoneType"
    override fun equals(other: Value): Result<Boolean> = Result.success(other is NoneValue)
    override fun compare(other: Value): Result<Int?> = Result.success(null)
    override fun getHash(): Result<StarlarkHashValue> = Result.success(StarlarkHashValue(0u))
    override fun toString(): String = "None"
    override fun isTruthy(): Boolean = false
}

/**
 * Represents a boolean value in Starlark.
 */
data class BoolValue(val value: Boolean) : Value() {
    override fun getRef(): StarlarkValue = BoolStarlarkValue(value)
    override fun getTypeName(): String = "bool"
    override fun equals(other: Value): Result<Boolean> =
        Result.success(other is BoolValue && other.value == value)
    override fun compare(other: Value): Result<Int?> = Result.success(null)
    override fun getHash(): Result<StarlarkHashValue> =
        Result.success(StarlarkHashValue(if (value) 1u else 0u))
    override fun toString(): String = if (value) "True" else "False"
    override fun isTruthy(): Boolean = value

    companion object {
        val TRUE = BoolValue(true)
        val FALSE = BoolValue(false)
    }
}

/**
 * Represents an integer value in Starlark.
 */
data class IntValue(val value: Int) : Value() {
    override fun getRef(): StarlarkValue = IntStarlarkValue(value)
    override fun getTypeName(): String = "int"
    override fun equals(other: Value): Result<Boolean> =
        Result.success(other is IntValue && other.value == value)
    override fun compare(other: Value): Result<Int?> =
        when (other) {
            is IntValue -> Result.success(value.compareTo(other.value))
            else -> Result.success(null)
        }
    override fun getHash(): Result<StarlarkHashValue> =
        Result.success(StarlarkHashValue(value.toUInt()))
    override fun toString(): String = value.toString()
    override fun isTruthy(): Boolean = value != 0
}

/**
 * Represents a string value in Starlark.
 */
data class StringValue(val value: String) : Value() {
    override fun getRef(): StarlarkValue = StringStarlarkValue(value)
    override fun getTypeName(): String = "string"
    override fun equals(other: Value): Result<Boolean> =
        Result.success(other is StringValue && other.value == value)
    override fun compare(other: Value): Result<Int?> =
        when (other) {
            is StringValue -> Result.success(value.compareTo(other.value))
            else -> Result.success(null)
        }
    override fun getHash(): Result<StarlarkHashValue> =
        Result.success(StarlarkHashValue(value.hashCode().toUInt()))
    override fun toString(): String = "\"$value\""
    override fun isTruthy(): Boolean = value.isNotEmpty()
}

/**
 * Represents a tuple value in Starlark.
 */
data class TupleValue(val elements: List<Value>) : Value() {
    override fun getRef(): StarlarkValue = TupleStarlarkValue(elements)
    override fun getTypeName(): String = "tuple"
    override fun equals(other: Value): Result<Boolean> =
        when (other) {
            is TupleValue -> {
                if (elements.size != other.elements.size) return Result.success(false)
                for (i in elements.indices) {
                    val eq = elements[i].equals(other.elements[i]).getOrElse { return Result.failure(it) }
                    if (!eq) return Result.success(false)
                }
                Result.success(true)
            }
            else -> Result.success(false)
        }
    override fun compare(other: Value): Result<Int?> = Result.success(null)
    override fun getHash(): Result<StarlarkHashValue> {
        var hash = 0u
        for (element in elements) {
            val elementHash = element.getHash().getOrElse { return Result.failure(it) }
            hash = hash * 31u + elementHash.value
        }
        return Result.success(StarlarkHashValue(hash))
    }
    override fun toString(): String = "(${elements.joinToString(", ") { it.toString() }})"
    override fun isTruthy(): Boolean = elements.isNotEmpty()
}

/**
 * Represents a heap-allocated value in Starlark.
 */
data class HeapValue(val pointer: Any, val vtable: ValueVTable) : Value() {
    override fun getRef(): StarlarkValue = vtable.getStarlarkValue(pointer)
    override fun getTypeName(): String = vtable.getTypeName()
    override fun equals(other: Value): Result<Boolean> = vtable.equals(pointer, other)
    override fun compare(other: Value): Result<Int?> = vtable.compare(pointer, other)
    override fun getHash(): Result<StarlarkHashValue> = vtable.getHash(pointer)
    override fun toString(): String = vtable.toString(pointer)
    override fun isTruthy(): Boolean = vtable.isTruthy(pointer)
}

/**
 * A frozen (immutable) Starlark value. Can be converted back to a [Value].
 *
 * A FrozenValue exists on a FrozenHeap. If the frozen heap gets dropped
 * while a FrozenValue from it still exists, undefined behavior may occur.
 */
sealed class FrozenValue {
    /**
     * Convert this FrozenValue to a Value.
     */
    abstract fun toValue(): Value

    /**
     * Check if this value equals another value.
     */
    abstract fun equals(other: Value): Result<Boolean>

    companion object {
        fun newNone(): FrozenValue = FrozenNoneValue
        fun newBool(value: Boolean): FrozenValue = if (value) FrozenBoolValue.TRUE else FrozenBoolValue.FALSE
        fun newInt(value: Int): FrozenValue = FrozenIntValue(value)
        fun newEmptyString(): FrozenValue = FrozenStringValue("")
        fun newEmptyTuple(): FrozenValue = FrozenTupleValue(emptyList())
    }
}

/**
 * Frozen None value.
 */
object FrozenNoneValue : FrozenValue() {
    override fun toValue(): Value = NoneValue
    override fun equals(other: Value): Result<Boolean> = Result.success(other is NoneValue)
}

/**
 * Frozen boolean value.
 */
data class FrozenBoolValue(val value: Boolean) : FrozenValue() {
    override fun toValue(): Value = BoolValue(value)
    override fun equals(other: Value): Result<Boolean> =
        Result.success(other is BoolValue && other.value == value)

    companion object {
        val TRUE = FrozenBoolValue(true)
        val FALSE = FrozenBoolValue(false)
    }
}

/**
 * Frozen integer value.
 */
data class FrozenIntValue(val value: Int) : FrozenValue() {
    override fun toValue(): Value = IntValue(value)
    override fun equals(other: Value): Result<Boolean> =
        Result.success(other is IntValue && other.value == value)
}

/**
 * Frozen string value.
 */
data class FrozenStringValue(val value: String) : FrozenValue() {
    override fun toValue(): Value = StringValue(value)
    override fun equals(other: Value): Result<Boolean> =
        Result.success(other is StringValue && other.value == value)
}

/**
 * Frozen tuple value.
 */
data class FrozenTupleValue(val elements: List<FrozenValue>) : FrozenValue() {
    override fun toValue(): Value = TupleValue(elements.map { it.toValue() })
    override fun equals(other: Value): Result<Boolean> =
        when (other) {
            is TupleValue -> {
                if (elements.size != other.elements.size) return Result.success(false)
                for (i in elements.indices) {
                    val eq = elements[i].equals(other.elements[i]).getOrElse { return Result.failure(it) }
                    if (!eq) return Result.success(false)
                }
                Result.success(true)
            }
            else -> Result.success(false)
        }
}

/**
 * Virtual table for heap-allocated values, providing dynamic dispatch.
 */
interface ValueVTable {
    fun getStarlarkValue(pointer: Any): StarlarkValue
    fun getTypeName(): String
    fun equals(pointer: Any, other: Value): Result<Boolean>
    fun compare(pointer: Any, other: Value): Result<Int?>
    fun getHash(pointer: Any): Result<StarlarkHashValue>
    fun toString(pointer: Any): String
    fun isTruthy(pointer: Any): Boolean
}

// Placeholder StarlarkValue implementations - these will be implemented in separate files
private object NoneStarlarkValue : StarlarkValue
private data class BoolStarlarkValue(val value: Boolean) : StarlarkValue
private data class IntStarlarkValue(val value: Int) : StarlarkValue
private data class StringStarlarkValue(val value: String) : StarlarkValue
private data class TupleStarlarkValue(val elements: List<Value>) : StarlarkValue

/**
 * Extension functions for Value unpacking.
 */

/**
 * Check if this value is None.
 */
fun Value.isNone(): Boolean = this is NoneValue

/**
 * Unpack this value as a boolean, or return null.
 */
fun Value.unpackBool(): Boolean? = (this as? BoolValue)?.value

/**
 * Unpack this value as an integer, or return null.
 */
fun Value.unpackInt(): Int? = (this as? IntValue)?.value

/**
 * Unpack this value as a string, or return null.
 */
fun Value.unpackStr(): String? = (this as? StringValue)?.value

/**
 * Unpack this value as a tuple, or return null.
 */
fun Value.unpackTuple(): List<Value>? = (this as? TupleValue)?.elements

/**
 * Check pointer equality between two values.
 */
fun Value.ptrEq(other: Value): Boolean = this === other

