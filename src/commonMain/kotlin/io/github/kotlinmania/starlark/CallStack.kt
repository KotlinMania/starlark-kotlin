// port-lint: source ../starlark_syntax/src/call_stack.rs
package io.github.kotlinmania.starlark

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

const val CALL_STACK_TRACEBACK_PREFIX: String = "Traceback (most recent call last):"

/** Owned call stack. */
// #[derive(Debug, Default, Clone, PartialEq, Eq, Hash)]
// pub struct CallStack {
//     pub frames: Vec<Frame>,
// }
data class CallStack(
    val frames: List<Frame> = emptyList(),
) {
    /** Is the call stack empty? */
    // pub fn is_empty(&self) -> bool
    fun isEmpty(): Boolean = frames.isEmpty()

    /** Take the contained frames. */
    // pub fn into_frames(self) -> Vec<Frame>
    fun intoFrames(): List<Frame> = frames

    // impl Display for CallStack
    override fun toString(): String {
        if (frames.isEmpty()) return ""
        val sb = StringBuilder()
        sb.appendLine(CALL_STACK_TRACEBACK_PREFIX)
        var prev = "<module>"
        for (x in frames) {
            x.writeTwoLines("  ", prev, sb)
            prev = x.name
        }
        return sb.toString()
    }
}
