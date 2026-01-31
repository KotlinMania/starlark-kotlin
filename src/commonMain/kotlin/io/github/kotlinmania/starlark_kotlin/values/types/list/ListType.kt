// port-lint: source src/values/types/list/list_type.rs
package io.github.kotlinmania.starlark_kotlin.values.types.list

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
 * A list type marker.
 *
 * [StarlarkTypeRepr] provides `list[T]`.
 * [UnpackValue] implementation verifies the types of items.
 */
class ListType<T> private constructor() {
    companion object {
        fun <T> instance(): ListType<T> = ListType()
    }
}

// Placeholder declarations for traits/interfaces not yet ported.
// These match the Rust trait signatures and will be replaced with
// actual implementations when dependencies are ported.

/**
 * Placeholder for StarlarkTypeRepr trait.
 * Corresponds to: impl<T: StarlarkTypeRepr> StarlarkTypeRepr for ListType<T>
 */
internal interface StarlarkTypeRepr<Self> {
    /**
     * Type Canonical
     * In Rust: `type Canonical = ListType<T::Canonical>;`
     */
    val canonical: Any

    /**
     * fn starlark_type_repr() -> Ty
     * In Rust: `Ty::list(T::starlark_type_repr())`
     */
    fun starlarkTypeRepr(): Ty
}

/**
 * Placeholder for Ty type.
 */
internal class Ty private constructor() {
    companion object {
        fun list(itemType: Ty): Ty = Ty()
    }
}

/**
 * StarlarkTypeRepr implementation for ListType<T> where T: StarlarkTypeRepr.
 *
 * Rust equivalent:
 * ```rust
 * impl<T: StarlarkTypeRepr> StarlarkTypeRepr for ListType<T> {
 *     type Canonical = ListType<T::Canonical>;
 *
 *     fn starlark_type_repr() -> Ty {
 *         Ty::list(T::starlark_type_repr())
 *     }
 * }
 * ```
 */
internal class ListTypeStarlarkTypeRepr<T>(
    private val tRepr: StarlarkTypeRepr<T>
) : StarlarkTypeRepr<ListType<T>> {

    override val canonical: Any
        get() = ListType.instance<Any>()

    override fun starlarkTypeRepr(): Ty {
        return Ty.list(tRepr.starlarkTypeRepr())
    }
}

/**
 * Placeholder for UnpackValue trait.
 * Corresponds to: impl<'v, T: UnpackValue<'v>> UnpackValue<'v> for ListType<T>
 */
internal interface UnpackValue<V, Self> {
    /**
     * Type Error = <T as UnpackValue<'v>>::Error
     */
    val errorType: Any

    /**
     * fn unpack_value_impl(value: Value<'v>) -> Result<Option<Self>, Self::Error>
     */
    fun unpackValueImpl(value: V): Result<Self?>
}

/**
 * Placeholder for Value type.
 */
internal class Value<V> private constructor()

/**
 * UnpackValue implementation for ListType<T> where T: UnpackValue<'v>.
 *
 * Rust equivalent:
 * ```rust
 * impl<'v, T: UnpackValue<'v>> UnpackValue<'v> for ListType<T> {
 *     type Error = <T as UnpackValue<'v>>::Error;
 *
 *     fn unpack_value_impl(value: Value<'v>) -> Result<Option<Self>, Self::Error> {
 *         match UnpackList::<UnpackAndDiscard<T>>::unpack_value_impl(value) {
 *             Ok(Some(_)) => Ok(Some(ListType {
 *                 _item: std::marker::PhantomData,
 *             })),
 *             Ok(None) => Ok(None),
 *             Err(e) => Err(e),
 *         }
 *     }
 * }
 * ```
 */
internal class ListTypeUnpackValue<V, T>(
    private val tUnpack: UnpackValue<V, T>
) : UnpackValue<V, ListType<T>> {

    override val errorType: Any
        get() = tUnpack.errorType

    override fun unpackValueImpl(value: V): Result<ListType<T>?> {
        // Placeholder implementation - requires UnpackList and UnpackAndDiscard to be ported.
        // The actual implementation will:
        // 1. Call UnpackList<UnpackAndDiscard<T>>.unpackValueImpl(value)
        // 2. Match on the result:
        //    - Ok(Some(_)) => Ok(Some(ListType.instance()))
        //    - Ok(None) => Ok(None)
        //    - Err(e) => Err(e)
        return Result.success(null)
    }
}
