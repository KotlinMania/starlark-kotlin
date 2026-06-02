// port-lint: tests tests/uncategorized.rs
package io.github.kotlinmania.starlark.tests

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

import io.github.kotlinmania.starlark.assert.Assert
import io.github.kotlinmania.starlark.assert.failsSkipTypecheck
import io.github.kotlinmania.starlark.environment.Globals
import io.github.kotlinmania.starlark.environment.GlobalsBuilder
import io.github.kotlinmania.starlark.environment.Module
import io.github.kotlinmania.starlark.eval.evalModule
import io.github.kotlinmania.starlark.eval.runtime.Evaluator
import io.github.kotlinmania.starlark.goldentesttemplate.goldenTestTemplate
import io.github.kotlinmania.starlark.syntax.AstModule
import io.github.kotlinmania.starlark.syntax.dialect.Dialect
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.AllocValue
import io.github.kotlinmania.starlark.values.ComplexValue
import io.github.kotlinmania.starlark.values.Freeze
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.Trace
import io.github.kotlinmania.starlark.values.layout.Freezer
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.avalues.allocComplex
import io.github.kotlinmania.starlark.values.layout.avalues.simple.allocSimple
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.layout.heap.Tracer
import io.github.kotlinmania.starlark.values.layout.heap.ValueHolder
import io.github.kotlinmania.starlark.values.types.dict.SmallMapUnpackValue
import io.github.kotlinmania.starlark.values.types.none.NoneType
import kotlin.test.Test
import kotlin.test.assertTrue

class UncategorizedTests {
    @Test
    fun aliasTest() {
        Assert.isTrue(
            """
a = [1, 2, 3]
b = a
a[2] = 0
a == [1, 2, 0] and b == [1, 2, 0]
""",
        )
    }

    @Test
    fun testBadBreak() {
        Assert.fails("break", listOf("break", "outside of", "loop"))
        Assert.fails(
            "def foo(x):\n  if 1:\n    break",
            listOf("break", "outside of", "loop"),
        )
        Assert.fails(
            "def foo(x):\n  if 1:\n    continue",
            listOf("continue", "outside of", "loop"),
        )
        Assert.fail(
            """
def foo(x):
    for y in x:
        def bar(y):
            continue""",
            "outside of",
        )
        Assert.fail("return 1", "outside of a `def`")
        Assert.fail("for x in []:\n  return 1", "outside of a `def`")
    }

    @Test
    fun testTabsFail() {
        val a = Assert()
        a.fail("def f():\n\tpass", "Parse error")
        a.fail("def f():\n x\t=3", "Parse error")
    }

    @Test
    fun testTopLevelStatements() {
        Assert.pass(
            """
j = 0
for i in range(10):
    if i % 2 == 0:
        j += i
assert_eq(j, 20)
""",
        )
    }

    @Test
    fun testCompiledLiterals() {
        Assert.isTrue(
            """
def f():
    return [[]]
y = f()
y.append(1)
y == [[],1]""",
        )
        Assert.isTrue(
            """
def f():
    return {1:2}
y = f()
y[3] = 4
y == {1:2, 3:4}""",
        )
        // This test breaks if we compile constants deep compile the literals
        // and don't deep thaw them
        Assert.isTrue(
            """
def f():
    return [[]]
y = f()[0]
y.append(1)
y == [1]""",
        )
    }

    @Test
    fun testFrozenIteration() {
        // nested iteration
        Assert.isTrue(
            """
def loop():
    xs = [1, 2, 3]
    z = 0
    for x in xs:
        for y in xs:
            z += x + y
    return z
loop() == 36""",
        )
        // iterate, mutate, iterate
        Assert.isTrue(
            """
def loop():
    y = 0
    xs = [1, 2, 3]
    for x in xs:
        y += x
    xs.append(4)
    for x in xs:
        y += x
    return y
loop() == 16""",
        )
        // iterate and mutate at the same time
        Assert.fail(
            """
def loop():
    xs = [1, 2, 3]
    for x in xs:
        if len(xs) == 3:
            xs.append(4)
loop()""",
            "mutates an iterable",
        )
    }

    @Test
    fun testLvalueOnce() {
        Assert.isTrue(
            """
ys = [1]
xs = [1,2,3,4]

def f():
    return ys[0]

def g():
    ys[0] = 2;
    return 10

xs[f()] += g()
# f must be evaluated first, and only once
xs == [1,12,3,4]
""",
        )
        Assert.isTrue(
            """
ys = [1]
xs = [1,2,3,4]

def f():
    return ys[0]

def g():
    ys[0] = 2;
    return 10

xs[f()] = g()
xs == [1, 2, 10, 4]
""",
        )
    }

    @Test
    fun testAddAssign() {
        // += behaves differently on different types
        Assert.pass(
            """
x = 1
x += 8
assert_eq(x, 9)""",
        )
        Assert.pass(
            """
orig = [1, 2]
x = orig
x += [3]
assert_eq(x, [1, 2, 3])
assert_eq(orig, [1, 2, 3])
""",
        )
        Assert.pass(
            """
orig = (1, 2)
x = orig
# TODO(nga): typechecker should accept it.
x = noop(x)
x += (3,)
assert_eq(x, (1, 2, 3))
assert_eq(orig, (1, 2))
""",
        )
        Assert.fail(
            """
x = {1: 2}
x += {3: 4}
""",
            "not supported",
        )
        Assert.pass(
            """
x = [1, 2]
x[0] += 5
assert_eq(x, [6, 2])
""",
        )
        Assert.pass(
            """
x = {1: 2}
x[1] += 5
assert_eq(x, {1: 7})
""",
        )
        Assert.fail(
            """
def foo():
    xs = [1, 2]
    for x in xs:
        xs += [1]
        break
foo()
""",
            "mutates an iterable",
        )
        Assert.fail(
            """
xs = (1, 2)
xs[1] += 1
""",
            "Immutable",
        )
    }

    @Test
    fun testRadd() {
        // We want select append to always produce a select, much like the
        // Bazel/Buck `select` function.

        class Select(
            val items: MutableList<Int>,
        ) : StarlarkValue,
            AllocValue {
            override val TYPE: String get() = "select"

            override fun toString(): String = "\$$items"

            override fun starlarkTypeRepr(): Ty = Ty.any()

            fun add(other: Select): Select {
                val result = items.toMutableList()
                result.addAll(other.items)
                return Select(result)
            }

            private fun fromValue(value: Value, heap: Heap): Select? {
                val direct = value.downcastRef<Select>()
                if (direct != null) return direct
                val iter = value.iterate(heap).getOrNull() ?: return null
                val ints = mutableListOf<Int>()
                for (v in iter) {
                    ints.add(v.unpackI32() ?: return null)
                }
                return Select(ints)
            }

            override fun radd(lhs: Value, heap: Heap): Result<Value>? {
                val lhsSelect = fromValue(lhs, heap) ?: return null
                return Result.success(heap.alloc(lhsSelect.add(this)))
            }

            override fun add(rhs: Value, heap: Heap): Result<Value>? {
                val rhsSelect = fromValue(rhs, heap) ?: return null
                return Result.success(heap.alloc(this.add(rhsSelect)))
            }

            override fun collectRepr(collector: StringBuilder) {
                collector.append(toString())
            }

            override fun allocValue(heap: Heap): Value = heap.allocSimple(this)
        }

        fun moduleFunctions(builder: GlobalsBuilder) {
            builder.setFunction("select") { args, eval ->
                val arg =
                    args.positionalAll().firstOrNull()
                        ?: return@setFunction Result.failure<Value>(Exception("expected list"))
                val iter = arg.iterate(eval.heap()).getOrThrow()
                val ints = mutableListOf<Int>()
                for (v in iter) {
                    ints.add(v.unpackI32() ?: throw Exception("expected int"))
                }
                Result.success<Value>(eval.heap().alloc(Select(ints)))
            }
        }

        val a = Assert()
        a.globalsAdd(::moduleFunctions)
        a.pass(
            """
s1 = select([1])
s2 = select([2])
assert_eq(repr(s1), "${'$'}[1]")
assert_eq(repr(s1 + [3]), "${'$'}[1, 3]")
assert_eq(repr([3] + s1), "${'$'}[3, 1]")
assert_eq(repr(s1 + s2), "${'$'}[1, 2]")

s1 += [3]
v = [4]
v += s2
s2 += s1
assert_eq(repr(s1), "${'$'}[1, 3]")
assert_eq(repr(v), "${'$'}[4, 2]")
assert_eq(repr(s2), "${'$'}[2, 1, 3]")
""",
        )
    }

    @Test
    fun testCompoundAssignment() {
        Assert.pass(
            """
x = 1
x <<= 8
assert_eq(x, 256)""",
        )
        Assert.pass(
            """
x = 1
x ^= 8
assert_eq(x, 9)""",
        )
    }

    @Test
    fun testStaticNameChecks() {
        val a = Assert()
        a.fail(
            """
def f():
    no_name()
True""",
            "no_name",
        )
    }

    @Test
    fun testFunctionToName() {
        val a = Assert()
        a.module(
            "x",
            """
def mine():
    pass
names = {repr: "repr", str: "str", mine: "mine"}
assert_eq(names[repr], "repr")
assert_eq(names[mine], "mine")
assert_eq(names[str], "str")
""",
        )
        a.pass(
            """
load("x", "mine", "names")
assert_eq(names[repr], "repr")
assert_eq(names[mine], "mine")
assert_eq(names[str], "str")
""",
        )
    }

    // Tests diagnostics error display.
    @Test
    fun testDiagnosticsDisplay() {
        fun fail1(): Result<Unit> = Result.failure(Exception("fail 1"))

        fun fail2(): Result<Unit> {
            return fail1().onFailure {
                return Result.failure(Exception("fail 2", it))
            }
        }

        fun fail3(): Result<Unit> {
            return fail2().onFailure {
                return Result.failure(Exception("fail 3", it))
            }
        }

        fun moduleFunctions(builder: GlobalsBuilder) {
            builder.setFunction("rust_failure") { _, _ ->
                fail3().onFailure {
                    return@setFunction Result.failure<Any>(Exception("rust failure", it))
                }
                Result.success<Any>(NoneType)
            }
        }

        val a = Assert()
        a.globalsAdd(::moduleFunctions)

        a.module(
            "imported",
            """
# blank lines to make line numbers bigger and more obvious
#
#
#
#
x = []
def should_fail():
    rust_failure()""",
        )

        val err =
            a.fail(
                """
load('imported', 'should_fail')
should_fail()""",
                "rust failure",
            )

        goldenTestTemplate(
            "src/tests/uncategorized_diagnostics_display_default.golden",
            trimRustBacktrace(err.toString()),
        )

        goldenTestTemplate(
            "src/tests/uncategorized_diagnostics_display_hash.golden",
            trimRustBacktrace(err.toString()),
        )

        goldenTestTemplate(
            "src/tests/uncategorized_diagnostics_display_debug.golden",
            trimRustBacktrace(err.toString()),
        )
    }

    // Check that errors print out "nicely"
    @Test
    fun testErrorDisplay() {
        val a = Assert()
        a.module(
            "imported",
            """
# blank lines to make line numbers bigger and more obvious
#
#
#
#
x = []
def add2(z):
  add(z)
def add(z):
  x.append(z)""",
        )

        val err =
            a.fail(
                """
load('imported', 'add2')
def add3(z):
    add2(z)
add3(8)""",
                "Immutable",
            )

        goldenTestTemplate(
            "src/tests/uncategorized_error_display.golden",
            trimRustBacktrace(err.toString()),
        )

        goldenTestTemplate(
            "src/tests/uncategorized_error_display_hash.golden",
            trimRustBacktrace(err.toString()),
        )
    }

    @Test
    fun testLoadReexport() {
        run {
            val a = Assert()
            a.dialectSet { d -> d.copy(enableLoadReexport = true) }
            a.module("a", "x = 1")
            a.module("b", "load('a', 'x')")
            a.pass("load('b', 'x')\nassert_eq(x, 1)")
        }

        run {
            val a = Assert()
            a.dialectSet { d -> d.copy(enableLoadReexport = false) }
            a.module("a", "x = 1")
            a.module("b", "load('a', 'x')")
            a.fail(
                "load('b', 'x')\nassert_eq(x, 1)",
                "Module symbol `x` is not exported",
            )
        }
    }

    @Test
    fun testModuleVisibilityPreservedByEvaluator() {
        // Make sure that when we use a module in the evaluator, the entering / exiting the
        // module with ScopeData preserves the visibility of symbols.

        val globals = Globals.standard()

        Module
            .withTempHeap { import ->
                import.set("a", Value.testingNewInt(1))
                import.setPrivate(
                    import.frozenHeap().allocStrIntern("b"),
                    Value.testingNewInt(2),
                )

                run {
                    val eval = Evaluator(import)
                    val ast = AstModule.parse("prelude.bzl", "c = 3", Dialect.Standard).getOrThrow()
                    // This mutates the original module named `import`
                    eval.evalModule(ast, globals).getOrThrow()
                }
                val frozenImport = import.freeze().getOrThrow()

                Module
                    .withTempHeap<Result<Unit>> { mUsesPublic ->
                        mUsesPublic.importPublicSymbols(frozenImport)
                        run {
                            val eval = Evaluator(mUsesPublic)
                            val ast = AstModule.parse("code.bzl", "d = a", Dialect.Standard).getOrThrow()
                            eval.evalModule(ast, globals).getOrThrow()
                        }
                        Result.success(Unit)
                    }.getOrThrow()

                Module
                    .withTempHeap<Result<Unit>> { mUsesPrivate ->
                        mUsesPrivate.importPublicSymbols(frozenImport)
                        run {
                            val eval = Evaluator(mUsesPrivate)
                            val ast = AstModule.parse("code.bzl", "d = b", Dialect.Standard).getOrThrow()
                            val err =
                                try {
                                    eval.evalModule(ast, globals).getOrThrow()
                                    error("Evaluation should have failed using a private symbol")
                                } catch (e: Exception) {
                                    e
                                }

                            val msg = err.toString()
                            val expectedMsg = "Variable `b` not found"
                            assertTrue(
                                msg.contains(expectedMsg),
                                "Expected `$expectedMsg` to be in error message `$msg`",
                            )
                        }
                        Result.success(Unit)
                    }.getOrThrow()

                Result.success(Unit)
            }.getOrThrow()
    }

    @Test
    fun testCancellation() {
        // Make sure that when we use a module in the evaluator, the entering / exiting the
        // module with ScopeData preserves the visibility of symbols.

        val globals = Globals.standard()
        Module
            .withTempHeap { import ->
                val eval = Evaluator(import)
                eval.setCheckCancelled { true }

                val ast =
                    AstModule
                        .parse(
                            "prelude.bzl",
                            // Note that the exact range here is unimportant, so long as it's small enough to not trigger the "infrequent" checks
                            "def loop():\n    for i in range(10):\n       pass\nloop()",
                            Dialect.Standard,
                        ).getOrThrow()
                assertTrue(eval.evalModule(ast, globals).isFailure)

                val ast2 =
                    AstModule
                        .parse(
                            "prelude.bzl",
                            // Note that the exact range here is unimportant, so long as it's large enough to trigger the "infrequent" checks
                            "def loop():\n    for i in range(1000000):\n       pass\nloop()",
                            Dialect.Standard,
                        ).getOrThrow()
                val err = eval.evalModule(ast2, globals)

                val expected = "Evaluation cancelled"
                val errMsg = err.toString()
                if (!errMsg.contains(expected)) {
                    error("Error:\n$errMsg\nExpected:\n$expected")
                }

                Result.success(Unit)
            }.getOrThrow()
    }

    @Test
    fun testLoadDidYouMean() {
        val a = Assert()
        a.module("categories", "colour = 1")
        a.fail(
            "load('categories', 'color')",
            "Module has no symbol `color`, did you mean `colour`?",
        )
    }

    @Test
    fun testGetAttrDidYouMeanBuiltin() {
        Assert.fail(
            "[].appen",
            "Object of type `list` has no attribute `appen`, did you mean `append`?",
        )
    }

    @Test
    fun testGetAttrDidYouMeanCustom() {
        Assert.fail(
            "noop(struct(grey=1)).gray",
            "Object of type `struct` has no attribute `gray`, did you mean `grey`?",
        )
        Assert.fail(
            "Rec = record(grey=int); Rec(grey=1).gray",
            "Object of type `record` has no attribute `gray`, did you mean `grey`?",
        )
    }

    @Test
    fun testGlobalsDidYouMean() {
        Assert.fail("true", "Variable `true` not found, did you mean `True`?")
    }

    @Test
    fun testModuleLevelDidYouMean() {
        Assert.fail(
            "_x = 1; print(x)",
            "Variable `x` not found, did you mean `_x`?",
        )
    }

    @Test
    fun testModuleLevelFromDefDidYouMean() {
        Assert.fail(
            "def _func(): return func",
            "Variable `func` not found, did you mean `_func`?",
        )
    }

    @Test
    fun testLocalFromDefDidYouMean() {
        Assert.fail(
            "def f(discreet): return discrete",
            "Variable `discrete` not found, did you mean `discreet`?",
        )
    }

    @Test
    fun testComprDidYouMean() {
        Assert.fail(
            "[val for value in []]",
            "Variable `val` not found, did you mean `value`?",
        )
    }

    @Test
    fun testUnassigned() {
        Assert.fails("y = x; x = 1", listOf("referenced before assignment", "`x`"))
        Assert.fails(
            "def f():\n y = x; x = 1\nf()",
            listOf("referenced before assignment", "`x`"),
        )
        Assert.fails(
            """
def f():
    y = x
    x = 1
def g(q = 1):
    f()
g()""",
            listOf("referenced before assignment", "`x`"),
        )
        Assert.failsSkipTypecheck(
            "[1 for _ in [1] for y in y]",
            listOf("referenced before assignment", "`y`"),
        )
        Assert.failsSkipTypecheck(
            "def f():\n [1 for _ in [1] for y in y]\nf()",
            listOf("referenced before assignment", "`y`"),
        )
    }

    @Test
    fun testSelfAssign() {
        // Starlark spec is not clear whether it is allowed or not.
        Assert.pass("x = [1,2]\na, x[0] = x")
        Assert.pass("x = {0:0,1:1}\na, x[0] = x")
    }

    @Test
    fun testNestedLoops() {
        // Nested loops with returns used to cause problems in some cases, add a test
        Assert.pass(
            """
def foo(y):
    for x in [1,2,3,4]:
        if x == 3:
            return y

def bar(xs):
    res = []
    for x in xs:
        if type(x) == type(1):
            fail("Type confusion")
        res.append(foo(x))
    assert_eq(xs, res)
bar(["a","b","c"])
""",
        )
    }

    @Test
    fun testLabelAssign() {
        // Test the a.b = c construct.
        // No builtin Starlark types support it, so we have to define a custom type (wrapping a dictionary)

        class FrozenWrapper : StarlarkValue {
            override val TYPE: String get() = "wrapper"

            override fun getTypeStarlarkRepr(): Ty = Ty.any()
        }

        class Wrapper(
            val map: MutableMap<String, Value> = mutableMapOf(),
        ) : ComplexValue,
            AllocValue,
            Trace,
            Freeze<FrozenWrapper> {
            override val TYPE: String get() = "wrapper"

            override fun starlarkTypeRepr(): Ty = Ty.any()

            override fun toString(): String = map.toString()

            override fun trace(tracer: Tracer) {
                for ((_, value) in map) {
                    val holder = ValueHolder(value)
                    tracer.trace(holder)
                }
            }

            override fun freeze(freezer: Freezer): Result<FrozenWrapper> = Result.success(FrozenWrapper())

            override fun getAttr(attribute: String, heap: Heap): Value? = map[attribute]

            override fun setAttr(attribute: String, newValue: Value): Result<Unit> {
                map[attribute] = newValue
                return Result.success(Unit)
            }

            override fun allocValue(heap: Heap): Value = heap.allocComplex(this)
        }

        fun moduleFunctions(builder: GlobalsBuilder) {
            builder.setFunction("wrapper") { _, eval ->
                Result.success(eval.heap().allocComplex(Wrapper()))
            }
        }

        val a = Assert()
        a.globalsAdd(::moduleFunctions)
        a.pass(
            """
a = wrapper()
b = wrapper()
a.foo = 100
a.bar = 93
b.foo = 7
assert_eq(a.bar + b.foo, a.foo)

a.foo += 8
assert_eq(a.foo, 108)

count = []
def mk_wrapper():
    count.append(1)
    res = wrapper()
    res.x = 9
    return res

mk_wrapper().x += 5
assert_eq(len(count), 1)
""",
        )
    }

    @Test
    fun testSelfMutateList() {
        // Check functions that mutate and access self on lists
        val a = Assert()
        // TODO(nga): fix and enable.
        a.disableStaticTypechecking()
        a.isTrue(
            """
xs = [1, 2, 3]
xs.extend(xs)
xs == [1, 2, 3, 1, 2, 3]
""",
        )
        a.isTrue(
            """
xs = [1, 2, 3]
xs += xs
xs == [1, 2, 3, 1, 2, 3]
""",
        )
        a.fail(
            """
xs = [1, 2, 3]
xs.pop(xs)
""",
            "Type of parameter `index` doesn't match",
        )
        a.fail(
            """
xs = [1, 2, 3]
xs.remove(xs)
""",
            "not found in list",
        )
        a.isTrue(
            """
xs = [1, 2, 3]
xs.append(xs)
xs.remove(xs)
xs == [1, 2, 3]
""",
        )
        a.isTrue(
            """
xs = [1, 2, 3]
xs += xs
xs == [1, 2, 3, 1, 2, 3]
""",
        )
        a.fail(
            """
xs = []
xs[xs]
""",
            "Expected `int`, but got",
        )
        a.fail(
            """
xs = []
xs[xs] = xs
""",
            "Expected `int`, but got",
        )
    }

    @Test
    fun testListSliceDoesNotAcceptBool() {
        // TODO(nga): this should fail.
        Assert.fail("[1][False]", "Expected `int`, but got `bool")
    }

    @Test
    fun testSelfMutateDict() {
        // Check functions that mutate and access self on dicts
        Assert.isTrue(
            """
xs = {1: 2}
xs |= xs
xs == {1: 2}
""",
        )
        Assert.fail(
            """
xs = {}
xs[xs]
""",
            "not hashable",
        )
        Assert.fail(
            """
xs = {}
xs[xs] = 1
""",
            "not hashable",
        )
        Assert.isTrue(
            """
xs = {}
xs[1] = xs
len(xs[1]) == 1
""",
        )
        Assert.isTrue(
            """
xs = {}
xs.update(xs)
len(xs) == 0
""",
        )
    }

    @Test
    fun testDictUnion() {
        Assert.isTrue(
            """
xs = {1: 2, 3: 4}
xs |= {5: 6}
xs |= {1: 7}
xs.items() == [(1, 7), (3, 4), (5, 6)]
""",
        )
    }

    @Test
    fun testDictWithFrozenListKeyInlined() {
        val a = Assert()
        a.module(
            "m.star",
            """
li = []
def f():
    # This should fail at runtime.
    return {li: 1}
    """,
        )
        a.fail(
            """
load('m.star', 'f')
f()
    """,
            "Value of type `list` is not hashable",
        )
    }

    @Test
    fun testJoe() {
        // Based on discussions at https://github.com/facebook/starlark-rust/issues/22
        val code = """
def animal(id):
    return {
        "kind": "giraffe",
        "name": "giraffe-%s" % id,
        "feeding": [
            {
                "name": "feeder",
                "image": "photos-%s" % id,
                "commands": [
                    "lift",
                    "roll-over",
                ],
            },
        ],
    }
animal("Joe")
"""
        Module
            .withTempHeap { m ->
                val globals = Globals.standard()
                val eval = Evaluator(m)
                val ast = AstModule.parse("code.bzl", code, Dialect.Standard).getOrThrow()
                val res: Value = eval.evalModule(ast, globals).getOrThrow()
                val animal = SmallMapUnpackValue.unpackValueImpl<String, Value>(res).getOrThrow()
                println("animal = $animal")
                Result.success(Unit)
            }.getOrThrow()
    }

    @Test
    fun testFuzzer59102() {
        // From https://bugs.chromium.org/p/oss-fuzz/issues/detail?id=59102
        val src = "\"\uDB40\uDC70"
        val res = AstModule.parse("hello_world.star", src, Dialect.Standard)
        // The panic actually only happens when we format the result
        val unused = res.toString()
    }

    @Test
    fun testFuzzer59371() {
        // From https://bugs.chromium.org/p/oss-fuzz/issues/detail?id=59371
        val src = "\"\u2009\\x"
        val res = AstModule.parse("hello_world.star", src, Dialect.Standard)
        // The panic actually only happens when we format the result
        val unused = res.toString()
    }

    @Test
    fun testFuzzer59839() {
        // From https://bugs.chromium.org/p/oss-fuzz/issues/detail?id=59839
        val src = "\"{20000000000000000396}\".format()"
        val ast = AstModule.parse("hello_world.star", src, Dialect.Standard).getOrThrow()
        val globals: Globals = Globals.standard()
        Module
            .withTempHeap { module ->
                val eval: Evaluator = Evaluator(module)
                assertTrue(eval.evalModule(ast, globals).isFailure)
                Result.success(Unit)
            }.getOrThrow()
    }
}
