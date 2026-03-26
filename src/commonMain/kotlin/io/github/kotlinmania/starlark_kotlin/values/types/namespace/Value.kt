// port-lint: source src/values/types/namespace/value.rs
package io.github.kotlinmania.starlark_kotlin.values.types.namespace

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

import io.github.kotlinmania.starlark_kotlin.assert.Assert
import io.github.kotlinmania.starlark_kotlin.collections.Hashed
import io.github.kotlinmania.starlark_kotlin.collections.SmallMap
import io.github.kotlinmania.starlark_kotlin.display.fmtKeyedContainer
import io.github.kotlinmania.starlark_kotlin.docs.DocItem
import io.github.kotlinmania.starlark_kotlin.docs.DocModule
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.util.ArcStr
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.toValue
import io.github.kotlinmania.starlark_kotlin.typing.fill_types_for_lint.ofValue

// #[derive(Clone, Coerce, Debug, Trace, Freeze, Allocative)]
data class MaybeDocHiddenValue<V>(
    val value: V,
    val docHidden: Boolean,
)

/** The return value of `namespace()` */
// #[derive(Clone, Debug, Trace, Freeze, ProvidesStaticType, Allocative)]
data class NamespaceGen<V>(
    val fields: SmallMap<String, MaybeDocHiddenValue<V>>
) : StarlarkValue {

    override val TYPE: String get() = "namespace"

    companion object {
        fun <V> new(fields: SmallMap<String, MaybeDocHiddenValue<V>>): NamespaceGen<V> =
            NamespaceGen(fields)
    }

    fun get(key: String): V? =
        fields.getHashedByValue(Hashed.new(key))?.value

    // unsafe impl Coerce<NamespaceGen<Value>> for NamespaceGen<FrozenValue>

    // starlark_complex_value!(pub Namespace)

    // impl Display for NamespaceGen
    override fun toString(): String =
        fmtKeyedContainer(
            "namespace(",
            ")",
            "=",
            fields.iter().map { (k, v) -> k to v.value },
        )

    // #[starlark_value(type = "namespace")]
    // impl StarlarkValue for NamespaceGen

    override fun collectReprCycle(collector: StringBuilder) {
        collector.append("namespace(...)")
    }

    override fun getAttr(attribute: String, heap: Heap): Value? =
        getAttrHashed(Hashed.new(attribute), heap)

    override fun getAttrHashed(attribute: Hashed<String>, heap: Heap): Value? =
        fields.getHashedByValue(attribute)?.value?.toValue()

    override fun dirAttr(): List<String> =
        fields.keys().map { x -> x }.toList()

    override fun documentation(): DocItem =
        DocItem.Module(DocModule(
            docs = null,
            members = fields.iter()
                .filter { (_, v) -> !v.docHidden }
                .associate { (k, v) -> k to v.value.toValue().documentation() },
        ))

    override fun getTypeStarlarkRepr(): Ty =
        Ty.custom(TyNamespace(
            fields = sortedMapOf(),
            extra = true,
        ))

    override fun typecheckerTy(): Ty? =
        Ty.custom(TyNamespace(
            fields = fields.iter()
                .associate { (name, value) ->
                    ArcStr.from(name) to Ty.ofValue(value.value.toValue())
                }
                .toSortedMap(),
            extra = false,
        ))

    // impl Serialize for NamespaceGen
    fun serialize(): Map<String, V> =
        fields.iter().associate { (k, v) -> k to v.value }
}

// unsafe impl Coerce<NamespaceGen<Value>> for NamespaceGen<FrozenValue>
@Suppress("UNCHECKED_CAST")
fun coerceNamespace(frozen: NamespaceGen<FrozenValue>): NamespaceGen<Value> =
    frozen as NamespaceGen<Value>

// starlark_complex_value!(pub Namespace)
typealias FrozenNamespace = NamespaceGen<FrozenValue>
typealias Namespace = NamespaceGen<Value>

// #[cfg(test)]
// mod tests

internal fun testRepr() {
    Assert.eq("repr(namespace(a=1, b=[]))", "'namespace(a=1, b=[])'")
    Assert.eq("str(namespace(a=1, b=[]))", "'namespace(a=1, b=[])'")
}

internal fun testReprCycle() {
    Assert.eq(
        "l = []; s = namespace(f=l); l.append(s); repr(s)",
        "'namespace(f=[namespace(...)])'",
    )
    Assert.eq(
        "l = []; s = namespace(f=l); l.append(s); str(s)",
        "'namespace(f=[namespace(...)])'",
    )
}

internal fun testToJsonCycle() {
    Assert.fail(
        "l = []; s = namespace(f=l); l.append(s); json.encode(s)",
        "Cycle detected when serializing value of type `namespace` to JSON",
    )
}

internal fun testKwargs() {
    Assert.eq(
        "d = {'b': 2}; s = namespace(a=1, **d); str(s)",
        "'namespace(a=1, b=2)'",
    )
}
