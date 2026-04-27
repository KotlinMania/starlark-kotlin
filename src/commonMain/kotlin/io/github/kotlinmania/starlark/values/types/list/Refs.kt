// port-lint: source src/values/types/list/refs.rs
package io.github.kotlinmania.starlark.values.types.list

/*
 * Copyright 2018 The Starlark in Rust Authors.
 * Copyright (c) Facebook, Inc. and its affiliates.
 * Copyright (c) 2025 Sydney Renee, The Solace Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not import this file except in compliance with the License.
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

import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark.values.UnpackValue
import io.github.kotlinmania.starlark.values.layout.Value

/**
 * Reference to list content (mutable or frozen).
 *
 * Wraps a read-only view of the list elements as [Value] references.
 * Both mutable and frozen lists can be viewed through [ListRef].
 *
 */
class ListRef private constructor(
    private val elements: List<Value>,
) {
    companion object {
        /** `type([])`, which is `"list"`. */
        const val TYPE: String = ListData.TYPE

        /** Create a new [ListRef] wrapping the given elements. */
        internal fun new(slice: List<Value>): ListRef = ListRef(slice)

        /** Empty list reference. */
        fun empty(): ListRef = ListRef(emptyList())

        /**
         * Downcast a [Value] to a [ListRef].
         *
         * Works for both mutable lists ([ListGen]<[ListData]>) and
         * frozen lists ([ListGen]<[FrozenListData]>).
         *
         * Returns `null` if the value is not a list.
         *
         */
        fun fromValue(x: Value): ListRef? {
            if (x.unpackFrozen() != null) {
                val gen = x.downcastRef<ListGen<*>>() ?: return null
                val data = gen.data as? FrozenListData ?: return null
                return new(data.content().map { it.toValue() })
            } else {
                val gen = x.downcastRef<ListGen<*>>() ?: return null
                val data = gen.data as? ListData ?: return null
                return new(data.content())
            }
        }

        /**
         * Downcast a [FrozenValue] to a [ListRef].
         *
         * Returns `null` if the frozen value is not a list.
         *
         */
        fun fromFrozenValue(x: FrozenValue): ListRef? {
            val gen = x.downcastRef<ListGen<*>>() ?: return null
            val data = gen.data as? FrozenListData ?: return null
            return new(data.content().map { it.toValue() })
        }
    }

    /** List elements. */
    fun content(): List<Value> = elements

    /** Number of elements. */
    fun len(): Int = elements.size

    /** Iterate over the elements in the list. */
    fun iter(): Iterator<Value> = elements.iterator()

    /** Get the element at the given index, or null if out of bounds. */
    operator fun get(index: Int): Value? = elements.getOrNull(index)

    /**
     * Get a sublist for the given range, or null if the range is invalid.
     *
     * In Kotlin, we clamp and return a subList.
     */
    fun get(range: IntRange): List<Value>? {
        val start = maxOf(0, range.first)
        val end = minOf(elements.size, range.last + 1)
        if (start > end) return null
        return elements.subList(start, end)
    }

    /**
     * Returns a [List] view of this reference's content.
     *
     */
    fun asList(): List<Value> = elements

    /**
     * Display implementation.
     *
     */
    override fun toString(): String = displayList(elements)
}

/**
 * Reference to frozen list content.
 *
 * Wraps a read-only view of the frozen list elements as [FrozenValue] references.
 *
 */
class FrozenListRef private constructor(
    private val elements: List<FrozenValue>,
) {
    companion object {
        /** `type([])`, which is `"list"`. */
        const val TYPE: String = ListRef.TYPE

        /** Create a new [FrozenListRef] wrapping the given elements. */
        internal fun new(slice: List<FrozenValue>): FrozenListRef = FrozenListRef(slice)

        /**
         * Downcast to the frozen list.
         *
         * This function returns `null` if the value is not a list or the list is not frozen.
         *
         */
        fun fromValue(x: Value): FrozenListRef? {
            val frozen = x.unpackFrozen() ?: return null
            return fromFrozenValue(frozen)
        }

        /**
         * Downcast to the frozen list.
         *
         * This function returns `null` if the value is not a frozen list.
         * (Value cannot be a mutable list because value is frozen.)
         *
         */
        fun fromFrozenValue(x: FrozenValue): FrozenListRef? {
            val gen = x.downcastRef<ListGen<*>>() ?: return null
            val data = gen.data as? FrozenListData ?: return null
            return new(data.contentFrozen())
        }
    }

    /** Frozen list elements. */
    fun content(): List<FrozenValue> = elements

    /** Number of elements. */
    fun len(): Int = elements.size

    /** Iterate over the frozen list elements. */
    fun iter(): Iterator<FrozenValue> = elements.iterator()

    /**
     * Returns a [List] view of this reference's content.
     *
     * where `type Target = [FrozenValue]`.
     */
    fun asList(): List<FrozenValue> = elements

    /**
     * Display implementation.
     *
     */
    override fun toString(): String = displayList(elements.map { it.toValue() })
}

// -- Deref implementations (structural equivalents) ---------------------------

/**
 *
 * be used directly as a slice. In Kotlin, the equivalent is calling
 * [ListRef.asList] or [ListRef.content].
 */
object ListRefDeref {
    /** The target type is `List<Value>`. */
    fun deref(ref: ListRef): List<Value> = ref.content()
}

object FrozenListRefDeref {
    /** The target type is `List<FrozenValue>`. */
    fun deref(ref: FrozenListRef): List<FrozenValue> = ref.content()
}

// -- Display implementations (structural equivalents) -------------------------

/**
 *
 * Formats the list reference as a Starlark list literal `[a, b, c]`.
 */
object ListRefDisplay {
    fun fmt(ref: ListRef): String = displayList(ref.content())
}

/**
 *
 * Formats the frozen list reference as a Starlark list literal.
 */
object FrozenListRefDisplay {
    fun fmt(ref: FrozenListRef): String = displayList(ref.content().map { it.toValue() })
}

// -- StarlarkTypeRepr implementations -----------------------------------------

object ListRefStarlarkTypeRepr : StarlarkTypeRepr {
    override fun starlarkTypeRepr(): Ty = Ty.anyList()
}

object FrozenListRefStarlarkTypeRepr : StarlarkTypeRepr {
    override fun starlarkTypeRepr(): Ty = Ty.anyList()
}

// -- UnpackValue implementations ----------------------------------------------

/**
 * [UnpackValue] for [ListRef].
 *
 */
object ListRefUnpackValue : UnpackValue<ListRef> {
    override fun starlarkTypeRepr(): Ty = Ty.anyList()

    override fun unpackValueImpl(value: Value): Result<ListRef?> {
        return Result.success(ListRef.fromValue(value))
    }
}

/**
 * [UnpackValue] for [FrozenListRef].
 *
 */
object FrozenListRefUnpackValue : UnpackValue<FrozenListRef> {
    override fun starlarkTypeRepr(): Ty = Ty.anyList()

    override fun unpackValueImpl(value: Value): Result<FrozenListRef?> {
        return Result.success(FrozenListRef.fromValue(value))
    }
}
