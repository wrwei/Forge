# Coverage (Requirement ↔ Java) — FAILED

## Summary
Coverage check could not run — input artifacts missing.

## Run history
- New this run: 0
- Recurring from previous run: 1
- Resolved since previous run: 0

## Issues
### Issue 1: missing_codegen_trace — result_codegen.json not found [recurring x2]

**Raw**
```
Expected: C:\Users\willr\gitee\formal_method_guided_vibe_coding\java.generated.project\result_codegen.json
```

**Fix directive**
Run /gen-trace to produce result_codegen.json before this phase. The skill consumes the requirements file + Java source and writes the requirement → Java mapping.

## Files to review
- CalcCDyn.java
- CalcCPA.java
- CalcCStc.java
- CalcVel.java
- CheckOPEZ.java
- LreController.java
- LreMode.java

## Next step
Resolve the input issue above (most commonly: run /gen-trace to produce result_codegen.json), then re-run this phase.
