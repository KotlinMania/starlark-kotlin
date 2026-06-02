package io.github.kotlinmania.starlark.util

import io.github.kotlinmania.starlark.util.refcell.RefCell
import io.github.kotlinmania.starlark.util.refcell.unleakBorrow

internal val unleakBorrow: (RefCell<*>) -> Unit = { refCell -> unleakBorrow(refCell) }
