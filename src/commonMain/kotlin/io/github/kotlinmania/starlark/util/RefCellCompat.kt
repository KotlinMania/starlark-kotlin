package io.github.kotlinmania.starlark_kotlin.util

import io.github.kotlinmania.starlark_kotlin.util.refcell.RefCell
import io.github.kotlinmania.starlark_kotlin.util.refcell.unleakBorrow as unleakBorrowImpl

internal val unleakBorrow: (RefCell<*>) -> Unit = { refCell -> unleakBorrowImpl(refCell) }
