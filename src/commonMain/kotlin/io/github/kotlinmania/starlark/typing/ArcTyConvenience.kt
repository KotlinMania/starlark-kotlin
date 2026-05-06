// port-lint: ignore
package io.github.kotlinmania.starlark.typing

// Convenience helpers on top of the Rust transliteration in `ArcTy.kt`.

fun ArcTy.isAny(): Boolean = deref().isAny()

fun ArcTy.isNever(): Boolean = deref().isNever()
