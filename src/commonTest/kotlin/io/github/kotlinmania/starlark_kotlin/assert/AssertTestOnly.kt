package io.github.kotlinmania.starlark_kotlin.assert

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

import io.github.kotlinmania.starlark_kotlin.Error
import io.github.kotlinmania.starlark_kotlin.golden_test_template.goldenTestTemplate

// Rust `#[cfg(test)]` items from `src/assert/assert.rs` live in `commonTest` in Kotlin MPP.

internal fun Assert.Companion.failGolden(path: String, program: String): Error = failGoldenImpl(path, program)

internal fun Assert.Companion.failSkipTypecheck(program: String, msg: String): Error = failSkipTypecheckImpl(program, msg)

internal fun Assert.Companion.failsSkipTypecheck(program: String, msgs: List<String>): Error = failsSkipTypecheckImpl(program, msgs)

internal fun Assert.Companion.isTrueSkipTypecheck(program: String) {
    isTrueSkipTypecheckImpl(program)
}

private fun failGoldenImpl(path: String, program: String): Error {
    val trimmed = program.trim()
    val e = fails(trimmed, emptyList())
    val output = "Program:\n\n$trimmed\n\nError:\n\n$e\n"
    goldenTestTemplate(path, output)
    return e
}

private fun failSkipTypecheckImpl(program: String, msg: String): Error {
    val a = Assert()
    a.disableStaticTypechecking()
    return a.fail(program, msg)
}

private fun failsSkipTypecheckImpl(program: String, msgs: List<String>): Error {
    val a = Assert()
    a.disableStaticTypechecking()
    return a.fails(program, msgs)
}

private fun isTrueSkipTypecheckImpl(program: String) {
    val a = Assert()
    a.disableStaticTypechecking()
    a.isTrue(program)
}
