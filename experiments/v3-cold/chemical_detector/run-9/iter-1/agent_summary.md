# Iteration summary: chemical_detector, pass 9

## What was written

18 Java files under `chemical_detector/`. The worktree contained no Java beforehand.

- **Controllers.** `GasAnalysisController` has the states Reading, Analysis, NoGas, GasDetected and Concluded. `MovementController` has Waiting, Going, Found, Avoiding, TryingAgain, AvoidingAgain, GettingOut and Halted. Each has a mode-nested `step`.
- **Events.** The controllers communicate only through `Turn`, `Stop` and `Resume`, emitted as `OutputEvent`s and consumed as same-named `InputEvent`s.
- **Sensors.** `GasSensorArray` provides `analysis`, `intensity`, `location` and `goreq`. `OdometerSensor` provides `odometer()`, and a `Clock` times the evasion.
- **Actuators.** The `Vehicle` provides the movement operations, `pause` and `Flag`. `CommandLink` carries the gas side's commands.

`result_codegen.json` maps all 81 requirements and all 18 files. The code compiles with javac and Gradle, and a scratch scenario behaved as specified.

## Decisions the requirements left open

- **Constants:** THR=2.0, LV=1.0, EVADE_TIME=2, STUCK_PERIOD=5, STUCK_DIST=1.0, OUT_PERIOD=3. All whole numbers, so model rounding changes no guard.
- **Initial values:** sts=noGas, ins=0.0, anl=a=Front, l=front, d0=d1=0.0.
- **"Indicates the target":** c equals the target Chem and i is positive.
- **angle(x):** positions map cyclically to Left, Right, Back, Front.
- **Empty readings:** `intensity` returns 0.0 and `location` returns Front.
- **Operations:** move, randomWalk, shortRandomWalk and changeDirection are Vehicle methods, because an argument-less `compute()` cannot carry their parameters.
- **During action:** Waiting's `randomWalk()` is issued on every transition into Waiting, so nothing is issued at start-up.
- **Odometer:** a sampled sensor quantity, not an event.
- **Final states:** the terminal modes Concluded and Halted give each behavioural requirement one transition.
- **Types:** Intensity is a real number and Chem is a record.

## Unresolved

- `intensity` and `location` ignore which chemical a value is for (CD-Fn2, CD-Fn3).
- The AvoidingAgain progress rule is implemented literally.
- There is no Java host wiring class.

Details are in `post_codegen.md`.
