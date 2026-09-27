# Iteration 1 — chemical_detector, run 1 (cold codegen)

`java.generated.project/src/main/java/` was empty; nothing was deleted. I wrote
17 Java files under `chemical_detector/`, `result_codegen.json` (107 rows, all
81 requirement ids, every file named), and `post_codegen.{md,json}`.
(`pipeline.yaml` already carried a raised FDR4 timeout/memory when the session
opened — not my edit.)

**Architecture.** Two controllers, per CD-ARCH2: `GasAnalysisController`
(Reading, Analysis, NoGas, GasDetected, Stopped) and `MovementController`
(Waiting, Going, Found, Avoiding, TryingAgain, AvoidingAgain, GettingOut),
sharing only `MovementBus` (turn/stop/resume). `Vehicle` holds the platform
operations, the odometer and `flag`; `GasAnalysisFunctions` holds
analysis/intensity/location/goreq so the extractor lifts them to RoboChart
functions; `Clock.nowMs()` drives the promoted clock and the `since(...)` guard.

**Decisions the requirements did not settle** (all nine itemised in
`post_codegen.md`); the load-bearing ones:

- CD-GA-Beh6 ends gas analysis in a *final* state. A `Final` state in the
  theory-generated machine makes deadlock-freedom unprovable, so it enters a
  live `Stopped` mode with a `gas`-triggered self-loop: `stop` is emitted once
  and no reading is classified again.
- Modes left by two autonomous transitions use complementary guards (`p`/`!p`)
  rather than two enum literals — a total guard cover, so no self-loop, and the
  Isabelle residual is `p or not p`. `Status` is two-valued, so this is
  semantically identical.
- `Chem` is opaque in CD-DM4; made `enum { Target, Other }`, with `analysis` =
  "some sensor reports Target". Intensity is `double`; constant values and the
  sensor-position→Angle map are invented. Odometer is a sensor read, not an
  event (CD-Evt3 says "samples"). `Found` accepts only `stop`.

**Unresolved.** Preflight rule8 will warn on `AvoidingAgain` (event branches
before autonomous ones). Both orders warn, and autonomous-first makes the M2M
conjoin unsatisfiable negations onto stop/resume, deleting CD-MV-Beh19/20 from
the model — so event-first stands.
