// port-lint: source tests:src/typing/user.rs
package io.github.kotlinmania.starlark.typing

/*
 * Copyright 2019 The Starlark in Rust Authors.
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

import io.github.kotlinmania.starlark.assert.Assert
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.typing.TyCallable
import io.github.kotlinmania.starlark.typing.ParamSpec
import io.github.kotlinmania.starlark.typing.TyUser
import io.github.kotlinmania.starlark.typing.TyUserParams
import io.github.kotlinmania.starlark.values.types.TypeInstanceId
import kotlin.test.Test

// Here we define minimal stubs to capture the test structure.

class UserTest {

    private fun fruit(name: String): Result<FruitCallable> {
        // Mirrors upstream `fn fruit(name: String) -> Result<FruitCallable>`.
        // The Kotlin port returns a thin holder that downstream tests treat
        // as a callable type token.
        val tyFruit = Ty.custom(
            TyUser.new(
                name,
                TyStarlarkValue.new<Fruit>(),
                TypeInstanceId.gen(),
                TyUserParams(
                    supertypes = AbstractPlant.getTypeStarlarkRepr().iterUnion().toList(),
                ),
            ).getOrThrow(),
        )
        val tyFruitCallable = Ty.custom(
            TyUser.new(
                "fruit[$name]",
                TyStarlarkValue.new<FruitCallable>(),
                TypeInstanceId.gen(),
                TyUserParams(
                    callable = TyCallable.new(ParamSpec.empty(), tyFruit),
                ),
            ).getOrThrow(),
        )
        return Result.success(FruitCallable(name, tyFruit, tyFruitCallable))
    }

    private fun mkFruit(): Result<Fruit> {
        error("not needed in test")
    }

    @Test
    fun testIntersectWithAbstractType() {
        val a = Assert()
        // a.globalsAdd(::globals)
        a.pass(
            """
Apple = fruit("apple")

def make_apple() -> Apple:
    return Apple()

def make_plant() -> Plant:
    return make_apple()
"""
        )
    }

    @Test
    fun testTyUserIntersectsWithBaseStarlarkValue() {
        val a = Assert()
        // a.globalsAdd(::globals)
        a.pass(
            """
Pear = fruit("pear")

def takes_pear(x: Pear):
    pass

def test():
    # `Pear` is `TyUser` with base `TyStarlarkValue::new::<Fruit>`.
    # `mk_fruit()` is `TyStarlarkValue::new::<Fruit>()`.
    # They should intersect.
    takes_pear(mk_fruit())
"""
        )
    }
}
