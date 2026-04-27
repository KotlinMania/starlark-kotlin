// port-lint: source src/values/types/record/instance.rs
package io.github.kotlinmania.starlark.values.types.record

/*
 * Copyright 2019 The Starlark in Rust Authors.
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

/** An actual record instance. */

import starlarkmap.smallmap.SmallMap
import starlarkmap.StarlarkHasher
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.ComplexValue
import io.github.kotlinmania.starlark.values.Freeze
import io.github.kotlinmania.starlark.values.layout.Freezer
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.freeze
import io.github.kotlinmania.starlark.values.types.record.recordtype.RecordTypeGen
import starlarkmap.Hashed
import io.github.kotlinmania.starlark.values.types.TypeInstanceId
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.types.record.recordtype.recordFields

/** Helper: format keyed container like "record[Name](a=1, b=2)". */
private fun <K, V> fmtKeyedContainer(
    start: String,
    end: String,
    sep: String,
    iter: Sequence<Pair<K, V>>,
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

/** Helper: compare two lists element-wise. */
private fun equalsSlice(
    a: List<Value>,
    b: List<Value>,
    eq: (Value, Value) -> Result<Boolean>,
): Result<Boolean> {
    if (a.size != b.size) return Result.success(false)
    for (i in a.indices) {
        val result = eq(a[i], b[i])
        val isEqual = result.getOrElse { return Result.failure(it) }
        if (!isEqual) return Result.success(false)
    }
    return Result.success(true)
}

/** An actual record. */
class RecordGen internal constructor(
    internal val typ: Value, // Must be RecordType
    internal val values: List<Value>,
) : ComplexValue, Freeze<RecordGen> {

    companion object {
        /** `type(x)` for records. */
        const val TYPE: String = "record"

        /** Attempt to extract a Record from a Value. */
        fun fromValue(value: Value): RecordGen? {
            return value.downcastRef()
        }
    }

    override fun toString(): String {
        val name = recordTypeName() ?: "anon"
        return fmtKeyedContainer("record[$name](", ")", "=", iter())
    }

    override fun freeze(freezer: Freezer): Result<RecordGen> {
        val frozenTyp = typ.freeze(freezer).getOrElse { return Result.failure(it) }
        val frozenValues: List<FrozenValue> = values.freeze<Value, FrozenValue>(freezer) { v: Value -> v.freeze(freezer) }
            .getOrElse { return Result.failure(it) }
        return Result.success(
            RecordGen(
                typ = frozenTyp.toValue(),
                values = frozenValues.map { v -> v.toValue() },
            )
        )
    }

    private fun getRecordType(): RecordTypeGen {
        // Safe to unwrap because we always ensure typ is RecordType
        return typ.downcastRef<RecordTypeGen>()!!
    }

    private fun recordTypeName(): String? {
        return getRecordType().tyRecordData()?.name
    }

    internal fun recordTypeId(): TypeInstanceId {
        return getRecordType().id
    }

    private fun getRecordFields(): SmallMap<String, Field> {
        @Suppress("UNCHECKED_CAST")
        return recordFields(getRecordType())
    }

    /** Iterate over the elements in the record. */
    fun iter(): Sequence<Pair<String, Value>> {
        return getRecordFields().keys()
            .zip(values.asSequence())
    }

    override fun equals(other: Value): Result<Boolean> {
        val otherRecord = fromValue(other) ?: return Result.success(false)
        val typEquals = typ.equals(otherRecord.typ).getOrElse { return Result.failure(it) }
        if (!typEquals) return Result.success(false)
        return equalsSlice(values, otherRecord.values) { x, y -> x.equals(y) }
    }

    override fun getAttr(attribute: String, heap: Heap): Value? {
        return getAttrHashed(Hashed.new(attribute), heap)
    }

    override fun getAttrHashed(attribute: Hashed<String>, heap: Heap): Value? {
        val fields = getRecordFields()
        val i = fields.getIndexOf(attribute.key()) ?: return null
        return values[i]
    }

    override fun writeHash(hasher: StarlarkHasher): Result<Unit> {
        typ.writeHash(hasher).getOrElse { return Result.failure(it) }
        for (v in values) {
            v.writeHash(hasher).getOrElse { return Result.failure(it) }
        }
        return Result.success(Unit)
    }

    override fun dirAttr(): List<String> {
        return getRecordFields().keys().toList()
    }

    override fun typecheckerTy(): Ty? {
        return getRecordType().instanceTy()
    }

    fun serialize(): Map<String, Value> {
        return iter().toMap()
    }
}

// Frozen and unfrozen records share `RecordGen`; the inner value type distinguishes them.
