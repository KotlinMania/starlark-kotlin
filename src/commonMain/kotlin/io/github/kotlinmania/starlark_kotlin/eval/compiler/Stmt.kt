// port-lint: source src/eval/compiler/stmt.rs
package io.github.kotlinmania.starlark_kotlin.eval.compiler

/*
 * Copyright 2018 The Starlark in Rust Authors.
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

// Evaluation environment, provide converters from Ast* element to value.
//
// # Starlark and BUILD dialect
//
// All evaluation functions can evaluate the full Starlark language (i.e.
// Bazel's .bzl files) or the BUILD file dialect (i.e. used to interpret
// Bazel's BUILD file). The BUILD dialect does not allow `def` statements.

import io.github.kotlinmania.starlark_kotlin.codemap.Span
import io.github.kotlinmania.starlark_kotlin.codemap.Spanned
import io.github.kotlinmania.starlark_kotlin.environment.FrozenModuleData
import io.github.kotlinmania.starlark_kotlin.eval.compiler.expr.Builtin1
import io.github.kotlinmania.starlark_kotlin.eval.compiler.expr.ExprCompiled
import io.github.kotlinmania.starlark_kotlin.eval.compiler.expr.ExprLogicalBinOp
import io.github.kotlinmania.starlark_kotlin.eval.compiler.opt_ctx.OptCtx
import io.github.kotlinmania.starlark_kotlin.eval.compiler.scope.payload.CstAssignTarget
import io.github.kotlinmania.starlark_kotlin.eval.compiler.scope.payload.CstExpr
import io.github.kotlinmania.starlark_kotlin.eval.compiler.scope.payload.CstStmt
import io.github.kotlinmania.starlark_kotlin.eval.runtime.FrameSpan
import io.github.kotlinmania.starlark_kotlin.eval.runtime.GC_THRESHOLD
import io.github.kotlinmania.starlark_kotlin.eval.runtime.LocalCapturedSlotId
import io.github.kotlinmania.starlark_kotlin.eval.runtime.LocalSlotId
import io.github.kotlinmania.starlark_kotlin.eval.runtime.frozen_file_span.FrozenFileSpan
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AssignOp
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AssignP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AssignTargetP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.DefP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.ForP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.ModuleSlotId
import io.github.kotlinmania.starlark_kotlin.syntax.ast.StmtP
import io.github.kotlinmania.starlark_kotlin.values.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.ValueError
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.types.dict.Dict
import io.github.kotlinmania.starlark_kotlin.values.types.dict.DictMut
import io.github.kotlinmania.starlark_kotlin.values.types.dict.DictRef
import io.github.kotlinmania.starlark_kotlin.values.types.list.ListData
import io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.compiled.TypeCompiled
import kotlin.math.max

// #[derive(Clone, Debug)]
// pub(crate) enum AssignModifyLhs
internal sealed class AssignModifyLhs {
    data class Dot(val expr: IrSpanned<ExprCompiled>, val name: String) : AssignModifyLhs()
    data class Array(val expr: IrSpanned<ExprCompiled>, val index: IrSpanned<ExprCompiled>) : AssignModifyLhs()
    data class Local(val slot: IrSpanned<LocalSlotId>) : AssignModifyLhs()
    data class LocalCaptured(val slot: IrSpanned<LocalCapturedSlotId>) : AssignModifyLhs()
    data class Module(val slot: IrSpanned<ModuleSlotId>) : AssignModifyLhs()
}

// #[derive(Clone, Debug)]
// pub(crate) enum StmtCompiled
internal sealed class StmtCompiled {
    data object PossibleGc : StmtCompiled()
    data class Return(val expr: IrSpanned<ExprCompiled>) : StmtCompiled()
    data class Expr(val expr: IrSpanned<ExprCompiled>) : StmtCompiled()
    data class Assign(
        val lhs: IrSpanned<AssignCompiledValue>,
        val ty: IrSpanned<TypeCompiled<FrozenValue>>?,
        val rhs: IrSpanned<ExprCompiled>,
    ) : StmtCompiled()
    data class AssignModify(
        val lhs: AssignModifyLhs,
        val op: AssignOp,
        val rhs: IrSpanned<ExprCompiled>,
    ) : StmtCompiled()
    data class If(
        val cond: IrSpanned<ExprCompiled>,
        val thenBlock: StmtsCompiled,
        val elseBlock: StmtsCompiled,
    ) : StmtCompiled()
    data class For(
        val variable: IrSpanned<AssignCompiledValue>,
        val over: IrSpanned<ExprCompiled>,
        val body: StmtsCompiled,
    ) : StmtCompiled()
    data object Break : StmtCompiled()
    data object Continue : StmtCompiled()
}

// #[derive(Debug, Default)]
// pub(crate) struct StmtCompileContext
internal data class StmtCompileContext(
    /** Current function has return type. */
    val hasReturnType: Boolean = false,
)

// pub(crate) struct OptimizeOnFreezeContext<'v, 'a>
internal class OptimizeOnFreezeContext(
    val module: FrozenModuleData,
    /**
     * Nothing useful should be left in the heap after the freeze,
     * but having a heap is useful to allocate objects temporarily
     * (when invoking operations which require heap).
     */
    val heap: Heap,
    val frozenHeap: FrozenHeap,
)

// impl AssignModifyLhs
// fn optimize(&self, ctx: &mut OptCtx) -> AssignModifyLhs
internal fun AssignModifyLhs.optimize(ctx: OptCtx): AssignModifyLhs = when (this) {
    is AssignModifyLhs.Dot -> AssignModifyLhs.Dot(expr.optimize(ctx), name)
    is AssignModifyLhs.Array -> AssignModifyLhs.Array(expr.optimize(ctx), index.optimize(ctx))
    is AssignModifyLhs.Local, is AssignModifyLhs.LocalCaptured, is AssignModifyLhs.Module -> this
}

// impl IrSpanned<StmtCompiled>
// fn optimize(&self, ctx: &mut OptCtx) -> StmtsCompiled
internal fun IrSpanned<StmtCompiled>.optimize(ctx: OptCtx): StmtsCompiled = when (val s = this.node) {
    is StmtCompiled.Return -> StmtsCompiled.one(IrSpanned(
        span = span, node = StmtCompiled.Return(s.expr.optimize(ctx)),
    ))
    is StmtCompiled.Expr -> StmtsCompiled.expr(s.expr.optimize(ctx))
    is StmtCompiled.Assign -> StmtsCompiled.one(IrSpanned(
        span = span, node = StmtCompiled.Assign(s.lhs.optimize(ctx), s.ty, s.rhs.optimize(ctx)),
    ))
    is StmtCompiled.If -> StmtsCompiled.ifStmt(
        span, s.cond.optimize(ctx), s.thenBlock.optimize(ctx), s.elseBlock.optimize(ctx),
    )
    is StmtCompiled.For -> StmtsCompiled.forStmt(
        span, s.variable.optimize(ctx), s.over.optimize(ctx), s.body.optimize(ctx),
    )
    is StmtCompiled.PossibleGc,
    is StmtCompiled.Break,
    is StmtCompiled.Continue -> StmtsCompiled.one(IrSpanned(span = span, node = s))
    is StmtCompiled.AssignModify -> StmtsCompiled.one(IrSpanned(
        span = span, node = StmtCompiled.AssignModify(s.lhs.optimize(ctx), s.op, s.rhs.optimize(ctx)),
    ))
}

// #[derive(Clone, Debug)]
// pub(crate) struct StmtsCompiled(SmallVec1<IrSpanned<StmtCompiled>>)
internal class StmtsCompiled private constructor(
    private var stmts: SmallVec1<IrSpanned<StmtCompiled>>,
) {

    companion object {
        fun empty(): StmtsCompiled = StmtsCompiled(SmallVec1.new())

        fun one(stmt: IrSpanned<StmtCompiled>): StmtsCompiled = StmtsCompiled(SmallVec1.One(stmt))

        // fn expr(expr: IrSpanned<ExprCompiled>) -> StmtsCompiled
        fun expr(expr: IrSpanned<ExprCompiled>): StmtsCompiled {
            val span = expr.span
            val node = expr.node
            if (node.isPureInfallible()) return empty()
            return when (node) {
                is ExprCompiled.ListExpr, is ExprCompiled.TupleExpr -> {
                    val xs = if (node is ExprCompiled.ListExpr) node.elements else (node as ExprCompiled.TupleExpr).elements
                    val stmts = empty()
                    for (x in xs) stmts.extend(expr(x))
                    stmts
                }
                // Unwrap infallible expressions.
                is ExprCompiled.Builtin1Expr -> when (node.op) {
                    is Builtin1.Not, is Builtin1.TypeIs -> expr(node.expr)
                    else -> defaultExpr(span, node)
                }
                // "And" and "or" for effect are equivalent to `if`.
                is ExprCompiled.LogicalBinOp -> when (node.op) {
                    ExprLogicalBinOp.And -> ifStmt(expr.span, node.lhs, expr(node.rhs), empty())
                    ExprLogicalBinOp.Or -> ifStmt(expr.span, node.lhs, empty(), expr(node.rhs))
                }
                else -> defaultExpr(span, node)
            }
        }

        private fun defaultExpr(span: FrameSpan, node: ExprCompiled): StmtsCompiled {
            val t = node.asType()
            return if (t != null) expr(t)
            else one(IrSpanned(span = span, node = StmtCompiled.Expr(IrSpanned(span = span, node = node))))
        }

        // fn if_stmt(span, cond, t, f) -> StmtsCompiled
        fun ifStmt(span: FrameSpan, cond: IrSpanned<ExprCompiled>, t: StmtsCompiled, f: StmtsCompiled): StmtsCompiled {
            val condBool = ExprCompiledBool.new(cond)
            return when (val node = condBool.node) {
                is ExprCompiledBool.Const -> if (node.value) t else f
                is ExprCompiledBool.Expr -> when (val condExpr = node.expr) {
                    is ExprCompiled.Builtin1Expr ->
                        if (condExpr.op is Builtin1.Not) ifStmt(span, condExpr.expr, f, t)
                        else ifStmtDefault(span, condExpr, t, f)
                    is ExprCompiled.Seq -> {
                        val stmt = empty()
                        stmt.extend(expr(condExpr.first))
                        stmt.extend(ifStmt(span, condExpr.second, t, f))
                        stmt
                    }
                    else -> ifStmtDefault(span, condExpr, t, f)
                }
            }
        }

        private fun ifStmtDefault(span: FrameSpan, condExpr: ExprCompiled, t: StmtsCompiled, f: StmtsCompiled): StmtsCompiled {
            val cond = IrSpanned(span = span, node = condExpr)
            return if (t.isEmpty() && f.isEmpty()) expr(cond)
            else one(IrSpanned(span = span, node = StmtCompiled.If(cond, t, f)))
        }

        // fn for_stmt(span, var, over, body) -> StmtsCompiled
        fun forStmt(span: FrameSpan, variable: IrSpanned<AssignCompiledValue>, over: IrSpanned<ExprCompiled>, body: StmtsCompiled): StmtsCompiled =
            if (over.node.isIterableEmpty()) empty()
            else one(IrSpanned(span = span, node = StmtCompiled.For(variable, over, body)))
    }

    fun isEmpty(): Boolean = when (val s = stmts) {
        is SmallVec1.One -> false
        is SmallVec1.Vec -> s.values.isEmpty()
    }

    fun stmts(): List<IrSpanned<StmtCompiled>> = stmts.asSlice()

    /** Last statement in this block is `break`, `continue` or `return`. */
    private fun isTerminal(): Boolean = when (val last = last()?.node) {
        is StmtCompiled.Break, is StmtCompiled.Continue, is StmtCompiled.Return -> true
        else -> false
    }

    fun extend(right: StmtsCompiled) {
        // Do not add any code after `break`, `continue` or `return`.
        if (isTerminal()) return
        stmts = stmts.extend(right.stmts)
    }

    fun optimize(ctx: OptCtx): StmtsCompiled {
        val result = empty()
        when (val s = stmts) {
            is SmallVec1.One -> result.extend(s.value.optimize(ctx))
            is SmallVec1.Vec -> for (stmt in s.values) {
                if (result.isTerminal()) break
                result.extend(stmt.optimize(ctx))
            }
        }
        return result
    }

    fun first(): IrSpanned<StmtCompiled>? = when (val s = stmts) {
        is SmallVec1.One -> s.value
        is SmallVec1.Vec -> s.values.firstOrNull()
    }

    fun last(): IrSpanned<StmtCompiled>? = when (val s = stmts) {
        is SmallVec1.One -> s.value
        is SmallVec1.Vec -> s.values.lastOrNull()
    }
}

// #[derive(Debug, Error)]
// pub(crate) enum AssignError
internal sealed class AssignError(message: String) : Exception(message) {
    // Incorrect number of value to unpack (expected, got)
    class IncorrectNumberOfValueToUnpack(expected: Int, got: Int) :
        AssignError("Unpacked $got values but expected $expected")
}

// #[derive(Clone, Debug, VisitSpanMut)]
// pub(crate) enum AssignCompiledValue
internal sealed class AssignCompiledValue {
    data class Dot(val obj: IrSpanned<ExprCompiled>, val field: String) : AssignCompiledValue()
    data class Index(val array: IrSpanned<ExprCompiled>, val index: IrSpanned<ExprCompiled>) : AssignCompiledValue()
    data class Tuple(val elements: List<IrSpanned<AssignCompiledValue>>) : AssignCompiledValue()
    data class Local(val slot: LocalSlotId) : AssignCompiledValue()
    data class LocalCaptured(val slot: LocalCapturedSlotId) : AssignCompiledValue()
    data class Module(val slot: ModuleSlotId, val name: String) : AssignCompiledValue()
}

// impl AssignCompiledValue
/** Assignment to a local non-captured variable. */
internal fun AssignCompiledValue.asLocalNonCaptured(): LocalSlotId? = when (this) {
    is AssignCompiledValue.Local -> slot
    else -> null
}

// impl IrSpanned<AssignCompiledValue>
internal fun IrSpanned<AssignCompiledValue>.optimize(ctx: OptCtx) = IrSpanned(
    span = this.span,
    node = when (val n = this.node) {
        is AssignCompiledValue.Dot -> AssignCompiledValue.Dot(n.obj.optimize(ctx), n.field)
        is AssignCompiledValue.Index -> AssignCompiledValue.Index(n.array.optimize(ctx), n.index.optimize(ctx))
        is AssignCompiledValue.Tuple -> AssignCompiledValue.Tuple(n.elements.map { it.optimize(ctx) })
        is AssignCompiledValue.Local,
        is AssignCompiledValue.LocalCaptured,
        is AssignCompiledValue.Module -> n
    },
)

// impl Compiler -- assign_target
/** Compile an assignment target. */
internal fun Compiler.assignTarget(expr: CstAssignTarget): IrSpanned<AssignCompiledValue> {
    val span = FrameSpan.new(FrozenFileSpan.new(this.codemap, expr.span))
    return IrSpanned(span = span, node = when (val node = expr.node) {
        is AssignTargetP.Dot -> AssignCompiledValue.Dot(this.expr(node.expr), node.field.node)
        is AssignTargetP.Index -> AssignCompiledValue.Index(this.expr(node.expr), this.expr(node.index))
        is AssignTargetP.Tuple -> AssignCompiledValue.Tuple(node.elements.map { assignTarget(it) })
        is AssignTargetP.Identifier -> {
            val name = node.ident.node.ident
            val bindingId = node.ident.node.payload ?: error("unresolved binding: `$name`")
            val binding = this.scopeData.getBinding(bindingId)
            val slot = binding.resolvedSlot(this.codemap) ?: error("unresolved slot")
            when {
                slot is Slot.Local && binding.captured == Captured.No ->
                    AssignCompiledValue.Local(LocalSlotId(slot.id.value))
                slot is Slot.Local && binding.captured == Captured.Yes ->
                    AssignCompiledValue.LocalCaptured(LocalCapturedSlotId(slot.id.value))
                slot is Slot.Module -> AssignCompiledValue.Module(slot.id, name)
                else -> error("unreachable")
            }
        }
    })
}

// fn assign_modify
private fun Compiler.assignModify(spanStmt: Span, lhs: CstAssignTarget, rhs: IrSpanned<ExprCompiled>, op: AssignOp): StmtsCompiled {
    val spanStmtFrame = FrameSpan.new(FrozenFileSpan.new(this.codemap, spanStmt))
    val spanLhs = FrameSpan.new(FrozenFileSpan.new(this.codemap, lhs.span))
    return when (val node = lhs.node) {
        is AssignTargetP.Dot -> {
            val e = this.expr(node.expr)
            StmtsCompiled.one(IrSpanned(span = spanStmtFrame,
                node = StmtCompiled.AssignModify(AssignModifyLhs.Dot(e, node.field.node), op, rhs)))
        }
        is AssignTargetP.Index -> {
            val e = this.expr(node.expr)
            val idx = this.expr(node.index)
            StmtsCompiled.one(IrSpanned(span = spanStmtFrame,
                node = StmtCompiled.AssignModify(AssignModifyLhs.Array(e, idx), op, rhs)))
        }
        is AssignTargetP.Identifier -> {
            val (slot, captured) = this.scopeData.getAssignIdentSlot(node.ident, this.codemap)
            val modifyLhs = when {
                slot is Slot.Local && captured == Captured.No ->
                    AssignModifyLhs.Local(IrSpanned(node = LocalSlotId(slot.id.value), span = spanLhs))
                slot is Slot.Local && captured == Captured.Yes ->
                    AssignModifyLhs.LocalCaptured(IrSpanned(node = LocalCapturedSlotId(slot.id.value), span = spanLhs))
                slot is Slot.Module ->
                    AssignModifyLhs.Module(IrSpanned(node = slot.id, span = spanLhs))
                else -> error("unreachable")
            }
            StmtsCompiled.one(IrSpanned(span = spanStmtFrame,
                node = StmtCompiled.AssignModify(modifyLhs, op, rhs)))
        }
        is AssignTargetP.Tuple -> error("Assign modify validates that the LHS is never a tuple")
    }
}

// There are two requirements to perform a GC:
//
// 1. We can't be profiling, since profiling relies on the redundant heap
//    entries. When profiling we set disable_gc.
// 2. We must be able to access all roots.
//
// We track as many roots as possible, and eventually aim to track them all, but
// for the moment we're only sure we have all roots when we are in the module
// evaluation eval. There are three roots we don't yet know about:
//
// 1. When evaluating an expression which has multiple subexpressions, e.g. List
//    we can't GC during that, as we can't see the root of the first list.
// 2. When evaluating inside a native function, especially if that native
//    function calls back to a non-native function, e.g. sort with a comparison
//    function.
// 3. When iterating we freeze the iteration variable, which means it
//    can't be moved by a GC. A special type of root.
//
// The first issue can be solved by moving to a bytecode interpreter and
// evaluation stack. The second issue can be solved by disabling GC while in
// such functions (it's probably rare). The third issue could be solved by
// making the freeze for iteration a separate flag to the RefCell, at the cost
// of an extra word in ValueMem. Or we could disable GC while iterating.
//
// For the moment we only GC when executing a statement at the root of the
// module, which we know is safe with respect to all three conditions.
//
// We also require that `extra_v` is None, since otherwise the user might have
// additional values stashed somewhere.

/** Perform a garbage collection if the allocated heap exceeds the threshold. */
internal fun possibleGc(eval: io.github.kotlinmania.starlark_kotlin.eval.runtime.Evaluator) {
    if (!eval.disableGc && eval.heap().allocatedBytes() >= eval.nextGcLevel) {
        // When we are at a module scope (as checked above) the eval contains
        // references to all values, so walking covers everything.
        eval.garbageCollect()
        eval.nextGcLevel = max(eval.heap().allocatedBytes() * 2, GC_THRESHOLD)
    }
}

/** Implement lhs |= rhs, which is special in Starlark, because dicts are mutated,
 * while all other types are not. */
internal fun bitOrAssign(lhs: Value, rhs: Value, heap: Heap): Result<Value> = runCatching {
    // The Starlark spec says dict |= mutates, while nothing else does.
    // When mutating, be careful if they alias, so we don't have `lhs`
    // mutably borrowed when we iterate over `rhs`, as they might alias.
    val lhsRef = lhs.getRef()
    val lhsTy = lhsRef.vtable().staticTypeOfValue.get()

    if (Dict.isDictType(lhsTy)) {
        val dict = DictMut.fromValue(lhs).getOrThrow()
        if (!lhs.ptrEq(rhs)) {
            val rhsDict = DictRef.fromValue(rhs) ?: throw ValueError.unsupportedOwned(
                lhsRef.vtable().typeName, "|=", rhs.getType())
            for ((k, v) in rhsDict.iterHashed()) {
                dict.aref.insertHashed(k, v)
            }
        }
        lhs
    } else {
        lhsRef.bitOr(rhs, heap).getOrThrow()
    }
}

/** Implement lhs += rhs, which is special in Starlark, because lists are mutated,
 * while all other types are not. */
internal fun addAssign(lhs: Value, rhs: Value, heap: Heap): Result<Value> {
    // Checking whether a value is an integer or a string is cheap (no virtual call),
    // and `Value::add` has optimizations for these types, so check them first
    // and delegate to `Value::add`.
    if (lhs.unpackInlineInt() != null || lhs.isStr()) {
        return lhs.add(rhs, heap)
    }

    // The Starlark spec says list += mutates, while nothing else does.
    // When mutating, be careful if they alias, so we don't have `lhs`
    // mutably borrowed when we iterate over `rhs`, as they might alias.

    // In practice, select is the only thing that implements radd.
    // If the users does x += select(...) we don't want an error,
    // we really want to x = x + select, so check radd first.
    val lhsRef = lhs.getRef()
    val lhsTy = lhsRef.vtable().staticTypeOfValue.get()

    if (ListData.isListType(lhsTy)) {
        val radd = rhs.getRef().radd(lhs, heap)
        if (radd != null) return radd
        return runCatching {
            val list = ListData.fromValueMut(lhs).getOrThrow()
            if (lhs.ptrEq(rhs)) {
                list.double(heap)
            } else {
                list.extend(rhs.iterate(heap).getOrThrow(), heap)
            }
            lhs
        }
    } else {
        return lhs.add(rhs, heap)
    }
}

// impl Compiler

/** Build a [StmtCompileContext]. */
internal fun Compiler.compileContext(hasReturnType: Boolean) = StmtCompileContext(hasReturnType = hasReturnType)

/** Compile a statement, optionally inserting a GC point before it. */
internal fun Compiler.stmt(stmt: CstStmt, allowGc: Boolean): StmtsCompiled {
    val span = FrameSpan.new(FrozenFileSpan.new(this.codemap, stmt.span))
    val isStatements = stmt.node is StmtP.Statements
    val res = stmtDirect(stmt, allowGc)
    // No point inserting a GC point around statements, since they will contain inner statements we can do
    return if (allowGc && !isStatements) {
        // We could do this more efficiently by fusing the possible_gc
        // into the inner closure, but no real need - we insert allow_gc fairly rarely
        val withGc = StmtsCompiled.one(IrSpanned(span = span, node = StmtCompiled.PossibleGc))
        withGc.extend(res)
        withGc
    } else {
        res
    }
}

/** Compile a module top-level statement. */
internal fun Compiler.moduleTopLevelStmt(stmt: CstStmt): StmtsCompiled = when (val node = stmt.node) {
    is StmtP.Statements -> error("top level statement lists are handled by outer loop")
    is StmtP.Expression -> {
        // When top level statement is an expression, compile it as return.
        // This is used to obtain the result of evaluation
        // of the last statement-expression in module.
        val wrappedStmt = Spanned(span = node.expr.span, node = StmtP.Return(node.expr))
        this.stmt(wrappedStmt, true)
    }
    else -> this.stmt(stmt, true)
}

private fun Compiler.stmtIf(span: FrameSpan, cond: CstExpr, thenBlock: CstStmt, allowGc: Boolean) =
    StmtsCompiled.ifStmt(span, this.expr(cond), this.stmt(thenBlock, allowGc), StmtsCompiled.empty())

private fun Compiler.stmtIfElse(span: FrameSpan, cond: CstExpr, thenBlock: CstStmt, elseBlock: CstStmt, allowGc: Boolean) =
    StmtsCompiled.ifStmt(span, this.expr(cond), this.stmt(thenBlock, allowGc), this.stmt(elseBlock, allowGc))

private fun Compiler.stmtExpr(expr: CstExpr) = StmtsCompiled.expr(this.expr(expr))

/** Core statement compilation dispatch. */
private fun Compiler.stmtDirect(stmt: CstStmt, allowGc: Boolean): StmtsCompiled {
    val span = FrameSpan.new(FrozenFileSpan.new(this.codemap, stmt.span))
    return when (val node = stmt.node) {
        is StmtP.Def -> {
            val def = node.defStmt
            val signatureSpan = def.signatureSpan()
            val frozenSignatureSpan = FrozenFileSpan.new(this.codemap, signatureSpan)
            val rhs = IrSpanned(span = span, node = this.function(
                def.name.node.ident, frozenSignatureSpan, def.payload,
                def.params, def.returnType, def.body,
            ))
            val lhs = assignTarget(Spanned(span = def.name.span, node = AssignTargetP.Identifier(def.name)))
            StmtsCompiled.one(IrSpanned(span = span, node = StmtCompiled.Assign(lhs, null, rhs)))
        }
        is StmtP.For -> {
            val over = listToTuple(node.forStmt.over)
            val variable = assignTarget(node.forStmt.varTarget)
            val overCompiled = this.expr(over)
            val st = this.stmt(node.forStmt.body, false)
            StmtsCompiled.forStmt(span, variable, overCompiled, st)
        }
        is StmtP.Return -> {
            val e = if (node.expr != null) this.expr(node.expr)
                    else IrSpanned(span = span, node = ExprCompiled.ValueExpr(FrozenValue.newNone()))
            StmtsCompiled.one(IrSpanned(span = span, node = StmtCompiled.Return(e)))
        }
        is StmtP.If -> stmtIf(span, node.cond, node.suite, allowGc)
        is StmtP.IfElse -> stmtIfElse(span, node.cond, node.suite1, node.suite2, allowGc)
        is StmtP.Statements -> {
            val r = StmtsCompiled.empty()
            for (s in node.stmts) {
                if (r.isTerminal()) break
                r.extend(this.stmt(s, allowGc))
            }
            r
        }
        is StmtP.Expression -> stmtExpr(node.expr)
        is StmtP.Assign -> {
            val rhs = this.expr(node.assign.rhs)
            val ty = this.exprForType(node.assign.ty)
            val lhs = assignTarget(node.assign.lhs)
            StmtsCompiled.one(IrSpanned(span = span, node = StmtCompiled.Assign(lhs, ty, rhs)))
        }
        is StmtP.AssignModify -> {
            val rhs = this.expr(node.rhs)
            assignModify(span.span.span(), node.lhs, rhs, node.op)
        }
        is StmtP.Load -> error("unreachable")
        is StmtP.Pass -> StmtsCompiled.empty()
        is StmtP.Break -> StmtsCompiled.one(IrSpanned(span = span, node = StmtCompiled.Break))
        is StmtP.Continue -> StmtsCompiled.one(IrSpanned(span = span, node = StmtCompiled.Continue))
    }
}
