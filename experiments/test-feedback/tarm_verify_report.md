# T-arm verification measurement — report (R2-3b protocol §3 step 2)

**Date:** 2026-09-08. **One-shot measurement: one pass, no repair, no feedback.**

## Question and answer

Pre-registered question: do the three T-arm terminal trees (tests-green +
static-clean SRanger controllers) VERIFY when pushed through the formal
pipeline?

**Answer: none of the three reached a verifier. All three were rejected by the
pipeline's phase-1 structural linter (preflight), same rule, same construct
class.** Per protocol, each run's pipeline stopped at the rejection; no model
was extracted and no Dafny/CSP/Isabelle artefact was generated for any run.

| run | lint verdict (verbatim rule) | offending construct | extraction | Dafny |
|---|---|---|---|---|
| run-1 | `rule4_double_missing_real_annotation` (error) | `Sensor.DEFAULT_DISTANCE` — private static final double, no `@RoboChartType("real")` (`Sensor.java:12`) | not run | not run |
| run-2 | `rule4_double_missing_real_annotation` (error) | `IrDistanceSensor.DEFAULT_DISTANCE` — same shape (`IrDistanceSensor.java:10`) | not run | not run |
| run-3 | `rule4_double_missing_real_annotation` (error) | `Sensor.DEFAULT_DISTANCE` — same shape (`Sensor.java:10`) | not run | not run |

Verbatim linter output (identical across runs modulo file/line), run-1:

```
{"rule": "rule4_double_missing_real_annotation", "severity": "error",
 "message": "field 'Sensor.DEFAULT_DISTANCE' is a double without @RoboChartType(\"real\")",
 "directive": "Annotate with @RoboChartType(\"real\"). Without the annotation, the formal
   model extraction cannot confidently map Java double to RoboChart real and may produce
   an incorrect model type. See java_codegen_rules.txt.",
 "file": ".../run-1/src/com/sranger/sensor/Sensor.java", "line_start": 12}
```

Each run had exactly ONE violation — this was the only thing between every
tree and extraction.

## Extractor used

- **Snapshot:** `/tmp/campaign/prefix-snapshot` (committed state 26fe49fb,
  WITHOUT the uncommitted T2M/M2M repairs), re-materialized from the durable
  artifact `campaign_evidence.tar.gz` exactly as its README prescribes.
- **Compiled classes:** `/tmp/campaign/classes` as shipped in the evidence
  tarball (built 2026-08-28 from the snapshot). Freshness verified: no
  snapshot `.java` is newer than the compiled `.class` files, and the
  resource files under `classes/{transformations,metamodels}` are
  byte-identical to the snapshot's `src/main/resources`. **No rebuild was
  needed; the working tree was never used.**
- **Runtime classpath:** the campaign's recorded `classpath.txt`; 80 of 652
  jar paths had gone stale (Gradle cache hash-dir churn) and were re-resolved
  by filename against `~/.gradle` and the repo's `.gradle-home`. All
  extraction-critical jars resolved to the same versions (spoon-core-11.3.0,
  Epsilon 2.8.0, EMF). The 80 unresolved names are Spring/testcontainers/
  docker-java — not used by the extraction phases.
- **Driver:** `TarmVerifyDriver.java` (included in the tarball) — the
  campaign's `CampaignDriver` phase order (T2M → M2M → Dafny/RCT/Isabelle)
  with the pipeline's `LintPhase` run FIRST, mirroring `pipeline.yaml`
  (preflight → t2m → m2m → m2t), gated exactly as
  `bridge._count_lint_errors` gates it: errors fail, warnings pass.
- **Sanity check (extractor equivalence):** the driver was first run on the
  reference tree `reference-runs/sranger/java`. Lint: 0 violations.
  `SRangerController.dfy`, `robochart_controller.rct`,
  `constant_defaults.json` reproduced **byte-identically** to the campaign's
  own `baseline/sranger`. `SRangerController_Beh.thy` differs only in the
  ordering of `triggers'` set literals (e.g. `{endTask, tick, obstacle}` vs
  `{endTask, obstacle, tick}`) — a known JVM `HashSet` iteration-order
  nondeterminism, semantically identical as a set. The `baseline-check/`
  outputs are in the tarball.

## Staging and seam reverts (full log)

Final-iteration sources (`iter-2` in all three runs) copied out of
`/tmp/tarm/runs/run-{1,2,3}/iter-2/java.generated.project/src/main/java`.
The trees keep their own base package `com.sranger.*` — the extractor has no
package expectations (Spoon discovers whatever is under the source root, the
ETL matches by simple names), so **no package renames were needed**.

The three harness seams were grafted into the trees BEFORE iteration 1 (no
pre-seam originals exist), so they were reverted mechanically to the
reference shapes; the controller/TurnTimer/etc. remain exactly as generated.
Every reverted file:

1. `run-1/src/com/sranger/actuator/Actuator.java` — removed observer seam
   (`MoveObserver` interface, `observer` field, `setObserver`, forward call
   in `receive`).
2. `run-1/src/com/sranger/constants/SRangerConstants.java` — removed
   `harnessReset`/`harnessConfigure`; re-finalised `MOVE_VEL`, `TURN_VEL`,
   `OBSTACLE_THRESHOLD`, `TURN_DURATION` (`static double` →
   `static final double`).
3. `run-2/src/com/sranger/actuator/DifferentialDriveActuator.java` — removed
   observer seam (as above).
4. `run-2/src/com/sranger/controller/SRangerConstants.java` — removed harness
   methods; re-finalised the four constants.
5. `run-2/src/com/sranger/controller/SRangerController.java` — removed
   `harnessFrozenSeconds` / `harnessSetTimeSeconds` / `harnessClearTime`;
   `currentTimeSeconds()` restored to a plain
   `System.nanoTime()/1e9` read (this run declared no Clock class; the seam
   was provision-only, comparison logic untouched).
6. `run-3/src/com/sranger/actuator/Actuator.java` — removed observer seam.
7. `run-3/src/com/sranger/controller/SRangerConstants.java` — removed harness
   methods; re-finalised the four constants.

Run-3's Clock needed no revert: the tree declares a `Clock` interface with an
injectable `SystemClock` implementation, and the deterministic clock lived in
the adapter, not the SUT. After reverting, zero `HARNESS`/`harness`/
`observer` references remain in any staged tree (grep-verified). No semantic
edits of any kind were made to generated code.

## Why every tree failed the same rule

The reference implementation carries its default-distance sentinel as an
**annotated public constant**
(`@RoboChartType("real") public static final double NO_READING_DEFAULT`).
All three generated trees independently invented the same construct as an
**unannotated private constant**
(`private static final double DEFAULT_DISTANCE = 1000.0;`) — idiomatic Java
that every compiler, test suite, and SpotBugs pass accepts. Rule 4 requires
the annotation on EVERY double field. The T-arm's feedback channels
(tests + static analysis) carry no signal about this rule, so the trees are
tests-green and static-clean yet phase-1-rejected — which is precisely the
gap the T-arm/V-arm comparison is designed to expose.

Note on extractor version sensitivity: the rule-4 check is **byte-identical**
between the pre-fix snapshot and the fixed working tree (the fixes touched
rule 2's NE-kind handling, rule 6, rule 7, and an empty-model guard). The
verdict is therefore not an artefact of using the pre-fix extractor.

## Held-out-style constructs (never seen by the pipeline; untested because extraction stopped)

Had the trees passed preflight, they contain constructs the extraction
pipeline has not seen in any campaign study; recorded here for completeness:

- **Interface-typed collaborators** — run-1: `ActuatorPort`, `ClockPort`
  interfaces implemented by `Actuator`/`Clock`; run-2: `Sensor` and
  `Actuator` as interfaces with `IrDistanceSensor` /
  `DifferentialDriveActuator` implementations; run-3: `Clock` interface with
  a `SystemClock` implementation. The ETL's dependency matching operates on
  concrete simple names (`Sensor`, `Actuator`, `Clock`); whether an
  interface+impl split maps correctly is untested.
- **Per-event record/class hierarchies** — run-1: separate `Move`,
  `TickEvent`, `ObstacleEvent`, `EndTaskEvent` types alongside
  `InputEvent`/`OutputEvent`; run-3 similar (`MoveEvent`, etc.). The
  reference nests these as members of `InputEvent`/`OutputEvent`.
- **run-2's inline time read** — no Clock type at all; the controller reads
  time directly inside `step()` via a private helper. The M2M clock-pattern
  matcher expects `clock.nowMs()`-style calls on a Clock-typed dependency;
  a self-call time source is a new shape.

## What was and was not run

- **Run:** seam revert + staging (3 trees); pipeline preflight structural
  lint (pre-fix snapshot classes, in-JVM); nothing further, per protocol.
- **Not run (because preflight rejected):** T2M, M2M, Dafny generation, RCT
  generation, CSP generation, Isabelle theory generation; Dafny verification;
  FDR4; Isabelle. No `.dfy`, `.rct`, `.csp`, or `.thy` exists for any run.
- **Not done:** no generated file was edited to appease the linter; no
  package renames (not needed); no second pass.
- `run_tarm_verifiers.sh` is the pre-committed verifier battery in the style
  of `run_sb_verifiers.sh` (licence pre-flight, loud empty-verdict guard,
  orphan-container sweep, 600 s per-goal timeouts). Against the current
  measurement it audits artefact presence, reports the recorded phase-1
  rejections loudly, and exits — it will verify unmodified artefacts if a
  future re-measurement produces them.

## Deliverables

- `tarm_verify_summary.csv` — one row per run.
- `tarm_formal_artefacts.tar.gz` — per run: staged+reverted sources,
  `lint_report.json` (the sole pipeline artefact each run produced), driver
  stdout/stderr; plus `seam_revert_log.txt`, `TarmVerifyDriver.java`, and the
  `baseline-check/` extractor-equivalence outputs.
- `run_tarm_verifiers.sh` — verifier battery (see above).
- This report.

## Sensitivity: annotated variant (experimenter intervention, outside the T-arm loop)

**Status of this section: DISCLOSED SENSITIVITY VARIANT, clearly separated from
the one-shot measurement above. The T-arm loop never saw this fix — no test,
no SpotBugs signal, no feedback channel could have produced it. The variant
measures the DEPTH of the admission failure (what lies behind the phase-1
gate), not the T-arm's capability.**

### Intervention (full diff, one added line per tree)

The minimal mechanical fix the lint directive itself prescribes:
`@RoboChartType("real")` added to the single flagged field. The import was
already present in all three files; nothing else was touched. Verbatim diffs
in `variant_diffs.txt`; each is exactly one inserted line, e.g. run-1:

```diff
     /** Large default distance so missing data never falsely triggers obstacle detection. */
+    @RoboChartType("real")
     private static final double DEFAULT_DISTANCE = 1000.0;
```

### Variant results

| run | lint | extraction | Dafny (local dafny 4.11.0 + z3) | CSP generation | Isabelle theory |
|---|---|---|---|---|---|
| run-1 | clean (0/0) | completed | **4 verified, 1 error** | **REJECTED by official generator** (empty RecordTypes) | emitted (not built) |
| run-2 | clean (0/0) | completed | **ill-formed: 2 resolution errors** (no verification attempted by Dafny) | OK; corrections applied; `_nodet` written | emitted (not built) |
| run-3 | clean (0/0) | completed | **4 verified, 1 error** | **REJECTED by official generator** (empty RecordTypes) | emitted (not built) |

The single rule-4 violation was indeed the only phase-1 blocker: lint came
back clean on all three variants (each additional violation would itself have
been data; there were none). But **no variant tree verifies end-to-end** —
behind the admission gate sit three distinct, deeper failures:

1. **run-1 / run-3 — Dafny obligation failure (semantic finding).**
   `transitionFromTURNING` fails its generated postcondition
   `ensures now() - clockResetTime >= 2.0 ==> mode == MOVING`
   (run-3: `currentTime()`). Witness: the generated Java (and hence the
   extracted Dafny body) checks `EndTask` BEFORE the timeout branch, so when
   the turn duration has elapsed and an `EndTask` arrives, the controller
   goes to FINAL while the extracted spec demands MOVING. The reference
   implementation orders the timeout branch first and verifies 5/0 under the
   identical local setup (see `baseline-check/dafny_baseline.log`). Whether
   the defect is in the trees' branch order or in the extractor's
   obligation synthesis (priority semantics of else-if chains) is exactly
   the class of question RQ6's rule-6 lint fix addresses — but through the
   PRE-FIX extractor used here, the obligation fails.
2. **run-2 — ill-formed Dafny program (extractor held-out construct).**
   `member 'currentTimeSeconds' does not exist in class 'SRangerController'`
   (dfy:64, 71). Run-2 declares no Clock class and reads time via a private
   Java helper; the extractor pulled `this.currentTimeSeconds()` into the
   spec and body verbatim but never generated the member. Dafny performs no
   verification on an unresolvable program. This is the inline-time-read
   held-out construct identified in the one-shot section, now with a
   concrete downstream consequence.
3. **run-1 / run-3 — CSP generation rejected (extractor held-out construct).**
   The official RoboChart CSP generator refuses the emitted `.rct`:
   `The feature 'fields' of RecordTypeImpl ... with 0 values must have at
   least 1 values` (3 occurrences each). Cause: the trees' per-event class
   hierarchies (`EndTaskEvent {}`, `TickEvent {}`, `ObstacleEvent {}` with
   no fields) are mapped by the M2M to field-less RoboChart record types,
   which the RoboChart metamodel forbids. No CSP exists for these runs, so
   FDR4 has nothing to check. Logs: `run-{1,3}/cspgen_variant.log`.

run-2's CSP tree was produced, corrected via the pipeline's own mechanism
(`apply_corrections_standalone.py`: `instantiations.csp` and
`timed/instantiations.csp` corrected), and given the standard
`_nodet` sibling (1 determinism assertion commented out). FDR4 and Isabelle
were NOT run here (per the original brief); `run_tarm_verifiers.sh` gained a
clearly-labeled VARIANT section (`TARM_VARIANT=1`) covering variant Dafny,
run-2 FDR4, and the three emitted Isabelle theories, with the same loud
guards as the main battery.

### Reading

One annotation line converts "rejected at the door" into "admitted, then
fails three different ways deeper in the pipeline" — one semantic proof
failure appearing in 2/3 trees, and two extractor blind spots triggered by
constructs the generation loop invented freely (interface splits, per-event
event-class hierarchies, inline time reads) because nothing in the T-arm's
feedback channels constrains them. The variant therefore strengthens, not
weakens, the headline: the phase-1 rejection was the SHALLOWEST of several
independent barriers between tests-green code and a verified model.

### What was and was not run (variant)

- Run: lint, T2M, M2M, Dafny gen, RCT gen, Isabelle gen (all three);
  local Dafny verification (all three; z3 via `--solver-path`); CSP
  generation attempt (all three, official generator, vendored jars);
  corrections + `_nodet` (run-2, the only run with CSP).
- Not run: FDR4, Isabelle builds (scripted, not executed — same as the
  steam-boiler protocol).
- Not done: no edit beyond the one annotation line per tree; runs 1/3's
  empty-record rejection and run-2's resolution errors were NOT patched —
  they are results.


## Variant verifier battery — user-machine run, 9 September 2026 (parent-verified from logs)

All three variant (annotated) trees, FDR4 + Isabelle columns:

| run | Dafny (recorded) | FDR4 | Isabelle |
|---|---|---|---|
| 1 | 4 verified / 1 error (branch-order obligation) | no CSP — csp-gen rejected (empty RecordTypes) | **build error**: `record EndTaskEvent =` — the same field-less event construct emits an EMPTY Isabelle record, outer-syntax error at SRangerController_Beh.thy:46 |
| 2 | ill-formed (2 resolution errors) | **3 passed / 0 failed** (coreassertions_nodet, 1s) | **timeout** at the 600s per-goal budget (exit 142, 623s) — no verdict, per the session rule an unfinished proof is not evidence |
| 3 | 4 verified / 1 error (branch-order obligation) | no CSP — csp-gen rejected | **build error**: same empty-record outer-syntax error (thy:46) |

Reading:
- The field-less per-event classes (held-out construct) now have a FOURTH
  manifestation: they break the Isabelle theory generator too (`record X =`
  with no fields is invalid outer syntax), not just the CSP generator. One
  generation idiom, two backend generators rejected it independently.
- Run-2's FDR4 pass (3/0) is a pass over the standard narrowed model of the
  one tree whose CSP existed; it does not alter run-2's overall verdict, which
  is already NEGATIVE at the Dafny layer (ill-formed program — nothing was
  verified). No variant tree verifies end-to-end on any backend combination.
- The verdict matrix is now fully populated: 3/3 one-shot rejections at
  phase 1; behind the disclosed one-line fix, every tree fails at least one
  backend for a distinct, named reason, and no backend chain completes.

Orphan sweep ran clean before the Isabelle builds; the clean-build flag (-c)
prevented heap-image reuse across the three same-named sessions.
