# PRE-REGISTRATION — condition F, Reference feedback with multi-controller generators

To be committed BEFORE any run of this condition. Written 2026-10-25; the pin
SHA is filled in by the operator at commit time (see "Pin").

## What F is

The Chemical Detector redone under the **Reference** feedback (the full
feedback of Section 3.3: diagnosis plus fix directive, codegen rule assets and
structural linter as published) with the Dafny and Isabelle generators
emitting **one file per controller**, so that both GasAnalysisController and
MovementController are verified by all three backends. It is condition C
(convergence-v3-frozen, b808424c) with the generators of condition E
(99952b9b and later), and nothing else changed on purpose.

Purpose: to replace the Reference Chemical Detector row of the paper's Table 8,
whose Dafny and Isabelle artefacts cover the first controller only, so that
every row of Table 8 verifies every controller and the Reference /
Diagnosis-only comparison for the Chemical Detector is single-factor again.

## Pin

`FORGE_SHA=5f403301f0e9f469d79dcaf9dc4b421adc885625` — the HEAD of `formal_method_guided_vibe_coding` on
the run machine at the time of this commit, which must be the pipeline as
published in the Forge repository. Before writing the SHA in, the operator
confirms at that commit:

1. `git merge-base --is-ancestor 99952b9b16eab1ec09877efdbb2d5efa69ad9d48 HEAD`
   exits 0 (per-controller generators present).
2. `grep -c 'ABLATION (condition' forge.assets/prompts/java_codegen_rules.txt
   forge.assets/prompts/codegen_trace_rules.txt` reports 0 for both files, and
   `bash scripts/verify_ablation_instrument.sh <worktree> <sha>` does NOT
   report an ablated instrument (this condition is the un-ablated one; a tree
   that passes the D2 verification is wrong for F).
3. The extractor run on `reference-runs/chemical_detector/java` emits two
   `.dfy` files and two `.thy` theories (one per controller).
4. `forge.transformations/`, `forge.dashboard/` and `forge.assets/prompts/`
   at the pin are byte-identical to the Forge repository's published copies
   (the row is meant to measure the shipped pipeline).

If any check fails, fix the tree, commit, and re-pin; do not start a run.

## Design

- Study: chemical_detector ONLY. SRanger and LRE have one controller, so their
  per-controller artefacts equal the first-controller ones (verified for
  condition E); re-running them would duplicate the condition-C samples.
- N = 5 cold runs, actor claude-code, autonomous, iteration cap 7 — as C and E.
- fdr4 budget 9000 s / 10240 MB, applied by `setup_claude_run.sh` step B2 as
  for every collection; the `OK   fdr4 budget 9000s / 10240MB` line in the
  setup output is the evidence and its absence is a stop.
- Isolation: `FORGE_ARM_DIR=experiments/reference-multicontroller-f`,
  `FORGE_WT_PREFIX=forge-rmf`, `FORGE_STAGE_DIR=/Users/ranwei/Gitee/rmf-staging`,
  `FORGE_SHA=5f403301f0e9f469d79dcaf9dc4b421adc885625`. Prompts `review/reference-f-prompts/f-cold-chemical_detector-run<k>.md`,
  generated from the condition-C prompts `frozen-cold-chemical_detector-run<k>.md`;
  the diff is (i) the collection block (worktree, four exports, preflight),
  (ii) the condition line in AUTONOMOUS MODE, (iii) the hard-constraint line
  on driving the loop, taken verbatim from the condition-E prompts, and (iv)
  the forbidden-read entry for `forge.transformations/**` and
  `forge.dashboard/**`, also taken verbatim from the condition-E prompts.
  Nothing else differs from C.
- Item (iv) is a deliberate departure from C: the condition-C prompts did not
  forbid reading the pipeline's own source, the D2/E prompts did. F adopts the
  prohibition because the row it produces is compared with the D2/E rows; the
  paper's threats section must then say that the Reference Chemical Detector
  runs, like the Diagnosis-only runs, were told not to read the pipeline
  source, while the Reference SRanger and LRE runs were not.
- Setup derives condition `F-reference-multicontroller` from the collection
  directory (patch to `setup_claude_run.sh` committed with this note) and
  refuses unknown collections, as before.

## What F can and cannot show

- CAN: whether cold convergence under the full feedback holds when both
  Chemical Detector controllers are verified by all three backends; the
  iteration counts and proof cost for that configuration.
- CANNOT: anything about SRanger or the LRE (unchanged instrument for them);
  anything about the diagnosis-only question (that is D2/E).

## Reporting

- The five runs replace the Reference Chemical Detector row of Table 8 as
  measured: converged count, per-run iterations, median (range), cold count.
  A non-converging run is reported as such; nothing is pre-written.
- The Diagnosis-only Chemical Detector row stays the condition-E runs.
- With both Chemical Detector rows per-controller, the Table 8 caption drops
  the first-controller/both-controllers distinction, Section 5.6(f) states the
  first-controller scope for the Table 7 runs only, and Section 5.6(m) drops
  the verified-scope caveat and gains the read-prohibition note above.
- Per-run records: `experiments/reference-multicontroller-f/chemical_detector/run-<k>/`.

## Stop rules

- Any FAIL line in the actor's preflight or the operator's checks: do not start.
- A run whose transcript shows a read of a forbidden path is discarded and
  re-run under a fresh number, as in C/D2/E.
- The instrument is not edited mid-collection; an instrument defect found by a
  run is reported, the collection is stopped, and a new condition is
  pre-registered.

**Verifier note (2026-10-25).** The pin above is the pipeline; the operator-side
setup checks (`scripts/setup_v2_run.sh`) were adjusted after the pin was fixed, in
a later commit on the same branch, because they identified repairs by internal
comment labels that the published pipeline does not carry. The three pipeline
trees at the pin are unchanged and remain byte-identical to Forge `09e67fca`.

## Re-pin (2026-10-25)

Run 1 at the first pin (`6b78ebe8f3325aa1d8bd815c66337a807e3bd344`) reached iteration 1 and
produced no verdict: every Gradle-backed phase failed with `Permission denied:
forge.transformations/gradlew`. The published Forge copy stores that file without
the executable bit (mode 100644, since the June 2026 export) and the pin copied
the mode. The actor stopped without editing the instrument. Per the stop rule the
collection was stopped; the mode was corrected in Forge and on the research
branch (a file-mode change, no byte of any pipeline tree changed), and the
condition is re-pinned at `5f403301f0e9f469d79dcaf9dc4b421adc885625`. The discarded attempt is kept
under `review/condition-f-discarded-run1-6b78ebe8/` and contributes to no figure.
The pin checks gained a fifth: every file the research main branch stores as
executable is executable at the pin, and one phase runs through `./gradlew`.
Design, prompts, prohibition and N are unchanged; run numbering restarts at 1.
