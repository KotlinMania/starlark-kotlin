# Starlark Kotlin Port - Agent Guidelines

This file contains guidelines for AI agents and human contributors working on the starlark-kotlin port.

## Project Context

This is a **line-by-line transliteration port** of [facebook/starlark-rust](https://github.com/facebook/starlark-rust) to Kotlin Multiplatform. The goal is semantic parity with upstream behavior while providing idiomatic Kotlin APIs.

## General Porting Principles

### 1. Semantic Parity (The "Dishonest Code" Rule)

- **Port the intent and behavior**, not just syntax
- Rust's traits often carry specific formatting contracts, behavioral expectations, or performance characteristics
- Do **not** oversimplify implementations if the original code performed non-trivial work
- Example: Rust's `Display` trait implementations often handle formatting, ANSI codes, truncation — translate that logic faithfully into Kotlin's `toString()` or helper methods

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
package io.github.kotlinmania.starlark.<module>

// Rest of file...
```

Examples:
```kotlin
// port-lint: source src/environment/module.rs
package io.github.kotlinmania.starlark.environment

// port-lint: source src/values/layout.rs
package io.github.kotlinmania.starlark.values

// port-lint: source src/eval/runtime/evaluator.rs
package io.github.kotlinmania.starlark.eval.runtime
```

**Path Format:** The path should be relative to `tmp/starlark/` (the Rust source root). So for a file at `tmp/starlark/src/values/layout.rs`, use `src/values/layout.rs`.

This enables the AST distance tool to track porting progress and verify completeness.

### 5. Copyright Headers

**REQUIRED:** Every ported Kotlin file must include this copyright header immediately after the port-lint header:

```kotlin
// port-lint: source <path>
package <package-name>

/*
 * Copyright 2019 The Starlark in Rust Authors.
 * Copyright (c) Facebook, Inc. and its affiliates.
 * Copyright (c) 2025 Sydney Renee, The Solace Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
```

This preserves the original Rust copyright while adding the maintainer's copyright for the Kotlin port.

### 6. Documentation

- Translate Rust doc comments (`///`, `//!`) to KDoc format
- Preserve examples, code blocks, and explanatory text
- Update references to Rust-specific concepts (e.g., "this trait" → "this interface")
- Add KDoc for public APIs

### 7. TODO Policy (IMPORTANT)

**DO NOT add TODO comments without explicit user approval.**

- If you cannot implement something fully, ASK the user first
- Research Rust documentation before adding TODOs
- Look for similar patterns in the codebase
- Prefer complete implementations or approved placeholder strategies
- TODOs should only be added when the user explicitly approves them

## Kotlin-Specific Guidelines

### CRITICAL: Kotlin Multiplatform - NO JAVA

**This is a Kotlin Multiplatform project targeting JVM, Native, and JS.**

**ABSOLUTELY NO Java-specific code is allowed in commonMain:**
- ❌ NO `import java.*`
- ❌ NO `java.util.concurrent.*`
- ❌ NO `java.io.*`
- ❌ NO `java.nio.*`
- ❌ NO JVM-only APIs

**Use Kotlin Multiplatform alternatives:**
- ✅ `kotlin.collections.*` for collections
- ✅ `kotlinx.atomicfu` for atomic operations (tree-sitter parsers already integrated)
- ✅ `kotlinx.coroutines` for concurrency
- ✅ `expect`/`actual` for platform-specific implementations
- ✅ Pure Kotlin standard library APIs

**Tree-sitter parsers are already available** - no need to implement parsers yourself.

### Naming Conventions

- **Files:** Match Rust file names but use PascalCase for Kotlin files (e.g., `module.rs` → `Module.kt`)
- **Packages:** Mirror Rust crate structure (e.g., `starlark::environment` → `io.github.kotlinmania.starlark.environment`)
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

### Trait default methods with `where` clauses → method-level Kotlin generic bounds

Rust traits routinely declare a default method whose body only typechecks
when the type parameter satisfies a stricter bound:

```rust
pub trait RangeBounds<T> {
    fn start_bound(&self) -> Bound<&T>;
    fn end_bound(&self) -> Bound<&T>;

    fn is_empty(&self) -> bool
    where T: PartialOrd,
    { /* default body uses < */ }
}
```

The trait stays unconstrained; the *method* picks up the bound via its
own `where` clause. Kotlin has no per-method `where` on an interface
member. Three obvious mappings fail:

1. **Tighten the interface to `<T : Comparable<T>>`.** Breaks every
   caller that holds the unbounded interface type for an opaque `Q`.
2. **Make the method abstract on the interface.** Forces every concrete
   impl, including ones whose Rust counterpart inherits the default,
   to invent a body and pile on `override` boilerplate.
3. **Runtime cast helper.** `if (left is Comparable<*> && right is Comparable<*>) ... else throw IllegalStateException(...)` — compile-time
   bounds become runtime crashes. The cheat detector flags this and
   zeros the file's score.

#### The faithful pattern

Translate the default to a Kotlin **extension function whose own type
parameter carries the bound**:

```kotlin
interface RangeBounds<T> {
    fun startBound(): Bound<T>
    fun endBound(): Bound<T>
    // The `is_empty(&self) where T: PartialOrd` default lives outside the
    // interface, on an extension function whose own type parameter
    // carries the bound.
}

fun <T : Comparable<T>> RangeBounds<T>.isEmpty(): Boolean { /* default body */ }
```

Concrete impls that want to specialise the default supply a same-named
**member function**. Kotlin resolves `range.isEmpty()` to the member for
the concrete static receiver type and to the extension for the interface
type — exactly mirroring Rust's "default method, per-impl override". No
`override` keyword on the member; there is nothing on the interface to
override.

Recipe:

1. Interface keeps only the methods declared without where-clauses.
2. Each default-method-with-where-clause becomes a Kotlin extension
   whose own type-parameter bound mirrors the where-clause
   (`<T : Comparable<T>>`, `<Q : Comparable<Q>>` plus
   `where K : Comparable<Q>`, etc.).
3. Concrete subtypes specialise by declaring a same-named member.
4. Callers holding the unbounded interface type cannot invoke the
   comparison-using methods. That is correct: Rust would reject the
   same call without the where-clause's bound.

#### Pair with the dual-overload pattern when both paths are needed

When a function has to work in both the comparator-aware and natural-order
paths, expose two overloads — the unbounded one takes the comparator
explicitly, the bounded one is sugar that delegates:

```kotlin
internal fun <Q> Tree.search(key: Q, compare: (StoredKey, Q) -> Int): Hit { /* heavy lifting */ }

internal fun <Q : Comparable<Q>> Tree.search(key: Q): Hit
    where StoredKey : Comparable<Q> =
    search(key) { stored, query -> stored.compareTo(query) }
```

The natural-order overload is one-line sugar; the comparator overload
holds the implementation. The canonical implementation lives in
`btree-kotlin`'s `Search.kt::searchTree`/`searchNode`/`findLowerBoundEdge`/
`findUpperBoundEdge` and `Navigate.kt::searchTreeForBifurcation`/
`lowerBound`/`upperBound`.

#### Why this is faithful, not engineering

- Interface mirrors Rust's trait declaration shape exactly.
- Extension's bound mirrors Rust's `where` clause exactly.
- Concrete-class members shadow the extension exactly the way Rust
  inherent-impl methods override a trait default.
- Unbounded callers being unable to invoke the methods mirrors Rust's
  compile-time rejection without the bound.
- No runtime casts, no `IllegalStateException`, no `is Comparable<*>`.

#### When you cannot apply this

When the bound is on a *class* type parameter (e.g. `impl<K: Ord> Map<K, V>`),
Kotlin has no method-level analog — class type parameters bind for the
whole class. Use the `Comparator<in K>` field pattern instead, with a
`compareKeys(a, b)` dispatch helper that prefers the supplied
comparator and falls back to a `Comparable<K>`-based path. The fallback
is the design contract, not a translation hack.

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

### Workflow (No Swarm Tasks)

This repo is **not** using the `ast_distance` swarm/task-assignment system. Do not create or depend on `tasks.json`.

Use `ast_distance` directly for analysis and verification:

```bash
# Analyze overall porting progress
./tools/ast_distance/ast_distance --deep tmp/starlark rust src kotlin

# Check similarity of a specific file
./tools/ast_distance/ast_distance tmp/starlark/src/values/layout.rs rust \
  src/commonMain/kotlin/io/github/kotlinmania/starlark/values/Layout.kt kotlin

# Find missing files ranked by importance (if supported by your ast_distance build)
./tools/ast_distance/ast_distance --missing tmp/starlark rust src kotlin
```

### Tracking Progress

Use the built-in AST distance tool:

```bash
# Analyze overall porting progress
./tools/ast_distance/ast_distance --deep tmp/starlark rust src kotlin

# Check similarity of specific files
./tools/ast_distance/ast_distance tmp/starlark/src/values/layout.rs rust src/commonMain/kotlin/io/github/kotlinmania/starlark/values/Layout.kt kotlin

# Find missing files ranked by importance
./tools/ast_distance/ast_distance --missing tmp/starlark rust src kotlin

# Scan for TODOs
./tools/ast_distance/ast_distance --todos src

# Run lint checks
./tools/ast_distance/ast_distance --lint src
```

**Similarity Targets:**
- `≥ 0.85` — Excellent port (aim for this)
- `0.60–0.85` — Good port, may need refinement
- `< 0.60` — Incomplete, needs more work

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
