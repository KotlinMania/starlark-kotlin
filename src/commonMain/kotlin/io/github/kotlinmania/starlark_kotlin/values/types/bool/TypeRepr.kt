// port-lint: source src/values/types/bool/type_repr.rs
package io.github.kotlinmania.starlark_kotlin.values.types.bool

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

// Placeholder declarations for dependencies not yet ported.
// These will be removed when the actual implementations from
// src/typing/ty.rs and src/values/type_repr.rs are ported.

/**
 * Placeholder for Ty until typing/ty.rs is ported.
 */
internal class Ty private constructor()

/**
 * Placeholder for StarlarkTypeRepr until values/type_repr.rs is ported.
 */
internal interface StarlarkTypeRepr {
    val canonical: StarlarkTypeRepr
    fun starlarkTypeRepr(): Ty
}

/**
 * StarlarkTypeRepr implementation for Boolean.
 *
 * In Rust, this is implemented as:
 * ```rust
 * impl StarlarkTypeRepr for bool {
 *     type Canonical = <StarlarkBool as StarlarkTypeRepr>::Canonical;
 *
 *     fn starlark_type_repr() -> Ty {
 *         StarlarkBool::get_type_starlark_repr()
 *     }
 * }
 * ```
 *
 * This provides the Starlark type representation for Kotlin's Boolean type,
 * delegating to StarlarkBool's implementation via [getTypeStarlarkRepr].
 */
internal object BoolStarlarkTypeRepr : StarlarkTypeRepr {
    /**
     * The canonical type is the same as StarlarkBool's canonical type.
     * In Rust: `type Canonical = <StarlarkBool as StarlarkTypeRepr>::Canonical;`
     */
    override val canonical: StarlarkTypeRepr
        get() = StarlarkBoolStarlarkTypeRepr.canonical

    /**
     * Returns the Starlark type representation for bool.
     * Delegates to StarlarkBool's type representation.
     * In Rust: `StarlarkBool::get_type_starlark_repr()`
     */
    override fun starlarkTypeRepr(): Ty {
        return getTypeStarlarkRepr()
    }
}

/**
 * Helper object to access StarlarkBool's StarlarkTypeRepr implementation.
 * This represents the `StarlarkBool as StarlarkTypeRepr` part from the Rust code.
 */
private object StarlarkBoolStarlarkTypeRepr : StarlarkTypeRepr {
    override val canonical: StarlarkTypeRepr
        get() = this

    override fun starlarkTypeRepr(): Ty {
        return getTypeStarlarkRepr()
    }
}
