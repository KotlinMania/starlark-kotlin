// port-lint: source src/eval/compiler/compr.rs
package io.github.kotlinmania.starlark_kotlin.eval.compiler

import io.github.kotlinmania.starlark_kotlin.eval.compiler.scope.CstExpr
import io.github.kotlinmania.starlark_kotlin.eval.compiler.scope.CstPayload
import io.github.kotlinmania.starlark_kotlin.syntax.ast.ClauseP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.ForClauseP
import io.github.kotlinmania.starlark_kotlin.eval.compiler.opt_ctx.OptCtx

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

/** List/dict/set comprehension evaluation. */

private fun listToTupleCompr(expr: CstExpr): CstExpr = expr

internal fun Compiler.listComprehension(
    x: CstExpr,
    forClause: ForClauseP<CstPayload>,
    clauses: List<ClauseP<CstPayload>>,
): Result<ExprCompiled> {
    val compiledClauses = compileClauses(forClause, clauses)
    if (compiledClauses.isFailure) return Result.failure(compiledClauses.exceptionOrNull()!!)
    val compiledX = this.expr(x).getOrElse { return Result.failure(it) }
    return Result.success(ExprCompiled.Compr(ComprCompiled.List(
        compiledX,
        compiledClauses.getOrThrow(),
    )))
}

internal fun Compiler.dictComprehension(
    k: CstExpr,
    v: CstExpr,
    forClause: ForClauseP<CstPayload>,
    clauses: List<ClauseP<CstPayload>>,
): Result<ExprCompiled> {
    val compiledClauses = compileClauses(forClause, clauses)
    if (compiledClauses.isFailure) return Result.failure(compiledClauses.exceptionOrNull()!!)
    val compiledK = this.expr(k).getOrElse { return Result.failure(it) }
    val compiledV = this.expr(v).getOrElse { return Result.failure(it) }
    return Result.success(ExprCompiled.Compr(ComprCompiled.Dict(
        Pair(compiledK, compiledV),
        compiledClauses.getOrThrow(),
    )))
}

/** Peel the final if's from clauses, and return them (in the order they started), plus the next for you get to. */
private fun Compiler.compileIfs(
    clauses: MutableList<ClauseP<CstPayload>>,
): Result<Pair<ForClauseP<CstPayload>?, List<IrSpanned<ExprCompiled>>>> {
    val ifs = mutableListOf<IrSpanned<ExprCompiled>>()
    while (clauses.isNotEmpty()) {
        when (val x = clauses.removeAt(clauses.lastIndex)) {
            is ClauseP.For<*> -> {
                ifs.reverse()
                @Suppress("UNCHECKED_CAST")
                return Result.success(Pair(x.forClause as ForClauseP<CstPayload>, ifs))
            }
            is ClauseP.If<*> -> {
                val compiled = this.exprTruth(x.cond as CstExpr).getOrElse { return Result.failure(it) }
                if (compiled.node is ExprCompiledBool.Const && compiled.node.value) {
                    // If the condition is always true, skip the clause.
                    continue
                }
                ifs.add(compiled.intoExpr())
            }
        }
    }
    ifs.reverse()
    return Result.success(Pair(null, ifs))
}

private fun Compiler.compileClauses(
    forClause: ForClauseP<CstPayload>,
    clauses: List<ClauseP<CstPayload>>,
): Result<ClausesCompiled> {
    // The first for.over is scoped before we enter the list comp
    val over = this.expr(listToTupleCompr(forClause.over)).getOrElse { return Result.failure(it) }

    val clausesMut = clauses.toMutableList()

    // Now we want to group them into a `for`, followed by any number of `if`.
    // The evaluator wants to use pop to consume them, so reverse the order.
    val res = mutableListOf<ClauseCompiled>()
    while (true) {
        val result = compileIfs(clausesMut)
        if (result.isFailure) return Result.failure(result.exceptionOrNull()!!)
        val (nextFor, ifs) = result.getOrThrow()
        if (nextFor == null) {
            val last = ClauseCompiled(
                variable = this.assignTarget(forClause.varTarget).getOrElse { return Result.failure(it) },
                over = over,
                ifs = ifs,
            )
            return Result.success(ClausesCompiled.new(res, last))
        } else {
            res.add(ClauseCompiled(
                over = this.expr(nextFor.over).getOrElse { return Result.failure(it) },
                variable = this.assignTarget(nextFor.varTarget).getOrElse { return Result.failure(it) },
                ifs = ifs,
            ))
        }
    }
}

internal sealed class ComprCompiled {
    class List(
        val x: IrSpanned<ExprCompiled>,
        val clauses: ClausesCompiled,
    ) : ComprCompiled()

    class Dict(
        val kv: Pair<IrSpanned<ExprCompiled>, IrSpanned<ExprCompiled>>,
        val clauses: ClausesCompiled,
    ) : ComprCompiled()

    fun clauses(): ClausesCompiled {
        return when (this) {
            is List -> clauses
            is Dict -> clauses
        }
    }

    fun optimize(ctx: OptCtx): ExprCompiled {
        return when (this) {
            is List -> {
                val optimizedClauses = clauses.optimize(ctx)
                ExprCompiled.Compr(List(x.optimize(ctx), optimizedClauses))
            }
            is Dict -> {
                val (k, v) = kv
                val optimizedClauses = clauses.optimize(ctx)
                ExprCompiled.Compr(Dict(
                    Pair(k.optimize(ctx), v.optimize(ctx)),
                    optimizedClauses.optimize(ctx),
                ))
            }
        }
    }
}

internal class ClauseCompiled(
    val variable: IrSpanned<AssignCompiledValue>,
    val over: IrSpanned<ExprCompiled>,
    val ifs: List<IrSpanned<ExprCompiled>>,
) {
    fun optimize(ctx: OptCtx): ClauseCompiled {
        return ClauseCompiled(
            variable = variable.map { it },
            over = over.optimize(ctx),
            ifs = ifs.mapNotNull { e ->
                val optimized = e.optimize(ctx)
                val asBool = ExprCompiledBool.new(optimized)
                when (val node = asBool.node) {
                    is ExprCompiledBool.Const -> if (node.value) null else IrSpanned(span = asBool.span, node = node.intoExpr())
                    else -> IrSpanned(span = asBool.span, node = node.intoExpr())
                }
            },
        )
    }
}

/** All clauses in a comprehension. Never empty. */
internal class ClausesCompiled private constructor(
    /**
     * Not empty.
     *
     * Clauses are in reverse order, i.e. the first executed clause is the last in the list.
     */
    private val clauses: List<ClauseCompiled>,
) {
    companion object {
        fun new(clauses: MutableList<ClauseCompiled>, last: ClauseCompiled): ClausesCompiled {
            clauses.add(last)
            return ClausesCompiled(clauses.toList())
        }
    }

    /** Clauses are definitely no-op, i.e. zero iterations, and no side effects of iteration. */
    fun isNop(): Boolean {
        // NOTE(nga): if the first loop argument is empty collection, clauses are definitely no-op.
        //   But this is not true for the rest of loops: if inner loop collection is empty,
        //   clauses produce no iterations, but the outer loop may still has side effects.
        //   There are missing optimizations here:
        //   * we could separate effects and emit empty list/dict.
        //   * or at least do not generate comprehension terminator.
        return splitLast().first.over.node.isIterableEmpty()
    }

    /** Last clause is the one which is executed first. */
    fun splitLast(): Pair<ClauseCompiled, List<ClauseCompiled>> {
        return Pair(clauses.last(), clauses.dropLast(1))
    }

    fun optimize(ctx: OptCtx): ClausesCompiled {
        return ClausesCompiled(clauses.map { c -> c.optimize(ctx) })
    }
}
