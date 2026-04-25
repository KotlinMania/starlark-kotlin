// port-lint: source src/eval/compiler/stmt.rs
package io.github.kotlinmania.starlark.eval.compiler

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

/**
 * Evaluation environment, provide converters from Ast* element to value.
 *
 * # <a name="build_file"></a>Starlark and BUILD dialect
 *
 * All evaluation function can evaluate the full Starlark language (i.e.
 * Bazel's .bzl files) or the BUILD file dialect (i.e. used to interpret
 * Bazel's BUILD file). The BUILD dialect does not allow `def` statements.
 */

import io.github.kotlinmania.starlark.codemap.Span
import io.github.kotlinmania.starlark.codemap.Spanned
import io.github.kotlinmania.starlark.environment.FrozenModuleData
import io.github.kotlinmania.starlark.environment.ModuleSlotId
import io.github.kotlinmania.starlark.eval.compiler.scope.CstPayload
import io.github.kotlinmania.starlark.eval.compiler.optctx.OptCtx
import io.github.kotlinmania.starlark.eval.runtime.Evaluator
import io.github.kotlinmania.starlark.eval.runtime.FrameSpan
import io.github.kotlinmania.starlark.eval.runtime.GC_THRESHOLD
import io.github.kotlinmania.starlark.eval.runtime.LocalCapturedSlotId
import io.github.kotlinmania.starlark.eval.runtime.LocalSlotId
import io.github.kotlinmania.starlark.eval.runtime.frozenfilespan.FrozenFileSpan
import io.github.kotlinmania.starlark.syntax.ast.AssignOp
import io.github.kotlinmania.starlark.syntax.ast.AssignIdentP
import io.github.kotlinmania.starlark.syntax.ast.AssignTargetP
import io.github.kotlinmania.starlark.syntax.ast.DefP
import io.github.kotlinmania.starlark.syntax.ast.ExprP
import io.github.kotlinmania.starlark.syntax.ast.ForP
import io.github.kotlinmania.starlark.syntax.ast.StmtP
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.ValueError
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.types.dict.Dict
import io.github.kotlinmania.starlark.values.types.dict.DictMut
import io.github.kotlinmania.starlark.values.types.dict.DictRef
import io.github.kotlinmania.starlark.values.types.dict.Either
import io.github.kotlinmania.starlark.values.types.dict.dictMutFromValue
import io.github.kotlinmania.starlark.values.types.dict.dictRefFromValue
import io.github.kotlinmania.starlark.values.types.list.ListData
import io.github.kotlinmania.starlark.values.typing.typecompiled.TypeCompiled

// #[derive(Clone, Debug)]
// pub(crate) enum AssignModifyLhs {
//     Dot(IrSpanned<ExprCompiled>, String),
//     Array(IrSpanned<ExprCompiled>, IrSpanned<ExprCompiled>),
//     Local(IrSpanned<LocalSlotId>),
//     LocalCaptured(IrSpanned<LocalCapturedSlotId>),
//     Module(IrSpanned<ModuleSlotId>),
// }
internal sealed class AssignModifyLhs {
    data class Dot(val expr: IrSpanned<ExprCompiled>, val name: String) : AssignModifyLhs()
    data class Array(val expr: IrSpanned<ExprCompiled>, val index: IrSpanned<ExprCompiled>) : AssignModifyLhs()
    data class Local(val slot: IrSpanned<LocalSlotId>) : AssignModifyLhs()
    data class LocalCaptured(val slot: IrSpanned<LocalCapturedSlotId>) : AssignModifyLhs()
    data class Module(val slot: IrSpanned<ModuleSlotId>) : AssignModifyLhs()
}

// impl AssignModifyLhs
// fn optimize(&self, ctx: &mut OptCtx) -> AssignModifyLhs
internal fun AssignModifyLhs.optimize(ctx: OptCtx): AssignModifyLhs {
    return when (this) {
        is AssignModifyLhs.Dot -> AssignModifyLhs.Dot(expr.optimize(ctx), name)
        is AssignModifyLhs.Array -> AssignModifyLhs.Array(expr.optimize(ctx), index.optimize(ctx))
        is AssignModifyLhs.Local -> this
        is AssignModifyLhs.LocalCaptured -> this
        is AssignModifyLhs.Module -> this
    }
}

// #[derive(Clone, Debug)]
// pub(crate) enum StmtCompiled {
//     PossibleGc,
//     Return(IrSpanned<ExprCompiled>),
//     Expr(IrSpanned<ExprCompiled>),
//     Assign(IrSpanned<AssignCompiledValue>, Option<IrSpanned<TypeCompiled>>, IrSpanned<ExprCompiled>),
//     AssignModify(AssignModifyLhs, AssignOp, IrSpanned<ExprCompiled>),
//     If(Box<(IrSpanned<ExprCompiled>, StmtsCompiled, StmtsCompiled)>),
//     For(Box<(IrSpanned<AssignCompiledValue>, IrSpanned<ExprCompiled>, StmtsCompiled)>),
//     Break,
//     Continue,
// }
internal sealed class StmtCompiled {
    data object PossibleGc : StmtCompiled()
    data class Return(val expr: IrSpanned<ExprCompiled>) : StmtCompiled()
    data class Expr(val expr: IrSpanned<ExprCompiled>) : StmtCompiled()
    data class Assign(
        val lhs: IrSpanned<AssignCompiledValue>,
        val ty: IrSpanned<TypeCompiled>?,
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

// impl IrSpanned<StmtCompiled>
// fn optimize(&self, ctx: &mut OptCtx) -> StmtsCompiled
internal fun IrSpanned<StmtCompiled>.optimize(ctx: OptCtx): StmtsCompiled {
    val span = this.span
    return when (val s = this.node) {
        is StmtCompiled.Return -> StmtsCompiled.one(IrSpanned(
            span,
            StmtCompiled.Return(s.expr.optimize(ctx)),
        ))
        is StmtCompiled.Expr -> {
            val expr = s.expr.optimize(ctx)
            StmtsCompiled.expr(expr)
        }
        is StmtCompiled.Assign -> {
            val lhs = s.lhs.optimize(ctx)
            val rhs = s.rhs.optimize(ctx)
            StmtsCompiled.one(IrSpanned(
                span,
                StmtCompiled.Assign(lhs, s.ty, rhs),
            ))
        }
        is StmtCompiled.If -> {
            val cond = s.cond.optimize(ctx)
            val t = s.thenBlock.optimize(ctx)
            val f = s.elseBlock.optimize(ctx)
            StmtsCompiled.ifStmt(span, cond, t, f)
        }
        is StmtCompiled.For -> {
            val variable = s.variable.optimize(ctx)
            val over = s.over.optimize(ctx)
            val body = s.body.optimize(ctx)
            StmtsCompiled.forStmt(span, variable, over, body)
        }
        is StmtCompiled.PossibleGc,
        is StmtCompiled.Break,
        is StmtCompiled.Continue -> {
            StmtsCompiled.one(IrSpanned(span, s))
        }
        is StmtCompiled.AssignModify -> StmtsCompiled.one(IrSpanned(
            span,
            StmtCompiled.AssignModify(s.lhs.optimize(ctx), s.op, s.rhs.optimize(ctx)),
        ))
    }
}

// #[derive(Debug, Default)]
// pub(crate) struct StmtCompileContext {
//     pub(crate) has_return_type: bool,
// }
internal data class StmtCompileContext(
    /** Current function has return type. */
    val hasReturnType: Boolean = false,
)

// pub(crate) struct OptimizeOnFreezeContext<'v, 'a> {
//     pub(crate) module: &'a FrozenModuleData,
//     pub(crate) heap: Heap<'v>,
//     pub(crate) frozen_heap: &'a FrozenHeap,
// }
internal class OptimizeOnFreezeContext(
    internal val module: FrozenModuleData,
    /**
     * Nothing useful should be left in the heap after the freeze,
     * but having a heap is useful to allocate objects temporarily
     * (when invoking operations which require heap).
     */
    internal val heap: Heap,
    internal val frozenHeap: FrozenHeap,
)

// #[derive(Clone, Debug)]
// pub(crate) struct StmtsCompiled(SmallVec1<IrSpanned<StmtCompiled>>)
internal class StmtsCompiled(
    private var stmts: SmallVec1<IrSpanned<StmtCompiled>>,
) {

    companion object {
        // pub(crate) fn empty() -> StmtsCompiled
        fun empty(): StmtsCompiled {
            return StmtsCompiled(SmallVec1.Vec(mutableListOf()))
        }

        // pub(crate) fn one(stmt: IrSpanned<StmtCompiled>) -> StmtsCompiled
        fun one(stmt: IrSpanned<StmtCompiled>): StmtsCompiled {
            return StmtsCompiled(SmallVec1.One(stmt))
        }

        // fn expr(expr: IrSpanned<ExprCompiled>) -> StmtsCompiled
        fun expr(expr: IrSpanned<ExprCompiled>): StmtsCompiled {
            val span = expr.span
            return when {
                expr.node.isPureInfallible() -> empty()
                expr.node is ExprCompiled.ListExpr -> {
                    val stmts = empty()
                    for (x in (expr.node).elements) {
                        stmts.extend(expr(x))
                    }
                    stmts
                }
                expr.node is ExprCompiled.TupleExpr -> {
                    val stmts = empty()
                    for (x in (expr.node).elements) {
                        stmts.extend(expr(x))
                    }
                    stmts
                }
                // Unwrap infallible expressions.
                expr.node is ExprCompiled.Builtin1Expr && (
                    (expr.node).op is Builtin1.Not ||
                    (expr.node).op is Builtin1.TypeIs
                ) -> expr((expr.node).expr)
                // "And" and "or" for effect are equivalent to `if`.
                expr.node is ExprCompiled.LogicalBinOp &&
                    (expr.node).op == ExprLogicalBinOp.And -> {
                    val binOp = expr.node
                    ifStmt(expr.span, binOp.lhs, expr(binOp.rhs), empty())
                }
                expr.node is ExprCompiled.LogicalBinOp &&
                    (expr.node).op == ExprLogicalBinOp.Or -> {
                    val binOp = expr.node
                    ifStmt(expr.span, binOp.lhs, empty(), expr(binOp.rhs))
                }
                else -> {
                    val ty = expr.node.asType()
                    if (ty != null) {
                        expr(ty)
                    } else {
                        one(IrSpanned(span, StmtCompiled.Expr(IrSpanned(span, expr.node))))
                    }
                }
            }
        }

        // fn if_stmt(span, cond, t, f) -> StmtsCompiled
        fun ifStmt(
            span: FrameSpan,
            cond: IrSpanned<ExprCompiled>,
            t: StmtsCompiled,
            f: StmtsCompiled,
        ): StmtsCompiled {
            val condBool = ExprCompiledBool.new(cond)
            return when (condBool.node) {
                is ExprCompiledBool.Const -> {
                    if ((condBool.node).b) t else f
                }
                is ExprCompiledBool.Expr -> {
                    val condExpr = (condBool.node).expr
                    when {
                        condExpr is ExprCompiled.Builtin1Expr && condExpr.op is Builtin1.Not ->
                            ifStmt(span, condExpr.expr, f, t)
                        condExpr is ExprCompiled.Seq -> {
                            val stmt = empty()
                            stmt.extend(expr(condExpr.first))
                            stmt.extend(ifStmt(span, condExpr.second, t, f))
                            stmt
                        }
                        else -> {
                            val condSpanned = IrSpanned(span, condExpr)
                            if (t.isEmpty() && f.isEmpty()) {
                                expr(condSpanned)
                            } else {
                                one(IrSpanned(
                                    span,
                                    StmtCompiled.If(condSpanned, t, f),
                                ))
                            }
                        }
                    }
                }
            }
        }

        // fn for_stmt(span, var, over, body) -> StmtsCompiled
        fun forStmt(
            span: FrameSpan,
            variable: IrSpanned<AssignCompiledValue>,
            over: IrSpanned<ExprCompiled>,
            body: StmtsCompiled,
        ): StmtsCompiled {
            if (over.node.isIterableEmpty()) {
                return empty()
            }
            return one(IrSpanned(
                span,
                StmtCompiled.For(variable, over, body),
            ))
        }
    }

    // pub(crate) fn is_empty(&self) -> bool
    fun isEmpty(): Boolean {
        val s = stmts
        return when (s) {
            is SmallVec1.One -> false
            is SmallVec1.Vec -> s.values.isEmpty()
        }
    }

    // pub(crate) fn stmts(&self) -> &[IrSpanned<StmtCompiled>]
    fun stmts(): List<IrSpanned<StmtCompiled>> {
        return stmts.asSlice()
    }

    /** Last statement in this block is `break`, `continue` or `return`. */
    // fn is_terminal(&self) -> bool
    internal fun isTerminal(): Boolean {
        val last = last() ?: return false
        return when (last.node) {
            is StmtCompiled.Break,
            is StmtCompiled.Continue,
            is StmtCompiled.Return -> true
            else -> false
        }
    }

    // pub(crate) fn extend(&mut self, right: StmtsCompiled)
    fun extend(right: StmtsCompiled) {
        // Do not add any code after `break`, `continue` or `return`.
        if (isTerminal()) {
            return
        }
        stmts = stmts.extend(right.stmts)
    }

    // pub(crate) fn optimize(&self, ctx: &mut OptCtx) -> StmtsCompiled
    fun optimize(ctx: OptCtx): StmtsCompiled {
        val result = empty()
        val s = stmts
        when (s) {
            is SmallVec1.One -> result.extend(s.value.optimize(ctx))
            is SmallVec1.Vec -> {
                for (item in s.values) {
                    if (result.isTerminal()) {
                        break
                    }
                    result.extend(item.optimize(ctx))
                }
            }
        }
        return result
    }

    // pub(crate) fn first(&self) -> Option<&IrSpanned<StmtCompiled>>
    fun first(): IrSpanned<StmtCompiled>? {
        val s = stmts
        return when (s) {
            is SmallVec1.One -> s.value
            is SmallVec1.Vec -> s.values.firstOrNull()
        }
    }

    // pub(crate) fn last(&self) -> Option<&IrSpanned<StmtCompiled>>
    fun last(): IrSpanned<StmtCompiled>? {
        val s = stmts
        return when (s) {
            is SmallVec1.One -> s.value
            is SmallVec1.Vec -> s.values.lastOrNull()
        }
    }
}

// #[derive(Debug, Error)]
// pub(crate) enum AssignError {
//     #[error("Unpacked {1} values but expected {0}")]
//     IncorrectNumberOfValueToUnpack(i32, i32),
// }
internal class AssignError {
    class IncorrectNumberOfValueToUnpack(expected: Int, got: Int) :
        Exception("Unpacked $got values but expected $expected")
}

// #[derive(Clone, Debug, VisitSpanMut)]
// pub(crate) enum AssignCompiledValue {
//     Dot(IrSpanned<ExprCompiled>, String),
//     Index(IrSpanned<ExprCompiled>, IrSpanned<ExprCompiled>),
//     Tuple(Vec<IrSpanned<AssignCompiledValue>>),
//     Local(LocalSlotId),
//     LocalCaptured(LocalCapturedSlotId),
//     Module(ModuleSlotId, String),
// }
internal sealed class AssignCompiledValue {
    data class Dot(val obj: IrSpanned<ExprCompiled>, val field: String) : AssignCompiledValue()
    data class Index(val array: IrSpanned<ExprCompiled>, val index: IrSpanned<ExprCompiled>) : AssignCompiledValue()
    data class Tuple(val elements: List<IrSpanned<AssignCompiledValue>>) : AssignCompiledValue()
    data class Local(val slot: LocalSlotId) : AssignCompiledValue()
    data class LocalCaptured(val slot: LocalCapturedSlotId) : AssignCompiledValue()
    data class Module(val slot: ModuleSlotId, val name: String) : AssignCompiledValue()
}

// impl AssignCompiledValue
// fn as_local_non_captured(&self) -> Option<LocalSlotId>
/** Assignment to a local non-captured variable. */
internal fun AssignCompiledValue.asLocalNonCaptured(): LocalSlotId? {
    return when (this) {
        is AssignCompiledValue.Local -> slot
        else -> null
    }
}

// impl IrSpanned<AssignCompiledValue>
// pub(crate) fn optimize(&self, ctx: &mut OptCtx) -> IrSpanned<AssignCompiledValue>
internal fun IrSpanned<AssignCompiledValue>.optimize(ctx: OptCtx): IrSpanned<AssignCompiledValue> {
    val span = this.span
    val assign = when (val n = this.node) {
        is AssignCompiledValue.Dot -> {
            AssignCompiledValue.Dot(n.obj.optimize(ctx), n.field)
        }
        is AssignCompiledValue.Index -> {
            AssignCompiledValue.Index(n.array.optimize(ctx), n.index.optimize(ctx))
        }
        is AssignCompiledValue.Tuple -> {
            AssignCompiledValue.Tuple(n.elements.map { it.optimize(ctx) })
        }
        is AssignCompiledValue.Local,
        is AssignCompiledValue.LocalCaptured,
        is AssignCompiledValue.Module -> n
    }
    return IrSpanned(span, assign)
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

// pub(crate) fn possible_gc(eval: &mut Evaluator)
internal fun possibleGc(eval: Evaluator) {
    if (!eval.disableGc && eval.heap().allocatedBytes() >= eval.nextGcLevel.toLong()) {
        eval.garbageCollect()
        eval.nextGcLevel = maxOf(eval.heap().allocatedBytes().toLong() * 2, GC_THRESHOLD.toLong()).toInt()
    }
}

/**
 * Implement lhs |= rhs, which is special in Starlark, because dicts are mutated,
 * while all other types are not.
 */
// pub(crate) fn bit_or_assign<'v>(lhs: Value<'v>, rhs: Value<'v>, heap: Heap<'v>) -> crate::Result<Value<'v>>
internal fun bitOrAssign(lhs: Value, rhs: Value, heap: Heap): Result<Value> {
    // The Starlark spec says dict |= mutates, while nothing else does.
    // When mutating, be careful if they alias, so we don't have `lhs`
    // mutably borrowed when we iterate over `rhs`, as they might alias.

    val lhsRef = lhs.getRef()
    val lhsTy = lhsRef.vtable().staticTypeOfValue.get()

    if (Dict.isDictType(lhsTy)) {
        val dict = dictMutFromValue(lhs).getOrElse { return Result.failure(it) }
        if (lhs.ptrEq(rhs)) {
            // Nothing to do as union is idempotent
        } else {
            val rhsDict = dictRefFromValue(rhs)
            if (rhsDict == null) {
                return ValueError.unsupportedOwned(
                    lhsRef.vtable().typeName,
                    "|=",
                    rhs.getType(),
                )
            }
            val rhsDictDeref = when (val ref = rhsDict.aref) {
                is Either.Left -> ref.value.value
                is Either.Right -> ref.value
            }
            for ((k, v) in rhsDictDeref.iterHashed()) {
                dict.aref.value.insertHashed(k, v)
            }
        }
        return Result.success(lhs)
    } else {
        return lhsRef.bitOr(rhs, heap)
    }
}

/**
 * Implement lhs += rhs, which is special in Starlark, because lists are mutated,
 * while all other types are not.
 */
// pub(crate) fn add_assign<'v>(lhs: Value<'v>, rhs: Value<'v>, heap: Heap<'v>) -> crate::Result<Value<'v>>
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
        if (radd != null) {
            return radd
        }
        val list = ListData.fromValueMut(lhs).getOrElse { return Result.failure(it) }
        if (lhs.ptrEq(rhs)) {
            list.double()
        } else {
            val iter = rhs.iterate(heap).getOrElse { return Result.failure(it) }
            list.extend(Iterable { iter })
        }
        return Result.success(lhs)
    } else {
        return lhs.add(rhs, heap)
    }
}

// impl Compiler

// pub(crate) fn compile_context(&self, has_return_type: bool) -> StmtCompileContext
internal fun Compiler.compileContext(hasReturnType: Boolean): StmtCompileContext {
    return StmtCompileContext(hasReturnType = hasReturnType)
}

// pub fn assign_target(&mut self, expr: &CstAssignTarget) -> Result<IrSpanned<AssignCompiledValue>, CompilerInternalError>
internal fun Compiler.assignTarget(
    expr: Spanned<AssignTargetP<CstPayload>>,
): Result<IrSpanned<AssignCompiledValue>> {
    val span = FrameSpan.new(FrozenFileSpan.new(this.codemap, expr.span))
    val assign = when (val node = expr.node) {
        is AssignTargetP.Dot -> {
            val e = this.expr(node.expr).getOrElse { return Result.failure(it) }
            AssignCompiledValue.Dot(e, node.field.node)
        }
        is AssignTargetP.Index -> {
            val e = this.expr(node.expr).getOrElse { return Result.failure(it) }
            val idx = this.expr(node.index).getOrElse { return Result.failure(it) }
            AssignCompiledValue.Index(e, idx)
        }
        is AssignTargetP.Tuple -> {
            val v = node.elements.map { x ->
                assignTarget(x).getOrElse { return Result.failure(it) }
            }
            AssignCompiledValue.Tuple(v)
        }
        is AssignTargetP.Identifier<CstPayload, *> -> {
            val name = node.ident.node.ident
            val bindingId = (node.ident.node.payload as BindingId?)
                ?: error("unresolved binding: `$name`")
            val binding = this.scopeData.getBinding(bindingId)
            val slot = binding.resolvedSlot(this.codemap.value)
            when {
                slot is Slot.Local && binding.captured == Captured.No ->
                    AssignCompiledValue.Local(LocalSlotId(slot.id.index))
                slot is Slot.Local && binding.captured == Captured.Yes ->
                    AssignCompiledValue.LocalCaptured(LocalCapturedSlotId(slot.id.index))
                slot is Slot.Module ->
                    AssignCompiledValue.Module(slot.id, name)
                else -> error("unreachable")
            }
        }
    }
    return Result.success(IrSpanned(span, assign))
}

// fn assign_modify(&mut self, span_stmt: Span, lhs: &CstAssignTarget, rhs: IrSpanned<ExprCompiled>, op: AssignOp) -> Result<StmtsCompiled, CompilerInternalError>
private fun Compiler.assignModify(
    spanStmt: Span,
    lhs: Spanned<AssignTargetP<CstPayload>>,
    rhs: IrSpanned<ExprCompiled>,
    op: AssignOp,
): Result<StmtsCompiled> {
    val spanStmtFrame = FrameSpan.new(FrozenFileSpan.new(this.codemap, spanStmt))
    val spanLhs = FrameSpan.new(FrozenFileSpan.new(this.codemap, lhs.span))
    return when (val node = lhs.node) {
        is AssignTargetP.Dot -> {
            val e = this.expr(node.expr).getOrElse { return Result.failure(it) }
            Result.success(StmtsCompiled.one(IrSpanned(
                spanStmtFrame,
                StmtCompiled.AssignModify(
                    AssignModifyLhs.Dot(e, node.field.node),
                    op,
                    rhs,
                ),
            )))
        }
        is AssignTargetP.Index -> {
            val e = this.expr(node.expr).getOrElse { return Result.failure(it) }
            val idx = this.expr(node.index).getOrElse { return Result.failure(it) }
            Result.success(StmtsCompiled.one(IrSpanned(
                spanStmtFrame,
                StmtCompiled.AssignModify(AssignModifyLhs.Array(e, idx), op, rhs),
            )))
        }
        is AssignTargetP.Identifier<CstPayload, *> -> {
            val ident = node.ident
            val (slot, captured) = this.scopeData.getAssignIdentSlot(ident, this.codemap.value)
            when {
                slot is Slot.Local && captured == Captured.No -> {
                    val lhsSpanned = IrSpanned(spanLhs, LocalSlotId(slot.id.index))
                    Result.success(StmtsCompiled.one(IrSpanned(
                        spanStmtFrame,
                        StmtCompiled.AssignModify(AssignModifyLhs.Local(lhsSpanned), op, rhs),
                    )))
                }
                slot is Slot.Local && captured == Captured.Yes -> {
                    val lhsSpanned = IrSpanned(spanLhs, LocalCapturedSlotId(slot.id.index))
                    Result.success(StmtsCompiled.one(IrSpanned(
                        spanStmtFrame,
                        StmtCompiled.AssignModify(AssignModifyLhs.LocalCaptured(lhsSpanned), op, rhs),
                    )))
                }
                slot is Slot.Module -> {
                    val lhsSpanned = IrSpanned(spanLhs, slot.id)
                    Result.success(StmtsCompiled.one(IrSpanned(
                        spanStmtFrame,
                        StmtCompiled.AssignModify(AssignModifyLhs.Module(lhsSpanned), op, rhs),
                    )))
                }
                else -> error("unreachable")
            }
        }
        is AssignTargetP.Tuple -> {
            error("Assign modify validates that the LHS is never a tuple")
        }
    }
}

// pub(crate) fn stmt(&mut self, stmt: &CstStmt, allow_gc: bool) -> Result<StmtsCompiled, CompilerInternalError>
internal fun Compiler.stmt(
    stmt: Spanned<StmtP<CstPayload>>,
    allowGc: Boolean,
): Result<StmtsCompiled> {
    val span = FrameSpan.new(FrozenFileSpan.new(this.codemap, stmt.span))
    val isStatements = stmt.node is StmtP.Statements
    val res = stmtDirect(stmt, allowGc).getOrElse { return Result.failure(it) }
    // No point inserting a GC point around statements, since they will contain inner statements we can do
    return if (allowGc && !isStatements) {
        // We could do this more efficiently by fusing the possible_gc
        // into the inner closure, but no real need - we insert allow_gc fairly rarely
        val withGc = StmtsCompiled.one(IrSpanned(
            span,
            StmtCompiled.PossibleGc,
        ))
        withGc.extend(res)
        Result.success(withGc)
    } else {
        Result.success(res)
    }
}

// pub(crate) fn module_top_level_stmt(&mut self, stmt: &CstStmt) -> Result<StmtsCompiled, CompilerInternalError>
internal fun Compiler.moduleTopLevelStmt(
    stmt: Spanned<StmtP<CstPayload>>,
): Result<StmtsCompiled> {
    return when (val node = stmt.node) {
        is StmtP.Statements -> {
            error("top level statement lists are handled by outer loop")
        }
        is StmtP.Expression -> {
            val wrappedStmt = Spanned(
                // When top level statement is an expression, compile it as return.
                // This is used to obtain the result of evaluation
                // of the last statement-expression in module.
                StmtP.Return<CstPayload>(node.expr),
                stmt.span,
            )
            this.stmt(wrappedStmt, true)
        }
        else -> this.stmt(stmt, true)
    }
}

// fn stmt_if(&mut self, span, cond, then_block, allow_gc) -> Result<StmtsCompiled, CompilerInternalError>
private fun Compiler.stmtIf(
    span: FrameSpan,
    cond: Spanned<ExprP<CstPayload>>,
    thenBlock: Spanned<StmtP<CstPayload>>,
    allowGc: Boolean,
): Result<StmtsCompiled> {
    val condCompiled = this.expr(cond).getOrElse { return Result.failure(it) }
    val thenCompiled = this.stmt(thenBlock, allowGc).getOrElse { return Result.failure(it) }
    return Result.success(StmtsCompiled.ifStmt(
        span,
        condCompiled,
        thenCompiled,
        StmtsCompiled.empty(),
    ))
}

// fn stmt_if_else(&mut self, span, cond, then_block, else_block, allow_gc) -> Result<StmtsCompiled, CompilerInternalError>
private fun Compiler.stmtIfElse(
    span: FrameSpan,
    cond: Spanned<ExprP<CstPayload>>,
    thenBlock: Spanned<StmtP<CstPayload>>,
    elseBlock: Spanned<StmtP<CstPayload>>,
    allowGc: Boolean,
): Result<StmtsCompiled> {
    val condCompiled = this.expr(cond).getOrElse { return Result.failure(it) }
    val thenCompiled = this.stmt(thenBlock, allowGc).getOrElse { return Result.failure(it) }
    val elseCompiled = this.stmt(elseBlock, allowGc).getOrElse { return Result.failure(it) }
    return Result.success(StmtsCompiled.ifStmt(span, condCompiled, thenCompiled, elseCompiled))
}

// fn stmt_expr(&mut self, expr: &CstExpr) -> Result<StmtsCompiled, CompilerInternalError>
private fun Compiler.stmtExpr(expr: Spanned<ExprP<CstPayload>>): Result<StmtsCompiled> {
    val compiled = this.expr(expr).getOrElse { return Result.failure(it) }
    return Result.success(StmtsCompiled.expr(compiled))
}

// fn stmt_direct(&mut self, stmt: &CstStmt, allow_gc: bool) -> Result<StmtsCompiled, CompilerInternalError>
private fun Compiler.stmtDirect(
    stmt: Spanned<StmtP<CstPayload>>,
    allowGc: Boolean,
): Result<StmtsCompiled> {
    val span = FrameSpan.new(FrozenFileSpan.new(this.codemap, stmt.span))
    return when (val node = stmt.node) {
        is StmtP.Def<CstPayload, *> -> {
            val defP = node.def
            val signatureSpan = defP.signatureSpan()
            val frozenSignatureSpan = FrozenFileSpan.new(this.codemap, signatureSpan)
            val rhs = IrSpanned(
                span,
                runCatching {
                    this.function(
                        defP.name.node.ident,
                        frozenSignatureSpan,
                        defP.payload as ScopeId,
                        defP.params,
                        defP.returnType,
                        defP.body,
                    )
                }.getOrElse { return Result.failure(it) },
            )
            @Suppress("UNCHECKED_CAST")
            val defName = defP.name as Spanned<AssignIdentP<CstPayload, BindingId?>>
            val lhs = assignTarget(Spanned(
                AssignTargetP.Identifier<CstPayload, BindingId?>(defName),
                defName.span,
            )).getOrElse { return Result.failure(it) }
            Result.success(StmtsCompiled.one(IrSpanned(
                span,
                StmtCompiled.Assign(lhs, null, rhs),
            )))
        }
        is StmtP.For -> {
            val over = listToTuple(node.forStmt.over)
            val variable = assignTarget(node.forStmt.varTarget).getOrElse { return Result.failure(it) }
            val overCompiled = this.expr(over).getOrElse { return Result.failure(it) }
            val st = this.stmt(node.forStmt.body, false).getOrElse { return Result.failure(it) }
            Result.success(StmtsCompiled.forStmt(span, variable, overCompiled, st))
        }
        is StmtP.Return -> {
            if (node.expr == null) {
                Result.success(StmtsCompiled.one(IrSpanned(
                    span,
                    StmtCompiled.Return(IrSpanned(span, ExprCompiled.ValueExpr(FrozenValue.newNone()))),
                )))
            } else {
                val e = this.expr(node.expr).getOrElse { return Result.failure(it) }
                Result.success(StmtsCompiled.one(IrSpanned(
                    span,
                    StmtCompiled.Return(e),
                )))
            }
        }
        is StmtP.If -> stmtIf(span, node.cond, node.suite, allowGc)
        is StmtP.IfElse -> stmtIfElse(span, node.cond, node.suite1, node.suite2, allowGc)
        is StmtP.Statements -> {
            val r = StmtsCompiled.empty()
            for (s in node.stmts) {
                if (r.isTerminal()) {
                    break
                }
                r.extend(this.stmt(s, allowGc).getOrElse { return Result.failure(it) })
            }
            Result.success(r)
        }
        is StmtP.Expression -> stmtExpr(node.expr)
        is StmtP.Assign -> {
            val rhs = this.expr(node.assign.rhs).getOrElse { return Result.failure(it) }
            val ty = this.exprForType(node.assign.ty)
            val lhs = assignTarget(node.assign.lhs).getOrElse { return Result.failure(it) }
            Result.success(StmtsCompiled.one(IrSpanned(
                span,
                StmtCompiled.Assign(lhs, ty, rhs),
            )))
        }
        is StmtP.AssignModify -> {
            val rhs = this.expr(node.rhs).getOrElse { return Result.failure(it) }
            assignModify(span.span.span(), node.lhs, rhs, node.op)
        }
        is StmtP.Load<CstPayload, *> -> error("unreachable")
        is StmtP.Pass -> Result.success(StmtsCompiled.empty())
        is StmtP.Break -> Result.success(StmtsCompiled.one(IrSpanned(
            span,
            StmtCompiled.Break,
        )))
        is StmtP.Continue -> Result.success(StmtsCompiled.one(IrSpanned(
            span,
            StmtCompiled.Continue,
        )))
    }
}
