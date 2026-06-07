# Post-Codegen Review — lre (compile-only ablation run, iter 1)

**Status:** uncertain

**Summary:** Cold codegen of the LRE controller from
`requirement_all.json` + system description. 15 Java files under
`java.generated.project/src/main/java/lre/`. All requirements traced in
`result_codegen.json`. Five issues flagged for review: two invented
defaults, two ambiguous requirements, one design choice.

## Issues

### 1. invented_default — NO_OBSTACLE_DISTANCE = 1000.0
- **Raw:** LRE-DM5 requires distance functions to return "a safe large
  distance" when no obstacle exists but does not give the value. Used
  1000.0 (`Sensor.NO_OBSTACLE_DISTANCE`).
- **Fix directive:** Confirm 1000.0 is an acceptable "safe large
  distance"; all guard thresholds are ~1.0 so any value ≫ 1 behaves
  identically.
- **Trace:** Sensor.java / NO_OBSTACLE_DISTANCE — [LRE-DM5]

### 2. ambiguous_requirement — DM5 vs OP5 no-obstacle accessor defaults
- **Raw:** LRE-DM5 says field accessors nsRelDist/ewRelDist/obsNsVel/
  obsEwVel return ZERO when no obstacle exists; LRE-OP5 says the Sensor
  layer returns "zero-velocity and LARGE-DISTANCE defaults" for the
  CalcCPA computation. Zero relative position would make cda = 0 and
  spuriously trigger MOM→CAM with no dynamic obstacle present. Resolved
  in favour of OP5: position accessors (nsRelDist, ewRelDist) return
  1000.0; velocity accessors (obsNsVel, obsEwVel) return 0.0.
- **Fix directive:** Confirm the OP5 reading. If DM5's literal "return
  zero" is required, CalcCPA needs an explicit no-dynamic-obstacle
  branch instead.
- **Trace:** Sensor.java / nsRelDist, ewRelDist — [LRE-DM5, LRE-OP5]

### 3. invented_default — CalcCPA formula and separating-obstacle case
- **Raw:** LRE-OP5 names cda/tcpa but gives no formulae. Used standard
  2D CPA: tcpa = -(p·v)/(v·v) with v = obstacle velocity − AUV velocity;
  cda = |p + v·tcpa| when tcpa ≥ 0, else current horizontal distance.
  When relative speed is zero, tcpa = -1.0 (sentinel meaning "no
  approach"), which keeps MOM→CAM (requires tcpa ≥ 0) disabled.
- **Fix directive:** Confirm the horizontal-plane CPA formulation and
  the tcpa = -1 convention for non-approaching geometry.
- **Trace:** CalcCPA.java / compute — [LRE-OP5]

### 4. ambiguous_requirement — CAM entry action unspecified
- **Raw:** LRE-FR4 says CAM "performs evasive manoeuvres" but specifies
  no advisory output on entry (unlike MOM advVel(1) and HCM advVel(0)).
  No entry action is emitted on entering CAM.
- **Fix directive:** Confirm CAM has no entry advisory; if an evasive
  velocity/heading should be advised, specify it.
- **Trace:** LreController.java / step — [LRE-FR4]

### 5. design_choice — transition priority order within MOM/HCM
- **Raw:** Requirements give no explicit priority among a mode's
  transitions. Chose: operator reqOCM/endTask first, then autonomous
  safety (inOpez, →CAM collision, →HCM proximity Beh9/10/11), then
  operator reqHCM last. LRE-Beh4's "greater than 1" distances are coded
  as literal 1.0 (not minSafeDist) per the requirement text.
- **Fix directive:** Confirm the inner if-else ordering and the literal
  1.0 in the OCM→MOM guard.
- **Trace:** LreController.java / step — [LRE-Beh4, LRE-Beh6, LRE-Beh7, LRE-Beh12]

**Next step:** Run the pipeline (compile → coverage → preflight → t2m →
m2m → m2t → dafny_gen → isabelle_gen) and iterate on the visible
feedback.
