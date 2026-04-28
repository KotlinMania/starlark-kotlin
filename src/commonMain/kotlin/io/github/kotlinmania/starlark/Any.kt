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

/** Methods that build upon the [Any][kotlin.Any] trait. */

import kotlin.reflect.KClass

/**
 * Provides access to the same type as `Self` but with all lifetimes dropped to `'static`
 * (including lifetimes of parameters).
 *
 * In Kotlin, since there are no lifetime parameters, this maps to providing
 * the [KClass] of the static type for runtime type identification.
 */
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

/**
 * Like [Any][kotlin.Any], but while `Any` requires `'static`, this version
 * allows a lifetime parameter.
 *
 * Code using this trait is _unsafe_ if your implementation of the inner methods do not meet the
 * invariants listed. Therefore, it is recommended you import one of the helper macros.
 *
 * You cannot implement this trait directly. You should instead implement [ProvidesStaticType],
 * usually via the derive macro.
 */
interface AnyLifetime {
    /**
     * Must return the type identifier of `Self` but where the lifetimes are changed
     * to `'static`. Must be consistent with [staticTypeOf].
     */
    fun staticTypeId(): KClass<*>

    /**
     * Must return the type identifier of `Self` but where the lifetimes are changed
     * to `'static`. Must be consistent with [staticTypeId]. Must not
     * consult the `self` parameter in any way.
     */
    fun staticTypeOf(): KClass<*>
}

/** A bound required by [AnyLifetime] for sealing it. */
interface ProvidesStaticTypeSealed

/** Is the value of type [T]. */
inline fun <reified T> AnyLifetime.isType(): Boolean {
    return this.staticTypeOf() == T::class
}

/**
 * Downcast a reference to type [T], or return `null` if it is not the
 * right type.
 */
inline fun <reified T> AnyLifetime.downcastRef(): T? {
    if (this.isType<T>()) {
        return this as T
    } else {
        return null
    }
}

/**
 * Downcast a mutable reference to type [T], or return `null` if it is not
 * the right type.
 */
inline fun <reified T> AnyLifetime.downcastMut(): T? {
    if (this.isType<T>()) {
        return this as T
    } else {
        return null
    }
}

/**
 * One of the disadvantages of AnyLifetime is there is no finite covering set of
 * types so we predeclare instances for things that seem useful, but the list is
 * pretty adhoc.
 */

class UnitStaticType : ProvidesStaticType {
    override val staticType: KClass<*> get() = Unit::class
}

class BoolStaticType : ProvidesStaticType {
    override val staticType: KClass<*> get() = Boolean::class
}

class U8StaticType : ProvidesStaticType {
    override val staticType: KClass<*> get() = UByte::class
}

class U16StaticType : ProvidesStaticType {
    override val staticType: KClass<*> get() = UShort::class
}

class U32StaticType : ProvidesStaticType {
    override val staticType: KClass<*> get() = UInt::class
}

class U64StaticType : ProvidesStaticType {
    override val staticType: KClass<*> get() = ULong::class
}

class U128StaticType : ProvidesStaticType {
    override val staticType: KClass<*> get() = ULong::class
}

class UsizeStaticType : ProvidesStaticType {
    override val staticType: KClass<*> get() = ULong::class
}

class I8StaticType : ProvidesStaticType {
    override val staticType: KClass<*> get() = Byte::class
}

class I16StaticType : ProvidesStaticType {
    override val staticType: KClass<*> get() = Short::class
}

class I32StaticType : ProvidesStaticType {
    override val staticType: KClass<*> get() = Int::class
}

class I64StaticType : ProvidesStaticType {
    override val staticType: KClass<*> get() = Long::class
}

class I128StaticType : ProvidesStaticType {
    override val staticType: KClass<*> get() = Long::class
}

class IsizeStaticType : ProvidesStaticType {
    override val staticType: KClass<*> get() = Long::class
}

class F32StaticType : ProvidesStaticType {
    override val staticType: KClass<*> get() = Float::class
}

class F64StaticType : ProvidesStaticType {
    override val staticType: KClass<*> get() = Double::class
}

class StringStaticType : ProvidesStaticType {
    override val staticType: KClass<*> get() = String::class
}

class StrStaticType : ProvidesStaticType {
    override val staticType: KClass<*> get() = CharSequence::class
}

class RefStaticType<T : ProvidesStaticType>(val inner: T) : ProvidesStaticType {
    override val staticType: KClass<*> get() = inner.staticType
}

class MutRefStaticType<T : ProvidesStaticType>(val inner: T) : ProvidesStaticType {
    override val staticType: KClass<*> get() = inner.staticType
}

class ConstPtrStaticType<T : ProvidesStaticType>(val inner: T) : ProvidesStaticType {
    override val staticType: KClass<*> get() = inner.staticType
}

class MutPtrStaticType<T : ProvidesStaticType>(val inner: T) : ProvidesStaticType {
    override val staticType: KClass<*> get() = inner.staticType
}

class SliceStaticType<T : ProvidesStaticType>(val inner: T) : ProvidesStaticType {
    override val staticType: KClass<*> get() = List::class
}

class BoxStaticType<T : ProvidesStaticType>(val inner: T) : ProvidesStaticType {
    override val staticType: KClass<*> get() = inner.staticType
}

class RcStaticType<T : ProvidesStaticType>(val inner: T) : ProvidesStaticType {
    override val staticType: KClass<*> get() = inner.staticType
}

class ArcStaticType<T : ProvidesStaticType>(val inner: T) : ProvidesStaticType {
    override val staticType: KClass<*> get() = inner.staticType
}

class CellStaticType<T : ProvidesStaticType>(val inner: T) : ProvidesStaticType {
    override val staticType: KClass<*> get() = inner.staticType
}

class UnsafeCellStaticType<T : ProvidesStaticType>(val inner: T) : ProvidesStaticType {
    override val staticType: KClass<*> get() = inner.staticType
}

class RefCellStaticType<T : ProvidesStaticType>(val inner: T) : ProvidesStaticType {
    override val staticType: KClass<*> get() = inner.staticType
}

class OptionStaticType<T : ProvidesStaticType>(val inner: T) : ProvidesStaticType {
    override val staticType: KClass<*> get() = inner.staticType
}

class ResultStaticType<T : ProvidesStaticType, E : ProvidesStaticType>(
    val okType: T,
    val errType: E,
) : ProvidesStaticType {
    override val staticType: KClass<*> get() = Result::class
}

class VecStaticType<T : ProvidesStaticType>(val inner: T) : ProvidesStaticType {
    override val staticType: KClass<*> get() = MutableList::class
}

class HashMapStaticType<K : ProvidesStaticType, V : ProvidesStaticType>(
    val keyType: K,
    val valueType: V,
) : ProvidesStaticType {
    override val staticType: KClass<*> get() = MutableMap::class
}

class BTreeMapStaticType<K : ProvidesStaticType, V : ProvidesStaticType>(
    val keyType: K,
    val valueType: V,
) : ProvidesStaticType {
    override val staticType: KClass<*> get() = Map::class
}
