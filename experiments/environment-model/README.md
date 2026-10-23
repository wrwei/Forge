# Environment model of the Isabelle chain

Evidence for the paragraph *"The environment the Isabelle theories assume"* in
Section 5.5 of the TOSEM revision.

- `preregistration.md` — the finding, the model, the probe design, the
  decision rule (written before any proof ran), the four probe iterations, and
  the decision (20 October 2026).
- `results/2026-10-18-run1` … `2026-10-20-run6-decision/` — every probe run:
  `SUMMARY.txt`, the Isabelle build logs, and (run 6) the ten proved theories
  (`*_B3_NoDlf.thy`: invariant lemmas only; `*_B3_S1.thy`: with the totality
  lemma).
- `regenerated/<study>/` — the theories the patched generator produces from the
  frozen runs' `robochart_model.xmi`; `scripts/verify_regenerated.sh` proves
  them (`results/2026-10-20-run7-regenerated/`: all seven PASS).
- `scripts/` — the model-A→B theory rewriter used by the probe, the probe
  runner, the regeneration driver and the verifier. They run against the
  pipeline's Docker Isabelle (`scripts/isabelle-docker.sh` in the toolchain).
- The toolchain in this repository (`forge.transformations/.../thy_generation_rule.egl`,
  `forge.dashboard/web/vacuity.py`, `vacuity_sat.py`) carries the repaired generator
  and the environment-aware Phase 6d check; its output on the five controllers is
  identical to `regenerated/` up to the order of set elements.
- `tierB-modelB/` — the guard-level conformance enumeration (Section 5.5) on
  theories the repaired generator extracts: 100% on all three studies, and
  exactly one operation enabled in every one of the 7,328 cells.
- `mutation-modelB/` — the mutation campaign's Isabelle column (Section 4.8,
  Table 9) re-run under model B: 131 builds, every one a verdict; the 11
  archived timeouts all verify; the one archived kill is the one kill; no
  verdict changed.

Summary of the decision run (all builds `Finished`, no `oops`, no warnings):

| controller | shipped theory (model A) | model B, invariant lemmas | model B, totality lemma |
|---|---|---|---|
| LRE | PASS 166 s | PASS 211 s | PASS 267 s |
| SRanger | PASS 40 s | PASS 71 s | PASS 75 s |
| Gas analysis | PASS 56 s | PASS 85 s | PASS 76 s |
| Movement | PASS 236 s | PASS 734 s | PASS 296 s |
| Steam Boiler | PASS 1240 s | PASS 858 s | PASS 1793 s |

Dead operations under the superseded model A, counted by the Phase 6d check
`precondition_unsat_env` over the 64 theories in this repository that carry the
`offered = listens` conjunct (35 affected): LRE 9 of 18 in four of five
Reference runs (run 1's Java emitted no absence conjuncts); SRanger 1 of 8;
Movement controller 2 of 23–28; Gas analysis and Steam Boiler none.
