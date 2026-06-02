// port-lint: source src/values/types/structs/value.rs
package io.github.kotlinmania.starlark.values.types.structs

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

import io.github.kotlinmania.starlark.collections.Hashed
import io.github.kotlinmania.starlark.collections.SmallMap
import io.github.kotlinmania.starlark.collections.StarlarkHasher
import io.github.kotlinmania.starlark.docs.DocItem
import io.github.kotlinmania.starlark.docs.DocMember
import io.github.kotlinmania.starlark.docs.DocProperty
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.typing.TyStruct
import io.github.kotlinmania.starlark.values.ComplexValue
import io.github.kotlinmania.starlark.values.Freeze
import io.github.kotlinmania.starlark.values.FreezeResult
import io.github.kotlinmania.starlark.values.Trace
import io.github.kotlinmania.starlark.values.ValueError
import io.github.kotlinmania.starlark.values.compareSmallMap
import io.github.kotlinmania.starlark.values.layout.Freezer
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.layout.heap.Tracer
import io.github.kotlinmania.starlark.values.layout.heap.ValueHolder

/**
 * The result of calling `struct()`.
 *
 * This is a generic struct implementation parametrized over V which represents
 * either Value or FrozenValue in the Rust implementation. The lifetime parameter
 * 'v from Rust is handled through Kotlin's type system.
 */
internal fun interface StructTraceField<V> {
    fun trace(
        value: V,
        tracer: Tracer,
    ): V
}

internal fun interface StructFreezeField<V> {
    fun freeze(
        value: V,
        freezer: Freezer,
    ): Result<FrozenValue>
}

class StructGen<V> internal constructor(
    /** The fields in a struct. */
    val fields: SmallMap<String, V>,
    private val traceField: StructTraceField<V>? = null,
    private val freezeField: StructFreezeField<V>? = null,
) : io.github.kotlinmania.starlark.values.StarlarkValue,
    ComplexValue,
    Trace,
    Freeze<FrozenStruct> {
    override val TYPE: String get() = Companion.TYPE
    override val HAS_equals: Boolean get() = true

    companion object {
        /** The result of calling `type()` on a struct. */
        const val TYPE: String = "struct"

        fun mutable(fields: SmallMap<String, Value>): Struct =
            StructGen(
                fields = fields,
                traceField =
                    StructTraceField { value, tracer ->
                        val holder = ValueHolder(value)
                        tracer.trace(holder)
                        holder.value
                    },
                freezeField =
                    StructFreezeField { value, freezer ->
                        freezer.freeze(value)
                    },
            )

        fun frozen(fields: SmallMap<String, FrozenValue>): FrozenStruct =
            StructGen(
                fields = fields,
                freezeField =
                    StructFreezeField { value, _ ->
                        Result.success(value)
                    },
            )
    }

    override fun trace(tracer: Tracer) {
        val traceField = traceField ?: return
        for (entry in fields.entries) {
            entry.value = traceField.trace(entry.value, tracer)
        }
    }

    override fun freeze(freezer: Freezer): FreezeResult<FrozenStruct> {
        val freezeField =
            freezeField
                ?: return Result.failure(IllegalStateException("Struct fields cannot be frozen"))
        val frozenFields = SmallMap.withCapacity<String, FrozenValue>(fields.len())
        for ((k, v) in fields.iter()) {
            val frozenVal = freezeField.freeze(v, freezer).getOrElse { return Result.failure(it) }
            frozenFields.insert(k, frozenVal)
        }
        return Result.success(frozen(frozenFields))
    }

    /**
     * Create a new [Struct].
     */
    fun new(fields: SmallMap<String, V>): StructGen<V> = StructGen(fields)

    /**
     * Iterate over the elements in the struct.
     *
     * In Rust, this returns (StringValue, V) pairs. Since the Kotlin port uses
     * plain String keys in SmallMap, this returns (String, V) pairs instead.
     */
    fun iter(): Sequence<Pair<String, V>> = fields.iter()

    private fun selfTy(): Ty {
        // Rust: Ty::of_value(value.to_value()) for each field value.
        // Ty::of_value is not yet ported; approximate with Ty.any() per value.
        return Ty.custom(
            TyStruct(
                fields =
                    fields.iter().associate { (k, _) ->
                        k to Ty.any()
                    },
                extra = false,
            ),
        )
    }

    override fun toString(): String =
        buildString {
            append("struct(")
            val items = iter().toList()
            items.forEachIndexed { index, (key, value) ->
                append(key)
                append("=")
                append(value)
                if (index < items.size - 1) {
                    append(", ")
                }
            }
            append(")")
        }

    override fun collectReprCycle(collector: StringBuilder) {
        collector.append("struct(...)")
    }

    override fun equals(other: Value): Result<Boolean> {
        val otherStruct = Struct.fromValue(other) ?: return Result.success(false)
        if (fields.len() != otherStruct.fields.len()) {
            return Result.success(false)
        }
        for ((key, value) in fields.iter()) {
            val lhs = value.asStructValueOrNull() ?: return Result.success(false)
            val rhs =
                otherStruct.fields.get(key)?.asStructValueOrNull()
                    ?: return Result.success(false)
            val equal = lhs.equals(rhs).getOrElse { return Result.failure(it) }
            if (!equal) {
                return Result.success(false)
            }
        }
        return Result.success(true)
    }

    override fun compare(other: Value): Result<Int> {
        val otherStruct =
            Struct.fromValue(other)
                ?: return ValueError.unsupportedWith(TYPE, "cmp()", other)

        val otherFields =
            otherStruct.valueFieldsOrNull()
                ?: return Result.failure(IllegalStateException("Unsupported value type in struct"))
        return compareSmallMap<Exception, String, String, Value, Value>(
            valueFieldsOrNull()
                ?: return Result.failure(IllegalStateException("Unsupported value type in struct")),
            otherFields,
            key = { k: String -> k },
        ) { x, y -> x.compare(y) }
    }

    override fun getAttr(attribute: String, heap: Heap): Value? = getAttrHashed(Hashed.new(attribute), heap)

    override fun getAttrHashed(attribute: Hashed<String>, heap: Heap): Value? = fields.getHashedByValue(attribute)?.asStructValueOrNull()

    override fun writeHash(hasher: StarlarkHasher): Result<Unit> {
        // Must use unordered hash because equality is unordered,
        // and `a = b  =>  hash(a) = hash(b)`.
        val unorderedHasher = UnorderedHasher.new()

        for ((hashedKey, v) in fields.iterHashed()) {
            // Should hash key and value together, so two structs
            // `a=1 b=2` and `a=2 b=1` would produce different hashes.
            val entryHasher = StarlarkHasher()
            // Hash the key's hash value into the entry hasher
            entryHasher.writeU32(hashedKey.hash().get())
            // Hash the value
            val value = v.asStructValueOrNull() ?: return Result.failure(IllegalStateException("Unsupported value type in struct"))
            value.writeHash(entryHasher).getOrElse { return Result.failure(it) }
            unorderedHasher.writeHash(entryHasher.finish())
        }

        hasher.writeU64(unorderedHasher.finish())

        return Result.success(Unit)
    }

    override fun dirAttr(): List<String> = fields.keys().toList()

    override fun documentation(): DocItem {
        // This treats structs as being value-like, and intentionally generates bad docs in the case
        // of namespace-like usage. See
        // <https://fb.workplace.com/groups/starlark/permalink/1463680027654154/> for some
        // additional discussion
        val typ = selfTy()
        return DocItem.Member(DocMember.Property(DocProperty(typ = typ)))
    }

    override fun getTypeStarlarkRepr(): Ty = Ty.anyStruct()

    override fun typecheckerTy(): Ty = selfTy()

    /**
     * Serialize to map format matching Rust serde implementation.
     */
    fun serialize(): Map<String, V> = iter().associate { (k, v) -> k to v }

    private fun valueFieldsOrNull(): SmallMap<String, Value>? {
        val values = SmallMap.withCapacity<String, Value>(fields.len())
        for ((key, value) in fields.iter()) {
            values.insert(key, value.asStructValueOrNull() ?: return null)
        }
        return values
    }
}

/**
 * Extension for StructGen<FrozenValue> to iterate with frozen types.
 */
fun StructGen<FrozenValue>.iterFrozen(): Sequence<Pair<String, FrozenValue>> = fields.iter()

/**
 * Type alias for mutable Struct - corresponds to starlark_complex_value!(pub(crate) Struct)
 */
typealias Struct = StructGen<Value>

/**
 * Type alias for frozen struct.
 */
typealias FrozenStruct = StructGen<FrozenValue>

/**
 * Helper function to extract struct from a value.
 */
fun StructGen.Companion.fromValue(value: Value): StructGen<*>? {
    // Try to get as unfrozen struct first, then try frozen and coerce
    return value.downcastRef<Struct>()
        ?: value.downcastRef<FrozenStruct>()
}

internal fun Any?.asStructValueOrNull(): Value? =
    when (this) {
        is Value -> this
        is FrozenValue -> toValue()
        else -> null
    }
