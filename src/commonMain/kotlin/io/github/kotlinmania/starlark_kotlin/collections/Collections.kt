<<<<<<< HEAD:src/commonMain/kotlin/io/github/kotlinmania/starlark/collections/Collections.kt
// port-lint: source collections.rs
package io.github.kotlinmania.starlark.collections
=======
// port-lint: source src/collections.rs
package io.github.kotlinmania.starlark_kotlin.collections
>>>>>>> origin/main:src/commonMain/kotlin/io/github/kotlinmania/starlark_kotlin/collections/Collections.kt

/*
 * Copyright 2019 The Starlark in Rust Authors.
 * Copyright (c) Facebook, Inc. and its affiliates.
 * Copyright (c) 2025 Sydney Renee, The Solace Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
<<<<<<< HEAD:src/commonMain/kotlin/io/github/kotlinmania/starlark/collections/Collections.kt
 * you may not import this file except in compliance with the License.
=======
 * you may not use this file except in compliance with the License.
>>>>>>> origin/main:src/commonMain/kotlin/io/github/kotlinmania/starlark_kotlin/collections/Collections.kt
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
<<<<<<< HEAD:src/commonMain/kotlin/io/github/kotlinmania/starlark/collections/Collections.kt
 * Defines `SmallMap` and `SmallSet` - collections with deterministic iteration and small memory
 * footprint.
 *
 * These structures use vector-backed storage if there are only a few elements, and an index for
 * larger collections. The API mirrors standard collections.
 *
 * In this Kotlin port, the collection types live in `io.github.kotlinmania.starlarkmap.*` and are
 * imported by users directly rather than re-exported here.
=======
 * Defines [SmallMap] and [SmallSet] - collections with deterministic iteration and small memory footprint.
 *
 * These structures use vector-backed storage if there are only a few elements, and an index
 * for larger collections. The API mirrors standard Rust collections.
 *
 * Rust uses `pub use ...` re-exports from `starlark_map` in this module. Kotlin has no direct
 * equivalent of `pub use`, so call sites should import the concrete types directly from
 * `io.github.kotlinmania.starlark_kotlin.collections.*`.
>>>>>>> origin/main:src/commonMain/kotlin/io/github/kotlinmania/starlark_kotlin/collections/Collections.kt
 */

