// port-lint: source src/typing/callable_param.rs
package io.github.kotlinmania.starlark_kotlin.typing.callable_param

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

// Placeholder types referenced from other modules
// These will be replaced with real imports as the port progresses
class Ty {
    companion object {
        fun any(): Ty = Ty()
    }

    fun displayWith(config: TypeRenderConfig): TyDisplay = TyDisplay(this, config)
}

class TyDisplay(val ty: Ty, val config: TypeRenderConfig)
class TypeRenderConfig {
    companion object {
        val Default: TypeRenderConfig = TypeRenderConfig()
    }
}

class ArcStr(val value: String) {
    fun asStr(): String = value
    override fun equals(other: Any?): Boolean = other is ArcStr && value == other.value
    override fun hashCode(): Int = value.hashCode()
    override fun toString(): String = value
}

class DefParamIndices(
    val numPositional: Int,
    val numPositionalOnly: Int,
    val args: Int?,
    val kwargs: Int?,
) {
    fun posOnly(): IntRange = 0 until numPositionalOnly
    fun posOrNamed(): IntRange = numPositionalOnly until numPositional
    fun namedOnly(totalLen: Int): IntRange {
        val start = (args?.plus(1)) ?: numPositional
        val end = kwargs ?: totalLen
        return start until end
    }
}

class ParamFmt<N, T, D>(val name: N, val ty: T?, val default: D?)

const val PARAM_FMT_OPTIONAL: String = "..."

fun fmtParamSpec(
    sb: StringBuilder,
    posOnly: List<ParamFmt<String, TyDisplay, String>>,
    posOrNamed: List<ParamFmt<String, TyDisplay, String>>,
    args: ParamFmt<String, TyDisplay, String>?,
    namedOnly: List<ParamFmt<String, TyDisplay, String>>,
    kwargs: ParamFmt<String, TyDisplay, String>?,
) {
    // Formatting implementation placeholder
}

/// Indication whether parameter is required.
enum class ParamIsRequired {
    /// Parameter is required.
    Yes,
    /// Parameter is optional.
    No,
}

/// The type of a parameter - can be positional, by name, `*args` or `**kwargs`.
sealed class ParamMode {
    /// Parameter can only be passed by position.
    class PosOnly(val required: ParamIsRequired) : ParamMode()
    /// Parameter can be passed by position or name.
    class PosOrName(val name: ArcStr, val required: ParamIsRequired) : ParamMode()
    /// Parameter can only be passed by name.
    class NameOnly(val name: ArcStr, val required: ParamIsRequired) : ParamMode()
    /// Parameter is `*args`.
    data object Args : ParamMode()
    /// Parameter is `**kwargs`.
    data object Kwargs : ParamMode()
}

/// A parameter argument to a function
internal class Param(
    /// The type of parameter
    val mode: ParamMode,
    /// The type of the parameter.
    /// For `*args` it is the type of the tuple elements.
    /// For `**kwargs` it is the type of the dict values.
    val ty: Ty,
) {
    companion object {
        /// Create a `*args` parameter.
        ///
        /// `ty` is a tuple item type.
        fun args(ty: Ty): Param = Param(ParamMode.Args, ty)

        /// Create a `**kwargs` parameter.
        ///
        /// `ty` is a dict value type.
        fun kwargs(ty: Ty): Param = Param(ParamMode.Kwargs, ty)
    }

    fun allowsPos(): Boolean = when (mode) {
        is ParamMode.PosOnly, is ParamMode.PosOrName, is ParamMode.Args -> true
        is ParamMode.NameOnly, is ParamMode.Kwargs -> false
    }

    fun name(): String? = when (mode) {
        is ParamMode.PosOnly -> null
        is ParamMode.PosOrName -> mode.name.asStr()
        is ParamMode.NameOnly -> mode.name.asStr()
        is ParamMode.Args -> null
        is ParamMode.Kwargs -> null
    }

    /// Get a display name for this parameter.
    fun nameDisplay(): String = when (mode) {
        is ParamMode.PosOnly -> "_"
        is ParamMode.PosOrName -> mode.name.asStr()
        is ParamMode.NameOnly -> mode.name.asStr()
        is ParamMode.Args -> "*args"
        is ParamMode.Kwargs -> "**kwargs"
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Param) return false
        return mode == other.mode && ty == other.ty
    }

    override fun hashCode(): Int = mode.hashCode() * 31 + ty.hashCode()
}

private class ParamSpecSplit(
    val posOnly: List<Param>,
    val posOrNamed: List<Param>,
    val args: Param?,
    val namedOnly: List<Param>,
    val kwargs: Param?,
)

/// Callable parameter specification (e.g. positional only followed by `**kwargs`).
class ParamSpec(
    internal val params: List<Param>,
    internal val indices: DefParamIndices,
) {
    fun fmtWithConfig(sb: StringBuilder, config: TypeRenderConfig) {
        fun pf(p: Param, config: TypeRenderConfig): ParamFmt<String, TyDisplay, String> {
            val name = when (val m = p.mode) {
                is ParamMode.PosOrName -> m.name.asStr()
                is ParamMode.NameOnly -> m.name.asStr()
                is ParamMode.PosOnly -> "_"
                is ParamMode.Args -> "args"
                is ParamMode.Kwargs -> "kwargs"
            }
            val default: String? = when (val m = p.mode) {
                is ParamMode.PosOnly -> if (m.required == ParamIsRequired.No) PARAM_FMT_OPTIONAL else null
                is ParamMode.PosOrName -> if (m.required == ParamIsRequired.No) PARAM_FMT_OPTIONAL else null
                is ParamMode.NameOnly -> if (m.required == ParamIsRequired.No) PARAM_FMT_OPTIONAL else null
                is ParamMode.Args -> null
                is ParamMode.Kwargs -> null
            }
            return ParamFmt(name, p.ty.displayWith(config), default)
        }

        val split = split()
        fmtParamSpec(
            sb,
            split.posOnly.map { pf(it, config) },
            split.posOrNamed.map { pf(it, config) },
            split.args?.let { pf(it, config) },
            split.namedOnly.map { pf(it, config) },
            split.kwargs?.let { pf(it, config) },
        )
    }

    fun displayWith(config: TypeRenderConfig): ParamSpecDisplay {
        return ParamSpecDisplay(this, config)
    }

    fun params(): List<Param> = params

    /// Create a new parameter specification from different parameter kinds in order.
    companion object {
        fun newParts(
            posOnly: List<Pair<ParamIsRequired, Ty>>,
            posOrName: List<Triple<ArcStr, ParamIsRequired, Ty>>,
            args: Ty?,
            namedOnly: List<Triple<ArcStr, ParamIsRequired, Ty>>,
            kwargs: Ty?,
        ): ParamSpec {
            val seenNames = mutableSetOf<ArcStr>()
            val paramsList = mutableListOf<Param>()

            for ((req, ty) in posOnly) {
                paramsList.add(Param(ParamMode.PosOnly(req), ty))
            }

            val numPositionalOnly = paramsList.size

            for ((name, req, ty) in posOrName) {
                if (!seenNames.add(name)) {
                    error("duplicate parameter name: `$name`")
                }
                paramsList.add(Param(ParamMode.PosOrName(name, req), ty))
            }

            val numPositional = paramsList.size

            var indexOfArgs: Int? = null
            if (args != null) {
                indexOfArgs = paramsList.size
                paramsList.add(Param(ParamMode.Args, args))
            }

            for ((name, req, ty) in namedOnly) {
                if (!seenNames.add(name)) {
                    error("duplicate parameter name: `$name`")
                }
                paramsList.add(Param(ParamMode.NameOnly(name, req), ty))
            }

            var indexOfKwargs: Int? = null
            if (kwargs != null) {
                indexOfKwargs = paramsList.size
                paramsList.add(Param(ParamMode.Kwargs, kwargs))
            }

            return ParamSpec(
                params = paramsList.toList(),
                indices = DefParamIndices(
                    numPositional = numPositional,
                    numPositionalOnly = numPositionalOnly,
                    args = indexOfArgs,
                    kwargs = indexOfKwargs,
                ),
            )
        }

        /// `*, x, y`.
        fun newNamedOnly(
            namedOnly: List<Triple<ArcStr, ParamIsRequired, Ty>>,
        ): ParamSpec {
            return newParts(emptyList(), emptyList(), null, namedOnly, null)
        }

        /// `*args`.
        fun args(ty: Ty): ParamSpec {
            return newParts(emptyList(), emptyList(), ty, emptyList(), null)
        }

        /// `**kwargs`.
        fun kwargs(ty: Ty): ParamSpec {
            return newParts(emptyList(), emptyList(), null, emptyList(), ty)
        }

        /// `arg=, arg=, ..., arg, arg, ..., /`.
        fun posOnly(
            required: List<Ty>,
            optional: List<Ty>,
        ): ParamSpec {
            val posOnlyParams = required.map { ParamIsRequired.Yes to it } +
                optional.map { ParamIsRequired.No to it }
            return newParts(posOnlyParams, emptyList(), null, emptyList(), null)
        }

        /// No parameters.
        fun empty(): ParamSpec = posOnly(emptyList(), emptyList())

        fun any(): ParamSpec {
            return ParamSpec(
                params = listOf(Param.args(Ty.any()), Param.kwargs(Ty.any())),
                indices = DefParamIndices(
                    numPositional = 0,
                    numPositionalOnly = 0,
                    args = 0,
                    kwargs = 1,
                ),
            )
        }
    }

    /// Is `*args, **kwargs`.
    fun isAny(): Boolean = this == any()

    private fun split(): ParamSpecSplit {
        return ParamSpecSplit(
            posOnly = params.slice(indices.posOnly()),
            posOrNamed = params.slice(indices.posOrNamed()),
            args = indices.args?.let { params[it] },
            namedOnly = params.slice(indices.namedOnly(params.size)),
            kwargs = indices.kwargs?.let { params[it] },
        )
    }

    /// All parameters are required and positional only.
    fun allRequiredPosOnly(): List<Ty>? {
        val (posOnly, namedOnly) = allRequiredPosOnlyNamedOnly() ?: return null
        return if (namedOnly.isEmpty()) {
            posOnly
        } else {
            null
        }
    }

    /// All parameters are required and positional only or named only.
    fun allRequiredPosOnlyNamedOnly(): Pair<List<Ty>, List<Pair<String, Ty>>>? {
        val s = split()
        if (s.posOrNamed.isNotEmpty() || s.loadStmt.args != null || s.kwargs != null) {
            return null
        }

        val posOnlyTys = mutableListOf<Ty>()
        for (p in s.posOnly) {
            if (p.mode is ParamMode.PosOnly && p.mode.required == ParamIsRequired.Yes) {
                posOnlyTys.add(p.ty)
            } else {
                return null
            }
        }

        val namedOnlyTys = mutableListOf<Pair<String, Ty>>()
        for (p in s.namedOnly) {
            if (p.mode is ParamMode.NameOnly && p.mode.required == ParamIsRequired.Yes) {
                namedOnlyTys.add(p.mode.name.asStr() to p.ty)
            } else {
                return null
            }
        }

        return posOnlyTys to namedOnlyTys
    }

    override fun toString(): String {
        val sb = StringBuilder()
        fmtWithConfig(sb, TypeRenderConfig.Default)
        return sb.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is ParamSpec) return false
        return params == other.params
    }

    override fun hashCode(): Int = params.hashCode()
}

internal class ParamSpecDisplay(
    val paramSpec: ParamSpec,
    val config: TypeRenderConfig,
) {
    override fun toString(): String {
        val sb = StringBuilder()
        paramSpec.fmtWithConfig(sb, config)
        return sb.toString()
    }
}
