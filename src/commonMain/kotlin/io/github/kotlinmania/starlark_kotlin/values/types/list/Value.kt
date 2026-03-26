// port-lint: source src/values/types/list/value.rs
package io.github.kotlinmania.starlark_kotlin.values.types.list

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

import io.github.kotlinmania.starlark_kotlin.environment.Methods
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.values.AllocFrozenValue
import io.github.kotlinmania.starlark_kotlin.values.AllocValue
import io.github.kotlinmania.starlark_kotlin.values.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.UnpackValue
import io.github.kotlinmania.starlark_kotlin.values.ValueError
import io.github.kotlinmania.starlark_kotlin.values.applySlice
import io.github.kotlinmania.starlark_kotlin.values.compareSlice
import io.github.kotlinmania.starlark_kotlin.values.convertIndex
import io.github.kotlinmania.starlark_kotlin.values.equalsSlice
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import kotlin.math.max

/**
 * Generic list container, parameterized on the data type.
 *
 * [ListGen] wraps either mutable [ListData] or frozen [FrozenListData],
 * providing the core Starlark list value implementation.
 *
 * Corresponds to Rust's `ListGen<T>` which is `#[repr(transparent)]`.
 */
class ListGen<T>(val data: T) : StarlarkValue {
    override val TYPE: String get() = ListData.TYPE

    /**
     * Returns `true` for list types (special fast path in evaluation).
     * Corresponds to Rust's `is_special` in the `StarlarkValue` impl.
     */
    fun isSpecial(): Boolean = true

    /** Get the registered Starlark methods for this list value. */
    fun getMethods(): Methods? = listMethods()

    override fun toString(): String = data.toString()
}

/**
 * Mutable list data. Holds a mutable backing list that can grow/shrink.
 *
 * Mutation is guarded by an iterator count: if any iterator is active
 * over this list, mutation operations will fail.
 *
 * Corresponds to Rust's `ListData<'v>`.
 */
class ListData(
    /** The data stored by the list. */
    private val content: MutableList<Value> = mutableListOf(),
) {
    /** Current number of active iterators over this list. */
    @PublishedApi
    internal var iterCount: Int = 0

    companion object {
        /** The result of calling `type()` on lists. */
        const val TYPE: String = "list"

        /**
         * Obtain a mutable [ListData] from a [Value], or return an error
         * if the value is not a mutable list.
         *
         * Corresponds to Rust's `ListData::from_value_mut`.
         */
        fun fromValueMut(x: Value): Result<ListData> {
            val gen = x.downcastRef<ListGen<*>>()
                ?: return Result.failure(NotListError(x.getType()))

            val data = gen.data
            if (data is FrozenListData) {
                return Result.failure(ValueError.CannotMutateImmutableValue)
            }
            if (data is ListData) {
                data.checkCanMutate().getOrElse { return Result.failure(it) }
                return Result.success(data)
            }
            return Result.failure(NotListError(x.getType()))
        }

        /**
         * Obtain a mutable [ListData] from a [Value] without checking
         * for mutation safety.
         *
         * This is the Kotlin equivalent of Rust's `from_value_unchecked_mut`
         * which skips the `check_can_mutate` assertion in release builds.
         * In Kotlin we still assert in debug builds for safety.
         *
         * Corresponds to Rust's `ListData::from_value_unchecked_mut`.
         */
        fun fromValueUncheckedMut(x: Value): ListData {
            val gen = x.downcastRef<ListGen<*>>()!!
            val data = gen.data as ListData
            assert(data.checkCanMutate().isSuccess) { "List is being iterated" }
            return data
        }
    }

    /** Return an error if there is at least one active iterator over the list. */
    fun checkCanMutate(): Result<Unit> {
        if (iterCount != 0) {
            return Result.failure(ValueError.MutationDuringIteration)
        }
        return Result.success(Unit)
    }

    /** Obtain the length of the list. */
    fun len(): Int = content.size

    /**
     * List content.
     *
     * Note this operation does not prevent mutation of this list while
     * holding the reference. But such mutation does not violate memory-safety in Kotlin.
     */
    fun content(): List<Value> = content

    /** Push a value onto the end of the list. */
    fun push(value: Value) {
        content.add(value)
    }

    /** Clear all elements from the list. */
    fun clear() {
        content.clear()
    }

    /** Insert a value at the given index, shifting subsequent elements. */
    fun insert(index: Int, value: Value) {
        content.add(index, value)
    }

    /** Remove and return the element at the given index. */
    fun remove(index: Int): Value {
        return content.removeAt(index)
    }

    /** Set the element at the given index to the given value. */
    fun setAt(i: Int, v: Value): Result<Unit> {
        checkCanMutate().getOrElse { return Result.failure(it) }
        content[i] = v
        return Result.success(Unit)
    }

    /**
     * Double the list contents by appending a copy of itself.
     * Used by `extend` when the argument is the same list.
     */
    fun double() {
        val copy = content.toList()
        content.addAll(copy)
    }

    /**
     * Extend the list with all elements from the given iterable.
     */
    fun extend(iter: Iterable<Value>) {
        for (v in iter) {
            content.add(v)
        }
    }

    /** Increment the active iterator count. */
    fun incIterCount() {
        iterCount++
    }

    /** Decrement the active iterator count. */
    fun decIterCount() {
        iterCount--
    }

    /**
     * Ensure enough capacity for [additional] more elements.
     *
     * In Kotlin, [MutableList] manages capacity automatically, but this
     * method is provided for structural parity with Rust's
     * `ListData::reserve_additional`.
     *
     * When the backing list is an [ArrayList], we can proactively grow
     * the capacity to amortise allocations (matching Rust's strategy
     * of doubling with a minimum of 4).
     */
    fun reserveAdditional(additional: Int) {
        val needed = content.size + additional
        if (content is ArrayList<*>) {
            val newCap = max(needed, content.size * 2)
            // Size of Array is 2 words and size of List is one word,
            // so allocating at least 4 words would not be too large waste.
            // Note Vec allocates 4 by default.
            // Also note Array removes extra capacity on GC.
            val cap = max(newCap, 4)
            (content as ArrayList<Value>).ensureCapacity(cap)
        }
    }

    override fun toString(): String = displayList(content)
}

/**
 * Frozen (immutable) list data.
 *
 * The content is stored as a fixed-size list of [FrozenValue].
 * In Rust, this is `#[repr(C)]` with a `len` field and a zero-length
 * trailing array; in Kotlin we simply wrap a [List].
 *
 * Corresponds to Rust's `FrozenListData`.
 */
class FrozenListData(
    /** The data stored by the frozen list. */
    private val content: List<FrozenValue>,
) {
    companion object {
        /**
         * Create a new [FrozenListData] with the given content.
         *
         * Corresponds to Rust's `FrozenListData::new`.
         */
        fun new(content: List<FrozenValue>): FrozenListData =
            FrozenListData(content)

        /** Create an empty [FrozenListData]. */
        fun empty(): FrozenListData = FrozenListData(emptyList())

        /**
         * Obtain the [FrozenListData] pointed at by a [FrozenValue].
         *
         * Corresponds to Rust's `FrozenListData::from_frozen_value`.
         */
        fun fromFrozenValue(x: FrozenValue): FrozenListData? {
            val gen = x.downcastRef<ListGen<*>>() ?: return null
            return gen.data as? FrozenListData
        }
    }

    /** Number of elements. Corresponds to Rust's `FrozenListData::len`. */
    fun len(): Int = content.size

    /**
     * The frozen list content.
     *
     * Corresponds to Rust's `FrozenListData::content`.
     */
    fun content(): List<FrozenValue> = content

    /**
     * Debug representation.
     *
     * Corresponds to Rust's `impl Debug for FrozenListData`.
     */
    fun debugStr(): String = "FrozenList(content=${content()})"

    override fun toString(): String {
        return displayList(content.map { it.toValue() })
    }
}

/** Alias for the mutable list type. Corresponds to Rust's `List<'v>`. */
typealias MutableStarlarkList = ListGen<ListData>

/** Alias is used in `StarlarkDocs` derive. Corresponds to Rust's `FrozenList`. */
typealias FrozenList = ListGen<FrozenListData>

/**
 * Offset of the content field within [FrozenListData].
 *
 * In Rust, this is computed via `memoffset::offset_of!` for direct pointer
 * arithmetic in the bytecode compiler. In Kotlin, this is not meaningful
 * for the same purpose but is retained for structural parity.
 *
 * Corresponds to Rust's `ListGen<FrozenListData>::offset_of_content`.
 */
fun FrozenList.offsetOfContent(): Int = 0

/** Error thrown when trying to treat a non-list value as a list. */
private class NotListError(type: String) :
    Exception("Value is not list, value type: `$type`")

// -- ListLike interface -------------------------------------------------------

/**
 * Trait shared by mutable and frozen list data, abstracting over
 * the operations needed by [ListGen]'s StarlarkValue implementation.
 *
 * Corresponds to Rust's `ListLike<'v>` trait.
 */
interface ListLike {
    /** List elements as [Value] references. */
    fun content(): List<Value>

    /** Set element at index [i] to [v]. Returns error if the list is immutable or iterating. */
    fun setAt(i: Int, v: Value): Result<Unit>

    /** Begin iteration; returns a token value. Increments iter count for mutable lists. */
    fun newIter(me: Value): Value

    /** Size hint for iteration at the given index. */
    fun iterSizeHint(index: Int): Pair<Int, Int?>

    /** Advance the iterator at the given index. */
    fun iterNext(index: Int): Value?

    /** Stop iteration. Decrements iter count for mutable lists. */
    fun iterStop()
}

/** [ListLike] adapter for mutable [ListData]. */
internal class ListDataListLike(private val data: ListData) : ListLike {
    override fun content(): List<Value> = data.content()

    override fun setAt(i: Int, v: Value): Result<Unit> = data.setAt(i, v)

    override fun newIter(me: Value): Value {
        data.incIterCount()
        return me
    }

    override fun iterSizeHint(index: Int): Pair<Int, Int?> {
        val rem = data.len() - index
        return Pair(rem, rem)
    }

    override fun iterNext(index: Int): Value? {
        if (index >= data.len()) return null
        return data.content()[index]
    }

    override fun iterStop() {
        data.decIterCount()
    }
}

/** [ListLike] adapter for frozen [FrozenListData]. */
internal class FrozenListDataListLike(private val data: FrozenListData) : ListLike {
    override fun content(): List<Value> = data.content().map { it.toValue() }

    override fun setAt(i: Int, v: Value): Result<Unit> =
        Result.failure(ValueError.CannotMutateImmutableValue)

    override fun newIter(me: Value): Value = me

    override fun iterSizeHint(index: Int): Pair<Int, Int?> {
        val rem = data.len() - index
        return Pair(rem, rem)
    }

    override fun iterNext(index: Int): Value? {
        if (index >= data.len()) return null
        return data.content()[index].toValue()
    }

    override fun iterStop() { /* no-op for frozen lists */ }
}

// -- Display helpers ----------------------------------------------------------

/** Format a list of values as a Starlark list literal `[a, b, c]`. */
internal fun displayList(xs: List<Value>): String {
    return buildString {
        append('[')
        xs.forEachIndexed { index, value ->
            if (index > 0) append(", ")
            append(value)
        }
        append(']')
    }
}

// -- List methods registration ------------------------------------------------

/** Return the registered Starlark methods for list values. */
fun listMethods(): Methods? {
    // Method registration would be handled by the method builder infrastructure.
    // This function is the hook point for the methods defined in Methods.kt.
    return null
}

// -- StarlarkValue-like operations on ListGen ---------------------------------

/**
 * Collect the repr (string representation) of this list into the given [StringBuilder].
 *
 * Fast path as `repr()` for lists is quite hot.
 */
fun ListGen<out ListLike>.collectRepr(s: StringBuilder) {
    s.append('[')
    data.content().forEachIndexed { i, v ->
        if (i != 0) {
            s.append(", ")
        }
        v.collectRepr(s)
    }
    s.append(']')
}

/** Repr when a cycle is detected (the list references itself). */
fun ListGen<out ListLike>.collectReprCycle(collector: StringBuilder) {
    collector.append("[...]")
}

/** Convert to boolean: empty list is false, non-empty is true. */
fun ListGen<out ListLike>.toBool(): Boolean {
    return data.content().isNotEmpty()
}

/**
 * Equality check against another value. Returns false if [other] is not a list.
 *
 * Corresponds to Rust's `StarlarkValue::equals` for `ListGen<T>`.
 */
fun ListGen<out ListLike>.starlarkEquals(other: Value): Result<Boolean> {
    val otherRef = ListRef.fromValue(other)
    if (otherRef == null) {
        return Result.success(false)
    }
    return equalsSlice<Exception, Value, Value>(
        data.content(),
        otherRef.content(),
    ) { x, y -> x.equals(y) }
}

/**
 * Comparison (lexicographic) against another value.
 *
 * Corresponds to Rust's `StarlarkValue::compare` for `ListGen<T>`.
 */
fun ListGen<out ListLike>.starlarkCompare(other: Value): Result<Int> {
    val otherRef = ListRef.fromValue(other)
    if (otherRef == null) {
        return ValueError.unsupportedWith(ListData.TYPE, "cmp()", other)
    }
    return compareSlice<Exception, Value, Value>(
        data.content(),
        otherRef.content(),
    ) { x, y -> x.compare(y) }
}

/** Index into the list: `list[i]`. */
fun ListGen<out ListLike>.at(index: Value, heap: Heap): Result<Value> {
    val i = convertIndex(index, data.content().size).getOrElse { return Result.failure(it) }
    return Result.success(data.content()[i])
}

/** Length of the list. */
fun ListGen<out ListLike>.length(): Result<Int> {
    return Result.success(data.content().size)
}

/** Membership test: `x in list`. */
fun ListGen<out ListLike>.isIn(other: Value): Result<Boolean> {
    for (x in data.content()) {
        if (x.equals(other).getOrElse { return Result.failure(it) }) {
            return Result.success(true)
        }
    }
    return Result.success(false)
}

/** Slice the list: `list[start:stop:stride]`. */
fun ListGen<out ListLike>.slice(
    start: Value?,
    stop: Value?,
    stride: Value?,
    heap: Heap,
): Result<Value> {
    val xs = data.content()
    val res = applySlice(xs, start, stop, stride).getOrElse { return Result.failure(it) }
    return Result.success(heap.alloc(AllocList(res)))
}

/** Begin iteration over this list. */
fun ListGen<out ListLike>.iterate(me: Value, heap: Heap): Result<Value> {
    return Result.success(data.newIter(me))
}

/** Iterator size hint. */
fun ListGen<out ListLike>.iterSizeHint(index: Int): Pair<Int, Int?> {
    return data.iterSizeHint(index)
}

/** Iterator next element. */
fun ListGen<out ListLike>.iterNext(index: Int, heap: Heap): Value? {
    return data.iterNext(index)
}

/** Stop iteration. */
fun ListGen<out ListLike>.iterStop() {
    data.iterStop()
}

/** List concatenation: `list1 + list2`. Returns null if [other] is not a list. */
fun ListGen<out ListLike>.add(other: Value, heap: Heap): Result<Value>? {
    val otherRef = ListRef.fromValue(other) ?: return null
    val combined = data.content() + otherRef.content()
    return Result.success(heap.alloc(AllocList(combined)))
}

/**
 * List repetition: `list * n`. Returns null if [other] is not an int.
 *
 * Pre-allocates the result list with the exact capacity needed,
 * matching the Rust implementation's `Vec::with_capacity`.
 */
fun ListGen<out ListLike>.mul(other: Value, heap: Heap): Result<Value>? {
    val l: Int
    val unpacked = other.unpackI32()
    if (unpacked == null) {
        return null
    } else {
        l = unpacked
    }
    val content = data.content()
    val resultSize = content.size * max(0, l)
    val result = ArrayList<Value>(resultSize)
    for (i in 0 until l) {
        result.addAll(content)
    }
    return Result.success(heap.alloc(AllocList(result.toList())))
}

/** Reverse multiplication: `n * list`. */
fun ListGen<out ListLike>.rmul(lhs: Value, heap: Heap): Result<Value>? {
    return mul(lhs, heap)
}

/** Set element at index: `list[i] = v`. */
fun ListGen<out ListLike>.setAt(index: Value, allocValue: Value): Result<Unit> {
    val i = convertIndex(index, data.content().size).getOrElse { return Result.failure(it) }
    return data.setAt(i, allocValue)
}

/** Typechecker type for this list instance. */
fun ListGen<out ListLike>.typecheckerTy(): Ty? = Ty.anyList()

/** Static Starlark type representation for lists. */
fun getTypeStarlarkRepr(): Ty = Ty.anyList()

// -- AllocValue / AllocFrozenValue for collections ----------------------------

/**
 * Allocate a [kotlin.collections.List] of allocatable items as a Starlark list [Value].
 */
fun <T : AllocValue> kotlin.collections.List<T>.allocListValue(heap: Heap): Value {
    return heap.alloc(AllocList(this.map { it.allocValue(heap) }))
}

/**
 * Allocate a [kotlin.collections.List] of frozen-allocatable items as a [FrozenValue].
 */
fun <T : AllocFrozenValue> kotlin.collections.List<T>.allocListFrozenValue(heap: FrozenHeap): FrozenValue {
    return heap.alloc(AllocList(this.map { it.allocFrozenValue(heap) }))
}

/**
 * [StarlarkTypeRepr] for list-typed slices/arrays.
 *
 * Corresponds to Rust's `impl StarlarkTypeRepr for &'a [V]`.
 */
inline fun <reified V : StarlarkTypeRepr> listStarlarkTypeRepr(): Ty {
    return Ty.anyList()
}

// -- AllocValue for slices/arrays ---------------------------------------------

// impl AllocValue for &[V] where &V: AllocValue
fun <T : AllocValue> Array<T>.allocListValue(heap: Heap): Value {
    return heap.alloc(AllocList(this.map { it.allocValue(heap) }))
}

// impl AllocFrozenValue for &[V] where &V: AllocFrozenValue
fun <T : AllocFrozenValue> Array<T>.allocListFrozenValue(heap: FrozenHeap): FrozenValue {
    return heap.alloc(AllocList(this.map { it.allocFrozenValue(heap) }))
}

// -- FrozenListData additional methods ----------------------------------------

/**
 * Create a [FrozenListData] pre-allocated to hold [len] elements,
 * each initialized to [FrozenValue.newNone].
 *
 * In Rust this uses `unsafe` to allocate uninitialized memory of the
 * given length. In Kotlin, we initialize with `None` values.
 *
 * Corresponds to Rust's `FrozenListData::new(len)`.
 */
fun FrozenListData.Companion.newEmpty(len: Int): FrozenListData {
    return FrozenListData(List(len) { FrozenValue.newNone() })
}

// -- ListData additional methods (new, getTypeValueStatic) --------------------

// impl ListData { fn new(content) }
fun newListData(content: MutableList<Value>): ListData {
    return ListData(content)
}

// impl ListData { fn get_type_value_static() }
fun getListTypeValueStatic(): String {
    return ListData.TYPE
}

// -- isListType check ---------------------------------------------------------

// impl ListData { fn is_list_type(x: TypeId) -> bool }
fun isListType(value: Value): Boolean {
    val ref = value.downcastRef<ListGen<*>>() ?: return false
    return ref.data is ListData || ref.data is FrozenListData
}

// -- Serialize for ListGen ----------------------------------------------------

// impl Serialize for ListGen<T>
fun ListGen<out ListLike>.serialize(): List<Value> {
    return data.content().toList()
}

// -- Display for ListGen<T> ---------------------------------------------------

// impl Display for ListGen<T>
fun ListGen<*>.display(): String {
    return data.toString()
}

// -- ListData.extend with Result ----------------------------------------------

/**
 * Extend the list with elements from a fallible iterator.
 *
 * Each item from [iter] is unwrapped; if any yields a failure, the
 * error is propagated immediately.
 *
 * Corresponds to Rust's `ListData::try_extend`.
 */
fun <E : Throwable> ListData.tryExtend(
    iter: Iterator<Result<Value>>,
): Result<Unit> {
    val sizeHint = if (iter is Collection<*>) iter.size else 0
    if (sizeHint > 0) {
        reserveAdditional(sizeHint)
    }
    for (item in iter) {
        val value = item.getOrElse { return Result.failure(it) }
        push(value)
    }
    return Result.success(Unit)
}

// -- VALUE_EMPTY_FROZEN_LIST --------------------------------------------------

/** The empty frozen list, statically allocated. */
val VALUE_EMPTY_FROZEN_LIST: FrozenList = ListGen(FrozenListData.empty())
