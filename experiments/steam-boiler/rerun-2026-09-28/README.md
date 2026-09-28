# Steam Boiler rerun at the current pipeline (2026-09-28)

The RQ8 run (Section 4.10) used the frozen September profile. This folder is a
SUPPLEMENTARY rerun of the generation and verification on the SAME recorded
Java, using the current pipeline (the extractor with the C1/C2/T1 repairs and
the terminal-state exemption). It does not replace the RQ8 results; it measures
whether the repairs transfer to the held-out subject.

## Results

| backend | September (RQ8) | this rerun |
|---|---|---|
| Dafny | 11/11 (post-state guards, D1 postprocess) | **15 verified / 0 errors**; every mode-transition postcondition now evaluates its guard in the pre-state (`old(...)`), matching the Java. Same D1 postprocess (event datatype), see `formal-artefacts/BoilerController_postprocessed.dfy`. |
| FDR4, three operation machines | 2/3 + termination-benign fail, each | **identical verdicts** (arm0 = September tree, arm1 = regenerated tree; six runs in `verify/fdr4/`). The bare `P :[deadlock-free]` fail is termination (trace ends in tick); the `;RUN({r__})` and divergence-freedom assertions pass. |
| FDR4, full state machine | infeasible at 16 GB (pre-registered scope result) | confirmed: a full-machine attempt exceeded 40 GB and was stopped. Not rerun. |
| Isabelle, elaboration | never reached a verdict (32-bit ML heap faults) | **completes in 788 s** under 64-bit ML with a 6 GB heap (`verify/isabelle/arm0_BoilerController_Skeleton_Check.log`). New measurement. |
| Isabelle, deadlock-freedom | predicted to fail (EMERGENCY_STOP has no outgoing operation) | **measured**: the September encoding's obligation fails at the terminal state (arm0/DlfOnly, exemption predicate `(%s. False)`). The regenerated theory carries `until "st = EMERGENCY_STOP"`; the exemption instantiates, invariant establishment and all 37 Hoare preservation obligations discharge (V0), and the remaining per-mode coverage goal did NOT close within a 5400 s budget (V1 monolithic, V2 split per mode). |

## The open item, stated precisely

The coverage goal is mathematically true: every non-terminal mode's else-if
chain ends in a bare `else` self-loop, and the extracted theory carries the
corresponding default operation whose precondition is the negated conjunction
of the mode's other guards (`INITIALIZATIONToINITIALIZATION_3`,
`NORMALToNORMAL_2`, `DEGRADEDToDEGRADED_2`, `RESCUEToRESCUE_2`). What exceeds
the budget is the automated tactic on a 37-operation goal, not the property.
This is the same per-lemma proof-cost ceiling RQ8 reports, localised to a
single named goal. A manual per-mode witness script would likely close it; we
record the ceiling instead of hand-proving past it.

## Layout

- `formal-artefacts/` -- regenerated .dfy / .thy / .rct / csp-gen tree, plus
  `instantiations_tuned.csp` (September's domain narrowing, widened to cover
  pump ids 1..4 which the current generator folds into core_int/core_nat).
- `verify/fdr4/` -- six framed-json verdicts + SUMMARY; `verify/isabelle/` --
  build logs for Skeleton (both arms), DlfOnly (both arms), V0/V1/V2 + SUMMARY.
- `verify/isabelle-ladder-arm{0,1}/` -- the isolation-ladder theories
  (make_thy_variants.py output, plus DlfOnly and the V0/V1/V2 probe proofs).
- `verify/sb_fdr4_rerun.sh`, `verify/sb_isa_rerun.sh` -- the exact runners.

Reproducibility: the artefacts regenerate deterministically from
`../steamboiler_java_generated` inputs at the pipeline used for the Table 7
experiments; the CSP tree is byte-identical modulo the generation timestamp,
the theory and Dafny files byte-identical.
