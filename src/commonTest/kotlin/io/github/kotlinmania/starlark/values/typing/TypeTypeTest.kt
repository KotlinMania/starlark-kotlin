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
import io.github.kotlinmania.starlark.typing.ParamSpec
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.types.none.NoneType
import io.github.kotlinmania.starlark.values.typing.ty.AbstractType
import kotlin.test.Test

class TypeTypeTest {
    @Test
    fun test() {
        fun module(globals: GlobalsBuilder) {
            fun takesType(t: TypeType): Result<NoneType> {
                t.toString()
                return Result.success(NoneType)
            }
            val TypeTypeUnpacker =
                object : io.github.kotlinmania.starlark.values.UnpackValue<TypeType> {
                    override fun starlarkTypeRepr(): Ty = AbstractType.starlarkTypeRepr()

                    override fun unpackValueImpl(value: io.github.kotlinmania.starlark.values.layout.Value): Result<TypeType?> =
                        Result.success(TypeType.unpackValue(value))
                }
            globals.setFunction(
                name = "takes_type",
                ty =
                    Ty.function(
                        ParamSpec.posOnly(listOf(AbstractType.starlarkTypeRepr())),
                        Ty.none(),
                    ),
            ) { args, _ ->
                val t = TypeTypeUnpacker.unpackNamedParam(args.positionalAll()[0], "_t")
                takesType(t)
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
