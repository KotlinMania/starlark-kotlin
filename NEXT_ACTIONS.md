# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Current Progress:** 100.0% (109/105 files)
- **Matched Files:** 105
- **Average Similarity:** 0.75
- **Critical Issues:** 15 files with <0.60 similarity

## Priority 1: Fix Incomplete High-Dependency Files

### 1. num.value
- **Similarity:** 0.82 (needs 3% improvement)
- **Dependencies:** 69
- **Priority Score:** 12.2
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
./ast_distance --init-tasks ../../tmp/starlark/src/values/types rust ../../src/commonMain/kotlin/io/github/kotlinmania/starlark_kotlin/values/types kotlin tasks.json ../../AGENTS.md

# Get next high-priority task
./ast_distance --assign tasks.json <agent-id>
```
