
❯ # Reference run — sranger, run 2

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
  /Users/ranwei/Gitee/forge-v3r7-run2-sranger
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
  cd /Users/ranwei/Gitee/forge-v3r7-run2-sranger 2>/dev/null || { echo "FAIL no
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

  ## The loop: you drive it yourself

  1. Write the Java, the trace and the summary (iteration 1).
  2. Run `bash /Users/ranwei/Gitee/formal_method_guided_vibe_coding/scripts/run
  _claude_iteration.sh sranger 2 <iteration> <tokens|unavailable>`.
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
      /experiments/v3-reference
      export FORGE_WT_PREFIX=forge-v3r7…)
  ⎿  (file diff unavailable for this command)

⏺ The checks all pass. Now I'll read the inputs.

  Read 5 files, listed 1 directory (ctrl+o to expand)

⏺ Bash(cd /Users/ranwei/Gitee/forge-v3r7-run2-sranger/forge.assets/vibe-coding-p
      rompts && for f in README.md 01_architecture.md 02_skeleton.md; do echo
      "=== $f"; cat $…)
  ⎿  (file diff unavailable for this command)

⏺ Bash(cd /Users/ranwei/Gitee/forge-v3r7-run2-sranger/forge.assets/vibe-coding-p
      rompts && for f in 03_leaves.md 04_controller.md 05_review.md; do echo
      "=== $f"; cat $f…)
  ⎿  (No output)

⏺ Bash(cd /Users/ranwei/Gitee/forge-v3r7-run2-sranger && find
      java.generated.project -maxdepth 4 -not -path '*/.gradle/*' -not -path
      '*/build/*' | head -50 && cat java…)
  ⎿  (No output)

  Listed 1 directory (ctrl+o to expand)

⏺ I've laid out the Layer 1 architecture, mapping packages (annotation, event,
  controller, sensor, actuator) to each requirement, with no operation package
  needed since none applies. Next I'll build Layer 2, the skeleton.

⏺ Bash(cd /Users/ranwei/Gitee/forge-v3r7-run2-sranger/java.generated.project &&
      mkdir -p
      src/main/java/sranger/{annotation,event,controller,sensor,actuator} && ls
      src/…)
  ⎿  (No output)

⏺ Write(java.generated.project/src/main/java/sranger/annotation/RoboChartType.ja
       va)
  ⎿  Wrote 15 lines to java.generated.project/src/main/java/sranger/annotat
     ion/RoboChartType.java
       1 package sranger.annotation;
       2
       3 import java.lang.annotation.ElementType;
       4 import java.lang.annotation.Retention;
       5 import java.lang.annotation.RetentionPolicy;
       6 import java.lang.annotation.Target;
       7
       8 /**
       9  * Names the RoboChart type a Java field, parameter or return va
         lue maps to.
     … +6 lines (ctrl+o to expand)

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
      8     /** The IR distance reading has crossed below the obstacle-de
        tection threshold. */
     … +8 lines (ctrl+o to expand)

⏺ Write(java.generated.project/src/main/java/sranger/event/OutputEvent.java)
  ⎿  Wrote 12 lines to
     java.generated.project/src/main/java/sranger/event/OutputEvent.java
       1 package sranger.event;
       2
       3 import sranger.annotation.RoboChartType;
       4
       5 /**
       6  * Output events of the SRanger controller: a single combined mo
         tor command.
       7  */
       8 public sealed interface OutputEvent {
       9
     … +3 lines (ctrl+o to expand)

⏺ Write(java.generated.project/src/main/java/sranger/controller/SRangerMode.java
       )
  ⎿  Wrote 10 lines to java.generated.project/src/main/java/sranger/control
     ler/SRangerMode.java
       1 package sranger.controller;
       2
       3 /**
       4  * Operating modes of the SRanger controller; the initial mode f
         irst.
       5  */
       6 public enum SRangerMode {
       7     MOVING,
       8     TURNING,
       9     FINAL
     … +1 line (ctrl+o to expand)

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
  ⎿  Wrote 23 lines to
     java.generated.project/src/main/java/sranger/sensor/Sensor.java
       1 package sranger.sensor;
       2
       3 import sranger.annotation.RoboChartType;
       4
       5 /**
       6  * Sensor service exposing the latest forward-facing IR distance
          reading.
       7  */
       8 public final class Sensor {
       9
     … +14 lines (ctrl+o to expand)

⏺ Write(java.generated.project/src/main/java/sranger/sensor/Clock.java)
  ⎿  Wrote 19 lines to
     java.generated.project/src/main/java/sranger/sensor/Clock.java
       1 package sranger.sensor;
       2
       3 import sranger.annotation.RoboChartType;
       4
       5 /**
       6  * Time source of the controller, in seconds since start-up.
       7  */
       8 public final class Clock {
       9
      10     /** Moves the clock forward by the given number of seconds.
     … +10 lines (ctrl+o to expand)

⏺ Write(java.generated.project/src/main/java/sranger/actuator/Actuator.java)
  ⎿  Wrote 31 lines to
     java.generated.project/src/main/java/sranger/actuator/Actuator.java
      1 package sranger.actuator;
      2
      3 import sranger.annotation.RoboChartType;
      4 import sranger.event.OutputEvent;
      5
      6 /**
      7  * Differential-drive actuator: receives Move commands and keeps
        the last one issued.
      8  */
      9 public final class Actuator {
     … +22 lines (ctrl+o to expand)

⏺ Write(java.generated.project/src/main/java/sranger/controller/SRangerControlle
       r.java)
  ⎿  Wrote 34 lines to java.generated.project/src/main/java/sranger/control
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
      10  * Reactive SRanger controller: drives forward, turns in place o
     … +25 lines (ctrl+o to expand)

⏺ Bash(OUT=/private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3r7-run2-sranger/4
      97f52e8-db04-4e2d-9ce8-fa172ffb3d2b/scratchpad/classes && rm -rf $OUT &&
      mkdir -p $OUT…)
  ⎿  (No output)

⏺ Layer 2 compiles. Now Layer 3, the leaves: Sensor, Clock and Actuator.

⏺ Write(java.generated.project/src/main/java/sranger/sensor/Sensor.java)
  ⎿  Added 16 lines, removed 2 lines
       7   */
       8  public final class Sensor {
       9  
      10 -    /** Records a new IR measurement in metres. */             
      10 +    // Reported while no reading is available, so that missing 
         +data never looks like an obstacle.                             
      11 +    @RoboChartType("real") private static final double NO_READI
         +NG_DISTANCE = 100.0;                                           
      12 +                                                               
      13 +    @RoboChartType("real") private double latestDistance = NO_R
         +EADING_DISTANCE;                                               
      14 +                                                               
      15 +    /**                                                        
      16 +     * Records a new IR measurement in metres. A negative or Na
         +N measurement is not a                                         
      17 +     * valid distance and is treated as no reading.            
      18 +     */                                                        
      19      public void update(@RoboChartType("real") double reading) {
      20 +        if (reading >= 0.0) {                                  
      21 +            latestDistance = reading;                          
      22 +        } else {                                               
      23 +            latestDistance = NO_READING_DISTANCE;              
      24 +        }                                                      
      25      }
      26  
      27      /** Records that no IR reading is currently available. */
      28      public void markNoReading() {
      29 +        latestDistance = NO_READING_DISTANCE;                  
      30      }
      31  
      32      /** The latest IR distance reading in metres (non-negative)
          . */
      33      @RoboChartType("real")
      34      public double distance() {
      21 -        return 0.0;                                            
      35 +        return latestDistance;                                 
      36      }
      37  }
⏺ Write(java.generated.project/src/main/java/sranger/sensor/Clock.java)
  ⎿  Added 7 lines, removed 2 lines
       7   */
       8  public final class Clock {
       9  
      10 -    /** Moves the clock forward by the given number of seconds.
         - */                                                            
      10 +    @RoboChartType("real") private double elapsedSeconds = 0.0;
      11 +                                                               
      12 +    /** Moves the clock forward by the given number of seconds;
         + time never runs backwards. */                                 
      13      public void advance(@RoboChartType("real") double seconds) 
          {
      14 +        if (seconds > 0.0) {                                   
      15 +            elapsedSeconds = elapsedSeconds + seconds;         
      16 +        }                                                      
      17      }
      18  
      19      /** The current time in seconds. */
      20      @RoboChartType("real")
      21      public double now() {
      17 -        return 0.0;                                            
      22 +        return elapsedSeconds;                                 
      23      }
      24  }

⏺ Write(java.generated.project/src/main/java/sranger/actuator/Actuator.java)
  ⎿  Added 14 lines, removed 4 lines
       8   */
       9  public final class Actuator {
      10  
      11 -    /** Receives a motor command from the controller. */       
      11 +    private OutputEvent lastCommand = new OutputEvent.Move(0.0,
         + 0.0);                                                         
      12 +    @RoboChartType("real") private double lastLinearVelocity = 
         +0.0;                                                           
      13 +    @RoboChartType("real") private double lastAngularVelocity =
         + 0.0;                                                          
      14 +                                                               
      15 +    /** Receives a motor command from the controller and record
         +s it. */                                                       
      16      public void apply(OutputEvent command) {
      17 +        if (command instanceof OutputEvent.Move) {             
      18 +            OutputEvent.Move move = (OutputEvent.Move) command;
      19 +            lastCommand = move;                                
      20 +            lastLinearVelocity = move.lv();                    
      21 +            lastAngularVelocity = move.av();                   
      22 +        }                                                      
      23      }
      24  
      25      /** The last command issued. */
      26      public OutputEvent lastCommand() {
      17 -        return null;                                           
      27 +        return lastCommand;                                    
      28      }
      29  
      30      /** The last commanded linear velocity (m/s). */
      31      @RoboChartType("real")
      32      public double lastLinearVelocity() {
      23 -        return 0.0;                                            
      33 +        return lastLinearVelocity;                             
      34      }
      35  
      36      /** The last commanded angular velocity (rad/s). */
      37      @RoboChartType("real")
      38      public double lastAngularVelocity() {
      29 -        return 0.0;                                            
      39 +        return lastAngularVelocity;                            
      40      }
      41  }

⏺ Now Layer 4, the controller's step().

⏺ Write(java.generated.project/src/main/java/sranger/controller/SRangerControlle
       r.java)
  ⎿  Added 34 lines
      3  import sranger.actuator.Actuator;
      4  import sranger.annotation.RoboChartType;
      5  import sranger.event.InputEvent;
      6 +import sranger.event.OutputEvent;                               
      7  import sranger.sensor.Clock;
      8  import sranger.sensor.Sensor;
      9  
     ...
      18      private final Actuator actuator;
      19      private final Clock timer;
      20  
      21 +    /** Powers up the controller in Moving and issues Moving's 
         +forward command. */                                            
      22      public SRangerController(Sensor sensor, Actuator actuator, 
          Clock timer) {
      23          this.sensor = sensor;
      24          this.actuator = actuator;
      25          this.timer = timer;
      26 +        this.actuator.apply(new OutputEvent.Move(SRangerConstan
         +ts.MOVE_VEL, 0.0));                                            
      27      }
      28  
      29      /** The current operating mode. */
     ...
      33  
      34      /** Handles one control cycle: the given input event and an
          y autonomous transition. */
      35      public void step(InputEvent event) {
      36 +        // --- Named boolean predicates ---                    
      37 +        boolean obstacleDetected = sensor.distance() <= SRanger
         +Constants.OBSTACLE_THRESHOLD;                                  
      38 +        boolean turnDurationElapsed = timer.now() - clockResetT
         +ime >= SRangerConstants.TURN_DURATION;                         
      39 +                                                               
      40 +        // --- One block per mode; transitions in priority orde
         +r ---                                                          
      41 +        if (currentMode == SRangerMode.MOVING) {               
      42 +            if (event instanceof InputEvent.EndTask) {         
      43 +                currentMode = SRangerMode.FINAL;               
      44 +                actuator.apply(new OutputEvent.Move(0.0, 0.0));
      45 +            } else if (event instanceof InputEvent.Obstacle && 
         +obstacleDetected) {                                            
      46 +                currentMode = SRangerMode.TURNING;             
      47 +                clockResetTime = timer.now();                  
      48 +                actuator.apply(new OutputEvent.Move(0.0, SRange
         +rConstants.TURN_VEL));                                         
      49 +            } else if (event instanceof InputEvent.Tick) {     
      50 +                currentMode = SRangerMode.MOVING;              
      51 +            }                                                  
      52 +                                                               
      53 +        } else if (currentMode == SRangerMode.TURNING) {       
      54 +            if (event instanceof InputEvent.EndTask) {         
      55 +                currentMode = SRangerMode.FINAL;               
      56 +                actuator.apply(new OutputEvent.Move(0.0, 0.0));
      57 +            } else if (turnDurationElapsed) {                  
      58 +                currentMode = SRangerMode.MOVING;              
      59 +                actuator.apply(new OutputEvent.Move(SRangerCons
         +tants.MOVE_VEL, 0.0));                                         
      60 +            } else if (event instanceof InputEvent.Tick && !tur
         +nDurationElapsed) {                                            
      61 +                currentMode = SRangerMode.TURNING;             
      62 +            }                                                  
      63 +                                                               
      64 +        } else if (currentMode == SRangerMode.FINAL) {         
      65 +            // Terminal mode: no outgoing transitions.         
      66 +        }                                                      
      67      }
      68  }

⏺ Bash(SP=/private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3r7-run2-sranger/49
      7f52e8-db04-4e2d-9ce8-fa172ffb3d2b/scratchpad && cd
      /Users/ranwei/Gitee/forge-v3r7-run2…)
  ⎿  (No output)

⏺ Behaviour matches the requirements. Next I'll check that the Gradle build also
  passes.

⏺ Bash(timeout 300 ./gradlew build -q --offline 2>&1 | tail -15; echo
      "exit=${PIPESTATUS[0]}")
  ⎿  (No output)

⏺ Bash(./gradlew build -q --offline 2>&1 | tail -15; echo
      "exit=${pipestatus[1]}")
  ⎿  (No output)

⏺ Gradle build passes. Now Layer 5, the review. I need line numbers for the
  trace entries in post_codegen.

⏺ Bash(cd /Users/ranwei/Gitee/forge-v3r7-run2-sranger/java.generated.project/src
      /main/java/sranger && grep -n "" controller/SRangerController.java | sed
      -n 14,70p; gre…)
  ⎿  (No output)

⏺ Now the trace file, result_codegen.json:

⏺ Bash(cd /Users/ranwei/Gitee/forge-v3r7-run2-sranger/java.generated.project &&
      python3 -I - <<'EOF'
      import json…)
  ⎿  (No output)

⏺ Next, the Layer 5 review files post_codegen.{json,md}:

⏺ Bash(cd /Users/ranwei/Gitee/forge-v3r7-run2-sranger && python3 -I - <<'EOF'
      import json…)
  ⎿  (No output)

⏺ Now ITER_SUMMARY.md (300 words at most):

⏺ Write(forge.assets/corrections/ITER_SUMMARY.md)
  ⎿  Wrote 37 lines to forge.assets/corrections/ITER_SUMMARY.md
       1 # SRanger run 2 — iteration 1
       2
       3 No Java was present at the start.
       4
       5 ## What I wrote
       6
       7 Nine files under `sranger/`:
       8
       9 - `annotation/RoboChartType`
      10 - `event/InputEvent` (`Obstacle`, `Tick`, `EndTask`) and `event/
     … +28 lines (~2 KB) (ctrl+o to expand)

⏺ Bash(wc -w forge.assets/corrections/ITER_SUMMARY.md && export
      FORGE_ARM_DIR=/Users/ranwei/Gitee/formal_method_guided_vibe_coding/experim
      ents/v3-reference FORGE_WT_PR…)
  ⎿  (No output)
  ⎿  (timeout 10m)

⏺ The run converged at iteration 1: the runner printed ALL PHASES PASSED, with
  all 12 phases passing.

  Pre-flight check output:
  OK   pinned commit
  OK   no Java present
  OK   study is sranger
  OK   only this study's inputs
  OK   no archive
  There was no Java under java.generated.project/src/main/java/ at the start, so
  I deleted nothing. Nothing earlier in this session concerned another run.

  What I wrote. I worked the five layers in order and wrote nine Java files
  under sranger/ (java.generated.project/src/main/java/sranger/):
  - the annotation;
  - the input events (Obstacle, Tick, EndTask) and the output event Move(lv,
    av);
  - the mode enum, the constants and the controller;
  - Sensor, Clock and Actuator.

  SRangerController.java (java.generated.project/src/main/java/sranger/controlle
  r/SRangerController.java) has one branch per SR-Beh transition requirement:
  three in Moving, three in Turning and none in Final, which is terminal. Before
  running the pipeline I compiled with javac --release 17 and with the Gradle
  build. A small out-of-tree smoke run also behaved as the requirements
  describe.

  Results.
  - FDR4: 3 of 3 assertions passed.
  - Dafny: verified.
  - Isabelle: 21 lemmas proved, plus deadlock freedom.
  - Vacuity: passed with 0 blocking items and 2 advisory ones. I didn't open
    that report, because the run instructions only allow reading reports of
    failing phases.

  Decisions the requirements didn't settle. These are listed in the review files
  and the summary:
  - The Moving → Turning transition needs both the obstacle event and the
    obstacleDetected guard (distance <= 0.5).
  - In Turning, endTask comes first, then the timed return to Moving, then the
    tick self-loop. The tick branch only applies while the turn time hasn't
    elapsed, so a tick every cycle can't hold the robot in Turning.
  - On entering Turning, the clock is reset before Move(0, turnVel) is issued.
    SR-Beh2 and SR-FR2 list these two steps in opposite orders; I followed
    SR-Beh2.
  - The constructor issues the power-up Move(moveVel, 0).
  - With no reading, the sensor returns 100.0 m, a value I invented; a negative
    or NaN reading counts as no reading.
  - The actuator starts at Move(0, 0).
  - The clock field is named timer, because clock is a reserved word.

  Unresolved: obstacleThreshold = 0.5 is rounded up to 1 in the model's
  constants. The verified model's obstacle guard is therefore looser than the
  Java's.

  Running the Gradle build myself also created a java.generated.project/build/
  output folder.

  Other files:
  - result_codegen.json (java.generated.project/result_codegen.json) maps all 23
    requirements and names all nine files.
  - post_codegen.md (forge.assets/corrections/post_codegen.md) and its .json
    mirror hold the review, with status uncertain and 10 issues.
  - ITER_SUMMARY.md (forge.assets/corrections/ITER_SUMMARY.md) is 247 words.

  I passed unavailable as the token figure, since this session shows no cost
  number.

  ITER_1_COMPLETE: 13 files edited