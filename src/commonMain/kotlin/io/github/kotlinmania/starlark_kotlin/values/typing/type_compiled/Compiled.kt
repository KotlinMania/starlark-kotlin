// port-lint: source src/values/typing/type_compiled/compiled.rs
package io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled

import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.TypeCompiledFactory
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.typed.StringValue
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.toValue
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.simple.allocSimple
import io.github.kotlinmania.starlark_kotlin.values.types.tuple.Tuple
import io.github.kotlinmania.starlark_kotlin.values.types.tuple.fromValue
import io.github.kotlinmania.starlark_kotlin.values.types.list.ListRef
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.AllocStaticSimple
import io.github.kotlinmania.starlark_kotlin.values.types.none.NoneType
import io.github.kotlinmania.starlark_kotlin.collections.StarlarkHasher
import io.github.kotlinmania.starlark_kotlin.values.demand.Demand
import io.github.kotlinmania.starlark_kotlin.environment.Methods
import io.github.kotlinmania.starlark_kotlin.environment.MethodsBuilder
import io.github.kotlinmania.starlark_kotlin.environment.MethodsStatic

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

// Real types should be imported from their respective packages

sealed class TypingError(message: String) : Exception(message) {
    /// The value does not have the specified type
    class TypeAnnotationMismatch(
        val value: String,
        val typeName: String,
        val typeAnnotation: String,
        val context: String,
    ) : TypingError("Value `$value` of type `$typeName` does not match the type annotation `$typeAnnotation` for $context")

    /// The given type annotation does not represent a type
    class InvalidTypeAnnotation(val typeName: String) : TypingError("Type `$typeName` is not a valid type annotation")

    data object Dict : TypingError("`{A: B}` cannot be used as type, perhaps you meant `dict[A, B]`")

    data object List : TypingError("`[X]` cannot be used as type, perhaps you meant `list[X]`")

    /// The given type annotation does not exist, but the user might have forgotten quotes around it
    class PerhapsYouMeant(
        val found: String,
        val suggestion: String,
    ) : TypingError("Found `$found` instead of a valid type annotation. Perhaps you meant \"$suggestion\"?")

    class ValueDoesNotMatchType(
        val reason: String,
        val typeName: String,
        val typeAnnotation: String,
    ) : TypingError("Value of type `$typeName` does not match type `$typeAnnotation`: $reason")

    class StringLiteralNotAllowed(
        val literal: String,
    ) : TypingError("String literals are not allowed in type expressions: `$literal`")
}

/// Trait for compiled type expressions that can be used dynamically.
interface TypeCompiledDyn {
    fun asTyDyn(): Ty
    fun isRuntimeWildcardDyn(): Boolean
    fun toFrozenDyn(heap: FrozenHeap): TypeCompiled
}

/// A compiled type expression wrapped as a Starlark value with a type matcher.
class TypeCompiledImplAsStarlarkValue<T : TypeMatcher>(
    internal val typeCompiledImpl: T,
    internal val ty: Ty,
) : TypeCompiledDyn {

    override fun asTyDyn(): Ty = ty

    override fun isRuntimeWildcardDyn(): Boolean = typeCompiledImpl.isWildcard()

    override fun toFrozenDyn(heap: FrozenHeap): TypeCompiled {
        return TypeCompiled(heap.allocSimple(this))
    }

    fun typeMatchesValue(value: Value): Boolean {
        return typeCompiledImpl.matches(value)
    }

    fun provide(demand: Demand) {
        // demand.provideRefStatic<TypeCompiledDyn>(this)
    }

    fun writeHash(hasher: StarlarkHasher): kotlin.Result<Unit> {
        // Hash::hash(&self.ty, hasher)
        return kotlin.Result.success(Unit)
    }

    fun evalType(): Ty? {
        return ty
    }

    fun getMethods(): Methods? {
        return MethodsStatic().methods(::typeCompiledMethods)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TypeCompiledImplAsStarlarkValue<*>) return false
        return ty == other.ty
    }

    override fun hashCode(): Int = ty.hashCode()

    override fun toString(): String = ty.toString()

    companion object {
        fun <T : TypeMatcher> allocStatic(
            imp: T,
            ty: Ty,
        ): AllocStaticSimple<TypeCompiledImplAsStarlarkValue<T>> {
            return AllocStaticSimple(TypeCompiledImplAsStarlarkValue(imp, ty))
        }
    }
}

/// Dummy type matcher used as a canonical type.
class DummyTypeMatcher : TypeMatcher {
    override fun matches(value: Value): Boolean {
        throw IllegalStateException("unreachable")
    }

    override fun isWildcard(): Boolean = false

    override fun hashCode(): Int = 0
    override fun equals(other: Any?): Boolean = other is DummyTypeMatcher
}

/// True iff the value matches this type.
fun typeCompiledMatches(thisValue: Value, value: Value): Boolean {
    return thisValue.getRef().typeMatchesValue(value)
}

/// Error if the value does not match this type.
fun typeCompiledCheckMatches(thisValue: Value, value: Value): NoneType {
    if (!thisValue.getRef().typeMatchesValue(value)) {
        throw TypingError.ValueDoesNotMatchType(
            value.toRepr(),
            value.getType(),
            TypeCompiled(thisValue).toString(),
        )
    }
    return NoneType.INSTANCE
}

/// Methods for compiled type values.
fun typeCompiledMethods(methods: MethodsBuilder) {
    // methods.addMethod("matches", ::typeCompiledMatches)
    // methods.addMethod("check_matches", ::typeCompiledCheckMatches)
}

/// Wrapper for a [Value] that acts like a runtime type matcher.
class TypeCompiled(
    /// Value is `TypeCompiledImplAsStarlarkValue`.
    private val inner: Value,
) {
    internal fun uncheckedNew(value: Value): TypeCompiled = TypeCompiled(value)

    private fun downcast(): TypeCompiledDyn {
        return inner.requestValue<TypeCompiledDyn>()
            ?: throw IllegalStateException("Not TypeCompiledImpl (internal error)")
    }

    /// Check if given value matches this type.
    fun matches(value: Value): Boolean {
        return inner.toValue().getRef().typeMatchesValue(value)
    }

    /// Get the typechecker type for this runtime type.
    fun asTy(): Ty {
        return downcast().asTyDyn()
    }

    /// True if `TypeCompiled` matches any type at runtime.
    /// However, compile-time/lint typechecker may still check the type.
    internal fun isRuntimeWildcard(): Boolean {
        return downcast().isRuntimeWildcardDyn()
    }

    private fun checkTypeError(value: Value, argName: String?): kotlin.Result<Unit> {
        return kotlin.Result.failure(
            TypingError.TypeAnnotationMismatch(
                value.toStr(),
                value.getType(),
                this.toString(),
                when (argName) {
                    null -> "return type"
                    else -> "argument `$argName`"
                },
            )
        )
    }

    internal fun checkType(value: Value, argName: String?): kotlin.Result<Unit> {
        return if (matches(value)) {
            kotlin.Result.success(Unit)
        } else {
            checkTypeError(value, argName)
        }
    }

    fun toValue(): TypeCompiled = TypeCompiled(inner.toValue())

    fun toInner(): Value = inner

    fun writeHash(hasher: StarlarkHasher): kotlin.Result<Unit> {
        return inner.toValue().writeHash(hasher)
    }

    fun typeEquals(other: TypeCompiled): kotlin.Result<Boolean> {
        return inner.toValue().equals(other.inner.toValue())
    }

    /// Reallocate the type in a frozen heap.
    fun toFrozen(heap: FrozenHeap): TypeCompiled {
        val frozen = inner.toValue().unpackFrozen()
        return if (frozen != null) {
            TypeCompiled(frozen)
        } else {
            toValue().downcast().toFrozenDyn(heap)
        }
    }

    override fun hashCode(): Int {
        val h = inner.toValue().getHash()
        return if (h.isSuccess) h.getOrThrow() else 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TypeCompiled) return false
        return inner.toValue().equals(other.inner.toValue()).getOrDefault(false)
    }

    override fun toString(): String {
        return try {
            val t = downcast()
            t.asTyDyn().toString()
        } catch (_: Exception) {
            // This is unreachable, but we should not panic in toString.
            inner.toString()
        }
    }

    companion object {
        fun uncheckedNew(value: Value): TypeCompiled = TypeCompiled(value)

        internal fun alloc(
            typeCompiledImpl: TypeMatcher,
            ty: Ty,
            heap: Heap,
        ): TypeCompiled {
            return TypeCompiled(heap.allocSimple(TypeCompiledImplAsStarlarkValue(typeCompiledImpl, ty)))
        }

        internal fun typeListOf(t: TypeCompiled, heap: Heap): TypeCompiled {
            return TypeCompiledFactory.allocTy(Ty.list(t.asTy()), heap)
        }

        internal fun typeSetOf(t: TypeCompiled, heap: Heap): TypeCompiled {
            return TypeCompiledFactory.allocTy(Ty.set(t.asTy()), heap)
        }

        internal fun typeAnyOfTwo(t0: TypeCompiled, t1: TypeCompiled, heap: Heap): TypeCompiled {
            val ty = Ty.union2(t0.asTy(), t1.asTy())
            return TypeCompiledFactory.allocTy(ty, heap)
        }

        internal fun typeAnyOf(ts: kotlin.collections.List<TypeCompiled>, heap: Heap): TypeCompiled {
            val ty = Ty.unions(ts.map { it.asTy() })
            return TypeCompiledFactory.allocTy(ty, heap)
        }

        internal fun typeDictOf(kt: TypeCompiled, vt: TypeCompiled, heap: Heap): TypeCompiled {
            val ty = Ty.dict(kt.asTy(), vt.asTy())
            return TypeCompiledFactory.allocTy(ty, heap)
        }

        /// Parse `[t1, t2, ...]` as type.
        private fun fromList(t: ListRef, heap: Heap): TypeCompiled {
            val content = t.content()
            return when {
                content.isEmpty() || content.size == 1 -> throw TypingError.List
                else -> {
                    // A union type, can match any
                    val ts = content.map { new(it, heap) }
                    typeAnyOf(ts, heap)
                }
            }
        }

        internal fun fromTy(ty: Ty, heap: Heap): TypeCompiled {
            return TypeCompiledFactory.allocTy(ty, heap)
        }

        /// Evaluate type annotation at runtime.
        fun new(ty: Value, heap: Heap): TypeCompiled {
            val s = StringValue.new(ty)
            if (s != null) {
                throw TypingError.StringLiteralNotAllowed(s.toString())
            }
            if (ty.isNone()) {
                return TypeCompiledFactory.allocTy(Ty.none(), heap)
            }
            val tuple = Tuple.fromValue(ty)
            if (tuple != null) {
                val elems = tuple.content().map { new(it, heap).asTy() }
                return fromTy(Ty.tuple(elems), heap)
            }
            val list = ListRef.fromValue(ty)
            if (list != null) {
                return fromList(list, heap)
            }
            if (ty.requestValue<TypeCompiledDyn>() != null) {
                // This branch is optimization: TypeCompiledAsStarlarkValue implements eval_type,
                // but this branch avoids copying the type.
                return TypeCompiled(ty)
            }
            val evalTy = ty.getRef().evalType()
            if (evalTy != null) {
                return fromTy(evalTy, heap)
            }
            throw invalidTypeAnnotation(ty, heap)
        }

        /// Evaluate type annotation at runtime (frozen).
        internal fun newFrozen(ty: FrozenValue, frozenHeap: FrozenHeap): TypeCompiled {
            return Heap.temp { heap ->
                val compiled = new(ty.toValue(), heap)
                compiled.toFrozen(frozenHeap)
            }
        }

        /// `typing.Any`.
        fun any(): TypeCompiled {
            val anything = TypeCompiledImplAsStarlarkValue.allocStatic(IsAny(), Ty.any())
            return uncheckedNew(anything.toFrozenValue())
        }
    }
}

private fun invalidTypeAnnotation(ty: Value, heap: Heap): TypingError {
    if (DictRef.fromValue(ty) != null) {
        return TypingError.Dict
    }
    if (ListRef.fromValue(ty) != null) {
        return TypingError.List
    }
    val attrResult = ty.getAttr("type", heap)
    if (attrResult.isSuccess) {
        val attrValue = attrResult.getOrNull()
        if (attrValue != null) {
            val name = attrValue.unpackStr()
            if (name != null) {
                return TypingError.PerhapsYouMeant(ty.toStr(), name)
            }
        }
    }
    return TypingError.InvalidTypeAnnotation(ty.toStr())
}
