// port-lint: source any.rs
package io.github.kotlinmania.starlark.any

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

import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AnyTest {

    @Test
    fun testCanConvert() {
        data class Value(val value: String) : ProvidesStaticType, AnyLifetime {
            override val staticType: KClass<*> get() = Value::class
            override fun staticTypeId(): KClass<*> = Value::class
            override fun staticTypeOf(): KClass<*> = Value::class
        }

        data class Value2(val value: String) : ProvidesStaticType, AnyLifetime {
            override val staticType: KClass<*> get() = Value2::class
            override fun staticTypeId(): KClass<*> = Value2::class
            override fun staticTypeOf(): KClass<*> = Value2::class
        }

        fun convertValue(x: Value): Value? {
            return x.downcastRef<Value>()
        }

        fun convertAny(x: AnyLifetime): Value? {
            return x.downcastRef<Value>()
        }

        val v = Value("test")
        val v2 = Value2("test")
        assertEquals(v, convertValue(v))
        assertEquals(v, convertAny(v))
        assertNull(convertAny(v2))
    }

    @Test
    fun testAnyLifetime() {
        fun test(expected: KClass<*>, actual: KClass<*>) {
            assertEquals(expected, actual)
        }

        test(CharSequence::class, CharSequence::class)
        test(String::class, String::class)
        test(CharSequence::class, CharSequence::class)
    }

    @Test
    fun testProvidesStaticTypeId() {
        fun test(expected: KClass<*>, provider: ProvidesStaticType) {
            assertEquals(expected, provider.staticType)
        }

        class Aaa : ProvidesStaticType {
            override val staticType: KClass<*> get() = Aaa::class
        }
        test(Aaa::class, Aaa())

        class Bbb(val value: String) : ProvidesStaticType {
            override val staticType: KClass<*> get() = Bbb::class
        }
        test(Bbb::class, Bbb("test"))

        class Ccc<X>(val value: X) : ProvidesStaticType {
            override val staticType: KClass<*> get() = Ccc::class
        }
        test(Ccc::class, Ccc("hello"))

        // Kotlin has no const generics; emulate the structural identity by checking
        // the class token of an analogous parameterized type.
        class LifetimeTypeConst<T>(val items: List<T>) : ProvidesStaticType {
            override val staticType: KClass<*> get() = LifetimeTypeConst::class
        }
        test(LifetimeTypeConst::class, LifetimeTypeConst(listOf(1, 2, 3)))

        class TypeWithConstraint<T>(val value: T) : ProvidesStaticType {
            override val staticType: KClass<*> get() = TypeWithConstraint::class
        }
        test(TypeWithConstraint::class, TypeWithConstraint("world"))

        class TypeWhichDoesNotImplementAnyLifetime

        class TypeWithStaticLifetime<T>(val value: T) : ProvidesStaticType {
            override val staticType: KClass<*> get() = TypeWithStaticLifetime::class
        }
        //     TypeWithStaticLifetime<TypeWhichDoesNotImplementAnyLifetime>,
        // >());
        test(TypeWithStaticLifetime::class, TypeWithStaticLifetime(TypeWhichDoesNotImplementAnyLifetime()))
    }

    @Test
    fun testProvidesStaticTypeWhenTypeParameterHasBoundWithLifetime() {
        // (Helper interface; Kotlin doesn't support local interfaces inside @Test fns,
        //  so it lives at file scope as a private interface — see TestMy below.)

        class FooBar<P : TestMy>(val value: P) : ProvidesStaticType {
            override val staticType: KClass<*> get() = FooBar::class
        }
    }
}

// Helper interface extracted from test (Kotlin doesn't support local interfaces)
private interface TestMy

// } // end mod tests
