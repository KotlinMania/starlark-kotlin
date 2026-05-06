// port-lint: ignore
package io.github.kotlinmania.starlark

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

const val CALL_STACK_TRACEBACK_PREFIX: String = "Traceback (most recent call last):"

/** Owned call stack. */
data class CallStack(
    val frames: List<Frame> = emptyList(),
) {
    /** Is the call stack empty? */
    fun isEmpty(): Boolean = frames.isEmpty()

    /** Take the contained frames. */
    fun intoFrames(): List<Frame> = frames

    override fun toString(): String {
        val sb = StringBuilder()
        if (frames.isNotEmpty()) {
            // Match Python output.
            sb.append(CALL_STACK_TRACEBACK_PREFIX)
            sb.append('\n')
            // Use real module name when available.
            var prev = "<module>"
            for (x in frames) {
                x.writeTwoLines("  ", prev, sb)
                prev = x.name
            }
        }
        return sb.toString()
    }
}
