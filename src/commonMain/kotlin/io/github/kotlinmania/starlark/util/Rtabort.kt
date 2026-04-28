// port-lint: source src/util/rtabort.rs
package io.github.kotlinmania.starlark.util

/*
 * Copyright 2018 The Starlark in Rust Authors.
 * Copyright (c) Facebook, Inc. and its affiliates.
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
 * Like [error], but aborts the process instead of unwinding.
 *
 * Although we compile buck2 with `panic=abort`, this is safer because
 * others may copy-paste code.
 */
internal fun rtabortImplFixedString(file: String, line: Int, message: String): Nothing {
    rtabortImpl(file, line, message)
}

internal fun rtabortImpl(file: String, line: Int, msg: String): Nothing {
    // Make sure we abort even if formatting throws.
    val abort = AbortOnDrop()

    // Stderr write followed by abort does not print anything in tests.
    try {
        println("$file:$line: abort: $msg")
    } catch (_: Throwable) {
    }
    abort.disarm()

    // Tell the compiler that we never return.
    throw Error("$file:$line: abort: $msg")
}

internal class AbortOnDrop {
    private var armed: Boolean = true

    fun disarm() {
        armed = false
    }
}
