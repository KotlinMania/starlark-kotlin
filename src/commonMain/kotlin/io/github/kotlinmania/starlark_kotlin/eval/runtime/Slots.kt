// port-lint: source src/eval/runtime/slots.rs
package io.github.kotlinmania.starlark_kotlin.eval.runtime

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

import io.github.kotlinmania.starlark_kotlin.eval.bc.BcSlot

/** Not captured. */
// #[derive(Clone, Copy, Dupe, Debug, PartialEq, Eq, Trace, Freeze, VisitSpanMut, Allocative)]
// pub(crate) struct LocalSlotId(pub(crate) u32)
internal data class LocalSlotId(val index: UInt) {
    /**
     * Each local slot is a valid BC slot.
     * When it is:
     * * known to be initialized
     * * or used for writing
     * * but not captured
     */
    // pub(crate) fn to_bc_slot(self) -> BcSlot
    fun toBcSlot(): BcSlot = BcSlot(index)

    // pub(crate) fn to_captured_or_not(self) -> LocalSlotIdCapturedOrNot
    fun toCapturedOrNot(): LocalSlotIdCapturedOrNot = LocalSlotIdCapturedOrNot(index)
}

/**
 * Captured local slot id.
 *
 * E.g. in code:
 *
 * ```python
 * def f():
 *   x = 1
 *   return lambda: x
 * ```
 *
 * `x` slots (in both `f` and `lambda`) are captured.
 */
// #[derive(Clone, Copy, Dupe, Debug, PartialEq, Eq, Trace, VisitSpanMut)]
// pub(crate) struct LocalCapturedSlotId(pub(crate) u32)
internal data class LocalCapturedSlotId(val index: UInt) {
    // pub(crate) fn to_bc_slot(self) -> BcSlot
    fun toBcSlot(): BcSlot = BcSlot(index)
}

/**
 * Local slot id, when we don't know if it is captured or not.
 *
 * This is used only during AST analysis.
 */
// #[derive(Clone, Copy, Dupe, Debug, PartialEq, Eq, Trace)]
// pub(crate) struct LocalSlotIdCapturedOrNot(pub(crate) u32)
data class LocalSlotIdCapturedOrNot(val index: UInt)
