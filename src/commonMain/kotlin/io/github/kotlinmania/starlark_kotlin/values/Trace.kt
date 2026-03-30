// port-lint: source src/values/trace.rs
package io.github.kotlinmania.starlark_kotlin.values

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

// use std::cell::OnceCell;
// use std::cell::RefCell;
// use std::cell::UnsafeCell;
// use std::cell::Cell;
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
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.ValueHolder
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.AtomicLong
import kotlinx.datetime.Instant

/// Called by the garbage collection, and must walk over every contained `Value` in the type.
/// Marked `unsafe` because if you miss a nested `Value`, it will probably segfault.
///
/// For the most cases `#[derive(Trace)]` is enough to implement this trait:
///
/// ```
/// # use starlark::values::Value;
/// # use starlark::values::Trace;
///
/// #[derive(Trace)]
/// struct MySet<'v> {
///     keys: Vec<Value<'v>>,
/// }
/// ```
// pub unsafe trait Trace<'v> {
//     /// Recursively "trace" the value.
//     ///
//     /// Note during trace, `Value` objects in `Self` might be already special forward-objects,
//     /// trying to unpack these values may crash the process.
//     ///
//     /// Generally this function should not do anything except calling `trace` on the fields.
//     fn trace(&mut self, tracer: &Tracer<'v>);
// }
interface Trace {
    /**
     * Recursively "trace" the value.
     *
     * Note during trace, [Value] objects in `this` might be already special forward-objects,
     * trying to unpack these values may crash the process.
     *
     * Generally this function should not do anything except calling `trace` on the fields.
     */
    fun trace(tracer: Tracer)
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for Vec<T> {
//     fn trace(&mut self, tracer: &Tracer<'v>) {
//         self.iter_mut().for_each(|x| x.trace(tracer));
//     }
// }
fun <T : Trace> MutableList<T>.trace(tracer: Tracer) {
    this.forEach { x -> x.trace(tracer) }
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for [T] {
//     fn trace(&mut self, tracer: &Tracer<'v>) {
//         self.iter_mut().for_each(|x| x.trace(tracer));
//     }
// }
fun <T : Trace> Array<T>.trace(tracer: Tracer) {
    this.forEach { x -> x.trace(tracer) }
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for HashTable<T> {
//     fn trace(&mut self, tracer: &Tracer<'v>) {
//         self.iter_mut().for_each(|e| e.trace(tracer));
//     }
// }
fun <T : Trace> MutableCollection<T>.trace(tracer: Tracer) {
    this.forEach { e -> e.trace(tracer) }
}

// unsafe impl<'v, K: Trace<'v>, V: Trace<'v>> Trace<'v> for SmallMap<K, V> {
//     fn trace(&mut self, tracer: &Tracer<'v>) {
//         for (k, v) in self.iter_mut_unchecked() {
//             k.trace(tracer);
//             v.trace(tracer);
//         }
//     }
// }
fun <K : Trace, V : Trace> SmallMap<K, V>.trace(tracer: Tracer) {
    for ((k, v) in this.iterMutUnchecked()) {
        k.trace(tracer)
        v.trace(tracer)
    }
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for SmallSet<T> {
//     fn trace(&mut self, tracer: &Tracer<'v>) {
//         for v in self.iter_mut_unchecked() {
//             v.trace(tracer);
//         }
//     }
// }
fun <T : Trace> SmallSet<T>.trace(tracer: Tracer) {
    for (v in this.iterMutUnchecked()) {
        v.trace(tracer)
    }
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for Hashed<T> {
//     fn trace(&mut self, tracer: &Tracer<'v>) {
//         self.key_mut().trace(tracer);
//     }
// }
fun <T : Trace> Hashed<T>.trace(tracer: Tracer) {
    this.keyMut().trace(tracer)
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for Option<T> {
//     fn trace(&mut self, tracer: &Tracer<'v>) {
//         if let Some(x) = self {
//             x.trace(tracer)
//         }
//     }
// }
fun <T : Trace> T?.trace(tracer: Tracer) {
    if (this != null) {
        this.trace(tracer)
    }
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for RefCell<T> {
//     fn trace(&mut self, tracer: &Tracer<'v>) {
//         self.get_mut().trace(tracer)
//     }
// }
// Kotlin has no RefCell; the value is traced directly via get_mut().
fun <T : Trace> traceRefCell(self: T, tracer: Tracer) {
    self.trace(tracer)
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for Cell<T> {
//     fn trace(&mut self, tracer: &Tracer<'v>) {
//         self.get_mut().trace(tracer);
//     }
// }
// Kotlin has no Cell; the value is traced directly via get_mut().
fun <T : Trace> traceCell(self: T, tracer: Tracer) {
    self.trace(tracer)
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for OnceCell<T> {
//     fn trace(&mut self, tracer: &Tracer<'v>) {
//         if let Some(x) = self.get_mut() {
//             x.trace(tracer)
//         }
//     }
// }
// Kotlin has no OnceCell; modeled as T? and delegates to nullable trace.
fun <T : Trace> traceOnceCell(self: T?, tracer: Tracer) {
    self?.trace(tracer)
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for UnsafeCell<T> {
//     fn trace(&mut self, tracer: &Tracer<'v>) {
//         self.get_mut().trace(tracer);
//     }
// }
// Kotlin has no UnsafeCell; the value is traced directly via get_mut().
fun <T : Trace> traceUnsafeCell(self: T, tracer: Tracer) {
    self.trace(tracer)
}

// unsafe impl<'v, T: Trace<'v> + ?Sized> Trace<'v> for Box<T> {
//     fn trace(&mut self, tracer: &Tracer<'v>) {
//         Box::as_mut(self).trace(tracer)
//     }
// }
// Kotlin has no Box; values are references. Trace is called directly on the value.
fun <T : Trace> traceBox(self: T, tracer: Tracer) {
    self.trace(tracer)
}

// unsafe impl<'v> Trace<'v> for () {
//     fn trace(&mut self, _tracer: &Tracer<'v>) {}
// }
@Suppress("UNUSED_PARAMETER")
fun traceUnit(tracer: Tracer) {
    // Unit has nothing to trace.
}

// unsafe impl<'v, T1: Trace<'v>> Trace<'v> for (T1,) {
//     fn trace(&mut self, tracer: &Tracer<'v>) {
//         self.0.trace(tracer);
//     }
// }
fun <T1 : Trace> Tuple1<T1>.trace(tracer: Tracer) {
    this.value0.trace(tracer)
}

// unsafe impl<'v, T1: Trace<'v>, T2: Trace<'v>> Trace<'v> for (T1, T2) {
//     fn trace(&mut self, tracer: &Tracer<'v>) {
//         self.0.trace(tracer);
//         self.1.trace(tracer);
//     }
// }
fun <T1 : Trace, T2 : Trace> Pair<T1, T2>.trace(tracer: Tracer) {
    this.first.trace(tracer)
    this.second.trace(tracer)
}

// unsafe impl<'v, T1: Trace<'v>, T2: Trace<'v>, T3: Trace<'v>> Trace<'v> for (T1, T2, T3) {
//     fn trace(&mut self, tracer: &Tracer<'v>) {
//         self.0.trace(tracer);
//         self.1.trace(tracer);
//         self.2.trace(tracer);
//     }
// }
fun <T1 : Trace, T2 : Trace, T3 : Trace> Triple<T1, T2, T3>.trace(tracer: Tracer) {
    this.first.trace(tracer)
    this.second.trace(tracer)
    this.third.trace(tracer)
}

// unsafe impl<'v, T1: Trace<'v>, T2: Trace<'v>, T3: Trace<'v>, T4: Trace<'v>> Trace<'v>
//     for (T1, T2, T3, T4)
// {
//     fn trace(&mut self, tracer: &Tracer<'v>) {
//         self.0.trace(tracer);
//         self.1.trace(tracer);
//         self.2.trace(tracer);
//         self.3.trace(tracer);
//     }
// }
fun <T1 : Trace, T2 : Trace, T3 : Trace, T4 : Trace> Tuple4<T1, T2, T3, T4>.trace(tracer: Tracer) {
    this.first.trace(tracer)
    this.second.trace(tracer)
    this.third.trace(tracer)
    this.fourth.trace(tracer)
}

// unsafe impl<'v, T1: Trace<'v>, T2: Trace<'v>> Trace<'v> for Either<T1, T2> {
//     fn trace(&mut self, tracer: &Tracer<'v>) {
//         match self {
//             Either::Left(x) => x.trace(tracer),
//             Either::Right(x) => x.trace(tracer),
//         }
//     }
// }
fun <T1 : Trace, T2 : Trace> Either<T1, T2>.trace(tracer: Tracer) {
    when (this) {
        is Either.Left -> this.value.trace(tracer)
        is Either.Right -> this.value.trace(tracer)
    }
}

// unsafe impl<'v> Trace<'v> for Value<'v> {
//     fn trace(&mut self, tracer: &Tracer<'v>) {
//         tracer.trace(self)
//     }
// }
fun ValueHolder.trace(tracer: Tracer) {
    tracer.trace(this)
}

// unsafe impl<'v> Trace<'v> for FrozenValue {
//     fn trace(&mut self, _tracer: &Tracer<'v>) {}
// }
@Suppress("UNUSED_PARAMETER")
fun FrozenValue.trace(tracer: Tracer) {
}

// unsafe impl<'v> Trace<'v> for String {
//     fn trace(&mut self, _tracer: &Tracer<'v>) {}
// }
@Suppress("UNUSED_PARAMETER")
fun String.trace(tracer: Tracer) {
}

// unsafe impl<'v> Trace<'v> for usize {
//     fn trace(&mut self, _tracer: &Tracer<'v>) {}
// }
@Suppress("UNUSED_PARAMETER")
fun Int.traceUsize(tracer: Tracer) {
}

// unsafe impl<'v> Trace<'v> for i32 {
//     fn trace(&mut self, _tracer: &Tracer<'v>) {}
// }
@Suppress("UNUSED_PARAMETER")
fun Int.trace(tracer: Tracer) {
}

// unsafe impl<'v> Trace<'v> for u32 {
//     fn trace(&mut self, _tracer: &Tracer<'v>) {}
// }
@Suppress("UNUSED_PARAMETER")
fun UInt.trace(tracer: Tracer) {
}

// unsafe impl<'v> Trace<'v> for u64 {
//     fn trace(&mut self, _tracer: &Tracer<'v>) {}
// }
@Suppress("UNUSED_PARAMETER")
fun ULong.trace(tracer: Tracer) {
}

// unsafe impl<'v> Trace<'v> for bool {
//     fn trace(&mut self, _tracer: &Tracer<'v>) {}
// }
@Suppress("UNUSED_PARAMETER")
fun Boolean.trace(tracer: Tracer) {
}

// unsafe impl<'v> Trace<'v> for AtomicBool {
//     fn trace(&mut self, _tracer: &Tracer<'v>) {}
// }
@Suppress("UNUSED_PARAMETER")
fun AtomicBoolean.trace(tracer: Tracer) {
}

// unsafe impl<'v> Trace<'v> for AtomicI8 {
//     fn trace(&mut self, _tracer: &Tracer<'v>) {}
// }
// Kotlin maps AtomicI8 to AtomicInt.
@Suppress("UNUSED_PARAMETER")
fun AtomicInt.traceAtomicI8(tracer: Tracer) {
}

// unsafe impl<'v> Trace<'v> for AtomicU8 {
//     fn trace(&mut self, _tracer: &Tracer<'v>) {}
// }
// Kotlin maps AtomicU8 to AtomicInt.
@Suppress("UNUSED_PARAMETER")
fun AtomicInt.traceAtomicU8(tracer: Tracer) {
}

// unsafe impl<'v> Trace<'v> for AtomicI16 {
//     fn trace(&mut self, _tracer: &Tracer<'v>) {}
// }
// Kotlin maps AtomicI16 to AtomicInt.
@Suppress("UNUSED_PARAMETER")
fun AtomicInt.traceAtomicI16(tracer: Tracer) {
}

// unsafe impl<'v> Trace<'v> for AtomicU16 {
//     fn trace(&mut self, _tracer: &Tracer<'v>) {}
// }
// Kotlin maps AtomicU16 to AtomicInt.
@Suppress("UNUSED_PARAMETER")
fun AtomicInt.traceAtomicU16(tracer: Tracer) {
}

// unsafe impl<'v> Trace<'v> for AtomicI32 {
//     fn trace(&mut self, _tracer: &Tracer<'v>) {}
// }
@Suppress("UNUSED_PARAMETER")
fun AtomicInt.trace(tracer: Tracer) {
}

// unsafe impl<'v> Trace<'v> for AtomicU32 {
//     fn trace(&mut self, _tracer: &Tracer<'v>) {}
// }
// Kotlin maps AtomicU32 to AtomicInt.
@Suppress("UNUSED_PARAMETER")
fun AtomicInt.traceAtomicU32(tracer: Tracer) {
}

// unsafe impl<'v> Trace<'v> for AtomicI64 {
//     fn trace(&mut self, _tracer: &Tracer<'v>) {}
// }
@Suppress("UNUSED_PARAMETER")
fun AtomicLong.trace(tracer: Tracer) {
}

// unsafe impl<'v> Trace<'v> for AtomicU64 {
//     fn trace(&mut self, _tracer: &Tracer<'v>) {}
// }
// Kotlin maps AtomicU64 to AtomicLong.
@Suppress("UNUSED_PARAMETER")
fun AtomicLong.traceAtomicU64(tracer: Tracer) {
}

// unsafe impl<'v> Trace<'v> for AtomicUsize {
//     fn trace(&mut self, _tracer: &Tracer<'v>) {}
// }
// Kotlin maps AtomicUsize to AtomicLong.
@Suppress("UNUSED_PARAMETER")
fun AtomicLong.traceAtomicUsize(tracer: Tracer) {
}

// unsafe impl<'v> Trace<'v> for AtomicIsize {
//     fn trace(&mut self, _tracer: &Tracer<'v>) {}
// }
// Kotlin maps AtomicIsize to AtomicLong.
@Suppress("UNUSED_PARAMETER")
fun AtomicLong.traceAtomicIsize(tracer: Tracer) {
}

// unsafe impl<'v> Trace<'v> for std::time::Instant {
//     fn trace(&mut self, _tracer: &Tracer<'v>) {}
// }
@Suppress("UNUSED_PARAMETER")
fun Instant.trace(tracer: Tracer) {
}

// unsafe impl<'v, T: ?Sized> Trace<'v> for marker::PhantomData<T> {
//     fn trace(&mut self, _tracer: &Tracer<'v>) {}
// }
@Suppress("UNUSED_PARAMETER")
fun <T> PhantomData<T>.trace(tracer: Tracer) {
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for Arc<Mutex<T>> {
//     fn trace(&mut self, tracer: &Tracer<'v>) {
//         self.lock().unwrap().trace(tracer);
//     }
// }
// In Kotlin, there is no Arc/Mutex in commonMain; callers should lock and trace the inner value.
fun <T : Trace> traceArcMutex(value: T, tracer: Tracer) {
    value.trace(tracer)
}

// unsafe impl<'v, A, R> Trace<'v> for fn(A) -> R {
//     fn trace(&mut self, _tracer: &Tracer<'v>) {}
// }
@Suppress("UNUSED_PARAMETER")
fun <A, R> traceFn1(value: (A) -> R, tracer: Tracer) {
}

// unsafe impl<'v, A, B, R> Trace<'v> for fn(A, B) -> R {
//     fn trace(&mut self, _tracer: &Tracer<'v>) {}
// }
@Suppress("UNUSED_PARAMETER")
fun <A, B, R> traceFn2(value: (A, B) -> R, tracer: Tracer) {
}

// unsafe impl<'v, A, B, C, R> Trace<'v> for fn(A, B, C) -> R {
//     fn trace(&mut self, _tracer: &Tracer<'v>) {}
// }
@Suppress("UNUSED_PARAMETER")
fun <A, B, C, R> traceFn3(value: (A, B, C) -> R, tracer: Tracer) {
}
