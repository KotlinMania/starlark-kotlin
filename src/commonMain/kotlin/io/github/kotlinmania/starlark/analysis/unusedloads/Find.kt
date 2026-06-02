// port-lint: source src/analysis/unused_loads/find.rs
package io.github.kotlinmania.starlark.analysis.unusedloads

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

import io.github.kotlinmania.starlark.codemap.CodeMap
import io.github.kotlinmania.starlark.codemap.FileSpan
import io.github.kotlinmania.starlark.codemap.Spanned
import io.github.kotlinmania.starlark.environment.MutableNames
import io.github.kotlinmania.starlark.eval.compiler.BindingId
import io.github.kotlinmania.starlark.eval.compiler.ModuleScopes
import io.github.kotlinmania.starlark.eval.compiler.ResolvedIdent
import io.github.kotlinmania.starlark.eval.compiler.Slot
import io.github.kotlinmania.starlark.eval.compiler.scope.CstIdent
import io.github.kotlinmania.starlark.eval.compiler.scope.CstStmt
import io.github.kotlinmania.starlark.eval.compiler.scope.ScopeResolverGlobals
import io.github.kotlinmania.starlark.eval.compiler.topLevelStmtsMut
import io.github.kotlinmania.starlark.syntax.AstModule
import io.github.kotlinmania.starlark.syntax.ast.AstPayload
import io.github.kotlinmania.starlark.syntax.ast.ExprP
import io.github.kotlinmania.starlark.syntax.ast.IdentP
import io.github.kotlinmania.starlark.syntax.ast.LoadArgP
import io.github.kotlinmania.starlark.syntax.ast.LoadP
import io.github.kotlinmania.starlark.syntax.ast.StmtP
import io.github.kotlinmania.starlark.syntax.dialect.Dialect
import io.github.kotlinmania.starlark.values.FrozenRef
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.types.allocAny

/** Unused load statement. */
internal class UnusedLoad(
    /** Location of the statement (i.e. position of `load` keyword). */
    val load: Spanned<LoadP<*, *>>,
    /** Unused local names, e. g. `x` in `load("foo", x="y")`. */
    val unusedArgs: List<LoadArgP<*, *>>,
) {
    /** If the whole `load` statement is unused. */
    fun allUnused(): Boolean = unusedArgs.size == load.node.args.size
}

/** Check if there are `@unused` markers on the lines with the given span. */
private fun hasUnusedMarkerInRange(span: FileSpan): Boolean {
    val beginLine = span.file.findLine(span.span.begin)
    val endLine = span.file.findLine(span.span.end)
    for (lineNo in beginLine..endLine) {
        val line = span.file.sourceLine(lineNo)
        if (line.endsWith("@unused") || line.contains("@unused ")) {
            return true
        }
    }
    return false
}

/**
 * Visit all identifiers in read position recursively within a CstStmt.
 */
private fun CstStmt.visitIdent(f: (CstIdent) -> Unit) {
    fun Spanned<IdentP<*, *>>.toCstIdent(): CstIdent {
        val resolvedPayload: ResolvedIdent? =
            when (val payload = node.payload) {
                null -> null
                is ResolvedIdent -> payload
                else -> throw IllegalStateException("identifier payload is not resolved")
            }
        return Spanned(IdentP(ident = node.ident, payload = resolvedPayload), span = span)
    }

    fun visitExprIdent(expr: Spanned<ExprP<out AstPayload>>) {
        when (val e = expr.node) {
            is ExprP.Identifier<*, *> -> f(e.ident.toCstIdent())
            is ExprP.Tuple<*> -> e.elements.forEach { visitExprIdent(it) }
            is ExprP.ListExpr<*> -> e.elements.forEach { visitExprIdent(it) }
            is ExprP.Dict<*> ->
                e.elements.forEach { (k, v) ->
                    visitExprIdent(k)
                    visitExprIdent(v)
                }
            is ExprP.If<*> -> {
                visitExprIdent(e.cond)
                visitExprIdent(e.v1)
                visitExprIdent(e.v2)
            }
            is ExprP.Dot<*> -> visitExprIdent(e.expr)
            is ExprP.Call<*> -> {
                visitExprIdent(e.expr)
                e.args.args.forEach { arg -> visitExprIdent(arg.node.expr()) }
            }
            is ExprP.Index<*> -> {
                visitExprIdent(e.expr)
                visitExprIdent(e.index)
            }
            is ExprP.Index2<*> -> {
                visitExprIdent(e.expr)
                visitExprIdent(e.index0)
                visitExprIdent(e.index1)
            }
            is ExprP.Slice<*> -> {
                visitExprIdent(e.expr)
                e.start?.let { visitExprIdent(it) }
                e.stop?.let { visitExprIdent(it) }
                e.step?.let { visitExprIdent(it) }
            }
            is ExprP.Not<*> -> visitExprIdent(e.expr)
            is ExprP.Minus<*> -> visitExprIdent(e.expr)
            is ExprP.Plus<*> -> visitExprIdent(e.expr)
            is ExprP.BitNot<*> -> visitExprIdent(e.expr)
            is ExprP.Op<*> -> {
                visitExprIdent(e.lhs)
                visitExprIdent(e.rhs)
            }
            is ExprP.ListComprehension<*> -> {
                visitExprIdent(e.expr)
            }
            is ExprP.DictComprehension<*> -> {
                visitExprIdent(e.key)
                visitExprIdent(e.value)
            }
            is ExprP.FString<*> -> {
                e.fstring.node.expressions
                    .forEach { visitExprIdent(it) }
            }
            is ExprP.Lambda<*, *> -> {
                visitExprIdent(e.lambda.body)
            }
            is ExprP.Literal<*> -> { /* no identifiers */ }
        }
    }

    fun visitStmt(stmt: Spanned<StmtP<out AstPayload>>) {
        when (val s = stmt.node) {
            is StmtP.Statements<*> -> s.stmts.forEach { visitStmt(it) }
            is StmtP.Expression<*> -> visitExprIdent(s.expr)
            is StmtP.Return<*> -> s.expr?.let { visitExprIdent(it) }
            is StmtP.Assign<*> -> {
                visitExprIdent(s.assign.rhs)
            }
            is StmtP.AssignModify<*> -> {
                visitExprIdent(s.rhs)
            }
            is StmtP.If<*> -> {
                visitExprIdent(s.cond)
                visitStmt(s.suite)
            }
            is StmtP.IfElse<*> -> {
                visitExprIdent(s.cond)
                visitStmt(s.suite1)
                visitStmt(s.suite2)
            }
            is StmtP.For<*> -> {
                visitExprIdent(s.forStmt.over)
                visitStmt(s.forStmt.body)
            }
            is StmtP.Def<*, *> -> {
                visitStmt(s.def.body)
            }
            is StmtP.Load<*, *> -> { /* no identifiers in read position */ }
            is StmtP.Break<*>,
            is StmtP.Continue<*>,
            is StmtP.Pass<*>,
            -> { /* no expressions */ }
        }
    }

    visitStmt(this)
}

/** Parse the module and find unused loads. */
internal fun findUnusedLoads(
    name: String,
    program: String,
): Result<Pair<FrozenRef<CodeMap>, List<UnusedLoad>>> {
    val module =
        AstModule
            .parse(name, program, Dialect.AllOptionsInternal)
            .getOrElse { return Result.failure(it) }
    val names = MutableNames.new()
    val heap = FrozenHeap.new()
    val (codemap, statement, dialect, _) = module.intoParts()
    val codemapRef = heap.allocAny(codemap)
    val moduleScopes =
        runCatching {
            ModuleScopes.checkModuleErr(
                names,
                heap,
                emptyMap(),
                statement,
                ScopeResolverGlobals.unknown(),
                codemapRef,
                dialect,
            )
        }.getOrElse { return Result.failure(it) }

    // --- Collect load statements ---

    class LoadSymbol(
        val arg: LoadArgP<*, *>,
        val bindingId: BindingId,
        var used: Boolean,
    )

    class LoadWip(
        val load: Spanned<LoadP<*, *>>,
        val args: MutableList<LoadSymbol>,
    ) {
        fun anyUnused(): Boolean = args.any { !it.used }
    }

    val loads = mutableListOf<LoadWip>()

    for (top in topLevelStmtsMut(moduleScopes.cst)) {
        val node = top.node
        if (node is StmtP.Load<*, *>) {
            val loadNode = node.loadStmt
            val args = mutableListOf<LoadSymbol>()
            for (arg in loadNode.args) {
                val bindingId =
                    arg.local.node.payload as? BindingId
                        ?: return Result.failure(IllegalStateException("payload is not set"))
                args.add(
                    LoadSymbol(
                        arg = arg,
                        bindingId = bindingId,
                        used = false,
                    ),
                )
            }
            loads.add(
                LoadWip(
                    load = Spanned(node = loadNode, span = top.span),
                    args = args,
                ),
            )
        }
    }

    // --- Mark used symbols ---

    for (top in topLevelStmtsMut(moduleScopes.cst)) {
        top.visitIdent { ident ->
            val resolved =
                ident.node.payload
                    ?: return@visitIdent
            if (resolved is ResolvedIdent.Slot &&
                resolved.slot is Slot.Module
            ) {
                val bindingId = resolved.bindingId
                for (load in loads) {
                    for (arg in load.args) {
                        if (arg.bindingId == bindingId) {
                            arg.used = true
                        }
                    }
                }
            }
        }
    }

    // --- Collect unused loads ---

    val unused = mutableListOf<UnusedLoad>()

    for (load in loads) {
        if (!load.anyUnused()) {
            continue
        }
        val unusedArgs =
            load.args
                .filter { arg ->
                    if (arg.used) {
                        false
                    } else if (hasUnusedMarkerInRange(
                            FileSpan(
                                file = codemapRef.deref(),
                                span = arg.arg.spanWithTrailingComma(),
                            ),
                        )
                    ) {
                        false
                    } else {
                        true
                    }
                }.map { it.arg }
        if (unusedArgs.isEmpty()) {
            continue
        }
        unused.add(
            UnusedLoad(
                load = Spanned(node = load.load.node, span = load.load.span),
                unusedArgs = unusedArgs,
            ),
        )
    }

    return Result.success(Pair(codemapRef, unused))
}
