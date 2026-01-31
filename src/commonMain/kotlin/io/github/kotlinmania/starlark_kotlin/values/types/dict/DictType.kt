// port-lint: source src/values/types/dict/dict_type.rs
package io.github.kotlinmania.starlark_kotlin.values.types.dict

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

/**
 * A dict type marker.
 *
 * [StarlarkTypeRepr] provides `dict[K, V]`.
 * [UnpackValue] implementation verifies the types of entries and discards them.
 */
class DictType<K, V> private constructor() {
    companion object {
        fun <K, V> instance(): DictType<K, V> = DictType()
    }
}

// Placeholder declarations for traits/interfaces not yet ported.
// These match the Rust trait signatures and will be replaced with
// actual implementations when dependencies are ported.

/**
 * Placeholder for StarlarkTypeRepr trait.
 * Corresponds to: impl<K: StarlarkTypeRepr, V: StarlarkTypeRepr> StarlarkTypeRepr for DictType<K, V>
 */
internal interface StarlarkTypeRepr<Self> {
    /**
     * Type Canonical
     * In Rust: `type Canonical = DictType<K::Canonical, V::Canonical>;`
     */
    val canonical: Any

    /**
     * fn starlark_type_repr() -> Ty
     * In Rust: `Ty::dict(K::starlark_type_repr(), V::starlark_type_repr())`
     */
    fun starlarkTypeRepr(): Ty
}

/**
 * Placeholder for Ty type.
 */
internal class Ty private constructor() {
    companion object {
        fun dict(keyType: Ty, valueType: Ty): Ty = Ty()
    }
}

/**
 * StarlarkTypeRepr implementation for DictType<K, V> where K: StarlarkTypeRepr, V: StarlarkTypeRepr.
 *
 * Rust equivalent:
 * ```rust
 * impl<K: StarlarkTypeRepr, V: StarlarkTypeRepr> StarlarkTypeRepr for DictType<K, V> {
 *     type Canonical = DictType<K::Canonical, V::Canonical>;
 *
 *     fn starlark_type_repr() -> Ty {
 *         Ty::dict(K::starlark_type_repr(), V::starlark_type_repr())
 *     }
 * }
 * ```
 */
internal class DictTypeStarlarkTypeRepr<K, V>(
    private val kRepr: StarlarkTypeRepr<K>,
    private val vRepr: StarlarkTypeRepr<V>
) : StarlarkTypeRepr<DictType<K, V>> {

    override val canonical: Any
        get() = DictType.instance<Any, Any>()

    override fun starlarkTypeRepr(): Ty {
        return Ty.dict(kRepr.starlarkTypeRepr(), vRepr.starlarkTypeRepr())
    }
}

/**
 * Placeholder for UnpackValue trait.
 * Corresponds to: impl<'v, K: UnpackValue<'v>, V: UnpackValue<'v>> UnpackValue<'v> for DictType<K, V>
 */
internal interface UnpackValue<Val, Self> {
    /**
     * Type Error = Either<K::Error, V::Error>
     */
    val errorType: Any

    /**
     * fn unpack_value_impl(value: Value<'v>) -> Result<Option<Self>, Self::Error>
     */
    fun unpackValueImpl(value: Val): Result<Self?>
}

/**
 * Placeholder for Value type.
 */
internal class Value<V> private constructor()

/**
 * Placeholder for Either type.
 * In Rust: `use either::Either;`
 */
internal sealed class Either<L, R> {
    data class Left<L, R>(val value: L) : Either<L, R>()
    data class Right<L, R>(val value: R) : Either<L, R>()
}

/**
 * UnpackValue implementation for DictType<K, V> where K: UnpackValue<'v>, V: UnpackValue<'v>.
 *
 * Rust equivalent:
 * ```rust
 * impl<'v, K: UnpackValue<'v>, V: UnpackValue<'v>> UnpackValue<'v> for DictType<K, V> {
 *     type Error = Either<K::Error, V::Error>;
 *
 *     fn unpack_value_impl(value: Value<'v>) -> Result<Option<Self>, Self::Error> {
 *         match UnpackDictEntries::<UnpackAndDiscard<K>, UnpackAndDiscard<V>>::unpack_value_impl(
 *             value,
 *         ) {
 *             Ok(Some(_)) => Ok(Some(DictType {
 *                 k: PhantomData,
 *                 v: PhantomData,
 *             })),
 *             Ok(None) => Ok(None),
 *             Err(e) => Err(e),
 *         }
 *     }
 * }
 * ```
 */
internal class DictTypeUnpackValue<Val, K, V>(
    private val kUnpack: UnpackValue<Val, K>,
    private val vUnpack: UnpackValue<Val, V>
) : UnpackValue<Val, DictType<K, V>> {

    override val errorType: Any
        get() = Either.Left<Any, Any>(kUnpack.errorType) // Simplified representation of Either<K::Error, V::Error>

    override fun unpackValueImpl(value: Val): Result<DictType<K, V>?> {
        // Placeholder implementation - requires UnpackDictEntries and UnpackAndDiscard to be ported.
        // The actual implementation will:
        // 1. Call UnpackDictEntries<UnpackAndDiscard<K>, UnpackAndDiscard<V>>.unpackValueImpl(value)
        // 2. Match on the result:
        //    - Ok(Some(_)) => Ok(Some(DictType.instance()))
        //    - Ok(None) => Ok(None)
        //    - Err(e) => Err(e)
        return Result.success(null)
    }
}
