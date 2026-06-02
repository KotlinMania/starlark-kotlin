// port-lint: source src/environment/slots.rs
package io.github.kotlinmania.starlark.environment

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

import io.github.kotlinmania.starlark.values.freezeList
import io.github.kotlinmania.starlark.values.freezeNullable
import io.github.kotlinmania.starlark.values.layout.Freezer
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.Value

// #[derive(Clone, Copy, Dupe, Debug, PartialEq, Eq, Allocative, Hash)]
// pub(crate) struct ModuleSlotId(pub(crate) u32);
data class ModuleSlotId(
    val index: Int,
) {
    // impl ModuleSlotId

    companion object {
        // pub fn new(index: u32) -> Self
        fun new(index: Int): ModuleSlotId = ModuleSlotId(index)
    }
}

/** Indexed slots of a module. May contain unassigned values as `None`. */
// #[derive(Debug)]
// pub(crate) struct MutableSlots<'v>(RefCell<Vec<Option<Value<'v>>>>);
class MutableSlots {
    // RefCell<Vec<Option<Value>>> → mutable list
    private val slots: MutableList<Value?> = mutableListOf()

    // impl MutableSlots

    companion object {
        // pub fn new() -> Self
        fun new(): MutableSlots = MutableSlots()
    }

    // pub(crate) fn get_slots_mut(&self) -> RefMut<'_, Vec<Option<Value<'v>>>>
    fun getSlotsMut(): MutableList<Value?> = slots

    // pub fn get_slot(&self, slot: ModuleSlotId) -> Option<Value<'v>>
    fun getSlot(slot: ModuleSlotId): Value? = slots[slot.index]

    // pub fn set_slot(&self, slot: ModuleSlotId, value: Value<'v>)
    fun setSlot(slot: ModuleSlotId, value: Value) {
        slots[slot.index] = value
    }

    // pub fn ensure_slot(&self, slot: ModuleSlotId)
    fun ensureSlot(slot: ModuleSlotId) {
        // To ensure that `slot` exists, we need at least `slot + 1` slots.
        ensureSlots(slot.index + 1)
    }

    // pub fn ensure_slots(&self, count: u32)
    fun ensureSlots(count: Int) {
        if (slots.size >= count) {
            return
        }
        val extra = count - slots.size
        for (i in 0 until extra) {
            slots.add(null)
        }
    }

    // pub(crate) fn values_by_slot_id(&self) -> Vec<(ModuleSlotId, Value<'v>)>
    fun valuesBySlotId(): List<Pair<ModuleSlotId, Value>> =
        slots.mapIndexedNotNull { i, v ->
            if (v != null) Pair(ModuleSlotId.new(i), v) else null
        }

    // pub(crate) fn freeze(self, freezer: &Freezer) -> Result<FrozenSlots>
    fun freeze(freezer: Freezer): Result<FrozenSlots> {
        val frozenSlots =
            freezeList(slots, freezer) { slot, f ->
                freezeNullable(slot, f) { v, ff -> ff.freeze(v) }
            }
        if (frozenSlots.isFailure) return Result.failure(frozenSlots.exceptionOrNull()!!)
        return Result.success(FrozenSlots(frozenSlots.getOrThrow()))
    }
}

/** Indexed slots of a frozen module. May contain unassigned values as `null`. */
// #[derive(Debug, Allocative)]
// pub(crate) struct FrozenSlots(Vec<Option<FrozenValue>>);
class FrozenSlots(
    private val slots: List<FrozenValue?>,
) {
    // impl FrozenSlots

    // pub fn get_slot(&self, slot: ModuleSlotId) -> Option<FrozenValue>
    fun getSlot(slot: ModuleSlotId): FrozenValue? = slots[slot.index]
}
