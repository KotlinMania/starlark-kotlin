// port-lint: source src/values/layout/heap/send.rs
package io.github.kotlinmania.starlark_kotlin.values.layout.heap

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

/// A trait for handling the unusual sendness requirements of starlark values.
///
/// In Rust, this module manages Send/Sync trait implementations for heap values
/// to enable safe cross-thread sharing of frozen values while allowing
/// non-thread-safe interior mutability for unfrozen values.
///
/// In Kotlin/JVM, thread safety is managed differently:
/// - All objects are inherently "sendable" across threads
/// - Thread safety is achieved through synchronization primitives
/// - The JVM's memory model handles visibility guarantees
///
/// This module provides marker interfaces that mirror the Rust traits
/// for API compatibility.

/// Marker interface corresponding to Rust's HeapSendable trait.
/// In Kotlin, this is a no-op marker since the JVM handles thread safety.
// pub trait HeapSendable<'v>: sealed_send::Sealed {}
interface HeapSendable

/// Marker interface corresponding to Rust's HeapSyncable trait.
/// In Kotlin, this is a no-op marker since the JVM handles thread safety.
// pub trait HeapSyncable<'v>: sealed_sync::Sealed {}
interface HeapSyncable

/// A helper to pass the send-if-static property through `dyn Trait`.
///
/// In Rust, this wraps a trait object to recover Send/Sync implementations.
/// In Kotlin, this is a simple wrapper since thread-safety properties
/// are not encoded in the type system the same way.
// pub struct DynStarlark<'v, T>
class DynStarlark<T>(
    private var inner: T,
) {
    companion object {
        // pub fn new(v: T) -> Self
        fun <T> new(v: T): DynStarlark<T> = DynStarlark(v)
    }

    // pub fn into_inner(self) -> T
    fun intoInner(): T = inner

    // Deref
    fun get(): T = inner

    // DerefMut
    fun set(value: T) {
        inner = value
    }

    override fun toString(): String = inner.toString()
}
