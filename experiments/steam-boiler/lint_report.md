# Steam Boiler — StructuralLinter report (frozen linter, run 2026-09-03)

## Harness

- Linter: `forge.transformations/src/main/java/forge/transformations/preflight/StructuralLinter.java`
  — NOT modified (frozen profile file; hash-pinned by review/steamboiler-profile-freeze.json).
- Invocation: single JVM (JDK 21), a 12-line local driver calling
  `StructuralLinter.lint(Path)` + `writeReport(Path)` exactly as `LintPhase.run`
  does. The Gradle route (`./gradlew run --args="preflight ..."`) was blocked in
  this sandbox: the Gradle daemon/worker needs a local server socket and fails
  with `java.net.SocketException: Operation not permitted` even with
  `--no-daemon` (details in t2m_report.md). The driver bypasses only the
  process launcher, not the linter.
- Classpath (cached, /tmp/steamboiler/forge_cp.txt): jdt/ecj **3.41.0 pinned
  first** (`ecj-3.41.0.jar`, `org.eclipse.jdt.core-3.41.0.jar`), then the
  project's compiled classes (`build/classes/java/main` — prebuilt, untouched),
  `spoon-core-11.3.0.jar`, and Spoon's transitive deps from the Gradle cache.
- Source root: `/tmp/steamboiler/java.generated.project/src/main/java` (14 files).

## Verdict — iteration 1, final

```
Lint: 0 violation(s) (0 error(s), 0 warning(s))
```

lint_report.json: `{"violations": []}` — with 12 top-level types scanned
(the rule0_no_types_found vacuity guard did not fire, so the empty report is a
real pass, not an empty-model artefact).

## Per-rule verdicts

| Rule | Severity class | Verdict on steam boiler |
|------|----------------|-------------------------|
| rule0_no_types_found | error | not fired (12 types scanned) |
| rule1_compute_local_variable | error | not fired — both compute() bodies (CalcThroughput, CalcLevelEstimate) are pure `this.field = expr` chains |
| rule2_step_outer_ne | error | not fired — outer chain is five `currentMode == X` blocks, no `!=` |
| rule3_step_no_named_predicates | warning | not fired — 17 named boolean predicates declared before the chain |
| rule4_double_missing_real_annotation | error | not fired — every double field/parameter carries @RoboChartType("real") |
| rule5_robochart_on_local | error | not fired — no annotation on locals |
| rule6_missing_priority_negation | warning | not fired — every triggerless branch conjoins negations of ALL preceding triggerless guards in its chain (predicted pressure point; discipline applied at generation) |
| rule7_signed_sentinel_on_controller_state | warning | not fired — no negative sentinel on any int state |
| rule8_event_branch_precedes_triggerless | warning | not fired — batch-facade encoding: step() contains no `instanceof` branches at all, so no event-triggered branch precedes a triggerless one |

## Notes

- The prereg predicted rule6/rule8 as the likely iteration pressure. Neither
  fired because the encoding (see encoding_choices.md items 1 and 6) removes
  event-triggered branches from step() entirely and the negation discipline
  was applied at generation time.
- The M2M phase separately emitted its advisory deadlock-lint line:
  `[deadlock-lint] BoilerController.EMERGENCY_STOP: no outgoing transitions
  (Isabelle deadlock_free proof will fail)` — this is the pre-registered
  EXPECTED-FAITHFUL outcome for the terminal emergency-stop mode (prereg item
  1) and is deliberately NOT repaired. It is an M2M advisory, not a
  StructuralLinter rule.
