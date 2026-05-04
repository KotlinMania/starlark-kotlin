// port-lint: source eval/compiler/scope.rs
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

/**
 * Compiler scope resolution module.
 *
 * Submodules:
 *  - scope/Payload.kt (payload)
 *  - scope/ScopeResolverGlobals.kt (scopeResolverGlobals)
 *  - scope/Tests.kt (tests)
 */

import io.github.kotlinmania.starlark.codemap.CodeMap
import io.github.kotlinmania.starlark.codemap.Span
import io.github.kotlinmania.starlark.codemap.Spanned
import io.github.kotlinmania.starlark.environment.Module
import io.github.kotlinmania.starlark.environment.ModuleSlotId
import io.github.kotlinmania.starlark.environment.MutableNames
import io.github.kotlinmania.starlark.errors.didYouMean
import io.github.kotlinmania.starlark.typing.InternalError
import io.github.kotlinmania.starlark.eval.compiler.scope.CstPayload
import io.github.kotlinmania.starlark.eval.compiler.scope.ScopeResolverGlobals
import io.github.kotlinmania.starlark.eval.compiler.scope.cstStmtFromAst
import io.github.kotlinmania.starlark.eval.runtime.LocalSlotIdCapturedOrNot
import io.github.kotlinmania.starlark.syntax.ast.AstNoPayload
import io.github.kotlinmania.starlark.syntax.ast.AssignIdentP
import io.github.kotlinmania.starlark.syntax.ast.AssignTargetP
import io.github.kotlinmania.starlark.syntax.ast.ClauseP
import io.github.kotlinmania.starlark.syntax.ast.DefP
import io.github.kotlinmania.starlark.syntax.ast.ExprP
import io.github.kotlinmania.starlark.syntax.ast.ForClauseP
import io.github.kotlinmania.starlark.syntax.ast.ForP
import io.github.kotlinmania.starlark.syntax.ast.LambdaP
import io.github.kotlinmania.starlark.syntax.ast.IdentP
import io.github.kotlinmania.starlark.syntax.ast.LoadArgP
import io.github.kotlinmania.starlark.syntax.ast.LoadP
import io.github.kotlinmania.starlark.syntax.ast.ParameterP
import io.github.kotlinmania.starlark.syntax.ast.StmtP
import io.github.kotlinmania.starlark.syntax.ast.TypeExprP
import io.github.kotlinmania.starlark.eval.compiler.scope.CstTypeExprPayload
import io.github.kotlinmania.starlark.syntax.ast.Visibility
import io.github.kotlinmania.starlark.syntax.dialect.Dialect
import io.github.kotlinmania.starlarksyntax.evalexception.EvalException
import io.github.kotlinmania.starlark.typing.Interface
import io.github.kotlinmania.starlark.typing.StarlarkError
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.FrozenRef
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.typed.FrozenStringValue

// ---------------------------------------------------------------------------
// Payload-narrowing helpers
//
// CST AST nodes carry an erased payload type parameter (`*`) on Spanned/IdentP/
// AssignIdentP/DefP/LambdaP. The CST flavor binds them to concrete types
// (BindingId? for assign idents, ResolvedIdent? for read idents, ScopeId for
// def/lambda). Each helper narrows a single shape — keep the casts narrow so
// they're auditable instead of suppressing whole functions.
// ---------------------------------------------------------------------------

private fun assignIdentAsBinding(
    ident: Spanned<AssignIdentP<CstPayload, *>>,
): Spanned<AssignIdentP<CstPayload, BindingId?>> =
    ident as Spanned<AssignIdentP<CstPayload, BindingId?>>

private fun identAsResolved(
    ident: Spanned<IdentP<CstPayload, *>>,
): Spanned<IdentP<CstPayload, ResolvedIdent?>> =
    ident as Spanned<IdentP<CstPayload, ResolvedIdent?>>

private fun defAsScopeId(def: DefP<CstPayload, *>): DefP<CstPayload, ScopeId> =
    def as DefP<CstPayload, ScopeId>

private fun lambdaAsScopeId(lambda: LambdaP<CstPayload, *>): LambdaP<CstPayload, ScopeId> =
    lambda as LambdaP<CstPayload, ScopeId>

// ---------------------------------------------------------------------------
// Visitor infrastructure (port of starlarkSyntax::syntax::uniplate)
// ---------------------------------------------------------------------------

/** VisitMut — mutable visitor over CST children. */
internal sealed class VisitMut {
    class Stmt(val stmt: Spanned<StmtP<CstPayload>>) : VisitMut()
    class Expr(val expr: Spanned<ExprP<CstPayload>>) : VisitMut()
}

/**
 * Flatten top-level Statements into a list.
 * Port of `topLevelStmtsMut`.
 */
internal fun topLevelStmtsMut(top: Spanned<StmtP<CstPayload>>): MutableList<Spanned<StmtP<CstPayload>>> {
    val result = mutableListOf<Spanned<StmtP<CstPayload>>>()
    fun flatten(stmt: Spanned<StmtP<CstPayload>>) {
        when (val node = stmt.node) {
            is StmtP.Statements -> {
                for (s in node.stmts) {
                    flatten(s)
                }
            }
            else -> result.add(stmt)
        }
    }
    flatten(top)
    return result
}

/**
 * Extension: visit children of a StmtP node (mutable visitor).
 * Port of `StmtP::visitChildrenMut`.
 */
internal fun Spanned<StmtP<CstPayload>>.visitChildrenMut(f: (VisitMut) -> Unit) {
    when (val node = this.node) {
        is StmtP.Statements -> {
            node.stmts.forEach { f(VisitMut.Stmt(it)) }
        }
        is StmtP.If -> {
            f(VisitMut.Expr(node.cond))
            f(VisitMut.Stmt(node.suite))
        }
        is StmtP.IfElse -> {
            f(VisitMut.Expr(node.cond))
            f(VisitMut.Stmt(node.suite1))
            f(VisitMut.Stmt(node.suite2))
        }
        is StmtP.Def<CstPayload, *> -> {
            val defP = node.def
            for (p in defP.params) {
                p.node.visitExprMut { f(VisitMut.Expr(it)) }
            }
            defP.returnType?.let { f(VisitMut.Expr(it.node.expr)) }
            f(VisitMut.Stmt(defP.body))
        }
        is StmtP.For -> {
            val forP = node.forStmt
            forP.varTarget.visitExprMut { f(VisitMut.Expr(it)) }
            f(VisitMut.Expr(forP.over))
            f(VisitMut.Stmt(forP.body))
        }
        is StmtP.Return -> {
            node.expr?.let { f(VisitMut.Expr(it)) }
        }
        is StmtP.Expression -> {
            f(VisitMut.Expr(node.expr))
        }
        is StmtP.Assign -> {
            val assignP = node.assign
            assignP.lhs.visitExprMut { f(VisitMut.Expr(it)) }
            assignP.ty?.let { f(VisitMut.Expr(it.node.expr)) }
            f(VisitMut.Expr(assignP.rhs))
        }
        is StmtP.AssignModify -> {
            node.lhs.visitExprMut { f(VisitMut.Expr(it)) }
            f(VisitMut.Expr(node.rhs))
        }
        is StmtP.Load<CstPayload, *> -> { /* no children */ }
        is StmtP.Break,
        is StmtP.Continue,
        is StmtP.Pass -> { /* no children */ }
    }
}

/**
 * Extension: visit only stmt children of StmtP (mutable).
 * Port of `StmtP::visitStmtMut`.
 */
internal fun StmtP<CstPayload>.visitStmtMut(f: (Spanned<StmtP<CstPayload>>) -> Unit) {
    when (this) {
        is StmtP.Statements -> stmts.forEach(f)
        is StmtP.If -> f(suite)
        is StmtP.IfElse -> {
            f(suite1)
            f(suite2)
        }
        is StmtP.Def<CstPayload, *> -> f(def.body)
        is StmtP.For -> f(forStmt.body)
        else -> { /* no stmt children */ }
    }
}

/**
 * Extension: visit expr children (mutable) of an ExprP.
 * Port of `ExprP::visitExprMut` (on the inner expression node, not the Spanned wrapper).
 */
internal fun Spanned<ExprP<CstPayload>>.visitExprMut(f: (Spanned<ExprP<CstPayload>>) -> Unit) {
    when (val node = this.node) {
        is ExprP.Tuple -> node.elements.forEach(f)
        is ExprP.Dot -> f(node.expr)
        is ExprP.Call -> {
            f(node.expr)
            for (arg in node.args.args) {
                f(arg.node.expr())
            }
        }
        is ExprP.Index -> {
            f(node.expr)
            f(node.index)
        }
        is ExprP.Slice -> {
            f(node.expr)
            node.start?.let(f)
            node.stop?.let(f)
            node.step?.let(f)
        }
        is ExprP.Not -> f(node.expr)
        is ExprP.Minus -> f(node.expr)
        is ExprP.Plus -> f(node.expr)
        is ExprP.BitNot -> f(node.expr)
        is ExprP.Op -> {
            f(node.lhs)
            f(node.rhs)
        }
        is ExprP.If -> {
            f(node.cond)
            f(node.v1)
            f(node.v2)
        }
        is ExprP.ListExpr -> node.elements.forEach(f)
        is ExprP.Dict -> {
            for ((k, v) in node.elements) {
                f(k); f(v)
            }
        }
        is ExprP.ListComprehension -> {
            node.forClause.visitExprMut(f)
            node.clauses.forEach { it.visitExprMut(f) }
            f(node.expr)
        }
        is ExprP.DictComprehension -> {
            node.forClause.visitExprMut(f)
            node.clauses.forEach { it.visitExprMut(f) }
            f(node.key)
            f(node.value)
        }
        is ExprP.Lambda<CstPayload, *> -> {
            val lambdaP = node.lambda
            for (p in lambdaP.params) {
                p.node.visitExprMut(f)
            }
            f(lambdaP.body)
        }
        is ExprP.Identifier<CstPayload, *>,
        is ExprP.Literal,
        is ExprP.FString -> { /* no expr children */ }
        is ExprP.Index2 -> {
            f(node.expr)
            f(node.index0)
            f(node.index1)
        }
    }
}

/**
 * Extension: visit expr children of an AssignTargetP.
 * Port of `AssignTargetP::visitExprMut`.
 */
internal fun Spanned<AssignTargetP<CstPayload>>.visitExprMut(f: (Spanned<ExprP<CstPayload>>) -> Unit) {
    when (val node = this.node) {
        is AssignTargetP.Tuple -> {
            for (elem in node.elements) {
                elem.visitExprMut(f)
            }
        }
        is AssignTargetP.Index -> {
            f(node.expr)
            f(node.index)
        }
        is AssignTargetP.Dot -> f(node.expr)
        is AssignTargetP.Identifier<CstPayload, *> -> { /* no expr children */ }
    }
}

/**
 * Extension: visit lvalue identifiers in an AssignTargetP.
 * Port of `AssignTargetP::visitLvalueMut`.
 */
internal fun AssignTargetP<CstPayload>.visitLvalueMut(f: (Spanned<AssignIdentP<CstPayload, *>>) -> Unit) {
    when (this) {
        is AssignTargetP.Identifier<CstPayload, *> -> f(ident)
        is AssignTargetP.Tuple -> {
            for (elem in elements) {
                elem.node.visitLvalueMut(f)
            }
        }
        else -> { /* Index, Dot have no lvalues */ }
    }
}

/**
 * Extension: visit expr children of a ParameterP.
 * Port of `ParameterP::visitExprMut`.
 */
internal fun ParameterP<CstPayload>.visitExprMut(f: (Spanned<ExprP<CstPayload>>) -> Unit) {
    val (_, ty, def) = this.splitMut()
    ty?.let { f(it.node.expr) }
    def?.let { f(it) }
}

/**
 * Extension: split a ParameterP into (name?, type?, default?).
 * Port of `ParameterP::splitMut`.
 */
internal fun ParameterP<CstPayload>.splitMut(): Triple<Spanned<AssignIdentP<CstPayload, *>>?, Spanned<TypeExprP<CstPayload, *>>?, Spanned<ExprP<CstPayload>>?> {
    return when (this) {
        is ParameterP.Normal -> Triple(name, typ, defaultVal)
        is ParameterP.Args -> Triple(name, typ, null)
        is ParameterP.KwArgs -> Triple(name, typ, null)
        is ParameterP.NoArgs,
        is ParameterP.Slash -> Triple(null, null, null)
    }
}

/** Extension: visit expr children of a ForClauseP. */
internal fun ForClauseP<CstPayload>.visitExprMut(f: (Spanned<ExprP<CstPayload>>) -> Unit) {
    varTarget.visitExprMut(f)
    f(over)
}

/** Extension: visit expr children of a ClauseP. */
internal fun ClauseP<CstPayload>.visitExprMut(f: (Spanned<ExprP<CstPayload>>) -> Unit) {
    when (this) {
        is ClauseP.For -> forClause.visitExprMut(f)
        is ClauseP.If -> f(cond)
    }
}

internal sealed class ScopeError(message: String) : StarlarkError(message) {
    class VariableNotFound(val name: String) : ScopeError("Variable `$name` not found")
    class VariableNotFoundDidYouMean(val name: String, val suggestion: String) : ScopeError("Variable `$name` not found, did you mean `$suggestion`?")
    class TypeExpressionGlobalOrBuiltin(val name: String) : ScopeError("Identifiers in type expressions can only refer globals or builtins: `$name`")
}

/** All scopes and bindings in a module. */
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
    fun topScopeId(): ScopeId = locals.last()

    fun scopeAtLevel(level: Int): ScopeNames {
        val scopeId = locals[level]
        return scopeData.getScope(scopeId)
    }

    fun scopeAtLevelMut(level: Int): ScopeNames {
        val scopeId = locals[level]
        return scopeData.mutScope(scopeId)
    }

    companion object {
        /**
         * Resolve symbols in a module.
         *
         * Checks all the symbols are resolved to locals/globals/captured/etc.
         * Do not check types yet. But validate type expressions.
         *
         * This function does not fail, errors are stored in the `errors` field.
         */
        fun enterModule(
            module: MutableNames,
            frozenHeap: FrozenHeap,
            loads: Map<String, Interface>,
            stmt: Spanned<StmtP<AstNoPayload>>,
            globals: ScopeResolverGlobals,
            codemap: FrozenRef<CodeMap>,
            dialect: Dialect,
        ): Pair<Spanned<StmtP<CstPayload>>, ModuleScopeBuilder> {
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

        fun collectDefinesInDef(
            scopeData: ModuleScopeData,
            scopeId: ScopeId,
            params: List<Spanned<ParameterP<CstPayload>>>,
            body: Spanned<StmtP<CstPayload>>?,
            frozenHeap: FrozenHeap,
            dialect: Dialect,
            codemap: CodeMap,
        ) {
            val paramIdents: MutableList<Spanned<AssignIdentP<CstPayload, *>>> = mutableListOf()
            for (p in params) {
                val ident = p.node.ident()
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

        fun collectDefinesRecursively(
            scopeData: ModuleScopeData,
            code: Spanned<StmtP<CstPayload>>,
            frozenHeap: FrozenHeap,
            dialect: Dialect,
            codemap: CodeMap,
        ) {
            val node = code.node
            if (node is StmtP.Def<CstPayload, *>) {
                val defP = node.def
                collectDefinesInDef(
                    scopeData,
                    defP.payload as ScopeId,
                    defP.params,
                    defP.body,
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

        fun collectDefinesRecursivelyInExpr(
            scopeData: ModuleScopeData,
            code: Spanned<ExprP<CstPayload>>,
            frozenHeap: FrozenHeap,
            dialect: Dialect,
            codemap: CodeMap,
        ) {
            val node = code.node
            if (node is ExprP.Lambda<CstPayload, *>) {
                val lambdaP = node.lambda
                collectDefinesInDef(
                    scopeData,
                    lambdaP.payload as ScopeId,
                    lambdaP.params,
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

        fun collectDefines(
            stmt: Spanned<StmtP<CstPayload>>,
            inLoop: InLoop,
            scopeData: ModuleScopeData,
            frozenHeap: FrozenHeap,
            result: MutableMap<FrozenStringValue, BindingId>,
            dialect: Dialect,
        ) {
            when (val node = stmt.node) {
                is StmtP.Assign<CstPayload> -> {
                    collectDefinesLvalue(
                        node.assign.lhs,
                        inLoop,
                        scopeData,
                        frozenHeap,
                        result,
                    )
                }
                is StmtP.AssignModify<CstPayload> -> {
                    collectDefinesLvalue(
                        node.lhs,
                        inLoop,
                        scopeData,
                        frozenHeap,
                        result,
                    )
                }
                is StmtP.For<CstPayload> -> {
                    val forP = node.forStmt
                    collectDefinesLvalue(
                        forP.varTarget,
                        InLoop.Yes,
                        scopeData,
                        frozenHeap,
                        result,
                    )
                    collectDefines(forP.body, InLoop.Yes, scopeData, frozenHeap, result, dialect)
                }
                is StmtP.Def<CstPayload, *> -> {
                    collectAssignIdent(
                        assignIdentAsBinding(node.def.name),
                        inLoop,
                        Visibility.Public,
                        scopeData,
                        frozenHeap,
                        result,
                    )
                }
                is StmtP.Load<CstPayload, *> -> {
                    val vis = if (dialect.enableLoadReexport) Visibility.Public else Visibility.Private
                    val loadP = node.loadStmt
                    for (loadArg in loadP.args) {
                        var argVis = vis
                        if (Module.defaultVisibility(loadArg.local.node.ident) == Visibility.Private) {
                            argVis = Visibility.Private
                        }
                        collectAssignIdent(
                            assignIdentAsBinding(loadArg.local),
                            inLoop,
                            argVis,
                            scopeData,
                            frozenHeap,
                            result,
                        )
                    }
                }
                else -> {
                    node.visitStmtMut { x ->
                        collectDefines(x, inLoop, scopeData, frozenHeap, result, dialect)
                    }
                }
            }
        }

        fun collectAssignIdent(
            assign: Spanned<AssignIdentP<CstPayload, BindingId?>>,
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

        fun collectDefinesLvalue(
            expr: Spanned<AssignTargetP<CstPayload>>,
            inLoop: InLoop,
            scopeData: ModuleScopeData,
            frozenHeap: FrozenHeap,
            result: MutableMap<FrozenStringValue, BindingId>,
        ) {
            expr.node.visitLvalueMut { x ->
                collectAssignIdent(
                    assignIdentAsBinding(x),
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

    fun resolveIdents(code: Spanned<StmtP<CstPayload>>) {
        when (val node = code.node) {
            is StmtP.Def<CstPayload, *> -> {
                val defP = defAsScopeId(node.def)
                resolveIdentsInDef(
                    defP.payload,
                    defP.params,
                    defP.returnType,
                    defP.body,
                    null,
                )
            }
            is StmtP.Assign -> {
                val assignP = node.assign
                resolveIdentsInAssign(assignP.lhs)
                if (assignP.ty != null) {
                    resolveIdentsInTypeExpr(assignP.ty)
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

    fun resolveIdentsInAssign(assign: Spanned<AssignTargetP<CstPayload>>) {
        assign.visitExprMut { expr -> resolveIdentsInExpr(expr) }
    }

    fun resolveIdentsInDef(
        scopeId: ScopeId,
        params: List<Spanned<ParameterP<CstPayload>>>,
        ret: Spanned<TypeExprP<CstPayload, *>>?,
        bodyStmt: Spanned<StmtP<CstPayload>>?,
        bodyExpr: Spanned<ExprP<CstPayload>>?,
    ) {
        for (param in params) {
            when (val p = param.node) {
                is ParameterP.Normal -> {
                    p.typ?.let { resolveIdentsInTypeExpr(it) }
                    p.defaultVal?.let { resolveIdentsInExpr(it) }
                }
                is ParameterP.Args -> {
                    p.typ?.let { resolveIdentsInTypeExpr(it) }
                }
                is ParameterP.KwArgs -> {
                    p.typ?.let { resolveIdentsInTypeExpr(it) }
                }
                is ParameterP.NoArgs, is ParameterP.Slash -> {}
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

    fun resolveIdentsInExprImpl(scope: ResolveIdentScope, expr: Spanned<ExprP<CstPayload>>) {
        when (val node = expr.node) {
            is ExprP.Identifier<CstPayload, *> -> resolveIdent(scope, identAsResolved(node.ident))
            is ExprP.Lambda<CstPayload, *> -> {
                val lambdaP = lambdaAsScopeId(node.lambda)
                resolveIdentsInDef(
                    lambdaP.payload,
                    lambdaP.params,
                    null,
                    null,
                    lambdaP.body,
                )
            }
            is ExprP.ListComprehension -> {
                resolveIdentsInCompr(
                    mutableListOf(node.expr),
                    node.forClause,
                    node.clauses.toMutableList(),
                )
            }
            is ExprP.DictComprehension -> {
                resolveIdentsInCompr(
                    mutableListOf(node.key, node.value),
                    node.forClause,
                    node.clauses.toMutableList(),
                )
            }
            else -> expr.visitExprMut { e -> resolveIdentsInExprImpl(scope, e) }
        }
    }

    fun resolveIdentsInExpr(expr: Spanned<ExprP<CstPayload>>) {
        resolveIdentsInExprImpl(ResolveIdentScope.Any, expr)
    }

    fun resolveIdentsInTypeExpr(expr: Spanned<TypeExprP<CstPayload, *>>) {
        resolveIdentsInExprImpl(
            ResolveIdentScope.GlobalForTypeExpression,
            expr.node.expr,
        )
    }

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

    fun variableNotFoundErr(ident: Spanned<IdentP<CstPayload, ResolvedIdent?>>): EvalException {
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

    fun resolveIdent(scope: ResolveIdentScope, ident: Spanned<IdentP<CstPayload, ResolvedIdent?>>) {
        check(ident.node.payload == null) { "resolveIdent: ident '${ident.node.ident}' already has payload=${ident.node.payload}" }
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

    fun resolveIdentsInCompr(
        exprs: MutableList<Spanned<ExprP<CstPayload>>>,
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

    fun resolveIdentsInForClause(forClause: ForClauseP<CstPayload>) {
        resolveIdentsInExpr(forClause.over)
        resolveIdentsInAssign(forClause.varTarget)
    }

    fun enterDef(scopeId: ScopeId) {
        check(scopeId != ScopeId.module())
        locals.add(scopeId)
    }

    fun exitDef(): ScopeNames {
        val scopeId = locals.removeAt(locals.lastIndex)
        return scopeData.mutScope(scopeId)
    }

    fun enterCompr() {
        unscopes.add(Unscope())
    }

    fun addCompr(vars: List<Spanned<AssignTargetP<CstPayload>>>) {
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

    fun exitCompr() {
        scopeData
            .mutScope(topScopeId())
            .unscope(unscopes.removeAt(unscopes.lastIndex))
    }

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
            val slot = binding.resolvedSlot(codemap.value)
            check(slot is Slot.Module)
            Pair(slot, bindingId)
        } else {
            null
        }
    }
}

internal class ModuleScopes(
    val scopeData: ModuleScopeData,
    val moduleSlotCount: Int,
    val cst: Spanned<StmtP<CstPayload>>,
    /** Number of top-level statements in the module. */
    val topLevelStmtCount: Int,
) {
    companion object {
        fun checkModuleErr(
            module: MutableNames,
            frozenHeap: FrozenHeap,
            loads: Map<String, Interface>,
            stmt: Spanned<StmtP<AstNoPayload>>,
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

        fun checkModule(
            module: MutableNames,
            frozenHeap: FrozenHeap,
            loads: Map<String, Interface>,
            stmt: Spanned<StmtP<AstNoPayload>>,
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

internal class UnscopeBinding(
    /**
     * Variable mappings in local scope are overwritten by comprehension variables.
     * When we pop the comprehension scope, we restore the mapping from this value.
     */
    val undo: Pair<LocalSlotIdCapturedOrNot, BindingId>?,
)

internal class Unscope(
    val bindings: MutableMap<FrozenStringValue, UnscopeBinding> = mutableMapOf(),
)

internal class ScopeNames(
    /** `Some` when scope is initialized. For module scope, the value is zero. */
    var paramCount: Int? = null,
    /**
     * Slots this scope uses, including for parameters and `parent`.
     * Indexed by LocalSlotId, values are variable names.
     */
    val used: MutableList<FrozenStringValue> = mutableListOf(),
    /** The names that are in this scope. */
    val mp: MutableMap<FrozenStringValue, Pair<LocalSlotIdCapturedOrNot, BindingId>> = mutableMapOf(),
    /** Slots to copy from the parent. */
    val parent: MutableList<CopySlotFromParent> = mutableListOf(),
) {
    fun setParamCount(paramCount: Int) {
        check(this.paramCount == null)
        this.paramCount = paramCount
    }

    fun paramCount(): Int {
        return paramCount ?: error("param_count must be set during analysis")
    }

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

    fun nextSlot(name: FrozenStringValue): LocalSlotIdCapturedOrNot {
        val res = LocalSlotIdCapturedOrNot(used.size.toUInt())
        used.add(name)
        return res
    }

    fun addName(
        name: FrozenStringValue,
        bindingId: BindingId,
    ): LocalSlotIdCapturedOrNot {
        val slot = nextSlot(name)
        val old = mp.put(name, Pair(slot, bindingId))
        check(old == null)
        return slot
    }

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

    fun getName(name: FrozenStringValue): Pair<LocalSlotIdCapturedOrNot, BindingId>? {
        return mp[name]
    }
}

internal sealed class Slot {
    /** Top-level module scope. */
    data class Module(val id: ModuleSlotId) : Slot()
    /** Local scope, always mutable. */
    data class Local(val id: LocalSlotIdCapturedOrNot) : Slot()
}

internal enum class ResolveIdentScope {
    /** Resolving normal identifier. */
    Any,
    /** Resolving identifier in type expression. */
    GlobalForTypeExpression,
}

/** While performing analysis. */
internal enum class InLoop {
    /** Current statement has an enclosing loop in the current scope. */
    Yes,
    /** Current statement has no enclosing loop in the current scope. */
    No,
}

/** Storage of objects referenced by AST. */
internal class ModuleScopeData(
    /** Bindings by id. */
    internal val bindings: MutableList<Binding> = mutableListOf(),
    /** Scopes by id. */
    private val scopes: MutableList<ScopeNames> = mutableListOf(),
) {
    // (default constructor serves this purpose)

    fun getBinding(id: BindingId): Binding = bindings[id.id]

    fun mutBinding(id: BindingId): Binding = bindings[id.id]

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

    fun getScope(id: ScopeId): ScopeNames = scopes[id.id]

    fun mutScope(id: ScopeId): ScopeNames = scopes[id.id]

    fun newScope(): Pair<ScopeId, ScopeNames> {
        val scopeId = ScopeId(scopes.size)
        val scope = ScopeNames()
        scopes.add(scope)
        return Pair(scopeId, scope)
    }

    /** Get resolved slot for assigning identifier. */
    fun getAssignIdentSlot(
        ident: Spanned<AssignIdentP<CstPayload, *>>,
        codemap: CodeMap,
    ): Pair<Slot, Captured> {
        val bindingId = ident.node.payload as? BindingId ?: error("binding not assigned for ident")
        val binding = getBinding(bindingId)
        val slot = binding.resolvedSlot(codemap)
        return Pair(slot, binding.captured)
    }
}

internal enum class AssignCount {
    /** Variable is assigned at most once during the execution of the scope. */
    AtMostOnce,
    /** Variable may be assigned more than once during execution of the scope. */
    Any,
}

/** Was a binding captured by nested def or lambda scopes? */
internal enum class Captured {
    Yes,
    No,
}

internal sealed class BindingSource {
    /** Variable is defined in the source of the module. */
    data class Source(val span: Span) : BindingSource()
    /** Variable came from `Module`, not defined in the source file. */
    data object FromModule : BindingSource()
}

/**
 * Binding defines a place for a variable.
 *
 * For example, in code `x = 1; x = 2`, there's one binding for name `x`.
 *
 * In code `x = 1; def f(): x = 2`, there are two bindings for name `x`.
 */
internal class Binding(
    val name: FrozenStringValue,
    val source: BindingSource,
    var vis: Visibility,
    var assignCount: AssignCount,
    /**
     * `slot` is `None` when it is not initialized yet.
     * When analysis is completed, `slot` is always `Some`.
     */
    internal var slot: Slot? = null,
    // Whether a variable defined in a scope gets captured in nested def or lambda scope.
    var captured: Captured = Captured.No,
) {
    fun span(): Span = when (source) {
        is BindingSource.Source -> source.span
        is BindingSource.FromModule -> Span.DEFAULT
    }

    /** Get resolved slot after analysis is completed. */
    fun resolvedSlot(codemap: CodeMap): Slot {
        return slot ?: throw InternalError.msg(
            "slot is not resolved",
            span(),
            codemap,
        )
    }

    /** Initialize the slot during analysis. */
    fun initSlot(newSlot: Slot, codemap: CodeMap) {
        if (slot != null) {
            throw InternalError.msg(
                "slot is already assigned",
                span(),
                codemap,
            )
        }
        slot = newSlot
    }
}

/** Id of a binding within current module. */
data class BindingId(val id: Int) : Comparable<BindingId> {
    override fun compareTo(other: BindingId): Int = id.compareTo(other.id)
}

/** Id of a scope within current module. */
internal data class ScopeId(val id: Int) {
    companion object {
        fun module(): ScopeId = ScopeId(0)
    }
}

internal sealed class ResolvedIdent {
    data class Slot(val slot: io.github.kotlinmania.starlark.eval.compiler.Slot, val bindingId: BindingId) : ResolvedIdent()
    data class Global(val value: FrozenValue) : ResolvedIdent()
}
