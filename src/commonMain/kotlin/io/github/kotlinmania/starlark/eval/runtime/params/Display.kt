// port-lint: source eval/runtime/params/display.rs
package io.github.kotlinmania.starlark.eval.runtime.params

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

/** Parameter or `*` or `/` separator, but only if needed for formatting. */
sealed class FmtParam<out T> {
    /** Positional-only, positional-or-named, or named-only parameter. */
    data class Regular<T>(val value: T) : FmtParam<T>()
    /** `*args` parameter. */
    data class Args<T>(val value: T) : FmtParam<T>()
    /** `**kwargs` parameter. */
    data class Kwargs<T>(val value: T) : FmtParam<T>()
    /** `/` separator. */
    data object Slash : FmtParam<Nothing>()
    /** `*` separator. */
    data object Star : FmtParam<Nothing>()
}

/** Flatten parameters and insert `/` and `*` separators if needed. */
internal fun <T> iterFmtParamSpec(
    posOnly: Iterable<T>,
    posNamed: Iterable<T>,
    args: T?,
    namedOnly: Iterable<T>,
    kwargs: T?,
): Sequence<FmtParam<T>> = sequence {
    val posOnlyIter = posOnly.iterator()
    val hasPositionalOnly = posOnlyIter.hasNext()
    val slash: FmtParam<T>? = if (hasPositionalOnly) {
        FmtParam.Slash
    } else {
        null
    }

    val namedOnlyList = namedOnly.toList()
    val hasNamedOnly = namedOnlyList.isNotEmpty()
    // `*args`, otherwise `*` if needed.
    val argsOrStar: FmtParam<T>? = when {
        args != null -> FmtParam.Args(args)
        hasNamedOnly -> FmtParam.Star
        else -> null
    }

    for (p in posOnlyIter) {
        yield(FmtParam.Regular(p))
    }
    if (slash != null) {
        yield(slash)
    }
    for (p in posNamed) {
        yield(FmtParam.Regular(p))
    }
    if (argsOrStar != null) {
        yield(argsOrStar)
    }
    for (p in namedOnlyList) {
        yield(FmtParam.Regular(p))
    }
    if (kwargs != null) {
        yield(FmtParam.Kwargs(kwargs))
    }
}

/** What to print for unknown default/optional. */
internal const val PARAM_FMT_OPTIONAL: String = "..."

internal data class ParamFmt<T, D>(
    /** Parameter name. */
    val name: String,
    /** Parameter type. If `None`, it will be omitted. */
    val ty: T?,
    val default: D?,
)

/** Utility to format function signature. */
internal fun <T, D> fmtParamSpec(
    f: StringBuilder,
    posOnly: Iterable<ParamFmt<T, D>>,
    posNamed: Iterable<ParamFmt<T, D>>,
    args: ParamFmt<T, D>?,
    namedOnly: Iterable<ParamFmt<T, D>>,
    kwargs: ParamFmt<T, D>?,
) {
    fmtParamSpecMaybeMultiline(
        f, null, posOnly, posNamed, args, namedOnly, kwargs, false,
    )
}

internal fun <T, D> fmtParamSpecMaybeMultiline(
    f: StringBuilder,
    // Single-line if `null`.
    indent: String?,
    posOnly: Iterable<ParamFmt<T, D>>,
    posNamed: Iterable<ParamFmt<T, D>>,
    args: ParamFmt<T, D>?,
    namedOnly: Iterable<ParamFmt<T, D>>,
    kwargs: ParamFmt<T, D>?,
    // If `true`, escape `*` to `\*` to avoid rendering in Markdown as bold or italic.
    escapeStars: Boolean,
) {
    class Printer(
        val f: StringBuilder,
        val escapeStars: Boolean,
    ) {
        fun writeParam(
            name: Any,
            ty: Any?,
            default: Any?,
        ) {
            f.append(name)
            if (ty != null) {
                f.append(": ")
                f.append(ty)
            }
            if (default != null) {
                f.append(" = ")
                f.append(default)
            }
        }
    }

    val printer = Printer(f, escapeStars)

    val iter = iterFmtParamSpec(posOnly, posNamed, args, namedOnly, kwargs).toList()

    val notEmpty = iter.isNotEmpty()

    for ((i, param) in iter.withIndex()) {
        if (i == 0) {
            if (indent != null) {
                printer.f.append(indent)
            }
        } else {
            if (indent != null) {
                printer.f.append(",\n")
                printer.f.append(indent)
            } else {
                printer.f.append(", ")
            }
        }
        val star = if (printer.escapeStars) "\\*" else "*"
        when (param) {
            is FmtParam.Regular -> {
                printer.writeParam(param.value.name, param.value.ty, param.value.default)
            }
            is FmtParam.Args -> {
                printer.writeParam("${star}${param.value.name}", param.value.ty, param.value.default)
            }
            is FmtParam.Kwargs -> {
                printer.writeParam("${star}${star}${param.value.name}", param.value.ty, param.value.default)
            }
            is FmtParam.Slash -> {
                printer.f.append("/")
            }
            is FmtParam.Star -> {
                printer.f.append(star)
            }
        }
    }

    if (notEmpty && indent != null) {
        printer.f.append(",\n")
    }
}
