package io.github.kotlinmania.starlark.collections

/**
 * Minimal stand-in for Rust's `hashbrown::HashTable<T>`.
 *
 * The Rust implementation is a low-level hash table with customizable hashing and probing.
 * For the Kotlin port we only need a container that preserves the "iterate mutably and visit all elements"
 * behavior for blanket implementations like `Trace`.
 */
class HashTable<T>(
    private val values: MutableList<T>,
) : MutableIterable<T> {
    companion object {
        fun <T> new(): HashTable<T> = HashTable(mutableListOf())
    }

    override fun iterator(): MutableIterator<T> = values.iterator()

    fun iterMut(): MutableIterable<T> = values
}
