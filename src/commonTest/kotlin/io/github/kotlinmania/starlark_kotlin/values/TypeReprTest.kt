// port-lint: source src/values/type_repr.rs (tests)
package io.github.kotlinmania.starlark_kotlin.values

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

import io.github.kotlinmania.starlark_kotlin.tests.util.TestComplexValue
import io.github.kotlinmania.starlark_kotlin.values.layout.value.Value
import kotlin.test.Test
import kotlin.test.assertNotEquals

class TypeReprTest {
    @Test
    fun testCanonicalForComplexValue() {
        // In the Rust original, StarlarkTypeRepr::Canonical for TestComplexValue<Value>
        // vs TestComplexValue<FrozenValue> are not equal because they are different Rust types.
        // In Kotlin, the type-erased generic means these are the same at runtime.
        val reprValue = TestComplexValue<Value>().starlarkTypeRepr()
        val reprFrozen = TestComplexValue<FrozenValue>().starlarkTypeRepr()
        // The Rust test asserts NOT equal; in Kotlin generics are erased so they may be equal.
        // We keep the test to document the behavioral difference.
        assertNotEquals(reprValue, reprFrozen)
    }
}
