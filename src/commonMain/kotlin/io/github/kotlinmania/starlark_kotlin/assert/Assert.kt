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

//! Utilities to test Starlark code execution.

import io.github.kotlinmania.starlark_kotlin.environment.FrozenModule
import io.github.kotlinmania.starlark_kotlin.environment.Globals
import io.github.kotlinmania.starlark_kotlin.environment.GlobalsBuilder
import io.github.kotlinmania.starlark_kotlin.environment.Module
import io.github.kotlinmania.starlark_kotlin.stdlib.PrintHandler
import io.github.kotlinmania.starlark_kotlin.values.AllocValue
import io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.factory.TypeCompiled
import io.github.kotlinmania.starlark_kotlin.values.types.structs.AllocStruct
import io.github.kotlinmania.starlark_kotlin.values.types.string.Evaluator
import io.github.kotlinmania.starlark_kotlin.values.types.list.NoneType
import io.github.kotlinmania.starlark_kotlin.values.owned.OwnedFrozenValue
import io.github.kotlinmania.starlark_kotlin.eval.runtime.file_loader.ReturnFileLoader
import io.github.kotlinmania.starlark_kotlin.analysis.unused_loads.FileSpanRef
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.syntax.dialect.Dialect
import io.github.kotlinmania.starlark_kotlin.stdlib.new
import io.github.kotlinmania.starlark_kotlin.starlark_error.Error
import io.github.kotlinmania.starlark_kotlin.eval.runtime.positional
import io.github.kotlinmania.starlark_kotlin.values.types.tuple.it
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

// fn mk_environment() -> GlobalsBuilder
private fun mkEnvironment(): GlobalsBuilder {
    return GlobalsBuilder.extended().with(::testFunctions)
}

// static GLOBALS: Lazy<Globals> = Lazy::new(|| mk_environment().build())
private val GLOBALS: Globals by lazy { mkEnvironment().build() }

// static ASSERTS_STAR: Lazy<FrozenModule> = ...
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

// fn assert_equals<'v>(a: Value<'v>, b: Value<'v>) -> starlark::Result<NoneType>
private fun assertEquals(a: Value, b: Value): Result<NoneType> {
    return if (!a.equals(b).getOrElse { return Result.failure(it) }) {
        Result.failure(Exception("assert_eq: expected $a, got $b"))
    } else {
        Result.success(NoneType)
    }
}

// fn assert_different<'v>(a: Value<'v>, b: Value<'v>) -> starlark::Result<NoneType>
private fun assertDifferent(a: Value, b: Value): Result<NoneType> {
    return if (a.equals(b).getOrElse { return Result.failure(it) }) {
        Result.failure(Exception("assert_ne: but $a == $b"))
    } else {
        Result.success(NoneType)
    }
}

// fn assert_less_than<'v>(a: Value<'v>, b: Value<'v>) -> starlark::Result<NoneType>
private fun assertLessThan(a: Value, b: Value): Result<NoneType> {
    val cmp = a.compareTo(b).getOrElse { return Result.failure(it) }
    return if (cmp >= 0) {
        Result.failure(Exception("assert_lt: but $a >= $b"))
    } else {
        Result.success(NoneType)
    }
}

/// How often we garbage collection _should_ be transparent to the tests,
/// so we run each test in three configurations.
// #[derive(Clone, Copy, Dupe, Debug)]
// enum GcStrategy
private enum class GcStrategy {
    Never,  // Disable GC
    Auto,   // Use the automatic heuristics (in practice, this does almost no GC)
    Always, // GC as aggressively as we can
}

/// Definitions to support assert.star as used by the Go test suite
// #[starlark_module]
// fn asserts_star(builder: &mut GlobalsBuilder)
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
        // val msg = args.positional(1) // We don't actually check the message
        when (val result = f.invokePos(emptyList(), eval)) {
            else -> if (result.isFailure) {
                Result.success(NoneType)
            } else {
                Result.failure(Exception("assert.fails: didn't fail"))
            }
        }
    }
}

// #[starlark_module]
// pub(crate) fn test_functions(builder: &mut GlobalsBuilder)
internal fun testFunctions(builder: GlobalsBuilder) {
    // Used by one of the test methods in Go
    builder.setConst("fibonacci", listOf(0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89))

    // Approximate version of a method used by the Go test suite
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
        TypeCompiled.new(ty, eval.heap()).getOrElse { return@setFunction Result.failure(it) }
            .checkType(v, "v").getOrElse { return@setFunction Result.failure(it) }
        Result.success(NoneType)
    }

    /// Function which consumes arguments and that's it.
    ///
    /// This function is unknown to optimizer, so it can be used in optimizer tests.
    builder.setFunction("noop") { args, _ ->
        // kwargs are ignored
        Result.success(args.positionals().firstOrNull() ?: Value.newNone())
    }
}

/// Environment in which to run assertion tests.
// pub struct Assert<'a>
class Assert(
    private var dialect: Dialect = Dialect.AllOptionsInternal.copy(),
    private val modules: MutableMap<String, FrozenModule> = mutableMapOf(
        "asserts.star" to ASSERTS_STAR,
    ),
    private var globals: Globals = GLOBALS,
    private var gcStrategy: GcStrategy? = null,
    private var setupEval: (Evaluator) -> Unit = {},
    // Ideally `print_handler` should be set up in `setup_eval`
    // but if you know how to do it, show me how.
    private var printHandler: PrintHandler? = null,
    private var staticTypechecking: Boolean = true,
) {
    /// Disable garbage collection on the tests.
    // pub fn disable_gc(&mut self)
    fun disableGc() {
        gcStrategy = GcStrategy.Never
    }

    /// Configure a callback which is used to setup evaluator before each evaluation.
    // pub fn setup_eval(&mut self, setup: impl Fn(&mut Evaluator) + 'static)
    fun setupEval(setup: (Evaluator) -> Unit) {
        setupEval = setup
    }

    /// Configure the handler for `print` function.
    // pub fn set_print_handler(&mut self, handler: ...)
    fun setPrintHandler(handler: PrintHandler) {
        printHandler = handler
    }

    /// Disable static typechecking for test. It is off by default in `Evaluator`,
    /// but on by default in `Assert`.
    // pub fn disable_static_typechecking(&mut self)
    fun disableStaticTypechecking() {
        staticTypechecking = false
    }

    // fn with_gc<A>(&self, f: impl Fn(GcStrategy) -> A) -> A
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

    // fn execute<'v>(...)
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

    // fn execute_fail<'v>(...)
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

    // fn execute_unwrap<'v>(...)
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

    // fn execute_unwrap_true<'v>(...)
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

    // fn execute_unwrap_false<'v>(...)
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

    /// Set the [`Dialect`] that future tests will use.
    // pub fn dialect(&mut self, x: &Dialect)
    fun dialect(x: Dialect) {
        dialect = x.copy()
    }

    /// Set specific fields in the [`Dialect`] that future tests will use.
    // pub fn dialect_set(&mut self, f: impl FnOnce(&mut Dialect))
    fun dialectSet(f: (Dialect) -> Unit) {
        f(dialect)
    }

    /// Add a [`FrozenModule`] to the environment that future tests can access via
    /// `load`.
    // pub fn module_add(&mut self, name: &str, module: FrozenModule)
    fun moduleAdd(name: String, module: FrozenModule) {
        modules[name] = module
    }

    /// Add a module to the environment that future tests can access.
    // pub fn module(&mut self, name: &str, program: &str) -> FrozenModule
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

    /// Set the [`Globals`] that future tests have access to.
    // pub fn globals(&mut self, x: Globals)
    fun globals(x: Globals) {
        globals = x
    }

    /// Modify the [`Globals`] that future tests have access to.
    // pub fn globals_add(&mut self, f: impl FnOnce(&mut GlobalsBuilder))
    fun globalsAdd(f: (GlobalsBuilder) -> Unit) {
        globals(mkEnvironment().with(f).build())
    }

    // fn fails_with_name(...)
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

    /// A program that must fail with an error message that contains a specific string.
    // pub fn fail(&self, program: &str, msg: &str) -> crate::Error
    fun fail(program: String, msg: String): io.github.kotlinmania.starlark_kotlin.Error {
        return failsWithName("fail", program, listOf(msg))
    }

    /// A program that must fail with an error message that contains a specific set of strings.
    // pub fn fails(&self, program: &str, msgs: &[&str]) -> crate::Error
    fun fails(program: String, msgs: List<String>): io.github.kotlinmania.starlark_kotlin.Error {
        return failsWithName("fails", program, msgs)
    }

    /// A program that must execute successfully without an exception. Returns the resulting value.
    // pub fn pass(&self, program: &str) -> OwnedFrozenValue
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

    /// A program that must execute successfully without an exception. Returns the frozen module.
    // pub fn pass_module(&self, program: &str) -> FrozenModule
    fun passModule(program: String): FrozenModule {
        return withGc { gc ->
            Module.withTempHeap { env ->
                executeUnwrap("pass", "assert.bzl", program, env, gc)
                env.freeze().getOrThrow()
            }
        }
    }

    /// A program that must evaluate to `True`.
    // pub fn is_true(&self, program: &str)
    fun isTrue(program: String) {
        withGc { gc ->
            Module.withTempHeap { env -> executeUnwrapTrue("is_true", program, env, gc) }
        }
    }

    /// A program that must evaluate to `False`.
    // pub fn is_false(&self, program: &str)
    fun isFalse(program: String) {
        withGc { gc ->
            Module.withTempHeap { env -> executeUnwrapFalse("is_false", program, env, gc) }
        }
    }

    /// Many lines, each of which must individually evaluate to `True` (or be blank lines).
    // pub fn all_true(&self, program: &str)
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

    /// Two programs that must evaluate to the same (non-error) result.
    // pub fn eq(&self, lhs: &str, rhs: &str)
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
        /// See [`Assert::eq`].
        // pub fn eq(lhs: &str, rhs: &str)
        fun eq(lhs: String, rhs: String) {
            Assert().eq(lhs, rhs)
        }

        /// See [`Assert::fail`].
        // pub fn fail(program: &str, msg: &str) -> crate::Error
        fun fail(program: String, msg: String): io.github.kotlinmania.starlark_kotlin.Error {
            return Assert().fail(program, msg)
        }

        // pub(crate) fn fail_golden(path: &str, program: &str) -> crate::Error
        internal fun failGolden(path: String, program: String): io.github.kotlinmania.starlark_kotlin.Error {
            val trimmed = program.trim()
            val e = fails(trimmed, emptyList())
            val output = "Program:\n\n$trimmed\n\nError:\n\n$e\n"
            io.github.kotlinmania.starlark_kotlin.golden_test_template.goldenTestTemplate(path, output)
            return e
        }

        // pub(crate) fn fail_skip_typecheck(program: &str, msg: &str) -> crate::Error
        internal fun failSkipTypecheck(program: String, msg: String): io.github.kotlinmania.starlark_kotlin.Error {
            val a = Assert()
            a.disableStaticTypechecking()
            return a.fail(program, msg)
        }

        /// See [`Assert::fails`].
        // pub fn fails(program: &str, msgs: &[&str]) -> crate::Error
        fun fails(program: String, msgs: List<String>): io.github.kotlinmania.starlark_kotlin.Error {
            return Assert().fails(program, msgs)
        }

        // pub(crate) fn fails_skip_typecheck(program: &str, msgs: &[&str]) -> crate::Error
        internal fun failsSkipTypecheck(program: String, msgs: List<String>): io.github.kotlinmania.starlark_kotlin.Error {
            val a = Assert()
            a.disableStaticTypechecking()
            return a.fails(program, msgs)
        }

        /// See [`Assert::is_true`].
        // pub fn is_true(program: &str)
        fun isTrue(program: String) {
            Assert().isTrue(program)
        }

        /// See [`Assert::is_false`].
        // pub fn is_false(program: &str)
        fun isFalse(program: String) {
            Assert().isFalse(program)
        }

        // pub(crate) fn is_true_skip_typecheck(program: &str)
        internal fun isTrueSkipTypecheck(program: String) {
            val a = Assert()
            a.disableStaticTypechecking()
            a.isTrue(program)
        }

        /// See [`Assert::all_true`].
        // pub fn all_true(expressions: &str)
        fun allTrue(expressions: String) {
            val a = Assert()
            // TODO(nga): fix and enable.
            a.disableStaticTypechecking()
            a.allTrue(expressions)
        }

        /// See [`Assert::pass`].
        // pub fn pass(program: &str) -> OwnedFrozenValue
        fun pass(program: String): OwnedFrozenValue {
            return Assert().pass(program)
        }

        /// See [`Assert::pass_module`].
        // pub fn pass_module(program: &str) -> FrozenModule
        fun passModule(program: String): FrozenModule {
            return Assert().passModule(program)
        }
    }
}
