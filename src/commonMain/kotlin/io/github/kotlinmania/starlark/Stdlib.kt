// port-lint: source src/stdlib.rs
package io.github.kotlinmania.starlark

import io.github.kotlinmania.starlark.environment.GlobalsBuilder
import io.github.kotlinmania.starlark.environment.GlobalsStatic
import io.github.kotlinmania.starlark.stdlib.breakpointGlobal
import io.github.kotlinmania.starlark.stdlib.callStackGlobal
import io.github.kotlinmania.starlark.stdlib.funcs.registerGlobals
import io.github.kotlinmania.starlark.stdlib.internal.registerInternal
import io.github.kotlinmania.starlark.stdlib.partialStdlib
import io.github.kotlinmania.starlark.stdlib.registerDebug
import io.github.kotlinmania.starlark.stdlib.registerFilter
import io.github.kotlinmania.starlark.stdlib.registerJson
import io.github.kotlinmania.starlark.stdlib.registerMap
import io.github.kotlinmania.starlark.stdlib.registerPprint
import io.github.kotlinmania.starlark.stdlib.registerPrepr
import io.github.kotlinmania.starlark.stdlib.registerPrint
import io.github.kotlinmania.starlark.stdlib.registerPstr
import io.github.kotlinmania.starlark.values.types.enumeration.registerEnum
import io.github.kotlinmania.starlark.values.types.namespace.registerNamespace
import io.github.kotlinmania.starlark.values.types.record.registerRecord
import io.github.kotlinmania.starlark.values.types.set.registerSet
import io.github.kotlinmania.starlark.values.types.structs.registerStruct
import io.github.kotlinmania.starlark.values.typing.registerTyping

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

/**
 * A module with the standard function and constants that are by default in all
 * dialect of Starlark
 */

// Submodules: breakpoint, call_stack, extra, funcs, internal, json, partial

private val STANDARD_STATIC = GlobalsStatic()

/**
 * Return the default global environment, it is not yet frozen so that a caller
 * can refine it.
 *
 * For example `standardEnvironment().freeze().child("test")` create a
 * child environment of this global environment that have been frozen.
 */
internal fun standardEnvironment(): GlobalsBuilder {
    val builder = GlobalsBuilder.new()
    STANDARD_STATIC.populate({ b -> registerGlobals(b) }, builder)
    return builder
}

/** The extra library definitions available in this Starlark implementation, but not in the standard. */
enum class LibraryExtension {
    /** Definitions to support the `struct` type, the `struct()` constructor. */
    StructType,

    /** Definitions to support the `record` type, the `record()` constructor and `field()` function. */
    RecordType,

    /** Definitions to support the `enum` type, the `enum()` constructor. */
    EnumType,

    /**
     * Add a function `namespace()` which acts much like `struct()` but is clear about its
     * intended use and stricter
     */
    NamespaceType,

    /** A function `map(f, xs)` which applies `f` to each element of `xs` and returns the result. */
    Map,

    /**
     * A function `filter(f, xs)` which applies `f` to each element of `xs` and returns those for which `f` returns `True`.
     * As a special case, `filter(None, xs)` removes all `None` values.
     */
    Filter,

    /**
     * Partially apply a function, `partial(f, *args, **kwargs)` will create a function where those `args` `kwargs`
     * are already applied to `f`.
     */
    Partial,

    /**
     * Add a function `debug(x)` which shows the debug representation of a value.
     * Useful when debugging, but the output should not be considered stable.
     */
    Debug,

    /** Add a function `print(x)` which prints to stderr. */
    Print,

    /** Add a function `pprint(x)` which pretty-prints to stderr. */
    Pprint,

    /** Add a function `pstr` which is a pretty-printed version of `str`. */
    Pstr,

    /** Add a function `prepr` which is a pretty-printed version of `repr`. */
    Prepr,

    /** Add a function `breakpoint()` which will drop into a console-module evaluation prompt. */
    Breakpoint,

    /** Add a function `json()` which will generate JSON for a module. */
    Json,

    /**
     * Provides `typing.All`, `typing.Callable` etc.
     * Usually used in conjunction with `Dialect.enableTypes`.
     */
    Typing,

    /**
     * Utilities exposing starlark-rust internals.
     * These are not for production use.
     */
    Internal,

    /**
     * Add a function `call_stack()` which returns a string representation of
     * the current call stack.
     */
    CallStack,

    /** Definitions to support the `set` type, the `set()` constructor. */
    SetType,

    ;

    companion object {
        /** A list of all extensions that will be updated as new methods are added. */
        fun all(): List<LibraryExtension> = entries
    }

    private val staticInstance: GlobalsStatic by lazy { GlobalsStatic() }

    /** Add a specific extension to a `GlobalsBuilder`. */
    fun add(builder: GlobalsBuilder) {
        staticInstance.populate({ b ->
            when (this) {
                StructType -> registerStruct(b)
                NamespaceType -> registerNamespace(b)
                RecordType -> registerRecord(b)
                EnumType -> registerEnum(b)
                SetType -> registerSet(b)
                Map -> registerMap(b)
                Filter -> registerFilter(b)
                Partial -> partialStdlib(b)
                Debug -> registerDebug(b)
                Print -> registerPrint(b)
                Pprint -> registerPprint(b)
                Pstr -> registerPstr(b)
                Prepr -> registerPrepr(b)
                Breakpoint -> breakpointGlobal(b)
                Json -> registerJson(b)
                Typing -> registerTyping(b)
                Internal -> registerInternal(b)
                CallStack -> callStackGlobal(b)
            }
        }, builder)
    }
}

// Tests are in commonTest, not here.
