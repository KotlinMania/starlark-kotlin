// port-lint: source values/typing/type_compiled/factory.rs
package io.github.kotlinmania.starlark.values.typing.typecompiled

import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.typing.typecompiled.TypeCompiled
import io.github.kotlinmania.starlark.values.typing.typecompiled.TypeCompiledImplAsStarlarkValue

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

// TypeMatcherAlloc<R> is defined in Alloc.kt (same package)
import io.github.kotlinmania.starlark.typing.TyCustom

/** Allocate a `Ty` with a `TypeMatcher` in starlark heap as `TypeCompiled`. */
class TypeCompiledFactory(
    private val heap: Heap,
    private val ty: Ty,
) : TypeMatcherAlloc<TypeCompiled> {

    override fun alloc(matcher: TypeMatcher): TypeCompiled {
        return TypeCompiled.alloc(matcher, ty, heap)
    }

    override fun custom(custom: TyCustom): TypeCompiled {
        return custom.matcherWithTypeCompiledFactory(this)
    }

    override fun fromTypeMatcherFactory(factory: TypeMatcherFactory): TypeCompiled {
        return factory.factory.typeCompiled(this)
    }

    override fun any(): TypeCompiled {
        return if (ty == Ty.any()) {
            TypeCompiled.any().toValue()
        } else {
            alloc(IsAny)
        }
    }

    override fun none(): TypeCompiled {
        return if (ty == Ty.none()) {
            val isNone = TypeCompiledImplAsStarlarkValue.allocStatic(IsNone, Ty.none())
            TypeCompiled.uncheckedNew(isNone.toFrozenValue().toValue())
        } else {
            alloc(IsNone)
        }
    }

    override fun bool(): TypeCompiled {
        return if (ty == Ty.bool()) {
            val isBool = TypeCompiledImplAsStarlarkValue.allocStatic(IsBool, Ty.bool())
            TypeCompiled.uncheckedNew(isBool.toFrozenValue().toValue())
        } else {
            alloc(IsBool)
        }
    }

    override fun int(): TypeCompiled {
        return if (ty == Ty.int()) {
            val isInt = TypeCompiledImplAsStarlarkValue.allocStatic(IsInt, Ty.int())
            TypeCompiled.uncheckedNew(isInt.toFrozenValue().toValue())
        } else {
            alloc(IsInt)
        }
    }

    override fun str(): TypeCompiled {
        return if (ty == Ty.string()) {
            val isString = TypeCompiledImplAsStarlarkValue.allocStatic(IsStr, Ty.string())
            TypeCompiled.uncheckedNew(isString.toFrozenValue().toValue())
        } else {
            alloc(IsStr)
        }
    }

    companion object {
        internal fun allocTy(ty: Ty, heap: Heap): TypeCompiled {
            return TypeCompiledFactory(heap, ty).ty(ty)
        }
    }
}
