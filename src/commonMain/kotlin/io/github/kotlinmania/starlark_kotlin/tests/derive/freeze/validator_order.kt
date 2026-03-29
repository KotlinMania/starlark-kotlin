// port-lint: source src/tests/derive/freeze/validator_order.rs
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

// struct FreezeSentinel { frozen: bool }
private class FreezeSentinel(
    val frozen: Boolean,
) : Freeze<FreezeSentinel> {
    // impl Freeze for FreezeSentinel
    // fn freeze(self, _: &Freezer) -> FreezeResult<Self>
    override fun freeze(freezer: Freezer): FreezeResult<FreezeSentinel> {
        check(!frozen)
        return Result.success(FreezeSentinel(frozen = true))
    }
}

// #[derive(Freeze)]
// #[freeze(validator = check_froze_before_validating)]
// struct ValidatorOrderTest { sentinel: FreezeSentinel }
private class ValidatorOrderTest(
    val sentinel: FreezeSentinel,
) : Freeze<ValidatorOrderTest> {
    override fun freeze(freezer: Freezer): FreezeResult<ValidatorOrderTest> {
        val frozenSentinel = sentinel.freeze(freezer).getOrElse { return Result.failure(it) }
        val result = ValidatorOrderTest(frozenSentinel)
        // validator: check_froze_before_validating
        checkFrozeBeforeValidating(result).getOrElse { return Result.failure(it) }
        return Result.success(result)
    }
}

// fn check_froze_before_validating(test: &ValidatorOrderTest) -> anyhow::Result<()>
private fun checkFrozeBeforeValidating(test: ValidatorOrderTest): Result<Unit> {
    // Accessing fields on a Starlark value before we call freeze() on it may fail (because we
    // read the forward not what it points to), so we check that validators receive frozen data.
    check(test.sentinel.frozen)
    return Result.success(Unit)
}

// #[test]
// fn test() -> anyhow::Result<()>
internal fun testValidatorOrder() {
    val t = ValidatorOrderTest(
        sentinel = FreezeSentinel(frozen = false),
    )
    val frozenHeap = FrozenHeap()
    val freezer = Freezer(frozenHeap)
    t.freeze(freezer).getOrThrow()
}
