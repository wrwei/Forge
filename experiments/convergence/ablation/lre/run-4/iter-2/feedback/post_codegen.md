# post_codegen — iter-1 (lre, compile-only ablation, cold codegen)

**Status:** uncertain
**Summary:** All 51 LRE requirements implemented across 15 Java files (package `lre`). 4 design choices, 3 invented defaults, 1 ambiguous requirement flagged.

## Issues

### 1. design_choice — Event record names are lowercase to match requirement event names
`InputEvent`/`OutputEvent` variant records are named `reqVel`, `reqHdng`, `reqOCM`, `reqMOM`, `reqHCM`, `endTask`, `advVel`, `advHdng` (lowercase initial), matching LRE-DM6/LRE-DM7 verbatim so the extracted RoboChart event channels carry the spec names.
*Fix:* rename to idiomatic Java (`ReqVel`, …) if spec-verbatim channel names are not required.

### 2. ambiguous_requirement — HCM entry velocity: 0 m/s (LRE-FR3) vs 0.1 m/s (system description)
`system_description.txt` describes HCM as "Reduced speed (0.1 m/s)"; LRE-FR3 says "advises a velocity of 0 m/s" on entry. Implemented `advVel(0.0)` per `requirement_all.json` (canonical).
*Fix:* confirm 0 vs 0.1; change the four HCM-entry `advVel` sites in `step()` if needed. (LRE-FR3, Beh9–Beh12)

### 3. invented_default — CAM has no entry action
LRE-FR4's "evasive manoeuvres" names no concrete actuator command; no entry action is emitted on entering CAM. Only CAM output is `advVel(0)` on the Beh18 exit. (LRE-FR4, Beh8, Beh14)

### 4. design_choice — cda evaluated at max(tcpa, 0); tcpa reported raw
`Sensor.cpaDist` clamps the CPA evaluation time to now when the closest point lies in the past, so a receding obstacle yields the current (growing) distance and CAM can exit via Beh18. `Sensor.cpaTime` returns the raw (possibly negative) tcpa for the Beh8/Beh14 guards. (LRE-OP5, Var7, Var8, Beh18)

### 5. design_choice — Transition priority within MOM/HCM blocks
No relative priority is specified. MOM order: reqOCM > endTask > reqHCM > inOpez > CAM-entry > HCM-entries (Beh9 > Beh10 > Beh11). HCM order: reqOCM > inOpez > CAM-entry > MOM-return. Reorder inner branches if a different policy is intended.

### 6. design_choice — CheckOPEZ/CalcCPA re-derive the closest index from the Sensor
Operation `compute()` bodies are restricted to sensor calls + own fields, so they call `sensor.closestStaticIndex()`/`closestDynamicIndex()` directly — the same value CalcCStc/CalcCDyn store in the same step. (LRE-OP2, LRE-OP5)

### 7. design_choice — cstc/cdyn are plain int (RoboChart int), not nat
LRE-Var5/6 say "natural number" but -1 is a documented legal value, so the default int mapping is used instead of `@RoboChartType("nat")`.

### 8. invented_default — Safe large distance = 1.0e9 m
LRE-DM5 gives no value for the "safe large distance" missing-obstacle default. Chosen 1.0e9 (private to Sensor).

### 9. invented_default — Literal 1.0 thresholds in Beh4/Beh9/Beh11 guards
Requirements state these as the literal number 1; coded as literal `1.0`. Named `LreConstants` are used only where a requirement names the threshold (minSafeDist, staticObs*Dist).

## Next step
Run the deterministic pipeline (compile → coverage → preflight → t2m → m2m → m2t → dafny_gen → isabelle_gen) and iterate on the visible feedback files.
