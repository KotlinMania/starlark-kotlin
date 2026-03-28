// port-lint: source src/values/types/string/intern/interner.rs
package io.github.kotlinmania.starlark_kotlin.values.types.string.intern

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

/**
 * Generic interner for starlark strings.
 */

// Import statements - these types should be defined in their respective modules
// For now, using placeholder imports assuming standard structure
// import io.github.kotlinmania.starlark_kotlin.collections.Hashed
import io.github.kotlinmania.starlark_kotlin.values.layout.typed.FrozenStringValue
// import io.github.kotlinmania.starlark_kotlin.values.layout.typed.StringValue
// import io.github.kotlinmania.starlark_kotlin.values.Trace

/**
 * [FrozenStringValue] interner.
 */
internal class FrozenStringValueInterner {
    private val map: HashTable<FrozenStringValue> = HashTable()

    fun intern(
        s: Hashed<String>,
        alloc: () -> FrozenStringValue
    ): FrozenStringValue {
        // Find existing entry with matching hash and content
        val found = map.find(s.hash().promote()) { x ->
            s == x.getHashedStr()
        }

        return if (found != null) {
            found
        } else {
            // Not found, allocate new and insert
            val frozenString = alloc()
            map.insertUnique(s.hash().promote(), frozenString) { x ->
                x.getHash().promote()
            }
            frozenString
        }
    }

    companion object {
        fun default(): FrozenStringValueInterner = FrozenStringValueInterner()
    }
}

/**
 * [StringValue] interner with lifetime parameter.
 */
internal class StringValueInterner<V> : Trace<V> {
    private val map: HashTable<StringValue<V>> = HashTable()

    fun intern(
        s: Hashed<String>,
        alloc: () -> StringValue<V>
    ): StringValue<V> {
        // Find existing entry with matching hash and content
        val found = map.find(s.hash().promote()) { x ->
            s == x.getHashedStr()
        }

        return if (found != null) {
            found
        } else {
            // Not found, allocate new and insert
            val stringValue = alloc()
            map.insertUnique(s.hash().promote(), stringValue) { x ->
                x.getHash().promote()
            }
            stringValue
        }
    }

    override fun trace(tracer: Tracer<V>) {
        // Trace all values in the hash table for garbage collection
        map.forEach { value ->
            value.trace(tracer)
        }
    }

    companion object {
        fun <V> default(): StringValueInterner<V> = StringValueInterner()
    }
}

// Placeholder type aliases and interfaces until the actual types are ported
// These should be removed once the proper types are available from their modules

/**
 * Placeholder for HashTable from hashbrown crate.
 * A hash table that stores values with hash-based lookup.
 */
internal class HashTable<T> {
    private val storage: MutableList<Entry<T>> = mutableListOf()

    /**
     * Find an entry with the given hash and matching predicate.
     */
    fun find(hash: ULong, predicate: (T) -> Boolean): T? {
        return storage.firstOrNull { it.hash == hash && predicate(it.value) }?.value
    }

    /**
     * Insert a value with unique hash.
     * The hasher function extracts the hash from the value for storage.
     */
    fun insertUnique(hash: ULong, value: T, hasher: (T) -> ULong) {
        storage.add(Entry(hasher(value), value))
    }

    /**
     * Iterate over all values.
     */
    fun forEach(action: (T) -> Unit) {
        storage.forEach { action(it.value) }
    }

    private data class Entry<T>(val hash: ULong, val value: T)
}

/**
 * Placeholder for Hashed<T> from collections module.
 */
interface Hashed<T> {
    fun hash(): StarlarkHashValue
    fun value(): T
}

/**
 * Placeholder for StarlarkHashValue.
 */
internal interface StarlarkHashValue {
    fun promote(): ULong
}


/**
 * Placeholder for StringValue with lifetime.
 */
internal interface StringValue<V> : Traceable<V> {
    fun getHashedStr(): Hashed<String>
    fun getHash(): StarlarkHashValue
}

/**
 * Placeholder for Trace trait.
 */
internal interface Trace<V> {
    fun trace(tracer: Tracer<V>)
}

/**
 * Placeholder for Traceable marker.
 */
internal interface Traceable<V> {
    fun trace(tracer: Tracer<V>)
}

/**
 * Placeholder for Tracer.
 */
internal interface Tracer<V>
