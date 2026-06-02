// port-lint: source src/environment/modules.rs
package io.github.kotlinmania.starlark.environment

/*
 * Copyright 2018 The Starlark in Rust Authors.
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
 * The environment, called "Module" in [the spec](
 * https://github.com/bazelbuild/starlark/blob/master/spec.md)
 * is the list of variable in the current scope. It can be frozen, after which
 * all values from this environment become immutable.
 */

import io.github.kotlinmania.starlark.EnvironmentError
import io.github.kotlinmania.starlark.collections.Hashed
import io.github.kotlinmania.starlark.collections.SmallMap
import io.github.kotlinmania.starlark.docs.DocItem
import io.github.kotlinmania.starlark.docs.DocModule
import io.github.kotlinmania.starlark.docs.DocString
import io.github.kotlinmania.starlark.docs.DocStringKind
import io.github.kotlinmania.starlark.docs.fromDocstring
import io.github.kotlinmania.starlark.errors.didYouMean
import io.github.kotlinmania.starlark.eval.runtime.profile.data.ProfileData
import io.github.kotlinmania.starlark.eval.runtime.profile.heap.RetainedHeapProfileMode
import io.github.kotlinmania.starlark.syntax.ast.Visibility
import io.github.kotlinmania.starlark.values.FrozenRef
import io.github.kotlinmania.starlark.values.layout.Freezer
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeapName
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeapRef
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.layout.heap.HeapKind
import io.github.kotlinmania.starlark.values.layout.heap.Tracer
import io.github.kotlinmania.starlark.values.layout.heap.ValueHolder
import io.github.kotlinmania.starlark.values.layout.heap.profile.AggregateHeapProfileInfo
import io.github.kotlinmania.starlark.values.layout.heap.profile.RetainedHeapProfile
import io.github.kotlinmania.starlark.values.layout.typed.FrozenStringValue
import io.github.kotlinmania.starlark.values.owned.OwnedFrozenValue
import kotlin.time.Duration
import kotlin.time.TimeSource

/**
 * #[derive(Debug, thiserror::Error)]
 * enum ModuleError
 */
sealed class ModuleError(
    message: String,
) : Exception(message) {
    /** #[error("Retained memory profiling is not enabled")] */
    class RetainedMemoryProfileNotEnabled : ModuleError("Retained memory profiling is not enabled")

    /** #[error("Extra value already set to a value of type `{}`", .0)] */
    class ExtraValueAlreadySet(
        val typeName: String,
    ) : ModuleError("Extra value already set to a value of type `$typeName`")
}

/**
 * The result of freezing a [Module], making it and its contained values immutable.
 *
 * The values of this [FrozenModule] are stored on a frozen heap, a reference to which
 * can be obtained using [frozenHeap]. Be careful not to use
 * these values after the [FrozenModule] has been released unless you obtain a reference
 * to the frozen heap.
 *
 * pub struct FrozenModule
 */
class FrozenModule internal constructor(
    private val heap: FrozenHeapRef,
    private val module: FrozenRef<FrozenModuleData>,
    private val _extraValue: FrozenValue?,
    /** Module evaluation duration. */
    internal val evalDuration: Duration,
) : AutoCloseable {

    override fun close() {
        heap.close()
    }
    /**
     * Convert items in [Globals] into a [FrozenModule].
     * This function can be used to implement starlark module
     * using the `#[starlark_module]` attribute.
     *
     * pub fn from_globals(globals: &Globals) -> Result<FrozenModule>
     */
    companion object {
        fun fromGlobals(globals: Globals): Result<FrozenModule> =
            Module.withTempHeap { module ->
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

    /** fn get_any_visibility_option(&self, name: &str) -> Option<(OwnedFrozenValue, Visibility)> */
    private fun getAnyVisibilityOption(name: String): Pair<OwnedFrozenValue, Visibility>? {
        val (slot, vis) = module.value.names.getName(name) ?: return null
        val value = module.value.slots.getSlot(slot) ?: return null
        return OwnedFrozenValue(heap.clone(), value) to vis
    }

    /**
     * Get value, exported or private by name.
     *
     * pub fn get_any_visibility(&self, name: &str) -> anyhow::Result<(OwnedFrozenValue, Visibility)>
     */
    fun getAnyVisibility(name: String): Result<Pair<OwnedFrozenValue, Visibility>> =
        getAnyVisibilityOption(name)?.let { Result.success(it) }
            ?: run {
                val better = didYouMean(name, names().map { it.asStr() }.toList())
                if (better != null) {
                    Result.failure(
                        EnvironmentError.ModuleHasNoSymbolDidYouMean(name, better),
                    )
                } else {
                    Result.failure(
                        EnvironmentError.ModuleHasNoSymbol(name),
                    )
                }
            }

    /**
     * Get the value of the exported variable [name].
     *
     * Returns `null` if symbol is not found, error if symbol is private.
     *
     * pub fn get_option(&self, name: &str) -> anyhow::Result<Option<OwnedFrozenValue>>
     */
    fun getOption(name: String): Result<OwnedFrozenValue?> =
        when (val entry = getAnyVisibilityOption(name)) {
            null -> Result.success(null)
            else ->
                when (entry.second) {
                    Visibility.Private ->
                        Result.failure(
                            EnvironmentError.ModuleSymbolIsNotExported(name),
                        )
                    Visibility.Public -> Result.success(entry.first)
                }
        }

    /**
     * Get the value of the exported variable [name].
     * Returns an error if the variable isn't defined in the module or it is private.
     *
     * pub fn get(&self, name: &str) -> anyhow::Result<OwnedFrozenValue>
     */
    fun get(name: String): Result<OwnedFrozenValue> =
        getAnyVisibility(name).mapCatching { (value, vis) ->
            when (vis) {
                Visibility.Private -> throw EnvironmentError.ModuleSymbolIsNotExported(name)
                Visibility.Public -> value
            }
        }

    /**
     * Iterate through all the names defined in this module.
     * Only includes symbols that are publicly exposed.
     *
     * pub fn names(&self) -> impl Iterator<Item = FrozenStringValue>
     */
    fun names(): Sequence<FrozenStringValue> = module.value.names()

    /**
     * Obtain the [FrozenHeapRef] which owns the storage of all values defined in this module.
     *
     * pub fn frozen_heap(&self) -> &FrozenHeapRef
     */
    fun frozenHeap(): FrozenHeapRef = heap

    /**
     * Print out some approximation of the module definitions.
     *
     * pub fn describe(&self) -> String
     */
    fun describe(): String = module.value.describe()

    /** pub(crate) fn all_items(&self) -> impl Iterator<Item = (FrozenStringValue, FrozenValue)> */
    internal fun allItems(): Sequence<Pair<FrozenStringValue, FrozenValue>> = module.value.allItems()

    /**
     * The documentation for the module, and all of its top level values.
     *
     * pub fn documentation(&self) -> DocModule
     */
    fun documentation(): DocModule {
        val members = SmallMap.new<String, DocItem>()
        for ((name, value) in allItems()) {
            val vis = getAnyVisibilityOption(name.asStr())
            if (vis != null) {
                try {
                    if (vis.second == Visibility.Public) {
                        members.insert(name.asStr(), value.toValue().documentation())
                    }
                } finally {
                    vis.first.close()
                }
            }
        }

        return DocModule(
            docs = module.value.documentation(),
            members = members,
        )
    }

    /**
     * Retained memory info, or error if not enabled.
     *
     * pub fn heap_profile(&self) -> anyhow::Result<ProfileData>
     */
    fun heapProfile(): Result<ProfileData> =
        when (val p = module.value.heapProfile) {
            null -> Result.failure(ModuleError.RetainedMemoryProfileNotEnabled())
            else -> Result.success(p.toProfile())
        }

    /**
     * `extra_value` field from `Module`, frozen.
     *
     * pub fn extra_value(&self) -> Option<FrozenValue>
     */
    fun extraValue(): FrozenValue? = _extraValue

    /**
     * `extra_value` field from `Module`, frozen.
     *
     * pub fn owned_extra_value(&self) -> Option<OwnedFrozenValue>
     */
    fun ownedExtraValue(): OwnedFrozenValue? = _extraValue?.let { OwnedFrozenValue(heap.clone(), it) }
}

/** pub(crate) struct FrozenModuleData */
internal class FrozenModuleData(
    val names: FrozenNames,
    val slots: FrozenSlots,
    private val docstring: String?,
    /** When heap profile enabled, this field stores retained memory info. */
    val heapProfile: RetainedHeapProfile?,
) {
    /** fn names(&self) -> impl Iterator<Item = FrozenStringValue> */
    fun names(): Sequence<FrozenStringValue> = names.symbols().map { it.first }

    /** fn describe(&self) -> String */
    fun describe(): String =
        items()
            .map { (name, value) -> value.toValue().describe(name.asStr()) }
            .joinToString("\n")

    /** fn items(&self) -> impl Iterator<Item = (FrozenStringValue, FrozenValue)> */
    private fun items(): Sequence<Pair<FrozenStringValue, FrozenValue>> =
        names
            .symbols()
            .mapNotNull { (name, slot) ->
                slots.getSlot(slot)?.let { name to it }
            }

    /** fn all_items(&self) -> impl Iterator<Item = (FrozenStringValue, FrozenValue)> */
    fun allItems(): Sequence<Pair<FrozenStringValue, FrozenValue>> =
        names
            .allSymbols()
            .mapNotNull { (name, slot) ->
                slots.getSlot(slot)?.let { name to it }
            }

    /** pub(crate) fn get_slot(&self, slot: ModuleSlotId) -> Option<FrozenValue> */
    internal fun getSlot(slot: ModuleSlotId): FrozenValue? = slots.getSlot(slot)

    /**
     * Try and go back from a slot to a name.
     * Inefficient - only use in error paths.
     *
     * pub(crate) fn get_slot_name(&self, slot: ModuleSlotId) -> Option<FrozenStringValue>
     */
    internal fun getSlotName(slot: ModuleSlotId): FrozenStringValue? {
        for ((s, i) in names.symbols()) {
            if (i == slot) {
                return s
            }
        }
        return null
    }

    /** fn documentation(&self) -> Option<DocString> */
    fun documentation(): DocString? = docstring?.let { DocString.fromDocstring(DocStringKind.Starlark, it) }
}

/**
 * A container for user values, used during execution.
 *
 * A module contains both a [FrozenHeap] and [Heap] on which different values are allocated.
 * You can get references to these heaps with [frozenHeap] and [heap].
 * Be careful not to use these values after the [Module] has been
 * released unless you obtain a reference to the frozen heap.
 *
 * pub struct Module<'v>
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
         *
         * pub fn with_temp_heap<R, F>(f: F) -> R
         */
        fun <R> withTempHeap(f: (Module) -> R): R =
            Heap.temp { h ->
                h.allowGc()
                f(withHeap(h))
            }

        /**
         * Like [withTempHeap], but async (suspend).
         *
         * pub async fn with_temp_heap_async<R, F>(f: F) -> R
         */
        suspend fun <R> withTempHeapAsync(f: suspend (Module) -> R): R =
            Heap.temp { h ->
                h.allowGc()
                // Note: suspend lambdas require kotlinx.coroutines runBlocking in some contexts
                @Suppress("UNCHECKED_CAST")
                (f as (Module) -> R)(withHeap(h))
            }

        /**
         * Create a new module environment with no contents that will use the provided heap.
         *
         * pub(crate) fn with_heap(heap: Heap<'v>) -> Self
         */
        internal fun withHeap(heap: Heap): Module = Module(heap = heap)

        /**
         * Symbols starting with underscore are considered private.
         *
         * pub(crate) fn default_visibility(symbol: &str) -> Visibility
         */
        internal fun defaultVisibility(symbol: String): Visibility = if (symbol.startsWith('_')) Visibility.Private else Visibility.Public
    }

    /** pub(crate) fn enable_retained_heap_profile(&self, mode: RetainedHeapProfileMode) */
    internal fun enableRetainedHeapProfile(mode: RetainedHeapProfileMode) {
        heapProfileOnFreeze = mode
    }

    /**
     * Get the heap on which values are allocated by this module.
     *
     * pub fn heap(&self) -> Heap<'v>
     */
    fun heap(): Heap = heap

    /**
     * Get the frozen heap on which frozen values are allocated by this module.
     *
     * pub fn frozen_heap(&self) -> &FrozenHeap
     */
    fun frozenHeap(): FrozenHeap = frozenHeap

    /**
     * Iterate through all the names defined in this module.
     * Only includes symbols that are publicly exposed.
     *
     * pub fn names(&self) -> impl Iterator<Item = FrozenStringValue>
     */
    fun names(): Sequence<FrozenStringValue> =
        names
            .allNamesAndVisibilities()
            .asSequence()
            .filter { (_, vis) -> vis == Visibility.Public }
            .map { (name, _) -> name }

    /** pub(crate) fn values_by_slot_id(&self) -> Vec<(ModuleSlotId, Value<'v>)> */
    internal fun valuesBySlotId(): List<Pair<ModuleSlotId, Value>> = slots().valuesBySlotId()

    /**
     * Iterate through all the names defined in this module, including those that are private.
     *
     * pub fn names_and_visibilities(&self) -> impl Iterator<Item = (FrozenStringValue, Visibility)>
     */
    fun namesAndVisibilities(): List<Pair<FrozenStringValue, Visibility>> = names.allNamesAndVisibilities()

    /** pub(crate) fn mutable_names(&self) -> &MutableNames */
    internal fun mutableNames(): MutableNames = names

    /** pub(crate) fn slots(&self) -> &MutableSlots<'v> */
    internal fun slots(): MutableSlots = slots

    /**
     * Get value, exported or private by name.
     *
     * pub(crate) fn get_any_visibility(&self, name: Hashed<&str>) -> Option<(Value<'v>, Visibility)>
     */
    internal fun getAnyVisibility(name: Hashed<String>): Pair<Value, Visibility>? {
        val (slot, vis) = names.getName(name) ?: return null
        val value = slots().getSlot(slot) ?: return null
        return value to vis
    }

    /**
     * Get the value of the exported variable [name].
     * Returns null if the variable isn't defined in the module or it is private.
     *
     * pub fn get(&self, name: &str) -> Option<Value<'v>>
     */
    fun get(name: String): Value? =
        getAnyVisibility(Hashed.new(name))?.let { (v, vis) ->
            when (vis) {
                Visibility.Private -> null
                Visibility.Public -> v
            }
        }

    /**
     * Freeze the environment, all its value will become immutable afterwards.
     *
     * pub fn freeze(self) -> Result<FrozenModule>
     */
    fun freeze(): Result<FrozenModule> = freezeImpl(null)

    /**
     * Freeze the environment and assign a name to the contained frozen heap.
     *
     * pub fn freeze_and_name(self, name: FrozenHeapName) -> Result<FrozenModule>
     */
    fun freezeAndName(name: FrozenHeapName): Result<FrozenModule> = freezeImpl(name)

    /** fn freeze_impl(self, name: Option<FrozenHeapName>) -> Result<FrozenModule> */
    private fun freezeImpl(name: FrozenHeapName?): Result<FrozenModule> {
        val start = TimeSource.Monotonic.markNow()
        val freezer = Freezer(frozenHeap)
        for (r in heap.referencedHeaps()) {
            frozenHeap.addReference(r)
        }
        val frozenSlots = slots.freeze(freezer).getOrElse { return Result.failure(it) }
        val extraValue =
            _extraValue?.let { v ->
                freezer.freeze(v).getOrElse { return Result.failure(it) }
            }
        val stacks =
            heapProfileOnFreeze?.let { mode ->
                val heapProfile = AggregateHeapProfileInfo.collect(heap, HeapKind.Frozen)
                RetainedHeapProfile(
                    info = heapProfile,
                    mode = mode,
                )
            }
        val rest =
            FrozenModuleData(
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
            ),
        )
    }

    /**
     * Set the value of a variable in the environment.
     * Modifying these variables while executing is ongoing can have
     * surprising effects.
     *
     * pub fn set(&self, name: &str, value: Value<'v>)
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
     *
     * pub(crate) fn set_private(&self, name: FrozenStringValue, value: Value<'v>)
     */
    internal fun setPrivate(name: FrozenStringValue, value: Value) {
        val slot = names.addNameVisibility(name, Visibility.Private)
        val slots = slots()
        slots.ensureSlot(slot)
        slots.setSlot(slot, value)
    }

    /**
     * Import symbols from a module, similar to what is done during `load()`.
     *
     * pub fn import_public_symbols(&self, module: &FrozenModule)
     */
    fun importPublicSymbols(module: FrozenModule) {
        frozenHeap.addReference(module.frozenHeap())
        for (k in module.names()) {
            if (defaultVisibility(k.asStr()) == Visibility.Public) {
                val value = module.get(k.asStr()).getOrThrow().uncheckedFrozenValue()
                setPrivate(k, Value.newFrozen(value))
            }
        }
    }

    /** pub(crate) fn load_symbol(&self, module: &FrozenModule, symbol: &str) -> crate::Result<Value<'v>> */
    internal fun loadSymbol(module: FrozenModule, symbol: String): Result<Value> {
        if (defaultVisibility(symbol) != Visibility.Public) {
            return Result.failure(
                EnvironmentError.CannotImportPrivateSymbol(symbol),
            )
        }
        val (value, vis) =
            module.getAnyVisibility(symbol).getOrElse {
                return Result.failure(it)
            }
        return try {
            when (vis) {
                Visibility.Public -> Result.success(heap().accessOwnedFrozenValue(value))
                Visibility.Private ->
                    Result.failure(
                        EnvironmentError.ModuleSymbolIsNotExported(symbol),
                    )
            }
        } finally {
            value.close()
        }
    }

    /** pub(crate) fn set_docstring(&self, docstring: String) */
    internal fun setDocstring(docstring: String) {
        this.docstring = docstring
    }

    /** pub(crate) fn add_eval_duration(&self, duration: Duration) */
    internal fun addEvalDuration(duration: Duration) {
        _evalDuration += duration
    }

    /** pub(crate) fn trace(&self, tracer: &Tracer<'v>) */
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
     *
     * pub fn set_extra_value(&self, v: Value<'v>)
     */
    fun setExtraValue(v: Value) {
        _extraValue = v
    }

    /**
     * Set extra value, but fail if it's already set.
     *
     * pub fn set_extra_value_no_overwrite(&self, v: Value<'v>) -> anyhow::Result<()>
     */
    fun setExtraValueNoOverwrite(v: Value): Result<Unit> {
        val existing = extraValue()
        if (existing != null) {
            return Result.failure(
                ModuleError.ExtraValueAlreadySet(existing.getType()),
            )
        }
        setExtraValue(v)
        return Result.success(Unit)
    }

    /**
     * Field that can be used for any purpose you want.
     *
     * pub fn extra_value(&self) -> Option<Value<'v>>
     */
    fun extraValue(): Value? = _extraValue
}
