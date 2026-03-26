// port-lint: source src/any.rs
package io.github.kotlinmania.starlark_kotlin.any

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

/** Methods that build upon the `Any` trait. */

/**
 * Provides access to the same type as `Self` but with all lifetimes dropped to `'static`
 * (including lifetimes of parameters).
 *
 * This type is usually implemented with `#[derive(ProvidesStaticType)]` in Rust.
 * In Kotlin, this is represented via [KClass] since there are no lifetime parameters.
 */
interface ProvidesStaticType {
    /** Same type as `Self` but with lifetimes dropped to `'static`. */
    val staticType: KClass<*>
}

/**
 * Like `Any`, but while `Any` requires `'static`, this version allows a lifetime parameter.
 *
 * You cannot implement this trait directly. You should instead implement [ProvidesStaticType].
 */
interface AnyLifetime {
    /** Must return the `TypeId` of `Self` but where the lifetimes are changed to `'static`. */
    fun staticTypeOf(): KClass<*>
}

// impl<'a, T: ProvidesStaticType<'a> + 'a + ?Sized> AnyLifetime<'a> for T { ... }
// Kotlin: Any ProvidesStaticType can implement AnyLifetime.
// This blanket impl is represented by having ProvidesStaticType extend AnyLifetime behavior.

// impl<'a> dyn AnyLifetime<'a> {

/** Is the value of type [T]. */
inline fun <reified T> AnyLifetime.isType(): Boolean {
    return this is T
}

/** Downcast a reference to type [T], or return `null` if it is not the right type. */
inline fun <reified T> AnyLifetime.downcastRef(): T? {
    return this as? T
}

/** Downcast a mutable reference to type [T], or return `null` if it is not the right type. */
inline fun <reified T> AnyLifetime.downcastMut(): T? {
    return this as? T
}

// }

// macro_rules! any_lifetime { ($t:ty) => { ... } }
// Kotlin: No macro needed. All Kotlin types inherently support runtime type checks.
// The any_lifetime! macro instances for primitive types (bool, u8, i32, String, etc.)
// are unnecessary in Kotlin because `is` and `as` work on all types.

// any_lifetime!(());
// any_lifetime!(bool);
// any_lifetime!(u8); ... any_lifetime!(u128);
// any_lifetime!(i8); ... any_lifetime!(i128);
// any_lifetime!(usize); any_lifetime!(isize);
// any_lifetime!(f32); any_lifetime!(f64);
// any_lifetime!(String); any_lifetime!(str);

// unsafe impl<'a, T: ProvidesStaticType<'a> + ?Sized> ProvidesStaticType<'a> for &'a T { ... }
// unsafe impl<'a, T: ProvidesStaticType<'a> + ?Sized> ProvidesStaticType<'a> for &'a mut T { ... }
// unsafe impl<'a, T: ProvidesStaticType<'a> + ?Sized> ProvidesStaticType<'a> for *const T { ... }
// unsafe impl<'a, T: ProvidesStaticType<'a> + ?Sized> ProvidesStaticType<'a> for *mut T { ... }
// unsafe impl<'a, T> ProvidesStaticType<'a> for [T] { ... }
// unsafe impl<'a, T: ProvidesStaticType<'a> + ?Sized> ProvidesStaticType<'a> for Box<T> { ... }
// unsafe impl<'a, T: ProvidesStaticType<'a> + ?Sized> ProvidesStaticType<'a> for Rc<T> { ... }
// unsafe impl<'a, T: ProvidesStaticType<'a> + ?Sized> ProvidesStaticType<'a> for Arc<T> { ... }
// unsafe impl<'a, T: ProvidesStaticType<'a>> ProvidesStaticType<'a> for Cell<T> { ... }
// unsafe impl<'a, T: ProvidesStaticType<'a>> ProvidesStaticType<'a> for UnsafeCell<T> { ... }
// unsafe impl<'a, T: ProvidesStaticType<'a>> ProvidesStaticType<'a> for RefCell<T> { ... }
// unsafe impl<'a, T> ProvidesStaticType<'a> for Option<T> { ... }
// unsafe impl<'a, T, E> ProvidesStaticType<'a> for Result<T, E> { ... }
// unsafe impl<'a, T> ProvidesStaticType<'a> for Vec<T> { ... }
// unsafe impl<'a, K, V> ProvidesStaticType<'a> for HashMap<K, V> { ... }
// unsafe impl<'a, K, V> ProvidesStaticType<'a> for BTreeMap<K, V> { ... }
// Kotlin: No blanket impls needed. Kotlin's type system handles
// generic containers (List, Map, etc.) via type erasure and reified generics.

// #[cfg(test)] mod tests { ... }
// Tests are in commonTest, not here.
