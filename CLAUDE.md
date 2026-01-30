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

This enables accurate AST tracking and similarity scoring.

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
