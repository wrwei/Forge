# Case Study: Chemical Detector

An autonomous mobile robot whose mission is to locate the source of a target
chemical (e.g. a gas leak) in an unknown environment that may contain
obstacles. Two cooperating controllers:

- **`GasAnalysisController`** (5 modes: Reading, Analysis, NoGas, GasDetected, Final) — classifies gas-sensor readings
- **`MovementController`** (8 modes including obstacle-avoidance recovery) — controls the robot's motion

This is the most structurally complex of the three case studies: shared state on a package-level `Ctrl_State` interface, typed event payloads (`Gas` carrying a `List<GasSensor>`), record-valued sensor data (`GasSensor` with `Chem` and `Intensity`), and a terminal mode (`Final`) whose handling forced an explicit deadlock-freedom design rule.

## Summary

| Metric                              | Value |
| ----------------------------------- | ----- |
| Controllers                         | 2     |
| Operating modes (total)             | 13    |
| Transitions                         | 36    |
| Requirements                        | 81    |
| Java files                          | 23    |
| Java LOC                            | 1,148 |
| Snapshot from iteration             | 9 (one trajectory; see note) |
| Dafny verification                  | 9 verified, 0 errors (Dafny 4.11.0 + Z3 4.15.4, re-run 2026-08-15; an earlier revision of this row said 7, predating the archived artefact's final state) |
| FDR4 (CSP)                          | deadlock-free, divergence-free pass; `[deterministic]` fails on `tick.in` (expected nondeterminism from overlapping else-if guards — see note below) |
| Isabelle/UTP build wall-clock       | ~18 s |

> **Snapshot note.** This artefact is from one converged trajectory; the
> iteration index above is that trajectory's converging iteration, not a
> headline convergence figure. The paper reports convergence as a distribution
> over nine independent runs (median 3, range 2--4).

## Source

ICECCS 2023 case-study archive — see `docs/archive/ICECCS2023/` in the parent
repository for the original RoboChart reference model and Z-machine theories
that the converged extraction was cross-checked against.

## Layout

```
chemical_detector/
  system-description/system_description.md   ← what was handed to the LLM
  requirements/requirement_all.json          ← 81 structured requirements
  java/                                      ← converged Java source (Phase 2 output)
  formal-artefacts/
    dafny/ChemDetectorApp.dfy                ← Phase 5a output
    csp/robochart_controller.rct             ← Phase 5b intermediate
    csp/csp-gen/                             ← Phase 5b CSP-M scripts (50 files)
    isabelle/GasAnalysisController_Beh.thy   ← Phase 5c output (Z-machine for the gas-analysis controller)
    isabelle/ROOT                            ← Isabelle session config
```

> **Note on `[deterministic]`.** The Java `else if` chains in
> `GasAnalysisController` encode implicit branch priority that the
> RoboChart M2M translation drops — branches become concurrent guarded
> transitions, not prioritised ones. The resulting nondeterminism on
> `tick.in` is therefore **expected** and informational, not a model
> defect. See `forge.assets/prompts/fdr4_system.txt` in the parent
> repo and the paper's RQ2 worked-example for the full discussion.

> **Note on the Isabelle theory.** The Z-machine encoding targets
> `GasAnalysisController`; the movement controller is
> verified through the CSP path. The deadlock-freedom proof on the gas
> analysis controller (over its terminal `Final` mode) was the example
> that motivated the "every state needs a bare-precondition outgoing
> transition" design rule documented in the paper.

## Reproducing verification

```bash
# Dafny (~10 s)
dafny verify formal-artefacts/dafny/ChemDetectorApp.dfy

# FDR4 (~60 s)
"<FDR4_PATH>/refines.exe" formal-artefacts/csp/csp-gen/defs/GasAnalysisController_coreassertions.csp

# Isabelle/UTP (~18 s)
isabelle build -D formal-artefacts/isabelle -v -o timeout=600
```

## License

MIT — see [`../../LICENSE`](../../LICENSE).
