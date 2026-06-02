// port-lint: source src/eval/runtime/file_loader.rs
package io.github.kotlinmania.starlark.eval.runtime.fileloader

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

// ! Define variants of the evaluation function with different support
// ! for the `load(...)` statement.

// use std::collections::HashMap;

// use dupe::Dupe;

// use crate::environment::FrozenModule;

import io.github.kotlinmania.starlark.environment.FrozenModule

// / A trait for turning a `path` given by a `load()` statement into a [`FrozenModule`].
// pub trait FileLoader {
interface FileLoader {
    // / Open the file given by the load statement `path`.
    // fn load(&self, path: &str) -> crate::Result<FrozenModule>;
    fun load(path: String): FrozenModule
}

// / [`FileLoader`] that looks up modules by name from a [`HashMap`].
// /
// / A list of all load statements can be obtained through
// / [`AstModule::loads`](crate::syntax::AstModule::loads).
// / This struct will raise an error if any requested files are not available.
// pub struct ReturnFileLoader<'a> {
//     pub modules: &'a HashMap<&'a str, &'a FrozenModule>,
// }
class ReturnFileLoader(
    // / Map from module name (first argument to `load` statement) to the actual module.
    val modules: Map<String, FrozenModule>,
) : FileLoader {
    // impl<'a> FileLoader for ReturnFileLoader<'a>
    //     fn load(&self, path: &str) -> crate::Result<FrozenModule>
    override fun load(path: String): FrozenModule =
        modules[path]
            ?: error("ReturnFileLoader does not know the module `$path`")
}

// / Same as [`ReturnFileLoader`], but does not require fighting the borrow checker.
// #[cfg(test)]
// pub(crate) struct ReturnOwnedFileLoader {
//     pub(crate) modules: HashMap<String, FrozenModule>,
// }
internal class ReturnOwnedFileLoader(
    val modules: Map<String, FrozenModule>,
) : FileLoader {
    // #[cfg(test)]
    // impl FileLoader for ReturnOwnedFileLoader
    //     fn load(&self, path: &str) -> crate::Result<FrozenModule>
    override fun load(path: String): FrozenModule =
        modules[path]
            ?: error("ReturnOwnedFileLoader does not know the module `$path`")
}
