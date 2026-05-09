// port-lint: source eval/runtime/profile/instant.rs
package io.github.kotlinmania.starlark.eval.runtime.profile

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

import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.TimeSource
import kotlin.time.toDuration

/** Real `Instant` for production code, thread-local counter for tests. */
internal class ProfilerInstant private constructor(
    private val nanos: Long,
) : Comparable<ProfilerInstant> {

    fun durationSince(earlier: ProfilerInstant): Duration {
        val diffNanos = nanos - earlier.nanos
        require(diffNanos >= 0) { "ProfilerInstant::duration_since: earlier is later than self" }
        return diffNanos.toDuration(DurationUnit.NANOSECONDS)
    }

    fun elapsed(): Duration {
        return now().durationSince(this)
    }

    operator fun minus(rhs: ProfilerInstant): Duration {
        return durationSince(rhs)
    }

    override fun compareTo(other: ProfilerInstant): Int {
        return nanos.compareTo(other.nanos)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProfilerInstant) return false
        return nanos == other.nanos
    }

    override fun hashCode(): Int = nanos.hashCode()

    override fun toString(): String = "ProfilerInstant(nanos=$nanos)"

    companion object {
        const val TEST_TICK_MILLIS: Long = 7L

        private val epoch: TimeSource.Monotonic.ValueTimeMark = TimeSource.Monotonic.markNow()

        fun now(): ProfilerInstant {
            val elapsed = epoch.elapsedNow()
            return ProfilerInstant(elapsed.inWholeNanoseconds)
        }
    }
}
