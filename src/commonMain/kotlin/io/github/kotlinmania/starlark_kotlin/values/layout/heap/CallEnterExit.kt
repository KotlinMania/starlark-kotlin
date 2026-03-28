// port-lint: source src/values/layout/heap/call_enter_exit.rs
package io.github.kotlinmania.starlark_kotlin.values.layout.heap

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

//! Marker objects to track allocations.

import io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.instant.ProfilerInstant
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.layout.Value

/// A type which is either drop or non-drop.
// pub(crate) trait MaybeDrop: Debug + Sync + Send + Allocative + 'static {}
internal interface MaybeDrop

/// Type which has `Drop`.
// #[derive(ProvidesStaticType, Debug, Trace, Allocative)]
// pub(crate) struct NeedsDrop;
internal class NeedsDrop : MaybeDrop, AutoCloseable {
    // impl Drop for NeedsDrop
    override fun close() {
        // Just make this type `Drop`.
    }
}

/// Type which doesn't have `Drop`.
// #[derive(ProvidesStaticType, Debug, Trace, Allocative)]
// pub(crate) struct NoDrop;
internal class NoDrop : MaybeDrop

// #[derive(Trace, Debug, Display, ProvidesStaticType, NoSerialize, Allocative)]
// #[display("CallEnter")]
// pub(crate) struct CallEnter<'v, D: MaybeDrop + 'static>
internal class CallEnter<D : MaybeDrop>(
    val function: Value,
    val time: ProfilerInstant,
    val maybeDrop: D,
) : StarlarkValue {
    // #[starlark_value(type = "call_enter")]
    override val TYPE: String get() = "call_enter"

    override fun toString(): String = "CallEnter"
}

// #[derive(Debug, Display, ProvidesStaticType, NoSerialize, Allocative)]
// #[display("CallExit")]
// pub(crate) struct CallExit<D: MaybeDrop + 'static>
internal class CallExit<D : MaybeDrop>(
    val time: ProfilerInstant,
    val maybeDrop: D,
) : StarlarkValue {
    // #[starlark_value(type = "call_exit")]
    override val TYPE: String get() = "call_exit"

    override fun toString(): String = "CallExit"
}
