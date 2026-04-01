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

import io.github.kotlinmania.starlark_kotlin.Either
import io.github.kotlinmania.starlark_kotlin.collections.Hashed
import io.github.kotlinmania.starlark_kotlin.collections.SmallMap
import io.github.kotlinmania.starlark_kotlin.collections.small_set.SmallSet
import io.github.kotlinmania.starlark_kotlin.values.layout.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Tracer
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.ValueHolder as Value
import io.github.kotlinmania.starlark_kotlin.util.refcell.RefCell

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

internal fun <T : Trace> MutableList<T>.trace(tracer: Tracer) {
    for (x in this) {
        x.trace(tracer)
    }
}

internal fun <T : Trace> Array<T>.trace(tracer: Tracer) {
    for (x in this) {
        x.trace(tracer)
    }
}

internal fun <T : Trace> MutableCollection<T>.trace(tracer: Tracer) {
    for (e in this) {
        e.trace(tracer)
    }
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

internal fun <T : Trace> T?.trace(tracer: Tracer) {
    if (this != null) {
        this.trace(tracer)
    }
}

internal fun <T : Trace> RefCell<T>.trace(tracer: Tracer) {
    this.getMut().trace(tracer)
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
fun <A, R> ((A) -> R).trace(_tracer: Tracer) {
}

@Suppress("UNUSED_PARAMETER")
fun <A, B, R> ((A, B) -> R).trace(_tracer: Tracer) {
}

@Suppress("UNUSED_PARAMETER")
fun <A, B, C, R> ((A, B, C) -> R).trace(_tracer: Tracer) {
}
