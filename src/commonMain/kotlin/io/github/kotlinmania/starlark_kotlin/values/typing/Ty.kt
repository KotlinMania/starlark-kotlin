// port-lint: source src/values/typing/ty.rs
package io.github.kotlinmania.starlark_kotlin.values.typing

import io.github.kotlinmania.starlark_kotlin.assert.Assert
import io.github.kotlinmania.starlark_kotlin.typing.ParamSpec
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.Value

/**
 * A typing operation wasn't able to produce a precise result,
 * so made some kind of approximation.
 */
data class Approximation(
    /** The category of the approximation, e.g. "Unknown type". */
    val category: String,
    /** The precise details of this approximation, e.g. which type was unknown. */
    val message: String
) {
    override fun toString(): String = "Approximation: $category = \"$message\""

    companion object {
        fun new(category: String, message: Any): Approximation =
            Approximation(category, message.toString())
    }
}

/**
 * A Starlark type.
 *
 * The type system uses a series of alternative types (union types).
 * When typechecking, we try all alternatives, and if at least one of them
 * succeeds, then the whole expression is considered to be a success.
 *
 * For example, when typechecking:
 * ```python
 * x = ... # string or int
 * y = ... # string
 * x + y   # `int + string` fails, but `string + string` succeeds,
 *         # so the whole expression is typechecked successfully as `string`
 * ```
 *
 * This is different handling of union types than in TypeScript for example,
 * TypeScript would consider such expression to be an error.
 */
data class Ty(
    /**
     * A series of alternative types.
     *
     * When empty, this represents the "never" type (can hold no value).
     * When containing a single TyBasic.Any, this represents the "any" type.
     */
    val alternatives: List<TyBasic>
) {
    /**
     * Turn a type back into a name, potentially erasing some structure.
     * E.g. the type `[bool]` would return `list`.
     * Types like [any] will return null.
     */
    fun asName(): String? = when {
        alternatives.size == 1 -> alternatives[0].asName()
        else -> null
    }

    /**
     * Check if this is the "any" type.
     */
    fun isAny(): Boolean = this == any()

    /**
     * Check if this is the "never" type.
     */
    fun isNever(): Boolean = alternatives.isEmpty()

    /**
     * Check if this is a list type.
     */
    fun isList(): Boolean = alternatives.singleOrNull() is TyBasic.List

    /**
     * Check if this is a function type.
     */
    fun isFunction(): Boolean = asName() == "function"

    /**
     * Check if this is a tuple type.
     */
    fun isTuple(): Boolean = alternatives.singleOrNull() is TyBasic.Tuple

    /**
     * Check if this is a dict type.
     */
    fun isDict(): Boolean = alternatives.singleOrNull() is TyBasic.Dict

    /**
     * Iterate over the union alternatives.
     */
    fun iterUnion(): List<TyBasic> = alternatives

    /**
     * Create a union of this type with another type.
     */
    fun union(other: Ty): Ty {
        if (this.isNever()) return other
        if (other.isNever()) return this
        if (this.isAny() || other.isAny()) return any()

        val combined = (alternatives + other.alternatives).distinct().sorted()
        return Ty(combined)
    }

    /**
     * Create an intersection of this type with another type.
     */
    fun intersect(other: Ty): Ty {
        if (this.isNever() || other.isNever()) return never()
        if (this.isAny()) return other
        if (other.isAny()) return this

        // Simplified intersection - in practice this would need more sophisticated logic
        val common = alternatives.intersect(other.alternatives.toSet()).toList()
        return if (common.isEmpty()) never() else Ty(common)
    }

    override fun toString(): String = when {
        isNever() -> "never"
        isAny() -> "Any"
        alternatives.size == 1 -> alternatives[0].toString()
        else -> alternatives.joinToString(" | ")
    }

    companion object {
        /** Create a Ty from a single basic type. */
        fun basic(basic: TyBasic): Ty = Ty(listOf(basic))

        /** "any" type: can hold any value. */
        fun any(): Ty = basic(TyBasic.Any)

        /** Never type: can hold no value. */
        fun never(): Ty = Ty(emptyList())

        /** Create a None type. */
        fun none(): Ty = basic(TyBasic.None)

        /** Create a boolean type. */
        fun bool(): Ty = basic(TyBasic.Bool)

        /** Create the int type. */
        fun int(): Ty = basic(TyBasic.Int)

        /** Create a float type. */
        fun float(): Ty = basic(TyBasic.Float)

        /** Create a string type. */
        fun string(): Ty = basic(TyBasic.String)

        /** Create a list type. */
        fun list(element: Ty): Ty = basic(TyBasic.List(element))

        /** Create a list with any element type. */
        fun anyList(): Ty = list(any())

        /** Create an iterable type. */
        fun iter(item: Ty): Ty = basic(TyBasic.Iter(item))

        /** Create a dictionary type. */
        fun dict(key: Ty, value: Ty): Ty = basic(TyBasic.Dict(key, value))

        /** Create a dict with any key/value types. */
        fun anyDict(): Ty = dict(any(), any())

        /** Create a set type. */
        fun set(item: Ty): Ty = basic(TyBasic.Set(item))

        /** Create a set with any element type. */
        fun anySet(): Ty = set(any())

        /** Create a tuple of two elements. */
        fun tuple2(a: Ty, b: Ty): Ty = tuple(listOf(a, b))

        /** Create a tuple of given elements. */
        fun tuple(elems: List<Ty>): Ty = basic(TyBasic.Tuple(TyTuple.Elems(elems)))

        /** Tuple where elements are unknown. */
        fun anyTuple(): Ty = tupleOf(any())

        /** Create a tuple type where all elements have the same type. */
        fun tupleOf(item: Ty): Ty = basic(TyBasic.Tuple(TyTuple.Of(item)))

        /** Create a function type. */
        fun function(params: ParamSpec, result: Ty): Ty = basic(TyBasic.Function(TyFunction(params, result)))

        /** Create a callable type. */
        fun callable(params: ParamSpec, result: Ty): Ty = basic(TyBasic.Callable(TyCallable(params, result)))

        /** Function type that accepts any arguments and returns any result. */
        fun anyCallable(): Ty = basic(TyBasic.Callable(TyCallable.any()))

        /** Create a struct type with any fields. */
        fun anyStruct(): Ty = basic(TyBasic.Struct(TyStruct.any()))

        /** Create a TODO marker (same as any, but tagged for future work). */
        fun todo(): Ty = any()
    }
}

/**
 * Basic type variants in the Starlark type system.
 */
sealed class TyBasic : Comparable<TyBasic> {
    /** Get the name of this basic type, if it has one. */
    abstract fun asName(): String?

    /** The "any" type - accepts all values. */
    object Any : TyBasic() {
        override fun asName(): String? = null
        override fun toString(): String = "Any"
    }

    /** The None type. */
    object None : TyBasic() {
        override fun asName(): String = "NoneType"
        override fun toString(): String = "None"
    }

    /** The boolean type. */
    object Bool : TyBasic() {
        override fun asName(): String = "bool"
        override fun toString(): String = "bool"
    }

    /** The integer type. */
    object Int : TyBasic() {
        override fun asName(): String = "int"
        override fun toString(): String = "int"
    }

    /** The float type. */
    object Float : TyBasic() {
        override fun asName(): String = "float"
        override fun toString(): String = "float"
    }

    /** The string type. */
    object String : TyBasic() {
        override fun asName(): String = "string"
        override fun toString(): kotlin.String = "string"
    }

    /** A list type with element type. */
    data class List(val element: Ty) : TyBasic() {
        override fun asName(): kotlin.String = "list"
        override fun toString(): kotlin.String = "[$element]"
    }

    /** An iterable type with item type. */
    data class Iter(val item: Ty) : TyBasic() {
        override fun asName(): kotlin.String = "typing.Iterable"
        override fun toString(): kotlin.String = "typing.Iterable[$item]"
    }

    /** A dictionary type with key and value types. */
    data class Dict(val key: Ty, val value: Ty) : TyBasic() {
        override fun asName(): kotlin.String = "dict"
        override fun toString(): kotlin.String = "{$key: $value}"
    }

    /** A set type with item type. */
    data class Set(val item: Ty) : TyBasic() {
        override fun asName(): kotlin.String = "set"
        override fun toString(): kotlin.String = "{$item}"
    }

    /** A tuple type. */
    data class Tuple(val tuple: TyTuple) : TyBasic() {
        override fun asName(): kotlin.String = "tuple"
        override fun toString(): kotlin.String = tuple.toString()
    }

    /** A function type. */
    data class Function(val function: TyFunction) : TyBasic() {
        override fun asName(): kotlin.String = "function"
        override fun toString(): kotlin.String = function.toString()
    }

    /** A callable type. */
    data class Callable(val callable: TyCallable) : TyBasic() {
        override fun asName(): kotlin.String = "callable"
        override fun toString(): kotlin.String = callable.toString()
    }

    /** A struct type. */
    data class Struct(val struct: TyStruct) : TyBasic() {
        override fun asName(): kotlin.String = "struct"
        override fun toString(): kotlin.String = struct.toString()
    }

    /** A custom type. */
    data class Custom(val name: kotlin.String, val inner: Any?) : TyBasic() {
        override fun asName(): kotlin.String = name
        override fun toString(): kotlin.String = name
    }

    override fun compareTo(other: TyBasic): kotlin.Int {
        // Define a stable ordering for deduplication
        val thisOrder = when (this) {
            is Any -> 0
            is None -> 1
            is Bool -> 2
            is Int -> 3
            is Float -> 4
            is String -> 5
            is List -> 6
            is Iter -> 7
            is Dict -> 8
            is Set -> 9
            is Tuple -> 10
            is Function -> 11
            is Callable -> 12
            is Struct -> 13
            is Custom -> 14
        }
        val otherOrder = when (other) {
            is Any -> 0
            is None -> 1
            is Bool -> 2
            is Int -> 3
            is Float -> 4
            is String -> 5
            is List -> 6
            is Iter -> 7
            is Dict -> 8
            is Set -> 9
            is Tuple -> 10
            is Function -> 11
            is Callable -> 12
            is Struct -> 13
            is Custom -> 14
        }
        return thisOrder.compareTo(otherOrder)
    }
}

/**
 * Tuple type variants.
 */
sealed class TyTuple {
    /** Tuple with specific element types. */
    data class Elems(val elements: List<Ty>) : TyTuple() {
        override fun toString(): kotlin.String = "(${elements.joinToString(", ")})"
    }

    /** Tuple where all elements have the same type. */
    data class Of(val itemType: Ty) : TyTuple() {
        override fun toString(): kotlin.String = "($itemType, ...)"
    }
}

/**
 * Function type.
 */
data class TyFunction(
    val params: ParamSpec,
    val result: Ty,
    val typeAttr: Ty? = null
) {
    override fun toString(): kotlin.String {
        val typeAttrStr = typeAttr?.let { " [type: $it]" } ?: ""
        return "def($params) -> $result$typeAttrStr"
    }
}

/**
 * Callable type.
 */
data class TyCallable(
    val params: ParamSpec,
    val result: Ty
) {
    override fun toString(): kotlin.String = "callable($params) -> $result"

    companion object {
        fun any(): TyCallable = TyCallable(ParamSpec.any(), Ty.any())
    }
}

/**
 * Struct type.
 */
data class TyStruct(
    val fields: Map<kotlin.String, Ty>?
) {
    override fun toString(): kotlin.String = when {
        fields == null -> "struct{...}"
        fields.isEmpty() -> "struct{}"
        else -> "struct{${fields.entries.joinToString(", ") { "${it.key}: ${it.value}" }}}"
    }

    companion object {
        fun any(): TyStruct = TyStruct(null)
        fun of(fields: Map<kotlin.String, Ty>): TyStruct = TyStruct(fields)
    }
}

/// Type of type.
// #[derive(Debug, Display, Allocative, ProvidesStaticType, NoSerialize)]
// pub enum AbstractType {}
// An uninhabited enum in Rust — no instances can be created.
// In Kotlin, represented as a sealed class with no subclasses.
sealed class AbstractType : StarlarkValue {
    // #[starlark_value(type = "type")]
    override fun starlarkType(): kotlin.String = "type"

    // fn get_type_starlark_repr() -> Ty
    companion object {
        fun getTypeStarlarkRepr(): Ty = Ty.basic(TyBasic.Type)
    }

    // fn eval_type(&self) -> Option<Ty>
    // This is unreachable, but this function is needed
    // so `TyStarlarkValue` could think this is a type.
    fun evalType(): Ty? {
        error("AbstractType is uninhabited")
    }

    override fun toString(): kotlin.String = "type"
}

// #[cfg(test)]
// mod tests

// #[test]
// fn test_isinstance()
internal fun testIsinstance() {
    Assert.isTrue("isinstance(int, type)")
    Assert.isFalse("isinstance(1, type)")
    Assert.isTrue("isinstance(list[str], type)")
    Assert.isTrue("isinstance(eval_type(list), type)")
}

// #[test]
// fn test_pass()
internal fun testPass() {
    Assert.pass(
        """
def accepts_type(t: type):
    pass

def test():
    accepts_type(int)
    accepts_type(list[str])
    accepts_type(None | int)

test()
""",
    )
}

// #[test]
// fn test_fail_compile_time()
internal fun testFailCompileTime() {
    Assert.fail(
        """
def accepts_type(t: type):
    pass

def test():
    accepts_type(1)
""",
        "Expected type `type` but got `int`",
    )
}

