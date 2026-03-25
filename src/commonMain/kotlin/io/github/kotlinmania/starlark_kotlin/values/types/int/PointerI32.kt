// port-lint: source src/values/types/int/pointer_i32.rs
package io.github.kotlinmania.starlark_kotlin.values.types.int

import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.kotlinmania.starlark_kotlin.tests.assert

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
 * The result of calling `type()` on integers.
 */
const val INT_TYPE: String = "int"

/**
 * WARNING: This type isn't a real type, a pointer to this is secretly an i32.
 * Therefore, don't derive stuff on it, since it will be wrong.
 * However, `ProvidesStaticType` promises not to peek at its value, so that's fine.
 *
 * This class represents integer values stored inline using pointer tagging.
 * In Rust, this leverages raw pointer manipulation. In Kotlin, we maintain
 * the semantic interface while adapting to platform constraints.
 */
internal class PointerI32 private constructor() {

    companion object {
        /**
         * Creates a PointerI32 from a raw pointer value.
         * UB if the pointer isn't aligned, or it is zero.
         * Alignment is 1, so that's not an issue.
         * And the pointer is not zero because it has `TAG_INT` bit set.
         */
        internal fun fromRawPointerUnchecked(rawPointer: RawPointer): PointerI32 {
            if (assertionsEnabled()) {
                assert(rawPointer.isInt())
            }
            return cast.usizeToPtr(rawPointer.ptrValue())
        }

        internal fun vtable(): AValueVTable {
            return AValueVTable.new<AValueBasic<PointerI32>>()
        }

        internal inline fun <reified T : StarlarkValue<*>> typeIsPointerI32(): Boolean {
            return T::staticTypeId() == PointerI32.staticTypeId()
        }
    }

    internal fun get(): InlineInt {
        return RawPointer.newUnchecked(this as Any as ULong).unpackIntUnchecked()
    }

    internal fun asAvalueDyn(): AValueDyn {
        return AValueDyn.new(StarlarkValueRawPtr.newPointerI32(this), vtable())
    }

    /**
     * This operation is expensive, use only if you have to.
     */
    private fun toBigInt(): BigInteger {
        return get().toBigInt()
    }

    override fun equals(other: Any?): Boolean {
        return this === other
    }

    override fun hashCode(): Int {
        return System.identityHashCode(this)
    }

    override fun toString(): String {
        return get().toString()
    }
}

// Placeholder stubs for dependencies that will be ported later
// These match the pattern used in InlineInt.kt

private fun assertionsEnabled(): Boolean = false

internal object cast {
    fun <T> usizeToPtr(value: ULong): T {
        @Suppress("UNCHECKED_CAST")
        return Any() as T
    }
}

internal class RawPointer private constructor() {
    companion object {
        fun newUnchecked(value: ULong): RawPointer = RawPointer()
    }

    fun isInt(): Boolean = false
    fun ptrValue(): ULong = 0UL
    fun unpackIntUnchecked(): InlineInt = InlineInt.ZERO
}

internal class AValueDyn private constructor() {
    companion object {
        fun new(ptr: StarlarkValueRawPtr, vtable: AValueVTable): AValueDyn = AValueDyn()
    }
}

internal class AValueVTable private constructor() {
    companion object {
        inline fun <reified T> new(): AValueVTable = AValueVTable()
    }
}

internal class AValueBasic<T> private constructor()

internal class StarlarkValueRawPtr private constructor() {
    companion object {
        fun newPointerI32(ptr: PointerI32): StarlarkValueRawPtr = StarlarkValueRawPtr()
    }
}

internal interface StarlarkValue<T> {
    fun staticTypeId(): Any = this::class
}

internal fun PointerI32.staticTypeId(): Any = PointerI32::class
