// port-lint: source src/typing/callable_param.rs
package io.github.kotlinmania.starlark_kotlin.typing

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
 * Indication whether a parameter is required.
 */
enum class ParamIsRequired {
    /** ParameterP<AstNoPayload> is required. */
    Yes,
    /** ParameterP<AstNoPayload> is optional. */
    No,
}

/**
 * The type of a parameter — can be positional, by name, `*args` or `**kwargs`.
 */
sealed class ParamMode : Comparable<ParamMode> {
    /** ParameterP<AstNoPayload> can only be passed by position. */
    data class PosOnly(val required: ParamIsRequired) : ParamMode()

    /** ParameterP<AstNoPayload> can be passed by position or name. */
    data class PosOrName(val name: String, val required: ParamIsRequired) : ParamMode()

    /** ParameterP<AstNoPayload> can only be passed by name. */
    data class NameOnly(val name: String, val required: ParamIsRequired) : ParamMode()

    /** ParameterP<AstNoPayload> is `*args`. */
    data object Args : ParamMode()

    /** ParameterP<AstNoPayload> is `**kwargs`. */
    data object Kwargs : ParamMode()

    override fun compareTo(other: ParamMode): Int {
        val thisOrd = ordinal()
        val otherOrd = other.ordinal()
        if (thisOrd != otherOrd) return thisOrd.compareTo(otherOrd)
        return when {
            this is PosOnly && other is PosOnly -> this.required.compareTo(other.required)
            this is PosOrName && other is PosOrName -> {
                val nameComp = this.name.compareTo(other.name)
                if (nameComp != 0) nameComp else this.required.compareTo(other.required)
            }
            this is NameOnly && other is NameOnly -> {
                val nameComp = this.name.compareTo(other.name)
                if (nameComp != 0) nameComp else this.required.compareTo(other.required)
            }
            else -> 0
        }
    }

    private fun ordinal(): Int = when (this) {
        is PosOnly -> 0
        is PosOrName -> 1
        is NameOnly -> 2
        is Args -> 3
        is Kwargs -> 4
    }
}

/**
 * A parameter argument to a function.
 */
data class Param(
    /** The type of parameter. */
    val mode: ParamMode,
    /**
     * The type of the parameter.
     * For `*args` it is the type of the tuple elements.
     * For `**kwargs` it is the type of the dict values.
     */
    val ty: Ty,
) : Comparable<Param> {

    companion object {
        /**
         * Create a `*args` parameter.
         *
         * @param ty tuple item type.
         */
        fun args(ty: Ty): Param = Param(ParamMode.Args, ty)

        /**
         * Create a `**kwargs` parameter.
         *
         * @param ty dict value type.
         */
        fun kwargs(ty: Ty): Param = Param(ParamMode.Kwargs, ty)
    }

    /** Whether this parameter allows positional arguments. */
    fun allowsPos(): Boolean = when (mode) {
        is ParamMode.PosOnly, is ParamMode.PosOrName, is ParamMode.Args -> true
        is ParamMode.NameOnly, is ParamMode.Kwargs -> false
    }

    /** Get the name of this parameter, if it has one. */
    fun name(): String? = when (mode) {
        is ParamMode.PosOnly -> null
        is ParamMode.PosOrName -> mode.name
        is ParamMode.NameOnly -> mode.name
        is ParamMode.Args -> null
        is ParamMode.Kwargs -> null
    }

    /** Get a display name for this parameter. */
    fun nameDisplay(): String = when (mode) {
        is ParamMode.PosOnly -> "_"
        is ParamMode.PosOrName -> mode.name
        is ParamMode.NameOnly -> mode.name
        is ParamMode.Args -> "*args"
        is ParamMode.Kwargs -> "**kwargs"
    }

    /** Whether this parameter is required. */
    fun isRequired(): Boolean = when (mode) {
        is ParamMode.PosOnly -> mode.required == ParamIsRequired.Yes
        is ParamMode.PosOrName -> mode.required == ParamIsRequired.Yes
        is ParamMode.NameOnly -> mode.required == ParamIsRequired.Yes
        is ParamMode.Args, is ParamMode.Kwargs -> false
    }

    override fun compareTo(other: Param): Int {
        val modeComp = mode.compareTo(other.mode)
        if (modeComp != 0) return modeComp
        return ty.compareTo(other.ty)
    }
}

/**
 * Split view of a parameter spec for formatting and analysis.
 */
data class ParamSpecSplit(
    val posOnly: List<Param>,
    val posOrNamed: List<Param>,
    val args: Param?,
    val namedOnly: List<Param>,
    val kwargs: Param?,
)

/**
 * Callable parameter specification (e.g. positional only followed by `**kwargs`).
 */
class ParamSpec private constructor(
    private val params: List<Param>,
    private val numPositional: Int,
    private val numPositionalOnly: Int,
    private val argsIndex: Int?,
    private val kwargsIndex: Int?,
) : Comparable<ParamSpec> {

    companion object {
        /**
         * Create a new parameter specification from different parameter kinds in order.
         */
        fun newParts(
            posOnly: List<Pair<ParamIsRequired, Ty>> = emptyList(),
            posOrName: List<Triple<String, ParamIsRequired, Ty>> = emptyList(),
            args: Ty? = null,
            namedOnly: List<Triple<String, ParamIsRequired, Ty>> = emptyList(),
            kwargs: Ty? = null,
        ): ParamSpec {
            val seenNames = mutableSetOf<String>()
            val paramList = mutableListOf<Param>()

            for ((req, ty) in posOnly) {
                paramList.add(Param(ParamMode.PosOnly(req), ty))
            }

            val numPositionalOnly = paramList.size

            for ((name, req, ty) in posOrName) {
                require(seenNames.add(name)) { "duplicate parameter name: `$name`" }
                paramList.add(Param(ParamMode.PosOrName(name, req), ty))
            }

            val numPositional = paramList.size

            var argsIndex: Int? = null
            if (args != null) {
                argsIndex = paramList.size
                paramList.add(Param.args(args))
            }

            for ((name, req, ty) in namedOnly) {
                require(seenNames.add(name)) { "duplicate parameter name: `$name`" }
                paramList.add(Param(ParamMode.NameOnly(name, req), ty))
            }

            var kwargsIndex: Int? = null
            if (kwargs != null) {
                kwargsIndex = paramList.size
                paramList.add(Param.kwargs(kwargs))
            }

            return ParamSpec(paramList, numPositional, numPositionalOnly, argsIndex, kwargsIndex)
        }

        /** Create a `*args` parameter spec. */
        fun args(ty: Ty): ParamSpec = newParts(args = ty)

        /** Create a `**kwargs` parameter spec. */
        fun kwargs(ty: Ty): ParamSpec = newParts(kwargs = ty)

        /** Create a positional-only parameter spec. */
        fun posOnly(required: List<Ty>, optional: List<Ty> = emptyList()): ParamSpec =
            newParts(
                posOnly = required.map { ParamIsRequired.Yes to it } +
                    optional.map { ParamIsRequired.No to it }
            )

        /** No parameters. */
        fun empty(): ParamSpec = posOnly(emptyList())

        /** Accepts any arguments: `*args, **kwargs` with `typing.Any`. */
        private val ANY_INSTANCE: ParamSpec by lazy {
            newParts(args = Ty.any(), kwargs = Ty.any())
        }

        /** Create a parameter spec that accepts any arguments. */
        fun any(): ParamSpec = ANY_INSTANCE
    }

    /** Is `*args, **kwargs` (accepts any arguments). */
    fun isAny(): Boolean = this == any()

    /** Get all parameters. */
    fun params(): List<Param> = params

    /** Split the parameter spec into its component parts. */
    fun split(): ParamSpecSplit {
        val posOnlyRange = 0 until numPositionalOnly
        val posOrNamedRange = numPositionalOnly until numPositional
        val namedOnlyStart = (argsIndex?.let { it + 1 } ?: numPositional)
        val namedOnlyEnd = kwargsIndex ?: params.size

        return ParamSpecSplit(
            posOnly = params.subList(posOnlyRange.first, posOnlyRange.last + 1.coerceAtMost(params.size)),
            posOrNamed = if (posOrNamedRange.isEmpty()) emptyList() else params.subList(posOrNamedRange.first, posOrNamedRange.last + 1),
            args = argsIndex?.let { params[it] },
            namedOnly = if (namedOnlyStart >= namedOnlyEnd) emptyList() else params.subList(namedOnlyStart, namedOnlyEnd),
            kwargs = kwargsIndex?.let { params[it] },
        )
    }

    /**
     * All parameters are required and positional only.
     * Returns the list of types if so, null otherwise.
     */
    fun allRequiredPosOnly(): List<Ty>? {
        val (posOnly, namedOnly) = allRequiredPosOnlyNamedOnly() ?: return null
        return if (namedOnly.isEmpty()) posOnly else null
    }

    /**
     * All parameters are required and either positional only or named only.
     * Returns (posOnly types, namedOnly name+type pairs) if so, null otherwise.
     */
    fun allRequiredPosOnlyNamedOnly(): Pair<List<Ty>, List<Pair<String, Ty>>>? {
        val splitView = split()
        if (splitView.posOrNamed.isNotEmpty() || splitView.args != null || splitView.kwargs != null) {
            return null
        }

        val posOnlyTypes = mutableListOf<Ty>()
        for (p in splitView.posOnly) {
            if (p.mode !is ParamMode.PosOnly || (p.mode as ParamMode.PosOnly).required != ParamIsRequired.Yes) {
                return null
            }
            posOnlyTypes.add(p.ty)
        }

        val namedOnlyPairs = mutableListOf<Pair<String, Ty>>()
        for (p in splitView.namedOnly) {
            if (p.mode !is ParamMode.NameOnly || (p.mode as ParamMode.NameOnly).required != ParamIsRequired.Yes) {
                return null
            }
            namedOnlyPairs.add((p.mode as ParamMode.NameOnly).name to p.ty)
        }

        return posOnlyTypes to namedOnlyPairs
    }

    /** Format with a custom rendering configuration. */
    fun displayWith(config: TypeRenderConfig): String {
        return fmtWithConfig(config)
    }

    /** Format with a custom rendering configuration. */
    fun fmtWithConfig(config: TypeRenderConfig): String {
        val split = split()
        return buildString {
            var first = true
            fun sep() { if (!first) append(", "); first = false }

            for (p in split.posOnly) {
                sep()
                append(p.nameDisplay())
                append(": ")
                append(p.ty.fmtWithConfig(config))
                if (!p.isRequired()) append(" = ...")
            }

            if (split.posOnly.isNotEmpty() && (split.posOrNamed.isNotEmpty() || split.args != null || split.namedOnly.isNotEmpty() || split.kwargs != null)) {
                sep()
                append("/")
            }

            for (p in split.posOrNamed) {
                sep()
                append(p.nameDisplay())
                append(": ")
                append(p.ty.fmtWithConfig(config))
                if (!p.isRequired()) append(" = ...")
            }

            if (split.args != null) {
                sep()
                append("*args: ")
                append(split.args.ty.fmtWithConfig(config))
            }

            for (p in split.namedOnly) {
                sep()
                append(p.nameDisplay())
                append(": ")
                append(p.ty.fmtWithConfig(config))
                if (!p.isRequired()) append(" = ...")
            }

            if (split.kwargs != null) {
                sep()
                append("**kwargs: ")
                append(split.kwargs.ty.fmtWithConfig(config))
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ParamSpec) return false
        return params == other.params && numPositional == other.numPositional &&
            numPositionalOnly == other.numPositionalOnly &&
            argsIndex == other.argsIndex && kwargsIndex == other.kwargsIndex
    }

    override fun hashCode(): Int {
        var result = params.hashCode()
        result = 31 * result + numPositional
        result = 31 * result + numPositionalOnly
        result = 31 * result + (argsIndex ?: -1)
        result = 31 * result + (kwargsIndex ?: -1)
        return result
    }

    override fun toString(): String = fmtWithConfig(TypeRenderConfig.Default)

    override fun compareTo(other: ParamSpec): Int {
        val sizeComp = params.size.compareTo(other.params.size)
        if (sizeComp != 0) return sizeComp
        for ((a, b) in params.zip(other.params)) {
            val cmp = a.compareTo(b)
            if (cmp != 0) return cmp
        }
        return 0
    }
}
