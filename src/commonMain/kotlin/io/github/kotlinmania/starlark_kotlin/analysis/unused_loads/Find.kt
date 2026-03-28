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

import io.github.kotlinmania.starlark_kotlin.environment.MutableNames
import io.github.kotlinmania.starlark_kotlin.eval.compiler.scope.scope_resolver_globals.ScopeResolverGlobals
import io.github.kotlinmania.starlark_kotlin.values.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.eval.compiler.Slot
import io.github.kotlinmania.starlark_kotlin.eval.compiler.ResolvedIdent
import io.github.kotlinmania.starlark_kotlin.syntax.ast.BindingId
import io.github.kotlinmania.starlark_kotlin.eval.compiler.ModuleScopes
import io.github.kotlinmania.starlark_kotlin.syntax.payload_and_span.Payload
import io.github.kotlinmania.starlark_kotlin.syntax.dialect.Dialect
import io.github.kotlinmania.starlark_kotlin.assert.parse
import io.github.kotlinmania.starlark_kotlin.values.types.allocAny
import io.github.kotlinmania.starlark_kotlin.eval.compiler.BindingId
import io.github.kotlinmania.starlark_kotlin.typing.cst
import io.github.kotlinmania.starlark_kotlin.eval.compiler.topLevelStmts
import io.github.kotlinmania.starlark_kotlin.environment.slot
import io.github.kotlinmania.starlark_kotlin.codemap.sourceLine
import io.github.kotlinmania.starlark_kotlin.codemap.findLine
import io.github.kotlinmania.starlark_kotlin.codemap.CodeMap
import io.github.kotlinmania.starlark_kotlin.codemap.Spanned
import io.github.kotlinmania.starlark_kotlin.values.types.dict.end
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.allocator.alloc.begin
import io.github.kotlinmania.starlark_kotlin.codemap.Span
import io.github.kotlinmania.starlark_kotlin.syntax.AstModule
import io.github.kotlinmania.starlark_kotlin.eval.compiler.scope.ScopeResolverGlobals

// Forward-reference AST types until starlark_syntax port is complete.
// use starlark_syntax::codemap::FileSpanRef;
// use starlark_syntax::syntax::ast::LoadArgP;
// use starlark_syntax::syntax::ast::LoadP;
// use starlark_syntax::syntax::ast::StmtP;

/// Unused load statement.
// pub(crate) struct UnusedLoad
internal class UnusedLoad(
    /// Location of the statement (i.e. position of `load` keyword).
    // pub(crate) load: Spanned<LoadP<CstPayload>>,
    val load: Spanned<LoadP>,
    /// Unused local names, e. g. `x` in `load("foo", x="y")`.
    // pub(crate) unused_args: Vec<LoadArgP<CstPayload>>,
    val unusedArgs: List<LoadArgP>,
) {
    // impl UnusedLoad

    /// If the whole `load` statement is unused.
    // pub(crate) fn all_unused(&self) -> bool
    fun allUnused(): Boolean {
        return unusedArgs.size == load.node.args.size
    }
}

/// Placeholder for LoadP AST node (load statement).
// starlark_syntax::syntax::ast::LoadP<CstPayload>
internal class LoadP(
    val args: List<LoadArgP> = emptyList(),
)

/// Placeholder for LoadArgP AST node (load argument).
// starlark_syntax::syntax::ast::LoadArgP<CstPayload>
internal class LoadArgP(
    val local: CstAssignIdent = CstAssignIdent(),
    val their: String = "",
) {
    /// Span of the argument including trailing comma.
    // fn span_with_trailing_comma(&self) -> Span
    fun spanWithTrailingComma(): Span = local.span
}

/// Placeholder for CstAssignIdent.
// CstPayload assign ident
internal class CstAssignIdent(
    val span: Span = Span(),
    val ident: String = "",
    val payload: BindingId? = null,
)

/// Placeholder for StmtP node.
// starlark_syntax::syntax::ast::StmtP<CstPayload>
internal sealed class StmtP {
    class Load(val load: LoadP) : StmtP()
    class Other : StmtP()
}

/// Placeholder for CstStmt (Spanned<StmtP>).
internal class CstStmt(
    val span: Span = Span(),
    val node: StmtP = StmtP.Other(),
) {
    /// Visit all identifier references in this statement.
    // fn visit_ident(&self, f: impl FnMut(&CstIdent) -> anyhow::Result<()>) -> anyhow::Result<()>
    fun visitIdent(f: (CstIdent) -> Result<Unit>): Result<Unit> {
        // Will be implemented with full AST visitor.
        return Result.success(Unit)
    }
}

/// Placeholder for CstIdent (identifier with resolved payload).
internal class CstIdent(
    val span: Span = Span(),
    val payload: ResolvedIdent? = null,
)

/// Placeholder for FileSpanRef.
// starlark_syntax::codemap::FileSpanRef
class FileSpanRef(
    val file: CodeMap,
    val span: Span,
)

/// Check if there are `@unused` markers on the lines with the given span.
// fn has_unused_marker_in_range(span: FileSpanRef) -> bool
private fun hasUnusedMarkerInRange(span: FileSpanRef): Boolean {
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

/// Parse the module and find unused loads.
// pub(crate) fn find_unused_loads(name: &str, program: &str) -> crate::Result<(CodeMap, Vec<UnusedLoad>)>
internal fun findUnusedLoads(
    name: String,
    program: String,
): Result<Pair<CodeMap, List<UnusedLoad>>> {
    val module = AstModule.parse(name, program, Dialect.AllOptionsInternal)
        .getOrElse { return Result.failure(it) }
    val names = MutableNames.new()
    val heap = FrozenHeap.new()
    val (codemap, statement, dialect) = module.intoParts()
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
        val arg: LoadArgP,
        val bindingId: BindingId,
        var used: Boolean,
    )

    // struct LoadWip<'a>
    class LoadWip(
        val load: Spanned<LoadP>,
        val args: MutableList<LoadSymbol>,
    ) {
        fun anyUnused(): Boolean = args.any { !it.used }
    }

    val loads = mutableListOf<LoadWip>()

    for (top in topLevelStmts(moduleScopes.cst)) {
        val node = top.node
        if (node is StmtP.Load) {
            val loadNode = node.load
            val args = mutableListOf<LoadSymbol>()
            for (arg in loadNode.args) {
                val bindingId = arg.local.payload
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

    for (top in topLevelStmts(moduleScopes.cst)) {
        top.visitIdent { ident ->
            val resolved = ident.Payload
                ?: return@visitIdent Result.failure(
                    IllegalStateException("ident is not resolved (internal error)")
                )
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
            Result.success(Unit)
        }.getOrElse { return Result.failure(it) }
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
                } else if (hasUnusedMarkerInRange(FileSpanRef(
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
