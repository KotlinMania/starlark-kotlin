// port-lint: source src/values/typing/type_compiled/type_matcher_factory.rs
package io.github.kotlinmania.starlark.values.typing.typecompiled

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

import io.github.kotlinmania.starlark.values.layout.Value

private class TypeMatcherFactoryImpl(
    private val matcher: TypeMatcher,
) : TypeMatcherFactoryDyn {
    override fun matcherBox(): TypeMatcherBox =
        TypeMatcherBox.new(
            object : TypeMatcherT {
                override fun matches(value: Value): Boolean = matcher.matches(value)
            },
        )

    override fun typeCompiled(factory: TypeCompiledFactory): TypeCompiled = factory.alloc(matcher)

    override fun toString(): String = "TypeMatcherFactoryImpl($matcher)"
}

// }
internal interface TypeMatcherFactoryDyn {
    fun matcherBox(): TypeMatcherBox

    fun typeCompiled(factory: TypeCompiledFactory): TypeCompiled
}

/** Boxed [TypeMatcher]. */
class TypeMatcherFactory internal constructor(
    internal val factory: TypeMatcherFactoryDyn,
) {
    // impl TypeMatcherFactory

    companion object {
        /** Create a new [TypeMatcherFactory] from a [TypeMatcher]. */
        fun new(matcher: TypeMatcher): TypeMatcherFactory = TypeMatcherFactory(TypeMatcherFactoryImpl(matcher))
    }

    override fun toString(): String = "TypeMatcherFactory($factory)"
}
