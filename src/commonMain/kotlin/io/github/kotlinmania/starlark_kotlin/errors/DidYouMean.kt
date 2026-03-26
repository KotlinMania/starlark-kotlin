// port-lint: source src/errors/did_you_mean.rs
package io.github.kotlinmania.starlark_kotlin.errors

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

/** Spelling suggestions. */

/** Find a suggestion for a typo. */
internal fun didYouMean(value: String, variants: Iterable<String>): String? {
    if (value.isEmpty()) {
        return null
    }

    val maxDist = if (value.length <= 2) {
        // we don't want to suggest "cd" for "a"
        1
    } else {
        2
    }

    return variants
        .map { v -> Pair(v, levenshtein(value, v)) }
        .filter { (_, dist) -> dist <= maxDist }
        .minByOrNull { (_, dist) -> dist }
        ?.first
}

/** Compute the Levenshtein distance between two strings. */
private fun levenshtein(a: String, b: String): Int {
    if (a.isEmpty()) return b.length
    if (b.isEmpty()) return a.length

    val m = a.length
    val n = b.length
    var prev = IntArray(n + 1) { it }
    var curr = IntArray(n + 1)

    for (i in 1..m) {
        curr[0] = i
        for (j in 1..n) {
            val cost = if (a[i - 1] == b[j - 1]) 0 else 1
            curr[j] = minOf(
                prev[j] + 1,      // deletion
                curr[j - 1] + 1,  // insertion
                prev[j - 1] + cost // substitution
            )
        }
        val tmp = prev
        prev = curr
        curr = tmp
    }
    return prev[n]
}
