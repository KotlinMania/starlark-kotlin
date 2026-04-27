// port-lint: source tests:src/util/refcell.rspackage io.github.kotlinmania.starlark.util.refcell

/*
 * Copyright 2018 The Starlark in Rust Authors.
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

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class RefcellTest {
    @Test
    fun testUnleakBorrow() {
        val refCell = RefCell(1)
        val firstMutBorrow = refCell.tryBorrowMut()
        assertNotNull(firstMutBorrow)
        firstMutBorrow.close()

        refCell.borrow().leak()
        assertNull(refCell.tryBorrowMut(), "RefCell is borrowed, so we cannot borrow it mutably")
        unleakBorrow(refCell)
        val secondMutBorrow = refCell.tryBorrowMut()
        assertNotNull(secondMutBorrow, "Borrow is unleaked, so we can borrow it mutably")
        secondMutBorrow.close()

        refCell.borrow().leak()
        refCell.borrow().leak()

        assertNull(refCell.tryBorrowMut(), "RefCell is borrowed, so we cannot borrow it mutably")
        unleakBorrow(refCell)
        assertNull(refCell.tryBorrowMut(), "RefCell is still borrowed")
        unleakBorrow(refCell)
        val finalMutBorrow = refCell.tryBorrowMut()
        assertNotNull(finalMutBorrow)
        finalMutBorrow.close()
    }
}
