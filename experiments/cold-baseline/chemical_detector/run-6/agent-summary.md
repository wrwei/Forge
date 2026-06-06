# Chemical Detector — Cold-Baseline Run 6 Summary

Single-shot Java implementation of the Chemical Detector case study (see
`requirement_all.json`). Two controllers wired together:

- `GasAnalysisController` (modes Reading, Analysis, NoGas, GasDetected, J1)
  with state vars `gs`, `sts`, `ins`, `anl`. Entry actions for Analysis and
  GasDetected are inlined at the mode transitions. Issues `Stop`, `Resume`,
  `Turn` to the movement subsystem by direct method call (`movement.receive`).
- `MovementController` (modes Waiting, Going, Avoiding, TryingAgain,
  AvoidingAgain, GettingOut, Found, J1) with state vars `a`, `l`, `d0`, `d1`
  and clock-driven stuck-detection via `SystemClock.nowMs() - T`.

Other elements: domain types (`Status`, `Angle`, `Loc`, `Chem`, `Intensity`,
`GasSensor`), `Constants`, sensor service exposing `analysis/intensity/
location/angle/odometer`, `Vehicle` actuator with `move/randomWalk/
shortRandomWalk/pause(@RoboChartWait)`, `ChangeDirection` operation
(CD-OP4), `SystemClock` (@Clock).

All controller predicates are named booleans evaluated before the
mode-nested if-else; no ternaries, lambdas, streams, or pattern instanceof.
High-priority `stop`/`resume` overrides are duplicated in each movement
mode block. Stuck-detection guards (`makingProgress`, `stuckDetected`) match
the spec exactly.

Invented defaults: `lv=1.0`, `thr=10.0`, `evadeTime=2`, `stuckPeriod=5`,
`stuckDist=1.0`, `outPeriod=3`; sensor-index-to-angle mapping cycles
Front/Left/Back/Right.
