<<<<<<< HEAD:src/commonTest/kotlin/io/github/kotlinmania/starlark/eval/runtime/profile/FlamegraphTest.kt
// port-lint: source tests:src/eval/runtime/profile/flamegraph.rs
package io.github.kotlinmania.starlark.eval.runtime.profile
=======
// port-lint: tests src/eval/runtime/profile/flamegraph.rs
package io.github.kotlinmania.starlark_kotlin.eval.runtime.profile
>>>>>>> origin/main:src/commonTest/kotlin/io/github/kotlinmania/starlark_kotlin/eval/runtime/profile/FlamegraphTest.kt

/*
 * Copyright 2019 The Starlark in Rust Authors.
 * Copyright (c) Facebook, Inc. and its affiliates.
 * Copyright (c) 2025 Sydney Renee, The Solace Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
<<<<<<< HEAD:src/commonTest/kotlin/io/github/kotlinmania/starlark/eval/runtime/profile/FlamegraphTest.kt
 * you may not import this file except in compliance with the License.
=======
 * you may not use this file except in compliance with the License.
>>>>>>> origin/main:src/commonTest/kotlin/io/github/kotlinmania/starlark_kotlin/eval/runtime/profile/FlamegraphTest.kt
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

<<<<<<< HEAD:src/commonTest/kotlin/io/github/kotlinmania/starlark/eval/runtime/profile/FlamegraphTest.kt
import io.github.kotlinmania.starlark.util.ArcStr
import kotlin.test.Test
import kotlin.test.assertEquals

class FlamegraphTest {

    @Test
    fun testFlamegraphWriter() {
        val writer = FlameGraphWriter()
        writer.write(listOf("aa", "bb"), 20UL)
=======
import io.github.kotlinmania.starlark_kotlin.util.ArcStr
import io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.flamegraph.FlameGraphData
import io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.flamegraph.FlameGraphWriter
import kotlin.test.Test
import kotlin.test.assertEquals

internal class FlamegraphTest {
    @Test
    fun testFlamegraphWriter() {
        val writer = FlameGraphWriter()
        writer.write(listOf("aa", "bb"), 20u)
>>>>>>> origin/main:src/commonTest/kotlin/io/github/kotlinmania/starlark_kotlin/eval/runtime/profile/FlamegraphTest.kt
        assertEquals("aa;bb 20\n", writer.finish())
    }

    @Test
    fun testFlamegraphData() {
        val data = FlameGraphData()
<<<<<<< HEAD:src/commonTest/kotlin/io/github/kotlinmania/starlark/eval/runtime/profile/FlamegraphTest.kt
        data.root().child(ArcStr.from("a")).add(10UL)
        data.root().child(ArcStr.from("a")).child(ArcStr.from("b")).add(20UL)
        data.root().child(ArcStr.from("a")).add(30UL)
        val out = data.write()
        assertEquals("a 40\na;b 20\n", out)
=======
        data.root().child(ArcStr.from("a")).add(10u)
        data.root().child(ArcStr.from("a")).child(ArcStr.from("b")).add(20u)
        data.root().child(ArcStr.from("a")).add(30u)
        val result = data.write()
        assertEquals("a 40\na;b 20\n", result)
>>>>>>> origin/main:src/commonTest/kotlin/io/github/kotlinmania/starlark_kotlin/eval/runtime/profile/FlamegraphTest.kt
    }

    @Test
    fun testMerge() {
        val a = FlameGraphData()
<<<<<<< HEAD:src/commonTest/kotlin/io/github/kotlinmania/starlark/eval/runtime/profile/FlamegraphTest.kt
        a.root().add(10UL)
        a.root().child(ArcStr.from("a")).add(100UL)
        a.root().child(ArcStr.from("b")).child(ArcStr.from("c")).add(1000UL)
        val b = FlameGraphData()
        b.root().add(20UL)
        b.root().child(ArcStr.from("a")).add(200UL)
=======
        a.root().add(10u)
        a.root().child(ArcStr.from("a")).add(100u)
        a.root().child(ArcStr.from("b")).child(ArcStr.from("c")).add(1000u)
        val b = FlameGraphData()
        b.root().add(20u)
        b.root().child(ArcStr.from("a")).add(200u)
>>>>>>> origin/main:src/commonTest/kotlin/io/github/kotlinmania/starlark_kotlin/eval/runtime/profile/FlamegraphTest.kt

        val c = FlameGraphData.merge(listOf(a, b))

        val expected = FlameGraphData()
<<<<<<< HEAD:src/commonTest/kotlin/io/github/kotlinmania/starlark/eval/runtime/profile/FlamegraphTest.kt
        expected.root().add(30UL)
        expected.root().child(ArcStr.from("a")).add(300UL)
        expected.root().child(ArcStr.from("b")).child(ArcStr.from("c")).add(1000UL)
=======
        expected.root().add(30u)
        expected.root().child(ArcStr.from("a")).add(300u)
        expected.root().child(ArcStr.from("b")).child(ArcStr.from("c")).add(1000u)
>>>>>>> origin/main:src/commonTest/kotlin/io/github/kotlinmania/starlark_kotlin/eval/runtime/profile/FlamegraphTest.kt

        assertEquals(expected, c)
    }
}
