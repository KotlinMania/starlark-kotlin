// port-lint: source src/values/types/structs/value.rs
package io.github.kotlinmania.starlark_kotlin.values.types.structs

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

import kotlinx.serialization.Serializable
import io.github.kotlinmania.starlark_kotlin.values.typing.TyStruct
import io.github.kotlinmania.starlark_kotlin.values.types.tuple.Ty
import io.github.kotlinmania.starlark_kotlin.values.layout.typed.FrozenStringValue
import io.github.kotlinmania.starlark_kotlin.values.types.string.ValueLike
import io.github.kotlinmania.starlark_kotlin.values.types.string.StringValue
import io.github.kotlinmania.starlark_kotlin.values.types.string.Hashed
import io.github.kotlinmania.starlark_kotlin.values.types.enumeration.value.StarlarkHasher
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.profile.SmallMap
import io.github.kotlinmania.starlark_kotlin.values.ValueError
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.util.ArcStr
import io.github.kotlinmania.starlark_kotlin.docs.DocProperty
import io.github.kotlinmania.starlark_kotlin.docs.DocMember
import io.github.kotlinmania.starlark_kotlin.docs.DocItem
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import starlark_map.writeU64
import io.github.kotlinmania.starlark_kotlin.values.types.dict.getHashed
import io.github.kotlinmania.starlark_kotlin.values.toValue
import io.github.kotlinmania.starlark_kotlin.util.asStr
import io.github.kotlinmania.starlark_kotlin.stdlib.new
import io.github.kotlinmania.starlark_kotlin.values.writeHash
import io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.custom
import io.github.kotlinmania.starlark_kotlin.values.typing.anyStruct
import io.github.kotlinmania.starlark_kotlin.values.types.tuple.it
import io.github.kotlinmania.starlark_kotlin.values.types.set.iterHashed
import io.github.kotlinmania.starlark_kotlin.values.layout.typed.toStringValue
import io.github.kotlinmania.starlark_kotlin.values.hash
import io.github.kotlinmania.starlark_kotlin.values.equalsSmallMap
import io.github.kotlinmania.starlark_kotlin.values.compare
import io.github.kotlinmania.starlark_kotlin.typing.fill_types_for_lint.ofValue
import io.github.kotlinmania.starlark_kotlin.coerce
import io.github.kotlinmania.starlark_kotlin.any.downcastRef
import io.github.kotlinmania.starlark_kotlin.analysis.keys

/**
 * The result of calling `struct()`.
 *
 * This is a generic struct implementation parametrized over V which represents
 * either Value<V_> or FrozenValue in the Rust implementation. The lifetime parameter
 * 'v from Rust is handled through Kotlin's type system.
 */
@Serializable
data class StructGen<V>(
    /** The fields in a struct. */
    val fields: SmallMap<String, V>
) where V : ValueLike {

    companion object {
        /** The result of calling `type()` on a struct. */
        const val TYPE: String = "struct"
    }

    /**
     * Create a new [Struct].
     */
    fun new(fields: SmallMap<String, V>): StructGen<V> {
        return StructGen(fields)
    }

    /**
     * Iterate over the elements in the struct.
     */
    fun iter(): Sequence<Pair<StringValue, V>> {
        return fields.asSequence().map { (name, value) ->
            name.toStringValue() to value
        }
    }

    private fun selfTy(): Ty {
        return Ty.custom(TyStruct(
            fields = fields.mapKeys { (k, _) -> ArcStr.from(k.asStr()) }
                .mapValues { (_, v) -> Ty.ofValue(v.toValue()) },
            extra = false
        ))
    }

    override fun toString(): String {
        return buildString {
            append("struct(")
            val items = iter().toList()
            items.forEachIndexed { index, (key, value) ->
                append(key.asStr())
                append("=")
                append(value)
                if (index < items.size - 1) {
                    append(", ")
                }
            }
            append(")")
        }
    }

    fun collectReprCycle(collector: StringBuilder) {
        collector.append("struct(...)")
    }

    fun equals(other: Value): Result<Boolean> {
        val otherStruct = Struct.fromValue(other) ?: return Result.success(false)
        return equalsSmallMap(
            coerce(fields),
            otherStruct.fields
        ) { x, y -> x.equals(y) }
    }

    fun compare(other: Value): Result<Int> {
        val otherStruct = Struct.fromValue(other)
            ?: return ValueError.unsupportedWith(this, "cmp()", other)
        return compareSmallMap(
            coerce(fields),
            otherStruct.fields,
            keyFn = { k -> k.asStr() }
        ) { x, y -> x.compare(y) }
    }

    fun getAttr(attribute: String, heap: Heap): Value? {
        return getAttrHashed(Hashed.new(attribute), heap)
    }

    fun getAttrHashed(attribute: Hashed<String>, heap: Heap): Value? {
        val coercedFields = coerce<SmallMap<String, Value>>(fields)
        return coercedFields.getHashed(attribute)
    }

    fun writeHash(hasher: StarlarkHasher): Result<Unit> {
        // Must use unordered hash because equality is unordered,
        // and `a = b  =>  hash(a) = hash(b)`.
        val unorderedHasher = UnorderedHasher()

        for ((k, v) in fields.iterHashed()) {
            // Should hash key and value together, so two structs
            // `a=1 b=2` and `a=2 b=1` would produce different hashes.
            val entryHasher = StarlarkHasher()
            k.hash().hash(entryHasher)
            v.writeHash(entryHasher).getOrElse { return Result.failure(it) }
            unorderedHasher.writeHash(entryHasher.finish())
        }

        hasher.writeU64(unorderedHasher.finish())

        return Result.success(Unit)
    }

    fun dirAttr(): List<String> {
        return fields.keys.map { it.asStr() }
    }

    fun documentation(): DocItem {
        // This treats structs as being value-like, and intentionally generates bad docs in the case
        // of namespace-like usage. See
        // <https://fb.workplace.com/groups/starlark/permalink/1463680027654154/> for some
        // additional discussion
        val typ = selfTy()
        return DocItem.Member(DocMember.Property(DocProperty(docs = null, typ = typ)))
    }

    fun getTypeStarlarkRepr(): Ty {
        return Ty.anyStruct()
    }

    fun typecheckerTy(): Ty? {
        return selfTy()
    }

    /**
     * Serialize to map format matching Rust serde implementation.
     */
    fun serialize(): Map<String, V> {
        return iter().associate { (k, v) -> k.asStr() to v }
    }
}

/**
 * Extension for StructGen<FrozenValue> to iterate with frozen types.
 */
fun StructGen<FrozenValue>.iterFrozen(): Sequence<Pair<FrozenStringValue, FrozenValue>> {
    return fields.asSequence().map { (name, value) -> name to value }
}

/**
 * Unsafe coercion for frozen structs - corresponds to Rust's unsafe impl for Coerce.
 */
@Suppress("UNCHECKED_CAST")
fun coerceStruct(frozen: StructGen<FrozenValue>): StructGen<Value> {
    return frozen as StructGen<Value>
}

/**
 * Type alias for mutable Struct - corresponds to starlark_complex_value!(pub(crate) Struct<V_>)
 */
typealias Struct = StructGen<Value>

/**
 * Type alias for frozen struct.
 */
typealias FrozenStruct = StructGen<FrozenValue>

/**
 * Helper function to extract struct from a value.
 */
fun Struct.Companion.fromValue(value: Value): Struct? {
    // Try to get as unfrozen struct first, then try frozen and coerce
    return value.downcastRef<Struct>()
        ?: value.downcastRef<FrozenStruct>()?.let { coerceStruct(it) }
}
