// port-lint: tests src/values/typing/type_type.rs
package io.github.kotlinmania.starlark.values.typing

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
 */

import io.github.kotlinmania.starlark.assert.Assert
import io.github.kotlinmania.starlark.environment.GlobalsBuilder
import io.github.kotlinmania.starlark.values.types.none.NoneType
import kotlin.test.Test

class TypeTypeTest {
    @Test
    fun test() {
        fun module(globals: GlobalsBuilder) {
            fun takesType(t: TypeType): Result<NoneType> {
                t.toString()
                return Result.success(NoneType)
            }
            globals.setFunction("takes_type") { args, _ ->
                takesType(args.positional<TypeType>(0))
            }
        }

        val a = Assert()
        a.globalsAdd(::module)
        a.pass("takes_type(int)")
        a.pass("takes_type(list[str] | None)")
        a.fail(
            "takes_type(1)",
            "Type of parameter `_t` doesn't match, expected `type`,",
        )
    }
}
