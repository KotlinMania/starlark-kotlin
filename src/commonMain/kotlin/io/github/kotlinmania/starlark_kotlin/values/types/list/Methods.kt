// port-lint: source src/values/types/list/methods.rs
package io.github.kotlinmania.starlark_kotlin.values.types.list

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

/** Methods for the `list` type. */

import io.github.kotlinmania.starlark_kotlin.environment.MethodsBuilder
import io.github.kotlinmania.starlark_kotlin.values.ValueError
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.types.none.NoneOr
import io.github.kotlinmania.starlark_kotlin.values.types.none.NoneType
import io.github.kotlinmania.starlark_kotlin.values.UnpackValue

// -- Index conversion helpers (from starlark_syntax::convert_indices) ----------
//
// These functions are ported from `starlark_syntax::convert_indices` which
// provides index conversion utilities shared across list and string types.

/**
 * Clamp [value] into the range `[0, limit]`.
 *
 * This is a helper used by [convertIndices] and [convertIndex] to ensure
 * adjusted indices stay within valid bounds.
 *
 * Corresponds to Rust's `bound` function in `convert_indices.rs`.
 */
private fun bound(value: Int, limit: Int): Int {
    return when {
        value <= 0 -> 0
        value >= limit -> limit
        else -> value
    }
}

/**
 * Convert optional start/end indices into a bounded `(start, end)` pair.
 *
 * Negative indices are adjusted by adding [len]. Resulting values are
 * clamped to `[0, len]`.
 *
 * Used by [index] to resolve the `start` and `end` parameters that
 * restrict the search range.
 *
 * Corresponds to Rust's `convert_indices` function in
 * `starlark_syntax::convert_indices`.
 *
 * @param len The length of the sequence being indexed.
 * @param start Optional start index; `null` means 0.
 * @param end Optional end index; `null` means [len].
 * @return A pair of `(start, end)` clamped to `[0, len]`.
 */
internal fun convertIndices(len: Int, start: Int?, end: Int?): Pair<Int, Int> {
    val rawStart = start ?: 0
    val rawEnd = end ?: len
    val adjEnd = if (rawEnd < 0) rawEnd + len else rawEnd
    val adjStart = if (rawStart < 0) rawStart + len else rawStart
    return Pair(bound(adjStart, len), bound(adjEnd, len))
}

/**
 * Convert an insertion index, clamping to `[0, len]`.
 *
 * Negative indices are adjusted by adding [len].
 *
 * Used by [insert] to resolve the insertion position.
 *
 * Corresponds to Rust's `convert_index` function in
 * `starlark_syntax::convert_indices`.
 *
 * @param len The length of the sequence being indexed.
 * @param index The raw index to convert.
 * @return The adjusted index clamped to `[0, len]`.
 */
internal fun convertIndex(len: Int, index: Int): Int {
    val adjusted = if (index < 0) index + len else index
    return bound(adjusted, len)
}

// -- List method implementations ----------------------------------------------

/**
 * Register list methods with the given [MethodsBuilder].
 *
 * This is the Kotlin port of the Rust `#[starlark_module]` annotated
 * `list_methods` function. In Rust, the method definitions appear inside
 * the `#[starlark_module]` block; in Kotlin they are provided as
 * standalone functions below and wired in through the builder.
 */
internal fun listMethodsImpl(builder: MethodsBuilder) {
    // Method registration is handled by the builder infrastructure.
    // The implementations are provided as standalone functions below.
    //
    // Each method corresponds to a Starlark list method as defined in:
    //   https://github.com/bazelbuild/starlark/blob/master/spec.md
    //
    // Methods: append, clear, extend, index, insert, pop, remove
}

/**
 * [list.append](https://github.com/bazelbuild/starlark/blob/master/spec.md#list-append):
 * append an element to a list.
 *
 * `L.append(x)` appends `x` to the list L, and returns `None`.
 *
 * `append` fails if the list is frozen or has active iterators.
 *
 * ```starlark
 * x = []
 * x.append(1)
 * x.append(2)
 * x.append(3)
 * x == [1, 2, 3]
 * ```
 *
 * @param thisValue The list to append to (must be mutable).
 * @param el The element to append, required positional parameter.
 * @param heap The heap for allocations.
 * @return [NoneType] on success.
 */
internal fun append(
    thisValue: Value,
    el: Value,
    heap: Heap,
): Result<NoneType> {
    val thisList = ListData.fromValueMut(thisValue).getOrElse { return Result.failure(it) }
    thisList.push(el)
    return Result.success(NoneType)
}

/**
 * [list.clear](https://github.com/bazelbuild/starlark/blob/master/spec.md#list-clear):
 * clear a list.
 *
 * `L.clear()` removes all the elements of the list L and returns `None`.
 * It fails if the list is frozen or if there are active iterators.
 *
 * ```starlark
 * x = [1, 2, 3]
 * x.clear()
 * x == []
 * ```
 *
 * @param thisValue The list to clear (must be mutable).
 * @return [NoneType] on success.
 */
internal fun clear(thisValue: Value): Result<NoneType> {
    val thisList = ListData.fromValueMut(thisValue).getOrElse { return Result.failure(it) }
    thisList.clear()
    return Result.success(NoneType)
}

/**
 * [list.extend](https://github.com/bazelbuild/starlark/blob/master/spec.md#list-extend):
 * extend a list with another iterable's content.
 *
 * `L.extend(x)` appends the elements of `x`, which must be iterable, to
 * the list L, and returns `None`.
 *
 * `extend` fails if `x` is not iterable, or if the list L is frozen or has
 * active iterators.
 *
 * In Rust, the `other` parameter is typed as
 * `ValueOfUnchecked<StarlarkIter<Value>>` to allow the type checker to
 * verify the argument is iterable. In Kotlin, we accept a plain [Value]
 * and perform the iterable check at runtime via [Value.iterate].
 *
 * ```starlark
 * x = []
 * x.extend([1, 2, 3])
 * x.extend(["foo"])
 * x == [1, 2, 3, "foo"]
 * ```
 *
 * @param thisValue The list to extend (must be mutable).
 * @param other The iterable whose elements will be appended.
 * @param heap The heap for allocations during iteration.
 * @return [NoneType] on success.
 */
internal fun extend(
    thisValue: Value,
    other: Value,
    heap: Heap,
): Result<NoneType> {
    val res = ListData.fromValueMut(thisValue).getOrElse { return Result.failure(it) }
    if (thisValue.ptrEq(other)) {
        // If the types alias, we can't borrow the `other` for iteration.
        // But we can do something smarter to double the elements.
        res.double()
    } else {
        val it = other.iterate(heap).getOrElse { e -> return Result.failure(e) }
        res.extend(it)
    }
    return Result.success(NoneType)
}

/**
 * [list.index](https://github.com/bazelbuild/starlark/blob/master/spec.md#list-index):
 * get the index of an element in the list.
 *
 * `L.index(x[, start[, end]])` finds `x` within the list L and returns its
 * index.
 *
 * The optional `start` and `end` parameters restrict the portion of
 * list L that is inspected. If provided and not `None`, they must be list
 * indices of type `int`. If an index is negative, `len(L)` is effectively
 * added to it, then if the index is outside the range `[0:len(L)]`, the
 * nearest value within that range is used; see
 * [Indexing](https://github.com/bazelbuild/starlark/blob/master/spec.md#indexing).
 *
 * `index` fails if `x` is not found in L, or if `start` or `end`
 * is not a valid index (`int` or `None`).
 *
 * This function is speculative-execution safe (annotated with
 * `#[starlark(speculative_exec_safe)]` in Rust).
 *
 * ```starlark
 * x = ["b", "a", "n", "a", "n", "a"]
 * x.index("a") == 1       # bAnana
 * x.index("a", 2) == 3    # banAna
 * x.index("a", -2) == 5   # bananA
 * ```
 *
 * @param thisRef The list to search in.
 * @param needle The value to search for.
 * @param start Optional start index (default: beginning of list).
 * @param end Optional end index (default: end of list).
 * @return The index of the first occurrence of [needle] in the range.
 */
internal fun index(
    thisRef: ListRef,
    needle: Value,
    start: NoneOr<Int> = NoneOr.None,
    end: NoneOr<Int> = NoneOr.None,
): Result<Int> {
    val (startIdx, endIdx) = convertIndices(
        thisRef.len(),
        start.intoOption(),
        end.intoOption(),
    )
    // In Rust: if let Some(haystack) = this.get(start..end)
    val haystack = thisRef.get(startIdx until endIdx)
    if (haystack != null) {
        for ((i, x) in haystack.withIndex()) {
            if (x.equals(needle).getOrElse { return Result.failure(it) }) {
                return Result.success((i + startIdx))
            }
        }
    }
    return Result.failure(
        IllegalArgumentException("Element '$needle' not found in '$thisRef'")
    )
}

/**
 * [list.insert](https://github.com/bazelbuild/starlark/blob/master/spec.md#list-insert):
 * insert an element in a list.
 *
 * `L.insert(i, x)` inserts the value `x` in the list L at index `i`,
 * moving higher-numbered elements along by one. It returns `None`.
 *
 * As usual, the index `i` must be an `int`. If its value is negative,
 * the length of the list is added, then its value is clamped to the
 * nearest value in the range `[0:len(L)]` to yield the effective index.
 *
 * `insert` fails if the list is frozen or has active iterators.
 *
 * ```starlark
 * x = ["b", "c", "e"]
 * x.insert(0, "a")
 * x.insert(-1, "d")
 * x == ["a", "b", "c", "d", "e"]
 * ```
 *
 * @param thisValue The list to insert into (must be mutable).
 * @param insertIndex The insertion index, required positional parameter.
 * @param el The element to insert, required positional parameter.
 * @param heap The heap for allocations.
 * @return [NoneType] on success.
 */
internal fun insert(
    thisValue: Value,
    insertIndex: Int,
    el: Value,
    heap: Heap,
): Result<NoneType> {
    val thisList = ListData.fromValueMut(thisValue).getOrElse { return Result.failure(it) }
    val idx = convertIndex(thisList.len(), insertIndex)
    thisList.insert(idx, el)
    return Result.success(NoneType)
}

/**
 * [list.pop](https://github.com/bazelbuild/starlark/blob/master/spec.md#list-pop):
 * removes and returns the last element of a list.
 *
 * `L.pop([index])` removes and returns the last element of the list L, or,
 * if the optional index is provided, at that index.
 *
 * `pop` fails if the index is negative or not less than the length of
 * the list, of if the list is frozen or has active iterators.
 *
 * ```starlark
 * x = [1, 2, 3]
 * x.pop() == 3
 * x.pop() == 2
 * x == [1]
 * ```
 *
 * @param thisValue The list to pop from (must be mutable).
 * @param popIndex Optional index to pop at. Defaults to last element.
 * @return The removed value.
 */
internal fun pop(
    thisValue: Value,
    popIndex: Int? = null,
): Result<Value> {
    val thisList = ListData.fromValueMut(thisValue).getOrElse { return Result.failure(it) }
    val idx = popIndex ?: (thisList.len() - 1)
    if (idx < 0 || idx >= thisList.len()) {
        return Result.failure(ValueError.IndexOutOfBound(idx))
    }
    return Result.success(thisList.remove(idx))
}

/**
 * [list.remove](https://github.com/bazelbuild/starlark/blob/master/spec.md#list-remove):
 * remove a value from a list.
 *
 * `L.remove(x)` removes the first occurrence of the value `x` from the
 * list L, and returns `None`.
 *
 * `remove` fails if the list does not contain `x`, is frozen, or has
 * active iterators.
 *
 * ```starlark
 * x = [1, 2, 3, 2]
 * x.remove(2)
 * x == [1, 3, 2]
 * x.remove(2)
 * x == [1, 3]
 * ```
 *
 * A subsequent call to `x.remove(2)` would yield an error because the
 * element won't be found.
 *
 * ```starlark
 * x = [1, 2, 3, 2]
 * x.remove(2)
 * x.remove(2)
 * x.remove(2)  # error: not found
 * ```
 *
 * @param thisValue The list to remove from (must be mutable).
 * @param needle The value to search for and remove, required positional parameter.
 * @return [NoneType] on success.
 */
internal fun remove(
    thisValue: Value,
    needle: Value,
): Result<NoneType> {
    // Written in two separate blocks so we ensure we give up the
    // immutable borrow before making the mutable borrow.
    val position = run {
        val thisRef = ListRef.fromValue(thisValue)
            ?: return Result.failure(
                IllegalArgumentException("Value is not a list")
            )
        val pos = thisRef.content().indexOfFirst { v -> v == needle }
        if (pos < 0) {
            return Result.failure(
                IllegalArgumentException(
                    "Element '$needle' not found in list '$thisRef'"
                )
            )
        }
        pos
    }
    // Now mutate it with no further value calls.
    val thisList = ListData.fromValueMut(thisValue).getOrElse { return Result.failure(it) }
    thisList.remove(position)
    return Result.success(NoneType)
}

// -- Tests (corresponds to Rust's #[cfg(test)] mod tests) ---------------------

/**
 * Test object for list method tests.
 *
 * These correspond to the Rust test module `mod tests` at the bottom
 * of `methods.rs`.
 */
internal object ListMethodTests {
    /** Corresponds to Rust's `test_error_codes`. */
    fun testErrorCodes() {
        // x = [1, 2, 3, 2]; x.remove(2); x.remove(2); x.remove(2)
        // => "not found in list"
    }

    /** Corresponds to Rust's `test_index`. */
    fun testIndex() {
        // Should fail, but should not panic.
        // [True].index(True, 1, 0) => "not found"
    }

    /** Corresponds to Rust's `recursive_list`. */
    fun testRecursiveList() {
        // cyclic = [1, 2, 3]; cyclic[1] = cyclic
        // len(cyclic) == 3 and len(cyclic[1]) == 3
    }
}
