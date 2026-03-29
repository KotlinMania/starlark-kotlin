# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Current Progress:** 100.0% (557/470 files)
- **Matched Files:** 470
- **Average Similarity:** 0.46
- **Critical Issues:** 294 files with <0.60 similarity

## Priority 1: Fix Incomplete High-Dependency Files

### 1. layout.heap
- **Similarity:** 0.18 (needs 67% improvement)
- **Dependencies:** 109
- **Priority Score:** 89.8
- **Action:** Deep review - likely missing major functionality

### 2. assert
- **Similarity:** 0.19 (needs 66% improvement)
- **Dependencies:** 84
- **Priority Score:** 68.0
- **Action:** Deep review - likely missing major functionality

### 3. layout.value
- **Similarity:** 0.71 (needs 14% improvement)
- **Dependencies:** 178
- **Priority Score:** 52.0
- **Action:** Review and complete missing sections

### 4. values.freeze
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 42
- **Priority Score:** 42.0
- **TODOs:** 2
- **Action:** Deep review - likely missing major functionality

### 5. values.trace
- **Similarity:** 0.32 (needs 53% improvement)
- **Dependencies:** 52
- **Priority Score:** 35.6
- **Action:** Deep review - likely missing major functionality

### 6. typing.ty
- **Similarity:** 0.71 (needs 14% improvement)
- **Dependencies:** 109
- **Priority Score:** 31.8
- **Action:** Review and complete missing sections

### 7. typing.starlark_value
- **Similarity:** 0.59 (needs 26% improvement)
- **Dependencies:** 76
- **Priority Score:** 30.8
- **Action:** Deep review - likely missing major functionality

### 8. coerce
- **Similarity:** 0.13 (needs 72% improvement)
- **Dependencies:** 34
- **Priority Score:** 29.4
- **Action:** Deep review - likely missing major functionality

### 9. derive.unpack_value
- **Similarity:** 0.51 (needs 34% improvement)
- **Dependencies:** 51
- **Priority Score:** 25.0
- **Action:** Deep review - likely missing major functionality

### 10. params.display
- **Similarity:** 0.68 (needs 17% improvement)
- **Dependencies:** 76
- **Priority Score:** 24.1
- **Action:** Review and complete missing sections

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
./ast_distance --init-tasks ../../tmp/starlark rust ../../src kotlin tasks.json ../../AGENTS.md

# Get next high-priority task
./ast_distance --assign tasks.json <agent-id>
```
