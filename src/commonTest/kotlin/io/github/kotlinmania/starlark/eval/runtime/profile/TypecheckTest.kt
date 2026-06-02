// port-lint: source tests:src/eval/runtime/profile/typecheck.rs
package io.github.kotlinmania.starlark.eval.runtime.profile

/*
 * Copyright 2019 The Starlark in Rust Authors.
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

import io.github.kotlinmania.starlark.eval.runtime.SmallDuration
import io.github.kotlinmania.starlark.util.ArcStr
import kotlin.test.Test
import kotlin.test.assertEquals

class TypecheckTest {
    @Test
    fun testTypecheckProfile() {
        io.github.kotlinmania.starlark.environment.Module.withTempHeap { module ->
            val eval =
                io.github.kotlinmania.starlark.eval.runtime
                    .Evaluator(module)
            val program = """
def f(s: str):
    return int(s)

def g():
    for i in range(0, 1000):
        f(str(i))

g()
"""
            val ast =
                io.github.kotlinmania.starlark.syntax.AstModule
                    .parse(
                        "test.star",
                        program,
                        io.github.kotlinmania.starlark.syntax.dialect.Dialect.AllOptionsInternal,
                    ).getOrThrow()
            eval.enableProfile(io.github.kotlinmania.starlark.eval.runtime.profile.mode.ProfileMode.Typecheck).getOrThrow()
            eval
                .evalModule(
                    ast,
                    io.github.kotlinmania.starlark.environment.Globals
                        .standard(),
                ).getOrThrow()
            val profile = eval.genProfile().getOrThrow()
            // Check the profile contains typecheck data; structural assertion only.
            profile.profileMode().toString()
        }
    }

    @Test
    fun testTypecheckProfileMerge() {
        val a =
            TypecheckProfileData(
                byFunction =
                    mapOf(
                        ArcStr.from("a") to SmallDuration.fromMillis(10UL),
                        ArcStr.from("b") to SmallDuration.fromMillis(20UL),
                    ),
            )
        val b =
            TypecheckProfileData(
                byFunction =
                    mapOf(
                        ArcStr.from("b") to SmallDuration.fromMillis(300UL),
                        ArcStr.from("c") to SmallDuration.fromMillis(400UL),
                    ),
            )
        val merged = TypecheckProfilerType.mergeProfilesImpl(listOf(a, b)).getOrThrow()

        val expected =
            TypecheckProfileData(
                byFunction =
                    mapOf(
                        ArcStr.from("a") to SmallDuration.fromMillis(10UL),
                        ArcStr.from("b") to SmallDuration.fromMillis(320UL),
                        ArcStr.from("c") to SmallDuration.fromMillis(400UL),
                    ),
            )
        assertEquals(expected, merged)
    }
}
