// port-lint: source src/eval/compiler/compr.rs
package io.github.kotlinmania.starlark_kotlin.eval.compiler.compr

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

/// List/dict/set comprehension evaluation.

// Placeholder types referenced from other modules
// These will be replaced with real imports as the port progresses
class Span
class IrSpanned<T>(val node: T, val span: Span = Span()) {
    fun optimize(ctx: OptCtx): IrSpanned<T> = this
    fun isIterableEmpty(): Boolean = false
    fun intoExpr(): IrSpanned<ExprCompiled> {
        @Suppress("UNCHECKED_CAST")
        return this as IrSpanned<ExprCompiled>
    }
}

class CstExpr
class CstPayload

class ForClauseP(val over: CstExpr, val variable: CstExpr)

sealed class ClauseP {
    class For(val clause: ForClauseP) : ClauseP()
    class If(val expr: CstExpr) : ClauseP()
}

class ExprCompiled {
    companion object {
        fun compr(compr: ComprCompiled): ExprCompiled = ExprCompiled()
    }
}

sealed class ExprCompiledBool {
    class Const(val value: Boolean) : ExprCompiledBool()
    class Other(val expr: IrSpanned<ExprCompiled>) : ExprCompiledBool()

    companion object {
        fun new(expr: IrSpanned<ExprCompiled>): IrSpanned<ExprCompiledBool> =
            IrSpanned(Other(expr))
    }

    fun intoExpr(): IrSpanned<ExprCompiled> {
        return when (this) {
            is Other -> expr
            is Const -> IrSpanned(ExprCompiled())
        }
    }
}

class AssignCompiledValue

class CompilerInternalError(message: String = "") : Exception(message)

class OptCtx

fun listToTuple(expr: CstExpr): CstExpr = expr

class Compiler {
    fun expr(x: CstExpr): IrSpanned<ExprCompiled> = IrSpanned(ExprCompiled())
    fun exprTruth(x: CstExpr): IrSpanned<ExprCompiledBool> = IrSpanned(ExprCompiledBool.Const(true))
    fun assignTarget(x: CstExpr): IrSpanned<AssignCompiledValue> = IrSpanned(AssignCompiledValue())

    fun listComprehension(
        x: CstExpr,
        for_: ForClauseP,
        clauses: List<ClauseP>,
    ): Result<ExprCompiled> {
        val compiledClauses = compileClauses(for_, clauses)
        if (compiledClauses.isFailure) return compiledClauses.map { ExprCompiled() }
        val compiledX = expr(x)
        return Result.success(ExprCompiled.compr(ComprCompiled.List(
            compiledX,
            compiledClauses.getOrThrow(),
        )))
    }

    fun dictComprehension(
        k: CstExpr,
        v: CstExpr,
        for_: ForClauseP,
        clauses: List<ClauseP>,
    ): Result<ExprCompiled> {
        val compiledClauses = compileClauses(for_, clauses)
        if (compiledClauses.isFailure) return compiledClauses.map { ExprCompiled() }
        val compiledK = expr(k)
        val compiledV = expr(v)
        return Result.success(ExprCompiled.compr(ComprCompiled.Dict(
            Pair(compiledK, compiledV),
            compiledClauses.getOrThrow(),
        )))
    }

    /// Peel the final if's from clauses, and return them (in the order they started), plus the next for you get to
    private fun compileIfs(
        clauses: MutableList<ClauseP>,
    ): Result<Pair<ForClauseP?, List<IrSpanned<ExprCompiled>>>> {
        val ifs = mutableListOf<IrSpanned<ExprCompiled>>()
        while (clauses.isNotEmpty()) {
            when (val x = clauses.removeAt(clauses.lastIndex)) {
                is ClauseP.For -> {
                    ifs.reverse()
                    return Result.success(Pair(x.clause, ifs))
                }
                is ClauseP.If -> {
                    val compiled = exprTruth(x.expr)
                    if (compiled.node is ExprCompiledBool.Const && compiled.node.value) {
                        // If the condition is always true, skip the clause.
                        continue
                    }
                    ifs.add(compiled.node.intoExpr())
                }
            }
        }
        ifs.reverse()
        return Result.success(Pair(null, ifs))
    }

    private fun compileClauses(
        for_: ForClauseP,
        clauses: List<ClauseP>,
    ): Result<ClausesCompiled> {
        // The first for.over is scoped before we enter the list comp
        val over = expr(listToTuple(for_.over))

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
                    variable = assignTarget(for_.variable),
                    over = over,
                    ifs = ifs,
                )
                return Result.success(ClausesCompiled.new(res, last))
            } else {
                res.add(ClauseCompiled(
                    over = expr(nextFor.over),
                    variable = assignTarget(nextFor.variable),
                    ifs = ifs,
                ))
            }
        }
    }
}

sealed class ComprCompiled {
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
                ExprCompiled.compr(List(x.optimize(ctx), optimizedClauses))
            }
            is Dict -> {
                val (k, v) = kv
                val optimizedClauses = clauses.optimize(ctx)
                ExprCompiled.compr(Dict(
                    Pair(k.optimize(ctx), v.optimize(ctx)),
                    optimizedClauses.optimize(ctx),
                ))
            }
        }
    }
}

class ClauseCompiled(
    internal val variable: IrSpanned<AssignCompiledValue>,
    internal val over: IrSpanned<ExprCompiled>,
    internal val ifs: List<IrSpanned<ExprCompiled>>,
) {
    fun optimize(ctx: OptCtx): ClauseCompiled {
        return ClauseCompiled(
            variable = variable.optimize(ctx),
            over = over.optimize(ctx),
            ifs = ifs.mapNotNull { e ->
                val optimized = e.optimize(ctx)
                val asBool = ExprCompiledBool.new(optimized)
                when (val node = asBool.node) {
                    is ExprCompiledBool.Const -> if (node.value) null else node.intoExpr()
                    else -> node.intoExpr()
                }
            },
        )
    }
}

/// All clauses in a comprehension. Never empty.
class ClausesCompiled private constructor(
    /// Not empty.
    ///
    /// Clauses are in reverse order, i. e. the first executed clause is the last in the list.
    private val clauses: List<ClauseCompiled>,
) {
    companion object {
        fun new(clauses: MutableList<ClauseCompiled>, last: ClauseCompiled): ClausesCompiled {
            clauses.add(last)
            return ClausesCompiled(clauses.toList())
        }
    }

    /// Clauses are definitely no-op, i. e. zero iterations, and no side effects of iteration.
    fun isNop(): Boolean {
        // NOTE(nga): if the first loop argument is empty collection, clauses are definitely no-op.
        //   But this is not true for the rest of loops: if inner loop collection is empty,
        //   clauses produce no iterations, but the outer loop may still has side effects.
        //   There are missing optimizations here:
        //   * we could separate effects and emit empty list/dict.
        //   * or at least do not generate comprehension terminator.
        return splitLast().first.over.isIterableEmpty()
    }

    /// Last clause is the one which is executed first.
    fun splitLast(): Pair<ClauseCompiled, List<ClauseCompiled>> {
        return Pair(clauses.last(), clauses.dropLast(1))
    }

    fun optimize(ctx: OptCtx): ClausesCompiled {
        return ClausesCompiled(clauses.map { c -> c.optimize(ctx) })
    }
}
