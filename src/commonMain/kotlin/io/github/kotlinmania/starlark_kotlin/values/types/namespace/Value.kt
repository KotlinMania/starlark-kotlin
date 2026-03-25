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

import kotlinx.serialization.Serializable
import io.github.kotlinmania.starlark_kotlin.docs.DocItem
import io.github.kotlinmania.starlark_kotlin.docs.DocModule
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.util.ArcStr
import io.github.kotlinmania.starlark_kotlin.docs.DocProperty
import io.github.kotlinmania.starlark_kotlin.docs.DocMember
import io.github.kotlinmania.starlark_kotlin.typing.fill_types_for_lint.ofValue

/**
 * Internal helper to wrap a value with doc visibility metadata.
 */
data class MaybeDocHiddenValue<V>(
    val value: V,
    val docHidden: Boolean
)

/**
 * The return value of `namespace()`
 */
@Serializable
data class NamespaceGen<V>(
    private val fields: Map<String, MaybeDocHiddenValue<V>>
) {
    companion object {
        fun <V> new(fields: Map<String, MaybeDocHiddenValue<V>>): NamespaceGen<V> {
            return NamespaceGen(fields)
        }
    }

    fun get(key: String): V? {
        return fields[key]?.value
    }

    /**
     * Returns the string representation of this namespace.
     */
    override fun toString(): String {
        val items = fields.entries.map { (k, v) ->
            "$k=${v.value}"
        }
        return "namespace(${items.joinToString(", ")})"
    }

    /**
     * Collect representation for cyclic references.
     */
    fun collectReprCycle(collector: StringBuilder) {
        collector.append("namespace(...)")
    }

    /**
     * Get attribute value by name.
     * Returns null if the attribute doesn't exist.
     */
    fun getAttr(attribute: String): V? {
        return fields[attribute]?.value
    }

    /**
     * Get attribute value by hashed name.
     * Returns null if the attribute doesn't exist.
     */
    fun getAttrHashed(attribute: String): V? {
        return fields[attribute]?.value
    }

    /**
     * Return a list of attribute names (directory).
     */
    fun dirAttr(): List<String> {
        return fields.keys.toList()
    }

    /**
     * Generate documentation for this namespace.
     */
    fun documentation(): DocItem {
        val members = fields.entries
            .filter { (_, v) -> !v.docHidden }
            .associate { (k, v) ->
                k to DocItem.Member(DocMember.Property(DocProperty(
                    docs = null,
                    typ = Ty.ofValue(v.value)
                )))
            }

        return DocItem.Module(DocModule(
            docs = null,
            members = members
        ))
    }

    /**
     * Get the type representation for this namespace type (static).
     */
    fun getTypeStarlarkRepr(): Ty {
        return Ty.custom(TyNamespace(
            fields = emptyMap(),
            extra = true
        ))
    }

    /**
     * Get the runtime type representation for this specific instance.
     */
    fun typecheckerTy(): Ty? {
        val typeFields = fields.entries.associate { (name, value) ->
            ArcStr.from(name) to Ty.ofValue(value.value)
        }
        return Ty.custom(TyNamespace(
            fields = typeFields,
            extra = false
        ))
    }
}

/**
 * Alias for the frozen (immutable) namespace type.
 */
typealias FrozenNamespace = NamespaceGen<FrozenValue>

/**
 * Alias for the mutable namespace type.
 */
typealias Namespace<V> = NamespaceGen<V>

/**
 * Serialize implementation for NamespaceGen.
 * Serializes as a map of field names to values, excluding doc_hidden metadata.
 */
fun <V> NamespaceGen<V>.serialize(): Map<String, V> {
    return fields.mapValues { (_, v) -> v.value }
}

// Note: The starlark_complex_value! macro from Rust would generate type aliases
// and coercion implementations. In Kotlin, we handle this through type aliases
// and explicit coercion functions where needed.

// Test functions (ported from Rust tests)
// These would typically go in a separate test file, but included here for reference

/**
 * Test representation of namespace
 */
internal fun testRepr() {
    // assert::eq("repr(namespace(a=1, b=[]))", "'namespace(a=1, b=[])'");
    // assert::eq("str(namespace(a=1, b=[]))", "'namespace(a=1, b=[])'");
}

/**
 * Test representation with cycles
 */
internal fun testReprCycle() {
    // assert::eq(
    //     "l = []; s = namespace(f=l); l.append(s); repr(s)",
    //     "'namespace(f=[namespace(...)])'",
    // );
    // assert::eq(
    //     "l = []; s = namespace(f=l); l.append(s); str(s)",
    //     "'namespace(f=[namespace(...)])'",
    // );
}

/**
 * Test JSON encoding with cycles (should fail)
 */
internal fun testToJsonCycle() {
    // assert::fail(
    //     "l = []; s = namespace(f=l); l.append(s); json.encode(s)",
    //     "Cycle detected when serializing value of type `namespace` to JSON",
    // );
}

/**
 * Test kwargs in namespace creation
 */
internal fun testKwargs() {
    // assert::eq(
    //     "d = {'b': 2}; s = namespace(a=1, **d); str(s)",
    //     "'namespace(a=1, b=2)'",
    // );
}
