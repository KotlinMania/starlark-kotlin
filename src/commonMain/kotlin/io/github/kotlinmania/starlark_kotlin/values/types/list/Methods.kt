// port-lint: source src/values/types/list/methods.rs
package io.github.kotlinmania.starlark_kotlin.values.types.list

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
 * Methods for the `list` type.
 */

// Placeholder types until the actual implementations are ported
expect class MethodsBuilder
expect class Heap<'v>
expect class Value<'v> {
    fun ptrEq(other: Value<'v>): Boolean
}
expect class ValueError {
    companion object {
        fun IndexOutOfBound(index: Int): ValueError
    }
}
expect class ValueOfUnchecked<'v, T>
expect class NoneOr<T> {
    fun intoOption(): T?

    companion object {
        fun <T> None(): NoneOr<T>
    }
}
expect class StarlarkIter<T>
expect class NoneType

// Placeholder for convert_indices functions
expect fun convertIndex(len: Int, index: Int): Int
expect fun convertIndices(len: Int, start: Int?, end: Int?): Pair<Int, Int>

/**
 * Register list methods.
 *
 * This is the Kotlin port of the Rust `#[starlark_module]` annotated function.
 */
internal fun listMethods(builder: MethodsBuilder) {
    // The implementations below would be registered through the MethodsBuilder
    // when it's properly ported.
}

/**
 * [list.append](
 * https://github.com/bazelbuild/starlark/blob/master/spec.md#list·append
 * ): append an element to a list.
 *
 * `L.append(x)` appends `x` to the list L, and returns `None`.
 *
 * `append` fails if the list is frozen or has active iterators.
 *
 * ```
 * # starlark::assert::is_true(r#"
 * x = []
 * x.append(1)
 * x.append(2)
 * x.append(3)
 * x == [1, 2, 3]
 * # "#);
 * ```
 */
internal fun <'v> append(
    thisValue: Value<'v>,
    el: Value<'v>,
    heap: Heap<'v>,
): Result<NoneType> {
    val thisList = ListData.fromValueMut(thisValue).getOrElse { return Result.failure(it) }
    thisList.push(el, heap)
    return Result.success(NoneType)
}

/**
 * [list.clear](
 * https://github.com/bazelbuild/starlark/blob/master/spec.md#list·clear
 * ): clear a list
 *
 * `L.clear()` removes all the elements of the list L and returns `None`.
 * It fails if the list is frozen or if there are active iterators.
 *
 * ```
 * # starlark::assert::is_true(r#"
 * x = [1, 2, 3]
 * x.clear()
 * x == []
 * # "#);
 * ```
 */
internal fun <'v> clear(thisValue: Value<'v>): Result<NoneType> {
    val thisList = ListData.fromValueMut(thisValue).getOrElse { return Result.failure(it) }
    thisList.clear()
    return Result.success(NoneType)
}

/**
 * [list.extend](
 * https://github.com/bazelbuild/starlark/blob/master/spec.md#list·extend
 * ): extend a list with another iterable's content.
 *
 * `L.extend(x)` appends the elements of `x`, which must be iterable, to
 * the list L, and returns `None`.
 *
 * `extend` fails if `x` is not iterable, or if the list L is frozen or has
 * active iterators.
 *
 * ```
 * # starlark::assert::is_true(r#"
 * x = []
 * x.extend([1, 2, 3])
 * x.extend(["foo"])
 * x == [1, 2, 3, "foo"]
 * # "#);
 * ```
 */
internal fun <'v> extend(
    thisValue: Value<'v>,
    other: ValueOfUnchecked<'v, StarlarkIter<Value<'v>>>,
    heap: Heap<'v>,
): Result<NoneType> {
    val res = ListData.fromValueMut(thisValue).getOrElse { return Result.failure(it) }
    if (thisValue.ptrEq(other.get())) {
        // If the types alias, we can't borrow the `other` for iteration.
        // But we can do something smarter to double the elements
        res.double(heap)
    } else {
        val it = other.get().iterate(heap).getOrElse { return Result.failure(it) }
        res.extend(it, heap)
    }
    return Result.success(NoneType)
}

/**
 * [list.index](
 * https://github.com/bazelbuild/starlark/blob/master/spec.md#list·index
 * ): get the index of an element in the list.
 *
 * `L.index(x[, start[, end]])` finds `x` within the list L and returns its
 * index.
 *
 * The optional `start` and `end` parameters restrict the portion of
 * list L that is inspected.  If provided and not `None`, they must be list
 * indices of type `int`. If an index is negative, `len(L)` is effectively
 * added to it, then if the index is outside the range `[0:len(L)]`, the
 * nearest value within that range is used; see [Indexing](#indexing).
 *
 * `index` fails if `x` is not found in L, or if `start` or `end`
 * is not a valid index (`int` or `None`).
 *
 * ```
 * # starlark::assert::is_true(r#"
 * x = ["b", "a", "n", "a", "n", "a"]
 * # (
 * x.index("a") == 1      # bAnana
 * # and
 * x.index("a", 2) == 3   # banAna
 * # and
 * x.index("a", -2) == 5  # bananA
 * # )"#);
 * ```
 */
internal fun <'v> index(
    thisRef: ListRef<'v>,
    needle: Value<'v>,
    start: NoneOr<Int> = NoneOr.None(),
    end: NoneOr<Int> = NoneOr.None(),
): Result<Int> {
    val (startIdx, endIdx) =
        convertIndices(thisRef.len(), start.intoOption(), end.intoOption())
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
 * [list.insert](
 * https://github.com/bazelbuild/starlark/blob/master/spec.md#list·insert
 * ): insert an element in a list.
 *
 * `L.insert(i, x)` inserts the value `x` in the list L at index `i`,
 * moving higher-numbered elements along by one.  It returns `None`.
 *
 * As usual, the index `i` must be an `int`. If its value is negative,
 * the length of the list is added, then its value is clamped to the
 * nearest value in the range `[0:len(L)]` to yield the effective index.
 *
 * `insert` fails if the list is frozen or has active iterators.
 *
 * ```
 * # starlark::assert::is_true(r#"
 * x = ["b", "c", "e"]
 * x.insert(0, "a")
 * x.insert(-1, "d")
 * x == ["a", "b", "c", "d", "e"]
 * # "#);
 * ```
 */
internal fun <'v> insert(
    thisValue: Value<'v>,
    index: Int,
    el: Value<'v>,
    heap: Heap<'v>,
): Result<NoneType> {
    val thisList = ListData.fromValueMut(thisValue).getOrElse { return Result.failure(it) }
    val idx = convertIndex(thisList.len(), index)
    thisList.insert(idx, el, heap)
    return Result.success(NoneType)
}

/**
 * [list.pop](
 * https://github.com/bazelbuild/starlark/blob/master/spec.md#list·pop
 * ): removes and returns the last element of a list.
 *
 * `L.pop([index])` removes and returns the last element of the list L, or,
 * if the optional index is provided, at that index.
 *
 * `pop` fails if the index is negative or not less than the length of
 * the list, of if the list is frozen or has active iterators.
 *
 * ```
 * # starlark::assert::is_true(r#"
 * x = [1, 2, 3]
 * # (
 * x.pop() == 3
 * # and
 * x.pop() == 2
 * # and
 * x == [1]
 * # )"#);
 * ```
 */
internal fun <'v> pop(
    thisValue: Value<'v>,
    index: Int?,
): Result<Value<'v>> {
    val thisList = ListData.fromValueMut(thisValue).getOrElse { return Result.failure(it) }
    val idx = index ?: (thisList.len() - 1)
    if (idx < 0 || idx >= thisList.len()) {
        return Result.failure(ValueError.IndexOutOfBound(idx))
    }
    return Result.success(thisList.remove(idx))
}

/**
 * [list.remove](
 * https://github.com/bazelbuild/starlark/blob/master/spec.md#list·remove
 * ): remove a value from a list
 *
 * `L.remove(x)` removes the first occurrence of the value `x` from the
 * list L, and returns `None`.
 *
 * `remove` fails if the list does not contain `x`, is frozen, or has
 * active iterators.
 *
 * ```
 * # starlark::assert::is_true(r#"
 * x = [1, 2, 3, 2]
 * x.remove(2)
 * # t = (
 * x == [1, 3, 2]
 * # )
 * x.remove(2)
 * # (t and (
 * x == [1, 3]
 * # ))"#);
 * ```
 *
 * A subsequent call to `x.remove(2)` would yield an error because the
 * element won't be found.
 *
 * ```
 * # starlark::assert::fail(r#"
 * x = [1, 2, 3, 2]
 * x.remove(2)
 * x.remove(2)
 * x.remove(2) # error: not found
 * # "#, "not found");
 * ```
 */
internal fun <'v> remove(
    thisValue: Value<'v>,
    needle: Value<'v>,
): Result<NoneType> {
    // Written in two separate blocks so we ensure we give up the
    // immutable borrow before making the mutable borrow.
    val position = run {
        val thisRef = ListRef.fromValue(thisValue) ?: return Result.failure(
            IllegalArgumentException("Value is not a list")
        )
        val pos = thisRef.iter().withIndex().firstOrNull { (_, v) -> v == needle }?.index
        pos ?: return Result.failure(
            IllegalArgumentException("Element '$needle' not found in list '$thisRef'")
        )
    }
    run {
        // now mutate it with no further value calls
        val thisList = ListData.fromValueMut(thisValue).getOrElse { return Result.failure(it) }
        thisList.remove(position)
        return Result.success(NoneType)
    }
}

// Note: Tests would be ported to the commonTest directory following Kotlin Multiplatform conventions
// The original Rust tests from methods.rs would go in:
// src/commonTest/kotlin/io/github/kotlinmania/starlark_kotlin/values/types/list/MethodsTest.kt
