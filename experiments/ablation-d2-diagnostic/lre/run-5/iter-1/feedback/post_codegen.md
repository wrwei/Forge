# post_codegen — LRE, iteration 1 (cold codegen)

**status:** uncertain

**summary:** 15 Java files written under `java.generated.project/src/main/java/lre/`
covering all 51 requirements of `requirement_all.json`. The project compiles
cleanly. Five items need human review: a contradiction between LRE-DM5 and
LRE-OP5 over the no-dynamic-obstacle default, an unspecified CPA formula, an
unspecified CAM evasive action, the `-1` index sentinel against the extractor's
unconditional `int -> nat` mapping, and the placement of the LRE-Var* variables.

## Issues

### 1. `ambiguous_requirement` — no-dynamic-obstacle default contradicts itself

**raw:** LRE-DM5 states that when no obstacle exists the field accessors
`nsRelDist`, `ewRelDist`, `obsNsVel`, `obsEwVel` return **zero**, while LRE-OP5
states the Sensor "returns zero-velocity and **large-distance** defaults when no
dynamic obstacle exists". Taken literally, a relative position of (0, 0) makes
the closest distance of approach zero, so `cda < minSafeDist` holds and the LRE
would enter CAM whenever there is no dynamic obstacle at all — the exact
opposite of the intended safe default.

**fix_directive:** Confirm that `cda` must fall back to the *overall/horizontal*
distance (which LRE-DM5 does define as a large safe value) rather than being
reconstructed from the zeroed field accessors. The implementation takes
`hdist(cdyn)` as the magnitude of the relative position, so the large-distance
default propagates into `cda` while the field accessors keep LRE-DM5's zero.

**java_trace:** `src/main/java/lre/operation/CalcCPA.java` `compute` —
requirement_ids: LRE-OP5, LRE-DM5, LRE-Var7, LRE-Beh8, LRE-Beh14, LRE-Beh18

### 2. `invented_default` — CPA formula and its degenerate case

**raw:** LRE-OP5 names `cda` and `tcpa` but gives no formula. The standard
constant-velocity CPA relations were used: with relative position `r` and
relative velocity `v = obstacleVel - auvVel`, `tcpa = -(r.v)/|v|^2` and
`cda = sqrt(|r|^2 - (r.v)^2/|v|^2)`. `|v|^2` is floored by `1.0e-9` so the
quotients stay defined when the AUV and the obstacle are not closing; in that
case `tcpa` is 0 and `cda` collapses to the present horizontal distance.

**fix_directive:** Confirm the CPA is horizontal-plane only (depth/rate-of-climb
are excluded), and confirm that a non-closing pair should yield `tcpa = 0`
rather than an undefined or infinite value.

**java_trace:** `src/main/java/lre/operation/CalcCPA.java` `compute` —
requirement_ids: LRE-OP5, LRE-Var7, LRE-Var8

### 3. `unimplemented` — CAM issues no evasive command

**raw:** LRE-FR4 says that in CAM "the AUV performs evasive manoeuvres", but
LRE-DM7 restricts the LRE to exactly two outputs (`advVel`, `advHdng`) and no
requirement gives the velocity or heading an evasive manoeuvre should advise.
Nothing is emitted on entry to CAM; only the exit transition LRE-Beh18 advises
`advVel(0)`, as that requirement specifies.

**fix_directive:** Supply the advised velocity and/or heading for CAM entry, or
confirm that CAM entry is silent and the manoeuvre is the autopilot's
responsibility.

**java_trace:** `src/main/java/lre/controller/LreController.java` `step` —
requirement_ids: LRE-FR4, LRE-Beh8, LRE-Beh14

### 4. `design_choice` — `-1` sentinel for `cstc` / `cdyn` under `int -> nat`

**raw:** LRE-DM5, LRE-Var5 and LRE-Var6 specify `-1` as the "no obstacle" index.
CLAUDE.md records that the extractor maps every Java `int` to RoboChart `nat`
unconditionally, so `-1` is not representable in the extracted model. The
sentinel is kept in the Java (the requirement is explicit) but no controller
predicate ever tests it: the Sensor returns a large distance for any index it
does not hold, so `odist(-1)`, `hdist(-1)` and `vdist(-1)` are all safe, which is
also what the "no sentinel checks" codegen rule demands.

**fix_directive:** No code change proposed. If the formal model must represent
"no obstacle" explicitly, the requirement needs a non-negative encoding instead
of `-1`.

**java_trace:** `src/main/java/lre/sensor/Sensor.java` `closestStaticIndex` —
requirement_ids: LRE-DM5, LRE-Var5, LRE-Var6, LRE-OP3, LRE-OP4

### 5. `design_choice` — where the LRE-Var* variables live

**raw:** LRE-Var1..8 say "the controller maintains" each variable, while the
operations compute them. `hvel`, `vvel`, `vel`, `cstc`, `cdyn`, `cda` and `tcpa`
are controller fields refreshed from the operations at the head of `step()`, so
the guards read plain identifiers (`vel <= 1.0`, `sensor.odist(cstc) > 1.0`).
`inOpez` is the one exception: LRE-GP1 asks for a *named predicate* `inOpez` in
`step()`, and a controller field of the same name would shadow it, so `inOpez`
is held by `CheckOPEZ` and bound to the named predicate each cycle.

**fix_directive:** Confirm this split. The alternative — making `inOpez` a
controller field and naming the predicate something else — is equally valid and
changes which interface the extracted variable lands in.

**java_trace:** `src/main/java/lre/controller/LreController.java` `step` —
requirement_ids: LRE-Var1, LRE-Var2, LRE-Var3, LRE-Var4, LRE-Var5, LRE-Var6, LRE-Var7, LRE-Var8, LRE-GP1

## next_step

Run the pipeline (compile -> coverage -> preflight -> T2M -> M2M -> M2T ->
verifiers). No blocking uncertainty; all five issues are questions for the
requirement owner, not defects that stop extraction.
