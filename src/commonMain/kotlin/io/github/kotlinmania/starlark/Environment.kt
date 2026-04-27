// port-lint: source src/environment.rs
package io.github.kotlinmania.starlark

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

/**
 * Types representing Starlark modules ([Module] and [FrozenModule]) and global variables ([Globals]).
 *
 * Global functions and values are stored in [Globals], which are typically
 * built using [GlobalsBuilder].
 * User executions store their values in a [Module], which have to be converted to a
 * [FrozenModule] using [freeze][Module.freeze] before they can be `load()`'d as a dependency.
 */

/**
 * Errors arising from module / environment operations.
 */
internal sealed class EnvironmentError(message: String) : Exception(message) {
    /** Cannot import private symbol, i.e. underscore prefixed. */
    // CannotImportPrivateSymbol(String)
    class CannotImportPrivateSymbol(symbol: String) :
        EnvironmentError("Cannot import private symbol `$symbol`")

    // ModuleHasNoSymbol(String)
    class ModuleHasNoSymbol(symbol: String) :
        EnvironmentError("Module has no symbol `$symbol`")

    // ModuleHasNoSymbolDidYouMean(String, String)
    class ModuleHasNoSymbolDidYouMean(symbol: String, suggestion: String) :
        EnvironmentError("Module has no symbol `$symbol`, did you mean `$suggestion`?")

    // ModuleSymbolIsNotExported(String)
    class ModuleSymbolIsNotExported(symbol: String) :
        EnvironmentError("Module symbol `$symbol` is not exported")
}
