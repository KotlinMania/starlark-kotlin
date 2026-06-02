// port-lint: tests tests/derive/freeze/identity.rs
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
import io.github.kotlinmania.starlark.values.freezeString
import io.github.kotlinmania.starlark.values.layout.Freezer
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap

// struct NonFreeze(u32)
private class NonFreeze(
    val value: UInt,
)

// #[derive(Freeze)]
// struct TestStruct { s: String, #[freeze(identity)] s2: NonFreeze }
private class TestStruct(
    val s: String,
    val s2: NonFreeze, // #[freeze(identity)]
) : Freeze<TestStruct> {
    override fun freeze(freezer: Freezer): Result<TestStruct> {
        return Result.success(TestStruct(freezeString(s, freezer).getOrElse { return Result.failure(it) }, s2))
    }
}

// #[derive(Freeze)]
// struct TestUnitStruct(String, #[freeze(identity)] NonFreeze)
private class TestUnitStruct(
    val component1: String,
    val component2: NonFreeze, // #[freeze(identity)]
) : Freeze<TestUnitStruct> {
    override fun freeze(freezer: Freezer): Result<TestUnitStruct> {
        return Result.success(TestUnitStruct(freezeString(component1, freezer).getOrElse { return Result.failure(it) }, component2))
    }
}

// #[derive(Freeze)]
// enum TestEnum { A(String), B(#[freeze(identity)] NonFreeze) }
private sealed class TestEnum : Freeze<TestEnum> {
    class A(
        val value: String,
    ) : TestEnum() {
        override fun freeze(freezer: Freezer): Result<TestEnum> {
            return Result.success(A(freezeString(value, freezer).getOrElse { return Result.failure(it) }))
        }
    }

    class B(
        val value: NonFreeze,
    ) : TestEnum() {
        // #[freeze(identity)]
        override fun freeze(freezer: Freezer): Result<TestEnum> {
            freezer.frozenHeap()
            return Result.success(B(value))
        }
    }
}

// #[test]
// fn test_struct() -> anyhow::Result<()>
internal fun testStruct() {
    val t =
        TestStruct(
            s = "test",
            s2 = NonFreeze(55u),
        )
    val frozenHeap = FrozenHeap()
    val freezer = Freezer(frozenHeap)
    t.freeze(freezer).getOrThrow()
}

// #[test]
// fn test_anon_struct() -> anyhow::Result<()>
internal fun testAnonStruct() {
    val t = TestUnitStruct("test", NonFreeze(56u))
    val frozenHeap = FrozenHeap()
    val freezer = Freezer(frozenHeap)
    t.freeze(freezer).getOrThrow()
}
