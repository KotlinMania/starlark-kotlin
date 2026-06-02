// port-lint: tests src/eval/runtime/profile/time_flame.rs
package io.github.kotlinmania.starlark.eval.runtime.profile

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
import io.github.kotlinmania.starlark.environment.Globals
import io.github.kotlinmania.starlark.environment.GlobalsBuilder
import io.github.kotlinmania.starlark.environment.Module
import io.github.kotlinmania.starlark.eval.evalModule
import io.github.kotlinmania.starlark.eval.runtime.Evaluator
import io.github.kotlinmania.starlark.eval.runtime.fileloader.ReturnOwnedFileLoader
import io.github.kotlinmania.starlark.eval.runtime.profile.mode.ProfileMode
import io.github.kotlinmania.starlark.syntax.AstModule
import io.github.kotlinmania.starlark.syntax.dialect.Dialect
import io.github.kotlinmania.starlark.values.types.none.NoneType
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

class TimeFlameTest {
    @Test
    fun testTimeFlameWorksInsideFrozenModule() {
        fun registerSleep(globals: GlobalsBuilder) {
            fun sleep(): Result<NoneType> {
                // The Rust upstream uses `thread::sleep(2ms)`. The Kotlin runtime
                // proxy is a brief busy-wait loop using TimeSource so the time
                // flame profile records a non-zero duration for the call.
                val start =
                    kotlin.time.TimeSource.Monotonic
                        .markNow()
                while (start.elapsedNow() < 2.milliseconds) {
                    // spin
                }
                return Result.success(NoneType)
            }
            globals.setFunction("sleep") { _, _ -> sleep() }
        }

        val a = Assert()
        a.globalsAdd(::registerSleep)
        val aBzl =
            a.passModule(
                """
def foo():
    for i in range(5):
        # Must sleep otherwise time flame will round the duration to zero and erase it.
        sleep()
    """,
            )

        val modules = HashMap<String, io.github.kotlinmania.starlark.environment.FrozenModule>()
        modules["a.bzl"] = aBzl
        val loader = ReturnOwnedFileLoader(modules)

        Module.withTempHeap { module ->
            val eval = Evaluator(module)
            eval.enableProfile(ProfileMode.TimeFlame)
            eval.setLoader(loader)
            eval
                .evalModule(
                    AstModule
                        .parse(
                            "x.star",
                            """
load("a.bzl", "foo")

def bar():
    for i in range(10):
        foo()

bar()
""",
                            Dialect.Standard,
                        ).getOrThrow(),
                    Globals.standard(),
                ).getOrThrow()

            val profile = eval.genProfile().genFlameData()
            val theLine = profile.lines().find { it.contains("foo") }
            assertNotNull(theLine, "There must be a line with `foo` in the profile: $profile")
            assertTrue(
                theLine.contains("bar"),
                "Profile must contain a line `bar.*foo`: $profile",
            )
        }
    }
}
