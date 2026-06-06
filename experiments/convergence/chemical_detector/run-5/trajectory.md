# chemical_detector — run-5 trajectory (condition B, me-as-developer)

**Outcome: CONVERGED at iter 1** (cap 7). Every phase `passed`, vacuity audit
0 findings, on the first pipeline run against the cold codegen.

## Headline table

| Iter | Change | compile | coverage | preflight | t2m | m2m | m2t | dafny_gen | isabelle_gen | fdr4 | dafny_verify | isabelle_verify | vacuity | Pipeline wall-clock | Converged |
|------|--------|---------|----------|-----------|-----|-----|-----|-----------|--------------|------|--------------|-----------------|---------|---------------------|-----------|
| 1 | Cold codegen (17 files, 542 LOC, 81/81 requirements traced) | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ 9/9 | ✅ | ✅ 10 lemmas + deadlock-free | ✅ 0 findings | **71.6 s** | **yes** |

FDR4 ran against the auto-discovered multi-controller aggregate
`GasAnalysis_System_Module_coreassertions.csp` (9 assertions checked, 9 passed,
2.5 s; 3 `:[deterministic]` assertions stripped per pipeline policy). Isabelle
generated and proved one theory, `GasAnalysis_Beh.thy` (10 lemmas incl.
`GasAnalysis_deadlock_free`, 27 s build). Dafny verified `GasAnalysis.dfy`.

## Run-level cost

- **Claude token total (measured, session transcript JSONL — `/cost` not
  exposed to the agent):** captured at archive time, immediately after the
  convergence verdict and before this write-up finished (final write-up tokens
  slightly undercounted):
  - output tokens: **268,527**
  - uncached input tokens: 6,706; cache-read input: 11,688,920; cache-creation
    input: 660,003 (total input incl. cache: **12,355,629**)
  - 92 assistant messages with usage blocks.
- **End-to-end execution time** (session span, first→last transcript timestamp
  at measurement point): **≈ 990 s (16.5 min)**, excluding the last minutes of
  this write-up.
  - **pipeline** = 71.6 s (single iter; per-phase in `iter-1/summary.json`;
    largest phase isabelle_verify 35.5 s, fdr4 only 2.5 s)
  - **agent** = end-to-end − pipeline ≈ **918 s** (codegen ≈ everything:
    canonical-input reading, design, 17 source files, traces, snapshot,
    write-up; no diagnosis time was needed)
- Per-iter `codegen_cost` left `kind: "estimated"`, nulls — per protocol.
- Consolidated row for `experiments/convergence/execution_times.md`: to be
  added at copy-back (file scrubbed from this worktree):
  `chemical_detector run-5 | condition B | 1 iter | pipeline 71.6 s | end-to-end ≈990 s | agent ≈918 s | output tokens 268,527`.

## Independence

- **Condition B** (honest independent; playbook absent). Actor:
  `me-as-developer`, single fresh session in a scrubbed worktree
  (`fmgvc-cd-run8-redo`) on a parentless severed base commit (`cc986c4
  "isolated run base"`); `CLAUDE_CONFIG_DIR=C:\tmp\claude-clean-cd-run8`
  (clean config home).
- **No prior-run content was injected**: no agent-memory entries, no
  `.remember` hook output, no run/iter terms in the git snapshot. Answer
  material (run dirs, KB, playbook, reference runs, cold baseline,
  execution_times.md) was absent from the worktree as expected.
- **Iter-1 inputs read (complete list):** `system_description.txt`,
  `requirement_all.json`, `CLAUDE.md` (+ its three `forge.assets/prompts/*`
  codegen files and `fdr4_system.txt`, injected as project instructions),
  `experiments/RUN_TRAJECTORY.md`, `experiments/HOWTO_RUN_CONVERGENCE_EXPERIMENT.md`.
- **Tool-source reads (the tool, not the answer key):** `pipeline.yaml`
  (setup §2), `forge.dashboard/corrections/type_ranges.json` (setup §2),
  `forge.dashboard/web/feedback/coverage.py` (only to recover the
  undocumented `result_codegen.json` schema). Nothing under `docs/`,
  `experiments/convergence/` (other than writing this run), or any §4 path
  was opened.
- Machine-local tuning applied (not committed): `pipeline.yaml`
  `dafny_path.win32` and `wsl_isabelle_bin` corrected to this machine's
  install paths; fdr4 `timeout: 3600` / `memory_limit_mb: 78002` were already
  set by the human.

## Iter-by-iter narrative

### Iter 1 — cold codegen → full convergence

Read only the canonical inputs, then wrote the complete `chemdetector/`
tree (packages `annotation`, `datamodel`, `mode`, `constants`, `event`,
`sensor`, `actuator`, `controller`; no `operation` package — see caveats).
Two controllers per CD-ARCH2:

- **GasAnalysis** (`GaMode`: Reading, Analysis, GasDetected, NoGas, Done):
  Reading consumes `gas ? gs` (payload captured as the branch's first action);
  Analysis entry `sts = analysis(gs)` with an autonomous **total guard cover**
  `stsIsNoGas` / `!stsIsNoGas`; GasDetected entry `ins = intensity(gs)` with
  total cover `insAtOrAboveThr` / `!insAtOrAboveThr` (`ins >= thr` written as
  a plain comparison, `goreq` kept on the sensor service); NoGas →
  unconditional autonomous → Reading (the only τ-transition; no cycle);
  above-threshold branch sends `stop` and goes to **Done** (a live mode with a
  `gas` event self-loop) instead of the spec's final state j1.
- **Movement** (`MvMode`: Waiting, Going, Avoiding, TryingAgain,
  AvoidingAgain, GettingOut, Found): `stop`→Found and `resume`→Waiting
  duplicated as the first two branches of every mode block; `turn ? a`
  captures then re-issues `move(lv, a)`; obstacle path Going→Avoiding resets
  clock `t` (`t = timer.nowMs()`) and runs the Avoiding entry sequence
  `d0 = odometer(); changeDirection(l); pause(evadeTime)`; AvoidingAgain
  has the stuck-detection total cover `makingProgress =
  (nowMs()-t < stuckPeriod) || (d1-d0 > stuckDist)` vs `!makingProgress`;
  Found emits `flag` + `move(0, Front)` on entry and self-loops on `stop`
  instead of going final.

Design decisions taken up-front from CLAUDE.md's inline guidance (these are
exactly the failure modes other runs hit during iteration, per the project
docs' framing — here they were applied as iter-1 constraints): **no Final
state on any controller**, **total guard covers instead of self-loops on
every autonomous-only mode**, trigger-payload capture as first action,
`Clock`-class/`nowMs()` idiom for `since()` rewriting, `pause()` for waits,
`@SensorService` to keep the `GasSensor` record from being misclassified,
lowercase event-record names matching the spec's event names verbatim.

Pipeline: all 12 phases passed first try. Advisory only: M2M deadlock-lint
flagged 11 states "without unconditional fallback" (all but NoGas) — the
known over-broad lint; both FDR4 and Isabelle subsequently *proved*
deadlock-freedom, confirming the advisories needed no action. The Isabelle
theory was generated for **GasAnalysis** (the primary controller for this
study); its `Done` mode satisfied the bare-precondition rule via the
event self-loop, and the proof closed with the standard tactic.

## Caveats (run-specific spec compromises / workarounds)

1. **Final states rerouted** (CD-GA-Beh6, CD-MV-Beh9): both controllers stay
   live instead of reaching final j1 — GasAnalysis: GasDetected→Done (gas
   self-loop) still sending `stop`; Movement: Found self-loops on `stop`
   after `flag` + `move(0, Front)`. Required by the theory generator's
   no-Final constraint (CLAUDE.md); applied to *both* controllers because
   which one gets the theory was not knowable a priori.
2. **odometer (CD-Evt3) realised as a zero-arg sensor method**, not an input
   event: the spec uses `odometer ? dN` as a transition/entry *action*
   (receive-as-action), which the pipeline's action-statement set cannot
   express; `dN = telemetry.odometer()` extracts as a Sensors-interface read.
3. **Chem / Intensity as primitives** (int nat / double real) — wrapper types
   would force banned method-chain guard expressions.
4. **Vehicle operations (CD-OP1..4) as plain methods** on `Vehicle`, not
   `compute()` operation classes (they are platform motion commands, not
   controller computations).
5. **Invented defaults** (also in `iter-1/feedback/post_codegen.md`):
   `analysis()` detects the target iff `c == targetChem && i > 0.0`;
   `angle()` maps 1→Front, 2→Right, 3→Back, 4→Left, else Front; constants all
   1 / 1.0 (within the FDR4 `{0..1}` instantiation ranges); empty-reading
   safe defaults intensity 0.0 / location Front / analysis noGas.
6. Machine-local `pipeline.yaml` path fixes (Dafny, WSL Isabelle) — see
   Independence; not committed.

## Findings (durable, generalisable)

- **F1 — Treat CLAUDE.md's verifier post-mortems as iter-1 *design
  constraints*, not just fix guidance.** This run converged in one iteration
  because the no-Final-state rule and the total-guard-cover-for-autonomous-
  modes rule were applied during cold codegen rather than discovered through
  failing iterations. *How to apply:* before writing any controller, list
  every autonomous-only mode and give it a jointly-exhaustive guard set; list
  every terminal transition and reroute it to a live mode (still emitting the
  terminating event).
- **F2 — Encode two-way guard splits as one predicate and its literal
  negation (`b` / `!b`), never as two independently-named predicates.** The
  extracted guards become independent booleans in the model; only the
  syntactic complement is *provably* total downstream (Isabelle's
  `deadlock_free` closer, FDR4 deadlock check). Worked for an enum split
  (`sts == noGas` / `!…`), a threshold split (`ins >= thr` / `!…`), and a
  compound stuck-detection split (`makingProgress` / `!…`).
- **F3 — With multiple controllers, assume *any* of them may be the
  theory-generated "primary": give none of them a Final state.** Here the
  theory went to GasAnalysis (first-declared/alphabetical, not the larger
  Movement machine); guessing "the other one" would have produced the
  hanging-proof failure mode. Rerouting both cost nothing elsewhere.
- **F4 — The M2M deadlock-lint is advisory noise on a healthy model:** it
  flagged 11 of 12 states on a model whose deadlock-freedom both FDR4 and
  Isabelle then proved. Only act on it when a flagged state genuinely has
  neither an event-triggered bare-precondition branch nor a total autonomous
  cover.
- **F5 — `result_codegen.json`'s schema is undocumented in the canonical
  inputs**; recover it from `forge.dashboard/web/feedback/coverage.py`:
  top-level `codegen_trace` list of `{requirement_gid, java_file,
  java_element}`; coverage matching is by element name *or* file basename, so
  one trace entry per file covers its members, and annotation infrastructure
  (`RoboChartType`, `Clock`, `SensorService`, `RoboChartWait`) is allowlisted.
- **F6 — Verify machine-local tool paths before the first pipeline run**
  (`Test-Path` the Dafny/FDR4 win32 paths; `wsl ls` the Isabelle binary).
  Both Dafny and WSL-Isabelle paths in this checkout pointed at another
  machine's user dir (`willr` vs `will`); caught in setup, zero iters burned.
- **F7 — Lowercase Java record names that exactly match the spec's event
  names (`gas`, `obstacle`, `turn`, `stop`, `resume`, `flag`) carry verbatim
  through the whole chain** (events, Shared-interface pairing between the two
  controllers, CSP channels), and the receive-as-action pattern
  (`odometer ? d`) is expressible as a zero-arg sensor read. Neither required
  iteration.

## Reproducibility

1. Stage the source: copy `run-5/iter-1/java/**` to
   `java.generated.project/src/main/java/chemdetector/` and
   `run-5/iter-1/traces/result_codegen.json` to
   `java.generated.project/result_codegen.json`.
2. Ensure `pipeline.yaml`: `agent.active_case_study: chemical_detector`,
   valid `dafny_path.win32` / `wsl_isabelle_bin` for the machine, fdr4
   `timeout: 3600`; `forge.dashboard/corrections/type_ranges.json` all
   `{0..1}`.
3. `python experiments/scripts/run_experiment_iteration.py` — expect all 12
   phases `passed` and `post_vacuity.json` with 0 findings. Formal artefacts
   land in `forge.transformations/output/` (compare against
   `run-5/iter-1/formal-artefacts/`).
