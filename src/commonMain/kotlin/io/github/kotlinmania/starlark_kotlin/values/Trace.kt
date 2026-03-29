// port-lint: source src/values/trace.rs
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

package io.github.kotlinmania.starlark_kotlin.values

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
 * Marked `unsafe` because if you miss a nested [Value], it will probably segfault.
 *
 * For the most cases `#[derive(Trace)]` is enough to implement this trait:
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
    for ((k, v) in this.iter()) {
        k.trace(tracer)
        v.trace(tracer)
    }
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for SmallSet<T>
fun <T : Trace> SmallSet<T>.trace(tracer: Tracer) {
    for (v in this.iter()) {
        v.trace(tracer)
    }
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for Hashed<T>
fun <T : Trace> Hashed<T>.trace(tracer: Tracer) {
    this.key().trace(tracer)
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for Option<T>
fun <T : Trace> traceNullable(self: T?, tracer: Tracer) {
    if (self != null) {
        self.trace(tracer)
    }
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for RefCell<T>
fun <T : Trace> traceRefCell(self: T, tracer: Tracer) {
    self.trace(tracer)
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for Cell<T>
fun <T : Trace> traceCell(self: T, tracer: Tracer) {
    self.trace(tracer)
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for OnceCell<T>
fun <T : Trace> traceOnceCell(self: T?, tracer: Tracer) {
    if (self != null) {
        self.trace(tracer)
    }
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for UnsafeCell<T>
fun <T : Trace> traceUnsafeCell(self: T, tracer: Tracer) {
    self.trace(tracer)
}

// unsafe impl<'v, T: Trace<'v> + ?Sized> Trace<'v> for Box<T>
fun <T : Trace> traceBox(self: T, tracer: Tracer) {
    self.trace(tracer)
}

// unsafe impl<'v> Trace<'v> for ()
fun traceUnit(_tracer: Tracer) {}

// unsafe impl<'v, T1: Trace<'v>> Trace<'v> for (T1,)
fun <T1 : Trace> traceTuple1(self: T1, tracer: Tracer) {
    self.trace(tracer)
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
fun <T1 : Trace, T2 : Trace> traceEither(self: Either<T1, T2>, tracer: Tracer) {
    when (self) {
        is Either.Left -> self.value.trace(tracer)
        is Either.Right -> self.value.trace(tracer)
    }
}

// unsafe impl<'v> Trace<'v> for Value<'v>
fun ValueHolder.trace(tracer: Tracer) {
    tracer.trace(this)
}

// unsafe impl<'v> Trace<'v> for FrozenValue
fun FrozenValue.trace(_tracer: Tracer) {}

// unsafe impl<'v> Trace<'v> for String
fun String.trace(_tracer: Tracer) {}

// unsafe impl<'v> Trace<'v> for usize
fun traceUsize(self: ULong, _tracer: Tracer) {}

// unsafe impl<'v> Trace<'v> for i32
fun Int.trace(_tracer: Tracer) {}

// unsafe impl<'v> Trace<'v> for u32
fun UInt.trace(_tracer: Tracer) {}

// unsafe impl<'v> Trace<'v> for u64
fun ULong.trace(_tracer: Tracer) {}

// unsafe impl<'v> Trace<'v> for bool
fun Boolean.trace(_tracer: Tracer) {}

// unsafe impl<'v> Trace<'v> for AtomicBool
fun AtomicBoolean.trace(_tracer: Tracer) {}

// unsafe impl<'v> Trace<'v> for AtomicI8
fun AtomicInt.traceI8(_tracer: Tracer) {}

// unsafe impl<'v> Trace<'v> for AtomicU8
fun AtomicInt.traceU8(_tracer: Tracer) {}

// unsafe impl<'v> Trace<'v> for AtomicI16
fun AtomicInt.traceI16(_tracer: Tracer) {}

// unsafe impl<'v> Trace<'v> for AtomicU16
fun AtomicInt.traceU16(_tracer: Tracer) {}

// unsafe impl<'v> Trace<'v> for AtomicI32
fun AtomicInt.traceI32(_tracer: Tracer) {}

// unsafe impl<'v> Trace<'v> for AtomicU32
fun AtomicInt.traceU32(_tracer: Tracer) {}

// unsafe impl<'v> Trace<'v> for AtomicI64
fun AtomicLong.traceI64(_tracer: Tracer) {}

// unsafe impl<'v> Trace<'v> for AtomicU64
fun AtomicLong.traceU64(_tracer: Tracer) {}

// unsafe impl<'v> Trace<'v> for AtomicUsize
fun AtomicLong.traceUsize(_tracer: Tracer) {}

// unsafe impl<'v> Trace<'v> for AtomicIsize
fun AtomicLong.traceIsize(_tracer: Tracer) {}

// unsafe impl<'v> Trace<'v> for std::time::Instant
fun Instant.trace(_tracer: Tracer) {}

// unsafe impl<'v, T: ?Sized> Trace<'v> for marker::PhantomData<T>
fun <T> tracePhantomData(self: T?, _tracer: Tracer) {}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for Arc<Mutex<T>>
fun <T : Trace> traceArcMutex(self: T, tracer: Tracer) {
    self.trace(tracer)
}

// unsafe impl<'v, A, R> Trace<'v> for fn(A) -> R
fun <A, R> traceFn1(self: (A) -> R, _tracer: Tracer) {}

// unsafe impl<'v, A, B, R> Trace<'v> for fn(A, B) -> R
fun <A, B, R> traceFn2(self: (A, B) -> R, _tracer: Tracer) {}

// unsafe impl<'v, A, B, C, R> Trace<'v> for fn(A, B, C) -> R
fun <A, B, C, R> traceFn3(self: (A, B, C) -> R, _tracer: Tracer) {}
