# post_codegen — LRE, iteration 1

**status:** uncertain

**summary:** All 51 LRE requirements are implemented across 15 Java files under
`java.generated.project/src/main/java/lre/`. The build is clean. Six points
required a judgement the requirements did not settle; none blocks the pipeline,
but each is worth a human glance.

## Issues

### 1. HCM entry velocity: 0 m/s (requirement) vs 0.1 m/s (system description)
- **kind:** `ambiguous_requirement`
- **raw:** LRE-FR3 says "On entering HCM, the LRE advises a velocity of 0 m/s".
  `system_description.txt` says HCM is "Reduced speed (0.1 m/s)".
- **fix_directive:** Implemented LRE-FR3 (0.0 m/s) because the structured
  requirement is the canonical input. If 0.1 m/s is intended, amend LRE-FR3.
- **java_trace:** `src/main/java/lre/controller/LreController.java` — `step`;
  requirement_ids: `LRE-FR3`.

### 2. CAM has no specified entry output
- **kind:** `ambiguous_requirement`
- **raw:** LRE-FR4 says CAM "performs evasive manoeuvres" but names no advised
  velocity or heading, and LRE-DM7 restricts outputs to advVel/advHdng.
- **fix_directive:** No output is emitted on entering CAM. If an evasive
  advisory is required, LRE-FR4 must state its value.
- **java_trace:** `src/main/java/lre/controller/LreController.java` — `step`;
  requirement_ids: `LRE-FR4`.

### 3. Safe large distance for absent obstacles
- **kind:** `invented_default`
- **raw:** LRE-DM5 requires odist/hdist/vdist to "return a safe large distance"
  when no obstacle exists, without giving a value.
- **fix_directive:** `Sensor.SAFE_LARGE_DIST = 1000.0` m. Any value well above
  the 1 m thresholds preserves the intended guard polarity.
- **java_trace:** `src/main/java/lre/sensor/Sensor.java` — `odist`;
  requirement_ids: `LRE-DM5`, `LRE-SF1`, `LRE-SF2`, `LRE-SF3`.

### 4. Relative-speed floor in CalcCPA
- **kind:** `invented_default`
- **raw:** LRE-OP5 does not define cda/tcpa when the relative speed is zero,
  which is exactly the "no dynamic obstacle and stationary AUV" case the same
  requirement pushes into the Sensor's zero defaults.
- **fix_directive:** `MIN_REL_SPEED_SQ = 1.0E-9` is added to the squared
  relative speed so tcpa stays finite instead of NaN. Negligible at operating
  scales; replace with an explicit specified behaviour if preferred.
- **java_trace:** `src/main/java/lre/operation/CalcCPA.java` — `compute`;
  requirement_ids: `LRE-OP5`, `LRE-Var7`, `LRE-Var8`.

### 5. cstc/cdyn are annotated `nat` but carry -1
- **kind:** `design_choice`
- **raw:** LRE-Var5/LRE-Var6 call cstc and cdyn natural numbers *and* give -1 as
  the "no obstacle" value. CLAUDE.md records that the ETL maps Java `int` to
  RoboChart `nat` unconditionally, so -1 is not representable in the extracted
  model.
- **fix_directive:** Followed the requirement literally: `int` fields annotated
  `@RoboChartType("nat")`, initialised to -1. The controller never tests for -1;
  the Sensor absorbs the absent-obstacle case via its safe defaults, so no guard
  depends on the unrepresentable value.
- **java_trace:** `src/main/java/lre/controller/LreController.java` — `cstc`,
  `cdyn`; requirement_ids: `LRE-Var5`, `LRE-Var6`.

### 6. Literal 1 vs named threshold in LRE-Beh4
- **kind:** `design_choice`
- **raw:** LRE-Beh4 says odist to cdyn and to cstc must be "greater than 1"
  without naming a threshold, while neighbouring requirements name
  minSafeDist / staticObs*Dist explicitly.
- **fix_directive:** Used the literal `1.0` where the requirement gives a bare
  number, and `LreConstants` only where a threshold is named. If LRE-Beh4 means
  minSafeDist, say so and the predicates become constant references.
- **java_trace:** `src/main/java/lre/controller/LreController.java` — `step`;
  requirement_ids: `LRE-Beh4`.
