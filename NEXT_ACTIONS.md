# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Current Progress:** 100.0% (513/468 files)
- **Matched Files:** 468
- **Average Similarity:** 0.48
- **Critical Issues:** 357 files with <0.60 similarity

## Priority 1: Fix Incomplete High-Dependency Files

### 1. layout.heap
- **Similarity:** 0.34 (needs 51% improvement)
- **Dependencies:** 109
- **Priority Score:** 72.0
- **Action:** Deep review - likely missing major functionality

### 2. layout.value
- **Similarity:** 0.69 (needs 16% improvement)
- **Dependencies:** 178
- **Priority Score:** 54.9
- **Action:** Review and complete missing sections

### 3. assert
- **Similarity:** 0.48 (needs 37% improvement)
- **Dependencies:** 84
- **Priority Score:** 43.6
- **Action:** Deep review - likely missing major functionality

### 4. derive.unpack_value
- **Similarity:** 0.45 (needs 40% improvement)
- **Dependencies:** 51
- **Priority Score:** 28.2
- **Action:** Deep review - likely missing major functionality

### 5. debug
- **Similarity:** 0.57 (needs 28% improvement)
- **Dependencies:** 53
- **Priority Score:** 22.8
- **Action:** Deep review - likely missing major functionality

### 6. coerce
- **Similarity:** 0.42 (needs 43% improvement)
- **Dependencies:** 34
- **Priority Score:** 19.7
- **Action:** Deep review - likely missing major functionality

### 7. values.trace
- **Similarity:** 0.66 (needs 19% improvement)
- **Dependencies:** 52
- **Priority Score:** 17.5
- **Action:** Review and complete missing sections

### 8. typing.type_compiled
- **Similarity:** 0.34 (needs 51% improvement)
- **Dependencies:** 22
- **Priority Score:** 14.5
- **Action:** Deep review - likely missing major functionality

### 9. __derive_refs.param_spec
- **Similarity:** 0.36 (needs 49% improvement)
- **Dependencies:** 20
- **Priority Score:** 12.7
- **Action:** Deep review - likely missing major functionality

### 10. values.alloc_value
- **Similarity:** 0.71 (needs 14% improvement)
- **Dependencies:** 42
- **Priority Score:** 12.0
- **Action:** Review and complete missing sections

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
./ast_distance --init-tasks ../../tmp/starlark/src rust ../../src/commonMain/kotlin/io/github/kotlinmania/starlark_kotlin kotlin tasks.json ../../AGENTS.md

# Get next high-priority task
./ast_distance --assign tasks.json <agent-id>
```
