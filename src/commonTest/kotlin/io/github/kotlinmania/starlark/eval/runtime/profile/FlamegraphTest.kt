// port-lint: tests src/eval/runtime/profile/flamegraph.rs
package io.github.kotlinmania.starlark.eval.runtime.profile

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

import io.github.kotlinmania.starlark.eval.runtime.profile.flamegraph.FlameGraphData
import io.github.kotlinmania.starlark.eval.runtime.profile.flamegraph.FlameGraphWriter
import io.github.kotlinmania.starlark.util.ArcStr
import kotlin.test.Test
import kotlin.test.assertEquals

internal class FlamegraphTest {
    @Test
    fun testFlamegraphWriter() {
        val writer = FlameGraphWriter()
        writer.write(listOf("aa", "bb"), 20u)
        assertEquals("aa;bb 20\n", writer.finish())
    }

    @Test
    fun testFlamegraphData() {
        val data = FlameGraphData()
        data.root().child(ArcStr.from("a")).add(10u)
        data
            .root()
            .child(ArcStr.from("a"))
            .child(ArcStr.from("b"))
            .add(20u)
        data.root().child(ArcStr.from("a")).add(30u)
        val result = data.write()
        assertEquals("a 40\na;b 20\n", result)
    }

    @Test
    fun testMerge() {
        val a = FlameGraphData()
        a.root().add(10u)
        a.root().child(ArcStr.from("a")).add(100u)
        a
            .root()
            .child(ArcStr.from("b"))
            .child(ArcStr.from("c"))
            .add(1000u)
        val b = FlameGraphData()
        b.root().add(20u)
        b.root().child(ArcStr.from("a")).add(200u)

        val c = FlameGraphData.merge(listOf(a, b))

        val expected = FlameGraphData()
        expected.root().add(30u)
        expected.root().child(ArcStr.from("a")).add(300u)
        expected
            .root()
            .child(ArcStr.from("b"))
            .child(ArcStr.from("c"))
            .add(1000u)

        assertEquals(expected, c)
    }
}
