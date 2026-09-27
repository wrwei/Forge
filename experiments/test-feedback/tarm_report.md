# R2-3b — T-arm (test + static-analysis feedback) run report

**Date:** 2026-09-08 · **Protocol:** review/r2-3b-feedback-arm-protocol.md (Sections 3-5)
**Arm:** T — the iteration loop receives ONLY JUnit test output and SpotBugs
static-analysis findings. No verifier (Dafny/FDR4/Isabelle) was invoked at any
point; the one-shot verification of the terminal artefacts is a separate,
subsequent step performed by the parent.

## Headline result

All three runs reached the T-arm terminal condition (35/35 tests pass AND
SpotBugs clean at the recorded threshold) at **iteration 2 of the 5-iteration
budget**:

| run | iter 1 | iter 2 | terminal state |
|---|---|---|---|
| 1 | 35/35 pass, 3 SpotBugs instances above floor | 35/35 pass, 0 above floor | tests-green+clean (iter 2) |
| 2 | 35/35 pass, 4 above floor | 35/35 pass, 0 above floor | tests-green+clean (iter 2) |
| 3 | 35/35 pass, 3 above floor | 35/35 pass, 0 above floor | tests-green+clean (iter 2) |

Per-iteration counts: tarm_runs_summary.csv. In every run the initial
generation already passed the full frozen suite; the residual iteration work
was entirely static-analysis driven (EI_EXPOSE_REP2 / UCF_USELESS_CONTROL_FLOW
/ MS_PKGPROTECT on the generated controller and constants classes).

**These artefacts now await the pre-registered one-shot verifier measurement**
(protocol Section 3): tests-green + analyser-clean does not by itself say
anything about whether the artefacts verify.

## Fixed instruments

- **JUnit suite** (byte-frozen, coauthor-authored, blindness-attested):
  `SrangerRequirementsTest.java` + `SrangerTestAdapter.java`, SHA-256 verified
  against the intake SHA256SUMS before the runs
  (d40af275… / 0ddf3901… — both MATCH). Baseline gate re-confirmed in this
  session: 35/35 against the reference classes.
- **Generator prompt bundle** (frozen): forge.assets/prompts/java_codegen_rules.txt
  + codegen_trace_rules.txt + forge.assets/case-studies/sranger/requirements/
  requirement_all.txt, assembled the same way for all three runs; repair
  prompts = the same rules + the iteration's feedback files + current source.
  Model: claude-sonnet-5 (host reasoning default) for every generation and
  repair call.
- **SpotBugs 4.8.6**, invocation FIXED:
  `java -jar spotbugs.jar -textui -effort:default -low -xml:withMessages=<out> <generated SUT classes>`
  — default ruleset, generated `sranger`/`com.sranger` classes only,
  experiment/** (suite, adapter, harness runner) excluded.

## SpotBugs noise-floor comparison method

Reference floor: the reference sranger classes (with the three frozen harness
seams applied) produce **13 instances** at this exact invocation:
EI_EXPOSE_REP2 x2, MS_CANNOT_BE_FINAL x4, MS_PKGPROTECT x1,
PA_PUBLIC_PRIMITIVE_ATTRIBUTE x5, URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD x1
(11 of 13 induced by the de-finalised-constants and observer seams).

"Analyser clean" = **no instances above the reference set**, compared as a
multiset keyed on (bug type, simple class name) — package stripped, inner
classes folded — NOT raw counts. Rationale: the generated trees carry the same
frozen seams under whatever base package the generator chose (com.sranger.*);
keying on fully-qualified names would count every seam-induced instance as new
and the loop would instruct the LLM to undo the frozen seams.
new_instances = Σ max(0, generated_count − reference_count) per key.
Comparison script shipped: spotbugs/spotbugs_compare.py; reference multiset:
spotbugs/reference_multiset.json.

## Feedback envelope (protocol Section 5)

Per-iteration `iter-N/feedback/` bundles: `post_tests.md` and `post_static.md`
(plus `post_compile.md` when javac fails), matching the verifier arm's
post_<phase>.md layout exactly: verdict line (`# <label> — PASSED|FAILED`),
`## Summary`, `## Run history` (new / recurring from previous / resolved, with
the thrashing warning at 3+), `## Issues` with per-issue **Raw** (assertion
message + SUT stack frames, or the SpotBugs LongMessage), **Java trace**
(file:line, element, requirement IDs), **Fix directive**, then
`## Files to review` and `## Next step`.

- post_tests.md: failing-test display-name chain, assertion message, stack
  frames filtered to SUT (`sranger.*`/`com.sranger.*`) code, requirement IDs
  parsed from the test's @DisplayName (SR-…) and joined with the generated
  tree's own result_codegen.json traceability.
- post_static.md: SpotBugs bug type, LongMessage, class/method/line,
  requirement IDs via result_codegen.json for the flagged file.

## Integrity check — zero hits in all feedback ever sent

Before **every** repair call, the entire feedback bundle was grep-scanned
(case-insensitive) for verifier vocabulary:
`Dafny|Isabelle|FDR|CSP|RoboChart|zoperation|lemma|obligation|counterexample|refinement|deadlock|\.thy|\.dfy|\.csp`.
A final re-scan of every feedback directory after the runs confirms:
**6 bundles scanned (3 runs × 2 iterations), 0 hits total.** Logs:
`run-N/integrity_check.log` (per-gate) and `final_integrity_scan.log`
(post-hoc full scan) — both inside the run tars.

## Suite coverage note

Per the intake COVERAGE.md (coauthor's attested table): **20 of 23
requirements tested** at the adapter interface; SR-DM5, SR-DM6 and SR-DC1
declared not-testable-at-interface with reasons (sensor-default provisioning
and actuator storage are behind the boundary; transition-uniqueness is a
structural property). 16 test methods / 35 test cases. The arm's strength is
conditional on this suite (protocol Section 7).

## Per-run narrative

**Run 1.** Initial generation (com.sranger.*, separate event records,
concrete Clock class with setTime — natively deterministic). Iter 1: compiles,
35/35 tests pass, 3 SpotBugs instances above floor (EI_EXPOSE_REP2 ×2 on the
controller constructor storing externally mutable Actuator/Clock;
UCF_USELESS_CONTROL_FLOW on an empty tick branch). Repair introduced
ActuatorPort/ClockPort interfaces (EI detectors do not fire on interface-typed
fields) and removed the empty branch. Iter 2: 35/35, 0 above floor — terminal.

**Run 2.** Initial generation (Sensor/Actuator as interfaces with
IrDistanceSensor/DifferentialDriveActuator implementations; no Clock class —
wall-clock read inline in the controller). Iter 1: 35/35, 4 above floor
(MS_PKGPROTECT ×3 on the de-finalised constants — the tree put the constants
class in the controller's own package, so SpotBugs recommends package-private;
UCF_USELESS_CONTROL_FLOW ×1). Repair reduced constants visibility to
package-private and removed the empty branches. Iter 2: 35/35, 0 — terminal.

**Run 3.** Initial generation (Clock as an interface + SystemClock impl —
the adapter supplies a deterministic implementation, so no clock seam graft
was needed). Iter 1: 35/35, 3 above floor (MS_PKGPROTECT ×3, same cause as
run 2). Repair made the constants package-private. Iter 2: 35/35, 0 — terminal.

## Instrumented-environment seams (kept in every tree; logged per run)

1. **Actuator observer** — forwards each received Move to the test adapter
   (duplicates/zeros/order preserved); storage behaviour unchanged.
2. **De-finalised constants + harnessReset/harnessConfigure** — lets the
   adapter supply the suite's Config record and restore SUT defaults.
3. **Deterministic clock** — run 1: the tree's Clock.setTime was already
   deterministic (no modification); run 2: frozen-time seam at the tree's
   single wall-clock read point (it declared no Clock abstraction);
   run 3: the adapter implements the tree's own Clock interface (no SUT file
   modified).

All seam code is marked `HARNESS INSTRUMENTATION` in-tree; the repair prompt
instructs the model to preserve those blocks, and seam presence was re-verified
after every repair.

## Deviations

1. **SrangerAdapterImpl per run** (allowed by the task: "if a generated
   controller's constructor differs, adapt ONLY SrangerAdapterImpl"): each run
   got its own adapter binding the frozen contract to that run's API
   (constructor arity, event types, accessor names). Run-1's first adapter
   draft failed to compile (member-type shadowing of Sensor/Actuator simple
   names); fixed harness-side with fully-qualified names. Adapter compile
   errors were never sent to the LLM — they are harness defects, not SUT
   feedback. Notes: run-N/adapter_notes.md.
2. **SpotBugs XML flag form**: the first trial used
   `-xml:withMessages -output <path>` (two-argument form), which silently
   dropped the ShortMessage/LongMessage elements. The invocation was corrected
   to the one-argument form `-xml:withMessages=<path>`, the noise floor
   (13 instances, identical multiset) was re-derived with it, and run-1
   iteration 1 was re-evaluated with it — every feedback file shipped in the
   run tars was produced by the one-argument form. NOTE: the first published
   tarm_driver.py artifact still contained the superseded two-argument
   run_spotbugs (the correction was applied in the live kernel but the file
   snapshot was not synchronized); the artifact has been updated (v2) to the
   executed one-argument version, along with the executed compile_tree
   (which excludes the generated trees' self-authored src/test/**).
3. **Comparator keys on simple class names**, documented above — a protocol
   interpretation ("compare by bug type+class"), recorded because the naive
   FQCN reading would have made the terminal condition unreachable without
   modifying frozen seams.
4. **Repair-call transport/token-budget retries** (run-1 iteration-2 repair
   only; prompt and feedback content identical across all attempts): two
   transport timeouts at the original 32k max_tokens; the cap was then
   lowered to 16k, which failed twice with an empty response
   (stop_reason=max_tokens — the second 16k attempt was an unchanged re-call,
   an operator error, not a deliberate retry policy); the cap was then raised
   to 40k, which succeeded. All subsequent generation/repair calls used 40k.
   Generation metadata (model, token usage, attempts) is in
   run-N/run_record.json.
5. **Generated trees' self-authored tests** (src/test/** emitted by the
   generator alongside the SUT) were not compiled or run — the frozen suite is
   the only test instrument in this arm.

## What was NOT done (by design)

No verifier was run on any artefact, no verifier-derived text entered any
prompt, and the terminal artefacts are unmodified since their terminal
iteration. The one-shot verification (Dafny locally; FDR4/Isabelle on the
user's machine) is the parent's next step.
