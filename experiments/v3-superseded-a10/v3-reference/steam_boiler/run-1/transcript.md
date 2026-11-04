
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
    `/Users/ranwei/Gitee/v3r9-staging/`, and the git history (`git log`, `git
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
  /Users/ranwei/Gitee/forge-v3r9-run1-steam_boiler
  ```

  `cd` there first. If it does not exist, **stop and say so**; no other
  worktree is a
  substitute. Export these four variables in your shell before running the
  runner:

  ```
  export FORGE_ARM_DIR=/Users/ranwei/Gitee/formal_method_guided_vibe_coding/exp
  eriments/v3-reference
  export FORGE_WT_PREFIX=forge-v3r9
  export FORGE_STAGE_DIR=/Users/ranwei/Gitee/v3r9-staging
  export FORGE_SHA=56cb43d7835b43e8a986e6ba4c0e82f35083d6f1
  ```

  Run this check before you write any Java, and paste its output. If any line
  reads
  FAIL, stop.

  ```
  cd /Users/ranwei/Gitee/forge-v3r9-run1-steam_boiler 2>/dev/null || { echo
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

⏺ Starting with the pre-flight check in the worktree.

⏺ Bash(export FORGE_ARM_DIR=/Users/ranwei/Gitee/formal_method_guided_vibe_coding
      /experiments/v3-reference
      export FORGE_WT_PREFIX=forge-v3r9…)
  ⎿  (file diff unavailable for this command)

⏺ All checks pass. Now reading the specified inputs.

  Read 7 files, listed 1 directory (ctrl+o to expand)

⏺ Now the five layer prompts.

  Read 5 files (ctrl+o to expand)

⏺ The study's README confirms there is no separate system description; the
  requirements are the whole input. Let me look at the Gradle scaffold and the
  corrections folder before designing.

⏺ Bash(cat java.generated.project/build.gradle
      java.generated.project/settings.gradle
      java.generated.project/gradle.properties && echo ---- && ls -la
      forge.assets/corr…)
  ⎿  (file diff unavailable for this command)

⏺ Pre-flight checks passed and the corrections folder is empty with no existing
  Java code to remove. I've designed the Layer 1 architecture (controller,
  sensor, actuator, event, and program classes with their constants) and am now
  moving to Layer 2: building the skeleton.

⏺ Bash(cd
      /Users/ranwei/Gitee/forge-v3r9-run1-steam_boiler/java.generated.project &&
      mkdir -p
      src/main/java/steam_boiler/{annotation,event,controller,sensor,actuator}…)
  ⎿  (No output)

⏺ Write(~/Gitee/forge-v3r9-run1-steam_boiler/java.generated.project/src/main/jav
       a/steam_boiler/annotation/RoboChartType.java)
  ⎿  Wrote 14 lines to ~/Gitee/forge-v3r9-run1-steam_boiler/java.generated.
     project/src/main/java/steam_boiler/annotation/RoboChartType.java
       1 package steam_boiler.annotation;
       2
       3 import java.lang.annotation.ElementType;
       4 import java.lang.annotation.Retention;
       5 import java.lang.annotation.RetentionPolicy;
       6 import java.lang.annotation.Target;
       7
       8 /** The RoboChart type a Java field, parameter or return value s
         tands for. */
       9 @Retention(RetentionPolicy.SOURCE)
     … +5 lines (ctrl+o to expand)

⏺ Write(~/Gitee/forge-v3r9-run1-steam_boiler/java.generated.project/src/main/jav
       a/steam_boiler/event/ModeValue.java)
  ⎿  Wrote 10 lines to ~/Gitee/forge-v3r9-run1-steam_boiler/java.generated.
     project/src/main/java/steam_boiler/event/ModeValue.java
       1 package steam_boiler.event;
       2
       3 /** The operating mode carried by the MODE(m) message: exactly f
         ive alternatives. */
       4 public enum ModeValue {
       5     MODE_INITIALIZATION,
       6     MODE_NORMAL,
       7     MODE_DEGRADED,
       8     MODE_RESCUE,
       9     MODE_EMERGENCY_STOP
     … +1 line (ctrl+o to expand)

⏺ Write(~/Gitee/forge-v3r9-run1-steam_boiler/java.generated.project/src/main/jav
       a/steam_boiler/event/WaterFlow.java)
  ⎿  Wrote 7 lines to ~/Gitee/forge-v3r9-run1-steam_boiler/java.generated.p
     roject/src/main/java/steam_boiler/event/WaterFlow.java
      1 package steam_boiler.event;
      2
      3 /** What a pump controller observes: water flows from its pump, o
        r it does not. */
      4 public enum WaterFlow {
      5     NOT_FLOWING,
      6     FLOWING
      7 }

⏺ Write(~/Gitee/forge-v3r9-run1-steam_boiler/java.generated.project/src/main/jav
       a/steam_boiler/event/PumpReport.java)
  ⎿  Wrote 12 lines to ~/Gitee/forge-v3r9-run1-steam_boiler/java.generated.
     project/src/main/java/steam_boiler/event/PumpReport.java
       1 package steam_boiler.event;
       2
       3 import steam_boiler.annotation.RoboChartType;
       4
       5 /**
       6  * Payload of PUMP_STATE(n, b): pump {@code reportedPump} (1 to
         4) is switched on or off.
       7  *
       8  * @param reportedPump the pump number
       9  * @param switchedOn whether the pump reports itself on
     … +3 lines (ctrl+o to expand)

⏺ Write(~/Gitee/forge-v3r9-run1-steam_boiler/java.generated.project/src/main/jav
       a/steam_boiler/event/PumpControlReport.java)
  ⎿  Wrote 12 lines to ~/Gitee/forge-v3r9-run1-steam_boiler/java.generated.
     project/src/main/java/steam_boiler/event/PumpControlReport.java
       1 package steam_boiler.event;
       2
       3 import steam_boiler.annotation.RoboChartType;
       4
       5 /**
       6  * Payload of PUMP_CONTROL_STATE(n, b): what the controller of p
         ump {@code controlledPump} sees.
       7  *
       8  * @param controlledPump the number of the pump the controller w
         atches
     … +4 lines (ctrl+o to expand)

⏺ Write(~/Gitee/forge-v3r9-run1-steam_boiler/java.generated.project/src/main/jav
       a/steam_boiler/event/InputEvent.java)
  ⎿  Wrote 59 lines to ~/Gitee/forge-v3r9-run1-steam_boiler/java.generated.
     project/src/main/java/steam_boiler/event/InputEvent.java
      1 package steam_boiler.event;
      2
      3 import steam_boiler.annotation.RoboChartType;
      4
      5 /**
      6  * Messages the program receives from the physical units, plus th
        e signal that one cycle's
      7  * transmission has been received in full. Pumps are numbered 1 t
        o 4.
      8  */
     … +51 lines (ctrl+o to expand)

⏺ Write(~/Gitee/forge-v3r9-run1-steam_boiler/java.generated.project/src/main/jav
       a/steam_boiler/event/OutputEvent.java)
  ⎿  Wrote 49 lines to ~/Gitee/forge-v3r9-run1-steam_boiler/java.generated.
     project/src/main/java/steam_boiler/event/OutputEvent.java
      1 package steam_boiler.event;
      2
      3 import steam_boiler.annotation.RoboChartType;
      4
      5 /** Messages the program sends to the physical units. Pumps are n
        umbered 1 to 4. */
      6 public sealed interface OutputEvent {
      7
      8     /** MODE(m): the program's current operating mode, sent on ev
        ery cycle. */
     … +41 lines (ctrl+o to expand)

⏺ Write(~/Gitee/forge-v3r9-run1-steam_boiler/java.generated.project/src/main/jav
       a/steam_boiler/controller/OperatingMode.java)
  ⎿  Wrote 10 lines to ~/Gitee/forge-v3r9-run1-steam_boiler/java.generated.
     project/src/main/java/steam_boiler/controller/OperatingMode.java
       1 package steam_boiler.controller;
       2
       3 /** The program's operating modes; it starts in initialization.
         */
       4 public enum OperatingMode {
       5     INITIALIZATION,
       6     NORMAL,
       7     DEGRADED,
       8     RESCUE,
       9     EMERGENCY_STOP
     … +1 line (ctrl+o to expand)

⏺ Write(~/Gitee/forge-v3r9-run1-steam_boiler/java.generated.project/src/main/jav
       a/steam_boiler/actuator/ValvePosition.java)
  ⎿  Wrote 7 lines to ~/Gitee/forge-v3r9-run1-steam_boiler/java.generated.p
     roject/src/main/java/steam_boiler/actuator/ValvePosition.java
      1 package steam_boiler.actuator;
      2
      3 /** Position of the boiler's evacuation valve, as last requested
        by the program. */
      4 public enum ValvePosition {
      5     VALVE_CLOSED,
      6     VALVE_OPEN
      7 }

⏺ Write(~/Gitee/forge-v3r9-run1-steam_boiler/java.generated.project/src/main/jav
       a/steam_boiler/SteamBoilerConstants.java)
  ⎿  Wrote 50 lines to ~/Gitee/forge-v3r9-run1-steam_boiler/java.generated.
     project/src/main/java/steam_boiler/SteamBoilerConstants.java
       1 package steam_boiler;
       2
       3 import steam_boiler.annotation.RoboChartType;
       4
       5 /**
       6  * Constants of the boiler, the steam exit and the pumps. The re
         quirements name these
       7  * quantities but give no values; the values below are chosen to
          respect
       8  * M1 &lt; N1 &lt; N2 &lt; M2 &lt; C and are whole numbers so th
     … +43 lines (~2 KB) (ctrl+o to expand)

⏺ Layer 2, continued: class skeletons with their public surfaces and empty
  bodies.

⏺ Write(~/Gitee/forge-v3r9-run1-steam_boiler/java.generated.project/src/main/jav
       a/steam_boiler/actuator/SteamBoilerActuator.java)
  ⎿  Wrote 33 lines to ~/Gitee/forge-v3r9-run1-steam_boiler/java.generated.
     project/src/main/java/steam_boiler/actuator/SteamBoilerActuator.java
      1 package steam_boiler.actuator;
      2
      3 import java.util.ArrayList;
      4 import java.util.List;
      5 import steam_boiler.event.ModeValue;
      6 import steam_boiler.event.OutputEvent;
      7
      8 /** Receives the program's outgoing messages for the current cycl
        e. */
      9 public final class SteamBoilerActuator {
     … +24 lines (ctrl+o to expand)

⏺ Write(~/Gitee/forge-v3r9-run1-steam_boiler/java.generated.project/src/main/jav
       a/steam_boiler/sensor/BoilerSensor.java)  ⎿  Wrote 141 lines to ~/Gitee/forge-v3r9-run1-steam_boiler/java.generated
     .project/src/main/java/steam_boiler/sensor/BoilerSensor.java
       1 package steam_boiler.sensor;
       2
       3 import java.util.ArrayList;
       4 import java.util.List;
       5 import steam_boiler.annotation.RoboChartType;
       6 import steam_boiler.event.InputEvent;
       7 import steam_boiler.event.OutputEvent;
       8
       9 /** The program's view of the physical units, rebuilt from each
         cycle's transmission. */
     … +132 lines (ctrl+o to expand)

⏺ Write(~/Gitee/forge-v3r9-run1-steam_boiler/java.generated.project/src/main/jav
       a/steam_boiler/controller/SteamBoilerController.java)
  ⎿  Wrote 33 lines to
     ~/Gitee/forge-v3r9-run1-steam_boiler/java.generated.project/src/main/j
     ava/steam_boiler/controller/SteamBoilerController.java
       1 package steam_boiler.controller;
       2
       3 import steam_boiler.actuator.SteamBoilerActuator;
       4 import steam_boiler.actuator.ValvePosition;
       5 import steam_boiler.annotation.RoboChartType;
       6 import steam_boiler.event.InputEvent;
       7 import steam_boiler.sensor.BoilerSensor;
       8
       9 /** The steam-boiler control program's operating-mode state mach
         ine. */
     … +24 lines (ctrl+o to expand)

⏺ Write(~/Gitee/forge-v3r9-run1-steam_boiler/java.generated.project/src/main/jav
       a/steam_boiler/SteamBoilerProgram.java)
  ⎿  Wrote 28 lines to ~/Gitee/forge-v3r9-run1-steam_boiler/java.generated.
     project/src/main/java/steam_boiler/SteamBoilerProgram.java
       1 package steam_boiler;
       2
       3 import java.util.ArrayList;
       4 import java.util.List;
       5 import steam_boiler.actuator.SteamBoilerActuator;
       6 import steam_boiler.controller.OperatingMode;
       7 import steam_boiler.controller.SteamBoilerController;
       8 import steam_boiler.event.InputEvent;
       9 import steam_boiler.event.OutputEvent;
      10 import steam_boiler.sensor.BoilerSensor;
     … +18 lines (ctrl+o to expand)

⏺ Bash(OUT=/private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3r9-run1-steam-boi
      ler/6ad4e450-4361-4ef7-b698-9a5dc45942fd/scratchpad/classes && mkdir -p
      $OUT && javac -…)
  ⎿  (No output)

⏺ The skeleton compiles. Layer 3 next: the leaf logic, starting with the
  per-unit bookkeeping.

⏺ Write(~/Gitee/forge-v3r9-run1-steam_boiler/java.generated.project/src/main/jav
       a/steam_boiler/sensor/UnitStatus.java)
  ⎿  Wrote 44 lines to ~/Gitee/forge-v3r9-run1-steam_boiler/java.generated.
     project/src/main/java/steam_boiler/sensor/UnitStatus.java
       1 package steam_boiler.sensor;
       2
       3 /**
       4  * What the program believes about one physical unit: sound or o
         ut of order, whether the
       5  * announced failure detection has been acknowledged, and whethe
         r a repair awaits its
       6  * acknowledgement.
       7  */
       8 final class UnitStatus {
     … +36 lines (ctrl+o to expand)

⏺ Write(~/Gitee/forge-v3r9-run1-steam_boiler/java.generated.project/src/main/jav
       a/steam_boiler/sensor/PumpMonitor.java)
  ⎿  Wrote 242 lines to ~/Gitee/forge-v3r9-run1-steam_boiler/java.generated
     .project/src/main/java/steam_boiler/sensor/PumpMonitor.java
       1 package steam_boiler.sensor;
       2
       3 import java.util.List;
       4 import steam_boiler.SteamBoilerConstants;
       5 import steam_boiler.annotation.RoboChartType;
       6 import steam_boiler.event.OutputEvent;
       7
       8 /**
       9  * What the program knows about one pump and its controller: the
          reports of the current
     … +233 lines (ctrl+o to expand)

⏺ Now the full BoilerSensor.

⏺ Write(~/Gitee/forge-v3r9-run1-steam_boiler/java.generated.project/src/main/jav
       a/steam_boiler/sensor/BoilerSensor.java)
  ⎿  Added 441 lines, removed 22 lines
        2  
        3  import java.util.ArrayList;
        4  import java.util.List;
        5 +import steam_boiler.SteamBoilerConstants;                     
        6  import steam_boiler.annotation.RoboChartType;
        7  import steam_boiler.event.InputEvent;
        8 +import steam_boiler.event.ModeValue;                          
        9  import steam_boiler.event.OutputEvent;
       10 +import steam_boiler.event.WaterFlow;                          
       11  
        9 -/** The program's view of the physical units, rebuilt from eac
          -h cycle's transmission. */                                    
       12 +/**                                                           
       13 + * The program's view of the physical units, rebuilt from each
          + cycle's transmission.                                        
       14 + *                                                            
       15 + * <p>Per cycle: {@link #startCycle} with the messages the pro
          +gram sent in the previous cycle,                              
       16 + * {@link #receive} for every incoming message, then {@link #a
          +nalyse}. The analysis judges the                              
       17 + * transmission, the pumps, the pump controllers and the two m
          +easuring devices, keeps each                                  
       18 + * unit's status, and advances the worst-case estimate of the 
          +water level. The controller reads                             
       19 + * the results through the query methods.                     
       20 + */                                                           
       21  public final class BoilerSensor {
       22  
       23 +    private final List<PumpMonitor> pumps = new ArrayList<>();
       24 +    private final UnitStatus levelDevice = new UnitStatus();  
       25 +    private final UnitStatus steamDevice = new UnitStatus();  
       26 +                                                              
       27 +    // What the program sent in the previous cycle.           
       28 +    private ModeValue lastModeSent = ModeValue.MODE_INITIALIZA
          +TION;                                                         
       29 +    private boolean programReadySent = false;                 
       30 +    private boolean valveOpen = false;                        
       31 +                                                              
       32 +    // Messages of the current transmission.                  
       33 +    @RoboChartType("nat") private int stopMessages = 0;       
       34 +    @RoboChartType("nat") private int waitingMessages = 0;    
       35 +    @RoboChartType("nat") private int unitsReadyMessages = 0; 
       36 +    @RoboChartType("nat") private int levelMessages = 0;      
       37 +    @RoboChartType("nat") private int steamMessages = 0;      
       38 +    @RoboChartType("real") private double levelValue = 0.0;   
       39 +    @RoboChartType("real") private double steamValue = 0.0;   
       40 +    private boolean levelRepairReported = false;              
       41 +    private boolean steamRepairReported = false;              
       42 +    private boolean levelAckReported = false;                 
       43 +    private boolean steamAckReported = false;                 
       44 +    private boolean aberrantMessage = false;                  
       45 +                                                              
       46 +    // Derived quantities read by the controller.             
       47 +    @RoboChartType("nat") private int successiveStopCount = 0;
       48 +    private boolean transmissionFailure = false;              
       49 +    private boolean steamBoilerWaitingReceived = false;       
       50 +    private boolean physicalUnitsReadyReceived = false;       
       51 +    @RoboChartType("real") private double levelReading = 0.0; 
       52 +    @RoboChartType("real") private double steamReading = 0.0; 
       53 +    @RoboChartType("real") private double pumpThroughput = 0.0
          +;                                                             
       54 +    @RoboChartType("real") private double levelEstimateLow = 0
          +.0;                                                           
       55 +    @RoboChartType("real") private double levelEstimateHigh = 
          +0.0;                                                          
       56 +    @RoboChartType("real") private double estimatedLevel = 0.0
          +;                                                             
       57 +    @RoboChartType("real") private double steamEstimateLow = 0
          +.0;                                                           
       58 +    @RoboChartType("real") private double steamEstimateHigh = 
          +0.0;                                                          
       59 +    private boolean levelHistory = false;                     
       60 +    private boolean steamHistory = false;                     
       61 +    @RoboChartType("real") private double minimumReachableLeve
          +l = 0.0;                                                      
       62 +    @RoboChartType("real") private double maximumReachableLeve
          +l = 0.0;                                                      
       63 +    private boolean levelDeviceOutOfOrder = false;            
       64 +    private boolean steamDeviceOutOfOrder = false;            
       65 +    private boolean pumpControllerOutOfOrder = false;         
       66 +    private boolean otherUnitOutOfOrder = false;              
       67 +    @RoboChartType("nat") private int openablePumpCount = 0;  
       68 +    @RoboChartType("nat") private int closablePumpCount = 0;  
       69 +    @RoboChartType("nat") private int pumpToOpen = 0;         
       70 +    @RoboChartType("nat") private int pumpToClose = 0;        
       71 +                                                              
       72 +    /** Creates the view of a plant with all units sound and n
          +othing received yet. */                                       
       73 +    public BoilerSensor() {                                   
       74 +        for (int number = 1; number <= SteamBoilerConstants.PU
          +MP_COUNT; number++) {                                         
       75 +            pumps.add(new PumpMonitor(number));               
       76 +        }                                                     
       77 +        refreshSummary();                                     
       78 +    }                                                         
       79 +                                                              
       80      /** Begins a cycle, given the messages the program sent in
            the previous one. */
       81      public void startCycle(List<OutputEvent> previouslySent) {
       82 +        for (PumpMonitor pump : pumps) {                      
       83 +            pump.startCycle();                                
       84 +        }                                                     
       85 +        programReadySent = false;                             
       86 +        for (OutputEvent message : previouslySent) {          
       87 +            if (message instanceof OutputEvent.Mode) {        
       88 +                OutputEvent.Mode modeMessage = (OutputEvent.Mo
          +de) message;                                                  
       89 +                lastModeSent = modeMessage.value();           
       90 +            } else if (message instanceof OutputEvent.ProgramR
          +eady) {                                                       
       91 +                programReadySent = true;                      
       92 +            } else if (message instanceof OutputEvent.Valve) {
       93 +                valveOpen = !valveOpen;                       
       94 +            } else if (message instanceof OutputEvent.OpenPump
          +) {                                                           
       95 +                OutputEvent.OpenPump open = (OutputEvent.OpenP
          +ump) message;                                                 
       96 +                if (validPump(open.pumpNumber())) {           
       97 +                    pumps.get(open.pumpNumber() - 1).ordered(t
          +rue);                                                         
       98 +                }                                             
       99 +            } else if (message instanceof OutputEvent.ClosePum
          +p) {                                                          
      100 +                OutputEvent.ClosePump close = (OutputEvent.Clo
          +sePump) message;                                              
      101 +                if (validPump(close.pumpNumber())) {          
      102 +                    pumps.get(close.pumpNumber() - 1).ordered(
          +false);                                                       
      103 +                }                                             
      104 +            }                                                 
      105 +        }                                                     
      106 +        stopMessages = 0;                                     
      107 +        waitingMessages = 0;                                  
      108 +        unitsReadyMessages = 0;                               
      109 +        levelMessages = 0;                                    
      110 +        steamMessages = 0;                                    
      111 +        levelRepairReported = false;                          
      112 +        steamRepairReported = false;                          
      113 +        levelAckReported = false;                             
      114 +        steamAckReported = false;                             
      115 +        aberrantMessage = false;                              
      116      }
      117  
      118      /** Takes one message of the current transmission. */
      119      public void receive(InputEvent message) {
      120 +        if (message instanceof InputEvent.StopRequest) {      
      121 +            stopMessages = stopMessages + 1;                  
      122 +        } else if (message instanceof InputEvent.SteamBoilerWa
          +iting) {                                                      
      123 +            waitingMessages = waitingMessages + 1;            
      124 +        } else if (message instanceof InputEvent.PhysicalUnits
          +Ready) {                                                      
      125 +            unitsReadyMessages = unitsReadyMessages + 1;      
      126 +        } else if (message instanceof InputEvent.PumpState) { 
      127 +            InputEvent.PumpState state = (InputEvent.PumpState
          +) message;                                                    
      128 +            int number = state.report().reportedPump();       
      129 +            if (validPump(number)) {                          
      130 +                pumps.get(number - 1).reportState(state.report
          +().switchedOn());                                             
      131 +            } else {                                          
      132 +                aberrantMessage = true;                       
      133 +            }                                                 
      134 +        } else if (message instanceof InputEvent.PumpControlSt
          +ate) {                                                        
      135 +            InputEvent.PumpControlState control = (InputEvent.
          +PumpControlState) message;                                    
      136 +            int number = control.report().controlledPump();   
      137 +            if (validPump(number)) {                          
      138 +                pumps.get(number - 1).reportControl(control.re
          +port().flow() == WaterFlow.FLOWING);                          
      139 +            } else {                                          
      140 +                aberrantMessage = true;                       
      141 +            }                                                 
      142 +        } else if (message instanceof InputEvent.Level) {     
      143 +            InputEvent.Level level = (InputEvent.Level) messag
          +e;                                                            
      144 +            levelMessages = levelMessages + 1;                
      145 +            levelValue = level.value();                       
      146 +        } else if (message instanceof InputEvent.Steam) {     
      147 +            InputEvent.Steam steam = (InputEvent.Steam) messag
          +e;                                                            
      148 +            steamMessages = steamMessages + 1;                
      149 +            steamValue = steam.value();                       
      150 +        } else if (message instanceof InputEvent.PumpRepaired)
          + {                                                            
      151 +            InputEvent.PumpRepaired repaired = (InputEvent.Pum
          +pRepaired) message;                                           
      152 +            if (validPump(repaired.pumpNumber())) {           
      153 +                pumps.get(repaired.pumpNumber() - 1).reportPum
          +pRepaired();                                                  
      154 +            } else {                                          
      155 +                aberrantMessage = true;                       
      156 +            }                                                 
      157 +        } else if (message instanceof InputEvent.PumpControlRe
          +paired) {                                                     
      158 +            InputEvent.PumpControlRepaired repaired = (InputEv
          +ent.PumpControlRepaired) message;                             
      159 +            if (validPump(repaired.pumpNumber())) {           
      160 +                pumps.get(repaired.pumpNumber() - 1).reportCon
          +trolRepaired();                                               
      161 +            } else {                                          
      162 +                aberrantMessage = true;                       
      163 +            }                                                 
      164 +        } else if (message instanceof InputEvent.LevelRepaired
          +) {                                                           
      165 +            levelRepairReported = true;                       
      166 +        } else if (message instanceof InputEvent.SteamRepaired
          +) {                                                           
      167 +            steamRepairReported = true;                       
      168 +        } else if (message instanceof InputEvent.PumpFailureAc
          +knowledgement) {                                              
      169 +            InputEvent.PumpFailureAcknowledgement ack =       
      170 +                    (InputEvent.PumpFailureAcknowledgement) me
          +ssage;                                                        
      171 +            if (validPump(ack.pumpNumber())) {                
      172 +                pumps.get(ack.pumpNumber() - 1).reportPumpAckn
          +owledged();                                                   
      173 +            } else {                                          
      174 +                aberrantMessage = true;                       
      175 +            }                                                 
      176 +        } else if (message instanceof InputEvent.PumpControlFa
          +ilureAcknowledgement) {                                       
      177 +            InputEvent.PumpControlFailureAcknowledgement ack =
      178 +                    (InputEvent.PumpControlFailureAcknowledgem
          +ent) message;                                                 
      179 +            if (validPump(ack.pumpNumber())) {                
      180 +                pumps.get(ack.pumpNumber() - 1).reportControlA
          +cknowledged();                                                
      181 +            } else {                                          
      182 +                aberrantMessage = true;                       
      183 +            }                                                 
      184 +        } else if (message instanceof InputEvent.LevelFailureA
          +cknowledgement) {                                             
      185 +            levelAckReported = true;                          
      186 +        } else if (message instanceof InputEvent.SteamOutcomeF
          +ailureAcknowledgement) {                                      
      187 +            steamAckReported = true;                          
      188 +        }                                                     
      189      }
      190  
      191      /** Analyses the current transmission once all its message
           s are received. */
      192      public void analyse() {
      193 +        transmissionFailure = aberrant() || !complete();      
      194 +        if (stopMessages > 0) {                               
      195 +            if (successiveStopCount < SteamBoilerConstants.STO
          +P_LIMIT) {                                                    
      196 +                successiveStopCount = successiveStopCount + 1;
      197 +            }                                                 
      198 +        } else {                                              
      199 +            successiveStopCount = 0;                          
      200 +        }                                                     
      201 +        if (waitingMessages > 0) {                            
      202 +            steamBoilerWaitingReceived = true;                
      203 +        }                                                     
      204 +        physicalUnitsReadyReceived = unitsReadyMessages > 0;  
      205 +        if (transmissionFailure) {                            
      206 +            // The readings cannot be trusted; the controller 
          +stops the program.                                            
      207 +            refreshSummary();                                 
      208 +            return;                                           
      209 +        }                                                     
      210 +        levelReading = levelValue;                            
      211 +        steamReading = steamValue;                            
      212 +        if (!steamBoilerWaitingReceived) {                    
      213 +            // Nothing is judged until the plant announces it 
          +is waiting.                                                   
      214 +            for (PumpMonitor pump : pumps) {                  
      215 +                pump.track();                                 
      216 +            }                                                 
      217 +            refreshSummary();                                 
      218 +            return;                                           
      219 +        }                                                     
      220 +        for (PumpMonitor pump : pumps) {                      
      221 +            pump.judge();                                     
      222 +        }                                                     
      223 +        if (levelAckReported) {                               
      224 +            levelDevice.acknowledgeDetection();               
      225 +        }                                                     
      226 +        if (steamAckReported) {                               
      227 +            steamDevice.acknowledgeDetection();               
      228 +        }                                                     
      229 +        if (levelRepairReported) {                            
      230 +            levelDevice.repair();                             
      231 +        }                                                     
      232 +        if (steamRepairReported) {                            
      233 +            steamDevice.repair();                             
      234 +        }                                                     
      235 +                                                              
      236 +        // Water that entered and steam that left during the c
          +ycle that has just ended.                                     
      237 +        double pastInLow = 0.0;                               
      238 +        double pastInHigh = 0.0;                              
      239 +        for (PumpMonitor pump : pumps) {                      
      240 +            pastInLow = pastInLow + pump.pastFlowLow() * Steam
          +BoilerConstants.CYCLE_SECONDS;                                
      241 +            pastInHigh = pastInHigh + pump.pastFlowHigh() * St
          +eamBoilerConstants.CYCLE_SECONDS;                             
      242 +        }                                                     
      243 +        double previousSteamLow = steamEstimateLow;           
      244 +        double previousSteamHigh = steamEstimateHigh;         
      245 +        if (!steamHistory) {                                  
      246 +            previousSteamLow = 0.0;                           
      247 +            previousSteamHigh = SteamBoilerConstants.MAX_STEAM
          +_W;                                                           
      248 +        }                                                     
      249 +        double pastOutLow = leastSteamOut(previousSteamLow);  
      250 +        double pastOutHigh = mostSteamOut(previousSteamHigh); 
      251 +                                                              
      252 +        // Steam measuring device: static range [0, W] and gra
          +dients U1, U2.                                                
      253 +        double predictedSteamLow = Math.max(0.0,              
      254 +                previousSteamLow - SteamBoilerConstants.STEAM_
          +DECREASE_U2                                                   
      255 +                        * SteamBoilerConstants.CYCLE_SECONDS);
      256 +        double predictedSteamHigh = Math.min(SteamBoilerConsta
          +nts.MAX_STEAM_W,                                              
      257 +                previousSteamHigh + SteamBoilerConstants.STEAM
          +_INCREASE_U1                                                  
      258 +                        * SteamBoilerConstants.CYCLE_SECONDS);
      259 +        if (!steamDevice.outOfOrder()) {                      
      260 +            boolean inRange = steamReading >= 0.0             
      261 +                    && steamReading <= SteamBoilerConstants.MA
          +X_STEAM_W;                                                    
      262 +            boolean reachable = steamReading >= predictedSteam
          +Low                                                           
      263 +                    && steamReading <= predictedSteamHigh;    
      264 +            if (!inRange || !reachable) {                     
      265 +                steamDevice.detectFailure();                  
      266 +            }                                                 
      267 +        }                                                     
      268 +        if (steamDevice.outOfOrder()) {                       
      269 +            steamEstimateLow = predictedSteamLow;             
      270 +            steamEstimateHigh = predictedSteamHigh;           
      271 +        } else {                                              
      272 +            steamEstimateLow = steamReading;                  
      273 +            steamEstimateHigh = steamReading;                 
      274 +        }                                                     
      275 +        steamHistory = true;                                  
      276 +                                                              
      277 +        // Water-level measuring device: static range [0, C] a
          +nd the dynamics of the cycle.                                 
      278 +        double predictedLow = levelEstimateLow + pastInLow - p
          +astOutHigh;                                                   
      279 +        double predictedHigh = levelEstimateHigh + pastInHigh 
          +- pastOutLow;                                                 
      280 +        if (valveOpen) {                                      
      281 +            // The evacuation valve drains at a rate the requi
          +rements do not give.                                          
      282 +            predictedLow = 0.0;                               
      283 +        }                                                     
      284 +        if (!levelHistory) {                                  
      285 +            predictedLow = 0.0;                               
      286 +            predictedHigh = SteamBoilerConstants.CAPACITY_C;  
      287 +        }                                                     
      288 +        predictedLow = clampLevel(predictedLow);              
      289 +        predictedHigh = clampLevel(predictedHigh);            
      290 +        if (!levelDevice.outOfOrder()) {                      
      291 +            boolean inRange = levelReading >= 0.0             
      292 +                    && levelReading <= SteamBoilerConstants.CA
          +PACITY_C;                                                     
      293 +            boolean reachable = levelReading >= predictedLow &
          +& levelReading <= predictedHigh;                              
      294 +            if (!inRange || !reachable) {                     
      295 +                levelDevice.detectFailure();                  
      296 +            }                                                 
      297 +        }                                                     
      298 +        if (levelDevice.outOfOrder()) {                       
      299 +            levelEstimateLow = predictedLow;                  
      300 +            levelEstimateHigh = predictedHigh;                
      301 +        } else {                                              
      302 +            levelEstimateLow = levelReading;                  
      303 +            levelEstimateHigh = levelReading;                 
      304 +        }                                                     
      305 +        levelHistory = true;                                  
      306 +                                                              
      307 +        // Worst case over the coming cycle; a pump ordered on
          + now pours nothing during it.                                 
      308 +        double nextInLow = 0.0;                               
      309 +        double nextInHigh = 0.0;                              
      310 +        for (PumpMonitor pump : pumps) {                      
      311 +            nextInLow = nextInLow + pump.nextFlowLow() * Steam
          +BoilerConstants.CYCLE_SECONDS;                                
      312 +            nextInHigh = nextInHigh + pump.nextFlowHigh() * St
          +eamBoilerConstants.CYCLE_SECONDS;                             
      313 +        }                                                     
      314 +        minimumReachableLevel = levelEstimateLow + nextInLow -
          + mostSteamOut(steamEstimateHigh);                             
      315 +        maximumReachableLevel = levelEstimateHigh + nextInHigh
          + - leastSteamOut(steamEstimateLow);                           
      316 +        refreshSummary();                                     
      317      }
      318  
      319      /** Failure announcements and repair acknowledgements due 
           in the current cycle. */
      320      public List<OutputEvent> protocolMessages() {
       26 -        return new ArrayList<>();                             
      321 +        List<OutputEvent> messages = new ArrayList<>();       
      322 +        for (PumpMonitor pump : pumps) {                      
      323 +            pump.addProtocolMessages(messages);               
      324 +        }                                                     
      325 +        if (levelDevice.detectionPending()) {                 
      326 +            messages.add(new OutputEvent.LevelFailureDetection
          +());                                                          
      327 +        }                                                     
      328 +        if (steamDevice.detectionPending()) {                 
      329 +            messages.add(new OutputEvent.SteamFailureDetection
          +());                                                          
      330 +        }                                                     
      331 +        if (levelDevice.takeRepairAcknowledgement()) {        
      332 +            messages.add(new OutputEvent.LevelRepairedAcknowle
          +dgement());                                                   
      333 +        }                                                     
      334 +        if (steamDevice.takeRepairAcknowledgement()) {        
      335 +            messages.add(new OutputEvent.SteamRepairedAcknowle
          +dgement());                                                   
      336 +        }                                                     
      337 +        return messages;                                      
      338      }
      339  
      340      /** Number of successive transmissions, up to the current 
           one, that carried STOP. */
      341      @RoboChartType("nat")
      342      public int successiveStopCount() {
       32 -        return 0;                                             
      343 +        return successiveStopCount;                           
      344      }
      345  
      346      /** Whether the current transmission lacks an indispensabl
           e message or has an aberrant one. */
      347      public boolean transmissionFailure() {
       37 -        return false;                                         
      348 +        return transmissionFailure;                           
      349      }
      350  
      351      /** Whether STEAM_BOILER_WAITING has arrived in this or an
            earlier transmission. */
      352      public boolean steamBoilerWaitingReceived() {
       42 -        return false;                                         
      353 +        return steamBoilerWaitingReceived;                    
      354      }
      355  
      356      /** Whether the current transmission carries PHYSICAL_UNIT
           S_READY. */
      357      public boolean physicalUnitsReadyReceived() {
       47 -        return false;                                         
      358 +        return physicalUnitsReadyReceived;                    
      359      }
      360  
      361      /** q: the water level most recently reported (litre). */
      362      @RoboChartType("real")
      363      public double levelReading() {
       53 -        return 0.0;                                           
      364 +        return levelReading;                                  
      365      }
      366  
      367      /** v: the steam rate most recently reported (litre/sec). 
           */
      368      @RoboChartType("real")
      369      public double steamReading() {
       59 -        return 0.0;                                           
      370 +        return steamReading;                                  
      371      }
      372  
      373      /** p: the current throughput of the pumps taken together 
           (litre/sec). */
      374      @RoboChartType("real")
      375      public double pumpThroughput() {
       65 -        return 0.0;                                           
      376 +        return pumpThroughput;                                
      377      }
      378  
      379      /** Lower bound of the water level the program currently a
           ssumes (litre). */
      380      @RoboChartType("real")
      381      public double levelEstimateLow() {
       71 -        return 0.0;                                           
      382 +        return levelEstimateLow;                              
      383      }
      384  
      385      /** Upper bound of the water level the program currently a
           ssumes (litre). */
      386      @RoboChartType("real")
      387      public double levelEstimateHigh() {
       77 -        return 0.0;                                           
      388 +        return levelEstimateHigh;                             
      389      }
      390  
      391      /** Midpoint of the level estimate, the level used for reg
           ulation (litre). */
      392      @RoboChartType("real")
      393      public double estimatedLevel() {
       83 -        return 0.0;                                           
      394 +        return estimatedLevel;                                
      395      }
      396  
      397      /** Lowest level the boiler can reach by the end of the co
           ming cycle (litre). */
      398      @RoboChartType("real")
      399      public double minimumReachableLevel() {
       89 -        return 0.0;                                           
      400 +        return minimumReachableLevel;                         
      401      }
      402  
      403      /** Highest level the boiler can reach by the end of the c
           oming cycle (litre). */
      404      @RoboChartType("real")
      405      public double maximumReachableLevel() {
       95 -        return 0.0;                                           
      406 +        return maximumReachableLevel;                         
      407      }
      408  
      409      /** Whether the water-level measuring device is out of ord
           er. */
      410      public boolean levelDeviceOutOfOrder() {
      100 -        return false;                                         
      411 +        return levelDeviceOutOfOrder;                         
      412      }
      413  
      414      /** Whether the steam measuring device is out of order. */
      415      public boolean steamDeviceOutOfOrder() {
      105 -        return false;                                         
      416 +        return steamDeviceOutOfOrder;                         
      417      }
      418  
      419      /** Whether some pump controller is out of order. */
      420      public boolean pumpControllerOutOfOrder() {
      110 -        return false;                                         
      421 +        return pumpControllerOutOfOrder;                      
      422      }
      423  
      424      /** Whether some unit other than the water-level measuring
            device is out of order. */
      425      public boolean otherUnitOutOfOrder() {
      115 -        return false;                                         
      426 +        return otherUnitOutOfOrder;                           
      427      }
      428  
      429      /** Number of sound pumps that are off and can be switched
            on. */
      430      @RoboChartType("nat")
      431      public int openablePumpCount() {
      121 -        return 0;                                             
      432 +        return openablePumpCount;                             
      433      }
      434  
      435      /** Number of sound pumps that are on and can be switched 
           off. */
      436      @RoboChartType("nat")
      437      public int closablePumpCount() {
      127 -        return 0;                                             
      438 +        return closablePumpCount;                             
      439      }
      440  
      441      /** The sound pump to switch on next (0 when there is none
           ). */
      442      @RoboChartType("nat")
      443      public int pumpToOpen() {
      133 -        return 0;                                             
      444 +        return pumpToOpen;                                    
      445      }
      446  
      447      /** The sound pump to switch off next (0 when there is non
           e). */
      448      @RoboChartType("nat")
      449      public int pumpToClose() {
      139 -        return 0;                                             
      450 +        return pumpToClose;                                   
      451      }
      452 +                                                              
      453 +    private boolean complete() {                              
      454 +        boolean complete = levelMessages == 1 && steamMessages
          + == 1;                                                        
      455 +        for (PumpMonitor pump : pumps) {                      
      456 +            if (!pump.transmissionComplete()) {               
      457 +                complete = false;                             
      458 +            }                                                 
      459 +        }                                                     
      460 +        return complete;                                      
      461 +    }                                                         
      462 +                                                              
      463 +    private boolean aberrant() {                              
      464 +        boolean aberrant = aberrantMessage;                   
      465 +        for (PumpMonitor pump : pumps) {                      
      466 +            if (pump.aberrant()) {                            
      467 +                aberrant = true;                              
      468 +            }                                                 
      469 +        }                                                     
      470 +        boolean initializing = lastModeSent == ModeValue.MODE_
          +INITIALIZATION;                                               
      471 +        if (waitingMessages > 0 && !initializing) {           
      472 +            aberrant = true;                                  
      473 +        }                                                     
      474 +        if (unitsReadyMessages > 0 && !(initializing && progra
          +mReadySent)) {                                                
      475 +            aberrant = true;                                  
      476 +        }                                                     
      477 +        if (levelRepairReported && !levelDevice.outOfOrder()) 
          +{                                                             
      478 +            aberrant = true;                                  
      479 +        }                                                     
      480 +        if (steamRepairReported && !steamDevice.outOfOrder()) 
          +{                                                             
      481 +            aberrant = true;                                  
      482 +        }                                                     
      483 +        if (levelAckReported && !levelDevice.detectionPending(
          +)) {                                                          
      484 +            aberrant = true;                                  
      485 +        }                                                     
      486 +        if (steamAckReported && !steamDevice.detectionPending(
          +)) {                                                          
      487 +            aberrant = true;                                  
      488 +        }                                                     
      489 +        return aberrant;                                      
      490 +    }                                                         
      491 +                                                              
      492 +    private void refreshSummary() {                           
      493 +        pumpThroughput = 0.0;                                 
      494 +        openablePumpCount = 0;                                
      495 +        closablePumpCount = 0;                                
      496 +        pumpToOpen = 0;                                       
      497 +        pumpToClose = 0;                                      
      498 +        boolean pumpOutOfOrder = false;                       
      499 +        boolean controlOutOfOrder = false;                    
      500 +        for (PumpMonitor pump : pumps) {                      
      501 +            if (pump.believedPouring()) {                     
      502 +                pumpThroughput = pumpThroughput + SteamBoilerC
          +onstants.PUMP_CAPACITY_P;                                     
      503 +            }                                                 
      504 +            if (pump.openable()) {                            
      505 +                openablePumpCount = openablePumpCount + 1;    
      506 +                if (pumpToOpen == 0) {                        
      507 +                    pumpToOpen = pump.pumpNumber();           
      508 +                }                                             
      509 +            }                                                 
      510 +            if (pump.closable()) {                            
      511 +                closablePumpCount = closablePumpCount + 1;    
      512 +                pumpToClose = pump.pumpNumber();              
      513 +            }                                                 
      514 +            if (pump.pumpOutOfOrder()) {                      
      515 +                pumpOutOfOrder = true;                        
      516 +            }                                                 
      517 +            if (pump.controlOutOfOrder()) {                   
      518 +                controlOutOfOrder = true;                     
      519 +            }                                                 
      520 +        }                                                     
      521 +        levelDeviceOutOfOrder = levelDevice.outOfOrder();     
      522 +        steamDeviceOutOfOrder = steamDevice.outOfOrder();     
      523 +        pumpControllerOutOfOrder = controlOutOfOrder;         
      524 +        otherUnitOutOfOrder = steamDeviceOutOfOrder || pumpOut
          +OfOrder || controlOutOfOrder;                                 
      525 +        estimatedLevel = (levelEstimateLow + levelEstimateHigh
          +) / 2.0;                                                      
      526 +    }                                                         
      527 +                                                              
      528 +    private static boolean validPump(@RoboChartType("nat") int
          + number) {                                                    
      529 +        return number >= 1 && number <= SteamBoilerConstants.P
          +UMP_COUNT;                                                    
      530 +    }                                                         
      531 +                                                              
      532 +    @RoboChartType("real")                                    
      533 +    private static double clampLevel(@RoboChartType("real") do
          +uble level) {                                                 
      534 +        return Math.max(0.0, Math.min(SteamBoilerConstants.CAP
          +ACITY_C, level));                                             
      535 +    }                                                         
      536 +                                                              
      537 +    /** Least steam (litre) that can leave in one cycle when t
          +he rate starts at {@code rate}. */                            
      538 +    @RoboChartType("real")                                    
      539 +    private static double leastSteamOut(@RoboChartType("real")
          + double rate) {                                               
      540 +        double t = SteamBoilerConstants.CYCLE_SECONDS;        
      541 +        double u2 = SteamBoilerConstants.STEAM_DECREASE_U2;   
      542 +        if (rate >= u2 * t) {                                 
      543 +            return rate * t - u2 * t * t / 2.0;               
      544 +        }                                                     
      545 +        return rate * rate / (2.0 * u2);                      
      546 +    }                                                         
      547 +                                                              
      548 +    /** Most steam (litre) that can leave in one cycle when th
          +e rate starts at {@code rate}. */                             
      549 +    @RoboChartType("real")                                    
      550 +    private static double mostSteamOut(@RoboChartType("real") 
          +double rate) {                                                
      551 +        double t = SteamBoilerConstants.CYCLE_SECONDS;        
      552 +        double u1 = SteamBoilerConstants.STEAM_INCREASE_U1;   
      553 +        double w = SteamBoilerConstants.MAX_STEAM_W;          
      554 +        if (rate + u1 * t <= w) {                             
      555 +            return rate * t + u1 * t * t / 2.0;               
      556 +        }                                                     
      557 +        double untilMax = Math.max(0.0, (w - rate) / u1);     
      558 +        return rate * untilMax + u1 * untilMax * untilMax / 2.
          +0 + w * (t - untilMax);                                       
      559 +    }                                                         
      560  }

⏺ Now the full actuator.

⏺ Write(~/Gitee/forge-v3r9-run1-steam_boiler/java.generated.project/src/main/jav
       a/steam_boiler/actuator/SteamBoilerActuator.java)
  ⎿  Added 23 lines, removed 4 lines
       5  import steam_boiler.event.ModeValue;
       6  import steam_boiler.event.OutputEvent;
       7  
       8 -/** Receives the program's outgoing messages for the current cy
         -cle. */                                                        
       8 +/**                                                            
       9 + * Receives the program's outgoing messages for the current cyc
         +le, which are all emitted                                      
      10 + * together at the end of the cycle, and records the last mode 
         +and valve position requested.                                  
      11 + */                                                            
      12  public final class SteamBoilerActuator {
      13  
      14 +    private final List<OutputEvent> outgoing = new ArrayList<>(
         +);                                                             
      15 +    private ModeValue lastMode = ModeValue.MODE_INITIALIZATION;
      16 +    private ValvePosition valvePosition = ValvePosition.VALVE_C
         +LOSED;                                                         
      17 +                                                               
      18      /** Issues one outgoing message in the current cycle. */
      19      public void apply(OutputEvent message) {
      20 +        outgoing.add(message);                                 
      21 +        if (message instanceof OutputEvent.Mode) {             
      22 +            OutputEvent.Mode modeMessage = (OutputEvent.Mode) m
         +essage;                                                        
      23 +            lastMode = modeMessage.value();                    
      24 +        } else if (message instanceof OutputEvent.Valve) {     
      25 +            if (valvePosition == ValvePosition.VALVE_CLOSED) { 
      26 +                valvePosition = ValvePosition.VALVE_OPEN;      
      27 +            } else {                                           
      28 +                valvePosition = ValvePosition.VALVE_CLOSED;    
      29 +            }                                                  
      30 +        }                                                      
      31      }
      32  
      33      /** Begins a new cycle with an empty outgoing transmission.
           */
      34      public void startCycle() {
      35 +        outgoing.clear();                                      
      36      }
      37  
      38      /** The messages issued so far in the current cycle. */
      39      public List<OutputEvent> outgoing() {
      21 -        return new ArrayList<>();                              
      40 +        return new ArrayList<>(outgoing);                      
      41      }
      42  
      43      /** The mode carried by the last MODE message issued. */
      44      public ModeValue lastMode() {
      26 -        return ModeValue.MODE_INITIALIZATION;                  
      45 +        return lastMode;                                       
      46      }
      47  
      48      /** The valve position the VALVE messages issued so far hav
          e requested. */
      49      public ValvePosition valvePosition() {
      31 -        return ValvePosition.VALVE_CLOSED;                     
      50 +        return valvePosition;                                  
      51      }
      52  }

⏺ Bash(OUT=/private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3r9-run1-steam-boi
      ler/6ad4e450-4361-4ef7-b698-9a5dc45942fd/scratchpad/classes && rm -rf $OUT
      && mkdir -p…)
  ⎿  (No output)

⏺ Layer 3 compiles. Layer 4: the controller's step() and the cycle driver.

⏺ Write(~/Gitee/forge-v3r9-run1-steam_boiler/java.generated.project/src/main/jav
       a/steam_boiler/controller/SteamBoilerController.java)
  ⎿  Added 211 lines, removed 1 line
       1  package steam_boiler.controller;
       2  
       3 +import steam_boiler.SteamBoilerConstants;                      
       4  import steam_boiler.actuator.SteamBoilerActuator;
       5  import steam_boiler.actuator.ValvePosition;
       6  import steam_boiler.annotation.RoboChartType;
       7  import steam_boiler.event.InputEvent;
       8 +import steam_boiler.event.ModeValue;                           
       9 +import steam_boiler.event.OutputEvent;                         
      10  import steam_boiler.sensor.BoilerSensor;
      11  
       9 -/** The steam-boiler control program's operating-mode state mac
         -hine. */                                                       
      12 +/**                                                            
      13 + * The steam-boiler control program's operating-mode state mach
         +ine.                                                           
      14 + *                                                             
      15 + * <p>{@link #step} runs once per five-second cycle, after the 
         +sensor has analysed the cycle's                                
      16 + * transmission. Every cycle ends with a MODE message carrying 
         +the mode the program is now in.                                
      17 + * Emergency stop is terminal: the program's control task ends 
         +there.                                                         
      18 + */                                                            
      19  public final class SteamBoilerController {
      20  
      21      private OperatingMode currentMode = OperatingMode.INITIALIZ
          ATION;
     ...
       38  
       39      /** Analyses one cycle's transmission and decides the cycl
           e's outgoing commands. */
       40      public void step(InputEvent event) {
       41 +        // --- Quantities and judgements of the current transm
          +ission ---                                                    
       42 +        boolean stopLimitReached = sensor.successiveStopCount(
          +) >= SteamBoilerConstants.STOP_LIMIT;                         
       43 +        boolean transmissionFailed = sensor.transmissionFailur
          +e();                                                          
       44 +        boolean boilerWaiting = sensor.steamBoilerWaitingRecei
          +ved();                                                        
       45 +        boolean unitsReady = sensor.physicalUnitsReadyReceived
          +();                                                           
       46 +        boolean steamNonZero = sensor.steamReading() != 0.0;  
       47 +        boolean levelFailed = sensor.levelDeviceOutOfOrder(); 
       48 +        boolean steamFailed = sensor.steamDeviceOutOfOrder(); 
       49 +        boolean pumpControlFailed = sensor.pumpControllerOutOf
          +Order();                                                      
       50 +        boolean otherUnitFailed = sensor.otherUnitOutOfOrder()
          +;                                                             
       51 +        boolean levelAboveN2 = sensor.levelReading() > SteamBo
          +ilerConstants.NORMAL_N2;                                      
       52 +        boolean levelBelowN1 = sensor.levelReading() < SteamBo
          +ilerConstants.NORMAL_N1;                                      
       53 +        boolean estimateAboveN2 = sensor.estimatedLevel() > St
          +eamBoilerConstants.NORMAL_N2;                                 
       54 +        boolean estimateBelowN1 = sensor.estimatedLevel() < St
          +eamBoilerConstants.NORMAL_N1;                                 
       55 +        boolean reachBelowM1 = sensor.minimumReachableLevel() 
          +<= SteamBoilerConstants.LIMIT_M1;                             
       56 +        boolean reachAboveM2 = sensor.maximumReachableLevel() 
          +>= SteamBoilerConstants.LIMIT_M2;                             
       57 +        boolean pumpOpenable = sensor.openablePumpCount() > 0;
       58 +        boolean pumpClosable = sensor.closablePumpCount() > 0;
       59 +        boolean valveOpen = evacuationValve == ValvePosition.V
          +ALVE_OPEN;                                                    
       60 +                                                              
       61 +        // --- Causes of emergency stop ---                   
       62 +        boolean stopOrLinkFailure = stopLimitReached || transm
          +issionFailed;                                                 
       63 +        boolean levelAtRisk = reachBelowM1 || reachAboveM2;   
       64 +        boolean rescueUntrustworthy = steamFailed || pumpContr
          +olFailed;                                                     
       65 +                                                              
       66 +        // --- Initialization: wait, check, adjust the level, 
          +hand over ---                                                 
       67 +        boolean initAbort = stopOrLinkFailure || (boilerWaitin
          +g && (steamNonZero || levelFailed));                          
       68 +        boolean initWaiting = !initAbort && !boilerWaiting;   
       69 +        boolean initActive = !initAbort && boilerWaiting;     
       70 +        boolean initLeave = initActive && unitsReady && !valve
          +Open && !fillingPumpOn;                                       
       71 +        boolean initToNormal = initLeave && !otherUnitFailed; 
       72 +        boolean initToDegraded = initLeave && otherUnitFailed;
       73 +        boolean initAdjust = initActive && !initLeave;        
       74 +        boolean initDraining = initAdjust && valveOpen && leve
          +lAboveN2;                                                     
       75 +        boolean initCloseValve = initAdjust && valveOpen && !l
          +evelAboveN2;                                                  
       76 +        boolean initFilling = initAdjust && !valveOpen && fill
          +ingPumpOn && levelBelowN1;                                    
       77 +        boolean initStopFilling = initAdjust && !valveOpen && 
          +fillingPumpOn && !levelBelowN1;                               
       78 +        boolean initIdle = initAdjust && !valveOpen && !fillin
          +gPumpOn;                                                      
       79 +        boolean initOpenValve = initIdle && levelAboveN2;     
       80 +        boolean initStartFilling = initIdle && !levelAboveN2 &
          +& levelBelowN1 && pumpOpenable;                               
       81 +        boolean initNoPumpToStart = initIdle && !levelAboveN2 
          +&& levelBelowN1 && !pumpOpenable;                             
       82 +        boolean initLevelReady = initIdle && !levelAboveN2 && 
          +!levelBelowN1;                                                
       83 +                                                              
       84 +        // --- Normal and degraded: regulate on the measured l
          +evel ---                                                      
       85 +        boolean regulatedAbort =                              
       86 +                stopOrLinkFailure || levelAtRisk || (levelFail
          +ed && rescueUntrustworthy);                                   
       87 +        boolean regulatedToRescue = !regulatedAbort && levelFa
          +iled;                                                         
       88 +        boolean regulatedContinue = !regulatedAbort && !levelF
          +ailed;                                                        
       89 +        boolean regulatedAllSound = regulatedContinue && !othe
          +rUnitFailed;                                                  
       90 +        boolean regulatedSomeFailed = regulatedContinue && oth
          +erUnitFailed;                                                 
       91 +        boolean levelOpenPump = levelBelowN1 && pumpOpenable; 
       92 +        boolean levelClosePump = levelAboveN2 && pumpClosable 
          +&& !levelOpenPump;                                            
       93 +        boolean levelHoldPumps = !levelOpenPump && !levelClose
          +Pump;                                                         
       94 +                                                              
       95 +        // --- Rescue: regulate on the estimated level ---    
       96 +        boolean rescueAbort = stopOrLinkFailure || levelAtRisk
          + || rescueUntrustworthy;                                      
       97 +        boolean rescueLevelBack = !rescueAbort && !levelFailed
          +;                                                             
       98 +        boolean rescueToNormal = rescueLevelBack && !otherUnit
          +Failed;                                                       
       99 +        boolean rescueToDegraded = rescueLevelBack && otherUni
          +tFailed;                                                      
      100 +        boolean rescueContinue = !rescueAbort && levelFailed; 
      101 +        boolean estimateOpenPump = estimateBelowN1 && pumpOpen
          +able;                                                         
      102 +        boolean estimateClosePump = estimateAboveN2 && pumpClo
          +sable && !estimateOpenPump;                                   
      103 +        boolean estimateHoldPumps = !estimateOpenPump && !esti
          +mateClosePump;                                                
      104 +                                                              
      105 +        // --- Two-level if-else: one block per mode, transiti
          +ons in priority order ---                                     
      106 +        if (currentMode == OperatingMode.INITIALIZATION) {    
      107 +            if (event instanceof InputEvent.TransmissionReceiv
          +ed && initAbort) {                                            
      108 +                currentMode = OperatingMode.EMERGENCY_STOP;   
      109 +                actuator.apply(new OutputEvent.Mode(ModeValue.
          +MODE_EMERGENCY_STOP));                                        
      110 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && initWaiting) {                                   
      111 +                currentMode = OperatingMode.INITIALIZATION;   
      112 +                actuator.apply(new OutputEvent.Mode(ModeValue.
          +MODE_INITIALIZATION));                                        
      113 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && initToNormal) {                                  
      114 +                currentMode = OperatingMode.NORMAL;           
      115 +                actuator.apply(new OutputEvent.Mode(ModeValue.
          +MODE_NORMAL));                                                
      116 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && initToDegraded) {                                
      117 +                currentMode = OperatingMode.DEGRADED;         
      118 +                actuator.apply(new OutputEvent.Mode(ModeValue.
          +MODE_DEGRADED));                                              
      119 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && initDraining) {                                  
      120 +                currentMode = OperatingMode.INITIALIZATION;   
      121 +                actuator.apply(new OutputEvent.Mode(ModeValue.
          +MODE_INITIALIZATION));                                        
      122 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && initCloseValve) {                                
      123 +                currentMode = OperatingMode.INITIALIZATION;   
      124 +                actuator.apply(new OutputEvent.Mode(ModeValue.
          +MODE_INITIALIZATION));                                        
      125 +                actuator.apply(new OutputEvent.Valve());      
      126 +                evacuationValve = ValvePosition.VALVE_CLOSED; 
      127 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && initFilling) {                                   
      128 +                currentMode = OperatingMode.INITIALIZATION;   
      129 +                actuator.apply(new OutputEvent.Mode(ModeValue.
          +MODE_INITIALIZATION));                                        
      130 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && initStopFilling) {                               
      131 +                currentMode = OperatingMode.INITIALIZATION;   
      132 +                actuator.apply(new OutputEvent.Mode(ModeValue.
          +MODE_INITIALIZATION));                                        
      133 +                actuator.apply(new OutputEvent.ClosePump(order
          +edPump));                                                     
      134 +                fillingPumpOn = false;                        
      135 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && initOpenValve) {                                 
      136 +                currentMode = OperatingMode.INITIALIZATION;   
      137 +                actuator.apply(new OutputEvent.Mode(ModeValue.
          +MODE_INITIALIZATION));                                        
      138 +                actuator.apply(new OutputEvent.Valve());      
      139 +                evacuationValve = ValvePosition.VALVE_OPEN;   
      140 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && initStartFilling) {                              
      141 +                currentMode = OperatingMode.INITIALIZATION;   
      142 +                actuator.apply(new OutputEvent.Mode(ModeValue.
          +MODE_INITIALIZATION));                                        
      143 +                orderedPump = sensor.pumpToOpen();            
      144 +                actuator.apply(new OutputEvent.OpenPump(ordere
          +dPump));                                                      
      145 +                fillingPumpOn = true;                         
      146 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && initNoPumpToStart) {                             
      147 +                currentMode = OperatingMode.INITIALIZATION;   
      148 +                actuator.apply(new OutputEvent.Mode(ModeValue.
          +MODE_INITIALIZATION));                                        
      149 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && initLevelReady) {                                
      150 +                currentMode = OperatingMode.INITIALIZATION;   
      151 +                actuator.apply(new OutputEvent.Mode(ModeValue.
          +MODE_INITIALIZATION));                                        
      152 +                actuator.apply(new OutputEvent.ProgramReady())
          +;                                                             
      153 +            }                                                 
      154 +                                                              
      155 +        } else if (currentMode == OperatingMode.NORMAL) {     
      156 +            if (event instanceof InputEvent.TransmissionReceiv
          +ed && regulatedAbort) {                                       
      157 +                currentMode = OperatingMode.EMERGENCY_STOP;   
      158 +                actuator.apply(new OutputEvent.Mode(ModeValue.
          +MODE_EMERGENCY_STOP));                                        
      159 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && regulatedToRescue) {                             
      160 +                currentMode = OperatingMode.RESCUE;           
      161 +                actuator.apply(new OutputEvent.Mode(ModeValue.
          +MODE_RESCUE));                                                
      162 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && regulatedSomeFailed) {                           
      163 +                currentMode = OperatingMode.DEGRADED;         
      164 +                actuator.apply(new OutputEvent.Mode(ModeValue.
          +MODE_DEGRADED));                                              
      165 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived                                                     
      166 +                    && regulatedAllSound && levelOpenPump) {  
      167 +                currentMode = OperatingMode.NORMAL;           
      168 +                actuator.apply(new OutputEvent.Mode(ModeValue.
          +MODE_NORMAL));                                                
      169 +                orderedPump = sensor.pumpToOpen();            
      170 +                actuator.apply(new OutputEvent.OpenPump(ordere
          +dPump));                                                      
      171 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived                                                     
      172 +                    && regulatedAllSound && levelClosePump) { 
      173 +                currentMode = OperatingMode.NORMAL;           
      174 +                actuator.apply(new OutputEvent.Mode(ModeValue.
          +MODE_NORMAL));                                                
      175 +                orderedPump = sensor.pumpToClose();           
      176 +                actuator.apply(new OutputEvent.ClosePump(order
          +edPump));                                                     
      177 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived                                                     
      178 +                    && regulatedAllSound && levelHoldPumps) { 
      179 +                currentMode = OperatingMode.NORMAL;           
      180 +                actuator.apply(new OutputEvent.Mode(ModeValue.
          +MODE_NORMAL));                                                
      181 +            }                                                 
      182 +                                                              
      183 +        } else if (currentMode == OperatingMode.DEGRADED) {   
      184 +            if (event instanceof InputEvent.TransmissionReceiv
          +ed && regulatedAbort) {                                       
      185 +                currentMode = OperatingMode.EMERGENCY_STOP;   
      186 +                actuator.apply(new OutputEvent.Mode(ModeValue.
          +MODE_EMERGENCY_STOP));                                        
      187 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && regulatedToRescue) {                             
      188 +                currentMode = OperatingMode.RESCUE;           
      189 +                actuator.apply(new OutputEvent.Mode(ModeValue.
          +MODE_RESCUE));                                                
      190 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && regulatedAllSound) {                             
      191 +                currentMode = OperatingMode.NORMAL;           
      192 +                actuator.apply(new OutputEvent.Mode(ModeValue.
          +MODE_NORMAL));                                                
      193 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived                                                     
      194 +                    && regulatedSomeFailed && levelOpenPump) {
      195 +                currentMode = OperatingMode.DEGRADED;         
      196 +                actuator.apply(new OutputEvent.Mode(ModeValue.
          +MODE_DEGRADED));                                              
      197 +                orderedPump = sensor.pumpToOpen();            
      198 +                actuator.apply(new OutputEvent.OpenPump(ordere
          +dPump));                                                      
      199 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived                                                     
      200 +                    && regulatedSomeFailed && levelClosePump) 
          +{                                                             
      201 +                currentMode = OperatingMode.DEGRADED;         
      202 +                actuator.apply(new OutputEvent.Mode(ModeValue.
          +MODE_DEGRADED));                                              
      203 +                orderedPump = sensor.pumpToClose();           
      204 +                actuator.apply(new OutputEvent.ClosePump(order
          +edPump));                                                     
      205 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived                                                     
      206 +                    && regulatedSomeFailed && levelHoldPumps) 
          +{                                                             
      207 +                currentMode = OperatingMode.DEGRADED;         
      208 +                actuator.apply(new OutputEvent.Mode(ModeValue.
          +MODE_DEGRADED));                                              
      209 +            }                                                 
      210 +                                                              
      211 +        } else if (currentMode == OperatingMode.RESCUE) {     
      212 +            if (event instanceof InputEvent.TransmissionReceiv
          +ed && rescueAbort) {                                          
      213 +                currentMode = OperatingMode.EMERGENCY_STOP;   
      214 +                actuator.apply(new OutputEvent.Mode(ModeValue.
          +MODE_EMERGENCY_STOP));                                        
      215 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && rescueToNormal) {                                
      216 +                currentMode = OperatingMode.NORMAL;           
      217 +                actuator.apply(new OutputEvent.Mode(ModeValue.
          +MODE_NORMAL));                                                
      218 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && rescueToDegraded) {                              
      219 +                currentMode = OperatingMode.DEGRADED;         
      220 +                actuator.apply(new OutputEvent.Mode(ModeValue.
          +MODE_DEGRADED));                                              
      221 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived                                                     
      222 +                    && rescueContinue && estimateOpenPump) {  
      223 +                currentMode = OperatingMode.RESCUE;           
      224 +                actuator.apply(new OutputEvent.Mode(ModeValue.
          +MODE_RESCUE));                                                
      225 +                orderedPump = sensor.pumpToOpen();            
      226 +                actuator.apply(new OutputEvent.OpenPump(ordere
          +dPump));                                                      
      227 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived                                                     
      228 +                    && rescueContinue && estimateClosePump) { 
      229 +                currentMode = OperatingMode.RESCUE;           
      230 +                actuator.apply(new OutputEvent.Mode(ModeValue.
          +MODE_RESCUE));                                                
      231 +                orderedPump = sensor.pumpToClose();           
      232 +                actuator.apply(new OutputEvent.ClosePump(order
          +edPump));                                                     
      233 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived                                                     
      234 +                    && rescueContinue && estimateHoldPumps) { 
      235 +                currentMode = OperatingMode.RESCUE;           
      236 +                actuator.apply(new OutputEvent.Mode(ModeValue.
          +MODE_RESCUE));                                                
      237 +            }                                                 
      238 +                                                              
      239 +        } else if (currentMode == OperatingMode.EMERGENCY_STOP
          +) {                                                           
      240 +            // Terminal: the control task has ended and the ph
          +ysical environment is in charge.                              
      241 +        }                                                     
      242      }
      243  }

⏺ Write(~/Gitee/forge-v3r9-run1-steam_boiler/java.generated.project/src/main/jav
       a/steam_boiler/SteamBoilerProgram.java)
  ⎿  Added 23 lines, removed 2 lines
       9  import steam_boiler.event.OutputEvent;
      10  import steam_boiler.sensor.BoilerSensor;
      11  
      12 -/** The steam-boiler control program: one call per five-second 
         -cycle. */                                                      
      12 +/**                                                            
      13 + * The steam-boiler control program: one call per five-second c
         +ycle. A cycle receives the                                     
      14 + * incoming messages (all considered simultaneous), analyses th
         +em, and returns the outgoing                                   
      15 + * messages (all emitted together). Once in emergency stop the 
         +program has stopped and                                        
      16 + * answers nothing.                                            
      17 + */                                                            
      18  public final class SteamBoilerProgram {
      19  
      20      private final BoilerSensor sensor = new BoilerSensor();
     ...
      23  
      24      /** Runs one cycle: receive the transmission, analyse it, a
          nd return the outgoing one. */
      25      public List<OutputEvent> cycle(List<InputEvent> transmissio
          n) {
      21 -        return new ArrayList<>();                              
      26 +        if (controller.currentMode() == OperatingMode.EMERGENCY
         +_STOP) {                                                       
      27 +            return new ArrayList<>();                          
      28 +        }                                                      
      29 +        sensor.startCycle(actuator.outgoing());                
      30 +        actuator.startCycle();                                 
      31 +        for (InputEvent message : transmission) {              
      32 +            sensor.receive(message);                           
      33 +        }                                                      
      34 +        sensor.analyse();                                      
      35 +        controller.step(new InputEvent.TransmissionReceived());
      36 +        if (controller.currentMode() == OperatingMode.EMERGENCY
         +_STOP) {                                                       
      37 +            return actuator.outgoing();                        
      38 +        }                                                      
      39 +        for (OutputEvent message : sensor.protocolMessages()) {
      40 +            actuator.apply(message);                           
      41 +        }                                                      
      42 +        return actuator.outgoing();                            
      43      }
      44  
      45      /** The program's current operating mode. */

⏺ Bash(OUT=/private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3r9-run1-steam-boi
      ler/6ad4e450-4361-4ef7-b698-9a5dc45942fd/scratchpad/classes && rm -rf $OUT
      && mkdir -p…)
  ⎿  (No output)

⏺ The build succeeded, so now I'll run a quick jshell smoke test simulating a
  plant through start-up and normal operation to confirm no false failures
  occur.

⏺ Bash(OUT=/private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3r9-run1-steam-boi
      ler/6ad4e450-4361-4ef7-b698-9a5dc45942fd/scratchpad/classes && jshell
      --class-path $OUT…)
  ⎿  (No output)

⏺ The nominal run confirms correct behavior: initialization to N1, PROGRAM_READY
  handshake, NORMAL entry, and pump regulation each cycle. Next I'll test a
  fault scenario covering level-device failure, acknowledgement, repair, and
  three STOPs.

⏺ Bash(OUT=/private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3r9-run1-steam-boi
      ler/6ad4e450-4361-4ef7-b698-9a5dc45942fd/scratchpad/classes && jshell
      --class-path $OUT…)
  ⎿  (No output)

⏺ Fault handling checks out: the bad level reading triggers RESCUE, repeated
  LEVEL_FAILURE_DETECTION until acknowledged, repair returns to NORMAL, and the
  third successive STOP correctly escalates to EMERGENCY_STOP. Now I'll move to
  the review layer and index every type and public member to build the trace.

⏺ Bash(grep -rnE '^\s*(public|record|enum)|^\s+[A-Z_0-9]+,?$|record [A-Za-z]+\('
      src/main/java | grep -vE '^\s*//' | sed -E 's/\{.*$//' | awk -F: '{printf
      "%s:%s:%s\n"…)
  ⎿  (No output)

⏺ I'll write the trace now.

⏺ Write(~/Gitee/forge-v3r9-run1-steam_boiler/java.generated.project/result_codeg
       en.json)
  ⎿  Wrote 262 lines to ~/Gitee/forge-v3r9-run1-steam_boiler/java.generated
     .project/result_codegen.json
      1 {
      2     "codegen_trace": [
      3         {"requirement_gid": "SB-V1", "java_file": "src/main/java/
        steam_boiler/controller/SteamBoilerController.java", "java_elemen
        t": "SteamBoilerController", "element_type": "class"},
      4         {"requirement_gid": "SB-V1", "java_file": "src/main/java/
        steam_boiler/SteamBoilerProgram.java", "java_element": "SteamBoil
        erProgram", "element_type": "class"},
      5         {"requirement_gid": "SB-V1", "java_file": "src/main/java/
        steam_boiler/sensor/BoilerSensor.java", "java_element": "BoilerSe
     … +258 lines (~35 KB) (ctrl+o to expand)

⏺ I'll check the trace mechanically: every requirement covered, every file
  named, every element present in its file, and every public element claimed.
⏺ Bash(cd /Users/ranwei/Gitee/forge-v3r9-run1-steam_boiler && python3 -I -
      <<'EOF'
      import json, re, os, glob…)
  ⎿  (No output)

⏺ The trace is complete: all 53 requirements are covered, all 16 files are
  named, every element exists, and no public element is unclaimed. Next I'll get
  line numbers for the Layer 5 review entries.

⏺ Bash(cd /Users/ranwei/Gitee/forge-v3r9-run1-steam_boiler/java.generated.projec
      t/src/main/java/steam_boiler && grep -nE 'CAPACITY_C =|STOP_LIMIT =|public
      void step|bo…)
  ⎿  (No output)

  Searched for 1 pattern (ctrl+o to expand)

⏺ Now the Layer 5 review files, post_codegen.json and its markdown mirror.

⏺ Write(post_codegen.json)
  ⎿  Wrote 139 lines to post_codegen.json
      1 {
      2   "phase": "codegen",
      3   "status": "uncertain",
      4   "summary": "All 53 requirements are implemented and traced. One
         controller (SteamBoilerController) with five modes and 30 transi
        tions, all triggered by the per-cycle TransmissionReceived event,
         with guards that are mutually exclusive and exhaustive within ea
        ch mode. Perception, failure detection, unit bookkeeping and the
        level estimate live in BoilerSensor (with PumpMonitor and UnitSta
        tus). The requirements leave several values and policies open; th
     … +136 lines (~12 KB) (ctrl+o to expand)

⏺ Write(post_codegen.md)
  ⎿  Wrote 81 lines to post_codegen.md
       1 # post_codegen
       2
       3 **status:** uncertain
       4
       5 **summary:** All 53 requirements are implemented and traced. One
          controller (SteamBoilerController) with five modes and 30 trans
         itions, all triggered by the per-cycle TransmissionReceived even
         t, with guards that are mutually exclusive and exhaustive within
          each mode. Perception, failure detection, unit bookkeeping and
         the level estimate live in BoilerSensor (with PumpMonitor and Un
     … +77 lines (~10 KB) (ctrl+o to expand)

⏺ Now ITER_SUMMARY.md, kept under 300 words.

⏺ Write(ITER_SUMMARY.md)
  ⎿  Wrote 16 lines to ITER_SUMMARY.md
      1 # ITER_SUMMARY — steam_boiler, run 1, iteration 1
      2
      3 No Java was present before writing. I wrote 16 files under `steam
        _boiler/`.
      4
      5 **What and why.** The design has one controller, `SteamBoilerCont
        roller`, with modes INITIALIZATION, NORMAL, DEGRADED, RESCUE and
        EMERGENCY_STOP. Its `step` runs once per cycle on `TransmissionRe
        ceived`. `SteamBoilerProgram.cycle` first gives every incoming me
        ssage to `BoilerSensor`, which judges the transmission, the pumps
     … +12 lines (~2 KB) (ctrl+o to expand)

⏺ Bash(cd /Users/ranwei/Gitee/forge-v3r9-run1-steam_boiler && wc -w
      forge.assets/corrections/ITER_SUMMARY.md && python3 -I -c "import
      json;json.load(open('forge.assets…)
  ⎿  (No output)

⏺ The pipeline.yaml and .gitignore changes were part of the initial setup
  snapshot, not mine, so I'll leave them untouched. Moving on to iteration 1 of
  the runner, passing unavailable since I lack a session token figure.

⏺ Bash(export FORGE_ARM_DIR=/Users/ranwei/Gitee/formal_method_guided_vibe_coding
      /experiments/v3-reference && export FORGE_WT_PREFIX=forge-v3r9 && export
      FORGE_STAGE_DI…)
  ⎿  Running in the background (↓ to manage)

⏺ Iteration 1 is running in the background; I'll read the result when it
  finishes.

⏺ I don't need the Monitor tool: the background run will notify me when it
  exits. Iteration 1 (Java, trace, post_codegen and ITER_SUMMARY.md) is written
  and the runner is still running. I'll read its result and any failing-phase
  reports as soon as it finishes.

⏺ Background command "Run pipeline iteration 1 via the runner" completed (exit
code 0)

  Read 1 file (ctrl+o to expand)

⏺ Iteration 1 results: everything up through code generation passes, but FDR4,
  Dafny, and Isabelle verification fail. I'll check the three failure reports
  now.

⏺ Bash(cd
      /Users/ranwei/Gitee/forge-v3r9-run1-steam_boiler/forge.assets/corrections
      && wc -c post_fdr4.md post_dafny_verify.md post_isabelle_verify.md && cat
      post_dafn…)
  ⎿  (No output)

⏺ Bash(cat post_fdr4.md && echo ======= && grep -E
      '^forge.transformations|Error' post_dafny_verify.md | sort | uniq -c |
      head -30 && grep -nE '^## |^### ' post_dafny_…)
  ⎿  Error: Exit code 1
     # 6b — Formal Verification (FDR4) — FAILED

     ## Summary
     FDR4 killed: memory usage exceeded 10240 MB limit (peak: 10308 MB)

     ## Run history
     - New this run: 1
     - Recurring from previous run: 0
     - Resolved since previous run: 0
     … +22 lines (ctrl+o to expand)

⏺ FDR4 shows an out-of-memory error (10.3 GB peak vs. 10 GB limit), with its fix
  directive pointing to raising a limit in pipeline.yaml rather than editing
  Java. I'll now read through the other two failing reports before deciding how
  to proceed.

⏺ Bash(grep -E 'Error:' post_dafny_verify.md | sed -E
      's/^.*\.dfy\(([0-9]+),[^)]*\): /line \1: /' | sort | uniq -c | sort -rn |
      head -20; grep -cE '^### Issue' post_da…)
  ⎿  (No output)

  Read 1 file (ctrl+o to expand)

⏺ I've stopped after iteration 1, as the run instructions require. The FDR4
  report tells me to change pipeline.yaml, which counts as an instrument change.
  I have not edited pipeline.yaml or anything else in the instrument, and I
  have not started iteration 2.

  Pre-flight output (no Java was present, so nothing was deleted):
  OK   pinned commit
  OK   no Java present
  OK   study is steam_boiler
  OK   only this study's inputs
  OK   no archive

  Iteration 1 result. Compile, coverage, profile check, all transformation
  phases, Dafny generation, Isabelle generation and vacuity all passed. FDR4,
  Dafny verification and Isabelle verification failed.

  FDR4 report, verbatim (forge.assets/corrections/post_fdr4.md):

  ▎ FDR4 killed: memory usage exceeded 10240 MB limit (peak: 10308 MB)
  ▎
  ▎ Fix directive: Do NOT edit Java or templates for this issue. Either raise
  ▎ phases.fdr4.memory_limit_mb in pipeline.yaml (machine permitting) and
  ▎ re-run, or record the assertion set as unmeasured under the memory budget.
  ▎ Sequence- or record-typed channel domains are the usual driver.

  The other two failures look fixable in my Java:
  - Isabelle: names of my guard predicates show up in the theory as untyped
    functions (transmissionFailed (), levelFailed (), boilerWaiting ()). The
    error is "ℝ is not a subtype of 𝔹". Each of these predicates is just a bare
    sensor call (for example boolean transmissionFailed =
    sensor.transmissionFailure();). I would put the sensor calls directly inside
    the combined predicates instead.
  - Dafny: 48 "Ambiguous use of && and ||" errors. The generated contracts seem
    to drop parentheses that are redundant in Java but that Dafny requires, as
    in a || (b && c). That is arguably a pipeline defect. I would work around it
    by rewriting the emergency-stop guards so that no || has an && operand (for
    example, !initSafe with initSafe a conjunction).

  Decision needed. The FDR4 memory could also be reduced in Java, against the
  report's "do not edit Java" line. The likely culprits are event channels the
  controller never uses but the model still declares. These are the record-typed
  PumpState(PumpReport) and PumpControlState(PumpControlReport) and the
  real-typed Level and Steam. Please choose one:
  1. Raise the FDR4 memory limit yourself; I then continue with iteration 2
     (Isabelle and Dafny fixes only).
  2. Let me continue with those fixes and also shrink the event channels in
     Java.
  3. End the run here.

  ITER_1_COMPLETE: 20 files edited