// port-lint: source src/values/trace.rs
package io.github.kotlinmania.starlark_kotlin.values

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

// use std::cell::Cell;
// use std::cell::OnceCell;
// use std::cell::RefCell;
// use std::cell::UnsafeCell;
// use std::marker;
// use std::sync::Arc;
// use std::sync::Mutex;
// use std::sync::atomic::AtomicBool;
// use std::sync::atomic::AtomicI8;
// use std::sync::atomic::AtomicI16;
// use std::sync::atomic::AtomicI32;
// use std::sync::atomic::AtomicI64;
// use std::sync::atomic::AtomicIsize;
// use std::sync::atomic::AtomicU8;
// use std::sync::atomic::AtomicU16;
// use std::sync::atomic::AtomicU32;
// use std::sync::atomic::AtomicU64;
// use std::sync::atomic::AtomicUsize;

// use either::Either;
// use hashbrown::HashTable;
// use starlark_map::Hashed;
// use starlark_map::small_set::SmallSet;

// use crate::collections::SmallMap;
// use crate::values::FrozenValue;
// use crate::values::Tracer;
// use crate::values::Value;

import io.github.kotlinmania.starlark_kotlin.Either
import io.github.kotlinmania.starlark_kotlin.collections.Hashed
import io.github.kotlinmania.starlark_kotlin.collections.SmallMap
import io.github.kotlinmania.starlark_kotlin.collections.SmallSet
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Tracer
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.ValueHolder
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.AtomicLong
import kotlinx.datetime.Instant

/**
 * Called by the garbage collection, and must walk over every contained [Value] in the type.
 *
 * In Rust this trait is marked `unsafe` because if you miss a nested [Value],
 * it will probably segfault.
 *
 * For most cases a derived implementation is enough:
 *
 * ```kotlin
 * class MySet(val keys: MutableList<Value>) : Trace {
 *     override fun trace(tracer: Tracer) {
 *         keys.forEach { it.trace(tracer) }
 *     }
 * }
 * ```
 */
// pub unsafe trait Trace<'v>
interface Trace {
    /**
     * Recursively "trace" the value.
     *
     * Note during trace, [Value] objects in `this` might be already special forward-objects,
     * trying to unpack these values may crash the process.
     *
     * Generally this function should not do anything except calling `trace` on the fields.
     */
    // fn trace(&mut self, tracer: &Tracer<'v>)
    fun trace(tracer: Tracer)
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for Vec<T>
fun <T : Trace> MutableList<T>.trace(tracer: Tracer) {
    this.forEach { x -> x.trace(tracer) }
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for [T]
fun <T : Trace> Array<T>.trace(tracer: Tracer) {
    this.forEach { x -> x.trace(tracer) }
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for HashTable<T>
fun <T : Trace> MutableCollection<T>.trace(tracer: Tracer) {
    this.forEach { e -> e.trace(tracer) }
}

// unsafe impl<'v, K: Trace<'v>, V: Trace<'v>> Trace<'v> for SmallMap<K, V>
fun <K : Trace, V : Trace> SmallMap<K, V>.trace(tracer: Tracer) {
    // Rust calls iter_mut_unchecked to get mutable references to keys and values.
    // In Kotlin, objects are reference types so tracing via iter() is equivalent.
    for ((k, v) in this.iter()) {
        k.trace(tracer)
        v.trace(tracer)
    }
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for SmallSet<T>
fun <T : Trace> SmallSet<T>.trace(tracer: Tracer) {
    // Rust calls iter_mut_unchecked to get mutable references.
    // In Kotlin, objects are reference types so tracing via iter() is equivalent.
    for (v in this.iter()) {
        v.trace(tracer)
    }
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for Hashed<T>
fun <T : Trace> Hashed<T>.trace(tracer: Tracer) {
    // Rust calls key_mut() for mutable access.
    // In Kotlin the key is a reference type, so key() suffices.
    this.key().trace(tracer)
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for Option<T>
fun <T : Trace> traceNullable(value: T?, tracer: Tracer) {
    if (value != null) {
        value.trace(tracer)
    }
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for RefCell<T>
// In Kotlin there is no RefCell; the value is accessed directly.
fun <T : Trace> traceRefCell(value: T, tracer: Tracer) {
    value.trace(tracer)
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for Cell<T>
// In Kotlin there is no Cell; the value is accessed directly.
fun <T : Trace> traceCell(value: T, tracer: Tracer) {
    value.trace(tracer)
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for OnceCell<T>
// OnceCell may or may not have a value, modeled as nullable.
fun <T : Trace> traceOnceCell(value: T?, tracer: Tracer) {
    if (value != null) {
        value.trace(tracer)
    }
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for UnsafeCell<T>
// In Kotlin there is no UnsafeCell; the value is accessed directly.
fun <T : Trace> traceUnsafeCell(value: T, tracer: Tracer) {
    value.trace(tracer)
}

// unsafe impl<'v, T: Trace<'v> + ?Sized> Trace<'v> for Box<T>
// In Kotlin there is no Box; the value is a reference directly.
fun <T : Trace> traceBoxed(value: T, tracer: Tracer) {
    value.trace(tracer)
}

// unsafe impl<'v> Trace<'v> for ()
// Unit has nothing to trace.
fun traceUnit(@Suppress("UNUSED_PARAMETER") tracer: Tracer) {}

// unsafe impl<'v, T1: Trace<'v>> Trace<'v> for (T1,)
// Single-element tuple. Kotlin has no 1-tuple, so this is a standalone function.
fun <T1 : Trace> traceSingle(value: T1, tracer: Tracer) {
    value.trace(tracer)
}

// unsafe impl<'v, T1: Trace<'v>, T2: Trace<'v>> Trace<'v> for (T1, T2)
fun <T1 : Trace, T2 : Trace> Pair<T1, T2>.trace(tracer: Tracer) {
    this.first.trace(tracer)
    this.second.trace(tracer)
}

// unsafe impl<'v, T1: Trace<'v>, T2: Trace<'v>, T3: Trace<'v>> Trace<'v> for (T1, T2, T3)
fun <T1 : Trace, T2 : Trace, T3 : Trace> Triple<T1, T2, T3>.trace(tracer: Tracer) {
    this.first.trace(tracer)
    this.second.trace(tracer)
    this.third.trace(tracer)
}

// unsafe impl<'v, T1: Trace<'v>, T2: Trace<'v>, T3: Trace<'v>, T4: Trace<'v>> Trace<'v>
//     for (T1, T2, T3, T4)
// Kotlin has no 4-tuple type, so this is a standalone function.
fun <T1 : Trace, T2 : Trace, T3 : Trace, T4 : Trace> traceTuple4(
    t1: T1,
    t2: T2,
    t3: T3,
    t4: T4,
    tracer: Tracer,
) {
    t1.trace(tracer)
    t2.trace(tracer)
    t3.trace(tracer)
    t4.trace(tracer)
}

// unsafe impl<'v, T1: Trace<'v>, T2: Trace<'v>> Trace<'v> for Either<T1, T2>
fun <T1 : Trace, T2 : Trace> Either<T1, T2>.trace(tracer: Tracer) {
    when (this) {
        is Either.Left -> this.value.trace(tracer)
        is Either.Right -> this.value.trace(tracer)
    }
}

// unsafe impl<'v> Trace<'v> for Value<'v>
// In Rust, tracer.trace mutates the Value in-place via &mut.
// In Kotlin, ValueHolder wraps a mutable Value reference.
fun ValueHolder.trace(tracer: Tracer) {
    tracer.trace(this)
}

// unsafe impl<'v> Trace<'v> for FrozenValue
// FrozenValue cannot contain unfrozen references, so tracing is a no-op.
@Suppress("UNUSED_PARAMETER")
fun FrozenValue.trace(tracer: Tracer) {}

// unsafe impl<'v> Trace<'v> for String
// Strings contain no Values, so tracing is a no-op.
@Suppress("UNUSED_PARAMETER")
fun String.trace(tracer: Tracer) {}

// unsafe impl<'v> Trace<'v> for usize
// Primitive type, no-op.
@Suppress("UNUSED_PARAMETER")
fun traceUSize(value: ULong, tracer: Tracer) {}

// unsafe impl<'v> Trace<'v> for i32
// Primitive type, no-op.
@Suppress("UNUSED_PARAMETER")
fun Int.trace(tracer: Tracer) {}

// unsafe impl<'v> Trace<'v> for u32
// Primitive type, no-op.
@Suppress("UNUSED_PARAMETER")
fun UInt.trace(tracer: Tracer) {}

// unsafe impl<'v> Trace<'v> for u64
// Primitive type, no-op.
@Suppress("UNUSED_PARAMETER")
fun ULong.trace(tracer: Tracer) {}

// unsafe impl<'v> Trace<'v> for bool
// Primitive type, no-op.
@Suppress("UNUSED_PARAMETER")
fun Boolean.trace(tracer: Tracer) {}

// unsafe impl<'v> Trace<'v> for AtomicBool
// Atomic primitive, no-op.
@Suppress("UNUSED_PARAMETER")
fun AtomicBoolean.trace(tracer: Tracer) {}

// unsafe impl<'v> Trace<'v> for AtomicI8
// Kotlin maps AtomicI8 to AtomicInt. Atomic primitive, no-op.
@Suppress("UNUSED_PARAMETER")
fun traceAtomicI8(value: AtomicInt, tracer: Tracer) {}

// unsafe impl<'v> Trace<'v> for AtomicU8
// Kotlin maps AtomicU8 to AtomicInt. Atomic primitive, no-op.
@Suppress("UNUSED_PARAMETER")
fun traceAtomicU8(value: AtomicInt, tracer: Tracer) {}

// unsafe impl<'v> Trace<'v> for AtomicI16
// Kotlin maps AtomicI16 to AtomicInt. Atomic primitive, no-op.
@Suppress("UNUSED_PARAMETER")
fun traceAtomicI16(value: AtomicInt, tracer: Tracer) {}

// unsafe impl<'v> Trace<'v> for AtomicU16
// Kotlin maps AtomicU16 to AtomicInt. Atomic primitive, no-op.
@Suppress("UNUSED_PARAMETER")
fun traceAtomicU16(value: AtomicInt, tracer: Tracer) {}

// unsafe impl<'v> Trace<'v> for AtomicI32
// Atomic primitive, no-op.
@Suppress("UNUSED_PARAMETER")
fun traceAtomicI32(value: AtomicInt, tracer: Tracer) {}

// unsafe impl<'v> Trace<'v> for AtomicU32
// Kotlin maps AtomicU32 to AtomicInt. Atomic primitive, no-op.
@Suppress("UNUSED_PARAMETER")
fun traceAtomicU32(value: AtomicInt, tracer: Tracer) {}

// unsafe impl<'v> Trace<'v> for AtomicI64
// Atomic primitive, no-op.
@Suppress("UNUSED_PARAMETER")
fun traceAtomicI64(value: AtomicLong, tracer: Tracer) {}

// unsafe impl<'v> Trace<'v> for AtomicU64
// Kotlin maps AtomicU64 to AtomicLong. Atomic primitive, no-op.
@Suppress("UNUSED_PARAMETER")
fun traceAtomicU64(value: AtomicLong, tracer: Tracer) {}

// unsafe impl<'v> Trace<'v> for AtomicUsize
// Kotlin maps AtomicUsize to AtomicLong. Atomic primitive, no-op.
@Suppress("UNUSED_PARAMETER")
fun traceAtomicUsize(value: AtomicLong, tracer: Tracer) {}

// unsafe impl<'v> Trace<'v> for AtomicIsize
// Kotlin maps AtomicIsize to AtomicLong. Atomic primitive, no-op.
@Suppress("UNUSED_PARAMETER")
fun traceAtomicIsize(value: AtomicLong, tracer: Tracer) {}

// unsafe impl<'v> Trace<'v> for std::time::Instant
// Instant contains no Values, no-op.
@Suppress("UNUSED_PARAMETER")
fun Instant.trace(tracer: Tracer) {}

// unsafe impl<'v, T: ?Sized> Trace<'v> for marker::PhantomData<T>
// PhantomData is a zero-size marker in Rust; in Kotlin it is modeled as a nullable ignored value.
@Suppress("UNUSED_PARAMETER")
fun <T> tracePhantomData(value: T?, tracer: Tracer) {}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for Arc<Mutex<T>>
// Arc<Mutex<T>> in Rust: lock the mutex, then trace the inner value.
// In Kotlin, there is no Arc/Mutex in commonMain; we trace the value directly.
fun <T : Trace> traceArcMutex(value: T, tracer: Tracer) {
    value.trace(tracer)
}

// unsafe impl<'v, A, R> Trace<'v> for fn(A) -> R
// Function pointers contain no Values, no-op.
@Suppress("UNUSED_PARAMETER")
fun <A, R> traceFn1(value: (A) -> R, tracer: Tracer) {}

// unsafe impl<'v, A, B, R> Trace<'v> for fn(A, B) -> R
// Function pointers contain no Values, no-op.
@Suppress("UNUSED_PARAMETER")
fun <A, B, R> traceFn2(value: (A, B) -> R, tracer: Tracer) {}

// unsafe impl<'v, A, B, C, R> Trace<'v> for fn(A, B, C) -> R
// Function pointers contain no Values, no-op.
@Suppress("UNUSED_PARAMETER")
fun <A, B, C, R> traceFn3(value: (A, B, C) -> R, tracer: Tracer) {}
