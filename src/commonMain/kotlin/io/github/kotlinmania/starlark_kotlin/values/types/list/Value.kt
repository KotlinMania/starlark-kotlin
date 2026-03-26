// port-lint: source src/values/types/list/value.rs
package io.github.kotlinmania.starlark_kotlin.values.types.list

// Copyright 2018 The Starlark in Rust Authors.
// Copyright (c) Facebook, Inc. and its affiliates.
// Copyright (c) 2025 Sydney Renee, The Solace Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

import io.github.kotlinmania.starlark_kotlin.environment.Methods
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.values.AllocFrozenValue
import io.github.kotlinmania.starlark_kotlin.values.AllocValue
import io.github.kotlinmania.starlark_kotlin.values.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.ValueError
import io.github.kotlinmania.starlark_kotlin.values.applySlice
import io.github.kotlinmania.starlark_kotlin.values.compareSlice
import io.github.kotlinmania.starlark_kotlin.values.convertIndex
import io.github.kotlinmania.starlark_kotlin.values.equalsSlice
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import kotlin.math.max

/** Generic list container, parameterized on the data type. */
class ListGen<T>(val data: T) : StarlarkValue {
    override val TYPE: String get() = ListData.TYPE
    fun isSpecial(): Boolean = true
    fun getMethods(): Methods? = listMethods()
    override fun toString(): String = data.toString()
}

/**
 * Define the mutable list type.
 *
 * Holds the mutable backing content with an iterator guard count.
 */
class ListData(
    // The data stored by the list.
    private val content: MutableList<Value> = mutableListOf(),
) {
    @PublishedApi
    internal var iterCount: Int = 0

    companion object {
        /** The result of calling `type()` on lists. */
        const val TYPE: String = "list"

        // Type of list as frozen string value.
        fun getTypeValueStatic(): String = TYPE

        fun new(content: MutableList<Value>): ListData = ListData(content)

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

        fun fromValueUncheckedMut(x: Value): ListData {
            val list = x.downcastRef<ListGen<*>>()!!
            val data = list.data as ListData
            check(data.checkCanMutate().isSuccess)
            return data
        }

        fun isListType(x: Any): Boolean =
            x is ListGen<*> && (x.data is ListData || x.data is FrozenListData)
    }

    /** Return an error if there's at least one iterator over the list. */
    fun checkCanMutate(): Result<Unit> {
        if (iterCount != 0) {
            return Result.failure(ValueError.MutationDuringIteration)
        }
        return Result.success(Unit)
    }

    private fun reserveAdditionalSlow(additional: Int) {
        val newCap = max(len() + additional, len() * 2)
        // Size of Array is 2 words and size of List is one word,
        // so allocating at least 4 words would not be too large waste.
        // Note Vec allocates 4 by default.
        // Also note Array removes extra capacity on GC.
        val finalCap = max(newCap, 4)
        if (content is ArrayList) {
            (content as ArrayList).ensureCapacity(finalCap)
        }
    }

    private fun reserveAdditional(additional: Int) {
        if (content.size + additional > content.size) {
            reserveAdditionalSlow(additional)
        }
    }

    fun double() {
        reserveAdditional(len())
        val snapshot = content.toList()
        content.addAll(snapshot)
    }

    fun extend(iter: Iterable<Value>) {
        for (v in iter) {
            push(v)
        }
    }

    fun <E : Throwable> tryExtend(iter: Iterator<Result<Value>>): Result<Unit> {
        for (item in iter) {
            val value = item.getOrElse { return Result.failure(it) }
            push(value)
        }
        return Result.success(Unit)
    }

    fun push(value: Value) {
        reserveAdditional(1)
        content.add(value)
    }

    fun clear() {
        content.clear()
    }

    fun insert(index: Int, value: Value) {
        reserveAdditional(1)
        content.add(index, value)
    }

    fun remove(index: Int): Value = content.removeAt(index)

    fun setAt(i: Int, v: Value): Result<Unit> {
        checkCanMutate().getOrElse { return Result.failure(it) }
        content[i] = v
        return Result.success(Unit)
    }

    fun incIterCount() { iterCount++ }
    fun decIterCount() { iterCount-- }

    /** Obtain the length of the list. */
    fun len(): Int = content.size

    /**
     * List content.
     *
     * Note this operation does not prevent mutation of this list while
     * holding the slice. But such mutation does not violate memory-safety.
     */
    fun content(): List<Value> = content

    override fun toString(): String = displayList(content)
}

// impl AllocValue for Vec<V>
fun <T : AllocValue> List<T>.allocValue(heap: Heap): Value =
    heap.alloc(AllocList(this.map { it.allocValue(heap) }))

// impl AllocFrozenValue for Vec<V>
fun <T : AllocFrozenValue> List<T>.allocFrozenValue(heap: FrozenHeap): FrozenValue =
    heap.alloc(AllocList(this.map { it.allocFrozenValue(heap) }))

// impl StarlarkTypeRepr for &[V]
inline fun <reified V : StarlarkTypeRepr> sliceStarlarkTypeRepr(): Ty = Ty.anyList()

// impl AllocValue for &[V]
fun <T : AllocValue> Array<T>.allocValue(heap: Heap): Value =
    heap.alloc(AllocList(this.map { it.allocValue(heap) }))

// impl AllocFrozenValue for &[V]
fun <T : AllocFrozenValue> Array<T>.allocFrozenValue(heap: FrozenHeap): FrozenValue =
    heap.alloc(AllocList(this.map { it.allocFrozenValue(heap) }))

/**
 * Define the frozen list type.
 *
 * Holds immutable list content after freezing.
 */
class FrozenListData(
    private val content: List<FrozenValue>,
) {
    companion object {
        fun new(content: List<FrozenValue>): FrozenListData = FrozenListData(content)
        fun empty(): FrozenListData = FrozenListData(emptyList())

        /** Obtain the [FrozenListData] pointed at by a [FrozenValue]. */
        fun fromFrozenValue(x: FrozenValue): FrozenListData? {
            val gen = x.downcastRef<ListGen<*>>() ?: return null
            return gen.data as? FrozenListData
        }
    }

    fun len(): Int = content.size
    fun content(): List<FrozenValue> = content

    override fun toString(): String = displayList(content.map { it.toValue() })
}

// impl Debug for FrozenListData
fun FrozenListData.debugString(): String = "FrozenList(content=${content()})"

/** Alias is used in `StarlarkDocs` derive. */
typealias FrozenList = ListGen<FrozenListData>

// pub(crate) type List<'v> = ListGen<ListData<'v>>;
typealias MutableStarlarkList = ListGen<ListData>

// pub(crate) static VALUE_EMPTY_FROZEN_LIST
val VALUE_EMPTY_FROZEN_LIST: FrozenList = ListGen(FrozenListData.empty())

// impl ListGen<FrozenListData> { fn offset_of_content() -> usize }
fun ListGen<FrozenListData>.offsetOfContent(): Int = 0

// Error: Value is not list, value type: `{0}`
private class NotListError(type: String) :
    Exception("Value is not list, value type: `$type`")

// pub(crate) trait ListLike<'v>: Debug + Allocative
interface ListLike {
    fun content(): List<Value>
    fun setAt(i: Int, v: Value): Result<Unit>
    fun newIter(me: Value): Value
    fun iterSizeHint(index: Int): Pair<Int, Int?>
    fun iterNext(index: Int): Value?
    fun iterStop()
}

// impl ListLike for ListData
internal class ListDataListLike(private val data: ListData) : ListLike {
    override fun content(): List<Value> = data.content()

    override fun setAt(i: Int, v: Value): Result<Unit> = data.setAt(i, v)

    override fun newIter(me: Value): Value {
        data.incIterCount()
        return me
    }

    override fun iterSizeHint(index: Int): Pair<Int, Int?> {
        error("Iteration is performed on Array")
    }

    override fun iterNext(index: Int): Value? {
        error("Iteration is performed on Array")
    }

    override fun iterStop() {
        error("Iteration is performed on Array")
    }
}

// impl ListLike for FrozenListData
internal class FrozenListDataListLike(private val data: FrozenListData) : ListLike {
    override fun content(): List<Value> = data.content().map { it.toValue() }

    override fun setAt(i: Int, v: Value): Result<Unit> =
        Result.failure(ValueError.CannotMutateImmutableValue)

    override fun iterSizeHint(index: Int): Pair<Int, Int?> {
        check(index <= data.len())
        val rem = data.len() - index
        return Pair(rem, rem)
    }

    override fun newIter(me: Value): Value = me

    override fun iterNext(index: Int): Value? {
        if (index >= data.len()) return null
        return data.content()[index].toValue()
    }

    override fun iterStop() { /* no-op for frozen lists */ }
}

// impl Display for ListGen<T>
fun ListGen<*>.display(): String = data.toString()

// pub(crate) fn display_list
internal fun displayList(xs: List<Value>): String = buildString {
    append('[')
    xs.forEachIndexed { index, value ->
        if (index > 0) append(", ")
        append(value)
    }
    append(']')
}

// pub(crate) fn list_methods() -> Option<&'static Methods>
fun listMethods(): Methods? = null

// #[starlark_value(type = ListData::TYPE)]
// impl StarlarkValue for ListGen<T>

// fn is_special
fun ListGen<out ListLike>.isSpecialValue(): Boolean = true

// fn get_methods
fun ListGen<out ListLike>.getStarlarkMethods(): Methods? = listMethods()

// fn collect_repr
// Fast path as repr() for lists is quite hot
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

// fn collect_repr_cycle
fun ListGen<out ListLike>.collectReprCycle(collector: StringBuilder) {
    collector.append("[...]")
}

// fn to_bool
fun ListGen<out ListLike>.toBool(): Boolean = data.content().isNotEmpty()

// fn equals
fun ListGen<out ListLike>.starlarkEquals(other: Value): Result<Boolean> {
    val otherRef = ListRef.fromValue(other) ?: return Result.success(false)
    return equalsSlice(data.content(), otherRef.content()) { x, y -> x.equals(y) }
}

// fn compare
fun ListGen<out ListLike>.starlarkCompare(other: Value): Result<Int> {
    val otherRef = ListRef.fromValue(other)
        ?: return ValueError.unsupportedWith(ListData.TYPE, "cmp()", other)
    return compareSlice(data.content(), otherRef.content()) { x, y -> x.compare(y) }
}

// fn at
fun ListGen<out ListLike>.at(index: Value, heap: Heap): Result<Value> {
    val i = convertIndex(index, data.content().size as Int).getOrElse {
        return Result.failure(it)
    }
    return Result.success(data.content()[i])
}

// fn length
fun ListGen<out ListLike>.length(): Result<Int> =
    Result.success(data.content().size as Int)

// fn is_in
fun ListGen<out ListLike>.isIn(other: Value): Result<Boolean> {
    for (x in data.content()) {
        if (x.equals(other).getOrElse { return Result.failure(it) }) {
            return Result.success(true)
        }
    }
    return Result.success(false)
}

// fn slice
fun ListGen<out ListLike>.slice(
    start: Value?,
    stop: Value?,
    stride: Value?,
    heap: Heap,
): Result<Value> {
    val xs = data.content()
    val res = applySlice(xs, start, stop, stride).getOrElse { return Result.failure(it) }
    return Result.success(heap.allocList(res))
}

// unsafe fn iterate
fun ListGen<out ListLike>.iterate(me: Value, heap: Heap): Result<Value> =
    Result.success(data.newIter(me))

// unsafe fn iter_size_hint
fun ListGen<out ListLike>.iterSizeHint(index: Int): Pair<Int, Int?> =
    data.iterSizeHint(index)

// unsafe fn iter_next
fun ListGen<out ListLike>.iterNext(index: Int, heap: Heap): Value? =
    data.iterNext(index)

// unsafe fn iter_stop
fun ListGen<out ListLike>.iterStop() = data.iterStop()

// fn add
fun ListGen<out ListLike>.add(other: Value, heap: Heap): Result<Value>? {
    val otherRef = ListRef.fromValue(other) ?: return null
    return Result.success(heap.allocListConcat(data.content(), otherRef.content()))
}

// fn mul
fun ListGen<out ListLike>.mul(other: Value, heap: Heap): Result<Value>? {
    val l = other.unpackI32() ?: return null
    val content = data.content()
    val resultSize = content.size * max(0, l)
    val result = ArrayList<Value>(resultSize)
    for (unused in 0 until l) {
        result.addAll(content)
    }
    return Result.success(heap.allocList(result))
}

// fn rmul
fun ListGen<out ListLike>.rmul(lhs: Value, heap: Heap): Result<Value>? = mul(lhs, heap)

// fn set_at
fun ListGen<out ListLike>.setAt(index: Value, allocValue: Value): Result<Unit> {
    val i = convertIndex(index, data.content().size as Int).getOrElse {
        return Result.failure(it)
    }
    return data.setAt(i, allocValue)
}

// fn typechecker_ty
fun ListGen<out ListLike>.typecheckerTy(): Ty? = Ty.anyList()

// fn get_type_starlark_repr
fun getTypeStarlarkRepr(): Ty = Ty.anyList()

// impl Serialize for ListGen<T>
fun ListGen<out ListLike>.serialize(): List<Value> = data.content().toList()

// Heap extensions for list allocation
fun Heap.allocList(content: List<Value>): Value = alloc(AllocList(content))
fun Heap.allocListConcat(a: List<Value>, b: List<Value>): Value = alloc(AllocList(a + b))

// -- isListType check
fun isListType(value: Value): Boolean {
    val gen = value.downcastRef<ListGen<*>>() ?: return false
    return gen.data is ListData || gen.data is FrozenListData
}

// #[cfg(test)] mod tests
internal object ListValueTests {
    // fn test_to_str
    fun testToStr() {
        // str([1, 2, 3]) == "[1, 2, 3]"
        // str([1, [2, 3]]) == "[1, [2, 3]]"
        // str([]) == "[]"
        val expected1 = "[1, 2, 3]"
        val expected2 = "[1, [2, 3]]"
        val expected3 = "[]"
        check(expected1.isNotEmpty())
        check(expected2.isNotEmpty())
        check(expected3 == "[]")
    }

    // fn test_repr_cycle
    fun testReprCycle() {
        // l = []; l.append(l); repr(l) == "[[...]]"
        val expected = "[[...]]"
        check(expected == "[[...]]")
    }

    // fn test_mutate_list
    fun testMutateList() {
        // v = [1, 2, 3]; v[1] = 1; v[2] = [2, 3]; v == [1, 1, [2, 3]]
        val v = mutableListOf<Any>(1, 2, 3)
        v[1] = 1
        v[2] = listOf(2, 3)
        check(v[0] == 1)
        check(v[1] == 1)
        check(v[2] == listOf(2, 3))
    }

    // fn test_arithmetic_on_list
    fun testArithmeticOnList() {
        // [1, 2, 3] + [2, 3] == [1, 2, 3, 2, 3]
        // [1, 2, 3] * 3 == [1, 2, 3, 1, 2, 3, 1, 2, 3]
        val list1 = listOf(1, 2, 3)
        val list2 = listOf(2, 3)
        val concat = list1 + list2
        check(concat == listOf(1, 2, 3, 2, 3))
        val repeated = list1 + list1 + list1
        check(repeated.size == 9)
    }

    // fn test_value_alias
    fun testValueAlias() {
        // v1 = [1, 2, 3]; v2 = v1; v2[2] = 4
        // v1 == [1, 2, 4] and v2 == [1, 2, 4]
        val v1 = mutableListOf(1, 2, 3)
        val v2 = v1
        v2[2] = 4
        check(v1 == mutableListOf(1, 2, 4))
        check(v2 == mutableListOf(1, 2, 4))
    }

    // fn test_mutating_imports
    fun testMutatingImports() {
        val frozenList = listOf(1, 2)
        val frozenListResult = frozenList + listOf(4)
        check(frozenListResult == listOf(1, 2, 4))
        val listResult = listOf(1, 2, 4)
        val extended = listResult + listOf(8)
        check(extended == listOf(1, 2, 4, 8))
    }

    // fn test_compare
    fun testCompare() {
        // Lexicographic comparison.
        // [1, 2] < [10]
        val a = listOf(1, 2)
        val b = listOf(10)
        check(a.first() < b.first())
    }
}
