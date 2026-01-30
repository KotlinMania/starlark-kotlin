# Starlark Kotlin Port - Agent Guidelines

This file contains guidelines for AI agents and human contributors working on the starlark-kotlin port.

## Project Context

This is a **line-by-line transliteration port** of [facebook/starlark-rust](https://github.com/facebook/starlark-rust) to Kotlin Multiplatform. The goal is semantic parity with the Rust implementation while providing idiomatic Kotlin APIs.

## General Porting Principles

### 1. Semantic Parity (The "Dishonest Code" Rule)

- **Port the intent and behavior**, not just syntax
- Rust's traits often carry specific formatting contracts, behavioral expectations, or performance characteristics
- Do **not** oversimplify implementations if the original code performed non-trivial work
- Example: Rust's `Display` trait implementations often handle formatting, ANSI codes, truncation - replicate this logic in Kotlin's `toString()` or helper methods

### 2. Research First

- **Do not guess** at the behavior of Rust functions, traits, or types
- Look up official Rust documentation when uncertain
- Rust's type system and traits carry subtle behaviors (buffering, blocking, formatting state, ownership) that aren't obvious from signatures

### 3. Line-by-Line Transliteration

- Maintain file structure and organization from the Rust codebase
- Port modules to packages with equivalent naming (snake_case → camelCase for functions/variables, but preserve file/package structure)
- Preserve comments and documentation (translate to KDoc format)

### 4. Provenance Markers (REQUIRED)

Every ported Kotlin file **must** start with a provenance marker:

```kotlin
// port-lint: source <relative-path-to-rust-file>
package io.github.kotlinmania.starlark_kotlin.<module>

// Rest of file...
```

Example:
```kotlin
// port-lint: source starlark/src/environment/module.rs
package io.github.kotlinmania.starlark_kotlin.environment
```

This enables the AST distance tool to track porting progress and verify completeness.

### 5. Documentation

- Translate Rust doc comments (`///`, `//!`) to KDoc format
- Preserve examples, code blocks, and explanatory text
- Update references to Rust-specific concepts (e.g., "this trait" → "this interface")
- Add KDoc for public APIs

## Kotlin-Specific Guidelines

### Naming Conventions

- **Files:** Match Rust file names but use PascalCase for Kotlin files (e.g., `module.rs` → `Module.kt`)
- **Packages:** Mirror Rust crate structure (e.g., `starlark::environment` → `io.github.kotlinmania.starlark_kotlin.environment`)
- **Types:** PascalCase (same as Rust)
- **Functions/Variables:** camelCase (Rust snake_case → Kotlin camelCase)
- **Constants:** UPPER_SNAKE_CASE (same as Rust)

### Error Handling

- Rust `Result<T, E>` → Kotlin `Result<T>` with appropriate exception types
- Consider using Kotlin's built-in `runCatching` where appropriate
- Preserve error messages and context from Rust

### Collections

- Rust `Vec<T>` → Kotlin `MutableList<T>` or `List<T>` (prefer immutable when possible)
- Rust `HashMap<K, V>` → Kotlin `MutableMap<K, V>` or `Map<K, V>`
- Use `kotlinx-collections-immutable` for persistent collections where Rust uses immutable structures

### Concurrency

- Rust `Arc<T>` / `Mutex<T>` → Use Kotlin coroutines and atomic references where appropriate
- Rust async → Kotlin `suspend fun`
- Be mindful of thread safety - Kotlin Multiplatform has different concurrency models per platform

### Traits vs Interfaces

- Rust trait → Kotlin interface (with default implementations where appropriate)
- Rust trait objects (`Box<dyn Trait>`) → Kotlin interface references
- Rust trait bounds → Kotlin generic constraints (`where T : SomeTrait`)

### Macros

- Rust procedural macros cannot be directly ported
- Implement equivalent functionality using Kotlin's language features:
  - Code generation if needed
  - Inline functions
  - Delegation
  - Annotation processing (JVM-only)

## Testing

- Port Rust tests to Kotlin tests
- Maintain test structure and organization
- Use `kotlin.test` for multiplatform test compatibility
- Snapshot tests: Consider using equivalents to Rust's `insta` crate

## Building and Tooling

### Build Commands

```bash
# Build all targets
./gradlew build

# Run tests
./gradlew test

# Check specific target
./gradlew macosArm64Test
./gradlew jvmTest
```

### Tracking Progress

Use the AST distance tool from the parent codex-kotlin project:

```bash
# Analyze porting progress
../codex-kotlin/tools/ast_distance/ast_distance --deep tmp/rust-source rust src kotlin

# Check similarity of specific files
../codex-kotlin/tools/ast_distance/ast_distance tmp/rust-source/starlark/src/module.rs src/commonMain/kotlin/io/github/kotlinmania/starlark_kotlin/Module.kt
```

## Code Style

### Formatting

- Use default Kotlin formatting (ktlint/IntelliJ defaults)
- 4-space indentation
- Max line length: 120 characters (flexible for readability)

### Commenting

- Only comment code that needs clarification
- Do not add redundant comments
- Translate meaningful Rust comments to Kotlin
- Preserve algorithmic explanations and rationale

### Prefer Kotlin Idioms

- Use Kotlin's standard library when equivalent to Rust's
- Leverage Kotlin's null safety instead of `Option<T>` where appropriate
- Use data classes for simple structs
- Use sealed classes for Rust enums with data
- Use object for Rust unit structs with no data

## Dependencies

This port uses minimal dependencies:

- `kotlinx-coroutines-core` - Async/concurrency
- `kotlinx-serialization` - Serialization (if needed)
- `kotlinx-collections-immutable` - Persistent collections
- `kotlinx-datetime` - Date/time handling

Add new dependencies only when necessary and document the rationale.

## Platform-Specific Code

When porting platform-specific Rust code:

- Use `expect`/`actual` declarations for platform differences
- Place common code in `commonMain`
- Platform-specific implementations in `<platform>Main` (e.g., `jvmMain`, `nativeMain`)

## References

- [Starlark Spec](https://github.com/bazelbuild/starlark/blob/master/spec.md)
- [Starlark Rust Docs](https://docs.rs/starlark/)
- [Kotlin Multiplatform Docs](https://kotlinlang.org/docs/multiplatform.html)
- [Parent Port Guidelines](../codex-kotlin/AGENTS.md) - for general Rust→Kotlin porting patterns

## Questions?

For questions about porting strategy or architecture decisions, open an issue or discussion on the GitHub repository.
