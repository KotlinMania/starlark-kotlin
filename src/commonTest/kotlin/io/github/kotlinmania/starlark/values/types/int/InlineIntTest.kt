package io.github.kotlinmania.starlark.values.types.int

import kotlin.test.Test
import kotlin.test.assertEquals

// Rust: #[cfg(test)] mod tests
class InlineIntTest {
    // Rust: #[test] fn test_min_max_for_bits()
    @Test
    fun testMinMaxForBits() {
        assertEquals(Pair(-1, 0), InlineInt.minMaxForBits(1))
        assertEquals(Pair(-2, 1), InlineInt.minMaxForBits(2))
        assertEquals(Pair(-4, 3), InlineInt.minMaxForBits(3))
        assertEquals(Pair(Int.MIN_VALUE, Int.MAX_VALUE), InlineInt.minMaxForBits(32))
    }
}
