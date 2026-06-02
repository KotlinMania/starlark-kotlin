// port-lint: source src/eval/runtime/profile/instant.rs
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

import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.TimeSource
import kotlin.time.toDuration

// / Real `Instant` for production code, thread-local counter for tests.
// );
internal class ProfilerInstant private constructor(
    private val value: Long, // millis in test mode, nanos in production
) : Comparable<ProfilerInstant> {
    companion object {
        const val TEST_TICK_MILLIS: Long = 7L

        // Rust uses #[cfg(test)] for compile-time switching; Kotlin uses runtime flag.
        var testMode: Boolean = false
        private var nowMillis: Long = 100003L

        fun resetTestCounter() {
            nowMillis = 100003L
        }

        private val epoch: TimeSource.Monotonic.ValueTimeMark = TimeSource.Monotonic.markNow()

        fun now(): ProfilerInstant {
            // thread_local! {
            // ProfilerInstant(NOW_MILLIS.with(|v| { let r = v.get(); v.set(r + TEST_TICK_MILLIS); r }))
            return if (testMode) {
                val r = nowMillis
                nowMillis += TEST_TICK_MILLIS
                ProfilerInstant(r)
            } else {
                // ProfilerInstant(std::time::Instant::now())
                val elapsed = epoch.elapsedNow()
                ProfilerInstant(elapsed.inWholeNanoseconds)
            }
        }
    }

    fun durationSince(earlier: ProfilerInstant): Duration =
        if (testMode) {
            val diffMillis = value - earlier.value
            require(diffMillis >= 0) { "ProfilerInstant::duration_since: earlier is later than self" }
            diffMillis.toDuration(DurationUnit.MILLISECONDS)
        } else {
            // self.0.duration_since(earlier.0)
            val diffNanos = value - earlier.value
            require(diffNanos >= 0) { "ProfilerInstant::duration_since: earlier is later than self" }
            diffNanos.toDuration(DurationUnit.NANOSECONDS)
        }

    fun elapsed(): Duration {
        // self.0.elapsed()
        return now().durationSince(this)
    }

    operator fun minus(rhs: ProfilerInstant): Duration = durationSince(rhs)

    override fun compareTo(other: ProfilerInstant): Int = value.compareTo(other.value)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProfilerInstant) return false
        return value == other.value
    }

    override fun hashCode(): Int = value.hashCode()

    override fun toString(): String = "ProfilerInstant(value=$value)"
}
