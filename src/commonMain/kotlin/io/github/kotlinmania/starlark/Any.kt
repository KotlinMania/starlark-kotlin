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
 * This type is usually implemented with `#[derive(ProvidesStaticType)]`.
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
 * invariants listed. Therefore, it is recommended you use one of the helper macros.
 *
 * You cannot implement this trait directly. You should instead implement `ProvidesStaticType`,
 * usually via the derive macro.
 */
interface AnyLifetime {
    /**
     * Must return the static type identifier of `Self`. Must be consistent
     * with [staticTypeOf].
     */
    fun staticTypeId(): KClass<*>

    /**
     * Must return the static type identifier of `Self`. Must be consistent
     * with [staticTypeId]. Must not consult the `self` parameter in any way.
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

