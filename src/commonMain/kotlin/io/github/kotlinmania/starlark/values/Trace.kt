// port-lint: source src/values/trace.rs
package io.github.kotlinmania.starlark.values

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

import io.github.kotlinmania.starlark.Either
import io.github.kotlinmania.starlark.collections.HashTable
import io.github.kotlinmania.starlark.collections.Hashed
import io.github.kotlinmania.starlark.collections.SmallMap
import io.github.kotlinmania.starlark.collections.smallset.SmallSet
import io.github.kotlinmania.starlark.util.atomics.AtomicBool
import io.github.kotlinmania.starlark.util.atomics.AtomicI16
import io.github.kotlinmania.starlark.util.atomics.AtomicI32
import io.github.kotlinmania.starlark.util.atomics.AtomicI64
import io.github.kotlinmania.starlark.util.atomics.AtomicI8
import io.github.kotlinmania.starlark.util.atomics.AtomicIsize
import io.github.kotlinmania.starlark.util.atomics.AtomicU16
import io.github.kotlinmania.starlark.util.atomics.AtomicU32
import io.github.kotlinmania.starlark.util.atomics.AtomicU64
import io.github.kotlinmania.starlark.util.atomics.AtomicU8
import io.github.kotlinmania.starlark.util.atomics.AtomicUsize
import io.github.kotlinmania.starlark.util.boxed.Box
import io.github.kotlinmania.starlark.util.cell.Cell
import io.github.kotlinmania.starlark.util.cell.OnceCell
import io.github.kotlinmania.starlark.util.cell.UnsafeCell
import io.github.kotlinmania.starlark.util.refcell.RefCell
import io.github.kotlinmania.starlark.util.scalar.Usize
import io.github.kotlinmania.starlark.util.sync.Arc
import io.github.kotlinmania.starlark.util.sync.Mutex
import io.github.kotlinmania.starlark.util.time.Instant
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.heap.Tracer
import io.github.kotlinmania.starlark.values.layout.heap.ValueHolder as Value

/**
 * Called by the garbage collection, and must walk over every contained [Value] in the type.
 *
 * Marked `unsafe` in Rust because if you miss a nested `Value`, it will probably segfault.
 */
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

/** unsafe impl<'v, T: Trace<'v>> Trace<'v> for Vec<T> */
internal fun <T : Trace> MutableList<T>.trace(tracer: Tracer) {
    this.forEach { it.trace(tracer) }
}

/** unsafe impl<'v, T: Trace<'v>> Trace<'v> for [T] */
internal fun <T : Trace> Array<T>.trace(tracer: Tracer) {
    this.forEach { it.trace(tracer) }
}

/** unsafe impl<'v, T: Trace<'v>> Trace<'v> for HashTable<T> */
internal fun <T : Trace> HashTable<T>.trace(tracer: Tracer) {
    this.iterMut().forEach { it.trace(tracer) }
}

/** unsafe impl<'v, K: Trace<'v>, V: Trace<'v>> Trace<'v> for SmallMap<K, V> */
internal fun <K : Trace, V : Trace> SmallMap<K, V>.trace(tracer: Tracer) {
    for ((k, v) in this) {
        k.trace(tracer)
        v.trace(tracer)
    }
}

/** unsafe impl<'v, T: Trace<'v>> Trace<'v> for SmallSet<T> */
internal fun <T : Trace> SmallSet<T>.trace(tracer: Tracer) {
    for (v in this) {
        v.trace(tracer)
    }
}

/** unsafe impl<'v, T: Trace<'v>> Trace<'v> for Hashed<T> */
internal fun <T : Trace> Hashed<T>.trace(tracer: Tracer) {
    this.key().trace(tracer)
}

/** unsafe impl<'v, T: Trace<'v>> Trace<'v> for Option<T> */
internal fun <T : Trace> T?.trace(tracer: Tracer) {
    if (this != null) {
        this.trace(tracer)
    }
}

/** unsafe impl<'v, T: Trace<'v>> Trace<'v> for RefCell<T> */
internal fun <T : Trace> RefCell<T>.trace(tracer: Tracer) {
    this.getMut().trace(tracer)
}

/** unsafe impl<'v, T: Trace<'v>> Trace<'v> for Cell<T> */
internal fun <T : Trace> Cell<T>.trace(tracer: Tracer) {
    this.getMut().trace(tracer)
}

/** unsafe impl<'v, T: Trace<'v>> Trace<'v> for OnceCell<T> */
internal fun <T : Trace> OnceCell<T>.trace(tracer: Tracer) {
    val x = this.getMut()
    if (x != null) {
        x.trace(tracer)
    }
}

/** unsafe impl<'v, T: Trace<'v>> Trace<'v> for UnsafeCell<T> */
internal fun <T : Trace> UnsafeCell<T>.trace(tracer: Tracer) {
    this.getMut().trace(tracer)
}

/** unsafe impl<'v, T: Trace<'v> + ?Sized> Trace<'v> for Box<T> */
internal fun <T : Trace> Box<T>.trace(tracer: Tracer) {
    this.asMut().trace(tracer)
}

/** unsafe impl<'v> Trace<'v> for () */
internal fun Unit.trace(tracer: Tracer) {
}

/** unsafe impl<'v, T1: Trace<'v>> Trace<'v> for (T1,) */
internal fun <T1 : Trace> Tuple1<T1>.trace(tracer: Tracer) {
    this.value0.trace(tracer)
}

/** unsafe impl<'v, T1: Trace<'v>, T2: Trace<'v>> Trace<'v> for (T1, T2) */
internal fun <T1 : Trace, T2 : Trace> Pair<T1, T2>.trace(tracer: Tracer) {
    this.first.trace(tracer)
    this.second.trace(tracer)
}

/** unsafe impl<'v, T1: Trace<'v>, T2: Trace<'v>, T3: Trace<'v>> Trace<'v> for (T1, T2, T3) */
internal fun <T1 : Trace, T2 : Trace, T3 : Trace> Triple<T1, T2, T3>.trace(tracer: Tracer) {
    this.first.trace(tracer)
    this.second.trace(tracer)
    this.third.trace(tracer)
}

/** unsafe impl<'v, T1: Trace<'v>, T2: Trace<'v>, T3: Trace<'v>, T4: Trace<'v>> Trace<'v> for (T1, T2, T3, T4) */
internal fun <T1 : Trace, T2 : Trace, T3 : Trace, T4 : Trace> Tuple4<T1, T2, T3, T4>.trace(tracer: Tracer) {
    this.first.trace(tracer)
    this.second.trace(tracer)
    this.third.trace(tracer)
    this.fourth.trace(tracer)
}

/** unsafe impl<'v, T1: Trace<'v>, T2: Trace<'v>> Trace<'v> for Either<T1, T2> */
internal fun <T1 : Trace, T2 : Trace> Either<T1, T2>.trace(tracer: Tracer) {
    when (this) {
        is Either.Left -> this.value.trace(tracer)
        is Either.Right -> this.value.trace(tracer)
    }
}

/** unsafe impl<'v> Trace<'v> for Value<'v> */
internal fun Value.trace(tracer: Tracer) {
    tracer.trace(this)
}

/** unsafe impl<'v> Trace<'v> for FrozenValue */
internal fun FrozenValue.trace(tracer: Tracer) {
}

/** unsafe impl<'v> Trace<'v> for String */
internal fun String.trace(tracer: Tracer) {
}

/** unsafe impl<'v> Trace<'v> for usize */
internal fun Usize.trace(tracer: Tracer) {
}

/** unsafe impl<'v> Trace<'v> for i32 */
internal fun Int.trace(tracer: Tracer) {
}

/** unsafe impl<'v> Trace<'v> for u32 */
internal fun UInt.trace(tracer: Tracer) {
}

/** unsafe impl<'v> Trace<'v> for u64 */
internal fun ULong.trace(tracer: Tracer) {
}

/** unsafe impl<'v> Trace<'v> for bool */
internal fun Boolean.trace(tracer: Tracer) {
}

/** unsafe impl<'v> Trace<'v> for AtomicBool */
internal fun AtomicBool.trace(tracer: Tracer) {
}

/** unsafe impl<'v> Trace<'v> for AtomicI8 */
internal fun AtomicI8.trace(tracer: Tracer) {
}

/** unsafe impl<'v> Trace<'v> for AtomicU8 */
internal fun AtomicU8.trace(tracer: Tracer) {
}

/** unsafe impl<'v> Trace<'v> for AtomicI16 */
internal fun AtomicI16.trace(tracer: Tracer) {
}

/** unsafe impl<'v> Trace<'v> for AtomicU16 */
internal fun AtomicU16.trace(tracer: Tracer) {
}

/** unsafe impl<'v> Trace<'v> for AtomicI32 */
internal fun AtomicI32.trace(tracer: Tracer) {
}

/** unsafe impl<'v> Trace<'v> for AtomicU32 */
internal fun AtomicU32.trace(tracer: Tracer) {
}

/** unsafe impl<'v> Trace<'v> for AtomicI64 */
internal fun AtomicI64.trace(tracer: Tracer) {
}

/** unsafe impl<'v> Trace<'v> for AtomicU64 */
internal fun AtomicU64.trace(tracer: Tracer) {
}

/** unsafe impl<'v> Trace<'v> for AtomicUsize */
internal fun AtomicUsize.trace(tracer: Tracer) {
}

/** unsafe impl<'v> Trace<'v> for AtomicIsize */
internal fun AtomicIsize.trace(tracer: Tracer) {
}

/** unsafe impl<'v> Trace<'v> for std::time::Instant */
internal fun Instant.trace(tracer: Tracer) {
}

/** unsafe impl<'v, T: ?Sized> Trace<'v> for marker::PhantomData<T> */
internal fun <T> PhantomData<T>.trace(tracer: Tracer) {
}

/** unsafe impl<'v, T: Trace<'v>> Trace<'v> for Arc<Mutex<T>> */
internal fun <T : Trace> Arc<Mutex<T>>.trace(tracer: Tracer) {
    this.getMut().lock().trace(tracer)
}

/** unsafe impl<'v, A, R> Trace<'v> for fn(A) -> R */
internal fun <A, R> ((A) -> R).trace(tracer: Tracer) {
}

/** unsafe impl<'v, A, B, R> Trace<'v> for fn(A, B) -> R */
internal fun <A, B, R> ((A, B) -> R).trace(tracer: Tracer) {
}

/** unsafe impl<'v, A, B, C, R> Trace<'v> for fn(A, B, C) -> R */
internal fun <A, B, C, R> ((A, B, C) -> R).trace(tracer: Tracer) {
}
