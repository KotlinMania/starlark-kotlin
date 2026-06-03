// port-lint: source src/eval/bc/call.rs
package io.github.kotlinmania.starlark.eval.bc

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

/** Call-related bytecode interpreter code. */

import io.github.kotlinmania.starlark.collections.symbol.Symbol
import io.github.kotlinmania.starlark.eval.compiler.FrozenDef
import io.github.kotlinmania.starlark.eval.runtime.ArgNames
import io.github.kotlinmania.starlark.eval.runtime.ArgSymbol
import io.github.kotlinmania.starlark.eval.runtime.ArgumentsFull
import io.github.kotlinmania.starlark.eval.runtime.ArgumentsImpl
import io.github.kotlinmania.starlark.eval.runtime.ArgumentsPos
import io.github.kotlinmania.starlark.eval.runtime.ResolvedArgName
import io.github.kotlinmania.starlark.values.layout.typed.FrozenStringValue

/** Call arguments. */
internal interface BcCallArgs<S : ArgSymbol> : BcInstrArg {
    fun popFromStack(frame: BcFramePtr): ArgumentsFull<S>
}

/** Call arguments for `def` call. */
internal interface BcCallArgsForDef : BcInstrArg {
    fun popFromStack(stack: BcFramePtr): ArgumentsImpl<ResolvedArgName>
}

/** Full call arguments: positional, named, star and star-star. All taken from the stack. */
internal class BcCallArgsFull<S : ArgSymbol>(
    val posNamed: BcSlotInRange,
    val names: List<Pair<S, FrozenStringValue>>,
    val args: BcSlotIn?,
    val kwargs: BcSlotIn?,
) {
    /** Number of positional arguments. */
    private fun pos(): UInt {
        check(posNamed.len() >= names.size.toUInt())
        return posNamed.len() - names.size.toUInt()
    }

    /** Display for BcCallArgsFull. */
    override fun toString(): String =
        buildString {
            append(posNamed)
            // Number of positional arguments.
            if (pos() != 0u) {
                append(" ${pos()}")
            }
            // Named arguments.
            for ((_, name) in names) {
                append(" ${name.asStr()}")
            }
            // Star argument?
            if (args != null) {
                append(" *$args")
            }
            // Star-star argument?
            if (kwargs != null) {
                append(" **$kwargs")
            }
        }
}

/** Positional-only call arguments, from stack. */
internal class BcCallArgsPos(
    /** Range of positional arguments. */
    val pos: BcSlotInRange,
)

/** Resolve symbol-based call args to resolved arg names for a specific def. */
internal fun BcCallArgsFull<Symbol>.resolve(def: FrozenDef): BcCallArgsFull<ResolvedArgName> =
    BcCallArgsFull(
        posNamed = posNamed,
        names =
            names.map { (name, value) ->
                Pair(def.resolveArgName(name.asStrHashed()), value)
            },
        args = args,
        kwargs = kwargs,
    )

/** Pop full call arguments from the stack frame. */
internal class BcCallArgsFullCallArgs<S : ArgSymbol>(
    private val full: BcCallArgsFull<S>,
) : BcCallArgs<S> {
    override fun popFromStack(frame: BcFramePtr): ArgumentsFull<S> {
        val posNamed = frame.getBcSlotRange(full.posNamed)
        val posCount = posNamed.size - full.names.size
        val pos = posNamed.subList(0, posCount)
        val named = posNamed.subList(posCount, posNamed.size)
        val argsVal = full.args?.let { frame.getBcSlot(it) }
        val kwargsVal = full.kwargs?.let { frame.getBcSlot(it) }
        return ArgumentsFull(
            pos = pos,
            named = named,
            names = ArgNames.newUnique(full.names.map { (s, fsv) -> Pair(s, fsv.toStringValue()) }),
            args = argsVal,
            kwargs = kwargsVal,
        )
    }

    override fun fmtAppend(
        ip: BcAddr,
        endArg: BcInstrEndArg?,
        f: StringBuilder,
    ) {
        f.append(" {$full}")
    }

    override fun visitJumpAddr(
        ip: BcAddr,
        consumer: (BcAddr) -> Unit,
    ) {
        // No jump addresses in call args.
    }
}

/** Pop positional-only call arguments from the stack frame. */
internal class BcCallArgsPosCallArgs<S : ArgSymbol>(
    private val posArgs: BcCallArgsPos,
) : BcCallArgs<S> {
    override fun popFromStack(frame: BcFramePtr): ArgumentsFull<S> {
        val pos = frame.getBcSlotRange(posArgs.pos)
        return ArgumentsFull(
            pos = pos,
            named = emptyList(),
            names = ArgNames.newUnique(emptyList()),
            args = null,
            kwargs = null,
        )
    }

    override fun fmtAppend(
        @Suppress("UNUSED_PARAMETER") ip: BcAddr,
        @Suppress("UNUSED_PARAMETER") endArg: BcInstrEndArg?,
        f: StringBuilder,
    ) {
        f.append(" ${posArgs.pos}")
    }

    override fun visitJumpAddr(
        @Suppress("UNUSED_PARAMETER") ip: BcAddr,
        @Suppress("UNUSED_PARAMETER") consumer: (BcAddr) -> Unit,
    ) {
        // No jump addresses in call args.
    }
}

/** Full call arguments for def calls, popping from the stack frame. */
internal class BcCallArgsFullForDef(
    private val full: BcCallArgsFull<ResolvedArgName>,
) : BcCallArgsForDef {
    override fun popFromStack(stack: BcFramePtr): ArgumentsFull<ResolvedArgName> {
        val posNamed = stack.getBcSlotRange(full.posNamed)
        val posCount = posNamed.size - full.names.size
        val pos = posNamed.subList(0, posCount)
        val named = posNamed.subList(posCount, posNamed.size)
        val argsVal = full.args?.let { stack.getBcSlot(it) }
        val kwargsVal = full.kwargs?.let { stack.getBcSlot(it) }
        return ArgumentsFull(
            pos = pos,
            named = named,
            names = ArgNames.newUnique(full.names.map { (s, fsv) -> Pair(s, fsv.toStringValue()) }),
            args = argsVal,
            kwargs = kwargsVal,
        )
    }

    override fun fmtAppend(
        @Suppress("UNUSED_PARAMETER") ip: BcAddr,
        @Suppress("UNUSED_PARAMETER") endArg: BcInstrEndArg?,
        f: StringBuilder,
    ) {
        f.append(" {$full}")
    }

    override fun visitJumpAddr(
        @Suppress("UNUSED_PARAMETER") ip: BcAddr,
        @Suppress("UNUSED_PARAMETER") consumer: (BcAddr) -> Unit,
    ) {
        // No jump addresses in call args.
    }
}

/** Positional-only call arguments for def calls, popping from the stack frame. */
internal class BcCallArgsPosForDef(
    private val posArgs: BcCallArgsPos,
) : BcCallArgsForDef {
    override fun popFromStack(stack: BcFramePtr): ArgumentsPos<ResolvedArgName> {
        val pos = stack.getBcSlotRange(posArgs.pos)
        return ArgumentsPos(pos = pos)
    }

    override fun fmtAppend(
        @Suppress("UNUSED_PARAMETER") ip: BcAddr,
        @Suppress("UNUSED_PARAMETER") endArg: BcInstrEndArg?,
        f: StringBuilder,
    ) {
        f.append(" ${posArgs.pos}")
    }

    override fun visitJumpAddr(
        @Suppress("UNUSED_PARAMETER") ip: BcAddr,
        @Suppress("UNUSED_PARAMETER") consumer: (BcAddr) -> Unit,
    ) {
        // No jump addresses in call args.
    }
}
