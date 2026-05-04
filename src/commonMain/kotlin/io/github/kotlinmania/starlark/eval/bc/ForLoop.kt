// port-lint: source eval/bc/forLoop.rs
package io.github.kotlinmania.starlark.eval.bc

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

/**
 * Depth of the loop. For example,
 *
 * ```text
 * def foo():
 * for i in range(10): # depth 0
 * for j in range(20): # depth 1
 * pass
 * ```
 */
//     Default, Debug, Copy, Clone, Dupe, deriveMore::Display,
//     Eq, PartialEq, Ord, PartialOrd
// )]
internal data class LoopDepth(val depth: Int = 0) : Comparable<LoopDepth> {
    override fun compareTo(other: LoopDepth): Int = depth.compareTo(other.depth)
    override fun toString(): String = depth.toString()
}
