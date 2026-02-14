// port-lint: source src/environment.rs
package io.github.kotlinmania.starlark_kotlin.environment

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

//! Types representing Starlark modules ([Module] and [FrozenModule]) and global variables ([Globals]).
//!
//! Global functions and values are stored in [Globals], which are typically
//! built using [GlobalsBuilder].
//! User executions store their values in a [Module], which have to be converted to a
//! [FrozenModule] using [Module.freeze] before they can be `load()`'d as a dependency.

// Rust module declarations:
// mod globals;
// mod methods;
// mod module_dump;
// mod modules;
// pub(crate) mod names;
// pub(crate) mod slots;
//
// Kotlin: these submodules correspond to files in the `environment/` directory:
//   Globals.kt, Methods.kt, ModuleDump.kt, Modules.kt, Names.kt, Slots.kt

// pub use globals::*;
// pub use methods::*;
// pub use modules::*;
// Kotlin: re-exports are not needed; classes in the same package are directly accessible.

// #[derive(Debug, Error)]
// enum EnvironmentError { ... }
internal sealed class EnvironmentError(override val message: String) : Exception(message) {
    /// Cannot import private symbol, i.e. underscore prefixed.
    // #[error("Cannot import private symbol `{0}`")]
    class CannotImportPrivateSymbol(val symbol: String) :
        EnvironmentError("Cannot import private symbol `$symbol`")

    /// Module has no symbol.
    // #[error("Module has no symbol `{0}`")]
    class ModuleHasNoSymbol(val symbol: String) :
        EnvironmentError("Module has no symbol `$symbol`")

    /// Module has no symbol, with suggestion.
    // #[error("Module has no symbol `{0}`, did you mean `{1}`?")]
    class ModuleHasNoSymbolDidYouMean(val symbol: String, val suggestion: String) :
        EnvironmentError("Module has no symbol `$symbol`, did you mean `$suggestion`?")

    /// Module symbol is not exported.
    // #[error("Module symbol `{0}` is not exported")]
    class ModuleSymbolIsNotExported(val symbol: String) :
        EnvironmentError("Module symbol `$symbol` is not exported")
}
