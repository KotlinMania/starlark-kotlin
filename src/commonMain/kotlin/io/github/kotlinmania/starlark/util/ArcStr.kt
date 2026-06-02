package io.github.kotlinmania.starlark.util

import io.github.kotlinmania.starlark.util.arc_or_static.ArcOrStatic

/// Wrapper for `Arc<str>`.
// #[derive(Clone, Dupe, Eq, PartialEq, Hash, Ord, PartialOrd, Debug, derive_more::Display, Allocative)]
// #[display("{}", &**self)]
// pub struct ArcStr(ArcOrStatic<str>);
class ArcStr private constructor(
    private val inner: ArcOrStatic<String>,
) : Comparable<ArcStr> {

    // impl ArcStr

    companion object {
        /// Create from static `str` without allocation.
        // pub fn new_static(s: &'static str) -> ArcStr
        fun newStatic(s: String): ArcStr {
            return ArcStr(ArcOrStatic.newStatic(s))
        }

        // impl<'a> From<&'a str> for ArcStr
        // fn from(s: &'a str) -> Self
        fun from(s: String): ArcStr {
            return if (s.isEmpty()) {
                ArcStr(ArcOrStatic.newStatic("io/github/kotlinmania/starlark.util/ArcStr.kt"))
            } else {
                ArcStr(ArcOrStatic.newArc(s))
            }
        }
    }

    /// Get the `str`.
    // pub fn as_str(&self) -> &str
    fun asStr(): String {
        return deref()
    }

    // impl Deref for ArcStr
    // type Target = str;
    // fn deref(&self) -> &str
    fun deref(): String {
        return inner.deref()
    }

    // impl Borrow<str> for ArcStr
    // fn borrow(&self) -> &str
    fun borrow(): String {
        return deref()
    }

    // impl Display for ArcStr
    // #[display("{}", &**self)]
    override fun toString(): String {
        return deref()
    }

    // impl PartialEq for ArcStr
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ArcStr) return false
        return inner == other.inner
    }

    // impl Hash for ArcStr
    override fun hashCode(): Int {
        return inner.hashCode()
    }

    // impl Ord for ArcStr
    // impl PartialOrd for ArcStr
    override fun compareTo(other: ArcStr): Int {
        return inner.compareTo(other.inner)
    }
}
