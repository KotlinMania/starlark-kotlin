// port-lint: source src/errors/did_you_mean.rs
package io.github.kotlinmania.starlark.errors

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

//! Spelling suggestions.

// use strsim::levenshtein;
// Kotlin: inline Levenshtein distance (replaces strsim crate).
private fun levenshtein(a: String, b: String): Int {
    val m = a.length
    val n = b.length
    if (m == 0) return n
    if (n == 0) return m

    val dp = Array(m + 1) { IntArray(n + 1) }
    for (i in 0..m) dp[i][0] = i
    for (j in 0..n) dp[0][j] = j
    for (i in 1..m) {
        for (j in 1..n) {
            val cost = if (a[i - 1] == b[j - 1]) 0 else 1
            dp[i][j] = minOf(
                dp[i - 1][j] + 1,
                dp[i][j - 1] + 1,
                dp[i - 1][j - 1] + cost,
            )
        }
    }
    return dp[m][n]
}

/// Find a suggestion for a typo.
// pub(crate) fn did_you_mean<'a>(
//     value: &str,
//     variants: impl IntoIterator<Item = &'a str>,
// ) -> Option<&'a str>
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

    // variants
    //     .into_iter()
    //     .map(|v| (v, levenshtein(value, v)))
    //     .filter(|(_, dist)| *dist <= max_dist)
    //     .min_by_key(|(_v, sim)| *sim)
    //     .map(|(v, _)| v)
    return variants
        .map { v -> Pair(v, levenshtein(value, v)) }
        .filter { (_, dist) -> dist <= maxDist }
        .minByOrNull { (_, dist) -> dist }
        ?.first
}

// #[cfg(test)]
// mod tests {
//     use crate::errors::did_you_mean::did_you_mean;
//
//     #[test]
//     fn prefixes() {
//         assert_eq!(Some("cxx_library"), did_you_mean("cxx_librar", vec!["cxx_library"]));
//         assert_eq!(Some("cxx_library"), did_you_mean("cxx_libra", vec!["cxx_library"]));
//         assert_eq!(None, did_you_mean("cxx_libr", vec!["cxx_library"]));
//     }
//
//     #[test]
//     fn typos() {
//         assert_eq!(Some("cxx_library"), did_you_mean("cxx_librarx", vec!["cxx_library"]));
//         assert_eq!(Some("cxx_library"), did_you_mean("cxx_libraxx", vec!["cxx_library"]));
//         assert_eq!(None, did_you_mean("cxx_librxxx", vec!["cxx_library"]));
//     }
//
//     #[test]
//     fn best() {
//         assert_eq!(Some("abc"), did_you_mean("abx", vec!["abc", "abcd"]));
//     }
//
//     #[test]
//     fn very_short() {
//         assert_eq!(Some("a"), did_you_mean("b", vec!["a"]));
//         assert_eq!(Some("ab"), did_you_mean("b", vec!["ab"]));
//         assert_eq!(None, did_you_mean("b", vec!["cd"]));
//         assert_eq!(None, did_you_mean("bc", vec!["de"]));
//     }
//
//     #[test]
//     fn earlier_variants_are_more_important() {
//         assert_eq!(Some("aaaay"), did_you_mean("aaaax", vec!["aaaay", "aaaaz"]));
//         assert_eq!(Some("aaaaz"), did_you_mean("aaaax", vec!["aaaaz", "aaaay"]));
//     }
// }
