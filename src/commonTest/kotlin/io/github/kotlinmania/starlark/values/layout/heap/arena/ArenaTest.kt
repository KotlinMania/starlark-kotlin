package io.github.kotlinmania.starlark.values.layout.heap.arena

import io.github.kotlinmania.starlark.values.layout.AValue
import io.github.kotlinmania.starlark.values.layout.AValueImpl
import io.github.kotlinmania.starlark.values.layout.heap.AValueHeader
import io.github.kotlinmania.starlark.values.types.StarlarkAny
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ArenaTest {
    private fun toRepr(x: AValueHeader): String {
        val s = StringBuilder()
        x.unpack().collectRepr(s)
        return s.toString()
    }

    private fun mkStr(x: String): AValueImpl<AValue> {
        return AValueImpl.new(StarlarkAny.new(x))
    }

    private fun reserveStr(arena: Arena, @Suppress("UNUSED_PARAMETER") x: AValueImpl<AValue>): Reservation<AValue> {
        return arena.reserveWithExtra(0)
    }

    @Test
    fun testTraitArenaIteration() {
        val limit = 1_000
        val arena = Arena()
        val reserved = mutableListOf<Pair<Reservation<AValue>, Int>>()

        for (i in 0 until limit) {
            if (i % 100 == 0) {
                val r = reserveStr(arena, mkStr(""))
                reserved.add(Pair(r, i))
            } else {
                arena.alloc(mkStr(i.toString()))
            }
        }

        assertTrue(reserved.isNotEmpty())

        for ((r, i) in reserved) {
            r.fill(StarlarkAny.new(i.toString()))
        }

        var j = 0
        arena.forEachOrdered { event ->
            when (event) {
                is ArenaVisitEvent.EnterBump -> {}
                is ArenaVisitEvent.Value -> {
                    val header = event.value.unpackHeader()
                    if (header != null) {
                        assertEquals(j.toString(), toRepr(header))
                        j += 1
                    }
                }
            }
        }
        assertEquals(limit, j)

        var count = 0
        arena.forEachDropUnordered { _ -> count += 1 }
        assertEquals(limit, count)
    }

    @Test
    fun dropWithBlackHole() {
        val arena = Arena()
        arena.alloc(mkStr("test"))
        // reserve but do not fill!
        reserveStr(arena, mkStr(""))
        arena.alloc(mkStr("hello"))

        val res = mutableListOf<AValueHeader>()
        arena.forEachOrdered { event ->
            when (event) {
                is ArenaVisitEvent.EnterBump -> {}
                is ArenaVisitEvent.Value -> {
                    val header = event.value.unpackHeader()
                    if (header != null) {
                        res.add(header)
                    }
                }
            }
        }

        assertEquals(3, res.size)
        assertEquals("test", toRepr(res[0]))
        assertEquals("hello", toRepr(res[2]))
    }

    @Test
    fun testAllocatedSummary() {
        val arena = Arena()
        arena.alloc(mkStr("test"))
        arena.alloc(mkStr("test"))
        val res = arena.allocatedSummary().summary
        assertEquals(1, res.len())
        val entry = res.values().first()
        assertEquals(2, entry.count)
        assertTrue(entry.bytes <= arena.allocatedBytes())
    }

    @Test
    fun testIsEmpty() {
        val arena = Arena()
        assertTrue(arena.isEmpty())
        arena.allocStr("xyz")
        assertFalse(arena.isEmpty())
    }
}
