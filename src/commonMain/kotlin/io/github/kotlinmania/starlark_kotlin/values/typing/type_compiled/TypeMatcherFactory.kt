// port-lint: source src/values/typing/type_compiled/type_matcher_factory.rs
package io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.type_matcher_factory

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

import io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.factory.TypeCompiledFactory
import io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.TypeCompiled
import io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.TypeMatcherBoxAlloc
import io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.TypeMatcherBox
import io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.TypeMatcher
import io.github.kotlinmania.starlark_kotlin.values.layout.Value

// #[derive(Allocative, Debug)]
// struct TypeMatcherFactoryImpl<M: TypeMatcher> { matcher: M }
private class TypeMatcherFactoryImpl(
    private val matcher: TypeMatcher,
) : TypeMatcherFactoryDyn {
    // impl<M: TypeMatcher> TypeMatcherFactoryDyn for TypeMatcherFactoryImpl<M>

    // fn matcher_box(&self) -> TypeMatcherBox
    override fun matcherBox(): TypeMatcherBox {
        return TypeMatcherBoxAlloc.alloc(matcher)
    }

    // fn type_compiled<'v>(&self, factory: TypeCompiledFactory<'_, 'v>) -> TypeCompiled<Value<'v>>
    override fun typeCompiled(factory: TypeCompiledFactory): TypeCompiled<Value> {
        return factory.alloc(matcher)
    }

    override fun toString(): String = "TypeMatcherFactoryImpl($matcher)"
}

// pub(crate) trait TypeMatcherFactoryDyn: Allocative + Debug + Send + Sync + 'static {
//     fn matcher_box(&self) -> TypeMatcherBox;
//     fn type_compiled<'v>(&self, factory: TypeCompiledFactory<'_, 'v>) -> TypeCompiled<Value<'v>>;
// }
interface TypeMatcherFactoryDyn {
    fun matcherBox(): TypeMatcherBox
    fun typeCompiled(factory: TypeCompiledFactory): TypeCompiled<Value>
}

/** Boxed [TypeMatcher]. */
class TypeMatcherFactory(
    internal val factory: TypeMatcherFactoryDyn,
) {
    // impl TypeMatcherFactory

    companion object {
        /** Create a new [TypeMatcherFactory] from a [TypeMatcher]. */
        fun new(matcher: TypeMatcher): TypeMatcherFactory {
            return TypeMatcherFactory(TypeMatcherFactoryImpl(matcher))
        }
    }

    override fun toString(): String = "TypeMatcherFactory($factory)"
}
