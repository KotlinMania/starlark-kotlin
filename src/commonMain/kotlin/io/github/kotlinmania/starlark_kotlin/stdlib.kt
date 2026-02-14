// port-lint: source src/stdlib.rs
package io.github.kotlinmania.starlark_kotlin.stdlib

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

/// A module with the standard function and constants that are by default in all
/// dialect of Starlark

// pub(crate) mod breakpoint;
// pub(crate) mod call_stack;
// pub(crate) mod extra;
// mod funcs;
// pub(crate) mod internal;
// pub(crate) mod json;
// pub(crate) mod partial;
// Submodules: breakpoint, call_stack, extra, funcs, internal, json, partial

// pub use extra::PrintHandler;

/// Return the default global environment, it is not yet frozen so that a caller
/// can refine it.
///
/// For example `standardEnvironment().freeze().child("test")` create a
/// child environment of this global environment that have been frozen.
// pub(crate) fn standard_environment() -> GlobalsBuilder
internal fun standardEnvironment(): GlobalsBuilder {
    return GlobalsBuilder.new().with { builder -> registerGlobals(builder) }
}

/// The extra library definitions available in this Starlark implementation, but not in the standard.
// #[derive(PartialEq, Eq, Copy, Clone, Dupe)]
// pub enum LibraryExtension
enum class LibraryExtension {
    /// Definitions to support the `struct` type, the `struct()` constructor.
    StructType,
    /// Definitions to support the `record` type, the `record()` constructor and `field()` function.
    RecordType,
    /// Definitions to support the `enum` type, the `enum()` constructor.
    EnumType,
    /// Add a function `namespace()` which acts much like `struct()` but is clear about its
    /// intended use and stricter
    NamespaceType,
    /// A function `map(f, xs)` which applies `f` to each element of `xs` and returns the result.
    Map,
    /// A function `filter(f, xs)` which applies `f` to each element of `xs` and returns those for which `f` returns `True`.
    /// As a special case, `filter(None, xs)` removes all `None` values.
    Filter,
    /// Partially apply a function, `partial(f, *args, **kwargs)` will create a function where those `args` `kwargs`
    /// are already applied to `f`.
    Partial,
    /// Add a function `debug(x)` which shows the debug representation of a value.
    /// Useful when debugging, but the output should not be considered stable.
    Debug,
    /// Add a function `print(x)` which prints to stderr.
    Print,
    /// Add a function `pprint(x)` which pretty-prints to stderr.
    Pprint,
    /// Add a function `pstr` which is a pretty-printed version of `str`.
    Pstr,
    /// Add a function `prepr` which is a pretty-printed version of `repr`.
    Prepr,
    /// Add a function `breakpoint()` which will drop into a console-module evaluation prompt.
    Breakpoint,
    /// Add a function `json()` which will generate JSON for a module.
    Json,
    /// Provides `typing.All`, `typing.Callable` etc.
    /// Usually used in conjunction with `Dialect.enableTypes`.
    Typing,
    /// Utilities exposing starlark-rust internals.
    /// These are not for production use.
    Internal,
    /// Add a function `call_stack()` which returns a string representation of
    /// the current call stack.
    CallStack,
    /// Definitions to support the `set` type, the `set()` constructor.
    SetType;

    companion object {
        // pub(crate) fn all() -> &'static [Self]
        /// A list of all extensions that will be updated as new methods are added.
        fun all(): List<LibraryExtension> = entries
    }

    // pub fn add(self, builder: &mut GlobalsBuilder)
    /// Add a specific extension to a `GlobalsBuilder`.
    fun add(builder: GlobalsBuilder) {
        when (this) {
            StructType -> registerStruct(builder)
            NamespaceType -> registerNamespace(builder)
            RecordType -> registerRecord(builder)
            EnumType -> registerEnum(builder)
            SetType -> registerSet(builder)
            Map -> extraMap(builder)
            Filter -> extraFilter(builder)
            Partial -> partialPartial(builder)
            Debug -> extraDebug(builder)
            Print -> extraPrint(builder)
            Pprint -> extraPprint(builder)
            Pstr -> extraPstr(builder)
            Prepr -> extraPrepr(builder)
            Breakpoint -> breakpointGlobal(builder)
            Json -> jsonJson(builder)
            Typing -> registerTyping(builder)
            Internal -> registerInternal(builder)
            CallStack -> callStackGlobal(builder)
        }
    }
}

// Placeholder types for dependencies not yet ported
internal class GlobalsBuilder {
    companion object {
        fun new(): GlobalsBuilder = GlobalsBuilder()
    }

    fun with(f: (GlobalsBuilder) -> Unit): GlobalsBuilder {
        f(this)
        return this
    }
}

// Placeholder registration functions — these will be replaced when submodules are ported
private fun registerGlobals(builder: GlobalsBuilder) {}
private fun registerStruct(builder: GlobalsBuilder) {}
private fun registerNamespace(builder: GlobalsBuilder) {}
private fun registerRecord(builder: GlobalsBuilder) {}
private fun registerEnum(builder: GlobalsBuilder) {}
private fun registerSet(builder: GlobalsBuilder) {}
private fun extraMap(builder: GlobalsBuilder) {}
private fun extraFilter(builder: GlobalsBuilder) {}
private fun partialPartial(builder: GlobalsBuilder) {}
private fun extraDebug(builder: GlobalsBuilder) {}
private fun extraPrint(builder: GlobalsBuilder) {}
private fun extraPprint(builder: GlobalsBuilder) {}
private fun extraPstr(builder: GlobalsBuilder) {}
private fun extraPrepr(builder: GlobalsBuilder) {}
private fun breakpointGlobal(builder: GlobalsBuilder) {}
private fun jsonJson(builder: GlobalsBuilder) {}
private fun registerTyping(builder: GlobalsBuilder) {}
private fun registerInternal(builder: GlobalsBuilder) {}
private fun callStackGlobal(builder: GlobalsBuilder) {}

// #[cfg(test)] mod tests
// Tests are in commonTest, not here.
