# Iteration 1 — chemical_detector run 4 (cold codegen)

## What changed
- I wrote a new tree of 19 files under `chemical_detector/`. No Java existed beforehand, so nothing was deleted.
- There are two mode-nested controllers:
  - `GasAnalysisController`: READING, ANALYSIS, NO_GAS, GAS_DETECTED, CONCLUDED.
  - `MovementController`: WAITING, GOING, FOUND, AVOIDING, TRYING_AGAIN, AVOIDING_AGAIN, GETTING_OUT.
- Gas analysis sends turn, stop and resume through `MovementCommandChannel.deliver(new MovementEvent.X(..))`, so the event names match Movement's triggers.
- `result_codegen.json` traces all 81 requirements and covers all 19 files. `post_codegen.*` lists 11 review items.
- The code compiles cleanly under `javac -Xlint:all`. A scratch smoke test passed all 14 of its checks.

## Decisions the spec did not settle
- **No Final state in gas analysis.** It is the first-discovered machine, so it gets the Isabelle theory, and a Final state there makes deadlock-freedom unprovable. When `ins >= thr` it sends stop and moves to CONCLUDED. That state is live: it records later readings but does not analyse them.
- **Movement FOUND** replaces its successor Final state with an obstacle-triggered self-loop. Flag and halt are sent once, on the stop transitions.
- **Autonomous-mode guards cover every case:** `sts==noGas`/`sts==gasD`, `ins>=thr` and its negation, `makingProgress` and its negation. NO_GAS→READING is unconditional.
- **Odometer** is read as a sensor, not received as an input event.
- **`changeDirection(l)`** has one argument, so it is extracted as an output event. `move` becomes an operation call.
- **Invented values:** the constants (thr=3.0, lv=1.0, evadeTime=1, stuckPeriod=2, stuckDist=1.0, outPeriod=1), the initial values, the mapping from sensor position to Angle, and "indicates target" = `c==target && i>0`.

## Unresolved
- CD-Fn2 takes the peak over all sensors, whatever their chemical. Taken literally, that can differ from the chemical that analysis detected.
