// port-lint: source src/stdlib/breakpoint.rs
package io.github.kotlinmania.starlark_kotlin.stdlib.breakpoint

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

import io.github.kotlinmania.starlark_kotlin.environment.GlobalsBuilder
import io.github.kotlinmania.starlark_kotlin.read_line.ReadLine
import io.github.kotlinmania.starlark_kotlin.values.types.string.Evaluator
import io.github.kotlinmania.starlark_kotlin.values.types.list.NoneType
import io.github.kotlinmania.starlark_kotlin.values.types.function
import io.github.kotlinmania.starlark_kotlin.syntax.dialect.Dialect
import io.github.kotlinmania.starlark_kotlin.assert.parse
import io.github.kotlinmania.starlark_kotlin.eval.runtime.callStack
import io.github.kotlinmania.starlark_kotlin.eval.runtime.breakpointHandler
import io.github.kotlinmania.starlark_kotlin.debug.localVariables
import io.github.kotlinmania.starlark_kotlin.debug.evalStatements
import io.github.kotlinmania.starlark_kotlin.values.types.none.isNone
import io.github.kotlinmania.starlark_kotlin.syntax.AstModule

// A breakpoint takes over the console UI, so having two going at once confuses everything.
// Have a global mutex to ensure one at a time.
// static BREAKPOINT_MUTEX: Lazy<Mutex<State>> = Lazy::new(|| Mutex::new(State::Allow));
private var breakpointState: State = State.Allow

/// `breakpoint` function uses this trait to perform console IO.
// pub(crate) trait BreakpointConsole
internal interface BreakpointConsole {
    /// Return `null` on EOF.
    // fn read_line(&mut self) -> anyhow::Result<Option<String>>
    fun readLine(): String?
    // fn println(&mut self, line: &str)
    fun println(line: String)
}

/// Breakpoint handler implemented with `ReadLine`.
// pub(crate) struct RealBreakpointConsole
internal class RealBreakpointConsole(
    private val readLine: ReadLine,
) : BreakpointConsole {
    // fn read_line(&mut self) -> anyhow::Result<Option<String>>
    override fun readLine(): String? = readLine.readLine("$> ")

    // fn println(&mut self, line: &str)
    override fun println(line: String) {
        kotlin.io.println(line)
    }

    companion object {
        // pub(crate) fn factory() -> Box<dyn Fn() -> ...>
        fun factory(): () -> BreakpointConsole {
            return {
                RealBreakpointConsole(
                    readLine = ReadLine.new("STARLARK_RUST_DEBUGGER_HISTFILE"),
                )
            }
        }
    }
}

/// Is debugging allowed or not? After the user hits Ctrl-C they probably
/// just want to stop hard, so don't keep dropping them into breakpoints.
// enum State { Allow, Stop }
private enum class State {
    Allow, // More breakpoints are fine
    Stop,  // No more breakpoints
}

/// We've run a breakpoint command, what should we do.
// enum Next { Again, Resume, Fail }
private enum class Next {
    Again,  // Accept another breakpoint command
    Resume, // Continue running
    Fail,   // Stop running
}

// fn cmd_help(eval, rl) -> anyhow::Result<Next>
private fun cmdHelp(eval: Evaluator, rl: BreakpointConsole): Next {
    for ((names, msg, _) in COMMANDS) {
        rl.println("* :${names[0]}, $msg")
    }
    return Next.Again
}

// fn cmd_variables(eval, rl) -> anyhow::Result<Next>
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

// fn cmd_stack(eval, rl) -> anyhow::Result<Next>
private fun cmdStack(eval: Evaluator, rl: BreakpointConsole): Next {
    for (line in eval.callStack().toString().lines()) {
        rl.println(line)
    }
    return Next.Again
}

// fn cmd_resume(eval, rl) -> anyhow::Result<Next>
private fun cmdResume(eval: Evaluator, rl: BreakpointConsole): Next {
    return Next.Resume
}

// fn cmd_fail(eval, rl) -> anyhow::Result<Next>
private fun cmdFail(eval: Evaluator, rl: BreakpointConsole): Next {
    return Next.Fail
}

// type CommandFn = fn(eval: &mut Evaluator, &mut dyn BreakpointConsole) -> anyhow::Result<Next>
private typealias CommandFn = (Evaluator, BreakpointConsole) -> Next

// const COMMANDS: &[(&[&str], &str, CommandFn)]
private val COMMANDS: List<Triple<List<String>, String, CommandFn>> = listOf(
    Triple(listOf("help", "?"), "Show this help message", ::cmdHelp),
    Triple(listOf("vars"), "Show all local variables", ::cmdVariables),
    Triple(listOf("stack"), "Show the stack trace", ::cmdStack),
    Triple(listOf("resume", "quit", "exit"), "Resume execution", ::cmdResume),
    Triple(listOf("fail"), "Abort with a failure message", ::cmdFail),
)

// fn pick_command(x, rl) -> Option<CommandFn>
private fun pickCommand(x: String, rl: BreakpointConsole): CommandFn? {
    // If we can find a command that matches perfectly, do that
    // Otherwise return the longest match, but if they are multiple, show a warning
    val poss = mutableListOf<Pair<String, CommandFn>>()
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

// fn breakpoint_loop(eval, rl) -> anyhow::Result<State>
private fun breakpointLoop(eval: Evaluator, rl: BreakpointConsole): State {
    while (true) {
        val readline = rl.readLine()
        if (readline == null) {
            return State.Stop
        }
        val line = readline
        if (line.startsWith(":")) {
            val cmdName = line.substring(1).trimEnd()
            val cmd = pickCommand(cmdName, rl)
            if (cmd != null) {
                when (cmd(eval, rl)) {
                    Next.Again -> {}
                    Next.Resume -> return State.Allow
                    Next.Fail -> throw RuntimeException("Selected :fail at breakpoint()")
                }
            }
        } else {
            try {
                val ast = AstModule.parse("interactive", line, Dialect.AllOptionsInternal)
                val v = eval.evalStatements(ast)
                if (!v.isNone()) {
                    rl.println(v.toString())
                }
            } catch (e: Exception) {
                rl.println(e.toString())
            }
        }
    }
    @Suppress("UNREACHABLE_CODE")
    error("unreachable")
}

// const BREAKPOINT_HIT_MESSAGE: &str = ...
private const val BREAKPOINT_HIT_MESSAGE: String =
    "BREAKPOINT HIT! :resume to continue, :help for all options"

// #[starlark_module]
// pub fn global(builder: &mut GlobalsBuilder) { fn breakpoint(...) }
fun global(builder: GlobalsBuilder) {
    /// When a debugger is available, breaks into the debugger.
    builder.function("breakpoint") { _, eval ->
        if (breakpointState == State.Allow) {
            val handler = eval.breakpointHandler
                ?: throw RuntimeException("Breakpoint handler is not enabled for current Evaluator")
            val rl = handler()
            rl.println(BREAKPOINT_HIT_MESSAGE)
            breakpointState = breakpointLoop(eval, rl)
        }
        NoneType
    }
}

// #[cfg(test)] mod tests
// Tests are in commonTest, not here.
