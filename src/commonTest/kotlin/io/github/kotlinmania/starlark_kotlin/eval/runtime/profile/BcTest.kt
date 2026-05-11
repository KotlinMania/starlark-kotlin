<<<<<<< HEAD:src/commonTest/kotlin/io/github/kotlinmania/starlark/eval/runtime/profile/BcTest.kt
// port-lint: source tests:src/eval/runtime/profile/bc.rs
package io.github.kotlinmania.starlark.eval.runtime.profile
=======
// port-lint: tests src/eval/runtime/profile/bc.rs
package io.github.kotlinmania.starlark_kotlin.eval.runtime.profile
>>>>>>> origin/main:src/commonTest/kotlin/io/github/kotlinmania/starlark_kotlin/eval/runtime/profile/BcTest.kt

/*
 * Copyright 2019 The Starlark in Rust Authors.
 * Copyright (c) Facebook, Inc. and its affiliates.
 * Copyright (c) 2025 Sydney Renee, The Solace Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
<<<<<<< HEAD:src/commonTest/kotlin/io/github/kotlinmania/starlark/eval/runtime/profile/BcTest.kt
 * you may not import this file except in compliance with the License.
=======
 * you may not use this file except in compliance with the License.
>>>>>>> origin/main:src/commonTest/kotlin/io/github/kotlinmania/starlark_kotlin/eval/runtime/profile/BcTest.kt
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

<<<<<<< HEAD:src/commonTest/kotlin/io/github/kotlinmania/starlark/eval/runtime/profile/BcTest.kt
import kotlin.test.Test

class BcTest {
=======
import io.github.kotlinmania.starlark_kotlin.environment.Globals
import io.github.kotlinmania.starlark_kotlin.environment.Module
import io.github.kotlinmania.starlark_kotlin.eval.runtime.Evaluator
import io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.mode.ProfileMode
import io.github.kotlinmania.starlark_kotlin.eval.evalModule
import io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.data.ProfileData
import io.github.kotlinmania.starlark_kotlin.eval.bc.BcOpcode
import io.github.kotlinmania.starlark_kotlin.syntax.AstModule
import io.github.kotlinmania.starlark_kotlin.syntax.dialect.Dialect
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
>>>>>>> origin/main:src/commonTest/kotlin/io/github/kotlinmania/starlark_kotlin/eval/runtime/profile/BcTest.kt

    @Test
    fun testBcProfileDataMerge() {
        val bc = BcProfileData()
<<<<<<< HEAD:src/commonTest/kotlin/io/github/kotlinmania/starlark/eval/runtime/profile/BcTest.kt
        // Smoke test.
=======
>>>>>>> origin/main:src/commonTest/kotlin/io/github/kotlinmania/starlark_kotlin/eval/runtime/profile/BcTest.kt
        BcProfileData.merge(listOf(bc, bc, bc))
    }

    @Test
    fun testBcPairsProfileDataMerge() {
        val bc = BcPairsProfileData()
<<<<<<< HEAD:src/commonTest/kotlin/io/github/kotlinmania/starlark/eval/runtime/profile/BcTest.kt
        // Smoke test.
=======
>>>>>>> origin/main:src/commonTest/kotlin/io/github/kotlinmania/starlark_kotlin/eval/runtime/profile/BcTest.kt
        BcPairsProfileData.merge(listOf(bc, bc, bc))
    }
}
