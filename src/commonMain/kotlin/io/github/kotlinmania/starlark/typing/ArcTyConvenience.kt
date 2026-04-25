package io.github.kotlinmania.starlark_kotlin.typing

// Convenience helpers on top of the Rust transliteration in `ArcTy.kt`.

fun ArcTy.isAny(): Boolean = deref().isAny()

fun ArcTy.isNever(): Boolean = deref().isNever()

