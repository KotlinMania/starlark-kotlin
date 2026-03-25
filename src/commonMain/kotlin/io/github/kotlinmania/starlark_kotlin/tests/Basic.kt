// port-lint: source src/tests/basic.rs
package io.github.kotlinmania.starlark_kotlin.tests

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

/// Basic expression tests.

import io.github.kotlinmania.starlark_kotlin.assert.Assert
import io.github.kotlinmania.starlark_kotlin.environment.GlobalsBuilder
import io.github.kotlinmania.starlark_kotlin.values.owned.OwnedFrozenValue
import io.github.kotlinmania.starlark_kotlin.values.layout.value

// #[test]
// fn arithmetic_test()
internal fun arithmeticTest() {
    Assert.isTrue("(1 + 2 == 3)")
    Assert.isTrue("(1 * 2 == 2)")
    Assert.isTrue("(-1 * 2 == -2)")
    Assert.isTrue("(5 // 2 == 2)")
    Assert.isTrue("(5 % 2 == 1)")
}

// #[test]
// fn bitwise_test()
internal fun bitwiseTest() {
    Assert.allTrue("""
3 & 6 == 2
3 & 6 == 2
-3 & 6 == 4
3 | 6 == 7
3 | -6 == -5
3 ^ 6 == 5
-3 ^ 6 == -5
-3 ^ -6 == 7
1 << 2 == 4
-1 << 2 == -4
1 >> 0 == 1
111 >> 2 == 27
~31 == -32
~-31 == 30
""")

    Assert.fail("1 << -13", "Negative left shift")
    Assert.fail("1 >> -13", "Negative right shift")
}

// #[test]
// fn test_operators()
internal fun testOperators() {
    Assert.eq("1+------2", "3")
}

// #[test]
// fn test_equality()
internal fun testEquality() {
    Assert.allTrue("""
None == None
True == True
True != False
1 == 1
1 != 2
"test" == "test"
"test" != "x"
[1, 2] == [1, 2]
[1, 3] != [1, 2]
[1, 3] != [1, 3, 4]
(1, 2) == (1, 2)
(1, 3) != (1, 2)
noop((1, 3)) != (1, 3, 4)
noop(range(4)) == range(0, 4, 1)
noop(range(4)) != range(0, 4, 2)
noop(range(4)) != [0,1,2,3,4]
{1: 2} == {1: 2}
{1: 2} != {}
{1: 2, 3: 4} == {1: 2, 3: 4}
{1: 2, 3: 4} == {3: 4, 1: 2}  # Spec is a little ambiguous here
repr == repr
repr != str
[].clear != [1].clear
x = []; x.clear != x.clear
x = []; y = x.clear; y == y
x = repr; y = repr; x == y
""")
}

// #[test]
// fn test_frozen_equality()
internal fun testFrozenEquality() {
    val program = "(str, (), 1, range(4), True, None, [8], {'test':3})"
    val a = Assert.pass(program)
    val b = Assert.pass(program)
    check(a.value() == b.value())

    val assert = Assert()
    assert.module("saved", "val = $program")
    assert.isTrue("load('saved', 'val'); val == $program")
}

// #[test]
// fn test_equality_multiple_globals()
internal fun testEqualityMultipleGlobals() {
    fun mkRepr(): OwnedFrozenValue {
        val a = Assert()
        val globals = GlobalsBuilder.extended().build()
        a.globals(globals)
        return a.pass("repr")
    }

    // Do things that compare by pointer still work if you
    // create fresh Globals for each of them.
    check(mkRepr().value() == mkRepr().value())
}

// #[test]
// fn test_comparison()
internal fun testComparison() {
    Assert.allTrue("""
False < True
1 < 2
"test" < "x"
[1, 3] > [1, 2]
[1, 3] < [1, 3, 4]
(1, 3) > (1, 2)
(1, 3) < (1, 3, 4)
""")
    Assert.fail("noop(None) < None", "`compare` not supported")
    Assert.failSkipTypecheck("(None, ) < (None, )", "`compare` not supported")
    Assert.failSkipTypecheck("x = (None,); x < x", "`compare` not supported")
    Assert.failSkipTypecheck("x = {}; x < x", "`compare` not supported")
    Assert.failSkipTypecheck("{} < {1: 2}", "`compare` not supported")
    Assert.failSkipTypecheck("range(1) < range(2)", "`compare` not supported")
    Assert.failSkipTypecheck("repr < str", "`compare` not supported")
}

// #[test]
// fn test_frozen_hash()
internal fun testFrozenHash() {
    val exprs = listOf("\"test\"", "\"x\"")
    val a = Assert()
    // TODO(nga): fix and enable.
    a.disableStaticTypechecking()
    a.module(
        "m",
        "dict = {x:len(x) for x in [${exprs.joinToString(",")}]}",
    )
    a.pass("""
load('m', frozen_dict='dict')
values = [${exprs.joinToString(",")}]
assert_eq(all([frozen_dict[x] != None for x in values]), True)
""")
}

// #[test]
// fn test_compare()
internal fun testCompare() {
    Assert.fail("noop(1) > False", "Operation `compare` not supported")
    Assert.isTrue("[1, 2] == [1, 2]")
    Assert.isTrue("noop(1) != True")
    Assert.isTrue("not (noop(None) == [1])")
    Assert.isTrue("""
xs = [1]
xs[0] = xs
xs == xs
""")
    Assert.isTrue("""
ys = [1]
xs = [ys]
ys[0] = xs
xs == xs
""")
    val a = Assert()
    // TODO(nga): fix and enable.
    a.disableStaticTypechecking()
    a.fail("""
ys = [1]
xs = [ys]
ys[0] = xs
xs == ys
""", "recursion")
}

// #[test]
// fn test_not_in_unhashable()
internal fun testNotInUnhashable() {
    // Note that [] can't be hashed
    Assert.fail("[] not in {123: 456}", "not hashable")
}

// #[test]
// fn test_not_hashable()
internal fun testNotHashable() {
    Assert.fail("""
x = {}
y = {}
x[y] = 1
""", "not hashable")
    Assert.fail("""
x = {'x': 1}
y = {}
x.get(y)
""", "not hashable")
    Assert.fail("""
x = {'x': 1}
y = {}
x[y]
""", "not hashable")
}
