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

import kotlin.reflect.KClass

/**
 * Provides the stable runtime type used for dynamic type checks.
 *
 * Implementations must return the type that should be used for erased runtime
 * comparisons, independent of instance state.
 */
interface ProvidesStaticType {
    /**
     * Stable type identity for this value.
     */
    val staticType: KClass<*>
}

/**
 * Any [ProvidesStaticType] value can expose object-safe type identity through [AnyLifetime].
 */

/**
 * Object-safe runtime type identity.
 */
interface AnyLifetime {
    /**
     * Static type identity for this implementation.
     */
    fun staticTypeId(): KClass<*>

    /**
     * Runtime type identity for this value. Must be consistent with [staticTypeId].
     */
    fun staticTypeOf(): KClass<*>
}

/**
 * A bound required by [AnyLifetime] for sealing it.
 */
interface ProvidesStaticTypeSealed

/**
 * Is the value of type [T].
 */
internal inline fun <reified T> AnyLifetime.isType(): Boolean = this.staticTypeOf() == T::class

/**
 * Downcast a reference to type [T], or return `null` if it is not the
 * right type.
 */
internal inline fun <reified T> AnyLifetime.downcastRef(): T? {
    if (!this.isType<T>()) return null
    return if (this is T) this else null
}

/**
 * Downcast a mutable reference to type [T], or return `null` if it is not
 * the right type.
 */
internal inline fun <reified T> AnyLifetime.downcastMut(): T? {
    if (!this.isType<T>()) return null
    return if (this is T) this else null
}

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

class RefStaticType<T : ProvidesStaticType>(
    val inner: T,
) : ProvidesStaticType {
    override val staticType: KClass<*> get() = inner.staticType
}

class MutRefStaticType<T : ProvidesStaticType>(
    val inner: T,
) : ProvidesStaticType {
    override val staticType: KClass<*> get() = inner.staticType
}

class ConstPtrStaticType<T : ProvidesStaticType>(
    val inner: T,
) : ProvidesStaticType {
    override val staticType: KClass<*> get() = inner.staticType
}

class MutPtrStaticType<T : ProvidesStaticType>(
    val inner: T,
) : ProvidesStaticType {
    override val staticType: KClass<*> get() = inner.staticType
}

class SliceStaticType<T : ProvidesStaticType>(
    val inner: T,
) : ProvidesStaticType {
    override val staticType: KClass<*> get() = List::class
}

class BoxStaticType<T : ProvidesStaticType>(
    val inner: T,
) : ProvidesStaticType {
    override val staticType: KClass<*> get() = inner.staticType
}

class RcStaticType<T : ProvidesStaticType>(
    val inner: T,
) : ProvidesStaticType {
    override val staticType: KClass<*> get() = inner.staticType
}

class ArcStaticType<T : ProvidesStaticType>(
    val inner: T,
) : ProvidesStaticType {
    override val staticType: KClass<*> get() = inner.staticType
}

class CellStaticType<T : ProvidesStaticType>(
    val inner: T,
) : ProvidesStaticType {
    override val staticType: KClass<*> get() = inner.staticType
}

class UnsafeCellStaticType<T : ProvidesStaticType>(
    val inner: T,
) : ProvidesStaticType {
    override val staticType: KClass<*> get() = inner.staticType
}

class RefCellStaticType<T : ProvidesStaticType>(
    val inner: T,
) : ProvidesStaticType {
    override val staticType: KClass<*> get() = inner.staticType
}

class OptionStaticType<T : ProvidesStaticType>(
    val inner: T,
) : ProvidesStaticType {
    override val staticType: KClass<*> get() = inner.staticType
}

class ResultStaticType<T : ProvidesStaticType, E : ProvidesStaticType>(
    val okType: T,
    val errType: E,
) : ProvidesStaticType {
    override val staticType: KClass<*> get() = Result::class
}

class VecStaticType<T : ProvidesStaticType>(
    val inner: T,
) : ProvidesStaticType {
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
