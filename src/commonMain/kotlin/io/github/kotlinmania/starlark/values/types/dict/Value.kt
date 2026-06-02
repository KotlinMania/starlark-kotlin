// port-lint: source src/values/types/dict/value.rs
package io.github.kotlinmania.starlark.values.types.dict

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

import io.github.kotlinmania.starlark.collections.Equivalent
import io.github.kotlinmania.starlark.collections.Hashed
import io.github.kotlinmania.starlark.collections.SmallMap
import io.github.kotlinmania.starlark.environment.Methods
import io.github.kotlinmania.starlark.environment.MethodsStatic
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.ComplexValue
import io.github.kotlinmania.starlark.values.Freeze
import io.github.kotlinmania.starlark.values.Trace
import io.github.kotlinmania.starlark.values.ValueError
import io.github.kotlinmania.starlark.values.freezeSmallMap
import io.github.kotlinmania.starlark.values.layout.Freezer
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.avalues.AllocStaticSimple
import io.github.kotlinmania.starlark.values.layout.avalues.allocComplex
import io.github.kotlinmania.starlark.values.layout.avalues.simple.allocSimple
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.layout.heap.Tracer
import io.github.kotlinmania.starlark.values.layout.heap.ValueHolder
import io.github.kotlinmania.starlark.values.layout.typed.StringValue
import kotlin.reflect.KClass

internal data class DictGen<T>(
    val inner: T,
) : ComplexValue,
    Trace {
    override val TYPE: String get() = Dict.TYPE
    override val HAS_iterate: Boolean get() = true
    override val HAS_equals: Boolean get() = true

    override fun trace(tracer: Tracer) {
        val innerVal = inner
        if (innerVal is Trace) {
            innerVal.trace(tracer)
        } else if (innerVal is AtomicRef<*>) {
            val v = innerVal.value
            if (v is Trace) {
                v.trace(tracer)
            }
        }
    }

    override fun toString(): String =
        when (val innerVal = inner.asDictLike()) {
            null -> super.toString()
            else -> {
                fmtKeyedContainer("{", "}", ": ", innerVal.content().iter())
            }
        }

    // --- StarlarkValue implementation (mirrors Rust's #[starlark_value] impl) ---

    override fun getMethods(): Methods? = getDictMethods()

    override fun collectRepr(collector: StringBuilder) {
        val innerVal = inner.asDictLike()
        if (innerVal != null) {
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
        val innerVal = inner.asDictLike()
        return if (innerVal != null) {
            !innerVal.content().isEmpty()
        } else {
            true
        }
    }

    override fun equals(other: Value): Result<Boolean> {
        val innerVal = inner.asDictLike() ?: return Result.success(false)
        val otherDict = dictRefFromValue(other) ?: return Result.success(false)
        return io.github.kotlinmania.starlark.values.equalsSmallMap<Exception, Value, Value, Value>(
            innerVal.content(),
            getDictFromRef(otherDict).content,
        ) { x, y -> x.equals(y) }
    }

    override fun at(index: Value, heap: Heap): Result<Value> {
        val innerVal = inner.asDictLike() ?: return ValueError.unsupported(TYPE, "[]")
        val hashed = index.getHashed().getOrElse { return Result.failure(it) }
        val v =
            innerVal.content().getHashedByValue(hashed)
                ?: return Result.failure(ValueError.KeyNotFound(index.toRepr()))
        return Result.success(v.toValue())
    }

    override fun length(): Result<Int> {
        val innerVal = inner.asDictLike() ?: return ValueError.unsupported(TYPE, "len()")
        return Result.success(innerVal.content().len())
    }

    override fun isIn(other: Value): Result<Boolean> {
        val innerVal = inner.asDictLike() ?: return ValueError.unsupported(TYPE, "in")
        val hashed = other.getHashed().getOrElse { return Result.failure(it) }
        return Result.success(innerVal.content().getHashedByValue(hashed) != null)
    }

    override fun iterate(me: Value, heap: Heap): Result<Value> {
        val innerVal = inner.asDictLike() ?: return ValueError.unsupported(TYPE, "(iter)")
        innerVal.iterStart()
        return Result.success(me)
    }

    override fun iterSizeHint(index: Int): Pair<Int, Int?> {
        val innerVal = inner.asDictLike() ?: return Pair(0, null)
        val rem = innerVal.content().len() - index
        return Pair(rem, rem)
    }

    override fun iterNext(index: Int, heap: Heap): Value? {
        val innerVal = inner.asDictLike() ?: return null
        return innerVal.contentUnchecked().keys().elementAtOrNull(index)
    }

    override fun iterStop() {
        inner.asDictLike()?.iterStop()
    }

    override fun setAt(index: Value, newValue: Value): Result<Unit> {
        val innerVal = inner.asDictLike() ?: return Result.failure(ValueError.CannotMutateImmutableValue)
        val hashed = index.getHashed().getOrElse { return Result.failure(it) }
        return innerVal.setAt(hashed, newValue)
    }

    override fun bitOr(other: Value, heap: Heap): Result<Value> {
        val innerVal = inner
        if (innerVal !is DictLike) return ValueError.unsupportedWith(TYPE, "|", other)

        val rhsDict =
            dictRefFromValue(other)
                ?: return ValueError.unsupportedWith(TYPE, "|", other)

        if (innerVal.content().isEmpty()) {
            // Clone rhs and return
            val rhsDictVal = getDictFromRef(rhsDict)
            val clonedContent = SmallMap.withCapacity<Value, Value>(rhsDictVal.len())
            for ((k, v) in rhsDictVal.iterHashed()) {
                clonedContent.insertHashed(k, v)
            }
            return Result.success(heap.allocComplex(MutableDict(DictGen(AtomicRef(Dict.new(clonedContent))))))
        }

        val items = SmallMap.withCapacity<Value, Value>(innerVal.content().len())
        for ((k, v) in innerVal.content().iterHashed()) {
            items.insertHashed(k, v)
        }
        val rhsDictVal = getDictFromRef(rhsDict)
        for ((k, v) in rhsDictVal.iterHashed()) {
            items.insertHashed(k, v)
        }
        return Result.success(heap.allocComplex(MutableDict(DictGen(AtomicRef(Dict.new(items))))))
    }

    override fun typecheckerTy(): Ty = Ty.anyDict()

    override fun getTypeStarlarkRepr(): Ty = Ty.anyDict()

    override fun tryFreezeDirectly(freezer: Freezer): Result<FrozenValue>? {
        val innerVal = inner
        if (innerVal.asDictLike()?.content()?.isEmpty() == true) {
            return Result.success(VALUE_EMPTY_FROZEN_DICT.toFrozenValue())
        }
        return null
    }
}

internal fun Dict.display(): String =
    fmtKeyedContainer("{", "}", ": ", iter())

/** Define the dict type. */
class Dict(
    /** The data stored by the dictionary. The keys must all be hashable values. */
    val content: SmallMap<Value, Value>,
) : Trace {
    companion object {
        /** The result of calling `type()` on dictionaries. */
        const val TYPE: String = "dict"

        /** This function is deprecated. Use [AllocDict] or [SmallMap] to allocate a new dictionary on the heap. */
        fun new(content: SmallMap<Value, Value>): Dict = Dict(content)

        fun isDictType(x: KClass<*>): Boolean =
            x == FrozenDict::class || x == MutableDict::class

        fun fromValueUncheckedMut(x: Value): Dict {
            val dict = dictGenFromValue(x)!!
            val inner = dict.inner
            check(inner is AtomicRef<*>)
            val mutable = inner.value
            check(mutable is Dict)
            return mutable
        }
    }

    override fun trace(tracer: Tracer) {
        for (entry in content.entries) {
            val keyHolder = ValueHolder(entry.key.key())
            tracer.trace(keyHolder)
            entry.key = Hashed.newUnchecked(entry.key.hash(), keyHolder.value)
            val valueHolder = ValueHolder(entry.value)
            tracer.trace(valueHolder)
            entry.value = valueHolder.value
            entry.key = Hashed.newUnchecked(entry.key.hash(), keyHolder.value)
        }
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
        val result = SmallMap.withCapacity<StringValue, Value>(content.len())
        for ((key, value) in content.iterHashed()) {
            val stringKey = StringValue.new(key.key()) ?: return null
            result.insertHashedUniqueUnchecked(
                Hashed.newUnchecked(key.hash(), stringKey),
                value,
            )
        }
        return result
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

internal fun Dict.allocValue(heap: Heap): Value =
    heap.allocComplex(MutableDict(DictGen(AtomicRef(this))))

class FrozenDictData(
    /** The data stored by the dictionary. The keys must all be hashable values. */
    val content: SmallMap<FrozenValue, FrozenValue>,
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
        if (content.isEmpty()) {
            VALUE_EMPTY_FROZEN_DICT.toFrozenValue()
        } else {
            heap.allocSimple(FrozenDict(DictGen(this)))
        }
}

internal fun FrozenDictData.toValueMap(): SmallMap<Value, Value> {
    val result = SmallMap.withCapacity<Value, Value>(content.len())
    for ((key, value) in content.iterHashed()) {
        result.insertHashedUniqueUnchecked(
            Hashed.newUnchecked(key.hash(), key.key().toValue()),
            value.toValue(),
        )
    }
    return result
}

private fun Any?.asDictLike(): DictLike? =
    when (this) {
        is DictLike -> this
        is FrozenDictData -> FrozenDictDataDictLike(this)
        is AtomicRef<*> -> {
            val dict = value
            if (dict is Dict) {
                RefCellDictLike(this)
            } else {
                null
            }
        }
        else -> null
    }

class FrozenDict internal constructor(
    internal val delegate: DictGen<FrozenDictData>,
) : ComplexValue by delegate,
    Trace {
    val inner: FrozenDictData get() = delegate.inner

    override fun trace(tracer: Tracer) {
        delegate.trace(tracer)
    }

    override fun toString(): String = delegate.toString()

    companion object {
        fun fromValue(value: Value): FrozenDict? = value.downcastRef<FrozenDict>()
    }
}

class MutableDict internal constructor(
    internal val delegate: DictGen<AtomicRef<Dict>>,
) : ComplexValue by delegate,
    Trace,
    Freeze<FrozenDict> {
    internal val inner: AtomicRef<Dict> get() = delegate.inner

    override fun trace(tracer: Tracer) {
        delegate.trace(tracer)
    }

    override fun toString(): String = delegate.toString()

    override fun freeze(freezer: Freezer): Result<FrozenDict> {
        val dict = inner.value
        return freezeDictData(dict, freezer)
    }

    companion object {
        fun fromValue(value: Value): MutableDict? = value.downcastRef<MutableDict>()
    }
}

internal val VALUE_EMPTY_FROZEN_DICT: AllocStaticSimple<FrozenDict> =
    AllocStaticSimple.alloc(FrozenDict(DictGen(FrozenDictData(SmallMap.new()))))

internal fun dictGenFromValue(value: Value): DictGen<*>? =
    value.downcastRef<MutableDict>()?.delegate
        ?: value.downcastRef<FrozenDict>()?.delegate

/** Helper type for lookups, not useful. */
data class ValueStr(
    val str: String,
) : Equivalent<Value> {
    override fun hashCode(): Int = hashStringValue(str)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ValueStr) return false
        return str == other.str
    }

    override fun equivalent(key: Value): Boolean = key.unpackStr() == str
}

/** Freeze implementation for MutableDict (mutable dict). */
internal fun MutableDict.freezeDict(freezer: Freezer): Result<FrozenDict> {
    val content = freezeDictContent(inner.value, freezer).getOrElse { return Result.failure(it) }
    return Result.success(FrozenDict(DictGen(FrozenDictData(content))))
}

private fun freezeDictData(
    dict: Dict,
    freezer: Freezer,
): Result<FrozenDict> {
    val content = freezeDictContent(dict, freezer).getOrElse { return Result.failure(it) }
    return Result.success(FrozenDict(DictGen(FrozenDictData(content))))
}

private fun freezeDictContent(
    dict: Dict,
    freezer: Freezer,
): Result<SmallMap<FrozenValue, FrozenValue>> {
    val frozenContent =
        freezeSmallMap(
            dict.content,
            freezer,
            { v, f -> v.freeze(f) },
            { v, f -> v.freeze(f) },
        )
    return Result.success(frozenContent.getOrElse { return Result.failure(it) })
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

internal class RefCellDictLike(
    private val cell: AtomicRef<*>,
) : DictLike {
    private fun dict(): Dict =
        cell.value as? Dict
            ?: error("not a mutable dict")

    override fun content(): SmallMap<Value, Value> = dict().content

    override fun iterStart() {
        // In Rust: mem::forget(self.borrow())
    }

    override fun iterStop() {
        // In Rust: unleak_borrow(self)
    }

    override fun contentUnchecked(): SmallMap<Value, Value> = dict().content

    override fun setAt(index: Hashed<Value>, value: Value): Result<Unit> =
        try {
            dict().content.insertHashed(index, value)
            Result.success(Unit)
        } catch (_: Exception) {
            Result.failure(ValueError.MutationDuringIteration)
        }
}

class FrozenDictDataDictLike(
    private val data: FrozenDictData,
) : DictLike {
    override fun content(): SmallMap<Value, Value> =
        data.toValueMap()

    override fun iterStart() {}

    override fun iterStop() {}

    override fun contentUnchecked(): SmallMap<Value, Value> =
        data.toValueMap()

    override fun setAt(index: Hashed<Value>, value: Value): Result<Unit> =
        Result.failure(ValueError.CannotMutateImmutableValue)
}

fun getDictMethods(): Methods = DICT_METHODS_STATIC.methods(::dictMethods)

private val DICT_METHODS_STATIC = MethodsStatic()

/** Helper to get Dict from a DictRef. */
private fun getDictFromRef(ref: DictRef): Dict =
    when (val aref = ref.aref) {
        is Either.Left -> aref.value.value
        is Either.Right -> aref.value
    }

internal fun <K, V> fmtKeyedContainer(
    start: String,
    end: String,
    sep: String,
    iter: Sequence<Pair<K, V>>,
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

internal class AtomicRef<T>(
    var value: T,
) : Trace {
    override fun trace(tracer: Tracer) {
        val v = value
        if (v is Trace) {
            v.trace(tracer)
        }
    }

    internal fun borrow(): Ref<T> = Ref(value)

    internal fun tryBorrowMut(): RefMut<T> = RefMut(value)
}

internal fun hashStringValue(s: String): Int = s.hashCode()
