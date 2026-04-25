// port-lint: source src/any.rs
package io.github.kotlinmania.starlark.any

/*
 * Copyright 2019 The Starlark in Rust Authors.
 * Copyright (c) Facebook, Inc. and its affiliates.
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

// //! Methods that build upon the [`Any` trait](std::any::Any).

import kotlin.reflect.KClass

// use std::any::TypeId;
// use std::cell::Cell;
// use std::cell::RefCell;
// use std::cell::UnsafeCell;
// use std::collections::BTreeMap;
// use std::collections::HashMap;
// use std::rc::Rc;
// use std::sync::Arc;

// pub use starlark_derive::ProvidesStaticType;

/**
 * Provides access to the same type as `Self` but with all lifetimes dropped to `'static`
 * (including lifetimes of parameters).
 *
 * This type is usually implemented with `#[derive(ProvidesStaticType)]`.
 *
 * In Kotlin, since there are no lifetime parameters, this maps to providing
 * the [KClass] of the static type for runtime type identification.
 */
// pub unsafe trait ProvidesStaticType<'a> {
//     /// Same type as `Self` but with lifetimes dropped to `'static`.
//     type StaticType: 'static + ?Sized;
// }
interface ProvidesStaticType {
    /**
     * Same type as `Self` but with lifetimes dropped to `'static`.
     *
     * The trait is unsafe because if this is implemented incorrectly,
     * the program might not work correctly.
     */
    val staticType: KClass<*>
}

/**
 * Any [ProvidesStaticType] can implement [AnyLifetime].
 *
 * Note [ProvidesStaticType] and [AnyLifetime] cannot be the same type,
 * because [AnyLifetime] needs to be object safe,
 * and [ProvidesStaticType] has type member.
 */
// impl<'a, T: ProvidesStaticType<'a> + 'a + ?Sized> AnyLifetime<'a> for T {
//     fn static_type_id() -> TypeId where Self: Sized {
//         TypeId::of::<T::StaticType>()
//     }
//     fn static_type_of(&self) -> TypeId {
//         TypeId::of::<T::StaticType>()
//     }
// }

/**
 * Like [Any][kotlin.Any], but while `Any` requires `'static`, this version
 * allows a lifetime parameter.
 *
 * Code using this trait is _unsafe_ if your implementation of the inner methods do not meet the
 * invariants listed. Therefore, it is recommended you use one of the helper macros.
 *
 * You cannot implement this trait directly. You should instead implement [ProvidesStaticType],
 * usually via the derive macro:
 *
 * ```
 * use starlark::any::ProvidesStaticType;
 * #[derive(ProvidesStaticType)]
 * struct Foo1();
 * #[derive(ProvidesStaticType)]
 * struct Foo2<'a>(&'a ());
 * ```
 *
 * If your data type is not of the form `Foo` or `Foo<'v>` you may need to implement
 * [ProvidesStaticType] directly.
 *
 * ```
 * use starlark::any::ProvidesStaticType;
 * struct Baz<T: Display>(T);
 * unsafe impl<'a, T> ProvidesStaticType<'a> for Baz<T>
 * where
 *     T: ProvidesStaticType<'a> + Display,
 *     T::StaticType: Display + Sized,
 * {
 *     type StaticType = Baz<T::StaticType>;
 * }
 * ```
 */
// pub trait AnyLifetime<'a>: seal::ProvidesStaticTypeSealed<'a> + 'a {
interface AnyLifetime {
    /**
     * Must return the `TypeId` of `Self` but where the lifetimes are changed
     * to `'static`. Must be consistent with [staticTypeOf].
     */
    // fn static_type_id() -> TypeId where Self: Sized;
    fun staticTypeId(): KClass<*>

    /**
     * Must return the `TypeId` of `Self` but where the lifetimes are changed
     * to `'static`. Must be consistent with [staticTypeId]. Must not
     * consult the `self` parameter in any way.
     */
    // fn static_type_of(&self) -> TypeId;
    fun staticTypeOf(): KClass<*>
    // Required so we can have a `dyn AnyLifetime`.
}

// mod seal {
//     /// A bound required by `AnyLifetime<'a>` for sealing it
//     pub trait ProvidesStaticTypeSealed<'a> {}
//     impl<'a, T: super::ProvidesStaticType<'a> + ?Sized> ProvidesStaticTypeSealed<'a> for T {}
// }

/**
 * A bound required by [AnyLifetime] for sealing it.
 */
interface ProvidesStaticTypeSealed

// impl<'a> dyn AnyLifetime<'a> {

/**
 * Is the value of type [T].
 */
// pub fn is<T: AnyLifetime<'a>>(&self) -> bool {
//     self.static_type_of() == T::static_type_id()
// }
inline fun <reified T> AnyLifetime.isType(): Boolean {
    return this.staticTypeOf() == T::class
}

/**
 * Downcast a reference to type [T], or return `null` if it is not the
 * right type.
 */
// pub fn downcast_ref<T: AnyLifetime<'a>>(&self) -> Option<&T> {
//     if self.is::<T>() {
//         unsafe { Some(&*(self as *const Self as *const T)) }
//     } else {
//         None
//     }
// }
inline fun <reified T> AnyLifetime.downcastRef(): T? {
    if (this.isType<T>()) {
        @Suppress("UNCHECKED_CAST")
        return this as T
    } else {
        return null
    }
}

/**
 * Downcast a mutable reference to type [T], or return `null` if it is not
 * the right type.
 */
// pub fn downcast_mut<T: AnyLifetime<'a>>(&mut self) -> Option<&mut T> {
//     if self.is::<T>() {
//         unsafe { Some(&mut *(self as *mut Self as *mut T)) }
//     } else {
//         None
//     }
// }
inline fun <reified T> AnyLifetime.downcastMut(): T? {
    if (this.isType<T>()) {
        @Suppress("UNCHECKED_CAST")
        return this as T
    } else {
        return null
    }
}

// } // end dyn AnyLifetime impl

// macro_rules! any_lifetime {
//     ( $t:ty ) => {
//         unsafe impl<'a> $crate::any::ProvidesStaticType<'a> for $t {
//             type StaticType = $t;
//         }
//     };
// }

// One of the disadvantages of AnyLifetime is there is no finite covering set of
// types so we predeclare instances for things that seem useful, but the list is
// pretty adhoc

// any_lifetime!(());
class UnitStaticType : ProvidesStaticType {
    override val staticType: KClass<*> get() = Unit::class
}

// any_lifetime!(bool);
class BoolStaticType : ProvidesStaticType {
    override val staticType: KClass<*> get() = Boolean::class
}

// any_lifetime!(u8);
class U8StaticType : ProvidesStaticType {
    override val staticType: KClass<*> get() = UByte::class
}

// any_lifetime!(u16);
class U16StaticType : ProvidesStaticType {
    override val staticType: KClass<*> get() = UShort::class
}

// any_lifetime!(u32);
class U32StaticType : ProvidesStaticType {
    override val staticType: KClass<*> get() = UInt::class
}

// any_lifetime!(u64);
class U64StaticType : ProvidesStaticType {
    override val staticType: KClass<*> get() = ULong::class
}

// any_lifetime!(u128);
class U128StaticType : ProvidesStaticType {
    override val staticType: KClass<*> get() = ULong::class
}

// any_lifetime!(usize);
class UsizeStaticType : ProvidesStaticType {
    override val staticType: KClass<*> get() = ULong::class
}

// any_lifetime!(i8);
class I8StaticType : ProvidesStaticType {
    override val staticType: KClass<*> get() = Byte::class
}

// any_lifetime!(i16);
class I16StaticType : ProvidesStaticType {
    override val staticType: KClass<*> get() = Short::class
}

// any_lifetime!(i32);
class I32StaticType : ProvidesStaticType {
    override val staticType: KClass<*> get() = Int::class
}

// any_lifetime!(i64);
class I64StaticType : ProvidesStaticType {
    override val staticType: KClass<*> get() = Long::class
}

// any_lifetime!(i128);
class I128StaticType : ProvidesStaticType {
    override val staticType: KClass<*> get() = Long::class
}

// any_lifetime!(isize);
class IsizeStaticType : ProvidesStaticType {
    override val staticType: KClass<*> get() = Long::class
}

// any_lifetime!(f32);
class F32StaticType : ProvidesStaticType {
    override val staticType: KClass<*> get() = Float::class
}

// any_lifetime!(f64);
class F64StaticType : ProvidesStaticType {
    override val staticType: KClass<*> get() = Double::class
}

// any_lifetime!(String);
class StringStaticType : ProvidesStaticType {
    override val staticType: KClass<*> get() = String::class
}

// any_lifetime!(str);
class StrStaticType : ProvidesStaticType {
    override val staticType: KClass<*> get() = CharSequence::class
}

// unsafe impl<'a, T: ProvidesStaticType<'a> + ?Sized> ProvidesStaticType<'a> for &'a T {
//     type StaticType = &'static T::StaticType;
// }
class RefStaticType<T : ProvidesStaticType>(val inner: T) : ProvidesStaticType {
    override val staticType: KClass<*> get() = inner.staticType
}

// unsafe impl<'a, T: ProvidesStaticType<'a> + ?Sized> ProvidesStaticType<'a> for &'a mut T {
//     type StaticType = &'static mut T::StaticType;
// }
class MutRefStaticType<T : ProvidesStaticType>(val inner: T) : ProvidesStaticType {
    override val staticType: KClass<*> get() = inner.staticType
}

// unsafe impl<'a, T: ProvidesStaticType<'a> + ?Sized> ProvidesStaticType<'a> for *const T {
//     type StaticType = *const T::StaticType;
// }
class ConstPtrStaticType<T : ProvidesStaticType>(val inner: T) : ProvidesStaticType {
    override val staticType: KClass<*> get() = inner.staticType
}

// unsafe impl<'a, T: ProvidesStaticType<'a> + ?Sized> ProvidesStaticType<'a> for *mut T {
//     type StaticType = *mut T::StaticType;
// }
class MutPtrStaticType<T : ProvidesStaticType>(val inner: T) : ProvidesStaticType {
    override val staticType: KClass<*> get() = inner.staticType
}

// unsafe impl<'a, T> ProvidesStaticType<'a> for [T]
// where T: ProvidesStaticType<'a>, T::StaticType: Sized,
// { type StaticType = [T::StaticType]; }
class SliceStaticType<T : ProvidesStaticType>(val inner: T) : ProvidesStaticType {
    override val staticType: KClass<*> get() = List::class
}

// unsafe impl<'a, T: ProvidesStaticType<'a> + ?Sized> ProvidesStaticType<'a> for Box<T> {
//     type StaticType = Box<T::StaticType>;
// }
class BoxStaticType<T : ProvidesStaticType>(val inner: T) : ProvidesStaticType {
    override val staticType: KClass<*> get() = inner.staticType
}

// unsafe impl<'a, T: ProvidesStaticType<'a> + ?Sized> ProvidesStaticType<'a> for Rc<T> {
//     type StaticType = Rc<T::StaticType>;
// }
class RcStaticType<T : ProvidesStaticType>(val inner: T) : ProvidesStaticType {
    override val staticType: KClass<*> get() = inner.staticType
}

// unsafe impl<'a, T: ProvidesStaticType<'a> + ?Sized> ProvidesStaticType<'a> for Arc<T> {
//     type StaticType = Arc<T::StaticType>;
// }
class ArcStaticType<T : ProvidesStaticType>(val inner: T) : ProvidesStaticType {
    override val staticType: KClass<*> get() = inner.staticType
}

// unsafe impl<'a, T: ProvidesStaticType<'a>> ProvidesStaticType<'a> for Cell<T> {
//     type StaticType = Cell<T::StaticType>;
// }
class CellStaticType<T : ProvidesStaticType>(val inner: T) : ProvidesStaticType {
    override val staticType: KClass<*> get() = inner.staticType
}

// unsafe impl<'a, T: ProvidesStaticType<'a>> ProvidesStaticType<'a> for UnsafeCell<T> {
//     type StaticType = UnsafeCell<T::StaticType>;
// }
class UnsafeCellStaticType<T : ProvidesStaticType>(val inner: T) : ProvidesStaticType {
    override val staticType: KClass<*> get() = inner.staticType
}

// unsafe impl<'a, T: ProvidesStaticType<'a>> ProvidesStaticType<'a> for RefCell<T> {
//     type StaticType = RefCell<T::StaticType>;
// }
class RefCellStaticType<T : ProvidesStaticType>(val inner: T) : ProvidesStaticType {
    override val staticType: KClass<*> get() = inner.staticType
}

// unsafe impl<'a, T> ProvidesStaticType<'a> for Option<T>
// where T: ProvidesStaticType<'a>, T::StaticType: Sized,
// { type StaticType = Option<T::StaticType>; }
class OptionStaticType<T : ProvidesStaticType>(val inner: T) : ProvidesStaticType {
    override val staticType: KClass<*> get() = inner.staticType
}

// unsafe impl<'a, T, E> ProvidesStaticType<'a> for Result<T, E>
// where T: ProvidesStaticType<'a>, T::StaticType: Sized,
//       E: ProvidesStaticType<'a>, E::StaticType: Sized,
// { type StaticType = Result<T::StaticType, E::StaticType>; }
class ResultStaticType<T : ProvidesStaticType, E : ProvidesStaticType>(
    val okType: T,
    val errType: E,
) : ProvidesStaticType {
    override val staticType: KClass<*> get() = Result::class
}

// unsafe impl<'a, T> ProvidesStaticType<'a> for Vec<T>
// where T: ProvidesStaticType<'a>, T::StaticType: Sized,
// { type StaticType = Vec<T::StaticType>; }
class VecStaticType<T : ProvidesStaticType>(val inner: T) : ProvidesStaticType {
    override val staticType: KClass<*> get() = MutableList::class
}

// unsafe impl<'a, K, V> ProvidesStaticType<'a> for HashMap<K, V>
// where K: ProvidesStaticType<'a>, K::StaticType: Sized,
//       V: ProvidesStaticType<'a>, V::StaticType: Sized,
// { type StaticType = HashMap<K::StaticType, V::StaticType>; }
class HashMapStaticType<K : ProvidesStaticType, V : ProvidesStaticType>(
    val keyType: K,
    val valueType: V,
) : ProvidesStaticType {
    override val staticType: KClass<*> get() = MutableMap::class
}

// unsafe impl<'a, K, V> ProvidesStaticType<'a> for BTreeMap<K, V>
// where K: ProvidesStaticType<'a>, K::StaticType: Sized,
//       V: ProvidesStaticType<'a>, V::StaticType: Sized,
// { type StaticType = BTreeMap<K::StaticType, V::StaticType>; }
class BTreeMapStaticType<K : ProvidesStaticType, V : ProvidesStaticType>(
    val keyType: K,
    val valueType: V,
) : ProvidesStaticType {
    override val staticType: KClass<*> get() = Map::class
}

// #[cfg(test)]
// mod tests {
//     use std::fmt::Display;
//     use super::*;
//     use crate as starlark;
// (Tests live in src/commonTest/.../any/AnyTest.kt — see SKILL.md)
// } // end mod tests
