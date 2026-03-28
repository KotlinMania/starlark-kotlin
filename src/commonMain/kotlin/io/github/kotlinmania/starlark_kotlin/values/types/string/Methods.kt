// port-lint: source src/values/types/string/methods.rs
package io.github.kotlinmania.starlark_kotlin.values.types.string

import io.github.kotlinmania.starlark_kotlin.values.types.list.None
import io.github.kotlinmania.starlark_kotlin.values.length
import io.github.kotlinmania.starlark_kotlin.values.sizeHint
import io.github.kotlinmania.starlark_kotlin.values.next
import io.github.kotlinmania.starlark_kotlin.values.layout.toValueOfUnchecked
import io.github.kotlinmania.starlark_kotlin.values.layout.newEmptyString
import io.github.kotlinmania.starlark_kotlin.eval.runtime.positions
import io.github.kotlinmania.starlark_kotlin.analysis.unused_loads.names
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.typed.StringValue
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.types.none.NoneOr
import io.github.kotlinmania.starlark_kotlin.values.ValueOfUnchecked
import io.github.kotlinmania.starlark_kotlin.values.types.list.AllocList
import io.github.kotlinmania.starlark_kotlin.values.typing.StarlarkIter
import io.github.kotlinmania.starlark_kotlin.values.types.list.UnpackList
import io.github.kotlinmania.starlark_kotlin.environment.MethodsBuilder
import io.github.kotlinmania.starlark_kotlin.collections.StringPool


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
 * Methods for the `string` type.
 */

// Real types should be imported from their respective packages

// This does not exist in Kotlin stdlib, split would cut the string incorrectly and
// split on whitespace cannot take a maxsplit parameter.
private fun splitnWhitespace(s: String, maxsplit: Int): List<String> {
    val v = mutableListOf<String>()
    var cur = StringBuilder()
    var split = 1
    var eatWs = true
    for (c in s) {
        if (split >= maxsplit && !eatWs) {
            cur.append(c)
        } else if (c.isWhitespace()) {
            if (cur.isNotEmpty()) {
                v.add(cur.toString())
                cur = StringBuilder()
                split += 1
                eatWs = true
            }
        } else {
            eatWs = false
            cur.append(c)
        }
    }
    if (cur.isNotEmpty()) {
        v.add(cur.toString())
    }
    return v
}

private fun rsplitnWhitespace(s: String, maxsplit: Int): List<String> {
    val v = mutableListOf<String>()
    var cur = StringBuilder()
    var split = 1
    var eatWs = true
    for (c in s.reversed()) {
        if (split >= maxsplit && !eatWs) {
            cur.append(c)
        } else if (c.isWhitespace()) {
            if (cur.isNotEmpty()) {
                v.add(cur.reversed().toString())
                cur = StringBuilder()
                split += 1
                eatWs = true
            }
        } else {
            eatWs = false
            cur.append(c)
        }
    }
    if (cur.isNotEmpty()) {
        v.add(cur.reversed().toString())
    }
    v.reverse()
    return v
}

sealed class StringOrTuple {
    data class String(val value: kotlin.String) : StringOrTuple()
    data class Tuple(val items: List<kotlin.String>) : StringOrTuple()
}

/**
 * Register string methods.
 *
 * This is the Kotlin port of the Rust `#[starlark_module]` annotated function.
 */
internal fun stringMethods(builder: MethodsBuilder) {
    // The implementations below would be registered through the MethodsBuilder
    // when it's properly ported.
}

/**
 * [string.elems](
 * https://github.com/bazelbuild/starlark/blob/master/spec.md#string·elems
 * ): returns an iterable of the bytes values of a string.
 *
 * `S.elems()` returns an iterable value containing the
 * sequence of numeric bytes values in the string S.
 *
 * To materialize the entire sequence of bytes, apply `list(...)` to the
 * result.
 *
 * ```
 * # starlark::assert::is_true(r#"
 * list("Hello, 世界".elems()) == ["H", "e", "l", "l", "o", ",", " ", "世", "界"]
 * # "#);
 * ```
 */
internal fun elems(
    thisStr: StringValue,
    heap: Heap,
): Result<ValueOfUnchecked<StarlarkIter<kotlin.String>>> {
    return Result.success(iterateChars(thisStr, heap))
}

/**
 * [string.capitalize](
 * https://github.com/bazelbuild/starlark/blob/master/spec.md#string%C2%B7capitalize
 * ): returns a copy of string S, where the first character (if any) is converted to uppercase;
 * all other characters are converted to lowercase.
 *
 * ```
 * # starlark::assert::all_true(r#"
 * "hello, world!".capitalize() == "Hello, world!"
 * "Hello, World!".capitalize() == "Hello, world!"
 * "".capitalize() == ""
 * # "#);
 * ```
 */
internal fun capitalize(thisStr: kotlin.String): Result<kotlin.String> {
    val result = StringBuilder(thisStr.length)
    for ((i, c) in thisStr.withIndex()) {
        if (i == 0) {
            result.append(c.uppercaseChar())
        } else {
            result.append(c.lowercaseChar())
        }
    }
    return Result.success(result.toString())
}

/**
 * [string.codepoints](
 * https://github.com/bazelbuild/starlark/blob/master/spec.md#string·codepoints
 * ): returns an iterable of the unicode codepoint of a string.
 *
 * `S.codepoints()` returns an iterable value containing the
 * sequence of integer Unicode code points encoded by the string S.
 * Each invalid code within the string is treated as if it encodes the
 * Unicode replacement character, U+FFFD.
 *
 * By returning an iterable, not a list, the cost of decoding the string
 * is deferred until actually needed; apply `list(...)` to the result to
 * materialize the entire sequence.
 *
 * ```
 * # starlark::assert::all_true(r#"
 * list("Hello, 世界".codepoints()) == [72, 101, 108, 108, 111, 44, 32, 19990, 30028]
 * # "#);
 * ```
 */
internal fun codepoints(
    thisStr: StringValue,
    heap: Heap,
): Result<ValueOfUnchecked<StarlarkIter<kotlin.String>>> {
    return Result.success(iterateCodepoints(thisStr, heap))
}

/**
 * [string.count](
 * https://github.com/bazelbuild/starlark/blob/master/spec.md#string·count
 * ): count the number of occurrences of a string in another string.
 *
 * `S.count(sub[, start[, end]])` returns the number of occurrences of
 * `sub` within the string S, or, if the optional substring indices
 * `start` and `end` are provided, within the designated substring of S.
 * They are interpreted according to Skylark's [indexing conventions](
 * https://github.com/bazelbuild/starlark/blob/master/spec.md#indexing).
 *
 * This implementation does not count occurrence of `sub` in the string `S`
 * that overlap other occurrence of S (which can happen if some suffix of S
 * is a prefix of S). For instance, `"abababa".count("aba")` returns 2
 * for `[aba]a[aba]`, not counting the middle occurrence: `ab[aba]ba`
 * (this is following Python behavior).
 *
 * ```
 * # starlark::assert::all_true(r#"
 * "hello, world!".count("o") == 2
 * "abababa".count("aba") == 2
 * "hello, world!".count("o", 7, 12) == 1  # in "world"
 * # "#);
 * ```
 */
internal fun count(
    thisStr: kotlin.String,
    needle: kotlin.String,
    start: NoneOr<Int> = NoneOr.None(),
    end: NoneOr<Int> = NoneOr.None(),
): Result<Int> {
    val indices = convertStrIndices(thisStr, start.intoOption(), end.intoOption())
    return if (indices != null) {
        Result.success(countMatches(indices.haystack, needle))
    } else {
        Result.success(0)
    }
}

/**
 * [string.endswith](
 * https://github.com/bazelbuild/starlark/blob/master/spec.md#string·endswith
 * ): determine if a string ends with a given suffix.
 *
 * `S.endswith(suffix)` reports whether the string S has the specified
 * suffix.
 *
 * ```
 * # starlark::assert::all_true(r#"
 * "filename.sky".endswith(".sky") == True
 * # "#);
 * ```
 */
internal fun endswith(
    thisStr: kotlin.String,
    suffix: StringOrTuple,
): Result<Boolean> {
    return when (suffix) {
        is StringOrTuple.String -> Result.success(thisStr.endsWith(suffix.value))
        is StringOrTuple.Tuple -> Result.success(suffix.items.any { thisStr.endsWith(it) })
    }
}

/**
 * [string.find](
 * https://github.com/bazelbuild/starlark/blob/master/spec.md#string·find
 * ): find a substring in a string.
 *
 * `S.find(sub[, start[, end]])` returns the index of the first
 * occurrence of the substring `sub` within S.
 *
 * If either or both of `start` or `end` are specified,
 * they specify a subrange of S to which the search should be restricted.
 * They are interpreted according to Skylark's [indexing
 * conventions](#indexing).
 *
 * If no occurrence is found, `found` returns -1.
 *
 * ```
 * # starlark::assert::all_true(r#"
 * "bonbon".find("on") == 1
 * "bonbon".find("on", 2) == 4
 * "bonbon".find("on", 2, 5) == -1
 * # "#);
 * ```
 */
internal fun find(
    thisStr: kotlin.String,
    needle: kotlin.String,
    start: NoneOr<Int> = NoneOr.None(),
    end: NoneOr<Int> = NoneOr.None(),
): Result<Int> {
    val indices = convertStrIndices(thisStr, start.intoOption(), end.intoOption())
    return if (indices != null) {
        val index = indices.haystack.indexOf(needle)
        if (index != -1) {
            val charIndex = strLen(indices.haystack.substring(0, index))
            Result.success((indices.start + charIndex))
        } else {
            Result.success(-1)
        }
    } else {
        Result.success(-1)
    }
}

/**
 * [string.format](
 * https://github.com/bazelbuild/starlark/blob/master/spec.md#string·format
 * ): format a string.
 *
 * `S.format(*args, **kwargs)` returns a version of the format string S
 * in which bracketed portions `{...}` are replaced
 * by arguments from `args` and `kwargs`.
 *
 * Within the format string, a pair of braces `{{` or `}}` is treated as
 * a literal open or close brace.
 * Each unpaired open brace must be matched by a close brace `}`.
 * The optional text between corresponding open and close braces
 * specifies which argument to use and how to format it, and consists of
 * three components, all optional:
 * a field name, a conversion preceded by '`!`', and a format specifier
 * preceded by '`:`'.
 *
 * ```text
 * {field}
 * {field:spec}
 * {field!conv}
 * {field!conv:spec}
 * ```
 *
 * The *field name* may be either a decimal number or a keyword.
 * A number is interpreted as the index of a positional argument;
 * a keyword specifies the value of a keyword argument.
 * If all the numeric field names form the sequence 0, 1, 2, and so on,
 * they may be omitted and those values will be implied; however,
 * the explicit and implicit forms may not be mixed.
 *
 * The *conversion* specifies how to convert an argument value `x` to a
 * string. It may be either `!r`, which converts the value using
 * `repr(x)`, or `!s`, which converts the value using `str(x)` and is
 * the default.
 *
 * The *format specifier*, after a colon, specifies field width,
 * alignment, padding, and numeric precision.
 * Currently it must be empty, but it is reserved for future use.
 *
 * ```rust
 * # starlark::assert::all_true(r#"
 * "a {} c".format(3) == "a 3 c"
 * "a{x}b{y}c{}".format(1, x=2, y=3) == "a2b3c1"
 * "a{}b{}c".format(1, 2) == "a1b2c"
 * "({1}, {0})".format("zero", "one") == "(one, zero)"
 * "Is {0!r} {0!s}?".format("heterological") == "Is \"heterological\" heterological?"
 * # "#);
 * ```
 */
internal fun format(
    thisStr: kotlin.String,
    args: Arguments,
    eval: Evaluator,
): Result<StringValue> {
    val iter = args.positions(eval.moduleEnv.heap())
    return dotFormat(
        thisStr,
        iter,
        args.names(),
        eval.stringPool,
        eval.moduleEnv.heap(),
    )
}

/**
 * [string.index](
 * https://github.com/bazelbuild/starlark/blob/master/spec.md#string·index
 * ): search a substring inside a string, failing on not found.
 *
 * `S.index(sub[, start[, end]])` returns the index of the first
 * occurrence of the substring `sub` within S, like `S.find`, except
 * that if the substring is not found, the operation fails.
 *
 * ```
 * # starlark::assert::all_true(r#"
 * "bonbon".index("on") == 1
 * "bonbon".index("on", 2) == 4
 * # "#);
 * # starlark::assert::fail(r#"
 * "bonbon".index("on", 2, 5)    # error: not found
 * # "#, "not found");
 * ```
 */
internal fun index(
    thisStr: kotlin.String,
    needle: kotlin.String,
    start: NoneOr<Int> = NoneOr.None(),
    end: NoneOr<Int> = NoneOr.None(),
): Result<Int> {
    val indices = convertStrIndices(thisStr, start.intoOption(), end.intoOption())
    return if (indices != null) {
        val index = indices.haystack.indexOf(needle)
        if (index != -1) {
            val charIndex = strLen(indices.haystack.substring(0, index))
            Result.success((indices.start + charIndex))
        } else {
            Result.failure(
                IllegalArgumentException("Substring '$needle' not found in '$thisStr'")
            )
        }
    } else {
        Result.failure(
            IllegalArgumentException("Substring '$needle' not found in '$thisStr'")
        )
    }
}

/**
 * [string.isalnum](
 * https://github.com/bazelbuild/starlark/blob/master/spec.md#string·isalnum
 * ): test if a string is composed only of letters and digits.
 *
 * `S.isalnum()` reports whether the string S is non-empty and consists
 * only Unicode letters and digits.
 *
 * ```
 * # starlark::assert::all_true(r#"
 * "base64".isalnum() == True
 * "Catch-22".isalnum() == False
 * # "#);
 * ```
 */
internal fun isalnum(thisStr: kotlin.String): Result<Boolean> {
    if (thisStr.isEmpty()) {
        return Result.success(false)
    }
    for (c in thisStr) {
        if (!c.isLetterOrDigit()) {
            return Result.success(false)
        }
    }
    return Result.success(true)
}

/**
 * [string.isalpha](
 * https://github.com/bazelbuild/starlark/blob/master/spec.md#string·isalpha
 * ): test if a string is composed only of letters.
 *
 * `S.isalpha()` reports whether the string S is non-empty and consists
 * only of Unicode letters.
 *
 * ```
 * # starlark::assert::all_true(r#"
 * "ABC".isalpha() == True
 * "Catch-22".isalpha() == False
 * "".isalpha() == False
 * # "#);
 * ```
 */
internal fun isalpha(thisStr: kotlin.String): Result<Boolean> {
    if (thisStr.isEmpty()) {
        return Result.success(false)
    }
    for (c in thisStr) {
        if (!c.isLetter()) {
            return Result.success(false)
        }
    }
    return Result.success(true)
}

/**
 * [string.isdigit](
 * https://github.com/bazelbuild/starlark/blob/master/spec.md#string·isdigit
 * ): test if a string is composed only of digits.
 *
 * `S.isdigit()` reports whether the string S is non-empty and consists
 * only of Unicode digits.
 *
 * ```
 * # starlark::assert::all_true(r#"
 * "123".isdigit() == True
 * "Catch-22".isdigit() == False
 * "".isdigit() == False
 * # "#);
 * ```
 */
internal fun isdigit(thisStr: kotlin.String): Result<Boolean> {
    if (thisStr.isEmpty()) {
        return Result.success(false)
    }
    for (c in thisStr) {
        if (!c.isDigit()) {
            return Result.success(false)
        }
    }
    return Result.success(true)
}

/**
 * [string.islower](
 * https://github.com/bazelbuild/starlark/blob/master/spec.md#string·islower
 * ): test if all letters of a string are lowercase.
 *
 * `S.islower()` reports whether the string S contains at least one cased
 * Unicode letter, and all such letters are lowercase.
 *
 * ```
 * # starlark::assert::all_true(r#"
 * "hello, world".islower() == True
 * "Catch-22".islower() == False
 * "123".islower() == False
 * # "#);
 * ```
 */
internal fun islower(thisStr: kotlin.String): Result<Boolean> {
    var result = false
    for (c in thisStr) {
        if (c.isUpperCase()) {
            return Result.success(false)
        } else if (c.isLowerCase()) {
            result = true
        }
    }
    return Result.success(result)
}

/**
 * [string.isspace](
 * https://github.com/bazelbuild/starlark/blob/master/spec.md#string·isspace
 * ): test if all characters of a string are whitespaces.
 *
 * `S.isspace()` reports whether the string S is non-empty and consists
 * only of Unicode spaces.
 *
 * ```
 * # starlark::assert::all_true(r#"
 * "    ".isspace() == True
 * "\r\t\n".isspace() == True
 * "".isspace() == False
 * # "#);
 * ```
 */
internal fun isspace(thisStr: kotlin.String): Result<Boolean> {
    if (thisStr.isEmpty()) {
        return Result.success(false)
    }
    for (c in thisStr) {
        if (!c.isWhitespace()) {
            return Result.success(false)
        }
    }
    return Result.success(true)
}

/**
 * [string.istitle](
 * https://github.com/bazelbuild/starlark/blob/master/spec.md#string·istitle
 * ): test if the string is title cased.
 *
 * `S.istitle()` reports whether the string S contains at least one cased
 * Unicode letter, and all such letters that begin a word are in title
 * case.
 *
 * ```
 * # starlark::assert::all_true(r#"
 * "Hello, World!".istitle() == True
 * "Catch-22".istitle() == True
 * "HAL-9000".istitle() == False
 * "123".istitle() == False
 * # "#);
 * ```
 */
internal fun istitle(thisStr: kotlin.String): Result<Boolean> {
    var lastSpace = true
    var result = false

    for (c in thisStr) {
        if (!c.isLetter()) {
            lastSpace = true
        } else {
            if (lastSpace) {
                if (c.isLowerCase()) {
                    return Result.success(false)
                }
            } else if (c.isUpperCase()) {
                return Result.success(false)
            }
            if (c.isLetter()) {
                result = true
            }
            lastSpace = false
        }
    }
    return Result.success(result)
}

/**
 * [string.isupper](
 * https://github.com/bazelbuild/starlark/blob/master/spec.md#string·isupper
 * ): test if all letters of a string are uppercase.
 *
 * `S.isupper()` reports whether the string S contains at least one cased
 * Unicode letter, and all such letters are uppercase.
 *
 * ```
 * # starlark::assert::all_true(r#"
 * "HAL-9000".isupper() == True
 * "Catch-22".isupper() == False
 * "123".isupper() == False
 * # "#);
 * ```
 */
internal fun isupper(thisStr: kotlin.String): Result<Boolean> {
    var result = false
    for (c in thisStr) {
        if (c.isLowerCase()) {
            return Result.success(false)
        } else if (c.isUpperCase()) {
            result = true
        }
    }
    return Result.success(result)
}

/**
 * [string.lower](
 * https://github.com/bazelbuild/starlark/blob/master/spec.md#string·lower
 * ): convert a string to all lowercase.
 *
 * `S.lower()` returns a copy of the string S with letters converted to
 * lowercase.
 *
 * ```
 * # starlark::assert::all_true(r#"
 * "Hello, World!".lower() == "hello, world!"
 * # "#);
 * ```
 */
internal fun lower(thisStr: kotlin.String): Result<kotlin.String> {
    return Result.success(thisStr.lowercase())
}

/**
 * [string.join](
 * https://github.com/bazelbuild/starlark/blob/master/spec.md#string·join
 * ): join elements with a separator.
 *
 * `S.join(iterable)` returns the string formed by concatenating each
 * element of its argument, with a copy of the string S between
 * successive elements. The argument must be an iterable whose elements
 * are strings.
 *
 * ```
 * # starlark::assert::all_true(r#"
 * ", ".join([]) == ""
 * ", ".join(("x", )) == "x"
 * ", ".join(["one", "two", "three"]) == "one, two, three"
 * "a".join("ctmrn".elems()) == "catamaran"
 * # "#);
 * ```
 */
internal fun join(
    thisStr: kotlin.String,
    toJoin: ValueOfUnchecked<StarlarkIter<kotlin.String>>,
    heap: Heap,
): Result<ValueOfUnchecked<kotlin.String>> {
    val it = toJoin.get().iterate(heap)
    val first = it.next() ?: return Result.success(ValueOfUnchecked.new(Value.newEmptyString()))

    val second = it.next()
    return if (second == null) {
        // If there is a singleton we can avoid reallocation
        Result.success(asStr(first).toValueOfUnchecked().cast())
    } else {
        val s1 = asStr(first).asStr()
        val s2 = asStr(second).asStr()
        // guess towards the upper bound, since we throw away over-allocations quickly
        // include a buffer (20 bytes)
        val n = it.sizeHint().first + 2
        val guess = (maxOf(s1.length, s2.length) * n) + (thisStr.length * (n - 1)) + 20
        val r = StringBuilder(guess)
        r.append(s1)
        r.append(thisStr)
        r.append(s2)
        for (x in it) {
            r.append(thisStr)
            r.append(asStr(x).asStr())
        }
        Result.success(heap.allocTypedUnchecked(r.toString()))
    }
}

/**
 * [string.lstrip](
 * https://github.com/bazelbuild/starlark/blob/master/spec.md#string·lstrip
 * ): trim leading whitespaces.
 *
 * `S.lstrip()` returns a copy of the string S with leading whitespace removed.
 * In most cases instead of passing an argument you should use `removeprefix`.
 *
 * ```
 * # starlark::assert::all_true(r#"
 * "  hello  ".lstrip() == "hello  "
 * "x!hello  ".lstrip("!x ") == "hello  "
 * # "#);
 * ```
 */
internal fun lstrip(
    thisStr: StringValue,
    chars: kotlin.String?,
    heap: Heap,
): Result<StringValue> {
    val res = if (chars == null) {
        thisStr.trimStart()
    } else {
        thisStr.trimStart { c -> chars.contains(c) }
    }
    return if (res.length == thisStr.length) {
        Result.success(thisStr)
    } else {
        Result.success(heap.allocStr(res))
    }
}

/**
 * [string.partition](
 * https://github.com/bazelbuild/starlark/blob/master/spec.md#string·partition
 * ): partition a string in 3 components
 *
 * `S.partition(x = " ")` splits string S into three parts and returns them
 * as a tuple: the portion before the first occurrence of string `x`,
 * `x` itself, and the portion following it.
 * If S does not contain `x`, `partition` returns `(S, "", "")`.
 *
 * `partition` fails if `x` is not a string, or is the empty string.
 *
 * ```
 * # starlark::assert::all_true(r#"
 * "one/two/three".partition("/") == ("one", "/", "two/three")
 * "one".partition("/") == ("one", "", "")
 * # "#);
 * ```
 */
internal fun partition(
    thisStr: StringValue,
    needle: StringValue,
    heap: Heap,
): Result<Triple<StringValue, StringValue, StringValue>> {
    if (needle.isEmpty()) {
        return Result.failure(
            IllegalArgumentException("Empty separator cannot be used for partitioning")
        )
    }
    val offset = thisStr.find(needle.asStr())
    return if (offset != null) {
        val offset2 = offset + needle.length
        Result.success(
            Triple(
                heap.allocStr(thisStr.get(0 until offset)),
                needle,
                heap.allocStr(thisStr.get(offset2 until thisStr.length))
            )
        )
    } else {
        val empty = StringValue.default()
        Result.success(Triple(thisStr, empty, empty))
    }
}

/**
 * [string.replace](
 * https://github.com/bazelbuild/starlark/blob/master/spec.md#string·replace
 * ): replace all occurrences of a substring.
 *
 * `S.replace(old, new[, count])` returns a copy of string S with all
 * occurrences of substring `old` replaced by `new`. If the optional
 * argument `count`, which must be an `int`, is non-negative, it
 * specifies a maximum number of occurrences to replace.
 *
 * ```
 * # starlark::assert::all_true(r#"
 * "banana".replace("a", "o") == "bonono"
 * "banana".replace("a", "o", 2) == "bonona"
 * "banana".replace("z", "x") == "banana"
 * "banana".replace("", "x") == "xbxaxnxaxnxax"
 * "banana".replace("", "x", 2) == "xbxanana"
 * "".replace("", "x") == "x"
 * # "# );
 * # starlark::assert::fail(r#"
 * "banana".replace("a", "o", -2)  # error: argument was negative
 * # "#, "argument was negative");
 * ```
 */
internal fun replace(
    thisStr: StringValue,
    old: kotlin.String,
    new: kotlin.String,
    count: Int?,
    heap: Heap,
): Result<StringValue> {
    return when {
        count != null && count >= 0 -> {
            Result.success(heap.allocStr(thisStr.replacen(old, new, count)))
        }
        count != null -> {
            Result.failure(
                IllegalArgumentException("Replace final argument was negative '$count'")
            )
        }
        else -> {
            // Optimise `replace` using the Kotlin standard library definition,
            // but avoiding redundant allocation in the last step
            val x = thisStr.asStr()
            val result = StringBuilder()
            var lastEnd = 0
            for (match in old.toRegex(RegexOption.LITERAL).findAll(x)) {
                result.append(x.substring(lastEnd, match.range.first))
                result.append(new)
                lastEnd = match.range.last + 1
            }
            if (result.isEmpty() && lastEnd == 0) {
                Result.success(thisStr)
            } else {
                Result.success(heap.allocStrConcat(result.toString(), x.substring(lastEnd)))
            }
        }
    }
}

/**
 * [string.rfind](
 * https://github.com/bazelbuild/starlark/blob/master/spec.md#string·rfind
 * ): find the last index of a substring.
 *
 * `S.rfind(sub[, start[, end]])` returns the index of the substring `sub`
 * within S, like `S.find`, except that `rfind` returns the index of
 * the substring's _last_ occurrence.
 *
 * ```
 * # starlark::assert::all_true(r#"
 * "bonbon".rfind("on") == 4
 * "bonbon".rfind("on", None, 5) == 1
 * "bonbon".rfind("on", 2, 5) == -1
 * # "#);
 * ```
 */
internal fun rfind(
    thisStr: kotlin.String,
    needle: kotlin.String,
    start: NoneOr<Int> = NoneOr.None(),
    end: NoneOr<Int> = NoneOr.None(),
): Result<Int> {
    val indices = convertStrIndices(thisStr, start.intoOption(), end.intoOption())
    return if (indices != null) {
        val index = indices.haystack.lastIndexOf(needle)
        if (index != -1) {
            val charIndex = strLen(indices.haystack.substring(0, index))
            Result.success((indices.start + charIndex))
        } else {
            Result.success(-1)
        }
    } else {
        Result.success(-1)
    }
}

/**
 * [string.rindex](
 * https://github.com/bazelbuild/starlark/blob/master/spec.md#string·rindex
 * ): find the last index of a substring, failing on not found.
 *
 * `S.rindex(sub[, start[, end]])` returns the index of the substring `sub`
 * within S, like `S.index`, except that `rindex` returns the index of
 * the substring's _last_ occurrence.
 *
 * ```
 * # starlark::assert::all_true(r#"
 * "bonbon".rindex("on") == 4
 * "bonbon".rindex("on", None, 5) == 1  # in "bonbo"
 * # "#);
 * # starlark::assert::fail(r#"
 * "bonbon".rindex("on", 2, 5) #   error: not found
 * # "#, "not found");
 * ```
 */
internal fun rindex(
    thisStr: kotlin.String,
    needle: kotlin.String,
    start: NoneOr<Int> = NoneOr.None(),
    end: NoneOr<Int> = NoneOr.None(),
): Result<Int> {
    val indices = convertStrIndices(thisStr, start.intoOption(), end.intoOption())
    return if (indices != null) {
        val index = indices.haystack.lastIndexOf(needle)
        if (index != -1) {
            val charIndex = strLen(indices.haystack.substring(0, index))
            Result.success((indices.start + charIndex))
        } else {
            Result.failure(
                IllegalArgumentException("Substring '$needle' not found in '$thisStr'")
            )
        }
    } else {
        Result.failure(
            IllegalArgumentException("Substring '$needle' not found in '$thisStr'")
        )
    }
}

/**
 * [string.rpartition](
 * https://github.com/bazelbuild/starlark/blob/master/spec.md#string·rpartition
 * ): partition a string in 3 elements.
 *
 * `S.rpartition([x = ' '])` is like `partition`, but splits `S` at the
 * last occurrence of `x`.
 *
 * ```
 * # starlark::assert::all_true(r#"
 * "one/two/three".rpartition("/") == ("one/two", "/", "three")
 * "one".rpartition("/") == ("", "", "one")
 * # "#);
 * ```
 */
internal fun rpartition(
    thisStr: StringValue,
    needle: StringValue,
    heap: Heap,
): Result<Triple<StringValue, StringValue, StringValue>> {
    if (needle.isEmpty()) {
        return Result.failure(
            IllegalArgumentException("Empty separator cannot be used for partitioning")
        )
    }
    val offset = thisStr.rfind(needle.asStr())
    return if (offset != null) {
        val offset2 = offset + needle.length
        Result.success(
            Triple(
                heap.allocStr(thisStr.get(0 until offset)),
                needle,
                heap.allocStr(thisStr.get(offset2 until thisStr.length))
            )
        )
    } else {
        val empty = StringValue.default()
        Result.success(Triple(empty, empty, thisStr))
    }
}

/**
 * [string.rsplit](
 * https://github.com/bazelbuild/starlark/blob/master/spec.md#string·rsplit
 * ): splits a string into substrings.
 *
 * `S.rsplit([sep[, maxsplit]])` splits a string into substrings like
 * `S.split`, except that when a maximum number of splits is specified,
 * `rsplit` chooses the rightmost splits.
 *
 * ```
 * # starlark::assert::all_true(r#"
 * "banana".rsplit("n") == ["ba", "a", "a"]
 * "banana".rsplit("n", 1) == ["bana", "a"]
 * "one two  three".rsplit(None, 1) == ["one two", "three"]
 * # "#);
 * ```
 */
internal fun rsplit(
    thisStr: kotlin.String,
    sep: NoneOr<kotlin.String> = NoneOr.None(),
    maxsplit: NoneOr<Int> = NoneOr.None(),
    heap: Heap,
): Result<ValueOfUnchecked<UnpackList<kotlin.String>>> {
    val maxsplitValue = when (val v = maxsplit.intoOption()) {
        null -> null
        else -> if (v < 0) null else (v + 1)
    }
    return when (val sepValue = sep.intoOption()) {
        null -> when (maxsplitValue) {
            null -> Result.success(
                heap.allocTypedUnchecked(AllocList(thisStr.split(Regex("\\s+"))))
                    .cast()
            )
            else -> Result.success(
                heap.allocTypedUnchecked(rsplitnWhitespace(thisStr, maxsplitValue))
                    .cast()
            )
        }
        else -> {
            val v = when (maxsplitValue) {
                null -> thisStr.split(sepValue).reversed()
                else -> thisStr.split(sepValue, maxsplitValue).reversed()
            }
            Result.success(heap.allocTypedUnchecked(AllocList(v)).cast())
        }
    }
}

/**
 * [string.rstrip](
 * https://github.com/bazelbuild/starlark/blob/master/spec.md#string·rstrip
 * ): trim trailing whitespace.
 *
 * `S.rstrip()` returns a copy of the string S with trailing whitespace removed.
 * In most cases instead of passing an argument you should use `removesuffix`.
 *
 * ```
 * # starlark::assert::all_true(r#"
 * "  hello  ".rstrip() == "  hello"
 * "  hello!x".rstrip(" x!") == "  hello"
 * # "#);
 * ```
 */
internal fun rstrip(
    thisStr: StringValue,
    chars: kotlin.String?,
    heap: Heap,
): Result<StringValue> {
    val res = if (chars == null) {
        thisStr.trimEnd()
    } else {
        thisStr.trimEnd { c -> chars.contains(c) }
    }
    return if (res.length == thisStr.length) {
        Result.success(thisStr)
    } else {
        Result.success(heap.allocStr(res))
    }
}

/**
 * [string.split](
 * https://github.com/bazelbuild/starlark/blob/master/spec.md#string·split
 * ): split a string in substrings.
 *
 * `S.split([sep [, maxsplit]])` returns the list of substrings of S,
 * splitting at occurrences of the delimiter string `sep`.
 *
 * Consecutive occurrences of `sep` are considered to delimit empty
 * strings, so `'food'.split('o')` returns `['f', '', D_']`.
 * Splitting an empty string with a specified separator returns `['']`.
 * If `sep` is the empty string, `split` fails.
 *
 * If `sep` is not specified or is `None`, `split` uses a different
 * algorithm: it removes all leading spaces from S
 * (or trailing spaces in the case of `rsplit`),
 * then splits the string around each consecutive non-empty sequence of
 * Unicode white space characters.
 *
 * If S consists only of white space, `split` returns the empty list.
 *
 * If `maxsplit` is given and non-negative, it specifies a maximum number
 * of splits.
 *
 * ```
 * # starlark::assert::all_true(r#"
 * "one two  three".split() == ["one", "two", "three"]
 * "one two  three".split(" ") == ["one", "two", "", "three"]
 * "one two  three".split(None, 1) == ["one", "two  three"]
 * "banana".split("n") == ["ba", "a", "a"]
 * "banana".split("n", 1) == ["ba", "ana"]
 * # "#);
 * ```
 */
internal fun split(
    thisStr: kotlin.String,
    sep: NoneOr<kotlin.String> = NoneOr.None(),
    maxsplit: NoneOr<Int> = NoneOr.None(),
    heap: Heap,
): Result<ValueOfUnchecked<UnpackList<kotlin.String>>> {
    val maxsplitValue = when (val v = maxsplit.intoOption()) {
        null -> null
        else -> if (v < 0) null else (v + 1)
    }
    return when (val sepValue = sep.intoOption()) {
        null -> when (maxsplitValue) {
            null -> Result.success(
                heap.allocTypedUnchecked(AllocList(thisStr.split(Regex("\\s+"))))
                    .cast()
            )
            else -> Result.success(
                heap.allocTypedUnchecked(AllocList(splitnWhitespace(thisStr, maxsplitValue)))
                    .cast()
            )
        }
        else -> {
            if (sepValue.length == 1) {
                // If we are searching for a 1-byte string, we can provide a much faster path.
                // Since it is one byte, given how UTF8 works, all the resultant slices must be UTF8 too.
                val b = sepValue[0]
                val count = countMatchesByte(thisStr, b)
                val res = mutableListOf<kotlin.String>()
                res.addAll(
                    thisStr.split(b).map { it }
                )
                Result.success(heap.allocTypedUnchecked(AllocList(res)).cast())
            } else {
                when (maxsplitValue) {
                    null -> Result.success(
                        heap.allocTypedUnchecked(AllocList(thisStr.split(sepValue)))
                            .cast()
                    )
                    else -> Result.success(
                        heap.allocTypedUnchecked(AllocList(thisStr.split(sepValue, maxsplitValue)))
                            .cast()
                    )
                }
            }
        }
    }
}

/**
 * [string.splitlines](
 * https://github.com/bazelbuild/starlark/blob/master/spec.md#string·splitlines
 * ): return the list of lines of a string.
 *
 * `S.splitlines([keepends])` returns a list whose elements are the
 * successive lines of S, that is, the strings formed by splitting S at
 * line terminators ('\n', '\r' or '\r\n').
 *
 * The optional argument, `keepends`, is interpreted as a Boolean.
 * If true, line terminators are preserved in the result, though
 * the final element does not necessarily end with a line terminator.
 *
 * ```
 * # starlark::assert::all_true(r#"
 * "one\n\ntwo".splitlines() == ["one", "", "two"]
 * "one\n\ntwo".splitlines(True) == ["one\n", "\n", "two"]
 * "a\nb".splitlines() == ["a", "b"]
 * # "#);
 * ```
 */
internal fun splitlines(
    thisStr: kotlin.String,
    keepends: Boolean = false,
    heap: Heap,
): Result<List<StringValue>> {
    var s = thisStr
    val lines = mutableListOf<StringValue>()
    while (true) {
        val x = s.indexOfAny(charArrayOf('\n', '\r'))
        if (x != null) {
            val y = x
            val x2 = if (s.substring(y, minOf(y + 2, s.length)) == "\r\n") {
                y + 2
            } else {
                y + 1
            }
            if (keepends) {
                lines.add(heap.allocStr(s.substring(0, x2)))
            } else {
                lines.add(heap.allocStr(s.substring(0, y)))
            }
            if (x2 == s.length) {
                return Result.success(lines)
            }
            s = s.substring(x2)
        } else {
            if (s.isNotEmpty()) {
                lines.add(heap.allocStr(s))
            }
            return Result.success(lines)
        }
    }
}

/**
 * [string.startswith](
 * https://github.com/bazelbuild/starlark/blob/master/spec.md#string·startswith
 * ): test whether a string starts with a given prefix.
 *
 * `S.startswith(suffix)` reports whether the string S has the specified
 * prefix.
 *
 * ```
 * # starlark::assert::all_true(r#"
 * "filename.sky".startswith("filename") == True
 * "filename.sky".startswith("sky") == False
 * 'abc'.startswith(('a', A_')) == True
 * 'ABC'.startswith(('a', A_')) == True
 * 'def'.startswith(('a', A_')) == False
 * # "#);
 * ```
 */
internal fun startswith(
    thisStr: kotlin.String,
    prefix: StringOrTuple,
): Result<Boolean> {
    return when (prefix) {
        is StringOrTuple.String -> Result.success(thisStr.startsWith(prefix.value))
        is StringOrTuple.Tuple -> Result.success(prefix.items.any { thisStr.startsWith(it) })
    }
}

/**
 * [string.strip](
 * https://github.com/bazelbuild/starlark/blob/master/spec.md#string·strip
 * ): trim leading and trailing whitespaces.
 *
 * `S.strip()` returns a copy of the string S with leading and trailing
 * whitespace removed.
 *
 * ```
 * # starlark::assert::all_true(r#"
 * "  hello  ".strip() == "hello"
 * "xxhello!!".strip("x!") == "hello"
 * # "#);
 * ```
 */
internal fun strip(
    thisStr: StringValue,
    chars: kotlin.String?,
    heap: Heap,
): Result<StringValue> {
    val res = if (chars == null) {
        thisStr.trim()
    } else {
        thisStr.trim { c -> chars.contains(c) }
    }
    return if (res.length == thisStr.length) {
        Result.success(thisStr)
    } else {
        Result.success(heap.allocStr(res))
    }
}

/**
 * [string.title](
 * https://github.com/bazelbuild/starlark/blob/master/spec.md#string·title
 * ): convert a string to title case.
 *
 * `S.title()` returns a copy of the string S with letters converted to
 * titlecase.
 *
 * Letters are converted to uppercase at the start of words, lowercase
 * elsewhere.
 *
 * ```
 * # starlark::assert::all_true(r#"
 * "hElLo, WoRlD!".title() == "Hello, World!"
 * # "#);
 * ```
 */
internal fun title(thisStr: kotlin.String): Result<kotlin.String> {
    var lastSpace = true
    val result = StringBuilder(thisStr.length)
    for (c in thisStr) {
        if (!c.isLetter()) {
            lastSpace = true
            result.append(c.lowercaseChar())
        } else {
            if (lastSpace) {
                result.append(c.uppercaseChar())
            } else {
                result.append(c.lowercaseChar())
            }
            lastSpace = false
        }
    }
    return Result.success(result.toString())
}

/**
 * [string.upper](
 * https://github.com/bazelbuild/starlark/blob/master/spec.md#string·upper
 * ): convert a string to all uppercase.
 *
 * `S.upper()` returns a copy of the string S with letters converted to
 * uppercase.
 *
 * ```
 * # starlark::assert::all_true(r#"
 * "Hello, World!".upper() == "HELLO, WORLD!"
 * # "#);
 * ```
 */
internal fun upper(thisStr: kotlin.String): Result<kotlin.String> {
    return Result.success(thisStr.uppercase())
}

/**
 * [string.removeprefix](
 * https://docs.python.org/3.9/library/stdtypes.html#str.removeprefix
 * ): remove a prefix from a string. _Not part of standard Starlark._
 *
 * If the string starts with the prefix string, return `string[len(prefix):]`.
 * Otherwise, return a copy of the original string:
 *
 * ```
 * # starlark::assert::all_true(r#"
 * "Hello, World!".removeprefix("Hello") == ", World!"
 * "Hello, World!".removeprefix("Goodbye") == "Hello, World!"
 * "Hello".removeprefix("Hello") == ""
 * # "#);
 * ```
 */
internal fun removeprefix(
    thisStr: StringValue,
    prefix: kotlin.String,
    heap: Heap,
): Result<StringValue> {
    val x = thisStr.asStr()
    return if (x.startsWith(prefix) && prefix.isNotEmpty()) {
        Result.success(heap.allocStr(x.substring(prefix.length)))
    } else {
        Result.success(thisStr)
    }
}

/**
 * [string.removesuffix](
 * https://docs.python.org/3.9/library/stdtypes.html#str.removesuffix
 * ): remove a prefix from a string. _Not part of standard Starlark._
 *
 * If the string starts with the prefix string, return `string[len(prefix):]`.
 * Otherwise, return a copy of the original string:
 *
 * ```
 * # starlark::assert::all_true(r#"
 * "Hello, World!".removesuffix("World!") == "Hello, "
 * "Hello, World!".removesuffix("World") == "Hello, World!"
 * "Hello".removesuffix("Hello") == ""
 * # "#);
 * ```
 */
internal fun removesuffix(
    thisStr: StringValue,
    suffix: kotlin.String,
    heap: Heap,
): Result<StringValue> {
    val x = thisStr.asStr()
    return if (x.endsWith(suffix) && suffix.isNotEmpty()) {
        Result.success(heap.allocStr(x.substring(0, x.length - suffix.length)))
    } else {
        Result.success(thisStr)
    }
}

// Helper functions that need to be implemented elsewhere
private fun iterateChars(thisStr: StringValue, heap: Heap): ValueOfUnchecked<StarlarkIter<kotlin.String>> {
    throw NotImplementedError("iterateChars needs to be ported from iter module")
}

private fun iterateCodepoints(thisStr: StringValue, heap: Heap): ValueOfUnchecked<StarlarkIter<kotlin.String>> {
    throw NotImplementedError("iterateCodepoints needs to be ported from iter module")
}

private data class StrIndices(val start: Int, val haystack: kotlin.String)

private fun convertStrIndices(str: kotlin.String, start: Int?, end: Int?): StrIndices? {
    throw NotImplementedError("convertStrIndices needs to be ported from fast_string module")
}

private fun strLen(str: kotlin.String): Int {
    throw NotImplementedError("strLen needs to be ported from fast_string module")
}

private fun countMatches(haystack: kotlin.String, needle: kotlin.String): Int {
    throw NotImplementedError("countMatches needs to be ported from fast_string module")
}

private fun countMatchesByte(haystack: kotlin.String, byte: Char): Int {
    throw NotImplementedError("countMatchesByte needs to be ported from fast_string module")
}

private fun dotFormat(
    format: kotlin.String,
    args: Any,
    kwargs: Any,
    stringPool: StringPool,
    heap: Heap,
): Result<StringValue> {
    throw NotImplementedError("dotFormat needs to be ported from dot_format module")
}

private fun asStr(value: Value): StringValue {
    throw NotImplementedError("asStr needs to be ported")
}
