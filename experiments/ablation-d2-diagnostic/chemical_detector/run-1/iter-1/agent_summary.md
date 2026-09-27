# Iteration 1 — cold codegen (chemical_detector, run 1, condition D2)

**What I did.** The tree was cold, so there was nothing to delete. I wrote 19 files under `java.generated.project/src/main/java/chemical_detector/`, plus one JUnit test and `result_codegen.json`. The trace covers all 81 requirements in 107 entries. The Java has:

- **Two controllers,** each with a mode-nested `step()`:
  - `GasAnalysis`: Reading, Analysis, NoGas, GasDetected, Concluded.
  - `Movement`: Waiting, Going, Found, Avoiding, TryingAgain, AvoidingAgain, GettingOut.
- **Events:** `InputEvent` carries gas, obstacle, turn, stop and resume. `OutputEvent` carries turn, stop, resume and flag. The shared events use the same names in both interfaces.
- **Supporting classes:**
  - `VehicleSensors` (`@SensorService`) holds odometer, analysis, intensity, location and goreq.
  - `Vehicle` provides move, randomWalk, shortRandomWalk, changeDirection and pause.
  - `Actuator.apply` emits output events.
  - `Clock` supplies time; clock T is `timer.nowMs()`.
  - `ChemicalDetector` wires the two controllers together.
- **Verification:** the Gradle build succeeds and 9 behavioural tests pass.

**Decisions the spec did not settle:**
- **Terminal modes.** Neither "concludes" (GA) nor "remains stopped" (Found) says what happens to later inputs. Concluded consumes later gas readings and never classifies them. Found consumes later obstacle reports and never moves. Every mode keeps an outgoing transition. Neither mode is named `Final`.
- **AvoidingAgain guards** are `withinStuckPeriod || advancedBeyondStuckDist` and its exact negation. The two are disjoint and together cover every case.
- **Analysis guards** are `stsIsNoGas` and `!stsIsNoGas`. This is equivalent because Status has exactly two values.
- **Operations CD-OP1..4** are Vehicle methods, not `compute()` classes, because they are actuations.
- **The odometer** is a sensor read, not an input event.
- **Chem** is `record(nat id)` and **Intensity** is `real`.
- **Invented values:**
  - Constants: thr=3, lv=1, evadeTime=1, stuckPeriod=2, stuckDist=1, outPeriod=1.
  - analysis(): the reading must be for the target chemical with intensity > 0.
  - angle(position 1..4): Front, Right, Back, Left.

**Unresolved:** none yet. `post_codegen.md` lists the 7 review items.
