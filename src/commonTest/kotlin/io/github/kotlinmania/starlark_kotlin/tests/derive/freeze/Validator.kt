// port-lint: source src/tests/derive/freeze/validator.rs
package io.github.kotlinmania.starlark_kotlin.tests.derive.freeze

/*
 * Copyright 2018 The Starlark in Rust Authors.
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

import io.github.kotlinmania.starlark_kotlin.values.Freeze
import io.github.kotlinmania.starlark_kotlin.values.Freezer
import io.github.kotlinmania.starlark_kotlin.values.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.freeze_error.FreezeResult
import io.github.kotlinmania.starlark_kotlin.values.freezeBoolean

// #[derive(Freeze)]
// #[freeze(validator = check_true)]
// struct ValidatorTest { field: bool }
private class ValidatorTest(
    val field: Boolean,
) : Freeze<ValidatorTest> {
    override fun freeze(freezer: Freezer): FreezeResult<ValidatorTest> {
        val result = ValidatorTest(freezeBoolean(field, freezer).getOrElse { return Result.failure(it) })
        // validator: check_true
        checkTrue(result).getOrElse { return Result.failure(it) }
        return Result.success(result)
    }
}

// fn check_true(test: &ValidatorTest) -> anyhow::Result<()>
private fun checkTrue(test: ValidatorTest): Result<Unit> {
    if (!test.field) {
        return Result.failure(Exception("Err"))
    }
    return Result.success(Unit)
}

// #[test]
// fn test_ok() -> anyhow::Result<()>
internal fun testOk() {
    val t = ValidatorTest(field = true)
    val frozenHeap = FrozenHeap()
    val freezer = Freezer(frozenHeap)
    t.freeze(freezer).getOrThrow()
}

// #[test]
// fn test_fail() -> anyhow::Result<()>
internal fun testFail() {
    val t = ValidatorTest(field = false)
    val frozenHeap = FrozenHeap()
    val freezer = Freezer(frozenHeap)
    check(t.freeze(freezer).isFailure)
}
