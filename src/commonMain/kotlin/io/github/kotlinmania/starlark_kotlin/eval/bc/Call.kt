// port-lint: source src/eval/bc/call.rs
package io.github.kotlinmania.starlark_kotlin.eval.bc

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

/// Call-related bytecode interpreter code.

import io.github.kotlinmania.starlark_kotlin.collections.symbol.Symbol
import io.github.kotlinmania.starlark_kotlin.eval.bc.frame.BcFramePtr
import io.github.kotlinmania.starlark_kotlin.eval.bc.stack_ptr.BcSlotIn
import io.github.kotlinmania.starlark_kotlin.eval.bc.stack_ptr.BcSlotInRange
import io.github.kotlinmania.starlark_kotlin.eval.compiler.def.FrozenDef
import io.github.kotlinmania.starlark_kotlin.values.layout.typed.FrozenStringValue
import io.github.kotlinmania.starlark_kotlin.stdlib.ArgumentsFull
import io.github.kotlinmania.starlark_kotlin.stdlib.ArgNames
import io.github.kotlinmania.starlark_kotlin.eval.runtime.ArgumentsPos
import io.github.kotlinmania.starlark_kotlin.eval.runtime.ArgumentsImpl
import io.github.kotlinmania.starlark_kotlin.eval.runtime.ArgSymbol
import io.github.kotlinmania.starlark_kotlin.eval.bc.ResolvedArgName
import io.github.kotlinmania.starlark_kotlin.eval.bc.BcInstrArg
import io.github.kotlinmania.starlark_kotlin.values.layout.typed.FrozenStringValue
import io.github.kotlinmania.starlark_kotlin.util.asStr

/// Call arguments.
// pub(crate) trait BcCallArgs<S: ArgSymbol>: BcInstrArg
internal interface BcCallArgs<S : ArgSymbol> : BcInstrArg {
    // fn pop_from_stack<'a, 'v>(&'a self, frame: BcFramePtr<'v>) -> ArgumentsFull<'v, 'a, S>;
    fun popFromStack(frame: BcFramePtr): ArgumentsFull<S>
}

/// Call arguments for `def` call.
// pub(crate) trait BcCallArgsForDef: BcInstrArg
internal interface BcCallArgsForDef : BcInstrArg {
    // type Args<'v, 'a>: ArgumentsImpl<'v, 'a, ArgSymbol = ResolvedArgName>
    // fn pop_from_stack<'a, 'v>(&'a self, stack: BcFramePtr<'v>) -> Self::Args<'v, 'a>;
    fun popFromStack(stack: BcFramePtr): ArgumentsImpl<ResolvedArgName>
}

/// Full call arguments: positional, named, star and star-star. All taken from the stack.
// #[derive(Debug)]
// pub(crate) struct BcCallArgsFull<S: ArgSymbol>
internal class BcCallArgsFull<S : ArgSymbol>(
    val posNamed: BcSlotInRange,
    val names: List<Pair<S, FrozenStringValue>>,
    val args: BcSlotIn?,
    val kwargs: BcSlotIn?,
) {
    // impl<S: ArgSymbol> BcCallArgsFull<S>

    /// Number of positional arguments.
    // fn pos(&self) -> u32
    private fun pos(): UInt {
        check(posNamed.len() >= names.size.toUInt())
        return posNamed.len() - names.size.toUInt()
    }

    /// Display for BcCallArgsFull.
    // impl<S: ArgSymbol> Display for BcCallArgsFull<S>
    override fun toString(): String {
        return buildString {
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
}

/// Positional-only call arguments, from stack.
// #[derive(Debug)]
// pub(crate) struct BcCallArgsPos
internal class BcCallArgsPos(
    /// Range of positional arguments.
    val pos: BcSlotInRange,
)

// impl BcCallArgsFull<Symbol>
/// Resolve symbol-based call args to resolved arg names for a specific def.
// pub(crate) fn resolve(self, def: &FrozenDef) -> BcCallArgsFull<ResolvedArgName>
internal fun BcCallArgsFull<Symbol>.resolve(def: FrozenDef): BcCallArgsFull<ResolvedArgName> {
    return BcCallArgsFull(
        posNamed = posNamed,
        names = names.map { (name, value) ->
            Pair(def.resolveArgName(name.asStrHashed()), value)
        },
        args = args,
        kwargs = kwargs,
    )
}

// impl<S: ArgSymbol> BcCallArgs<S> for BcCallArgsFull<S>
/// Pop full call arguments from the stack frame.
internal class BcCallArgsFullCallArgs<S : ArgSymbol>(
    private val full: BcCallArgsFull<S>,
) : BcCallArgs<S> {
    // fn pop_from_stack<'a, 'v>(&'a self, stack: BcFramePtr<'v>) -> ArgumentsFull<'v, 'a, S>
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
            names = ArgNames.newUnique(full.names),
            args = argsVal,
            kwargs = kwargsVal,
        )
    }

    override fun fmtAppend(ip: Any, endArg: Any?, f: StringBuilder) {
        f.append(" $full")
    }
}

// impl<S: ArgSymbol> BcCallArgs<S> for BcCallArgsPos
/// Pop positional-only call arguments from the stack frame.
internal class BcCallArgsPosCallArgs<S : ArgSymbol>(
    private val posArgs: BcCallArgsPos,
) : BcCallArgs<S> {
    // fn pop_from_stack<'a, 'v>(&'a self, stack: BcFramePtr<'v>) -> ArgumentsFull<'v, 'a, S>
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

    override fun fmtAppend(ip: Any, endArg: Any?, f: StringBuilder) {
        f.append(" ${posArgs.pos}")
    }
}

// impl BcCallArgsForDef for BcCallArgsFull<ResolvedArgName>
/// Full call arguments for def calls, popping from the stack frame.
internal class BcCallArgsFullForDef(
    private val full: BcCallArgsFull<ResolvedArgName>,
) : BcCallArgsForDef {
    // fn pop_from_stack<'a, 'v>(&'a self, stack: BcFramePtr<'v>) -> ArgumentsFull<'v, 'a, ResolvedArgName>
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
            names = ArgNames.newUnique(full.names),
            args = argsVal,
            kwargs = kwargsVal,
        )
    }

    override fun fmtAppend(ip: Any, endArg: Any?, f: StringBuilder) {
        f.append(" $full")
    }
}

// impl BcCallArgsForDef for BcCallArgsPos
/// Positional-only call arguments for def calls, popping from the stack frame.
internal class BcCallArgsPosForDef(
    private val posArgs: BcCallArgsPos,
) : BcCallArgsForDef {
    // fn pop_from_stack<'a, 'v>(&'a self, stack: BcFramePtr<'v>) -> ArgumentsPos<'v, 'a, ResolvedArgName>
    override fun popFromStack(stack: BcFramePtr): ArgumentsPos<ResolvedArgName> {
        val pos = stack.getBcSlotRange(posArgs.pos)
        return ArgumentsPos(pos = pos)
    }

    override fun fmtAppend(ip: Any, endArg: Any?, f: StringBuilder) {
        f.append(" ${posArgs.pos}")
    }
}
