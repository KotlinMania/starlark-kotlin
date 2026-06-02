// port-lint: tests src/typing/user.rs
package io.github.kotlinmania.starlark.typing

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

import io.github.kotlinmania.starlark.assert.Assert
import kotlin.test.Test

// Test types: In Rust these use #[derive] macros and #[starlark_value].
// Here we define minimal stubs to capture the test structure.

// #[derive(Debug, Display, ProvidesStaticType, Allocative, NoSerialize)]
// #[display("plant")]
// enum AbstractPlant {}
// #[starlark_value(type = "plant")]
// impl<'v> StarlarkValue<'v> for AbstractPlant { ... }

// #[derive(Debug, Display, ProvidesStaticType, Allocative, NoSerialize)]
// #[display("fruit_callable")]
// struct FruitCallable { name: String, ty_fruit_callable: Ty, ty_fruit: Ty }
// #[starlark_value(type = "fruit_callable")]
// impl<'v> StarlarkValue<'v> for FruitCallable { ... }

// #[derive(Debug, Display, ProvidesStaticType, Allocative, NoSerialize)]
// struct Fruit { name: String }
// #[starlark_value(type = "fruit")]
// impl<'v> StarlarkValue<'v> for Fruit { ... }

// #[starlark_module]
// fn globals(globals: &mut GlobalsBuilder) {
//     fn fruit(name: String) -> starlark::Result<FruitCallable> { ... }
//     fn mk_fruit() -> anyhow::Result<Fruit> { panic!("not needed in test") }
//     const Plant: StarlarkValueAsType<AbstractPlant> = StarlarkValueAsType::new();
// }

class UserTest {
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
""",
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
""",
        )
    }
}
