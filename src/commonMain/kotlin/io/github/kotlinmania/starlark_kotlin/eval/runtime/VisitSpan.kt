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

/** Visitor for code spans in the IR. */
internal interface VisitSpanMut {
    fun visitSpans(visitor: (FrameSpan) -> FrameSpan)
}

/** VisitSpanMut for [IrSpanned] — visits the span and delegates to the node. */
internal fun <V : VisitSpanMut> IrSpanned<V>.visitSpansMut(visitor: (FrameSpan) -> FrameSpan): IrSpanned<V> {
    val newSpan = visitor(span)
    return IrSpanned(newSpan, node).also { node.visitSpans(visitor) }
}

/** VisitSpanMut for [FrozenValue] — no spans to visit. */
internal fun FrozenValue.visitSpansMut(visitor: (FrameSpan) -> FrameSpan) { }

/** VisitSpanMut for [TypeCompiled] — no spans to visit. */
internal fun TypeCompiled.visitSpansMut(visitor: (FrameSpan) -> FrameSpan) { }

/** VisitSpanMut for [String] — no spans to visit. */
internal fun String.visitSpansMut(visitor: (FrameSpan) -> FrameSpan) { }

/** VisitSpanMut for [Boolean] — no spans to visit. */
internal fun Boolean.visitSpansMut(visitor: (FrameSpan) -> FrameSpan) { }

/** VisitSpanMut for [UInt] (u32) — no spans to visit. */
internal fun UInt.visitSpansMut(visitor: (FrameSpan) -> FrameSpan) { }

/** VisitSpanMut for [ModuleSlotId] — no spans to visit. */
internal fun ModuleSlotId.visitSpansMut(visitor: (FrameSpan) -> FrameSpan) { }

/** VisitSpanMut for [CompareOp] — no spans to visit. */
internal fun CompareOp.visitSpansMut(visitor: (FrameSpan) -> FrameSpan) { }

/** VisitSpanMut for boxed values — delegates to inner. */
internal fun <V : VisitSpanMut> V.visitSpansMutBoxed(visitor: (FrameSpan) -> FrameSpan) {
    visitSpans(visitor)
}

/** VisitSpanMut for [FrozenValueTyped] — no spans to visit. */
internal fun <T : StarlarkValue> FrozenValueTyped<T>.visitSpansMut(visitor: (FrameSpan) -> FrameSpan) { }

/** VisitSpanMut for [FrozenRef] — no spans to visit. */
internal fun <T> FrozenRef<T>.visitSpansMut(visitor: (FrameSpan) -> FrameSpan) { }

/** VisitSpanMut for [Symbol] — no spans to visit. */
internal fun Symbol.visitSpansMut(visitor: (FrameSpan) -> FrameSpan) { }

/** VisitSpanMut for 2-tuple — visits both elements. */
internal fun <A : VisitSpanMut, B : VisitSpanMut> Pair<A, B>.visitSpansMut(
    visitor: (FrameSpan) -> FrameSpan,
) {
    first.visitSpans(visitor)
    second.visitSpans(visitor)
}

/** VisitSpanMut for 3-tuple — visits all elements. */
internal fun <A : VisitSpanMut, B : VisitSpanMut, C : VisitSpanMut> Triple<A, B, C>.visitSpansMut(
    visitor: (FrameSpan) -> FrameSpan,
) {
    first.visitSpans(visitor)
    second.visitSpans(visitor)
    third.visitSpans(visitor)
}

/** VisitSpanMut for 4-tuple — visits all elements. */
internal fun <A : VisitSpanMut, B : VisitSpanMut, C : VisitSpanMut, D : VisitSpanMut> Tuple4<A, B, C, D>.visitSpansMut(
    visitor: (FrameSpan) -> FrameSpan,
) {
    first.visitSpans(visitor)
    second.visitSpans(visitor)
    third.visitSpans(visitor)
    fourth.visitSpans(visitor)
}

/** VisitSpanMut for [MutableList] (Vec) — visits each element. */
internal fun <V : VisitSpanMut> MutableList<V>.visitSpansMut(visitor: (FrameSpan) -> FrameSpan) {
    for (v in this) {
        v.visitSpans(visitor)
    }
}

/** VisitSpanMut for nullable (Option) — visits if present. */
internal fun <V : VisitSpanMut> V?.visitSpansMut(visitor: (FrameSpan) -> FrameSpan) {
    this?.visitSpans(visitor)
}

/** VisitSpanMut for [DefRegularParamMode] — no spans to visit. */
internal fun DefRegularParamMode.visitSpansMut(visitor: (FrameSpan) -> FrameSpan) { }

/** VisitSpanMut for [DefParamIndices] — no spans to visit. */
internal fun DefParamIndices.visitSpansMut(visitor: (FrameSpan) -> FrameSpan) { }
