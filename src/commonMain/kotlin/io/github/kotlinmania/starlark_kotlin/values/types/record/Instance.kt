// port-lint: source src/values/types/record/instance.rs
package io.github.kotlinmania.starlark_kotlin.values.types.record

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

/// An actual record instance.

import io.github.kotlinmania.starlark_kotlin.collections.SmallMap
import io.github.kotlinmania.starlark_kotlin.collections.StarlarkHasher
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.values.Freeze
import io.github.kotlinmania.starlark_kotlin.values.Freezer
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.types.record.record_type.FrozenRecordType
import io.github.kotlinmania.starlark_kotlin.values.types.record.record_type.RecordType
import io.github.kotlinmania.starlark_kotlin.values.types.record.record_type.RecordTypeGen
import starlark_map.Hashed
import io.github.kotlinmania.starlark_kotlin.values.types.TypeInstanceId
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.tests.freeze
import io.github.kotlinmania.starlark_kotlin.values.writeHash
import io.github.kotlinmania.starlark_kotlin.values.types.record.record_type.recordFields
import io.github.kotlinmania.starlark_kotlin.values.layout.getStarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.key

/// Helper: format keyed container like "record[Name](a=1, b=2)".
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

/// Helper: compare two lists element-wise.
// crate::values::comparison::equals_slice
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

/// An actual record.
// #[derive(Clone, Debug, Trace, Coerce, Freeze, ProvidesStaticType, Allocative)]
// #[repr(C)]
// pub struct RecordGen<V: ValueLifetimeless> {
//     pub(crate) typ: V, // Must be RecordType
//     pub(crate) values: Box<[V]>,
// }
// starlark_complex_value!(pub Record);
class RecordGen internal constructor(
    internal val typ: Value, // Must be RecordType
    internal val values: List<Value>,
) : StarlarkValue, Freeze {

    companion object {
        /// `type(x)` for records.
        // pub const TYPE: &'static str = "record";
        const val TYPE: String = "record"

        /// Attempt to extract a Record from a Value.
        // From starlark_complex_value!(pub Record) macro expansion.
        fun fromValue(value: Value): RecordGen? {
            val sv = value.getStarlarkValue()
            return sv as? RecordGen
        }
    }

    // impl Display for RecordGen
    override fun toString(): String {
        val name = recordTypeName() ?: "anon"
        return fmtKeyedContainer("record[$name](", ")", "=", iter())
    }

    // impl Freeze for RecordGen
    // fn freeze(self, freezer: &Freezer) -> FreezeResult<Self::Frozen>
    override fun freeze(freezer: Freezer): RecordGen {
        return RecordGen(
            typ = typ.freeze(freezer),
            values = values.map { it.freeze(freezer) },
        )
    }

    // impl RecordGen

    // fn get_record_type(&self) -> Either<&'v RecordType<'v>, &'v FrozenRecordType>
    private fun getRecordType(): RecordTypeGen {
        // Safe to unwrap because we always ensure typ is RecordType
        val sv = typ.getStarlarkValue()
        return sv as RecordTypeGen
    }

    // fn record_type_name(&self) -> Option<&'v str>
    private fun recordTypeName(): String? {
        return getRecordType().tyRecordData()?.name
    }

    // pub(crate) fn record_type_id(&self) -> TypeInstanceId
    internal fun recordTypeId(): TypeInstanceId {
        return getRecordType().id
    }

    // fn get_record_fields(&self) -> &'v SmallMap<String, FieldGen<Value<'v>>>
    private fun getRecordFields(): SmallMap<String, Field> {
        @Suppress("UNCHECKED_CAST")
        return recordFields(getRecordType()) as SmallMap<String, Field>
    }

    /// Iterate over the elements in the record.
    // pub fn iter<'a>(&'a self) -> impl ExactSizeIterator<Item = (&'v str, V)> + 'a
    fun iter(): Sequence<Pair<String, Value>> {
        return getRecordFields().keys()
            .zip(values)
            .asSequence()
    }

    // #[starlark_value(type = Record::TYPE)]
    // impl StarlarkValue for RecordGen

    // fn equals(&self, other: Value<'v>) -> crate::Result<bool>
    fun equals(other: Value): Result<Boolean> {
        val otherRecord = fromValue(other) ?: return Result.success(false)
        val typEquals = typ.equals(otherRecord.typ).getOrElse { return Result.failure(it) }
        if (!typEquals) return Result.success(false)
        return equalsSlice(values, otherRecord.values) { x, y -> x.equals(y) }
    }

    // fn get_attr(&self, attribute: &str, heap: Heap<'v>) -> Option<Value<'v>>
    fun getAttr(attribute: String, heap: Heap): Value? {
        return getAttrHashed(Hashed.new(attribute), heap)
    }

    // fn get_attr_hashed(&self, attribute: Hashed<&str>, _heap: Heap<'v>) -> Option<Value<'v>>
    fun getAttrHashed(attribute: Hashed<String>, heap: Heap): Value? {
        val fields = getRecordFields()
        val i = fields.getIndexOf(attribute.key) ?: return null
        return values[i]
    }

    // fn write_hash(&self, hasher: &mut StarlarkHasher) -> crate::Result<()>
    fun writeHash(hasher: StarlarkHasher): Result<Unit> {
        typ.writeHash(hasher).getOrElse { return Result.failure(it) }
        for (v in values) {
            v.writeHash(hasher).getOrElse { return Result.failure(it) }
        }
        return Result.success(Unit)
    }

    // fn dir_attr(&self) -> Vec<String>
    fun dirAttr(): List<String> {
        return getRecordFields().keys().toList()
    }

    // fn typechecker_ty(&self) -> Option<Ty>
    fun typecheckerTy(): Ty? {
        return getRecordType().instanceTy()
    }

    // impl Serialize for RecordGen
    // fn serialize<S>(&self, serializer: S) -> Result<S::Ok, S::Error>
    fun serialize(): Map<String, Value> {
        return iter().toMap()
    }
}

/// Type alias for unfrozen record.
// pub type Record<'v> = RecordGen<Value<'v>>;
typealias Record = RecordGen
/// Type alias for frozen record.
// pub type FrozenRecord = RecordGen<FrozenValue>;
typealias FrozenRecord = RecordGen
