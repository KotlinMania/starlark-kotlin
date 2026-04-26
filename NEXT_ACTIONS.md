# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Current Progress:** 81.0% (395/468 files)
- **Matched Files:** 379
- **Average Similarity:** 0.60
- **Critical Issues:** 150 files with <0.60 similarity

## Priority 1: Fix Incomplete High-Dependency Files

### 1. int.inline_int
- **Similarity:** 0.61 (needs 24% improvement)
- **Dependencies:** 11
- **Priority Score:** 199.4
- **Symbol Deficit:** 13 (functions: 10, types: 3)
- **Missing Tests:** 2 of 2 `#[test]` functions have no Kotlin counterpart
- **Action:** Review and complete missing sections

### 2. coerce
- **Similarity:** 0.61 (needs 24% improvement)
- **Dependencies:** 34
- **Priority Score:** 195.1
- **Symbol Deficit:** 11 (functions: 4, types: 7)
- **Missing Tests:** 4 of 4 `#[test]` functions have no Kotlin counterpart
- **Action:** Review and complete missing sections

### 3. values.value_of_unchecked
- **Similarity:** 0.77 (needs 8% improvement)
- **Dependencies:** 20
- **Priority Score:** 164.4
- **Symbol Deficit:** 10 (functions: 6, types: 4)
- **Missing Tests:** 6 of 6 `#[test]` functions have no Kotlin counterpart
- **Action:** Minor refinements needed

### 4. typing.starlark_value
- **Similarity:** 0.72 (needs 13% improvement)
- **Dependencies:** 76
- **Priority Score:** 118.1
- **Symbol Deficit:** 6 (functions: 6, types: 0)
- **Action:** Review and complete missing sections

### 5. environment.globals
- **Similarity:** 0.80 (needs 5% improvement)
- **Dependencies:** 20
- **Priority Score:** 115.6
- **Symbol Deficit:** 7 (functions: 6, types: 1)
- **Missing Tests:** 5 of 5 `#[test]` functions have no Kotlin counterpart
- **Action:** Minor refinements needed

### 6. values.frozen_ref
- **Similarity:** 0.70 (needs 15% improvement)
- **Dependencies:** 27
- **Priority Score:** 88.4
- **Symbol Deficit:** 5 (functions: 3, types: 2)
- **Action:** Review and complete missing sections

### 7. layout.const_frozen_string
- **Similarity:** 0.70 (needs 15% improvement)
- **Dependencies:** 12
- **Priority Score:** 34.1
- **Symbol Deficit:** 2 (functions: 2, types: 0)
- **Missing Tests:** 2 of 2 `#[test]` functions have no Kotlin counterpart
- **Action:** Review and complete missing sections

### 8. util.refcell
- **Similarity:** 0.52 (needs 33% improvement)
- **Dependencies:** 20
- **Priority Score:** 23.3
- **Symbol Deficit:** 1 (functions: 1, types: 0)
- **Missing Tests:** 1 of 1 `#[test]` functions have no Kotlin counterpart
- **Action:** Deep review - likely missing major functionality

### 9. runtime.frame_span
- **Similarity:** 0.60 (needs 25% improvement)
- **Dependencies:** 26
- **Priority Score:** 23.1
- **Symbol Deficit:** 1 (functions: 1, types: 0)
- **Action:** Review and complete missing sections

### 10. values.error
- **Similarity:** 0.50 (needs 35% improvement)
- **Dependencies:** 17
- **Priority Score:** 23.0
- **Symbol Deficit:** 1 (functions: 1, types: 0)
- **Action:** Deep review - likely missing major functionality

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

1. **assert** (84 deps)
   - Path: `assert.rs`
   - Essential for 84 other files

2. **debug** (53 deps)
   - Path: `debug.rs`
   - Essential for 53 other files

3. **derive.unpack_value** (51 deps)
   - Path: `tests/derive/unpack_value.rs`
   - Essential for 51 other files

4. **types.dict** (12 deps)
   - Path: `values/types/dict.rs`
   - Essential for 12 other files

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

## Next Commands

```bash
# Initialize task queue for systematic porting
cd tools/ast_distance
./ast_distance --init-tasks ../../tmp/starlark/src rust ../../src/commonMain/kotlin/io/github/kotlinmania/starlark kotlin tasks.json ../../AGENTS.md

# Get next high-priority task
./ast_distance --assign tasks.json <agent-id>
```
## Reexport / Wiring Modules

These files match patterns in `ast_distance.config.json` (`reexport_modules`) and are excluded from the priority and
missing ladders above. They are typically declarations-only
(`pub mod foo; pub use bar::*;`) and should NOT be transliterated
directly. **Consult them when porting** — they tell you which
submodule a caller actually resolves to in the Rust source.

### Untracked (no matching Kotlin file)

| Source | Deps | Path |
|--------|------|------|
| `layout.heap` | 109 | `values/layout/heap.rs` |

