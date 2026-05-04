// port-lint: source values/layout/heap/profile/allocCounts.rs
package io.github.kotlinmania.starlark.values.layout.heap.profile.alloccounts

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

/** Allocations counters. */
data class AllocCounts(
    var bytes: Long = 0,
    var count: Int = 0,
) {

    internal fun normalizeForGoldenTests() {
        // Value sizes depend on compiler version, so normalize them.
        bytes = count.toLong() * 8
    }

    operator fun plusAssign(other: AllocCounts) {
        bytes += other.bytes
        count += other.count
    }

    operator fun plus(other: AllocCounts): AllocCounts {
        return AllocCounts(
            bytes = bytes + other.bytes,
            count = count + other.count,
        )
    }

    companion object {
        fun default(): AllocCounts = AllocCounts()
    }
}

fun Iterable<AllocCounts>.sum(): AllocCounts {
    return fold(AllocCounts.default()) { acc, x -> acc + x }
}
