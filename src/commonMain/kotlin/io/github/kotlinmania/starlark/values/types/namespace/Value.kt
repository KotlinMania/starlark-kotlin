// port-lint: source src/values/types/namespace/value.rs
package io.github.kotlinmania.starlark.values.types.namespace

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
import io.github.kotlinmania.starlark.docs.DocItem
import io.github.kotlinmania.starlark.docs.DocModule
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.util.ArcStr
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.heap.Heap

data class MaybeDocHiddenValue<V>(
    val value: V,
    val docHidden: Boolean,
)

/** The return value of `namespace()` */
data class NamespaceGen<V>(
    val fields: SmallMap<String, MaybeDocHiddenValue<V>>,
) : StarlarkValue {
    override val TYPE: String get() = "namespace"

    companion object {
        fun <V> new(fields: SmallMap<String, MaybeDocHiddenValue<V>>): NamespaceGen<V> =
            NamespaceGen(fields)
    }

    fun get(key: String): V? =
        fields.getHashedByValue(Hashed.new(key))?.value

    override fun toString(): String =
        fmtKeyedContainer(
            "namespace(",
            ")",
            "=",
            fields.iter().map { (k, v) -> k to v.value },
        )

    override fun collectReprCycle(collector: StringBuilder) {
        collector.append("namespace(...)")
    }

    override fun getAttr(attribute: String, heap: Heap): Value? =
        getAttrHashed(Hashed.new(attribute), heap)

    @Suppress("UNCHECKED_CAST")
    override fun getAttrHashed(attribute: Hashed<String>, heap: Heap): Value? {
        val v = fields.getHashedByValue(attribute) ?: return null
        return when (val raw = v.value) {
            is Value -> raw
            is FrozenValue -> raw.toValue()
            else -> null
        }
    }

    override fun dirAttr(): List<String> =
        fields.keys().map { x -> x }.toList()

    @Suppress("UNCHECKED_CAST")
    override fun documentation(): DocItem {
        val members = SmallMap.new<String, DocItem>()
        for ((k, v) in fields.iter()) {
            if (!v.docHidden) {
                val value =
                    when (val raw = v.value) {
                        is Value -> raw
                        is FrozenValue -> raw.toValue()
                        else -> continue
                    }
                members.insert(k, value.documentation())
            }
        }
        return DocItem.Module(
            DocModule(
                members = members,
            ),
        )
    }

    override fun getTypeStarlarkRepr(): Ty =
        Ty.custom(
            TyNamespace(
                fields = emptyMap(),
                extra = true,
            ),
        )

    @Suppress("UNCHECKED_CAST")
    override fun typecheckerTy(): Ty {
        val result = mutableMapOf<ArcStr, Ty>()
        for ((name, mdv) in fields.iter()) {
            val value =
                when (val raw = mdv.value) {
                    is Value -> raw
                    is FrozenValue -> raw.toValue()
                    else -> continue
                }
            result[ArcStr.from(name)] = Ty.ofValue(value)
        }
        return Ty.custom(
            TyNamespace(
                fields = result,
                extra = false,
            ),
        )
    }

    fun serialize(): Map<String, V> =
        fields.iter().associate { (k, v) -> k to v.value }
}

@Suppress("UNCHECKED_CAST")
fun coerceNamespace(frozen: NamespaceGen<FrozenValue>): NamespaceGen<Value> =
    frozen as NamespaceGen<Value>

typealias FrozenNamespace = NamespaceGen<FrozenValue>
typealias Namespace = NamespaceGen<Value>

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
        if (!first) builder.append(", ")
        builder.append(k)
        builder.append(sep)
        builder.append(v)
        first = false
    }
    builder.append(end)
    return builder.toString()
}
