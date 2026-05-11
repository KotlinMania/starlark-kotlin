<<<<<<< HEAD:src/commonTest/kotlin/io/github/kotlinmania/starlark/tests/derive/trace/Statics.kt
// port-lint: source tests/derive/trace/statics.rs
package io.github.kotlinmania.starlark.tests.derive.trace
=======
// port-lint: tests tests/derive/trace/statics.rs
package io.github.kotlinmania.starlark_kotlin.tests.derive.trace
>>>>>>> origin/main:src/commonTest/kotlin/io/github/kotlinmania/starlark_kotlin/tests/derive/trace/Statics.kt

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

// Just check it compiles.

import io.github.kotlinmania.starlark_kotlin.values.layout.Value

// #[derive(Trace)]
// struct TraceWithStatic<'v>
@Suppress("unused")
private class TraceWithStatic(
    val actualValue: Value,
    // This field doesn't have a Trace trait, but should be ignored
    // because it looks like it is static
    val ignoredBecauseStatic: StaticType<String>,
    val ignoredBecauseStaticInDyn: Any,
    val ignoredBecauseStaticInDynWithStaticBound: Any,
    // #[trace(static)] // This is no-op, because it is inferred automatically.
    val explicitStatic: String,
)

// struct StaticType<'a, T>
@Suppress("unused")
private class StaticType<T>(val inner: T)
