// port-lint: source src/values/layout/heap/profile/summary_by_function.rs
package io.github.kotlinmania.starlark_kotlin.values.layout.heap.profile

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

import io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.csv.CsvWriter
import io.github.kotlinmania.starlark_kotlin.eval.runtime.SmallDuration
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.profile.alloc_counts.AllocCounts
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.profile.string_index.StringId
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.profile.string_index.StringIndex

/// Information relating to a function.
// #[derive(Default, Debug, Clone)]
// pub(crate) struct FuncInfo
internal data class FuncInfo(
    /// Number of times this function was called
    var calls: Int = 0,
    /// Who called this function (and how many times each)
    val callers: MutableMap<String, Int> = mutableMapOf(),
    /// Time spent directly in this function
    var time: SmallDuration = SmallDuration(),
    /// Time spent directly in this function and recursive functions.
    var timeRec: SmallDuration = SmallDuration(),
    /// Allocations made by this function
    val allocations: MutableMap<String, AllocCounts> = mutableMapOf(),
) {
    companion object {
        // pub(crate) fn merge<'a>(xs: impl Iterator<Item = &'a Self>) -> Self
        fun merge(xs: Iterable<FuncInfo>): FuncInfo {
            val result = FuncInfo()
            for (x in xs) {
                result.calls += x.calls
                result.time += x.time
                for ((k, v) in x.allocations) {
                    val entry = result.allocations.getOrPut(k) { AllocCounts() }
                    entry += v
                }
            }
            // Recursive time doesn't accumulate nicely, the time is the right value
            result.timeRec = result.time
            return result
        }
    }

    /// Total number of allocations made by this function.
    // fn alloc_count(&self) -> usize
    fun allocCount(): Int = allocations.values.sumOf { it.count }

    /// Total number of bytes allocated by this function.
    // fn alloc_bytes(&self) -> usize
    fun allocBytes(): Int = allocations.values.sumOf { it.bytes }
}

/// We morally have two pieces of information:
/// 1. Information about each function.
/// 2. The call stack.
///
/// However, we are always updating the top of the call stack,
/// so pull out top_stack/top_info as a cache.
// pub(crate) struct HeapSummaryByFunction
class HeapSummaryByFunction(
    /// Information about all functions.
    private val info: MutableMap<String, FuncInfo> = mutableMapOf(),
) {
    companion object {
        // pub(crate) fn init(stacks: &AggregateHeapProfileInfo) -> HeapSummaryByFunction
        fun init(stacks: AggregateHeapProfileInfo): HeapSummaryByFunction {
            val summary = HeapSummaryByFunction()
            summary.initChildren(stacks.root, "(root)", stacks.strings)
            return summary
        }
    }

    // fn init_children(...)
    private fun initChildren(
        frame: StackFrame,
        name: String,
        strings: StringIndex,
    ): SmallDuration {
        var timeRec = SmallDuration()
        for ((func, child) in frame.callees) {
            timeRec += initChild(func, child, name, strings)
        }
        return timeRec
    }

    // fn init_child(...)
    private fun initChild(
        func: StringId,
        frame: StackFrame,
        caller: String,
        strings: StringIndex,
    ): SmallDuration {
        val funcStr = strings.get(func)
        info.getOrPut(funcStr) { FuncInfo() }.time += frame.timeX2
        info.getOrPut(funcStr) { FuncInfo() }.calls += frame.callsX2
        info.getOrPut(funcStr) { FuncInfo() }.callers.merge(caller, 1) { a, b -> a + b }
        for ((t, allocs) in frame.allocs.summary) {
            val entry = info.getOrPut(funcStr) { FuncInfo() }.allocations.getOrPut(t) { AllocCounts() }
            entry += allocs
        }

        val timeRec = frame.timeX2 + initChildren(frame, funcStr, strings)
        info.getOrPut(funcStr) { FuncInfo() }.timeRec += timeRec
        return timeRec
    }

    // fn totals(&self) -> FuncInfo
    private fun totals(): FuncInfo = FuncInfo.merge(info.values)

    // pub(crate) fn info(&self) -> Vec<(&ArcStr, &FuncInfo)>
    fun info(): List<Pair<String, FuncInfo>> = info.entries.map { (k, v) -> k to v }

    // pub(crate) fn gen_csv(&self) -> String
    fun genCsv(): String {
        // Add a totals column
        val totals = totals()
        val columns: MutableList<Pair<String, AllocCounts>> =
            totals.allocations.entries.map { (k, v) -> k to v }.toMutableList()

        columns.sortByDescending { it.second.count }

        val infoList = info().toMutableList()
        infoList.sortByDescending { it.second.time.nanos }

        val totalsStr = "TOTALS"
        val rows = listOf(Triple(totalsStr, totals, true)) +
            infoList.map { (k, v) -> Triple(k, v, false) }

        val csv = CsvWriter(
            listOf(
                "Function",
                "Time(s)",
                "TimeRec(s)",
                "Calls",
                "Callers",
                "TopCaller",
                "TopCallerCount",
                "Allocs",
                "AllocBytes",
            ) + columns.map { it.first },
        )
        for ((rowname, rowInfo, _) in rows) {
            val blank = ""
            val callers = rowInfo.callers.maxByOrNull { it.value }
                ?.let { it.key to it.value } ?: (blank to 0)
            check(rowInfo.calls % 2 == 0) {
                "we enter calls twice, for drop and non_drop"
            }
            // We divide calls and time by two
            // because we count calls twice: for drop and non-drop bumps.
            csv.writeValue(rowname)
            csv.writeValue(rowInfo.time / 2)
            csv.writeValue(rowInfo.timeRec / 2)
            csv.writeValue(rowInfo.calls / 2)
            csv.writeValue(rowInfo.callers.size)
            csv.writeValue(callers.first)
            csv.writeValue(callers.second)
            csv.writeValue(rowInfo.allocCount())
            csv.writeValue(rowInfo.allocBytes())
            for (c in columns) {
                csv.writeValue(rowInfo.alloc[c.first]?.count ?: 0)
            }
            csv.finishRow()
        }
        return csv.finish()
    }
}

// #[cfg(test)]
// mod tests

// Test data is collected from both drop and non-drop heaps.
// #[test]
// fn drop_non_drop()
internal fun dropNonDrop() {
    val ast = io.github.kotlinmania.starlark_kotlin.syntax.AstModule.parse(
        "x.star",
        """
_ignore = {1: 2}       # allocate a dict in drop
_ignore = str([1])     # allocate a string in non_drop
        """.trimIndent(),
        io.github.kotlinmania.starlark_kotlin.syntax.Dialect.AllOptionsInternal,
    ).getOrThrow()

    val globals = io.github.kotlinmania.starlark_kotlin.environment.Globals.standard()
    io.github.kotlinmania.starlark_kotlin.environment.Module.withTempHeap { module ->
        val eval = io.github.kotlinmania.starlark_kotlin.eval.Evaluator(module)
        eval.enableProfile(io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.mode.ProfileMode.HeapSummaryAllocated)
            .getOrThrow()

        eval.evalModule(ast, globals).getOrThrow()

        val stacks = AggregateHeapProfileInfo.collect(eval.heap(), null)

        val info = HeapSummaryByFunction.init(stacks)

        // Run the assertions.
        info.genCsv()

        val total = FuncInfo.merge(info.info().map { it.second })
        // from non-drop heap
        check(total.allocations["string"]!!.count == 1)
        // from drop heap
        check(total.allocations["dict"]!!.count == 1)
        Result.success(Unit)
    }.getOrThrow()
}
