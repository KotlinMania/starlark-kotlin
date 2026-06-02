// port-lint: source src/values/types/string/intern/interner.rs
package io.github.kotlinmania.starlark.values.types.string.intern

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

// Generic interner for starlark strings.


import io.github.kotlinmania.starlark.collections.Hashed
import io.github.kotlinmania.starlark.values.Trace
import io.github.kotlinmania.starlark.values.layout.heap.Tracer
import io.github.kotlinmania.starlark.values.layout.typed.FrozenStringValue
import io.github.kotlinmania.starlark.values.layout.typed.StringValue

/**
 * [FrozenStringValue] interner.
 *
 * Caches frozen string allocations so that identical strings share the same value.
 */
//     map: HashTable<FrozenStringValue>,
// }
internal class FrozenStringValueInterner {
    // HashTable<FrozenStringValue> in Rust.
    // In Kotlin, we use a HashMap keyed by hash+content for O(1) lookup.
    private val map: HashMap<ULong, MutableList<FrozenStringValue>> = HashMap()

    fun intern(
        s: Hashed<String>,
        alloc: () -> FrozenStringValue,
    ): FrozenStringValue {
        val hash = s.hash().promote()
        val bucket = map[hash]
        if (bucket != null) {
            for (existing in bucket) {
                if (s == existing.getHashedStr()) {
                    return existing
                }
            }
        }

        // Not found, allocate new and insert
        val frozenString = alloc()
        map.getOrPut(hash) { mutableListOf() }.add(frozenString)
        return frozenString
    }

    companion object {
        fun default(): FrozenStringValueInterner = FrozenStringValueInterner()
    }
}

/**
 * [StringValue] interner.
 *
 * Caches string allocations so that identical strings share the same value.
 */
//     map: HashTable<StringValue<'v>>,
// }
internal class StringValueInterner : Trace {
    // HashTable<StringValue> in Rust.
    // In Kotlin, we use a HashMap keyed by hash for O(1) lookup.
    private val map: HashMap<ULong, MutableList<StringValue>> = HashMap()

    fun intern(
        s: Hashed<String>,
        alloc: () -> StringValue,
    ): StringValue {
        val hash = s.hash().promote()
        val bucket = map[hash]
        if (bucket != null) {
            for (existing in bucket) {
                if (s == existing.getHashedStr()) {
                    return existing
                }
            }
        }

        // Not found, allocate new and insert
        val stringValue = alloc()
        map.getOrPut(hash) { mutableListOf() }.add(stringValue)
        return stringValue
    }

    // In Rust, this walks the HashTable and traces each StringValue's inner Value.
    // In Kotlin, the GC handles reference tracking, so this is effectively a no-op.
    // We keep the method for structural parity with the Rust Trace derive.
    override fun trace(
        @Suppress("unused") tracer: Tracer,
    ) {
        // Kotlin's GC manages StringValue references automatically.
        // No manual pointer adjustment needed.
    }

    companion object {
        fun default(): StringValueInterner = StringValueInterner()
    }
}
