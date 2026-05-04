// port-lint: source eval/compiler/type_expr.rs
package io.github.kotlinmania.starlark.eval.compiler

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

import io.github.kotlinmania.starlark.values.typing.typecompiled.TypeCompiled

/** IR expression in type position. */
internal class TypeExprCompiled(
    private val expr: IrSpanned<ExprCompiled>,
) {

    companion object {
        fun newExpr(expr: IrSpanned<ExprCompiled>): TypeExprCompiled {
            return TypeExprCompiled(expr)
        }
    }

    /** True if type is known to match any type. */
    fun isWildcard(): Boolean {
        val value = expr.node.asValue() ?: return false
        return TypeCompiled(value.toValue()).isRuntimeWildcard()
    }
}
