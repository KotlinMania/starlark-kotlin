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
import kotlin.time.toDuration

/// Real `Instant` for production code, thread-local counter for tests.
// #[derive(Debug, Copy, Clone, Eq, PartialEq, Ord, PartialOrd, Allocative)]
// pub(crate) struct ProfilerInstant(
//     #[cfg(not(test))] std::time::Instant,
//     #[cfg(test)] u64, // Millis.
// );
internal class ProfilerInstant private constructor(
    private val value: Long, // millis in test mode, nanos in production
) : Comparable<ProfilerInstant> {

    // impl ProfilerInstant

    companion object {
        // #[cfg(test)]
        // pub(crate) const TEST_TICK_MILLIS: u64 = 7;
        const val TEST_TICK_MILLIS: Long = 7L

        // Rust uses #[cfg(test)] for compile-time switching; Kotlin uses runtime flag.
        var testMode: Boolean = false
        private var nowMillis: Long = 100003L

        fun resetTestCounter() {
            nowMillis = 100003L
        }

        private val epoch: TimeSource.Monotonic.ValueTimeMark = TimeSource.Monotonic.markNow()

        // #[inline]
        // pub(crate) fn now() -> Self
        fun now(): ProfilerInstant {
            // #[cfg(test)]
            // thread_local! {
            //     static NOW_MILLIS: std::cell::Cell<u64> = const { std::cell::Cell::new(100003) };
            // }
            // ProfilerInstant(NOW_MILLIS.with(|v| { let r = v.get(); v.set(r + TEST_TICK_MILLIS); r }))
            return if (testMode) {
                val r = nowMillis
                nowMillis += TEST_TICK_MILLIS
                ProfilerInstant(r)
            } else {
                // #[cfg(not(test))]
                // ProfilerInstant(std::time::Instant::now())
                val elapsed = epoch.elapsedNow()
                ProfilerInstant(elapsed.inWholeNanoseconds)
            }
        }
    }

    // #[inline]
    // pub(crate) fn duration_since(&self, earlier: ProfilerInstant) -> Duration
    fun durationSince(earlier: ProfilerInstant): Duration {
        return if (testMode) {
            // #[cfg(test)]
            // Duration::from_millis(self.0.checked_sub(earlier.0).unwrap())
            val diffMillis = value - earlier.value
            require(diffMillis >= 0) { "ProfilerInstant::duration_since: earlier is later than self" }
            diffMillis.toDuration(DurationUnit.MILLISECONDS)
        } else {
            // #[cfg(not(test))]
            // self.0.duration_since(earlier.0)
            val diffNanos = value - earlier.value
            require(diffNanos >= 0) { "ProfilerInstant::duration_since: earlier is later than self" }
            diffNanos.toDuration(DurationUnit.NANOSECONDS)
        }
    }

    // #[inline]
    // pub(crate) fn elapsed(&self) -> Duration
    fun elapsed(): Duration {
        // #[cfg(not(test))]
        // self.0.elapsed()
        // #[cfg(test)]
        // ProfilerInstant::now().duration_since(*self)
        return now().durationSince(this)
    }

    // impl Sub for ProfilerInstant
    // type Output = Duration;
    // #[inline]
    // fn sub(self, rhs: Self) -> Self::Output
    operator fun minus(rhs: ProfilerInstant): Duration {
        return durationSince(rhs)
    }

    override fun compareTo(other: ProfilerInstant): Int {
        return value.compareTo(other.value)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProfilerInstant) return false
        return value == other.value
    }

    override fun hashCode(): Int = value.hashCode()

    override fun toString(): String = "ProfilerInstant(value=$value)"
}
