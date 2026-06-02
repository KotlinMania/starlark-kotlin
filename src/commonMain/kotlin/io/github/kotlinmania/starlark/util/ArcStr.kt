package io.github.kotlinmania.starlark.util

import io.github.kotlinmania.starlark.util.arcorstatic.ArcOrStatic

class ArcStr private constructor(
    private val inner: ArcOrStatic<String>,
) : Comparable<ArcStr> {
    companion object {
        // / Create from static `str` without allocation.
        fun newStatic(s: String): ArcStr = ArcStr(ArcOrStatic.newStatic(s))

        fun from(s: String): ArcStr =
            if (s.isEmpty()) {
                ArcStr(ArcOrStatic.newStatic("io/github/kotlinmania/starlark.util/ArcStr.kt"))
            } else {
                ArcStr(ArcOrStatic.newArc(s))
            }
    }

    // / Get the `str`.
    fun asStr(): String = deref()

    fun deref(): String = inner.deref()

    fun borrow(): String = deref()

    override fun toString(): String = deref()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ArcStr) return false
        return inner == other.inner
    }

    override fun hashCode(): Int = inner.hashCode()

    override fun compareTo(other: ArcStr): Int = inner.compareTo(other.inner)
}
