// port-lint: source src/eval/runtime/small_duration.rs
package io.github.kotlinmania.starlark_kotlin.eval.runtime.small_duration

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
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.milliseconds

/// Slightly faster than `Duration`.
// #[derive(Copy, Clone, Dupe, Default, Eq, PartialEq, Ord, PartialOrd, Debug, Allocative)]
// pub(crate) struct SmallDuration { pub(crate) nanos: u64 }
internal data class SmallDuration(
    /// `u64::MAX` nanos is 500 years.
    // pub(crate) nanos: u64
    internal var nanos: ULong = 0u,
) : Comparable<SmallDuration> {

    override fun compareTo(other: SmallDuration): Int {
        return nanos.compareTo(other.nanos)
    }

    // impl SmallDuration

    companion object {
        // fn default() -> SmallDuration
        fun default(): SmallDuration = SmallDuration(0u)

        // pub(crate) fn from_duration(duration: Duration) -> SmallDuration
        fun fromDuration(duration: Duration): SmallDuration {
            return SmallDuration(duration.inWholeNanoseconds.toULong())
        }

        // #[cfg(test)]
        // pub(crate) fn from_millis(millis: u64) -> SmallDuration
        internal fun fromMillis(millis: ULong): SmallDuration {
            return fromDuration(millis.toLong().milliseconds)
        }

        // impl<'a> Sum<&'a SmallDuration> for SmallDuration
        // impl Sum<SmallDuration> for SmallDuration
        fun sum(durations: Iterable<SmallDuration>): SmallDuration {
            return durations.fold(default()) { acc, x -> acc + x }
        }
    }

    // pub(crate) fn to_duration(self) -> Duration
    fun toDuration(): Duration {
        return nanos.toLong().nanoseconds
    }

    // impl AddAssign for SmallDuration
    // fn add_assign(&mut self, other: Self)
    // impl AddAssign<Duration> for SmallDuration
    // fn add_assign(&mut self, other: Duration)

    // impl Add<SmallDuration> for SmallDuration
    operator fun plus(other: SmallDuration): SmallDuration {
        return SmallDuration(nanos + other.nanos)
    }

    // impl Add<Duration> for SmallDuration
    operator fun plus(other: Duration): SmallDuration {
        return SmallDuration(nanos + other.inWholeNanoseconds.toULong())
    }

    // impl Div<u64> for SmallDuration
    operator fun div(other: ULong): SmallDuration {
        return SmallDuration(nanos / other)
    }
}
