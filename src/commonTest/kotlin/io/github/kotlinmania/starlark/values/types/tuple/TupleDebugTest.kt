package io.github.kotlinmania.starlark_kotlin.values.types.tuple

import io.github.kotlinmania.starlark_kotlin.assert.Assert
import kotlin.test.Test

class TupleDebugTest {
    @Test
    fun testTupleStr() {
        val a = Assert()
        // Get actual str result
        a.eq("str((1, 2, 3))", "'(1, 2, 3)'")
    }
}
