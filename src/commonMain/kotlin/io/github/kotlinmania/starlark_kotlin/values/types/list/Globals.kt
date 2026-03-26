// port-lint: source src/values/types/list/globals.rs
package io.github.kotlinmania.starlark_kotlin.values.types.list

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

import io.github.kotlinmania.starlark_kotlin.codemap.Span
import io.github.kotlinmania.starlark_kotlin.codemap.Spanned
import io.github.kotlinmania.starlark_kotlin.environment.GlobalsBuilder
import io.github.kotlinmania.starlark_kotlin.typing.ParamSpec
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.typing.TyFunction
import io.github.kotlinmania.starlark_kotlin.typing.callable.TyCallable
import io.github.kotlinmania.starlark_kotlin.typing.error.TypingOrInternalError
import io.github.kotlinmania.starlark_kotlin.typing.function.TyCustomFunctionImpl
import io.github.kotlinmania.starlark_kotlin.typing.TyCallArgs
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap

/**
 * Custom type function implementation for the `list` constructor.
 *
 * Handles type-checking for `list()` and `list(iterable)` calls.
 *
 * Corresponds to Rust's private `ListType` struct implementing `TyCustomFunctionImpl`.
 */
internal object ListTypeFunction : TyCustomFunctionImpl {
    override fun isType(): Boolean = true

    override fun asCallable(): TyCallable = LIST_FUNCTION.callable

    /**
     * Validate a call to `list()` or `list(iterable)`.
     *
     * When an iterable argument is provided, attempts to infer the element type
     * from the iterable's type to produce a more precise return type.
     */
    override fun validateCall(
        span: Span,
        args: TyCallArgs,
        oracle: Any,
    ): Result<Ty> {
        // Validate against the list() signature.
        // If a positional argument is provided, attempt to infer the element type.
        val firstArg = args.pos.firstOrNull()
        if (firstArg != null) {
            // When we can determine the iterable's element type, return list[element_type].
            // For now, we return any_list as the default.
            return Result.success(Ty.anyList())
        }
        return Result.success(Ty.anyList())
    }
}

/** Lazy-initialized type function for the `list` constructor. */
private val LIST_FUNCTION: TyFunction by lazy {
    TyFunction.newWithTypeAttr(
        ParamSpec.posOnly(emptyList(), listOf(Ty.iter(Ty.any()))),
        Ty.anyList(),
        Ty.anyList(),
    )
}

/**
 * Register the `list` global function.
 *
 * [list](https://github.com/bazelbuild/starlark/blob/master/spec.md#list):
 * construct a list.
 *
 * `list(x)` returns a new list containing the elements of the
 * iterable sequence x.
 *
 * With no argument, `list()` returns a new empty list.
 *
 * ```starlark
 * list()        == []
 * list((1,2,3)) == [1, 2, 3]
 * ```
 *
 * Calling `list()` on a non-iterable type yields an error:
 *
 * ```starlark
 * list("strings are not iterable")  # error: not supported on type
 * ```
 *
 * Corresponds to Rust's `register_list` function with `#[starlark_module]`.
 */
internal fun registerList(globals: GlobalsBuilder) {
    // The list() function takes an optional positional argument (an iterable).
    // If no argument is provided, it returns an empty list.
    // If an iterable is provided, its elements are collected into a new list.
    // If the argument is already a list, its contents are copied efficiently.
    //
    // Registration is handled through GlobalsBuilder when the builder
    // infrastructure is fully ported.
}

/**
 * Implementation of the `list()` built-in function.
 *
 * The function is annotated in Rust with:
 * - `as_type = FrozenList` (establishes the canonical type)
 * - `speculative_exec_safe` (safe for speculative evaluation)
 * - `special_builtin_function = SpecialBuiltinFunction::List`
 * - `ty_custom_function = ListType`
 *
 * The return type in Rust is `ValueOfUnchecked<&ListRef>`, wrapping
 * the newly allocated list. In Kotlin we return a plain `Result<Value>`.
 *
 * @param a Optional iterable argument. If `null`, returns an empty list.
 * @param heap The heap on which to allocate the new list.
 * @return A new list value.
 */
internal fun listBuiltin(a: Value?, heap: Heap): Result<Value> {
    if (a != null) {
        // If the argument is already a list, copy its contents directly.
        val xs = ListRef.fromValue(a)
        if (xs != null) {
            return Result.success(heap.alloc(AllocList(xs.content())))
        }
        // Otherwise, iterate the argument and collect elements into a new list.
        val it = a.iterate(heap).getOrElse { return Result.failure(it) }
        return Result.success(heap.alloc(AllocList(it)))
    } else {
        return Result.success(heap.alloc(AllocList.EMPTY))
    }
}
