// port-lint: source src/eval/runtime/params/spec.rs
package io.github.kotlinmania.starlark_kotlin.eval.runtime.params.spec

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

import io.github.kotlinmania.starlark_kotlin.collections.symbol.map.SymbolMap
import io.github.kotlinmania.starlark_kotlin.docs.DocParam
import io.github.kotlinmania.starlark_kotlin.docs.DocParams
import io.github.kotlinmania.starlark_kotlin.docs.DocString
import io.github.kotlinmania.starlark_kotlin.typing.ParamIsRequired
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.values.layout.ValueLike
import io.github.kotlinmania.starlark_kotlin.values.layout.typed.StringValue
import starlark_map.Hashed
import io.github.kotlinmania.starlark_kotlin.eval.runtime.Evaluator
import io.github.kotlinmania.starlark_kotlin.eval.runtime.Arguments
import io.github.kotlinmania.starlark_kotlin.eval.runtime.ArgumentsImpl
import io.github.kotlinmania.starlark_kotlin.eval.runtime.FunctionError
import io.github.kotlinmania.starlark_kotlin.eval.runtime.ResolvedArgName
import io.github.kotlinmania.starlark_kotlin.typing.DefParamIndices
import io.github.kotlinmania.starlark_kotlin.eval.runtime.params.ParamFmt
import io.github.kotlinmania.starlark_kotlin.eval.runtime.params.PARAM_FMT_OPTIONAL
import io.github.kotlinmania.starlark_kotlin.eval.runtime.params.fmtParamSpec
import io.github.kotlinmania.starlark_kotlin.eval.runtime.params.ParametersParser
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.types.dict.Dict
import io.github.kotlinmania.starlark_kotlin.values.types.dict.DictRef
import io.github.kotlinmania.starlark_kotlin.values.types.dict.dictRefFromValue
import io.github.kotlinmania.starlark_kotlin.values.types.dict.getValue
import io.github.kotlinmania.starlark_kotlin.values.types.dict.allocValue
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.allocTuple
import starlark_map.small_map.SmallMap

/** Describe parameter for [`ParametersSpec`]. */
// #[derive(Debug, Clone, Copy, Dupe, PartialEq, Eq, PartialOrd, Ord, Trace, Freeze, Allocative)]
// pub enum ParametersSpecParam<V>
sealed class ParametersSpecParam<out V> {
    /** ParameterP<AstNoPayload> is required. */
    data object Required : ParametersSpecParam<Nothing>()
    /** ParameterP<AstNoPayload> is optional (returned as `None`). */
    data object Optional : ParametersSpecParam<Nothing>()
    /** ParameterP<AstNoPayload> has default value. */
    data class Defaulted<V>(val value: V) : ParametersSpecParam<V>()

    // pub(crate) fn is_required(&self) -> ParamIsRequired
    fun isRequired(): ParamIsRequired = when (this) {
        is Required -> ParamIsRequired.Yes
        is Optional, is Defaulted -> ParamIsRequired.No
    }
}

// #[derive(Debug, Copy, Clone, Dupe, Coerce, PartialEq, Trace, Freeze, Allocative)]
// #[repr(C)]
// pub(crate) enum ParameterKind<V>
sealed class ParameterKind<out V> {
    data object Required : ParameterKind<Nothing>()
    /**
     * When optional parameter is not supplied, there's no error,
     * but the slot remains `None`.
     *
     * This is used only in native code, parameters of type `Option<T>` become `Optional`.
     */
    data object Optional : ParameterKind<Nothing>()
    data class Defaulted<V>(val value: V) : ParameterKind<V>()
    data object Args : ParameterKind<Nothing>()
    data object KWargs : ParameterKind<Nothing>()
}

// #[derive(Debug, Copy, Clone, Dupe, PartialEq, Eq, PartialOrd, Ord)]
// enum CurrentParameterStyle
internal enum class CurrentParameterStyle {
    /** ParameterP<AstNoPayload> can be only filled positionally. */
    PosOnly,
    /** ParameterP<AstNoPayload> can be filled positionally or by name. */
    PosOrNamed,
    /** ParameterP<AstNoPayload> can be filled by name only. */
    NamedOnly,
    /** No more args accepted. */
    NoMore,
}

/** Builder for [`ParametersSpec`] */
// pub(crate) struct ParametersSpecBuilder<V>
internal class ParametersSpecBuilder<V>(
    private val functionName: String,
    private val params: MutableList<Pair<String, ParameterKind<V>>> = mutableListOf(),
    private val names: SymbolMap<UInt> = SymbolMap(),
    /** Number of parameters that can be filled only positionally. */
    private var positionalOnly: Int = 0,
    /** Number of parameters that can be filled positionally. */
    private var positional: Int = 0,
    /** Has the no_args been passed */
    private var currentStyle: CurrentParameterStyle = CurrentParameterStyle.PosOnly,
    private var argsIndex: Int? = null,
    private var kwargsIndex: Int? = null,
) {
    // fn add(&mut self, name: &str, val: ParameterKind<V>)
    private fun add(name: String, kind: ParameterKind<V>) {
        check(kind !is ParameterKind.Args && kind !is ParameterKind.KWargs) {
            "adding parameter `$name` to `$functionName`"
        }

        // Regular arguments cannot follow `**kwargs`, but can follow `*args`.
        check(currentStyle < CurrentParameterStyle.NoMore) {
            "adding parameter `$name` to `$functionName`"
        }
        check(kwargsIndex == null) {
            "adding parameter `$name` to `$functionName`"
        }

        val i = params.size
        params.add(Pair(name, kind))
        if (currentStyle != CurrentParameterStyle.PosOnly) {
            val old = names.insert(name, i.toUInt())
            check(old == null) { "Repeated parameter `$name`" }
        }
        if (argsIndex == null && currentStyle != CurrentParameterStyle.NamedOnly) {
            // If you've already seen `args` or `no_args`, you can't enter these
            // positionally
            positional = i + 1
            if (currentStyle == CurrentParameterStyle.PosOnly) {
                positionalOnly = i + 1
            }
        }
    }

    /**
     * Add a required parameter. Will be an error if the caller doesn't supply
     * it. If you want to supply a position-only argument, prepend a `$` to
     * the name.
     */
    // pub(crate) fn required(&mut self, name: &str)
    fun required(name: String) {
        add(name, ParameterKind.Required)
    }

    /**
     * Add an optional parameter. Will be None if the caller doesn't supply it.
     * If you want to supply a position-only argument, prepend a `$` to the
     * name.
     */
    // pub(crate) fn optional(&mut self, name: &str)
    fun optional(name: String) {
        add(name, ParameterKind.Optional)
    }

    /**
     * Add an optional parameter. Will be the default value if the caller
     * doesn't supply it. If you want to supply a position-only argument,
     * prepend a `$` to the name.
     */
    // pub(crate) fn defaulted(&mut self, name: &str, val: V)
    fun defaulted(name: String, value: V) {
        add(name, ParameterKind.Defaulted(value))
    }

    // fn param(&mut self, name: &str, param: ParametersSpecParam<V>)
    fun param(name: String, param: ParametersSpecParam<V>) {
        when (param) {
            is ParametersSpecParam.Required -> required(name)
            is ParametersSpecParam.Optional -> optional(name)
            is ParametersSpecParam.Defaulted -> defaulted(name, param.value)
        }
    }

    /**
     * Add an `*args` parameter which will be an iterable sequence of parameters,
     * recorded into a list. A function can only have one `args`
     * parameter. After this call, any subsequent
     * required, optional or defaulted
     * parameters can _only_ be supplied by name.
     */
    // pub(crate) fn args(&mut self)
    fun args() {
        check(argsIndex == null) { "adding *args to `$functionName`" }
        check(currentStyle < CurrentParameterStyle.NamedOnly) { "adding *args to `$functionName`" }
        check(kwargsIndex == null) { "adding *args to `$functionName`" }
        params.add(Pair("*args", ParameterKind.Args))
        argsIndex = params.size - 1
        currentStyle = CurrentParameterStyle.NamedOnly
    }

    /** Following parameters can be filled positionally or by name. */
    // pub(crate) fn no_more_positional_only_args(&mut self)
    fun noMorePositionalOnlyArgs() {
        check(currentStyle == CurrentParameterStyle.PosOnly) {
            "adding / to `$functionName`"
        }
        currentStyle = CurrentParameterStyle.PosOrNamed
    }

    /**
     * This function has no `*args` parameter, corresponds to the Python parameter `*`.
     * After this call, any subsequent
     * required, optional or defaulted
     * parameters can _only_ be supplied by name.
     */
    // pub(crate) fn no_more_positional_args(&mut self)
    fun noMorePositionalArgs() {
        check(argsIndex == null) { "adding * to `$functionName`" }
        check(currentStyle < CurrentParameterStyle.NamedOnly) { "adding * to `$functionName`" }
        check(kwargsIndex == null) { "adding * to `$functionName`" }
        currentStyle = CurrentParameterStyle.NamedOnly
    }

    /**
     * Add a `**kwargs` parameter which will be a dictionary, recorded into a map.
     * A function can only have one `kwargs` parameter.
     */
    // pub(crate) fn kwargs(&mut self)
    fun kwargs() {
        check(kwargsIndex == null) { "adding **kwargs to `$functionName`" }
        params.add(Pair("**kwargs", ParameterKind.KWargs))
        currentStyle = CurrentParameterStyle.NoMore
        kwargsIndex = params.size - 1
    }

    /** Construct the parameters specification. */
    // pub(crate) fn finish(self) -> ParametersSpec<V>
    fun finish(): ParametersSpec<V> {
        val posOnly: UInt = positionalOnly.toUInt()
        val pos: UInt = positional.toUInt()
        check(posOnly <= pos) { "building `$functionName`" }
        val (paramNames, paramKinds) = params.unzip()
        return ParametersSpec(
            functionName = functionName,
            paramKinds = paramKinds,
            paramNames = paramNames,
            names = names,
            indices = DefParamIndices(
                numPositionalOnly = posOnly,
                numPositional = pos,
                args = argsIndex?.toUInt(),
                kwargs = kwargsIndex?.toUInt(),
            ),
        )
    }
}

/**
 * Define a list of parameters. This code assumes that all names are distinct and that
 * `*args`/`**kwargs` occur in well-formed locations.
 */
// V = Value, or FrozenValue
// #[derive(Debug, Clone, Trace, Freeze, Allocative)]
// #[repr(C)]
// pub struct ParametersSpec<V>
class ParametersSpec<V>(
    /** Only used in error messages */
    internal val functionName: String,
    /** Parameters in the order they occur. */
    internal val paramKinds: List<ParameterKind<V>>,
    /** ParameterP<AstNoPayload> names in the order they occur. */
    internal val paramNames: List<String>,
    /** Mapping from name to index where the argument lives. */
    internal val names: SymbolMap<UInt>,
    internal val indices: DefParamIndices,
) {
    companion object {
        /** Create a new [`ParametersSpec`] with the given function name and an advance capacity hint. */
        // pub(crate) fn with_capacity(function_name: String, capacity: usize) -> ParametersSpecBuilder<V>
        internal fun <V> withCapacity(functionName: String, capacity: Int = 0): ParametersSpecBuilder<V> =
            ParametersSpecBuilder(
                functionName = functionName,
                params = ArrayList(capacity),
                names = SymbolMap(capacity),
            )

        /** Create a new [`ParametersSpec`]. */
        // pub fn new_parts(...)
        fun <V> newParts(
            functionName: String,
            posOnly: List<Pair<String, ParametersSpecParam<V>>>,
            posOrNamed: List<Pair<String, ParametersSpecParam<V>>>,
            args: Boolean,
            namedOnly: List<Pair<String, ParametersSpecParam<V>>>,
            kwargs: Boolean,
        ): ParametersSpec<V> {
            val builder = withCapacity<V>(
                functionName,
                posOnly.size + posOrNamed.size + (if (args) 1 else 0) + namedOnly.size + (if (kwargs) 1 else 0),
            )

            for ((name, param) in posOnly) {
                builder.param(name, param)
            }
            builder.noMorePositionalOnlyArgs()
            for ((name, param) in posOrNamed) {
                builder.param(name, param)
            }
            if (args) {
                builder.args()
            } else {
                builder.noMorePositionalArgs()
            }
            for ((name, param) in namedOnly) {
                builder.param(name, param)
            }
            if (kwargs) {
                builder.kwargs()
            }
            return builder.finish()
        }

        /** ParameterP<AstNoPayload> parse with only named parameters. */
        // pub fn new_named_only(...)
        fun <V> newNamedOnly(
            functionName: String,
            namedOnly: List<Pair<String, ParametersSpecParam<V>>>,
        ): ParametersSpec<V> = newParts(
            functionName = functionName,
            posOnly = emptyList(),
            posOrNamed = emptyList(),
            args = false,
            namedOnly = namedOnly,
            kwargs = false,
        )
    }

    /** Produce an approximate signature for the function, combining the name and arguments. */
    // pub fn signature(&self) -> String
    fun signature(): String {
        val collector = StringBuilder()
        collectSignature(collector)
        return collector.toString()
    }

    // Generate a good error message for it
    // pub(crate) fn collect_signature(&self, collector: &mut String)
    internal fun collectSignature(collector: StringBuilder) {
        collector.append(functionName)
    }

    /**
     * Function parameter as they would appear in `def`
     * (excluding types, default values and formatting).
     */
    // pub fn parameters_str(&self) -> String
    fun parametersStr(): String {
        fun err(msg: String): String {
            return "<$msg>"
        }

        indices.args?.let { argsIdx ->
            if (argsIdx != indices.numPositional) {
                return err(
                    "Inconsistent *args: $functionName, args=$argsIdx, positional=${indices.numPositional}"
                )
            }
        }
        indices.kwargs?.let { kwargsIdx ->
            if (kwargsIdx.toInt() + 1 != paramKinds.size) {
                return err(
                    "Inconsistent **kwargs: $functionName, kwargs=$kwargsIdx, param_kinds.len()=${paramKinds.size}"
                )
            }
        }

        val pf = { i: Int ->
            var name = paramNames[i]
            name = name.removePrefix("**")
            name = name.removePrefix("*")
            ParamFmt(
                name = name,
                ty = null,
                default = when (paramKinds[i]) {
                    is ParameterKind.Defaulted, is ParameterKind.Optional -> PARAM_FMT_OPTIONAL
                    is ParameterKind.Required, is ParameterKind.Args, is ParameterKind.KWargs -> null
                },
            )
        }

        val s = StringBuilder()
        fmtParamSpec(
            s,
            indices.posOnly().map(pf),
            indices.posOrNamed().map(pf),
            indices.args?.let { pf(it.toInt()) },
            indices.namedOnly(paramKinds.size).map(pf),
            indices.kwargs?.let { pf(it.toInt()) },
        )
        return s.toString()
    }

    // pub(crate) fn resolve_name(&self, name: Hashed<&str>) -> ResolvedArgName
    internal fun resolveName(name: Hashed<String>): ResolvedArgName {
        val hash = name.hash()
        val paramIndex = names.getHashedStr(name)
        return ResolvedArgName(hash = hash, paramIndex = paramIndex?.toInt())
    }

    // pub(crate) fn has_args_or_kwargs(&self) -> bool
    internal fun hasArgsOrKwargs(): Boolean =
        indices.args != null || indices.kwargs != null

    /** Generate documentation for each of the parameters, using a custom formatter for default values. */
    // pub fn documentation_with_default_value_formatter(...)
    fun documentationWithDefaultValueFormatter(
        parameterTypes: List<Ty>,
        parameterDocs: MutableMap<String, DocString?>,
        formatter: (V) -> String,
    ): DocParams {
        check(paramKinds.size == parameterTypes.size) {
            "function: `$functionName`"
        }

        val dp = { i: Int ->
            var name = paramNames[i]
            name = name.removePrefix("**")
            name = name.removePrefix("*")

            val docs = parameterDocs.remove(name)

            DocParam(
                name = name,
                docs = docs,
                typ = parameterTypes[i],
                defaultValue = when (val kind = paramKinds[i]) {
                    is ParameterKind.Required -> null
                    is ParameterKind.Optional -> PARAM_FMT_OPTIONAL
                    is ParameterKind.Defaulted -> formatter(kind.value)
                    is ParameterKind.Args -> null
                    is ParameterKind.KWargs -> null
                },
            )
        }

        return DocParams(
            posOnly = indices.posOnly().map(dp),
            posOrNamed = indices.posOrNamed().map(dp),
            args = indices.args?.let { dp(it.toInt()) },
            namedOnly = indices.namedOnly(paramKinds.size).map(dp),
            kwargs = indices.kwargs?.let { dp(it.toInt()) },
        )
    }

    /** Number of function parameters. */
    // pub fn len(&self) -> usize
    fun len(): Int = paramKinds.size

    /**
     * Move parameters from [`Arguments`] to a list of [`Value`],
     * using the supplied [`ParametersSpec`].
     */
    // pub fn collect(...)
    fun collect(
        args: Arguments,
        slots: MutableList<Value?>,
        heap: Heap,
    ) {
        collectInline(args.inner, slots, heap)
    }

    /**
     * Collect `N` arguments.
     *
     * This function is called by generated code.
     */
    // pub fn collect_into<const N: usize>(...)
    fun collectInto(
        n: Int,
        args: Arguments,
        heap: Heap,
    ): MutableList<Value?> {
        val slots = MutableList<Value?>(n) { null }
        collect(args, slots, heap)
        return slots
    }

    /**
     * A variant of `collect` that is always inlined
     * for Def and NativeFunction that are hot-spots
     */
    // pub(crate) fn collect_inline(...)
    internal fun collectInline(
        args: ArgumentsImpl<*>,
        slots: MutableList<Value?>,
        heap: Heap,
    ) {
        collectInlineImpl(args, slots, heap)
    }

    // fn collect_inline_impl(...)
    private fun collectInlineImpl(
        args: ArgumentsImpl<*>,
        slots: MutableList<Value?>,
        heap: Heap,
    ) {
        // If the arguments equal the length and the kinds, and we don't have any other args,
        // then no_args, *args and **kwargs must all be unset,
        // and we don't have to create args/kwargs objects, we can skip everything else
        if (args.pos().size == indices.numPositional.toInt()
            && args.pos().size == paramKinds.size
            && args.named().isEmpty()
            && args.args() == null
            && args.kwargs() == null
        ) {
            for ((i, v) in args.pos().withIndex()) {
                slots[i] = v
            }
            return
        }

        collectSlow(args, slots, heap)
    }

    // fn collect_slow(...)
    private fun collectSlow(
        args: ArgumentsImpl<*>,
        slots: MutableList<Value?>,
        heap: Heap,
    ) {
        /** Lazily initialized `kwargs` object. */
        class LazyKwargs {
            var kwargs: SmallMap<Value, Value>? = null

            // Return true if the value is a duplicate
            fun insert(key: Hashed<StringValue>, value: Value): Boolean {
                val valueKey = Hashed.newUnchecked(key.hash(), key.key().toValue())
                val mp = kwargs
                if (mp == null) {
                    val newMp = SmallMap.withCapacity<Value, Value>(12)
                    newMp.insertHashedUniqueUnchecked(valueKey, value)
                    kwargs = newMp
                    return false
                }
                return mp.insertHashed(valueKey, value) != null
            }

            fun insertUniqueUnchecked(key: Hashed<StringValue>, value: Value) {
                val valueKey = Hashed.newUnchecked(key.hash(), key.key().toValue())
                val mp = kwargs
                if (mp == null) {
                    val newMp = SmallMap.withCapacity<Value, Value>(12)
                    newMp.insertHashedUniqueUnchecked(valueKey, value)
                    kwargs = newMp
                } else {
                    mp.insertHashedUniqueUnchecked(valueKey, value)
                }
            }

            fun alloc(heap: Heap): Value {
                val kwargsMap = kwargs ?: SmallMap.new()
                val dict = Dict(kwargsMap)
                return dict.allocValue(heap)
            }
        }

        val len = paramKinds.size
        // We might do unchecked stuff later on, so make sure we have as many slots as we expect
        check(slots.size >= len)

        val starArgs = mutableListOf<Value>()
        val kwargs = LazyKwargs()
        var nextPosition = 0

        // First deal with positional parameters
        if (args.pos().size <= indices.numPositional.toInt()) {
            // fast path for when we don't need to bounce down to filling in args
            for ((i, v) in args.pos().withIndex()) {
                slots[i] = v
            }
            nextPosition = args.pos().size
        } else {
            for (v in args.pos()) {
                if (nextPosition < indices.numPositional.toInt()) {
                    slots[nextPosition] = v
                    nextPosition++
                } else {
                    starArgs.add(v)
                }
            }
        }

        // Next deal with named parameters
        // The lowest position at which we've written a name.
        var lowestName = Int.MAX_VALUE
        // Avoid a lot of loop setup etc in the common case
        if (args.names().names().isNotEmpty()) {
            for ((nameEntry, v) in args.names().names().zip(args.named())) {
                val (name, nameValue) = nameEntry
                // Safe to use new_unchecked because hash for the Value and str are the same
                val paramIndex = name.getIndexFromParamSpec(this)
                if (paramIndex == null) {
                    kwargs.insertUniqueUnchecked(
                        Hashed.newUnchecked(name.smallHash(), nameValue),
                        v,
                    )
                } else {
                    slots[paramIndex] = v
                    lowestName = minOf(lowestName, paramIndex)
                }
            }
        }

        // Next up are the *args parameters
        args.args()?.let { paramArgs ->
            for (v in paramArgs.iterate(heap).getOrElse { throw FunctionError.ArgsArrayIsNotIterable }) {
                if (nextPosition < indices.numPositional.toInt()) {
                    slots[nextPosition] = v
                    nextPosition++
                } else {
                    starArgs.add(v)
                }
            }
        }

        // Check if the named arguments clashed with the positional arguments
        if (nextPosition > lowestName) {
            throw FunctionError.RepeatedArg(name = paramNames[lowestName])
        }

        // Now insert the kwargs, if there are any
        args.kwargs()?.let { paramKwargs ->
            val dictRef = dictRefFromValue(paramKwargs)
                ?: throw FunctionError.KwArgsIsNotDict
            val dict: Dict by dictRef
            for ((k, v) in dict.content.iterHashed()) {
                val keyValue = k.key() // Value
                val s = StringValue.new(keyValue)
                    ?: throw FunctionError.ArgsValueIsNotString
                val hashedStr = Hashed.newUnchecked(k.hash(), s.asStr())
                val paramIndex = names.getHashedStringValue(hashedStr)
                if (paramIndex == null) {
                    val repeat = kwargs.insert(Hashed.newUnchecked(k.hash(), s), v)
                    if (repeat) {
                        throw FunctionError.RepeatedArg(name = s.asStr())
                    }
                } else {
                    val thisSlot = slots[paramIndex.toInt()]
                    val repeat = thisSlot != null
                    slots[paramIndex.toInt()] = v
                    if (repeat) {
                        throw FunctionError.RepeatedArg(name = s.asStr())
                    }
                }
            }
        }

        // We have moved parameters into all the relevant slots, so need to finalise things.
        // We need to set default values and error if any required values are missing
        for (index in nextPosition until paramKinds.size) {
            val slot = slots[index]

            // We know that up to next_position got filled positionally, so we don't need to check those
            if (slot != null) {
                continue
            }
            when (val def = paramKinds[index]) {
                is ParameterKind.Required -> {
                    val paramName = paramNames[index]
                    if (index < indices.numPositionalOnly.toInt()) {
                        error("Missing positional-only parameter `$paramName` for call to `$functionName`")
                    } else if (index >= indices.numPositional.toInt()) {
                        error("Missing named-only parameter `$paramName` for call to `$functionName`")
                    } else {
                        error("Missing parameter `$paramName` for call to `$functionName`")
                    }
                }
                is ParameterKind.Defaulted -> {
                    @Suppress("UNCHECKED_CAST")
                    slots[index] = (def.value as? Value)
                }
                else -> {}
            }
        }

        // Now set the kwargs/args slots, if they are requested, and fail if they are absent but used
        // Note that we deliberately give warnings about missing parameters _before_ giving warnings
        // about unexpected extra parameters, so if a user misspells an argument they get a better error.
        val argsPos = indices.args
        if (argsPos != null) {
            slots[argsPos.toInt()] = heap.allocTuple(starArgs)
        } else if (starArgs.isNotEmpty()) {
            throw FunctionError.ExtraPositionalArg(
                count = starArgs.size,
                function = signature(),
            )
        }

        val kwargsPos = indices.kwargs
        if (kwargsPos != null) {
            slots[kwargsPos.toInt()] = kwargs.alloc(heap)
        } else if (kwargs.kwargs != null) {
            throw FunctionError.ExtraNamedArg(
                names = kwargs.kwargs!!.keys().map { it.toStr() }.toList(),
                function = signature(),
            )
        }
    }

    /** Check if current parameters can be filled with given arguments signature. */
    // pub fn can_fill_with_args(&self, pos: usize, names: &[&str]) -> bool
    fun canFillWithArgs(pos: Int, argNames: List<String>): Boolean {
        val filled = BooleanArray(paramKinds.size)
        for (p in 0 until pos) {
            if (p < indices.numPositional.toInt()) {
                filled[p] = true
            } else if (indices.args != null) {
                // Filled into `*args`.
            } else {
                return false
            }
        }
        if (pos > indices.numPositional.toInt() && indices.args == null) {
            return false
        }
        for (name in argNames) {
            val i = names.getStr(name)
            if (i != null) {
                if (filled[i.toInt()]) {
                    // Duplicate argument.
                    return false
                }
                filled[i.toInt()] = true
            } else {
                if (indices.kwargs == null) {
                    return false
                }
            }
        }
        for ((isFilled, p) in filled.zip(paramKinds)) {
            if (isFilled) {
                continue
            }
            when (p) {
                is ParameterKind.Args -> {}
                is ParameterKind.KWargs -> {}
                is ParameterKind.Defaulted -> {}
                is ParameterKind.Optional -> {}
                is ParameterKind.Required -> return false
            }
        }
        return true
    }

    /** Generate documentation for each of the parameters. */
    // pub fn documentation(...)
    fun documentation(
        parameterTypes: List<Ty>,
        parameterDocs: MutableMap<String, DocString?>,
    ): DocParams = documentationWithDefaultValueFormatter(
        parameterTypes,
        parameterDocs,
    ) { v ->
        @Suppress("UNCHECKED_CAST")
        (v as? Value)?.toRepr() ?: v.toString()
    }

    /** Create a [`ParametersParser`] for given arguments. */
    // pub fn parser(...)
    fun <R> parser(
        args: Arguments,
        eval: Evaluator,
        k: (ParametersParser, Evaluator) -> R,
    ): R {
        val slots = MutableList<Value?>(len()) { null }
        collectInline(args.inner, slots, eval.heap())
        val parser = ParametersParser.new(slots, paramNames)
        val r = k(parser, eval)
        check(parser.isEof()) {
            "Parser for `$functionName` did not consume all arguments"
        }
        return r
    }
}
