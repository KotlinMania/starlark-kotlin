// port-lint: source src/analysis/unused_loads/find.rs
package io.github.kotlinmania.starlark_kotlin.analysis.unused_loads

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

import io.github.kotlinmania.starlark_kotlin.codemap.CodeMap
import io.github.kotlinmania.starlark_kotlin.codemap.FileSpan
import io.github.kotlinmania.starlark_kotlin.codemap.Span
import io.github.kotlinmania.starlark_kotlin.codemap.Spanned
import io.github.kotlinmania.starlark_kotlin.environment.MutableNames
import io.github.kotlinmania.starlark_kotlin.eval.compiler.BindingId
import io.github.kotlinmania.starlark_kotlin.eval.compiler.ModuleScopes
import io.github.kotlinmania.starlark_kotlin.eval.compiler.ResolvedIdent
import io.github.kotlinmania.starlark_kotlin.eval.compiler.Slot
import io.github.kotlinmania.starlark_kotlin.eval.compiler.scope.CstIdent
import io.github.kotlinmania.starlark_kotlin.eval.compiler.scope.CstStmt
import io.github.kotlinmania.starlark_kotlin.eval.compiler.scope.ScopeResolverGlobals
import io.github.kotlinmania.starlark_kotlin.eval.compiler.topLevelStmtsMut
import io.github.kotlinmania.starlark_kotlin.syntax.AstModule
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AstPayload
import io.github.kotlinmania.starlark_kotlin.syntax.ast.ExprP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.LoadArgP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.LoadP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.StmtP
import io.github.kotlinmania.starlark_kotlin.syntax.dialect.Dialect
import io.github.kotlinmania.starlark_kotlin.values.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.FrozenRef
import io.github.kotlinmania.starlark_kotlin.values.types.allocAny

// Forward-reference AST types until starlark_syntax port is complete.
// use starlark_syntax::codemap::FileSpanRef;
// use starlark_syntax::syntax::ast::LoadArgP;
// use starlark_syntax::syntax::ast::LoadP;
// use starlark_syntax::syntax::ast::StmtP;

/** Unused load statement. */
// pub(crate) struct UnusedLoad
internal class UnusedLoad(
    /** Location of the statement (i.e. position of `load` keyword). */
    // pub(crate) load: Spanned<LoadP<CstPayload>>,
    val load: Spanned<LoadP<*, *>>,
    /** Unused local names, e. g. `x` in `load("foo", x="y")`. */
    // pub(crate) unused_args: Vec<LoadArgP<CstPayload>>,
    val unusedArgs: List<LoadArgP<*, *>>,
) {
    // impl UnusedLoad

    /** If the whole `load` statement is unused. */
    // pub(crate) fn all_unused(&self) -> bool
    fun allUnused(): Boolean {
        return unusedArgs.size == load.node.args.size
    }
}

/** Check if there are `@unused` markers on the lines with the given span. */
// fn has_unused_marker_in_range(span: FileSpanRef) -> bool
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
 * Port of `StmtP::visit_ident` from Rust's `uniplate.rs`.
 */
@Suppress("UNCHECKED_CAST")
private fun CstStmt.visitIdent(f: (CstIdent) -> Unit) {
    fun visitExprIdent(expr: Spanned<out ExprP<*>>) {
        when (val e = expr.node) {
            is ExprP.Identifier<*, *> -> f(e.ident as CstIdent)
            is ExprP.Tuple<*> -> e.elements.forEach { visitExprIdent(it as Spanned<ExprP<*>>) }
            is ExprP.ListExpr<*> -> e.elements.forEach { visitExprIdent(it as Spanned<ExprP<*>>) }
            is ExprP.Dict<*> -> e.elements.forEach { (k, v) ->
                visitExprIdent(k as Spanned<ExprP<*>>)
                visitExprIdent(v as Spanned<ExprP<*>>)
            }
            is ExprP.If<*> -> {
                visitExprIdent(e.cond as Spanned<ExprP<*>>)
                visitExprIdent(e.v1 as Spanned<ExprP<*>>)
                visitExprIdent(e.v2 as Spanned<ExprP<*>>)
            }
            is ExprP.Dot<*> -> visitExprIdent(e.expr as Spanned<ExprP<*>>)
            is ExprP.Call<*> -> {
                visitExprIdent(e.expr as Spanned<ExprP<*>>)
                e.args.args.forEach { arg -> visitExprIdent(arg.node.expr() as Spanned<ExprP<*>>) }
            }
            is ExprP.Index<*> -> {
                visitExprIdent(e.expr as Spanned<ExprP<*>>)
                visitExprIdent(e.index as Spanned<ExprP<*>>)
            }
            is ExprP.Index2<*> -> {
                visitExprIdent(e.expr as Spanned<ExprP<*>>)
                visitExprIdent(e.index0 as Spanned<ExprP<*>>)
                visitExprIdent(e.index1 as Spanned<ExprP<*>>)
            }
            is ExprP.Slice<*> -> {
                visitExprIdent(e.expr as Spanned<ExprP<*>>)
                e.start?.let { visitExprIdent(it as Spanned<ExprP<*>>) }
                e.stop?.let { visitExprIdent(it as Spanned<ExprP<*>>) }
                e.step?.let { visitExprIdent(it as Spanned<ExprP<*>>) }
            }
            is ExprP.Not<*> -> visitExprIdent(e.expr as Spanned<ExprP<*>>)
            is ExprP.Minus<*> -> visitExprIdent(e.expr as Spanned<ExprP<*>>)
            is ExprP.Plus<*> -> visitExprIdent(e.expr as Spanned<ExprP<*>>)
            is ExprP.BitNot<*> -> visitExprIdent(e.expr as Spanned<ExprP<*>>)
            is ExprP.Op<*> -> {
                visitExprIdent(e.lhs as Spanned<ExprP<*>>)
                visitExprIdent(e.rhs as Spanned<ExprP<*>>)
            }
            is ExprP.ListComprehension<*> -> {
                visitExprIdent(e.expr as Spanned<ExprP<*>>)
            }
            is ExprP.DictComprehension<*> -> {
                visitExprIdent(e.key as Spanned<ExprP<*>>)
                visitExprIdent(e.value as Spanned<ExprP<*>>)
            }
            is ExprP.FString<*> -> {
                e.fstring.node.expressions.forEach { visitExprIdent(it as Spanned<ExprP<*>>) }
            }
            is ExprP.Lambda<*, *> -> {
                visitExprIdent(e.lambda.body as Spanned<ExprP<*>>)
            }
            is ExprP.Literal<*> -> { /* no identifiers */ }
        }
    }

    fun visitStmt(stmt: CstStmt) {
        when (val s = stmt.node) {
            is StmtP.Statements<*> -> s.stmts.forEach { visitStmt(it as CstStmt) }
            is StmtP.Expression<*> -> visitExprIdent(s.expr as Spanned<ExprP<*>>)
            is StmtP.Return<*> -> (s.expr as Spanned<ExprP<*>>?)?.let { visitExprIdent(it) }
            is StmtP.Assign<*> -> {
                visitExprIdent(s.assign.rhs as Spanned<ExprP<*>>)
            }
            is StmtP.AssignModify<*> -> {
                visitExprIdent(s.rhs as Spanned<ExprP<*>>)
            }
            is StmtP.If<*> -> {
                visitExprIdent(s.cond as Spanned<ExprP<*>>)
                visitStmt(s.suite as CstStmt)
            }
            is StmtP.IfElse<*> -> {
                visitExprIdent(s.cond as Spanned<ExprP<*>>)
                visitStmt(s.suite1 as CstStmt)
                visitStmt(s.suite2 as CstStmt)
            }
            is StmtP.For<*> -> {
                visitExprIdent(s.forStmt.over as Spanned<ExprP<*>>)
                visitStmt(s.forStmt.body as CstStmt)
            }
            is StmtP.Def<*, *> -> {
                visitStmt(s.def.body as CstStmt)
            }
            is StmtP.Load<*, *> -> { /* no identifiers in read position */ }
            is StmtP.Break<*>,
            is StmtP.Continue<*>,
            is StmtP.Pass<*> -> { /* no expressions */ }
        }
    }

    visitStmt(this)
}

/** Parse the module and find unused loads. */
// pub(crate) fn find_unused_loads(name: &str, program: &str) -> crate::Result<(CodeMap, Vec<UnusedLoad>)>
internal fun findUnusedLoads(
    name: String,
    program: String,
): Result<Pair<FrozenRef<CodeMap>, List<UnusedLoad>>> {
    val module = AstModule.parse(name, program, Dialect.AllOptionsInternal)
        .getOrElse { return Result.failure(it) }
    val names = MutableNames.new()
    val heap = FrozenHeap.new()
    val (codemap, statement, dialect, _) = module.intoParts()
    val codemapRef = heap.allocAny(codemap)
    val moduleScopes = runCatching {
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

    // struct LoadSymbol<'a>
    class LoadSymbol(
        val arg: LoadArgP<*, *>,
        val bindingId: BindingId,
        var used: Boolean,
    )

    // struct LoadWip<'a>
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
                val bindingId = arg.local.node.payload as? BindingId
                    ?: return Result.failure(IllegalStateException("payload is not set"))
                args.add(LoadSymbol(
                    arg = arg,
                    bindingId = bindingId,
                    used = false,
                ))
            }
            loads.add(LoadWip(
                load = Spanned(node = loadNode, span = top.span),
                args = args,
            ))
        }
    }

    // --- Mark used symbols ---

    for (top in topLevelStmtsMut(moduleScopes.cst)) {
        top.visitIdent { ident ->
            val resolved = ident.node.payload
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
        val unusedArgs = load.args
            .filter { arg ->
                if (arg.used) {
                    false
                } else if (hasUnusedMarkerInRange(FileSpan(
                        file = codemapRef.deref(),
                        span = arg.arg.spanWithTrailingComma(),
                    ))
                ) {
                    false
                } else {
                    true
                }
            }
            .map { it.arg }
        if (unusedArgs.isEmpty()) {
            continue
        }
        unused.add(UnusedLoad(
            load = Spanned(node = load.load.node, span = load.load.span),
            unusedArgs = unusedArgs,
        ))
    }

    return Result.success(Pair(codemapRef, unused))
}
