// port-lint: source src/values/layout/identity.rs
package io.github.kotlinmania.starlark_kotlin.values.layout

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

import io.github.kotlinmania.starlark_kotlin.values.Value

/// An opaque value representing the identity of a given Value. Two values have the same identity
/// if and only if `Value.ptrEq` would return `true` on them.
// pub struct ValueIdentity<'v>
class ValueIdentity private constructor(
    private val identity: Any,
) {
    companion object {
        // pub(crate) fn new(value: Value<'v>) -> ValueIdentity<'v>
        internal fun new(value: Value): ValueIdentity {
            return ValueIdentity(value.ptrValue())
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ValueIdentity) return false
        return identity === other.identity
    }

    override fun hashCode(): Int = System.identityHashCode(identity)
}
