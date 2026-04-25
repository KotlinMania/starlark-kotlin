// port-lint: source src/any.rs
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

// #[cfg(test)]
// mod tests {
//     use std::fmt::Display;
//     use super::*;
//     use crate as starlark;

class AnyTest {

    // #[test]
    // fn test_can_convert() {
    @Test
    fun testCanConvert() {
        // #[derive(Debug, PartialEq, ProvidesStaticType)]
        // struct Value<'a>(&'a str);
        data class Value(val value: String) : ProvidesStaticType, AnyLifetime {
            override val staticType: KClass<*> get() = Value::class
            override fun staticTypeId(): KClass<*> = Value::class
            override fun staticTypeOf(): KClass<*> = Value::class
        }

        // #[derive(ProvidesStaticType)]
        // #[allow(dead_code)] // field `0` is never read
        // struct Value2<'a>(&'a str);
        data class Value2(val value: String) : ProvidesStaticType, AnyLifetime {
            override val staticType: KClass<*> get() = Value2::class
            override fun staticTypeId(): KClass<*> = Value2::class
            override fun staticTypeOf(): KClass<*> = Value2::class
        }

        // Changing the return type too `Value<'static>` causes a compile error.
        // fn convert_value<'a>(x: &'a Value<'a>) -> Option<&'a Value<'a>> {
        //     <dyn AnyLifetime>::downcast_ref(x)
        // }
        fun convertValue(x: Value): Value? {
            return x.downcastRef<Value>()
        }

        // fn convert_any<'p, 'a>(x: &'p dyn AnyLifetime<'a>) -> Option<&'p Value<'a>> {
        //     x.downcast_ref()
        // }
        fun convertAny(x: AnyLifetime): Value? {
            return x.downcastRef<Value>()
        }

        val v = Value("test")
        val v2 = Value2("test")
        // assert_eq!(convert_value(&v), Some(&v));
        assertEquals(v, convertValue(v))
        // assert_eq!(convert_any(&v), Some(&v));
        assertEquals(v, convertAny(v))
        // assert_eq!(convert_any(&v2), None);
        assertNull(convertAny(v2))
    }

    // #[test]
    // fn test_any_lifetime() {
    @Test
    fun testAnyLifetime() {
        // fn test<'a, A: AnyLifetime<'a>>(expected: TypeId) {
        //     assert_eq!(expected, A::static_type_id());
        // }
        fun test(expected: KClass<*>, actual: KClass<*>) {
            assertEquals(expected, actual)
        }

        // test::<&str>(TypeId::of::<&str>());
        test(CharSequence::class, CharSequence::class)
        // test::<&String>(TypeId::of::<&String>());
        test(String::class, String::class)
        // test::<Box<str>>(TypeId::of::<Box<str>>());
        test(CharSequence::class, CharSequence::class)
    }

    // #[test]
    // fn test_provides_static_type_id() {
    @Test
    fun testProvidesStaticTypeId() {
        // fn test<'a, A: AnyLifetime<'a>>(expected: TypeId) {
        //     assert_eq!(expected, A::static_type_id());
        // }
        fun test(expected: KClass<*>, provider: ProvidesStaticType) {
            assertEquals(expected, provider.staticType)
        }

        // #[derive(ProvidesStaticType)]
        // struct Aaa;
        class Aaa : ProvidesStaticType {
            override val staticType: KClass<*> get() = Aaa::class
        }
        // test::<Aaa>(TypeId::of::<Aaa>());
        test(Aaa::class, Aaa())

        // #[derive(ProvidesStaticType)]
        // #[allow(dead_code)] // field `0` is never read
        // struct Bbb<'a>(&'a str);
        class Bbb(val value: String) : ProvidesStaticType {
            override val staticType: KClass<*> get() = Bbb::class
        }
        // test::<Bbb>(TypeId::of::<Bbb<'static>>());
        test(Bbb::class, Bbb("test"))

        // #[derive(ProvidesStaticType)]
        // struct Ccc<X>(X);
        class Ccc<X>(val value: X) : ProvidesStaticType {
            override val staticType: KClass<*> get() = Ccc::class
        }
        // test::<Ccc<String>>(TypeId::of::<Ccc<String>>());
        test(Ccc::class, Ccc("hello"))

        // #[derive(ProvidesStaticType)]
        // struct LifetimeTypeConst<'a, T, const N: usize>([&'a T; N]);
        // test::<LifetimeTypeConst<i32, 3>>(TypeId::of::<LifetimeTypeConst<'static, i32, 3>>());
        // Kotlin has no const generics; emulate the structural identity by checking
        // the class token of an analogous parameterized type.
        class LifetimeTypeConst<T>(val items: List<T>) : ProvidesStaticType {
            override val staticType: KClass<*> get() = LifetimeTypeConst::class
        }
        test(LifetimeTypeConst::class, LifetimeTypeConst(listOf(1, 2, 3)))

        // #[derive(ProvidesStaticType)]
        // struct TypeWithConstraint<T: Display>(T);
        class TypeWithConstraint<T>(val value: T) : ProvidesStaticType {
            override val staticType: KClass<*> get() = TypeWithConstraint::class
        }
        // test::<TypeWithConstraint<String>>(TypeId::of::<TypeWithConstraint<String>>());
        test(TypeWithConstraint::class, TypeWithConstraint("world"))

        // struct TypeWhichDoesNotImplementAnyLifetime;
        class TypeWhichDoesNotImplementAnyLifetime

        // #[derive(ProvidesStaticType)]
        // struct TypeWithStaticLifetime<T: 'static>(T);
        class TypeWithStaticLifetime<T>(val value: T) : ProvidesStaticType {
            override val staticType: KClass<*> get() = TypeWithStaticLifetime::class
        }
        // test::<TypeWithStaticLifetime<TypeWhichDoesNotImplementAnyLifetime>>(TypeId::of::<
        //     TypeWithStaticLifetime<TypeWhichDoesNotImplementAnyLifetime>,
        // >());
        test(TypeWithStaticLifetime::class, TypeWithStaticLifetime(TypeWhichDoesNotImplementAnyLifetime()))
    }

    // #[test]
    // fn test_provides_static_type_when_type_parameter_has_bound_with_lifetime() {
    @Test
    fun testProvidesStaticTypeWhenTypeParameterHasBoundWithLifetime() {
        // trait My<'a> {}
        // (Helper interface; Kotlin doesn't support local interfaces inside @Test fns,
        //  so it lives at file scope as a private interface — see TestMy below.)

        // #[derive(ProvidesStaticType)]
        // #[allow(dead_code)] // field `0` is never read
        // struct FooBar<'x, P: My<'x>>(&'x P);
        class FooBar<P : TestMy>(val value: P) : ProvidesStaticType {
            override val staticType: KClass<*> get() = FooBar::class
        }
    }
}

// Helper interface extracted from test (Kotlin doesn't support local interfaces)
private interface TestMy

// } // end mod tests
