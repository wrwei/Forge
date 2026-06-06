# Post-Codegen Review — chemical_detector iter 2

**Status:** complete

## Summary

Minimal fixes to GasAnalysisController driven by iter-1 fdr4 / dafny_verify /
isabelle_verify feedback. No other files touched.

## Issues

1. **design_choice** — `insAboveThr` predicate changed from
   `sensor.goreq(ins, THR)` to the direct comparison `ins >= DetectorConstants.THR`.
   The M2M cannot model a multi-arg sensor call inside a guard (it lifted the
   predicate to a mistyped `var insAboveThr : nat`, breaking FDR4 CSP typing,
   and the Dafny generator emitted a 1-param `goreq` signature against 2-arg
   call sites). The comparison is the definition of goreq (CD-Fn4), so the
   threshold check semantics (CD-GA-Beh6/7) are unchanged. `goreq` remains on
   GasSensorArray as the CD-Fn4 implementation.
2. **design_choice** — Added gas-triggered self-loops (with payload capture
   into `gs` and entry-action re-fire) to `Analysis` and `GasDetected`. The
   Isabelle `deadlock_free` closer (`metis St.exhaust_disc`) requires every
   mode to have at least one bare-precondition operation; both modes had only
   guarded autonomous exits, which made the closer time out (iter-1
   post_isabelle_verify fix directive). An event-triggered no-guard branch is
   the CLAUDE.md-preferred pattern (b); the unconditional-else alternative
   would introduce a τ-self-loop and a divergence risk. **Spec deviation:**
   CD-GA-FR3/FR4 do not specify gas self-loops on these transient states;
   re-reading a fresh sample while classifying is benign (recomputes
   sts/ins from the newer reading).
3. **design_choice** — `Located` self-loop now captures the gas payload into
   `gs` so the trigger rebinds to a correctly-typed variable (`gas ? gs`);
   iter 1 left the payload uncaptured, producing `trigger gas ? v` against the
   default `var v : real` — a latent CSP type mismatch behind the
   insAboveThr error.

## Next step

Re-run the pipeline; expect fdr4/dafny_verify/isabelle_verify to progress.
