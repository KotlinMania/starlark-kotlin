@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)
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
import io.github.kotlinmania.starlark.values.ComplexValue
import io.github.kotlinmania.starlark.values.Freeze
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.Trace
import io.github.kotlinmania.starlark.values.freezeSmallMap
import io.github.kotlinmania.starlark.values.layout.Freezer
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.layout.heap.Tracer
import io.github.kotlinmania.starlark.values.layout.heap.ValueHolder
import kotlin.native.HiddenFromObjC

internal fun interface NamespaceTraceValue<V> {
    fun trace(
        value: V,
        tracer: Tracer,
    ): V
}

internal fun interface NamespaceFreezeValue<V> {
    fun freeze(
        value: V,
        freezer: Freezer,
    ): Result<FrozenValue>
}

data class MaybeDocHiddenValue<V>(
    var value: V,
    val docHidden: Boolean,
)

/** The return value of `namespace()` */
internal class NamespaceGen<V> internal constructor(
    val fields: SmallMap<String, MaybeDocHiddenValue<V>>,
    private val traceValue: NamespaceTraceValue<V>? = null,
    private val freezeValue: NamespaceFreezeValue<V>? = null,
) : StarlarkValue,
    ComplexValue,
    Trace {
    override val TYPE: String get() = "namespace"

    companion object {
        fun mutable(fields: SmallMap<String, MaybeDocHiddenValue<Value>>): Namespace =
            Namespace(NamespaceGen(
                fields = fields,
                traceValue =
                    NamespaceTraceValue { value, tracer ->
                        val holder = ValueHolder(value)
                        tracer.trace(holder)
                        holder.value
                    },
                freezeValue =
                    NamespaceFreezeValue { value, freezer ->
                        freezer.freeze(value)
                    },
            ))

        fun frozen(fields: SmallMap<String, MaybeDocHiddenValue<FrozenValue>>): FrozenNamespace =
            FrozenNamespace(NamespaceGen(
                fields = fields,
                freezeValue =
                    NamespaceFreezeValue { value, _ ->
                        Result.success(value)
                    },
            ))

        fun fromValue(value: Value): NamespaceGen<*>? =
            value.downcastRef<Namespace>()?.delegate
                ?: value.downcastRef<FrozenNamespace>()?.delegate
    }

    override fun trace(tracer: Tracer) {
        val traceValue = traceValue ?: return
        for ((_, field) in fields) {
            field.value = traceValue.trace(field.value, tracer)
        }
    }

    fun freeze(freezer: Freezer): Result<NamespaceGen<FrozenValue>> {
        val freezeValue =
            freezeValue
                ?: return Result.failure(IllegalStateException("Namespace fields cannot be frozen"))
        val frozenFields =
            freezeSmallMap(
                fields,
                freezer,
                freezeKey = { key, _ -> Result.success(key) },
                freezeValue = { field, currentFreezer ->
                    freezeValue
                        .freeze(field.value, currentFreezer)
                        .map { MaybeDocHiddenValue(it, field.docHidden) }
                },
            ).getOrElse { return Result.failure(it) }
        return Result.success(frozen(frozenFields).delegate)
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

    @HiddenFromObjC
    fun serialize(): Map<String, V> =
        fields.iter().associate { (k, v) -> k to v.value }
}

class Namespace internal constructor(
    internal val delegate: NamespaceGen<Value>,
) : StarlarkValue by delegate,
    ComplexValue,
    Trace,
    Freeze<FrozenNamespace> {
    val fields: SmallMap<String, MaybeDocHiddenValue<Value>> get() = delegate.fields

    override fun toString(): String = delegate.toString()

    override fun trace(tracer: Tracer) {
        delegate.trace(tracer)
    }

    override fun freeze(freezer: Freezer): Result<FrozenNamespace> {
        val frozenGen = delegate.freeze(freezer).getOrElse { return Result.failure(it) }
        return Result.success(FrozenNamespace(frozenGen))
    }

    companion object {
        fun fromValue(value: Value): Namespace? {
            return value.downcastRef<Namespace>()
        }
    }
}

class FrozenNamespace internal constructor(
    internal val delegate: NamespaceGen<FrozenValue>,
) : StarlarkValue by delegate,
    ComplexValue,
    Trace {
    val fields: SmallMap<String, MaybeDocHiddenValue<FrozenValue>> get() = delegate.fields

    override fun toString(): String = delegate.toString()

    override fun trace(tracer: Tracer) {
        delegate.trace(tracer)
    }

    companion object {
        fun fromValue(value: Value): FrozenNamespace? {
            return value.downcastRef<FrozenNamespace>()
        }
    }
}

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
