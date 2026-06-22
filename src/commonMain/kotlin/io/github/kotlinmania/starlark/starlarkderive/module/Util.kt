// port-lint: source module/util.rs
package io.github.kotlinmania.starlark.starlarkderive.module

import io.github.kotlinmania.syn.GenericArgument
import io.github.kotlinmania.syn.Ident
import io.github.kotlinmania.syn.Path
import io.github.kotlinmania.syn.PathArguments
import io.github.kotlinmania.syn.SynType

/**
 * Whether a type matches a given name by checking the last path segment.
 */
internal fun isTypeName(x: SynType, name: String): Boolean {
    if (x is SynType.Path) {
        val last = x.path.segments.last()
        return last?.ident?.toString() == name
    }
    return false
}

/**
 * If the type is `Option<T>`, return the inner type `T`.
 */
internal fun unpackOption(x: SynType): SynType? {
    if (x is SynType.Path) {
        val last = x.path.segments.last() ?: return null
        if (last.ident.toString() == "Option") {
            val args = last.arguments
            if (args is PathArguments.AngleBracketed) {
                val first = args.args.first()
                if (first is GenericArgument.TypeArg) {
                    return first.type
                }
            }
        }
    }
    return null
}

/**
 * Convert an identifier to a string, stripping the raw identifier prefix.
 */
internal fun identString(x: Ident): String {
    val s = x.toString()
    return if (s.startsWith("r#")) s.substring(2) else s
}