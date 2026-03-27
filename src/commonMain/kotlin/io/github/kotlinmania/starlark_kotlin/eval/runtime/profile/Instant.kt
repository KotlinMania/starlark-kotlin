// port-lint: source src/eval/runtime/profile/instant.rs
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

import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.TimeSource
import io.github.kotlinmania.starlark_kotlin.eval.runtime.small_duration.toDuration

/// Real `Instant` for production code, thread-local counter for tests.
// #[derive(Debug, Copy, Clone, Eq, PartialEq, Ord, PartialOrd, Allocative)]
// pub(crate) struct ProfilerInstant(
//     #[cfg(not(test))] std::time::Instant,
//     #[cfg(test)] u64, // Millis.
// );
// Kotlin: uses kotlin.time.TimeSource.Monotonic for real time.
// TODO: stub - ProfilerInstant needs real import
internal class ProfilerInstant private constructor(
    private val nanos: Long,
) : Comparable<ProfilerInstant> {

    companion object {
        // #[cfg(test)]
        // pub(crate) const TEST_TICK_MILLIS: u64 = 7;
        const val TEST_TICK_MILLIS: Long = 7L

        private val epoch: TimeSource.Monotonic.ValueTimeMark = TimeSource.Monotonic.markNow()

        // #[inline]
        // pub(crate) fn now() -> Self
        fun now(): ProfilerInstant {
            val elapsed = epoch.elapsedNow()
            return ProfilerInstant(elapsed.inWholeNanoseconds)
        }
    }

    // #[inline]
    // pub(crate) fn duration_since(&self, earlier: ProfilerInstant) -> Duration
    fun durationSince(earlier: ProfilerInstant): Duration {
        val diffNanos = nanos - earlier.nanos
        require(diffNanos >= 0) { "ProfilerInstant::duration_since: earlier is later than self" }
        return diffNanos.toDuration(DurationUnit.NANOSECONDS)
    }

    // #[inline]
    // pub(crate) fn elapsed(&self) -> Duration
    fun elapsed(): Duration {
        return now().durationSince(this)
    }

    // impl Sub for ProfilerInstant
    // fn sub(self, rhs: Self) -> Duration
    operator fun minus(rhs: ProfilerInstant): Duration {
        return durationSince(rhs)
    }

    // impl Ord for ProfilerInstant
    override fun compareTo(other: ProfilerInstant): Int {
        return nanos.compareTo(other.nanos)
    }

    // impl PartialEq for ProfilerInstant
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProfilerInstant) return false
        return nanos == other.nanos
    }

    // impl Hash for ProfilerInstant
    override fun hashCode(): Int = nanos.hashCode()

    // impl Debug for ProfilerInstant
    override fun toString(): String = "ProfilerInstant(nanos=$nanos)"
}
