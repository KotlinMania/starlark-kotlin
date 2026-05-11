# Agent guide - starlark-kotlin

This file is the quick-reference operating contract for starlark-kotlin. The longer
project story lives in `CLAUDE.md`, `README.md`, and any repo-local notes. Read
those before editing. This guide captures the workspace-wide porting discipline
that must not drift: Kotlin stays Kotlin, source comments stay Kotlin-facing,
and required port inventory is done with `ast_distance` when the repo ships it.

## What this repo is

starlark-kotlin is a Kotlin Multiplatform port of the upstream Rust crate or module
[`impl Iterator for X`](./README.md). Upstream Rust is the behavioral oracle while the
repo is still in parity mode. Never edit `tmp/` or any fetched upstream source
to make the port easier.

No JVM-only dependencies, no `java.*` / `javax.*`, no shortcuts through
established JVM libraries, and no replacing a Cargo dependency with an unrelated
Kotlin library when a `*-kotlin` sibling port exists or should exist.

## Project phase

Check the repo before choosing a workflow.

- **If `tools/ast_distance/` exists:** the repo is still in parity/porting
  mode. Drift measurement is required, not optional. Use the repo's
  `tools/ast_distance` binary/script to identify missing files, missing
  functions, provenance/header drift, and cheat-detection failures before
  choosing work and again at file or phase boundaries. Do not chase similarity
  scores in the middle of translating a half-read file, and never Rustify
  Kotlin to appease the tool.
- **If `tools/ast_distance/` does not exist:** the repo has matured past the
  structural-port phase and is optimizing for idiomatic Kotlin. Work like a
  Kotlin maintainer: preserve behavior and public API intent, improve Kotlin
  shape when appropriate, and use the repo's tests/docs as the gate. Do not
  reintroduce Rust-shaped code or comments.

## Required workflow in parity mode

1. Read `CLAUDE.md`, `README.md`, this file, and any repo-local status files.
2. Confirm the upstream Rust source is present under the `tmp/` path named by
   `CLAUDE.md` or `.ast_distance_config.json`. Fetch it using the repo's helper
   if needed. Never edit it.
3. If `tools/ast_distance/` exists, run the repo's `ast_distance --deep`
   workflow before picking work. Use it as the required inventory for unported
   files/functions and provenance drift.
4. Pick bottom-up work: dependencies before consumers, leaves before roots.
5. Read the whole upstream `.rs` file before typing. If the file is too large,
   split the turn into "read" and "write"; never start from a half-read file.
6. Keep the mapping one Rust file -> one Kotlin file unless the upstream file is
   pure `mod.rs` re-export glue covered by the `mod.rs` rules below.
7. Translate top-to-bottom in upstream order. Preserve declaration order.
8. Translate comments and docs as content. See "Source comments and KDoc."
9. Leave hard files visible; do not fill holes with stubs.
10. After a file lands, run the relevant compile/test gate and, when available,
    `ast_distance` again.

## Required workflow in mature Kotlin mode

1. Read the repo-local docs and tests first.
2. Make idiomatic Kotlin changes that preserve behavior and public API intent.
3. Remove stale Rust-shaped scaffolding when it is no longer part of the repo's
   Kotlin design.
4. Keep comments Kotlin-facing. Historical Rust notes belong in docs, not source
   comments, unless the repo explicitly keeps a provenance ledger.
5. Run the repo's normal Gradle/test gates.

## Source comments and KDoc

Comments are content. They are part of the port, not decoration.

- Preserve upstream module docs, KDoc-equivalent sections, inline notes, safety
  notes, panic/error docs, and upstream TODO/FIXME items by translating them.
- **No Rust in comments:** KDoc and `//` comments must describe the Kotlin API
  in Kotlin terms. Translate Rust syntax inside comments to Kotlin equivalents:
  `Vec<T>` -> `List<T>`, `Option<&str>` -> `String?`, `Self::foo()` -> `foo()`,
  `snake_case` function names -> `lowerCamelCase`, Rust lifetimes disappear,
  `cfg(test)` / `#[derive(...)]` become prose when relevant.
- **Do not Rustify Kotlin:** this is a translation direction, not a renaming
  scheme. Never rename Kotlin files, packages, functions, locals, parameters,
  or identifiers to `snake_case` to match upstream. Kotlin source stays Kotlin.
- **No porting narratives in source:** do not add comments explaining Kotlin
  workarounds, "Rust vs Kotlin" rationale, ast_distance strategy, or translation
  decisions. Put those in `CLAUDE.md`, `NEXT_ACTIONS.md`, commit messages, or
  review notes.
- Source comments should be upstream comments translated into Kotlin-facing API
  names/signatures, plus required provenance/license headers and required
  migration ledgers such as the `mod.rs` ledger below.
- If `ast_distance` zeros a file because Rust syntax leaked into Kotlin source
  code or comments, treat that as a literal instruction to make the Kotlin
  source Kotlin-native.

## Provenance headers

In parity mode, every Kotlin file translated from a Rust source file starts with
the repo's `port-lint` source header before the package line:

```kotlin
// port-lint: source <relative-path-to-rust-file>
package io.github.kotlinmania.starlark_kotlin.<module>

// Rest of file...
```

Use the path convention from `CLAUDE.md` or `.ast_distance_config.json`. Do not
invent absolute upstream paths. If a repo requires an attribution line after the
`port-lint` header, preserve it exactly.

For files with no single Rust counterpart, use `// port-lint: ignore` only when
repo docs allow it, and add the shortest possible upstream-derived or ledger
note. Do not use ignored files as a place for translation rationale.

## Naming

The translation direction is always Rust -> Kotlin.

| Thing | Kotlin form |
|---|---|
| Files and types | `PascalCase` |
| Functions, properties, parameters, locals | `lowerCamelCase` |
| Interfaces | `PascalCase`, no `I` prefix |
| `const val`, enum entries, true constants | `SCREAMING_SNAKE_CASE` allowed |
| Type parameters | `T`, `K`, `V`, or meaningful `PascalCase` when clearer |
| Packages | lowercase, no underscores, no camelCase |

Examples:
```kotlin
// port-lint: source src/environment/module.rs
package io.github.kotlinmania.starlark_kotlin.environment

// port-lint: source src/values/layout.rs
package io.github.kotlinmania.starlark_kotlin.values

// port-lint: source src/eval/runtime/evaluator.rs
package io.github.kotlinmania.starlark_kotlin.eval.runtime
```

Do not tighten the whole Kotlin interface to `T : Comparable<T>`. Do not make
the method abstract just to satisfy Kotlin. Do not use runtime comparable casts
that turn Rust compile-time bounds into Kotlin runtime crashes.

Translate the default to an extension function whose own type parameter carries
the bound:

```kotlin
interface RangeBounds<T> {
    fun startBound(): Bound<T>
    fun endBound(): Bound<T>
}

fun <T : Comparable<T>> RangeBounds<T>.isEmpty(): Boolean {
    // translated default body
}
```

Concrete implementations that specialize the default provide a same-named
member function without `override`; Kotlin member resolution mirrors Rust's
per-impl specialization of a default method.

When both comparator-aware and natural-order paths are needed, put the heavy
logic in an unbounded overload that takes a comparator explicitly, then add a
bounded one-line natural-order overload:

```kotlin
internal fun <K, Q> search(key: Q, compare: (K, Q) -> Int): Hit {
    // heavy lifting
}

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

### Task Management Workflow (REQUIRED)

**⚠️ IMPORTANT: Use the task system - DO NOT port files randomly!**

The project uses a task assignment system to coordinate parallel porting work and prevent conflicts.

#### Getting Your Next Task

```bash
./tools/ast_distance/ast_distance --assign tasks.json <your-agent-id>
```

This will:
1. Assign you the highest-priority unassigned task
2. Show you the source file path and target path
3. Output complete porting instructions
4. Lock the task to prevent other agents from taking it

#### Completing a Task

After porting a file:
```bash
./tools/ast_distance/ast_distance --complete tasks.json <source_qualified_name>
```

Example:
```bash
./tools/ast_distance/ast_distance --complete tasks.json layout.value
```

#### Releasing a Task (if blocked)

If you cannot complete a task:
```bash
./tools/ast_distance/ast_distance --release tasks.json <source_qualified_name>
```

#### Viewing Task Status

```bash
./tools/ast_distance/ast_distance --tasks tasks.json
```

Shows pending, assigned, and completed tasks with priority rankings.

#### ⚠️ WARNING: Do NOT Re-Initialize Tasks

**NEVER run `--init-tasks` if `tasks.json` already exists!** This will overwrite all task assignments and progress. The task file is already initialized and managed.

### Tracking Progress

Use the built-in AST distance tool:

```bash
# Analyze overall porting progress
./tools/ast_distance/ast_distance --deep tmp/starlark rust src kotlin

# Check similarity of specific files
./tools/ast_distance/ast_distance tmp/starlark/src/values/layout.rs rust src/commonMain/kotlin/io/github/kotlinmania/starlark_kotlin/values/Layout.kt kotlin

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

Approved common dependencies, when the repo already uses or needs them:

- `kotlinx-coroutines-core`
- `kotlinx-serialization-core`
- `kotlinx-serialization-json`
- `kotlinx-collections-immutable`
- `kotlinx-datetime`
- `kotlinx-io`
- `com.ionspin.kotlin:bignum` only when numeric behavior requires it
- `io.github.kotlinmania:*-kotlin` sibling ports for Rust transitive deps

Add a dependency only when stdlib plus approved siblings cannot reproduce the
behavior, and only after confirming it publishes artifacts for every target this
repo ships. If the Rust crate has no KMP equivalent, port that crate instead of
leaving a TODO or using a JVM-only shortcut.

## Forbidden

- Rust syntax leaking into Kotlin code or comments.
- Rustifying Kotlin names, files, packages, or API shape to improve similarity.
- `@Suppress(...)` unless a repo-local doc already records a narrow, reviewed
  invariant that Kotlin cannot encode. New suppressions require discussion.
- `TODO()`, `error("not implemented")`, empty shells, fake implementations, or
  placeholder bodies.
- Re-export `typealias` bridges for upstream `mod.rs` glue.
- `import kotlin.jvm.*`, `java.*`, or `javax.*` from shared/common source.
- JVM-only annotations such as `@JvmName`, `@JvmStatic`, `@JvmField`, or
  `@JvmOverloads` in common code.
- Repo-wide source rewrites with global `sed`/`perl`/`find -exec`. Source edits
  are task-scoped and reviewed.
- Bulk-editing source comments. Comment changes are intentional translation
  work and must be reviewed as such.
- Subagent-driven `.kt` edits. Translation happens in the main loop so mistakes
  are visible immediately.

## Tests and gates

Use the repo's documented Gradle tasks. Common gates include:

```bash
./gradlew test
./gradlew macosArm64Test
./gradlew linuxX64Test
./gradlew jsNodeTest
./gradlew wasmJsNodeTest
```

In parity repos with `tools/ast_distance/`, also run the repo's deep scan, for
example:

```bash
./tools/ast_distance/ast_distance --deep <upstream-root> rust <kotlin-source-root> kotlin
```

The exact paths come from `.ast_distance_config.json`, `CLAUDE.md`, or existing
repo scripts. Use this scan as a progress dashboard for missing files/functions,
header drift, and cheat detection. A file is not done merely because a
similarity score looks good; it is done when the behavior is ported and the
relevant tests pass.

Port tests too. Rust `#[test]` becomes Kotlin `@Test`. Test utilities needed by
ported upstream tests belong in `src/commonTest`, not `commonMain`, unless the
upstream behavior is truly public runtime behavior.

## Scope and commits

- More than about five source files in one change is usually too much; stop and
  ask unless the user explicitly requested a mechanical sweep.
- Commit at file or coherent phase boundaries.
- Commit messages are clear and human: no AI branding, no "Generated with"
  footers, no robot attribution, no `Co-Authored-By` lines unless the human asks.

## When unsure

Read upstream again. Read the repo-local `CLAUDE.md` again. If a construct is
not covered here, add the rule to project docs with the translation you chose.
The goal is not to make Kotlin look like Rust; the goal is to preserve behavior
while moving steadily toward Kotlin that Kotlin developers can maintain.
