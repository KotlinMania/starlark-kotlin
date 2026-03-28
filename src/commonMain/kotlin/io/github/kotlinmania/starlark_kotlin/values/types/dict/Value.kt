// port-lint: source src/values/types/dict/value.rs
package io.github.kotlinmania.starlark_kotlin.values.types.dict

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

import io.github.kotlinmania.starlark_kotlin.any.ProvidesStaticType
import io.github.kotlinmania.starlark_kotlin.collections.Hashed
import io.github.kotlinmania.starlark_kotlin.collections.SmallMap
import io.github.kotlinmania.starlark_kotlin.environment.Methods
import io.github.kotlinmania.starlark_kotlin.environment.MethodsStatic
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.values.AllocFrozenValue
import io.github.kotlinmania.starlark_kotlin.values.AllocValue
import io.github.kotlinmania.starlark_kotlin.values.Freeze
import io.github.kotlinmania.starlark_kotlin.values.Freezer
import io.github.kotlinmania.starlark_kotlin.values.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.Trace
import io.github.kotlinmania.starlark_kotlin.values.ValueError
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.toValue
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.AllocStaticSimple
import io.github.kotlinmania.starlark_kotlin.values.freeze_error.FreezeResult
import io.github.kotlinmania.starlark_kotlin.values.layout.typed.FrozenStringValue
import io.github.kotlinmania.starlark_kotlin.values.layout.typed.StringValue
import kotlin.reflect.KClass
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.allocSimple
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.allocComplex

@ProvidesStaticType
data class DictGen<T>(val inner: T) : Trace {
    override fun toString(): String = when (inner) {
        is DictLike -> {
            @Suppress("UNCHECKED_CAST")
            fmtKeyedContainer("{", "}", ": ", (inner as DictLike).content().iter())
        }
        else -> super.toString()
    }
}

fun Dict.display(): String =
    fmtKeyedContainer("{", "}", ": ", iter())

/** Define the dict type. */
@ProvidesStaticType
class Dict(
    /** The data stored by the dictionary. The keys must all be hashable values. */
    val content: SmallMap<Value, Value>
) : Trace {
    companion object {
        /** The result of calling `type()` on dictionaries. */
        const val TYPE: String = "dict"

        /** Dict type string as Starlark frozen string value. */
        fun getTypeValueStatic(): FrozenStringValue =
            DictGen.getTypeValueStatic<FrozenDictData>()

        /** This function is deprecated. Use [AllocDict] or [SmallMap] to allocate a new dictionary on the heap. */
        fun new(content: SmallMap<Value, Value>): Dict = Dict(content)

        fun isDictType(x: KClass<*>): Boolean =
            x == DictGen::class

        fun fromValueUncheckedMut(x: Value): AtomicRef<Dict> {
            @Suppress("UNCHECKED_CAST")
            val dict = x.downcastRefUnchecked<DictGen<AtomicRef<Dict>>>()
            return dict.inner
        }
    }

    fun starlarkTypeRepr(): Ty = DictType.starlarkTypeRepr<FrozenValue, FrozenValue>()

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
        content.get(ValueStr(key))

    /** Like [getStr], but where you already have the hash. */
    fun getStrHashed(key: Hashed<String>): Value? =
        content.getHashedByValue(Hashed.newUnchecked(key.hash(), ValueStr(key.key())))

    /** Try to coerce all keys to strings. */
    internal fun downcastRefKeyString(): SmallMap<StringValue, Value>? {
        for (key in content.keys()) {
            if (!key.isStr()) {
                return null
            }
        }
        @Suppress("UNCHECKED_CAST")
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
        content.shiftRemoveHashed(key.asRef())

    /** Remove all elements from the dictionary. */
    fun clear() {
        content.clear()
    }
}

fun Dict.allocValue(heap: Heap): Value =
    heap.allocComplex(DictGen(AtomicRef(this)))

@ProvidesStaticType
class FrozenDictData(
    /** The data stored by the dictionary. The keys must all be hashable values. */
    val content: SmallMap<FrozenValue, FrozenValue>
) {
    /** Iterate through the key/value pairs in the dictionary. */
    fun iter(): Sequence<Pair<FrozenValue, FrozenValue>> =
        content.iter().map { (l, r) -> Pair(l, r) }

    /** Get the value associated with a particular string. Equivalent to allocating the
     * string on the heap, turning it into a value, and looking up using that. */
    fun getStr(key: String): FrozenValue? =
        content.get(ValueStr(key))

    fun starlarkTypeRepr(): Ty = Ty.dict(Ty.any(), Ty.any())

    fun allocFrozenValue(heap: FrozenHeap): FrozenValue =
        if (content.isEmpty()) VALUE_EMPTY_FROZEN_DICT.toFrozenValue()
        else heap.allocSimple(DictGen(this))
}

/** Alias is used in `StarlarkDocs` derive. */
typealias FrozenDict = DictGen<FrozenDictData>

typealias MutableDict = DictGen<AtomicRef<Dict>>

val VALUE_EMPTY_FROZEN_DICT: AllocStaticSimple<DictGen<FrozenDictData>> =
    AllocStaticSimple.alloc(DictGen(FrozenDictData(SmallMap.new())))

/** Helper type for lookups, not useful. */
data class ValueStr(val str: String) {
    override fun hashCode(): Int = hashStringValue(str)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ValueStr) return false
        return str == other.str
    }

    fun equivalent(key: Value): Boolean = key.unpackStr() == str

    fun equivalent(key: FrozenValue): Boolean = key.unpackStr() == str
}

fun DictGen<AtomicRef<Dict>>.freeze(freezer: Freezer): FreezeResult<DictGen<FrozenDictData>> {
    val content = this.inner.value.content.freeze(freezer) ?: return null
    return FreezeResult.success(DictGen(FrozenDictData(content)))
}

interface DictLike {
    fun content(): SmallMap<Value, Value>
    // These functions are unsafe for the same reason
    // StarlarkValue iterator functions are unsafe.
    fun iterStart()
    fun contentUnchecked(): SmallMap<Value, Value>
    fun iterStop()
    fun setAt(index: Hashed<Value>, value: Value): Result<Unit>
}

class RefCellDictLike(private val cell: AtomicRef<Dict>) : DictLike {
    override fun content(): SmallMap<Value, Value> = cell.value.content

    override fun iterStart() {
        // In Rust: mem::forget(self.borrow())
    }

    override fun iterStop() {
        // In Rust: unleak_borrow(self)
    }

    override fun contentUnchecked(): SmallMap<Value, Value> = cell.value.content

    override fun setAt(index: Hashed<Value>, value: Value): Result<Unit> = try {
        cell.value.content.insertHashed(index, value)
        Result.success(Unit)
    } catch (_: Exception) {
        Result.failure(ValueError.MutationDuringIteration())
    }
}

class FrozenDictDataDictLike(private val data: FrozenDictData) : DictLike {
    @Suppress("UNCHECKED_CAST")
    override fun content(): SmallMap<Value, Value> =
        data.content as SmallMap<Value, Value>

    override fun iterStart() {}

    override fun iterStop() {}

    @Suppress("UNCHECKED_CAST")
    override fun contentUnchecked(): SmallMap<Value, Value> =
        data.content as SmallMap<Value, Value>

    override fun setAt(index: Hashed<Value>, value: Value): Result<Unit> =
        Result.failure(ValueError.CannotMutateImmutableValue())
}

fun dictMethods(): Methods? = DICT_METHODS_STATIC.methods()

private val DICT_METHODS_STATIC = MethodsStatic()

/** StarlarkValue implementation for DictGen<T> where T: DictLike. */
class DictGenStarlarkValue<T : DictLike>(
    val dictGen: DictGen<T>
) : StarlarkValue {

    val canonical: KClass<*> get() = FrozenDict::class

    fun getMethods(): Methods? = dictMethods()

    fun collectRepr(r: StringBuilder) {
        // Fast path as repr() for dicts is quite hot
        r.append('{')
        for ((i, entry) in dictGen.inner.content().iter().withIndex()) {
            if (i != 0) {
                r.append(", ")
            }
            val (name, value) = entry
            name.collectRepr(r)
            r.append(": ")
            value.collectRepr(r)
        }
        r.append('}')
    }

    fun collectReprCycle(collector: StringBuilder) {
        collector.append("{...}")
    }

    fun toBool(): Boolean = !dictGen.inner.content().isEmpty()

    fun equals(other: Value): Result<Boolean> {
        val otherDict = dictRefFromValue(other) ?: return Result.success(false)
        return equalsSmallMap(dictGen.inner.content(), other) { x, y -> x.equals(y) }
    }

    fun at(index: Value, heap: Heap): Result<Value> {
        val hashed = index.getHashed().getOrElse { return Result.failure(it) }
        val v = dictGen.inner.content().getHashedByValue(hashed)
            ?: return Result.failure(ValueError.KeyNotFound(index.toRepr()))
        return Result.success(v.toValue())
    }

    fun length(): Result<Int> = Result.success(dictGen.inner.content().len())

    fun isIn(other: Value): Result<Boolean> {
        val hashed = other.getHashed().getOrElse { return Result.failure(it) }
        return Result.success(dictGen.inner.content().containsKeyHashedByValue(hashed))
    }

    fun iterate(me: Value, heap: Heap): Result<Value> {
        dictGen.inner.iterStart()
        return Result.success(me)
    }

    fun iterSizeHint(index: Int): Pair<Int, Int?> {
        val rem = dictGen.inner.content().len() - index
        return Pair(rem, rem)
    }

    fun iterNext(index: Int, heap: Heap): Value? =
        dictGen.inner.contentUnchecked().keys().elementAtOrNull(index)

    fun iterStop() {
        dictGen.inner.iterStop()
    }

    fun setAt(index: Value, allocValue: Value): Result<Unit> {
        val hashed = index.getHashed().getOrElse { return Result.failure(it) }
        return dictGen.inner.setAt(hashed, allocValue)
    }

    fun bitOr(rhs: Value, heap: Heap): Result<Value> {
        val rhsDict = dictRefFromValue(rhs)
            ?: return Result.failure(ValueError.unsupportedWith(dictGen, "|", rhs))

        if (dictGen.inner.content().isEmpty()) {
            val cloned = when (val ref = rhsDict.aref) {
                is Either.Left -> ref.value.value
                is Either.Right -> ref.value
            }
            return Result.success(heap.allocComplex(cloned.clone()))
        }

        // Might be faster if we preallocate the capacity, but then copying in the LHS
        // is more expensive and might oversize given the behaviour on duplicates.
        // If this becomes a bottleneck, benchmark.
        val items = dictGen.inner.content().clone()
        val rhsDictValue = when (val ref = rhsDict.aref) {
            is Either.Left -> ref.value.value
            is Either.Right -> ref.value
        }
        for ((k, v) in rhsDictValue.iterHashed()) {
            items.insertHashed(k, v)
        }
        return Result.success(heap.allocComplex(DictGen(AtomicRef(Dict.new(items)))))
    }

    fun typecheckerTy(): Ty? = Ty.anyDict()

    fun getTypeStarlarkRepr(): Ty = Ty.anyDict()

    fun tryFreezeDirectly(freezer: Freezer): FreezeResult<FrozenValue>? =
        if (dictGen.inner.content().isEmpty()) FreezeResult.success(VALUE_EMPTY_FROZEN_DICT.toFrozenValue())
        else null
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

class AtomicRef<T>(var value: T) {
    fun borrow(): Ref<T> = Ref(value)
    fun tryBorrowMut(): RefMut<T>? = RefMut(value)
}

internal fun hashStringValue(s: String): Int = s.hashCode()

internal fun <K, V> equalsSmallMap(
    a: SmallMap<K, V>,
    b: Any?,
    comparator: (V, V) -> Result<Boolean>
): Result<Boolean> = Result.success(a == b)
