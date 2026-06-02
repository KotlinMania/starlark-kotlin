// port-lint: tests src/any.rs
package io.github.kotlinmania.starlark.any

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

import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private data class Value(
    val value: String,
) : ProvidesStaticType,
    AnyLifetime {
    override val staticType: KClass<*> get() = Value::class

    override fun staticTypeId(): KClass<*> = Value::class

    override fun staticTypeOf(): KClass<*> = Value::class
}

private data class Value2(
    val value: String,
) : ProvidesStaticType,
    AnyLifetime {
    override val staticType: KClass<*> get() = Value2::class

    override fun staticTypeId(): KClass<*> = Value2::class

    override fun staticTypeOf(): KClass<*> = Value2::class
}

private class TypeCarrier(
    private val type: KClass<*>,
) : ProvidesStaticType,
    AnyLifetime {
    override val staticType: KClass<*> get() = type

    override fun staticTypeId(): KClass<*> = type

    override fun staticTypeOf(): KClass<*> = type
}

private class Aaa : ProvidesStaticType {
    override val staticType: KClass<*> get() = Aaa::class
}

private class Bbb(
    val value: String,
) : ProvidesStaticType {
    override val staticType: KClass<*> get() = Bbb::class
}

private class Ccc<X>(
    val value: X,
) : ProvidesStaticType {
    override val staticType: KClass<*> get() = Ccc::class
}

private class LifetimeTypeConst<T>(
    val values: List<T>,
) : ProvidesStaticType {
    override val staticType: KClass<*> get() = LifetimeTypeConst::class
}

private class TypeWithConstraint<T : CharSequence>(
    val value: T,
) : ProvidesStaticType {
    override val staticType: KClass<*> get() = TypeWithConstraint::class
}

private class TypeWhichDoesNotImplementAnyLifetime

private class TypeWithStaticLifetime<T>(
    val value: T,
) : ProvidesStaticType {
    override val staticType: KClass<*> get() = TypeWithStaticLifetime::class
}

private interface My

private class FooBar<P : My>(
    val value: P,
) : ProvidesStaticType {
    override val staticType: KClass<*> get() = FooBar::class
}

internal class AnyTest {
    @Test
    fun testCanConvert() {
        fun convertValue(x: Value): Value? = x.downcastRef<Value>()

        fun convertAny(x: AnyLifetime): Value? = x.downcastRef<Value>()

        val v = Value("test")
        val v2 = Value2("test")
        assertEquals(v, convertValue(v))
        assertEquals(v, convertAny(v))
        assertNull(convertAny(v2))
    }

    @Test
    fun testAnyLifetime() {
        fun test(expected: KClass<*>, actual: AnyLifetime) {
            assertEquals(expected, actual.staticTypeId())
        }

        test(CharSequence::class, TypeCarrier(CharSequence::class))
        test(String::class, TypeCarrier(String::class))
        test(CharSequence::class, TypeCarrier(CharSequence::class))
    }

    @Test
    fun testProvidesStaticTypeId() {
        fun test(expected: KClass<*>, provider: ProvidesStaticType) {
            assertEquals(expected, provider.staticType)
        }

        test(Aaa::class, Aaa())
        test(Bbb::class, Bbb("test"))
        test(Ccc::class, Ccc("hello"))
        test(LifetimeTypeConst::class, LifetimeTypeConst(listOf(1, 2, 3)))
        test(TypeWithConstraint::class, TypeWithConstraint("world"))
        test(TypeWithStaticLifetime::class, TypeWithStaticLifetime(TypeWhichDoesNotImplementAnyLifetime()))
    }

    @Test
    fun testProvidesStaticTypeWhenTypeParameterHasBoundWithLifetime() {
        val fooBar = FooBar(object : My {})
        assertEquals(FooBar::class, fooBar.staticType)
    }
}
