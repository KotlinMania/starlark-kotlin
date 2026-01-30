# Starlark in Kotlin

[![GitHub link](https://img.shields.io/badge/GitHub-KotlinMania%2Fstarlark--kotlin-blue.svg)](https://github.com/KotlinMania/starlark-kotlin)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.kotlinmania/starlark-kotlin)](https://central.sonatype.com/artifact/io.github.kotlinmania/starlark-kotlin)
[![Build status](https://img.shields.io/github/actions/workflow/status/KotlinMania/starlark-kotlin/ci.yml?branch=main)](https://github.com/KotlinMania/starlark-kotlin/actions)

This is a Kotlin Multiplatform line-by-line transliteration port of Facebook's [starlark-rust](https://github.com/facebook/starlark-rust) implementation.

**Original Project:** This port is based on [facebook/starlark-rust](https://github.com/facebook/starlark-rust), a Rust implementation of the [Starlark language](https://github.com/bazelbuild/starlark/blob/master/spec.md). Starlark (formerly codenamed Skylark) is a deterministic language inspired by Python3, used for configuration in build systems like [Bazel](https://bazel.build), [Buck](https://buck.build), and [Buck2](https://buck2.build).

## About This Port

This Kotlin Multiplatform port maintains the structure and semantics of the original Rust implementation while adapting idiomatically to Kotlin. The original Rust sources are vendored in `tmp/rust-source/` (gitignored) for reference during porting.

### Porting Status

This is an **in-progress port**. The goal is to achieve feature parity with starlark-rust while providing a native Kotlin Multiplatform API.

## Supported Platforms

- **JVM** (Java 21+)
- **Native:** macOS (ARM64, x64), Linux (x64), Windows (mingw-x64)
- **iOS:** ARM64, x64, Simulator ARM64
- **JavaScript:** Browser and Node.js
- **WASM:** WasmJs (Browser and Node.js)
- **Android** (API 24+)

## Installation

Add to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("io.github.kotlinmania:starlark-kotlin:0.1.0-SNAPSHOT")
}
```

## Usage

*Documentation pending as APIs are ported*

## Porting Guidelines

See [AGENTS.md](AGENTS.md) for detailed porting guidelines and conventions.

### Key Principles

1. **Line-by-line transliteration** - Port structure, not just functionality
2. **Preserve semantics** - Match Rust behavior exactly
3. **Add provenance markers** - Every file must have `// port-lint: source <path>`
4. **Document APIs** - KDoc for public types/functions

### Tracking Progress

Use the AST distance tool to track porting progress:

```bash
# From parent codex-kotlin project
tools/ast_distance/ast_distance --deep tmp/rust-source rust src kotlin
```

## Building

```bash
./gradlew build
```

## Testing

```bash
./gradlew test
```

## Contributing

This port is maintained by [Sydney Renee](https://github.com/sydneyrenee) and The Solace Project.

Contributions welcome! Please follow the porting guidelines in [AGENTS.md](AGENTS.md).

## License

This project maintains the Apache 2.0 license of the original starlark-rust implementation.

Original work Copyright (c) Facebook, Inc. and its affiliates.
Kotlin port Copyright (c) 2026 Sydney Renee and The Solace Project.

See [LICENSE](LICENSE) for full details.

## Acknowledgments

This port is based on the excellent work of the starlark-rust team at Facebook. The original project was started by [Damien Martin-Guillerez](https://github.com/damienmg).

## Related Projects

- [facebook/starlark-rust](https://github.com/facebook/starlark-rust) - Original Rust implementation
- [bazelbuild/starlark](https://github.com/bazelbuild/starlark) - Java implementation
- [google/starlark-go](https://github.com/google/starlark-go) - Go implementation
- [Starlark Language Spec](https://github.com/bazelbuild/starlark/blob/master/spec.md)
