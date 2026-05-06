// port-lint: source stdlib/breakpoint.rs
@file:OptIn(ExperimentalAtomicApi::class)

package io.github.kotlinmania.starlark.stdlib

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

import io.github.kotlinmania.starlark.debug.evalStatements
import io.github.kotlinmania.starlark.debug.localVariables
import io.github.kotlinmania.starlark.environment.GlobalsBuilder
import io.github.kotlinmania.starlark.eval.runtime.Evaluator
import io.github.kotlinmania.starlark.readline.ReadLine
import io.github.kotlinmania.starlark.syntax.AstModule
import io.github.kotlinmania.starlark.syntax.dialect.Dialect
import io.github.kotlinmania.starlark.values.types.none.NoneType
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi

// A breakpoint takes over the console UI, so having two going at once confuses everything.
// Have a global mutex to ensure one at a time.
private val breakpointMutex = AtomicInt(0)
private var breakpointState: State = State.Allow

private inline fun <T> withBreakpointMutex(action: () -> T): T {
    while (!breakpointMutex.compareAndSet(0, 1)) {
        // We do not expect contention here. This is a best-effort translation of Rust's
        // blocking mutex to a multiplatform-compatible primitive.
    }
    try {
        return action()
    } finally {
        breakpointMutex.store(0)
    }
}

/**
 * `breakpoint` function uses this interface to perform console IO.
 */
internal interface BreakpointConsole {
    /** Return `null` on EOF. */
    fun readLine(): String?

    fun println(line: String)
}

/**
 * Breakpoint handler implemented with [ReadLine].
 */
internal class RealBreakpointConsole(
    private val readLine: ReadLine,
) : BreakpointConsole {

    override fun readLine(): String? = readLine.readLine("$> ")

    override fun println(line: String) {
        kotlin.io.println(line)
    }

    companion object {
        fun factory(): () -> BreakpointConsole {
            return {
                RealBreakpointConsole(
                    readLine = ReadLine.new("STARLARK_RUST_DEBUGGER_HISTFILE"),
                )
            }
        }
    }
}

/**
 * Is debugging allowed or not? After the user hits Ctrl-C they probably
 * just want to stop hard, so don't keep dropping them into breakpoints.
 */
private enum class State {
    Allow, // More breakpoints are fine
    Stop,  // No more breakpoints
}

/** We've run a breakpoint command, what should we do. */
private enum class Next {
    Again,  // Accept another breakpoint command
    Resume, // Continue running
    Fail,   // Stop running
}

private fun cmdHelp(_eval: Evaluator, rl: BreakpointConsole): Next {
    for ((names, msg, _) in COMMANDS) {
        rl.println("* :${names[0]}, $msg")
    }
    return Next.Again
}

private fun cmdVariables(eval: Evaluator, rl: BreakpointConsole): Next {
    fun truncate(s: String, n: Int): String {
        return if (s.length > n) {
            s.substring(0, n) + "..."
        } else {
            s
        }
    }

    for ((name, value) in eval.localVariables()) {
        rl.println("* $name = ${truncate(value.toString(), 80)}")
    }
    return Next.Again
}

private fun cmdStack(eval: Evaluator, rl: BreakpointConsole): Next {
    for (line in eval.callStack().toString().lines()) {
        rl.println(line)
    }
    return Next.Again
}

private fun cmdResume(_eval: Evaluator, _rl: BreakpointConsole): Next {
    return Next.Resume
}

private fun cmdFail(_eval: Evaluator, _rl: BreakpointConsole): Next {
    return Next.Fail
}

private val COMMANDS: List<Triple<List<String>, String, (Evaluator, BreakpointConsole) -> Next>> = listOf(
    Triple(listOf("help", "?"), "Show this help message", ::cmdHelp),
    Triple(listOf("vars"), "Show all local variables", ::cmdVariables),
    Triple(listOf("stack"), "Show the stack trace", ::cmdStack),
    Triple(listOf("resume", "quit", "exit"), "Resume execution", ::cmdResume),
    Triple(listOf("fail"), "Abort with a failure message", ::cmdFail),
)

private fun pickCommand(x: String, rl: BreakpointConsole): ((Evaluator, BreakpointConsole) -> Next)? {
    // If we can find a command that matches perfectly, do that
    // Otherwise return the longest match, but if they are multiple, show a warning
    val poss = mutableListOf<Pair<String, (Evaluator, BreakpointConsole) -> Next>>()
    for ((names, _, cmd) in COMMANDS) {
        for (n in names) {
            if (n == x) {
                return cmd
            }
            if (n.startsWith(x)) {
                poss.add(Pair(n, cmd))
                break
            }
        }
    }
    return when {
        poss.isEmpty() -> {
            rl.println("Unrecognised command, type :help for all commands")
            null
        }
        poss.size == 1 -> poss[0].second
        else -> {
            rl.println("Ambiguous command, could have been any of: ${poss.joinToString(" ") { it.first }}")
            null
        }
    }
}

private fun breakpointLoop(eval: Evaluator, rl: BreakpointConsole): State {
    while (true) {
        val readline = rl.readLine() ?: return State.Stop
        if (readline.startsWith(":")) {
            val cmdName = readline.substring(1).trimEnd()
            val cmd = pickCommand(cmdName, rl)
            if (cmd != null) {
                when (cmd(eval, rl)) {
                    Next.Again -> {}
                    Next.Resume -> return State.Allow
                    Next.Fail -> throw RuntimeException("Selected :fail at breakpoint()")
                }
            }
        } else {
            val res = AstModule.parse("interactive", readline, Dialect.AllOptionsInternal)
                .mapCatching { ast -> eval.evalStatements(ast).getOrThrow() }
            res.fold(
                onSuccess = { v ->
                    if (!v.isNone()) {
                        rl.println(v.toString())
                    }
                },
                onFailure = { e ->
                    rl.println(e.toString())
                },
            )
        }
    }
}

/** Error thrown when no breakpoint handler is configured on the evaluator. */
internal class BreakpointError(message: String) : RuntimeException(message)

internal const val BREAKPOINT_HIT_MESSAGE: String =
    "BREAKPOINT HIT! :resume to continue, :help for all options"

internal fun resetBreakpointGlobalStateForTests() {
    // `breakpoint()` function modifies the global state.
    withBreakpointMutex {
        breakpointState = State.Allow
    }
}

/**
 * When a debugger is available, breaks into the debugger.
 *
 * Registers the `breakpoint` built-in function into the given [GlobalsBuilder].
 */
fun breakpointGlobal(builder: GlobalsBuilder) {
    builder.setFunction("breakpoint") { _, eval ->
        withBreakpointMutex {
            if (breakpointState == State.Allow) {
                val handler = eval.breakpointHandler
                    ?: throw BreakpointError("Breakpoint handler is not enabled for current Evaluator")
                val rl = handler()
                rl.println(BREAKPOINT_HIT_MESSAGE)
                breakpointState = breakpointLoop(eval, rl)
            }
        }
        NoneType
    }
}
