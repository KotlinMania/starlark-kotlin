// port-lint: source src/eval/runtime/visit_span.rs
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

import io.github.kotlinmania.starlark_kotlin.eval.compiler.CompareOp
import io.github.kotlinmania.starlark_kotlin.values.Tuple4
import io.github.kotlinmania.starlark_kotlin.values.FrozenRef
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.TypeCompiled
import io.github.kotlinmania.starlark_kotlin.environment.ModuleSlotId
import io.github.kotlinmania.starlark_kotlin.typing.DefParamIndices
import io.github.kotlinmania.starlark_kotlin.collections.symbol.Symbol
import io.github.kotlinmania.starlark_kotlin.eval.compiler.IrSpanned
import io.github.kotlinmania.starlark_kotlin.typing.DefRegularParamMode
import io.github.kotlinmania.starlark_kotlin.values.layout.FrozenValueTyped

/// Visitor for code spans in the IR.
// pub(crate) trait VisitSpanMut
internal interface VisitSpanMut {
    // fn visit_spans(&mut self, visitor: &mut impl FnMut(&mut FrameSpan))
    fun visitSpans(visitor: (FrameSpan) -> FrameSpan)
}

/// [VisitSpanMut] impl for [IrSpanned].
// impl<V: VisitSpanMut> VisitSpanMut for IrSpanned<V>
internal fun <V : VisitSpanMut> IrSpanned<V>.visitSpansMut(visitor: (FrameSpan) -> FrameSpan): IrSpanned<V> {
    val newSpan = visitor(span)
    return IrSpanned(newSpan, node).also { node.visitSpans(visitor) }
}

/// [VisitSpanMut] impl for [FrozenValue] — no spans.
// impl VisitSpanMut for FrozenValue
internal fun FrozenValue.visitSpansMut(visitor: (FrameSpan) -> FrameSpan) { }

/// [VisitSpanMut] impl for [TypeCompiled] — no spans.
// impl VisitSpanMut for TypeCompiled
internal fun TypeCompiled.visitSpansMut(visitor: (FrameSpan) -> FrameSpan) { }

/// [VisitSpanMut] impl for [String] — no spans.
// impl VisitSpanMut for String
internal fun String.visitSpansMut(visitor: (FrameSpan) -> FrameSpan) { }

/// [VisitSpanMut] impl for [Boolean] — no spans.
// impl VisitSpanMut for bool
internal fun Boolean.visitSpansMut(visitor: (FrameSpan) -> FrameSpan) { }

/// [VisitSpanMut] impl for [UInt] — no spans.
// impl VisitSpanMut for u32
internal fun UInt.visitSpansMut(visitor: (FrameSpan) -> FrameSpan) { }

/// [VisitSpanMut] impl for [ModuleSlotId] — no spans.
// impl VisitSpanMut for ModuleSlotId
internal fun ModuleSlotId.visitSpansMut(visitor: (FrameSpan) -> FrameSpan) { }

/// [VisitSpanMut] impl for [CompareOp] — no spans.
// impl VisitSpanMut for CompareOp
internal fun CompareOp.visitSpansMut(visitor: (FrameSpan) -> FrameSpan) { }

/// [VisitSpanMut] impl for boxed values — delegates to inner.
// impl<V: VisitSpanMut> VisitSpanMut for Box<V>
internal fun <V : VisitSpanMut> V.visitSpansMutBoxed(visitor: (FrameSpan) -> FrameSpan) {
    visitSpans(visitor)
}

/// [VisitSpanMut] impl for [FrozenValueTyped] — no spans.
// impl<T: StarlarkValue<'static>> VisitSpanMut for FrozenValueTyped<'static, T>
internal fun <T : StarlarkValue> FrozenValueTyped<T>.visitSpansMut(visitor: (FrameSpan) -> FrameSpan) { }

/// [VisitSpanMut] impl for [FrozenRef] — no spans.
// impl<T> VisitSpanMut for FrozenRef<'static, T>
internal fun <T> FrozenRef<T>.visitSpansMut(visitor: (FrameSpan) -> FrameSpan) { }

/// [VisitSpanMut] impl for [Symbol] — no spans.
// impl VisitSpanMut for Symbol
internal fun Symbol.visitSpansMut(visitor: (FrameSpan) -> FrameSpan) { }

/// [VisitSpanMut] impl for [Pair] (2-tuple).
// impl<A: VisitSpanMut, B: VisitSpanMut> VisitSpanMut for (A, B)
internal fun <A : VisitSpanMut, B : VisitSpanMut> Pair<A, B>.visitSpansMut(
    visitor: (FrameSpan) -> FrameSpan,
) {
    first.visitSpans(visitor)
    second.visitSpans(visitor)
}

/// [VisitSpanMut] impl for [Triple] (3-tuple).
// impl<A: VisitSpanMut, B: VisitSpanMut, C: VisitSpanMut> VisitSpanMut for (A, B, C)
internal fun <A : VisitSpanMut, B : VisitSpanMut, C : VisitSpanMut> Triple<A, B, C>.visitSpansMut(
    visitor: (FrameSpan) -> FrameSpan,
) {
    first.visitSpans(visitor)
    second.visitSpans(visitor)
    third.visitSpans(visitor)
}

/// [VisitSpanMut] impl for [Tuple4] (4-tuple).
// impl<A: VisitSpanMut, B: VisitSpanMut, C: VisitSpanMut, D: VisitSpanMut> VisitSpanMut for (A, B, C, D)
internal fun <A : VisitSpanMut, B : VisitSpanMut, C : VisitSpanMut, D : VisitSpanMut> Tuple4<A, B, C, D>.visitSpansMut(
    visitor: (FrameSpan) -> FrameSpan,
) {
    first.visitSpans(visitor)
    second.visitSpans(visitor)
    third.visitSpans(visitor)
    fourth.visitSpans(visitor)
}

/// [VisitSpanMut] impl for [List].
// impl<V: VisitSpanMut> VisitSpanMut for Vec<V>
internal fun <V : VisitSpanMut> MutableList<V>.visitSpansMut(visitor: (FrameSpan) -> FrameSpan) {
    for (v in this) {
        v.visitSpans(visitor)
    }
}

/// [VisitSpanMut] impl for nullable (Option).
// impl<V: VisitSpanMut> VisitSpanMut for Option<V>
internal fun <V : VisitSpanMut> V?.visitSpansMut(visitor: (FrameSpan) -> FrameSpan) {
    this?.visitSpans(visitor)
}

/// [VisitSpanMut] impl for [DefRegularParamMode] — no spans.
// impl VisitSpanMut for DefRegularParamMode
internal fun DefRegularParamMode.visitSpansMut(visitor: (FrameSpan) -> FrameSpan) { }

/// [VisitSpanMut] impl for [DefParamIndices] — no spans.
// impl VisitSpanMut for DefParamIndices
internal fun DefParamIndices.visitSpansMut(visitor: (FrameSpan) -> FrameSpan) { }
