// port-lint: source src/environment/names.rs
package io.github.kotlinmania.starlark.environment

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

import starlarkmap.Hashed
import starlarkmap.smallmap.SmallMap
import io.github.kotlinmania.starlark.values.layout.typed.FrozenStringValue
import io.github.kotlinmania.starlark.syntax.ast.Visibility

/**
 * MutableNames are how we allocate slots (index-based) to variables
 * (name-based). The slots field is the current active mapping of names to
 * index.
 *
 * In a statement context there are things that define variables, e.g. x=...,
 * for x in ... Importantly, the expression x can refer to either a global x,
 * or a local x that hasn't yet been defined, based on the future
 * presence/absence of a statement defining x. Therefore, we first capture all
 * the definitions with collectDefinesLvalue, allocate them slots,
 * then replace variables with slot numbers when compiling.
 *
 * Comprehensions are a bit different. Given [x for x in y] that defines x, but
 * in a way that shadows any existing x, and the definition immediately binds
 * x. We do that with addScoped()/unscope(). On an addScope, we allocate
 * fresh slots at the end, and bind them to the names in the comprehension.
 * On an unscope, we do the reverse, putting things back to how they were
 * before (apart from the total) number of slots required.
 */
class MutableNames {
    // RefCell<SmallMap<...>> → mutable SmallMap field
    private val map: SmallMap<FrozenStringValue, Pair<ModuleSlotId, Visibility>> = SmallMap.new()

    companion object {
        fun new(): MutableNames = MutableNames()
    }

    fun slotCount(): Int = map.len()

    /**
     * Try and go back from a slot to a name.
     * Inefficient - only import in error paths.
     */
    fun getSlot(slot: ModuleSlotId): FrozenStringValue? {
        for ((name, pair) in map.iter()) {
            val (id, _) = pair
            if (id == slot) {
                return name
            }
        }
        return null
    }

    fun getName(name: Hashed<String>): Pair<ModuleSlotId, Visibility>? {
        for ((k, v) in map.iter()) {
            if (k.asStr() == name.key()) return v
        }
        return null
    }

    /** Add a name with explicit visibility to the module. */
    fun addNameVisibility(name: FrozenStringValue, vis: Visibility): ModuleSlotId {
        val existing = map.getHashedByValue(name.getHashed())
        if (existing != null) {
            val (slot, storedVis) = existing
            // Public visibility wins.
            if (storedVis == Visibility.Private) {
                map.insertHashed(name.getHashed(), Pair(slot, vis))
            }
            return slot
        } else {
            val slot = ModuleSlotId.new(map.len())
            map.insertHashed(name.getHashed(), Pair(slot, vis))
            return slot
        }
    }

    /** Add an exported name, or if it's already there, return the existing name. */
    fun addName(name: FrozenStringValue): ModuleSlotId {
        return addNameVisibility(name, Visibility.Public)
    }

    fun hideName(name: String) {
        val index = map.entries.indexOfFirst { it.key.key().asStr() == name }
        if (index >= 0) {
            map.entries.removeAt(index)
        }
    }

    fun allNamesAndSlots(): List<Pair<FrozenStringValue, ModuleSlotId>> {
        return map.iter().map { (name, pair) -> Pair(name, pair.first) }.toList()
    }

    fun allNamesAndVisibilities(): List<Pair<FrozenStringValue, Visibility>> {
        return map.iter().map { (name, pair) -> Pair(name, pair.second) }.toList()
    }

    fun allNamesSlotsAndVisibilities(): List<Triple<FrozenStringValue, ModuleSlotId, Visibility>> {
        return map.iter().map { (name, pair) -> Triple(name, pair.first, pair.second) }.toList()
    }

    fun freeze(): FrozenNames {
        return FrozenNames(map)
    }
}

/** Frozen (immutable) form of [MutableNames]. */
class FrozenNames(
    private val map: SmallMap<FrozenStringValue, Pair<ModuleSlotId, Visibility>>,
) {

    fun getName(name: String): Pair<ModuleSlotId, Visibility>? {
        for ((k, v) in map.iter()) {
            if (k.asStr() == name) return v
        }
        return null
    }

    /** Symbols including private. */
    fun allSymbols(): Sequence<Pair<FrozenStringValue, ModuleSlotId>> {
        return map.iter().asSequence().map { (name, pair) -> Pair(name, pair.first) }
    }

    /** Exported symbols. */
    fun symbols(): Sequence<Pair<FrozenStringValue, ModuleSlotId>> {
        return map.iter().asSequence().mapNotNull { (name, pair) ->
            val (slot, vis) = pair
            when (vis) {
                Visibility.Private -> null
                Visibility.Public -> Pair(name, slot)
            }
        }
    }
}
