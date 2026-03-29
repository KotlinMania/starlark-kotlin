// port-lint: source src/eval/runtime/profile/bc.rs
package io.github.kotlinmania.starlark_kotlin.eval.runtime.profile

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

//! Bytecode profiler.

import io.github.kotlinmania.starlark_kotlin.environment.Globals
import io.github.kotlinmania.starlark_kotlin.environment.Module
import io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.csv.CsvWriter
import io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.data.ProfileDataImpl
import io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.mode.ProfileMode
import io.github.kotlinmania.starlark_kotlin.eval.runtime.Evaluator
import io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.data.ProfileData
import io.github.kotlinmania.starlark_kotlin.eval.bc.BcOpcode
import io.github.kotlinmania.starlark_kotlin.syntax.dialect.Dialect
import io.github.kotlinmania.starlark_kotlin.eval.evalModule
import io.github.kotlinmania.starlark_kotlin.syntax.AstModule
import kotlin.math.roundToLong

// Format a Double to 3 decimal places (KMP-safe, no String.format)
private fun formatF3(value: Double): String {
    val rounded = (value * 1000.0).roundToLong()
    val intPart = rounded / 1000
    val fracPart = kotlin.math.abs(rounded % 1000)
    return if (value < 0) "-${kotlin.math.abs(intPart)}.${fracPart.toString().padStart(3, '0')}"
    else "$intPart.${fracPart.toString().padStart(3, '0')}"
}

// pub(crate) struct BcProfilerType
internal object BcProfilerType : ProfilerType<BcProfileData> {
    override val profileMode: ProfileMode = ProfileMode.Bytecode

    override fun dataFromGeneric(profileData: ProfileDataImpl): BcProfileData? =
        when (profileData) {
            is ProfileDataImpl.Bc -> profileData.data
            else -> null
        }

    override fun dataToGeneric(data: BcProfileData): ProfileDataImpl =
        ProfileDataImpl.Bc(data)

    override fun mergeProfilesImpl(profiles: List<BcProfileData>): Result<BcProfileData> =
        Result.success(BcProfileData.merge(profiles))
}

// pub(crate) struct BcPairsProfilerType
internal object BcPairsProfilerType : ProfilerType<BcPairsProfileData> {
    override val profileMode: ProfileMode = ProfileMode.BytecodePairs

    override fun dataFromGeneric(profileData: ProfileDataImpl): BcPairsProfileData? =
        when (profileData) {
            is ProfileDataImpl.BcPairs -> profileData.data
            else -> null
        }

    override fun dataToGeneric(data: BcPairsProfileData): ProfileDataImpl =
        ProfileDataImpl.BcPairs(data)

    override fun mergeProfilesImpl(profiles: List<BcPairsProfileData>): Result<BcPairsProfileData> =
        Result.success(BcPairsProfileData.merge(profiles))
}

// #[derive(Debug, thiserror::Error)]
// enum BcProfileError
internal sealed class BcProfileError : Exception() {
    // #[error("Can't call `write_bc_profile` unless you first call `enable_bc_profile`.")]
    data object BcProfilingNotEnabled : BcProfileError() {
        override val message: String get() =
            "Can't call `write_bc_profile` unless you first call `enable_bc_profile`."
    }
}

// #[derive(Default, Clone, Dupe, Copy, Debug)]
// struct BcInstrStat
internal data class BcInstrStat(
    var count: ULong = 0u,
) {
    operator fun plusAssign(other: BcInstrStat) {
        count += other.count
    }
}

// #[derive(Default, Clone, Copy, Dupe, Debug)]
// struct BcInstrPairsStat
internal data class BcInstrPairsStat(
    var count: ULong = 0u,
) {
    operator fun plusAssign(other: BcInstrPairsStat) {
        count += other.count
    }
}

// #[derive(Clone, Debug)]
// pub(crate) struct BcProfileData
internal class BcProfileData(
    val byInstr: Array<BcInstrStat> = Array(BcOpcode.COUNT) { BcInstrStat() },
) {
    // fn before_instr(&mut self, opcode: BcOpcode)
    fun beforeInstr(opcode: BcOpcode) {
        byInstr[opcode.ordinal].count++
    }

    // pub(crate) fn gen_csv(&self) -> String
    fun genCsv(): String {
        val sorted = byInstr.mapIndexed { i, st ->
            Pair(BcOpcode.byNumber(i.toUInt())!!, st)
        }.sortedByDescending { it.second.count }

        val total = BcInstrStat()
        for ((_, st) in sorted) {
            total.count += st.count
        }

        val csv = CsvWriter(listOf("Opcode", "Count", "Count / Total"))
        csv.writeDisplay("TOTAL")
        csv.writeValue(total.count)
        csv.writeDisplay(formatF3(1.0))
        csv.finishRow()

        for ((opcode, instrStats) in sorted) {
            csv.writeDebug(opcode)
            csv.writeValue(instrStats.count)
            csv.writeDisplay(formatF3(instrStats.count.toDouble() / total.count.toDouble()))
            csv.finishRow()
        }
        return csv.finish()
    }

    operator fun plusAssign(other: BcProfileData) {
        for (i in byInstr.indices) {
            byInstr[i] += other.byInstr[i]
        }
    }

    companion object {
        // fn merge(iter: impl IntoIterator<Item = &'a BcProfileData>) -> BcProfileData
        fun merge(iter: Iterable<BcProfileData>): BcProfileData {
            val sum = BcProfileData()
            for (profile in iter) {
                sum += profile
            }
            return sum
        }
    }
}

// #[derive(Default, Clone, Debug)]
// pub(crate) struct BcPairsProfileData
internal class BcPairsProfileData(
    var last: BcOpcode? = null,
    val byInstr: MutableMap<Pair<BcOpcode, BcOpcode>, BcInstrPairsStat> = mutableMapOf(),
) {
    // fn before_instr(&mut self, opcode: BcOpcode)
    fun beforeInstr(opcode: BcOpcode) {
        last?.let { lastOpcode ->
            val key = Pair(lastOpcode, opcode)
            val entry = byInstr.getOrPut(key) { BcInstrPairsStat() }
            entry.count++
        }
        last = opcode
    }

    // pub(crate) fn gen_csv(&self) -> String
    fun genCsv(): String {
        val sorted = byInstr.entries
            .map { (opcodes, stat) -> Pair(opcodes, stat) }
            .sortedWith(compareByDescending<Pair<Pair<BcOpcode, BcOpcode>, BcInstrPairsStat>> {
                it.second.count
            }.thenBy {
                it.first.first.ordinal
            }.thenBy {
                it.first.second.ordinal
            })

        val countTotal = sorted.sumOf { it.second.count }
        val csv = CsvWriter(listOf("Opcode[0]", "Opcode[1]", "Count", "Count / Total"))
        for ((opcodes, instrStats) in sorted) {
            val (o0, o1) = opcodes
            csv.writeDebug(o0)
            csv.writeDebug(o1)
            csv.writeValue(instrStats.count)
            csv.writeDisplay(formatF3(instrStats.count.toDouble() / countTotal.toDouble()))
            csv.finishRow()
        }
        return csv.finish()
    }

    operator fun plusAssign(other: BcPairsProfileData) {
        last = null
        for ((pair, stat) in other.byInstr) {
            val entry = byInstr.getOrPut(pair) { BcInstrPairsStat() }
            entry += stat
        }
    }

    companion object {
        // fn merge(iter: impl IntoIterator<Item = &'a BcPairsProfileData>) -> BcPairsProfileData
        fun merge(iter: Iterable<BcPairsProfileData>): BcPairsProfileData {
            val sum = BcPairsProfileData()
            for (profile in iter) {
                sum += profile
            }
            return sum
        }
    }
}

// enum BcProfileDataMode
internal sealed class BcProfileDataMode {
    data class Bc(val data: BcProfileData) : BcProfileDataMode()
    data class BcPairs(val data: BcPairsProfileData) : BcProfileDataMode()
    data object Disabled : BcProfileDataMode()
}

// pub(crate) struct BcProfile
internal class BcProfile(
    private var data: BcProfileDataMode = BcProfileDataMode.Disabled,
) {
    companion object {
        // pub(crate) fn new() -> BcProfile
        fun new(): BcProfile = BcProfile()
    }

    // pub(crate) fn enable_1(&mut self)
    fun enable1() {
        data = BcProfileDataMode.Bc(BcProfileData())
    }

    // pub(crate) fn enable_2(&mut self)
    fun enable2() {
        data = BcProfileDataMode.BcPairs(BcPairsProfileData())
    }

    // pub(crate) fn enabled(&self) -> bool
    fun enabled(): Boolean = when (data) {
        is BcProfileDataMode.Bc -> true
        is BcProfileDataMode.BcPairs -> true
        is BcProfileDataMode.Disabled -> false
    }

    // pub(crate) fn gen_bc_profile(&mut self) -> crate::Result<ProfileData>
    fun genBcProfile(): ProfileData {
        val prev = data
        data = BcProfileDataMode.Disabled
        return when (prev) {
            is BcProfileDataMode.Bc -> ProfileData(
                profile = ProfileDataImpl.Bc(prev.data),
            )
            else -> throw BcProfileError.BcProfilingNotEnabled
        }
    }

    // pub(crate) fn gen_bc_pairs_profile(&mut self) -> crate::Result<ProfileData>
    fun genBcPairsProfile(): ProfileData {
        val prev = data
        data = BcProfileDataMode.Disabled
        return when (prev) {
            is BcProfileDataMode.BcPairs -> ProfileData(
                profile = ProfileDataImpl.BcPairs(prev.data),
            )
            else -> throw BcProfileError.BcProfilingNotEnabled
        }
    }

    /// Called from bytecode.
    // pub(crate) fn before_instr(&mut self, opcode: BcOpcode)
    fun beforeInstr(opcode: BcOpcode) {
        when (val d = data) {
            is BcProfileDataMode.Bc -> d.data.beforeInstr(opcode)
            is BcProfileDataMode.BcPairs -> d.data.beforeInstr(opcode)
            is BcProfileDataMode.Disabled -> error("unreachable when bytecode profiling is not enabled")
        }
    }
}

// --- Tests ---

// #[test] fn test_smoke()
internal fun testSmoke() {
    Module.withTempHeap { module ->
        val globals = Globals.standard()
        val eval = Evaluator(module)
        eval.enableProfile(ProfileMode.Bytecode)
        eval.evalModule(
            AstModule.parse("bc.star", "repr([1, 2])", Dialect.Standard).getOrThrow(),
            globals,
        )
        val csv = eval.genBcProfile().genCsv()
        check(csv.contains("\"${BcOpcode.CallFrozenNativePos}\",1,"))
    }
}

// #[test] fn test_smoke_2()
internal fun testSmoke2() {
    Module.withTempHeap { module ->
        val globals = Globals.standard()
        val eval = Evaluator(module)
        eval.enableProfile(ProfileMode.BytecodePairs)
        eval.evalModule(
            AstModule.parse("bc.star", "repr([1, 2])", Dialect.Standard).getOrThrow(),
            globals,
        )
        val csv = eval.genBcPairsProfile().genCsv()
        check(csv.contains("\"${BcOpcode.ListOfConsts}\",\"${BcOpcode.CallFrozenNativePos}\",1"))
    }
}

// #[test] fn test_bc_profile_data_merge()
internal fun testBcProfileDataMerge() {
    val bc = BcProfileData()
    // Smoke test.
    BcProfileData.merge(listOf(bc, bc, bc))
}

// #[test] fn test_bc_pairs_profile_data_merge()
internal fun testBcPairsProfileDataMerge() {
    val bc = BcPairsProfileData()
    // Smoke test.
    BcPairsProfileData.merge(listOf(bc, bc, bc))
}
