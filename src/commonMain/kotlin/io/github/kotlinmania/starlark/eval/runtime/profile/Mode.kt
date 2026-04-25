// port-lint: source src/eval/runtime/profile/mode.rs
package io.github.kotlinmania.starlark.eval.runtime.profile.mode

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

/** How to profile starlark code. */
// #[derive(Debug, PartialEq, Eq, Hash, Clone, Dupe, Copy, Allocative)]
// #[non_exhaustive]
// pub enum ProfileMode
enum class ProfileMode {
    /**
     * The heap profile mode provides information about the time spent in each function and allocations
     * performed by each function.
     */
    HeapSummaryAllocated,
    /** Like heap summary, but information about retained memory after module is frozen. */
    HeapSummaryRetained,
    /** Like heap profile, but writes output comparable with flamegraph.pl. */
    HeapFlameAllocated,
    /** Like heap flame, but information about retained memory after module is frozen. */
    HeapFlameRetained,
    /** HeapSummaryAllocated+HeapFlameAllocated */
    HeapAllocated,
    /** HeapSummaryRetained+HeapFlameRetained */
    HeapRetained,
    /** The statement profile mode provides information about time spent in each statement. */
    Statement,
    /** Code coverage. */
    Coverage,
    /** The bytecode profile mode provides information about bytecode instructions. */
    Bytecode,
    /** The bytecode profile mode provides information about bytecode instruction pairs. */
    BytecodePairs,
    /** Provide output compatible with flamegraph.pl. */
    TimeFlame,
    /** Profile runtime typechecking. */
    Typecheck,
    /** Don't record any profile information. */
    None;

    companion object {
        // pub(crate) const ALL: [ProfileMode; 13]
        val ALL: List<ProfileMode> = entries

        // impl FromStr for ProfileMode
        fun fromString(s: String): ProfileMode {
            for (mode in ALL) {
                if (s == mode.name()) return mode
            }
            throw IllegalArgumentException("Invalid ProfileMode: `$s`")
        }
    }

    /** Name of this profile mode. */
    // pub(crate) fn name(&self) -> &str
    fun name(): String = when (this) {
        HeapSummaryAllocated -> "heap-summary-allocated"
        HeapSummaryRetained -> "heap-summary-retained"
        HeapFlameAllocated -> "heap-flame-allocated"
        HeapFlameRetained -> "heap-flame-retained"
        HeapAllocated -> "heap-allocated"
        HeapRetained -> "heap-retained"
        Statement -> "statement"
        Coverage -> "coverage"
        Bytecode -> "bytecode"
        BytecodePairs -> "bytecode-pairs"
        TimeFlame -> "time-flame"
        Typecheck -> "typecheck"
        None -> "none"
    }

    /** Profile data for this mode can be obtained from FrozenModule.heapProfile. */
    // pub fn requires_frozen_module(&self) -> bool
    fun requiresFrozenModule(): Boolean = when (this) {
        HeapSummaryRetained, HeapFlameRetained, HeapRetained -> true
        else -> false
    }

    override fun toString(): String = name()
}
