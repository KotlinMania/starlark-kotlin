# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Current Progress:** 0.0% (495/468 files)
- **Matched Files:** 0
- **Average Similarity:** 0.00
- **Critical Issues:** 0 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

1. **layout.value** (178 deps)
   - Path: `values/layout/value.rs`
   - Essential for 178 other files

2. **typing.ty** (109 deps)
   - Path: `typing/ty.rs`
   - Essential for 109 other files

3. **assert** (84 deps)
   - Path: `assert.rs`
   - Essential for 84 other files

4. **typing.starlark_value** (76 deps)
   - Path: `typing/starlark_value.rs`
   - Essential for 76 other files

5. **params.display** (76 deps)
   - Path: `eval/runtime/params/display.rs`
   - Essential for 76 other files

6. **runtime.evaluator** (55 deps)
   - Path: `eval/runtime/evaluator.rs`
   - Essential for 55 other files

7. **debug** (53 deps)
   - Path: `debug.rs`
   - Essential for 53 other files

8. **values.trace** (52 deps)
   - Path: `values/trace.rs`
   - Essential for 52 other files

9. **derive.unpack_value** (51 deps)
   - Path: `tests/derive/unpack_value.rs`
   - Essential for 51 other files

10. **values.freeze** (42 deps)
   - Path: `values/freeze.rs`
   - Essential for 42 other files

11. **values.alloc_value** (42 deps)
   - Path: `values/alloc_value.rs`
   - Essential for 42 other files

12. **layout.freezer** (36 deps)
   - Path: `values/layout/freezer.rs`
   - Essential for 36 other files

13. **coerce** (34 deps)
   - Path: `coerce.rs`
   - Essential for 34 other files

14. **compiler.span** (29 deps)
   - Path: `eval/compiler/span.rs`
   - Essential for 29 other files

15. **values.frozen_ref** (27 deps)
   - Path: `values/frozen_ref.rs`
   - Essential for 27 other files

16. **none.none_type** (27 deps)
   - Path: `values/types/none/none_type.rs`
   - Essential for 27 other files

17. **runtime.frame_span** (26 deps)
   - Path: `eval/runtime/frame_span.rs`
   - Essential for 26 other files

18. **runtime.arguments** (25 deps)
   - Path: `eval/runtime/arguments.rs`
   - Essential for 25 other files

19. **typing.type_compiled** (22 deps)
   - Path: `values/typing/type_compiled.rs`
   - Essential for 22 other files

20. **util.arc_str** (21 deps)
   - Path: `util/arc_str.rs`
   - Essential for 21 other files

21. **derive.module** (20 deps)
   - Path: `tests/derive/module.rs`
   - Essential for 20 other files

22. **environment.globals** (20 deps)
   - Path: `environment/globals.rs`
   - Essential for 20 other files

23. **util.refcell** (20 deps)
   - Path: `util/refcell.rs`
   - Essential for 20 other files

24. **__derive_refs.param_spec** (20 deps)
   - Path: `__derive_refs/param_spec.rs`
   - Essential for 20 other files

25. **values.value_of_unchecked** (20 deps)
   - Path: `values/value_of_unchecked.rs`
   - Essential for 20 other files

26. **values.error** (17 deps)
   - Path: `values/error.rs`
   - Essential for 17 other files

27. **environment.methods** (17 deps)
   - Path: `environment/methods.rs`
   - Essential for 17 other files

28. **values.iter** (17 deps)
   - Path: `values/iter.rs`
   - Essential for 17 other files

29. **collections.symbol** (15 deps)
   - Path: `collections/symbol.rs`
   - Essential for 15 other files

30. **private** (15 deps)
   - Path: `private.rs`
   - Essential for 15 other files

31. **layout.avalue** (14 deps)
   - Path: `values/layout/avalue.rs`
   - Essential for 14 other files

32. **typing.tuple** (12 deps)
   - Path: `typing/tuple.rs`
   - Essential for 12 other files

33. **layout.value_lifetimeless** (12 deps)
   - Path: `values/layout/value_lifetimeless.rs`
   - Essential for 12 other files

34. **layout.const_frozen_string** (12 deps)
   - Path: `values/layout/const_frozen_string.rs`
   - Essential for 12 other files

35. **types.dict** (12 deps)
   - Path: `values/types/dict.rs`
   - Essential for 12 other files

36. **int.inline_int** (11 deps)
   - Path: `values/types/int/inline_int.rs`
   - Essential for 11 other files

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

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

These files match `reexport_modules` patterns in `.ast_distance_config.json`. They are filtered out of
normal priority and missing-file ladders because they are wiring
modules, not direct logic ports. Consult them for call-site routing;
do not treat them as the next implementation target by default.

### Missing

| Source | Deps | Path |
|--------|------|------|
| `layout.heap` | 109 | `values/layout/heap.rs` |
| `lib` | 0 | `lib.rs` |

