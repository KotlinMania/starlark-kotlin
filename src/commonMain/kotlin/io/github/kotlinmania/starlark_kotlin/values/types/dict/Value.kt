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
import io.github.kotlinmania.starlark_kotlin.cast.transmute
import io.github.kotlinmania.starlark_kotlin.coerce.Coerce
import io.github.kotlinmania.starlark_kotlin.coerce.coerce
import io.github.kotlinmania.starlark_kotlin.collections.Hashed
import io.github.kotlinmania.starlark_kotlin.collections.SmallMap
import io.github.kotlinmania.starlark_kotlin.environment.Methods
import io.github.kotlinmania.starlark_kotlin.environment.MethodsStatic
import io.github.kotlinmania.starlark_kotlin.hint.unlikely
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.util.refcell.unleakBorrow
import io.github.kotlinmania.starlark_kotlin.values.AllocFrozenValue
import io.github.kotlinmania.starlark_kotlin.values.AllocStaticSimple
import io.github.kotlinmania.starlark_kotlin.values.AllocValue
import io.github.kotlinmania.starlark_kotlin.values.Freeze
import io.github.kotlinmania.starlark_kotlin.values.FreezeResult
import io.github.kotlinmania.starlark_kotlin.values.Freezer
import io.github.kotlinmania.starlark_kotlin.values.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.FrozenStringValue
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.Heap
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.StringValue
import io.github.kotlinmania.starlark_kotlin.values.Trace
import io.github.kotlinmania.starlark_kotlin.values.Value
import io.github.kotlinmania.starlark_kotlin.values.ValueLike
import io.github.kotlinmania.starlark_kotlin.values.comparison.equalsSmallMap
import io.github.kotlinmania.starlark_kotlin.values.dict.DictRef
import io.github.kotlinmania.starlark_kotlin.values.error.ValueError
import io.github.kotlinmania.starlark_kotlin.values.string.strType.hashStringValue
import io.github.kotlinmania.starlark_kotlin.values.typeRepr.StarlarkTypeRepr
import io.github.kotlinmania.starlark_kotlin.values.types.dict.dictType.DictType
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlin.reflect.KClass

/**
 * Generic dictionary wrapper type.
 */
@ProvidesStaticType
data class DictGen<T>(val inner: T) : Trace {
    override fun toString(): String {
        return when (inner) {
            is DictLike<*> -> {
                val content = (inner as DictLike<*>).content()
                fmtKeyedContainer("{", "}", ": ", content.iter())
            }
            else -> super.toString()
        }
    }
}

/**
 * Display implementation for Dict<'v>
 */
fun <'v> DictDisplay(dict: Dict<'v>): String {
    return fmtKeyedContainer("{", "}", ": ", dict.iter())
}

/**
 * Define the dict type.
 */
@ProvidesStaticType
@JvmInline
value class Dict<'v>(
    /** The data stored by the dictionary. The keys must all be hashable values. */
    val content: SmallMap<Value<'v>, Value<'v>>
) : Trace {
    companion object {
        /** The result of calling `type()` on dictionaries. */
        const val TYPE: String = "dict"

        /** Dict type string as Starlark frozen string value. */
        fun getTypeValueStatic(): FrozenStringValue {
            return DictGen.getTypeValueStatic<FrozenDictData>()
        }

        /**
         * This function is deprecated.
         * Use [AllocDict] or [SmallMap] to allocate a new dictionary on the heap.
         */
        @Deprecated("Use AllocDict or SmallMap")
        fun <'v> new(content: SmallMap<Value<'v>, Value<'v>>): Dict<'v> {
            return Dict(content)
        }

        fun <'v> isDictType(x: KClass<*>): Boolean {
            return x == DictGen::class
        }

        @Suppress("UNCHECKED_CAST")
        fun <'v> fromValueUncheckedMut(x: Value<'v>): AtomicRef<Dict<'v>> {
            val dict = x.downcastRefUnchecked<DictGen<AtomicRef<Dict<'v>>>>()
            return dict.inner
        }
    }

    /** Number of elements in the dict. */
    fun len(): Int = content.len()

    /** Is the dict empty? */
    fun isEmpty(): Boolean = content.isEmpty()

    /** Iterate through the key/value pairs in the dictionary. */
    fun iter(): Sequence<Pair<Value<'v>, Value<'v>>> {
        return content.iter().map { (l, r) -> Pair(l, r) }
    }

    /** Iterate through the key/value pairs in the dictionary, but retaining the hash of the keys. */
    fun iterHashed(): Sequence<Pair<Hashed<Value<'v>>, Value<'v>>> {
        return content.iterHashed().map { (l, r) -> Pair(l.copied(), r) }
    }

    /** Iterator over keys. */
    fun keys(): Sequence<Value<'v>> {
        return content.keys()
    }

    /** Iterator over values. */
    fun values(): Sequence<Value<'v>> {
        return content.values()
    }

    /**
     * Get the value associated with a particular key. Will return [Result.failure] if the key is not hashable,
     * and otherwise [null] if the key doesn't exist or the value if it does.
     */
    fun get(key: Value<'v>): Result<Value<'v>?> {
        return key.getHashed().map { hashed -> getHashed(hashed) }
    }

    /** Lookup the value by the given prehashed key. */
    fun getHashed(key: Hashed<Value<'v>>): Value<'v>? {
        return content.getHashedByValue(key)
    }

    /**
     * Get the value associated with a particular string. Equivalent to allocating the
     * string on the heap, turning it into a value, and looking up using that.
     */
    fun getStr(key: String): Value<'v>? {
        return content.get(ValueStr(key))
    }

    /** Like [getStr], but where you already have the hash. */
    fun getStrHashed(key: Hashed<String>): Value<'v>? {
        return content.getHashedByValue(Hashed.newUnchecked(key.hash(), ValueStr(key.key())))
    }

    /** Try to coerce all keys to strings. */
    fun downcastRefKeyString(): SmallMap<StringValue<'v>, Value<'v>>? {
        for (key in content.keys()) {
            if (unlikely(!key.isStr())) {
                return null
            }
        }

        // Scary part: `SmallMap` has the same repr for `Value` and `StringValue`,
        // and we just checked above that all keys are strings.

        @Suppress("UNUSED_PARAMETER")
        fun <'v> assertCoerce(
            s: SmallMap<StringValue<'v>, Value<'v>>
        ): SmallMap<Value<'v>, Value<'v>> {
            return coerce(s)
        }

        @Suppress("UNCHECKED_CAST")
        return transmute<SmallMap<Value<'v>, Value<'v>>, SmallMap<StringValue<'v>, Value<'v>>>(content)
    }

    /** Reserve capacity to insert [additional] elements without reallocating. */
    fun reserve(additional: Int) {
        content.reserve(additional)
    }

    /** Insert a key/value pair into the dictionary. */
    fun insertHashed(key: Hashed<Value<'v>>, value: Value<'v>): Value<'v>? {
        return content.insertHashed(key, value)
    }

    /** Remove given key from the dictionary. */
    fun removeHashed(key: Hashed<Value<'v>>): Value<'v>? {
        return content.shiftRemoveHashed(key.asRef())
    }

    /** Remove all elements from the dictionary. */
    fun clear() {
        content.clear()
    }
}

/** StarlarkTypeRepr for Dict<'v> */
object DictStarlarkTypeRepr {
    fun <'v> starlarkTypeRepr(): Ty {
        return DictType.starlarkTypeRepr<FrozenValue, FrozenValue>()
    }
}

/**
 * Frozen dict data structure.
 */
@ProvidesStaticType
@JvmInline
value class FrozenDictData(
    /** The data stored by the dictionary. The keys must all be hashable values. */
    val content: SmallMap<FrozenValue, FrozenValue>
) {
    /** Iterate through the key/value pairs in the dictionary. */
    fun iter(): Sequence<Pair<FrozenValue, FrozenValue>> {
        return content.iter().map { (l, r) -> Pair(l, r) }
    }

    /**
     * Get the value associated with a particular string. Equivalent to allocating the
     * string on the heap, turning it into a value, and looking up using that.
     */
    fun getStr(key: String): FrozenValue? {
        return content.get(ValueStr(key))
    }
}

/** Alias is used in `StarlarkDocs` derive. */
typealias FrozenDict = DictGen<FrozenDictData>

/** Mutable dict type */
typealias MutableDict<'v> = DictGen<AtomicRef<Dict<'v>>>

/** Empty frozen dict constant */
val VALUE_EMPTY_FROZEN_DICT: AllocStaticSimple<DictGen<FrozenDictData>> =
    AllocStaticSimple.alloc(DictGen(FrozenDictData(SmallMap.new())))

/** Coerce implementation for FrozenDictData */
@Suppress("UNCHECKED_CAST")
class FrozenDictDataCoerce : Coerce<Dict<*>, FrozenDictData> {
    override fun coerce(value: FrozenDictData): Dict<*> {
        return Dict(value.content as SmallMap<Value<*>, Value<*>>)
    }
}

/** AllocValue for Dict<'v> */
object DictAllocValue {
    fun <'v> allocValue(dict: Dict<'v>, heap: Heap<'v>): Value<'v> {
        return heap.allocComplex(DictGen(atomic(dict)))
    }
}

/** StarlarkTypeRepr for FrozenDictData */
object FrozenDictDataStarlarkTypeRepr {
    fun starlarkTypeRepr(): Ty {
        return Ty.dict(Ty.any(), Ty.any())
    }
}

/** AllocFrozenValue for FrozenDictData */
object FrozenDictDataAllocFrozenValue {
    fun allocFrozenValue(data: FrozenDictData, heap: FrozenHeap): FrozenValue {
        return if (data.content.isEmpty()) {
            VALUE_EMPTY_FROZEN_DICT.toFrozenValue()
        } else {
            heap.allocSimple(DictGen(data))
        }
    }
}

/**
 * Helper type for lookups, not useful externally.
 */
data class ValueStr(val str: String) {
    override fun hashCode(): Int {
        return hashStringValue(str, null)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ValueStr) return false
        return str == other.str
    }
}

/** Equivalent trait implementation for ValueStr with Value<'v> */
fun <'v> ValueStr.equivalent(key: Value<'v>): Boolean {
    return key.unpackStr() == this.str
}

/** Equivalent trait implementation for ValueStr with FrozenValue */
fun ValueStr.equivalentFrozen(key: FrozenValue): Boolean {
    return key.unpackStr() == this.str
}

/** Freeze implementation for DictGen<AtomicRef<Dict<'v>>> */
fun <'v> freezeDictGen(
    dict: DictGen<AtomicRef<Dict<'v>>>,
    freezer: Freezer
): FreezeResult<DictGen<FrozenDictData>> {
    val innerDict = dict.inner.value
    val frozenContent = innerDict.content.freeze(freezer)?
    return FreezeResult.success(DictGen(FrozenDictData(frozenContent)))
}

/**
 * DictLike trait - provides common interface for both mutable and frozen dicts.
 */
interface DictLike<'v> {
    fun content(): SmallMap<Value<'v>, Value<'v>>

    // These functions are unsafe for the same reason StarlarkValue iterator functions are unsafe.
    fun iterStart()
    fun contentUnchecked(): SmallMap<Value<'v>, Value<'v>>
    fun iterStop()
    fun setAt(index: Hashed<Value<'v>>, value: Value<'v>): Result<Unit>
}

/** DictLike implementation for AtomicRef<Dict<'v>> */
class RefCellDictLike<'v>(private val cell: AtomicRef<Dict<'v>>) : DictLike<'v> {
    override fun content(): SmallMap<Value<'v>, Value<'v>> {
        return cell.value.content
    }

    override fun iterStart() {
        // In Rust, this does mem::forget(self.borrow())
        // In Kotlin, we don't have the same borrow checking, so this is a no-op
    }

    override fun iterStop() {
        // In Rust, this calls unleak_borrow
        // In Kotlin, we don't have the same borrow checking, so this is a no-op
    }

    override fun contentUnchecked(): SmallMap<Value<'v>, Value<'v>> {
        // In Rust, this unsafely accesses the content without borrowing
        // In Kotlin, we just access the value
        return cell.value.content
    }

    override fun setAt(index: Hashed<Value<'v>>, value: Value<'v>): Result<Unit> {
        return try {
            val dict = cell.value
            dict.content.insertHashed(index, value)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(ValueError.MutationDuringIteration())
        }
    }
}

/** DictLike implementation for FrozenDictData */
class FrozenDictDataDictLike<'v>(private val data: FrozenDictData) : DictLike<'v> {
    @Suppress("UNCHECKED_CAST")
    override fun content(): SmallMap<Value<'v>, Value<'v>> {
        return coerce(data.content) as SmallMap<Value<'v>, Value<'v>>
    }

    override fun iterStart() {}

    override fun iterStop() {}

    @Suppress("UNCHECKED_CAST")
    override fun contentUnchecked(): SmallMap<Value<'v>, Value<'v>> {
        return coerce(data.content) as SmallMap<Value<'v>, Value<'v>>
    }

    override fun setAt(index: Hashed<Value<'v>>, value: Value<'v>): Result<Unit> {
        return Result.failure(ValueError.CannotMutateImmutableValue())
    }
}

/** Get dictionary methods */
fun dictMethods(): Methods? {
    return RES.methods()
}

private object RES : MethodsStatic() {
    override fun methods(): Methods {
        return crate.values.types.dict.methods.dictMethods()
    }
}

/**
 * StarlarkValue implementation for DictGen<T>
 * where T: DictLike<'v>
 */
@StarlarkValue(type = Dict.TYPE)
interface DictGenStarlarkValue<'v, T> : StarlarkValue<'v>
    where T : DictLike<'v>,
          T : ProvidesStaticType<'v> {

    val dictGen: DictGen<T>

    override val canonical get() = FrozenDict::class

    override fun getMethods(): Methods? = dictMethods()

    override fun collectRepr(r: StringBuilder) {
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

    override fun collectReprCycle(collector: StringBuilder) {
        collector.append("{...}")
    }

    override fun toBool(): Boolean {
        return !dictGen.inner.content().isEmpty()
    }

    override fun equals(other: Value<'v>): Result<Boolean> {
        val otherDict = DictRef.fromValue(other) ?: return Result.success(false)
        return equalsSmallMap(
            dictGen.inner.content(),
            otherDict.content
        ) { x, y -> x.equals(y) }
    }

    override fun at(index: Value<'v>, heap: Heap<'v>): Result<Value<'v>> {
        val hashed = index.getHashed().getOrElse { return Result.failure(it) }
        val value = dictGen.inner.content().getHashedByValue(hashed)
        return if (value != null) {
            Result.success(value.toValue())
        } else {
            Result.failure(ValueError.KeyNotFound(index.toRepr()))
        }
    }

    override fun length(): Result<Int> {
        return Result.success(dictGen.inner.content().len())
    }

    override fun isIn(other: Value<'v>): Result<Boolean> {
        val hashed = other.getHashed().getOrElse { return Result.failure(it) }
        return Result.success(dictGen.inner.content().containsKeyHashedByValue(hashed))
    }

    override fun iterate(me: Value<'v>, heap: Heap<'v>): Result<Value<'v>> {
        dictGen.inner.iterStart()
        return Result.success(me)
    }

    override fun iterSizeHint(index: Int): Pair<Int, Int?> {
        check(index <= dictGen.inner.content().len()) {
            "Index $index exceeds dict length ${dictGen.inner.content().len()}"
        }
        val rem = dictGen.inner.content().len() - index
        return Pair(rem, rem)
    }

    override fun iterNext(index: Int, heap: Heap<'v>): Value<'v>? {
        return dictGen.inner.contentUnchecked().keys().elementAtOrNull(index)
    }

    override fun iterStop() {
        dictGen.inner.iterStop()
    }

    override fun setAt(index: Value<'v>, value: Value<'v>): Result<Unit> {
        val hashed = index.getHashed().getOrElse { return Result.failure(it) }
        return dictGen.inner.setAt(hashed, value)
    }

    override fun bitOr(rhs: Value<'v>, heap: Heap<'v>): Result<Value<'v>> {
        val rhsDict = DictRef.fromValue(rhs)
            ?: return ValueError.unsupportedWith(this, "|", rhs)

        if (dictGen.inner.content().isEmpty()) {
            return Result.success(heap.alloc(rhsDict.clone()))
        }

        // Might be faster if we preallocate the capacity, but then copying in the LHS
        // is more expensive and might oversize given the behaviour on duplicates.
        // If this becomes a bottleneck, benchmark.
        val items = dictGen.inner.content().clone()
        for ((k, v) in rhsDict.iterHashed()) {
            items.insertHashed(k, v)
        }
        return Result.success(heap.alloc(Dict.new(items)))
    }

    override fun typecheckerTy(): Ty? {
        return Ty.anyDict()
    }

    override fun getTypeStarlarkRepr(): Ty {
        return Ty.anyDict()
    }

    override fun tryFreezeDirectly(freezer: Freezer): FreezeResult<FrozenValue>? {
        return if (dictGen.inner.content().isEmpty()) {
            FreezeResult.success(VALUE_EMPTY_FROZEN_DICT.toFrozenValue())
        } else {
            null
        }
    }
}

/**
 * Serialize implementation for DictGen<T>
 */
fun <'v, T : DictLike<'v>> serializeDictGen(
    dict: DictGen<T>,
    serializer: kotlinx.serialization.SerializationStrategy<Map<*, *>>
): Result<Unit> {
    return try {
        val map = dict.inner.content().iter().toMap()
        serializer.serialize(kotlinx.serialization.encoding.Encoder.INSTANCE, map)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

/**
 * Helper function to format keyed containers like "{key1: value1, key2: value2}"
 */
private fun <K, V> fmtKeyedContainer(
    start: String,
    end: String,
    sep: String,
    iter: Sequence<Pair<K, V>>
): String {
    val builder = StringBuilder()
    builder.append(start)
    var first = true
    for ((k, v) in iter) {
        if (!first) {
            builder.append(", ")
        }
        builder.append(k.toString())
        builder.append(sep)
        builder.append(v.toString())
        first = false
    }
    builder.append(end)
    return builder.toString()
}
