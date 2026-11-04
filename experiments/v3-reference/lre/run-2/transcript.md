
❯ # Reference run — lre, run 2

  You are an independent developer implementing a reactive safety controller in
  Java from a specification. A downstream toolchain extracts a formal model
  from your
  Java and verifies it.

  This session drives **exactly one run** — this one — and nothing else. If
  anything earlier in this conversation concerned another run of this or any
  other
  system, **stop and tell the operator**: the run is contaminated and must be
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

  ## Forbidden — a violation voids this run

  Reading any of these voids the run; the transcript and session log are
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
    `/Users/ranwei/Gitee/v3r7-staging/`, and the git history (`git log`, `git
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
  /Users/ranwei/Gitee/forge-v3r7-run2-lre
  ```

  `cd` there first. If it does not exist, **stop and say so**; no other
  worktree is a
  substitute. Export these four variables in your shell before running the
  runner:

  ```
  export FORGE_ARM_DIR=/Users/ranwei/Gitee/formal_method_guided_vibe_coding/exp
  eriments/v3-reference
  export FORGE_WT_PREFIX=forge-v3r7
  export FORGE_STAGE_DIR=/Users/ranwei/Gitee/v3r7-staging
  export FORGE_SHA=56cb43d7835b43e8a986e6ba4c0e82f35083d6f1
  ```

  Run this check before you write any Java, and paste its output. If any line
  reads
  FAIL, stop.

  ```
  cd /Users/ranwei/Gitee/forge-v3r7-run2-lre 2>/dev/null || { echo "FAIL no
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

  ## The loop: you drive it yourself

  1. Write the Java, the trace and the summary (iteration 1).
  2. Run `bash /Users/ranwei/Gitee/formal_method_guided_vibe_coding/scripts/run
  _claude_iteration.sh lre 2 <iteration> <tokens|unavailable>`.
     Use the runner for **every** iteration; it records each one.
  3. If it prints **ALL PHASES PASSED**, stop: the run has converged. Report
  the
     iteration number.
  4. Otherwise read **only** `forge.assets/corrections/post_<phase>.md` for
  each
     failing phase, change **your Java** (and the trace, if the Java's elements
     changed), update `ITER_SUMMARY.md`, and go to 2 with the next iteration
  number.
  5. **The cap is iteration 7.** At the cap, stop and report non-convergence
  with the
     failing phases.

  If a report tells you to change part of the instrument (a template, the
  extractor,
  the rules, `pipeline.yaml`), **stop and report it to the operator verbatim**:
  that
  is a defect of the instrument, and a run that edits the instrument is void.
  If you
  believe a failure is the pipeline's and not your code's, say so in the
  summary and
  work around it in your Java.

  End each iteration's chat reply with `ITER_<n>_COMPLETE: <N> files edited`.

⏺ Bash(export FORGE_ARM_DIR=/Users/ranwei/Gitee/formal_method_guided_vibe_coding
      /experiments/v3-reference && export FORGE_WT_PREFIX=forge-v3r7 && export
      FORGE_STAGE_DI…)
  ⎿  (file diff unavailable for this command)

⏺ Pre-flight check passes on all five lines. Reading the inputs now.

  Read 1 file, listed 1 directory (ctrl+o to expand)

⏺ Bash(for f in forge.assets/vibe-coding-prompts/*.md; do echo "======== $f";
      cat "$f"; done)
  ⎿  (file diff unavailable for this command)

⏺ Bash(cat java.generated.project/build.gradle
      java.generated.project/settings.gradle
      java.generated.project/gradle.properties; ls -la java.generated.project
      forge.ass…)
  ⎿  (No output)

⏺ I've laid out the Layer 1 architecture (events, sensor, actuator, operations,
  constants, controller) and documented three requirement conflicts I
  resolved—using index 0 for "no obstacle," deriving CPA range from hdist, and
  following LRE-FR3's 0 m/s for HCM speed. Next I'll move through Layers 2–5
  without pausing.

⏺ Bash(cd /Users/ranwei/Gitee/forge-v3r7-run2-lre/java.generated.project &&
      mkdir -p src/main/java/lre/annotation src/main/java/lre/event
      src/main/java/lre/sensor src/…)
  ⎿  (No output)

⏺ Layer 2/3: annotation, events, data types, constants, sensor, actuator.

⏺ Write(annotation/RoboChartType.java)
  ⎿  Wrote 17 lines to annotation/RoboChartType.java
       1 package lre.annotation;
       2
       3 import java.lang.annotation.ElementType;
       4 import java.lang.annotation.Retention;
       5 import java.lang.annotation.RetentionPolicy;
       6 import java.lang.annotation.Target;
       7
       8 /**
       9  * Declares the RoboChart type ({@code real}, {@code nat}) a Jav
         a field,
     … +8 lines (ctrl+o to expand)

⏺ Write(event/InputEvent.java)
  ⎿  Wrote 27 lines to event/InputEvent.java
       1 package lre.event;
       2
       3 import lre.annotation.RoboChartType;
       4
       5 /**
       6  * Inputs from the operator controller to the LRE (LRE-DM6).
       7  */
       8 public sealed interface InputEvent {
       9
      10     /** Requested velocity, m/s. */
     … +17 lines (ctrl+o to expand)

⏺ Write(event/OutputEvent.java)
  ⎿  Wrote 15 lines to event/OutputEvent.java
       1 package lre.event;
       2
       3 import lre.annotation.RoboChartType;
       4
       5 /**
       6  * The only outputs of the LRE to the autopilot controller (LRE-
         DM7).
       7  */
       8 public sealed interface OutputEvent {
       9
     … +6 lines (ctrl+o to expand)

⏺ Write(sensor/Obstacle.java)
  ⎿  Wrote 27 lines to sensor/Obstacle.java
       1 package lre.sensor;
       2
       3 import lre.annotation.RoboChartType;
       4
       5 /**
       6  * An obstacle seen by the AUV (LRE-DM2). Distances in metres, v
         elocities in m/s.
       7  *
       8  * @param ns_rel_dist relative distance north-south
       9  * @param ew_rel_dist relative distance east-west
     … +18 lines (ctrl+o to expand)

⏺ Write(sensor/ObstacleRegister.java)
  ⎿  Wrote 80 lines to sensor/ObstacleRegister.java
       1 package lre.sensor;
       2
       3 import java.util.ArrayList;
       4 import java.util.List;
       5 import java.util.NoSuchElementException;
       6 import java.util.TreeMap;
       7
       8 import lre.annotation.RoboChartType;
       9
      10 /**
     … +70 lines (ctrl+o to expand)

⏺ Write(sensor/Sensor.java)
  ⎿  Wrote 158 lines to sensor/Sensor.java
       1 package lre.sensor;
       2
       3 import java.util.List;
       4
       5 import lre.annotation.RoboChartType;
       6
       7 /**
       8  * The AUV's environmental data and the obstacle functions deriv
         ed from it
       9  * (LRE-DM5, LRE-SF1, LRE-SF2, LRE-SF3). Indices that name no ob
     … +150 lines (~5 KB) (ctrl+o to expand)

⏺ Write(actuator/Actuator.java)
  ⎿  Wrote 37 lines to actuator/Actuator.java
       1 package lre.actuator;
       2
       3 import lre.annotation.RoboChartType;
       4 import lre.event.OutputEvent;
       5
       6 /**
       7  * Receives the LRE's advice and keeps the latest values for the
          autopilot
       8  * controller (LRE-DM8). Both values are zero until the first ad
         vice.
     … +29 lines (ctrl+o to expand)

⏺ Write(LreConstants.java)
  ⎿  Wrote 36 lines to LreConstants.java
      1 package lre;
      2
      3 import lre.annotation.RoboChartType;
      4
      5 /**
      6  * Constants of the LRE: the four safety thresholds of LRE-DM4 (m
        etres) and
      7  * the advised speeds of LRE-FR2, LRE-FR3, LRE-Beh7 and LRE-Beh18
         (m/s).
      8  */
     … +28 lines (ctrl+o to expand)

⏺ Now the operation classes (LRE-OP1–5).

⏺ Write(operation/CalcVel.java)
  ⎿  Wrote 45 lines to operation/CalcVel.java
      1 package lre.operation;
      2
      3 import lre.annotation.RoboChartType;
      4 import lre.sensor.Sensor;
      5
      6 /**
      7  * Computes the AUV's horizontal, vertical and total velocity (LR
        E-OP1).
      8  */
      9 public final class CalcVel {
     … +36 lines (ctrl+o to expand)

⏺ Write(operation/CalcCStc.java)
  ⎿  Wrote 29 lines to operation/CalcCStc.java
      1 package lre.operation;
      2
      3 import lre.annotation.RoboChartType;
      4 import lre.sensor.Sensor;
      5
      6 /**
      7  * Finds the closest static obstacle (LRE-OP3).
      8  */
      9 public final class CalcCStc {
     … +20 lines (ctrl+o to expand)

⏺ Write(operation/CalcCDyn.java)
  ⎿  Wrote 29 lines to operation/CalcCDyn.java
      1 package lre.operation;
      2
      3 import lre.annotation.RoboChartType;
      4 import lre.sensor.Sensor;
      5
      6 /**
      7  * Finds the closest dynamic obstacle (LRE-OP4).
      8  */
      9 public final class CalcCDyn {
     … +20 lines (ctrl+o to expand)

⏺ Write(operation/CheckOPEZ.java)
  ⎿  Wrote 32 lines to operation/CheckOPEZ.java
      1 package lre.operation;
      2
      3 import lre.LreConstants;
      4 import lre.sensor.Sensor;
      5
      6 /**
      7  * Decides whether the AUV is in an Object Proximity Exclusion Zo
        ne (LRE-OP2).
      8  */
      9 public final class CheckOPEZ {
     … +23 lines (ctrl+o to expand)
⏺ Write(operation/CalcCPA.java)
  ⎿  Wrote 60 lines to operation/CalcCPA.java
       1 package lre.operation;
       2
       3 import lre.LreConstants;
       4 import lre.annotation.RoboChartType;
       5 import lre.sensor.Sensor;
       6
       7 /**
       8  * Computes the closest distance of approach (cda) and the time
         at the closest
       9  * point of approach (tcpa) to the closest dynamic obstacle, in
     … +52 lines (~2 KB) (ctrl+o to expand)

⏺ Layer 4: mode enum and controller.

⏺ Write(controller/LreMode.java)
  ⎿  Wrote 15 lines to controller/LreMode.java
       1 package lre.controller;
       2
       3 /**
       4  * The four operating modes of the LRE (LRE-DM1); OCM, the initi
         al mode, first.
       5  */
       6 public enum LreMode {
       7     /** Operator Control Mode: the LRE passes operator requests
         through (LRE-FR1). */
       8     OCM,
     … +7 lines (ctrl+o to expand)

⏺ Write(controller/LreController.java)
  ⎿  Wrote 134 lines to controller/LreController.java
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
     … +124 lines (ctrl+o to expand)

⏺ Compiling and running a quick behavioural smoke test from the scratchpad.

⏺ Bash(SP=/private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3r7-run2-lre/aee177
      d3-9225-4f21-a24c-3df05876fc8e/scratchpad && mkdir -p $SP/classes
      $SP/smoke && javac --…)
  ⎿  (No output)

⏺ Compiles cleanly with -Xlint:all, and the smoke test passes. Now Layer 5: the
  trace file, review, and summary. Getting line numbers for the review entries:

⏺ Bash(grep -n -E "NO_OBSTACLE = |SAFE_LARGE_DISTANCE = |CPA_EPSILON|HCM_SPEED =
      |NORMAL_SPEED = |MIN_SAFE_DIST = |advisedVelocity = |advisedHeading =
      |this.vvel =|thi…)
  ⎿  (No output)

⏺ Bash(SP=/private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3r7-run2-lre/aee177
      d3-9225-4f21-a24c-3df05876fc8e/scratchpad && cat > $SP/gen_trace.py
      <<'EOF'
      import json…)
  ⎿  (No output)

⏺ All 51 requirements and all 15 files are traced, and every named element
  exists in its file. Installing the trace:

⏺ Bash(SP=/private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3r7-run2-lre/aee177
      d3-9225-4f21-a24c-3df05876fc8e/scratchpad && cp $SP/result_codegen.json
      /Users/ranwei/G…)
  ⎿  (No output)

⏺ Bash(cd /Users/ranwei/Gitee/forge-v3r7-run2-lre/java.generated.project/src/mai
      n/java/lre && grep -n -E "currentMode == |boolean
      (inOpez|velAtMostNormal|staticObsBeyo…)
  ⎿  (No output)

⏺ Bash(SP=/private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3r7-run2-lre/aee177
      d3-9225-4f21-a24c-3df05876fc8e/scratchpad && cat > $SP/gen_post_codegen.py
      <<'EOF'
      impo…)
  ⎿  (No output)

⏺ Now the iteration summary (≤300 words).

⏺ Write(~/Gitee/forge-v3r7-run2-lre/forge.assets/corrections/ITER_SUMMARY.md)
  ⎿  Wrote 24 lines to
     ~/Gitee/forge-v3r7-run2-lre/forge.assets/corrections/ITER_SUMMARY.md
      1 # ITER_SUMMARY — lre, run 2
      2
      3 ## Iteration 1
      4
      5 No Java was present at the start, so nothing was deleted.
      6
      7 **What I wrote and why.** I wrote 15 files under `lre`: events (`
        InputEvent`, `OutputEvent`), the data types (`Obstacle` and `Obst
        acleRegister`), `Sensor` (SF1–3, DM5), `Actuator`, `LreConstants`
        , and the five operations OP1–5, whose getters are named after Va
     … +18 lines (~2 KB) (ctrl+o to expand)

  Read 1 file (ctrl+o to expand)

⏺ Over 300 words; trimming.

⏺ Write(~/Gitee/forge-v3r7-run2-lre/forge.assets/corrections/ITER_SUMMARY.md)
  ⎿  Added 10 lines, removed 10 lines
       4  
       5  No Java was present at the start, so nothing was deleted.
       6  
       7 -**What I wrote and why.** I wrote 15 files under `lre`: events 
         -(`InputEvent`, `OutputEvent`), the data types (`Obstacle` and `
         -ObstacleRegister`), `Sensor` (SF1–3, DM5), `Actuator`, `LreCons
         -tants`, and the five operations OP1–5, whose getters are named 
         -after Var1–8. `LreController.step` runs the operations, declare
         -s the named predicates (`inOpez` among them) and then a two-lev
         -el if-else over OCM, MOM, HCM and CAM. In MOM, HCM and CAM, `re
         -qOCM` is the first, unguarded branch. Every predicate-only bran
         -ch is negated in the branches below it.                        
       7 +**What I wrote.** I wrote 15 files under `lre`: the events, `Ob
         +stacle`, `ObstacleRegister`, `Sensor` (SF1–3, DM5), `Actuator`,
         + `LreConstants`, and the operations OP1–5, whose getters are na
         +med after Var1–8. `LreController.step` runs the operations, dec
         +lares the named predicates (`inOpez` among them) and then a two
         +-level if-else over the four modes. In MOM, HCM and CAM, `reqOC
         +M` is the first, unguarded branch. Every predicate-only branch 
         +is negated in the branches below it.                           
       8  
       9  **Decisions the requirements left open:**
      10 -- **No-obstacle index.** "-1 = no obstacle" conflicts with nat.
         - The sentinel is index 0 (`NO_OBSTACLE`), and the register only
         - accepts keys ≥ 1.                                             
      11 -- **CPA with no obstacle.** DM5's zero field defaults would mak
         -e CalcCPA report cda = 0 (a false CAM). CalcCPA therefore takes
         - the range from `hdist`, which carries the large default. It wo
         -rks in the horizontal plane with relative velocity, and adds `C
         -PA_EPSILON` to the divisor.                                    
      12 -- **HCM speed.** I advise 0 m/s (FR3); the system description s
         -ays 0.1 m/s.                                                   
      13 -- **Literal "1" thresholds.** I named them: `NORMAL_SPEED` for 
         -velocities and `MIN_SAFE_DIST` for Beh4's distances.           
      14 -- **Transition priority.** In MOM: reqOCM, inOpez, CAM, endTask
         -, reqHCM, then Beh9, Beh11, Beh10. Beh11 comes before Beh10 so 
         -that it is not dead code.                                      
      10 +- **No-obstacle index.** "-1 = none" conflicts with nat. The se
         +ntinel is index 0 (`NO_OBSTACLE`), and register keys are ≥ 1.  
      11 +- **CPA with no obstacle.** DM5's zero field defaults would giv
         +e cda = 0 (a false CAM). CalcCPA therefore takes the range from
         + `hdist`, which carries the large default. It works in the hori
         +zontal plane with relative velocity, and adds `CPA_EPSILON` to 
         +the divisor.                                                   
      12 +- **HCM speed.** I advise 0 m/s (FR3), not the description's 0.
         +1.                                                             
      13 +- **Literal 1s.** Velocities use `NORMAL_SPEED`; Beh4's distanc
         +es use `MIN_SAFE_DIST`.                                        
      14 +- **Priority in MOM.** reqOCM, inOpez, CAM, endTask, reqHCM, th
         +en Beh9, Beh11, Beh10. Beh11 comes before Beh10 so that it is n
         +ot dead code.                                                  
      15  - **`vvel`.** It is the signed rate of climb, as written.
      16 -- **CheckOPEZ.** It reads the closest static index from the sen
         -sor, because the operation profile admits only sensor calls.   
      17 -- **No input this cycle.** `step(null)` means no operator input
         - in this cycle.                                                
      18 -- **Invented defaults.** The safe distance is 10^6 m. Readings 
         -and advice start at 0. Ties in the closest-obstacle search go t
         -o the lowest index.                                            
      16 +- **CheckOPEZ.** It reads the closest static index from the sen
         +sor, as the profile requires.                                  
      17 +- **No input this cycle.** `step(null)` means no operator input
         +.                                                              
      18 +- **Invented defaults.** The safe distance is 10^6 m. Readings 
         +and advice start at 0. Ties go to the lowest index.            
      19  
      20  **Unresolved:**
      21  - CAM's evasive manoeuvre is unspecified, so CAM has no entry a
          ction.
      22  - MOM and HCM could oscillate if `staticObsDfltVertDist > stati
          cObsVertDist`. The guards are exclusive at the given defaults.
      23  
      24 -Full detail is in `post_codegen.md`.                           
      24 +Details are in `post_codegen.md`.                              

  Read 1 file (ctrl+o to expand)

⏺ Running the pipeline for iteration 1. I can't see a session token figure, so
  I'm passing unavailable.

⏺ Bash(cd /Users/ranwei/Gitee/forge-v3r7-run2-lre && export FORGE_ARM_DIR=/Users
      /ranwei/Gitee/formal_method_guided_vibe_coding/experiments/v3-reference
      FORGE_WT_PREFIX…)
  ⎿  Running in the background (↓ to manage)

⏺ The pipeline is running in the background; I'll pick up when it finishes.

  ITER_1_COMPLETE: 18 files edited

⏺ Background command "Run the pipeline runner for iteration 1" completed (exit
code 0)

  Read 1 file (ctrl+o to expand)

⏺ The run converged at iteration 1: the runner printed ALL PHASES PASSED, so I
  stopped there.

  All 12 phases passed:
  - Checks on the Java: compile, coverage, profile check.
  - Model transformations: T2M, M2M, M2T, Dafny generation and Isabelle
    generation.
  - FDR4: 3 of 3 assertions passed; the runner skipped the determinism assertion
    by design.
  - Dafny: verified.
  - Isabelle: 47 lemmas, deadlock freedom proved.
  - Vacuity: 0 blocking findings and 3 advisory ones. I didn't open
    post_vacuity.md, because the instructions let me read reports only for
    failing phases.

  What I did
  - The pre-flight check printed OK on all 5 lines. There was no Java in the
    project at the start, so I deleted nothing.
  - I wrote 15 Java files under java.generated.project/src/main/java/lre/, plus
    result_codegen.json (128 entries covering all 51 requirements and all 15
    files), post_codegen.md/.json and ITER_SUMMARY.md (297 words).
  - I passed unavailable as the token figure, because no session cost figure was
    visible to me.

  Decisions the requirements didn't settle (all recorded in post_codegen.md and
  ITER_SUMMARY.md):
  - "No obstacle" index: the requirements use -1, but indices must be natural
    numbers in the model. I used index 0 to mean "no obstacle" and the register
    only accepts indices from 1.
  - Collision check with no moving obstacle: taken literally, the requirements
    would report a collision when no moving obstacle exists. The calculation
    therefore takes the distance from the sensor's horizontal-distance function,
    which returns a large value in that case.
  - HCM speed: I advise 0 m/s, as the requirement says, rather than the 0.1 m/s
    in the system description.
  - Transition order: MOM's transitions are ordered safety-first, and one
    HCM-entry transition is placed earlier so it can still fire.

  Not resolved (also flagged in both files):
  - What the vehicle should do in collision avoidance mode isn't specified, so
    that mode sends no advice on entry.
  - Two of the distance thresholds have the same default today. If they are ever
    set to different values, the controller could switch back and forth between
    MOM and HCM.

  For you: the runner listed three fields to fill in for the staging record
  (converged, failure_summary, actor_codegen_summary). I left them alone,
  because the staging directory is off-limits to me. Nothing was committed and
  nothing in the pipeline itself was changed.

  ITER_1_COMPLETE: 0 files edited