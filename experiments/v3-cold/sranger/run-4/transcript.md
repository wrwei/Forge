
❯ # Single cold pass — sranger, pass 4

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

  - `forge.assets/case-studies/sranger/system/system_description.txt` — the
  system
  - `forge.assets/case-studies/sranger/requirements/requirement_all.json` — the
  requirements
  - `forge.assets/case-studies/sranger/requirements/README.md` — the
  requirement-ID conventions
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
  - Write Java only under `java.generated.project/src/main/java/sranger/`, plus
  the
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
  /Users/ranwei/Gitee/forge-v3c7-run4-sranger
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
  cd /Users/ranwei/Gitee/forge-v3c7-run4-sranger 2>/dev/null || { echo "FAIL no
  worktree"; exit 1; }
  [ "$(git rev-parse HEAD)" = "$FORGE_SHA" ] && echo "OK   pinned commit" ||
  echo "FAIL wrong commit"
  [ "$(find java.generated.project/src -name '*.java' 2>/dev/null | wc -l | tr
  -d ' ')" = "0" ] && echo "OK   no Java present" || echo "FAIL Java present"
  [ "$(grep -m1 active_case_study pipeline.yaml | awk '{print $2}')" =
  "sranger" ] && echo "OK   study is sranger" || echo "FAIL wrong study"
  [ "$(ls forge.assets/case-studies)" = "sranger" ] && echo "OK   only this
  study's inputs" || echo "FAIL other case studies present"
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
  ude_iteration.sh sranger 4 1 <tokens|unavailable>`
  2. When it finishes, **stop**. Report the line it prints after
     `=== iteration 1 : what to do next ===`, verbatim. Do **not** open any
     `forge.assets/corrections/post_*.md` file, do **not** change the Java, and
  do
     **not** run the runner again: a pass whose Java changes after the runner
  starts,
     or that reads a report, is void.

  Then finish with the line `ITER_1_COMPLETE: <N> files written`.

⏺ Bash(cd /Users/ranwei/Gitee/forge-v3c7-run4-sranger 2>/dev/null || { echo
      "FAIL no worktree"; exit 1; }
      export FORGE_ARM_DIR=/Users/ranwei/Gitee/formal_method_guided…)
  ⎿  (file diff unavailable for this command)

⏺ Pre-flight check passed (all five lines OK). Now reading the inputs.

  Read 5 files, listed 1 directory (ctrl+o to expand)

⏺ Bash(for f in README.md 01_architecture.md 02_skeleton.md 03_leaves.md
      04_controller.md 05_review.md; do echo "==== $f"; cat
      forge.assets/vibe-coding-prompts/$f; don…)
  ⎿  (file diff unavailable for this command)

⏺ Bash(cat java.generated.project/build.gradle
      java.generated.project/settings.gradle
      java.generated.project/gradle.properties; ls -la forge.assets/corrections
      2>/dev/…)
  ⎿  (No output)

⏺ Pre-flight checks passed and all source files were read. I've mapped out the
  layer 1 architecture—packages, classes, and key design decisions (clock field
  naming, state transition priorities, tick self-loops, power-up
  command)—covering all requirements, and I'm moving on to building the layer 2
  skeleton.

⏺ Bash(cd /Users/ranwei/Gitee/forge-v3c7-run4-sranger/java.generated.project &&
      mkdir -p src/main/java/sranger/annotation src/main/java/sranger/event
      src/main/java/sra…)
  ⎿  (No output)

⏺ Write(java.generated.project/src/main/java/sranger/annotation/RoboChartType.ja
       va)
  ⎿  Wrote 16 lines to java.generated.project/src/main/java/sranger/annotat
     ion/RoboChartType.java
       1 package sranger.annotation;
       2
       3 import java.lang.annotation.ElementType;
       4 import java.lang.annotation.Retention;
       5 import java.lang.annotation.RetentionPolicy;
       6 import java.lang.annotation.Target;
       7
       8 /**
       9  * Declares the RoboChart type of a field, parameter or method r
         eturn value
     … +7 lines (ctrl+o to expand)

⏺ Write(java.generated.project/src/main/java/sranger/event/InputEvent.java)
  ⎿  Wrote 16 lines to
     java.generated.project/src/main/java/sranger/event/InputEvent.java
      1 package sranger.event;
      2
      3 /**
      4  * Input events of the SRanger controller. All three are signals
        without payload.
      5  */
      6 public sealed interface InputEvent {
      7
      8     /** Emitted by the IR sensor framework when the distance read
        ing crosses below the obstacle threshold. */
     … +8 lines (ctrl+o to expand)

⏺ Write(java.generated.project/src/main/java/sranger/event/OutputEvent.java)
  ⎿  Wrote 17 lines to
     java.generated.project/src/main/java/sranger/event/OutputEvent.java
       1 package sranger.event;
       2
       3 import sranger.annotation.RoboChartType;
       4
       5 /**
       6  * Output events of the SRanger controller. Move is the only one
         .
       7  */
       8 public sealed interface OutputEvent {
       9
     … +8 lines (ctrl+o to expand)

⏺ Write(java.generated.project/src/main/java/sranger/controller/SRangerMode.java
       )
  ⎿  Wrote 13 lines to java.generated.project/src/main/java/sranger/control
     ler/SRangerMode.java
       1 package sranger.controller;
       2
       3 /**
       4  * Operating modes of the SRanger controller; the initial mode c
         omes first.
       5  */
       6 public enum SRangerMode {
       7     /** Initial mode: the robot drives forward at moveVel. */
       8     MOVING,
       9     /** The robot rotates in place at turnVel for turnDuration s
     … +5 lines (ctrl+o to expand)

⏺ Write(java.generated.project/src/main/java/sranger/controller/SRangerConstants
       .java)
  ⎿  Wrote 23 lines to java.generated.project/src/main/java/sranger/control
     ler/SRangerConstants.java
       1 package sranger.controller;
       2
       3 import sranger.annotation.RoboChartType;
       4
       5 /**
       6  * Fixed configuration constants of the SRanger controller.
       7  */
       8 public final class SRangerConstants {
       9
      10     /** Linear velocity commanded in Moving (m/s). */
     … +13 lines (ctrl+o to expand)

⏺ Write(java.generated.project/src/main/java/sranger/sensor/Sensor.java)
  ⎿  Wrote 27 lines to
     java.generated.project/src/main/java/sranger/sensor/Sensor.java
       1 package sranger.sensor;
       2
       3 import sranger.annotation.RoboChartType;
       4
       5 /**
       6  * Sensor service exposing the latest IR distance reading.
       7  */
       8 public final class Sensor {
       9
      10     /**
     … +17 lines (ctrl+o to expand)

⏺ Write(java.generated.project/src/main/java/sranger/sensor/Clock.java)
  ⎿  Wrote 19 lines to
     java.generated.project/src/main/java/sranger/sensor/Clock.java
       1 package sranger.sensor;
       2
       3 import sranger.annotation.RoboChartType;
       4
       5 /**
       6  * Time source of the SRanger controller.
       7  */
       8 public final class Clock {
       9
      10     /**
     … +9 lines (ctrl+o to expand)

⏺ Write(java.generated.project/src/main/java/sranger/actuator/Actuator.java)
  ⎿  Wrote 38 lines to
     java.generated.project/src/main/java/sranger/actuator/Actuator.java
      1 package sranger.actuator;
      2
      3 import sranger.annotation.RoboChartType;
      4 import sranger.event.OutputEvent;
      5
      6 /**
      7  * Differential-drive actuator: receives Move commands and stores
         the last one issued.
      8  */
      9 public final class Actuator {
     … +29 lines (ctrl+o to expand)

⏺ Write(java.generated.project/src/main/java/sranger/controller/SRangerControlle
       r.java)
  ⎿  Wrote 52 lines to java.generated.project/src/main/java/sranger/control
     ler/SRangerController.java
       1 package sranger.controller;
       2
       3 import sranger.actuator.Actuator;
       4 import sranger.annotation.RoboChartType;
       5 import sranger.event.InputEvent;
       6 import sranger.sensor.Clock;
       7 import sranger.sensor.Sensor;
       8
       9 /**
      10  * Reactive SRanger controller: drives forward, turns in place f
     … +43 lines (~1 KB) (ctrl+o to expand)

⏺ Bash(OUT=/private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3c7-run4-sranger/9
      6711bf1-7f10-4b5d-9281-3e8d34884330/scratchpad/classes && rm -rf "$OUT" &&
      mkdir -p "$O…)
  ⎿  (No output)

⏺ Skeleton compiles. Now Layer 3, the leaves (Sensor, Clock, Actuator):

⏺ Write(java.generated.project/src/main/java/sranger/sensor/Sensor.java)
  ⎿  Added 13 lines, removed 2 lines
       7   */
       8  public final class Sensor {
       9  
      10 +    /** Distance reported when no valid reading is available; f
         +ar above the obstacle threshold. */                            
      11 +    @RoboChartType("real") private static final double NO_READI
         +NG_DISTANCE = 1000.0;                                          
      12 +                                                               
      13 +    @RoboChartType("real") private double latestDistance = NO_R
         +EADING_DISTANCE;                                               
      14 +                                                               
      15      /**
      11 -     * Records a new IR distance measurement.                  
      16 +     * Records a new IR distance measurement. A negative or NaN
         + measurement is                                                
      17 +     * not a valid reading and is treated as no reading availab
         +le.                                                            
      18       *
      19       * @param reading measured distance in metres
      20       */
      21      public void update(@RoboChartType("real") double reading) {
      22 +        if (reading >= 0.0) {                                  
      23 +            this.latestDistance = reading;                     
      24 +        } else {                                               
      25 +            this.latestDistance = NO_READING_DISTANCE;         
      26 +        }                                                      
      27      }
      28  
      29      /**
     ...
      33       */
      34      @RoboChartType("real")
      35      public double distance() {
      25 -        return 0.0;                                            
      36 +        return latestDistance;                                 
      37      }
      38  }

⏺ Write(java.generated.project/src/main/java/sranger/sensor/Clock.java)
  ⎿  Added 4 lines, removed 2 lines
       3  import sranger.annotation.RoboChartType;
       4  
       5  /**
       6 - * Time source of the SRanger controller.                      
       6 + * Monotonic time source of the SRanger controller, in seconds 
         +since construction.                                            
       7   */
       8  public final class Clock {
       9  
      10 +    @RoboChartType("real") private final double originSeconds =
         + System.nanoTime() / 1.0e9;                                    
      11 +                                                               
      12      /**
      13       * Returns the current time in seconds.
      14       *
     ...
      16       */
      17      @RoboChartType("real")
      18      public double now() {
      17 -        return 0.0;                                            
      19 +        return System.nanoTime() / 1.0e9 - originSeconds;      
      20      }
      21  }

⏺ Write(java.generated.project/src/main/java/sranger/actuator/Actuator.java)  ⎿  Added 11 lines, removed 3 lines
       8   */
       9  public final class Actuator {
      10  
      11 +    @RoboChartType("real") private double lastLinearVelocity = 
         +0.0;                                                           
      12 +    @RoboChartType("real") private double lastAngularVelocity =
         + 0.0;                                                          
      13 +                                                               
      14      /**
      12 -     * Receives an output command from the controller.         
      15 +     * Receives an output command from the controller and recor
         +ds it.                                                         
      16       *
      17       * @param command the command issued
      18       */
      19      public void apply(OutputEvent command) {
      20 +        if (command instanceof OutputEvent.Move) {             
      21 +            OutputEvent.Move move = (OutputEvent.Move) command;
      22 +            this.lastLinearVelocity = move.lv();               
      23 +            this.lastAngularVelocity = move.av();              
      24 +        }                                                      
      25      }
      26  
      27      /**
     ...
      31       */
      32      @RoboChartType("real")
      33      public double lastLinearVelocity() {
      26 -        return 0.0;                                            
      34 +        return lastLinearVelocity;                             
      35      }
      36  
      37      /**
     ...
      41       */
      42      @RoboChartType("real")
      43      public double lastAngularVelocity() {
      36 -        return 0.0;                                            
      44 +        return lastAngularVelocity;                            
      45      }
      46  }

⏺ Leaves done (the IDE classpath warnings are just the editor not having
  imported the Gradle project). Now Layer 4, the controller's step() and the
  power-up command:

⏺ Update(java.generated.project/src/main/java/sranger/controller/SRangerControll
        er.java)
  ⎿  Added 2 lines
      31          this.sensor = sensor;
      32          this.actuator = actuator;
      33          this.timer = timer;
      34 +        // Power-up: entry action of the initial mode Moving.  
      35 +        this.actuator.apply(new OutputEvent.Move(SRangerConstan
         +ts.MOVE_VEL, 0.0));                                            
      36      }
      37  
      38      /**

⏺ Update(java.generated.project/src/main/java/sranger/controller/SRangerControll
        er.java)
  ⎿  Added 31 lines
      50       * @param event the input event of this cycle
      51       */
      52      public void step(InputEvent event) {
      53 +        // --- Named guard predicates ---                      
      54 +        boolean obstacleDetected = sensor.distance() <= SRanger
         +Constants.OBSTACLE_THRESHOLD;                                  
      55 +        boolean turnDurationElapsed = timer.now() - clockResetT
         +ime >= SRangerConstants.TURN_DURATION;                         
      56 +                                                               
      57 +        // --- One block per mode; transitions in priority orde
         +r ---                                                          
      58 +        if (currentMode == SRangerMode.MOVING) {               
      59 +            if (event instanceof InputEvent.EndTask) {         
      60 +                currentMode = SRangerMode.FINAL;               
      61 +                actuator.apply(new OutputEvent.Move(0.0, 0.0));
      62 +            } else if (event instanceof InputEvent.Obstacle && 
         +obstacleDetected) {                                            
      63 +                currentMode = SRangerMode.TURNING;             
      64 +                clockResetTime = timer.now();                  
      65 +                actuator.apply(new OutputEvent.Move(0.0, SRange
         +rConstants.TURN_VEL));                                         
      66 +            } else if (event instanceof InputEvent.Tick) {     
      67 +                currentMode = SRangerMode.MOVING;              
      68 +            }                                                  
      69 +                                                               
      70 +        } else if (currentMode == SRangerMode.TURNING) {       
      71 +            if (event instanceof InputEvent.EndTask) {         
      72 +                currentMode = SRangerMode.FINAL;               
      73 +                actuator.apply(new OutputEvent.Move(0.0, 0.0));
      74 +            } else if (turnDurationElapsed) {                  
      75 +                currentMode = SRangerMode.MOVING;              
      76 +                actuator.apply(new OutputEvent.Move(SRangerCons
         +tants.MOVE_VEL, 0.0));                                         
      77 +            } else if (event instanceof InputEvent.Tick && !tur
         +nDurationElapsed) {                                            
      78 +                currentMode = SRangerMode.TURNING;             
      79 +            }                                                  
      80 +                                                               
      81 +        } else if (currentMode == SRangerMode.FINAL) {         
      82 +            // Terminal mode: no outgoing transitions.         
      83 +        }                                                      
      84      }
      85  }

⏺ Update(java.generated.project/src/main/java/sranger/controller/SRangerControll
        er.java)
  ⎿  Added 1 line
      3  import sranger.actuator.Actuator;
      4  import sranger.annotation.RoboChartType;
      5  import sranger.event.InputEvent;
      6 +import sranger.event.OutputEvent;                               
      7  import sranger.sensor.Clock;
      8  import sranger.sensor.Sensor;
      9  

⏺ Bash(SP=/private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3c7-run4-sranger/96
      711bf1-7f10-4b5d-9281-3e8d34884330/scratchpad && OUT=$SP/classes && rm -rf
      "$OUT" && mk…)
  ⎿  (No output)

⏺ The behaviour matches every transition requirement. Before writing the trace
  and review, I'm grepping for constructs outside the Java profile:

  Searched for 1 pattern (ctrl+o to expand)

⏺ Those four unannotated double lines actually have @RoboChartType("real") on
  the previous line, and the only reserved-word matches are the allowed event
  package and step parameter. Now I'll pull the line numbers for the trace and
  review.

  Searched for 1 pattern (ctrl+o to expand)

⏺ Now the trace file (Layer 4 wrap-up):

⏺ Bash(python3 -I - <<'EOF'
      import json…)
  ⎿  (No output)

⏺ Trace covers all 23 requirements and all 9 files. Now Layer 5, the
  self-review. Checking a few line ranges first:

⏺ Bash(sed -n '28,38p;60,86p'
      src/main/java/sranger/controller/SRangerController.java | cat -n | sed
      's/^/ /' | head -5; awk 'NR>=31 && NR<=37 {print NR": "$0} NR>=63…)
  ⎿  (No output)

⏺ Bash(cd /Users/ranwei/Gitee/forge-v3c7-run4-sranger && python3 -I - <<'EOF'
      import json…)
  ⎿  (No output)

⏺ Layer 5 changed no Java, so the trace is still current. Now the iteration
  summary (300 words at most):

⏺ Write(forge.assets/corrections/ITER_SUMMARY.md)
  ⎿  Wrote 43 lines to forge.assets/corrections/ITER_SUMMARY.md
       1 # SRanger — pass 4, iteration 1
       2
       3 ## What was written
       4
       5 Nine Java files under `sranger/`:
       6
       7 - `annotation/RoboChartType`
       8 - `event/InputEvent`, holding `Obstacle`, `Tick` and `EndTask`
       9 - `event/OutputEvent`, holding `Move(lv, av)`
      10 - `controller/SRangerMode`, with the modes `MOVING`, `TURNING` a
     … +34 lines (~2 KB) (ctrl+o to expand)

⏺ Bash(wc -w forge.assets/corrections/ITER_SUMMARY.md && git status --short |
      grep -vE '^ D \.(claude|remember)/' | head -30)
  ⎿  (No output)

⏺ The summary is 308 words; I'll trim it under 300. (The deleted files in git
  status were already deleted before this session started, by the pass setup.)

⏺ Update(forge.assets/corrections/ITER_SUMMARY.md)
  ⎿  Added 1 line, removed 1 line
      19  - `obstacleDetected`: `distance() <= OBSTACLE_THRESHOLD`
      20  - `turnDurationElapsed`: `timer.now() - clockResetTime >= TURN_
          DURATION`
      21  
      22 -Supporting files: `result_codegen.json` (54 entries; all 23 req
         -uirements and all 9 files covered) and `post_codegen.{md,json}`
         - (11 issues, status `uncertain`). The Java compiles under `--re
         -lease 17`. A scratch smoke test exercised all seven transitions
         -.                                                              
      22 +Also written: `result_codegen.json` (54 entries; all 23 require
         +ments and 9 files covered) and `post_codegen.{md,json}` (11 iss
         +ues, `uncertain`). The Java compiles under `--release 17`; a sc
         +ratch smoke test exercised all seven transitions.              
      23  
      24  ## Decisions the requirements did not settle
      25  

⏺ Update(forge.assets/corrections/ITER_SUMMARY.md)
  ⎿  Added 2 lines, removed 2 lines
      39  ## Unresolved
      40  
      41  - **Fractional threshold:** the model rounds `obstacleThreshold
          ` 0.5 up to 1, so the modelled guard is `distance <= 1`.
      42 -- **Initial entry action:** the model may not show it, because 
         -the profile has no branch for the initial transition.          
      43 -- **Two-component `Move`:** whether the extractor accepts a two
         --component event is unknown.                                   
      42 +- **Initial entry action:** the model may not show it; the prof
         +ile has no branch for the initial transition.                  
      43 +- **Two-component `Move`:** extractor support is unknown.      

⏺ Bash(wc -w forge.assets/corrections/ITER_SUMMARY.md &&
      OUT=/private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3c7-run4-sranger/96
      711bf1-7f10-4b5d-9281-3e8d34884330/s…)
  ⎿  (No output)

⏺ Everything checks out—Java compiles, the summary hits the 300-word limit, and
  both JSON files parse correctly. Since no token figure is available, I'll pass
  unavailable and run the pipeline runner now.

⏺ Bash(export FORGE_ARM_DIR=/Users/ranwei/Gitee/formal_method_guided_vibe_coding
      /experiments/v3-cold && export FORGE_WT_PREFIX=forge-v3c7 && export
      FORGE_STAGE_DIR=/Us…)
  ⎿  Running in the background (↓ to manage)

⏺ The runner is running in the background. I'll report its result when it
  finishes.

⏺ Background command "Run the pipeline runner once for sranger pass 4 iteration
1" completed (exit code 0)

⏺ Bash(L=/private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3c7-run4-sranger/967
      11bf1-7f10-4b5d-9281-3e8d34884330/scratchpad/runner.log; grep -n -A3 '===
      iteration 1 :…)  ⎿  (No output)

⏺ The pipeline converged on the first run: every phase passed. The runner ran
  once and exited with status 0. I did not open any post_*.md report or change
  the Java after it started.

  The line printed after === iteration 1 : what to do next ===, verbatim:

    ALL PHASES PASSED -> this run has CONVERGED. Stop iterating.

  It was followed by Report: converged at iteration 1.

  - Pre-flight: all five checks printed OK. No Java was present beforehand, so
    nothing was deleted.
  - Java (9 files under java.generated.project/src/main/java/sranger/):
    - the RoboChartType annotation;
    - InputEvent (Obstacle, Tick, EndTask) and OutputEvent (Move(lv, av));
    - SRangerMode (MOVING, TURNING, FINAL);
    - SRangerConstants;
    - SRangerController, whose step() has one block per mode and two named
      guards: obstacleDetected and turnDurationElapsed;
    - Sensor, Clock and Actuator.
  - Other files written:
    - java.generated.project/result_codegen.json: 54 entries, covering all 23
      requirements and all 9 files;
    - forge.assets/corrections/post_codegen.md and post_codegen.json: 11 issues,
      status uncertain;
    - forge.assets/corrections/ITER_SUMMARY.md: at the 300-word limit.
  - Decisions the requirements left open:
    - Moving → Turning needs both the Obstacle event and the obstacleDetected
      guard.
    - In Turning, EndTask takes priority, then the timed return to Moving, then
      Tick. Tick arrives every cycle and would otherwise block the timed exit.
    - The tick self-loops take no action, as SR-Beh4 and SR-Beh7 require.
    - The constructor issues the power-up Move(moveVel, 0), because no step()
      branch represents the initial transition.
    - Move carries two real values.
    - The clock field is named timer because clock is a reserved word.
    - The sensor reports 1000.0 m when it has no reading; that value is my own
      choice.
  - Still open: the model rounds obstacleThreshold from 0.5 up to 1, so the
    guard it checks is distance <= 1, not <= 0.5. The verifiers passed on that
    rounded guard.

  ITER_1_COMPLETE: 13 files written