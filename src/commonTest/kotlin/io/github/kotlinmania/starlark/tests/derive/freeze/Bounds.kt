// port-lint: tests tests/derive/freeze/bounds.rs
package io.github.kotlinmania.starlark.tests.derive.freeze

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

import io.github.kotlinmania.starlark.values.Freeze
import io.github.kotlinmania.starlark.values.layout.Freezer

internal interface Bound

internal class BoundsTest<V>(
    val field: V,
) : Freeze<BoundsTest<V>> where V : Freeze<V> {
    @Suppress("UNCHECKED_CAST")
    override fun freeze(freezer: Freezer): Result<BoundsTest<V>> {
        val frozenField = (field as Freeze<V>).freeze(freezer).getOrElse { return Result.failure(it) }
        return Result.success(BoundsTest(frozenField))
    }
}

internal fun <V> checkType(t: BoundsTest<V>): Result<Unit> where V : Bound, V : Freeze<V> = Result.success(Unit)

@Suppress("unused")
internal fun assertImpl() {
    class Impl :
        Bound,
        Freeze<Impl> {
        override fun freeze(freezer: Freezer): Result<Impl> = Result.success(this)
    }

    fun check(value: Freeze<*>) {}

    check(BoundsTest(field = Impl()))
}
