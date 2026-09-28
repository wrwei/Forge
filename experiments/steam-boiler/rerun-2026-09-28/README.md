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
| Isabelle, deadlock-freedom | predicted to fail (EMERGENCY_STOP has no outgoing operation) | **pair completed**: the regenerated theory (with `until "st = EMERGENCY_STOP"`) **verifies in full** -- all 38 invariant-preservation lemmas and `BoilerController_deadlock_free` -- in 2051 s wall / 51 min CPU (`verify/isabelle/arm1_BoilerController_Full_Check.log`; no sorry, no quick_and_dirty). The September-encoding baseline proves the same 38 preservation lemmas (NoDlf, 950 s) but its deadlock-freedom obligation is false at the terminal state (exemption predicate `(%s. False)`; residual goal in `verify/isabelle/arm0_BoilerController_DlfOnly_Check.log`), and the stock tactic does not terminate on the false goal within 5400 s (`arm0_BoilerController_Full_Check`, recorded as a non-verdict timeout). |

## The headline result, and a correction

Same machinery, one changed clause, opposite verdicts on exactly the obligation
the repair targets: the September encoding's deadlock-freedom obligation is
false at the terminal state (pre-registered expected-faithful outcome, now
measured), and the regenerated encoding's exemption closes it -- the full
theory verifies with the generator's stock tactics, unmodified.

Correction to an earlier version of this README: we previously recorded a
"per-mode coverage goal exceeds the proof budget" ceiling. That measurement was
an artefact of our isolation variants, which stripped the individually-proved
`_inv` preservation lemmas that the `deadlock_free` method consumes (via the
`hoare_lemmas` attribute set); the method was left with 37 open Hoare subgoals
no closer could discharge. The unmodified generated theory has the correct
architecture and verifies. The isolation-ladder theories and their failure
logs are retained under `verify/isabelle-ladder-*` as the record of that
detour. Environment requirements for reproduction: 64-bit Poly/ML with a 6 GB
heap the Docker VM can back (Docker Desktop set to 8 GB / 8 CPUs; threads=8),
per-goal timeout 5400 s. September's 32-bit runs and the earlier failures in
this folder were environment faults, not verdicts.

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
