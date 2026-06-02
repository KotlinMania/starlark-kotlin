// port-lint: tests tests/derive/attrs.rs
package io.github.kotlinmania.starlark_kotlin.tests.derive

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

import io.github.kotlinmania.starlark.assert.Assert
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.typing.TyStarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.AllocFrozenValue
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.layout.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.simple.allocSimple
import io.github.kotlinmania.starlark_kotlin.values.types.bigint.allocValue

// #[test]
// fn test_derive_attrs()
internal fun testDeriveAttrs() {
    // #[derive(Debug, StarlarkAttrs, Display, ProvidesStaticType, NoSerialize, Allocative)]
    // struct Nested { foo: String }
    class Nested(val foo: String) : StarlarkValue, AllocFrozenValue {
        // #[starlark_value(type = "nested")]
        override val TYPE: String get() = "nested"
        override fun toString(): String = foo

        override fun getAttr(attribute: String, heap: Heap): Value? = when (attribute) {
            "foo" -> heap.allocStr(foo)
            else -> null
        }

        override fun dirAttr(): List<String> = listOf("foo")

        override fun starlarkTypeRepr(): Ty = Ty.starlarkValue(TyStarlarkValue.new(TYPE))
        override fun allocFrozenValue(heap: FrozenHeap): FrozenValue = heap.allocSimple(this)
    }

    // #[derive(Debug, StarlarkAttrs, Display, ProvidesStaticType, NoSerialize, Allocative)]
    // struct Example { hello: String, answer: i64, nested: Nested, type: i64, escaped: String }
    class Example(
        val hello: String,
        @Suppress("unused") val answer: Long, // #[starlark(skip)]
        val nested: Nested, // #[starlark(clone)]
        val typeValue: Long, // r#type
        val escaped: String, // r#escaped
    ) : StarlarkValue, AllocFrozenValue {
        // #[starlark_value(type = "example")]
        override val TYPE: String get() = "example"
        override fun toString(): String = "Example(hello=$hello, answer=$answer, nested=$nested, typeValue=$typeValue, escaped=$escaped)"

        // starlark_attrs!()
        override fun getAttr(attribute: String, heap: Heap): Value? = when (attribute) {
            "hello" -> heap.allocStr(hello)
            "nested" -> heap.allocSimple(nested)
            "type" -> typeValue.allocValue(heap)
            "escaped" -> heap.allocStr(escaped)
            else -> null
        }

        override fun dirAttr(): List<String> = listOf("escaped", "hello", "nested", "type")

        override fun starlarkTypeRepr(): Ty = Ty.starlarkValue(TyStarlarkValue.new(TYPE))
        override fun allocFrozenValue(heap: FrozenHeap): FrozenValue = heap.allocSimple(this)
    }

    val a = Assert()
    a.globalsAdd { gb ->
        gb.set(
            "example",
            Example(
                hello = "world",
                answer = 42,
                nested = Nested(foo = "bar"),
                typeValue = 1,
                escaped = "baz",
            ),
        )
    }

    // dir
    a.eq(
        "dir(example)",
        """["escaped", "hello", "nested", "type"]""",
    )

    // getattr
    a.eq("example.hello", "\"world\"")
    a.eq("example.nested.foo", "\"bar\"")
    a.eq("example.type", "1")
    a.eq("example.escaped", "\"baz\"")

    // hasattr
    a.isTrue("not hasattr(example, \"answer\")")
    a.isTrue("hasattr(example, \"hello\")")
    a.isTrue("hasattr(example, \"nested\")")
    a.isTrue("hasattr(example, \"type\")")
    a.isTrue("not hasattr(example, \"r#type\")")
    a.isTrue("hasattr(example, \"escaped\")")
    a.isTrue("not hasattr(example, \"r#escaped\")")
}
