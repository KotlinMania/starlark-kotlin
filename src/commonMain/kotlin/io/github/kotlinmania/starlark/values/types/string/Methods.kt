// port-lint: source src/values/types/string/methods.rs
package io.github.kotlinmania.starlark.values.types.string

import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.typed.StringValue
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.types.none.NoneOr
import io.github.kotlinmania.starlark.environment.MethodsBuilder
import io.github.kotlinmania.starlark.collections.StringPool
import io.github.kotlinmania.starlark.eval.runtime.Arguments
import io.github.kotlinmania.starlark.eval.runtime.Evaluator
import io.github.kotlinmania.starlark.values.layout.avalues.str.allocStrConcat
import io.github.kotlinmania.starlark.values.layout.avalues.allocListIter


/*
 * Copyright 2019 The Starlark in Rust Authors.
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
 * # starlark::assert::isTrue(r#"
 * list("Hello, 世界".elems()) == ["H", "e", "l", "l", "o", ",", " ", "世", "界"]
 * # "#);
 * ```
 */
internal fun elems(
    thisStr: StringValue,
    heap: Heap,
): Result<Value> {
    return Result.success(iterateChars(thisStr, heap))
}

/**
 * [string.capitalize](
 * https://github.com/bazelbuild/starlark/blob/master/spec.md#string%C2%B7capitalize
 * ): returns a copy of string S, where the first character (if any) is converted to uppercase;
 * all other characters are converted to lowercase.
 *
 * ```
 * # starlark::assert::allTrue(r#"
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
 * # starlark::assert::allTrue(r#"
 * list("Hello, 世界".codepoints()) == [72, 101, 108, 108, 111, 44, 32, 19990, 30028]
 * # "#);
 * ```
 */
internal fun codepoints(
    thisStr: StringValue,
    heap: Heap,
): Result<Value> {
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
 * They are interpreted according to the [indexing conventions](
 * https://github.com/bazelbuild/starlark/blob/master/spec.md#indexing).
 *
 * This implementation does not count occurrence of `sub` in the string `S`
 * that overlap other occurrence of S (which can happen if some suffix of S
 * is a prefix of S). For instance, `"abababa".count("aba")` returns 2
 * for `[aba]a[aba]`, not counting the middle occurrence: `ab[aba]ba`
 * (this is following Python behavior).
 *
 * ```
 * # starlark::assert::allTrue(r#"
 * "hello, world!".count("o") == 2
 * "abababa".count("aba") == 2
 * "hello, world!".count("o", 7, 12) == 1  # in "world"
 * # "#);
 * ```
 */
internal fun count(
    thisStr: kotlin.String,
    needle: kotlin.String,
    start: NoneOr<Int> = NoneOr.None,
    end: NoneOr<Int> = NoneOr.None,
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
 * # starlark::assert::allTrue(r#"
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
 * They are interpreted according to the [indexing
 * conventions](#indexing).
 *
 * If no occurrence is found, `found` returns -1.
 *
 * ```
 * # starlark::assert::allTrue(r#"
 * "bonbon".find("on") == 1
 * "bonbon".find("on", 2) == 4
 * "bonbon".find("on", 2, 5) == -1
 * # "#);
 * ```
 */
internal fun find(
    thisStr: kotlin.String,
    needle: kotlin.String,
    start: NoneOr<Int> = NoneOr.None,
    end: NoneOr<Int> = NoneOr.None,
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
 * specifies which argument to import and how to format it, and consists of
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
 * ```
 * "a {} c".format(3) == "a 3 c"
 * "a{x}b{y}c{}".format(1, x=2, y=3) == "a2b3c1"
 * "a{}b{}c".format(1, 2) == "a1b2c"
 * "({1}, {0})".format("zero", "one") == "(one, zero)"
 * "Is {0!r} {0!s}?".format("heterological") == "Is \"heterological\" heterological?"
 * ```
 */
internal fun format(
    thisStr: kotlin.String,
    args: Arguments,
    eval: Evaluator,
): Result<StringValue> {
    val iter = args.positions(eval.heap()).getOrElse { return Result.failure(it) }
    val names = args.names().getOrElse { return Result.failure(it) }
    return dotFormat(
        thisStr,
        iter,
        names,
        eval.stringPool,
        eval.heap(),
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
 * # starlark::assert::allTrue(r#"
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
    start: NoneOr<Int> = NoneOr.None,
    end: NoneOr<Int> = NoneOr.None,
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
 * # starlark::assert::allTrue(r#"
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
 * # starlark::assert::allTrue(r#"
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
 * # starlark::assert::allTrue(r#"
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
 * # starlark::assert::allTrue(r#"
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
 * # starlark::assert::allTrue(r#"
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
 * # starlark::assert::allTrue(r#"
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
 * # starlark::assert::allTrue(r#"
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
 * # starlark::assert::allTrue(r#"
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
 * # starlark::assert::allTrue(r#"
 * ", ".join([]) == ""
 * ", ".join(("x", )) == "x"
 * ", ".join(["one", "two", "three"]) == "one, two, three"
 * "a".join("ctmrn".elems()) == "catamaran"
 * # "#);
 * ```
 */
internal fun join(
    thisStr: kotlin.String,
    toJoin: Value,
    heap: Heap,
): Result<Value> {
    val it = toJoin.iterate(heap).getOrElse { return Result.failure(it) }
    if (!it.hasNext()) {
        return Result.success(Value.newEmptyString())
    }
    val first = it.next()

    if (!it.hasNext()) {
        // If there is a singleton we can avoid reallocation
        val sv = asStr(first)
        return Result.success(sv.toValue())
    }

    val second = it.next()
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
    return Result.success(heap.allocStr(r.toString()).toValue())
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
 * # starlark::assert::allTrue(r#"
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
    val s = thisStr.asStr()
    val res = if (chars == null) {
        s.trimStart()
    } else {
        s.trimStart { c -> chars.contains(c) }
    }
    return if (res.length == s.length) {
        Result.success(thisStr)
    } else {
        Result.success(allocStrValue(heap, res))
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
 * # starlark::assert::allTrue(r#"
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
    val needleStr = needle.asStr()
    if (needleStr.isEmpty()) {
        return Result.failure(
            IllegalArgumentException("Empty separator cannot be used for partitioning")
        )
    }
    val s = thisStr.asStr()
    val offset = s.indexOf(needleStr)
    return if (offset != -1) {
        val offset2 = offset + needleStr.length
        Result.success(
            Triple(
                allocStrValue(heap, s.substring(0, offset)),
                needle,
                allocStrValue(heap, s.substring(offset2))
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
 * # starlark::assert::allTrue(r#"
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
            Result.success(allocStrValue(heap, replacen(thisStr.asStr(), old, new, count)))
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
            var searchFrom = 0
            while (true) {
                val idx = x.indexOf(old, searchFrom)
                if (idx == -1) break
                result.append(x.substring(lastEnd, idx))
                result.append(new)
                lastEnd = idx + old.length
                searchFrom = lastEnd
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
 * # starlark::assert::allTrue(r#"
 * "bonbon".rfind("on") == 4
 * "bonbon".rfind("on", None, 5) == 1
 * "bonbon".rfind("on", 2, 5) == -1
 * # "#);
 * ```
 */
internal fun rfind(
    thisStr: kotlin.String,
    needle: kotlin.String,
    start: NoneOr<Int> = NoneOr.None,
    end: NoneOr<Int> = NoneOr.None,
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
 * # starlark::assert::allTrue(r#"
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
    start: NoneOr<Int> = NoneOr.None,
    end: NoneOr<Int> = NoneOr.None,
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
 * # starlark::assert::allTrue(r#"
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
    val needleStr = needle.asStr()
    if (needleStr.isEmpty()) {
        return Result.failure(
            IllegalArgumentException("Empty separator cannot be used for partitioning")
        )
    }
    val s = thisStr.asStr()
    val offset = s.lastIndexOf(needleStr)
    return if (offset != -1) {
        val offset2 = offset + needleStr.length
        Result.success(
            Triple(
                allocStrValue(heap, s.substring(0, offset)),
                needle,
                allocStrValue(heap, s.substring(offset2))
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
 * # starlark::assert::allTrue(r#"
 * "banana".rsplit("n") == ["ba", "a", "a"]
 * "banana".rsplit("n", 1) == ["bana", "a"]
 * "one two  three".rsplit(None, 1) == ["one two", "three"]
 * # "#);
 * ```
 */
internal fun rsplit(
    thisStr: kotlin.String,
    sep: NoneOr<kotlin.String> = NoneOr.None,
    maxsplit: NoneOr<Int> = NoneOr.None,
    heap: Heap,
): Result<Value> {
    val maxsplitValue = when (val v = maxsplit.intoOption()) {
        null -> null
        else -> if (v < 0) null else (v + 1)
    }
    return when (val sepValue = sep.intoOption()) {
        null -> when (maxsplitValue) {
            null -> Result.success(allocStringList(thisStr.trim().split(Regex("\\s+")), heap))
            else -> Result.success(allocStringList(rsplitnWhitespace(thisStr, maxsplitValue), heap))
        }
        else -> {
            val v = when (maxsplitValue) {
                null -> thisStr.split(sepValue).reversed()
                else -> thisStr.split(sepValue, limit = maxsplitValue).reversed()
            }
            Result.success(allocStringList(v, heap))
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
 * # starlark::assert::allTrue(r#"
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
    val s = thisStr.asStr()
    val res = if (chars == null) {
        s.trimEnd()
    } else {
        s.trimEnd { c -> chars.contains(c) }
    }
    return if (res.length == s.length) {
        Result.success(thisStr)
    } else {
        Result.success(allocStrValue(heap, res))
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
 * # starlark::assert::allTrue(r#"
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
    sep: NoneOr<kotlin.String> = NoneOr.None,
    maxsplit: NoneOr<Int> = NoneOr.None,
    heap: Heap,
): Result<Value> {
    val maxsplitValue = when (val v = maxsplit.intoOption()) {
        null -> null
        else -> if (v < 0) null else (v + 1)
    }
    return when (val sepValue = sep.intoOption()) {
        null -> when (maxsplitValue) {
            null -> Result.success(allocStringList(thisStr.trim().split(Regex("\\s+")), heap))
            else -> Result.success(allocStringList(splitnWhitespace(thisStr, maxsplitValue), heap))
        }
        else -> {
            when (maxsplitValue) {
                null -> Result.success(allocStringList(thisStr.split(sepValue), heap))
                else -> Result.success(
                    allocStringList(thisStr.split(sepValue, limit = maxsplitValue), heap)
                )
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
 * # starlark::assert::allTrue(r#"
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
        if (x != -1) {
            val y = x
            val x2 = if (s.substring(y, minOf(y + 2, s.length)) == "\r\n") {
                y + 2
            } else {
                y + 1
            }
            if (keepends) {
                lines.add(allocStrValue(heap, s.substring(0, x2)))
            } else {
                lines.add(allocStrValue(heap, s.substring(0, y)))
            }
            if (x2 == s.length) {
                return Result.success(lines)
            }
            s = s.substring(x2)
        } else {
            if (s.isNotEmpty()) {
                lines.add(allocStrValue(heap, s))
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
 * # starlark::assert::allTrue(r#"
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
 * # starlark::assert::allTrue(r#"
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
    val s = thisStr.asStr()
    val res = if (chars == null) {
        s.trim()
    } else {
        s.trim { c -> chars.contains(c) }
    }
    return if (res.length == s.length) {
        Result.success(thisStr)
    } else {
        Result.success(allocStrValue(heap, res))
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
 * # starlark::assert::allTrue(r#"
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
 * # starlark::assert::allTrue(r#"
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
 * # starlark::assert::allTrue(r#"
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
        Result.success(allocStrValue(heap, x.substring(prefix.length)))
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
 * # starlark::assert::allTrue(r#"
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
        Result.success(allocStrValue(heap, x.substring(0, x.length - suffix.length)))
    } else {
        Result.success(thisStr)
    }
}

// Helper functions

private data class StrIndices(val start: Int, val haystack: kotlin.String)

// Clamps value to [0, limit].
private fun bound(value: Int, limit: Int): Int {
    return when {
        value <= 0 -> 0
        value >= limit -> limit
        else -> value
    }
}

// Convert optional Python-style indices (which may be negative) into a clamped [start, end] pair.
private fun convertIndices(len: Int, start: Int?, end: Int?): Pair<Int, Int> {
    val s = start ?: 0
    val e = end ?: len
    val adjustedEnd = if (e < 0) e + len else e
    val adjustedStart = if (s < 0) s + len else s
    return Pair(bound(adjustedStart, len), bound(adjustedEnd, len))
}

// Slice a string by code-point indices, returning the sub-string and the absolute start index,
// or null if the indices are invalid.
private fun convertStrIndices(str: kotlin.String, start: Int?, end: Int?): StrIndices? {
    val len = str.codePointCount()
    return when {
        // (null, null) => full string
        start == null && end == null -> StrIndices(0, str)
        // (start, null) where start >= 0
        start != null && end == null && start >= 0 -> {
            val byteStart = codePointOffset(str, start) ?: return null
            StrIndices(start, str.substring(byteStart))
        }
        // (null, end) where end >= 0
        start == null && end != null && end >= 0 -> {
            val byteEnd = codePointOffsetClamped(str, end)
            StrIndices(0, str.substring(0, byteEnd))
        }
        // (start, end) where start >= 0 && end >= start
        start != null && end != null && start >= 0 && end >= start -> {
            val byteStart = codePointOffset(str, start) ?: return null
            val remaining = str.substring(byteStart)
            val byteEnd = codePointOffsetClamped(remaining, end - start)
            StrIndices(start, remaining.substring(0, byteEnd))
        }
        // Both same sign and start > end => null
        start != null && end != null
            && ((start >= 0) == (end >= 0)) && start > end -> null
        // Slow path: need full length for negative indices
        else -> {
            val (s, e) = convertIndices(len, start, end)
            if (s > e) return null
            val byteStart = codePointOffsetClamped(str, s)
            val byteEnd = codePointOffsetClamped(str, e)
            StrIndices(s, str.substring(byteStart, byteEnd))
        }
    }
}

// Count Unicode code points in a string (handles surrogate pairs).
private fun kotlin.String.codePointCount(): Int {
    var count = 0
    var i = 0
    while (i < this.length) {
        val c = this[i]
        if (c.isHighSurrogate() && i + 1 < this.length && this[i + 1].isLowSurrogate()) {
            i += 2
        } else {
            i += 1
        }
        count++
    }
    return count
}

// Convert a code point index to a UTF-16 char offset. Returns null if index is out of bounds.
private fun codePointOffset(str: kotlin.String, codePointIndex: Int): Int? {
    var cpCount = 0
    var i = 0
    while (i < str.length && cpCount < codePointIndex) {
        val c = str[i]
        if (c.isHighSurrogate() && i + 1 < str.length && str[i + 1].isLowSurrogate()) {
            i += 2
        } else {
            i += 1
        }
        cpCount++
    }
    return if (cpCount == codePointIndex) i else null
}

// Convert a code point index to a UTF-16 char offset, clamped to string length.
private fun codePointOffsetClamped(str: kotlin.String, codePointIndex: Int): Int {
    var cpCount = 0
    var i = 0
    while (i < str.length && cpCount < codePointIndex) {
        val c = str[i]
        if (c.isHighSurrogate() && i + 1 < str.length && str[i + 1].isLowSurrogate()) {
            i += 2
        } else {
            i += 1
        }
        cpCount++
    }
    return i
}

// Find the length of the string in characters (code points).
private fun strLen(str: kotlin.String): Int {
    return str.codePointCount()
}

// Find the number of times a needle occurs within a string, non-overlapping.
private fun countMatches(haystack: kotlin.String, needle: kotlin.String): Int {
    if (needle.isEmpty()) return strLen(haystack) + 1
    var count = 0
    var startIndex = 0
    while (true) {
        val index = haystack.indexOf(needle, startIndex)
        if (index < 0) break
        count++
        startIndex = index + needle.length
    }
    return count
}

// Find the number of times a needle char occurs within a string.
private fun countMatchesByte(haystack: kotlin.String, byte: Char): Int {
    return haystack.count { it == byte }
}

// Delegate to the fully ported DotFormat.format function.
private fun dotFormat(
    format: kotlin.String,
    args: Iterator<Value>,
    kwargs: io.github.kotlinmania.starlark.values.types.dict.Dict,
    stringPool: StringPool,
    heap: Heap,
): Result<StringValue> {
    return format(format, args, kwargs, stringPool, heap)
}

/** Unpack a [Value] as a [StringValue], or throw if it is not a string. */
private fun asStr(value: Value): StringValue {
    return StringValue.new(value)
        ?: throw IllegalArgumentException("Expected a string value in 'to_join'")
}

/**
 * Allocate a string on the heap, returning a [StringValue].
 * Thin wrapper over [Heap.allocStr] kept for naming clarity at call sites.
 */
private fun allocStrValue(heap: Heap, x: kotlin.String): StringValue {
    return heap.allocStr(x)
}

/**
 * Allocate a list of strings on the heap, returning a [Value] representing the list.
 */
private fun allocStringList(strings: List<kotlin.String>, heap: Heap): Value {
    val values = strings.map { heap.allocStr(it).toValue() }
    return heap.allocListIter(values)
}

/** Replace the first [count] occurrences of [old] with [new] in [s]. */
private fun replacen(s: kotlin.String, old: kotlin.String, new: kotlin.String, count: Int): kotlin.String {
    if (count == 0) return s
    val result = StringBuilder()
    var remaining = s
    var replacements = 0
    while (replacements < count) {
        val index = remaining.indexOf(old)
        if (index == -1) break
        result.append(remaining.substring(0, index))
        result.append(new)
        remaining = remaining.substring(index + old.length)
        replacements++
    }
    result.append(remaining)
    return result.toString()
}
