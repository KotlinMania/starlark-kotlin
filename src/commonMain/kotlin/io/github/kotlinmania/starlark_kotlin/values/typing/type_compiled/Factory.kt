// port-lint: source src/values/typing/type_compiled/factory.rs
package io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.factory

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

// Placeholder types referenced from other modules
// These will be replaced with real imports as the port progresses
class Ty(private val kind: String = "") {
    companion object {
        fun any(): Ty = Ty("any")
        fun none(): Ty = Ty("none")
        fun bool(): Ty = Ty("bool")
        fun int(): Ty = Ty("int")
        fun string(): Ty = Ty("string")
    }
    override fun equals(other: Any?): Boolean = other is Ty && kind == other.kind
    override fun hashCode(): Int = kind.hashCode()
    fun clone(): Ty = Ty(kind)
}

// TODO: stub - TyCustom needs real import
class TyCustom {
    fun matcherWithTypeCompiledFactory(factory: TypeCompiledFactory): TypeCompiled {
        return factory.alloc(IsAny)
    }
}

// TODO: stub - Value needs real import
class Value
// TODO: stub - Heap needs real import
class Heap

// TODO: stub - TypeMatcher needs real import
interface TypeMatcher
// TODO: stub - IsAny needs real import
object IsAny : TypeMatcher
// TODO: stub - IsNone needs real import
object IsNone : TypeMatcher
// TODO: stub - IsBool needs real import
object IsBool : TypeMatcher
// TODO: stub - IsInt needs real import
object IsInt : TypeMatcher
// TODO: stub - IsStr needs real import
object IsStr : TypeMatcher

class TypeCompiled(private val value: Value = Value()) {
    companion object {
        fun alloc(matcher: TypeMatcher, ty: Ty, heap: Heap): TypeCompiled = TypeCompiled()
        fun any(): TypeCompiled = TypeCompiled()
        fun uncheckedNew(value: Value): TypeCompiled = TypeCompiled(value)
    }
    fun toValue(): TypeCompiled = this
}

class TypeCompiledImplAsStarlarkValue<T : TypeMatcher>(val matcher: T, val ty: Ty) {
    companion object {
        fun <T : TypeMatcher> allocStatic(matcher: T, ty: Ty): TypeCompiledImplAsStarlarkValue<T> =
            TypeCompiledImplAsStarlarkValue(matcher, ty)
    }
    fun toFrozenValue(): FrozenValue = FrozenValue()
}

// TODO: stub - FrozenValue needs real import
class FrozenValue {
    fun toValue(): Value = Value()
}

class AllocStaticSimple<T>(val value: T) {
    fun toFrozenValue(): FrozenValue = FrozenValue()
}

// TODO: stub - TypeMatcherFactory needs real import
class TypeMatcherFactory(val factory: TypeMatcherFactoryInner)
class TypeMatcherFactoryInner {
    fun typeCompiled(factory: TypeCompiledFactory): TypeCompiled = TypeCompiled()
}

interface TypeMatcherAlloc {
    fun <T : TypeMatcher> alloc(matcher: T): TypeCompiled
    fun custom(custom: TyCustom): TypeCompiled
    fun fromTypeMatcherFactory(factory: TypeMatcherFactory): TypeCompiled
    fun any(): TypeCompiled
    fun none(): TypeCompiled
    fun bool(): TypeCompiled
    fun int(): TypeCompiled
    fun str(): TypeCompiled
    fun ty(ty: Ty): TypeCompiled { return any() }
}

/// Allocate a `Ty` with a `TypeMatcher` in starlark heap as `TypeCompiled`.
class TypeCompiledFactory(
    private val heap: Heap,
    private val ty: Ty,
) : TypeMatcherAlloc {

    override fun <T : TypeMatcher> alloc(matcher: T): TypeCompiled {
        return TypeCompiled.alloc(matcher, ty.clone(), heap)
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
