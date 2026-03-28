// port-lint: source src/values/typing/type_compiled/matcher.rs
package io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled

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

import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.typing.matcherWithBox
import io.github.kotlinmania.starlark_kotlin.typing.TyCustom

/// Marker interface for type matchers which are registered.
///
/// In Rust this is an unsafe trait auto-implemented by the `#[type_matcher]` proc macro.
/// In Kotlin we use a plain marker interface — all TypeMatcher implementations are
/// considered registered.
// pub unsafe trait TypeMatcherRegistered {}
interface TypeMatcherRegistered

/// Base interface for type matchers.
///
/// In Rust this is feature-gated behind `pagable` and requires `Allocative + Debug + Clone + Send + Sync + 'static`.
/// In Kotlin the combined constraints are represented as an empty base interface.
// pub trait TypeMatcherBase: Allocative + Debug + Clone + Sized + Send + Sync + 'static {}
interface TypeMatcherBase

/// Runtime type matcher. E.g. when `isinstance(1, int)` is called,
/// implementation of `TypeMatcherT` for `int` is used.
// pub trait TypeMatcher: TypeMatcherBase
interface TypeMatcherT : TypeMatcherBase {
    /// Check if the value matches the type.
    fun matches(value: Value): Boolean
    /// True if this matcher matches any value.
    fun isWildcard(): Boolean = false
}

// pub(crate) trait TypeMatcherDyn: Debug + Allocative + Send + Sync + 'static
internal interface TypeMatcherDyn {
    fun matchesDyn(value: Value): Boolean
    fun isWildcardDyn(): Boolean
    fun toBox(): TypeMatcherBox
}

// impl<T: TypeMatcher> TypeMatcherDyn for T — blanket impl
// In Kotlin we provide a wrapper adapter instead.
internal class TypeMatcherDynAdapter<T : TypeMatcherT>(private val inner: T) : TypeMatcherDyn {
    override fun matchesDyn(value: Value): Boolean = inner.matches(value)
    override fun isWildcardDyn(): Boolean = inner.isWildcard()
    override fun toBox(): TypeMatcherBox = TypeMatcherBox(TypeMatcherDynAdapter(inner))
}

// #[derive(Debug, Allocative)]
// pub(crate) struct TypeMatcherBox(pub(crate) Box<dyn TypeMatcherDyn>)
internal class TypeMatcherBox(internal val inner: TypeMatcherDyn) : TypeMatcherT {
    companion object {
        // pub(crate) fn new<T: TypeMatcher>(matcher: T) -> TypeMatcherBox
        fun <T : TypeMatcherT> new(matcher: T): TypeMatcherBox {
            return TypeMatcherBox(TypeMatcherDynAdapter(matcher))
        }
    }

    // impl Clone for TypeMatcherBox
    fun clone(): TypeMatcherBox = inner.toBox()

    // #[type_matcher]
    // impl TypeMatcher for TypeMatcherBox
    override fun matches(value: Value): Boolean = inner.matchesDyn(value)

    override fun isWildcard(): Boolean = inner.isWildcardDyn()
}

/// Type allocator which allocates `TypeMatcher` into `TypeMatcherBox`.
// pub(crate) struct TypeMatcherBoxAlloc
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
        return factory.factoryMatcherBox()
    }
}
