// port-lint: source src/eval/bc/definitely_assigned.rs
package io.github.kotlinmania.starlark_kotlin.eval.bc

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

import io.github.kotlinmania.starlark_kotlin.eval.runtime.slots.LocalSlotId

/// Tracker for local variables which are definitely assigned.
///
/// For example, when compiling a program like:
///
/// ```python
/// foo(x)
/// bar(x)
/// ```
///
/// To access `x` variable first time, when evaluating `foo(x)` we need to check
/// that `x` is assigned, and return an error if it is not.
/// But when evaluating `bar(x)` we don't need to check `x` is assigned,
/// because we know for sure it is assigned: we checked that when evaluating `foo(x)`.
internal data class BcDefinitelyAssigned(
    /// Map from local variable slot to flag indicating whether it is definitely assigned
    /// at the current program point.
    private val definitelyAssigned: BooleanArray,
) {
    constructor(localCount: UInt) : this(
        definitelyAssigned = BooleanArray(localCount.toInt()),
    )

    /// Is local variable definitely assigned at given program point?
    fun isDefinitelyAssigned(local: LocalSlotId): Boolean {
        return definitelyAssigned[local.index]
    }

    /// Mark variable definitely assigned.
    ///
    /// For example, after execution of:
    ///
    /// ```python
    /// foo(x)
    /// ```
    ///
    /// both `foo` and `x` are definitely assigned.
    fun markDefinitelyAssigned(local: LocalSlotId) {
        definitelyAssigned[local.index] = true
    }

    /// Assert that each variable definitely assigned in self,
    /// also definitely assigned in other.
    fun assertSmallerThan(other: BcDefinitelyAssigned) {
        require(definitelyAssigned.size == other.definitelyAssigned.size)
        for (i in definitelyAssigned.indices) {
            val a = definitelyAssigned[i]
            val b = other.definitelyAssigned[i]
            require(b || !a)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BcDefinitelyAssigned) return false
        return definitelyAssigned.contentEquals(other.definitelyAssigned)
    }

    override fun hashCode(): Int = definitelyAssigned.contentHashCode()

    fun clone(): BcDefinitelyAssigned = BcDefinitelyAssigned(definitelyAssigned.copyOf())
}
