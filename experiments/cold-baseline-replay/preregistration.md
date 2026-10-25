# Pre-registration: replaying the cold baseline and the compile-only ablation with the published extractor

Date: 25 October 2026, before any replay pass ran. Extractor: `wrwei/Forge` at `d88ad2a1`
(the commit the condition-F pin `5f403301` copies; recorded again by the runner at run time).

## Why

The paper reports one extractor. The cold baseline (Section 4.5, Tables 5 and 6) and the
compile-only ablation (Section 4.7, Table 9) were measured with the extractor of the original
submission; the feedback-loop runs they are compared with (Table 7) were collected with the
published one. The replay puts every number on the same pipeline version.

## What is replayed, and what is not

- Cold baseline: the 30 recorded cold Java sources (`experiments/cold-baseline/<study>/run-1..10/src`),
  one pipeline pass each, all twelve phases. The cold sources are the recorded ones; no LLM is run.
- Compile-only ablation: the recorded stop-point Java of each of the 15 runs
  (`experiments/convergence/ablation/<study>/run-N/iter-<last>/java`), one pipeline pass each.
  The actor's stop decision is the recorded one; the replay re-judges the Java it stopped at.
- Not replayed: the mutation campaign (separate pre-registration) and the LLM-driven runs.

## What is recorded

Per pass: the twelve phase verdicts, the feedback reports, the generated artefacts, the driver log,
under `experiments/cold-baseline-replay/` and `experiments/convergence/ablation-replay/`.
A pass that hits the per-pass cap (3600 s) is recorded as *no verdict* and rerun; it is never counted.

## How the paper is updated (decided now)

- Table 5: compiled / failing-phase / converged per study from the replay, Phase 2b excluded as before.
- Table 6: best-of-N recomputed over the replayed runs with the same disjoint-group rule; the
  Fisher p-values recomputed from the counts.
- RQ3 text: whatever the counts are. If any cold run converges, the sentence "no single-pass cold
  generation does (0 of 30)" and its echoes in the abstract, introduction, discussion and conclusion
  change to the measured count.
- Table 9 and RQ5: `ships broken` per study from the replay. If a stop point now passes every
  verifier, the 15-of-15 claim becomes the measured count and the necessity argument in Sections 4.7
  and 5.1 is weakened accordingly.
- The archived measurements stay in the response letter as the history of the revision.

## Expectations (stated for the record, not as a decision rule)

The published extractor is stricter than the archived one on branch priority and event triggers and
more faithful on types, so a cold source that failed before is expected to fail again, usually at the
same verifier; a source that failed only through an archived-extractor defect could pass. We expect
0 of 30 cold convergences and 15 of 15 broken stop points, and we report what is measured.

## Amendment 1 (25 October 2026, after the first complete pass of all 45 replays)

Three things the replay found in the instrument, recorded here before any pass is rerun:

1. **Phase 6d double-reported the exhaustive-mode idle operation.** When a mode's branches cover
   every case, the idle operation (the Java `else`) is unsatisfiable; the environment-aware check
   reports it as advisory (by design), but the older symbolic check reported the same operation
   again as blocking. Thirteen Chemical Detector passes (8 cold, 5 ablation) failed the vacuity
   audit on that finding alone or in part. Fix: the symbolic check defers idle operations under the
   current environment model to the environment-aware check (`forge.dashboard/web/vacuity.py`, with
   a regression test). Verified on the recorded theories: the fix removes exactly that finding in
   exactly those 13 passes and changes nothing else. The 13 passes are rerun at the fixed commit.
2. **FDR4 memory kill.** Chemical Detector cold run 3 was killed at the 4 GB FDR4 cap (peak 4.1 GB).
   A memory kill is an environment outcome. The cap is raised to 16 GB (the Isabelle cap) in
   `pipeline.yaml` and the pass is rerun.
3. **Generation failures of the published extractor are recorded as such, not fixed.** The
   extractor crashes in an expression helper on two LRE cold sources and one LRE stop point (M2M);
   the CSP generator emits ill-typed CSP for some Chemical Detector event payloads (FDR4 parse error
   on two stop points and one cold source); the Dafny generator types some real constants with
   integer initialisers and some real guards as booleans; the Isabelle generator leaves an event
   payload variable unbound in some operation definitions. Each is a defect or limit of the
   published pipeline on a model shape the Reference runs did not produce. They are not edited for
   this measurement; they count as *not converged* under the paper's criterion, and the paper and
   letter say that the failure is the pipeline's, not a verifier's verdict on the code.

The decision in "How the paper is updated" stands. Two observations are added to what the letter
reports: the archived Chemical Detector cold baseline's "0 of 10 compiled" was a JVM memory error on
the Windows host (`os::commit_memory` failed: paging file too small), not a code failure, and the
archived LRE Dafny failures were postconditions the archived generator wrote without the
higher-priority-branch negations (C1).
