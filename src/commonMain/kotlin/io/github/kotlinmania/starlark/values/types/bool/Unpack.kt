// port-lint: source values/types/bool/unpack.rs
package io.github.kotlinmania.starlark.values.types.bool

/*
 * Copyright 2019 The Starlark in Rust Authors.
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

import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.UnpackValue
import io.github.kotlinmania.starlark.values.layout.Value

/**
 *
 * `type Error = Infallible`.
 *
 * delegates to `value.unpackBool()`, which returns `None` if the value is not
 * a Starlark bool. Conversion never fails, so the error type is `Infallible`.
 */
object BooleanUnpack : UnpackValue<Boolean> {
    override fun starlarkTypeRepr(): Ty = Ty.bool()

    override fun unpackValueImpl(value: Value): Result<Boolean?> {
        return Result.success(value.unpackBool())
    }
}
