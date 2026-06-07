# Phase 2 — Interactive Code Generation (iter 1, cold) — chemical_detector

**Status:** complete

Cold codegen from canonical inputs only: 18 Java files under
`java.generated.project/src/main/java/chemdetector/`, two controllers
(GasAnalysisController, MovementController) with shared `turn`/`stop`/`resume`
events (SignalBus emissions name-matched to MovementEvent triggers), plus
`result_codegen.json` tracing all 61 requirement ids.

## Issues for user review

1. **design_choice — No Final mode in either controller.** CD-GA-Beh6's
   terminal transition emits `stop` then enters a live `Finished` mode
   (self-loop on gas) instead of a Final state, per CLAUDE.md's rule that the
   theory-generated controller must not contain `Final`. Movement's `Found`
   self-loops on `stop` (CD-MV-Beh9 "stays halted").
2. **design_choice — Finished self-loop re-captures the gas payload** into
   `gs` (typed-trigger binding) but performs no analysis: readings are
   consumed-and-ignored after conclusion.
3. **invented_default — Chem enum** `{chemA, chemB}`, target = `chemA`
   (CD-DM4 is opaque; equality only).
4. **invented_default — angle(x) mapping** 0→Front, 1→Left, 2→Right,
   ≥3→Back; empty reading → Front (safe default).
5. **invented_default — constant values** THR=1.0, LV=1.0, EVADE_TIME=1,
   STUCK_PERIOD=1, STUCK_DIST=1.0, OUT_PERIOD=1 (inside FDR4 {0..1} ranges).
6. **design_choice — analysis() criterion**: gasD iff some sample has
   `c == target && i > 0.0`.
7. **design_choice — changeDirection is an event in the model** (single-arg
   vehicle calls become Communications; only multi-arg become LOperations).
8. **design_choice — total guard cover** for autonomous-only modes
   (Analysis, GasDetected, AvoidingAgain) instead of self-loops.

**Next step:** run the deterministic pipeline and read the eight visible
phase feedback files.
