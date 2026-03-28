// port-lint: source src/tests/derive/docs.rs
package io.github.kotlinmania.starlark_kotlin.tests.derive

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

import io.github.kotlinmania.starlark_kotlin.docs.DocMember
import io.github.kotlinmania.starlark_kotlin.docs.DocString
import io.github.kotlinmania.starlark_kotlin.docs.DocStringKind
import io.github.kotlinmania.starlark_kotlin.docs.DocType
import io.github.kotlinmania.starlark_kotlin.docs.fromDocstring
import io.github.kotlinmania.starlark_kotlin.environment.Methods
import io.github.kotlinmania.starlark_kotlin.environment.MethodsBuilder
import io.github.kotlinmania.starlark_kotlin.environment.MethodsStatic
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap

/** Main module docs */
// #[starlark_module]
// fn object_docs_1(_: &mut MethodsBuilder)
private fun objectDocs1(builder: MethodsBuilder) {
    builder.setDocstring("Main module docs")
    /** Returns the string "foo" */
    // #[starlark(attribute)]
    // fn foo(this: &TestExample) -> Result<String>
    builder.setAttribute("foo", "Returns the string \"foo\"") { _: Value, heap: Heap ->
        Result.success(heap.allocStr("foo"))
    }
}

// #[derive(Debug, Display, ProvidesStaticType, NoSerialize, Allocative)]
// struct TestExample {}
private class TestExample : StarlarkValue {
    // #[starlark_value(type = "TestExample")]
    override val TYPE: String get() = "TestExample"
    override fun toString(): String = "TestExample"

    // fn get_methods() -> Option<&'static Methods>
    companion object {
        private val METHODS = MethodsStatic()
        fun getMethods(): Methods = METHODS.methods(::objectDocs1)
    }
}

// #[derive(Clone, Debug, Coerce, Display, Trace, Freeze, ProvidesStaticType, Allocative)]
// struct ComplexTestExampleGen<V>(V)
private class ComplexTestExampleGen<V>(val value: V) : StarlarkValue {
    // #[starlark_value(type = "ComplexTestExample")]
    override val TYPE: String get() = "ComplexTestExample"
    override fun toString(): String = value.toString()

    companion object {
        private val METHODS = MethodsStatic()
        fun getMethods(): Methods = METHODS.methods(::objectDocs1)
    }
}

// type alias for frozen variant
private typealias FrozenComplexTestExample = ComplexTestExampleGen<Any>

// #[test]
// fn test_derive_docs()
internal fun testDeriveDocs() {
    val obj = DocType.fromStarlarkValue(TestExample())

    check(
        DocString.fromDocstring(DocStringKind.Rust, "Main module docs") == obj.docs
    )
    check(
        DocString.fromDocstring(DocStringKind.Rust, "Returns the string \"foo\"") ==
            obj.members.iter().firstNotNullOfOrNull { (name, m) ->
                when {
                    m is DocMember.Property && name == "foo" -> m.property.docs
                    else -> null
                }
            }
    )
}

// #[test]
// fn test_derive_docs_on_complex_values()
internal fun testDeriveDocsOnComplexValues() {
    val complexObj = DocType.fromStarlarkValue(ComplexTestExampleGen<Any>(Unit))

    check(
        DocString.fromDocstring(DocStringKind.Rust, "Main module docs") == complexObj.docs
    )
    check(
        DocString.fromDocstring(DocStringKind.Rust, "Returns the string \"foo\"") ==
            complexObj.members.iter().firstNotNullOfOrNull { (name, m) ->
                when {
                    m is DocMember.Property && name == "foo" -> m.property.docs
                    else -> null
                }
            }
    )
}
