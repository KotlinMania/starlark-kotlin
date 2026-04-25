// port-lint: source src/values/typing/ty.rs
package io.github.kotlinmania.starlark.values.typing.ty

import io.github.kotlinmania.starlark.assert.Assert
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.typing.TyBasic
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.layout.Value

/** Type of type. */
// #[derive(Debug, Display, Allocative, ProvidesStaticType, NoSerialize)]
// pub enum AbstractType {}
// An uninhabited enum in Rust — no instances can be created.
// In Kotlin, represented as a sealed class with no subclasses.
sealed class AbstractType : StarlarkValue {
    // #[starlark_value(type = "type")]
    override val TYPE: kotlin.String get() = "type"

    override fun getTypeStarlarkRepr(): Ty = Companion.starlarkTypeRepr()

    // fn eval_type(&self) -> Option<Ty>
    // This is unreachable, but this function is needed
    // so `TyStarlarkValue` could think this is a type.
    override fun evalType(): Ty? {
        error("AbstractType is uninhabited")
    }

    override fun toString(): kotlin.String = "type"

    companion object {
        // fn get_type_starlark_repr() -> Ty
        fun starlarkTypeRepr(): Ty = Ty.basic(TyBasic.Type)
    }
}

// #[cfg(test)]
// mod tests

// #[test]
// fn test_isinstance()
internal fun testIsinstance() {
    Assert.isTrue("isinstance(int, type)")
    Assert.isFalse("isinstance(1, type)")
    Assert.isTrue("isinstance(list[str], type)")
    Assert.isTrue("isinstance(eval_type(list), type)")
}

// #[test]
// fn test_pass()
internal fun testPass() {
    Assert.pass(
        """
def accepts_type(t: type):
    pass

def test():
    accepts_type(int)
    accepts_type(list[str])
    accepts_type(None | int)

test()
""",
    )
}

// #[test]
// fn test_fail_compile_time()
internal fun testFailCompileTime() {
    Assert.fail(
        """
def accepts_type(t: type):
    pass

def test():
    accepts_type(1)
""",
        "Expected type `type` but got `int`",
    )
}
