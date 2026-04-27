// port-lint: source src/values/types/structs/value.rs
package io.github.kotlinmania.starlark.values.types.structs

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

import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.typing.TyStruct
import starlarkmap.Hashed
import starlarkmap.StarlarkHasher
import starlarkmap.smallmap.SmallMap
import io.github.kotlinmania.starlark.values.ValueError
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.docs.DocProperty
import io.github.kotlinmania.starlark.docs.DocMember
import io.github.kotlinmania.starlark.docs.DocItem
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.compareSmallMap
import io.github.kotlinmania.starlark.values.equalsSmallMap

/**
 * The result of calling `struct()`.
 *
 * Generic over `V`, which is either `Value` or `FrozenValue`.
 */
data class StructGen<V>(
    /** The fields in a struct. */
    val fields: SmallMap<String, V>
) : io.github.kotlinmania.starlark.values.StarlarkValue {
    override val TYPE: String get() = Companion.TYPE

    companion object {
        /** The result of calling `type()` on a struct. */
        const val TYPE: String = "struct"
    }

    /** Create a new [Struct]. */
    fun new(fields: SmallMap<String, V>): StructGen<V> {
        return StructGen(fields)
    }

    /** Iterate over the elements in the struct. */
    fun iter(): Sequence<Pair<String, V>> {
        return fields.iter()
    }

    private fun selfTy(): Ty {
        return Ty.custom(TyStruct(
            fields = fields.iter().associate { (k, v) ->
                val asValue = when (v) {
                    is Value -> v
                    is FrozenValue -> v.toValue()
                    else -> error("StructGen V must be Value or FrozenValue, got: ${v!!::class}")
                }
                k to Ty.ofValue(asValue)
            },
            extra = false
        ))
    }

    override fun toString(): String {
        return buildString {
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
    }

    override fun collectReprCycle(collector: StringBuilder) {
        collector.append("struct(...)")
    }

    override fun equals(other: Value): Result<Boolean> {
        val otherStruct = StructGen.fromValue(other) ?: return Result.success(false)
        @Suppress("UNCHECKED_CAST")
        val thisFields = fields as SmallMap<String, Value>
        return equalsSmallMap<Exception, String, Value, Value>(
            thisFields,
            otherStruct.fields
        ) { x, y -> x.equals(y) }
    }

    override fun compare(other: Value): Result<Int> {
        val otherStruct = StructGen.fromValue(other)
            ?: return ValueError.unsupportedWith(TYPE, "cmp()", other)
        @Suppress("UNCHECKED_CAST")
        val thisFields = fields as SmallMap<String, Value>
        return compareSmallMap<Exception, String, String, Value, Value>(
            thisFields,
            otherStruct.fields,
            key = { k: String -> k }
        ) { x, y -> x.compare(y) }
    }

    override fun getAttr(attribute: String, heap: Heap): Value? {
        return getAttrHashed(Hashed.new(attribute), heap)
    }

    override fun getAttrHashed(attribute: Hashed<String>, heap: Heap): Value? {
        @Suppress("UNCHECKED_CAST")
        val valueFields = fields as SmallMap<String, Value>
        return valueFields.getHashedByValue(attribute)
    }

    override fun writeHash(hasher: StarlarkHasher): Result<Unit> {
        // Must import unordered hash because equality is unordered,
        // and `a = b  =>  hash(a) = hash(b)`.
        val unorderedHasher = UnorderedHasher.new()

        for ((hashedKey, v) in fields.iterHashed()) {
            // Should hash key and value together, so two structs
            // `a=1 b=2` and `a=2 b=1` would produce different hashes.
            val entryHasher = StarlarkHasher()
            // Hash the key's hash value into the entry hasher
            entryHasher.writeU32(hashedKey.hash().get())
            // Hash the value
            val value = v as? Value ?: (v as? FrozenValue)?.toValue()
                ?: return Result.failure(IllegalStateException("Unsupported value type in struct"))
            value.writeHash(entryHasher).getOrElse { return Result.failure(it) }
            unorderedHasher.writeHash(entryHasher.finish())
        }

        hasher.writeU64(unorderedHasher.finish())

        return Result.success(Unit)
    }

    override fun dirAttr(): List<String> {
        return fields.keys().toList()
    }

    override fun documentation(): DocItem {
        // This treats structs as being value-like, and intentionally generates bad docs in the case
        // of namespace-like usage. See
        // <https://fb.workplace.com/groups/starlark/permalink/1463680027654154/> for some
        // additional discussion
        val typ = selfTy()
        return DocItem.Member(DocMember.Property(DocProperty(docs = null, typ = typ)))
    }

    override fun getTypeStarlarkRepr(): Ty {
        return Ty.anyStruct()
    }

    override fun typecheckerTy(): Ty? {
        return selfTy()
    }

    /** Serialize the struct's fields to a map. */
    fun serialize(): Map<String, V> {
        return iter().associate { (k, v) -> k to v }
    }
}

/**
 * Extension for StructGen<FrozenValue> to iterate with frozen types.
 */
fun StructGen<FrozenValue>.iterFrozen(): Sequence<Pair<String, FrozenValue>> {
    return fields.iter()
}

@Suppress("UNCHECKED_CAST")
fun coerceStruct(frozen: StructGen<FrozenValue>): StructGen<Value> {
    return frozen as StructGen<Value>
}

/**
 * Helper function to extract struct from a value.
 */
fun StructGen.Companion.fromValue(value: Value): StructGen<Value>? {
    // Try to get as unfrozen struct first, then try frozen and coerce
    return value.downcastRef<StructGen<Value>>()
        ?: value.downcastRef<StructGen<FrozenValue>>()?.let { coerceStruct(it) }
}
