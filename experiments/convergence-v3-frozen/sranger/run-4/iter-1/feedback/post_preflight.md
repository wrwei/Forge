# 2c — Preflight (Structural Lint) — PASSED

## Summary
2c — Preflight (Structural Lint): no structural errors.

## Run history
- New this run: 2
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: lint_rule8_event_branch_precedes_triggerless — SRangerController.step(): triggerless branch guarded by 'turnDurationElapsed' follows event-triggered branch(es): event instanceof sranger.event.InputEvent.EndTask — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: SRangerController.step(): triggerless branch guarded by 'turnDurationElapsed' follows event-triggered branch(es): event instanceof sranger.event.InputEvent.EndTask — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-frozen-run4-sranger/java.generated.project/src/main/java/sranger/controller/SRangerController.java:69
```

**Java trace**
  - Java: `SRangerController.java`:69-74

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. Trigger disjointness does NOT close this (it only separates two EVENT-triggered branches), and the negation synthesis that closes rule6 cannot either, because the event-triggered branch contributes no data predicate to negate. RoboChart has no event-absence guard, so this must be fixed in the Java: either give the later branch an explicit guard that excludes the earlier branch's case, or place the triggerless branch FIRST so the ordinary priority-negation synthesis applies. Witness: LreController.java CAM block (reqOCM then cdaAboveOrAtMinSafe) — 128 of 8192 LRE abstract states admit two operations where the Java is deterministic.

### Issue 2: lint_rule6_missing_priority_negation — SRangerController.step(): branch guarded by 'event instanceof sranger.event.InputEvent.Tick' does not negate preceding triggerless guard(s): turnDurationElapsed

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: SRangerController.step(): branch guarded by 'event instanceof sranger.event.InputEvent.Tick' does not negate preceding triggerless guard(s): turnDurationElapsed
Location: /Users/ranwei/Gitee/forge-frozen-run4-sranger/java.generated.project/src/main/java/sranger/controller/SRangerController.java:72
```

**Java trace**
  - Java: `SRangerController.java`:72-74

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

## Files to review
(none identified)

## Next step
Proceed to Phase 3 (T2M — Spoon Discovery).
