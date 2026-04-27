// port-lint: tests src/coerce.rs
package io.github.kotlinmania.starlark

/*
 * Copyright 2018 The Starlark in Rust Authors.
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

import io.github.kotlinmania.starlark.values.PhantomData
import io.github.kotlinmania.starlark.values.Tuple1
import kotlin.test.Test
import kotlin.test.assertEquals

class CoerceTest {
    @Test
    fun testPtrCoerce() {
        fun f(x: Tuple1<String>): Tuple1<CharSequence> {
            return coerce(x)
        }

        val x = "test"
        assertEquals(Tuple1<CharSequence>(x), f(Tuple1("test")))
    }

    @Test
    fun testCoerceTypeAndLifetimeParams() {
        //
        // storing a value that can be viewed through both Aaa and Bbb interfaces.
        val ten = IntRef(10)
        val old = StructWithLifetimeAndTypeParams<Aaa>(
            x = AaaBbb(ten),
            marker = PhantomData.new(),
        )

        val new: StructWithLifetimeAndTypeParams<Bbb> = coerce(old)
        assertEquals(10, new.x.value.value)
    }

    @Test
    fun testCoerceIsUnsound() {
        //
        // Kotlin cannot express the Rust associated-type soundness issue, but this preserves the intent:
        // a coercion that "type-checks" yet would be unsound under stronger guarantees.
        val s: Struct<UByte> = Struct(Unit)
        val c: Struct<Newtype> = coerce(s)
    }
}

private interface Aaa {
    val value: IntRef
}

private interface Bbb {
    val value: IntRef
}

private class AaaBbb(override val value: IntRef) : Aaa, Bbb

private data class StructWithLifetimeAndTypeParams<X>(
    val x: X,
    val marker: PhantomData<IntRef>,
)
private class Struct<T>(val assoc: Any?)
private class Newtype(val value: UByte)
private data class IntRef(val value: Int)
