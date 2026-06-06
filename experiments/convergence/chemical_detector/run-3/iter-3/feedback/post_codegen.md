# Post-Codegen Review — chemical_detector iter 3

**Status:** complete

## Summary

Reshaped the GasAnalysisController self-loops from iter-2 feedback: dropped
the gas-capturing self-loops (Dafny postcondition preemption + Isabelle
duplicate `SeqGs` constant) in favour of a payload-less `Tick` input event.
Files touched: InputEvent.java, GasAnalysisController.java.

## Issues

1. **design_choice** — Added `InputEvent.Tick` (payload-less control-cycle
   tick). The Isabelle theory generator emits one `definition Seq<Var>` per
   gas-payload-capturing transition with no dedup (template PART 2), so only
   ONE transition machine-wide may capture `gas ? gs` (Reading→Analysis).
   The bare-precondition self-loops that Analysis/GasDetected/Located need
   for the `deadlock_free` closer therefore trigger on `Tick` instead.
   **Spec deviation:** Tick is not in the requirements; it is a verification
   harness event (documented pipeline pattern for this trilemma).
2. **design_choice** — In Analysis and GasDetected the autonomous guarded
   branches now come FIRST and the Tick self-loop LAST. The Dafny generator
   emits unconditional `ensures <guard> ==> mode == <target>` for autonomous
   transitions and replicates the Java branch order, so an event branch ahead
   of the guards creates a violating return path; placed last it is dead code
   in Dafny (Status/boolean guards are exhaustive) and the ensures prove.
   In the RoboChart model branch order is immaterial (transitions are
   concurrent choices), so the Tick self-loop still yields the bare
   `st = <Mode>` precondition Isabelle needs.
3. **design_choice** — `Located` self-loop no longer stores the latest gas
   reading (Tick-triggered, no capture); the spec assigns `gs` only on the
   Reading→Analysis transition (CD-GA-Var1), so this is spec-neutral.

## Next step

Re-run the pipeline; expect dafny_verify and isabelle_verify to progress.
fdr4 must stay green (Tick adds an offered-but-ignored boundary event).
