// port-lint: source src/tests/derive/docs.rs
package io.github.kotlinmania.starlark.tests.derive

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

import io.github.kotlinmania.starlark.docs.DocMember
import io.github.kotlinmania.starlark.docs.DocString
import io.github.kotlinmania.starlark.docs.DocStringKind
import io.github.kotlinmania.starlark.docs.DocType
import io.github.kotlinmania.starlark.docs.fromDocstring
import io.github.kotlinmania.starlark.environment.Methods
import io.github.kotlinmania.starlark.environment.MethodsBuilder
import io.github.kotlinmania.starlark.environment.MethodsStatic
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import kotlin.test.Test

/** Main module docs */
private fun objectDocs1(builder: MethodsBuilder) {
    builder.setDocstring("Main module docs")
    /** Returns the string "foo" */
    builder.setAttribute("foo", "Returns the string \"foo\"") { _: Value, heap: Heap ->
        Result.success(heap.allocStr("foo").toValue())
    }
}

private class TestExample : StarlarkValue {
    override val TYPE: String get() = "TestExample"
    override fun toString(): String = "TestExample"

    companion object {
        private val METHODS = MethodsStatic()
        fun getMethods(): Methods = METHODS.methods(::objectDocs1)
    }
}

private class ComplexTestExampleGen<V>(val value: V) : StarlarkValue {
    override val TYPE: String get() = "ComplexTestExample"
    override fun toString(): String = value.toString()

    companion object {
        private val METHODS = MethodsStatic()
        fun getMethods(): Methods = METHODS.methods(::objectDocs1)
    }
}

class DocsTests {
    @Test
    fun testDeriveDocs() {
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

    @Test
    fun testDeriveDocsOnComplexValues() {
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
}
