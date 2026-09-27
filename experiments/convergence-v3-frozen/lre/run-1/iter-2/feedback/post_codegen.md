# post_codegen — LRE iteration 1

**status:** uncertain

**summary:** Cold codegen of the LRE safety controller from
`forge.assets/case-studies/lre/requirements/requirement_all.json`. 15 Java files
under `java.generated.project/src/main/java/lre/`; `./gradlew build` clean. All
50 requirements traced in `result_codegen.json`. Five points need human review.

## Issues

### 1. `ambiguous_requirement` — HCM entry velocity: 0 m/s or 0.1 m/s?
`system_description.txt` says HCM is "Reduced speed (0.1 m/s)"; LRE-FR3 says
"On entering HCM, the LRE advises a velocity of 0 m/s". Implemented **0.0**
(requirement_all.json is canonical per CLAUDE.md).
*fix_directive:* If 0.1 is intended, change every `advVel(0.0)` on a
MOM→HCM branch in `LreController.step()` to `advVel(0.1)`.
requirement_ids: LRE-FR3

### 2. `ambiguous_requirement` — no-dynamic-obstacle defaults for `nsRelDist`/`ewRelDist`
LRE-DM5 says all four field accessors return **zero** when no obstacle exists;
LRE-OP5 says the Sensor returns "zero-velocity **and large-distance** defaults".
Taken literally, DM5 makes `cda = 0` with no dynamic obstacle, which forces a
spurious and permanent CAM. Implemented OP5's reading: `nsRelDist`/`ewRelDist`
return `SAFE_LARGE_DISTANCE`, `obsNsVel`/`obsEwVel` return `0.0`.
*fix_directive:* Reconcile DM5 and OP5; if DM5 is authoritative, CalcCPA needs a
separate no-obstacle path.
requirement_ids: LRE-DM5, LRE-OP5

### 3. `invented_default` — safe large distance and the CPA denominator
No requirement gives the "safe large distance". Used `1000.0`
(`Sensor.SAFE_LARGE_DISTANCE`). CalcCPA divides by relative closing speed
squared, which is 0 when AUV and obstacle move identically; added
`MIN_CLOSING_SPEED_SQ = 1.0e-9` to the denominator because `compute()` may not
branch or use locals.
requirement_ids: LRE-DM5, LRE-OP5

### 4. `design_choice` — transition priority within each mode
The requirements assign no relative priority to transitions out of the same
mode. Chose: autonomous safety guards first (`inOpez` → OCM, then CAM), then the
remaining autonomous guards, then event-triggered branches. Triggerless-first
is also what preflight rule8 asks for; each triggerless branch textually negates
its predecessors (rule6).
requirement_ids: LRE-Beh5, LRE-Beh6, LRE-Beh7, LRE-Beh8, LRE-Beh9, LRE-Beh10, LRE-Beh11, LRE-Beh12

### 5. `design_choice` — ObstacleRegister as a dense list
LRE-DM3 specifies a partial function nat → Obstacle. Implemented as an
immutable `List<Obstacle>` with `has(index)` guarding lookup: the ETL maps
`List` to `Seq(...)` while `Map` degrades to `real`. Index gaps are therefore
not representable.
requirement_ids: LRE-DM3
