# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Current Progress:** 100.0% (392/392 files)
- **Matched Files:** 392
- **Average Similarity:** 0.40
- **Critical Issues:** 380 files with <0.60 similarity

## Priority 1: Fix Incomplete High-Dependency Files

### 1. layout.value
- **Similarity:** 0.34 (needs 51% improvement)
- **Dependencies:** 160
- **Priority Score:** 105.0
- **Action:** Deep review - likely missing major functionality

### 2. typing.ty
- **Similarity:** 0.40 (needs 45% improvement)
- **Dependencies:** 107
- **Priority Score:** 64.5
- **Action:** Deep review - likely missing major functionality

### 3. layout.heap
- **Similarity:** 0.53 (needs 32% improvement)
- **Dependencies:** 100
- **Priority Score:** 46.9
- **Action:** Deep review - likely missing major functionality

### 4. params.display
- **Similarity:** 0.36 (needs 49% improvement)
- **Dependencies:** 68
- **Priority Score:** 43.8
- **Action:** Deep review - likely missing major functionality

### 5. typing.starlark_value
- **Similarity:** 0.36 (needs 49% improvement)
- **Dependencies:** 67
- **Priority Score:** 42.8
- **Action:** Deep review - likely missing major functionality

### 6. runtime.evaluator
- **Similarity:** 0.33 (needs 52% improvement)
- **Dependencies:** 46
- **Priority Score:** 30.9
- **Action:** Deep review - likely missing major functionality

### 7. assert
- **Similarity:** 0.44 (needs 41% improvement)
- **Dependencies:** 53
- **Priority Score:** 29.8
- **Action:** Deep review - likely missing major functionality

### 8. values.trace
- **Similarity:** 0.37 (needs 48% improvement)
- **Dependencies:** 46
- **Priority Score:** 29.1
- **Action:** Deep review - likely missing major functionality

### 9. values.alloc_value
- **Similarity:** 0.34 (needs 51% improvement)
- **Dependencies:** 38
- **Priority Score:** 25.2
- **Action:** Deep review - likely missing major functionality

### 10. coerce
- **Similarity:** 0.32 (needs 53% improvement)
- **Dependencies:** 33
- **Priority Score:** 22.4
- **Action:** Deep review - likely missing major functionality

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

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
./ast_distance --init-tasks ../../tmp/starlark rust ../../src/commonMain/kotlin kotlin tasks.json ../../AGENTS.md

# Get next high-priority task
./ast_distance --assign tasks.json <agent-id>
```
