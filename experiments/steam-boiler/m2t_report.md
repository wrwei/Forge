# Steam Boiler — M2T / backend emission + verifier handoff report (2026-09-03)

## Extractor state (provenance)

- Extractor: the WORKING TREE of forge.transformations — this session's
  uncommitted revision fixes are the system under test for the Steam Boiler run.
- `git rev-parse HEAD` = 26fe49fb0049; `git status --porcelain
  forge.transformations | wc -l` = **30** modified/untracked files (16 modified
  incl. java2robochart.etl, robochart2rct.egl, thy_generation_rule.egl,
  java2dafny.egl, RctPhase/RoboChart2RctTransformer, StructuralLinter; 14
  untracked regression tests). No stash used; the tree was not modified by
  this run (one 1-line ETL diagnostic was applied ONLY to a COPY under
  /tmp/steamboiler/diag-resources during fault isolation and is NOT part of
  the artefact-producing runs — see finding E1).
- Harness: single JVM, pinned classpath (ecj/jdt 3.41.0 first, Spoon 11.3.0,
  Epsilon 2.8.0, EMF, antlr-runtime 3.5.2, guava, snakeyaml), working-tree
  classes compiled to /tmp/steamboiler/forge-classes + repo src/main/resources
  on the classpath. Gradle remains sandbox-blocked (socket bind denied).

## Findings against the FIXED extractor (the point of the experiment)

**E1 (ETL crash, transient trigger — resolved by iteration 3).** The fixed
extractor's new presence-check simplification (`isPresenceCheckExpr`,
java2robochart.etl:3752) reads `funcExpr.value` after only an
`isKindOf(StringExp)` test; on the iteration-1/2 steam-boiler model a
DynamicEObject reached it whose eGet returned no `value`, aborting M2M with
"Property 'value' not found in object ... (eClass: StringExp)". The three
admitted studies do not trip it (chemical_detector re-extracts cleanly: 13
states / 36 transitions). A 1-line reflective-access diagnostic on a COPY of
the ETL confirmed the locus. After iteration 3 (guard predicates promoted from
locals to fields, matching the LRE reference), the UNPATCHED working-tree ETL
extracts cleanly — the crash path is no longer reached. Recorded as a
robustness finding for the extractor (guard against missing `value` at
etl:3752), not a blocker.

**D1 (Dafny generator gap, persists).** java2dafny.egl derives the
`datatype InputEvent = ...` variants from `instanceof` tests in step(). The
batch-facade encoding (profile-check construct 1) has NO instanceof branches,
so no event datatype is emitted while the methods still take
`event: InputEvent` — 7 resolution/type errors on the iteration-2 file
(where D2 compounds it), 6 on the final iteration-3 file. First study to exercise an
eventless step(); the admitted studies all have instanceof branches.
Post-processed in BoilerController_postprocessed.dfy (adds
`datatype InputEvent = Transmission`); raw generator output kept beside it.

**D2 (Dafny type mapping, resolved by iteration 3).** With locals, the
generator typed boolean-returning sensor methods (`transmissionBroken()` etc.)
as `function ...: real` while inlined guards used them as booleans (also the
int-vs-real comparison `stopCount() >= 3.0`). After the field promotion the
guards reference typed `var ...: bool` fields and the defect no longer
surfaces in emitted obligations.

## Iterations 2-3 (continuing iterations.md; budget 6)

- **Iteration 2** — CSP generator (vendored RoboChart 3.0.0) rejected the RCT:
  `mismatched input 'on' expecting '}'` — `on` is a RoboChart keyword and
  `PumpReport.on` reached the datatype verbatim (the codegen rules' reserved
  list omits `on`). Renamed the record component to `running` (2 files).
  Lint 0 violations; 14/14 scenarios.
- **Iteration 3** — guard predicates promoted from step()-locals to controller
  fields (identical to the admitted LRE controller's iteration-3 fix, for the
  same M2T-Dafny inlining reason; also cures E1/D2). Lint 0 violations; 14/14
  scenarios; M2M now emits a BoilerController_Refresh operation for the field
  block (5 states / 40 transitions).

**Converged at iteration 3** — inside the studies' 2-4 range.

## Emission results (all from the unpatched working-tree extractor, iter-3 Java)

| Stage | Result | Output |
|---|---|---|
| M2M (ETL) | completes | robochart_model.xmi: 5 states, 40 transitions, 5 events; deadlock-lint advisories on all 5 modes (see below) |
| RCT (robochart2rct) | completes, 0 warnings | robochart_controller.rct (57 trace entries) |
| CSP (vendored generator 3.0.0) | completes, no ERROR | csp-gen/ + timed/ trees, BoilerController_{,Ctrl_,Module_}coreassertions.csp |
| Isabelle theory | completes | isabelle/BoilerController_Beh.thy (37 zoperations; 77 lemmas: 37 R1-preservation + 37 inv-preservation [hoare_lemmas] + 2 Init-establishes + 1 deadlock_free), ROOT (parent Z_Machines) |
| Dafny | completes with gap D1 | BoilerController.dfy (raw) + BoilerController_postprocessed.dfy (D1 fixed) |

Deadlock-lint advisories: EMERGENCY_STOP "no outgoing transitions" is the
pre-registered EXPECTED-FAITHFUL terminal mode. The four live modes are
flagged "all outgoing transitions guarded" — their guard covers are
negation-complete by construction (each mode's final else-branch carries the
conjunction of all negations), i.e. total guard cover per CLAUDE.md's
autonomous-only-mode guidance; the advisory is the M2M lint being too broad
(documented in CLAUDE.md itself).

## CSP post-processing (pipeline's own logic)

- `scripts/apply_corrections_standalone.py` (reuses csp_corrections.py's
  parse/merge/naming verbatim): instantiations.csp + timed/instantiations.csp
  corrected — core_nat/int/real/string = {0..1}, core_boolean = Bool.
- `_nodet` sibling per runners.run_fdr4: auto-discovery picked
  `BoilerController_coreassertions.csp` (controller-level; no _System_Module
  aggregate for a single-controller model with operation machines), 1
  `:[deterministic]` assertion commented out →
  `BoilerController_coreassertions_nodet.csp` (remaining: 2 deadlock-free +
  1 divergence-free).

## Dafny verification (local, dafny 4.11.0 + z3 4.15.4 via --solver-path)

Raw BoilerController.dfy (final, iteration-3): 6 resolution/type errors, all finding D1 (dafny's own tally; the iteration-2 file showed 7, D2 adding one). Postprocessed:

```
Dafny program verifier finished with 11 verified, 0 errors   (~1.1 s)
```

Per-method: _ctor, transitionFromINITIALIZATION, transitionFromNORMAL,
transitionFromDEGRADED, transitionFromRESCUE, transitionFromEMERGENCY_STOP,
step — all Correct; determinism lemmas INITIALIZATION/NORMAL/DEGRADED/
RESCUE_deterministic — all Correct (verify/dafny_verdicts.txt).
Note the generated determinism lemmas PASS: the priority-negation discipline
makes the extracted guard preconditions mutually exclusive.

## Verifier handoff (licence/Docker-gated stages)

/tmp/steamboiler/verify/run_sb_verifiers.sh (bash -n clean):
1. FDR4 licence pre-flight on a real assertion (not --version), then
   `refines --format framed_json` on the narrowed _nodet.csp, run from defs/
   so includes resolve; 1800 s alarm; verdict counts parsed from framed_json.
2. Isabelle via scripts/isabelle-docker.sh: `build -D isabelle/ -v -o
   timeout=600` with optional `-d $ZMACH`; outcomes classified
   verified/refuted/timeout with the Final-state hang signature called out.
Banner prints the pre-registered expectation: the ONLY acceptable failure is
the terminal-mode deadlock (FDR4 deadlock-freedom into EMERGENCY_STOP;
Isabelle deadlock_free lemma). Results echo to /tmp/steamboiler/verify/out/.


## Verifier-load findings 1 and 2 (user's first run of run_sb_verifiers.sh)

Both verifiers failed at LOAD; neither failure is the prereg deadlock. Fixed
at the two NON-frozen layers and regenerated (Java untouched):

- **Finding 1 — FDR4**: `closePump.out.2` rejected at BoilerController.csp:707;
  core_int={0..1} vs pump ids 1..4. Surveyed every int carrier in the model
  (openPump/closePump payloads 1..4, pumpInflow args 1..4, stopCount 0..3 vs
  stopRepeatLimit=3): per-study corrections/type_ranges.json now sets
  int={0..4}; instantiations.csp + timed/ regenerated via
  apply_corrections_standalone.py; _nodet rebuilt. The old {0..1} also made
  stopCount>=3 unreachable — the widening cures that latent unsoundness too.
  core_nat/core_real/core_string/core_boolean unchanged (no value demands
  more). State space: core_int 2->5; openPump/closePump alphabets 4->10
  events each, stopCount domain 2->5 — constant-factor growth in a
  boolean-dominated space.
- **Finding 2 — Isabelle**: `Undefined type name: boolean` at thy line 45; the
  record emission path emitted raw Java type names (scalar path already
  mapped). thy_generation_rule.egl (working tree, not frozen) record fields
  now map through zmType(); regenerated theory has bool/real fields, 37
  zoperations / 77 lemmas unchanged, and no Java type name remains
  (word-boundary grep clean). First-exercise gap: no admitted study has a
  record field of primitive type.

**Parent note (post-finding 1):** `forge.dashboard/corrections/type_ranges.json` is a
GLOBAL file; the int {0..4} widening needed for the boiler was applied there
transiently for generation and has been REVERTED in the repo (verified {0..1}
restored). The boiler's shipped `instantiations.csp` (both trees) carries {0..4}
baked in, so the verifier run is unaffected. If the boiler is ever regenerated,
the widening must be re-applied transiently — a per-study corrections mechanism
is the proper fix and is recorded as an infrastructure finding (finding 1b).


## Verifier re-run findings 3 and 4 (second user run)

- **Finding 3 — Isabelle**: `(*I1-CYCLE:..*)` diagnostic markers inside quoted terms
  (inline-renderer cycle guard; never fired on the acyclic three studies) are
  not legal Isabelle inner syntax. The steam boiler's carried projections
  (levelLow/projLow, levelHigh/projHigh) re-enter the expansion stack; in a
  sequential compute() body that read is the PRE-state, i.e. the bare lens
  under simultaneous substitution. thy_generation_rule.egl now emits the bare
  name and prints the diagnostic at generation time (296 logged). Theory
  regenerated: 37 zops / 77 lemmas, no `(*` in any term, no marker in
  RCT/CSP/Dafny (grep-verified; Dafny 11/11 stands).
- **Finding 4 — FDR4**: SIGKILL at 1322s compiling under the global int={0..4}
  widening. Mitigation (a) applied: int back to {0..1}; pump channels
  post-processed to core_pumpid={1..4}, stopCount channels to
  core_stopct={0..3} (14 defs files, documented like _nodet; zero core_int
  channels remain). (b) n/a — assertions already at the narrowest
  (statemachine) scope; module-level recorded as not-checked-at-this-scale.
  (c) none — runners.py passes no compression flags; dbisim already in the
  generated CSP. _nodet rebuilt; verify-script banner states the mitigation.


## Third verifier run: findings 5 and 6

- **Finding 5 — Isabelle**: operation-carried state variables (construct 7) had no
  zstore lens; the auto-consts fallback shaped them `unit => T` and mis-typed
  projLow as int (Java: double; RCT: real — model typing was correct, the thy
  fallback heuristic was not). thy_generation_rule.egl now collects
  OperationDef variableLists as model-typed zstore lenses and extends the
  usage scan across data-plane reads. Regenerated theory matches the admitted
  studies' bare-lens reference form; 37 zops / 77 lemmas; new static checker
  verify/thy_term_check.py: 74/74 pre/update strings clean (also clean on the
  three reference theories; fails on the pre-fix theory).
- **Finding 6 — FDR4 (recorded)**: second SIGKILL (732s, per-channel narrowing;
  previously 1322s at int={0..4}, whose err stream carried the "Found 2000
  processes including 70 names" compile-phase evidence; the second run's err
  file holds only the 'Killed: 9' line). No statemachine-scope process smaller than
  P_BoilerController exists in the generated tree (Ctrl/Module wrap it;
  operation machines lack the mode automaton). Outcome recorded:
  FDR4-infeasible at this machine's memory for this study; Isabelle
  deadlock_free covers the property class. run_sb_verifiers.sh rev 4 checks
  the three operation machines (supplementary, tractable) and gates the full
  check behind SB_FORCE_FULL=1.


## Fourth verifier run: findings 7 and 8

- **Finding 7 — FDR4 load**: duplicate memory-channel definitions in the op machines
  (get_unitsReady at CalcLevelEstimate.csp:51+:61 etc.) — root cause a
  Sensors/Ctrl_State double declaration of `unitsReady` in the RCT (ETL
  promotes every batch-record field into Sensors; the same name is a guard
  field). The finding 5 zstore fix is exonerated (thy-only). Fixed in
  robochart2rct.egl: state declaration wins, shadowed Sensors entry dropped
  with a notice. Full regeneration; nodets rebuilt. Duplicate evidence
  (corrected after review): naive per-file grep still shows 110-166 repeats
  in Ctrl/Module/file_robochart_controller — cross-module re-declarations,
  not errors; the module-SCOPED scan over ALL 48 defs files (both trees) is
  the authoritative check and reports 0 in-scope duplicates post-fix, and as
  positive control flags the pre-fix v6 tree (unitsReady blocks x2 in
  OP_CalcLevelEstimate and BoilerController_Ctrl). Script rev 5 converts silent 0/0 results into loud
  '!! LOAD FAILURE' + first error (mutation-testing arm guard).
- **Finding 8 — Isabelle**: anonymous 620s timeout recorded as NO EVIDENCE (mutation-testing
  rule). make_thy_variants.py ships NoDlf / HoareA / HoareB isolation
  variants + per-variant ROOT sessions; rev-5 script runs NoDlf first (exit 0
  => cost localized to deadlock_free => prereg-defensible with the M2M
  advisory), bisects the 76 preservation lemmas only if needed.

## Fifth run interpretation (parent, 3 Sept)

**FDR4 operation machines — healthy, no finding.** Each reports passed=2
failed=1, and the "failure" is the raw `P :[deadlock-free]` assertion whose
counterexample trace ends in ✓ (termination): a terminating operation trivially
"deadlocks" after tick. The generator anticipates exactly this by emitting the
paired termination-absorbing assertion `P;RUN({r__}) :[deadlock-free]`, which
PASSES — as does divergence-freedom — on all three machines. The archived
studies' op-machine files carry the identical assertion pair. Verdict: all
operation machines verify under the meaningful assertions; the raw-assertion
failure is the known semantics of terminating operations, not a defect.

**Isabelle isolation ladder — did not run: session-setup error (finding 8b).** All
three variants died in 8-9s with 'Duplicate use of directory' — Isabelle2023
forbids multiple sessions rooted in one directory. Fixed by the parent:
per-variant subdirectories (NoDlf/, HoareA/, HoareB/) with `session ... in
"dir"` ROOT entries. The canonical 620s anonymous timeout stands as
NO-EVIDENCE pending the ladder.


## Sixth verifier run: finding 9

- **Finding 9 — Isabelle scale**: all three v1 isolation variants timed out (NoDlf
  1831s, HoareA 1830s, HoareB 1481s) — cost NOT localized to deadlock_free but
  spread across the 76 preservation lemmas. Cost profile: 37 zops x 29 update
  clauses x 30-field zstore + nested records (16-field TransmissionData, 4x
  8-field PumpReport). The invariant is minimal (tr nonempty, no listens-pins
  — fully autonomous machine), so the mutation-testing experiment's filtered-pin fix does not
  apply. Ladder v2 shipped (Skeleton / OneLemma / NoDlf / HoareA / HoareB,
  per-dir ROOT sessions): Skeleton prices elaboration, OneLemma prices one
  zpog_full proof, and the script chooses the rest from the OneLemma timing.
  Heap: failed runs were on 32-bit ML (~4GB cap, cpu/elapsed factor 2.57 =
  thrash signature); rev-6 script offers SB_ML64=1 to switch the mounted
  ISABELLE_HOME_USER settings to 64-bit ML with --maxheap 12g. Canonical run
  gated behind SB_CANONICAL=1.

## Seventh run interpretation (parent, 4 Sept) — verdict INVALID, script defect (finding 9b)

The Skeleton "timeout" is not a Skeleton verdict: `run_variant` invoked
`isabelle build -D` (select ALL sessions under the directory), so the 1120s
window was spent building all five variants concurrently — the log shows
`Running` lines for HoareA/HoareB/NoDlf/OneLemma/Skeleton together. Fixed to
`-d` (register root, build only the named session). The "cost is upstream of
proofs" interpretation printed by the banner is therefore RETRACTED pending the
eighth run. Note the ML64 settings write succeeded and heap rebuild happened
inside the same window, further inflating it.

## Eighth run interpretation (parent, 4 Sept) — finding 9c: heap vs Docker VM cap

The -d fix worked: only Skeleton built this time. But exit 137 (SIGKILL) at
216s with cpu/elapsed factor 2.21 is the Docker VM's OOM killer, not an
Isabelle timeout: run 7's SB_ML64 wrote `--maxheap 12g` while the Docker VM
(default) has far less memory, so Poly/ML allocated past the cgroup limit and
was killed during elaboration. The banner's printed interpretation ("cost is
upstream of proofs") is again NOT yet established — the Skeleton has not run
to a verdict under a heap the VM can back. Fix (rev 8): the ML block is
rewritten on every SB_ML64 run, default maxheap 6g, overridable via SB_HEAP;
user should either keep 6g or raise the Docker Desktop VM memory and set
SB_HEAP accordingly. The Skeleton verdict remains OPEN.
