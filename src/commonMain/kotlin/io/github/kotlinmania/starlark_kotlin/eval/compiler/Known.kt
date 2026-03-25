// port-lint: source src/eval/compiler/known.rs
package io.github.kotlinmania.starlark_kotlin.eval.compiler

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

/// Things that operate on known values where we know we can do better.

import io.github.kotlinmania.starlark_kotlin.syntax.ast.ExprP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.CstExpr
import io.github.kotlinmania.starlark_kotlin.values.types.list.List
import io.github.kotlinmania.starlark_kotlin.values.types.enumeration.enum_type.elements
import io.github.kotlinmania.starlark_kotlin.typing.fill_types_for_lint.CstExpr
import io.github.kotlinmania.starlark_kotlin.analysis.node
import io.github.kotlinmania.starlark_kotlin.analysis.span
import io.github.kotlinmania.starlark_kotlin.codemap.Spanned

/// Convert a list into a tuple. In many cases (iteration, `in`) these types
/// behave the same, but a list has identity and mutability, so much better to
/// switch to tuple where it makes no difference. A tuple of constants
/// will go on the FrozenHeap, while a list of constants will be continually
/// reallocated.
// pub(crate) fn list_to_tuple(x: &CstExpr) -> Cow<'_, CstExpr>
internal fun listToTuple(x: CstExpr): CstExpr {
    return when (val node = x.node) {
        is ExprP.List -> Spanned(
            node = ExprP.Tuple(node.elements),
            span = x.span,
        )
        else -> x
    }
}
