// port-lint: source src/assert/assert.rs
package io.github.kotlinmania.starlark_kotlin.assert

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

/** Utilities to test Starlark code execution. */

import io.github.kotlinmania.starlark_kotlin.environment.FrozenModule
import io.github.kotlinmania.starlark_kotlin.environment.Globals
import io.github.kotlinmania.starlark_kotlin.environment.GlobalsBuilder
import io.github.kotlinmania.starlark_kotlin.environment.Module
import io.github.kotlinmania.starlark_kotlin.stdlib.PrintHandler
import io.github.kotlinmania.starlark_kotlin.values.AllocValue
import io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.TypeCompiled
import io.github.kotlinmania.starlark_kotlin.values.types.structs.AllocStruct
import io.github.kotlinmania.starlark_kotlin.eval.runtime.Evaluator
import io.github.kotlinmania.starlark_kotlin.values.types.none.NoneType
import io.github.kotlinmania.starlark_kotlin.values.owned.OwnedFrozenValue
import io.github.kotlinmania.starlark_kotlin.eval.runtime.file_loader.ReturnFileLoader
import io.github.kotlinmania.starlark_kotlin.analysis.unused_loads.FileSpanRef
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.syntax.dialect.Dialect
import io.github.kotlinmania.starlark_kotlin.starlark_error.Error
import io.github.kotlinmania.starlark_kotlin.eval.runtime.positional
import io.github.kotlinmania.starlark_kotlin.values.owned.unpackBool
import io.github.kotlinmania.starlark_kotlin.tests.getAttr
import io.github.kotlinmania.starlark_kotlin.tests.derive.freeze.checkType
import io.github.kotlinmania.starlark_kotlin.eval.runtime.triggerGc
import io.github.kotlinmania.starlark_kotlin.eval.runtime.setLoader
import io.github.kotlinmania.starlark_kotlin.eval.runtime.enableStaticTypechecking
import io.github.kotlinmania.starlark_kotlin.eval.runtime.beforeStmtFn
import io.github.kotlinmania.starlark_kotlin.eval.evalModule
import io.github.kotlinmania.starlark_kotlin.analysis.unused_loads.heap
import io.github.kotlinmania.starlark_kotlin.syntax.AstModule

/** Functional type alias for value equality assertion. */
internal typealias AssertEquals = (Value, Value) -> Result<NoneType>

/** Functional type alias for value inequality assertion. */
internal typealias AssertDifferent = (Value, Value) -> Result<NoneType>

/** Functional type alias for less-than assertion. */
internal typealias AssertLessThan = (Value, Value) -> Result<NoneType>

/** Functional type alias for the asserts_star starlark module builder. */
internal typealias AssertsStar = (GlobalsBuilder) -> Unit

private fun mkEnvironment(): GlobalsBuilder {
    return GlobalsBuilder.extended().with(::testFunctions)
}

private val GLOBALS: Globals by lazy { mkEnvironment().build() }

private val ASSERTS_STAR: FrozenModule by lazy {
    val g = GlobalsBuilder()
        .withNamespace("asserts", ::assertsStar)
        .build()
    Module.withTempHeap { m ->
        val asserts = g.getOwned("asserts")!!
        val assertsValue = m.heap().accessOwnedFrozenValue(asserts)
        m.set("asserts", assertsValue)
        m.set(
            "freeze",
            assertsValue.getAttr("freeze", m.heap())!!,
        )
        m.freeze().getOrThrow()
    }
}

internal fun assertEquals(a: Value, b: Value): Result<NoneType> {
    return if (!a.equals(b).getOrElse { return Result.failure(it) }) {
        Result.failure(Exception("assert_eq: expected $a, got $b"))
    } else {
        Result.success(NoneType)
    }
}

private fun assertDifferent(a: Value, b: Value): Result<NoneType> {
    return if (a.equals(b).getOrElse { return Result.failure(it) }) {
        Result.failure(Exception("assert_ne: but $a == $b"))
    } else {
        Result.success(NoneType)
    }
}

private fun assertLessThan(a: Value, b: Value): Result<NoneType> {
    val cmp = a.compare(b).getOrElse { return Result.failure(it) }
    return if (cmp >= 0) {
        Result.failure(Exception("assert_lt: but $a >= $b"))
    } else {
        Result.success(NoneType)
    }
}

/**
 * How often we garbage collection _should_ be transparent to the tests,
 * so we run each test in three configurations.
 */
enum class GcStrategy {
    /** Disable GC */
    Never,
    /** Use the automatic heuristics (in practice, this does almost no GC) */
    Auto,
    /** GC as aggressively as we can */
    Always,
}

/** Definitions to support assert.star as used by the Go test suite */
private fun assertsStar(builder: GlobalsBuilder) {
    builder.setFunction("eq") { args, _ ->
        assertEquals(args.positional(0), args.positional(1))
    }

    builder.setFunction("ne") { args, _ ->
        assertDifferent(args.positional(0), args.positional(1))
    }

    builder.setFunction("lt") { args, _ ->
        assertLessThan(args.positional(0), args.positional(1))
    }

    builder.setFunction("contains") { args, _ ->
        val xs = args.positional(0)
        val x = args.positional(1)
        if (!xs.isIn(x).getOrElse { return@setFunction Result.failure(it) }) {
            Result.failure(Exception("assert.contains: expected $x to be in $xs"))
        } else {
            Result.success(NoneType)
        }
    }

    builder.setFunction("true") { args, _ ->
        assertEquals(Value.newBool(args.positional(0).toBool()), Value.newBool(true))
    }

    // We don't allow this at runtime - just to be compatible with the Go Starlark test suite
    builder.setFunction("freeze") { args, _ ->
        Result.success(args.positional(0))
    }

    builder.setFunction("fails") { args, eval ->
        val f = args.positional(0)
        val _ = args.positional(1) // msg - We don't actually check the message
        when (val result = f.invokePos(emptyList(), eval)) {
            else -> if (result.isFailure) {
                Result.success(NoneType)
            } else {
                Result.failure(Exception("assert.fails: didn't fail"))
            }
        }
    }
}

fun testFunctions(builder: GlobalsBuilder) {
    // Used by one of the test methods in Go
    builder.setConst("fibonacci", listOf(0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89))

    /** Approximate version of a method used by the Go test suite */
    builder.setFunction("hasfields") { _, _ ->
        Result.success(AllocStruct.EMPTY)
    }

    builder.setFunction("assert_eq") { args, _ ->
        assertEquals(args.positional(0), args.positional(1))
    }

    builder.setFunction("assert_ne") { args, _ ->
        assertDifferent(args.positional(0), args.positional(1))
    }

    builder.setFunction("assert_lt") { args, _ ->
        assertLessThan(args.positional(0), args.positional(1))
    }

    builder.setFunction("assert_true") { args, _ ->
        if (!args.positional(0).toBool()) {
            Result.failure(Exception("assertion failed"))
        } else {
            Result.success(NoneType)
        }
    }

    builder.setFunction("assert_false") { args, _ ->
        if (args.positional(0).toBool()) {
            Result.failure(Exception("assertion failed"))
        } else {
            Result.success(NoneType)
        }
    }

    // This is only safe to call at the top-level of a Starlark module
    builder.setFunction("garbage_collect") { _, eval ->
        eval.triggerGc()
        Result.success(NoneType)
    }

    builder.setFunction("assert_type") { args, eval ->
        val v = args.positional(0)
        val ty = args.positional(1)
        runCatching { TypeCompiled.new(ty, eval.heap()) }
            .getOrElse { return@setFunction Result.failure(it) }
            .checkType(v, "v").getOrElse { return@setFunction Result.failure(it) }
        Result.success(NoneType)
    }

    /**
     * Function which consumes arguments and that's it.
     *
     * This function is unknown to optimizer, so it can be used in optimizer tests.
     */
    builder.setFunction("noop") { args, _ ->
        // kwargs are ignored
        Result.success(args.positionals().firstOrNull() ?: Value.newNone())
    }
}

/** Environment in which to run assertion tests. */
class Assert(
    private var dialect: Dialect = Dialect.AllOptionsInternal.copy(),
    private val modules: MutableMap<String, FrozenModule> = mutableMapOf(
        "asserts.star" to ASSERTS_STAR,
    ),
    private var globals: Globals = GLOBALS,
    private var gcStrategy: GcStrategy? = null,
    private var setupEval: (Evaluator) -> Unit = {},
    // Ideally `printHandler` should be set up in `setupEval`
    // but if you know how to do it, show me how.
    private var printHandler: PrintHandler? = null,
    private var staticTypechecking: Boolean = true,
) {
    /**
     * Create a new assert object, which will by default use
     * extended dialect and all library extensions,
     * plus some additional global functions like `assert_eq`.
     * The usual pattern is to create an `Assert`, modify some properties
     * and then execute some tests.
     */
    constructor() : this(
        dialect = Dialect.AllOptionsInternal.copy(),
        modules = mutableMapOf("asserts.star" to ASSERTS_STAR),
        globals = GLOBALS,
        gcStrategy = null,
        setupEval = {},
        printHandler = null,
        staticTypechecking = true,
    )

    /** Disable garbage collection on the tests. */
    fun disableGc() {
        gcStrategy = GcStrategy.Never
    }

    /** Configure a callback which is used to setup evaluator before each evaluation. */
    fun setupEval(setup: (Evaluator) -> Unit) {
        setupEval = setup
    }

    /** Configure the handler for `print` function. */
    fun setPrintHandler(handler: PrintHandler) {
        printHandler = handler
    }

    /**
     * Disable static typechecking for test. It is off by default in `Evaluator`,
     * but on by default in `Assert`.
     */
    fun disableStaticTypechecking() {
        staticTypechecking = false
    }

    private fun <A> withGc(f: (GcStrategy) -> A): A {
        return when (val gc = gcStrategy) {
            null -> {
                // We want to run with Auto first, and use that as the result, because that's the default
                val res = f(GcStrategy.Auto)
                f(GcStrategy.Never)
                f(GcStrategy.Always)
                res
            }
            else -> f(gc)
        }
    }

    private fun execute(
        path: String,
        program: String,
        module: Module,
        gc: GcStrategy,
    ): Result<Value> {
        val modulesRef = modules.mapValues { it.value }
        val loader = ReturnFileLoader(modulesRef)
        val ast = AstModule.parse(path, program, dialect).getOrElse { return Result.failure(it) }
        val gcAlways = { _span: FileSpanRef, continued: Boolean, eval: Evaluator ->
            if (!continued) {
                eval.triggerGc()
            }
        }
        val eval = Evaluator(module)
        eval.enableStaticTypechecking(staticTypechecking)
        setupEval(eval)
        printHandler?.let { eval.setPrintHandler(it) }

        when (gc) {
            GcStrategy.Never -> eval.disableGc()
            GcStrategy.Auto -> {}
            GcStrategy.Always -> eval.beforeStmtFn(gcAlways)
        }
        eval.setLoader(loader)
        return eval.evalModule(ast, globals)
    }

    private fun executeFail(
        func: String,
        program: String,
        module: Module,
        gc: GcStrategy,
    ): io.github.kotlinmania.starlark_kotlin.Error {
        return when (val result = execute("assert.bzl", program, module, gc)) {
            else -> if (result.isSuccess) {
                val v = result.getOrThrow()
                error("starlark::assert::$func, didn't fail!\nCode:\n$program\nResult:\n$v\n")
            } else {
                result.exceptionOrNull() as io.github.kotlinmania.starlark_kotlin.Error
            }
        }
    }

    private fun executeUnwrap(
        func: String,
        path: String,
        program: String,
        module: Module,
        gc: GcStrategy,
    ): Value {
        return when (val result = execute(path, program, module, gc)) {
            else -> if (result.isSuccess) {
                result.getOrThrow()
            } else {
                val err = result.exceptionOrNull()!!
                error("starlark::assert::$func, failed to execute!\nCode:\n$program\nGot error: $err")
            }
        }
    }

    private fun executeUnwrapTrue(
        func: String,
        program: String,
        module: Module,
        gc: GcStrategy,
    ) {
        val v = executeUnwrap(func, "assert.bzl", program, module, gc)
        when (v.unpackBool()) {
            true -> {}
            false -> error("starlark::assert::$func, got false!\nCode:\n$program")
            null -> error("starlark::assert::$func, not a bool!\nCode:\n$program\nResult\n$v")
        }
    }

    private fun executeUnwrapFalse(
        func: String,
        program: String,
        module: Module,
        gc: GcStrategy,
    ) {
        val v = executeUnwrap(func, "assert.bzl", program, module, gc)
        when (v.unpackBool()) {
            false -> {}
            true -> error("starlark::assert::$func, got true!\nCode:\n$program")
            null -> error("starlark::assert::$func, not a bool!\nCode:\n$program\nResult\n$v")
        }
    }

    /** Set the [Dialect] that future tests will use. */
    fun dialect(x: Dialect) {
        dialect = x.copy()
    }

    /** Set specific fields in the [Dialect] that future tests will use. */
    fun dialectSet(f: (Dialect) -> Unit) {
        f(dialect)
    }

    /**
     * Add a [FrozenModule] to the environment that future tests can access via
     * `load`. To construct the [FrozenModule] automatically use [module].
     */
    fun moduleAdd(name: String, module: FrozenModule) {
        modules[name] = module
    }

    /**
     * Add a module to the environment that future tests can access.
     *
     * ```
     * val a = Assert()
     * a.module("hello.star", "hello = 'world'")
     * a.isTrue("load('hello.star', 'hello'); hello == 'world'")
     * ```
     */
    fun module(name: String, program: String): FrozenModule {
        val module = withGc { gc ->
            Module.withTempHeap { module ->
                executeUnwrap("module", "$name.bzl", program, module, gc)
                module.freeze()
            }
        }.getOrThrow()
        moduleAdd(name, module)
        return module
    }

    /** Set the [Globals] that future tests have access to. */
    fun globals(x: Globals) {
        globals = x
    }

    /**
     * Modify the [Globals] that future tests have access to.
     * Note that this method will start from the default environment for [Assert],
     * ignoring any previous [globals] or [globalsAdd] calls.
     */
    fun globalsAdd(f: (GlobalsBuilder) -> Unit) {
        globals(mkEnvironment().with(f).build())
    }

    private fun failsWithName(func: String, program: String, msgs: List<String>): io.github.kotlinmania.starlark_kotlin.Error {
        return withGc { gc ->
            Module.withTempHeap { moduleEnv ->
                val original = executeFail(func, program, moduleEnv, gc)
                // We really want to check the error message, but if in our doc tests we do:
                // fail("bad") # error: magic
                // Then when we print the source code, magic is contained in the error message.
                // Therefore, find the internals.
                val inner = original.withoutDiagnostic()
                val errMsg = inner.toString()
                for (msg in msgs) {
                    if (!errMsg.contains(msg)) {
                        error(
                            "starlark::assert::$func, failed with the wrong message!\n" +
                                "Code:\n$program\n" +
                                "Error:\n$inner\n" +
                                "Missing:\n$msg\n" +
                                "Expected:\n$msgs"
                        )
                    }
                }
                original
            }
        }
    }

    /**
     * A program that must fail with an error message that contains a specific
     * string. Remember that the purpose of `fail` is to ensure you get
     * the right error, not to fully specify the error - usually only one or
     * two words will be sufficient to ensure that.
     *
     * ```
     * Assert().fail("fail('hello')", "ello")
     * ```
     */
    fun fail(program: String, msg: String): io.github.kotlinmania.starlark_kotlin.Error {
        return failsWithName("fail", program, listOf(msg))
    }

    /**
     * A program that must fail with an error message that contains a specific
     * set of strings. Remember that the purpose of `fail` is to ensure you get
     * the right error, not to fully specify the error - usually only one or
     * two words will be sufficient to ensure that. The words do not have to be
     * in order.
     *
     * ```
     * Assert().fails("fail('hello')", listOf("fail", "ello"))
     * ```
     */
    fun fails(program: String, msgs: List<String>): io.github.kotlinmania.starlark_kotlin.Error {
        return failsWithName("fails", program, msgs)
    }

    /**
     * A program that must execute successfully without an exception. Often uses
     * assert_eq. Returns the resulting value.
     *
     * ```
     * Assert().pass("assert_eq(1, 1)")
     * ```
     */
    fun pass(program: String): OwnedFrozenValue {
        return withGc { gc ->
            Module.withTempHeap { env ->
                val res = executeUnwrap("pass", "assert.bzl", program, env, gc)
                env.set("_", res)
                env.freeze()
                    .getOrThrow()
                    .get("_")!!
            }
        }
    }

    /**
     * A program that must execute successfully without an exception. Returns the frozen module
     * that `program` was evaluated in.
     */
    fun passModule(program: String): FrozenModule {
        return withGc { gc ->
            Module.withTempHeap { env ->
                executeUnwrap("pass", "assert.bzl", program, env, gc)
                env.freeze().getOrThrow()
            }
        }
    }

    /**
     * A program that must evaluate to `True`.
     *
     * ```
     * Assert().isTrue("""
     * x = 1 + 1
     * x == 2
     * """)
     * ```
     */
    fun isTrue(program: String) {
        withGc { gc ->
            Module.withTempHeap { env -> executeUnwrapTrue("is_true", program, env, gc) }
        }
    }

    /** A program that must evaluate to `False`. */
    fun isFalse(program: String) {
        withGc { gc ->
            Module.withTempHeap { env -> executeUnwrapFalse("is_false", program, env, gc) }
        }
    }

    /**
     * Many lines, each of which must individually evaluate to `True` (or be blank lines).
     *
     * ```
     * Assert().allTrue("""
     * 1 == 1
     *
     * 2 == 1 + 1
     * """)
     * ```
     */
    fun allTrue(program: String) {
        withGc { gc ->
            for (s in program.lines()) {
                if (s == "") {
                    continue
                }
                Module.withTempHeap { env -> executeUnwrapTrue("all_true", s, env, gc) }
            }
        }
    }

    /**
     * Two programs that must evaluate to the same (non-error) result.
     *
     * ```
     * Assert().eq("1 + 1", "2")
     * ```
     */
    fun eq(lhs: String, rhs: String) {
        withGc { gc ->
            Heap.temp { heap ->
                val lhsM = Module.withHeap(heap)
                val rhsM = Module.withHeap(heap)
                val lhsV = executeUnwrap("eq", "lhs.bzl", lhs, lhsM, gc)
                val rhsV = executeUnwrap("eq", "rhs.bzl", rhs, rhsM, gc)
                if (lhsV != rhsV) {
                    error(
                        "starlark::assert::eq, values differ!\n" +
                            "Code 1:\n$lhs\n" +
                            "Code 2:\n$rhs\n" +
                            "Value 1:\n$lhsV\n" +
                            "Value 2\n$rhsV"
                    )
                }
            }
        }
    }

    companion object {
        /** See [Assert.eq]. */
        fun eq(lhs: String, rhs: String) {
            Assert().eq(lhs, rhs)
        }

        /** See [Assert.fail]. */
        fun fail(program: String, msg: String): io.github.kotlinmania.starlark_kotlin.Error {
            return Assert().fail(program, msg)
        }

        internal fun failGolden(path: String, program: String): io.github.kotlinmania.starlark_kotlin.Error {
            val trimmed = program.trim()
            val e = fails(trimmed, emptyList())
            val output = "Program:\n\n$trimmed\n\nError:\n\n$e\n"
            io.github.kotlinmania.starlark_kotlin.golden_test_template.goldenTestTemplate(path, output)
            return e
        }

        internal fun failSkipTypecheck(program: String, msg: String): io.github.kotlinmania.starlark_kotlin.Error {
            val a = Assert()
            a.disableStaticTypechecking()
            return a.fail(program, msg)
        }

        /** See [Assert.fails]. */
        fun fails(program: String, msgs: List<String>): io.github.kotlinmania.starlark_kotlin.Error {
            return Assert().fails(program, msgs)
        }

        internal fun failsSkipTypecheck(program: String, msgs: List<String>): io.github.kotlinmania.starlark_kotlin.Error {
            val a = Assert()
            a.disableStaticTypechecking()
            return a.fails(program, msgs)
        }

        /** See [Assert.isTrue]. */
        fun isTrue(program: String) {
            Assert().isTrue(program)
        }

        /** See [Assert.isFalse]. */
        fun isFalse(program: String) {
            Assert().isFalse(program)
        }

        internal fun isTrueSkipTypecheck(program: String) {
            val a = Assert()
            a.disableStaticTypechecking()
            a.isTrue(program)
        }

        /** See [Assert.allTrue]. */
        fun allTrue(expressions: String) {
            val a = Assert()
            a.disableStaticTypechecking()
            a.allTrue(expressions)
        }

        /** See [Assert.pass]. */
        fun pass(program: String): OwnedFrozenValue {
            return Assert().pass(program)
        }

        /** See [Assert.passModule]. */
        fun passModule(program: String): FrozenModule {
            return Assert().passModule(program)
        }
    }
}
