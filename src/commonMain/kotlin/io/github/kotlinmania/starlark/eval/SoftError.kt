// port-lint: source src/eval/softError.rs
package io.github.kotlinmania.starlark.eval

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

/** Deprecation handler provided by a user. */
interface SoftErrorHandler {
    /**
     * Handle deprecation error. If this function returns `Ok`, error will be ignored,
     * otherwise error will be propagated.
     */
    fun softError(category: String, error: io.github.kotlinmania.starlark.Error)
}

/** Default handler: warnings are treated as errors. */
internal object HardErrorSoftErrorHandler : SoftErrorHandler {
    //     Err(error)
    override fun softError(_category: String, error: io.github.kotlinmania.starlark.Error) {
        throw error
    }
}
