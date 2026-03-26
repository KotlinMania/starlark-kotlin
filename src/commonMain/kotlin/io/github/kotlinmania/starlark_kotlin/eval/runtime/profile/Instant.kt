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
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

/** Real `Instant` for production code. */
internal class ProfilerInstant private constructor(
    private val nanos: Long,
) : Comparable<ProfilerInstant> {

    override fun compareTo(other: ProfilerInstant): Int {
        return nanos.compareTo(other.nanos)
    }

    override fun equals(other: Any?): Boolean =
        other is ProfilerInstant && nanos == other.nanos

    override fun hashCode(): Int = nanos.hashCode()

    /** Duration since an earlier instant. */
    fun durationSince(earlier: ProfilerInstant): Duration {
        return (nanos - earlier.nanos).milliseconds
    }

    /** Duration since this instant was recorded. */
    fun elapsed(): Duration {
        return now().durationSince(this)
    }

    /** Subtract two instants to get a [Duration]. */
    operator fun minus(rhs: ProfilerInstant): Duration {
        return durationSince(rhs)
    }

    companion object {
        private val timeSource = TimeSource.Monotonic
        private val startMark = timeSource.markNow()

        fun now(): ProfilerInstant {
            val elapsed = startMark.elapsedNow()
            return ProfilerInstant(elapsed.inWholeNanoseconds)
        }
    }
}
