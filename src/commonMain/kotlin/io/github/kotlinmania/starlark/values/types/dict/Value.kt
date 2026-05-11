// port-lint: source values/types/dict/value.rs
package io.github.kotlinmania.starlark.values.types.dict

/*
 * Copyright 2018 The Starlark in Rust Authors.
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

import io.github.kotlinmania.starlarkmap.Hashed
import io.github.kotlinmania.starlarkmap.smallmap.SmallMap
import io.github.kotlinmania.starlark.environment.Methods
import io.github.kotlinmania.starlark.environment.MethodsStatic
import io.github.kotlinmania.starlark.util.refcell.RefCell
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.ComplexValue
import io.github.kotlinmania.starlark.values.Freeze
import io.github.kotlinmania.starlark.values.layout.Freezer
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.Trace
import io.github.kotlinmania.starlark.values.trace
import io.github.kotlinmania.starlark.values.layout.heap.Tracer
import io.github.kotlinmania.starlark.values.ValueError
import io.github.kotlinmania.starlark.values.freeze
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.avalues.AllocStaticSimple
import io.github.kotlinmania.starlark.values.layout.avalues.allocComplex
import io.github.kotlinmania.starlark.values.layout.avalues.simple.allocSimple
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.layout.typed.FrozenStringValue
import io.github.kotlinmania.starlark.values.layout.typed.StringValue
import io.github.kotlinmania.starlark.values.toValue
import io.github.kotlinmania.starlarkmap.Equivalent
import kotlin.reflect.KClass
import io.github.kotlinmania.starlark.Either

data class DictGen<T>(val inner: T) : ComplexValue, Trace, Freeze<StarlarkValue> {

    override fun freeze(freezer: Freezer): Result<StarlarkValue> {
        val mutableSelf = this as DictGen<RefCell<Dict>>
        return mutableSelf.freezeDict(freezer) as Result<StarlarkValue>
    }

    override val TYPE: String get() = Dict.TYPE

    override fun trace(tracer: Tracer) {
        // DictGen delegates tracing to its inner value if it implements Trace
        val innerVal = inner
        if (innerVal is Trace) {
            innerVal.trace(tracer)
        }
    }

    override fun toString(): String = when (val innerVal = inner) {
        is DictLike -> fmtKeyedContainer("{", "}", ": ", innerVal.content().iter())
        else -> super<ComplexValue>.toString()
    }

    override fun getMethods(): Methods? = getDictMethods()

    override fun collectRepr(collector: StringBuilder) {
        val innerVal = inner
        if (innerVal is DictLike) {
            // Fast path as repr() for dicts is quite hot
            collector.append('{')
            for ((i, entry) in innerVal.content().iter().withIndex()) {
                if (i != 0) {
                    collector.append(", ")
                }
                val (name, value) = entry
                name.collectRepr(collector)
                collector.append(": ")
                value.collectRepr(collector)
            }
            collector.append('}')
        } else {
            super.collectRepr(collector)
        }
    }

    override fun collectReprCycle(collector: StringBuilder) {
        collector.append("{...}")
    }

    override fun toBool(): Boolean {
        val innerVal = inner
        return if (innerVal is DictLike) !innerVal.content().isEmpty()
        else true
    }

    override fun equals(other: Value): Result<Boolean> {
        val innerVal = inner
        if (innerVal !is DictLike) return Result.success(false)
        val otherDict = dictRefFromValue(other) ?: return Result.success(false)
        return io.github.kotlinmania.starlark.values.equalsSmallMap<Exception, Value, Value, Value>(
            innerVal.content(),
            getDictFromRef(otherDict).content,
        ) { x, y -> x.equals(y) }
    }

    override fun at(index: Value, _heap: Heap): Result<Value> {
        val innerVal = inner
        if (innerVal !is DictLike) return ValueError.unsupported(TYPE, "[]")
        val hashed = index.getHashed().getOrElse { return Result.failure(it) }
        val v = innerVal.content().getHashedByValue(hashed)
            ?: return Result.failure(ValueError.KeyNotFound(index.toRepr()))
        return Result.success(v.toValue())
    }

    override fun length(): Result<Int> {
        val innerVal = inner
        if (innerVal !is DictLike) return ValueError.unsupported(TYPE, "len()")
        return Result.success(innerVal.content().len())
    }

    override fun isIn(other: Value): Result<Boolean> {
        val innerVal = inner
        if (innerVal !is DictLike) return ValueError.unsupported(TYPE, "in")
        val hashed = other.getHashed().getOrElse { return Result.failure(it) }
        return Result.success(innerVal.content().getHashedByValue(hashed) != null)
    }

    override fun iterate(me: Value, _heap: Heap): Result<Value> {
        val innerVal = inner
        if (innerVal !is DictLike) return ValueError.unsupported(TYPE, "(iter)")
        innerVal.iterStart()
        return Result.success(me)
    }

    override fun iterSizeHint(index: Int): Pair<Int, Int?> {
        val innerVal = inner
        if (innerVal !is DictLike) return Pair(0, null)
        val rem = innerVal.content().len() - index
        return Pair(rem, rem)
    }

    override fun iterNext(index: Int, heap: Heap): Value? {
        val innerVal = inner
        if (innerVal !is DictLike) return null
        return innerVal.contentUnchecked().keys().elementAtOrNull(index)
    }

    override fun iterStop() {
        val innerVal = inner
        if (innerVal is DictLike) {
            innerVal.iterStop()
        }
    }

    override fun setAt(index: Value, newValue: Value): Result<Unit> {
        val innerVal = inner
        if (innerVal !is DictLike) return Result.failure(ValueError.CannotMutateImmutableValue)
        val hashed = index.getHashed().getOrElse { return Result.failure(it) }
        return innerVal.setAt(hashed, newValue)
    }

    override fun bitOr(other: Value, heap: Heap): Result<Value> {
        val innerVal = inner
        if (innerVal !is DictLike) return ValueError.unsupportedWith(TYPE, "|", other)

        val rhsDict = dictRefFromValue(other)
            ?: return ValueError.unsupportedWith(TYPE, "|", other)

        if (innerVal.content().isEmpty()) {
            // Clone rhs and return
            val rhsDictVal = getDictFromRef(rhsDict)
            val clonedContent = SmallMap.withCapacity<Value, Value>(rhsDictVal.len())
            for ((k, v) in rhsDictVal.iterHashed()) {
                clonedContent.insertHashed(k, v)
            }
            return Result.success(heap.allocComplex(DictGen(RefCell(Dict.new(clonedContent)))))
        }

        val items = SmallMap.withCapacity<Value, Value>(innerVal.content().len())
        for ((k, v) in innerVal.content().iterHashed()) {
            items.insertHashed(k, v)
        }
        val rhsDictVal = getDictFromRef(rhsDict)
        for ((k, v) in rhsDictVal.iterHashed()) {
            items.insertHashed(k, v)
        }
        return Result.success(heap.allocComplex(DictGen(RefCell(Dict.new(items)))))
    }

    override fun typecheckerTy(): Ty? = Ty.anyDict()

    override fun getTypeStarlarkRepr(): Ty = Ty.anyDict()

    override fun tryFreezeDirectly(freezer: Freezer): Result<FrozenValue>? {
        val innerVal = inner
        if (innerVal is DictLike && innerVal.content().isEmpty()) {
            return Result.success(VALUE_EMPTY_FROZEN_DICT.toFrozenValue())
        }
        return null
    }
}

fun Dict.display(): String =
    fmtKeyedContainer("{", "}", ": ", iter())

/** Define the dict type. */
class Dict(
    /** The data stored by the dictionary. The keys must all be hashable values. */
    val content: SmallMap<Value, Value>
) : Trace {
    companion object {
        /** The result of calling `type()` on dictionaries. */
        const val TYPE: String = "dict"

        /** This function is deprecated. Use [AllocDict] or [SmallMap] to allocate a new dictionary on the heap. */
        fun new(content: SmallMap<Value, Value>): Dict = Dict(content)

        fun isDictType(x: KClass<*>): Boolean =
            x == DictGen::class

        internal fun fromValueUncheckedMut(x: Value): RefCell<Dict> {
            val dict = x.downcastRefUnchecked<DictGen<RefCell<Dict>>>()
            return dict.inner
        }
    }

    override fun trace(tracer: Tracer) {
        content.trace(tracer)
    }

    fun starlarkTypeRepr(): Ty = Ty.dict(Ty.any(), Ty.any())

    /** Number of elements in the dict. */
    fun len(): Int = content.len()

    /** Is the dict empty? */
    fun isEmpty(): Boolean = content.isEmpty()

    /** Iterate through the key/value pairs in the dictionary. */
    fun iter(): Sequence<Pair<Value, Value>> =
        content.iter().map { (l, r) -> Pair(l, r) }

    /** Iterate through the key/value pairs in the dictionary, but retaining the hash of the keys. */
    fun iterHashed(): Sequence<Pair<Hashed<Value>, Value>> =
        content.iterHashed().map { (l, r) -> Pair(l.copied(), r) }

    /** Iterator over keys. */
    fun keys(): Sequence<Value> = content.keys()

    /** Iterator over values. */
    fun values(): Sequence<Value> = content.values()

    /** Get the value associated with a particular key. Will be an error if the key is not hashable,
     * and otherwise [Some] if the key exists in the dictionary and [None] otherwise. */
    fun get(key: Value): Result<Value?> =
        key.getHashed().map { hashed -> getHashed(hashed) }

    /** Lookup the value by the given prehashed key. */
    fun getHashed(key: Hashed<Value>): Value? =
        content.getHashedByValue(key)

    /** Get the value associated with a particular string. Equivalent to allocating the
     * string on the heap, turning it into a value, and looking up using that. */
    fun getStr(key: String): Value? =
        content.get<ValueStr>(ValueStr(key))

    /** Like [getStr], but where you already have the hash. */
    fun getStrHashed(key: Hashed<String>): Value? =
        content.getHashed(Hashed.newUnchecked(key.hash(), ValueStr(key.key())))

    /** Try to coerce all keys to strings. */
    internal fun downcastRefKeyString(): SmallMap<StringValue, Value>? {
        for (key in content.keys()) {
            if (!key.isStr()) {
                return null
            }
        }
        return content as SmallMap<StringValue, Value>
    }

    /** Reserve capacity to insert [additional] elements without reallocating. */
    fun reserve(additional: Int) {
        content.reserve(additional)
    }

    /** Insert a key/value pair into the dictionary. */
    fun insertHashed(key: Hashed<Value>, value: Value): Value? =
        content.insertHashed(key, value)

    /** Remove given key from the dictionary. */
    fun removeHashed(key: Hashed<Value>): Value? =
        content.shiftRemoveHashedByValue(key.asRef())

    /** Remove all elements from the dictionary. */
    fun clear() {
        content.clear()
    }
}

fun Dict.allocValue(heap: Heap): Value =
    heap.allocComplex(DictGen(RefCell(this)))

class FrozenDictData(
    /** The data stored by the dictionary. The keys must all be hashable values. */
    val content: SmallMap<FrozenValue, FrozenValue>
) {
    /** Iterate through the key/value pairs in the dictionary. */
    fun iter(): Sequence<Pair<FrozenValue, FrozenValue>> =
        content.iter().map { (l, r) -> Pair(l, r) }

    /** Get the value associated with a particular string. Equivalent to allocating the
     * string on the heap, turning it into a value, and looking up using that. */
    fun getStr(key: String): FrozenValue? {
        for ((k, v) in content.iter()) {
            if (k.unpackStr() == key) return v
        }
        return null
    }

    fun starlarkTypeRepr(): Ty = Ty.dict(Ty.any(), Ty.any())

    fun allocFrozenValue(heap: FrozenHeap): FrozenValue =
        if (content.isEmpty()) VALUE_EMPTY_FROZEN_DICT.toFrozenValue()
        else heap.allocSimple(DictGen(this))
}

/** Rust has distinct `FrozenDict`/`MutableDict` types; Kotlin uses [DictGen] directly. */

val VALUE_EMPTY_FROZEN_DICT: AllocStaticSimple<DictGen<FrozenDictData>> =
    AllocStaticSimple.alloc(DictGen(FrozenDictData(SmallMap.new())))

/** Helper type for lookups, not useful. */
data class ValueStr(val str: String) : Equivalent<Value> {
    override fun hashCode(): Int = hashStringValue(str)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ValueStr) return false
        return str == other.str
    }

    override fun equivalent(key: Value): Boolean = key.unpackStr() == str
}

/** Freeze implementation for DictGen<RefCell<Dict>> (mutable dict). */
internal fun DictGen<RefCell<Dict>>.freezeDict(freezer: Freezer): Result<DictGen<FrozenDictData>> {
    val frozenContent = this.inner.borrow().value.content.freeze(
        freezer,
        freezeKey = { v -> v.freeze(freezer) },
        freezeValue = { v -> v.freeze(freezer) },
    )
    val content = frozenContent.getOrElse { return Result.failure(it) }
    return Result.success(DictGen(FrozenDictData(content)))
}

interface DictLike {
    fun content(): SmallMap<Value, Value>
    // These functions carry the same caller-upheld invariants as
    // StarlarkValue's iterator functions.
    fun iterStart()
    fun contentUnchecked(): SmallMap<Value, Value>
    fun iterStop()
    fun setAt(index: Hashed<Value>, value: Value): Result<Unit>
}

internal class RefCellDictLike(private val cell: RefCell<Dict>) : DictLike {
    override fun content(): SmallMap<Value, Value> = cell.borrow().value.content

    override fun iterStart() {
        cell.borrow()
    }

    override fun iterStop() {
        cell.releaseBorrow()
    }

    override fun contentUnchecked(): SmallMap<Value, Value> = cell.borrow().value.content

    override fun setAt(index: Hashed<Value>, value: Value): Result<Unit> = try {
        cell.borrow().value.content.insertHashed(index, value)
        Result.success(Unit)
    } catch (_: Exception) {
        Result.failure(ValueError.MutationDuringIteration)
    }
}

class FrozenDictDataDictLike(private val data: FrozenDictData) : DictLike {
    override fun content(): SmallMap<Value, Value> =
        data.content as SmallMap<Value, Value>

    override fun iterStart() {}

    override fun iterStop() {}

    override fun contentUnchecked(): SmallMap<Value, Value> =
        data.content as SmallMap<Value, Value>

    override fun setAt(index: Hashed<Value>, value: Value): Result<Unit> =
        Result.failure(ValueError.CannotMutateImmutableValue)
}

fun getDictMethods(): Methods? = DICT_METHODS_STATIC.methods(::dictMethods)

private val DICT_METHODS_STATIC = MethodsStatic()

/** Helper to get Dict from a DictRef. */
private fun getDictFromRef(ref: DictRef): Dict = when (val aref = ref.aref) {
    is Either.Left -> aref.value.value
    is Either.Right -> aref.value
}

fun <T : DictLike> DictGen<T>.serialize(): Map<Value, Value> =
    inner.content().iter().toMap()

internal fun <K, V> fmtKeyedContainer(
    start: String,
    end: String,
    sep: String,
    iter: Sequence<Pair<K, V>>
): String {
    val builder = StringBuilder()
    builder.append(start)
    var first = true
    for ((k, v) in iter) {
        if (!first) builder.append(", ")
        builder.append(k.toString())
        builder.append(sep)
        builder.append(v.toString())
        first = false
    }
    builder.append(end)
    return builder.toString()
}

internal fun hashStringValue(s: String): Int = s.hashCode()
