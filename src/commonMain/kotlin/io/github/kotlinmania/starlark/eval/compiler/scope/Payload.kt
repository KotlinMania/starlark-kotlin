// port-lint: source src/eval/compiler/scope/payload.rs

package io.github.kotlinmania.starlark.eval.compiler.scope

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

/** AST payload for type checking. */

// We use CST as acronym for compiler-specific AST.

import io.github.kotlinmania.starlark.codemap.Spanned
import io.github.kotlinmania.starlark.eval.compiler.BindingId
import io.github.kotlinmania.starlark.eval.compiler.ResolvedIdent
import io.github.kotlinmania.starlark.eval.compiler.ScopeId
import io.github.kotlinmania.starlark.syntax.ast.AstAssignIdentP
import io.github.kotlinmania.starlark.syntax.ast.AstAssignTargetP
import io.github.kotlinmania.starlark.syntax.ast.AstExprP
import io.github.kotlinmania.starlark.syntax.ast.AstIdentP
import io.github.kotlinmania.starlark.syntax.ast.AstParameterP
import io.github.kotlinmania.starlark.syntax.ast.AstPayload
import io.github.kotlinmania.starlark.syntax.ast.AstStmtP
import io.github.kotlinmania.starlark.syntax.ast.TypeExprP
import io.github.kotlinmania.starlark.syntax.ast.TypeExprPayload
import io.github.kotlinmania.starlark.typing.Interface
import io.github.kotlinmania.starlark.typing.Ty

/** Compiler-specific AST payload. */
object CstPayload : AstPayload

internal typealias CstLoadPayload = Interface
internal typealias CstIdentPayload = ResolvedIdent?
internal typealias CstIdentAssignPayload = BindingId?
internal typealias CstDefPayload = ScopeId
internal typealias CstTypeExprPayloadType = CstTypeExprPayload

internal data class CstTypeExprPayload(
    /** Populated before evaluation of top level statements in normal evaluation. */
    var compilerTy: Ty? = null,
    /** Populated during lightweight evaluation for the lint type checker. */
    var typecheckerTy: Ty? = null,
) : TypeExprPayload

internal typealias CstExpr = AstExprP<CstPayload>

internal typealias CstTypeExpr = Spanned<TypeExprP<CstPayload>>

internal val CstTypeExpr.cstPayload: CstTypeExprPayload
    get() =
        node.payload as? CstTypeExprPayload
            ?: error("compiler type expression missing compiler payload")

internal typealias CstAssignTarget = AstAssignTargetP<CstPayload>

internal typealias CstAssignIdent = AstAssignIdentP<CstPayload, CstIdentAssignPayload>

internal typealias CstIdent = AstIdentP<CstPayload, CstIdentPayload>

internal typealias CstParameter = AstParameterP<CstPayload>

internal typealias CstStmt = AstStmtP<CstPayload>
