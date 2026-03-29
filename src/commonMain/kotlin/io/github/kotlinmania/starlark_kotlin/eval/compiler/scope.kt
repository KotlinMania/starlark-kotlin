// port-lint: source src/eval/compiler/scope.rs
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

/// Compiler scope resolution module.
///
/// Submodules:
///  - scope/Payload.kt (payload)
///  - scope/ScopeResolverGlobals.kt (scope_resolver_globals)
///  - scope/Tests.kt (tests)

import io.github.kotlinmania.starlark_kotlin.codemap.CodeMap
import io.github.kotlinmania.starlark_kotlin.codemap.Span
import io.github.kotlinmania.starlark_kotlin.codemap.Spanned
import io.github.kotlinmania.starlark_kotlin.environment.Module
import io.github.kotlinmania.starlark_kotlin.environment.ModuleSlotId
import io.github.kotlinmania.starlark_kotlin.environment.MutableNames
import io.github.kotlinmania.starlark_kotlin.errors.didYouMean
import io.github.kotlinmania.starlark_kotlin.eval.compiler.scope.CstAssignTarget
import io.github.kotlinmania.starlark_kotlin.eval.compiler.scope.CstExpr
import io.github.kotlinmania.starlark_kotlin.eval.compiler.scope.CstIdent
import io.github.kotlinmania.starlark_kotlin.eval.compiler.scope.CstAssignIdent
import io.github.kotlinmania.starlark_kotlin.eval.compiler.scope.CstParameter
import io.github.kotlinmania.starlark_kotlin.eval.compiler.scope.CstPayload
import io.github.kotlinmania.starlark_kotlin.eval.compiler.scope.CstStmt
import io.github.kotlinmania.starlark_kotlin.eval.compiler.scope.CstTypeExpr
import io.github.kotlinmania.starlark_kotlin.eval.compiler.scope.ScopeResolverGlobals
import io.github.kotlinmania.starlark_kotlin.eval.runtime.LocalSlotIdCapturedOrNot
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AstAssignIdentP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AstStmt
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AssignIdentP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AssignTargetP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.ClauseP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.DefP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.ExprP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.ForClauseP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.ForP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.LambdaP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.LoadArgP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.ParameterP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.StmtP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.TypeExprP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.Visibility
import io.github.kotlinmania.starlark_kotlin.syntax.dialect.Dialect
import io.github.kotlinmania.starlark_kotlin.typing.EvalException
import io.github.kotlinmania.starlark_kotlin.typing.Interface
import io.github.kotlinmania.starlark_kotlin.typing.StarlarkError
import io.github.kotlinmania.starlark_kotlin.values.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.FrozenRef
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.layout.typed.FrozenStringValue

// ---------------------------------------------------------------------------
// Visitor infrastructure (port of starlark_syntax::syntax::uniplate)
// ---------------------------------------------------------------------------

/// VisitMut — mutable visitor over CST children.
internal sealed class VisitMut {
    class Stmt(val stmt: CstStmt) : VisitMut()
    class Expr(val expr: CstExpr) : VisitMut()
}

/// Flatten top-level Statements into a list.
/// Port of `top_level_stmts_mut`.
internal fun topLevelStmtsMut(top: CstStmt): MutableList<CstStmt> {
    val result = mutableListOf<CstStmt>()
    fun flatten(stmt: CstStmt) {
        when (val node = stmt.node) {
            is StmtP.Statements<*> -> {
                @Suppress("UNCHECKED_CAST")
                for (s in node.stmts as List<CstStmt>) {
                    flatten(s)
                }
            }
            else -> result.add(stmt)
        }
    }
    flatten(top)
    return result
}

/// Convert AstStmt (with AstNoPayload) to CstStmt (with CstPayload).
/// Port of `CstStmt::from_ast`.
/// This is a simplified version that performs payload mapping.
internal fun cstStmtFromAst(
    stmt: AstStmt,
    scopeData: ModuleScopeData,
    loads: Map<String, Interface>,
): CstStmt {
    // The full implementation maps payloads from AstNoPayload -> CstPayload.
    // For now, we cast since the AST node structure is the same; payload
    // fields that differ (ident payload, def payload, etc.) start as null/default
    // and are filled in during scope resolution.
    @Suppress("UNCHECKED_CAST")
    return stmt as CstStmt
}

/// Extension: visit children of a StmtP node (mutable visitor).
/// Port of `StmtP::visit_children_mut`.
internal fun CstStmt.visitChildrenMut(f: (VisitMut) -> Unit) {
    when (val node = this.node) {
        is StmtP.Statements<*> -> {
            @Suppress("UNCHECKED_CAST")
            (node.stmts as List<CstStmt>).forEach { f(VisitMut.Stmt(it)) }
        }
        is StmtP.If<*> -> {
            @Suppress("UNCHECKED_CAST")
            f(VisitMut.Expr(node.cond as CstExpr))
            @Suppress("UNCHECKED_CAST")
            f(VisitMut.Stmt(node.suite as CstStmt))
        }
        is StmtP.IfElse<*> -> {
            @Suppress("UNCHECKED_CAST")
            f(VisitMut.Expr(node.cond as CstExpr))
            @Suppress("UNCHECKED_CAST")
            f(VisitMut.Stmt(node.suite1 as CstStmt))
            @Suppress("UNCHECKED_CAST")
            f(VisitMut.Stmt(node.suite2 as CstStmt))
        }
        is StmtP.Def<*, *> -> {
            val defP = node.def
            @Suppress("UNCHECKED_CAST")
            for (p in defP.params as List<Spanned<ParameterP<CstPayload>>>) {
                p.node.visitExprMut { f(VisitMut.Expr(it)) }
            }
            defP.returnType?.let {
                @Suppress("UNCHECKED_CAST")
                (it as Spanned<TypeExprP<CstPayload, *>>).node.expr.let { expr ->
                    @Suppress("UNCHECKED_CAST")
                    f(VisitMut.Expr(expr as CstExpr))
                }
            }
            @Suppress("UNCHECKED_CAST")
            f(VisitMut.Stmt(defP.body as CstStmt))
        }
        is StmtP.For<*> -> {
            val forP = node.forStmt
            @Suppress("UNCHECKED_CAST")
            (forP.varTarget as Spanned<AssignTargetP<CstPayload>>).visitExprMut { f(VisitMut.Expr(it)) }
            @Suppress("UNCHECKED_CAST")
            f(VisitMut.Expr(forP.over as CstExpr))
            @Suppress("UNCHECKED_CAST")
            f(VisitMut.Stmt(forP.body as CstStmt))
        }
        is StmtP.Return<*> -> {
            @Suppress("UNCHECKED_CAST")
            node.expr?.let { f(VisitMut.Expr(it as CstExpr)) }
        }
        is StmtP.Expression<*> -> {
            @Suppress("UNCHECKED_CAST")
            f(VisitMut.Expr(node.expr as CstExpr))
        }
        is StmtP.Assign<*> -> {
            val assignP = node.assign
            @Suppress("UNCHECKED_CAST")
            (assignP.lhs as CstAssignTarget).visitExprMut { f(VisitMut.Expr(it)) }
            @Suppress("UNCHECKED_CAST")
            assignP.ty?.let {
                (it as Spanned<TypeExprP<CstPayload, *>>).node.expr.let { expr ->
                    @Suppress("UNCHECKED_CAST")
                    f(VisitMut.Expr(expr as CstExpr))
                }
            }
            @Suppress("UNCHECKED_CAST")
            f(VisitMut.Expr(assignP.rhs as CstExpr))
        }
        is StmtP.AssignModify<*> -> {
            @Suppress("UNCHECKED_CAST")
            (node.lhs as CstAssignTarget).visitExprMut { f(VisitMut.Expr(it)) }
            @Suppress("UNCHECKED_CAST")
            f(VisitMut.Expr(node.rhs as CstExpr))
        }
        is StmtP.Load<*, *> -> { /* no children */ }
        is StmtP.Break<*>,
        is StmtP.Continue<*>,
        is StmtP.Pass<*> -> { /* no children */ }
    }
}

/// Extension: visit only stmt children of StmtP (mutable).
/// Port of `StmtP::visit_stmt_mut`.
internal fun StmtP<CstPayload>.visitStmtMut(f: (CstStmt) -> Unit) {
    // Wrap in a Spanned to reuse visitChildrenMut - we need to construct a temporary
    // Actually, we can just filter VisitMut.Stmt from visitChildrenMut
    // But visitChildrenMut is on CstStmt (Spanned), not on StmtP directly.
    // So we implement directly:
    when (this) {
        is StmtP.Statements<*> -> {
            @Suppress("UNCHECKED_CAST")
            (this.stmts as List<CstStmt>).forEach { f(it) }
        }
        is StmtP.If<*> -> {
            @Suppress("UNCHECKED_CAST")
            f(this.suite as CstStmt)
        }
        is StmtP.IfElse<*> -> {
            @Suppress("UNCHECKED_CAST")
            f(this.suite1 as CstStmt)
            @Suppress("UNCHECKED_CAST")
            f(this.suite2 as CstStmt)
        }
        is StmtP.Def<*, *> -> {
            @Suppress("UNCHECKED_CAST")
            f(this.def.body as CstStmt)
        }
        is StmtP.For<*> -> {
            @Suppress("UNCHECKED_CAST")
            f(this.forStmt.body as CstStmt)
        }
        else -> { /* no stmt children */ }
    }
}

/// Extension: visit expr children (mutable) of an ExprP.
/// Port of `ExprP::visit_expr_mut` (on the inner expression node, not the Spanned wrapper).
internal fun CstExpr.visitExprMut(f: (CstExpr) -> Unit) {
    when (val node = this.node) {
        is ExprP.Tuple<*> -> {
            @Suppress("UNCHECKED_CAST")
            (node.elements as List<CstExpr>).forEach { f(it) }
        }
        is ExprP.Dot<*> -> {
            @Suppress("UNCHECKED_CAST")
            f(node.expr as CstExpr)
        }
        is ExprP.Call<*> -> {
            @Suppress("UNCHECKED_CAST")
            f(node.expr as CstExpr)
            @Suppress("UNCHECKED_CAST")
            for (arg in node.args.args) {
                f(arg.node.expr() as CstExpr)
            }
        }
        is ExprP.Index<*> -> {
            @Suppress("UNCHECKED_CAST")
            f(node.expr as CstExpr)
            @Suppress("UNCHECKED_CAST")
            f(node.index as CstExpr)
        }
        is ExprP.Slice<*> -> {
            @Suppress("UNCHECKED_CAST")
            f(node.expr as CstExpr)
            @Suppress("UNCHECKED_CAST")
            node.start?.let { f(it as CstExpr) }
            @Suppress("UNCHECKED_CAST")
            node.stop?.let { f(it as CstExpr) }
            @Suppress("UNCHECKED_CAST")
            node.step?.let { f(it as CstExpr) }
        }
        is ExprP.Not<*> -> {
            @Suppress("UNCHECKED_CAST")
            f(node.expr as CstExpr)
        }
        is ExprP.Minus<*> -> {
            @Suppress("UNCHECKED_CAST")
            f(node.expr as CstExpr)
        }
        is ExprP.Plus<*> -> {
            @Suppress("UNCHECKED_CAST")
            f(node.expr as CstExpr)
        }
        is ExprP.BitNot<*> -> {
            @Suppress("UNCHECKED_CAST")
            f(node.expr as CstExpr)
        }
        is ExprP.Op<*> -> {
            @Suppress("UNCHECKED_CAST")
            f(node.lhs as CstExpr)
            @Suppress("UNCHECKED_CAST")
            f(node.rhs as CstExpr)
        }
        is ExprP.If<*> -> {
            @Suppress("UNCHECKED_CAST")
            f(node.cond as CstExpr)
            @Suppress("UNCHECKED_CAST")
            f(node.v1 as CstExpr)
            @Suppress("UNCHECKED_CAST")
            f(node.v2 as CstExpr)
        }
        is ExprP.ListExpr<*> -> {
            @Suppress("UNCHECKED_CAST")
            (node.elements as List<CstExpr>).forEach { f(it) }
        }
        is ExprP.Dict<*> -> {
            @Suppress("UNCHECKED_CAST")
            for ((k, v) in node.elements as List<Pair<CstExpr, CstExpr>>) {
                f(k); f(v)
            }
        }
        is ExprP.ListComprehension<*> -> {
            @Suppress("UNCHECKED_CAST")
            (node.forClause as ForClauseP<CstPayload>).visitExprMut(f)
            @Suppress("UNCHECKED_CAST")
            (node.clauses as List<ClauseP<CstPayload>>).forEach { it.visitExprMut(f) }
            @Suppress("UNCHECKED_CAST")
            f(node.expr as CstExpr)
        }
        is ExprP.DictComprehension<*> -> {
            @Suppress("UNCHECKED_CAST")
            (node.forClause as ForClauseP<CstPayload>).visitExprMut(f)
            @Suppress("UNCHECKED_CAST")
            (node.clauses as List<ClauseP<CstPayload>>).forEach { it.visitExprMut(f) }
            @Suppress("UNCHECKED_CAST")
            f(node.key as CstExpr)
            @Suppress("UNCHECKED_CAST")
            f(node.value as CstExpr)
        }
        is ExprP.Lambda<*, *> -> {
            val lambdaP = node.lambda
            @Suppress("UNCHECKED_CAST")
            for (p in lambdaP.params as List<Spanned<ParameterP<CstPayload>>>) {
                p.node.visitExprMut(f)
            }
            @Suppress("UNCHECKED_CAST")
            f(lambdaP.body as CstExpr)
        }
        is ExprP.Identifier<*, *>,
        is ExprP.Literal<*>,
        is ExprP.FString<*> -> { /* no expr children */ }
        is ExprP.Index2<*> -> {
            @Suppress("UNCHECKED_CAST")
            f(node.expr as CstExpr)
            @Suppress("UNCHECKED_CAST")
            f(node.index0 as CstExpr)
            @Suppress("UNCHECKED_CAST")
            f(node.index1 as CstExpr)
        }
        is ExprP.DictComprehension<*> -> { /* already handled above */ }
    }
}

/// Extension: visit expr children of an AssignTargetP.
/// Port of `AssignTargetP::visit_expr_mut`.
internal fun Spanned<AssignTargetP<CstPayload>>.visitExprMut(f: (CstExpr) -> Unit) {
    when (val node = this.node) {
        is AssignTargetP.Tuple<*> -> {
            @Suppress("UNCHECKED_CAST")
            for (elem in node.elements as List<Spanned<AssignTargetP<CstPayload>>>) {
                elem.visitExprMut(f)
            }
        }
        is AssignTargetP.Index<*> -> {
            @Suppress("UNCHECKED_CAST")
            f(node.expr as CstExpr)
            @Suppress("UNCHECKED_CAST")
            f(node.index as CstExpr)
        }
        is AssignTargetP.Dot<*> -> {
            @Suppress("UNCHECKED_CAST")
            f(node.expr as CstExpr)
        }
        is AssignTargetP.Identifier<*, *> -> { /* no expr children */ }
    }
}

/// Extension: visit lvalue identifiers in an AssignTargetP.
/// Port of `AssignTargetP::visit_lvalue_mut`.
internal fun AssignTargetP<CstPayload>.visitLvalueMut(f: (CstAssignIdent) -> Unit) {
    when (this) {
        is AssignTargetP.Identifier<*, *> -> {
            @Suppress("UNCHECKED_CAST")
            f(this.ident as CstAssignIdent)
        }
        is AssignTargetP.Tuple<*> -> {
            @Suppress("UNCHECKED_CAST")
            for (elem in this.elements as List<Spanned<AssignTargetP<CstPayload>>>) {
                elem.node.visitLvalueMut(f)
            }
        }
        else -> { /* Index, Dot have no lvalues */ }
    }
}

/// Extension: visit expr children of a ParameterP.
/// Port of `ParameterP::visit_expr_mut`.
internal fun ParameterP<CstPayload>.visitExprMut(f: (CstExpr) -> Unit) {
    val (_, ty, def) = this.splitMut()
    @Suppress("UNCHECKED_CAST")
    ty?.let { (it as Spanned<TypeExprP<CstPayload, *>>).node.expr.let { expr -> f(expr as CstExpr) } }
    def?.let { f(it) }
}

/// Extension: split a ParameterP into (name?, type?, default?).
/// Port of `ParameterP::split_mut`.
internal fun ParameterP<CstPayload>.splitMut(): Triple<AstAssignIdentP<CstPayload, *>?, Spanned<TypeExprP<CstPayload, *>>?, CstExpr?> {
    return when (this) {
        is ParameterP.Normal<*> -> {
            @Suppress("UNCHECKED_CAST")
            Triple(
                this.name as AstAssignIdentP<CstPayload, *>,
                this.typ as Spanned<TypeExprP<CstPayload, *>>?,
                this.defaultVal as CstExpr?,
            )
        }
        is ParameterP.Args<*> -> {
            @Suppress("UNCHECKED_CAST")
            Triple(
                this.name as AstAssignIdentP<CstPayload, *>,
                this.typ as Spanned<TypeExprP<CstPayload, *>>?,
                null,
            )
        }
        is ParameterP.KwArgs<*> -> {
            @Suppress("UNCHECKED_CAST")
            Triple(
                this.name as AstAssignIdentP<CstPayload, *>,
                this.typ as Spanned<TypeExprP<CstPayload, *>>?,
                null,
            )
        }
        is ParameterP.NoArgs<*>,
        is ParameterP.Slash<*> -> Triple(null, null, null)
    }
}

/// Extension: visit expr children of a ForClauseP.
internal fun ForClauseP<CstPayload>.visitExprMut(f: (CstExpr) -> Unit) {
    @Suppress("UNCHECKED_CAST")
    (this.varTarget as Spanned<AssignTargetP<CstPayload>>).visitExprMut(f)
    f(this.over)
}

/// Extension: visit expr children of a ClauseP.
internal fun ClauseP<CstPayload>.visitExprMut(f: (CstExpr) -> Unit) {
    when (this) {
        is ClauseP.For<*> -> {
            @Suppress("UNCHECKED_CAST")
            (this.forClause as ForClauseP<CstPayload>).visitExprMut(f)
        }
        is ClauseP.If<*> -> {
            @Suppress("UNCHECKED_CAST")
            f(this.cond as CstExpr)
        }
    }
}

// #[derive(Debug, thiserror::Error)]
// enum ScopeError
internal sealed class ScopeError(message: String) : StarlarkError(message) {
    // #[error("Variable `{0}` not found")]
    class VariableNotFound(val name: String) : ScopeError("Variable `$name` not found")
    // #[error("Variable `{0}` not found, did you mean `{1}`?")]
    class VariableNotFoundDidYouMean(val name: String, val suggestion: String) : ScopeError("Variable `$name` not found, did you mean `$suggestion`?")
    // #[error("Identifiers in type expressions can only refer globals or builtins: `{0}`")]
    class TypeExpressionGlobalOrBuiltin(val name: String) : ScopeError("Identifiers in type expressions can only refer globals or builtins: `$name`")
}

/// All scopes and bindings in a module.
// struct ModuleScopeBuilder<'a>
internal class ModuleScopeBuilder(
    var scopeData: ModuleScopeData,
    val module: MutableNames,
    val frozenHeap: FrozenHeap,
    var moduleBindings: MutableMap<FrozenStringValue, BindingId>,
    // The first scope is a module-level scope (including comprehensions in module scope).
    // The rest are scopes for functions (which include their comprehensions).
    val locals: MutableList<ScopeId>,
    val unscopes: MutableList<Unscope>,
    val codemap: FrozenRef<CodeMap>,
    val globals: ScopeResolverGlobals,
    val errors: MutableList<EvalException>,
    var topLevelStmtCount: Int,
) {
    // fn top_scope_id(&self) -> ScopeId
    fun topScopeId(): ScopeId = locals.last()

    // fn scope_at_level(&self, level: usize) -> &ScopeNames
    fun scopeAtLevel(level: Int): ScopeNames {
        val scopeId = locals[level]
        return scopeData.getScope(scopeId)
    }

    // fn scope_at_level_mut(&mut self, level: usize) -> &mut ScopeNames
    fun scopeAtLevelMut(level: Int): ScopeNames {
        val scopeId = locals[level]
        return scopeData.mutScope(scopeId)
    }

    companion object {
        /// Resolve symbols in a module.
        ///
        /// Checks all the symbols are resolved to locals/globals/captured/etc.
        /// Do not check types yet. But validate type expressions.
        ///
        /// This function does not fail, errors are stored in the `errors` field.
        // fn enter_module(...)
        fun enterModule(
            module: MutableNames,
            frozenHeap: FrozenHeap,
            loads: Map<String, Interface>,
            stmt: AstStmt,
            globals: ScopeResolverGlobals,
            codemap: FrozenRef<CodeMap>,
            dialect: Dialect,
        ): Pair<CstStmt, ModuleScopeBuilder> {
            val scopeData = ModuleScopeData()
            val (scopeId, _) = scopeData.newScope()
            var cst = cstStmtFromAst(stmt, scopeData, loads)

            val topLevelStmts = topLevelStmtsMut(cst)

            // Not really important, sanity check
            check(scopeId == ScopeId.module())

            scopeData.mutScope(scopeId).setParamCount(0)

            val localBindings: MutableMap<FrozenStringValue, BindingId> = mutableMapOf()

            val existingModuleNamesAndVisibilities = module.allNamesAndVisibilities()
            for ((name, vis) in existingModuleNamesAndVisibilities) {
                val (bindingId, _) = scopeData.newBinding(
                    name,
                    BindingSource.FromModule,
                    vis,
                    AssignCount.AtMostOnce,
                )
                localBindings[name] = bindingId
            }

            for (s in topLevelStmts) {
                collectDefines(
                    s,
                    InLoop.No,
                    scopeData,
                    frozenHeap,
                    localBindings,
                    dialect,
                )
            }

            val moduleBindings: MutableMap<FrozenStringValue, BindingId> = mutableMapOf()
            for ((x, bindingId) in localBindings) {
                val binding = scopeData.mutBinding(bindingId)
                val slot = module.addNameVisibility(x, binding.vis)
                binding.initSlot(Slot.Module(slot), codemap.value)
                check(moduleBindings.put(x, bindingId) == null)
            }

            // Here we traverse the AST second time to collect scopes of defs
            for (s in topLevelStmts) {
                collectDefinesRecursively(
                    scopeData,
                    s,
                    frozenHeap,
                    dialect,
                    codemap.value,
                )
            }
            val scope = ModuleScopeBuilder(
                scopeData = scopeData,
                frozenHeap = frozenHeap,
                module = module,
                moduleBindings = moduleBindings,
                locals = mutableListOf(scopeId),
                unscopes = mutableListOf(),
                codemap = codemap,
                globals = globals,
                errors = mutableListOf(),
                topLevelStmtCount = topLevelStmts.size,
            )
            for (s in topLevelStmts) {
                scope.resolveIdents(s)
            }
            return Pair(cst, scope)
        }

        // fn collect_defines_in_def(...)
        fun collectDefinesInDef(
            scopeData: ModuleScopeData,
            scopeId: ScopeId,
            params: List<CstParameter>,
            body: CstStmt?,
            frozenHeap: FrozenHeap,
            dialect: Dialect,
            codemap: CodeMap,
        ) {
            val paramIdents: MutableList<AstAssignIdentP<CstPayload, *>> = mutableListOf()
            for (p in params) {
                val ident = p.node.splitMut().first
                if (ident != null) {
                    paramIdents.add(ident)
                }
            }
            scopeData.mutScope(scopeId).setParamCount(paramIdents.size)
            val localBindings: MutableMap<FrozenStringValue, BindingId> = mutableMapOf()
            for (p in paramIdents) {
                val name = frozenHeap.allocStrIntern(p.node.ident)
                // Subtle invariant: the slots for the params must be ordered and at the beginning
                val (bindingId, _) = scopeData.newBinding(
                    name,
                    BindingSource.Source(p.span),
                    Visibility.Public,
                    AssignCount.AtMostOnce,
                )
                @Suppress("UNCHECKED_CAST")
                (p.node as AssignIdentP<CstPayload, BindingId?>).payload = bindingId
                check(localBindings.put(name, bindingId) == null)
            }
            if (body != null) {
                collectDefines(
                    body,
                    InLoop.No,
                    scopeData,
                    frozenHeap,
                    localBindings,
                    dialect,
                )
            }
            for ((name, bindingId) in localBindings) {
                val slot = scopeData.mutScope(scopeId).addName(name, bindingId)
                val binding = scopeData.mutBinding(bindingId)
                binding.initSlot(Slot.Local(slot), codemap)
            }
        }

        // fn collect_defines_recursively(...)
        fun collectDefinesRecursively(
            scopeData: ModuleScopeData,
            code: CstStmt,
            frozenHeap: FrozenHeap,
            dialect: Dialect,
            codemap: CodeMap,
        ) {
            val node = code.node
            if (node is StmtP.Def<*, *>) {
                val defP = node.def
                @Suppress("UNCHECKED_CAST")
                collectDefinesInDef(
                    scopeData,
                    defP.payload as ScopeId,
                    defP.params as List<CstParameter>,
                    defP.body as CstStmt,
                    frozenHeap,
                    dialect,
                    codemap,
                )
            }

            code.visitChildrenMut { visit ->
                when (visit) {
                    is VisitMut.Expr -> collectDefinesRecursivelyInExpr(
                        scopeData,
                        visit.expr,
                        frozenHeap,
                        dialect,
                        codemap,
                    )
                    is VisitMut.Stmt -> collectDefinesRecursively(
                        scopeData,
                        visit.stmt,
                        frozenHeap,
                        dialect,
                        codemap,
                    )
                }
            }
        }

        // fn collect_defines_recursively_in_expr(...)
        fun collectDefinesRecursivelyInExpr(
            scopeData: ModuleScopeData,
            code: CstExpr,
            frozenHeap: FrozenHeap,
            dialect: Dialect,
            codemap: CodeMap,
        ) {
            val node = code.node
            if (node is ExprP.Lambda<*, *>) {
                val lambdaP = node.lambda
                @Suppress("UNCHECKED_CAST")
                collectDefinesInDef(
                    scopeData,
                    lambdaP.payload as ScopeId,
                    lambdaP.params as List<CstParameter>,
                    null,
                    frozenHeap,
                    dialect,
                    codemap,
                )
            }

            code.visitExprMut { e ->
                collectDefinesRecursivelyInExpr(scopeData, e, frozenHeap, dialect, codemap)
            }
        }

        // fn collect_defines(...) — Stmt trait impl
        fun collectDefines(
            stmt: CstStmt,
            inLoop: InLoop,
            scopeData: ModuleScopeData,
            frozenHeap: FrozenHeap,
            result: MutableMap<FrozenStringValue, BindingId>,
            dialect: Dialect,
        ) {
            when (val node = stmt.node) {
                is StmtP.Assign<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val assignP = node.assign as io.github.kotlinmania.starlark_kotlin.syntax.ast.AssignP<CstPayload>
                    collectDefinesLvalue(
                        assignP.lhs,
                        inLoop,
                        scopeData,
                        frozenHeap,
                        result,
                    )
                }
                is StmtP.AssignModify<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    collectDefinesLvalue(
                        node.lhs as CstAssignTarget,
                        inLoop,
                        scopeData,
                        frozenHeap,
                        result,
                    )
                }
                is StmtP.For<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val forP = node.forStmt as ForP<CstPayload>
                    collectDefinesLvalue(
                        forP.varTarget,
                        InLoop.Yes,
                        scopeData,
                        frozenHeap,
                        result,
                    )
                    collectDefines(forP.body, InLoop.Yes, scopeData, frozenHeap, result, dialect)
                }
                is StmtP.Def<*, *> -> {
                    @Suppress("UNCHECKED_CAST")
                    val defP = node.def as DefP<CstPayload, ScopeId>
                    collectAssignIdent(
                        defP.name as CstAssignIdent,
                        inLoop,
                        Visibility.Public,
                        scopeData,
                        frozenHeap,
                        result,
                    )
                }
                is StmtP.Load<*, *> -> {
                    val vis = if (dialect.enableLoadReexport) Visibility.Public else Visibility.Private
                    @Suppress("UNCHECKED_CAST")
                    val loadP = node.loadStmt as io.github.kotlinmania.starlark_kotlin.syntax.ast.LoadP<CstPayload, *>
                    for (loadArg in loadP.args) {
                        var argVis = vis
                        if (Module.defaultVisibility(loadArg.local.node.ident) == Visibility.Private) {
                            argVis = Visibility.Private
                        }
                        @Suppress("UNCHECKED_CAST")
                        collectAssignIdent(
                            loadArg.local as CstAssignIdent,
                            inLoop,
                            argVis,
                            scopeData,
                            frozenHeap,
                            result,
                        )
                    }
                }
                else -> {
                    @Suppress("UNCHECKED_CAST")
                    (node as StmtP<CstPayload>).visitStmtMut { x ->
                        collectDefines(x, inLoop, scopeData, frozenHeap, result, dialect)
                    }
                }
            }
        }

        // fn collect_assign_ident(...) — AssignIdent trait impl
        fun collectAssignIdent(
            assign: CstAssignIdent,
            inLoop: InLoop,
            vis: Visibility,
            scopeData: ModuleScopeData,
            frozenHeap: FrozenHeap,
            result: MutableMap<FrozenStringValue, BindingId>,
        ) {
            val name = frozenHeap.allocStrIntern(assign.node.ident)
            val span = assign.span

            check(assign.node.payload == null) {
                "binding can be assigned only once: `${name.asStr()}`"
            }

            var effectiveVis = vis
            if (effectiveVis == Visibility.Public) {
                effectiveVis = Module.defaultVisibility(name.asStr())
            }

            val existing = result[name]
            if (existing != null) {
                val prevBinding = scopeData.mutBinding(existing)
                // If we are in the map as Public and Private, then Public wins.
                if (effectiveVis == Visibility.Public) {
                    prevBinding.vis = Visibility.Public
                }
                prevBinding.assignCount = AssignCount.Any
                assign.node.payload = existing
            } else {
                val assignCount = when (inLoop) {
                    InLoop.Yes -> AssignCount.Any
                    InLoop.No -> AssignCount.AtMostOnce
                }
                val (newBindingId, _) = scopeData.newBinding(
                    name,
                    BindingSource.Source(span),
                    effectiveVis,
                    assignCount,
                )
                result[name] = newBindingId
                assign.node.payload = newBindingId
            }
        }

        // fn collect_defines_lvalue(...) — AssignTarget trait impl
        fun collectDefinesLvalue(
            expr: CstAssignTarget,
            inLoop: InLoop,
            scopeData: ModuleScopeData,
            frozenHeap: FrozenHeap,
            result: MutableMap<FrozenStringValue, BindingId>,
        ) {
            expr.node.visitLvalueMut { x ->
                collectAssignIdent(
                    x,
                    inLoop,
                    Visibility.Public,
                    scopeData,
                    frozenHeap,
                    result,
                )
            }
        }
    }

    // Number of module slots I need, a struct holding all scopes, and module bindings.
    // fn exit_module(mut self) -> (u32, ModuleScopeData, SmallMap<...>)
    fun exitModule(): Triple<Int, ModuleScopeData, MutableMap<FrozenStringValue, BindingId>> {
        check(locals.size == 1)
        check(unscopes.isEmpty())
        val scopeId = locals.removeAt(locals.lastIndex)
        check(scopeId == ScopeId.module())
        val scope = scopeData.getScope(scopeId)
        check(scope.parent.isEmpty())
        return Triple(
            module.slotCount(),
            scopeData,
            moduleBindings,
        )
    }

    // fn resolve_idents(&mut self, code: &mut CstStmt)
    @Suppress("UNCHECKED_CAST")
    fun resolveIdents(code: CstStmt) {
        when (val node = code.node) {
            is StmtP.Def<*, *> -> {
                val defP = node.def as DefP<CstPayload, ScopeId>
                resolveIdentsInDef(
                    defP.payload,
                    defP.params as List<CstParameter>,
                    defP.returnType as CstTypeExpr?,
                    defP.body as CstStmt,
                    null,
                )
            }
            is StmtP.Assign<*> -> {
                val assignP = node.assign as io.github.kotlinmania.starlark_kotlin.syntax.ast.AssignP<CstPayload>
                resolveIdentsInAssign(assignP.lhs)
                if (assignP.ty != null) {
                    resolveIdentsInTypeExpr(assignP.ty!!)
                }
                resolveIdentsInExpr(assignP.rhs)
            }
            else -> code.visitChildrenMut { visit ->
                when (visit) {
                    is VisitMut.Stmt -> resolveIdents(visit.stmt)
                    is VisitMut.Expr -> resolveIdentsInExpr(visit.expr)
                }
            }
        }
    }

    // fn resolve_idents_in_assign(&mut self, assign: &mut CstAssignTarget)
    fun resolveIdentsInAssign(assign: CstAssignTarget) {
        assign.visitExprMut { expr -> resolveIdentsInExpr(expr) }
    }

    // fn resolve_idents_in_def(...)
    @Suppress("UNCHECKED_CAST")
    fun resolveIdentsInDef(
        scopeId: ScopeId,
        params: List<CstParameter>,
        ret: CstTypeExpr?,
        bodyStmt: CstStmt?,
        bodyExpr: CstExpr?,
    ) {
        for (param in params) {
            val (_, ty, def) = param.node.splitMut()
            if (ty != null) {
                resolveIdentsInTypeExpr(ty as CstTypeExpr)
            }
            if (def != null) {
                resolveIdentsInExpr(def)
            }
        }
        if (ret != null) {
            resolveIdentsInTypeExpr(ret)
        }

        enterDef(scopeId)
        if (bodyStmt != null) {
            resolveIdents(bodyStmt)
        }
        if (bodyExpr != null) {
            resolveIdentsInExpr(bodyExpr)
        }
        exitDef()
    }

    // fn resolve_idents_in_expr_impl(&mut self, scope: ResolveIdentScope, expr: &mut CstExpr)
    @Suppress("UNCHECKED_CAST")
    fun resolveIdentsInExprImpl(scope: ResolveIdentScope, expr: CstExpr) {
        when (val node = expr.node) {
            is ExprP.Identifier<*, *> -> resolveIdent(scope, node.ident as CstIdent)
            is ExprP.Lambda<*, *> -> {
                val lambdaP = node.lambda as LambdaP<CstPayload, ScopeId>
                resolveIdentsInDef(lambdaP.payload, lambdaP.params as List<CstParameter>, null, null, lambdaP.body)
            }
            is ExprP.ListComprehension<*> -> {
                resolveIdentsInCompr(
                    mutableListOf(node.expr as CstExpr),
                    node.forClause as ForClauseP<CstPayload>,
                    (node.clauses as List<ClauseP<CstPayload>>).toMutableList(),
                )
            }
            is ExprP.DictComprehension<*> -> {
                resolveIdentsInCompr(
                    mutableListOf(node.key as CstExpr, node.value as CstExpr),
                    node.forClause as ForClauseP<CstPayload>,
                    (node.clauses as List<ClauseP<CstPayload>>).toMutableList(),
                )
            }
            else -> expr.visitExprMut { e -> resolveIdentsInExprImpl(scope, e) }
        }
    }

    // fn resolve_idents_in_expr(&mut self, expr: &mut CstExpr)
    fun resolveIdentsInExpr(expr: CstExpr) {
        resolveIdentsInExprImpl(ResolveIdentScope.Any, expr)
    }

    // fn resolve_idents_in_type_expr(&mut self, expr: &mut CstTypeExpr)
    fun resolveIdentsInTypeExpr(expr: CstTypeExpr) {
        resolveIdentsInExprImpl(
            ResolveIdentScope.GlobalForTypeExpression,
            expr.node.expr,
        )
    }

    // fn current_scope_all_visible_names_for_did_you_mean(&self) -> Option<Vec<String>>
    fun currentScopeAllVisibleNamesForDidYouMean(): List<String>? {
        // It is OK to return non-unique identifiers
        val r: MutableList<String> = mutableListOf()
        for (scopeId in locals.asReversed()) {
            val scope = scopeData.getScope(scopeId)
            r.addAll(scope.mp.keys.map { it.asStr() })
        }
        r.addAll(moduleBindings.keys.map { it.asStr() })
        val globalNames = globals.names() ?: return null
        r.addAll(globalNames)
        return r
    }

    // fn variable_not_found_err(&self, ident: &CstIdent) -> EvalException
    fun variableNotFoundErr(ident: CstIdent): EvalException {
        val variants = currentScopeAllVisibleNamesForDidYouMean() ?: emptyList()
        val better = didYouMean(
            ident.node.ident,
            variants,
        )
        return EvalException.new(
            if (better != null) {
                ScopeError.VariableNotFoundDidYouMean(ident.node.ident, better)
            } else {
                ScopeError.VariableNotFound(ident.node.ident)
            },
            ident.span,
            codemap.value,
        )
    }

    // fn resolve_ident(&mut self, scope: ResolveIdentScope, ident: &mut CstIdent)
    fun resolveIdent(scope: ResolveIdentScope, ident: CstIdent) {
        check(ident.node.payload == null)
        val name = frozenHeap.allocStrIntern(ident.node.ident)
        val resolved: ResolvedIdent = when (val found = getName(name)) {
            null -> {
                // Must be a global, since we know all variables
                val v = globals.getGlobal(ident.node.ident)
                if (v == null) {
                    errors.add(variableNotFoundErr(ident))
                    return
                }
                ResolvedIdent.Global(v)
            }
            else -> {
                val (slot, bindingId) = found
                ResolvedIdent.Slot(slot, bindingId)
            }
        }
        when (scope) {
            ResolveIdentScope.Any -> { /* no extra check */ }
            ResolveIdentScope.GlobalForTypeExpression -> when (resolved) {
                is ResolvedIdent.Slot -> when (resolved.slot) {
                    is Slot.Local -> {
                        errors.add(
                            EvalException.new(
                                ScopeError.TypeExpressionGlobalOrBuiltin(ident.node.ident),
                                ident.span,
                                codemap.value,
                            )
                        )
                        return
                    }
                    is Slot.Module -> { /* ok */ }
                }
                is ResolvedIdent.Global -> { /* ok */ }
            }
        }
        ident.node.payload = resolved
    }

    // fn resolve_idents_in_compr(...)
    fun resolveIdentsInCompr(
        exprs: MutableList<CstExpr>,
        firstFor: ForClauseP<CstPayload>,
        clauses: MutableList<ClauseP<CstPayload>>,
    ) {
        // First for is resolved in outer scope
        resolveIdentsInForClause(firstFor)

        enterCompr()

        // Add identifiers to compr scope
        val vars = mutableListOf(firstFor.varTarget)
        for (clause in clauses) {
            if (clause is ClauseP.For) {
                vars.add(clause.forClause.varTarget)
            }
        }
        addCompr(vars)

        // Now resolve idents in compr scope
        for (clause in clauses) {
            when (clause) {
                is ClauseP.For -> resolveIdentsInForClause(clause.forClause)
                is ClauseP.If -> resolveIdentsInExpr(clause.cond)
            }
        }

        // Finally, resolve the item expression
        for (expr in exprs) {
            resolveIdentsInExpr(expr)
        }

        exitCompr()
    }

    // fn resolve_idents_in_for_clause(&mut self, for_clause: &mut ForClauseP<CstPayload>)
    fun resolveIdentsInForClause(forClause: ForClauseP<CstPayload>) {
        resolveIdentsInExpr(forClause.over)
        resolveIdentsInAssign(forClause.varTarget)
    }

    // pub fn enter_def(&mut self, scope_id: ScopeId)
    fun enterDef(scopeId: ScopeId) {
        check(scopeId != ScopeId.module())
        locals.add(scopeId)
    }

    // pub fn exit_def(&mut self) -> &mut ScopeNames
    fun exitDef(): ScopeNames {
        val scopeId = locals.removeAt(locals.lastIndex)
        return scopeData.mutScope(scopeId)
    }

    // fn enter_compr(&mut self)
    fun enterCompr() {
        unscopes.add(Unscope())
    }

    // fn add_compr(...)
    fun addCompr(vars: List<CstAssignTarget>) {
        val scopeId = topScopeId()
        val localBindings: MutableMap<FrozenStringValue, BindingId> = mutableMapOf()
        for (v in vars) {
            collectDefinesLvalue(
                v,
                InLoop.Yes,
                scopeData,
                frozenHeap,
                localBindings,
            )
        }
        for ((name, bindingId) in localBindings) {
            val slot = scopeData.mutScope(scopeId).addScoped(
                name,
                bindingId,
                unscopes.last(),
            )
            val binding = scopeData.mutBinding(bindingId)
            binding.initSlot(Slot.Local(slot), codemap.value)
        }
    }

    // fn exit_compr(&mut self)
    fun exitCompr() {
        scopeData
            .mutScope(topScopeId())
            .unscope(unscopes.removeAt(unscopes.lastIndex))
    }

    // fn get_name(&mut self, name: FrozenStringValue) -> Option<(Slot, BindingId)>
    fun getName(name: FrozenStringValue): Pair<Slot, BindingId>? {
        // look upwards to find the first place the variable occurs
        // then copy that variable downwards
        for (i in (0 until locals.size).reversed()) {
            val found = scopeAtLevel(i).getName(name)
            if (found != null) {
                var (v, bindingId) = found
                if (i + 1 != locals.size) {
                    scopeData.mutBinding(bindingId).captured = Captured.Yes
                }
                for (j in (i + 1) until locals.size) {
                    v = scopeAtLevelMut(j).copyParent(v, bindingId, name)
                }
                return Pair(Slot.Local(v), bindingId)
            }
        }
        val bindingId = moduleBindings[name]
        return if (bindingId != null) {
            val binding = scopeData.mutBinding(bindingId)
            if (locals.size > 1) {
                binding.captured = Captured.Yes
            }
            val slot = binding.resolvedSlot(codemap.value)!!
            check(slot is Slot.Module)
            Pair(slot, bindingId)
        } else {
            null
        }
    }
}

// pub(crate) struct ModuleScopes
internal class ModuleScopes(
    val scopeData: ModuleScopeData,
    val moduleSlotCount: Int,
    val cst: CstStmt,
    /// Number of top-level statements in the module.
    val topLevelStmtCount: Int,
) {
    companion object {
        // pub(crate) fn check_module_err(...)
        fun checkModuleErr(
            module: MutableNames,
            frozenHeap: FrozenHeap,
            loads: Map<String, Interface>,
            stmt: AstStmt,
            globals: ScopeResolverGlobals,
            codemap: FrozenRef<CodeMap>,
            dialect: Dialect,
        ): ModuleScopes {
            val (errors, scopes) = checkModule(module, frozenHeap, loads, stmt, globals, codemap, dialect)
            val firstError = errors.firstOrNull()
            if (firstError != null) {
                throw firstError.intoError()
            }
            return scopes
        }

        // pub(crate) fn check_module(...)
        fun checkModule(
            module: MutableNames,
            frozenHeap: FrozenHeap,
            loads: Map<String, Interface>,
            stmt: AstStmt,
            globals: ScopeResolverGlobals,
            codemap: FrozenRef<CodeMap>,
            dialect: Dialect,
        ): Pair<List<EvalException>, ModuleScopes> {
            val (cst, scope) = ModuleScopeBuilder.enterModule(
                module,
                frozenHeap,
                loads,
                stmt,
                globals,
                codemap,
                dialect,
            )
            val topLevelStmtCount = scope.topLevelStmtCount
            val errors = scope.errors.toList()
            scope.errors.clear()
            val (moduleSlotCount, scopeData, _) = scope.exitModule()
            return Pair(
                errors,
                ModuleScopes(
                    cst = cst,
                    scopeData = scopeData,
                    moduleSlotCount = moduleSlotCount,
                    topLevelStmtCount = topLevelStmtCount,
                ),
            )
        }
    }
}

// struct UnscopeBinding
internal class UnscopeBinding(
    /// Variable mappings in local scope are overwritten by comprehension variables.
    /// When we pop the comprehension scope, we restore the mapping from this value.
    val undo: Pair<LocalSlotIdCapturedOrNot, BindingId>?,
)

// #[derive(Default)]
// struct Unscope(SmallMap<FrozenStringValue, UnscopeBinding>)
internal class Unscope(
    val bindings: MutableMap<FrozenStringValue, UnscopeBinding> = mutableMapOf(),
)

// #[derive(Default, Debug)]
// pub(crate) struct ScopeNames
internal class ScopeNames(
    /// `Some` when scope is initialized. For module scope, the value is zero.
    var paramCount: Int? = null,
    /// Slots this scope uses, including for parameters and `parent`.
    /// Indexed by LocalSlotId, values are variable names.
    val used: MutableList<FrozenStringValue> = mutableListOf(),
    /// The names that are in this scope.
    val mp: MutableMap<FrozenStringValue, Pair<LocalSlotIdCapturedOrNot, BindingId>> = mutableMapOf(),
    /// Slots to copy from the parent.
    val parent: MutableList<CopySlotFromParent> = mutableListOf(),
) {
    // fn set_param_count(&mut self, param_count: u32)
    fun setParamCount(paramCount: Int) {
        check(this.paramCount == null)
        this.paramCount = paramCount
    }

    // pub(crate) fn param_count(&self) -> u32
    fun paramCount(): Int {
        return paramCount ?: error("param_count must be set during analysis")
    }

    // fn copy_parent(...)
    fun copyParent(
        parentSlot: LocalSlotIdCapturedOrNot,
        bindingId: BindingId,
        name: FrozenStringValue,
    ): LocalSlotIdCapturedOrNot {
        check(getName(name) == null) // Or we'll be overwriting our variable
        val res = addName(name, bindingId)
        parent.add(CopySlotFromParent(parent = parentSlot, child = res))
        return res
    }

    // fn next_slot(&mut self, name: FrozenStringValue) -> LocalSlotIdCapturedOrNot
    fun nextSlot(name: FrozenStringValue): LocalSlotIdCapturedOrNot {
        val res = LocalSlotIdCapturedOrNot(used.size.toUInt())
        used.add(name)
        return res
    }

    // fn add_name(...)
    fun addName(
        name: FrozenStringValue,
        bindingId: BindingId,
    ): LocalSlotIdCapturedOrNot {
        val slot = nextSlot(name)
        val old = mp.put(name, Pair(slot, bindingId))
        check(old == null)
        return slot
    }

    // fn add_scoped(...)
    fun addScoped(
        name: FrozenStringValue,
        bindingId: BindingId,
        unscope: Unscope,
    ): LocalSlotIdCapturedOrNot {
        val slot = nextSlot(name)
        val existing = mp[name]
        val undo: Pair<LocalSlotIdCapturedOrNot, BindingId>? = if (existing != null) {
            val old = existing
            mp[name] = Pair(slot, bindingId)
            old
        } else {
            mp[name] = Pair(slot, bindingId)
            null
        }
        check(unscope.bindings.put(name, UnscopeBinding(undo)) == null)
        return slot
    }

    // fn unscope(&mut self, unscope: Unscope)
    fun unscope(unscope: Unscope) {
        for ((name, unscopeBinding) in unscope.bindings) {
            val undo = unscopeBinding.undo
            if (undo == null) {
                mp.remove(name)
            } else {
                mp[name] = undo
            }
        }
    }

    // fn get_name(&self, name: FrozenStringValue) -> Option<(LocalSlotIdCapturedOrNot, BindingId)>
    fun getName(name: FrozenStringValue): Pair<LocalSlotIdCapturedOrNot, BindingId>? {
        return mp[name]
    }
}

// #[derive(Copy, Clone, Dupe, Debug)]
// pub(crate) enum Slot
internal sealed class Slot {
    /// Top-level module scope.
    data class Module(val id: ModuleSlotId) : Slot()
    /// Local scope, always mutable.
    data class Local(val id: LocalSlotIdCapturedOrNot) : Slot()
}

// #[derive(Clone, Copy, Dupe)]
// enum ResolveIdentScope
internal enum class ResolveIdentScope {
    /// Resolving normal identifier.
    Any,
    /// Resolving identifier in type expression.
    GlobalForTypeExpression,
}

/// While performing analysis.
// #[derive(Copy, Clone, Dupe)]
// enum InLoop
internal enum class InLoop {
    /// Current statement has an enclosing loop in the current scope.
    Yes,
    /// Current statement has no enclosing loop in the current scope.
    No,
}

/// Storage of objects referenced by AST.
// #[derive(Default)]
// pub(crate) struct ModuleScopeData
internal class ModuleScopeData(
    /// Bindings by id.
    internal val bindings: MutableList<Binding> = mutableListOf(),
    /// Scopes by id.
    private val scopes: MutableList<ScopeNames> = mutableListOf(),
) {
    // pub(crate) fn new() -> ModuleScopeData
    // (default constructor serves this purpose)

    // pub(crate) fn get_binding(&self, BindingId(id): BindingId) -> &Binding
    fun getBinding(id: BindingId): Binding = bindings[id.id]

    // fn mut_binding(&mut self, BindingId(id): BindingId) -> &mut Binding
    fun mutBinding(id: BindingId): Binding = bindings[id.id]

    // fn new_binding(...) -> (BindingId, &mut Binding)
    fun newBinding(
        name: FrozenStringValue,
        source: BindingSource,
        vis: Visibility,
        assignedCount: AssignCount,
    ): Pair<BindingId, Binding> {
        val bindingId = BindingId(bindings.size)
        val binding = Binding(name, source, vis, assignedCount)
        bindings.add(binding)
        return Pair(bindingId, binding)
    }

    // pub(crate) fn get_scope(&self, ScopeId(id): ScopeId) -> &ScopeNames
    fun getScope(id: ScopeId): ScopeNames = scopes[id.id]

    // pub(crate) fn mut_scope(&mut self, ScopeId(id): ScopeId) -> &mut ScopeNames
    fun mutScope(id: ScopeId): ScopeNames = scopes[id.id]

    // pub(crate) fn new_scope(&mut self) -> (ScopeId, &mut ScopeNames)
    fun newScope(): Pair<ScopeId, ScopeNames> {
        val scopeId = ScopeId(scopes.size)
        val scope = ScopeNames()
        scopes.add(scope)
        return Pair(scopeId, scope)
    }

    /// Get resolved slot for assigning identifier.
    // pub(crate) fn get_assign_ident_slot(...)
    fun getAssignIdentSlot(
        ident: CstAssignIdent,
        codemap: CodeMap,
    ): Pair<Slot, Captured> {
        val bindingId = ident.node.payload ?: error("binding not assigned for ident")
        val binding = getBinding(bindingId)
        val slot = binding.resolvedSlot(codemap)!!
        return Pair(slot, binding.captured)
    }
}

// #[derive(Debug, Eq, PartialEq)]
// pub(crate) enum AssignCount
internal enum class AssignCount {
    /// Variable is assigned at most once during the execution of the scope.
    AtMostOnce,
    /// Variable may be assigned more than once during execution of the scope.
    Any,
}

/// Was a binding captured by nested def or lambda scopes?
// #[derive(Debug, Copy, Clone, Dupe, Eq, PartialEq, VisitSpanMut)]
// pub(crate) enum Captured
internal enum class Captured {
    Yes,
    No,
}

// #[derive(Debug)]
// pub(crate) enum BindingSource
internal sealed class BindingSource {
    /// Variable is defined in the source of the module.
    data class Source(val span: Span) : BindingSource()
    /// Variable came from `Module`, not defined in the source file.
    data object FromModule : BindingSource()
}

/// Binding defines a place for a variable.
///
/// For example, in code `x = 1; x = 2`, there's one binding for name `x`.
///
/// In code `x = 1; def f(): x = 2`, there are two bindings for name `x`.
// #[derive(Debug)]
// pub(crate) struct Binding
internal class Binding(
    val name: FrozenStringValue,
    val source: BindingSource,
    var vis: Visibility,
    var assignCount: AssignCount,
    /// `slot` is `None` when it is not initialized yet.
    /// When analysis is completed, `slot` is always `Some`.
    internal var slot: Slot? = null,
    // Whether a variable defined in a scope gets captured in nested def or lambda scope.
    var captured: Captured = Captured.No,
) {
    // fn span(&self) -> Span
    fun span(): Span = when (source) {
        is BindingSource.Source -> source.span
        is BindingSource.FromModule -> Span.DEFAULT
    }

    /// Get resolved slot after analysis is completed.
    // pub(crate) fn resolved_slot(&self, codemap: &CodeMap) -> Result<Slot, InternalError>
    fun resolvedSlot(codemap: CodeMap): Slot? {
        return slot ?: run {
            // In Rust this returns Err(InternalError), in Kotlin we return null
            null
        }
    }

    /// Initialize the slot during analysis.
    // pub(crate) fn init_slot(&mut self, slot: Slot, codemap: &CodeMap) -> Result<(), InternalError>
    fun initSlot(newSlot: Slot, codemap: CodeMap) {
        check(slot == null) { "slot is already assigned" }
        slot = newSlot
    }
}

/// Id of a binding within current module.
// #[derive(Copy, Clone, Dupe, Debug, Hash, PartialEq, Eq, Ord, PartialOrd)]
// pub(crate) struct BindingId(usize)
data class BindingId(val id: Int) : Comparable<BindingId> {
    override fun compareTo(other: BindingId): Int = id.compareTo(other.id)
}

/// Id of a scope within current module.
// #[derive(Copy, Clone, Dupe, Debug, Eq, PartialEq)]
// pub(crate) struct ScopeId(usize)
internal data class ScopeId(val id: Int) {
    companion object {
        // pub(crate) fn module() -> ScopeId
        fun module(): ScopeId = ScopeId(0)
    }
}

// #[derive(Debug, Clone, Dupe, Copy)]
// pub(crate) enum ResolvedIdent
internal sealed class ResolvedIdent {
    data class Slot(val slot: io.github.kotlinmania.starlark_kotlin.eval.compiler.Slot, val bindingId: BindingId) : ResolvedIdent()
    data class Global(val value: FrozenValue) : ResolvedIdent()
}
