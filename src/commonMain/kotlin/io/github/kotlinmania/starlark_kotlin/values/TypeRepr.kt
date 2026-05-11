<<<<<<< HEAD:src/commonMain/kotlin/io/github/kotlinmania/starlark/values/TypeRepr.kt
// port-lint: source values/type_repr.rs
package io.github.kotlinmania.starlark.values
=======
// port-lint: source src/values/type_repr.rs
package io.github.kotlinmania.starlark_kotlin.values
>>>>>>> origin/main:src/commonMain/kotlin/io/github/kotlinmania/starlark_kotlin/values/TypeRepr.kt

/*
 * Copyright 2018 The Starlark in Rust Authors.
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
 * Trait and default implementations of a trait that will show starlark type annotations for a
 * given type.
 */

import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.values.layout.typed.StarlarkStr
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.types.none.NoneType
import io.github.kotlinmania.starlark_kotlin.values.types.list.ListType

/**
 * Provides a starlark type representation, even if StarlarkValue is not implemented.
 *
 * # Sealed-class implementations
 *
 * `StarlarkTypeRepr` can be implemented on sealed classes whose subclasses each have a single
 * field, for example:
 *
 * ```
 * sealed class IntOrString : StarlarkTypeRepr {
 *     data class Int(val value: kotlin.Int) : IntOrString()
 *     data class Str(val value: String) : IntOrString()
 * }
 * ```
 *
 * It emits type `int | str`.
 *
 * This interface is useful in combination with [UnpackValue].
 */
interface StarlarkTypeRepr {
<<<<<<< HEAD:src/commonMain/kotlin/io/github/kotlinmania/starlark/values/TypeRepr.kt
    /**
     * Different Kotlin types representing the same Starlark type.
     *
     * For example, `Boolean` and `StarlarkBool` represent the same Starlark type `bool`.
     */
=======
    /** The representation of a type that a user would use verbatim in starlark type annotations. */
>>>>>>> origin/main:src/commonMain/kotlin/io/github/kotlinmania/starlark_kotlin/values/TypeRepr.kt
    fun starlarkTypeRepr(): Ty
}

/**
 * A set used just for display purposes.
 *
 * `SetOf` requires `Unpack` to be implemented, and `Set` does not take type parameters so
 * we need something for documentation generation.
 */
class SetType<T : StarlarkTypeRepr>(
    private val elementRepr: T,
) : StarlarkTypeRepr {
    override fun starlarkTypeRepr(): Ty {
        return Ty.set(elementRepr.starlarkTypeRepr())
    }
}

/** StarlarkTypeRepr implementation for String. */
object StringTypeRepr : StarlarkTypeRepr {
    override fun starlarkTypeRepr(): Ty {
        return Ty.string()
    }
}

/**
 * StarlarkTypeRepr implementation for Option<T> (nullable T).
 * Represents `None | T` in Starlark type annotations.
 */
class OptionTypeRepr<T : StarlarkTypeRepr>(
    private val inner: T,
) : StarlarkTypeRepr {
    override fun starlarkTypeRepr(): Ty {
        return Ty.union2(NoneType.starlarkTypeRepr(), inner.starlarkTypeRepr())
    }
}

/** StarlarkTypeRepr implementation for List<T>. */
class ListTypeRepr<T : StarlarkTypeRepr>(
    private val element: T,
) : StarlarkTypeRepr {
    override fun starlarkTypeRepr(): Ty {
        return ListType.starlarkTypeRepr(element.starlarkTypeRepr())
    }
}

/**
 * StarlarkTypeRepr implementation for Either<TLeft, TRight>.
 * Represents `TLeft | TRight` in Starlark type annotations.
 */
class EitherTypeRepr<TLeft : StarlarkTypeRepr, TRight : StarlarkTypeRepr>(
    private val left: TLeft,
    private val right: TRight,
) : StarlarkTypeRepr {
    override fun starlarkTypeRepr(): Ty {
        return Ty.union2(left.starlarkTypeRepr(), right.starlarkTypeRepr())
    }
}

/**
 * Derive macros generate a reference to this method to be able to get the `type_repr` of types
 * they can't name.
 */
fun <T : StarlarkTypeRepr> typeReprFromAttrImpl(
    f: (Any?, Heap) -> Result<T>,
    instance: T,
): Ty {
    return instance.starlarkTypeRepr()
}
