// port-lint: source values/typing/typeCompiled/matcher.rs
package io.github.kotlinmania.starlark.values.typing.typecompiled

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

import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.typing.TyCustom

/** Marker interface for type matchers which are registered. */
interface TypeMatcherRegistered

/** Base interface for type matchers. */
interface TypeMatcherBase

/**
 * Runtime type matcher. E.g. when `isinstance(1, int)` is called,
 * implementation of `TypeMatcherT` for `int` is used.
 */
interface TypeMatcherT : TypeMatcherBase {
    /** Check if the value matches the type. */
    fun matches(value: Value): Boolean
    /** True if this matcher matches any value. */
    fun isWildcard(): Boolean = false
}

internal interface TypeMatcherDyn {
    fun matchesDyn(value: Value): Boolean
    fun isWildcardDyn(): Boolean
    fun toBox(): TypeMatcherBox
}

internal class TypeMatcherDynAdapter<T : TypeMatcherT>(private val inner: T) : TypeMatcherDyn {
    override fun matchesDyn(value: Value): Boolean = inner.matches(value)
    override fun isWildcardDyn(): Boolean = inner.isWildcard()
    override fun toBox(): TypeMatcherBox = TypeMatcherBox(TypeMatcherDynAdapter(inner))
}

internal class TypeMatcherBox(internal val inner: TypeMatcherDyn) : TypeMatcherT {
    companion object {
        fun <T : TypeMatcherT> new(matcher: T): TypeMatcherBox {
            return TypeMatcherBox(TypeMatcherDynAdapter(matcher))
        }
    }

    fun clone(): TypeMatcherBox = inner.toBox()

    override fun matches(value: Value): Boolean = inner.matchesDyn(value)

    override fun isWildcard(): Boolean = inner.isWildcardDyn()
}

/** Type allocator which allocates `TypeMatcher` into `TypeMatcherBox`. */
internal class TypeMatcherBoxAllocImpl : TypeMatcherAlloc<TypeMatcherBox> {
    override fun alloc(matcher: TypeMatcher): TypeMatcherBox {
        return TypeMatcherBox.new(object : TypeMatcherT {
            override fun matches(value: Value): Boolean = matcher.matches(value)
            override fun isWildcard(): Boolean = matcher.isWildcard()
        })
    }

    override fun custom(custom: TyCustom): TypeMatcherBox {
        return custom.matcherWithBox()
    }

    override fun fromTypeMatcherFactory(factory: TypeMatcherFactory): TypeMatcherBox {
        return factory.factory.matcherBox()
    }
}
