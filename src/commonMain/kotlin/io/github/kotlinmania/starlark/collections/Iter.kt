// port-lint: source src/iter.rs
package io.github.kotlinmania.starlark.collections

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

// Rust defines helper macros in this file (`def_iter`, `def_double_ended_iter`) for iterator boilerplate.
// Kotlin does not have macros, so we model the same behaviour with small wrapper classes.

internal abstract class DefIter<T, R>(
    protected val iter: Iterator<T>,
) {
    protected abstract fun map(item: T): R

    fun next(): R? =
        if (iter.hasNext()) {
            map(iter.next())
        } else {
            null
        }

    fun nth(n: Int): R? {
        var i = 0
        while (i < n && iter.hasNext()) {
            iter.next()
            i += 1
        }
        return next()
    }

    fun last(): R? {
        var last: R? = null
        while (iter.hasNext()) {
            last = map(iter.next())
        }
        return last
    }

    fun sizeHint(): Pair<Int, Int?> {
        // Kotlin iterators do not expose a size hint, so return unknown upper bound.
        return Pair(0, null)
    }

    fun count(): Int {
        var count = 0
        while (iter.hasNext()) {
            iter.next()
            count += 1
        }
        return count
    }

    fun <C : MutableCollection<R>> collect(into: C): C {
        while (iter.hasNext()) {
            into.add(map(iter.next()))
        }
        return into
    }
}

internal abstract class DefDoubleEndedIter<T, R>(
    protected val iter: ListIterator<T>,
) {
    protected abstract fun map(item: T): R

    fun nextBack(): R? =
        if (iter.hasPrevious()) {
            map(iter.previous())
        } else {
            null
        }
}
