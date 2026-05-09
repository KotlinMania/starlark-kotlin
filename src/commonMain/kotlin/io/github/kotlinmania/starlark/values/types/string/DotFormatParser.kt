// port-lint: source ../starlark_syntax/src/dot_format_parser.rs
package io.github.kotlinmania.starlark.values.types.string

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

/** Output the capture as `str` or `repr`. */
enum class FormatConv {
    Str,
    Repr,
}

/** Token in the format string. */
sealed class FormatToken {
    /** Text to copy verbatim to the output. */
    data class Text(val text: String) : FormatToken()
    data class Capture(
        /** Format part inside curly braces before the conversion. */
        val capture: String,
        /** The position of this capture. This does not include the curly braces. */
        val pos: Int,
        /** The conversion to apply to this capture. */
        val conv: FormatConv
    ) : FormatToken()
    data class Escape(val escape: EscapeCurlyBrace) : FormatToken()
}

/** Emitted when processing an escape (`{{` or `}}`). */
enum class EscapeCurlyBrace {
    Open,
    Close;

    /** Get what this represents. */
    fun asStr(): String {
        return when (this) {
            Open -> "{"
            Close -> "}"
        }
    }

    /** Get back the escaped form for this. */
    fun backToEscape(): String {
        return when (this) {
            Open -> "{{"
            Close -> "}}"
        }
    }
}

/** Parser for `.format()` arguments. */
class FormatParser(private val view: String) {
    private var i: Int = 0

    /** Parse the next token from the format string. */
    fun next(): Result<FormatToken?> {
        var start = i

        while (i < view.length) {
            val c = view[i]
            if ((c == '{' || c == '}') && i != start) {
                val text = view.substring(start, i)
                return Result.success(FormatToken.Text(text))
            }
            when (c) {
                '{' -> {
                    check(i == start)
                    // Position of the identifier relative to the start of the format string.
                    val pos = i + 1
                    i += 1
                    while (i < view.length) {
                        when (view[i]) {
                            '}' -> {
                                val capture = view.substring(pos, i)
                                i += 1
                                return Result.success(FormatToken.Capture(capture, pos, FormatConv.Str))
                            }
                            '!' -> {
                                val capture = view.substring(pos, i)
                                val rem = view.substring(i + 1)
                                val conv = if (rem.startsWith("r")) {
                                    FormatConv.Repr
                                } else if (rem.startsWith("s")) {
                                    FormatConv.Str
                                } else if (rem.startsWith("}")) {
                                    return Result.failure(IllegalArgumentException(
                                        "Missing conversion character in format string `$view`"
                                    ))
                                } else {
                                    return Result.failure(IllegalArgumentException(
                                        "Invalid conversion in format string `$view`"
                                    ))
                                }
                                i += 2 // `!` and `r` or `s`
                                if (i >= view.length || view[i] != '}') {
                                    break
                                }
                                i += 1 // Closing brace.
                                return Result.success(FormatToken.Capture(capture, pos, conv))
                            }
                            '{' -> {
                                if (i == start + 1) {
                                    i += 1
                                    return Result.success(FormatToken.Escape(EscapeCurlyBrace.Open))
                                }
                                break
                            }
                            else -> i += 1
                        }
                    }
                    return Result.failure(IllegalArgumentException(
                        "Unmatched '{{' in format string `$view`"
                    ))
                }
                '}' -> {
                    check(i == start)
                    if (view.startsWith("}}", i)) {
                        i += 2
                        return Result.success(FormatToken.Escape(EscapeCurlyBrace.Close))
                    }
                    return Result.failure(IllegalArgumentException(
                        "Standalone '}}' in format string `$view`"
                    ))
                }
                else -> i += 1
            }
        }

        return if (start == i) {
            Result.success(null)
        } else {
            val text = view.substring(start, i)
            Result.success(FormatToken.Text(text))
        }
    }
}
