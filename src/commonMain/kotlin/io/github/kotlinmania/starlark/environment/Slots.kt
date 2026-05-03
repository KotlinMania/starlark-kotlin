// port-lint: source src/environment/slots.rs
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

import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.Freezer
import io.github.kotlinmania.starlark.values.layout.Value

data class ModuleSlotId(val index: Int) {

    companion object {
        fun new(index: Int): ModuleSlotId = ModuleSlotId(index)
    }
}

/** Indexed slots of a module. May contain unassigned values as `None`. */
class MutableSlots {
    private val slots: MutableList<Value?> = mutableListOf()

    companion object {
        fun new(): MutableSlots = MutableSlots()
    }

    fun getSlotsMut(): MutableList<Value?> = slots

    fun getSlot(slot: ModuleSlotId): Value? {
        return slots[slot.index]
    }

    fun setSlot(slot: ModuleSlotId, value: Value) {
        slots[slot.index] = value
    }

    fun ensureSlot(slot: ModuleSlotId) {
        // To ensure that `slot` exists, we need at least `slot + 1` slots.
        ensureSlots(slot.index + 1)
    }

    fun ensureSlots(count: Int) {
        if (slots.size >= count) {
            return
        }
        val extra = count - slots.size
        for (i in 0 until extra) {
            slots.add(null)
        }
    }

    fun valuesBySlotId(): List<Pair<ModuleSlotId, Value>> {
        return slots.mapIndexedNotNull { i, v ->
            if (v != null) Pair(ModuleSlotId.new(i), v) else null
        }
    }

    fun freeze(freezer: Freezer): Result<FrozenSlots> {
        val frozenSlots = mutableListOf<FrozenValue?>()
        for (slot in slots) {
            if (slot == null) {
                frozenSlots.add(null)
            } else {
                val frozen = freezer.freeze(slot)
                if (frozen.isFailure) return Result.failure(frozen.exceptionOrNull()!!)
                frozenSlots.add(frozen.getOrThrow())
            }
        }
        return Result.success(FrozenSlots(frozenSlots))
    }
}

/** Indexed slots of a frozen module. May contain unassigned values as `null`. */
class FrozenSlots(
    private val slots: List<FrozenValue?>,
) {

    fun getSlot(slot: ModuleSlotId): FrozenValue? {
        return slots[slot.index]
    }
}
