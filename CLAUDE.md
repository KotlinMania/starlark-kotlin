# Claude Code Project Instructions

## Project Overview

This is **starlark-kotlin**, a line-by-line port of Facebook's starlark-rust to Kotlin Multiplatform. The Rust sources are in `tmp/starlark/` and we're building the Kotlin implementation in `src/`.

## Critical Workflows

### 1. Task Assignment System (MANDATORY)

**DO NOT port files randomly!** Use the task management system:

```bash
# Get your next assignment
./tools/ast_distance/ast_distance --assign tasks.json claude-agent-001

# Complete when done
./tools/ast_distance/ast_distance --complete tasks.json <source_qualified_name>

# Release if blocked
./tools/ast_distance/ast_distance --release tasks.json <source_qualified_name>
```

**⚠️ NEVER run `--init-tasks` - the task file is already initialized!**

### 2. Port-Lint Headers (REQUIRED)

Every Kotlin file MUST start with:

```kotlin
// port-lint: source <path-relative-to-tmp/starlark>
package io.github.kotlinmania.starlark_kotlin.module
```

Example:
```kotlin
// port-lint: source src/values/layout.rs
package io.github.kotlinmania.starlark_kotlin.values
```

This is how `ast_distance` tracks provenance — which Rust file each Kotlin file was translated from. Without this header, the file is invisible to all port analysis tooling. Never remove, move, or alter the header unless the file is being re-targeted to a different Rust source.

### 3. Quality Verification

After porting a file, verify with:

```bash
./tools/ast_distance/ast_distance \
  tmp/starlark/src/values/layout.rs rust \
  src/commonMain/kotlin/io/github/kotlinmania/starlark_kotlin/values/Layout.kt kotlin
```

**Target: Similarity ≥ 0.85** (excellent port)

## Build Commands

```bash
# Full build
./gradlew build

# Run tests
./gradlew test

# Specific platform
./gradlew jvmTest
./gradlew macosArm64Test
```

## Porting Guidelines

See [AGENTS.md](./AGENTS.md) for complete porting patterns.

**Key principles:**
1. **Semantic parity** - Port behavior, not just syntax
2. **Research first** - Don't guess at Rust semantics
3. **Line-by-line** - Maintain file structure
4. **Documentation** - Translate all doc comments to KDoc
5. **No oversimplification** - Replicate formatting logic, ANSI handling, etc.

## STRICT RULES — Translation, Not Engineering

### This is a translation project.

Every Kotlin file is a line-by-line port of a Rust source file in `tmp/starlark/src/`. The `// port-lint: source` header at the top of each `.kt` file tells you which Rust file it came from. That header is how `ast_distance` tracks provenance — never remove or change it.

**When you encounter a compile error, the fix is ALWAYS in the Rust source.** Do not invent solutions to make the Kotlin compiler happy. Do not make classes extend Exception because it "seems right." Do not change visibility, delete code, or add shims. Read the corresponding Rust file and translate faithfully.

### No code stubs. Period.

Do not write stub files, placeholder classes, empty implementations, or skeleton code. Every line of Kotlin must be a faithful translation of the corresponding Rust source. If you can't fully translate a file, don't create it at all — a missing file is better than a stub that will conflict with the real implementation later.

This means:
- **No `class Foo` with an empty body** when the Rust struct has fields and methods
- **No `fun bar() = TODO()`** or `fun bar() { error("not implemented") }`
- **No partial ports** that translate the class declaration but skip the methods
- **No "placeholder until the evaluator is ready"** comments with commented-out code

If a dependency doesn't exist yet, port that dependency first. If the dependency chain is too deep, pick a different file to work on.

### Use ast_distance for all analysis.

The `tools/ast_distance/ast_distance` tool is the single source of truth for:
- `--import-map`: Finding unresolved types, duplicate definitions, and ambiguous imports
- `--symbols-duplicates`: Finding duplicate symbol definitions across files
- `--compiler-fixup`: Suggesting import fixes from gradle error output
- `--symbol-parity`: Comparing Rust vs Kotlin symbol coverage
- `--deep`: Full cross-language AST comparison report

### Do NOT pipe, redirect, or wrap ast_distance output.

The tool detects and rejects stdout piping (`|`), redirection (`>`), and wrappers like `script -q`. Run it directly in the terminal. Read its output directly from the tool result.

### Do NOT create typealias re-export files.

Root-package `.kt` files that re-export types from subpackages via `typealias` cause massive ambiguity errors across the codebase. Types like `Ty`, `Value`, `Freezer` must be imported from their actual defining package, not from a convenience re-export.

### Do NOT create stub/placeholder types.

If a type doesn't exist yet, port the file that defines it. Don't create placeholder classes like `class Ty` or `class CodeMap` in random files — they conflict with real implementations when those files get ported.

## Progress Tracking

```bash
# Overall progress
./tools/ast_distance/ast_distance --deep tmp/starlark rust src kotlin

# Task queue status
./tools/ast_distance/ast_distance --tasks tasks.json

# Missing files by priority
./tools/ast_distance/ast_distance --missing tmp/starlark rust src kotlin
```

## Naming Conventions

- **Files:** PascalCase (e.g., `module.rs` → `Module.kt`)
- **Packages:** Mirror Rust crate structure
- **Functions/Variables:** camelCase (Rust snake_case → Kotlin camelCase)
- **Types:** PascalCase
- **Constants:** UPPER_SNAKE_CASE

## Common Patterns

### Rust → Kotlin Mappings

- `Result<T, E>` → `Result<T>` with exceptions
- `Option<T>` → `T?` (nullable types)
- `Vec<T>` → `MutableList<T>` or `List<T>`
- `HashMap<K, V>` → `MutableMap<K, V>` or `Map<K, V>`
- `Arc<T>` → Appropriate Kotlin concurrency primitives
- Trait → Interface
- Enum with data → Sealed class
- Struct → Data class

### Error Handling

Preserve error messages and context. Use Kotlin's `Result` type or throw appropriate exceptions.

## File Organization

```
src/
├── commonMain/kotlin/io/github/kotlinmania/starlark_kotlin/
│   ├── values/      # Port of tmp/starlark/src/values/
│   ├── eval/        # Port of tmp/starlark/src/eval/
│   ├── typing/      # Port of tmp/starlark/src/typing/
│   └── ...
├── jvmMain/
├── nativeMain/
└── commonTest/
```

## Testing

- Port all Rust tests
- Use `kotlin.test` for multiplatform compatibility
- Maintain test structure and coverage

## Dependencies

Minimal approach:
- kotlinx-coroutines-core
- kotlinx-serialization
- kotlinx-collections-immutable
- kotlinx-datetime

Add new dependencies only when necessary.

## Documentation References

- [AGENTS.md](./AGENTS.md) - Detailed porting patterns
- [PORTING.md](./PORTING.md) - Quick reference workflow
- [README.md](./README.md) - Project overview
- [Starlark Spec](https://github.com/bazelbuild/starlark/blob/master/spec.md)
- [Starlark Rust Docs](https://docs.rs/starlark/)

## Commit Messages

Follow Sydney's style:
- No AI branding or attribution
- Clear, descriptive messages focused on what changed and why
- No "Co-Authored-By" lines
- No emoji or robot references

Example:
```
Add layout module port from Rust

Port layout.rs to Layout.kt with semantic parity. Includes:
- Value allocation and freezing logic
- Heap management structures
- AST similarity: 0.89
```

## When to Ask

Ask the user for clarification if:
- Rust semantics are unclear and documentation doesn't help
- Architectural decisions affect multiple files
- Build configuration needs changes
- You encounter blocking issues
- **You want to add a TODO comment** - get user approval first

## TODO Policy

**DO NOT add TODO comments without explicit user approval.**

If you encounter something that cannot be fully implemented:
1. Ask the user if a TODO is appropriate
2. If approved, use the format: `// TODO: <description>`
3. Better: Ask the user how they want to handle the incomplete functionality
4. Best: Complete the implementation or find an alternative approach

Avoid TODOs by:
- Researching Rust documentation thoroughly
- Looking at similar patterns in the codebase
- Asking the user for guidance on complex Rust idioms
- Using placeholder implementations only when explicitly approved

## Priority Files (Top 10)

1. `layout.value` (160 deps)
2. `typing.ty` (107 deps)
3. `layout.heap` (100 deps)
4. `params.display` (68 deps)
5. `typing.starlark_value` (67 deps)
6. `debug` (53 deps)
7. `assert` (53 deps)
8. `runtime.evaluator` (46 deps)
9. `values.trace` (46 deps)
10. `values.alloc_value` (38 deps)

Start with leaf nodes (low dependency count) and work up to high-dependency modules.
