# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Current Progress:** 98.5% (532/470 files)
- **Matched Files:** 463
- **Average Similarity:** 0.44
- **Critical Issues:** 322 files with <0.60 similarity

## Priority 1: Fix Incomplete High-Dependency Files

### 1. layout.value
- **Similarity:** 0.63 (needs 22% improvement)
- **Dependencies:** 178
- **Priority Score:** 66.1
- **Action:** Review and complete missing sections

### 2. values.trace
- **Similarity:** 0.16 (needs 69% improvement)
- **Dependencies:** 52
- **Priority Score:** 43.7
- **Action:** Deep review - likely missing major functionality

### 3. values.freeze
- **Similarity:** 0.07 (needs 78% improvement)
- **Dependencies:** 42
- **Priority Score:** 39.2
- **Action:** Deep review - likely missing major functionality

### 4. typing.ty
- **Similarity:** 0.69 (needs 16% improvement)
- **Dependencies:** 109
- **Priority Score:** 33.5
- **Action:** Review and complete missing sections

### 5. typing.starlark_value
- **Similarity:** 0.58 (needs 27% improvement)
- **Dependencies:** 76
- **Priority Score:** 31.8
- **Action:** Deep review - likely missing major functionality

### 6. coerce
- **Similarity:** 0.14 (needs 71% improvement)
- **Dependencies:** 34
- **Priority Score:** 29.2
- **Action:** Deep review - likely missing major functionality

### 7. derive.unpack_value
- **Similarity:** 0.51 (needs 34% improvement)
- **Dependencies:** 51
- **Priority Score:** 25.2
- **Action:** Deep review - likely missing major functionality

### 8. params.display
- **Similarity:** 0.68 (needs 17% improvement)
- **Dependencies:** 76
- **Priority Score:** 24.5
- **Action:** Review and complete missing sections

### 9. runtime.evaluator
- **Similarity:** 0.63 (needs 22% improvement)
- **Dependencies:** 56
- **Priority Score:** 20.9
- **Action:** Review and complete missing sections

### 10. layout.freezer
- **Similarity:** 0.49 (needs 36% improvement)
- **Dependencies:** 36
- **Priority Score:** 18.5
- **Action:** Deep review - likely missing major functionality

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

1. **layout.heap** (109 deps)
   - Path: `src/values/layout/heap.rs`
   - Essential for 109 other files

2. **assert** (84 deps)
   - Path: `src/assert.rs`
   - Essential for 84 other files

3. **debug** (53 deps)
   - Path: `src/debug.rs`
   - Essential for 53 other files

4. **typing.type_compiled** (22 deps)
   - Path: `src/values/typing/type_compiled.rs`
   - Essential for 22 other files

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
