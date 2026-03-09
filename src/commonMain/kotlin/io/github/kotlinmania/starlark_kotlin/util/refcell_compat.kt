package io.github.kotlinmania.starlark_kotlin.util

import io.github.kotlinmania.starlark_kotlin.util.refcell.RefCell

internal val unleakBorrow: (RefCell<*>) -> Unit =
    io.github.kotlinmania.starlark_kotlin.util.refcell::unleakBorrow
