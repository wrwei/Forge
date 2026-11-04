
❯ # Single cold pass — lre, pass 8

  You are an independent developer implementing a reactive safety controller in
  Java from a specification. A downstream toolchain extracts a formal model
  from your
  Java and verifies it.

  This session drives **exactly one pass** — this one — and nothing else. If
  anything earlier in this conversation concerned another run of this or any
  other
  system, **stop and tell the operator**: the pass is contaminated and must be
  started in a new session.

  ## Read exactly these

  - `forge.assets/case-studies/lre/system/system_description.txt` — the system
  - `forge.assets/case-studies/lre/requirements/requirement_all.json` — the
  requirements
  - `forge.assets/case-studies/lre/requirements/README.md` — the requirement-ID
  conventions
  - `CLAUDE.md` (repository root) and the files it names under
  `forge.assets/prompts/`
  - the five layer prompts in `forge.assets/vibe-coding-prompts/`

  You may also read `docs/corpus/` (the Z-Machine framework and generic
  examples)
  if you need background on Isabelle. Nothing else under `docs/`.

  ## Forbidden — a violation voids this pass

  Reading any of these voids the pass; the transcript and session log are
  audited.

  - `forge.transformations/` and `forge.dashboard/` — the source of the phases
  that
    judge your Java. Write the Java from the requirements and the rules, not
  from the
    checkers. If you believe you cannot proceed without knowing what a phase
  does
    internally, **stop and report that to the operator**.
  - `experiments/`, `reference-runs/`, and `docs/` outside `docs/corpus/`;
  - any other folder under `forge.assets/case-studies/` (setup removes them);
  - the source of the scripts you run, other worktrees, the staging directory
    `/Users/ranwei/Gitee/v3c7-staging/`, and the git history (`git log`, `git
  show`, `git diff`).

  ## Hard constraints

  - Do not change anything under `forge.transformations/`, `forge.dashboard/`,
    `forge.assets/prompts/`, `forge.assets/vibe-coding-prompts/`,
    `forge.assets/case-studies/`, `pipeline.yaml` or `CLAUDE.md`. These are the
    instrument and the input, not your code.
  - Write Java only under `java.generated.project/src/main/java/lre/`, plus the
    trace file and the files the Output section names.
  - Run the pipeline only through the runner named below. Do not invoke the
  pipeline
    driver or any phase directly. Do not commit.

  ## What to do

  `java.generated.project` contains a Gradle scaffold and no Java. If you find
  any
  Java under `java.generated.project/src/main/java/`, delete it before writing
  and
  say so in your summary.

  1. Work the five layers in order — Architecture, Skeleton, Leaves,
  Controller,
     Review — without pausing between them. You may compile your code with
  `javac`
     or Gradle while you work.
  2. Write `java.generated.project/result_codegen.json`, the map from every
     requirement to the Java elements that implement it, exactly as
     `forge.assets/prompts/codegen_trace_rules.txt` describes. Every
  requirement
     needs at least one entry, and every Java file you write needs at least one
  entry
     naming it. A requirement type that the trace rules do not list maps to the
  Java
     element that realises it (a transition to `step`, a whole-machine
  constraint to
     the controller class).
  3. Write `forge.assets/corrections/ITER_SUMMARY.md` (at most 300 words): what
  you
     wrote and why, every decision the requirements did not settle and how you
     resolved it, and anything you could not resolve.

  Do not paste Java into the chat.

  ## Where to work

  Your working directory is

  ```
  /Users/ranwei/Gitee/forge-v3c7-run8-lre
  ```

  `cd` there first. If it does not exist, **stop and say so**; no other
  worktree is a
  substitute. Export these four variables in your shell before running the
  runner:

  ```
  export FORGE_ARM_DIR=/Users/ranwei/Gitee/formal_method_guided_vibe_coding/exp
  eriments/v3-cold
  export FORGE_WT_PREFIX=forge-v3c7
  export FORGE_STAGE_DIR=/Users/ranwei/Gitee/v3c7-staging
  export FORGE_SHA=56cb43d7835b43e8a986e6ba4c0e82f35083d6f1
  ```

  Run this check before you write any Java, and paste its output. If any line
  reads
  FAIL, stop.

  ```
  cd /Users/ranwei/Gitee/forge-v3c7-run8-lre 2>/dev/null || { echo "FAIL no
  worktree"; exit 1; }
  [ "$(git rev-parse HEAD)" = "$FORGE_SHA" ] && echo "OK   pinned commit" ||
  echo "FAIL wrong commit"
  [ "$(find java.generated.project/src -name '*.java' 2>/dev/null | wc -l | tr
  -d ' ')" = "0" ] && echo "OK   no Java present" || echo "FAIL Java present"
  [ "$(grep -m1 active_case_study pipeline.yaml | awk '{print $2}')" = "lre" ]
  && echo "OK   study is lre" || echo "FAIL wrong study"
  [ "$(ls forge.assets/case-studies)" = "lre" ] && echo "OK   only this study's
  inputs" || echo "FAIL other case studies present"
  [ ! -e docs/archive ] && echo "OK   no archive" || echo "FAIL docs/archive
  present"
  ```

  **Cost.** Pass your own token figure as the last argument of the runner if
  your
  session shows one (`/cost`); otherwise pass `unavailable`. Do not estimate.

  ## Run the pipeline once, then stop

  This is a **single cold pass**: you write the Java once, run the pipeline
  once, and
  stop, whatever the result.

  1. When the Java, the trace and the summary are written, run, **once**:
     `bash /Users/ranwei/Gitee/formal_method_guided_vibe_coding/scripts/run_cla
  ude_iteration.sh lre 8 1 <tokens|unavailable>`
  2. When it finishes, **stop**. Report the line it prints after
     `=== iteration 1 : what to do next ===`, verbatim. Do **not** open any
     `forge.assets/corrections/post_*.md` file, do **not** change the Java, and
  do
     **not** run the runner again: a pass whose Java changes after the runner
  starts,
     or that reads a report, is void.

  Then finish with the line `ITER_1_COMPLETE: <N> files written`.

⏺ Bash(export FORGE_ARM_DIR=/Users/ranwei/Gitee/formal_method_guided_vibe_coding
      /experiments/v3-cold FORGE_WT_PREFIX=forge-v3c7
      FORGE_STAGE_DIR=/Users/ranwei/Gitee/v3c…)
  ⎿  (file diff unavailable for this command)

⏺ All checks pass. Now reading the inputs.

  Read 1 file, listed 1 directory (ctrl+o to expand)

⏺ Bash(cd forge.assets/vibe-coding-prompts && cat README.md 01_architecture.md
      02_skeleton.md)
  ⎿  (No output)

⏺ Bash(cd forge.assets/vibe-coding-prompts && cat 03_leaves.md 04_controller.md
      05_review.md)
  ⎿  (No output)

⏺ Bash(cd /Users/ranwei/Gitee/forge-v3c7-run8-lre && find java.generated.project
      -maxdepth 4 -not -path '*/.gradle/*' -not -path '*/build/*' | head -50 &&
      cat java.gen…)
  ⎿  (file diff unavailable for this command)

⏺ All pre-flight checks pass with no Java to remove. I'm now mapping Layer 1's
  architecture into packages (annotation, event, sensor, actuator, operation,
  controller, constants), resolving the requirements' use of −1 for "no
  obstacle" by using a non-negative sentinel (0 = none, obstacles numbered from
  1) since the profile only allows non-negative integers.

⏺ Bash(cd /Users/ranwei/Gitee/forge-v3c7-run8-lre/java.generated.project &&
      mkdir -p src/main/java/lre/annotation src/main/java/lre/event
      src/main/java/lre/sensor src/…)
  ⎿  (No output)

⏺ Layer 2/3: types, events, sensor, actuator and operations.

⏺ Write(java.generated.project/src/main/java/lre/annotation/RoboChartType.java)
  ⎿  Wrote 14 lines to
     java.generated.project/src/main/java/lre/annotation/RoboChartType.java
       1 package lre.annotation;
       2
       3 import java.lang.annotation.ElementType;
       4 import java.lang.annotation.Retention;
       5 import java.lang.annotation.RetentionPolicy;
       6 import java.lang.annotation.Target;
       7
       8 /** Names the RoboChart type that a Java field, parameter or ret
         urn value stands for. */
       9 @Retention(RetentionPolicy.SOURCE)
     … +5 lines (ctrl+o to expand)

⏺ Write(java.generated.project/src/main/java/lre/event/InputEvent.java)
  ⎿  Wrote 24 lines to
     java.generated.project/src/main/java/lre/event/InputEvent.java
       1 package lre.event;
       2
       3 import lre.annotation.RoboChartType;
       4
       5 /** Inputs from the operator controller to the LRE. */
       6 public sealed interface InputEvent {
       7     /** Requested velocity, m/s. */
       8     record ReqVel(@RoboChartType("real") double value) implement
         s InputEvent {}
       9
     … +15 lines (ctrl+o to expand)

⏺ Write(java.generated.project/src/main/java/lre/event/OutputEvent.java)
  ⎿  Wrote 12 lines to
     java.generated.project/src/main/java/lre/event/OutputEvent.java
       1 package lre.event;
       2
       3 import lre.annotation.RoboChartType;
       4
       5 /** The only outputs of the LRE to the autopilot controller. */
       6 public sealed interface OutputEvent {
       7     /** Advised velocity, m/s. */
       8     record AdvVel(@RoboChartType("real") double value) implement
         s OutputEvent {}
       9
     … +3 lines (ctrl+o to expand)

⏺ Write(java.generated.project/src/main/java/lre/actuator/Actuator.java)
  ⎿  Wrote 34 lines to
     java.generated.project/src/main/java/lre/actuator/Actuator.java
       1 package lre.actuator;
       2
       3 import lre.annotation.RoboChartType;
       4 import lre.event.OutputEvent;
       5
       6 /** Receives the LRE's advice and keeps the latest values for th
         e autopilot controller. */
       7 public final class Actuator {
       8
       9     @RoboChartType("real") private double lastVelocity = 0.0;
     … +25 lines (ctrl+o to expand)

⏺ Write(java.generated.project/src/main/java/lre/sensor/Obstacle.java)
  ⎿  Wrote 21 lines to
     java.generated.project/src/main/java/lre/sensor/Obstacle.java
       1 package lre.sensor;
       2
       3 import lre.annotation.RoboChartType;
       4
       5 /**
       6  * An obstacle near the AUV: its position relative to the AUV, i
         ts depth and its velocity.
       7  * Distances in metres, velocities in m/s.
       8  */
       9 public record Obstacle(
     … +12 lines (ctrl+o to expand)

⏺ Write(java.generated.project/src/main/java/lre/sensor/ObstacleRegister.java)
  ⎿  Wrote 96 lines to
     java.generated.project/src/main/java/lre/sensor/ObstacleRegister.java
       1 package lre.sensor;
       2
       3 import java.util.ArrayList;
       4 import java.util.List;
       5 import java.util.NoSuchElementException;
       6 import java.util.Objects;
       7 import lre.annotation.RoboChartType;
       8
       9 /**
      10  * Obstacles indexed by natural numbers: a partial function from
     … +87 lines (~3 KB) (ctrl+o to expand)

⏺ Write(java.generated.project/src/main/java/lre/sensor/Sensor.java)
  ⎿  Wrote 155 lines to
     java.generated.project/src/main/java/lre/sensor/Sensor.java
       1 package lre.sensor;
       2
       3 import java.util.List;
       4 import lre.annotation.RoboChartType;
       5
       6 /**
       7  * The AUV's raw environmental data (depth, velocity components,
          rate of climb) and the
       8  * obstacle register, with the distance, field-accessor and sele
         ction functions derived
     … +147 lines (ctrl+o to expand)

⏺ Write(java.generated.project/src/main/java/lre/LreConstants.java)
  ⎿  Wrote 33 lines to
     java.generated.project/src/main/java/lre/LreConstants.java
      1 package lre;
      2
      3 import lre.annotation.RoboChartType;
      4
      5 /** Safety thresholds and advised velocities of the LRE. */
      6 public final class LreConstants {
      7
      8     /** Minimal safe distance to any obstacle, metres. */
      9     @RoboChartType("real") public static final double MIN_SAFE_DI
        ST = 1.0;
     … +24 lines (ctrl+o to expand)

⏺ Write(java.generated.project/src/main/java/lre/operation/CalcVel.java)
  ⎿  Wrote 42 lines to
     java.generated.project/src/main/java/lre/operation/CalcVel.java
       1 package lre.operation;
       2
       3 import lre.annotation.RoboChartType;
       4 import lre.sensor.Sensor;
       5
       6 /** Computes the AUV's horizontal, vertical and overall velocity
          from the raw sensor data. */
       7 public final class CalcVel {
       8
       9     private final Sensor sensor;
     … +33 lines (ctrl+o to expand)

⏺ Write(java.generated.project/src/main/java/lre/operation/CalcCStc.java)
  ⎿  Wrote 26 lines to
     java.generated.project/src/main/java/lre/operation/CalcCStc.java
       1 package lre.operation;
       2
       3 import lre.annotation.RoboChartType;
       4 import lre.sensor.Sensor;
       5
       6 /** Finds the index of the closest static obstacle. */
       7 public final class CalcCStc {
       8
       9     private final Sensor sensor;
      10     @RoboChartType("nat") private int cstc;
     … +16 lines (ctrl+o to expand)

⏺ Write(java.generated.project/src/main/java/lre/operation/CalcCDyn.java)
  ⎿  Wrote 26 lines to
     java.generated.project/src/main/java/lre/operation/CalcCDyn.java
       1 package lre.operation;
       2
       3 import lre.annotation.RoboChartType;
       4 import lre.sensor.Sensor;
       5
       6 /** Finds the index of the closest dynamic obstacle. */
       7 public final class CalcCDyn {
       8
       9     private final Sensor sensor;
      10     @RoboChartType("nat") private int cdyn;
     … +16 lines (ctrl+o to expand)

⏺ Write(java.generated.project/src/main/java/lre/operation/CheckOPEZ.java)  ⎿  Wrote 31 lines to
     java.generated.project/src/main/java/lre/operation/CheckOPEZ.java
       1 package lre.operation;
       2
       3 import lre.LreConstants;
       4 import lre.annotation.RoboChartType;
       5 import lre.sensor.Sensor;
       6
       7 /** Decides whether the AUV is within an Object Proximity Exclus
         ion Zone. */
       8 public final class CheckOPEZ {
       9
     … +22 lines (ctrl+o to expand)

⏺ Write(java.generated.project/src/main/java/lre/operation/CalcCPA.java)
  ⎿  Wrote 60 lines to
     java.generated.project/src/main/java/lre/operation/CalcCPA.java
       1 package lre.operation;
       2
       3 import lre.annotation.RoboChartType;
       4 import lre.sensor.Sensor;
       5
       6 /**
       7  * Computes the time at closest point of approach (tcpa) and the
          closest distance of approach
       8  * (cda) to the closest dynamic obstacle, the one CalcCDyn store
         s (read from the same sensor
     … +52 lines (ctrl+o to expand)

⏺ Layer 4: mode enum and controller.

⏺ Write(java.generated.project/src/main/java/lre/controller/LreMode.java)
  ⎿  Wrote 13 lines to
     java.generated.project/src/main/java/lre/controller/LreMode.java
       1 package lre.controller;
       2
       3 /** The LRE's four operating modes; OCM, the start-up mode, come
         s first. */
       4 public enum LreMode {
       5     /** Operator Control Mode: the LRE is inactive and passes op
         erator requests through. */
       6     OCM,
       7     /** Main Operating Mode: autonomous control at normal speed.
          */
     … +6 lines (ctrl+o to expand)

⏺ Write(java.generated.project/src/main/java/lre/controller/LreController.java)
  ⎿  Wrote 135 lines to
     java.generated.project/src/main/java/lre/controller/LreController.java
       1 package lre.controller;
       2
       3 import lre.LreConstants;
       4 import lre.actuator.Actuator;
       5 import lre.event.InputEvent;
       6 import lre.event.OutputEvent;
       7 import lre.operation.CalcCDyn;
       8 import lre.operation.CalcCPA;
       9 import lre.operation.CalcCStc;
      10 import lre.operation.CalcVel;
     … +125 lines (ctrl+o to expand)

⏺ Compiling, then a quick behaviour check from the scratchpad (kept outside the
  project).

⏺ Bash(S=/private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3c7-run8-lre/b93685c
      e-9031-4a30-8410-7305271879f6/scratchpad && mkdir -p $S/out $S/smoke &&
      javac --release…)
  ⎿  (No output)

⏺ Bash(S=/private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3c7-run8-lre/b93685c
      e-9031-4a30-8410-7305271879f6/scratchpad && cat > $S/smoke/Smoke.java
      <<'EOF'
      import lr…)
  ⎿  (No output)

⏺ Smoke test passes. Next, a quick profile self-check for constructs the rules
  forbid:

  Searched for 1 pattern (ctrl+o to expand)

⏺ Only the lre.event package name matched, and the rules list that sub-package
  by name. Now the trace file:

⏺ Write(java.generated.project/result_codegen.json)
  ⎿  Wrote 145 lines to java.generated.project/result_codegen.json
      1 {
      2     "codegen_trace": [
      3         {"requirement_gid": "LRE-ARCH1", "java_file": "src/main/j
        ava/lre/controller/LreController.java", "java_element": "LreContr
        oller", "element_type": "class"},
      4         {"requirement_gid": "LRE-ARCH1", "java_file": "src/main/j
        ava/lre/controller/LreController.java", "java_element": "step", "
        element_type": "method"},
      5         {"requirement_gid": "LRE-ARCH2", "java_file": "src/main/j
        ava/lre/controller/LreController.java", "java_element": "LreContr
     … +141 lines (~19 KB) (ctrl+o to expand)

⏺ Bash(cd /Users/ranwei/Gitee/forge-v3c7-run8-lre && python3 -I - <<'EOF'
      import json, os, re…)
  ⎿  (No output)

⏺ All 51 requirements and all 15 files are traced, every traced element exists
  in its file, and every public element is claimed. Layer 5: I need line numbers
  for the review's java_trace entries.

⏺ Bash(cd /Users/ranwei/Gitee/forge-v3c7-run8-lre/java.generated.project/src/mai
      n/java/lre && grep -n 'NO_OBSTACLE = 0\|public void put\|SAFE_LARGE_DIST =
      \|public dou…)
  ⎿  (No output)

  Searched for 1 pattern (ctrl+o to expand)

⏺ Writing the Layer 5 review (post_codegen.json, with the .md rendered from the
  same data):

⏺ Write(forge.assets/corrections/post_codegen.json)
  ⎿  Wrote 132 lines to forge.assets/corrections/post_codegen.json
      1 {
      2   "phase": "codegen",
      3   "status": "uncertain",
      4   "summary": "All 51 LRE requirements are implemented in 15 Java
        files under java.generated.project/src/main/java/lre and traced i
        n result_codegen.json. The code compiles (javac --release 17), an
        d a scratch smoke test of the main transitions passed. The issues
         below record what the requirements left open or contradictory an
        d how it was resolved.",
      5   "issues": [
     … +127 lines (ctrl+o to expand)

⏺ Bash(cd /Users/ranwei/Gitee/forge-v3c7-run8-lre/forge.assets/corrections &&
      python3 -I - <<'EOF'
      import json…)
  ⎿  (No output)

⏺ Now the iteration summary (300-word limit):

⏺ Write(forge.assets/corrections/ITER_SUMMARY.md)
  ⎿  Wrote 24 lines to forge.assets/corrections/ITER_SUMMARY.md
       1 # LRE, pass 8, iteration 1
       2
       3 ## What was written
       4 - 15 Java files in package `lre`: the annotation, events, sensor
          (Obstacle, ObstacleRegister, Sensor), actuator, five operations
         , constants, and the mode enum and controller. One `step()`; the
          operations run before the guards.
       5 - `result_codegen.json`, covering all 51 requirements.
       6 - `post_codegen.{md,json}`.
       7 - No Java existed beforehand. Compiled with javac (release 17);
     … +18 lines (~2 KB) (ctrl+o to expand)

⏺ Bash(cd /Users/ranwei/Gitee/forge-v3c7-run8-lre && wc -w
      forge.assets/corrections/ITER_SUMMARY.md &&
      S=/private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3c7-run8-lr…)
  ⎿  (No output)

⏺ The summary is 303 words, over the 300 limit, so I'll trim it.

⏺ Update(forge.assets/corrections/ITER_SUMMARY.md)
  ⎿  Added 1 line, removed 1 line
       9  ## Decisions the requirements left open
      10  - **"No obstacle" index.** The requirements give −1, which the 
          nat-only profile forbids. Index 0 means "none", and obstacles a
          re numbered from 1.
      11  - **Safe large distance.** Set to 1000000.0 m.
      12 -- **CPA.** tcpa = (r·u)/|u|² and cda = √(|r−u·tcpa|² + vdist²),
         - with vertical separation held constant. Using vdist means DM5'
         -s zero accessors and OP5's large default no longer conflict: wi
         -th no dynamic obstacle, cda is large.                          
      12 +- **CPA.** tcpa = (r·u)/|u|² and cda = √(|r−u·tcpa|² + vdist²),
         + with vertical separation held constant. Through vdist, OP5's l
         +arge default reaches cda despite DM5's zero accessors.         
      13  - **Index source.** CheckOPEZ and CalcCPA call the same sensor 
          selection function that CalcCStc and CalcCDyn store, because co
          mpute() may only use sensor calls and its own fields.
      14  - **Variables.** They live in the operations that compute them.
      15  - **Constants.** VEL_LIMIT, MOM_VELOCITY, HCM_VELOCITY and STOP
          _VELOCITY were added. Beh4's distance "1" uses MIN_SAFE_DIST.

⏺ Update(forge.assets/corrections/ITER_SUMMARY.md)
  ⎿  Added 1 line, removed 1 line
      13  - **Index source.** CheckOPEZ and CalcCPA call the same sensor 
          selection function that CalcCStc and CalcCDyn store, because co
          mpute() may only use sensor calls and its own fields.
      14  - **Variables.** They live in the operations that compute them.
      15  - **Constants.** VEL_LIMIT, MOM_VELOCITY, HCM_VELOCITY and STOP
          _VELOCITY were added. Beh4's distance "1" uses MIN_SAFE_DIST.
      16 -- **Priority within a mode.** reqOCM first, then other events, 
         -then inOpez, then collision, then Beh9, Beh11, Beh10, each with
         - the negations of earlier guards. Beh11 comes before Beh10 beca
         -use otherwise Beh11 could never fire.                          
      16 +- **Priority within a mode.** reqOCM, other events, inOpez, col
         +lision, then Beh9, Beh11, Beh10, each negating earlier guards. 
         +Beh11 precedes Beh10, which would otherwise make it unsatisfiab
         +le.                                                            
      17  - **HCM velocity.** Advised as 0 per FR3, not the 0.1 in the de
          scription.
      18  - **vvel.** The signed rate of climb, as OP1 states.
      19  - **CAM.** Issues nothing on entry.

  Read 1 file (ctrl+o to expand)
⏺ The summary is now 286 words. Everything is written, so I'm running the runner
  once. My session doesn't show a token figure, so I'm passing unavailable.

⏺ Bash(export FORGE_ARM_DIR=/Users/ranwei/Gitee/formal_method_guided_vibe_coding
      /experiments/v3-cold FORGE_WT_PREFIX=forge-v3c7
      FORGE_STAGE_DIR=/Users/ranwei/Gitee/v3c…)
  ⎿  Running in the background (↓ to manage)

⏺ The runner is running in the background. I'll wait for it to finish and won't
  touch the Java or open any reports in the meantime.

⏺ Background command "Run the pipeline iteration runner once" completed (exit
code 0)

  Searched for 1 pattern (ctrl+o to expand)

⏺ I ran the pipeline once and it passed every phase on the first try.

  The line it printed after === iteration 1 : what to do next ===, verbatim:

    ALL PHASES PASSED -> this run has CONVERGED. Stop iterating.

  The line after that reads Report: converged at iteration 1.

  What I wrote
  - Java: 15 files under java.generated.project/src/main/java/lre/. There was no
    Java to delete beforehand. It compiled with javac --release 17, and a quick
    behaviour test I kept in the scratchpad passed before the run.
  - Trace: java.generated.project/result_codegen.json maps all 51 requirements
    and names every Java file.
  - Review: forge.assets/corrections/post_codegen.md and post_codegen.json are
    the Layer 5 review, with 13 issues and status uncertain.
  - Summary: forge.assets/corrections/ITER_SUMMARY.md (286 words).

  Decisions the requirements didn't settle
  - "No obstacle" index: the requirements use −1, but the profile only allows
    non-negative integers. I used 0 to mean "none" and number obstacles from 1.
  - Closest point of approach: I computed it from horizontal motion and kept the
    vertical gap fixed. That way, with no moving obstacle, the closest distance
    comes out large instead of 0, which would have falsely triggered collision
    avoidance.
  - Order of transitions within a mode: "request OCM" is checked first, then the
    other operator events, then the exclusion zone, then collision, then the
    three static-obstacle checks. One of those checks would have been
    unreachable in the other order.

  Still open: if the AUV and the obstacle move at the same velocity, the time to
  closest approach divides zero by zero. It doesn't trigger collision avoidance
  and it does allow leaving it. No verifier flagged this.

  git status shows pipeline.yaml as modified. I never edited it; it was already
  like that when the session started.

  I didn't open any post_* report the pipeline wrote, didn't change the Java
  after starting the run, and didn't run it a second time.

  ITER_1_COMPLETE: 19 files written