# Starlark Kotlin Port

This is a line-by-line transliteration port of [facebook/starlark-rust](https://github.com/facebook/starlark-rust) to Kotlin Multiplatform.

## Structure

- `rust-source/` - Vendored Rust source files for reference (gitignored)
- `src/` - Kotlin port organized to mirror Rust crate structure
- `build.gradle.kts` - Kotlin Multiplatform build configuration

## Porting Guidelines

See parent project `codex-kotlin/AGENTS.md` for complete porting guidelines.

### Key Principles

1. **Line-by-line transliteration** - Port structure, not just functionality
2. **Preserve semantics** - Match Rust behavior exactly
3. **Add port-lint headers** - Every file must have `// port-lint: source <path>`
4. **Document API** - KDoc for public types/functions

## Progress

Use the AST distance tool from codex-kotlin to track progress:

```bash
../codex-kotlin/tools/ast_distance/ast_distance --deep rust-source rust src kotlin
```

## Building

```bash
./gradlew build
```
