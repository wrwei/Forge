# LRE — cold pass 1

**Written.** 15 Java files under `lre/`: `LreController` (one mode-nested `step`), `LreMode`, `LreConstants`, sealed `InputEvent`/`OutputEvent`, `Sensor` with `Obstacle` and a package-private `ObstacleRegister`, `Actuator` (`apply(OutputEvent)`), and the five operations, which own Var1–Var8. `step` runs the five `compute()` calls, then declares named predicates, then the transitions. The trace covers all 51 requirements (148 entries). The review is in `post_codegen.{md,json}` (13 items). There was no Java to delete. The code compiles with `javac -Xlint:all` and passes a scratch scenario check run outside the project.

**Decisions the requirements left open.**
- "-1 = none" contradicts the natural-number type, so `NO_OBSTACLE = 0` and obstacles are indexed from 1.
- Safe large distance: 1000000.0 m.
- HCM velocity: 0 m/s (FR3) wins over the system description's 0.1 m/s.
- CPA is horizontal: tcpa = p·c/(|c|²+ε), cda = √(hdist² − tcpa·p·c), with c = AUV velocity minus obstacle velocity. Using hdist means "no dynamic obstacle" gives a large cda and no CAM.
- ε = 1e-12 avoids division by zero. The model rounds it up to 1, so the model's cda is larger than the Java value; tcpa keeps its sign.
- CheckOPEZ and CalcCPA re-read `closestStaticIndex()`/`closestDynamicIndex()` from the sensor, because a `compute()` may only call the sensor.
- Priority: reqOCM, then inOpez, endTask, CAM, then HCM via Beh9, Beh11, Beh10 (Beh11 before Beh10, which would otherwise subsume it), then reqHCM. Later guards conjoin negations of the earlier guard-only branches.
- vvel is the signed rate of climb.
- Bare literals in the requirements became named constants.
- Unhandled events are ignored; `step(null)` means no input.

**Unresolved.** CAM's "evasive manoeuvre" has no specified output, so entering CAM issues nothing. 3-D CPA and |vvel| are open questions.
