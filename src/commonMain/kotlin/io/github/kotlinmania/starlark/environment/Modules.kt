// port-lint: source environment/modules.rs
package io.github.kotlinmania.starlark.environment

/*
 * Copyright 2018 The Starlark in Rust Authors.
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
 * The environment, called "Module" in [the spec](
 * https://github.com/bazelbuild/starlark/blob/master/spec.md)
 * is the list of variable in the current scope. It can be frozen, after which
 * all values from this environment become immutable.
 */

import starlarkmap.Hashed
import starlarkmap.smallmap.SmallMap
import io.github.kotlinmania.starlark.docs.DocItem
import io.github.kotlinmania.starlark.docs.DocModule
import io.github.kotlinmania.starlark.docs.DocString
import io.github.kotlinmania.starlark.docs.DocStringKind
import io.github.kotlinmania.starlark.docs.fromDocstring
import io.github.kotlinmania.starlark.eval.evalModule
import io.github.kotlinmania.starlark.eval.runtime.Evaluator
import io.github.kotlinmania.starlark.eval.runtime.profile.ProfileMode
import io.github.kotlinmania.starlark.eval.runtime.profile.heap.RetainedHeapProfileMode
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeapRef
import io.github.kotlinmania.starlark.values.FrozenRef
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.avalues.allocList
import kotlin.time.Duration
import kotlin.time.TimeSource
import io.github.kotlinmania.starlark.values.layout.typed.FrozenStringValue
import io.github.kotlinmania.starlark.values.owned.OwnedFrozenValue
import io.github.kotlinmania.starlark.values.layout.heap.profile.RetainedHeapProfile
import io.github.kotlinmania.starlark.eval.runtime.profile.data.ProfileData
import io.github.kotlinmania.starlark.values.layout.heap.profile.AggregateHeapProfileInfo
import io.github.kotlinmania.starlark.values.layout.heap.HeapKind
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.layout.Freezer
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.heap.Tracer
import io.github.kotlinmania.starlark.values.layout.heap.ValueHolder
import io.github.kotlinmania.starlark.values.types.list.ListRef
import io.github.kotlinmania.starlark.syntax.AstModule
import io.github.kotlinmania.starlark.syntax.Dialect
import io.github.kotlinmania.starlark.syntax.ast.Visibility
import io.github.kotlinmania.starlark.eval.compiler.postFreeze
import io.github.kotlinmania.starlark.errors.didYouMean
import io.github.kotlinmania.starlark.EnvironmentError

/**
 * enum ModuleError
 */
sealed class ModuleError(message: String) : Exception(message) {
    class RetainedMemoryProfileNotEnabled :
        ModuleError("Retained memory profiling is not enabled")

    class ExtraValueAlreadySet(val typeName: String) :
        ModuleError("Extra value already set to a value of type `$typeName`")
}

/**
 * The result of freezing a [Module], making it and its contained values immutable.
 *
 * The values of this [FrozenModule] are stored on a frozen heap, a reference to which
 * can be obtained using [frozenHeap]. Be careful not to use
 * these values after the [FrozenModule] has been released unless you obtain a reference
 * to the frozen heap.
 */
class FrozenModule internal constructor(
    private val heap: FrozenHeapRef,
    private val module: FrozenRef<FrozenModuleData>,
    private val _extraValue: FrozenValue?,
    /** Module evaluation duration. */
    internal val evalDuration: Duration,
) {
    /**
     * Convert items in [Globals] into a [FrozenModule].
     * This function can be used to implement starlark module
     */
    companion object {
        fun fromGlobals(globals: Globals): Result<FrozenModule> {
            return Module.withTempHeap { module ->
                module.frozenHeap().addReference(globals.heap())

                for ((name, value) in globals.iter()) {
                    module.set(name, value.toValue())
                }

                globals.docstring()?.let { docstring ->
                    module.setDocstring(docstring)
                }

                module.freeze()
            }
        }
    }

    private fun getAnyVisibilityOption(name: String): Pair<OwnedFrozenValue, Visibility>? {
        val (slot, vis) = module.value.names.getName(name) ?: return null
        val value = module.value.slots.getSlot(slot) ?: return null
        return OwnedFrozenValue(heap, value) to vis
    }

    /**
     * Get value, exported or private by name.
     */
    fun getAnyVisibility(name: String): Result<Pair<OwnedFrozenValue, Visibility>> {
        return getAnyVisibilityOption(name)?.let { Result.success(it) }
            ?: run {
                val better = didYouMean(name, names().map { it.asStr() }.toList())
                if (better != null) {
                    Result.failure(
                        EnvironmentError.ModuleHasNoSymbolDidYouMean(name, better)
                    )
                } else {
                    Result.failure(
                        EnvironmentError.ModuleHasNoSymbol(name)
                    )
                }
            }
    }

    /**
     * Get the value of the exported variable [name].
     *
     * Returns `null` if symbol is not found, error if symbol is private.
     */
    fun getOption(name: String): Result<OwnedFrozenValue?> {
        return when (val entry = getAnyVisibilityOption(name)) {
            null -> Result.success(null)
            else -> when (entry.second) {
                Visibility.Private -> Result.failure(
                    EnvironmentError.ModuleSymbolIsNotExported(name)
                )
                Visibility.Public -> Result.success(entry.first)
            }
        }
    }

    /**
     * Get the value of the exported variable [name].
     * Returns an error if the variable isn't defined in the module or it is private.
     */
    fun get(name: String): Result<OwnedFrozenValue> {
        return getAnyVisibility(name).mapCatching { (value, vis) ->
            when (vis) {
                Visibility.Private -> throw EnvironmentError.ModuleSymbolIsNotExported(name)
                Visibility.Public -> value
            }
        }
    }

    /**
     * Iterate through all the names defined in this module.
     * Only includes symbols that are publicly exposed.
     */
    fun names(): Sequence<FrozenStringValue> {
        return module.value.names()
    }

    /**
     * Obtain the [FrozenHeapRef] which owns the storage of all values defined in this module.
     */
    fun frozenHeap(): FrozenHeapRef {
        return heap
    }

    /**
     * Print out some approximation of the module definitions.
     */
    fun describe(): String {
        return module.value.describe()
    }

    internal fun allItems(): Sequence<Pair<FrozenStringValue, FrozenValue>> {
        return module.value.allItems()
    }

    /**
     * The documentation for the module, and all of its top level values.
     */
    fun documentation(): DocModule {
        val members = SmallMap.new<String, DocItem>()
        for ((name, value) in allItems()) {
            val vis = getAnyVisibilityOption(name.asStr())
            if (vis != null && vis.second == Visibility.Public) {
                members.insert(name.asStr(), value.toValue().documentation())
            }
        }

        return DocModule(
            docs = module.value.documentation(),
            members = members,
        )
    }

    /**
     * Retained memory info, or error if not enabled.
     */
    fun heapProfile(): Result<ProfileData> {
        return when (val p = module.value.heapProfile) {
            null -> Result.failure(ModuleError.RetainedMemoryProfileNotEnabled())
            else -> Result.success(p.toProfile())
        }
    }

    /**
     * `extraValue` field from `Module`, frozen.
     */
    fun extraValue(): FrozenValue? {
        return _extraValue
    }

    /**
     * `extraValue` field from `Module`, frozen.
     */
    fun ownedExtraValue(): OwnedFrozenValue? {
        return _extraValue?.let { OwnedFrozenValue(heap, it) }
    }
}

internal class FrozenModuleData(
    val names: FrozenNames,
    val slots: FrozenSlots,
    private val docstring: String?,
    /** When heap profile enabled, this field stores retained memory info. */
    val heapProfile: RetainedHeapProfile?,
) {
    fun names(): Sequence<FrozenStringValue> {
        return names.symbols().map { it.first }
    }

    fun describe(): String {
        return items()
            .map { (name, value) -> value.toValue().describe(name.asStr()) }
            .joinToString("\n")
    }

    private fun items(): Sequence<Pair<FrozenStringValue, FrozenValue>> {
        return names.symbols()
            .mapNotNull { (name, slot) ->
                slots.getSlot(slot)?.let { name to it }
            }
    }

    fun allItems(): Sequence<Pair<FrozenStringValue, FrozenValue>> {
        return names.allSymbols()
            .mapNotNull { (name, slot) ->
                slots.getSlot(slot)?.let { name to it }
            }
    }

    internal fun getSlot(slot: ModuleSlotId): FrozenValue? {
        return slots.getSlot(slot)
    }

    /**
     * Try and go back from a slot to a name.
     * Inefficient - only import in error paths.
     */
    internal fun getSlotName(slot: ModuleSlotId): FrozenStringValue? {
        for ((s, i) in names.symbols()) {
            if (i == slot) {
                return s
            }
        }
        return null
    }

    fun documentation(): DocString? {
        return docstring?.let { DocString.fromDocstring(DocStringKind.Starlark, it) }
    }
}

/**
 * A container for user values, used during execution.
 *
 * A module contains both a [FrozenHeap] and [Heap] on which different values are allocated.
 * You can get references to these heaps with [frozenHeap] and [heap].
 * Be careful not to import these values after the [Module] has been
 * released unless you obtain a reference to the frozen heap.
 */
class Module internal constructor(
    private val heap: Heap,
    private val frozenHeap: FrozenHeap = FrozenHeap(),
    private val names: MutableNames = MutableNames(),
    private val slots: MutableSlots = MutableSlots(),
    private var docstring: String? = null,
    /** Module evaluation duration. */
    private var _evalDuration: Duration = Duration.ZERO,
    /** Field that can be used for any purpose you want. */
    private var _extraValue: Value? = null,
    /** When `Some`, heap profile is collected on freeze. */
    private var heapProfileOnFreeze: RetainedHeapProfileMode? = null,
) {
    companion object {
        /**
         * Create a new module environment with no contents and make it available to the user.
         * Module is discarded after the function returns.
         */
        fun <R> withTempHeap(f: (Module) -> R): R {
            return Heap.temp { h ->
                h.allowGc()
                f(withHeap(h))
            }
        }

        /**
         * Like [withTempHeap], but async (suspend).
         */
        suspend fun <R> withTempHeapAsync(f: suspend (Module) -> R): R {
            return Heap.tempAsync { h ->
                h.allowGc()
                f(withHeap(h))
            }
        }

        /**
         * Create a new module environment with no contents that will import the provided heap.
         */
        internal fun withHeap(heap: Heap): Module {
            return Module(heap = heap)
        }

        /**
         * Symbols starting with underscore are considered private.
         */
        internal fun defaultVisibility(symbol: String): Visibility {
            return if (symbol.startsWith('_')) Visibility.Private else Visibility.Public
        }
    }

    internal fun enableRetainedHeapProfile(mode: RetainedHeapProfileMode) {
        heapProfileOnFreeze = mode
    }

    /**
     * Get the heap on which values are allocated by this module.
     */
    fun heap(): Heap {
        return heap
    }

    /**
     * Get the frozen heap on which frozen values are allocated by this module.
     */
    fun frozenHeap(): FrozenHeap {
        return frozenHeap
    }

    /**
     * Iterate through all the names defined in this module.
     * Only includes symbols that are publicly exposed.
     */
    fun names(): Sequence<FrozenStringValue> {
        return names.allNamesAndVisibilities()
            .asSequence()
            .filter { (_, vis) -> vis == Visibility.Public }
            .map { (name, _) -> name }
    }

    internal fun valuesBySlotId(): List<Pair<ModuleSlotId, Value>> {
        return slots().valuesBySlotId()
    }

    /**
     * Iterate through all the names defined in this module, including those that are private.
     */
    fun namesAndVisibilities(): List<Pair<FrozenStringValue, Visibility>> {
        return names.allNamesAndVisibilities()
    }

    internal fun mutableNames(): MutableNames {
        return names
    }

    internal fun slots(): MutableSlots {
        return slots
    }

    /**
     * Get value, exported or private by name.
     */
    internal fun getAnyVisibility(name: Hashed<String>): Pair<Value, Visibility>? {
        val (slot, vis) = names.getName(name) ?: return null
        val value = slots().getSlot(slot) ?: return null
        return value to vis
    }

    /**
     * Get the value of the exported variable [name].
     * Returns null if the variable isn't defined in the module or it is private.
     */
    fun get(name: String): Value? {
        return getAnyVisibility(Hashed.new(name))?.let { (v, vis) ->
            when (vis) {
                Visibility.Private -> null
                Visibility.Public -> v
            }
        }
    }

    /**
     * Freeze the environment, all its value will become immutable afterwards.
     */
    fun freeze(): Result<FrozenModule> {
        return freezeImpl(null)
    }

    /**
     * Freeze the environment and assign a name to the contained frozen heap.
     */
    fun freezeAndName(name: Any): Result<FrozenModule> {
        return freezeImpl(name)
    }

    private fun freezeImpl(name: Any?): Result<FrozenModule> {
        val start = TimeSource.Monotonic.markNow()
        val freezer = Freezer(frozenHeap)
        for (r in heap.referencedHeaps()) {
            frozenHeap.addReference(r)
        }
        val frozenSlots = slots.freeze(freezer).getOrElse { return Result.failure(it) }
        val extraValue = _extraValue?.let { v ->
            freezer.freeze(v).getOrElse { return Result.failure(it) }
        }
        val stacks = heapProfileOnFreeze?.let { mode ->
            val heapProfile = AggregateHeapProfileInfo.collect(heap, HeapKind.Frozen)
            RetainedHeapProfile(
                info = heapProfile,
                mode = mode,
            )
        }
        val rest = FrozenModuleData(
            names = names.freeze(),
            slots = frozenSlots,
            docstring = docstring,
            heapProfile = stacks,
        )
        val frozenModuleRef = FrozenRef(rest)
        for (frozenDef in freezer.frozenDefs) {
            frozenDef.value.postFreeze(frozenModuleRef, heap, freezer.heap)
        }

        return Result.success(
            FrozenModule(
                heap = frozenHeap.intoRefImpl(name),
                module = frozenModuleRef,
                _extraValue = extraValue,
                evalDuration = start.elapsedNow() + _evalDuration,
            )
        )
    }

    /**
     * Set the value of a variable in the environment.
     * Modifying these variables while executing is ongoing can have
     * surprising effects.
     */
    fun set(name: String, value: Value) {
        val slot = names.addName(frozenHeap.allocStrIntern(name))
        val slots = slots()
        slots.ensureSlot(slot)
        slots.setSlot(slot, value)
    }

    /**
     * Set the value of a variable in the environment. Set its visibility to
     * "private" to ensure that it is not re-exported.
     */
    internal fun setPrivate(name: FrozenStringValue, value: Value) {
        val slot = names.addNameVisibility(name, Visibility.Private)
        val slots = slots()
        slots.ensureSlot(slot)
        slots.setSlot(slot, value)
    }

    /**
     * Import symbols from a module, similar to what is done during `load()`.
     */
    fun importPublicSymbols(module: FrozenModule) {
        frozenHeap.addReference(module.frozenHeap())
        for ((k, value) in module.allItems()) {
            if (defaultVisibility(k.asStr()) == Visibility.Public) {
                setPrivate(k, Value.newFrozen(value))
            }
        }
    }

    internal fun loadSymbol(module: FrozenModule, symbol: String): Result<Value> {
        if (defaultVisibility(symbol) != Visibility.Public) {
            return Result.failure(
                EnvironmentError.CannotImportPrivateSymbol(symbol)
            )
        }
        val (value, vis) = module.getAnyVisibility(symbol).getOrElse {
            return Result.failure(it)
        }
        return when (vis) {
            Visibility.Public -> Result.success(heap().accessOwnedFrozenValue(value))
            Visibility.Private -> Result.failure(
                EnvironmentError.ModuleSymbolIsNotExported(symbol)
            )
        }
    }

    internal fun setDocstring(docstring: String) {
        this.docstring = docstring
    }

    internal fun addEvalDuration(duration: Duration) {
        _evalDuration += duration
    }

    internal fun trace(tracer: Tracer) {
        val slotsMut = slots().getSlotsMut()
        for (i in slotsMut.indices) {
            slotsMut[i]?.let { v ->
                val holder = ValueHolder(v)
                tracer.trace(holder)
                slotsMut[i] = holder.value
            }
        }

        _extraValue?.let { extra ->
            val holder = ValueHolder(extra)
            tracer.trace(holder)
            _extraValue = holder.value
        }

        heap().traceInterner(tracer)
    }

    /**
     * Field that can be used for any purpose you want.
     */
    fun setExtraValue(v: Value) {
        _extraValue = v
    }

    /**
     * Set extra value, but fail if it's already set.
     */
    fun setExtraValueNoOverwrite(v: Value): Result<Unit> {
        val existing = extraValue()
        if (existing != null) {
            return Result.failure(
                ModuleError.ExtraValueAlreadySet(existing.getType())
            )
        }
        setExtraValue(v)
        return Result.success(Unit)
    }

    /**
     * Field that can be used for any purpose you want.
     */
    fun extraValue(): Value? {
        return _extraValue
    }
}

fun testSendSync() {
    val v: FrozenModule? = null
}

fun testGenHeapSummaryProfile() {
    Module.withTempHeap { module ->
        val eval = Evaluator.new(module)
        eval.enableProfile(ProfileMode.HeapSummaryRetained)
        eval.evalModule(
            AstModule.parse(
                "x.star",
                """
def f(x):
    return list([x])

x = f(1)
""".trimIndent(),
                Dialect.AllOptionsInternal,
            ).getOrThrow(),
            Globals.standard(),
        ).getOrThrow()

        val frozen = module.freeze().getOrThrow()
        val heapSummary = frozen.heapProfile().getOrThrow().genCsv()
        check(heapSummary.contains("\"x.star.f\"")) { heapSummary }
        Result.success(Unit)
    }.getOrThrow()
}

fun testFrozenModuleFromGlobals() {
    val globals = GlobalsBuilder.new()
    someGlobals(globals)
    val globalsBuilt = globals.build()

    val module = FrozenModule.fromGlobals(globalsBuilt).getOrThrow()
    check("function" == module.get("foo").getOrThrow().value().getType())
    check(
        0 == ListRef.fromValue(module.get("BAR").getOrThrow().value())!!
            .len()
    )
}

fun someGlobals(globals: GlobalsBuilder) {
    globals.setFunction("foo") { _, _ -> foo() }
    val emptyVec = globals.frozenHeap().allocList(emptyList())
    globals.setInner("BAR", emptyVec, false)
}

fun foo(): Result<Int> {
    return Result.success(17)
}
