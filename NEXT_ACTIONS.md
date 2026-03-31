# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Current Progress:** 100.0% (468/468 source files matched)
- **Target Files (total):** 496 (includes Kotlin-only helpers)
- **Matched Files:** 468
- **Average Similarity:** 0.46
- **Critical Issues:** 284 files with <0.60 similarity

## Priority 1: Fix Incomplete High-Dependency Files

### 1. layout.value
- **Similarity:** 0.51 (needs 34% improvement)
- **Dependencies:** 178
- **Priority Score:** 87.0
- **Action:** Deep review - likely missing major functionality

### 2. values.freeze
- **Similarity:** 0.03 (needs 82% improvement)
- **Dependencies:** 42
- **Priority Score:** 40.7
- **Action:** Deep review - likely missing major functionality

### 3. layout.heap
- **Similarity:** 0.72 (needs 13% improvement)
- **Dependencies:** 109
- **Priority Score:** 30.4
- **Action:** Review and complete missing sections

### 4. assert
- **Similarity:** 0.64 (needs 21% improvement)
- **Dependencies:** 84
- **Priority Score:** 30.3
- **Action:** Review and complete missing sections

### 5. coerce
- **Similarity:** 0.13 (needs 72% improvement)
- **Dependencies:** 34
- **Priority Score:** 29.4
- **Action:** Deep review - likely missing major functionality

### 6. params.display
- **Similarity:** 0.68 (needs 17% improvement)
- **Dependencies:** 76
- **Priority Score:** 24.2
- **Action:** Review and complete missing sections

### 7. typing.starlark_value
- **Similarity:** 0.68 (needs 17% improvement)
- **Dependencies:** 76
- **Priority Score:** 24.0
- **Action:** Review and complete missing sections

### 8. derive.unpack_value
- **Similarity:** 0.54 (needs 31% improvement)
- **Dependencies:** 51
- **Priority Score:** 23.6
- **Action:** Deep review - likely missing major functionality

### 9. typing.ty
- **Similarity:** 0.80 (needs 5% improvement)
- **Dependencies:** 109
- **Priority Score:** 21.5
- **Action:** Minor refinements needed

### 10. values.frozen_ref
- **Similarity:** 0.28 (needs 57% improvement)
- **Dependencies:** 27
- **Priority Score:** 19.4
- **Action:** Deep review - likely missing major functionality

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

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
./ast_distance --init-tasks ../../tmp/starlark/src rust ../../src/commonMain/kotlin/io/github/kotlinmania/starlark_kotlin kotlin tasks.json ../../AGENTS.md

# Get next high-priority task
./ast_distance --assign tasks.json <agent-id>
```
