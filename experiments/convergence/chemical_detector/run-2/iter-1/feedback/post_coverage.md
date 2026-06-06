# Coverage (Requirement ↔ Java) — FAILED

## Summary
5 over-implementation(s)

## Run history
- New this run: 0
- Recurring from previous run: 5
- Resolved since previous run: 0

## Issues
### Issue 1: over_implementation — Public type 'Actuator' has no requirement trace [recurring x2]

**Raw**
```
chemdetector/actuator/Actuator.java: public type Actuator
```

**Java trace**
  - Java: `Actuator.java` (Actuator)

**Fix directive**
The public type 'Actuator' in chemdetector/actuator/Actuator.java is not mapped to any requirement in result_codegen.json. Either: (a) re-run /gen-trace if the mapping was missed, (b) the element implements an existing requirement that isn't yet captured — add it to /gen-trace's output, or (c) the element isn't required and should be removed.

### Issue 2: over_implementation — Public method 'apply' has no requirement trace [recurring x2]

**Raw**
```
chemdetector/actuator/Actuator.java: public method apply
```

**Java trace**
  - Java: `Actuator.java` (apply)

**Fix directive**
The public method 'apply' in chemdetector/actuator/Actuator.java is not mapped to any requirement in result_codegen.json. Either: (a) re-run /gen-trace if the mapping was missed, (b) the element implements an existing requirement that isn't yet captured — add it to /gen-trace's output, or (c) the element isn't required and should be removed.

### Issue 3: over_implementation — Public method 'lastEvent' has no requirement trace [recurring x2]

**Raw**
```
chemdetector/actuator/Actuator.java: public method lastEvent
```

**Java trace**
  - Java: `Actuator.java` (lastEvent)

**Fix directive**
The public method 'lastEvent' in chemdetector/actuator/Actuator.java is not mapped to any requirement in result_codegen.json. Either: (a) re-run /gen-trace if the mapping was missed, (b) the element implements an existing requirement that isn't yet captured — add it to /gen-trace's output, or (c) the element isn't required and should be removed.

### Issue 4: over_implementation — Public method 'nowMs' has no requirement trace [recurring x2]

**Raw**
```
chemdetector/sensor/Clock.java: public method nowMs
```

**Java trace**
  - Java: `Clock.java` (nowMs)

**Fix directive**
The public method 'nowMs' in chemdetector/sensor/Clock.java is not mapped to any requirement in result_codegen.json. Either: (a) re-run /gen-trace if the mapping was missed, (b) the element implements an existing requirement that isn't yet captured — add it to /gen-trace's output, or (c) the element isn't required and should be removed.

### Issue 5: over_implementation — Public method 'advance' has no requirement trace [recurring x2]

**Raw**
```
chemdetector/sensor/Clock.java: public method advance
```

**Java trace**
  - Java: `Clock.java` (advance)

**Fix directive**
The public method 'advance' in chemdetector/sensor/Clock.java is not mapped to any requirement in result_codegen.json. Either: (a) re-run /gen-trace if the mapping was missed, (b) the element implements an existing requirement that isn't yet captured — add it to /gen-trace's output, or (c) the element isn't required and should be removed.

## Files to review
- Angle.java
- GasAnalysisController.java
- GasAnalysisMode.java
- Loc.java
- MovementMode.java
- Status.java
- Vehicle.java

## Next step
Address each missing_implementation by implementing or tracing the requirement. For each over_implementation, decide whether the Java element should be removed or a requirement should be added. Use /fix-from-feedback.
