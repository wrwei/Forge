# Iteration 1 — chemical_detector

## What was written

19 Java files under `chemical_detector/`. There are two controllers, each with
its own `step`. `GasAnalysisController` has modes Reading, Analysis, NoGas,
GasDetected and Concluded. `MovementController` has modes Waiting, Going, Found,
Avoiding, TryingAgain, AvoidingAgain, GettingOut and Halted.

The other files: domain enums and records; sealed `InputEvent`/`OutputEvent`;
`GasSensorArray` (analysis, intensity, location, goreq); `OdometerSensor`;
`Clock`; `VehicleActuator`; `CommandLink` (signals between the controllers);
`ChemicalDetector` (wiring); `DetectorConstants`. All 81 requirements are traced.

## Decisions the requirements left open

- **Constants:** THR=5.0, LV=1.0, EVADE_TIME=1, STUCK_PERIOD=2,
  STUCK_DIST=2.0, OUT_PERIOD=1. All are whole numbers, so model rounding
  changes nothing. HALT_VELOCITY=0.0 is used for halting.
- **Types:** Intensity is a real, so thr is numeric. The GasSensor record is
  named `GasSample`, because "sensor" in a name marks a sensor service. Chem is
  a record with a nat id.
- **analysis:** a sample counts if it is of the target chemical (a constructor
  parameter) and has positive intensity. The peak is taken over all samples.
  Positions 1–4 map to Left/Right/Back/Front, repeating; ties go to the first.
  An empty reading gives 0.0 or Front.
- **Operations:** the four operations are actuator methods, not `compute()`
  classes. The odometer is a sensor method, not an event.
- **Shared events:** turn, stop and resume are identically named OutputEvent
  (sent) and InputEvent (received) records.
- **randomWalk** runs on every entry to Waiting except the initial one.
- **Final states:** Concluded is terminal; Found → Halted is unconditional.
- **Initial values:** sts=noGas, ins=0, anl=a=Front, l=front, d0=d1=0.

## Unresolved

Whether the extractor pairs the duplicated records into shared events, and
whether it accepts mode blocks with no inner `if` (NoGas, Found). Details are
in `post_codegen.md`.
