// port-lint: source src/eval/runtime/small_duration.rs
package io.github.kotlinmania.starlark_kotlin.eval.runtime

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
import kotlin.time.Duration.Companion.nanoseconds

/**
 * Slightly faster than [Duration].
 */
internal data class SmallDuration(
    /** `ULong.MAX_VALUE` nanos is ~500 years. */
    internal var nanos: ULong = 0u,
) : Comparable<SmallDuration> {

    override fun compareTo(other: SmallDuration): Int {
        return nanos.compareTo(other.nanos)
    }

    companion object {
        fun default(): SmallDuration = SmallDuration(0u)

        fun fromDuration(duration: Duration): SmallDuration {
            return SmallDuration(duration.inWholeNanoseconds.toULong())
        }

        internal fun fromMillis(millis: ULong): SmallDuration {
            return fromDuration(millis.toLong().milliseconds)
        }

        fun sum(durations: Iterable<SmallDuration>): SmallDuration {
            return durations.fold(default()) { acc, x -> acc + x }
        }
    }

    fun toDuration(): Duration {
        return nanos.toLong().nanoseconds
    }

    operator fun plus(other: SmallDuration): SmallDuration {
        return SmallDuration(nanos + other.nanos)
    }

    operator fun plus(other: Duration): SmallDuration {
        return SmallDuration(nanos + other.inWholeNanoseconds.toULong())
    }

    operator fun div(other: ULong): SmallDuration {
        return SmallDuration(nanos / other)
    }
}
