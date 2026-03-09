// port-lint: source values/trace.rs
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

import kotlinx.atomicfu.AtomicBoolean
import kotlinx.atomicfu.AtomicInt
import kotlinx.atomicfu.AtomicLong
import kotlinx.atomicfu.AtomicRef
import io.github.kotlinmania.starlark_kotlin.collections.Hashed

/**
 * Called by the garbage collection, and must walk over every contained `Value` in the type.
 *
 * For the most cases `Trace` is enough to implement this interface:
 *
 * ```
 * class MySet(
 *     val keys: MutableList<Value>,
 * ) : Trace {
 *     override fun trace(tracer: Tracer) {
 *         traceList(keys, tracer)
 *     }
 * }
 * ```
 */
interface Trace {
    /**
     * Recursively "trace" the value.
     *
     * Note during trace, `Value` objects in `this` might be already special forward-objects,
     * trying to unpack these values may crash the process.
     *
     * Generally this function should not do anything except calling `trace` on the fields.
     */
    fun trace(tracer: Tracer)
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for Vec<T>
fun <T : Trace> traceList(list: MutableList<T>, tracer: Tracer) {
    for (x in list) {
        x.trace(tracer)
    }
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for [T]
fun <T : Trace> traceArray(array: Array<T>, tracer: Tracer) {
    for (x in array) {
        x.trace(tracer)
    }
}

// unsafe impl<'v, K: Trace<'v>, V: Trace<'v>> Trace<'v> for SmallMap<K, V>
fun <K : Trace, V : Trace> traceMap(map: MutableMap<K, V>, tracer: Tracer) {
    for ((k, v) in map) {
        k.trace(tracer)
        v.trace(tracer)
    }
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for SmallSet<T>
fun <T : Trace> traceSet(set: MutableSet<T>, tracer: Tracer) {
    for (v in set) {
        v.trace(tracer)
    }
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for Hashed<T>
fun <T : Trace> traceHashed(hashed: Hashed<T>, tracer: Tracer) {
    hashed.key().trace(tracer)
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for Option<T>
fun <T : Trace> traceNullable(value: T?, tracer: Tracer) {
    value?.trace(tracer)
}

// unsafe impl<'v, T: Trace<'v> + ?Sized> Trace<'v> for Box<T>
fun <T : Trace> traceBoxed(value: T, tracer: Tracer) {
    value.trace(tracer)
}

// unsafe impl<'v> Trace<'v> for ()
// No-op trace: Unit has nothing to trace.

// unsafe impl<'v, T1: Trace<'v>> Trace<'v> for (T1,)
fun <T1 : Trace> traceTuple1(t1: T1, tracer: Tracer) {
    t1.trace(tracer)
}

// unsafe impl<'v, T1: Trace<'v>, T2: Trace<'v>> Trace<'v> for (T1, T2)
fun <T1 : Trace, T2 : Trace> traceTuple2(t1: T1, t2: T2, tracer: Tracer) {
    t1.trace(tracer)
    t2.trace(tracer)
}

// unsafe impl<'v, T1: Trace<'v>, T2: Trace<'v>, T3: Trace<'v>> Trace<'v> for (T1, T2, T3)
fun <T1 : Trace, T2 : Trace, T3 : Trace> traceTuple3(
    t1: T1,
    t2: T2,
    t3: T3,
    tracer: Tracer,
) {
    t1.trace(tracer)
    t2.trace(tracer)
    t3.trace(tracer)
}

// unsafe impl<'v, T1..T4: Trace<'v>> Trace<'v> for (T1, T2, T3, T4)
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
fun <T1 : Trace, T2 : Trace> traceEither(either: Any, tracer: Tracer) {
    when (either) {
        is Trace -> either.trace(tracer)
    }
}

// unsafe impl<'v> Trace<'v> for Value<'v>
fun traceValue(value: Value, tracer: Tracer) {
    tracer.trace(value)
}

// unsafe impl<'v> Trace<'v> for FrozenValue
fun traceFrozenValue(
    @Suppress("UNUSED_PARAMETER") value: FrozenValue,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {
}

// unsafe impl<'v> Trace<'v> for ()
fun traceUnit(@Suppress("UNUSED_PARAMETER") tracer: Tracer) {
}

// unsafe impl<'v> Trace<'v> for String
fun traceString(
    @Suppress("UNUSED_PARAMETER") value: String,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {
}

// unsafe impl<'v> Trace<'v> for usize
fun traceUsize(
    @Suppress("UNUSED_PARAMETER") value: UInt,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {
}

// unsafe impl<'v> Trace<'v> for i32
fun traceI32(
    @Suppress("UNUSED_PARAMETER") value: Int,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {
}

// unsafe impl<'v> Trace<'v> for u32
fun traceU32(
    @Suppress("UNUSED_PARAMETER") value: UInt,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {
}

// unsafe impl<'v> Trace<'v> for u64
fun traceU64(
    @Suppress("UNUSED_PARAMETER") value: ULong,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {
}

// unsafe impl<'v> Trace<'v> for bool
fun traceBool(
    @Suppress("UNUSED_PARAMETER") value: Boolean,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {
}

// unsafe impl<'v> Trace<'v> for AtomicBool
fun traceAtomicBoolean(
    @Suppress("UNUSED_PARAMETER") value: AtomicBoolean,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {
}

// unsafe impl<'v> Trace<'v> for AtomicI32 / AtomicU32
fun traceAtomicInt(
    @Suppress("UNUSED_PARAMETER") value: AtomicInt,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {
}

// unsafe impl<'v> Trace<'v> for AtomicI64 / AtomicU64
fun traceAtomicLong(
    @Suppress("UNUSED_PARAMETER") value: AtomicLong,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {
}

// unsafe impl<'v, T: ?Sized> Trace<'v> for marker::PhantomData<T>
fun <T> tracePhantomData(
    @Suppress("UNUSED_PARAMETER") value: T?,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for Arc<Mutex<T>>
fun <T : Trace> traceAtomicRef(atomicRef: AtomicRef<T>, tracer: Tracer) {
    atomicRef.value.trace(tracer)
}

// unsafe impl<'v, A, R> Trace<'v> for fn(A) -> R
fun <A, R> traceFn1(
    @Suppress("UNUSED_PARAMETER") fn: (A) -> R,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {
}

// unsafe impl<'v, A, B, R> Trace<'v> for fn(A, B) -> R
fun <A, B, R> traceFn2(
    @Suppress("UNUSED_PARAMETER") fn: (A, B) -> R,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {
}

// unsafe impl<'v, A, B, C, R> Trace<'v> for fn(A, B, C) -> R
fun <A, B, C, R> traceFn3(
    @Suppress("UNUSED_PARAMETER") fn: (A, B, C) -> R,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {
}
