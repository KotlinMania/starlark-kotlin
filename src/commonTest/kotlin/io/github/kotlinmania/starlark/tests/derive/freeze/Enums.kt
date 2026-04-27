// port-lint: source src/tests/derive/freeze/enums.rs
package io.github.kotlinmania.starlark.tests.derive.freeze

/*
 * Copyright 2018 The Starlark in Rust Authors.
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

// Only check it compiles.

import io.github.kotlinmania.starlark.values.Freeze

@Suppress("unused")
private sealed class TestFreezeEnum<out V> {
    // A
    data object A : TestFreezeEnum<Nothing>()
    // B()
    class B : TestFreezeEnum<Nothing>()
    // C(V)
    class C<V>(val value: V) : TestFreezeEnum<V>()
    // D(V, V)
    class D<V>(val first: V, val second: V) : TestFreezeEnum<V>()
    // E {}
    class E : TestFreezeEnum<Nothing>()
    // F { a: V }
    class F<V>(val a: V) : TestFreezeEnum<V>()
    // G { a: V, b: V }
    class G<V>(val a: V, val b: V) : TestFreezeEnum<V>()
}
