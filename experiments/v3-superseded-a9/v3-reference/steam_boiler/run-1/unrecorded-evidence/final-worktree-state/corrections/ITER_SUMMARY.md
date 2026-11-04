# ITER_SUMMARY — steam_boiler, run 1, iteration 3 (run stopped)

**What I wrote.** The Java is in `steam_boiler/`. `SteamBoilerController` holds the five-mode logic, with every branch triggered by one per-cycle event, and the last branch of each mode tests the event only. `MessageReceiver` handles reception, failure detection, the level estimate and the worst-case forecasts. `Transmitter` composes the outgoing messages. Iteration 3 changes are listed in `post_codegen.md` (13 issues).

**Iteration 3 result.** compile, coverage, preflight, t2m, m2m, m2t, dafny_gen, isabelle_gen, dafny_verify and vacuity passed. fdr4 and isabelle_verify failed.

**Why the run stopped.** The FDR4 report directs a change to the instrument. Verbatim:
"FDR4 killed: memory usage exceeded 10240 MB limit (peak: 10311 MB)" and "Do NOT edit Java or templates for this issue. Either raise phases.fdr4.memory_limit_mb in pipeline.yaml (machine permitting) and re-run, or record the assertion set as unmeasured under the memory budget. Sequence- or record-typed channel domains are the usual driver."
The run protocol says to stop and report a directive to change `pipeline.yaml`, so iteration 4 was not run. If the operator allows a Java workaround instead, the likely one is the hint about record-typed channels: drop the record payloads of the unused `PumpState`/`PumpControlState` events.

**Isabelle (unaddressed).** At `zoperation` (line 141 of the generated theory), the predicates `plantWaiting`, `transmissionFault` and `steamNonZero` appear as unresolved `unit ⇒ ℝ` constants. The operation is now typed `ℙ Evt ⇒ …`. This first appeared after the guard-free fallback branches were introduced, so the generator probably builds the negated earlier guards for the fallback without inlining the predicates.

**Recorder.** Every iteration was refused: `snapshot_v2.py: error: argument study: invalid choice: 'steam_boiler' (choose from 'sranger', 'lre', 'chemical_detector')`.

**Unresolved.** Convergence was not reached.
