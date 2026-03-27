# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Current Progress:** 100.0% (524/470 files)
- **Matched Files:** 470
- **Average Similarity:** 0.78
- **Critical Issues:** 32 files with <0.60 similarity

## Priority 1: Fix Incomplete High-Dependency Files

### 1. layout.value
- **Similarity:** 0.82 (needs 3% improvement)
- **Dependencies:** 178
- **Priority Score:** 32.1
- **TODOs:** 2
- **Action:** Minor refinements needed

### 2. derive.unpack_value
- **Similarity:** 0.62 (needs 23% improvement)
- **Dependencies:** 51
- **Priority Score:** 19.6
- **Action:** Review and complete missing sections

### 3. layout.heap
- **Similarity:** 0.83 (needs 2% improvement)
- **Dependencies:** 109
- **Priority Score:** 19.0
- **Action:** Minor refinements needed

### 4. typing.starlark_value
- **Similarity:** 0.84 (needs 1% improvement)
- **Dependencies:** 76
- **Priority Score:** 11.9
- **TODOs:** 4
- **Action:** Minor refinements needed

### 5. values.trace
- **Similarity:** 0.79 (needs 6% improvement)
- **Dependencies:** 52
- **Priority Score:** 11.0
- **TODOs:** 1
- **Action:** Minor refinements needed

### 6. debug
- **Similarity:** 0.79 (needs 6% improvement)
- **Dependencies:** 53
- **Priority Score:** 11.0
- **Action:** Minor refinements needed

### 7. values.alloc_value
- **Similarity:** 0.74 (needs 11% improvement)
- **Dependencies:** 42
- **Priority Score:** 10.8
- **Action:** Review and complete missing sections

### 8. compiler.span
- **Similarity:** 0.77 (needs 8% improvement)
- **Dependencies:** 29
- **Priority Score:** 6.6
- **TODOs:** 1
- **Action:** Minor refinements needed

### 9. private
- **Similarity:** 0.56 (needs 29% improvement)
- **Dependencies:** 15
- **Priority Score:** 6.5
- **Action:** Deep review - likely missing major functionality

### 10. coerce
- **Similarity:** 0.82 (needs 3% improvement)
- **Dependencies:** 34
- **Priority Score:** 6.1
- **Action:** Minor refinements needed

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
