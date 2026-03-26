# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Current Progress:** 100.0% (674/470 files)
- **Matched Files:** 470
- **Average Similarity:** 0.69
- **Critical Issues:** 128 files with <0.60 similarity

## Priority 1: Fix Incomplete High-Dependency Files

### 1. layout.heap
- **Similarity:** 0.50 (needs 35% improvement)
- **Dependencies:** 109
- **Priority Score:** 54.7
- **Action:** Deep review - likely missing major functionality

### 2. assert
- **Similarity:** 0.51 (needs 34% improvement)
- **Dependencies:** 84
- **Priority Score:** 40.8
- **Action:** Deep review - likely missing major functionality

### 3. layout.value
- **Similarity:** 0.82 (needs 3% improvement)
- **Dependencies:** 178
- **Priority Score:** 32.1
- **Action:** Minor refinements needed

### 4. debug
- **Similarity:** 0.52 (needs 33% improvement)
- **Dependencies:** 53
- **Priority Score:** 25.5
- **Action:** Deep review - likely missing major functionality

### 5. derive.unpack_value
- **Similarity:** 0.57 (needs 28% improvement)
- **Dependencies:** 51
- **Priority Score:** 21.8
- **Action:** Deep review - likely missing major functionality

### 6. values.trace
- **Similarity:** 0.72 (needs 13% improvement)
- **Dependencies:** 52
- **Priority Score:** 14.7
- **Action:** Review and complete missing sections

### 7. typing.type_compiled
- **Similarity:** 0.36 (needs 49% improvement)
- **Dependencies:** 22
- **Priority Score:** 14.1
- **Action:** Deep review - likely missing major functionality

### 8. __derive_refs.param_spec
- **Similarity:** 0.36 (needs 49% improvement)
- **Dependencies:** 20
- **Priority Score:** 12.7
- **Action:** Deep review - likely missing major functionality

### 9. typing.starlark_value
- **Similarity:** 0.84 (needs 1% improvement)
- **Dependencies:** 76
- **Priority Score:** 11.9
- **TODOs:** 1
- **Action:** Minor refinements needed

### 10. derive.module
- **Similarity:** 0.46 (needs 39% improvement)
- **Dependencies:** 21
- **Priority Score:** 11.3
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
./ast_distance --init-tasks ../../tmp/starlark rust ../../src kotlin tasks.json ../../AGENTS.md

# Get next high-priority task
./ast_distance --assign tasks.json <agent-id>
```
