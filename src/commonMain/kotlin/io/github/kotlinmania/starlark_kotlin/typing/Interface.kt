<<<<<<< HEAD:src/commonMain/kotlin/io/github/kotlinmania/starlark/typing/Interface.kt
// port-lint: source typing/interface.rs
package io.github.kotlinmania.starlark.typing
=======
// port-lint: source src/typing/interface.rs
package io.github.kotlinmania.starlark_kotlin.typing
>>>>>>> origin/main:src/commonMain/kotlin/io/github/kotlinmania/starlark_kotlin/typing/Interface.kt

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

/** Interface representing the types of all bindings in a module. */
// #[derive(Default, Dupe, Clone, Debug)]
// pub struct Interface(Arc<HashMap<String, Ty>>);
class Interface private constructor(
    private val bindings: Map<String, Ty>,
) {
    // impl Interface

    companion object {
        /** Create an empty interface, with no bindings. */
        // pub fn empty() -> Self
        fun empty(): Interface = Interface(emptyMap())

        /** Create a new interface with the given bindings. */
        // pub fn new(bindings: HashMap<String, Ty>) -> Self
        fun new(bindings: Map<String, Ty>): Interface = Interface(bindings)
    }

    /** Get the type for a given binding. */
    // pub fn get(&self, name: &str) -> Option<&Ty>
    fun get(name: String): Ty? = bindings[name]
}
