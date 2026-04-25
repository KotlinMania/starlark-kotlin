// port-lint: source src/values/types/set/value.rs
package io.github.kotlinmania.starlark.values.types.set

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

import starlarkmap.Hashed
import starlarkmap.smallset.SmallSet
import io.github.kotlinmania.starlark.environment.Methods
import io.github.kotlinmania.starlark.environment.MethodsStatic
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.ComplexValue
import io.github.kotlinmania.starlark.values.Freeze
import io.github.kotlinmania.starlark.values.layout.Freezer
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.Trace
import io.github.kotlinmania.starlark.values.layout.heap.Tracer
import io.github.kotlinmania.starlark.values.ValueError
import io.github.kotlinmania.starlark.values.freeze
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.avalues.allocComplex
import io.github.kotlinmania.starlark.values.layout.heap.Heap

/**
 * Generic set wrapper.
 *
 * Transparent wrapper around the inner set implementation.
 * Corresponds to Rust's `SetGen<T>` with `#[repr(transparent)]`.
 */
// #[derive(Clone, Default, Trace, Debug, ProvidesStaticType, Allocative)]
// pub(crate) struct SetGen<T>(pub(crate) T);
data class SetGen<T>(val inner: T) : ComplexValue, Trace, Freeze<StarlarkValue> {

    // impl Freeze for SetGen<RefCell<SetData>>
    @Suppress("UNCHECKED_CAST")
    override fun freeze(freezer: Freezer): Result<StarlarkValue> {
        val innerVal = inner
        if (innerVal is RefCell<*>) {
            val borrowed = innerVal.borrow()
            val frozenContent = borrowed.data.content
                .freeze<Value, FrozenValue>(freezer) { v: Value -> v.freeze(freezer) }
                .getOrElse { return Result.failure(it) }
            return Result.success(SetGen(FrozenSetData(frozenContent)))
        }
        // Already frozen.
        if (innerVal is FrozenSetData) return Result.success(this as StarlarkValue)
        return Result.failure(IllegalStateException("Unexpected SetGen inner: ${innerVal!!::class}"))
    }
    override val TYPE: String get() = SET_TYPE

    override fun trace(tracer: Tracer) {
        val innerVal = inner
        if (innerVal is Trace) {
            innerVal.trace(tracer)
        }
    }

    private fun setLike(): SetLike = inner as SetLike

    // #[starlark_value(type = "set")]
    // impl StarlarkValue for SetGen<T>

    // fn length(&self) -> crate::Result<i32>
    override fun length(): Result<Int> =
        Result.success(setLike().content().len())

    // fn is_in(&self, other: Value<'v>) -> crate::Result<bool>
    override fun isIn(other: Value): Result<Boolean> {
        return try {
            val hashed = other.getHashed().getOrThrow()
            Result.success(setLike().content().containsHashed(hashed.asRef()))
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    // fn equals(&self, other: Value<'v>) -> crate::Result<bool>
    override fun equals(other: Value): Result<Boolean> {
        val otherSet = SetRef.unpackValueOpt(other)
            ?: return Result.success(false)
        return Result.success(equalsSmallSet(setLike().content(), otherSet.content))
    }

    // fn get_methods() -> Option<&'static Methods>
    override fun getMethods(): Methods? = setMethods()

    // unsafe fn iterate(&self, me: Value<'v>, _heap: Heap<'v>) -> crate::Result<Value<'v>>
    override fun iterate(me: Value, heap: Heap): Result<Value> {
        setLike().iterStart()
        return Result.success(me)
    }

    // unsafe fn iter_size_hint(&self, index: usize) -> (usize, Option<usize>)
    override fun iterSizeHint(index: Int): Pair<Int, Int?> {
        check(index <= setLike().content().len())
        val rem = setLike().content().len() - index
        return Pair(rem, rem)
    }

    // unsafe fn iter_next(&self, index: usize, _heap: Heap<'v>) -> Option<Value<'v>>
    override fun iterNext(index: Int, heap: Heap): Value? {
        return setLike().contentUnchecked().iter().drop(index).firstOrNull()
    }

    // unsafe fn iter_stop(&self)
    override fun iterStop() {
        setLike().iterStop()
    }

    // fn to_bool(&self) -> bool
    override fun toBool(): Boolean =
        !setLike().content().isEmpty()

    // fn bit_or(&self, rhs: Value<'v>, heap: Heap<'v>) -> crate::Result<Value<'v>>
    override fun bitOr(other: Value, heap: Heap): Result<Value> {
        return try {
            // Unlike in `union` it is not possible to `|` `set` and iterable. This is due python semantics.
            val otherSet = SetRef.unpackValueOpt(other)
                ?: return ValueError.unsupportedWith(SET_TYPE, "|", other)

            if (setLike().content().isEmpty()) {
                return Result.success(copySetData(otherSet.content).allocValue(heap))
            }

            val items = copySmallSet(setLike().content())
            for (h in otherSet.iterHashed()) {
                items.insertHashed(h)
            }
            Result.success(SetData().apply { content.addAll(items.iterHashed().asIterable()) }.allocValue(heap))
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    // fn bit_and(&self, rhs: Value<'v>, heap: Heap<'v>) -> crate::Result<Value<'v>>
    override fun bitAnd(other: Value, heap: Heap): Result<Value> {
        return try {
            val otherSet = SetRef.unpackValueOpt(other)
                ?: return ValueError.unsupportedWith(SET_TYPE, "&", other)

            if (setLike().content().isEmpty()) {
                return Result.success(SetData().allocValue(heap))
            }

            val items = SmallSet<Value>()
            for (h in otherSet.iterHashed()) {
                if (setLike().content().containsHashed(h.asRef())) {
                    items.insertHashedUniqueUnchecked(h)
                }
            }

            Result.success(SetData().apply { content.addAll(items.iterHashed().asIterable()) }.allocValue(heap))
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    // fn bit_xor(&self, rhs: Value<'v>, heap: Heap<'v>) -> crate::Result<Value<'v>>
    override fun bitXor(other: Value, heap: Heap): Result<Value> {
        return try {
            val otherSet = SetRef.unpackValueOpt(other)
                ?: return ValueError.unsupportedWith(SET_TYPE, "^", other)

            if (otherSet.content.isEmpty()) {
                return Result.success(copySetData(setLike().content()).allocValue(heap))
            }

            val data = SetData()
            for (elem in setLike().content().iterHashed()) {
                if (!otherSet.containsHashed(elem.copied())) {
                    data.addHashedUniqueUnchecked(elem.copied())
                }
            }

            for (hashed in otherSet.iterHashed()) {
                if (!setLike().content().containsHashed(hashed.asRef())) {
                    data.addHashed(hashed)
                }
            }
            Result.success(data.allocValue(heap))
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    // fn sub(&self, rhs: Value<'v>, heap: Heap<'v>) -> crate::Result<Value<'v>>
    override fun sub(other: Value, heap: Heap): Result<Value> {
        return try {
            val otherSet = SetRef.unpackValueOpt(other)
                ?: return ValueError.unsupportedWith(SET_TYPE, "-", other)

            if (setLike().content().isEmpty()) {
                return Result.success(SetData().allocValue(heap))
            }

            if (otherSet.content.isEmpty()) {
                return Result.success(copySetData(setLike().content()).allocValue(heap))
            }

            val data = SetData()

            for (elem in setLike().content().iterHashed()) {
                if (!otherSet.containsHashed(elem.copied())) {
                    data.addHashed(elem.copied())
                }
            }
            Result.success(data.allocValue(heap))
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    // fn typechecker_ty(&self) -> Option<Ty>
    override fun typecheckerTy(): Ty? = Ty.anySet()

    // fn get_type_starlark_repr() -> Ty
    override fun getTypeStarlarkRepr(): Ty = Ty.anySet()

    // impl Display for SetGen<T>
    override fun toString(): String {
        return fmtContainer("set([", "])", setLike().content().iter())
    }
}

private const val SET_TYPE: String = "set"

/**
 * Define the mutable set type.
 *
 * Corresponds to Rust's `SetData`.
 */
class SetData internal constructor(
    /** The data stored by the set. */
    val content: SmallSet<Value>,
) {
    constructor() : this(SmallSet())

    fun clear() {
        content.clear()
    }

    /**
     * Iterate through the values in the set.
     */
    fun iter(): Sequence<Value> {
        return content.iter()
    }

    /**
     * Iterate through the values in the set, but retaining the hash of the values.
     */
    fun iterHashed(): Sequence<Hashed<Value>> {
        return content.iterHashed().map { it.copied() }
    }

    /**
     * Check if the set contains a hashed element.
     */
    fun containsHashed(key: Hashed<Value>): Boolean {
        return content.containsHashed(key.asRef())
    }

    fun addHashed(value: Hashed<Value>): Boolean {
        return content.insertHashed(value)
    }

    fun addHashedUniqueUnchecked(value: Hashed<Value>) {
        content.insertHashedUniqueUnchecked(value)
    }

    fun removeHashed(value: Hashed<Value>): Boolean {
        return content.shiftRemoveHashed(value)
    }
}

/**
 * Define the frozen set type.
 *
 * Corresponds to Rust's `FrozenSetData`.
 */
class FrozenSetData(
    /** The data stored by the set. The values must all be hashable values. */
    val content: SmallSet<FrozenValue> = SmallSet()
)

/**
 * AllocValue implementation for SetData.
 */
fun SetData.allocValue(heap: Heap): Value {
    return heap.allocComplex(SetGen(RefCell(this)))
}

/**
 * StarlarkTypeRepr implementation for SetData.
 */
fun SetData.starlarkTypeRepr(): Ty {
    return Ty.anySet()
}


/**
 * Get set methods.
 */
fun setMethods(): Methods? {
    return RES.methods(::setMethodsImpl)
}

private val RES = MethodsStatic()

/** Delegate to the set methods registration in Methods.kt. */
private fun setMethodsImpl(builder: io.github.kotlinmania.starlark.environment.MethodsBuilder) {
    setMethods(builder)
}

/**
 * Trait for set-like operations.
 *
 * Corresponds to Rust's `SetLike` trait.
 */
interface SetLike {
    fun content(): SmallSet<Value>

    // These functions are unsafe for the same reason
    // StarlarkValue iterator functions are unsafe.
    fun iterStart()
    fun contentUnchecked(): SmallSet<Value>
    fun iterStop()
}

/**
 * SetLike implementation for RefCell<SetData>.
 */
class RefCellSetDataSetLike(private val cell: RefCell<SetData>) : SetLike {
    override fun content(): SmallSet<Value> {
        return cell.borrow().data.content
    }

    override fun iterStart() {
        // In Rust, mem::forget(self.borrow()) leaks a borrow to prevent mutation during iteration.
        // In Kotlin, the RefCell tracks borrow count; we increment it without releasing.
        cell.borrow()
    }

    override fun iterStop() {
        cell.releaseBorrow()
    }

    override fun contentUnchecked(): SmallSet<Value> {
        return cell.borrow().data.content
    }
}

/**
 * SetLike implementation for FrozenSetData.
 */
class FrozenSetDataSetLike(private val data: FrozenSetData) : SetLike {
    @Suppress("UNCHECKED_CAST")
    override fun content(): SmallSet<Value> {
        return data.content as SmallSet<Value>
    }

    override fun iterStart() {
        // No-op for frozen data
    }

    override fun iterStop() {
        // No-op for frozen data
    }

    @Suppress("UNCHECKED_CAST")
    override fun contentUnchecked(): SmallSet<Value> {
        return data.content as SmallSet<Value>
    }
}

// impl Serialize for SetGen<T>
fun SetGen<out SetLike>.serialize(): List<Value> = inner.content().iter().toList()

// Register vtable for frozen set (special type not handled by #[starlark_value] macro, because V is not ValueLike).
// Note: registerAvalueSimpleFrozen!(SetGen<FrozenSetData>) - to be implemented in registration system

/**
 * Format a container with start/end delimiters and comma-separated items.
 * Corresponds to Rust's `display_container::fmt_container`.
 */
private fun <T> fmtContainer(
    start: String,
    end: String,
    iter: Sequence<T>,
): String {
    val builder = StringBuilder()
    builder.append(start)
    var first = true
    for (item in iter) {
        if (!first) builder.append(", ")
        builder.append(item.toString())
        first = false
    }
    builder.append(end)
    return builder.toString()
}

/**
 * Compare two SmallSets for equality by checking containment in both directions.
 * Corresponds to Rust's `equals_small_set`.
 */
private fun <K> equalsSmallSet(xs: SmallSet<K>, ys: SmallSet<K>): Boolean {
    if (xs.len() != ys.len()) {
        return false
    }
    for (x in xs.iter()) {
        if (!ys.contains(x)) {
            return false
        }
    }
    return true
}

/**
 * Create a new SetData as a copy of an existing SmallSet<Value>.
 */
private fun copySetData(source: SmallSet<Value>): SetData {
    val data = SetData()
    data.content.addAll(source.iterHashed().asIterable())
    return data
}

/**
 * Create a shallow copy of a SmallSet by reinserting all hashed entries.
 */
private fun <T> copySmallSet(source: SmallSet<T>): SmallSet<T> {
    val copy = SmallSet<T>()
    copy.addAll(source.iterHashed().asIterable())
    return copy
}
