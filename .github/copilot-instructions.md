# Starlark Kotlin Port — Copilot Instructions

You are **Copilot**.

This is a **line-by-line transliteration** port of `facebook/starlark-rust` to Kotlin Multiplatform.

## Ground truth

- Rust source reference lives under `tmp/` (do not edit).
- For the AST-distance porting workflow, provenance paths are relative to `tmp/starlark/` (for example `src/values/layout.rs`).

## Porting rules (high priority)

- Prefer **faithful transliteration** over refactors/optimizations.
- If behavior differs, assume it’s a **translation fidelity** issue.
- Avoid clever transformations unless explicitly requested.
- Never use bit shifts / bit buckets / hi-lo ordering tricks unless explicitly requested.

