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
import io.github.kotlinmania.starlark.environment.GlobalsBuilder
import io.github.kotlinmania.starlark.eval.runtime.Arguments
import io.github.kotlinmania.starlark.eval.runtime.Evaluator
import io.github.kotlinmania.starlark.values.AllocValue
import io.github.kotlinmania.starlark.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.avalues.simple.allocSimple
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.types.TypeInstanceId
import io.github.kotlinmania.starlark.values.types.starlarkvalueastype.StarlarkValueAsType
import kotlin.test.Test

class UserTest {
    class AbstractPlant :
        StarlarkValue,
        StarlarkTypeRepr {
        override val TYPE: String get() = "plant"

        override fun starlarkTypeRepr(): Ty = getTypeStarlarkRepr()
    }

    class Fruit(
        val name: String,
    ) : StarlarkValue,
        AllocValue,
        StarlarkTypeRepr {
        override val TYPE: String get() = "fruit"

        override fun starlarkTypeRepr(): Ty = getTypeStarlarkRepr()

        override fun allocValue(heap: Heap): Value = throw Exception("not needed in test")
    }

    class FruitCallable(
        val name: String,
        val tyFruitCallable: Ty,
        val tyFruit: Ty,
    ) : StarlarkValue,
        AllocValue,
        StarlarkTypeRepr {
        override val TYPE: String get() = "fruit_callable"
        override val HAS_invoke: Boolean get() = true

        override fun typecheckerTy(): Ty? = tyFruitCallable

        override fun evalType(): Ty? = tyFruit

        override fun starlarkTypeRepr(): Ty = getTypeStarlarkRepr()

        override fun allocValue(heap: Heap): Value = heap.allocSimple(this)

        override fun invoke(
            me: Value,
            args: Arguments,
            eval: Evaluator,
        ): Result<Value> = throw Exception("not needed in tests, but typechecker requires it")
    }

    private fun globals(builder: GlobalsBuilder) {
        builder.setFunction("fruit") { args, eval ->
            val name =
                args.positionalAll().firstOrNull()?.unpackStr()
                    ?: return@setFunction Result.failure<Value>(Exception("expected string"))
            val tyFruit =
                Ty.custom(
                    TyUser
                        .new(
                            name = name,
                            base = TyStarlarkValue.new("fruit"),
                            id = TypeInstanceId.gen(),
                            params =
                                TyUserParams(
                                    supertypes = AbstractPlant().getTypeStarlarkRepr().iterUnion(),
                                ),
                        ).getOrThrow(),
                )
            val tyFruitCallable =
                Ty.custom(
                    TyUser
                        .new(
                            name = "fruit[$name]",
                            base = TyStarlarkValue.new("fruit_callable", hasInvoke = true),
                            id = TypeInstanceId.gen(),
                            params =
                                TyUserParams(
                                    callable = TyCallable.new(ParamSpec.empty(), tyFruit),
                                ),
                        ).getOrThrow(),
                )
            Result.success(eval.heap().alloc(FruitCallable(name, tyFruitCallable, tyFruit)))
        }

        builder.setFunction("mk_fruit") { _, _ ->
            Result.failure<Value>(Exception("not needed in test"))
        }

        builder.set("Plant", StarlarkValueAsType.new<AbstractPlant>(AbstractPlant()))
    }

    @Test
    fun testIntersectWithAbstractType() {
        val a = Assert()
        a.globalsAdd(::globals)
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
        a.globalsAdd(::globals)
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
