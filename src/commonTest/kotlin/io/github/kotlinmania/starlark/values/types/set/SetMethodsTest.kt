// port-lint: source tests:src/values/types/set/methods.rspackage io.github.kotlinmania.starlark.values.types.set

/*
 * Copyright 2018 The Starlark in Rust Authors.
 * Copyright (c) Facebook, Inc. and its affiliates.
 * Copyright (c) 2025 Sydney Renee, The Solace Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not import this file except in compliance with the License.
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
import kotlin.test.Test

class SetMethodsTest {

    @Test
    fun testEmpty() {
        Assert.isTrue("s = set(); len(s) == 0")
    }

    @Test
    fun testSingle() {
        Assert.isTrue("s = set([0, 1]); len(s) == 2")
    }

    @Test
    fun testEq() {
        Assert.isTrue("set([1, 2, 3]) == set([3, 2, 1])")
    }

    @Test
    fun testClear() {
        Assert.isTrue("s = set([1, 2, 3]); s.clear(); s == set()")
    }

    @Test
    fun testType() {
        Assert.eq("type(set([1, 2, 3]))", "'set'")
    }

    @Test
    fun testIter() {
        Assert.isTrue("list([elem for elem in set([1, 2, 3])]) ==  [1, 2, 3]")
    }

    @Test
    fun testBoolTrue() {
        Assert.isTrue("bool(set([1, 2, 3]))")
    }

    @Test
    fun testBoolFalse() {
        Assert.isFalse("bool(set())")
    }

    @Test
    fun testUnion() {
        Assert.eq(
            "set([1, 2, 3]).union(set([3, 4, 5]))",
            "set([1, 2, 3, 4, 5])",
        )
    }

    @Test
    fun testUnionEmpty() {
        Assert.eq("set([1, 2, 3]).union(set([]))", "set([1, 2, 3])")
    }

    @Test
    fun testUnionIter() {
        Assert.eq("set([1, 2, 3]).union([3, 4])", "set([1, 2, 3, 4])")
    }

    @Test
    fun testUnionOrderingMixed() {
        Assert.eq("list(set([1, 3, 5]).union(set([4, 3])))", "[1, 3, 5, 4]")
    }

    @Test
    fun testIntersection() {
        Assert.eq("set([1, 2, 3]).intersection(set([3, 4, 5]))", "set([3])")
    }

    @Test
    fun testIntersectionEmpty() {
        Assert.eq("set([1, 2, 3]).intersection(set([]))", "set([])")
    }

    @Test
    fun testIntersectionIter() {
        Assert.eq("set([1, 2, 3]).intersection([3, 4])", "set([3])")
    }

    @Test
    fun testIntersectionOrder() {
        Assert.eq("list(set([1, 2, 3]).intersection([4, 3, 1]))", "[1, 3]")
    }

    @Test
    fun testSymmetricDifference() {
        Assert.eq(
            "set([1, 2, 3]).symmetric_difference(set([3, 4, 5]))",
            "set([1, 2, 4, 5])",
        )
    }

    @Test
    fun testSymmetricDifferenceEmpty() {
        Assert.eq(
            "set([1, 2, 3]).symmetric_difference(set([]))",
            "set([1, 2, 3])",
        )
    }

    @Test
    fun testSymmetricDifferenceIter() {
        Assert.eq(
            "set([1, 2, 3]).symmetric_difference([3, 4])",
            "set([1, 2, 4])",
        )
    }

    @Test
    fun testSymmetricDifferenceOrd() {
        Assert.eq(
            "list(set([1, 2, 3, 7]).symmetric_difference(set([4, 3, 1])))",
            "[2, 7, 4]",
        )
    }

    @Test
    fun testAdd() {
        Assert.eq("x = set([1, 2, 3]);x.add(0);x", "set([0, 1, 2, 3])")
    }

    @Test
    fun testAddEmpty() {
        Assert.eq("x = set([]);x.add(0);x", "set([0])")
    }

    @Test
    fun testAddExisting() {
        Assert.eq("x = set([0]);x.add(0);x", "set([0])")
    }

    @Test
    fun testAddOrder() {
        Assert.eq("x = set([1, 2, 3]);x.add(2);list(x)", "[1, 2, 3]")
        Assert.eq("x = set([1, 2, 3]);x.add(0);list(x)", "[1, 2, 3, 0]")
    }

    @Test
    fun testRemove() {
        Assert.eq("x = set([0, 1]);x.remove(1);x", "set([0])")
    }

    @Test
    fun testRemoveEmpty() {
        Assert.fail("set([]).remove(0)", "`0` not found in `set([])`")
    }

    @Test
    fun testRemoveNotExisting() {
        Assert.fail("set([1]).remove(0)", "`0` not found in `set([1])`")
    }

    @Test
    fun testDiscard() {
        Assert.eq("x = set([0, 1]);x.discard(1);x", "set([0])")
    }

    @Test
    fun testDiscardMultipleTimes() {
        Assert.eq("x = set([0, 1]); x.discard(0); x.discard(0); x", "set([1])")
    }

    @Test
    fun testPop() {
        Assert.isTrue("x = set([1, 0]); (x.pop() == 0 and x.pop() == 1 and x == set())")
    }

    @Test
    fun testPopEmpty() {
        Assert.fail("x = set([]); x.pop()", "pop from an empty set")
    }

    @Test
    fun testDifference() {
        Assert.eq("set([1, 2, 3]).difference(set([2]))", "set([1, 3])")
    }

    @Test
    fun testDifferenceIter() {
        Assert.eq("set([1, 2, 3]).difference([3, 2])", "set([1])")
    }

    @Test
    fun testDifferenceOrder() {
        Assert.eq("list(set([3, 2, 1]).difference([2]))", "[3, 1]")
    }

    @Test
    fun testDifferenceEmptyLhs() {
        Assert.eq("set([]).difference(set([2]))", "set([])")
    }

    @Test
    fun testDifferenceEmptyRhs() {
        Assert.eq("set([1, 2]).difference(set([]))", "set([2, 1])")
    }

    @Test
    fun testIsSuperset() {
        Assert.isTrue("set([1, 2, 3, 4]).issuperset(set([1, 3, 2]))")
    }

    @Test
    fun testIsNotSuperset() {
        Assert.isFalse("set([1, 2]).issuperset(set([1, 3, 5]))")
    }

    @Test
    fun testIsNotSupersetEmptyLhs() {
        Assert.isFalse("set([]).issuperset(set([1]))")
    }

    @Test
    fun testIsSupersetEmptyRhs() {
        Assert.isTrue("set([1, 2]).issuperset(set([]))")
        Assert.isTrue("set([]).issuperset(set([]))")
    }

    @Test
    fun testIsSupersetIter() {
        Assert.isTrue("set([1, 2, 3]).issuperset([3, 1])")
    }

    @Test
    fun testIsSubset() {
        Assert.isTrue("set([1, 2]).issubset(set([1, 3, 2]))")
    }

    @Test
    fun testIsNotSubset() {
        Assert.isFalse("set([1, 2]).issubset(set([1, 3, 5]))")
    }

    @Test
    fun testIsSubsetEmptyLhs() {
        Assert.isTrue("set([]).issubset(set([1, 3, 5]))")
        Assert.isTrue("set([]).issubset(set([]))")
    }

    @Test
    fun testIsNotSubsetEmptyRhs() {
        Assert.isFalse("set([1, 2]).issubset(set([]))")
    }

    @Test
    fun testIsSubsetIter() {
        Assert.isTrue("set([1, 2]).issubset([1, 3, 2])")
    }

    @Test
    fun testUpdate() {
        Assert.eq(
            "x = set([1, 3, 2]); x.update([4, 3]); list(x)",
            "[1, 3, 2, 4]",
        )
    }

    @Test
    fun testUpdateEmpty() {
        Assert.eq("x = set([1, 3, 2]); x.update([]); list(x)", "[1, 3, 2]")
        Assert.eq("x = set(); x.update(set([4, 3])); list(x)", "[4, 3]")
        Assert.eq("x = set(); x.update([]); x", "set()")
    }

    @Test
    fun testUpdateSelf() {
        Assert.eq("x = set([1, 3, 2]); x.update(x); list(x)", "[1, 3, 2]")
    }

    @Test
    fun testUpdateFrozenSetCannotBeUpdatedWithSelf() {
        val a = io.github.kotlinmania.starlark.assert.Assert()
        a.module("s.star", "S = set([1, 2, 3])")
        a.fail(
            """
            load('s.star', 'S')

            S.update(S)
            """.trimIndent(),
            "Immutable",
        )
    }
}
