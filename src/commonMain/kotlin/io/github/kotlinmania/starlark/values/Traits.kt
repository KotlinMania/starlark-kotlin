// port-lint: source src/values/traits.rs
package io.github.kotlinmania.starlark.values

/*
 * Copyright 2018 The Starlark in Rust Authors.
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

/**
 * The values module define a trait `StarlarkValue` that defines the attribute of
 * any value in Starlark and a few macro to help implementing this trait.
 * The `Value` struct defines the actual structure holding a StarlarkValue. It is
 * mostly used to enable mutable and Rc behavior over a StarlarkValue.
 * This modules also defines this traits for the basic immutable values: int,
 * bool and NoneType. Sub-modules implement other common types of all Starlark
 * dialect.
 *
 * __Note__: we use _sequence_, _iterable_ and _indexable_ according to the
 * definition in the Starlark specification.
 * We also use the term _container_ for denoting any of those type that can
 * hold several values.
 */

import io.github.kotlinmania.starlark.collections.Hashed
import io.github.kotlinmania.starlark.collections.StarlarkHashValue
import io.github.kotlinmania.starlark.collections.StarlarkHasher
import io.github.kotlinmania.starlark.docs.DocItem
import io.github.kotlinmania.starlark.docs.DocMember
import io.github.kotlinmania.starlark.docs.DocProperty
import io.github.kotlinmania.starlark.environment.Methods
import io.github.kotlinmania.starlark.eval.runtime.Arguments
import io.github.kotlinmania.starlark.eval.runtime.Evaluator
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.typing.TyBasic
import io.github.kotlinmania.starlark.typing.TyStarlarkValue
import io.github.kotlinmania.starlark.typing.TypingBinOp
import io.github.kotlinmania.starlark.values.demand.Demand
import io.github.kotlinmania.starlark.values.layout.Freezer
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.avalues.allocTuple
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.layout.typed.FrozenStringValue
import io.github.kotlinmania.starlark.values.types.FUNCTION_TYPE
import io.github.kotlinmania.starlark.values.typing.typecompiled.TypeCompiled

/**
 * A trait for values which are more complex - because they are either mutable,
 * or contain references to other values.
 *
 * For values that contain nested Value types (mutable or not) there are a bunch of helpers.
 *
 * A Starlark type containing values will need to exist in two states: one containing Value
 * and one containing FrozenValue. To deal with that, if we are defining the type
 * containing a single value, let's call it `One`, we'd define `OneGen`
 * (for the general version), and then have type aliases generate `One` and `FrozenOne`.
 */
interface ComplexValue : StarlarkValue

/**
 * How to put Kotlin values into Values.
 *
 * Every Kotlin value stored in a Value must implement this trait.
 * You _must_ also implement ComplexValue if:
 *
 * * A type is not thread-safe, typically because it contains interior mutability.
 * * A type contains nested Starlark Values.
 *
 * There are only two required members of StarlarkValue, namely
 * TYPE and getTypeValueStatic.
 *
 * Every additional field enables further features in Starlark. In most cases the default
 * implementation returns an "unimplemented" error.
 */
interface StarlarkValue {
    /**
     * Capability flags mirroring Rust's `#[starlark_value]` `HAS_*` constants.
     * These indicate which StarlarkValue trait methods are meaningfully overridden.
     * The proc-macro in Rust auto-generates these; in Kotlin each implementation
     * overrides them when it provides the corresponding method.
     */
    val HAS_invoke: Boolean get() = false
    val HAS_eval_type: Boolean get() = false
    val HAS_iterate: Boolean get() = false
    val HAS_equals: Boolean get() = false

    /**
     * Return a string describing the type of self, as returned by the type()
     * function.
     */
    val TYPE: String
        get() = error("TYPE must be implemented by StarlarkValue implementations")

    /**
     * Like TYPE, but returns a reusable FrozenStringValue
     * pointer to it.
     */
    fun getTypeValueStatic(): FrozenStringValue {
        error("getTypeValueStatic must be implemented by StarlarkValue implementations")
    }

    /**
     * Return a string that is the representation of a type that a user would use in
     * type annotations. This often will be the same as TYPE, but in
     * some instances it might be slightly different than what is returned by TYPE.
     */
    fun getTypeStarlarkRepr(): Ty = Ty.starlarkValue(TyStarlarkValue.new(TYPE))

    /**
     * Type is special in Starlark, it is implemented differently than user defined types.
     * For example, some special types like `bool` cannot be heap allocated.
     */
    fun isSpecial(): Boolean = false

    /** Function is implemented for type values. */
    fun typeMatchesValue(value: Value): Boolean {
        error("typeMatchesValue should only be called on special types")
    }

    /**
     * Get the members associated with this type, accessible via `this_type.x`.
     * These members will have `dir`/`getattr`/`hasattr` properly implemented,
     * so it is the preferred way to go if possible.
     */
    fun getMethods(): Methods? = null

    /**
     * Return the documentation for this value.
     *
     * This should be the doc-item that is expected to be generated when this value appears as a
     * global in a module.
     */
    fun documentation(): DocItem {
        val ty = typecheckerTy() ?: getTypeStarlarkRepr()
        return DocItem.Member(
            DocMember.Property(
                DocProperty(
                    typ = ty,
                ),
            ),
        )
    }

    /**
     * Type of this instance for typechecker.
     * Note this can be more precise than generic type.
     */
    fun typecheckerTy(): Ty? = null

    /** Evaluate this value as a type expression. Basically, `eval_type(this)`. */
    fun evalType(): Ty? = null

    /**
     * Return a string representation of self, as returned by the `repr()` function.
     * Defaults to the `toString` instance - which should be fine for nearly all types.
     * In many cases the `repr()` representation will also be a Starlark expression
     * for creating the value.
     */
    fun collectRepr(collector: StringBuilder) {
        collector.append(this.toString())
    }

    /** Invoked to print `repr` when a cycle in the object stack is detected. */
    fun collectReprCycle(collector: StringBuilder) {
        collector.append("<$TYPE...>")
    }

    /** String used when printing call stack. `repr(self)` by default. */
    fun nameForCallStack(me: Value): String = me.toRepr()

    /**
     * Convert self to a boolean, as returned by the bool() function.
     * The default implementation returns true.
     */
    fun toBool(): Boolean = true

    /**
     * Return a hash data for self to be used when self is placed as a key in a `Dict`.
     * Return an error if there is no hash for this value (e.g. list).
     * Must be stable between frozen and non-frozen values.
     */
    fun writeHash(hasher: StarlarkHasher): Result<Unit> =
        if (TYPE == FUNCTION_TYPE) {
            // The Starlark spec says values of type "function" must be hashable.
            // We could return the address of the function, but that changes
            // with frozen/non-frozen which breaks freeze for Dict.
            // We could create an atomic counter and use that, but it takes memory,
            // effort, complexity etc, and we don't expect many Dict's keyed by
            // function. Returning 0 as the hash is valid, as Eq will sort it out.
            // Rust: let _ = hasher
            Result.success(Unit)
        } else {
            Result.failure(ControlError.NotHashableValue(TYPE))
        }

    /** Get the hash value. Calls writeHash by default. */
    fun getHash(): Result<StarlarkHashValue> {
        val hasher = StarlarkHasher()
        val result = writeHash(hasher)
        return result.map { hasher.finishSmall() }
    }

    /**
     * Compare `self` with `other` for equality.
     * Should only return an error on excessive recursion.
     *
     * This function can only be called when it is known that self pointer
     * is not equal to the other pointer.
     *
     * Equality must be symmetric (`a == b` implies `b == a`).
     */
    fun equals(other: Value): Result<Boolean> {
        // Type is only equal via a pointer
        return Result.success(false)
    }

    /**
     * Compare `self` with `other`.
     * This method returns a result of type Ordering, or an error
     * if the two types differ.
     */
    fun compare(other: Value): Result<Int> = ValueError.unsupportedWith(TYPE, "compare", other)

    /**
     * Directly invoke a function.
     * The number of `named` and `names` arguments are guaranteed to be equal.
     */
    fun invoke(
        me: Value,
        args: Arguments,
        eval: Evaluator,
    ): Result<Value> = ValueError.unsupported(TYPE, "call()")

    /** Return the result of `a[index]` if `a` is indexable. */
    fun at(index: Value, heap: Heap): Result<Value> = ValueError.unsupportedWith(TYPE, "[]", index)

    /** Return the result of `a[index0, index1]` if `a` is indexable by two parameters. */
    fun at2(
        index0: Value,
        index1: Value,
        heap: Heap,
    ): Result<Value> = ValueError.unsupported(TYPE, "[,]")

    /**
     * Extract a slice of the underlying object if the object is indexable. The
     * result will be object between `start` and `stop` (both of them are
     * added length() if negative and then clamped between 0 and length()).
     * `stride` indicates the direction.
     */
    fun slice(
        start: Value?,
        stop: Value?,
        stride: Value?,
        heap: Heap,
    ): Result<Value> = ValueError.unsupported(TYPE, "[::]")

    /**
     * Implement iteration over the value of this container by providing
     * the values in a list.
     */
    fun iterateCollect(heap: Heap): Result<List<Value>> = ValueError.unsupported(TYPE, "(iter)")

    /**
     * Returns an iterator over the value of this container if this value holds
     * an iterable container.
     *
     * This function calls iterateCollect by default.
     *
     * Returned iterator value must implement
     * iterNext and iterStop.
     */
    fun iterate(me: Value, heap: Heap): Result<Value> {
        val collected = iterateCollect(heap)
        return collected.map { heap.allocTuple(it) }
    }

    /** Returns the size hint for the iterator. */
    fun iterSizeHint(index: Int): Pair<Int, Int?> = Pair(0, null)

    /**
     * Yield the next value from the iterator.
     *
     * This function is called on the iterator value returned by iterate.
     * This function accepts an index, which starts at 0 and is incremented by 1
     * for each call to iterNext.
     */
    fun iterNext(index: Int, heap: Heap): Value? {
        error("iterNext called on non-iterable value of type $TYPE")
    }

    /**
     * Indicate that the iteration is finished.
     *
     * This function is typically used to release mutation lock.
     */
    fun iterStop() {
        error("iterStop called on non-iterable value of type $TYPE")
    }

    /** Returns the length of the value, if this value is a sequence. */
    fun length(): Result<Int> = ValueError.unsupported(TYPE, "len()")

    /**
     * Attribute type, for the typechecker.
     *
     * If getAttr is implemented, this should return Some(Any).
     */
    fun attrTy(name: String): Ty? = Ty.any()

    /**
     * Get an attribute for the current value as would be returned by dotted
     * expression (i.e. `a.attribute`).
     *
     * The three methods getAttr, hasAttr and dirAttr
     * must be consistent - if you implement one, you should probably implement all three.
     *
     * This operation must have no side effects, because it can be called speculatively.
     */
    fun getAttr(attribute: String, heap: Heap): Value? = null

    /**
     * A version of getAttr which takes `Hashed<String>` instead of `String`,
     * thus implementation may reuse the hash of the string if this is called
     * repeatedly with the same string.
     */
    fun getAttrHashed(attribute: Hashed<String>, heap: Heap): Value? = getAttr(attribute.key(), heap)

    /**
     * Return true if an attribute of name `attribute` exists for the current value.
     *
     * Default implementation of this function delegates to getAttr.
     */
    fun hasAttr(attribute: String, heap: Heap): Boolean = getAttr(attribute, heap) != null

    /** Return a list of string listing all attributes of the current value. */
    fun dirAttr(): List<String> = emptyList()

    /** Tell whether `other` is in the current value, if it is a container. */
    fun isIn(other: Value): Result<Boolean> = ValueError.unsupportedOwned(other.getType(), "in", TYPE)

    /** Apply the `+` unary operator to the current value. */
    fun plus(heap: Heap): Result<Value> = ValueError.unsupported(TYPE, "+")

    /** Apply the `-` unary operator to the current value. */
    fun minus(heap: Heap): Result<Value> = ValueError.unsupported(TYPE, "-")

    /**
     * Add with the arguments the other way around.
     * Normal `add` should return null in order for it to be evaluated.
     */
    fun radd(lhs: Value, heap: Heap): Result<Value>? = null

    /**
     * Add `rhs` to the current value. Should return null
     * to fall through to `radd`.
     */
    fun add(rhs: Value, heap: Heap): Result<Value>? = null

    /** Subtract `other` from the current value. */
    fun sub(other: Value, heap: Heap): Result<Value> = ValueError.unsupportedWith(TYPE, "-", other)

    /** Called on `rhs` of `lhs * rhs` when `lhs.mul` returns null. */
    fun rmul(lhs: Value, heap: Heap): Result<Value>? = null

    /**
     * Multiply the current value with `other`.
     *
     * When this function returns null, starlark-kotlin calls `rhs.rmul(lhs)`.
     */
    fun mul(rhs: Value, heap: Heap): Result<Value>? = null

    /** Divide the current value by `other`. Always results in a float value. */
    fun div(other: Value, heap: Heap): Result<Value> = ValueError.unsupportedWith(TYPE, "/", other)

    /**
     * Apply the percent operator between the current value and `other`. Usually used on
     * strings, as per the Starlark spec string interpolation.
     */
    fun percent(other: Value, heap: Heap): Result<Value> = ValueError.unsupportedWith(TYPE, "%", other)

    /** Floor division between the current value and `other`. */
    fun floorDiv(other: Value, heap: Heap): Result<Value> = ValueError.unsupportedWith(TYPE, "//", other)

    /** Bitwise `&` operator. */
    fun bitAnd(other: Value, heap: Heap): Result<Value> = ValueError.unsupportedWith(TYPE, "&", other)

    /** Bitwise `|` operator. */
    fun bitOr(other: Value, heap: Heap): Result<Value> {
        val thisEval = this.evalType()
        if (thisEval != null) {
            return runCatching {
                val thisCompiled = TypeCompiled.fromTy(thisEval, heap)
                val otherCompiled = TypeCompiled.new(other, heap)
                TypeCompiled.typeAnyOfTwo(thisCompiled, otherCompiled, heap).toInner()
            }
        }
        return ValueError.unsupportedWith(TYPE, "|", other)
    }

    /** Bitwise `^` operator. */
    fun bitXor(other: Value, heap: Heap): Result<Value> = ValueError.unsupportedWith(TYPE, "^", other)

    /** Bitwise `~` operator. */
    fun bitNot(heap: Heap): Result<Value> = ValueError.unsupported(TYPE, "~")

    /** Bitwise `<<` operator. */
    fun leftShift(other: Value, heap: Heap): Result<Value> = ValueError.unsupportedWith(TYPE, "<<", other)

    /** Bitwise `>>` operator. */
    fun rightShift(other: Value, heap: Heap): Result<Value> = ValueError.unsupportedWith(TYPE, ">>", other)

    /** Typecheck `this op rhs`. */
    fun binOpTy(op: TypingBinOp, rhs: TyBasic): Ty? = null

    /** Typecheck `lhs op this`. */
    fun rbinOpTy(lhs: TyBasic, op: TypingBinOp): Ty? = null

    /** Called when exporting a value under a specific name. */
    fun exportAs(
        variableName: String,
        eval: Evaluator,
    ): Result<Unit> {
        // Most data types ignore how they are exported
        // but rules/providers like to use it as a helpful hint for users
        return Result.success(Unit)
    }

    /** Set the value at `index` with the new value. */
    fun setAt(index: Value, newValue: Value): Result<Unit> = Result.failure(ValueError.CannotMutateImmutableValue)

    /**
     * Set the attribute named `attribute` of the current value to
     * `value` (e.g. `a.attribute = value`).
     */
    fun setAttr(attribute: String, newValue: Value): Result<Unit> = ValueError.unsupported(TYPE, ".$attribute=")

    /** Dynamically provide values based on type. */
    fun provide(demand: Demand) {
        // Rust: let _ = demand
    }

    /**
     * When freezing, this function is called on the value first and can return a FrozenValue
     * directly to bypass the freeze impl.
     *
     * This function is needed in the rare case when freezing some values, it may be possible
     * to return a statically allocated value instead of allocating a new one.
     */
    fun tryFreezeDirectly(freezer: Freezer): Result<FrozenValue>? = null
}
