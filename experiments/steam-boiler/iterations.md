# Steam Boiler — generation/lint iteration log

Budget: 6 iterations max (studies' convergence range 2–3).

## Iteration 1 (2026-09-03)

- Generated the full tree (14 files, package `steamboiler`) from
  requirement_all.json under the frozen prompt bundle
  (java_codegen_rules.txt + codegen_trace_rules.txt + CLAUDE.md constraints +
  chain_of_thought_codegen.txt phases 0–3).
- `javac` (JDK 21, `-d build/classes`, whole tree): **clean, zero warnings' text output** — compile OK on first attempt.
- StructuralLinter (frozen; Spoon 11.3.0 + jdt/ecj 3.41.0 pinned first on the
  classpath, single JVM, cached classpath at /tmp/steamboiler/forge_cp.txt):
  **0 violations (0 errors, 0 warnings)** — lint_report.json is an empty
  violations array with 12 types scanned (rule0 vacuity guard did not fire).
- Behavioural scenario checks (local harness, 14 checks S1–S10 covering
  startup, STOP×3, terminal refusal, init steam check, transmission failure,
  level/steam failure routing, repair return, valve/pump init actions, MODE
  broadcast, N1 regulation): 14/14 PASS after one harness-side timing fix
  (S3 originally delivered the nonzero-steam reading one cycle after
  STEAM_BOILER_WAITING, by which point the SB-Beh3 zero-steam check had
  already passed on the waiting cycle itself — the fix moved the defective
  reading INTO the waiting-arrival cycle; the generated Java was not
  changed).

**Converged at iteration 1** (no Java edits required by lint). The studies'
range was 2–3; converging below it is consistent with the linter's rule6/rule8
pressure being anticipated at generation time (the negation discipline and
the batch-facade encoding were applied up front rather than retrofitted).

## Rule-pressure notes (prereg item 4)

- rule6 (priority negations): the predicted pressure point. Long per-mode
  chains (init: 9 branches) carry full explicit negation conjunctions of all
  higher triggerless guards; no violation fired.
- rule8 (event-before-triggerless): avoided structurally — the batch-facade
  encoding makes every branch triggerless, so no event-triggered branch can
  precede a triggerless one anywhere in step().
- rule1 (compute locals): both compute() bodies are pure chained
  `this.field = expr` assignments; clamping pushed into Sensor methods.
- rule7 (signed sentinels): no negative sentinel anywhere on controller or
  operation state; pump ids 1..4, counters 0-based nats, level bounds
  initialised to [0, C].


## Iteration 2 (2026-09-03, M2T stage)

- Trigger: vendored RoboChart CSP generator 3.0.0 rejected the RCT —
  `mismatched input 'on' expecting '}'` at the PumpReport datatype. `on` is a
  RoboChart keyword; the frozen reserved-word list in java_codegen_rules.txt
  does not include it (list gap — recorded, not edited).
- Change: PumpReport record component `on` -> `running` (PumpReport.java,
  Sensor.java). javac clean; lint 0 violations; 14/14 scenarios.

## Iteration 3 (2026-09-03, M2T stage)

- Trigger: (a) working-tree ETL crash at java2robochart.etl:3752 on the
  iter-2 model (finding E1); (b) Dafny obligations unprovable with inlined
  local guards (finding D2) — same defect class the admitted LRE study fixed
  in ITS iteration 3.
- Change: the 19 named guard predicates promoted from step() locals to
  controller boolean fields, refreshed at the top of step() (LRE pattern).
  javac clean; lint 0 violations; 14/14 scenarios; unpatched working-tree
  extractor completes end-to-end (M2M/RCT/CSP/thy/dfy).

**Converged at iteration 3 of 6** (studies' range 2-4). Remaining Dafny gap
D1 (no InputEvent datatype for an eventless step()) is a GENERATOR finding,
post-processed in BoilerController_postprocessed.dfy, raw output preserved.


## Iteration 4 (2026-09-03, verifier-load stage — findings 1 and 2)

User ran run_sb_verifiers.sh: BOTH verifiers failed at LOAD — neither failure
is the pre-registered deadlock. Two findings, both fixed at the
NON-frozen layers (per-study corrections + thy_generation_rule.egl working
tree); generated Java unchanged.

- **Finding 1 (FDR4 load, corrections layer).** BoilerController.csp:707 rejects
  `closePump.out.2`: openPump/closePump are typed `InOut.core_int` and the
  standard narrowing sets core_int={0..1}, but pump ids run 1..4. Value survey
  of every int carrier: openPump/closePump payloads {1..4}; pumpInflow args
  {1..4}; stopCount 0..3 (must reach stopRepeatLimit=3 — under {0..1} the
  STOP×3 emergency transition was UNREPRESENTABLE, a second latent load-level
  unsoundness the same fix cures). One-line fix: corrections/type_ranges.json
  int -> {"lower":0,"upper":4}; reapplied apply_corrections_standalone.py
  (core_nat/real/string/boolean unchanged — no surveyed value demands more).
  State-space: core_int cardinality 2->5 (openPump/closePump event alphabets
  4->10 each; stopCount domain 2->5); boolean-dominated space, so growth is a
  small constant factor, not exponential.

- **Finding 2 (Isabelle load, thy generator).** BoilerController_Beh.thy:45
  `Undefined type name: boolean` — the RECORD emission path (PumpReport /
  TransmissionData) emitted raw `type.ref.name`, while the scalar zstore path
  maps through zmType() (boolean->bool). None of the three admitted studies
  has a record field of primitive type, so the path was never exercised.
  One-line fix in thy_generation_rule.egl (working tree, NOT frozen):
  `e.fields.at(i).type.ref.name` -> `e.fields.at(i).type.zmType()`.
  Regenerated: records now `statePresent :: bool ... level :: real`;
  structure intact (37 zoperations / 77 lemmas); word-boundary grep for
  boolean/double/float/Integer/etc. over the regenerated theory: NONE.

Java unchanged, so lint/scenarios/trace results stand. Iteration count for
the JAVA remains 3; findings 1 and 2 are extractor/toolchain findings, not code-gen
iterations.


## Iteration 5 (2026-09-03, verifier re-run — findings 3 and 4)

Second user run: findings 1 and 2 cured (records parse, CSP loads past the old failure),
two NEW load/scale findings. Java again unchanged.

- **Finding 3 (Isabelle parse, thy generator).** `Failed to parse term` at the first
  zoperation: the emitted terms contained `(*I1-CYCLE:levelLow*)` markers —
  the inline-renderer's cycle guard emits its diagnostic INSIDE the quoted
  term, but Isabelle inner syntax does not accept `(*..*)` comments. Root
  cause located in thy_generation_rule.egl inlineRender (both the CallExp and
  RefExp cycle arms); the marker never fired on the three studies (their
  data-plane graphs are acyclic). The steam boiler's carried-projection pair
  (levelLow = baseLevelLow(projLow); projLow = levelLow - ...) re-enters the
  expansion stack: in a SEQUENTIAL compute() body such a re-entrant read is a
  PRE-STATE read (previous cycle's carried value), which under a zoperation's
  simultaneous substitution is exactly the bare lens. Fix (non-frozen working
  tree): emit the bare name and print the I1-CYCLE diagnostic at GENERATION
  time instead of inside the term (296 pre-state reads logged: levelLow/
  levelHigh 111 each, projLow/projHigh 37 each). Regenerated: 37 zops / 77
  lemmas intact; no `(*` remains inside any quoted term; word-boundary greps
  confirm no marker ever leaked into RCT/CSP/Dafny (Dafny's 11/11 verdict
  from the marker-free emission stands).

- **Finding 4 (FDR4 scale).** exit 137 (SIGKILL) at 1322s during compilation
  ("Found 2000 processes including 70 names" repeating, empty verdict list):
  the {0..4} global int widening times the 37-operation machine exhausts
  memory; the three studies never left {0..1}. Mitigations evaluated in the
  mandated order:
  (a) APPLIED — survey shows core_int has exactly TWO carrier families:
      pump-id channels (openPump/closePump, values 1..4) and stopCount
      channels (0..3, saturating at stopRepeatLimit=3). Reverted the
      per-study range to int={0..1} and post-processed the generated defs
      (documented transform, sibling of _nodet): pump channels ->
      core_pumpid={1..4}, stopCount channels -> core_stopct={0..3}; 14 defs
      files retyped across csp-gen/ and timed/; zero core_int channel refs
      remain. Alphabet cost vs the failed run: pump events 10->8, stopCount
      events 10->8 per channel, and every OTHER int carrier back to 2 values.
  (b) NOT APPLICABLE — the asserted P_BoilerController is already the
      narrowest (statemachine-level) process; the module/ctrl assertion files
      were never in the run. Recorded: module-level deadlock/divergence
      not checked at this scale.
  (c) NONE AVAILABLE — runners.py's run_fdr4 passes only --format
      framed_json (no compression/RTS flags to mirror); the generated CSP
      already carries dbisim compression (61 sites). No new configuration
      invented.
  If FDR4 still exhausts memory after (a), that is the recordable scale
  outcome for the paper; the prereg target remains a clean deadlock verdict
  into EMERGENCY_STOP.

Java iteration count still 3; findings 3 and 4 are extractor/scale findings.


## Iteration 6 (2026-09-03, third verifier run — findings 5 and 6)

- **Finding 5 (Isabelle typing, thy generator).** `Type unification failed:
  Operator baseLevelLow :: R => R, Operand projLow :: unit => Z`. Diagnosis:
  BOTH sub-problems trace to ONE omission — the zstore collector read only the
  Ctrl_State interface, so OPERATION-CARRIED variables (CalcLevelEstimate's
  levelLow/levelHigh/projLow/projHigh — stressed construct 7) got no zstore
  lens. Their bare pre-state reads (finding 3's bare-lens emission) then fell through
  to the auto-consts fallback, which (i) shapes names as zero-arg constants
  `unit => T` instead of lens reads (the admitted studies' theories reference
  zstore variables as plain lens names elaborated by the zstore context — the
  fallback shape can never match that), and (ii) typed projLow `int` via the
  index-argument heuristic, though Java declares `double` and the RCT `real`
  (so NOT a record/scalar-path leak and NOT an M2M signature-inference miss —
  the model itself types projLow real throughout; the mis-typing was purely
  the thy fallback heuristic). Fix in thy_generation_rule.egl (non-frozen):
  collect p.operations[*].variableList into stateVarNames/stateVarTypes
  (model-typed lenses; consts fallback suppressed by the existing gate), and
  extend the used-variable scan so names read/assigned by data-plane
  definitions get their lens even when no machine transition references them
  textually. Regenerated: projLow/levelLow/... now `:: "real"` zstore lenses,
  zero `consts <carried-var>` fallbacks, 37 zops / 77 lemmas intact.

- **Checker contribution (task requirement).** verify/thy_term_check.py — a
  static checker for generated *_Beh.thy: parses zstore/consts/enumtype/record
  declarations, then checks every zoperation pre/update string for balanced
  parens/brackets, inner-syntax comments (finding 3 class), unit-typed constants
  applied to arguments and argument-type disagreement against declared
  signatures (finding 5 class), and undeclared identifiers. Steam boiler theory:
  74 pre/update strings, 0 findings. Controls: LRE (32), sranger (16),
  chemical_detector (22) — all 0 findings. The checker fails (exit 1) on the
  pre-finding 5 theory and on the pre-finding 3 marker theory.

- **Finding 6 (FDR4 scale — RECORDED OUTCOME).** exit 137 again at 732s with
  per-channel narrowing (previously 1322s at global int={0..4}). The
  compile-phase evidence "Found 2000 processes including 70 names" repeating
  is from the FIRST (1322s) run as reported with finding 4; the second run's err
  stream contains only the shell 'Killed: 9' line (1 line total — verified
  by grep before writing this entry), i.e. the kill left no compile
  progress in the captured stderr. Mitigation survey per instructions: the untimed
  P_BoilerController (defs/, statemachine semantics O__) is the SMALLEST
  statemachine-scope process the generated tree offers — the only other
  assertion targets are P_BoilerController_Ctrl / _Module (strictly larger
  wrappers) and the three OPERATION machines (P_OP_CalcLevelEstimate,
  P_OP_CalcThroughput, P_OP_BoilerController_Refresh — smaller, but they do
  not contain the mode automaton). Therefore: FDR4 deadlock checking of the
  steam-boiler statemachine is recorded as infeasible at this machine's
  memory, with the Isabelle deadlock_free lemma covering the same property
  class for this study (prereg target unchanged). Script rev 4 runs the three
  tractable operation machines as supplementary FDR4 checks and skips the
  full machine unless SB_FORCE_FULL=1.

Java untouched throughout; iteration count for the JAVA remains 3.


## Iteration 7 (2026-09-03, fourth verifier run — findings 7 and 8)

- **Finding 7 (FDR4 op-machine load failures + script defect).** All three
  operation-machine runs reported 'exit=0 passed=0 failed=0' — actually LOAD
  FAILURES the script mis-reported as neutral (the same silent-zero class that
  bit the mutation-testing FDR4 arm). The json errors field shows duplicate memory
  channels, e.g. OP_CalcLevelEstimate::get_unitsReady defined at
  CalcLevelEstimate.csp:51 AND :61. Diagnosis: NOT the finding 5 zstore fix (that
  touches only thy generation) — the RCT declared `unitsReady` in BOTH
  BoilerController_State (guard field, finding 5-era Ctrl_State) and Sensors (the ETL
  promotes every TransmissionData record field); an operation `requires` both
  interfaces, so the vendored CSP generator emitted the whole
  get_/set_/setL_/setR_/set_EXT_ block twice. XMI check: Sensors∩Ctrl_State =
  {unitsReady} exactly; the Sensors copy is textually unreferenced in the RCT.
  The controller's own defs (BoilerController.csp untimed) were NOT affected —
  only files whose scope requires both interfaces (op machines + timed tree;
  grep evidence). Fix at the RCT layer (robochart2rct.egl, non-frozen):
  Sensors/Actuators emission now drops any name shadowed by a Ctrl_State
  declaration, with a generation-time notice ("[rct] finding 7 dedup: dropping
  Sensors.unitsReady"). Regenerated RCT + full CSP trees; corrections +
  per-channel narrowing reapplied; all four nodet siblings rebuilt.
  DUPLICATE-SCAN EVIDENCE (corrected after review): a naive per-file grep
  still counts 110-166 repeated channel names in the Ctrl/Module/
  file_robochart_controller files — those are cross-MODULE re-declarations
  (each CSP module redeclares its memory channels), not load errors. The
  authoritative check is module-SCOPED (nested-module stack; counts identical
  channel lines within one scope): run on ALL 48 defs files across both
  trees post-fix — 0 files with in-scope duplicates. Positive control: the
  same scanner on the PRE-fix v6 tarball flags the finding 7 duplicates
  (OP_CalcLevelEstimate get_/set_/set_EXT_unitsReady x2; also inside
  BoilerController_Ctrl — so pre-fix damage extended beyond the op machines,
  and only the untimed BoilerController.csp among the machine files was
  unaffected). Script rev 5 adds the
  load-failure guard: non-empty errors field OR zero total verdicts prints
  '!! LOAD FAILURE' with the first error — on the op machines AND the gated
  full run.

- **Finding 8 (Isabelle anonymous timeout — isolation, not conclusion).** exit 142,
  '*** Timeout' after 620s with no lemma named. Per the mutation-testing rule, an
  unfinished proof is never evidence of refutation or of the expected failure,
  so nothing is recorded from this run. Isolation variants produced by
  verify/make_thy_variants.py (documented post-processing, sibling of _nodet):
  BoilerController_Beh_NoDlf (deadlock_free lemma removed; everything else
  identical — 76 lemmas), and HoareA/HoareB (NoDlf with first/second half of
  the 76 preservation lemmas — 38 each) used only if NoDlf also times out.
  isabelle-variants/ROOT declares one session per variant so a single
  `isabelle build -D` covers them. Script rev 5 runs NoDlf FIRST: exit 0
  localizes the cost to deadlock_free — which, with the M2M terminal-mode
  advisory, is the defensible evidence for the prereg outcome; nonzero
  triggers the HoareA/HoareB bisect. Canonical session still runs afterwards
  for the record.

Java untouched; iteration count for the JAVA remains 3.


## Iteration 8 (2026-09-03, sixth verifier run — finding 9)

- **Finding 9 (Isabelle scale — all v1 variants time out).** NoDlf 1831s / HoareA
  1830s / HoareB 1481s, all exit 142. The finding 8 hypothesis (cost localized to
  deadlock_free) is REFUTED: the cost is spread across the 76
  invariant-preservation lemmas. This is a genuine scale finding on the Steam Boiler: 37
  zoperations, each update carrying 29 clauses over a 30-field zstore, plus
  the 16-field TransmissionData record embedding 4x 8-field PumpReport records
  — the largest state the pipeline's thy path has ever emitted (chem = the
  previous ceiling). Notably the boiler theory's `where inv` is just
  `tr \<noteq> []` and there are ZERO listens-pins in the invariant (the
  machine is fully autonomous — no event-triggered operations), so the
  mutation-testing experiment's filtered-pin fix does NOT apply: the cost driver is the
  update-clause width times zpog_full's substitution reasoning, not
  invariant conjunct count.
- **Isolation ladder v2** (make_thy_variants.py v2; per-dir sessions kept per
  the user's ROOT restructure): Skeleton (zstore + records + all 37 zops +
  zmachine, ZERO lemmas — prices elaboration alone), OneLemma (Skeleton + the
  single InitialToINITIALIZATION_inv lemma — prices one zpog_full proof),
  then NoDlf or the HoareA/B halves chosen by the script from the OneLemma
  timing (naive 76x estimate vs 1800s budget). Ladder semantics: Skeleton
  timeout => cost upstream of proofs (record simp setup / machine
  elaboration), bisection moot; OneLemma timeout => per-lemma cost finding,
  bisection moot at this heap.
- **Heap ceiling.** The failed runs used the container's default
  ML_PLATFORM=x86_64_32-linux (~4GB Poly/ML cap); the canonical run showed
  cpu/elapsed factor 2.57 — consistent with GC thrash at the cap. The
  distribution also ships 64-bit x86_64-linux Poly/ML, selectable via
  ISABELLE_HOME_USER etc/settings (the wrapper bind-mounts that dir, so no
  image change needed). Script rev 6: SB_ML64=1 writes
  ML_PLATFORM=x86_64-linux + ML_OPTIONS=--maxheap 12g to the mounted
  settings (one-off heap rebuild on first use). Recommended first move
  before interpreting any ladder timeout as intrinsic.
- Script rev 6 additionally prints a per-variant verdict table (session /
  exit / seconds) and gates the canonical run behind SB_CANONICAL=1 (it hung
  on run 6 and the ladder supersedes it as evidence).

Java untouched; iteration count for the JAVA remains 3.
