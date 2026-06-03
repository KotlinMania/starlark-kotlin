// port-lint: source src/values/typing/ty.rs
package io.github.kotlinmania.starlark.values.typing.ty

import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.typing.TyBasic
import io.github.kotlinmania.starlark.values.StarlarkValue

/** Type of type. */
// An uninhabited enum in Rust — no instances can be created.
// In Kotlin, represented as a sealed class with no subclasses.
sealed class AbstractType : StarlarkValue {
    override val TYPE: String get() = "type"
    override val HAS_eval_type: Boolean get() = true

    override fun getTypeStarlarkRepr(): Ty = starlarkTypeRepr()

    // This is unreachable, but this function is needed
    // so `TyStarlarkValue` could think this is a type.
    override fun evalType(): Ty? {
        error("AbstractType is uninhabited")
    }

    override fun toString(): String = "type"

    companion object {
        fun starlarkTypeRepr(): Ty = Ty.basic(TyBasic.TypeObject)
    }
}
