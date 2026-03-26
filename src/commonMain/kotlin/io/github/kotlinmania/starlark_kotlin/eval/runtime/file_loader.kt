// port-lint: source src/eval/runtime/file_loader.rs
package io.github.kotlinmania.starlark_kotlin.eval.runtime.file_loader

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

/**
 * Define variants of the evaluation function with different support
 * for the `load(...)` statement.
 */

import io.github.kotlinmania.starlark_kotlin.environment.FrozenModule

/** A trait for turning a `path` given by a `load()` statement into a [FrozenModule]. */
interface FileLoader {
    /** Open the file given by the load statement `path`. */
    fun load(path: String): FrozenModule
}

/**
 * [FileLoader] that looks up modules by name from a map.
 *
 * A list of all load statements can be obtained through
 * `AstModule.loads`.
 * This struct will raise an error if any requested files are not available.
 */
class ReturnFileLoader(
    /** Map from module name (first argument to `load` statement) to the actual module. */
    val modules: Map<String, FrozenModule>,
) : FileLoader {

    // impl FileLoader for ReturnFileLoader
    override fun load(path: String): FrozenModule {
        return modules[path]
            ?: error("ReturnFileLoader does not know the module `$path`")
    }
}

/** Same as [ReturnFileLoader], but owns its modules. */
internal class ReturnOwnedFileLoader(
    val modules: Map<String, FrozenModule>,
) : FileLoader {

    // impl FileLoader for ReturnOwnedFileLoader
    override fun load(path: String): FrozenModule {
        return modules[path]
            ?: error("ReturnOwnedFileLoader does not know the module `$path`")
    }
}
