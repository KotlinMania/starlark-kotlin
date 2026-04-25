package io.github.kotlinmania.starlark.values.types

import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.UnpackValue
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.avalues.allocListIter
import io.github.kotlinmania.starlark.values.layout.avalues.allocTupleIter
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.types.bigint.allocValue
import io.github.kotlinmania.starlark.values.types.string.allocValue
import io.github.kotlinmania.starlark.values.types.string.unpackValueImplBorrowedString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ListOrTupleTest {
    private object StringUnpackValue : UnpackValue<String> {
        override fun starlarkTypeRepr(): Ty = Ty.string()

        override fun unpackValueImpl(value: Value): Result<String?> {
            return unpackValueImplBorrowedString(value)
        }
    }

    @Test
    fun testUnpack() {
        Heap.temp { heap ->
            val list = heap.allocListIter(
                listOf(
                    "a".allocValue(heap),
                    "b".allocValue(heap),
                )
            )
            val tuple = heap.allocTupleIter(
                listOf(
                    "a".allocValue(heap),
                    "b".allocValue(heap),
                )
            )
            val listOfInts = heap.allocListIter(
                listOf(
                    1.allocValue(heap),
                    2.allocValue(heap),
                )
            )
            val tupleOfInts = heap.allocTupleIter(
                listOf(
                    1.allocValue(heap),
                    2.allocValue(heap),
                )
            )

            val unpacker = UnpackListOrTupleUnpackValue(StringUnpackValue)

            assertEquals(
                listOf("a", "b"),
                unpacker.unpackValue(list).getOrThrow()!!.items,
            )
            assertEquals(
                listOf("a", "b"),
                unpacker.unpackValue(tuple).getOrThrow()!!.items,
            )
            assertNull(unpacker.unpackValue(listOfInts).getOrThrow())
            assertNull(unpacker.unpackValue(tupleOfInts).getOrThrow())
            assertNull(unpacker.unpackValue(1.allocValue(heap)).getOrThrow())
        }
    }
}

