// port-lint: source src/tests/derive/module/type_annotation.rs
package io.github.kotlinmania.starlark_kotlin.tests.derive.module

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
import io.github.kotlinmania.starlark_kotlin.environment.GlobalsBuilder
import io.github.kotlinmania.starlark_kotlin.eval.runtime.positional
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.typing.TyStarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue

// #[derive(Debug, Display, ProvidesStaticType, NoSerialize, Allocative)]
// #[display("foo")]
// struct Foo;
private class Foo : StarlarkValue {
    // #[starlark_value(type = "Foo")]
    override val TYPE: String get() = "Foo"
    override fun toString(): String = "foo"
}

// #[starlark_module]
// fn type_annotation_functions(globals: &mut GlobalsBuilder)
private fun typeAnnotationFunctions(globals: GlobalsBuilder) {
    // #[starlark(as_type = Foo)]
    // fn foo(x: i32) -> Result<i32>
    globals.setFunction("foo", asType = Ty.starlarkValue(TyStarlarkValue.new("Foo"))) { args, _ ->
        val x = args.positional<Int>(0)
        Result.success(x)
    }
}

// #[test]
// fn test_type_annotation()
internal fun testTypeAnnotation() {
    val a = Assert()
    a.globalsAdd(::typeAnnotationFunctions)
    a.eq("'Foo'", "foo.type")
}
