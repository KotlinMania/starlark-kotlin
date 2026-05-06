// port-lint: ignore
package io.github.kotlinmania.starlark.util

import io.github.kotlinmania.starlark.util.refcell.RefCell
import io.github.kotlinmania.starlark.util.refcell.unleakBorrow as unleakBorrowImpl

internal val unleakBorrow: (RefCell<*>) -> Unit = { refCell -> unleakBorrowImpl(refCell) }
