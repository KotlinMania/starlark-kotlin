<<<<<<< HEAD:src/commonTest/kotlin/io/github/kotlinmania/starlark/CoerceTest.kt
// port-lint: tests coerce.rs
package io.github.kotlinmania.starlark
=======
// port-lint: tests src/coerce.rs
package io.github.kotlinmania.starlark_kotlin
>>>>>>> origin/main:src/commonTest/kotlin/io/github/kotlinmania/starlark_kotlin/CoerceTest.kt

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

import io.github.kotlinmania.starlark_kotlin.values.PhantomData
import io.github.kotlinmania.starlark_kotlin.values.Tuple1
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

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
        val ten = IntRef(10)
        val old = StructWithLifetimeAndTypeParams<Aaa>(
            x = Aaa(ten),
            marker = PhantomData.new(),
        )

        val new: StructWithLifetimeAndTypeParams<Bbb> = coerce(old)
        assertEquals(10, new.x.value.value)
    }

    @Test
    fun testCoerceIsUnsound() {
        val s: Struct<Unit> = Struct(Unit)
        val c: Struct<Newtype> = coerce(s)
        assertFailsWith<ClassCastException> { c.assoc as Newtype }
    }
}

private data class Aaa(val value: IntRef)
private typealias Bbb = Aaa
private data class StructWithLifetimeAndTypeParams<X>(
    val x: X,
    val marker: PhantomData<IntRef>,
)
private class Struct<T>(val assoc: Any?)
// @JvmInline not available in commonTest (JVM-only annotation)
private class Newtype(val value: UByte)
private data class IntRef(val value: Int)
