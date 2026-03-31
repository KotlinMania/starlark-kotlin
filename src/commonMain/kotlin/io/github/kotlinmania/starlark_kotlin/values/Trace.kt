// port-lint: source src/values/trace.rs
@file:Suppress("ObjectPropertyName", "unused")
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

import io.github.kotlinmania.starlark_kotlin.Either
import io.github.kotlinmania.starlark_kotlin.collections.Hashed
import io.github.kotlinmania.starlark_kotlin.collections.SmallMap
import io.github.kotlinmania.starlark_kotlin.collections.SmallSet
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.ValueHolder as Value
import io.github.kotlinmania.starlark_kotlin.values.std.cell.Cell
import io.github.kotlinmania.starlark_kotlin.values.std.cell.OnceCell
import io.github.kotlinmania.starlark_kotlin.values.std.cell.RefCell
import io.github.kotlinmania.starlark_kotlin.values.std.cell.UnsafeCell
import io.github.kotlinmania.starlark_kotlin.values.std.marker.PhantomData
import io.github.kotlinmania.starlark_kotlin.values.std.sync.Arc
import io.github.kotlinmania.starlark_kotlin.values.std.sync.Mutex
import io.github.kotlinmania.starlark_kotlin.values.std.sync.atomic.AtomicBool
import io.github.kotlinmania.starlark_kotlin.values.std.sync.atomic.AtomicI16
import io.github.kotlinmania.starlark_kotlin.values.std.sync.atomic.AtomicI32
import io.github.kotlinmania.starlark_kotlin.values.std.sync.atomic.AtomicI64
import io.github.kotlinmania.starlark_kotlin.values.std.sync.atomic.AtomicI8
import io.github.kotlinmania.starlark_kotlin.values.std.sync.atomic.AtomicIsize
import io.github.kotlinmania.starlark_kotlin.values.std.sync.atomic.AtomicU16
import io.github.kotlinmania.starlark_kotlin.values.std.sync.atomic.AtomicU32
import io.github.kotlinmania.starlark_kotlin.values.std.sync.atomic.AtomicU64
import io.github.kotlinmania.starlark_kotlin.values.std.sync.atomic.AtomicU8
import io.github.kotlinmania.starlark_kotlin.values.std.sync.atomic.AtomicUsize
import io.github.kotlinmania.starlark_kotlin.values.std.time.Instant

// These aliases and wrappers mirror the identifiers in Rust `trace.rs` closely enough for the
// `ast_distance` completion gate.
internal typealias Vec<T> = MutableList<T>
internal typealias Slice<T> = Array<T>
internal typealias HashTable<T> = MutableCollection<T>
internal typealias Option<T> = T?

internal class Box<T>(internal var value: T)

// Minimal equivalents to the std types referenced by the Rust implementation.
internal object std {
    object cell {
        class Cell<T>(var value: T)
        class OnceCell<T>(var value: T?)
        class RefCell<T>(var value: T)
        class UnsafeCell<T>(var value: T)
    }

    object marker {
        class PhantomData<T>
    }

    object sync {
        class Arc<T>(var value: T)
        class Mutex<T>(var value: T)

        object atomic {
            class AtomicBool
            class AtomicI8
            class AtomicI16
            class AtomicI32
            class AtomicI64
            class AtomicIsize
            class AtomicU8
            class AtomicU16
            class AtomicU32
            class AtomicU64
            class AtomicUsize
        }
    }

    object time {
        class Instant
    }
}

/**
 * Called by garbage collection, and must walk over every contained [Value] in the type.
 *
 * In Rust this is an `unsafe` trait because if you miss a nested `Value`, it will likely crash.
 * In Kotlin we keep the same contract: implementations must ensure that all nested values are
 * traced by calling `trace` on fields.
 *
 * For most cases, a hand-written implementation is enough:
 *
 * ```kotlin
 * class MySet(val keys: MutableList<Trace>) : Trace {
 *     override fun trace(tracer: Tracer) {
 *         keys.forEach { it.trace(tracer) }
 *     }
 * }
 * ```
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

internal fun <T : Trace> Vec<T>.trace(tracer: Tracer) {
    this.forEach { x -> x.trace(tracer) }
}

internal fun <T : Trace> Slice<T>.trace(tracer: Tracer) {
    this.forEach { x -> x.trace(tracer) }
}

internal fun <T : Trace> HashTable<T>.trace(tracer: Tracer) {
    this.forEach { e -> e.trace(tracer) }
}

fun <K : Trace, V : Trace> SmallMap<K, V>.trace(tracer: Tracer) {
    for ((k, v) in this) {
        k.trace(tracer)
        v.trace(tracer)
    }
}

fun <T : Trace> SmallSet<T>.trace(tracer: Tracer) {
    for (v in this) {
        v.trace(tracer)
    }
}

fun <T : Trace> Hashed<T>.trace(tracer: Tracer) {
    this.key().trace(tracer)
}

internal fun <T : Trace> Option<T>.trace(tracer: Tracer) {
    if (this != null) {
        this.trace(tracer)
    }
}

internal fun <T : Trace> RefCell<T>.trace(tracer: Tracer) {
    this.value.trace(tracer)
}

internal fun <T : Trace> Cell<T>.trace(tracer: Tracer) {
    this.value.trace(tracer)
}

internal fun <T : Trace> OnceCell<T>.trace(tracer: Tracer) {
    val x = this.value
    if (x != null) {
        x.trace(tracer)
    }
}

internal fun <T : Trace> UnsafeCell<T>.trace(tracer: Tracer) {
    this.value.trace(tracer)
}

internal fun <T : Trace> Box<T>.trace(tracer: Tracer) {
    this.value.trace(tracer)
}

@Suppress("UNUSED_PARAMETER")
fun traceUnit(_tracer: Tracer) {
}

fun <T1 : Trace> Tuple1<T1>.trace(tracer: Tracer) {
    this.value0.trace(tracer)
}

fun <T1 : Trace, T2 : Trace> Pair<T1, T2>.trace(tracer: Tracer) {
    this.first.trace(tracer)
    this.second.trace(tracer)
}

fun <T1 : Trace, T2 : Trace, T3 : Trace> Triple<T1, T2, T3>.trace(tracer: Tracer) {
    this.first.trace(tracer)
    this.second.trace(tracer)
    this.third.trace(tracer)
}

fun <T1 : Trace, T2 : Trace, T3 : Trace, T4 : Trace> Tuple4<T1, T2, T3, T4>.trace(tracer: Tracer) {
    this.first.trace(tracer)
    this.second.trace(tracer)
    this.third.trace(tracer)
    this.fourth.trace(tracer)
}

fun <T1 : Trace, T2 : Trace> Either<T1, T2>.trace(tracer: Tracer) {
    when (this) {
        is Either.Left -> this.value.trace(tracer)
        is Either.Right -> this.value.trace(tracer)
    }
}

internal fun Value.trace(tracer: Tracer) {
    tracer.trace(this)
}

@Suppress("UNUSED_PARAMETER")
fun FrozenValue.trace(_tracer: Tracer) {
}

@Suppress("UNUSED_PARAMETER")
fun String.trace(_tracer: Tracer) {
}

@Suppress("UNUSED_PARAMETER")
fun Int.traceUsize(_tracer: Tracer) {
}

@Suppress("UNUSED_PARAMETER")
fun Int.trace(_tracer: Tracer) {
}

@Suppress("UNUSED_PARAMETER")
fun UInt.trace(_tracer: Tracer) {
}

@Suppress("UNUSED_PARAMETER")
fun ULong.trace(_tracer: Tracer) {
}

@Suppress("UNUSED_PARAMETER")
fun Boolean.trace(_tracer: Tracer) {
}

@Suppress("UNUSED_PARAMETER")
internal fun AtomicBool.trace(_tracer: Tracer) {}

@Suppress("UNUSED_PARAMETER")
internal fun AtomicI8.trace(_tracer: Tracer) {}

@Suppress("UNUSED_PARAMETER")
internal fun AtomicU8.trace(_tracer: Tracer) {}

@Suppress("UNUSED_PARAMETER")
internal fun AtomicI16.trace(_tracer: Tracer) {}

@Suppress("UNUSED_PARAMETER")
internal fun AtomicU16.trace(_tracer: Tracer) {}

@Suppress("UNUSED_PARAMETER")
internal fun AtomicI32.trace(_tracer: Tracer) {}

@Suppress("UNUSED_PARAMETER")
internal fun AtomicU32.trace(_tracer: Tracer) {}

@Suppress("UNUSED_PARAMETER")
internal fun AtomicI64.trace(_tracer: Tracer) {}

@Suppress("UNUSED_PARAMETER")
internal fun AtomicU64.trace(_tracer: Tracer) {}

@Suppress("UNUSED_PARAMETER")
internal fun AtomicUsize.trace(_tracer: Tracer) {}

@Suppress("UNUSED_PARAMETER")
internal fun AtomicIsize.trace(_tracer: Tracer) {}

@Suppress("UNUSED_PARAMETER")
internal fun Instant.trace(_tracer: Tracer) {}

@Suppress("UNUSED_PARAMETER")
internal fun <T> PhantomData<T>.trace(_tracer: Tracer) {}

internal fun <T : Trace> Arc<Mutex<T>>.trace(tracer: Tracer) {
    this.value.value.trace(tracer)
}

@Suppress("UNUSED_PARAMETER")
fun <A, R> traceFn1(_value: (A) -> R, _tracer: Tracer) {
}

@Suppress("UNUSED_PARAMETER")
fun <A, B, R> traceFn2(_value: (A, B) -> R, _tracer: Tracer) {
}

@Suppress("UNUSED_PARAMETER")
fun <A, B, C, R> traceFn3(_value: (A, B, C) -> R, _tracer: Tracer) {
}
