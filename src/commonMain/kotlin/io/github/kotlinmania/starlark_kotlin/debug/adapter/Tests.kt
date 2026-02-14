// port-lint: source src/debug/adapter/tests.rs
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

import io.github.kotlinmania.starlark_kotlin.assert.testFunctions
import io.github.kotlinmania.starlark_kotlin.debug.DapAdapter
import io.github.kotlinmania.starlark_kotlin.debug.DapAdapterClient
import io.github.kotlinmania.starlark_kotlin.debug.DapAdapterEvalHook
import io.github.kotlinmania.starlark_kotlin.debug.StepKind
import io.github.kotlinmania.starlark_kotlin.debug.VariablePath
import io.github.kotlinmania.starlark_kotlin.debug.adapter.implementation.prepareDapAdapter
import io.github.kotlinmania.starlark_kotlin.debug.adapter.implementation.resolveBreakpoints
import io.github.kotlinmania.starlark_kotlin.environment.GlobalsBuilder
import io.github.kotlinmania.starlark_kotlin.environment.Module
import io.github.kotlinmania.starlark_kotlin.eval.Evaluator
import io.github.kotlinmania.starlark_kotlin.eval.ReturnFileLoader
import io.github.kotlinmania.starlark_kotlin.syntax.AstModule
import io.github.kotlinmania.starlark_kotlin.syntax.Dialect
import io.github.kotlinmania.starlark_kotlin.values.OwnedFrozenValue
import io.github.kotlinmania.starlark_kotlin.wasm.isWasm
import kotlin.concurrent.atomics.AtomicInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

// #[cfg(test)]
// mod t

// #[derive(Debug)]
// struct Client
private class Client(
    val controller: BreakpointController,
) : DapAdapterClient {
    // impl DapAdapterClient for Client
    // fn event_stopped(&self) -> crate::Result<()>
    override fun eventStopped(): Result<Unit> {
        println("stopped!")
        return controller.evalStopped()
    }
}

// #[derive(Debug, Clone, Dupe)]
// struct BreakpointController
private class BreakpointController {
    /// The number of breakpoint hits or 999999 if cancelled.
    // breakpoints_hit: Arc<AtomicUsize>
    val breakpointsHit: AtomicInt = AtomicInt(0)

    // fn get_client(&self) -> Box<dyn DapAdapterClient>
    fun getClient(): DapAdapterClient {
        return Client(this)
    }

    // fn eval_stopped(&self) -> crate::Result<()>
    fun evalStopped(): Result<Unit> {
        while (true) {
            val current = breakpointsHit.load()
            if (current == 999999) {
                System.err.println("eval_stopped: cancelled")
                return Result.failure(Exception("cancelled"))
            }
            if (breakpointsHit.compareAndSet(current, current + 1)) {
                return Result.success(Unit)
            }
        }
        @Suppress("UNREACHABLE_CODE")
        error("unreachable")
    }

    // fn wait_for_eval_stopped(&self, breakpoint_count: usize, timeout: Duration)
    fun waitForEvalStopped(breakpointCount: Int, timeout: Duration) {
        val start = TimeSource.Monotonic.markNow()
        while (true) {
            val current = breakpointsHit.load()
            check(current != 999999) { "cancelled" }
            check(current <= breakpointCount)
            if (current == breakpointCount) {
                break
            }
            if (start.elapsedNow() > timeout) {
                error("didn't hit expected breakpoint")
            }
            // spin
        }
    }
}

// struct BreakpointControllerDropGuard
private class BreakpointControllerDropGuard(
    val controller: BreakpointController,
) : AutoCloseable {
    // impl Drop for BreakpointControllerDropGuard
    override fun close() {
        System.err.println("dropping controller")
        controller.breakpointsHit.store(999999)
    }
}

// fn breakpoint(line: i64, condition: Option<&str>) -> SourceBreakpoint
private fun breakpoint(line: Long, condition: String?): SourceBreakpoint {
    return SourceBreakpoint(
        column = null,
        condition = condition,
        hitCondition = null,
        line = line,
        logMessage = null,
    )
}

// fn breakpoints_args(path: &str, lines: &[(i64, Option<&str>)]) -> SetBreakpointsArguments
private fun breakpointsArgs(path: String, lines: List<Pair<Long, String?>>): SetBreakpointsArguments {
    return SetBreakpointsArguments(
        breakpoints = lines.map { (line, condition) -> breakpoint(line, condition) },
        lines = null,
        source = Source(
            adapterData = null,
            checksums = null,
            name = null,
            origin = null,
            path = path,
            presentationHint = null,
            sourceReference = null,
            sources = null,
        ),
        sourceModified = null,
    )
}

// fn eval_with_hook(ast: AstModule, hook: Box<dyn DapAdapterEvalHook>) -> crate::Result<OwnedFrozenValue>
private fun evalWithHook(
    ast: AstModule,
    hook: DapAdapterEvalHook,
): Result<OwnedFrozenValue> {
    val modules = HashMap<String, Any>()
    val loader = ReturnFileLoader(modules)
    val globals = GlobalsBuilder.extended().with(::testFunctions).build()
    return Module.withTempHeap { env ->
        val res = run {
            val eval = Evaluator(env)
            hook.addDapHooks(eval)
            eval.setLoader(loader)
            eval.evalModule(ast, globals).getOrElse { return@withTempHeap Result.failure(it) }
        }

        env.set("_", res)
        Result.success(
            env.freeze().getOrElse { error("error freezing module") }
                .get("_") ?: error("missing _"),
        )
    }
}

// fn join_timeout<T>(waiting: ScopedJoinHandle<T>, timeout: Duration) -> T
private fun <T> joinTimeout(waiting: Thread, result: () -> T, timeout: Duration): T {
    val start = TimeSource.Monotonic.markNow()
    while (waiting.isAlive) {
        if (start.elapsedNow() > timeout) {
            error("timeout waiting for thread")
        }
    }
    return result()
}

// static TIMEOUT: Duration = Duration::from_secs(10)
private val TIMEOUT: Duration = 10.seconds

// fn dap_test_template<'env, F, R>(f: F) -> crate::Result<R>
private fun <R> dapTestTemplate(
    f: (BreakpointController, DapAdapter, DapAdapterEvalHook) -> Result<R>,
): Result<R> {
    val controller = BreakpointController()

    BreakpointControllerDropGuard(controller).use {
        val (adapter, evalHook) = prepareDapAdapter(controller.getClient())
        return f(controller, adapter, evalHook)
    }
}

// #[test]
// fn test_breakpoint() -> crate::Result<()>
internal fun testBreakpoint() {
    if (isWasm()) {
        return
    }

    val fileContents = """

x = [1, 2, 3]
print(x)
        """
    dapTestTemplate { controller, adapter, evalHook ->
        val ast = AstModule.parse(
            "test.bzl",
            fileContents,
            Dialect.AllOptionsInternal,
        ).getOrThrow()
        val breakpoints =
            resolveBreakpoints(breakpointsArgs("test.bzl", listOf(3L to null)), ast).getOrThrow()
        adapter.setBreakpoints("test.bzl", breakpoints).getOrThrow()

        var evalResult: Result<OwnedFrozenValue>? = null
        val thread = Thread {
            evalResult = evalWithHook(ast, evalHook)
        }.also { it.start() }

        controller.waitForEvalStopped(1, TIMEOUT)
        adapter.continue_().getOrThrow()
        controller.waitForEvalStopped(2, TIMEOUT)

        adapter.continue_().getOrThrow()

        joinTimeout(thread, { evalResult!!.getOrThrow() }, TIMEOUT)
        Result.success(Unit)
    }.getOrThrow()
}

// #[test]
// fn test_breakpoint_with_failing_condition() -> crate::Result<()>
internal fun testBreakpointWithFailingCondition() {
    if (isWasm()) {
        return
    }

    val fileContents = """

x = [1, 2, 3]
print(x)
        """
    dapTestTemplate { _, adapter, evalHook ->
        val ast = AstModule.parse(
            "test.bzl",
            fileContents,
            Dialect.AllOptionsInternal,
        ).getOrThrow()
        val breakpoints =
            resolveBreakpoints(breakpointsArgs("test.bzl", listOf(3L to "5 in x")), ast).getOrThrow()
        adapter.setBreakpoints("test.bzl", breakpoints).getOrThrow()

        var evalResult: Result<OwnedFrozenValue>? = null
        val thread = Thread {
            evalResult = evalWithHook(ast, evalHook)
        }.also { it.start() }

        joinTimeout(thread, { evalResult!!.getOrThrow() }, TIMEOUT)
        Result.success(Unit)
    }.getOrThrow()
}

// #[test]
// fn test_breakpoint_with_passing_condition() -> crate::Result<()>
internal fun testBreakpointWithPassingCondition() {
    if (isWasm()) {
        return
    }

    val fileContents = """

x = [1, 2, 3]
print(x)
        """
    dapTestTemplate { controller, adapter, evalHook ->
        val ast = AstModule.parse(
            "test.bzl",
            fileContents,
            Dialect.AllOptionsInternal,
        ).getOrThrow()
        val breakpoints =
            resolveBreakpoints(breakpointsArgs("test.bzl", listOf(3L to "2 in x")), ast).getOrThrow()
        adapter.setBreakpoints("test.bzl", breakpoints).getOrThrow()

        var evalResult: Result<OwnedFrozenValue>? = null
        val thread = Thread {
            evalResult = evalWithHook(ast, evalHook)
        }.also { it.start() }

        controller.waitForEvalStopped(1, TIMEOUT)
        adapter.continue_().getOrThrow()
        controller.waitForEvalStopped(2, TIMEOUT)
        adapter.continue_().getOrThrow()

        joinTimeout(thread, { evalResult!!.getOrThrow() }, TIMEOUT)
        Result.success(Unit)
    }.getOrThrow()
}

// #[test]
// fn test_step_over() -> crate::Result<()>
internal fun testStepOver() {
    if (isWasm()) {
        return
    }

    val fileContents = """

def adjust(y):
    y[0] += 1
    y[1] += 1 # line 4
    y[2] += 1
x = [1, 2, 3]
adjust(x) # line 7
adjust(x)
print(x)
        """
    dapTestTemplate { controller, adapter, evalHook ->
        val ast = AstModule.parse(
            "test.bzl",
            fileContents,
            Dialect.AllOptionsInternal,
        ).getOrThrow()
        val breakpoints =
            resolveBreakpoints(breakpointsArgs("test.bzl", listOf(7L to null)), ast).getOrThrow()
        adapter.setBreakpoints("test.bzl", breakpoints).getOrThrow()

        var evalResult: Result<OwnedFrozenValue>? = null
        val thread = Thread {
            evalResult = evalWithHook(ast, evalHook)
        }.also { it.start() }

        controller.waitForEvalStopped(1, TIMEOUT)
        adapter.continue_().getOrThrow()
        controller.waitForEvalStopped(2, TIMEOUT)

        check("1" == adapter.evaluate("x[0]").getOrThrow().result)
        check("2" == adapter.evaluate("x[1]").getOrThrow().result)
        check("3" == adapter.evaluate("x[2]").getOrThrow().result)
        adapter.step(StepKind.Over).getOrThrow()
        controller.waitForEvalStopped(3, TIMEOUT)
        check("2" == adapter.evaluate("x[0]").getOrThrow().result)
        check("3" == adapter.evaluate("x[1]").getOrThrow().result)
        check("4" == adapter.evaluate("x[2]").getOrThrow().result)

        adapter.step(StepKind.Over).getOrThrow()
        controller.waitForEvalStopped(4, TIMEOUT)
        adapter.step(StepKind.Over).getOrThrow()
        controller.waitForEvalStopped(5, TIMEOUT)
        check("3" == adapter.evaluate("x[0]").getOrThrow().result)
        check("4" == adapter.evaluate("x[1]").getOrThrow().result)
        check("5" == adapter.evaluate("x[2]").getOrThrow().result)
        adapter.continue_().getOrThrow()
        joinTimeout(thread, { evalResult!!.getOrThrow() }, TIMEOUT)
        Result.success(Unit)
    }.getOrThrow()
}

// #[test]
// fn test_step_into() -> crate::Result<()>
internal fun testStepInto() {
    if (isWasm()) {
        return
    }

    val fileContents = """

def adjust(y):
    y[0] += 1
    y[1] += 1 # line 4
    y[2] += 1
x = [1, 2, 3]
adjust(x) # line 7
adjust(x)
print(x)
        """
    dapTestTemplate { controller, adapter, evalHook ->
        val ast = AstModule.parse(
            "test.bzl",
            fileContents,
            Dialect.AllOptionsInternal,
        ).getOrThrow()
        val breakpoints =
            resolveBreakpoints(breakpointsArgs("test.bzl", listOf(7L to null)), ast).getOrThrow()
        adapter.setBreakpoints("test.bzl", breakpoints).getOrThrow()

        var evalResult: Result<OwnedFrozenValue>? = null
        val thread = Thread {
            evalResult = evalWithHook(ast, evalHook)
        }.also { it.start() }

        controller.waitForEvalStopped(1, TIMEOUT)
        adapter.continue_().getOrThrow()
        controller.waitForEvalStopped(2, TIMEOUT)

        check("1" == adapter.evaluate("x[0]").getOrThrow().result)
        check("2" == adapter.evaluate("x[1]").getOrThrow().result)
        check("3" == adapter.evaluate("x[2]").getOrThrow().result)

        // into adjust
        adapter.step(StepKind.Into).getOrThrow()
        controller.waitForEvalStopped(3, TIMEOUT)
        check("1" == adapter.evaluate("y[0]").getOrThrow().result)
        check("2" == adapter.evaluate("y[1]").getOrThrow().result)
        check("3" == adapter.evaluate("y[2]").getOrThrow().result)

        // into should go to next line
        adapter.step(StepKind.Into).getOrThrow()
        controller.waitForEvalStopped(4, TIMEOUT)
        check("2" == adapter.evaluate("y[0]").getOrThrow().result)
        check("2" == adapter.evaluate("y[1]").getOrThrow().result)
        check("3" == adapter.evaluate("y[2]").getOrThrow().result)

        // two more intos should get us out of the function call
        adapter.step(StepKind.Into).getOrThrow()
        controller.waitForEvalStopped(5, TIMEOUT)
        adapter.step(StepKind.Into).getOrThrow()
        controller.waitForEvalStopped(6, TIMEOUT)
        check("2" == adapter.evaluate("x[0]").getOrThrow().result)
        check("3" == adapter.evaluate("x[1]").getOrThrow().result)
        check("4" == adapter.evaluate("x[2]").getOrThrow().result)

        // and once more back into the function
        adapter.step(StepKind.Into).getOrThrow()
        controller.waitForEvalStopped(7, TIMEOUT)

        adapter.step(StepKind.Into).getOrThrow()
        controller.waitForEvalStopped(8, TIMEOUT)

        check("2" == adapter.evaluate("y[0]").getOrThrow().result)
        check("3" == adapter.evaluate("y[1]").getOrThrow().result)
        check("4" == adapter.evaluate("y[2]").getOrThrow().result)

        adapter.continue_().getOrThrow()
        joinTimeout(thread, { evalResult!!.getOrThrow() }, TIMEOUT)
        Result.success(Unit)
    }.getOrThrow()
}

// #[test]
// fn test_step_out() -> crate::Result<()>
internal fun testStepOut() {
    if (isWasm()) {
        return
    }

    val fileContents = """

def adjust(y):
    y[0] += 1
    y[1] += 1 # line 4
    y[2] += 1
x = [1, 2, 3]
adjust(x) # line 7
adjust(x)
print(x)
        """
    dapTestTemplate { controller, adapter, evalHook ->
        val ast = AstModule.parse(
            "test.bzl",
            fileContents,
            Dialect.AllOptionsInternal,
        ).getOrThrow()
        val breakpoints =
            resolveBreakpoints(breakpointsArgs("test.bzl", listOf(4L to null)), ast).getOrThrow()
        adapter.setBreakpoints("test.bzl", breakpoints).getOrThrow()

        var evalResult: Result<OwnedFrozenValue>? = null
        val thread = Thread {
            evalResult = evalWithHook(ast, evalHook)
        }.also { it.start() }

        // should break on the first time hitting line 4
        controller.waitForEvalStopped(1, TIMEOUT)
        check("2" == adapter.evaluate("y[0]").getOrThrow().result)
        check("2" == adapter.evaluate("y[1]").getOrThrow().result)
        check("3" == adapter.evaluate("y[2]").getOrThrow().result)

        // step out should take us to line 8
        adapter.step(StepKind.Out).getOrThrow()
        controller.waitForEvalStopped(2, TIMEOUT)
        check("2" == adapter.evaluate("x[0]").getOrThrow().result)
        check("3" == adapter.evaluate("x[1]").getOrThrow().result)
        check("4" == adapter.evaluate("x[2]").getOrThrow().result)

        // step out should actually hit the breakpoint at 4 first (before getting out)
        adapter.step(StepKind.Out).getOrThrow()
        controller.waitForEvalStopped(3, TIMEOUT)
        check("3" == adapter.evaluate("y[0]").getOrThrow().result)
        check("3" == adapter.evaluate("y[1]").getOrThrow().result)
        check("4" == adapter.evaluate("y[2]").getOrThrow().result)

        // step out should get out to the print
        adapter.step(StepKind.Out).getOrThrow()
        controller.waitForEvalStopped(4, TIMEOUT)
        check("3" == adapter.evaluate("x[0]").getOrThrow().result)
        check("4" == adapter.evaluate("x[1]").getOrThrow().result)
        check("5" == adapter.evaluate("x[2]").getOrThrow().result)

        // one more out should be equivalent to continue
        adapter.step(StepKind.Out).getOrThrow()
        joinTimeout(thread, { evalResult!!.getOrThrow() }, TIMEOUT)
        Result.success(Unit)
    }.getOrThrow()
}

// #[test]
// fn test_local_variables() -> crate::Result<()>
internal fun testLocalVariables() {
    if (isWasm()) {
        return
    }

    val fileContents = """

def do():
    a = struct(
        f1 = "1",
        f2 = 123,
    )
    arr = [1, 2, 3, 4, 6, "234", 123.32]
    t = (1, 2)
    d = dict(a = 1, b = "2")
    empty_dict = {}
    empty_list = []
    empty_tuple = ()
    return d # line 13
print(do())
        """
    val result = dapTestTemplate { controller, adapter, evalHook ->
        val ast = AstModule.parse(
            "test.bzl",
            fileContents,
            Dialect.AllOptionsInternal,
        ).getOrThrow()
        val breakpoints =
            resolveBreakpoints(breakpointsArgs("test.bzl", listOf(13L to null)), ast).getOrThrow()
        adapter.setBreakpoints("test.bzl", breakpoints).getOrThrow()

        var evalResult: Result<OwnedFrozenValue>? = null
        val thread = Thread {
            evalResult = evalWithHook(ast, evalHook)
        }.also { it.start() }

        controller.waitForEvalStopped(1, TIMEOUT)
        val variables = adapter.variables().getOrThrow()
        adapter.continue_().getOrThrow()
        joinTimeout(thread, { evalResult!!.getOrThrow() }, TIMEOUT)
        Result.success(variables)
    }.getOrThrow()

    // It's easier to handle errors outside of thread::scope block as the test is quite flaky
    // and hangs in case error propagates
    check(
        listOf(
            Triple("a", "<type:struct, size=2>", true),
            Triple("arr", "<list, size=7>", true),
            Triple("t", "<tuple, size=2>", true),
            Triple("d", "<dict, size=2>", true),
            Triple("empty_dict", "{}", false),
            Triple("empty_list", "[]", false),
            Triple("empty_tuple", "()", false),
        ) == result.locals.map { v -> Triple(v.name, v.value, v.hasChildren) },
    )
}

// #[test]
// fn test_inspect_variables() -> crate::Result<()>
internal fun testInspectVariables() {
    if (isWasm()) {
        return
    }

    val fileContents = """

def do():
    a = struct(
        f1 = "1",
        f2 = 123,
    )
    arr = [1, 2, 3, 4, 6, "234", 123.32]
    t = (1, 2)
    d = dict(a = 1, b = "2")
    empty_dict = {}
    empty_list = []
    empty_tuple = ()
    return d # line 13
print(do())
        """
    val result = dapTestTemplate { controller, adapter, evalHook ->
        val inspectResults = mutableListOf<Result<Variable>>()
        val ast = AstModule.parse(
            "test.bzl",
            fileContents,
            Dialect.AllOptionsInternal,
        ).getOrThrow()
        val breakpoints =
            resolveBreakpoints(breakpointsArgs("test.bzl", listOf(13L to null)), ast).getOrThrow()
        adapter.setBreakpoints("test.bzl", breakpoints).getOrThrow()

        var evalResult: Result<OwnedFrozenValue>? = null
        val thread = Thread {
            evalResult = evalWithHook(ast, evalHook)
        }.also { it.start() }

        controller.waitForEvalStopped(1, TIMEOUT)
        inspectResults.addAll(
            listOf(
                adapter.inspectVariable(VariablePath.newLocal("a")),
                adapter.inspectVariable(VariablePath.newLocal("arr")),
                adapter.inspectVariable(VariablePath.newLocal("t")),
                adapter.inspectVariable(VariablePath.newLocal("d")),
            ),
        )
        adapter.continue_().getOrThrow()
        joinTimeout(thread, { evalResult!!.getOrThrow() }, TIMEOUT)
        Result.success(inspectResults.map { it.getOrThrow() })
    }.getOrThrow()

    // It's easier to handle errors outside of thread::scope block as the test is quite flaky
    // and hangs in case error propagates

    assertVariable("f1", "1", false, result[0].subValues[0])
    assertVariable("f2", "123", false, result[0].subValues[1])
    assertVariable("0", "1", false, result[1].subValues[0])
    assertVariable("5", "234", false, result[1].subValues[5])
    assertVariable("0", "1", false, result[2].subValues[0])
    assertVariable("1", "2", false, result[2].subValues[1])
    assertVariable("\"a\"", "1", false, result[3].subValues[0])
    assertVariable("\"b\"", "2", false, result[3].subValues[1])
}

// #[test]
// fn test_evaluate_expression() -> crate::Result<()>
internal fun testEvaluateExpression() {
    if (isWasm()) {
        return
    }

    val fileContents = """

def do():
    s = struct(
        inner = struct(
            inner = struct(
                value = "more_inner"
            ),
            value = "inner",
            arr = [dict(a = 1, b = "2"), 1337]
        )
    )
    return s # line 12
print(do())
        """
    val result = dapTestTemplate { controller, adapter, evalHook ->
        val evalResults = mutableListOf<Result<EvaluateResult>>()
        val ast = AstModule.parse(
            "test.bzl",
            fileContents,
            Dialect.AllOptionsInternal,
        ).getOrThrow()
        val breakpoints =
            resolveBreakpoints(breakpointsArgs("test.bzl", listOf(12L to null)), ast).getOrThrow()
        adapter.setBreakpoints("test.bzl", breakpoints).getOrThrow()

        var evalResult: Result<OwnedFrozenValue>? = null
        val thread = Thread {
            evalResult = evalWithHook(ast, evalHook)
        }.also { it.start() }

        controller.waitForEvalStopped(1, TIMEOUT)
        evalResults.addAll(
            listOf(
                adapter.evaluate("s.inner.value"),
                adapter.evaluate("s.inner.inner.value"),
                adapter.evaluate("s.inner.arr[0]"),
                adapter.evaluate("s.inner.arr[0][\"a\"]"),
                adapter.evaluate("s.inner.arr[1]"),
            ),
        )
        adapter.continue_().getOrThrow()
        joinTimeout(thread, { evalResult!!.getOrThrow() }, TIMEOUT)
        Result.success(evalResults.map { it.getOrThrow() })
    }.getOrThrow()

    // It's easier to handle errors outside of thread::scope block as the test is quite flaky
    // and hangs in case error propagates
    check(
        listOf(
            "inner" to false,
            "more_inner" to false,
            "<dict, size=2>" to true,
            "1" to false,
            "1337" to false,
        ) == result.map { v -> v.result to v.hasChildren },
    )
}

// fn assert_variable(name: &str, value: &str, has_children: bool, var: &Variable)
private fun assertVariable(
    name: String,
    value: String,
    hasChildren: Boolean,
    variable: Variable,
) {
    check(
        Triple(name, value, hasChildren) ==
            Triple(variable.name, variable.value, variable.hasChildren),
    )
}

// #[test]
// pub fn test_truncate_string()
internal fun testTruncateString() {
    check("Hello" == Variable.truncateString("Hello", 10))
    check("Hello" == Variable.truncateString("Hello", 5))
    // A string that should be truncated at a character boundary
    check("Hello, ...(truncated)" == Variable.truncateString("Hello, \u4E16\u754C", 7))
    // A string that would be truncated within a multi-byte character
    check("Hello, ...(truncated)" == Variable.truncateString("Hello, \u4E16\u754C", 8))
    // A string that should be truncated just before a multi-byte character
    check("Hello, ...(truncated)" == Variable.truncateString("Hello, \u4E16\u754C", 9))
    check("Hello, \u4E16...(truncated)" == Variable.truncateString("Hello, \u4E16\u754C", 10))
}
