// port-lint: source tests:src/stdlib/breakpoint.rspackage io.github.kotlinmania.starlark.stdlib

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

import io.github.kotlinmania.starlark.ReentrantLock
import io.github.kotlinmania.starlark.assert.Assert
import io.github.kotlinmania.starlark.withLock
import kotlin.test.Test
import kotlin.test.assertEquals

// Breakpoint tests should not be executed concurrently
// to avoid interfering with the breakpoint state.
private val testLock = ReentrantLock()

/** Tests for the breakpoint module. */
class BreakpointTests {
    /**
     * Test with: BREAKPOINT=1 to enable real terminal breakpoint.
     * Skipped by default since it requires interactive input.
     */
    @Test
    fun testBreakpointReal() {
        testLock.withLock {
            resetBreakpointGlobalStateForTests()

            // Skip unless BREAKPOINT=1 is set in the environment.
            // In Kotlin/Multiplatform there is no universal env access, so this
            // test is effectively a no-op placeholder matching the Rust original.
            return
        }
    }

    @Test
    fun testBreakpointMock() {
        testLock.withLock {
            resetBreakpointGlobalStateForTests()

            val printedLines = mutableListOf<String>()

            val a = Assert()
            a.globalsAdd(::breakpointGlobal)
            a.setupEval { eval ->
                // `Assert` runs tests several times, take only lines from the last iteration.
                printedLines.clear()

                eval.breakpointHandler = {
                    object : BreakpointConsole {
                        private var called = false

                        override fun readLine(): String? {
                            val wasCalled = called
                            called = true
                            return if (!wasCalled) "x" else null
                        }

                        override fun println(line: String) {
                            printedLines.add(line)
                        }
                    }
                }
            }
            a.pass("x = [1,2,3]; breakpoint()")

            assertEquals(
                listOf(BREAKPOINT_HIT_MESSAGE, "[1, 2, 3]"),
                printedLines,
            )
        }
    }

    @Test
    fun testBreakpointDisabled() {
        testLock.withLock {
            resetBreakpointGlobalStateForTests()

            val a = Assert()
            a.globalsAdd(::breakpointGlobal)
            a.fail(
                "x = [1,2,3]; breakpoint()",
                "Breakpoint handler is not enabled",
            )
        }
    }
}

