// port-lint: source src/eval/compiler/type_expr.rs
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

import io.github.kotlinmania.starlark_kotlin.eval.compiler.expr.ExprCompiled
import io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.factory.TypeCompiled
import io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.TypeCompiled
import io.github.kotlinmania.starlark_kotlin.values.toValue
import io.github.kotlinmania.starlark_kotlin.eval.compiler.args.asValue

/// IR expression in type position.
// #[derive(Clone, Debug, VisitSpanMut)]
// pub(crate) struct TypeExprCompiled
internal data class TypeExprCompiled(
    val expr: IrSpanned<ExprCompiled>,
) {
    companion object {
        // pub(crate) fn new_expr(expr: IrSpanned<ExprCompiled>) -> TypeExprCompiled
        internal fun newExpr(expr: IrSpanned<ExprCompiled>): TypeExprCompiled {
            return TypeExprCompiled(expr)
        }
    }

    /// True if type is known to match any type.
    // pub(crate) fn is_wildcard(&self) -> bool
    internal fun isWildcard(): Boolean {
        val value = expr.asValue()
        return if (value != null) {
            TypeCompiled.isWildcardValue(value.toValue())
        } else {
            false
        }
    }
}
