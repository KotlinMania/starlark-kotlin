// port-lint: source src/values/types/list/value.rs
package io.github.kotlinmania.starlark_kotlin.values.types.list

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

import kotlin.jvm.JvmInline

@JvmInline
value class ListGen<T>(val value: T)

/** Define the mutable list type. */
class ListData<V>(
    /** The data stored by the list. */
    val content: Cell<ValueTyped<V, Array<V>>>
)

/** Define the frozen list type. */
class FrozenListData private constructor(
    val len: Int,
    /** The data stored by the tuple. */
    private val contentField: kotlin.Array<FrozenValue>
) {
    fun content(): kotlin.Array<FrozenValue> {
        return contentField.copyOfRange(0, len)
    }

    override fun toString(): String {
        return "FrozenList(content=${content().contentToString()})"
    }

    companion object {
        fun new(len: Int): FrozenListData {
            return FrozenListData(len, emptyArray())
        }
    }
}

/** Alias is used in `StarlarkDocs` derive. */
typealias FrozenList = ListGen<FrozenListData>

typealias List<V> = ListGen<ListData<V>>

val VALUE_EMPTY_FROZEN_LIST: AllocStaticSimple<ListGen<FrozenListData>> =
    AllocStaticSimple.alloc(ListGen(FrozenListData.new(0)))

object FrozenListDataCompanion {
    fun offsetOfContent(): Int {
        // Kotlin doesn't have direct memory layout control like Rust
        // This would need platform-specific implementation
        return 0
    }
}

private class NotListError(type: String) : Throwable("Value is not list, value type: `$type`")

fun <V> fromValueMut(x: Value<V>): Result<ListData<V>> {
    fun error(x: Value<V>): Throwable {
        return if (x.downcastRef<ListGen<FrozenListData>>() != null) {
            ValueError.CannotMutateImmutableValue()
        } else {
            NotListError(x.getType())
        }
    }

    val downcast = x.downcastRef<ListGen<ListData<V>>>()
    return if (downcast != null) {
        val data = downcast.value
        data.checkCanMutate().map { data }
    } else {
        Result.failure(error(x))
    }
}

fun <V> fromValueUncheckedMut(x: Value<V>): ListData<V> {
    val list = x.downcastRefUnchecked<ListGen<ListData<V>>>()
    check(list.value.checkCanMutate().isSuccess)
    return list.value
}

fun <V> ListData<V>.isListType(x: TypeId): Boolean {
    return x == typeId<ListGen<ListData<*>>>() || x == typeId<ListGen<FrozenListData>>()
}

/** Return an error if there's at least one iterator over the list. */
private fun <V> ListData<V>.checkCanMutate(): Result<Unit> {
    if (unlikely(content.get().asRef().iterCountIsNonZero())) {
        return Result.failure(ValueError.MutationDuringIteration())
    }
    return Result.success(Unit)
}

private fun <V> ListData<V>.reserveAdditionalSlow(additional: Int, heap: Heap<V>) {
    val newCap = maxOf(len() + additional, len() * 2)
    // Size of `Array` is 2 words and size of `List` is one word,
    // so allocating at least 4 words would not be too large waste.
    // Note `Vec` allocates 4 by default.
    // Also note `Array` removes extra capacity on GC.
    val finalCap = maxOf(newCap, 4)

    val newArray = heap.allocArray<V>(finalCap)
    newArray.extendFromSlice(content())
    content.set(newArray)
}

private fun <V> ListData<V>.reserveAdditional(additional: Int, heap: Heap<V>) {
    if (likely(content.get().asRef().remainingCapacity() >= additional)) {
        return
    }

    reserveAdditionalSlow(additional, heap)
}

fun <V> ListData<V>.double(heap: Heap<V>) {
    reserveAdditional(len(), heap)
    content.get().double()
}

fun <V> ListData<V>.extend(iter: Iterable<Value<V>>, heap: Heap<V>) {
    when (val result = tryExtend(iter.asSequence().map { Result.success(it) }.asIterable(), heap)) {
        is Result.Success -> Unit
        is Result.Failure -> throw result.exception
    }
}

fun <V, E : Throwable> ListData<V>.tryExtend(
    iter: Iterable<Result<Value<V>>>,
    heap: Heap<V>
): Result<Unit> {
    val iterator = iter.iterator()
    val (lo, hi) = iterator.sizeHint()

    return when {
        hi != null && lo == hi -> {
            // Exact size iterator.
            reserveAdditional(lo, heap)
            // Extend will panic if upper bound is provided incorrectly.
            content.get().tryExtend(iterator)
        }
        hi != null && content.get().remainingCapacity() >= hi -> {
            // Enough capacity for upper bound.
            // Extend will panic if upper bound is provided incorrectly.
            content.get().tryExtend(iterator)
        }
        else -> {
            // Default slow version.
            reserveAdditional(lo, heap)
            runCatching {
                for (item in iterator) {
                    push(item.getOrThrow(), heap)
                }
            }
        }
    }
}

fun <V> ListData<V>.push(value: Value<V>, heap: Heap<V>) {
    reserveAdditional(1, heap)
    content.get().push(value)
}

fun <V> ListData<V>.clear() {
    content.get().clear()
}

fun <V> ListData<V>.insert(index: Int, value: Value<V>, heap: Heap<V>) {
    reserveAdditional(1, heap)
    content.get().insert(index, value)
}

fun <V> ListData<V>.remove(index: Int): Value<V> {
    return content.get().remove(index)
}

// AllocValue implementation for Vec<V>
fun <V, T : AllocValue<V>> kotlin.collections.List<T>.allocValue(heap: Heap<V>): Value<V> {
    return heap.allocListIter(this.map { it.allocValue(heap) })
}

// AllocFrozenValue implementation for Vec<V>
fun <T : AllocFrozenValue> kotlin.collections.List<T>.allocFrozenValue(heap: FrozenHeap): FrozenValue {
    return heap.allocList(this.map { it.allocFrozenValue(heap) })
}

// StarlarkTypeRepr for &[V]
fun <V : StarlarkTypeRepr> sliceStarlarkTypeRepr(): Ty {
    return listStarlarkTypeRepr<V>()
}

// AllocValue for &[V]
fun <V, T : AllocValue<V>> kotlin.Array<T>.allocValue(heap: Heap<V>): Value<V> {
    return heap.allocListIter(this.map { it.allocValue(heap) })
}

// AllocFrozenValue for &[V]
fun <T : AllocFrozenValue> kotlin.Array<T>.allocFrozenValue(heap: FrozenHeap): FrozenValue {
    return heap.allocList(this.map { it.allocFrozenValue(heap) })
}

fun FrozenListData.len(): Int {
    return len
}

/** Obtain the [FrozenListData] pointed at by a [FrozenValue]. */
fun FrozenListData.Companion.fromFrozenValue(x: FrozenValue): FrozenListData? {
    return x.downcastRef<ListGen<FrozenListData>>()?.value
}

// ListData constants and methods
object ListDataConstants {
    /** The result of calling `type()` on lists. */
    const val TYPE: String = "list"
}

/** Type of list as frozen string value. */
fun <V> getTypeValueStatic(): FrozenStringValue {
    return ListGen<FrozenListData>().getTypeValueStatic()
}

fun <V> newListData(content: ValueTyped<V, Array<V>>): ListData<V> {
    return ListData(Cell(content))
}

/** Obtain the length of the list. */
fun <V> ListData<V>.len(): Int {
    return content.get().len()
}

/**
 * List content.
 *
 * Note this operation does not prevent mutation of this list while
 * holding the slice. But such mutation does not violate memory-safety.
 */
fun <V> ListData<V>.content(): kotlin.Array<Value<V>> {
    return content.get().asRef().content()
}

// Display for ListData
fun <V> ListData<V>.display(): String {
    return displayList(content.get().content())
}

// Display for FrozenListData
fun FrozenListData.display(): String {
    return displayList(coerce(content()))
}

// ListLike interface - trait equivalent
interface ListLike<V> {
    fun content(): kotlin.Array<Value<V>>
    fun setAt(i: Int, v: Value<V>): Result<Unit>

    // These functions are unsafe for the same reason
    // StarlarkValue iterator functions are unsafe.
    fun newIter(me: Value<V>): Value<V>
    fun iterSizeHint(index: Int): Pair<Int, Int?>
    fun iterNext(index: Int): Value<V>?
    fun iterStop()
}

// ListLike implementation for ListData
fun <V> ListData<V>.asListLike(): ListLike<V> = object : ListLike<V> {
    override fun content(): kotlin.Array<Value<V>> {
        return this@asListLike.content.get().asRef().content()
    }

    override fun setAt(i: Int, v: Value<V>): Result<Unit> {
        return this@asListLike.checkCanMutate().mapCatching {
            this@asListLike.content.get().setAt(i, v)
        }
    }

    override fun newIter(me: Value<V>): Value<V> {
        this@asListLike.content.get().incIterCount()
        return this@asListLike.content.get().toValue()
    }

    override fun iterSizeHint(index: Int): Pair<Int, Int?> {
        error("Iteration is performed on Array")
    }

    override fun iterNext(index: Int): Value<V>? {
        error("Iteration is performed on Array")
    }

    override fun iterStop() {
        error("Iteration is performed on Array")
    }
}

// ListLike implementation for FrozenListData
fun <V> FrozenListData.asListLike(): ListLike<V> = object : ListLike<V> {
    override fun content(): kotlin.Array<Value<V>> {
        return coerce(this@asListLike.content())
    }

    override fun setAt(i: Int, v: Value<V>): Result<Unit> {
        return Result.failure(ValueError.CannotMutateImmutableValue())
    }

    override fun iterSizeHint(index: Int): Pair<Int, Int?> {
        check(index <= this@asListLike.len())
        val rem = this@asListLike.len() - index
        return Pair(rem, rem)
    }

    override fun newIter(me: Value<V>): Value<V> {
        return me
    }

    override fun iterNext(index: Int): Value<V>? {
        return this@asListLike.content().getOrNull(index)?.toValue()
    }

    override fun iterStop() {}
}

// Display for ListGen
fun <T> ListGen<T>.display(): String {
    return value.toString()
}

fun displayList(xs: kotlin.Array<Value<*>>): String {
    return buildString {
        append('[')
        xs.forEachIndexed { index, value ->
            if (index > 0) append(", ")
            append(value)
        }
        append(']')
    }
}

fun listMethods(): Methods? {
    return methodsStatic.methods { listMethodsImpl() }
}

private val methodsStatic = MethodsStatic()

// StarlarkValue implementation for ListGen<T>
fun <V, T : ListLike<V>> ListGen<T>.isSpecial(private: Private): Boolean {
    return true
}

fun <V, T : ListLike<V>> ListGen<T>.getMethods(): Methods? {
    return listMethods()
}

fun <V, T : ListLike<V>> ListGen<T>.collectRepr(s: StringBuilder) {
    // Fast path as repr() for lists is quite hot
    s.append('[')
    value.content().forEachIndexed { i, v ->
        if (i != 0) {
            s.append(", ")
        }
        v.collectRepr(s)
    }
    s.append(']')
}

fun <V, T : ListLike<V>> ListGen<T>.collectReprCycle(collector: StringBuilder) {
    collector.append("[...]")
}

fun <V, T : ListLike<V>> ListGen<T>.toBool(): Boolean {
    return value.content().isNotEmpty()
}

fun <V, T : ListLike<V>> ListGen<T>.equals(other: Value<V>): Result<Boolean> {
    val otherList = ListRef.fromValue(other) ?: return Result.success(false)
    return equalsSlice(value.content(), otherList.content) { x, y -> x.equals(y) }
}

fun <V, T : ListLike<V>> ListGen<T>.compare(other: Value<V>): Result<Ordering> {
    val otherList = ListRef.fromValue(other)
        ?: return ValueError.unsupportedWith(this, "cmp()", other)
    return compareSlice(value.content(), otherList.content) { x, y -> x.compare(y) }
}

fun <V, T : ListLike<V>> ListGen<T>.at(index: Value<*>, heap: Heap<V>): Result<Value<V>> {
    val i = convertIndex(index, value.content().size) ?: return Result.failure(IndexError("Index out of range"))
    return Result.success(value.content()[i])
}

fun <V, T : ListLike<V>> ListGen<T>.length(): Result<Int> {
    return Result.success(value.content().size)
}

fun <V, T : ListLike<V>> ListGen<T>.isIn(other: Value<V>): Result<Boolean> {
    for (x in value.content()) {
        if (x.equals(other).getOrThrow()) {
            return Result.success(true)
        }
    }
    return Result.success(false)
}

fun <V, T : ListLike<V>> ListGen<T>.slice(
    start: Value<*>?,
    stop: Value<*>?,
    stride: Value<*>?,
    heap: Heap<V>
): Result<Value<V>> {
    val xs = value.content()
    val res = applySlice(xs, start, stop, stride) ?: return Result.failure(SliceError("Invalid slice"))
    return Result.success(heap.allocList(res))
}

fun <V, T : ListLike<V>> ListGen<T>.iterate(me: Value<V>, heap: Heap<V>): Result<Value<V>> {
    return Result.success(value.newIter(me))
}

fun <V, T : ListLike<V>> ListGen<T>.iterSizeHint(index: Int): Pair<Int, Int?> {
    return value.iterSizeHint(index)
}

fun <V, T : ListLike<V>> ListGen<T>.iterNext(index: Int, heap: Heap<V>): Value<V>? {
    return value.iterNext(index)
}

fun <V, T : ListLike<V>> ListGen<T>.iterStop() {
    value.iterStop()
}

fun <V, T : ListLike<V>> ListGen<T>.add(other: Value<V>, heap: Heap<V>): Result<Value<V>>? {
    val otherList = ListRef.fromValue(other) ?: return null
    return Result.success(heap.allocListConcat(value.content(), otherList.content()))
}

fun <V, T : ListLike<V>> ListGen<T>.mul(other: Value<*>, heap: Heap<V>): Result<Value<V>>? {
    val l = when (val unpacked = other.unpackInt()) {
        is UnpackResult.Some -> unpacked.value
        is UnpackResult.None -> return null
        is UnpackResult.Err -> return Result.failure(unpacked.error)
    }

    val result = buildList {
        repeat(maxOf(0, l)) {
            addAll(value.content().asIterable())
        }
    }
    return Result.success(heap.allocList(result.toTypedArray()))
}

fun <V, T : ListLike<V>> ListGen<T>.rmul(lhs: Value<V>, heap: Heap<V>): Result<Value<V>>? {
    return mul(lhs, heap)
}

fun <V, T : ListLike<V>> ListGen<T>.setAt(index: Value<V>, allocValue: Value<V>): Result<Unit> {
    val i = convertIndex(index, value.content().size) ?: return Result.failure(IndexError("Index out of range"))
    return value.setAt(i, allocValue)
}

fun <V, T : ListLike<V>> ListGen<T>.typecheckerTy(): Ty? {
    return Ty.anyList()
}

fun getTypeStarlarkRepr(): Ty {
    return Ty.anyList()
}

// Serialize implementation for ListGen<T>
fun <V, T : ListLike<V>> ListGen<T>.serialize(serializer: kotlinx.serialization.KSerializer<*>): kotlin.collections.List<*> {
    return value.content().toList()
}

