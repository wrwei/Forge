# LRE pass 8, iteration 1

**What was written.** The pass wrote 15 Java files under `lre/`:
- events: sealed `InputEvent` and `OutputEvent`;
- data: `Obstacle` and `ObstacleRegister`;
- `Sensor`, providing the DM5 and SF1–3 functions;
- `Actuator.apply(OutputEvent)`;
- five operations;
- `LreConstants`, `LreMode` and `LreController`.

`step()` runs the operations, then evaluates the named predicates (including `inOpez`), then a mode-nested chain. It holds 17 transitions, one per Beh requirement, plus the initial OCM. `result_codegen.json` traces all 51 requirements. The code compiles with no warnings, and 26 scenario checks pass.

**Decisions the requirements left open:**
- **"None" index.** 0, not -1, because cstc and cdyn are nats. Register identifiers start at 1.
- **Large distance.** 100000.0 m.
- **CPA.** Uses the closing velocity (AUV minus obstacle) and adds vdist, so the zero-valued "no obstacle" accessors don't produce cda = 0 and a spurious CAM. cda is measured at max(tcpa, 0); otherwise a receding obstacle keeps cda ≈ 0 and Beh18 never fires.
- **Divisor.** tcpa divides by |c|² + 0.000001. The model rounds this up to 1, which leaves tcpa's sign unchanged.
- **Closest index in operations.** CheckOPEZ and CalcCPA get it from the sensor; the profile's `compute()` cannot read another operation's result.
- **Transition order.** reqOCM, then OPEZ, then collision, then endTask/reqHCM, then autonomous HCM triggers. Lower branches negate the guard-only branches above them.
- **Events and values.** No input is passed as `step(null)`. HCM advises 0 m/s, per FR3; the system description says 0.1.

**Unresolved:**
- CAM's evasive manoeuvre has no specified output.
- Beh11 uses the signed rate of climb, so descending never triggers it.
- Beh13 undoes reqHCM at once. MOM and HCM cannot alternate with no event only while the default vertical distance ≤ the vertical distance.

All 14 points are listed in `post_codegen.{md,json}`.
