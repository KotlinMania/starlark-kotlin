// port-lint: source src/values/types/namespace/globals.rs
package io.github.kotlinmania.starlark_kotlin.values.types.namespace

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

// Placeholder types until the actual implementations are ported
expect class GlobalsBuilder

expect class Arguments<V_> {
    fun <A_> noPositionalArgs(heap: Heap<V_>): Result<Unit>
    fun <A_> namesMap(): Result<Map<String, Value<V_>>>
}

expect class Heap<V_>

expect class Value<V_>

/**
 * Register namespace-related global functions.
 *
 * This is the Kotlin port of the Rust `#[starlark_module]` annotated function.
 * The macro in Rust generates code to register these globals; in Kotlin, we
 * implement this explicitly as a regular function.
 */
internal fun registerNamespace(builder: GlobalsBuilder) {
    // The namespace function would be registered here.
    // In Rust, the #[starlark_module] macro generates:
    // - Function registration with special attributes:
    //   - ty_custom_function = TyNamespaceFunction
    //   - as_type = FrozenNamespace
    // - The function implementation that:
    //   1. Validates no positional args
    //   2. Gets named arguments
    //   3. Wraps values in MaybeDocHiddenValue
    //   4. Creates and returns a Namespace

    // This will be implemented when GlobalsBuilder API is ported
}

/**
 * Implementation of the namespace() builtin function.
 *
 * Creates a namespace from keyword arguments.
 * Corresponds to the namespace function in the Rust source.
 */
internal fun <V_> namespace(args: Arguments<V_>, heap: Heap<V_>): Result<Namespace<V_>> {
    args.noPositionalArgs(heap).getOrElse { return Result.failure(it) }

    val namesMap = args.namesMap().getOrElse { return Result.failure(it) }

    val fields = namesMap.mapValues { (_, v) ->
        MaybeDocHiddenValue(
            value = v,
            docHidden = false
        )
    }

    return Result.success(Namespace.new(fields))
}
