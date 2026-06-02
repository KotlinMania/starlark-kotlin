// port-lint: source src/values/types/structs/value.rs
@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)

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
import io.github.kotlinmania.starlark.values.ValueError
import io.github.kotlinmania.starlark.values.compareSmallMap
import io.github.kotlinmania.starlark.values.equalsSmallMap
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import kotlin.native.HiddenFromObjC

/**
 * The result of calling `struct()`.
 *
 * This is a generic struct implementation parametrized over V which represents
 * either Value or FrozenValue in the Rust implementation. The lifetime parameter
 * 'v from Rust is handled through Kotlin's type system.
 */
data class StructGen<V>(
    /** The fields in a struct. */
    val fields: SmallMap<String, V>,
) : io.github.kotlinmania.starlark.values.StarlarkValue {
    override val TYPE: String get() = Companion.TYPE
    override val HAS_equals: Boolean get() = true

    companion object {
        /** The result of calling `type()` on a struct. */
        const val TYPE: String = "struct"
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

        @Suppress("UNCHECKED_CAST")
        val thisFields = fields as SmallMap<String, Value>
        return equalsSmallMap<Exception, String, Value, Value>(
            thisFields,
            otherStruct.fields,
        ) { x, y -> x.equals(y) }
    }

    override fun compare(other: Value): Result<Int> {
        val otherStruct =
            Struct.fromValue(other)
                ?: return ValueError.unsupportedWith(TYPE, "cmp()", other)

        @Suppress("UNCHECKED_CAST")
        val thisFields = fields as SmallMap<String, Value>
        return compareSmallMap<Exception, String, String, Value, Value>(
            thisFields,
            otherStruct.fields,
            key = { k: String -> k },
        ) { x, y -> x.compare(y) }
    }

    override fun getAttr(attribute: String, heap: Heap): Value? = getAttrHashed(Hashed.new(attribute), heap)

    override fun getAttrHashed(attribute: Hashed<String>, heap: Heap): Value? {
        @Suppress("UNCHECKED_CAST")
        val valueFields = fields as SmallMap<String, Value>
        return valueFields.getHashedByValue(attribute)
    }

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
            @Suppress("UNCHECKED_CAST")
            val value =
                v as? Value ?: (v as? FrozenValue)?.toValue()
                    ?: return Result.failure(IllegalStateException("Unsupported value type in struct"))
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
    @HiddenFromObjC
    fun serialize(): Map<String, V> = iter().associate { (k, v) -> k to v }
}

/**
 * Extension for StructGen<FrozenValue> to iterate with frozen types.
 */
fun StructGen<FrozenValue>.iterFrozen(): Sequence<Pair<String, FrozenValue>> = fields.iter()

/**
 * Unsafe coercion for frozen structs - corresponds to Rust's unsafe impl for Coerce.
 */
@Suppress("UNCHECKED_CAST")
fun coerceStruct(frozen: StructGen<FrozenValue>): StructGen<Value> = frozen as StructGen<Value>

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
fun StructGen.Companion.fromValue(value: Value): Struct? {
    // Try to get as unfrozen struct first, then try frozen and coerce
    return value.downcastRef<Struct>()
        ?: value.downcastRef<FrozenStruct>()?.let { coerceStruct(it) }
}
