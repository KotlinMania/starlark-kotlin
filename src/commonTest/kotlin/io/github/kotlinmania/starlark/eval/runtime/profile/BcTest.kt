// port-lint: tests src/eval/runtime/profile/bc.rs
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

import io.github.kotlinmania.starlark.environment.Globals
import io.github.kotlinmania.starlark.environment.Module
import io.github.kotlinmania.starlark.eval.bc.BcOpcode
import io.github.kotlinmania.starlark.eval.evalModule
import io.github.kotlinmania.starlark.eval.runtime.Evaluator
import io.github.kotlinmania.starlark.eval.runtime.profile.mode.ProfileMode
import io.github.kotlinmania.starlark.syntax.AstModule
import io.github.kotlinmania.starlark.syntax.dialect.Dialect
import kotlin.test.Test
import kotlin.test.assertEquals

internal class BcTest {
    @Test
    fun testSmoke() {
        Module.withTempHeap { module ->
            val globals = Globals.standard()
            val eval = Evaluator(module)
            eval.enableProfile(ProfileMode.Bytecode)
            eval.evalModule(
                AstModule.parse("bc.star", "repr([1, 2])", Dialect.Standard).getOrThrow(),
                globals,
            )
            val csv = eval.genBcProfile().genCsv()
            assertEquals(true, csv.contains("\"${BcOpcode.CallFrozenNativePos}\",1,"))
        }
    }

    @Test
    fun testSmoke2() {
        Module.withTempHeap { module ->
            val globals = Globals.standard()
            val eval = Evaluator(module)
            eval.enableProfile(ProfileMode.BytecodePairs)
            eval.evalModule(
                AstModule.parse("bc.star", "repr([1, 2])", Dialect.Standard).getOrThrow(),
                globals,
            )
            val csv = eval.genBcPairsProfile().genCsv()
            assertEquals(true, csv.contains("\"${BcOpcode.ListOfConsts}\",\"${BcOpcode.CallFrozenNativePos}\",1"))
        }
    }

    @Test
    fun testBcProfileDataMerge() {
        val bc = BcProfileData()
        BcProfileData.merge(listOf(bc, bc, bc))
    }

    @Test
    fun testBcPairsProfileDataMerge() {
        val bc = BcPairsProfileData()
        BcPairsProfileData.merge(listOf(bc, bc, bc))
    }
}
