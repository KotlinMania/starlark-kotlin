// port-lint: tests src/eval/runtime/profile/tests.rs
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

import io.github.kotlinmania.starlark.assert.testFunctions
import io.github.kotlinmania.starlark.environment.GlobalsBuilder
import io.github.kotlinmania.starlark.environment.Module
import io.github.kotlinmania.starlark.eval.evalModule
import io.github.kotlinmania.starlark.eval.runtime.Evaluator
import io.github.kotlinmania.starlark.eval.runtime.profile.data.ProfileData
import io.github.kotlinmania.starlark.eval.runtime.profile.data.ProfileDataImpl
import io.github.kotlinmania.starlark.eval.runtime.profile.mode.ProfileMode
import io.github.kotlinmania.starlark.goldentesttemplate.goldenTestTemplate
import io.github.kotlinmania.starlark.syntax.AstModule
import io.github.kotlinmania.starlark.syntax.dialect.Dialect

private fun testProfileGoldenForMode(mode: ProfileMode) {
    Module.withTempHeap { module ->
        val eval = Evaluator(module)
        eval.enableProfile(mode)
        eval.evalModule(
            AstModule
                .parse(
                    "test.star",
                    """
def inner(x: int):
    if noop():
        return 10
    else:
        for x in range(10):
            noop()

def test():
    r = []
    for x in noop([1, 2, 3, 4, 5]):
        inner(x)
        r += noop([1] * 3)
    return r

test()
test()
test()

R = test()
""",
                    Dialect.AllOptionsInternal,
                ).getOrThrow(),
            GlobalsBuilder.extended().with(::testFunctions).build(),
        )

        var profileData =
            when (mode) {
                ProfileMode.HeapSummaryRetained, ProfileMode.HeapFlameRetained -> {
                    // Drop eval, freeze module
                    val frozen = module.freeze().getOrThrow()
                    frozen.heapProfile().getOrThrow()
                }
                else -> eval.genProfile()
            }

        val profile = profileData.profile
        when (profile) {
            is ProfileDataImpl.HeapRetained -> profile.data.normalizeForGoldenTests()
            is ProfileDataImpl.HeapAllocated -> profile.data.normalizeForGoldenTests()
            is ProfileDataImpl.HeapFlameRetained -> profile.data.normalizeForGoldenTests()
            is ProfileDataImpl.HeapFlameAllocated -> profile.data.normalizeForGoldenTests()
            is ProfileDataImpl.HeapSummaryRetained -> profile.data.normalizeForGoldenTests()
            is ProfileDataImpl.HeapSummaryAllocated -> profile.data.normalizeForGoldenTests()
            else -> {}
        }

        when (mode) {
            ProfileMode.HeapRetained,
            ProfileMode.HeapFlameRetained,
            ProfileMode.HeapAllocated,
            ProfileMode.HeapFlameAllocated,
            ProfileMode.TimeFlame,
            -> {
                goldenTestTemplate(
                    "src/eval/runtime/profile/golden/${mode.modeName().replace('-', '_')}.flame.golden",
                    profileData.genFlameData(),
                )
            }
            else -> {}
        }

        when (mode) {
            ProfileMode.HeapFlameRetained,
            ProfileMode.HeapFlameAllocated,
            ProfileMode.TimeFlame,
            -> {}
            else -> {
                goldenTestTemplate(
                    "src/eval/runtime/profile/golden/${mode.modeName().replace('-', '_')}.csv.golden",
                    profileData.genCsv(),
                )
            }
        }
        // Smoke test for profile merging.
        ProfileData.merge(listOf(profileData, profileData))
    }
}

internal fun testProfileGoldenHeapAllocated() {
    testProfileGoldenForMode(ProfileMode.HeapAllocated)
}

internal fun testProfileGoldenHeapRetained() {
    testProfileGoldenForMode(ProfileMode.HeapRetained)
}

internal fun testProfileGoldenHeapSummaryAllocated() {
    testProfileGoldenForMode(ProfileMode.HeapSummaryAllocated)
}

internal fun testProfileGoldenHeapSummaryRetained() {
    testProfileGoldenForMode(ProfileMode.HeapSummaryRetained)
}

internal fun testProfileGoldenHeapFlameAllocated() {
    testProfileGoldenForMode(ProfileMode.HeapFlameAllocated)
}

internal fun testProfileGoldenHeapFlameRetained() {
    testProfileGoldenForMode(ProfileMode.HeapFlameRetained)
}

internal fun testProfileGoldenStatement() {
    testProfileGoldenForMode(ProfileMode.Statement)
}

internal fun testProfileGoldenCoverage() {
    testProfileGoldenForMode(ProfileMode.Coverage)
}

internal fun testProfileGoldenBytecode() {
    testProfileGoldenForMode(ProfileMode.Bytecode)
}

internal fun testProfileGoldenBytecodePairs() {
    testProfileGoldenForMode(ProfileMode.BytecodePairs)
}

internal fun testProfileGoldenTimeFlame() {
    testProfileGoldenForMode(ProfileMode.TimeFlame)
}

internal fun testProfileGoldenTypecheck() {
    testProfileGoldenForMode(ProfileMode.Typecheck)
}
