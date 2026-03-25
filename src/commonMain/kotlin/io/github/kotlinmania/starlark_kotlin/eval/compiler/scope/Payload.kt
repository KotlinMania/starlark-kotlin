// port-lint: source src/eval/compiler/scope/payload.rs
package io.github.kotlinmania.starlark_kotlin.eval.compiler.scope

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

/// AST payload for type checking.

// We use CST as acronym for compiler-specific AST.

import io.github.kotlinmania.starlark_kotlin.eval.compiler.BindingId
import io.github.kotlinmania.starlark_kotlin.eval.compiler.ModuleScopeData
import io.github.kotlinmania.starlark_kotlin.eval.compiler.ResolvedIdent
import io.github.kotlinmania.starlark_kotlin.eval.compiler.ScopeId
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.typing.error.InternalError
import io.github.kotlinmania.starlark_kotlin.typing..Interface
import io.github.kotlinmania.starlark_kotlin.syntax.payload_and_span.Payload
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AstTypeExprP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AstStmtP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AstPayload
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AstParameterP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AstNoPayload
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AstIdentP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AstExprP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AstAssignTargetP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AstAssignIdentP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AstStmt
import io.github.kotlinmania.starlark_kotlin.codemap.CodeMap

/// Compiler-specific AST payload.
// #[derive(Debug, Clone)]
// pub(crate) struct CstPayload
// impl AstPayload for CstPayload
internal object CstPayload : AstPayload

internal typealias CstLoadPayload = Interface
internal typealias CstIdentPayload = ResolvedIdent?
internal typealias CstIdentAssignPayload = BindingId?
internal typealias CstDefPayload = ScopeId
internal typealias CstTypeExprPayloadType = CstTypeExprPayload

// #[derive(Default, Debug, Clone)]
// pub(crate) struct CstTypeExprPayload
internal data class CstTypeExprPayload(
    /// Populated before evaluation of top level statements in normal evaluation.
    var compilerTy: Ty? = null,
    /// Populated during lightweight evaluation for the lint type checker.
    var typecheckerTy: Ty? = null,
)



// pub(crate) type CstExpr = AstExprP<CstPayload>
internal typealias CstExpr = AstExprP<CstPayload>
// pub(crate) type CstTypeExpr = AstTypeExprP<CstPayload>
internal typealias CstTypeExpr = AstTypeExprP<CstPayload>
// pub(crate) type CstAssignTarget = AstAssignTargetP<CstPayload>
internal typealias CstAssignTarget = AstAssignTargetP<CstPayload>
// pub(crate) type CstAssignIdent = AstAssignIdentP<CstPayload>
internal typealias CstAssignIdent = AstAssignIdentP<CstPayload>
// pub(crate) type CstIdent = AstIdentP<CstPayload>
internal typealias CstIdent = AstIdentP<CstPayload>
// pub(crate) type CstParameter = AstParameterP<CstPayload>
internal typealias CstParameter = AstParameterP<CstPayload>
// pub(crate) type CstStmt = AstStmtP<CstPayload>
internal typealias CstStmt = AstStmtP<CstPayload>
