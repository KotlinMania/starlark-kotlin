<<<<<<< HEAD:src/commonMain/kotlin/io/github/kotlinmania/starlark/values/types/record/TyRecordType.kt
// port-lint: source values/types/record/ty_record_type.rs
package io.github.kotlinmania.starlark.values.types.record
=======
// port-lint: source src/values/types/record/ty_record_type.rs
package io.github.kotlinmania.starlark_kotlin.values.types.record
>>>>>>> origin/main:src/commonMain/kotlin/io/github/kotlinmania/starlark_kotlin/values/types/record/TyRecordType.kt

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

import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.values.layout.FrozenValue
import io.github.kotlinmania.starlark_kotlin.eval.runtime.params.spec.ParametersSpec

/**
 * Data associated with a record type for typechecking.
 *
 * #[derive(Allocative, Debug)]
 * #[doc(hidden)]
 * pub struct TyRecordData
 */
class TyRecordData(
    /** Name of the record type. */
    internal val name: String,
    /** Type of record instance. */
    internal val tyRecord: Ty,
    /** Type of record type. */
    internal val tyRecordType: Ty,
    /**
     * Creating these on every invoke is pretty expensive (profiling shows)
     * so compute them in advance and cache.
     */
    internal val parameterSpec: ParametersSpec<FrozenValue>,
)
