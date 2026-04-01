# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Current Progress:** 84.6% (441/468 files)
- **Matched Files:** 396
- **Average Similarity:** 0.42
- **Critical Issues:** 274 files with <0.60 similarity

## Priority 1: Fix Incomplete High-Dependency Files

### 1. layout.heap
- **Similarity:** 0.17 (needs 68% improvement)
- **Dependencies:** 109
- **Priority Score:** 90.5
- **Action:** Deep review - likely missing major functionality

### 2. layout.value
- **Similarity:** 0.51 (needs 34% improvement)
- **Dependencies:** 178
- **Priority Score:** 87.6
- **Action:** Deep review - likely missing major functionality

### 3. values.freeze
- **Similarity:** 0.03 (needs 82% improvement)
- **Dependencies:** 42
- **Priority Score:** 40.8
- **Action:** Deep review - likely missing major functionality

### 4. values.trace
- **Similarity:** 0.38 (needs 47% improvement)
- **Dependencies:** 52
- **Priority Score:** 32.3
- **Action:** Deep review - likely missing major functionality

### 5. assert
- **Similarity:** 0.65 (needs 20% improvement)
- **Dependencies:** 84
- **Priority Score:** 29.8
- **Action:** Review and complete missing sections

### 6. coerce
- **Similarity:** 0.13 (needs 72% improvement)
- **Dependencies:** 34
- **Priority Score:** 29.4
- **Action:** Deep review - likely missing major functionality

### 7. params.display
- **Similarity:** 0.68 (needs 17% improvement)
- **Dependencies:** 76
- **Priority Score:** 24.2
- **Action:** Review and complete missing sections

### 8. typing.starlark_value
- **Similarity:** 0.68 (needs 17% improvement)
- **Dependencies:** 76
- **Priority Score:** 24.0
- **Action:** Review and complete missing sections

### 9. typing.ty
- **Similarity:** 0.80 (needs 5% improvement)
- **Dependencies:** 109
- **Priority Score:** 22.0
- **Action:** Minor refinements needed

### 10. values.frozen_ref
- **Similarity:** 0.30 (needs 55% improvement)
- **Dependencies:** 27
- **Priority Score:** 19.0
- **Action:** Deep review - likely missing major functionality

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

1. **derive.unpack_value** (51 deps)
   - Path: `tests/derive/unpack_value.rs`
   - Essential for 51 other files

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
