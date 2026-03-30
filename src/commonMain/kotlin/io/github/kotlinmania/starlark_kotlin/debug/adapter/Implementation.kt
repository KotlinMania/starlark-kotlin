// port-lint: source src/debug/adapter/implementation.rs
package io.github.kotlinmania.starlark_kotlin.debug.adapter

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

import io.github.kotlinmania.starlark_kotlin.codemap.FileSpan
import io.github.kotlinmania.starlark_kotlin.codemap.FileSpanRef
import io.github.kotlinmania.starlark_kotlin.codemap.Span
import io.github.kotlinmania.starlark_kotlin.debug.Breakpoint
import io.github.kotlinmania.starlark_kotlin.debug.DapAdapter
import io.github.kotlinmania.starlark_kotlin.debug.DapAdapterClient
import io.github.kotlinmania.starlark_kotlin.debug.DapAdapterEvalHook
import io.github.kotlinmania.starlark_kotlin.debug.DapBreakpoint
import io.github.kotlinmania.starlark_kotlin.debug.EvaluateExprInfo
import io.github.kotlinmania.starlark_kotlin.debug.InspectVariableInfo
import io.github.kotlinmania.starlark_kotlin.debug.PathSegment
import io.github.kotlinmania.starlark_kotlin.debug.ResolvedBreakpoints
import io.github.kotlinmania.starlark_kotlin.debug.ScopesInfo
import io.github.kotlinmania.starlark_kotlin.debug.SetBreakpointsArguments
import io.github.kotlinmania.starlark_kotlin.debug.SetBreakpointsResponseBody
import io.github.kotlinmania.starlark_kotlin.debug.StackFrame
import io.github.kotlinmania.starlark_kotlin.debug.StackTraceArguments
import io.github.kotlinmania.starlark_kotlin.debug.StackTraceResponseBody
import io.github.kotlinmania.starlark_kotlin.debug.StepKind
import io.github.kotlinmania.starlark_kotlin.debug.Variable
import io.github.kotlinmania.starlark_kotlin.debug.VariablePath
import io.github.kotlinmania.starlark_kotlin.debug.VariablesInfo
import io.github.kotlinmania.starlark_kotlin.debug.evalStatements
import io.github.kotlinmania.starlark_kotlin.debug.localVariables
import io.github.kotlinmania.starlark_kotlin.eval.runtime.Evaluator
import io.github.kotlinmania.starlark_kotlin.eval.runtime.before_stmt.BeforeStmtFunc
import io.github.kotlinmania.starlark_kotlin.syntax.AstModule
import io.github.kotlinmania.starlark_kotlin.syntax.dialect.Dialect
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import kotlin.concurrent.atomics.AtomicInt
import io.github.kotlinmania.starlark_kotlin.runBlocking
import io.github.kotlinmania.starlark_kotlin.ReentrantLock
import io.github.kotlinmania.starlark_kotlin.withLock

internal object implementation {

    fun prepareDapAdapter(
        client: DapAdapterClient,
    ): Pair<DapAdapter, DapAdapterEvalHook> {
        val state = SharedAdapterState(
            client = client,
            breakpoints = BreakpointConfig(),
            disableBreakpoints = AtomicInt(0),
        )

        val channel = MessageChannel<ToEvalMessage>()
        return Pair(
            DapAdapterImpl(state = state, sender = channel),
            DapAdapterEvalHookImpl.create(state, channel),
        )
    }

    fun resolveBreakpoints(
        args: SetBreakpointsArguments,
        ast: AstModule,
    ): Result<ResolvedBreakpoints> {
        val poss: Map<Int, FileSpan> = ast.stmtLocations()
            .associateBy { span -> span.resolveSpan().begin.line }

        val resolved = args.breakpoints?.map { x ->
            poss[(x.line - 1).toInt()]?.let { span ->
                Breakpoint(
                    span = span,
                    condition = x.condition,
                )
            }
        } ?: emptyList()

        return Result.success(ResolvedBreakpoints(resolved))
    }

    fun resolvedBreakpointsToDap(
        breakpoints: ResolvedBreakpoints,
    ): SetBreakpointsResponseBody {
        return SetBreakpointsResponseBody(
            breakpoints = breakpoints.breakpoints.map { x ->
                makeBreakpoint(x != null)
            }
        )
    }
}

/** Type alias for the message sent to the evaluation thread. */
private typealias ToEvalMessage = (FileSpanRef, Evaluator) -> Next

/**
 * Simple channel implementation for message passing between adapter and eval threads.
 * This replaces Rust's `mpsc::channel`.
 * Uses kotlinx.coroutines Channel for multiplatform compatibility.
 */
private class MessageChannel<T> {
    private val channel = kotlinx.coroutines.channels.Channel<T>(kotlinx.coroutines.channels.Channel.UNLIMITED)

    fun send(value: T) {
        channel.trySend(value)
    }

    fun recv(): Result<T> {
        return runBlocking {
            try {
                Result.success(channel.receive())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}

/** The DapAdapter allows controlling a running evaluator from a different thread. */
private class DapAdapterImpl(
    private val state: SharedAdapterState,
    private val sender: MessageChannel<ToEvalMessage>,
) : DapAdapter {

    override fun setBreakpoints(
        source: String,
        breakpoints: ResolvedBreakpoints,
    ): Result<Unit> {
        return state.breakpointsLock.withLock {
            state.breakpoints.setBreakpoints(source, breakpoints)
        }
    }

    override fun topFrame(): Result<StackFrame?> {
        return withCtx { span, eval ->
            val frame = eval.callStackTopFrame()
            val name = frame?.name ?: ""
            Result.success(convertFrame(0, name, FileSpan(span.file, span.span)))
        }
    }

    override fun stackTrace(_args: StackTraceArguments): Result<StackTraceResponseBody> {
        // Our model of a Frame and the debugger model are a bit different.
        // We record the location of the call, but DAP wants the location we are at.
        // We also have them in the wrong order
        return withCtx { span, eval ->
            val frames = eval.callStack().intoFrames()
            var next: FileSpan? = FileSpan(span.file, span.span)
            val res = mutableListOf<StackFrame>()
            for ((i, x) in frames.reversed().withIndex()) {
                res.add(convertFrame(i, x.name, next))
                next = x.location
            }
            res.add(convertFrame(frames.size, "Root", next))
            Result.success(StackTraceResponseBody(
                totalFrames = res.size,
                stackFrames = res,
            ))
        }
    }

    override fun scopes(): Result<ScopesInfo> {
        return withCtx { _, eval ->
            val vars = eval.localVariables()
            Result.success(ScopesInfo(numLocals = vars.len()))
        }
    }

    override fun variables(): Result<VariablesInfo> {
        return withCtx { _, eval ->
            val vars = eval.localVariables()
            Result.success(VariablesInfo(
                locals = vars.iter().map { (name, value) ->
                    Variable.fromValue(PathSegment.Attr(name), value)
                }.toList()
            ))
        }
    }

    override fun inspectVariable(path: VariablePath): Result<InspectVariableInfo> {
        return withCtx { _, eval ->
            val accessPath = path.accessPath
            var value = when (val scope = path.scope) {
                is io.github.kotlinmania.starlark_kotlin.debug.Scope.Local -> {
                    val vars = eval.localVariables()
                    // since vars is owned within this closure scope we can just remove value from the map
                    // obtaining owned variable as the rest of the map will be dropped anyway
                    vars.shiftRemove(scope.name)
                        ?: return@withCtx Result.failure(Exception("Local variable ${scope.name} not found"))
                }
                is io.github.kotlinmania.starlark_kotlin.debug.Scope.Expr -> {
                    evaluateExpr(state, eval, scope.expression)
                        .getOrElse { return@withCtx Result.failure(it) }
                }
            }

            for (p in accessPath) {
                value = p.get(value, eval.heap())
                    .getOrElse { return@withCtx Result.failure(it) }
            }
            InspectVariableInfo.tryFromValue(value, eval.heap())
        }
    }

    override fun continue_(): Result<Unit> {
        injectNext(Next.Continue)
        return Result.success(Unit)
    }

    override fun step(kind: StepKind): Result<Unit> {
        injectNext(Next.Step(kind))
        return Result.success(Unit)
    }

    override fun evaluate(expr: String): Result<EvaluateExprInfo> {
        val expression = expr
        return withCtx { _, eval ->
            evaluateExpr(state, eval, expression).map { v -> EvaluateExprInfo.fromValue(v) }
        }
    }

    private fun <T : Any> inject(
        f: (FileSpanRef, Evaluator) -> Pair<Next, T>,
    ): T {
        val resultChannel = MessageChannel<T>()
        sender.send { span, eval ->
            val (next, res) = f(span, eval)
            resultChannel.send(res)
            next
        }
        return resultChannel.recv().getOrThrow()
    }

    private fun injectNext(next: Next) {
        inject<Unit> { _, _ -> Pair(next, Unit) }
    }

    private fun <T : Any> withCtx(
        f: (FileSpanRef, Evaluator) -> T,
    ): T {
        return inject { span, eval ->
            Pair(Next.RemainPaused, f(span, eval))
        }
    }
}

/** The evaluation-side hook implementation. */
private class DapAdapterEvalHookImpl private constructor(
    private val state: SharedAdapterState,
    private val receiver: MessageChannel<ToEvalMessage>,
    private var step: Pair<StepKind, Int>?,
) : DapAdapterEvalHook, io.github.kotlinmania.starlark_kotlin.eval.runtime.before_stmt.BeforeStmtFuncDyn {

    companion object {
        fun create(state: SharedAdapterState, receiver: MessageChannel<ToEvalMessage>): DapAdapterEvalHookImpl {
            return DapAdapterEvalHookImpl(state, receiver, step = null)
        }
    }

    override fun addDapHooks(eval: Evaluator) {
        eval.beforeStmtForDap(BeforeStmtFunc.fromDyn(this))
    }

    override fun call(
        span: FileSpanRef,
        continued: Boolean,
        eval: Evaluator,
    ) {
        beforeStmt(span, continued, eval).getOrThrow()
    }

    private fun beforeStmt(
        spanLoc: FileSpanRef,
        continued: Boolean,
        eval: Evaluator,
    ): Result<Unit> {
        // The debug adapter should only break on the "initial" instruction that
        // makes up any given statement. "Continued" instructions are part of
        // the still-executing/previous statement, and should be ignored.
        if (continued) {
            return Result.success(Unit)
        }

        val stop = if (state.disableBreakpoints.load() > 0) {
            false
        } else {
            val breakpoint = state.breakpointsLock.withLock {
                state.breakpoints.at(spanLoc)
            }
            when {
                breakpoint != null && breakpoint.condition != null -> {
                    evaluateExpr(state, eval, breakpoint.condition)
                        .map { it.toBool() }
                        .getOrElse { true } // If failed to evaluate the condition, stop.
                }
                breakpoint != null -> true
                else -> false
            }
        }

        val stepStop = when (val s = step) {
            null -> false
            else -> when (s.first) {
                StepKind.Into -> true
                // These aren't quite right because we only get called before statements
                // and so we could return from the current function and be in an expression
                // that then calls another function without hitting a new statement in the
                // outer function.
                StepKind.Over -> eval.callStackCount() <= s.second
                StepKind.Out -> eval.callStackCount() < s.second
            }
        }

        if (stop || stepStop) {
            step = null
            state.client.eventStopped().getOrElse { return Result.failure(it) }
            while (true) {
                val msg = receiver.recv()
                when {
                    msg.isFailure -> {
                        // DapAdapter has been dropped so we'll continue.
                        break
                    }
                    else -> when (val next = msg.getOrThrow()(spanLoc, eval)) {
                        Next.Continue -> break
                        is Next.Step -> {
                            step = Pair(next.kind, eval.callStackCount())
                            break
                        }
                        Next.RemainPaused -> { /* continue loop */ }
                    }
                }
            }
        }
        return Result.success(Unit)
    }

    override fun toString(): String = "DapAdapterEvaluationWrapper"
}

/** Breakpoint configuration: maps source filenames to breakpoint spans. */
private class BreakpointConfig {
    // maps a source filename to the breakpoint spans for the file
    private val breakpoints: MutableMap<String, Map<Span, Breakpoint>> = mutableMapOf()

    fun at(spanLoc: FileSpanRef): Breakpoint? {
        return breakpoints[spanLoc.file.filename()]?.get(spanLoc.span)
    }

    fun setBreakpoints(
        source: String,
        breakpoints: ResolvedBreakpoints,
    ): Result<Unit> {
        if (breakpoints.breakpoints.isEmpty()) {
            this.breakpoints.remove(source)
        } else {
            this.breakpoints[source] = breakpoints.breakpoints
                .filterNotNull()
                .associateBy { it.span.span }
        }
        return Result.success(Unit)
    }
}

/** Shared state between the adapter and eval hook. */
private class SharedAdapterState(
    val client: DapAdapterClient,
    // These breakpoints must all match statements as per before_stmt.
    // Those values for which we abort the execution.
    val breakpoints: BreakpointConfig,
    // Lock protecting access to breakpoints (replaces Rust's Mutex)
    val breakpointsLock: ReentrantLock = ReentrantLock(),
    // Set while we are doing evaluate calls (>= 1 means disable)
    val disableBreakpoints: AtomicInt,
)

/** The next action after a breakpoint pause. */
private sealed class Next {
    data object Continue : Next()
    data object RemainPaused : Next()
    data class Step(val kind: StepKind) : Next()
}

private fun evaluateExpr(
    state: SharedAdapterState,
    eval: Evaluator,
    expr: String,
): Result<Value> {
    // We don't want to trigger breakpoints during an evaluate,
    // not least because we currently don't allow reentrant evaluate
    state.disableBreakpoints.fetchAndAdd(1)
    // Don't use getOrThrow, we need to reset disableBreakpoints.
    val ast = AstModule.parse("interactive", expr, Dialect.AllOptionsInternal)
    // This technically loses structured access to the diagnostic information. However, it's
    // completely unused, so there's not much point in converting all of this code to using
    // starlark::Error, only for buck2 to then go and blindly turn it into an anyhow::Error
    // anyway.
    val res = ast.mapCatching { module -> eval.evalStatements(module).getOrThrow() }
    state.disableBreakpoints.fetchAndAdd(-1)
    return res
}

private fun convertFrame(id: Int, name: String, location: FileSpan?): StackFrame {
    val s = StackFrame(
        id = id,
        name = name,
        source = null,
        line = 0,
        column = 0,
        endColumn = null,
        endLine = null,
        moduleId = null,
        presentationHint = null,
    )
    if (location != null) {
        val span = location.resolveSpan()
        return s.copy(
            line = span.begin.line + 1,
            column = span.begin.column + 1,
            endLine = span.end.line + 1,
            endColumn = span.end.column + 1,
            source = location.file.filename(),
        )
    }
    return s
}

internal fun makeBreakpoint(verified: Boolean): DapBreakpoint {
    return DapBreakpoint(
        column = null,
        endColumn = null,
        endLine = null,
        id = null,
        line = null,
        message = null,
        source = null,
        verified = verified,
    )
}
