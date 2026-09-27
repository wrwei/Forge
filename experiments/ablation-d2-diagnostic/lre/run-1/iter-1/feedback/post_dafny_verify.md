# 6a — Dafny Verification — FAILED

## Summary
Dafny: 1 file(s), 0 verified, 0 errors.

## Run history
- New this run: 1
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: dafny_other — Dafny error: this symbol not expected in Dafny

**Raw**
```
forge.transformations/output/LreController.dfy(14,?): Error: this symbol not expected in Dafny
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

## Files to review
- CalcCDyn.java
- CalcCPA.java
- CalcCStc.java
- CalcVel.java
- CheckOPEZ.java
- LreController.java
- LreMode.java

## Next step
Read each issue above, follow the fix directive, edit the linked Java file, re-run Phase 5b (Dafny Generation), then Phase 6b (Dafny Verification).
