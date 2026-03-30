// port-lint: source src/values/types/bool/value.rs
package io.github.kotlinmania.starlark_kotlin.values.types.bool

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

import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.collections.StarlarkHashValue
import io.github.kotlinmania.starlark_kotlin.collections.StarlarkHasher
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.ValueError
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.AllocStaticSimple
import io.github.kotlinmania.starlark_kotlin.values.layout.Value

/** The result of calling type() on booleans. */
const val BOOL_TYPE: String = "bool"

/** bool value. */
@ConsistentCopyVisibility
data class StarlarkBool internal constructor(internal val _0: Boolean) : StarlarkValue {

    override val TYPE: String get() = BOOL_TYPE

    override fun toString(): String {
        return if (_0) "True" else "False"
    }

    override fun isSpecial(): Boolean {
        return true
    }

    override fun collectRepr(collector: StringBuilder) {
        // repr() for bool is quite hot, so optimise it
        if (_0) {
            collector.append("True")
        } else {
            collector.append("False")
        }
    }

    override fun toBool(): Boolean {
        return _0
    }

    override fun writeHash(hasher: StarlarkHasher): Result<Unit> {
        hasher.writeU8(if (_0) 1u else 0u)
        return Result.success(Unit)
    }

    override fun getHash(): Result<StarlarkHashValue> {
        // These constants are just two random numbers.
        return Result.success(
            StarlarkHashValue.newUnchecked(
                if (_0) {
                    0xa4acba08u
                } else {
                    0x71e8ba71u
                }
            )
        )
    }

    override fun compare(other: Value): Result<Int> {
        val otherBool = other.unpackBool()
        return if (otherBool != null) {
            Result.success(_0.compareTo(otherBool))
        } else {
            ValueError.unsupportedWith(BOOL_TYPE, "<>", other)
        }
    }

    override fun typecheckerTy(): Ty? {
        return Ty.bool()
    }

    override fun getTypeStarlarkRepr(): Ty {
        return Ty.bool()
    }
}

internal val VALUE_FALSE_TRUE: Array<AllocStaticSimple<StarlarkBool>> = arrayOf(
    AllocStaticSimple.alloc(StarlarkBool(false)),
    AllocStaticSimple.alloc(StarlarkBool(true))
)
