
❯ # Reference run — steam_boiler, run 1

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

  - `forge.assets/case-studies/steam_boiler/requirements/requirement_all.json`
  — the requirements
  - `forge.assets/case-studies/steam_boiler/requirements/README.md` — the
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
    `/Users/ranwei/Gitee/v3r10-staging/`, and the git history (`git log`, `git
  show`, `git diff`).

  ## Hard constraints

  - Do not change anything under `forge.transformations/`, `forge.dashboard/`,
    `forge.assets/prompts/`, `forge.assets/vibe-coding-prompts/`,
    `forge.assets/case-studies/`, `pipeline.yaml` or `CLAUDE.md`. These are the
    instrument and the input, not your code.
  - Write Java only under `java.generated.project/src/main/java/steam_boiler/`,
  plus the
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
  /Users/ranwei/Gitee/forge-v3r10-run1-steam_boiler
  ```

  `cd` there first. If it does not exist, **stop and say so**; no other
  worktree is a
  substitute. Export these four variables in your shell before running the
  runner:

  ```
  export FORGE_ARM_DIR=/Users/ranwei/Gitee/formal_method_guided_vibe_coding/exp
  eriments/v3-reference
  export FORGE_WT_PREFIX=forge-v3r10
  export FORGE_STAGE_DIR=/Users/ranwei/Gitee/v3r10-staging
  export FORGE_SHA=56cb43d7835b43e8a986e6ba4c0e82f35083d6f1
  ```

  Run this check before you write any Java, and paste its output. If any line
  reads
  FAIL, stop.

  ```
  cd /Users/ranwei/Gitee/forge-v3r10-run1-steam_boiler 2>/dev/null || { echo
  "FAIL no worktree"; exit 1; }
  [ "$(git rev-parse HEAD)" = "$FORGE_SHA" ] && echo "OK   pinned commit" ||
  echo "FAIL wrong commit"
  [ "$(find java.generated.project/src -name '*.java' 2>/dev/null | wc -l | tr
  -d ' ')" = "0" ] && echo "OK   no Java present" || echo "FAIL Java present"
  [ "$(grep -m1 active_case_study pipeline.yaml | awk '{print $2}')" =
  "steam_boiler" ] && echo "OK   study is steam_boiler" || echo "FAIL wrong
  study"
  [ "$(ls forge.assets/case-studies)" = "steam_boiler" ] && echo "OK   only
  this study's inputs" || echo "FAIL other case studies present"
  [ ! -e docs/archive ] && echo "OK   no archive" || echo "FAIL docs/archive
  present"
  ```

  **Cost.** Pass your own token figure as the last argument of the runner if
  your
  session shows one (`/cost`); otherwise pass `unavailable`. Do not estimate.

  ## The loop: you drive it yourself

  1. Write the Java, the trace and the summary (iteration 1).
  2. Run `bash /Users/ranwei/Gitee/formal_method_guided_vibe_coding/scripts/run
  _claude_iteration.sh steam_boiler 1 <iteration> <tokens|unavailable>`.
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

⏺ I'll start with the required pre-check.

⏺ Bash(cd /Users/ranwei/Gitee/forge-v3r10-run1-steam_boiler 2>/dev/null || {
      echo "FAIL no worktree"; exit 1; }
      export FORGE_ARM_DIR=/Users/ranwei/Gitee/formal_method_…)
  ⎿  (file diff unavailable for this command)

⏺ Pre-check output (all OK):

  OK   pinned commit
  OK   no Java present
  OK   study is steam_boiler
  OK   only this study's inputs
  OK   no archive

  Now reading the inputs.

  Read 7 files, listed 1 directory (ctrl+o to expand)

⏺ Now the five layer prompts.

⏺ Bash(cd /Users/ranwei/Gitee/forge-v3r10-run1-steam_boiler/forge.assets/vibe-co
      ding-prompts && for f in README.md 01_architecture.md 02_skeleton.md
      03_leaves.md 04_co…)
  ⎿  (file diff unavailable for this command)

⏺ Bash(cd /Users/ranwei/Gitee/forge-v3r10-run1-steam_boiler && cat
      java.generated.project/build.gradle java.generated.project/settings.gradle
      java.generated.project/gr…)
  ⎿  (No output)

  Listed 1 directory (ctrl+o to expand)

⏺ I've mapped out the Layer 1 architecture for the steam boiler system—packages
  for events, controller, sensor, actuator, and program driver, with predicates
  isolated to sensor calls per the profile's requirements. Moving on to Layers
  2–4 and writing the files now.

⏺ Bash(cd
      /Users/ranwei/Gitee/forge-v3r10-run1-steam_boiler/java.generated.project
      && mkdir -p
      src/main/java/steam_boiler/{annotation,event,controller,sensor,actuator}…)
  ⎿  (No output)

⏺ Write(~/Gitee/forge-v3r10-run1-steam_boiler/java.generated.project/src/main/ja
       va/steam_boiler/annotation/RoboChartType.java)
  ⎿  Wrote 16 lines to ~/Gitee/forge-v3r10-run1-steam_boiler/java.generated
     .project/src/main/java/steam_boiler/annotation/RoboChartType.java
       1 package steam_boiler.annotation;
       2
       3 import java.lang.annotation.ElementType;
       4 import java.lang.annotation.Retention;
       5 import java.lang.annotation.RetentionPolicy;
       6 import java.lang.annotation.Target;
       7
       8 /**
       9  * Declares the RoboChart type of a field, parameter or return v
         alue
     … +7 lines (ctrl+o to expand)

⏺ Write(~/Gitee/forge-v3r10-run1-steam_boiler/java.generated.project/src/main/ja
       va/steam_boiler/BoilerConstants.java)
  ⎿  Wrote 52 lines to ~/Gitee/forge-v3r10-run1-steam_boiler/java.generated
     .project/src/main/java/steam_boiler/BoilerConstants.java
       1 package steam_boiler;
       2
       3 import steam_boiler.annotation.RoboChartType;
       4
       5 /**
       6  * Constant characteristics of the plant (SB-DM1, SB-DM2, SB-DM3
         ) and of the
       7  * communication rhythm (SB-V3, SB-DM9). The requirements name t
         hese
       8  * quantities but give no values; the values below are chosen to
     … +45 lines (~2 KB) (ctrl+o to expand)

⏺ Write(~/Gitee/forge-v3r10-run1-steam_boiler/java.generated.project/src/main/ja
       va/steam_boiler/event/FlowObservation.java)
  ⎿  Wrote 10 lines to ~/Gitee/forge-v3r10-run1-steam_boiler/java.generated
     .project/src/main/java/steam_boiler/event/FlowObservation.java
       1 package steam_boiler.event;
       2
       3 /**
       4  * What a pump controller observes about the circulation of wate
         r from its
       5  * pump into the boiler (SB-DM4).
       6  */
       7 public enum FlowObservation {
       8     NO_WATER_FLOW,
       9     WATER_FLOWING
     … +1 line (ctrl+o to expand)

⏺ Write(~/Gitee/forge-v3r10-run1-steam_boiler/java.generated.project/src/main/ja
       va/steam_boiler/event/ModeValue.java)
  ⎿  Wrote 13 lines to ~/Gitee/forge-v3r10-run1-steam_boiler/java.generated
     .project/src/main/java/steam_boiler/event/ModeValue.java
       1 package steam_boiler.event;
       2
       3 /**
       4  * The operating mode as carried by the MODE(m) message (SB-DM10
         ): exactly
       5  * five alternatives.
       6  */
       7 public enum ModeValue {
       8     INITIALIZATION_MODE,
       9     NORMAL_MODE,
     … +4 lines (ctrl+o to expand)

⏺ Write(~/Gitee/forge-v3r10-run1-steam_boiler/java.generated.project/src/main/ja
       va/steam_boiler/event/PumpStateData.java)
  ⎿  Wrote 10 lines to ~/Gitee/forge-v3r10-run1-steam_boiler/java.generated
     .project/src/main/java/steam_boiler/event/PumpStateData.java
       1 package steam_boiler.event;
       2
       3 import steam_boiler.annotation.RoboChartType;
       4
       5 /**
       6  * Content of PUMP_STATE(n, b): pump number n (1 to 4) and wheth
         er it is on
       7  * (SB-DM3, SB-DM9).
       8  */
       9 public record PumpStateData(@RoboChartType("nat") int pump, bool
     … +2 lines (ctrl+o to expand)

⏺ Write(~/Gitee/forge-v3r10-run1-steam_boiler/java.generated.project/src/main/ja
       va/steam_boiler/event/PumpControlStateData.java)
  ⎿  Wrote 10 lines to ~/Gitee/forge-v3r10-run1-steam_boiler/java.generated
     .project/src/main/java/steam_boiler/event/PumpControlStateData.java
       1 package steam_boiler.event;
       2
       3 import steam_boiler.annotation.RoboChartType;
       4
       5 /**
       6  * Content of PUMP_CONTROL_STATE(n, b): pump number n (1 to 4) a
         nd what its
       7  * controller observes (SB-DM4, SB-DM9).
       8  */
       9 public record PumpControlStateData(@RoboChartType("nat") int pum
     … +2 lines (ctrl+o to expand)

⏺ Write(~/Gitee/forge-v3r10-run1-steam_boiler/java.generated.project/src/main/ja
       va/steam_boiler/event/InputEvent.java)
  ⎿  Wrote 58 lines to ~/Gitee/forge-v3r10-run1-steam_boiler/java.generated
     .project/src/main/java/steam_boiler/event/InputEvent.java
      1 package steam_boiler.event;
      2
      3 import steam_boiler.annotation.RoboChartType;
      4
      5 /**
      6  * Messages received by the program (SB-DM9), plus the marker tha
        t closes the
      7  * receive stage of a cycle (SB-V3). Pump numbers run from 1 to 4
        .
      8  */
     … +50 lines (ctrl+o to expand)

⏺ Write(~/Gitee/forge-v3r10-run1-steam_boiler/java.generated.project/src/main/ja
       va/steam_boiler/event/OutputEvent.java)
  ⎿  Wrote 49 lines to ~/Gitee/forge-v3r10-run1-steam_boiler/java.generated
     .project/src/main/java/steam_boiler/event/OutputEvent.java
      1 package steam_boiler.event;
      2
      3 import steam_boiler.annotation.RoboChartType;
      4
      5 /**
      6  * Messages sent by the program to the physical units (SB-DM8). P
        ump numbers
      7  * run from 1 to 4.
      8  */
      9 public sealed interface OutputEvent {
     … +40 lines (ctrl+o to expand)

⏺ Write(~/Gitee/forge-v3r10-run1-steam_boiler/java.generated.project/src/main/ja
       va/steam_boiler/controller/OperatingMode.java)
  ⎿  Wrote 13 lines to ~/Gitee/forge-v3r10-run1-steam_boiler/java.generated
     .project/src/main/java/steam_boiler/controller/OperatingMode.java
       1 package steam_boiler.controller;
       2
       3 /**
       4  * The five operating modes of the program (SB-V2); the program
         starts in
       5  * initialization mode (SB-Beh1).
       6  */
       7 public enum OperatingMode {
       8     INITIALIZATION,
       9     NORMAL,
     … +4 lines (ctrl+o to expand)

⏺ Write(~/Gitee/forge-v3r10-run1-steam_boiler/java.generated.project/src/main/ja
       va/steam_boiler/actuator/ValvePosition.java)
  ⎿  Wrote 9 lines to ~/Gitee/forge-v3r10-run1-steam_boiler/java.generated.
     project/src/main/java/steam_boiler/actuator/ValvePosition.java
      1 package steam_boiler.actuator;
      2
      3 /**
      4  * Position of the evacuation valve (SB-DM7).
      5  */
      6 public enum ValvePosition {
      7     VALVE_CLOSED,
      8     VALVE_OPEN
      9 }

⏺ Layer 2 types are in place. Now Layer 3, the leaves: unit status, the sensor
  (receive and analysis stages), and the actuator.

⏺ Write(~/Gitee/forge-v3r10-run1-steam_boiler/java.generated.project/src/main/ja
       va/steam_boiler/sensor/UnitStatus.java)
  ⎿  Wrote 56 lines to ~/Gitee/forge-v3r10-run1-steam_boiler/java.generated
     .project/src/main/java/steam_boiler/sensor/UnitStatus.java
      1 package steam_boiler.sensor;
      2
      3 /**
      4  * Bookkeeping for one physical unit (SB-V9): whether it is belie
        ved sound or
      5  * out of order, whether the announced failure detection still aw
        aits its
      6  * acknowledgement (SB-FR6), and whether a repair notification re
        ceived in the
      7  * current cycle must be acknowledged (SB-FR7).
     … +49 lines (ctrl+o to expand)
⏺ Write(~/Gitee/forge-v3r10-run1-steam_boiler/java.generated.project/src/main/ja
       va/steam_boiler/sensor/BoilerSensor.java)
  ⎿  Wrote 758 lines to ~/Gitee/forge-v3r10-run1-steam_boiler/java.generate
     d.project/src/main/java/steam_boiler/sensor/BoilerSensor.java
       1 package steam_boiler.sensor;
       2
       3 import java.util.List;
       4 import steam_boiler.BoilerConstants;
       5 import steam_boiler.annotation.RoboChartType;
       6 import steam_boiler.event.FlowObservation;
       7 import steam_boiler.event.InputEvent;
       8 import steam_boiler.event.OutputEvent;
       9 import steam_boiler.event.PumpControlStateData;
      10 import steam_boiler.event.PumpStateData;
     … +748 lines (ctrl+o to expand)

⏺ Bash(sed -i '' 's/^    private static final int PUMPS =
      BoilerConstants.PUMP_COUNT;/    @RoboChartType("nat") private static final
      int PUMPS = BoilerConstants.PUMP_C…)
  ⎿  (No output)

⏺ Write(~/Gitee/forge-v3r10-run1-steam_boiler/java.generated.project/src/main/ja
       va/steam_boiler/actuator/BoilerActuator.java)
  ⎿  Wrote 145 lines to ~/Gitee/forge-v3r10-run1-steam_boiler/java.generate
     d.project/src/main/java/steam_boiler/actuator/BoilerActuator.java
       1 package steam_boiler.actuator;
       2
       3 import java.util.ArrayList;
       4 import java.util.List;
       5 import steam_boiler.BoilerConstants;
       6 import steam_boiler.annotation.RoboChartType;
       7 import steam_boiler.event.ModeValue;
       8 import steam_boiler.event.OutputEvent;
       9 import steam_boiler.sensor.BoilerSensor;
      10 import steam_boiler.sensor.UnitStatus;
     … +135 lines (ctrl+o to expand)

⏺ Layer 3 done. Now Layer 4, the controller step().

⏺ Write(~/Gitee/forge-v3r10-run1-steam_boiler/java.generated.project/src/main/ja
       va/steam_boiler/controller/SteamBoilerController.java)
  ⎿  Wrote 216 lines to
     ~/Gitee/forge-v3r10-run1-steam_boiler/java.generated.project/src/main/
     java/steam_boiler/controller/SteamBoilerController.java
      1 package steam_boiler.controller;
      2
      3 import steam_boiler.BoilerConstants;
      4 import steam_boiler.actuator.BoilerActuator;
      5 import steam_boiler.actuator.ValvePosition;
      6 import steam_boiler.event.InputEvent;
      7 import steam_boiler.event.ModeValue;
      8 import steam_boiler.event.OutputEvent;
      9 import steam_boiler.sensor.BoilerSensor;
     … +207 lines (ctrl+o to expand)

⏺ Write(~/Gitee/forge-v3r10-run1-steam_boiler/java.generated.project/src/main/ja
       va/steam_boiler/SteamBoilerProgram.java)
  ⎿  Wrote 56 lines to ~/Gitee/forge-v3r10-run1-steam_boiler/java.generated
     .project/src/main/java/steam_boiler/SteamBoilerProgram.java
       1 package steam_boiler;
       2
       3 import java.util.ArrayList;
       4 import java.util.List;
       5 import steam_boiler.actuator.BoilerActuator;
       6 import steam_boiler.controller.OperatingMode;
       7 import steam_boiler.controller.SteamBoilerController;
       8 import steam_boiler.event.InputEvent;
       9 import steam_boiler.event.OutputEvent;
      10 import steam_boiler.sensor.BoilerSensor;
     … +46 lines (ctrl+o to expand)

⏺ Compiling.

⏺ Bash(OUT=/private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3r10-run1-steam-bo
      iler/cd66d3b8-45a4-4d0a-9a36-7fdea2f69068/scratchpad/classes && rm -rf
      $OUT && mkdir -p…)
  ⎿  (No output)

⏺ Clean compile, no lint warnings. Before running the pipeline I'll smoke-test
  the logic with a scratch plant simulation (in the scratchpad, outside the
  project) to catch spurious failure detections.

⏺ Bash(SP=/private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3r10-run1-steam-boi
      ler/cd66d3b8-45a4-4d0a-9a36-7fdea2f69068/scratchpad && mkdir -p $SP/sim &&
      cat > $SP/si…)
  ⎿  (No output)

⏺ The smoke test revealed two issues: reactive regulation is too slow given pump
  latency and triggers level-risk stops, so I'll make it predictive; also every
  scenario hits emergency stop around cycle 13–18, which I need to debug by
  printing per-cycle details.

⏺ Bash(SP=/private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3r10-run1-steam-boi
      ler/cd66d3b8-45a4-4d0a-9a36-7fdea2f69068/scratchpad && cat >
      $SP/sim/Dbg.java <<'EOF'
      i…)
  ⎿  (No output)

⏺ Found that emergency stops are triggered solely by late regulation, not
  failure detection or dynamics checks. I'll fix this by making regulation
  predictive—accounting for the one-cycle latency and treating already-on pumps
  as guaranteed inflow in the risk prediction.

  Read 1 file (ctrl+o to expand)

⏺ Update(~/Gitee/forge-v3r10-run1-steam_boiler/java.generated.project/src/main/j
        ava/steam_boiler/sensor/BoilerSensor.java)
  ⎿  Added 1 line, removed 1 line
      584              if (pouringNow) {
      585                  current = current + P;
      586              }
      587 -            if (pouringNow && !pump.outOfOrder()) {           
      587 +            if (pumpOn[i] && !pump.outOfOrder()) {            
      588                  sure = sure + P;
      589              }
      590              if (pump.outOfOrder()) {

⏺ Update(~/Gitee/forge-v3r10-run1-steam_boiler/java.generated.project/src/main/j
        ava/steam_boiler/sensor/BoilerSensor.java)
  ⎿  Added 4 lines, removed 3 lines
      548      /**
      549       * SB-V7, SB-Beh25: bounds on the water the pumps poured d
           uring the last
      550       * cycle, the current throughput p, and the inflow the nex
           t cycle is sure of
      551 -     * (pouring sound pumps kept on) or cannot prevent (pumps 
          -out of order). A                                              
      552 -     * pump that has just been switched on pours nothing for o
          -ne cycle; a pump                                              
      553 -     * switched off stops at once.                            
      551 +     * (sound pumps that are on now and kept on: their balanci
          +ng is over by the                                             
      552 +     * next cycle) or cannot prevent (pumps out of order). A p
          +ump that has just                                             
      553 +     * been switched on pours nothing for one cycle; a pump sw
          +itched off stops                                              
      554 +     * at once.                                               
      555       */
      556      private void estimateInflow() {
      557          double low = 0.0;

  Read 1 file (ctrl+o to expand)

⏺ Update(~/Gitee/forge-v3r10-run1-steam_boiler/java.generated.project/src/main/j
        ava/steam_boiler/sensor/BoilerSensor.java)
  ⎿  Added 28 lines, removed 19 lines
      646      }
      647  
      648      /**
      649 -     * SB-Beh10, SB-Beh15, SB-Beh19: below N1 more pumps are s
          -witched on, above                                             
      650 -     * N2 pumps are switched off, aiming at the middle of [N1,
          - N2]; between N1                                              
      651 -     * and N2 the running pumps are kept.                     
      649 +     * SB-Beh10, SB-Beh15, SB-Beh19, SB-Beh25: the running pum
          +ps are kept while                                             
      650 +     * the level, predicted over the next two cycles, stays be
          +tween N1 and N2;                                              
      651 +     * otherwise the number of sound pumps is chosen that brin
          +gs the predicted                                              
      652 +     * level closest to the middle of [N1, N2]. Pumps on now p
          +our during the                                                
      653 +     * next cycle; pumps switched on now pour only from the cy
          +cle after.                                                    
      654       */
      655      private void planPumps() {
      656          int usable = 0;
     ...
      666          double middle = (levelLow + levelHigh) / 2.0;
      667          double steamMiddle = (steamLow + steamHigh) / 2.0;
      668          double target = (BoilerConstants.NORMAL_N1 + BoilerCon
           stants.NORMAL_N2) / 2.0;
      667 -        double neededRate = steamMiddle + (target - middle) / 
          -T;                                                            
      669 +        double nextLevel = levelAfterOneCycle(middle, steamMid
          +dle, running, running);                                       
      670 +        double laterLevel = levelAfterTwoCycles(middle, steamM
          +iddle, running, running);                                     
      671 +        boolean staysNormal = withinNormal(nextLevel) && withi
          +nNormal(laterLevel);                                          
      672          int required = running;
      669 -        if (middle < BoilerConstants.NORMAL_N1) {             
      670 -            int wanted = (int) Math.ceil(neededRate / P);     
      671 -            if (wanted > required) {                          
      672 -                required = wanted;                            
      673 +        if (!staysNormal) {                                   
      674 +            double bestDistance = Math.abs(laterLevel - target
          +);                                                            
      675 +            for (int pumps = 0; pumps <= usable; pumps++) {   
      676 +                double distance = Math.abs(levelAfterTwoCycles
          +(middle, steamMiddle, pumps, running) - target);              
      677 +                if (distance < bestDistance) {                
      678 +                    bestDistance = distance;                  
      679 +                    required = pumps;                         
      680 +                }                                             
      681              }
      674 -        } else if (middle > BoilerConstants.NORMAL_N2) {      
      675 -            int wanted = 0;                                   
      676 -            if (neededRate > 0.0) {                           
      677 -                wanted = (int) Math.floor(neededRate / P);    
      678 -            }                                                 
      679 -            if (wanted < required) {                          
      680 -                required = wanted;                            
      681 -            }                                                 
      682          }
      683          if (required > usable) {
      684              required = usable;
      685          }
      686 -        if (required < 0) {                                   
      687 -            required = 0;                                     
      688 -        }                                                     
      686          pumpsNeeded = required;
      687      }
      688  
      689 +    private static boolean withinNormal(double level) {       
      690 +        return level >= BoilerConstants.NORMAL_N1 && level <= 
          +BoilerConstants.NORMAL_N2;                                    
      691 +    }                                                         
      692 +                                                              
      693 +    private static double levelAfterOneCycle(double level, dou
          +ble steam, int pumps, int running) {                          
      694 +        return level - T * steam + T * P * Math.min(pumps, run
          +ning);                                                        
      695 +    }                                                         
      696 +                                                              
      697 +    private static double levelAfterTwoCycles(double level, do
          +uble steam, int pumps, int running) {                         
      698 +        return levelAfterOneCycle(level, steam, pumps, running
          +) - T * steam + T * P * pumps;                                
      699 +    }                                                         
      700 +                                                              
      701      private void rememberCycle() {
      702          for (int i = 0; i < PUMPS; i++) {
      703              previousPumpOn[i] = pumpOn[i];

⏺ Recompiling, then re-running the scenarios plus fault injection: a stuck pump,
  a level-device failure and repair, and a missing message.

⏺ Bash(SP=/private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3r10-run1-steam-boi
      ler/cd66d3b8-45a4-4d0a-9a36-7fdea2f69068/scratchpad && cd
      /Users/ranwei/Gitee/forge-v3r…)
  ⎿  (No output)

⏺ The simulation now behaves correctly across all scenarios: nominal regulation,
  device failures, missing messages, and stuck pumps. Moving to Layer 5, I'll
  first index every type and public method to ensure the trace covers them all.

⏺ Bash(for f in $(find src/main/java -name '*.java' | sort); do echo "## $f";
      grep -nE '^\s*(public|private|protected)?\s*(static\s+)?(final\s+)?(sealed
      \s+)?(class|rec…)
  ⎿  (No output)

⏺ The index matches my plan. I'll generate result_codegen.json from an explicit
  mapping list. The mapping covers every requirement, every Java file, every
  public method and every public constant.

⏺ Bash(SP=/private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3r10-run1-steam-boi
      ler/cd66d3b8-45a4-4d0a-9a36-7fdea2f69068/scratchpad && cat >
      $SP/gen_trace.py <<'EOF'
      i…)
  ⎿  (No output)

⏺ Checking that every traced element exists in its file, and that every file and
  public method is named:

⏺ Bash(cd
      /Users/ranwei/Gitee/forge-v3r10-run1-steam_boiler/java.generated.project
      && python3 -I - <<'EOF'
      import json, re, glob…)  ⎿  (No output)

⏺ Trace validated: no missing elements, every file and every public
  method/constant is traced. Now I'll gather line numbers for the Layer 5 review
  files.

⏺ Bash(cd /Users/ranwei/Gitee/forge-v3r10-run1-steam_boiler/java.generated.proje
      ct/src/main/java/steam_boiler && wc -l
      controller/SteamBoilerController.java sensor/Boi…)
  ⎿  (No output)

⏺ Writing post_codegen.json and the .md mirror from one source, so their content
  is identical:

⏺ Bash(SP=/private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3r10-run1-steam-boi
      ler/cd66d3b8-45a4-4d0a-9a36-7fdea2f69068/scratchpad && cat >
      $SP/gen_post_codegen.py <<…)
  ⎿  (No output)

⏺ Bash(grep -n 'boolean linkFailed' controller/SteamBoilerController.java; sed
      -n '69,82p' sensor/BoilerSensor.java | head -3; sed -n '689,697p'
      sensor/BoilerSensor.ja…)
  ⎿  (No output)

⏺ Bash(cd
      /Users/ranwei/Gitee/forge-v3r10-run1-steam_boiler/forge.assets/corrections
      && sed -i '' 's/"line_range": "51"/"line_range": "53"/' post_codegen.json
      && sed -…)
  ⎿  (No output)

⏺ Now ITER_SUMMARY.md (at most 300 words):

⏺ Write(ITER_SUMMARY.md)
  ⎿  Wrote 27 lines to ITER_SUMMARY.md
      1 # Iteration 1 — steam_boiler
      2
      3 No Java was present at start; nothing deleted.
      4
      5 ## What was written
      6
      7 - `controller/SteamBoilerController`: the only state machine. Its
         five modes (INITIALIZATION first) are written as one `step`. Eve
        ry transition is triggered by `TransmissionReceived` (end of the
        receive stage), so no guard-only transition can diverge. In each
     … +21 lines (~2 KB) (ctrl+o to expand)

⏺ Bash(wc -w /Users/ranwei/Gitee/forge-v3r10-run1-steam_boiler/forge.assets/corr
      ections/ITER_SUMMARY.md; cd
      /Users/ranwei/Gitee/forge-v3r10-run1-steam_boiler && git st…)
  ⎿  (No output)

⏺ Iteration 1 is set up with the Java files, trace, review files, and
  summary—the previously deleted files were already gone before I started. Now
  running the pipeline through the runner, passing unavailable since no token
  figure is shown for this session.

⏺ Bash(export FORGE_ARM_DIR=/Users/ranwei/Gitee/formal_method_guided_vibe_coding
      /experiments/v3-reference && export FORGE_WT_PREFIX=forge-v3r10 && export
      FORGE_STAGE_D…)
  ⎿  Running in the background (↓ to manage)

⏺ Iteration 1 is running in the background; I'll be notified when it finishes.

⏺ Iteration 1 is written, and the pipeline is now running on it in the
  background through the runner.

  - Written: 16 Java files under
    java.generated.project/src/main/java/steam_boiler/.
  - Trace: result_codegen.json maps all 53 requirements (233 entries) and names
    every file, public method and public constant.
  - Review: post_codegen.md/.json record 13 items: the invented constant values,
    interpretations of the requirements, and design choices.
  - Summary: ITER_SUMMARY.md is 269 words.

  No Java was present at the start, so nothing was deleted.

  Design:
  - SteamBoilerController is the only state machine. Every transition fires on
    one end-of-receive event, so no transition runs on a guard alone and the
    model can't loop internally.
  - In each mode the guards don't overlap and together cover every case.
    Emergency stop wins over rescue, and rescue over degraded. Emergency stop is
    a terminal state.
  - The data analysis, failure detection and the per-unit announcements and
    acknowledgements are in the sensor and actuator classes. That keeps the
    extracted model small, but it also means those messages are not verified by
    the pipeline.

  Before the run: the code compiles cleanly with javac, release 17 and all lint
  warnings on. I also ran a throwaway plant simulation in the scratchpad (not
  part of the project). It showed that pump control which only reacted once the
  level left N1–N2 was too slow, because a pump pours nothing for one cycle
  after starting, and it ended in emergency stop. I made the regulation look two
  cycles ahead. After that the simulation behaved as expected:
  - normal running settles at two pumps;
  - a level-meter failure goes to RESCUE and back to NORMAL on repair;
  - a missing message causes emergency stop;
  - a stuck pump goes to DEGRADED;
  - three STOPs in a row cause emergency stop.

  I'll read the failing phase reports, or report convergence, when the runner
  finishes.

  ITER_1_COMPLETE: 19 files edited

⏺ Background command "Run pipeline iteration 1 through the runner" completed
(exit code 0)

  Read 1 file (ctrl+o to expand)

⏺ Four phases failed: preflight, m2t, fdr4 and isabelle_verify. Reading their
  reports:

⏺ Bash(cd
      /Users/ranwei/Gitee/forge-v3r10-run1-steam_boiler/forge.assets/corrections
      && cat post_preflight.md; echo; echo "=================== M2T"; cat
      post_m2t.md)
  ⎿  (No output)

⏺ Found two issues: private sensor helpers need @RoboChartType("real") on their
  double parameters, and on must be renamed since it's a reserved keyword in
  RoboChart's notation despite missing from the rules' reserved-word list. Now
  I'll check fdr4 and Isabelle.

  Read 1 file (ctrl+o to expand)

⏺ fdr4 failed before reaching M2T, and the Isabelle errors reveal the generator
  inlines compound predicates, typing bare sensor calls as ℝ, which broke
  startMessageOutOfPlace. The fix is to give each boolean sensor call its own
  predicate and only combine predicates for compounds—applying the three fixes
  in the Java now.

⏺ Bash(cd /Users/ranwei/Gitee/forge-v3r10-run1-steam_boiler/java.generated.proje
      ct/src/main/java/steam_boiler && grep -n 'private static\|\.on()\|boolean
      on\|pumpsRequ…)
  ⎿  (No output)

⏺ Bash(python3 -I - <<'EOF'
      import re…)
  ⎿  (No output)

⏺ Checking that no other unannotated double parameters remain anywhere:

  Searched for 2 patterns (ctrl+o to expand)

⏺ All double parameters and returns are annotated. Now the controller predicate
  fix:

  Read 1 file (ctrl+o to expand)

⏺ Update(~/Gitee/forge-v3r10-run1-steam_boiler/java.generated.project/src/main/j
        ava/steam_boiler/controller/SteamBoilerController.java)  ⎿  Added 1 line, removed 1 line
      38      public void step(InputEvent event) {
      39          boolean operatorStopRequested = sensor.stopRequestedThr
          ice();
      40          boolean transmissionLost = sensor.transmissionFailure()
          ;
      41 -        boolean startMessageOutOfPlace = sensor.steamBoilerWait
         -ingReceived() || sensor.physicalUnitsReadyReceived();          
      41 +        boolean waitingSignalNow = sensor.steamBoilerWaitingRec
         +eived();                                                       
      42          boolean plantWaiting = sensor.plantWaitingSignalled();
      43          boolean unitsReady = sensor.physicalUnitsReadyReceived(
          );
      44          boolean readyAnnounced = sensor.programReadyAnnounced()
          ;

⏺ Update(~/Gitee/forge-v3r10-run1-steam_boiler/java.generated.project/src/main/j
        ava/steam_boiler/controller/SteamBoilerController.java)
  ⎿  Added 1 line
      50          boolean levelAboveN2 = sensor.waterLevel() > BoilerCons
          tants.NORMAL_N2;
      51          boolean levelBelowN1 = sensor.waterLevel() < BoilerCons
          tants.NORMAL_N1;
      52          boolean levelNearLimit = sensor.levelRisk();
      53 +        boolean startMessageOutOfPlace = waitingSignalNow || un
         +itsReady;                                                      
      54          boolean linkFailed = transmissionLost || startMessageOu
          tOfPlace;
      55          boolean startupFault = transmissionLost || steamNotZero
           || levelUnitFailed;
      56          boolean initialChecksPassed = plantWaiting && !startupF
          ault;

⏺ Recompiling and re-running the simulation to confirm behaviour is unchanged:

⏺ Bash(SP=/private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3r10-run1-steam-boi
      ler/cd66d3b8-45a4-4d0a-9a36-7fdea2f69068/scratchpad && cd
      /Users/ranwei/Gitee/forge-v3r…)
  ⎿  (No output)

⏺ Behaviour is unchanged. Re-validating the trace against the edited sources,
  then updating ITER_SUMMARY.md:

⏺ Bash(python3 -I - <<'EOF'
      import json, re, glob…)
  ⎿  (No output)

⏺ Write(ITER_SUMMARY.md)
  ⎿  Added 21 lines, removed 18 lines
       1 -# Iteration 1 — steam_boiler                                   
       1 +# Iteration 2 — steam_boiler                                   
       2  
       3 -No Java was present at start; nothing deleted.                 
       3 +## Iteration 1 result                                          
       4  
       5 -## What was written                                            
       5 +Failed: preflight, m2t, isabelle_verify; fdr4 had no verdict (b
         +locked by m2t).                                                
       6  
       7 -- `controller/SteamBoilerController`: the only state machine. I
         -ts five modes (INITIALIZATION first) are written as one `step`.
         - Every transition is triggered by `TransmissionReceived` (end o
         -f the receive stage), so no guard-only transition can diverge. 
         -In each mode the guards are exclusive and together cover every 
         -case, ordered by SB-FR8 (stop, transmission, level risk, rescue
         -, degraded). EMERGENCY_STOP is terminal.                       
       8 -- `sensor/BoilerSensor` (+ `UnitStatus`): receive and analysis 
         -stages. Holds q, v, p, the level bounds, unit status and the ST
         -OP count, and runs the FR1–FR5 checks. The controller only call
         -s trivial getters.                                             
       9 -- `actuator/BoilerActuator`: the outbox, valve and pump command
         -s, and the FR6/FR7 messages. `SteamBoilerProgram`: the cycle dr
         -iver.                                                          
      10 -- Events and payload types follow SB-DM8/DM9. `ModeValue` is th
         -e MODE payload.                                                
       7 +## Changes for iteration 2 (Java only; the trace's elements are
         + unchanged)                                                    
       8  
      12 -## Decisions the requirements left open                        
       9 +- Preflight: added `@RoboChartType("real")` (and `"nat"`) to th
         +e parameters and returns of the private numeric helpers in `Boi
         +lerSensor`.                                                    
      10 +- M2T: the `PumpStateData` component `on` is a keyword of RoboC
         +hart's textual notation, although the rules' reserved-word list
         + doesn't include it. It is renamed `switchedOn`. The `regulateP
         +umps` parameter is renamed `targetCount` so it no longer shares
         + the sensor method name `pumpsRequired`.                       
      11 +- Isabelle: in the generated theory, a compound predicate is in
         +lined, and a bare boolean sensor call inside it was typed `ℝ`. 
         +`startMessageOutOfPlace` now combines two predicates (`waitingS
         +ignalNow`, `unitsReady`), each holding a single sensor call. Be
         +haviour is unchanged.                                          
      12  
      14 -- Constant values (C=1000, M1=150, N1=400, N2=600, M2=800, W=25
         -, U1=U2=2, P=10) are invented.                                 
      15 -- Before STEAM_BOILER_WAITING only STOP counts; transmission fa
         -ilure is judged from then on.                                  
      16 -- Aberrant messages: duplicates, bad pump numbers, PHYSICAL_UNI
         -TS_READY before PROGRAM_READY, an acknowledgement or repair for
         - the wrong unit, start messages outside initialization.        
      17 -- "Pump-controller units fail" (Beh21) is read as any controlle
         -r.                                                             
      18 -- A level failure in normal or degraded goes to rescue first, e
         -ven with a steam failure (FR8, literal reading).               
      19 -- Level risk uses worst-case steam with the best pump action. R
         -egulation is predictive over two cycles, to allow for the pump 
         -start-up latency.                                              
      20 -- MODE(emergency stop) is sent once; then the program stops.   
      21 -- Per-unit protocol messages are outside the extracted model.  
      13 +## Design (from iteration 1)                                   
      14  
      23 -Details are in `post_codegen.md`.                              
      15 +- One state machine. Every transition is triggered by `Transmis
         +sionReceived`. In each mode the guards are exclusive and cover 
         +every case, ordered by SB-FR8. Emergency stop is terminal.     
      16 +- Analysis and FR1–FR5 are in `BoilerSensor`, the outbox and FR
         +6/FR7 messages in `BoilerActuator`, and the cycle driver is `St
         +eamBoilerProgram`.                                             
      17  
      18 +## Decisions the requirements left open                        
      19 +                                                               
      20 +- The constant values are invented (C=1000, M1=150, N1=400, N2=
         +600, M2=800, W=25, U1=U2=2, P=10).                             
      21 +- Before STEAM_BOILER_WAITING only STOP counts.                
      22 +- Aberrant messages are defined in `post_codegen.md`.          
      23 +- "Pump-controller units fail" means any controller.           
      24 +- A level failure in normal or degraded goes to rescue first (F
         +R8, literal reading).                                          
      25 +- Risk and regulation are worst-case and predictive over two cy
         +cles.                                                          
      26 +- MODE(emergency stop) is sent once.                           
      27 +                                                               
      28  ## Unresolved
      29  
      27 -None known.                                                    
      30 +None known. `on` missing from the rules' reserved-word list may
         + be a gap in the instrument's documentation; I worked around it
         + in the Java.                                                  

⏺ Bash(wc -w /Users/ranwei/Gitee/forge-v3r10-run1-steam_boiler/forge.assets/corr
      ections/ITER_SUMMARY.md && cd
      /Users/ranwei/Gitee/forge-v3r10-run1-steam_boiler && expo…)
  ⎿  Running in the background (↓ to manage)

⏺ Iteration 1 did not converge: preflight, M2T and Isabelle failed, and FDR4
  never ran because it depends on M2T. I've fixed the Java, and iteration 2 is
  now running through the runner in the background.

  What failed and what I changed:
  - Preflight: private calculation helpers in BoilerSensor had double parameters
    without the @RoboChartType("real") annotation. I annotated every numeric
    parameter and return value in that class, and a scan finds no unannotated
    double left anywhere.
  - M2T: the field on in PumpStateData is a keyword in RoboChart's text format,
    but the rules' reserved-word list doesn't include it. I renamed it to
    switchedOn. I also renamed the regulatePumps parameter to targetCount,
    because it had the same name as the sensor method pumpsRequired. The rules'
    list missing on may be a gap in the instrument's documentation; I've noted
    it in the summary.
  - Isabelle: a predicate built from one sensor call becomes a boolean function
    in the generated theory. A predicate built from several gets inlined, and a
    boolean sensor call inside it was given the type real (ℝ).
    startMessageOutOfPlace had two sensor calls joined by ||. I split each call
    into its own predicate and combined those, which leaves the behaviour
    unchanged.

  The code still compiles, the simulation scenarios give the same results as
  before, and the trace file still points at existing elements, so it needed no
  changes. ITER_SUMMARY.md is updated for iteration 2.

  ITER_2_COMPLETE: 6 files edited

⏺ Background command "Run pipeline iteration 2 through the runner" completed
(exit code 0)

  Read 1 file (ctrl+o to expand)

⏺ Progress: preflight and M2T now pass. FDR4 and Isabelle still fail. Reading
  both reports:

⏺ Bash(cd
      /Users/ranwei/Gitee/forge-v3r10-run1-steam_boiler/forge.assets/corrections
      && cat post_fdr4.md)
  ⎿  (No output)

⏺ Bash(cd
      /Users/ranwei/Gitee/forge-v3r10-run1-steam_boiler/forge.assets/corrections
      && head -c 9000 post_isabelle_verify.md)
  ⎿  (No output)

  Searched for 1 pattern (ctrl+o to expand)

⏺ Both failures trace back to how the extractor models Java shapes, not the
  instrument itself: FDR4 needs actuator calls with no arguments to avoid a CSP
  type error, and Isabelle needs guards built from single-call predicates
  instead of compound ones. I'll start by updating the actuator.

  Read 1 file (ctrl+o to expand)

⏺ Update(~/Gitee/forge-v3r10-run1-steam_boiler/java.generated.project/src/main/j
        ava/steam_boiler/actuator/BoilerActuator.java)
  ⎿  Added 32 lines, removed 7 lines
      42          }
      43      }
      44  
      45 +    /** Opens the evacuation valve to drain excess water (SB-DM
         +7, SB-Beh4). */                                                
      46 +    public void openValve() {                                  
      47 +        requestValve(ValvePosition.VALVE_OPEN);                
      48 +    }                                                          
      49 +                                                               
      50 +    /** Closes the evacuation valve if it is open (SB-DM7, SB-B
         +eh4). */                                                       
      51 +    public void closeValve() {                                 
      52 +        requestValve(ValvePosition.VALVE_CLOSED);              
      53 +    }                                                          
      54 +                                                               
      55      /**
      46 -     * Requests a position of the evacuation valve (SB-DM7, SB-
         -Beh4); VALVE is                                                
      47 -     * sent only when the requested position differs from the c
         -urrent one.                                                    
      56 +     * Switches pumps on or off to the number the regulation re
         +quires                                                         
      57 +     * (SB-Beh10, SB-Beh15, SB-Beh19).                         
      58       */
      49 -    public void setValve(ValvePosition position) {             
      59 +    public void regulatePumps() {                              
      60 +        runPumps(sensor.pumpsRequired());                      
      61 +    }                                                          
      62 +                                                               
      63 +    /** Keeps exactly one pump running to fill the boiler (SB-B
         +eh5). */                                                       
      64 +    public void runFillingPump() {                             
      65 +        runPumps(1);                                           
      66 +    }                                                          
      67 +                                                               
      68 +    /** Switches every running pump off. */                    
      69 +    public void stopPumps() {                                  
      70 +        runPumps(0);                                           
      71 +    }                                                          
      72 +                                                               
      73 +    /** VALVE toggles the valve, so it is sent only when the po
         +sition changes. */                                             
      74 +    private void requestValve(ValvePosition position) {        
      75          if (position != valve) {
      76              valve = position;
      77              outbox.add(new OutputEvent.Valve());
     ...
      79      }
      80  
      81      /**
      57 -     * Switches sound pumps on or off so that {@code targetCoun
         -t} of them run                                                 
      58 -     * (SB-Beh5, SB-Beh10): OPEN_PUMP for the lowest-numbered s
         -ound pumps that                                                
      59 -     * are off, CLOSE_PUMP for the highest-numbered sound pumps
         - that are on.                                                  
      82 +     * Switches sound pumps on or off so that {@code targetCoun
         +t} of them run:                                                
      83 +     * OPEN_PUMP for the lowest-numbered sound pumps that are o
         +ff, CLOSE_PUMP for                                             
      84 +     * the highest-numbered sound pumps that are on.           
      85       */
      61 -    public void regulatePumps(@RoboChartType("nat") int targetC
         -ount) {                                                        
      86 +    private void runPumps(@RoboChartType("nat") int targetCount
         +) {                                                            
      87          int running = 0;
      88          for (int pump = 1; pump <= BoilerConstants.PUMP_COUNT; 
          pump++) {
      89              if (sensor.pumpUsable(pump) && sensor.pumpReportedO
          n(pump)) {

⏺ Now the sensor's single startMessageReceived() query:

⏺ Bash(cd /Users/ranwei/Gitee/forge-v3r10-run1-steam_boiler/java.generated.proje
      ct/src/main/java/steam_boiler && python3 -I - <<'EOF'
      def sub(path, pairs):…)
  ⎿  (No output)

⏺ Rewriting the controller's step(): only single-call predicates, every one used
  by name in branch conditions, and argument-free actuator commands.

⏺ Write(~/Gitee/forge-v3r10-run1-steam_boiler/java.generated.project/src/main/ja
       va/steam_boiler/controller/SteamBoilerController.java)
  ⎿  Added 72 lines, removed 53 lines
      2  
      3  import steam_boiler.BoilerConstants;
      4  import steam_boiler.actuator.BoilerActuator;
      5 -import steam_boiler.actuator.ValvePosition;                     
      5  import steam_boiler.event.InputEvent;
      6  import steam_boiler.event.ModeValue;
      7  import steam_boiler.event.OutputEvent;
     ...
      37      public void step(InputEvent event) {
      38          boolean operatorStopRequested = sensor.stopRequestedThr
          ice();
      39          boolean transmissionLost = sensor.transmissionFailure()
          ;
      41 -        boolean waitingSignalNow = sensor.steamBoilerWaitingRec
         -eived();                                                       
      40 +        boolean startMessageArrived = sensor.startMessageReceiv
         +ed();                                                          
      41          boolean plantWaiting = sensor.plantWaitingSignalled();
      42          boolean unitsReady = sensor.physicalUnitsReadyReceived(
          );
      43          boolean readyAnnounced = sensor.programReadyAnnounced()
          ;
     ...
      49          boolean levelAboveN2 = sensor.waterLevel() > BoilerCons
          tants.NORMAL_N2;
      50          boolean levelBelowN1 = sensor.waterLevel() < BoilerCons
          tants.NORMAL_N1;
      51          boolean levelNearLimit = sensor.levelRisk();
      53 -        boolean startMessageOutOfPlace = waitingSignalNow || un
         -itsReady;                                                      
      54 -        boolean linkFailed = transmissionLost || startMessageOu
         -tOfPlace;                                                      
      55 -        boolean startupFault = transmissionLost || steamNotZero
         - || levelUnitFailed;                                           
      56 -        boolean initialChecksPassed = plantWaiting && !startupF
         -ault;                                                          
      57 -        boolean runningChecksPassed = !linkFailed && !levelNear
         -Limit;                                                         
      58 -        boolean estimateUnreliable = steamUnitFailed || pumpCon
         -trolUnitFailed;                                                
      52  
      53          if (currentMode == OperatingMode.INITIALIZATION) {
      54              if (event instanceof InputEvent.TransmissionReceive
          d && operatorStopRequested) {
     ...
       71                  currentMode = OperatingMode.EMERGENCY_STOP;
       72                  actuator.apply(new OutputEvent.ModeMessage(Mod
           eValue.EMERGENCY_STOP_MODE));
       73              } else if (event instanceof InputEvent.Transmissio
           nReceived && !operatorStopRequested
       81 -                    && initialChecksPassed && unitsReady && !n
          -onLevelUnitFailed) {                                          
       74 +                    && plantWaiting && !transmissionLost && !s
          +teamNotZero && !levelUnitFailed                               
       75 +                    && unitsReady && !nonLevelUnitFailed) {   
       76                  currentMode = OperatingMode.NORMAL;
       77                  actuator.apply(new OutputEvent.ModeMessage(Mod
           eValue.NORMAL_MODE));
       84 -                actuator.setValve(ValvePosition.VALVE_CLOSED);
       85 -                actuator.regulatePumps(sensor.pumpsRequired())
          -;                                                             
       78 +                actuator.closeValve();                        
       79 +                actuator.regulatePumps();                     
       80              } else if (event instanceof InputEvent.Transmissio
           nReceived && !operatorStopRequested
       87 -                    && initialChecksPassed && unitsReady && no
          -nLevelUnitFailed) {                                           
       81 +                    && plantWaiting && !transmissionLost && !s
          +teamNotZero && !levelUnitFailed                               
       82 +                    && unitsReady && nonLevelUnitFailed) {    
       83                  currentMode = OperatingMode.DEGRADED;
       84                  actuator.apply(new OutputEvent.ModeMessage(Mod
           eValue.DEGRADED_MODE));
       90 -                actuator.setValve(ValvePosition.VALVE_CLOSED);
       91 -                actuator.regulatePumps(sensor.pumpsRequired())
          -;                                                             
       85 +                actuator.closeValve();                        
       86 +                actuator.regulatePumps();                     
       87              } else if (event instanceof InputEvent.Transmissio
           nReceived && !operatorStopRequested
       93 -                    && initialChecksPassed && !unitsReady && r
          -eadyAnnounced) {                                              
       88 +                    && plantWaiting && !transmissionLost && !s
          +teamNotZero && !levelUnitFailed                               
       89 +                    && !unitsReady && readyAnnounced) {       
       90                  currentMode = OperatingMode.INITIALIZATION;
       91                  actuator.apply(new OutputEvent.ModeMessage(Mod
           eValue.INITIALIZATION_MODE));
       92                  actuator.apply(new OutputEvent.ProgramReady())
           ;
       93              } else if (event instanceof InputEvent.Transmissio
           nReceived && !operatorStopRequested
       98 -                    && initialChecksPassed && !unitsReady && !
          -readyAnnounced && levelAboveN2) {                             
       94 +                    && plantWaiting && !transmissionLost && !s
          +teamNotZero && !levelUnitFailed                               
       95 +                    && !unitsReady && !readyAnnounced && level
          +AboveN2) {                                                    
       96                  currentMode = OperatingMode.INITIALIZATION;
       97                  actuator.apply(new OutputEvent.ModeMessage(Mod
           eValue.INITIALIZATION_MODE));
      101 -                actuator.setValve(ValvePosition.VALVE_OPEN);  
      102 -                actuator.regulatePumps(0);                    
       98 +                actuator.openValve();                         
       99 +                actuator.stopPumps();                         
      100              } else if (event instanceof InputEvent.Transmissio
           nReceived && !operatorStopRequested
      104 -                    && initialChecksPassed && !unitsReady && !
          -readyAnnounced && !levelAboveN2                               
      105 -                    && levelBelowN1) {                        
      101 +                    && plantWaiting && !transmissionLost && !s
          +teamNotZero && !levelUnitFailed                               
      102 +                    && !unitsReady && !readyAnnounced && !leve
          +lAboveN2 && levelBelowN1) {                                   
      103                  currentMode = OperatingMode.INITIALIZATION;
      104                  actuator.apply(new OutputEvent.ModeMessage(Mod
           eValue.INITIALIZATION_MODE));
      108 -                actuator.setValve(ValvePosition.VALVE_CLOSED);
      109 -                actuator.regulatePumps(1);                    
      105 +                actuator.closeValve();                        
      106 +                actuator.runFillingPump();                    
      107              } else if (event instanceof InputEvent.Transmissio
           nReceived && !operatorStopRequested
      111 -                    && initialChecksPassed && !unitsReady && !
          -readyAnnounced && !levelAboveN2                               
      112 -                    && !levelBelowN1) {                       
      108 +                    && plantWaiting && !transmissionLost && !s
          +teamNotZero && !levelUnitFailed                               
      109 +                    && !unitsReady && !readyAnnounced && !leve
          +lAboveN2 && !levelBelowN1) {                                  
      110                  currentMode = OperatingMode.INITIALIZATION;
      111                  actuator.apply(new OutputEvent.ModeMessage(Mod
           eValue.INITIALIZATION_MODE));
      115 -                actuator.setValve(ValvePosition.VALVE_CLOSED);
      116 -                actuator.regulatePumps(0);                    
      112 +                actuator.closeValve();                        
      113 +                actuator.stopPumps();                         
      114                  actuator.apply(new OutputEvent.ProgramReady())
           ;
      115              }
      116  
     ...
      119                  currentMode = OperatingMode.EMERGENCY_STOP;
      120                  actuator.apply(new OutputEvent.ModeMessage(Mod
           eValue.EMERGENCY_STOP_MODE));
      121              } else if (event instanceof InputEvent.Transmissio
           nReceived && !operatorStopRequested
      125 -                    && linkFailed) {                          
      122 +                    && transmissionLost) {                    
      123                  currentMode = OperatingMode.EMERGENCY_STOP;
      124                  actuator.apply(new OutputEvent.ModeMessage(Mod
           eValue.EMERGENCY_STOP_MODE));
      125              } else if (event instanceof InputEvent.Transmissio
           nReceived && !operatorStopRequested
      129 -                    && !linkFailed && levelNearLimit) {       
      126 +                    && !transmissionLost && startMessageArrive
          +d) {                                                          
      127                  currentMode = OperatingMode.EMERGENCY_STOP;
      128                  actuator.apply(new OutputEvent.ModeMessage(Mod
           eValue.EMERGENCY_STOP_MODE));
      129              } else if (event instanceof InputEvent.Transmissio
           nReceived && !operatorStopRequested
      133 -                    && runningChecksPassed && levelUnitFailed)
          - {                                                            
      130 +                    && !transmissionLost && !startMessageArriv
          +ed && levelNearLimit) {                                       
      131 +                currentMode = OperatingMode.EMERGENCY_STOP;   
      132 +                actuator.apply(new OutputEvent.ModeMessage(Mod
          +eValue.EMERGENCY_STOP_MODE));                                 
      133 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && !operatorStopRequested                           
      134 +                    && !transmissionLost && !startMessageArriv
          +ed && !levelNearLimit && levelUnitFailed) {                   
      135                  currentMode = OperatingMode.RESCUE;
      136                  actuator.apply(new OutputEvent.ModeMessage(Mod
           eValue.RESCUE_MODE));
      136 -                actuator.regulatePumps(sensor.pumpsRequired())
          -;                                                             
      137 +                actuator.regulatePumps();                     
      138              } else if (event instanceof InputEvent.Transmissio
           nReceived && !operatorStopRequested
      138 -                    && runningChecksPassed && !levelUnitFailed
          - && nonLevelUnitFailed) {                                     
      139 +                    && !transmissionLost && !startMessageArriv
          +ed && !levelNearLimit && !levelUnitFailed                     
      140 +                    && nonLevelUnitFailed) {                  
      141                  currentMode = OperatingMode.DEGRADED;
      142                  actuator.apply(new OutputEvent.ModeMessage(Mod
           eValue.DEGRADED_MODE));
      141 -                actuator.regulatePumps(sensor.pumpsRequired())
          -;                                                             
      143 +                actuator.regulatePumps();                     
      144              } else if (event instanceof InputEvent.Transmissio
           nReceived && !operatorStopRequested
      143 -                    && runningChecksPassed && !levelUnitFailed
          - && !nonLevelUnitFailed) {                                    
      145 +                    && !transmissionLost && !startMessageArriv
          +ed && !levelNearLimit && !levelUnitFailed                     
      146 +                    && !nonLevelUnitFailed) {                 
      147                  currentMode = OperatingMode.NORMAL;
      148                  actuator.apply(new OutputEvent.ModeMessage(Mod
           eValue.NORMAL_MODE));
      146 -                actuator.regulatePumps(sensor.pumpsRequired())
          -;                                                             
      149 +                actuator.regulatePumps();                     
      150              }
      151  
      152          } else if (currentMode == OperatingMode.DEGRADED) {
     ...
      154                  currentMode = OperatingMode.EMERGENCY_STOP;
      155                  actuator.apply(new OutputEvent.ModeMessage(Mod
           eValue.EMERGENCY_STOP_MODE));
      156              } else if (event instanceof InputEvent.Transmissio
           nReceived && !operatorStopRequested
      154 -                    && linkFailed) {                          
      157 +                    && transmissionLost) {                    
      158                  currentMode = OperatingMode.EMERGENCY_STOP;
      159                  actuator.apply(new OutputEvent.ModeMessage(Mod
           eValue.EMERGENCY_STOP_MODE));
      160              } else if (event instanceof InputEvent.Transmissio
           nReceived && !operatorStopRequested
      158 -                    && !linkFailed && levelNearLimit) {       
      161 +                    && !transmissionLost && startMessageArrive
          +d) {                                                          
      162                  currentMode = OperatingMode.EMERGENCY_STOP;
      163                  actuator.apply(new OutputEvent.ModeMessage(Mod
           eValue.EMERGENCY_STOP_MODE));
      164              } else if (event instanceof InputEvent.Transmissio
           nReceived && !operatorStopRequested
      162 -                    && runningChecksPassed && levelUnitFailed)
          - {                                                            
      165 +                    && !transmissionLost && !startMessageArriv
          +ed && levelNearLimit) {                                       
      166 +                currentMode = OperatingMode.EMERGENCY_STOP;   
      167 +                actuator.apply(new OutputEvent.ModeMessage(Mod
          +eValue.EMERGENCY_STOP_MODE));                                 
      168 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && !operatorStopRequested                           
      169 +                    && !transmissionLost && !startMessageArriv
          +ed && !levelNearLimit && levelUnitFailed) {                   
      170                  currentMode = OperatingMode.RESCUE;
      171                  actuator.apply(new OutputEvent.ModeMessage(Mod
           eValue.RESCUE_MODE));
      165 -                actuator.regulatePumps(sensor.pumpsRequired())
          -;                                                             
      172 +                actuator.regulatePumps();                     
      173              } else if (event instanceof InputEvent.Transmissio
           nReceived && !operatorStopRequested
      167 -                    && runningChecksPassed && !levelUnitFailed
          - && !nonLevelUnitFailed) {                                    
      174 +                    && !transmissionLost && !startMessageArriv
          +ed && !levelNearLimit && !levelUnitFailed                     
      175 +                    && !nonLevelUnitFailed) {                 
      176                  currentMode = OperatingMode.NORMAL;
      177                  actuator.apply(new OutputEvent.ModeMessage(Mod
           eValue.NORMAL_MODE));
      170 -                actuator.regulatePumps(sensor.pumpsRequired())
          -;                                                             
      178 +                actuator.regulatePumps();                     
      179              } else if (event instanceof InputEvent.Transmissio
           nReceived && !operatorStopRequested
      172 -                    && runningChecksPassed && !levelUnitFailed
          - && nonLevelUnitFailed) {                                     
      180 +                    && !transmissionLost && !startMessageArriv
          +ed && !levelNearLimit && !levelUnitFailed                     
      181 +                    && nonLevelUnitFailed) {                  
      182                  currentMode = OperatingMode.DEGRADED;
      183                  actuator.apply(new OutputEvent.ModeMessage(Mod
           eValue.DEGRADED_MODE));
      175 -                actuator.regulatePumps(sensor.pumpsRequired())
          -;                                                             
      184 +                actuator.regulatePumps();                     
      185              }
      186  
      187          } else if (currentMode == OperatingMode.RESCUE) {
     ...
      189                  currentMode = OperatingMode.EMERGENCY_STOP;
      190                  actuator.apply(new OutputEvent.ModeMessage(Mod
           eValue.EMERGENCY_STOP_MODE));
      191              } else if (event instanceof InputEvent.Transmissio
           nReceived && !operatorStopRequested
      183 -                    && linkFailed) {                          
      192 +                    && transmissionLost) {                    
      193                  currentMode = OperatingMode.EMERGENCY_STOP;
      194                  actuator.apply(new OutputEvent.ModeMessage(Mod
           eValue.EMERGENCY_STOP_MODE));
      195              } else if (event instanceof InputEvent.Transmissio
           nReceived && !operatorStopRequested
      187 -                    && !linkFailed && levelNearLimit) {       
      196 +                    && !transmissionLost && startMessageArrive
          +d) {                                                          
      197                  currentMode = OperatingMode.EMERGENCY_STOP;
      198                  actuator.apply(new OutputEvent.ModeMessage(Mod
           eValue.EMERGENCY_STOP_MODE));
      199              } else if (event instanceof InputEvent.Transmissio
           nReceived && !operatorStopRequested
      191 -                    && runningChecksPassed && estimateUnreliab
          -le) {                                                         
      200 +                    && !transmissionLost && !startMessageArriv
          +ed && levelNearLimit) {                                       
      201                  currentMode = OperatingMode.EMERGENCY_STOP;
      202                  actuator.apply(new OutputEvent.ModeMessage(Mod
           eValue.EMERGENCY_STOP_MODE));
      203              } else if (event instanceof InputEvent.Transmissio
           nReceived && !operatorStopRequested
      195 -                    && runningChecksPassed && !estimateUnrelia
          -ble && levelUnitFailed) {                                     
      204 +                    && !transmissionLost && !startMessageArriv
          +ed && !levelNearLimit && steamUnitFailed) {                   
      205 +                currentMode = OperatingMode.EMERGENCY_STOP;   
      206 +                actuator.apply(new OutputEvent.ModeMessage(Mod
          +eValue.EMERGENCY_STOP_MODE));                                 
      207 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && !operatorStopRequested                           
      208 +                    && !transmissionLost && !startMessageArriv
          +ed && !levelNearLimit && !steamUnitFailed                     
      209 +                    && pumpControlUnitFailed) {               
      210 +                currentMode = OperatingMode.EMERGENCY_STOP;   
      211 +                actuator.apply(new OutputEvent.ModeMessage(Mod
          +eValue.EMERGENCY_STOP_MODE));                                 
      212 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && !operatorStopRequested                           
      213 +                    && !transmissionLost && !startMessageArriv
          +ed && !levelNearLimit && !steamUnitFailed                     
      214 +                    && !pumpControlUnitFailed && levelUnitFail
          +ed) {                                                         
      215                  currentMode = OperatingMode.RESCUE;
      216                  actuator.apply(new OutputEvent.ModeMessage(Mod
           eValue.RESCUE_MODE));
      198 -                actuator.regulatePumps(sensor.pumpsRequired())
          -;                                                             
      217 +                actuator.regulatePumps();                     
      218              } else if (event instanceof InputEvent.Transmissio
           nReceived && !operatorStopRequested
      200 -                    && runningChecksPassed && !estimateUnrelia
          -ble && !levelUnitFailed                                       
      201 -                    && !nonLevelUnitFailed) {                 
      219 +                    && !transmissionLost && !startMessageArriv
          +ed && !levelNearLimit && !steamUnitFailed                     
      220 +                    && !pumpControlUnitFailed && !levelUnitFai
          +led && !nonLevelUnitFailed) {                                 
      221                  currentMode = OperatingMode.NORMAL;
      222                  actuator.apply(new OutputEvent.ModeMessage(Mod
           eValue.NORMAL_MODE));
      204 -                actuator.regulatePumps(sensor.pumpsRequired())
          -;                                                             
      223 +                actuator.regulatePumps();                     
      224              } else if (event instanceof InputEvent.Transmissio
           nReceived && !operatorStopRequested
      206 -                    && runningChecksPassed && !estimateUnrelia
          -ble && !levelUnitFailed                                       
      207 -                    && nonLevelUnitFailed) {                  
      225 +                    && !transmissionLost && !startMessageArriv
          +ed && !levelNearLimit && !steamUnitFailed                     
      226 +                    && !pumpControlUnitFailed && !levelUnitFai
          +led && nonLevelUnitFailed) {                                  
      227                  currentMode = OperatingMode.DEGRADED;
      228                  actuator.apply(new OutputEvent.ModeMessage(Mod
           eValue.DEGRADED_MODE));
      210 -                actuator.regulatePumps(sensor.pumpsRequired())
          -;                                                             
      229 +                actuator.regulatePumps();                     
      230              }
      231  
      232          } else if (currentMode == OperatingMode.EMERGENCY_STOP
           ) {

⏺ Every predicate now appears by name in a branch condition. Recompiling and
  re-running the simulation:

⏺ Bash(SP=/private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3r10-run1-steam-boi
      ler/cd66d3b8-45a4-4d0a-9a36-7fdea2f69068/scratchpad && cd
      /Users/ranwei/Gitee/forge-v3r…)
  ⎿  (No output)

⏺ Behaviour is identical. Updating the trace mapping for the renamed and new
  actuator and sensor methods, then regenerating and validating it:

⏺ Bash(SP=/private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3r10-run1-steam-boi
      ler/cd66d3b8-45a4-4d0a-9a36-7fdea2f69068/scratchpad && python3 -I -
      "$SP/gen_trace.py"…)
  ⎿  (No output)

⏺ The trace is valid: 238 entries, every public element named. Updating
  post_codegen for the new actuator commands and the shifted line numbers:

⏺ Bash(cd /Users/ranwei/Gitee/forge-v3r10-run1-steam_boiler/java.generated.proje
      ct/src/main/java/steam_boiler && grep -n 'currentMode ==
      OperatingMode\|public void ste…)  ⎿  (No output)

⏺ Bash(SP=/private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3r10-run1-steam-boi
      ler/cd66d3b8-45a4-4d0a-9a36-7fdea2f69068/scratchpad && python3 -I -
      "$SP/gen_post_codeg…)
  ⎿  (No output)

⏺ The script still had the original line "51" (I corrected that earlier in the
  output files, not in the script), so nothing was replaced. Retrying with the
  right string:

⏺ Bash(SP=/private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3r10-run1-steam-boi
      ler/cd66d3b8-45a4-4d0a-9a36-7fdea2f69068/scratchpad && grep -c 'tr(C,
      "51", "step (link…)
  ⎿  (No output)

⏺ Now the iteration summary:

⏺ Write(ITER_SUMMARY.md)
  ⎿  Added 9 lines, removed 9 lines
       1 -# Iteration 2 — steam_boiler                                   
       1 +# Iteration 3 — steam_boiler                                   
       2  
       3 -## Iteration 1 result                                          
       3 +## Iteration 2 result                                          
       4  
       5 -Failed: preflight, m2t, isabelle_verify; fdr4 had no verdict (b
         -locked by m2t).                                                
       5 +Passed: compile, coverage, preflight, t2m, m2m, m2t, dafny_gen,
         + isabelle_gen, dafny_verify, vacuity. Failed: fdr4, isabelle_ve
         +rify.                                                          
       6  
       7 -## Changes for iteration 2 (Java only; the trace's elements are
         - unchanged)                                                    
       7 +## Changes for iteration 3 (Java and trace)                    
       8  
       9 -- Preflight: added `@RoboChartType("real")` (and `"nat"`) to th
         -e parameters and returns of the private numeric helpers in `Boi
         -lerSensor`.                                                    
      10 -- M2T: the `PumpStateData` component `on` is a keyword of RoboC
         -hart's textual notation, although the rules' reserved-word list
         - doesn't include it. It is renamed `switchedOn`. The `regulateP
         -umps` parameter is renamed `targetCount` so it no longer shares
         - the sensor method name `pumpsRequired`.                       
      11 -- Isabelle: in the generated theory, a compound predicate is in
         -lined, and a bare boolean sensor call inside it was typed `ℝ`. 
         -`startMessageOutOfPlace` now combines two predicates (`waitingS
         -ignalNow`, `unitsReady`), each holding a single sensor call. Be
         -haviour is unchanged.                                          
       9 +- FDR4 (CSP type error at `regulatePumps.out`): an actuator cal
         +l becomes an output event named after the method, and its argum
         +ent is dropped. An `int` parameter still gives the event a type
         +, so `regulatePumps(n)` became an `Int` event emitted with no v
         +alue. The controller now uses argument-free commands: `regulate
         +Pumps()` (the count comes from the sensor), `runFillingPump()`,
         + `stopPumps()`, `openValve()`, `closeValve()`. Open and close a
         +re now separate events in the model.                           
      10 +- Isabelle (`waitingSignalNow () :: ℝ`): a predicate referenced
         + only inside another predicate was given type `ℝ`. The compound
         + predicates are removed. Every guard is built in the `if` condi
         +tions from single-call predicates, each used by name in some br
         +anch. SBW/PUR outside initialization is one sensor query, `star
         +tMessageReceived()`.                                           
      11 +- Behaviour is unchanged (checked with a scratch simulation). T
         +he trace and `post_codegen` are refreshed for the renamed actua
         +tor methods.                                                   
      12  
      13 -## Design (from iteration 1)                                   
      13 +## Design (unchanged)                                          
      14  
      15  - One state machine. Every transition is triggered by `Transmis
          sionReceived`. In each mode the guards are exclusive and cover 
          every case, ordered by SB-FR8. Emergency stop is terminal.
      16  - Analysis and FR1–FR5 are in `BoilerSensor`, the outbox and FR
          6/FR7 messages in `BoilerActuator`, and the cycle driver is `St
          eamBoilerProgram`.
     ...
      27  
      28  ## Unresolved
      29  
      30 -None known. `on` missing from the rules' reserved-word list may
         - be a gap in the instrument's documentation; I worked around it
         - in the Java.                                                  
      30 +The MODE payload enum is dropped by the extraction (`modeMessag
         +e` is untyped in the CSP), so the model does not distinguish th
         +e mode values sent. I have not worked around it.               

⏺ Bash(wc -w /Users/ranwei/Gitee/forge-v3r10-run1-steam_boiler/forge.assets/corr
      ections/ITER_SUMMARY.md && cd
      /Users/ranwei/Gitee/forge-v3r10-run1-steam_boiler && expo…)
  ⎿  Running in the background (↓ to manage)

⏺ Iteration 2 didn't converge. 10 of 12 phases passed, including preflight and
  M2T, which iteration 2 fixed; FDR4 and Isabelle still failed. I've changed the
  Java for both, and iteration 3 is now running in the background.

  FDR4: the generated CSP had a type error. The pipeline turns each call on the
  actuator into an output event named after the method and drops the argument.
  An int parameter still made the event typed, so regulatePumps(n) became a
  number-typed event sent with no number. The controller now uses commands with
  no arguments: regulatePumps() (the actuator reads the pump count from the
  sensor), runFillingPump(), stopPumps(), openValve() and closeValve(). As a
  bonus, opening and closing the valve are now separate events in the model.

  Isabelle: a predicate used only inside another predicate was typed as real
  instead of boolean. This time waitingSignalNow was the case, and
  steamUnitFailed and pumpControlUnitFailed were about to hit it too. I removed
  all compound predicates, so every guard is built in the if conditions from
  single-call predicates, each used by name in some branch. The SBW/PUR "start
  message out of place" check is now one sensor query, startMessageReceived().

  Checks: behaviour is unchanged; all the earlier simulation scenarios give the
  same results. The trace file is regenerated (238 entries, nothing untraced),
  and post_codegen and ITER_SUMMARY.md are updated.

  Known gap: the pipeline also drops the mode value carried by the MODE message,
  so the model can't tell which mode was sent. That doesn't make any phase
  fail, so I've only recorded it in the summary.

  ITER_3_COMPLETE: 7 files edited

❯ still running?

⏺ Bash(tail -15 /private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3r10-run1-ste
      am-boiler/cd66d3b8-45a4-4d0a-9a36-7fdea2f69068/tasks/bn4xvbjsz.output;
      echo "---"; ps a…)
  ⎿  (No output)

⏺ Iteration 3 is still running in the background, and I noticed ITER_SUMMARY.md
  has crept over the 300-word limit (324 words), so I'll trim it now before the
  runner's record step snapshots it.

⏺ Write(ITER_SUMMARY.md)
  ⎿  Added 10 lines, removed 11 lines
       2  
       3  ## Iteration 2 result
       4  
       5 -Passed: compile, coverage, preflight, t2m, m2m, m2t, dafny_gen,
         - isabelle_gen, dafny_verify, vacuity. Failed: fdr4, isabelle_ve
         -rify.                                                          
       5 +Failed: fdr4, isabelle_verify. All other phases passed.        
       6  
       7 -## Changes for iteration 3 (Java and trace)                    
       7 +## Changes for iteration 3                                     
       8  
       9 -- FDR4 (CSP type error at `regulatePumps.out`): an actuator cal
         -l becomes an output event named after the method, and its argum
         -ent is dropped. An `int` parameter still gives the event a type
         -, so `regulatePumps(n)` became an `Int` event emitted with no v
         -alue. The controller now uses argument-free commands: `regulate
         -Pumps()` (the count comes from the sensor), `runFillingPump()`,
         - `stopPumps()`, `openValve()`, `closeValve()`. Open and close a
         -re now separate events in the model.                           
      10 -- Isabelle (`waitingSignalNow () :: ℝ`): a predicate referenced
         - only inside another predicate was given type `ℝ`. The compound
         - predicates are removed. Every guard is built in the `if` condi
         -tions from single-call predicates, each used by name in some br
         -anch. SBW/PUR outside initialization is one sensor query, `star
         -tMessageReceived()`.                                           
      11 -- Behaviour is unchanged (checked with a scratch simulation). T
         -he trace and `post_codegen` are refreshed for the renamed actua
         -tor methods.                                                   
       9 +- FDR4 (CSP type error at `regulatePumps.out`): an actuator cal
         +l becomes an event named after the method, and its argument is 
         +dropped. An `int` parameter still typed the event, so `regulate
         +Pumps(n)` had a type but no value. The controller now uses argu
         +ment-free commands: `regulatePumps()` (count read from the sens
         +or), `runFillingPump()`, `stopPumps()`, `openValve()`, `closeVa
         +lve()`.                                                        
      10 +- Isabelle (`waitingSignalNow () :: ℝ`): a predicate referenced
         + only inside another predicate was typed `ℝ`. The compound pred
         +icates are removed. Every guard is built in the `if` conditions
         + from single-call predicates, each used by name in some branch.
         + SBW/PUR outside initialization is now one sensor query, `start
         +MessageReceived()`.                                            
      11 +- Behaviour is unchanged (checked in a scratch simulation). The
         + trace and `post_codegen` are refreshed.                       
      12  
      13 -## Design (unchanged)                                          
      13 +## Design                                                      
      14  
      15 -- One state machine. Every transition is triggered by `Transmis
         -sionReceived`. In each mode the guards are exclusive and cover 
         -every case, ordered by SB-FR8. Emergency stop is terminal.     
      16 -- Analysis and FR1–FR5 are in `BoilerSensor`, the outbox and FR
         -6/FR7 messages in `BoilerActuator`, and the cycle driver is `St
         -eamBoilerProgram`.                                             
      15 +There is one state machine, and every transition is triggered b
         +y `TransmissionReceived`. In each mode the guards are exclusive
         + and cover every case, ordered by SB-FR8. Emergency stop is ter
         +minal. Analysis and FR1–FR5 are in `BoilerSensor`, the outbox a
         +nd FR6/FR7 in `BoilerActuator`, and the cycle driver is `SteamB
         +oilerProgram`.                                                 
      16  
      17  ## Decisions the requirements left open
      18  
      20 -- The constant values are invented (C=1000, M1=150, N1=400, N2=
         -600, M2=800, W=25, U1=U2=2, P=10).                             
      19 +- Invented constants (C=1000, M1=150, N1=400, N2=600, M2=800, W
         +=25, U1=U2=2, P=10).                                           
      20  - Before STEAM_BOILER_WAITING only STOP counts.
      22 -- Aberrant messages are defined in `post_codegen.md`.          
      21 +- Aberrant messages are listed in `post_codegen.md`.           
      22  - "Pump-controller units fail" means any controller.
      23  - A level failure in normal or degraded goes to rescue first (F
          R8, literal reading).
      24  - Risk and regulation are worst-case and predictive over two cy
          cles.
     ...
      26  
      27  ## Unresolved
      28  
      30 -The MODE payload enum is dropped by the extraction (`modeMessag
         -e` is untyped in the CSP), so the model does not distinguish th
         -e mode values sent. I have not worked around it.               
      29 +The extraction drops the MODE payload (`modeMessage` is untyped
         +), so the model does not distinguish mode values.              

  Read 1 file (ctrl+o to expand)

⏺ Yes, iteration 3 is still running. It started about when I posted the
  iteration 3 update; my iteration 1 and 2 runs took several minutes each,
  mostly in the Isabelle step. I'll be notified when it finishes.

  Checking on it, I saw that the runner had printed a 324-word count for
  ITER_SUMMARY.md, which breaks the 300-word limit. I've cut it to 275 words
  with no change to the content. The runner should read the summary only when it
  records the iteration at the end, so it will pick up the shorter version. I
  didn't check the word count against the limit for iteration 2; that summary
  was 280 words, within the limit.

⏺ That's the fallback wake-up I scheduled during iteration 1; that run finished
  long ago, and I've already handled iterations 1 and 2. Checking the iteration
  3 run (task bn4xvbjsz):

⏺ Bash(ps aux | grep -c '[r]un_claude_iteration'; tail -40
      /private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3r10-run1-steam-boiler/
      cd66d3b8-45a4-4d0a-9a36-7fdea2f690…)
  ⎿  (No output)

⏺ Iteration 3 is still running, likely slower since FDR4 is now actually
  model-checking instead of failing at parsing. I'll wait for the completion
  notification, with this fallback check guarding against a hang.

⏺ This wake-up was scheduled during iteration 1, which finished long ago. The
  loop is now on iteration 3, and that run is still going.

  It's probably taking longer because FDR4 is now checking the model for real;
  in iterations 1 and 2 it stopped at a parse error. I'll read the failing phase
  reports or record convergence when the runner finishes. I've set another
  fallback wake-up for 12:56 in case it hangs.

⏺ Background command "Run pipeline iteration 3 through the runner" completed
(exit code 0)

  Read 1 file (ctrl+o to expand)

⏺ Iteration 3 still fails FDR4 (961 s) and Isabelle (597 s), but both now ran
  for real instead of stopping at parse or type errors. Reading the reports:

⏺ Bash(cd
      /Users/ranwei/Gitee/forge-v3r10-run1-steam_boiler/forge.assets/corrections
      && cat post_fdr4.md | head -150)
  ⎿  (No output)

⏺ The FDR4 "parse error" is actually a state-space blow-up—it was still
  compiling processes (104000+) when the 961s phase cut off, meaning the model
  needs to be much smaller. I'll check the Isabelle report next before deciding
  what to shrink.

  Read 1 file (ctrl+o to expand)

⏺ Both remaining failures are size-related rather than correctness
  issues—Isabelle's proof times out and FDR's compilation exceeds the limit
  given 14 boolean predicates, two real-valued comparisons, and 37 transitions.
  Before shrinking the model, I'll check docs/corpus for how Z-Machine deadlock
  freedom is typically stated, to see if a terminal emergency-stop state is
  handled there.

⏺ Bash(cd /Users/ranwei/Gitee/forge-v3r10-run1-steam_boiler/docs/corpus && sed
      -n '1,60p' README.md; grep -n -i 'deadlock'
      zmachine-framework/Z_Machine.thy | head -20)
  ⎿  (No output)

⏺ Bash(sed -n '60,125p' zmachine-framework/Z_Machine.thy)
  ⎿  (No output)

⏺ The timeout stems from FDR4/Isabelle struggling with 37 transitions over 14
  predicates plus real comparisons, so I'll shrink the model to ~20 transitions
  over 9 booleans by having the sensor combine judgements upfront and merging
  controller causes that lead to the same mode, while keeping transition targets
  unchanged. Starting with the sensor now.
⏺ Bash(cd /Users/ranwei/Gitee/forge-v3r10-run1-steam_boiler/java.generated.proje
      ct/src/main/java/steam_boiler/sensor && grep -n 'private boolean
      startMessage;\|startMe…)
  ⎿  (No output)

⏺ Bash(python3 -I - <<'EOF'
      def sub(path, pairs):…)
  ⎿  (No output)

⏺ Now the reduced controller:

⏺ Write(~/Gitee/forge-v3r10-run1-steam_boiler/java.generated.project/src/main/ja
       va/steam_boiler/controller/SteamBoilerController.java)
  ⎿  Added 42 lines, removed 119 lines
      1  package steam_boiler.controller;
      2  
      3 -import steam_boiler.BoilerConstants;                            
      3  import steam_boiler.actuator.BoilerActuator;
      4  import steam_boiler.event.InputEvent;
      5  import steam_boiler.event.ModeValue;
     ...
       34  
       35      /** Analysis stage of one cycle: decides the mode and the 
           messages to send. */
       36      public void step(InputEvent event) {
       38 -        boolean operatorStopRequested = sensor.stopRequestedTh
          -rice();                                                       
       39 -        boolean transmissionLost = sensor.transmissionFailure(
          -);                                                            
       40 -        boolean startMessageArrived = sensor.startMessageRecei
          -ved();                                                        
       37 +        boolean startupEmergency = sensor.initialEmergency(); 
       38          boolean plantWaiting = sensor.plantWaitingSignalled();
       42 -        boolean unitsReady = sensor.physicalUnitsReadyReceived
          -();                                                           
       43 -        boolean readyAnnounced = sensor.programReadyAnnounced(
          -);                                                            
       44 -        boolean steamNotZero = sensor.steamRate() != 0.0;     
       39 +        boolean unitsConfirmedReady = sensor.startConfirmed();
       40 +        boolean excessWater = sensor.drainNeeded();           
       41 +        boolean lowWater = sensor.fillNeeded();               
       42 +        boolean operatingEmergency = sensor.runningEmergency()
          +;                                                             
       43          boolean levelUnitFailed = sensor.levelDeviceFailed();
       46 -        boolean steamUnitFailed = sensor.steamDeviceFailed(); 
       47 -        boolean pumpControlUnitFailed = sensor.pumpControlFail
          -ed();                                                         
       44          boolean nonLevelUnitFailed = sensor.otherUnitFailed();
       49 -        boolean levelAboveN2 = sensor.waterLevel() > BoilerCon
          -stants.NORMAL_N2;                                             
       50 -        boolean levelBelowN1 = sensor.waterLevel() < BoilerCon
          -stants.NORMAL_N1;                                             
       51 -        boolean levelNearLimit = sensor.levelRisk();          
       45 +        boolean levelEstimateUnreliable = sensor.estimateUnrel
          +iable();                                                      
       46  
       47          if (currentMode == OperatingMode.INITIALIZATION) {
       54 -            if (event instanceof InputEvent.TransmissionReceiv
          -ed && operatorStopRequested) {                                
       48 +            if (event instanceof InputEvent.TransmissionReceiv
          +ed && startupEmergency) {                                     
       49                  currentMode = OperatingMode.EMERGENCY_STOP;
       50                  actuator.apply(new OutputEvent.ModeMessage(Mod
           eValue.EMERGENCY_STOP_MODE));
       57 -            } else if (event instanceof InputEvent.Transmissio
          -nReceived && !operatorStopRequested                           
       58 -                    && !plantWaiting) {                       
       59 -                currentMode = OperatingMode.INITIALIZATION;   
       60 -                actuator.apply(new OutputEvent.ModeMessage(Mod
          -eValue.INITIALIZATION_MODE));                                 
       61 -            } else if (event instanceof InputEvent.Transmissio
          -nReceived && !operatorStopRequested                           
       62 -                    && plantWaiting && transmissionLost) {    
       63 -                currentMode = OperatingMode.EMERGENCY_STOP;   
       64 -                actuator.apply(new OutputEvent.ModeMessage(Mod
          -eValue.EMERGENCY_STOP_MODE));                                 
       65 -            } else if (event instanceof InputEvent.Transmissio
          -nReceived && !operatorStopRequested                           
       66 -                    && plantWaiting && !transmissionLost && st
          -eamNotZero) {                                                 
       67 -                currentMode = OperatingMode.EMERGENCY_STOP;   
       68 -                actuator.apply(new OutputEvent.ModeMessage(Mod
          -eValue.EMERGENCY_STOP_MODE));                                 
       69 -            } else if (event instanceof InputEvent.Transmissio
          -nReceived && !operatorStopRequested                           
       70 -                    && plantWaiting && !transmissionLost && !s
          -teamNotZero && levelUnitFailed) {                             
       71 -                currentMode = OperatingMode.EMERGENCY_STOP;   
       72 -                actuator.apply(new OutputEvent.ModeMessage(Mod
          -eValue.EMERGENCY_STOP_MODE));                                 
       73 -            } else if (event instanceof InputEvent.Transmissio
          -nReceived && !operatorStopRequested                           
       74 -                    && plantWaiting && !transmissionLost && !s
          -teamNotZero && !levelUnitFailed                               
       75 -                    && unitsReady && !nonLevelUnitFailed) {   
       51 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && !startupEmergency                                
       52 +                    && unitsConfirmedReady && !nonLevelUnitFai
          +led) {                                                        
       53                  currentMode = OperatingMode.NORMAL;
       54                  actuator.apply(new OutputEvent.ModeMessage(Mod
           eValue.NORMAL_MODE));
       55                  actuator.closeValve();
       56                  actuator.regulatePumps();
       80 -            } else if (event instanceof InputEvent.Transmissio
          -nReceived && !operatorStopRequested                           
       81 -                    && plantWaiting && !transmissionLost && !s
          -teamNotZero && !levelUnitFailed                               
       82 -                    && unitsReady && nonLevelUnitFailed) {    
       57 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && !startupEmergency                                
       58 +                    && unitsConfirmedReady && nonLevelUnitFail
          +ed) {                                                         
       59                  currentMode = OperatingMode.DEGRADED;
       60                  actuator.apply(new OutputEvent.ModeMessage(Mod
           eValue.DEGRADED_MODE));
       61                  actuator.closeValve();
       62                  actuator.regulatePumps();
       87 -            } else if (event instanceof InputEvent.Transmissio
          -nReceived && !operatorStopRequested                           
       88 -                    && plantWaiting && !transmissionLost && !s
          -teamNotZero && !levelUnitFailed                               
       89 -                    && !unitsReady && readyAnnounced) {       
       63 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && !startupEmergency                                
       64 +                    && !unitsConfirmedReady && !plantWaiting) 
          +{                                                             
       65                  currentMode = OperatingMode.INITIALIZATION;
       66                  actuator.apply(new OutputEvent.ModeMessage(Mod
           eValue.INITIALIZATION_MODE));
       92 -                actuator.apply(new OutputEvent.ProgramReady())
          -;                                                             
       93 -            } else if (event instanceof InputEvent.Transmissio
          -nReceived && !operatorStopRequested                           
       94 -                    && plantWaiting && !transmissionLost && !s
          -teamNotZero && !levelUnitFailed                               
       95 -                    && !unitsReady && !readyAnnounced && level
          -AboveN2) {                                                    
       67 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && !startupEmergency                                
       68 +                    && !unitsConfirmedReady && plantWaiting &&
          + excessWater) {                                               
       69                  currentMode = OperatingMode.INITIALIZATION;
       70                  actuator.apply(new OutputEvent.ModeMessage(Mod
           eValue.INITIALIZATION_MODE));
       71                  actuator.openValve();
       72                  actuator.stopPumps();
      100 -            } else if (event instanceof InputEvent.Transmissio
          -nReceived && !operatorStopRequested                           
      101 -                    && plantWaiting && !transmissionLost && !s
          -teamNotZero && !levelUnitFailed                               
      102 -                    && !unitsReady && !readyAnnounced && !leve
          -lAboveN2 && levelBelowN1) {                                   
       73 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && !startupEmergency                                
       74 +                    && !unitsConfirmedReady && plantWaiting &&
          + !excessWater && lowWater) {                                  
       75                  currentMode = OperatingMode.INITIALIZATION;
       76                  actuator.apply(new OutputEvent.ModeMessage(Mod
           eValue.INITIALIZATION_MODE));
       77                  actuator.closeValve();
       78                  actuator.runFillingPump();
      107 -            } else if (event instanceof InputEvent.Transmissio
          -nReceived && !operatorStopRequested                           
      108 -                    && plantWaiting && !transmissionLost && !s
          -teamNotZero && !levelUnitFailed                               
      109 -                    && !unitsReady && !readyAnnounced && !leve
          -lAboveN2 && !levelBelowN1) {                                  
       79 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && !startupEmergency                                
       80 +                    && !unitsConfirmedReady && plantWaiting &&
          + !excessWater && !lowWater) {                                 
       81                  currentMode = OperatingMode.INITIALIZATION;
       82                  actuator.apply(new OutputEvent.ModeMessage(Mod
           eValue.INITIALIZATION_MODE));
       83                  actuator.closeValve();
     ...
       86              }
       87  
       88          } else if (currentMode == OperatingMode.NORMAL) {
      118 -            if (event instanceof InputEvent.TransmissionReceiv
          -ed && operatorStopRequested) {                                
       89 +            if (event instanceof InputEvent.TransmissionReceiv
          +ed && operatingEmergency) {                                   
       90                  currentMode = OperatingMode.EMERGENCY_STOP;
       91                  actuator.apply(new OutputEvent.ModeMessage(Mod
           eValue.EMERGENCY_STOP_MODE));
      121 -            } else if (event instanceof InputEvent.Transmissio
          -nReceived && !operatorStopRequested                           
      122 -                    && transmissionLost) {                    
      123 -                currentMode = OperatingMode.EMERGENCY_STOP;   
      124 -                actuator.apply(new OutputEvent.ModeMessage(Mod
          -eValue.EMERGENCY_STOP_MODE));                                 
      125 -            } else if (event instanceof InputEvent.Transmissio
          -nReceived && !operatorStopRequested                           
      126 -                    && !transmissionLost && startMessageArrive
          -d) {                                                          
      127 -                currentMode = OperatingMode.EMERGENCY_STOP;   
      128 -                actuator.apply(new OutputEvent.ModeMessage(Mod
          -eValue.EMERGENCY_STOP_MODE));                                 
      129 -            } else if (event instanceof InputEvent.Transmissio
          -nReceived && !operatorStopRequested                           
      130 -                    && !transmissionLost && !startMessageArriv
          -ed && levelNearLimit) {                                       
      131 -                currentMode = OperatingMode.EMERGENCY_STOP;   
      132 -                actuator.apply(new OutputEvent.ModeMessage(Mod
          -eValue.EMERGENCY_STOP_MODE));                                 
      133 -            } else if (event instanceof InputEvent.Transmissio
          -nReceived && !operatorStopRequested                           
      134 -                    && !transmissionLost && !startMessageArriv
          -ed && !levelNearLimit && levelUnitFailed) {                   
       92 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && !operatingEmergency                              
       93 +                    && levelUnitFailed) {                     
       94                  currentMode = OperatingMode.RESCUE;
       95                  actuator.apply(new OutputEvent.ModeMessage(Mod
           eValue.RESCUE_MODE));
       96                  actuator.regulatePumps();
      138 -            } else if (event instanceof InputEvent.Transmissio
          -nReceived && !operatorStopRequested                           
      139 -                    && !transmissionLost && !startMessageArriv
          -ed && !levelNearLimit && !levelUnitFailed                     
      140 -                    && nonLevelUnitFailed) {                  
       97 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && !operatingEmergency                              
       98 +                    && !levelUnitFailed && nonLevelUnitFailed)
          + {                                                            
       99                  currentMode = OperatingMode.DEGRADED;
      100                  actuator.apply(new OutputEvent.ModeMessage(Mod
           eValue.DEGRADED_MODE));
      101                  actuator.regulatePumps();
      144 -            } else if (event instanceof InputEvent.Transmissio
          -nReceived && !operatorStopRequested                           
      145 -                    && !transmissionLost && !startMessageArriv
          -ed && !levelNearLimit && !levelUnitFailed                     
      146 -                    && !nonLevelUnitFailed) {                 
      102 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && !operatingEmergency                              
      103 +                    && !levelUnitFailed && !nonLevelUnitFailed
          +) {                                                           
      104                  currentMode = OperatingMode.NORMAL;
      105                  actuator.apply(new OutputEvent.ModeMessage(Mod
           eValue.NORMAL_MODE));
      106                  actuator.regulatePumps();
      107              }
      108  
      109          } else if (currentMode == OperatingMode.DEGRADED) {
      153 -            if (event instanceof InputEvent.TransmissionReceiv
          -ed && operatorStopRequested) {                                
      110 +            if (event instanceof InputEvent.TransmissionReceiv
          +ed && operatingEmergency) {                                   
      111                  currentMode = OperatingMode.EMERGENCY_STOP;
      112                  actuator.apply(new OutputEvent.ModeMessage(Mod
           eValue.EMERGENCY_STOP_MODE));
      156 -            } else if (event instanceof InputEvent.Transmissio
          -nReceived && !operatorStopRequested                           
      157 -                    && transmissionLost) {                    
      158 -                currentMode = OperatingMode.EMERGENCY_STOP;   
      159 -                actuator.apply(new OutputEvent.ModeMessage(Mod
          -eValue.EMERGENCY_STOP_MODE));                                 
      160 -            } else if (event instanceof InputEvent.Transmissio
          -nReceived && !operatorStopRequested                           
      161 -                    && !transmissionLost && startMessageArrive
          -d) {                                                          
      162 -                currentMode = OperatingMode.EMERGENCY_STOP;   
      163 -                actuator.apply(new OutputEvent.ModeMessage(Mod
          -eValue.EMERGENCY_STOP_MODE));                                 
      164 -            } else if (event instanceof InputEvent.Transmissio
          -nReceived && !operatorStopRequested                           
      165 -                    && !transmissionLost && !startMessageArriv
          -ed && levelNearLimit) {                                       
      166 -                currentMode = OperatingMode.EMERGENCY_STOP;   
      167 -                actuator.apply(new OutputEvent.ModeMessage(Mod
          -eValue.EMERGENCY_STOP_MODE));                                 
      168 -            } else if (event instanceof InputEvent.Transmissio
          -nReceived && !operatorStopRequested                           
      169 -                    && !transmissionLost && !startMessageArriv
          -ed && !levelNearLimit && levelUnitFailed) {                   
      113 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && !operatingEmergency                              
      114 +                    && levelUnitFailed) {                     
      115                  currentMode = OperatingMode.RESCUE;
      116                  actuator.apply(new OutputEvent.ModeMessage(Mod
           eValue.RESCUE_MODE));
      117                  actuator.regulatePumps();
      173 -            } else if (event instanceof InputEvent.Transmissio
          -nReceived && !operatorStopRequested                           
      174 -                    && !transmissionLost && !startMessageArriv
          -ed && !levelNearLimit && !levelUnitFailed                     
      175 -                    && !nonLevelUnitFailed) {                 
      118 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && !operatingEmergency                              
      119 +                    && !levelUnitFailed && !nonLevelUnitFailed
          +) {                                                           
      120                  currentMode = OperatingMode.NORMAL;
      121                  actuator.apply(new OutputEvent.ModeMessage(Mod
           eValue.NORMAL_MODE));
      122                  actuator.regulatePumps();
      179 -            } else if (event instanceof InputEvent.Transmissio
          -nReceived && !operatorStopRequested                           
      180 -                    && !transmissionLost && !startMessageArriv
          -ed && !levelNearLimit && !levelUnitFailed                     
      181 -                    && nonLevelUnitFailed) {                  
      123 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && !operatingEmergency                              
      124 +                    && !levelUnitFailed && nonLevelUnitFailed)
          + {                                                            
      125                  currentMode = OperatingMode.DEGRADED;
      126                  actuator.apply(new OutputEvent.ModeMessage(Mod
           eValue.DEGRADED_MODE));
      127                  actuator.regulatePumps();
      128              }
      129  
      130          } else if (currentMode == OperatingMode.RESCUE) {
      188 -            if (event instanceof InputEvent.TransmissionReceiv
          -ed && operatorStopRequested) {                                
      131 +            if (event instanceof InputEvent.TransmissionReceiv
          +ed && operatingEmergency) {                                   
      132                  currentMode = OperatingMode.EMERGENCY_STOP;
      133                  actuator.apply(new OutputEvent.ModeMessage(Mod
           eValue.EMERGENCY_STOP_MODE));
      191 -            } else if (event instanceof InputEvent.Transmissio
          -nReceived && !operatorStopRequested                           
      192 -                    && transmissionLost) {                    
      134 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && !operatingEmergency                              
      135 +                    && levelEstimateUnreliable) {             
      136                  currentMode = OperatingMode.EMERGENCY_STOP;
      137                  actuator.apply(new OutputEvent.ModeMessage(Mod
           eValue.EMERGENCY_STOP_MODE));
      195 -            } else if (event instanceof InputEvent.Transmissio
          -nReceived && !operatorStopRequested                           
      196 -                    && !transmissionLost && startMessageArrive
          -d) {                                                          
      197 -                currentMode = OperatingMode.EMERGENCY_STOP;   
      198 -                actuator.apply(new OutputEvent.ModeMessage(Mod
          -eValue.EMERGENCY_STOP_MODE));                                 
      199 -            } else if (event instanceof InputEvent.Transmissio
          -nReceived && !operatorStopRequested                           
      200 -                    && !transmissionLost && !startMessageArriv
          -ed && levelNearLimit) {                                       
      201 -                currentMode = OperatingMode.EMERGENCY_STOP;   
      202 -                actuator.apply(new OutputEvent.ModeMessage(Mod
          -eValue.EMERGENCY_STOP_MODE));                                 
      203 -            } else if (event instanceof InputEvent.Transmissio
          -nReceived && !operatorStopRequested                           
      204 -                    && !transmissionLost && !startMessageArriv
          -ed && !levelNearLimit && steamUnitFailed) {                   
      205 -                currentMode = OperatingMode.EMERGENCY_STOP;   
      206 -                actuator.apply(new OutputEvent.ModeMessage(Mod
          -eValue.EMERGENCY_STOP_MODE));                                 
      207 -            } else if (event instanceof InputEvent.Transmissio
          -nReceived && !operatorStopRequested                           
      208 -                    && !transmissionLost && !startMessageArriv
          -ed && !levelNearLimit && !steamUnitFailed                     
      209 -                    && pumpControlUnitFailed) {               
      210 -                currentMode = OperatingMode.EMERGENCY_STOP;   
      211 -                actuator.apply(new OutputEvent.ModeMessage(Mod
          -eValue.EMERGENCY_STOP_MODE));                                 
      212 -            } else if (event instanceof InputEvent.Transmissio
          -nReceived && !operatorStopRequested                           
      213 -                    && !transmissionLost && !startMessageArriv
          -ed && !levelNearLimit && !steamUnitFailed                     
      214 -                    && !pumpControlUnitFailed && levelUnitFail
          -ed) {                                                         
      138 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && !operatingEmergency                              
      139 +                    && !levelEstimateUnreliable && levelUnitFa
          +iled) {                                                       
      140                  currentMode = OperatingMode.RESCUE;
      141                  actuator.apply(new OutputEvent.ModeMessage(Mod
           eValue.RESCUE_MODE));
      142                  actuator.regulatePumps();
      218 -            } else if (event instanceof InputEvent.Transmissio
          -nReceived && !operatorStopRequested                           
      219 -                    && !transmissionLost && !startMessageArriv
          -ed && !levelNearLimit && !steamUnitFailed                     
      220 -                    && !pumpControlUnitFailed && !levelUnitFai
          -led && !nonLevelUnitFailed) {                                 
      143 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && !operatingEmergency                              
      144 +                    && !levelEstimateUnreliable && !levelUnitF
          +ailed && !nonLevelUnitFailed) {                               
      145                  currentMode = OperatingMode.NORMAL;
      146                  actuator.apply(new OutputEvent.ModeMessage(Mod
           eValue.NORMAL_MODE));
      147                  actuator.regulatePumps();
      224 -            } else if (event instanceof InputEvent.Transmissio
          -nReceived && !operatorStopRequested                           
      225 -                    && !transmissionLost && !startMessageArriv
          -ed && !levelNearLimit && !steamUnitFailed                     
      226 -                    && !pumpControlUnitFailed && !levelUnitFai
          -led && nonLevelUnitFailed) {                                  
      148 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && !operatingEmergency                              
      149 +                    && !levelEstimateUnreliable && !levelUnitF
          +ailed && nonLevelUnitFailed) {                                
      150                  currentMode = OperatingMode.DEGRADED;
      151                  actuator.apply(new OutputEvent.ModeMessage(Mod
           eValue.DEGRADED_MODE));
      152                  actuator.regulatePumps();

⏺ 9 predicates, 20 transitions, and in each mode the guards are exclusive and
  cover every case. Compiling and re-running the scenarios:

⏺ Bash(SP=/private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3r10-run1-steam-boi
      ler/cd66d3b8-45a4-4d0a-9a36-7fdea2f69068/scratchpad && cd
      /Users/ranwei/Gitee/forge-v3r…)
  ⎿  (No output)

⏺ Same behavior as before. I'll now test a few more edge cases: non-zero steam
  at start, a bad level reading during INIT, PUR before PROGRAM_READY, STOP
  before SBW, and a steam failure while in RESCUE.

⏺ Bash(SP=/private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3r10-run1-steam-boi
      ler/cd66d3b8-45a4-4d0a-9a36-7fdea2f69068/scratchpad && cat >
      $SP/sim/Edge.java <<'EOF'…)
  ⎿  (No output)

⏺ All edge paths behave as required. Updating the trace mapping (drop
  startMessageReceived, add the six combined queries) and validating it:

⏺ Bash(SP=/private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3r10-run1-steam-boi
      ler/cd66d3b8-45a4-4d0a-9a36-7fdea2f69068/scratchpad && python3 -I -
      "$SP/gen_trace.py"…)
  ⎿  (No output)

⏺ The trace checks out at 251 entries. Next I'll run post_codegen, updating
  controller line numbers and adding a design-choice entry noting that ES causes
  are combined into single sensor judgements to keep the model tractable.

⏺ Bash(cd /Users/ranwei/Gitee/forge-v3r10-run1-steam_boiler/java.generated.proje
      ct/src/main/java/steam_boiler && grep -n 'currentMode ==
      OperatingMode\|public void ste…)
  ⎿  (No output)

⏺ Bash(SP=/private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3r10-run1-steam-boi
      ler/cd66d3b8-45a4-4d0a-9a36-7fdea2f69068/scratchpad && python3 -I -
      "$SP/gen_post_codeg…)
  ⎿  (No output)

⏺ Now the iteration summary, kept under 300 words:

⏺ Write(ITER_SUMMARY.md)
  ⎿  Added 15 lines, removed 10 lines
       1 -# Iteration 3 — steam_boiler                                   
       1 +# Iteration 4 — steam_boiler                                   
       2  
       3 -## Iteration 2 result                                          
       3 +## Iteration 3 result                                          
       4  
       5 -Failed: fdr4, isabelle_verify. All other phases passed.        
       5 +Failed: fdr4, isabelle_verify. All other phases passed. Both we
         +re timeouts, not type or parse errors:                         
       6  
       7 -## Changes for iteration 3                                     
       7 +- FDR4 was still compiling (104000+ processes) after 961 s. The
         + report labels this a "parse error".                           
       8 +- The Isabelle deadlock-freedom proof timed out.               
       9  
       9 -- FDR4 (CSP type error at `regulatePumps.out`): an actuator cal
         -l becomes an event named after the method, and its argument is 
         -dropped. An `int` parameter still typed the event, so `regulate
         -Pumps(n)` had a type but no value. The controller now uses argu
         -ment-free commands: `regulatePumps()` (count read from the sens
         -or), `runFillingPump()`, `stopPumps()`, `openValve()`, `closeVa
         -lve()`.                                                        
      10 -- Isabelle (`waitingSignalNow () :: ℝ`): a predicate referenced
         - only inside another predicate was typed `ℝ`. The compound pred
         -icates are removed. Every guard is built in the `if` conditions
         - from single-call predicates, each used by name in some branch.
         - SBW/PUR outside initialization is now one sensor query, `start
         -MessageReceived()`.                                            
      11 -- Behaviour is unchanged (checked in a scratch simulation). The
         - trace and `post_codegen` are refreshed.                       
      10 +The model was too large: 37 transitions, 14 boolean predicates 
         +and real-valued comparisons.                                   
      11  
      13 -## Design                                                      
      12 +## Changes for iteration 4                                     
      13  
      15 -There is one state machine, and every transition is triggered b
         -y `TransmissionReceived`. In each mode the guards are exclusive
         - and cover every case, ordered by SB-FR8. Emergency stop is ter
         -minal. Analysis and FR1–FR5 are in `BoilerSensor`, the outbox a
         -nd FR6/FR7 in `BoilerActuator`, and the cycle driver is `SteamB
         -oilerProgram`.                                                 
      14 +- `BoilerSensor` now combines the causes that lead to the same 
         +mode change into single judgements:                            
      15 +  - `initialEmergency`: STOP x3, or after STEAM_BOILER_WAITING 
         +a transmission failure, non-zero steam or a failed level device
         +;                                                              
      16 +  - `runningEmergency`: STOP x3, a transmission failure (includ
         +ing start messages outside initialization) or level risk;      
      17 +  - `estimateUnreliable`: the steam device or a pump controller
         + out of order;                                                 
      18 +  - also `startConfirmed`, `drainNeeded`, `fillNeeded`.        
      19 +- `SteamBoilerController` now has 20 transitions over 9 boolean
         + predicates, with no real-valued comparisons. Per mode the guar
         +ds are exclusive and cover every case, ordered by SB-FR8. Each 
         +predicate is used by name in a branch condition.               
      20 +- Behaviour is unchanged. I checked it with the scratch simulat
         +ion and edge cases: steam ≠ 0 at start, bad level in initializa
         +tion, PUR before PROGRAM_READY, STOP before STEAM_BOILER_WAITIN
         +G, steam failure in rescue, and SBW in normal mode. The trace a
         +nd `post_codegen` are refreshed.                               
      21  
      22  ## Decisions the requirements left open
      23  
     ...
      31  
      32  ## Unresolved
      33  
      29 -The extraction drops the MODE payload (`modeMessage` is untyped
         -), so the model does not distinguish mode values.              
      34 +The extraction drops the MODE payload, so the model does not di
         +stinguish mode values. Merged causes share one transition in th
         +e model.                                                       

  Read 1 file (ctrl+o to expand)

⏺ Bash(cd
      /Users/ranwei/Gitee/forge-v3r10-run1-steam_boiler/forge.assets/corrections
      && python3 -I - <<'EOF'
      p = "ITER_SUMMARY.md"; s = open(p).read()…)
  ⎿  (No output)

⏺ 290 words. Running iteration 4:

⏺ Bash(cd /Users/ranwei/Gitee/forge-v3r10-run1-steam_boiler && export
      FORGE_ARM_DIR=/Users/ranwei/Gitee/formal_method_guided_vibe_coding/experim
      ents/v3-reference && ex…)
  ⎿  Running in the background (↓ to manage)

⏺ Iteration 3 didn't converge: FDR4 and Isabelle still failed. Both ran for real
  this time, so the earlier type and parse errors are gone. Both ran out of
  time because the model was too big.

  - FDR4: the report calls it a "CSP-M parse error", but the raw output shows
    FDR still building the model (104,000+ processes) when the phase ended after
    961 s. That's a size problem, not a parse problem.
  - Isabelle: the proof that the controller can never get stuck timed out.
    Reading Z_Machine.thy in docs/corpus (which I'm allowed to read) showed that
    a terminal emergency-stop state is excluded from that check. So the cause
    is the number and length of the guards, not the terminal state.

  Changes for iteration 4: where several causes lead to the same mode change,
  the sensor now combines them into one yes/no answer:
  - initialEmergency: three STOPs, or, once STEAM_BOILER_WAITING has arrived, a
    transmission failure, non-zero steam or a failed level device.
  - runningEmergency: three STOPs, a transmission failure (including start
    messages outside initialization), or a risk of reaching M1/M2.
  - estimateUnreliable: the steam device or a pump controller is out of order.
  - Three for initialization: startConfirmed, drainNeeded, fillNeeded.

  The controller now has 20 transitions over 9 yes/no predicates, down from 37
  transitions over 14 predicates. It no longer compares real-valued readings.

  Trade-off: each requirement still leads to its own target mode, but causes
  with the same target now share one transition in the model. For example, the
  three initialization emergency-stop causes are now one transition.

  The program behaves exactly as before. All the earlier simulation scenarios
  match, and the new edge cases all stop or wait correctly:
  - non-zero steam at start;
  - a bad level reading during initialization;
  - PHYSICAL_UNITS_READY before PROGRAM_READY;
  - three STOPs before STEAM_BOILER_WAITING;
  - a steam failure while in RESCUE;
  - STEAM_BOILER_WAITING while in NORMAL.

  The trace file (251 entries), post_codegen (new item on the combined
  judgements) and ITER_SUMMARY.md (290 words) are updated. Iteration 4 is now
  running in the background; three iterations remain before the cap of 7.

  ITER_4_COMPLETE: 6 files edited

⏺ Background command "Run pipeline iteration 4 through the runner" completed
(exit code 0)

  Read 1 file (ctrl+o to expand)

⏺ The runner printed ALL PHASES PASSED: the run converged at iteration 4.
  Cancelling the fallback wake-up, since there is nothing left to wait for.

⏺ The run converged at iteration 4: all 12 phases passed, and the runner printed
  ALL PHASES PASSED.

  ┌────────────────────────────────┬────────────────────────────────────────┐
  │             Phase              │              Iteration 4               │
  ├────────────────────────────────┼────────────────────────────────────────┤
  │ compile, coverage, preflight   │ passed                                 │
  ├────────────────────────────────┼────────────────────────────────────────┤
  │ t2m, m2m, m2t, dafny_gen,      │ passed                                 │
  │ isabelle_gen                   │                                        │
  ├────────────────────────────────┼────────────────────────────────────────┤
  │ fdr4                           │ passed (19 s; it timed out at 961 s in │
  │                                │  iteration 3)                          │
  ├────────────────────────────────┼────────────────────────────────────────┤
  │ dafny_verify                   │ passed                                 │
  ├────────────────────────────────┼────────────────────────────────────────┤
  │ isabelle_verify                │ passed (203 s): 53 lemmas, deadlock    │
  │                                │ freedom proved                         │
  ├────────────────────────────────┼────────────────────────────────────────┤
  │ vacuity                        │ passed (0 blocking, 1 advisory)        │
  └────────────────────────────────┴────────────────────────────────────────┘

  What each iteration fixed:
  1. → 2: the preflight check wanted the real type annotation on helper
     parameters. The field name on is a keyword in RoboChart's text format, but
     the rules' reserved-word list doesn't include it. A boolean sensor call
     inside a compound condition was typed as a real number in the Isabelle
     theory.
  2. → 3: calls on the actuator lose their argument when turned into model
     events, so the actuator commands now take no arguments. Any condition used
     only inside another condition was also typed as real, so compound
     conditions were removed.
  3. → 4: FDR4 and the Isabelle proof both timed out on model size. Causes that
     lead to the same mode change are now combined into one sensor answer, which
     brought the controller down to 20 transitions over 9 yes/no conditions.

  Where the requirements left room:
  - Invented values: the constants (C=1000, M1=150, N1=400, N2=600, M2=800,
    W=25, U1=U2=2, P=10) are my own choice; the requirements give none.
  - Not in the verified model: pump selection, failure announcements and repair
    acknowledgements live in the sensor and actuator classes, so the pipeline
    checks none of them. I tested them with a scratch simulation instead.
  - Combined transitions: each requirement still leads to its own target mode,
    but causes with the same target share one transition in the model.
  - Mode value dropped: the pipeline drops the mode value carried by MODE, so
    the model can't tell which mode was sent.
  - Rules list: on missing from the reserved-word list may be a gap in the
    instrument's documentation; I worked around it in the Java.

  No Java was present at the start, so nothing was deleted. I changed nothing in
  the instrument and didn't commit. I had no token figure, so every run was
  recorded as unavailable.

  Files are in java.generated.project and forge.assets/corrections:
  - src/main/java/steam_boiler/ (15 files)
  - result_codegen.json
  - post_codegen.md / post_codegen.json
  - ITER_SUMMARY.md