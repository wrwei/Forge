
❯ # Single cold pass — steam_boiler, pass 5

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
    `/Users/ranwei/Gitee/v3c16-staging/`, and the git history (`git log`, `git
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
  /Users/ranwei/Gitee/forge-v3c16-run5-steam_boiler
  ```

  `cd` there first. If it does not exist, **stop and say so**; no other
  worktree is a
  substitute. Export these four variables in your shell before running the
  runner:

  ```
  export FORGE_ARM_DIR=/Users/ranwei/Gitee/formal_method_guided_vibe_coding/exp
  eriments/v3-cold
  export FORGE_WT_PREFIX=forge-v3c16
  export FORGE_STAGE_DIR=/Users/ranwei/Gitee/v3c16-staging
  export FORGE_SHA=aaddf2225b38eb1eb749e816f6f5874d7ec93eaa
  ```

  Run this check before you write any Java, and paste its output. If any line
  reads
  FAIL, stop.

  ```
  cd /Users/ranwei/Gitee/forge-v3c16-run5-steam_boiler 2>/dev/null || { echo
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

  ## Run the pipeline once, then stop

  This is a **single cold pass**: you write the Java once, run the pipeline
  once, and
  stop, whatever the result.

  1. When the Java, the trace and the summary are written, run, **once**:
     `bash /Users/ranwei/Gitee/formal_method_guided_vibe_coding/scripts/run_cla
  ude_iteration.sh steam_boiler 5 1 <tokens|unavailable>`
  2. When it finishes, **stop**. Report the line it prints after
     `=== iteration 1 : what to do next ===`, verbatim. Do **not** open any
     `forge.assets/corrections/post_*.md` file, do **not** change the Java, and
  do
     **not** run the runner again: a pass whose Java changes after the runner
  starts,
     or that reads a report, is void.

  Then finish with the line `ITER_1_COMPLETE: <N> files written`.

⏺ No earlier pass appears in this conversation, so I'm treating the session as
  clean. First I'll run the pre-flight check.

⏺ Bash(cd /Users/ranwei/Gitee/forge-v3c16-run5-steam_boiler 2>/dev/null || {
      echo "FAIL no worktree"; exit 1; }
      export FORGE_ARM_DIR=/Users/ranwei/Gitee/formal_method_…)
  ⎿  (file diff unavailable for this command)

⏺ Pre-flight output:

  OK   pinned commit
  OK   no Java present
  OK   study is steam_boiler
  OK   only this study's inputs
  OK   no archive

  All checks pass. Next I'll read the permitted inputs.

  Read 2 files, listed 1 directory (ctrl+o to expand)

⏺ No system description exists for this study; the README says the requirements
  are the whole input. Next, the rules and prompts.

  Read 5 files (ctrl+o to expand)

⏺ Bash(cd /Users/ranwei/Gitee/forge-v3c16-run5-steam_boiler/forge.assets/vibe-co
      ding-prompts && for f in README.md 01_architecture.md 02_skeleton.md
      03_leaves.md 04_co…)
  ⎿  (file diff unavailable for this command)

⏺ Bash(cd /Users/ranwei/Gitee/forge-v3c16-run5-steam_boiler && cat
      java.generated.project/build.gradle java.generated.project/settings.gradle
      java.generated.project/gr…)
  ⎿  (No output)

⏺ I'll check the permitted Isabelle background in docs/corpus/ to see how
  Z-Machine deadlock freedom is framed, since that'll shape how I design
  terminal modes and guard coverage.

⏺ Bash(cd /Users/ranwei/Gitee/forge-v3c16-run5-steam_boiler/docs/corpus && cat
      README.md && find . -type f | head -50 && wc -c $(find . -type f) | tail
      -1)
  ⎿  (No output)

⏺ Bash(grep -n -i "deadlock" zmachine-framework/Z_Machine.thy | head -30; echo
      ----; sed -n 1,200p zmachine-framework/Z_Machine.thy | grep -n -i -A3
      "deadlock_free\|de…)
  ⎿  (No output)

⏺ Bash(sed -n 83,106p zmachine-framework/Z_Machine.thy; grep -n "until"
      zmachine-examples/*.thy | head)
  ⎿  (No output)

⏺ Layer 1 — architecture (no code yet).

  Packages and classes
  - steam_boiler.annotation: RoboChartType.
  - steam_boiler.domain: the domain types. ModeIndicator is the five-value
    payload of MODE(m) (SB-DM10). FlowObservation is FLOW / NO_FLOW (SB-DM4).
    The records PumpStateReport and PumpControlReport carry the (n, b) payloads
    (SB-DM3/DM4/DM9).
  - steam_boiler.event: InputEvent, a sealed interface with one record per
    incoming message plus TransmissionReceived, the trigger for the per-cycle
    analysis stage (SB-V3). OutputEvent, a sealed interface with one record per
    outgoing message (SB-DM8).
  - steam_boiler.BoilerConstants: C, M1, M2, N1, N2, W, U1, U2, P, the pump
    count, the cycle length and the stop limit (SB-DM1–DM3).
  - steam_boiler.controller: BoilerMode (the five modes, INITIALIZATION first)
    and SteamBoilerController, which holds the mode, the sensor and the actuator
    and has step(InputEvent).
  - steam_boiler.sensor: BoilerSensor, which buffers the cycle's messages and
    does the analysis stage: failure detection (FR1–FR5), stop count (V10), q, v
    and p, the estimate bounds (V8) and the worst-case forecast. The
    package-private UnitStatusBoard keeps the per-unit status (V9) and builds
    the failure-announcement and repair-acknowledgement messages (FR6/FR7).
  - steam_boiler.actuator: BoilerActuator.apply(OutputEvent) collects the
    cycle's outgoing messages.
  - steam_boiler.SteamBoilerProgram: the cycle driver (receive → analyse →
    transmit).

  Key choices
  - Guards and events: guards use only sensor calls and constants, so the
    controller's only state is its mode.
  - No autonomous transitions: every transition fires on TransmissionReceived,
    which rules out divergence.
  - Mode coverage: every non-terminal mode has a catch-all branch, and every
    guard conjoins the negations of the guards above it.
  - Emergency stop: EMERGENCY_STOP is terminal.
  - Mode changes: a cycle that changes mode leaves the pumps as they are.
    Regulation changes at most one pump per cycle.

  Every requirement has a place in this design. Layer 2 next: the skeleton.

⏺ Bash(cd
      /Users/ranwei/Gitee/forge-v3c16-run5-steam_boiler/java.generated.project
      && mkdir -p
      src/main/java/steam_boiler/{annotation,domain,event,controller,sensor,ac…)
  ⎿  (No output)

⏺ Write(src/main/java/steam_boiler/annotation/RoboChartType.java)
  ⎿  Wrote 14 lines to
     src/main/java/steam_boiler/annotation/RoboChartType.java
       1 package steam_boiler.annotation;
       2
       3 import java.lang.annotation.ElementType;
       4 import java.lang.annotation.Retention;
       5 import java.lang.annotation.RetentionPolicy;
       6 import java.lang.annotation.Target;
       7
       8 /** Declares the RoboChart type ("nat" or "real") a Java field,
         parameter or return value stands for. */
       9 @Retention(RetentionPolicy.SOURCE)
     … +5 lines (ctrl+o to expand)

⏺ Write(src/main/java/steam_boiler/domain/ModeIndicator.java)
  ⎿  Wrote 12 lines to src/main/java/steam_boiler/domain/ModeIndicator.java
       1 package steam_boiler.domain;
       2
       3 /**
       4  * The operating mode as carried by the MODE(m) message: exactly
          five alternatives.
       5  */
       6 public enum ModeIndicator {
       7     INITIALIZATION_MODE,
       8     NORMAL_MODE,
       9     DEGRADED_MODE,
     … +3 lines (ctrl+o to expand)

⏺ Write(src/main/java/steam_boiler/domain/FlowObservation.java)
  ⎿  Wrote 9 lines to
     src/main/java/steam_boiler/domain/FlowObservation.java
      1 package steam_boiler.domain;
      2
      3 /**
      4  * What a pump controller reports: water flows from its pump into
         the boiler, or it does not.
      5  */
      6 public enum FlowObservation {
      7     FLOW,
      8     NO_FLOW
      9 }

⏺ Write(src/main/java/steam_boiler/domain/PumpStateReport.java)
  ⎿  Wrote 9 lines to
     src/main/java/steam_boiler/domain/PumpStateReport.java
      1 package steam_boiler.domain;
      2
      3 import steam_boiler.annotation.RoboChartType;
      4
      5 /**
      6  * Payload of PUMP_STATE(n, b): pump {@code reportedPump} (1..4)
        is on ({@code pumpOn}) or off.
      7  */
      8 public record PumpStateReport(@RoboChartType("nat") int reportedP
        ump, boolean pumpOn) {
     … +1 line (ctrl+o to expand)

⏺ Write(src/main/java/steam_boiler/domain/PumpControlReport.java)
  ⎿  Wrote 10 lines to
     src/main/java/steam_boiler/domain/PumpControlReport.java
       1 package steam_boiler.domain;
       2
       3 import steam_boiler.annotation.RoboChartType;
       4
       5 /**
       6  * Payload of PUMP_CONTROL_STATE(n, b): the controller of pump {
         @code controlledPump} (1..4)
       7  * observes {@code observation}.
       8  */
       9 public record PumpControlReport(@RoboChartType("nat") int contro
     … +2 lines (ctrl+o to expand)

⏺ Write(src/main/java/steam_boiler/event/InputEvent.java)
  ⎿  Wrote 61 lines to src/main/java/steam_boiler/event/InputEvent.java
       1 package steam_boiler.event;
       2
       3 import steam_boiler.annotation.RoboChartType;
       4 import steam_boiler.domain.PumpControlReport;
       5 import steam_boiler.domain.PumpStateReport;
       6
       7 /**
       8  * Messages the program receives from the physical units, one re
         cord per message, plus the
       9  * per-cycle trigger {@link TransmissionReceived} that starts th
     … +53 lines (~2 KB) (ctrl+o to expand)

⏺ Write(src/main/java/steam_boiler/event/OutputEvent.java)
  ⎿  Wrote 50 lines to src/main/java/steam_boiler/event/OutputEvent.java
       1 package steam_boiler.event;
       2
       3 import steam_boiler.annotation.RoboChartType;
       4 import steam_boiler.domain.ModeIndicator;
       5
       6 /**
       7  * Messages the program sends to the physical units, one record
         per message. Pumps are
       8  * numbered 1 to 4.
       9  */
     … +41 lines (ctrl+o to expand)

⏺ Write(src/main/java/steam_boiler/BoilerConstants.java)
  ⎿  Wrote 51 lines to src/main/java/steam_boiler/BoilerConstants.java
       1 package steam_boiler;
       2
       3 import steam_boiler.annotation.RoboChartType;
       4
       5 /**
       6  * Constants of the boiler, the steam exit, the pumps and the co
         ntrol cycle. The requirements
       7  * name these quantities but give no values; the values below ar
         e chosen (see post_codegen.md)
       8  * so that M1 &lt; N1 &lt; N2 &lt; M2 &lt; C and the four pumps
     … +44 lines (~2 KB) (ctrl+o to expand)
⏺ Write(src/main/java/steam_boiler/controller/BoilerMode.java)
  ⎿  Wrote 12 lines to
     src/main/java/steam_boiler/controller/BoilerMode.java
       1 package steam_boiler.controller;
       2
       3 /**
       4  * The five operating modes of the program; it starts in initial
         ization.
       5  */
       6 public enum BoilerMode {
       7     INITIALIZATION,
       8     NORMAL,
       9     DEGRADED,
     … +3 lines (ctrl+o to expand)

⏺ Next come the skeleton stubs for the actuator, sensor, controller and driver,
  with signatures only.

⏺ Write(src/main/java/steam_boiler/actuator/BoilerActuator.java)
  ⎿  Wrote 24 lines to
     src/main/java/steam_boiler/actuator/BoilerActuator.java
       1 package steam_boiler.actuator;
       2
       3 import java.util.ArrayList;
       4 import java.util.List;
       5 import steam_boiler.event.OutputEvent;
       6
       7 /**
       8  * Receives the controller's outgoing messages for the current c
         ycle.
       9  */
     … +15 lines (ctrl+o to expand)

⏺ Write(src/main/java/steam_boiler/sensor/BoilerSensor.java)
  ⎿  Wrote 146 lines to src/main/java/steam_boiler/sensor/BoilerSensor.java
       1 package steam_boiler.sensor;
       2
       3 import java.util.ArrayList;
       4 import java.util.List;
       5 import steam_boiler.annotation.RoboChartType;
       6 import steam_boiler.event.InputEvent;
       7 import steam_boiler.event.OutputEvent;
       8
       9 /**
      10  * The program's view of the physical units (skeleton).
     … +136 lines (ctrl+o to expand)

⏺ Write(src/main/java/steam_boiler/controller/SteamBoilerController.java)
  ⎿  Wrote 30 lines to
     src/main/java/steam_boiler/controller/SteamBoilerController.java
       1 package steam_boiler.controller;
       2
       3 import steam_boiler.actuator.BoilerActuator;
       4 import steam_boiler.event.InputEvent;
       5 import steam_boiler.sensor.BoilerSensor;
       6
       7 /**
       8  * Mode logic of the steam-boiler control program (skeleton).
       9  */
      10 public final class SteamBoilerController {
     … +20 lines (ctrl+o to expand)

⏺ Write(src/main/java/steam_boiler/SteamBoilerProgram.java)
  ⎿  Wrote 30 lines to src/main/java/steam_boiler/SteamBoilerProgram.java
       1 package steam_boiler;
       2
       3 import java.util.ArrayList;
       4 import java.util.List;
       5 import steam_boiler.actuator.BoilerActuator;
       6 import steam_boiler.controller.BoilerMode;
       7 import steam_boiler.controller.SteamBoilerController;
       8 import steam_boiler.event.InputEvent;
       9 import steam_boiler.event.OutputEvent;
      10 import steam_boiler.sensor.BoilerSensor;
     … +20 lines (ctrl+o to expand)

⏺ Bash(OUT=/private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3c16-run5-steam-bo
      iler/3a122a7e-8db3-44c1-a904-27351f4e1506/scratchpad/classes && rm -rf
      "$OUT" && mkdir…)
  ⎿  (No output)

⏺ The skeleton compiles, so now I'll write a trace generator in the scratchpad
  that maps requirements to elements, verifies each named element exists, and
  flags any untraced public elements or files—re-running it at each layer.

  Made 1 scratchpad edit +175 (ctrl+o to expand)

⏺ Bash(python3 -I /private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3c16-run5-s
      team-boiler/3a122a7e-8db3-44c1-a904-27351f4e1506/scratchpad/gen_trace.py)
  ⎿  (No output)

⏺ The remaining problems are all elements Layer 3 adds: the sensor's variable
  fields and UnitStatusBoard. Layer 2 is done.

  Layer 3, the leaves. First the unit-status board (SB-V9, FR6, FR7).

⏺ Write(src/main/java/steam_boiler/sensor/UnitStatusBoard.java)
  ⎿  Wrote 124 lines to
     src/main/java/steam_boiler/sensor/UnitStatusBoard.java
       1 package steam_boiler.sensor;
       2
       3 import java.util.ArrayList;
       4 import java.util.List;
       5 import steam_boiler.BoilerConstants;
       6 import steam_boiler.event.OutputEvent;
       7
       8 /**
       9  * Status of every physical unit: out of order or sound, and whe
         ther the failure detection
     … +115 lines (ctrl+o to expand)

⏺ Now the full BoilerSensor, in one file, written in plain imperative Java:
  reception, the analysis stage, recording what was sent, and the query methods.

⏺ Write(src/main/java/steam_boiler/sensor/BoilerSensor.java)
  ⎿  Added 559 lines, removed 36 lines
        1  package steam_boiler.sensor;
        2  
        3 -import java.util.ArrayList;                                   
        3  import java.util.List;
        4 +import steam_boiler.BoilerConstants;                          
        5  import steam_boiler.annotation.RoboChartType;
        6 +import steam_boiler.domain.FlowObservation;                   
        7 +import steam_boiler.domain.ModeIndicator;                     
        8  import steam_boiler.event.InputEvent;
        9  import steam_boiler.event.OutputEvent;
       10  
       11  /**
       10 - * The program's view of the physical units (skeleton).       
       12 + * The program's view of the physical units. In the reception 
          +stage of a cycle it buffers the                               
       13 + * incoming transmission; in the analysis stage ({@link #analy
          +se()}) it judges unit and                                     
       14 + * transmission failures, keeps the unit status, the STOP coun
          +t, q, v, p and the level                                      
       15 + * estimate, and forecasts the worst-case level at the end of 
          +the coming cycle. It also                                     
       16 + * observes the outgoing transmission ({@link #recordSent(Outp
          +utEvent)}), which it needs to                                 
       17 + * judge the pumps and pump controllers. Pumps are numbered 1 
          +to 4.                                                         
       18   */
       19  public final class BoilerSensor {
       20  
       14 -    /** Starts the reception stage of a new cycle. */         
       21 +    private static final int PUMPS = BoilerConstants.PUMP_COUN
          +T;                                                            
       22 +    private static final double DT = BoilerConstants.CYCLE_SEC
          +ONDS;                                                         
       23 +                                                              
       24 +    // ---- contents of the current incoming transmission ----
       25 +    private boolean stopPresent;                              
       26 +    private boolean waitingPresent;                           
       27 +    private boolean unitsReadyPresent;                        
       28 +    @RoboChartType("nat") private int levelMessages;          
       29 +    @RoboChartType("nat") private int steamMessages;          
       30 +    @RoboChartType("real") private double receivedLevel;      
       31 +    @RoboChartType("real") private double receivedSteam;      
       32 +    private final int[] pumpStateMessages = new int[PUMPS];   
       33 +    private final boolean[] receivedPumpOn = new boolean[PUMPS
          +];                                                            
       34 +    private final int[] pumpControlMessages = new int[PUMPS]; 
       35 +    private final boolean[] receivedFlow = new boolean[PUMPS];
       36 +    private final boolean[] pumpRepairedPresent = new boolean[
          +PUMPS];                                                       
       37 +    private final boolean[] pumpControlRepairedPresent = new b
          +oolean[PUMPS];                                                
       38 +    private final boolean[] pumpFailureAckPresent = new boolea
          +n[PUMPS];                                                     
       39 +    private final boolean[] pumpControlFailureAckPresent = new
          + boolean[PUMPS];                                              
       40 +    private boolean levelRepairedPresent;                     
       41 +    private boolean steamRepairedPresent;                     
       42 +    private boolean levelFailureAckPresent;                   
       43 +    private boolean steamFailureAckPresent;                   
       44 +    private boolean unknownPumpReferenced;                    
       45 +                                                              
       46 +    // ---- knowledge kept across cycles ----                 
       47 +    private final UnitStatusBoard units = new UnitStatusBoard(
          +);                                                            
       48 +    @RoboChartType("nat") private int stopCount;              
       49 +    private boolean boilerWaitingLatched;                     
       50 +    private boolean initializationLeft;                       
       51 +    private boolean programReadySent;                         
       52 +    private boolean valveIsOpen;                              
       53 +    private boolean previousCycleAnalysed;                    
       54 +    private boolean previousSteamKnown;                       
       55 +    @RoboChartType("real") private double previousSteam;      
       56 +    private final boolean[] previousPumpOn = new boolean[PUMPS
          +];                                                            
       57 +    private final boolean[] previousFlow = new boolean[PUMPS];
       58 +    private final boolean[] orderedThisCycle = new boolean[PUM
          +PS];                                                          
       59 +    private final boolean[] orderedOnThisCycle = new boolean[P
          +UMPS];                                                        
       60 +    private final boolean[] orderedLastCycle = new boolean[PUM
          +PS];                                                          
       61 +    private final boolean[] orderedOnLastCycle = new boolean[P
          +UMPS];                                                        
       62 +    private final boolean[] orderedTwoCyclesAgo = new boolean[
          +PUMPS];                                                       
       63 +    private final boolean[] orderedOnTwoCyclesAgo = new boolea
          +n[PUMPS];                                                     
       64 +    private final boolean[] pouringSurely = new boolean[PUMPS]
          +;                                                             
       65 +    private final boolean[] pouringPossibly = new boolean[PUMP
          +S];                                                           
       66 +    @RoboChartType("real") private double steamOutMin;        
       67 +    @RoboChartType("real") private double steamOutMax;        
       68 +    private boolean levelKnown;                               
       69 +                                                              
       70 +    // ---- results of the analysis stage ----                
       71 +    private boolean transmissionFailed;                       
       72 +    @RoboChartType("real") private double waterLevelQ;        
       73 +    @RoboChartType("real") private double steamRateV;         
       74 +    @RoboChartType("real") private double pumpThroughputP;    
       75 +    @RoboChartType("real") private double estimatedLow;       
       76 +    @RoboChartType("real") private double estimatedHigh = Boil
          +erConstants.CAPACITY_C;                                       
       77 +    @RoboChartType("real") private double forecastMin;        
       78 +    @RoboChartType("real") private double forecastMax = Boiler
          +Constants.CAPACITY_C;                                         
       79 +                                                              
       80 +    // =======================================================
          +==============================                                
       81 +    // Reception stage                                        
       82 +    // =======================================================
          +==============================                                
       83 +                                                              
       84 +    /** Starts the reception stage of a new cycle: the previou
          +s transmission is forgotten. */                               
       85      public void beginCycle() {
       86 +        stopPresent = false;                                  
       87 +        waitingPresent = false;                               
       88 +        unitsReadyPresent = false;                            
       89 +        levelMessages = 0;                                    
       90 +        steamMessages = 0;                                    
       91 +        levelRepairedPresent = false;                         
       92 +        steamRepairedPresent = false;                         
       93 +        levelFailureAckPresent = false;                       
       94 +        steamFailureAckPresent = false;                       
       95 +        unknownPumpReferenced = false;                        
       96 +        for (int i = 0; i < PUMPS; i++) {                     
       97 +            pumpStateMessages[i] = 0;                         
       98 +            pumpControlMessages[i] = 0;                       
       99 +            pumpRepairedPresent[i] = false;                   
      100 +            pumpControlRepairedPresent[i] = false;            
      101 +            pumpFailureAckPresent[i] = false;                 
      102 +            pumpControlFailureAckPresent[i] = false;          
      103 +            orderedTwoCyclesAgo[i] = orderedLastCycle[i];     
      104 +            orderedOnTwoCyclesAgo[i] = orderedOnLastCycle[i]; 
      105 +            orderedLastCycle[i] = orderedThisCycle[i];        
      106 +            orderedOnLastCycle[i] = orderedOnThisCycle[i];    
      107 +            orderedThisCycle[i] = false;                      
      108 +            orderedOnThisCycle[i] = false;                    
      109 +        }                                                     
      110 +        units.beginCycle();                                   
      111      }
      112  
      113      /** Records one incoming message of the current transmissi
           on. */
      114      public void receive(InputEvent message) {
      115 +        if (message instanceof InputEvent.StopSignal) {       
      116 +            stopPresent = true;                               
      117 +        } else if (message instanceof InputEvent.SteamBoilerWa
          +iting) {                                                      
      118 +            waitingPresent = true;                            
      119 +        } else if (message instanceof InputEvent.PhysicalUnits
          +Ready) {                                                      
      120 +            unitsReadyPresent = true;                         
      121 +        } else if (message instanceof InputEvent.PumpState) { 
      122 +            InputEvent.PumpState ps = (InputEvent.PumpState) m
          +essage;                                                       
      123 +            int n = ps.report().reportedPump();               
      124 +            if (validPump(n)) {                               
      125 +                pumpStateMessages[n - 1] = pumpStateMessages[n
          + - 1] + 1;                                                    
      126 +                receivedPumpOn[n - 1] = ps.report().pumpOn(); 
      127 +            }                                                 
      128 +        } else if (message instanceof InputEvent.PumpControlSt
          +ate) {                                                        
      129 +            InputEvent.PumpControlState pcs = (InputEvent.Pump
          +ControlState) message;                                        
      130 +            int n = pcs.report().controlledPump();            
      131 +            if (validPump(n)) {                               
      132 +                pumpControlMessages[n - 1] = pumpControlMessag
          +es[n - 1] + 1;                                                
      133 +                receivedFlow[n - 1] = pcs.report().observation
          +() == FlowObservation.FLOW;                                   
      134 +            }                                                 
      135 +        } else if (message instanceof InputEvent.Level) {     
      136 +            InputEvent.Level lv = (InputEvent.Level) message; 
      137 +            levelMessages = levelMessages + 1;                
      138 +            receivedLevel = lv.litres();                      
      139 +        } else if (message instanceof InputEvent.Steam) {     
      140 +            InputEvent.Steam st = (InputEvent.Steam) message; 
      141 +            steamMessages = steamMessages + 1;                
      142 +            receivedSteam = st.litresPerSecond();             
      143 +        } else if (message instanceof InputEvent.PumpRepaired)
          + {                                                            
      144 +            InputEvent.PumpRepaired pr = (InputEvent.PumpRepai
          +red) message;                                                 
      145 +            if (validPump(pr.pumpNumber())) {                 
      146 +                pumpRepairedPresent[pr.pumpNumber() - 1] = tru
          +e;                                                            
      147 +            }                                                 
      148 +        } else if (message instanceof InputEvent.PumpControlRe
          +paired) {                                                     
      149 +            InputEvent.PumpControlRepaired pcr = (InputEvent.P
          +umpControlRepaired) message;                                  
      150 +            if (validPump(pcr.pumpNumber())) {                
      151 +                pumpControlRepairedPresent[pcr.pumpNumber() - 
          +1] = true;                                                    
      152 +            }                                                 
      153 +        } else if (message instanceof InputEvent.LevelRepaired
          +) {                                                           
      154 +            levelRepairedPresent = true;                      
      155 +        } else if (message instanceof InputEvent.SteamRepaired
          +) {                                                           
      156 +            steamRepairedPresent = true;                      
      157 +        } else if (message instanceof InputEvent.PumpFailureAc
          +knowledgement) {                                              
      158 +            InputEvent.PumpFailureAcknowledgement pa = (InputE
          +vent.PumpFailureAcknowledgement) message;                     
      159 +            if (validPump(pa.pumpNumber())) {                 
      160 +                pumpFailureAckPresent[pa.pumpNumber() - 1] = t
          +rue;                                                          
      161 +            }                                                 
      162 +        } else if (message instanceof InputEvent.PumpControlFa
          +ilureAcknowledgement) {                                       
      163 +            InputEvent.PumpControlFailureAcknowledgement pca =
      164 +                    (InputEvent.PumpControlFailureAcknowledgem
          +ent) message;                                                 
      165 +            if (validPump(pca.pumpNumber())) {                
      166 +                pumpControlFailureAckPresent[pca.pumpNumber() 
          +- 1] = true;                                                  
      167 +            }                                                 
      168 +        } else if (message instanceof InputEvent.LevelFailureA
          +cknowledgement) {                                             
      169 +            levelFailureAckPresent = true;                    
      170 +        } else if (message instanceof InputEvent.SteamOutcomeF
          +ailureAcknowledgement) {                                      
      171 +            steamFailureAckPresent = true;                    
      172 +        }                                                     
      173      }
      174  
      175 +    private boolean validPump(int n) {                        
      176 +        boolean valid = n >= 1 && n <= PUMPS;                 
      177 +        if (!valid) {                                         
      178 +            unknownPumpReferenced = true;                     
      179 +        }                                                     
      180 +        return valid;                                         
      181 +    }                                                         
      182 +                                                              
      183 +    // =======================================================
          +==============================                                
      184 +    // Analysis stage                                         
      185 +    // =======================================================
          +==============================                                
      186 +                                                              
      187      /** Analysis stage: judges failures and updates every deri
           ved quantity. */
      188      public void analyse() {
      189 +        transmissionFailed = transmissionMissing() || transmis
          +sionAberrant();                                               
      190 +        if (stopPresent) {                                    
      191 +            if (stopCount < BoilerConstants.STOP_LIMIT) {     
      192 +                stopCount = stopCount + 1;                    
      193 +            }                                                 
      194 +        } else {                                              
      195 +            stopCount = 0;                                    
      196 +        }                                                     
      197 +        if (waitingPresent && !initializationLeft) {          
      198 +            boilerWaitingLatched = true;                      
      199 +        }                                                     
      200 +        if (transmissionFailed) {                             
      201 +            // The controller goes to emergency stop; the read
          +ings of this transmission are                                 
      202 +            // not trusted and not used.                      
      203 +            return;                                           
      204 +        }                                                     
      205 +        processAcknowledgementsAndRepairs();                  
      206 +        double expectedLow = expectedLevelLow();              
      207 +        double expectedHigh = expectedLevelHigh();            
      208 +        detectSteamFailure();                                 
      209 +        detectPumpFailures();                                 
      210 +        detectPumpControlFailures();                          
      211 +        detectLevelFailure(expectedLow, expectedHigh);        
      212 +        updateLevelKnowledge(expectedLow, expectedHigh);      
      213 +        updateSteamOutflow();                                 
      214 +        updatePumpInflow();                                   
      215 +        forecastMin = estimatedLow - steamOutMax + countOf(pou
          +ringSurely) * BoilerConstants.PUMP_CAPACITY_P * DT;           
      216 +        forecastMax = estimatedHigh - steamOutMin + countOf(po
          +uringPossibly) * BoilerConstants.PUMP_CAPACITY_P * DT;        
      217 +        previousSteam = receivedSteam;                        
      218 +        previousSteamKnown = !units.outOfOrder(UnitStatusBoard
          +.STEAM_UNIT);                                                 
      219 +        for (int i = 0; i < PUMPS; i++) {                     
      220 +            previousPumpOn[i] = receivedPumpOn[i];            
      221 +            previousFlow[i] = receivedFlow[i];                
      222 +        }                                                     
      223 +        previousCycleAnalysed = true;                         
      224      }
      225  
      226 +    /** An indispensable message (a data message of some unit)
          + is missing. */                                               
      227 +    private boolean transmissionMissing() {                   
      228 +        boolean missing = levelMessages == 0 || steamMessages 
          +== 0;                                                         
      229 +        for (int i = 0; i < PUMPS; i++) {                     
      230 +            if (pumpStateMessages[i] == 0 || pumpControlMessag
          +es[i] == 0) {                                                 
      231 +                missing = true;                               
      232 +            }                                                 
      233 +        }                                                     
      234 +        return missing;                                       
      235 +    }                                                         
      236 +                                                              
      237 +    /** A message is present whose presence is aberrant. */   
      238 +    private boolean transmissionAberrant() {                  
      239 +        boolean aberrant = levelMessages > 1 || steamMessages 
          +> 1 || unknownPumpReferenced;                                 
      240 +        if (waitingPresent && initializationLeft) {           
      241 +            aberrant = true;                                  
      242 +        }                                                     
      243 +        if (unitsReadyPresent && (initializationLeft || !progr
          +amReadySent)) {                                               
      244 +            aberrant = true;                                  
      245 +        }                                                     
      246 +        for (int i = 0; i < PUMPS; i++) {                     
      247 +            int pump = UnitStatusBoard.pumpUnit(i);           
      248 +            int control = UnitStatusBoard.pumpControlUnit(i); 
      249 +            if (pumpStateMessages[i] > 1 || pumpControlMessage
          +s[i] > 1) {                                                   
      250 +                aberrant = true;                              
      251 +            }                                                 
      252 +            if (pumpRepairedPresent[i] && !units.outOfOrder(pu
          +mp)) {                                                        
      253 +                aberrant = true;                              
      254 +            }                                                 
      255 +            if (pumpControlRepairedPresent[i] && !units.outOfO
          +rder(control)) {                                              
      256 +                aberrant = true;                              
      257 +            }                                                 
      258 +            if (pumpFailureAckPresent[i] && !units.awaitingAck
          +nowledgement(pump)) {                                         
      259 +                aberrant = true;                              
      260 +            }                                                 
      261 +            if (pumpControlFailureAckPresent[i] && !units.awai
          +tingAcknowledgement(control)) {                               
      262 +                aberrant = true;                              
      263 +            }                                                 
      264 +        }                                                     
      265 +        if (levelRepairedPresent && !units.outOfOrder(UnitStat
          +usBoard.LEVEL_UNIT)) {                                        
      266 +            aberrant = true;                                  
      267 +        }                                                     
      268 +        if (steamRepairedPresent && !units.outOfOrder(UnitStat
          +usBoard.STEAM_UNIT)) {                                        
      269 +            aberrant = true;                                  
      270 +        }                                                     
      271 +        if (levelFailureAckPresent && !units.awaitingAcknowled
          +gement(UnitStatusBoard.LEVEL_UNIT)) {                         
      272 +            aberrant = true;                                  
      273 +        }                                                     
      274 +        if (steamFailureAckPresent && !units.awaitingAcknowled
          +gement(UnitStatusBoard.STEAM_UNIT)) {                         
      275 +            aberrant = true;                                  
      276 +        }                                                     
      277 +        return aberrant;                                      
      278 +    }                                                         
      279 +                                                              
      280 +    private void processAcknowledgementsAndRepairs() {        
      281 +        for (int i = 0; i < PUMPS; i++) {                     
      282 +            if (pumpFailureAckPresent[i]) {                   
      283 +                units.acknowledgeFailure(UnitStatusBoard.pumpU
          +nit(i));                                                      
      284 +            }                                                 
      285 +            if (pumpControlFailureAckPresent[i]) {            
      286 +                units.acknowledgeFailure(UnitStatusBoard.pumpC
          +ontrolUnit(i));                                               
      287 +            }                                                 
      288 +            if (pumpRepairedPresent[i]) {                     
      289 +                units.repair(UnitStatusBoard.pumpUnit(i));    
      290 +            }                                                 
      291 +            if (pumpControlRepairedPresent[i]) {              
      292 +                units.repair(UnitStatusBoard.pumpControlUnit(i
          +));                                                           
      293 +            }                                                 
      294 +        }                                                     
      295 +        if (levelFailureAckPresent) {                         
      296 +            units.acknowledgeFailure(UnitStatusBoard.LEVEL_UNI
          +T);                                                           
      297 +        }                                                     
      298 +        if (steamFailureAckPresent) {                         
      299 +            units.acknowledgeFailure(UnitStatusBoard.STEAM_UNI
          +T);                                                           
      300 +        }                                                     
      301 +        if (levelRepairedPresent) {                           
      302 +            units.repair(UnitStatusBoard.LEVEL_UNIT);         
      303 +        }                                                     
      304 +        if (steamRepairedPresent) {                           
      305 +            units.repair(UnitStatusBoard.STEAM_UNIT);         
      306 +        }                                                     
      307 +    }                                                         
      308 +                                                              
      309 +    /**                                                       
      310 +     * Lowest level reachable now from what was known one cycl
          +e ago, under worst-case steam                                 
      311 +     * (rising at U1) and the pumps surely pouring during the 
          +last cycle.                                                   
      312 +     */                                                       
      313 +    private double expectedLevelLow() {                       
      314 +        if (!levelKnown || valveIsOpen) {                     
      315 +            // No earlier knowledge, or the evacuation valve d
          +rained an unknown quantity.                                   
      316 +            return 0.0;                                       
      317 +        }                                                     
      318 +        return clamp(estimatedLow - steamOutMax + countOf(pour
          +ingSurely) * BoilerConstants.PUMP_CAPACITY_P * DT);           
      319 +    }                                                         
      320 +                                                              
      321 +    /**                                                       
      322 +     * Highest level reachable now from what was known one cyc
          +le ago, under worst-case steam                                
      323 +     * (falling at U2) and every pump possibly pouring during 
          +the last cycle.                                               
      324 +     */                                                       
      325 +    private double expectedLevelHigh() {                      
      326 +        if (!levelKnown) {                                    
      327 +            return BoilerConstants.CAPACITY_C;                
      328 +        }                                                     
      329 +        return clamp(estimatedHigh - steamOutMin + countOf(pou
          +ringPossibly) * BoilerConstants.PUMP_CAPACITY_P * DT);        
      330 +    }                                                         
      331 +                                                              
      332 +    /** SB-FR4 (and SB-Beh3: in initialization the steam rate 
          +must be zero). */                                             
      333 +    private void detectSteamFailure() {                       
      334 +        int unit = UnitStatusBoard.STEAM_UNIT;                
      335 +        if (units.outOfOrder(unit)) {                         
      336 +            return;                                           
      337 +        }                                                     
      338 +        boolean failed = receivedSteam < 0.0 || receivedSteam 
          +> BoilerConstants.MAX_STEAM_W;                                
      339 +        if (previousSteamKnown && previousCycleAnalysed && !un
          +its.repairedThisCycle(unit)) {                                
      340 +            if (receivedSteam > previousSteam + BoilerConstant
          +s.STEAM_INCREASE_U1 * DT                                      
      341 +                    || receivedSteam < previousSteam - BoilerC
          +onstants.STEAM_DECREASE_U2 * DT) {                            
      342 +                failed = true;                                
      343 +            }                                                 
      344 +        }                                                     
      345 +        if (!initializationLeft && boilerWaitingLatched && rec
          +eivedSteam != 0.0) {                                          
      346 +            failed = true;                                    
      347 +        }                                                     
      348 +        if (failed) {                                         
      349 +            units.detectFailure(unit);                        
      350 +        }                                                     
      351 +    }                                                         
      352 +                                                              
      353 +    /**                                                       
      354 +     * SB-FR1: after an order sent in the previous cycle the p
          +ump must show the ordered state;                              
      355 +     * without an order it must not change state.             
      356 +     */                                                       
      357 +    private void detectPumpFailures() {                       
      358 +        for (int i = 0; i < PUMPS; i++) {                     
      359 +            int unit = UnitStatusBoard.pumpUnit(i);           
      360 +            if (previousCycleAnalysed && !units.outOfOrder(uni
          +t) && !units.repairedThisCycle(unit)) {                       
      361 +                boolean expectedOn = previousPumpOn[i];       
      362 +                if (orderedLastCycle[i]) {                    
      363 +                    expectedOn = orderedOnLastCycle[i];       
      364 +                }                                             
      365 +                if (receivedPumpOn[i] != expectedOn) {        
      366 +                    units.detectFailure(unit);                
      367 +                }                                             
      368 +            }                                                 
      369 +        }                                                     
      370 +    }                                                         
      371 +                                                              
      372 +    /**                                                       
      373 +     * SB-FR2: judged only while the pump itself is known to w
          +ork correctly. In the second                                  
      374 +     * transmission after an order the controller must show th
          +e coming or ceasing of flow;                                  
      375 +     * the first transmission after an order is a transition a
          +nd is not judged; without a                                   
      376 +     * recent order the observation must not change.          
      377 +     */                                                       
      378 +    private void detectPumpControlFailures() {                
      379 +        for (int i = 0; i < PUMPS; i++) {                     
      380 +            int unit = UnitStatusBoard.pumpControlUnit(i);    
      381 +            int pump = UnitStatusBoard.pumpUnit(i);           
      382 +            boolean judged = previousCycleAnalysed && !units.o
          +utOfOrder(unit) && !units.repairedThisCycle(unit)             
      383 +                    && !units.outOfOrder(pump) && !units.repai
          +redThisCycle(pump) && !orderedLastCycle[i];                   
      384 +            if (judged) {                                     
      385 +                boolean expectedFlow = previousFlow[i];       
      386 +                if (orderedTwoCyclesAgo[i]) {                 
      387 +                    expectedFlow = orderedOnTwoCyclesAgo[i];  
      388 +                }                                             
      389 +                if (receivedFlow[i] != expectedFlow) {        
      390 +                    units.detectFailure(unit);                
      391 +                }                                             
      392 +            }                                                 
      393 +        }                                                     
      394 +    }                                                         
      395 +                                                              
      396 +    /** SB-FR3: outside [0, C], or outside the range reachable
          + from the previous cycle. */                                  
      397 +    private void detectLevelFailure(double expectedLow, double
          + expectedHigh) {                                              
      398 +        int unit = UnitStatusBoard.LEVEL_UNIT;                
      399 +        if (units.outOfOrder(unit)) {                         
      400 +            return;                                           
      401 +        }                                                     
      402 +        boolean failed = receivedLevel < 0.0 || receivedLevel 
          +> BoilerConstants.CAPACITY_C;                                 
      403 +        if (levelKnown && !units.repairedThisCycle(unit)      
      404 +                && (receivedLevel < expectedLow || receivedLev
          +el > expectedHigh)) {                                         
      405 +            failed = true;                                    
      406 +        }                                                     
      407 +        if (failed) {                                         
      408 +            units.detectFailure(unit);                        
      409 +        }                                                     
      410 +    }                                                         
      411 +                                                              
      412 +    /** q, and the level bounds: the reading itself, or the es
          +timate while the device is out of order. */                   
      413 +    private void updateLevelKnowledge(double expectedLow, doub
          +le expectedHigh) {                                            
      414 +        waterLevelQ = receivedLevel;                          
      415 +        if (units.outOfOrder(UnitStatusBoard.LEVEL_UNIT)) {   
      416 +            estimatedLow = expectedLow;                       
      417 +            estimatedHigh = expectedHigh;                     
      418 +        } else {                                              
      419 +            estimatedLow = receivedLevel;                     
      420 +            estimatedHigh = receivedLevel;                    
      421 +        }                                                     
      422 +        levelKnown = true;                                    
      423 +    }                                                         
      424 +                                                              
      425 +    /** v, and the steam volume that can leave during the comi
          +ng cycle (bounded by W, U1, U2). */                           
      426 +    private void updateSteamOutflow() {                       
      427 +        steamRateV = receivedSteam;                           
      428 +        if (units.outOfOrder(UnitStatusBoard.STEAM_UNIT)) {   
      429 +            steamOutMax = BoilerConstants.MAX_STEAM_W * DT;   
      430 +            steamOutMin = 0.0;                                
      431 +        } else {                                              
      432 +            steamOutMax = receivedSteam * DT + 0.5 * BoilerCon
          +stants.STEAM_INCREASE_U1 * DT * DT;                           
      433 +            if (steamOutMax > BoilerConstants.MAX_STEAM_W * DT
          +) {                                                           
      434 +                steamOutMax = BoilerConstants.MAX_STEAM_W * DT
          +;                                                             
      435 +            }                                                 
      436 +            steamOutMin = receivedSteam * DT - 0.5 * BoilerCon
          +stants.STEAM_DECREASE_U2 * DT * DT;                           
      437 +            if (steamOutMin < 0.0) {                          
      438 +                steamOutMin = 0.0;                            
      439 +            }                                                 
      440 +        }                                                     
      441 +    }                                                         
      442 +                                                              
      443 +    /**                                                       
      444 +     * p, and which pumps surely / possibly pour during the co
          +ming cycle. A pump ordered on in                              
      445 +     * this cycle pours nothing during the coming cycle (start
          +-up latency, SB-Beh25), so only                               
      446 +     * pumps already reported on count; a pump closed in this 
          +cycle stops at once (see                                      
      447 +     * {@link #recordSent(OutputEvent)}).                     
      448 +     */                                                       
      449 +    private void updatePumpInflow() {                         
      450 +        int confirmed = 0;                                    
      451 +        for (int i = 0; i < PUMPS; i++) {                     
      452 +            boolean pumpSound = !units.outOfOrder(UnitStatusBo
          +ard.pumpUnit(i));                                             
      453 +            boolean controlSound = !units.outOfOrder(UnitStatu
          +sBoard.pumpControlUnit(i));                                   
      454 +            boolean knownOn = pumpSound && receivedPumpOn[i]; 
      455 +            pouringSurely[i] = knownOn && (!controlSound || re
          +ceivedFlow[i] || orderedLastCycle[i]);                        
      456 +            pouringPossibly[i] = knownOn || !pumpSound;       
      457 +            if (receivedPumpOn[i] && receivedFlow[i]) {       
      458 +                confirmed = confirmed + 1;                    
      459 +            }                                                 
      460 +        }                                                     
      461 +        pumpThroughputP = confirmed * BoilerConstants.PUMP_CAP
          +ACITY_P;                                                      
      462 +    }                                                         
      463 +                                                              
      464 +    private static int countOf(boolean[] flags) {             
      465 +        int count = 0;                                        
      466 +        for (int i = 0; i < flags.length; i++) {              
      467 +            if (flags[i]) {                                   
      468 +                count = count + 1;                            
      469 +            }                                                 
      470 +        }                                                     
      471 +        return count;                                         
      472 +    }                                                         
      473 +                                                              
      474 +    private static double clamp(double litres) {              
      475 +        double result = litres;                               
      476 +        if (result < 0.0) {                                   
      477 +            result = 0.0;                                     
      478 +        }                                                     
      479 +        if (result > BoilerConstants.CAPACITY_C) {            
      480 +            result = BoilerConstants.CAPACITY_C;              
      481 +        }                                                     
      482 +        return result;                                        
      483 +    }                                                         
      484 +                                                              
      485 +    // =======================================================
          +==============================                                
      486 +    // Transmission stage                                     
      487 +    // =======================================================
          +==============================                                
      488 +                                                              
      489      /** Records one message transmitted at the end of the curr
           ent cycle. */
      490      public void recordSent(OutputEvent message) {
      491 +        if (message instanceof OutputEvent.ModeMessage) {     
      492 +            OutputEvent.ModeMessage mm = (OutputEvent.ModeMess
          +age) message;                                                 
      493 +            if (mm.operatingMode() != ModeIndicator.INITIALIZA
          +TION_MODE) {                                                  
      494 +                initializationLeft = true;                    
      495 +            }                                                 
      496 +        } else if (message instanceof OutputEvent.ProgramReady
          +) {                                                           
      497 +            programReadySent = true;                          
      498 +        } else if (message instanceof OutputEvent.Valve) {    
      499 +            valveIsOpen = !valveIsOpen;                       
      500 +        } else if (message instanceof OutputEvent.OpenPump) { 
      501 +            OutputEvent.OpenPump op = (OutputEvent.OpenPump) m
          +essage;                                                       
      502 +            int n = op.pumpNumber();                          
      503 +            if (n >= 1 && n <= PUMPS) {                       
      504 +                orderedThisCycle[n - 1] = true;               
      505 +                orderedOnThisCycle[n - 1] = true;             
      506 +            }                                                 
      507 +        } else if (message instanceof OutputEvent.ClosePump) {
      508 +            OutputEvent.ClosePump cp = (OutputEvent.ClosePump)
          + message;                                                     
      509 +            int n = cp.pumpNumber();                          
      510 +            if (n >= 1 && n <= PUMPS) {                       
      511 +                orderedThisCycle[n - 1] = true;               
      512 +                orderedOnThisCycle[n - 1] = false;            
      513 +                pouringSurely[n - 1] = false;                 
      514 +                if (!units.outOfOrder(UnitStatusBoard.pumpUnit
          +(n - 1))) {                                                   
      515 +                    pouringPossibly[n - 1] = false;           
      516 +                }                                             
      517 +            }                                                 
      518 +        }                                                     
      519      }
      520  
       30 -    /** Failure announcements and repair acknowledgements due 
          -in the current cycle. */                                      
      521 +    /** Failure announcements still awaiting acknowledgement, 
          +and this cycle's repair acknowledgements. */                  
      522      public List<OutputEvent> unitProtocolMessages() {
       32 -        return new ArrayList<>();                             
      523 +        return units.protocolMessages();                      
      524      }
      525  
       35 -    /** Number of successive transmissions that carried STOP. 
          -*/                                                            
      526 +    // =======================================================
          +==============================                                
      527 +    // Quantities the controller reads                        
      528 +    // =======================================================
          +==============================                                
      529 +                                                              
      530 +    /** Number of successive transmissions that carried STOP (
          +capped at the stop limit). */                                 
      531      @RoboChartType("nat")
      532      public int successiveStops() {
       38 -        return 0;                                             
      533 +        return stopCount;                                     
      534      }
      535  
       41 -    /** True when the current transmission is judged failed. *
          -/                                                             
      536 +    /** True when the current transmission is judged failed (m
          +essage missing or aberrant). */                               
      537      public boolean transmissionFailure() {
       43 -        return false;                                         
      538 +        return transmissionFailed;                            
      539      }
      540  
      541      /** True while the water-level measuring device is out of 
           order. */
      542      public boolean levelDeviceFailed() {
       48 -        return false;                                         
      543 +        return units.outOfOrder(UnitStatusBoard.LEVEL_UNIT);  
      544      }
      545  
      546      /** True while the steam measuring device is out of order.
            */
      547      public boolean steamDeviceFailed() {
       53 -        return false;                                         
      548 +        return units.outOfOrder(UnitStatusBoard.STEAM_UNIT);  
      549      }
      550  
      551      /** True while some pump is out of order. */
      552      public boolean pumpFailurePresent() {
       58 -        return false;                                         
      553 +        return units.anyPumpOutOfOrder();                     
      554      }
      555  
      556      /** True while some pump controller is out of order. */
      557      public boolean pumpControlFailurePresent() {
       63 -        return false;                                         
      558 +        return units.anyPumpControlOutOfOrder();              
      559      }
      560  
      561      /** True once STEAM_BOILER_WAITING has arrived during init
           ialization. */
      562      public boolean boilerWaitingReceived() {
       68 -        return false;                                         
      563 +        return boilerWaitingLatched;                          
      564      }
      565  
      566      /** True when the current transmission carries PHYSICAL_UN
           ITS_READY. */
      567      public boolean unitsReadyReceived() {
       73 -        return false;                                         
      568 +        return unitsReadyPresent;                             
      569      }
      570  
      571      /** q: the water level most recently reported, litres. */
      572      @RoboChartType("real")
      573      public double waterLevel() {
       79 -        return 0.0;                                           
      574 +        return waterLevelQ;                                   
      575      }
      576  
      577      /** v: the steam rate most recently reported, litres per s
           econd. */
      578      @RoboChartType("real")
      579      public double steamRate() {
       85 -        return 0.0;                                           
      580 +        return steamRateV;                                    
      581      }
      582  
       88 -    /** p: the current throughput of the pumps taken together,
          - litres per second. */                                        
      583 +    /** p: throughput of the pumps reported on whose controlle
          +r sees water flowing, litres per second. */                   
      584      @RoboChartType("real")
      585      public double pumpThroughput() {
       91 -        return 0.0;                                           
      586 +        return pumpThroughputP;                               
      587      }
      588  
       94 -    /** Lower bound of the water level known now, litres. */  
      589 +    /** Lower bound of the current water level (equal to q whi
          +le the level device is sound), litres. */                     
      590      @RoboChartType("real")
      591      public double estimatedLevelLow() {
       97 -        return 0.0;                                           
      592 +        return estimatedLow;                                  
      593      }
      594  
      100 -    /** Upper bound of the water level known now, litres. */  
      595 +    /** Upper bound of the current water level (equal to q whi
          +le the level device is sound), litres. */                     
      596      @RoboChartType("real")
      597      public double estimatedLevelHigh() {
      103 -        return 0.0;                                           
      598 +        return estimatedHigh;                                 
      599      }
      600  
      106 -    /** Worst-case lowest level at the end of the coming cycle
          -, litres. */                                                  
      601 +    /** Worst-case lowest level at the end of the coming cycle
          + with the current pumps, litres. */                           
      602      @RoboChartType("real")
      603      public double levelForecastMin() {
      109 -        return 0.0;                                           
      604 +        return forecastMin;                                   
      605      }
      606  
      112 -    /** Worst-case highest level at the end of the coming cycl
          -e, litres. */                                                 
      607 +    /** Worst-case highest level at the end of the coming cycl
          +e with the current pumps, litres. */                          
      608      @RoboChartType("real")
      609      public double levelForecastMax() {
      115 -        return 0.0;                                           
      610 +        return forecastMax;                                   
      611      }
      612  
      613      /** True while the evacuation valve is open. */
      614      public boolean valveOpen() {
      120 -        return false;                                         
      615 +        return valveIsOpen;                                   
      616      }
      617  
      123 -    /** Number of sound pumps that are off and could be opened
          -. */                                                          
      618 +    /** Number of sound pumps reported off, which could be ope
          +ned. */                                                       
      619      @RoboChartType("nat")
      620      public int openablePumpCount() {
      126 -        return 0;                                             
      621 +        int count = 0;                                        
      622 +        for (int i = 0; i < PUMPS; i++) {                     
      623 +            if (!units.outOfOrder(UnitStatusBoard.pumpUnit(i))
          + && !receivedPumpOn[i]) {                                     
      624 +                count = count + 1;                            
      625 +            }                                                 
      626 +        }                                                     
      627 +        return count;                                         
      628      }
      629  
      129 -    /** Number of sound pumps that are on and could be closed.
          - */                                                           
      630 +    /** Number of sound pumps reported on, which could be clos
          +ed. */                                                        
      631      @RoboChartType("nat")
      632      public int closablePumpCount() {
      132 -        return 0;                                             
      633 +        int count = 0;                                        
      634 +        for (int i = 0; i < PUMPS; i++) {                     
      635 +            if (!units.outOfOrder(UnitStatusBoard.pumpUnit(i))
          + && receivedPumpOn[i]) {                                      
      636 +                count = count + 1;                            
      637 +            }                                                 
      638 +        }                                                     
      639 +        return count;                                         
      640      }
      641  
      135 -    /** Number (1..4) of the pump to open next. */            
      642 +    /** Number of the lowest-numbered openable pump; 1 when no
          +ne is openable. */                                            
      643      @RoboChartType("nat")
      644      public int pumpToOpen() {
      138 -        return 1;                                             
      645 +        int chosen = 1;                                       
      646 +        boolean found = false;                                
      647 +        for (int i = 0; i < PUMPS; i++) {                     
      648 +            if (!found && !units.outOfOrder(UnitStatusBoard.pu
          +mpUnit(i)) && !receivedPumpOn[i]) {                           
      649 +                chosen = i + 1;                               
      650 +                found = true;                                 
      651 +            }                                                 
      652 +        }                                                     
      653 +        return chosen;                                        
      654      }
      655  
      141 -    /** Number (1..4) of the pump to close next. */           
      656 +    /** Number of the highest-numbered closable pump; 4 when n
          +one is closable. */                                           
      657      @RoboChartType("nat")
      658      public int pumpToClose() {
      144 -        return 1;                                             
      659 +        int chosen = PUMPS;                                   
      660 +        boolean found = false;                                
      661 +        for (int i = PUMPS - 1; i >= 0; i--) {                
      662 +            if (!found && !units.outOfOrder(UnitStatusBoard.pu
          +mpUnit(i)) && receivedPumpOn[i]) {                            
      663 +                chosen = i + 1;                               
      664 +                found = true;                                 
      665 +            }                                                 
      666 +        }                                                     
      667 +        return chosen;                                        
      668      }
      669  }

⏺ Now the actuator and the cycle driver.

⏺ Write(src/main/java/steam_boiler/actuator/BoilerActuator.java)
  ⎿  Added 7 lines, removed 2 lines
       5  import steam_boiler.event.OutputEvent;
       6  
       7  /**
       8 - * Receives the controller's outgoing messages for the current 
         -cycle.                                                         
       8 + * Receives the controller's outgoing messages and records thos
         +e issued in the current cycle,                                 
       9 + * in the order issued; they are transmitted together at the en
         +d of the cycle.                                                
      10   */
      11  public final class BoilerActuator {
      12  
      13 +    private final List<OutputEvent> issuedMessages = new ArrayL
         +ist<>();                                                       
      14 +                                                               
      15      /** Issues one outgoing message in the current cycle. */
      16      public void apply(OutputEvent message) {
      17 +        issuedMessages.add(message);                           
      18      }
      19  
      20      /** The messages issued since the last {@link #clear()}. */
      21      public List<OutputEvent> issued() {
      18 -        return new ArrayList<>();                              
      22 +        return new ArrayList<>(issuedMessages);                
      23      }
      24  
      25      /** Starts a new cycle with no message issued. */
      26      public void clear() {
      27 +        issuedMessages.clear();                                
      28      }
      29  }

⏺ Write(src/main/java/steam_boiler/SteamBoilerProgram.java)
  ⎿  Added 25 lines, removed 3 lines
      10  import steam_boiler.sensor.BoilerSensor;
      11  
      12  /**
      13 - * The steam-boiler control program (skeleton).                
      13 + * The steam-boiler control program: wires the sensor, the cont
         +roller and the actuator, and                                   
      14 + * runs the three stages of each five-second cycle (receive, an
         +alyse, transmit). All incoming                                 
      15 + * messages of a cycle are taken as received together, and all 
         +outgoing messages are emitted                                  
      16 + * together. Once in emergency stop the program has stopped and
         + transmits nothing more.                                       
      17   */
      18  public final class SteamBoilerProgram {
      19  
     ...
      21      private final BoilerActuator actuator = new BoilerActuator(
          );
      22      private final SteamBoilerController controller = new SteamB
          oilerController(sensor, actuator);
      23  
      21 -    /** Runs one five-second cycle and returns the outgoing tra
         -nsmission. */                                                  
      24 +    /** Runs one five-second cycle on the incoming transmission
         + and returns the outgoing one. */                              
      25      public List<OutputEvent> cycle(List<InputEvent> incoming) {
      23 -        return new ArrayList<>();                              
      26 +        List<OutputEvent> outgoing = new ArrayList<>();        
      27 +        if (controller.currentMode() == BoilerMode.EMERGENCY_ST
         +OP) {                                                          
      28 +            return outgoing;                                   
      29 +        }                                                      
      30 +        // Stage 1: receive the incoming messages.             
      31 +        sensor.beginCycle();                                   
      32 +        for (InputEvent message : incoming) {                  
      33 +            sensor.receive(message);                           
      34 +        }                                                      
      35 +        // Stage 2: analyse the information.                   
      36 +        sensor.analyse();                                      
      37 +        actuator.clear();                                      
      38 +        controller.step(new InputEvent.TransmissionReceived());
      39 +        // Stage 3: transmit the outgoing messages.            
      40 +        outgoing.addAll(actuator.issued());                    
      41 +        outgoing.addAll(sensor.unitProtocolMessages());        
      42 +        for (OutputEvent message : outgoing) {                 
      43 +            sensor.recordSent(message);                        
      44 +        }                                                      
      45 +        return outgoing;                                       
      46      }
      47  
      48      /** The program's current operating mode. */

⏺ Bash(OUT=/private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3c16-run5-steam-bo
      iler/3a122a7e-8db3-44c1-a904-27351f4e1506/scratchpad/classes && rm -rf
      "$OUT" && mkdir…)
  ⎿  (No output)

⏺ Layer 3 compiles and the trace validates clean. Layer 4: the controller's
  step().

⏺ Write(src/main/java/steam_boiler/controller/SteamBoilerController.java)
  ⎿  Added 186 lines, removed 1 line
       1  package steam_boiler.controller;
       2  
       3 +import steam_boiler.BoilerConstants;                           
       4  import steam_boiler.actuator.BoilerActuator;
       5 +import steam_boiler.domain.ModeIndicator;                      
       6  import steam_boiler.event.InputEvent;
       7 +import steam_boiler.event.OutputEvent;                         
       8  import steam_boiler.sensor.BoilerSensor;
       9  
      10  /**
       8 - * Mode logic of the steam-boiler control program (skeleton).  
      11 + * Mode logic of the steam-boiler control program. It is always
         + in exactly one of five modes                                  
      12 + * and starts in initialization. {@link #step(InputEvent)} is t
         +he analysis stage of one cycle:                                
      13 + * it is called with {@link InputEvent.TransmissionReceived} on
         +ce the cycle's incoming                                        
      14 + * transmission has been received and analysed by the sensor, d
         +ecides the mode, and issues                                    
      15 + * MODE(m) and the mode's commands (PROGRAM_READY, VALVE, OPEN_
         +PUMP, CLOSE_PUMP).                                             
      16 + *                                                             
      17 + * <p>Within each mode the transitions are listed in priority o
         +rder and every guard conjoins                                  
      18 + * the negation of the guards above it, so that the most severe
         + consequence prevails                                          
      19 + * (SB-FR8): emergency stop over rescue, rescue over degraded. 
         +A cycle that changes mode leaves                               
      20 + * the pumps as they are; pump regulation changes at most one p
         +ump per cycle. Emergency stop                                  
      21 + * is terminal.                                                
      22   */
      23  public final class SteamBoilerController {
      24  
     ...
       39  
       40      /** Analysis stage of one control cycle. */
       41      public void step(InputEvent event) {
       42 +        // --- Unit status, transmission and operator stop (SB
          +-V9, SB-V10, SB-FR1..SB-FR5) ---                              
       43 +        boolean operatorStopDemanded = sensor.successiveStops(
          +) >= BoilerConstants.STOP_LIMIT;                              
       44 +        boolean transmissionFailed = sensor.transmissionFailur
          +e();                                                          
       45 +        boolean levelFailed = sensor.levelDeviceFailed();     
       46 +        boolean steamFailed = sensor.steamDeviceFailed();     
       47 +        boolean pumpFailed = sensor.pumpFailurePresent();     
       48 +        boolean pumpControlFailed = sensor.pumpControlFailureP
          +resent();                                                     
       49 +        boolean otherUnitFailed = steamFailed || pumpFailed ||
          + pumpControlFailed;                                           
       50 +        boolean allUnitsSound = !levelFailed && !otherUnitFail
          +ed;                                                           
       51 +        boolean estimateUnreliable = steamFailed || pumpContro
          +lFailed;                                                      
       52 +                                                              
       53 +        // --- Worst-case level forecast for the coming cycle 
          +(SB-V4, SB-Beh13, SB-Beh25) ---                               
       54 +        boolean levelRiskLow = sensor.levelForecastMin() <= Bo
          +ilerConstants.MIN_LIMIT_M1;                                   
       55 +        boolean levelRiskHigh = sensor.levelForecastMax() >= B
          +oilerConstants.MAX_LIMIT_M2;                                  
       56 +        boolean emergencyCondition = operatorStopDemanded || t
          +ransmissionFailed || levelRiskLow || levelRiskHigh;           
       57 +        boolean regulatedEmergency = emergencyCondition || (le
          +velFailed && estimateUnreliable);                             
       58 +        boolean rescueEmergency = emergencyCondition || estima
          +teUnreliable;                                                 
       59 +                                                              
       60 +        // --- Pump regulation towards [N1, N2] (SB-Beh10, SB-
          +Beh15, SB-Beh19) ---                                          
       61 +        boolean forecastBelowNormal = sensor.levelForecastMin(
          +) < BoilerConstants.MIN_NORMAL_N1;                            
       62 +        boolean forecastAboveNormal = sensor.levelForecastMax(
          +) > BoilerConstants.MAX_NORMAL_N2;                            
       63 +        boolean pumpCanOpen = sensor.openablePumpCount() > 0; 
       64 +        boolean pumpCanClose = sensor.closablePumpCount() > 0;
       65 +        boolean closeOnePump = forecastAboveNormal && !forecas
          +tBelowNormal && pumpCanClose;                                 
       66 +        boolean openOnePump = forecastBelowNormal && !forecast
          +AboveNormal && pumpCanOpen;                                   
       67 +        boolean holdPumps = !closeOnePump && !openOnePump;    
       68 +                                                              
       69 +        // --- Initialization (SB-Beh2..SB-Beh9) ---          
       70 +        boolean plantWaiting = sensor.boilerWaitingReceived();
       71 +        boolean unitsReady = sensor.unitsReadyReceived();     
       72 +        boolean steamNotZero = sensor.steamRate() != 0.0;     
       73 +        boolean initEmergency = operatorStopDemanded || transm
          +issionFailed || (plantWaiting && (steamNotZero || levelFailed)
          +);                                                            
       74 +        boolean initAdjusting = !initEmergency && plantWaiting
          + && !unitsReady;                                              
       75 +        boolean levelAboveNormal = sensor.waterLevel() > Boile
          +rConstants.MAX_NORMAL_N2;                                     
       76 +        boolean levelBelowNormal = sensor.waterLevel() < Boile
          +rConstants.MIN_NORMAL_N1;                                     
       77 +        boolean levelWithinNormal = !levelAboveNormal && !leve
          +lBelowNormal;                                                 
       78 +        boolean valveIsOpen = sensor.valveOpen();             
       79 +        boolean drainStart = levelAboveNormal && !valveIsOpen;
       80 +        boolean drainStop = !levelAboveNormal && valveIsOpen; 
       81 +        boolean fillStart = levelBelowNormal && !levelAboveNor
          +mal && !valveIsOpen && !pumpCanClose && pumpCanOpen;          
       82 +        boolean fillStop = levelWithinNormal && !valveIsOpen &
          +& pumpCanClose;                                               
       83 +        boolean readyToStart = levelWithinNormal && !valveIsOp
          +en && !pumpCanClose;                                          
       84 +        boolean valveOnly = drainStart || (drainStop && !level
          +WithinNormal);                                                
       85 +        boolean levelSettling = !drainStart && !drainStop && !
          +fillStart && !fillStop && !readyToStart;                      
       86 +                                                              
       87 +        if (currentMode == BoilerMode.INITIALIZATION) {       
       88 +            if (event instanceof InputEvent.TransmissionReceiv
          +ed && initEmergency) {                                        
       89 +                currentMode = BoilerMode.EMERGENCY_STOP;      
       90 +                actuator.apply(new OutputEvent.ModeMessage(Mod
          +eIndicator.EMERGENCY_STOP_MODE));                             
       91 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && !initEmergency && plantWaiting && unitsReady     
       92 +                    && allUnitsSound) {                       
       93 +                currentMode = BoilerMode.NORMAL;              
       94 +                actuator.apply(new OutputEvent.ModeMessage(Mod
          +eIndicator.NORMAL_MODE));                                     
       95 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && !initEmergency && plantWaiting && unitsReady     
       96 +                    && !allUnitsSound) {                      
       97 +                currentMode = BoilerMode.DEGRADED;            
       98 +                actuator.apply(new OutputEvent.ModeMessage(Mod
          +eIndicator.DEGRADED_MODE));                                   
       99 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && !initEmergency && !plantWaiting) {               
      100 +                currentMode = BoilerMode.INITIALIZATION;      
      101 +                actuator.apply(new OutputEvent.ModeMessage(Mod
          +eIndicator.INITIALIZATION_MODE));                             
      102 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && initAdjusting && valveOnly) {                    
      103 +                currentMode = BoilerMode.INITIALIZATION;      
      104 +                actuator.apply(new OutputEvent.ModeMessage(Mod
          +eIndicator.INITIALIZATION_MODE));                             
      105 +                actuator.apply(new OutputEvent.Valve());      
      106 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && initAdjusting && drainStop                       
      107 +                    && levelWithinNormal) {                   
      108 +                currentMode = BoilerMode.INITIALIZATION;      
      109 +                actuator.apply(new OutputEvent.ModeMessage(Mod
          +eIndicator.INITIALIZATION_MODE));                             
      110 +                actuator.apply(new OutputEvent.Valve());      
      111 +                actuator.apply(new OutputEvent.ProgramReady())
          +;                                                             
      112 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && initAdjusting && fillStart) {                    
      113 +                currentMode = BoilerMode.INITIALIZATION;      
      114 +                actuator.apply(new OutputEvent.ModeMessage(Mod
          +eIndicator.INITIALIZATION_MODE));                             
      115 +                actuator.apply(new OutputEvent.OpenPump(sensor
          +.pumpToOpen()));                                              
      116 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && initAdjusting && fillStop) {                     
      117 +                currentMode = BoilerMode.INITIALIZATION;      
      118 +                actuator.apply(new OutputEvent.ModeMessage(Mod
          +eIndicator.INITIALIZATION_MODE));                             
      119 +                actuator.apply(new OutputEvent.ClosePump(senso
          +r.pumpToClose()));                                            
      120 +                actuator.apply(new OutputEvent.ProgramReady())
          +;                                                             
      121 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && initAdjusting && readyToStart) {                 
      122 +                currentMode = BoilerMode.INITIALIZATION;      
      123 +                actuator.apply(new OutputEvent.ModeMessage(Mod
          +eIndicator.INITIALIZATION_MODE));                             
      124 +                actuator.apply(new OutputEvent.ProgramReady())
          +;                                                             
      125 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && initAdjusting && levelSettling) {                
      126 +                currentMode = BoilerMode.INITIALIZATION;      
      127 +                actuator.apply(new OutputEvent.ModeMessage(Mod
          +eIndicator.INITIALIZATION_MODE));                             
      128 +            }                                                 
      129 +                                                              
      130 +        } else if (currentMode == BoilerMode.NORMAL) {        
      131 +            if (event instanceof InputEvent.TransmissionReceiv
          +ed && regulatedEmergency) {                                   
      132 +                currentMode = BoilerMode.EMERGENCY_STOP;      
      133 +                actuator.apply(new OutputEvent.ModeMessage(Mod
          +eIndicator.EMERGENCY_STOP_MODE));                             
      134 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && !regulatedEmergency && levelFailed) {            
      135 +                currentMode = BoilerMode.RESCUE;              
      136 +                actuator.apply(new OutputEvent.ModeMessage(Mod
          +eIndicator.RESCUE_MODE));                                     
      137 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && !regulatedEmergency && !levelFailed              
      138 +                    && otherUnitFailed) {                     
      139 +                currentMode = BoilerMode.DEGRADED;            
      140 +                actuator.apply(new OutputEvent.ModeMessage(Mod
          +eIndicator.DEGRADED_MODE));                                   
      141 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && !regulatedEmergency && allUnitsSound             
      142 +                    && closeOnePump) {                        
      143 +                currentMode = BoilerMode.NORMAL;              
      144 +                actuator.apply(new OutputEvent.ModeMessage(Mod
          +eIndicator.NORMAL_MODE));                                     
      145 +                actuator.apply(new OutputEvent.ClosePump(senso
          +r.pumpToClose()));                                            
      146 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && !regulatedEmergency && allUnitsSound             
      147 +                    && openOnePump) {                         
      148 +                currentMode = BoilerMode.NORMAL;              
      149 +                actuator.apply(new OutputEvent.ModeMessage(Mod
          +eIndicator.NORMAL_MODE));                                     
      150 +                actuator.apply(new OutputEvent.OpenPump(sensor
          +.pumpToOpen()));                                              
      151 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && !regulatedEmergency && allUnitsSound             
      152 +                    && holdPumps) {                           
      153 +                currentMode = BoilerMode.NORMAL;              
      154 +                actuator.apply(new OutputEvent.ModeMessage(Mod
          +eIndicator.NORMAL_MODE));                                     
      155 +            }                                                 
      156 +                                                              
      157 +        } else if (currentMode == BoilerMode.DEGRADED) {      
      158 +            if (event instanceof InputEvent.TransmissionReceiv
          +ed && regulatedEmergency) {                                   
      159 +                currentMode = BoilerMode.EMERGENCY_STOP;      
      160 +                actuator.apply(new OutputEvent.ModeMessage(Mod
          +eIndicator.EMERGENCY_STOP_MODE));                             
      161 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && !regulatedEmergency && levelFailed) {            
      162 +                currentMode = BoilerMode.RESCUE;              
      163 +                actuator.apply(new OutputEvent.ModeMessage(Mod
          +eIndicator.RESCUE_MODE));                                     
      164 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && !regulatedEmergency && allUnitsSound) {          
      165 +                currentMode = BoilerMode.NORMAL;              
      166 +                actuator.apply(new OutputEvent.ModeMessage(Mod
          +eIndicator.NORMAL_MODE));                                     
      167 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && !regulatedEmergency && !levelFailed              
      168 +                    && otherUnitFailed && closeOnePump) {     
      169 +                currentMode = BoilerMode.DEGRADED;            
      170 +                actuator.apply(new OutputEvent.ModeMessage(Mod
          +eIndicator.DEGRADED_MODE));                                   
      171 +                actuator.apply(new OutputEvent.ClosePump(senso
          +r.pumpToClose()));                                            
      172 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && !regulatedEmergency && !levelFailed              
      173 +                    && otherUnitFailed && openOnePump) {      
      174 +                currentMode = BoilerMode.DEGRADED;            
      175 +                actuator.apply(new OutputEvent.ModeMessage(Mod
          +eIndicator.DEGRADED_MODE));                                   
      176 +                actuator.apply(new OutputEvent.OpenPump(sensor
          +.pumpToOpen()));                                              
      177 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && !regulatedEmergency && !levelFailed              
      178 +                    && otherUnitFailed && holdPumps) {        
      179 +                currentMode = BoilerMode.DEGRADED;            
      180 +                actuator.apply(new OutputEvent.ModeMessage(Mod
          +eIndicator.DEGRADED_MODE));                                   
      181 +            }                                                 
      182 +                                                              
      183 +        } else if (currentMode == BoilerMode.RESCUE) {        
      184 +            if (event instanceof InputEvent.TransmissionReceiv
          +ed && rescueEmergency) {                                      
      185 +                currentMode = BoilerMode.EMERGENCY_STOP;      
      186 +                actuator.apply(new OutputEvent.ModeMessage(Mod
          +eIndicator.EMERGENCY_STOP_MODE));                             
      187 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && !rescueEmergency && allUnitsSound) {             
      188 +                currentMode = BoilerMode.NORMAL;              
      189 +                actuator.apply(new OutputEvent.ModeMessage(Mod
          +eIndicator.NORMAL_MODE));                                     
      190 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && !rescueEmergency && !levelFailed                 
      191 +                    && otherUnitFailed) {                     
      192 +                currentMode = BoilerMode.DEGRADED;            
      193 +                actuator.apply(new OutputEvent.ModeMessage(Mod
          +eIndicator.DEGRADED_MODE));                                   
      194 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && !rescueEmergency && levelFailed                  
      195 +                    && closeOnePump) {                        
      196 +                currentMode = BoilerMode.RESCUE;              
      197 +                actuator.apply(new OutputEvent.ModeMessage(Mod
          +eIndicator.RESCUE_MODE));                                     
      198 +                actuator.apply(new OutputEvent.ClosePump(senso
          +r.pumpToClose()));                                            
      199 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && !rescueEmergency && levelFailed                  
      200 +                    && openOnePump) {                         
      201 +                currentMode = BoilerMode.RESCUE;              
      202 +                actuator.apply(new OutputEvent.ModeMessage(Mod
          +eIndicator.RESCUE_MODE));                                     
      203 +                actuator.apply(new OutputEvent.OpenPump(sensor
          +.pumpToOpen()));                                              
      204 +            } else if (event instanceof InputEvent.Transmissio
          +nReceived && !rescueEmergency && levelFailed                  
      205 +                    && holdPumps) {                           
      206 +                currentMode = BoilerMode.RESCUE;              
      207 +                actuator.apply(new OutputEvent.ModeMessage(Mod
          +eIndicator.RESCUE_MODE));                                     
      208 +            }                                                 
      209 +                                                              
      210 +        } else if (currentMode == BoilerMode.EMERGENCY_STOP) {
      211 +            // Terminal mode (SB-Beh22): the control task has 
          +ended and the program has stopped;                            
      212 +            // no transition leaves it and nothing more is tra
          +nsmitted.                                                     
      213 +        }                                                     
      214      }
      215  }

⏺ I'll compile and mechanically verify the guards, brute-forcing all truth
  assignments of the 18 atomic conditions to confirm exactly one branch is
  enabled in every non-terminal mode.

⏺ Bash(OUT=/private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3c16-run5-steam-bo
      iler/3a122a7e-8db3-44c1-a904-27351f4e1506/scratchpad/classes && rm -rf
      "$OUT" && mkdir…)
  ⎿  (No output)

⏺ I've confirmed the guards are exhaustive and mutually exclusive, satisfying
  the deadlock-freedom precondition propositionally. Now I'll run a crude plant
  simulation in the scratchpad to test the full sequence: initialization →
  normal → level-device failure → rescue → repair.

⏺ Bash(SP=/private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3c16-run5-steam-boi
      ler/3a122a7e-8db3-44c1-a904-27351f4e1506/scratchpad && mkdir -p $SP/smoke
      && cat > $SP/…)
  ⎿  (No output)

⏺ The smoke test uncovered a real bug: at k=3 the level device triggered a false
  failure alarm, since the lower bound wrongly counted a just-ordered pump as
  surely pouring before its flow was confirmed (SB-FR2). I'll fix the lower
  bound to only count a pump once its controller confirms flow, or, if the
  controller is out of order, once it's been on for more than one cycle.
⏺ Update(src/main/java/steam_boiler/sensor/BoilerSensor.java)
  ⎿  Added 3 lines, removed 1 line
      452              boolean pumpSound = !units.outOfOrder(UnitStatusBo
           ard.pumpUnit(i));
      453              boolean controlSound = !units.outOfOrder(UnitStatu
           sBoard.pumpControlUnit(i));
      454              boolean knownOn = pumpSound && receivedPumpOn[i];
      455 -            pouringSurely[i] = knownOn && (!controlSound || re
          -ceivedFlow[i] || orderedLastCycle[i]);                        
      455 +            // Surely pouring: the controller confirms the flo
          +w, or (controller out of order) the                           
      456 +            // pump has been on for longer than its start-up l
          +atency.                                                       
      457 +            pouringSurely[i] = knownOn && ((controlSound && re
          +ceivedFlow[i]) || (!controlSound && !orderedLastCycle[i]));   
      458              pouringPossibly[i] = knownOn || !pumpSound;
      459              if (receivedPumpOn[i] && receivedFlow[i]) {
      460                  confirmed = confirmed + 1;

⏺ Update(src/main/java/steam_boiler/sensor/BoilerSensor.java)
  ⎿  Added 2 lines, removed 1 line
      443      /**
      444       * p, and which pumps surely / possibly pour during the co
           ming cycle. A pump ordered on in
      445       * this cycle pours nothing during the coming cycle (start
           -up latency, SB-Beh25), so only
      446 -     * pumps already reported on count; a pump closed in this 
          -cycle stops at once (see                                      
      446 +     * pumps already reported on count, and a pump counts as s
          +urely pouring only once its flow                              
      447 +     * is confirmed; a pump closed in this cycle stops at once
          + (see                                                         
      448       * {@link #recordSent(OutputEvent)}).
      449       */
      450      private void updatePumpInflow() {

⏺ Bash(SP=/private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3c16-run5-steam-boi
      ler/3a122a7e-8db3-44c1-a904-27351f4e1506/scratchpad && cd
      /Users/ranwei/Gitee/forge-v3c…)
  ⎿  (No output)

⏺ Found that the second emergency stop was a simulation bug: it applied
  CLOSE_PUMP after computing inflow instead of immediately, letting it keep
  pouring against the requirements. I'll fix the sim to apply orders before
  computing inflow.

⏺ Bash(SP=/private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3c16-run5-steam-boi
      ler/3a122a7e-8db3-44c1-a904-27351f4e1506/scratchpad && cd $SP/smoke &&
      python3 -I - <<'…)
  ⎿  (No output)

⏺ The core behaviors check out, but the emergency stop at k=31 traces to two
  causes: the repair arrived with a still-bad reading (re-failing the device)
  and the widening worst-case estimate correctly triggering conservative rescue
  behavior near M1/M2. Next I'll test the repair path using a valid reading sent
  earlier.

⏺ Bash(SP=/private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3c16-run5-steam-boi
      ler/3a122a7e-8db3-44c1-a904-27351f4e1506/scratchpad && cd $SP/smoke && sed
      -i '' 's/if…)
  ⎿  (No output)

⏺ RESCUE → NORMAL on a valid repair works, with the repair acknowledgement sent.
  Next: regenerate the trace and scan the Java for constructs outside the
  profile.

⏺ Bash(cd
      /Users/ranwei/Gitee/forge-v3c16-run5-steam_boiler/java.generated.project
      && python3 -I
      /private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3c16-run5-steam-boi…)
  ⎿  (No output)

⏺ Found one real issue: the private DT double constant in BoilerSensor is
  missing @RoboChartType("real"), which the profile checker flags as an
  error—the other hits are exempt local variables and the prescribed event
  package name. I'll fix the DT constant next.

⏺ Update(src/main/java/steam_boiler/sensor/BoilerSensor.java)
  ⎿  Added 1 line, removed 1 line
      19  public final class BoilerSensor {
      20  
      21      private static final int PUMPS = BoilerConstants.PUMP_COUNT
          ;
      22 -    private static final double DT = BoilerConstants.CYCLE_SECO
         -NDS;                                                           
      22 +    @RoboChartType("real") private static final double DT = Boi
         +lerConstants.CYCLE_SECONDS;                                    
      23  
      24      // ---- contents of the current incoming transmission ----
      25      private boolean stopPresent;

⏺ Bash(SP=/private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3c16-run5-steam-boi
      ler/3a122a7e-8db3-44c1-a904-27351f4e1506/scratchpad && rm -rf $SP/classes
      && mkdir -p $…)
  ⎿  (No output)

⏺ Bash(./gradlew --offline -q compileJava >
      /private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3c16-run5-steam-boiler/
      3a122a7e-8db3-44c1-a904-27351f4e1506/scratchpad/g…)
  ⎿  (No output)

⏺ Gradle compiles too. My Gradle run left build output in the project, so I'll
  check what it created and clean up.

⏺ Bash(ls -la && rm -rf build && ls -la | grep -E 'build|gradle')
  ⎿  (No output)

⏺ Bash(rm -rf .gradle && ls -a)
  ⎿  (No output)

⏺ Layers 3 and 4 check out—compilation, exhaustive/exclusive guards, and a clean
  trace. Moving to layer 5 now, starting by gathering line ranges for the
  java_trace entries in post_codegen.

⏺ Bash(cd /Users/ranwei/Gitee/forge-v3c16-run5-steam_boiler/java.generated.proje
      ct/src/main/java/steam_boiler && grep -nE 'public void step|currentMode ==
      BoilerMode|b…)
  ⎿  (No output)

⏺ Now I'll write post_codegen.{json,md}. One script renders both from the same
  issue list so they stay identical.

  Made 1 scratchpad edit +167 (ctrl+o to expand)

⏺ Before running the script I'll check the cited line ranges against the source.

⏺ Bash(grep -nE 'knowledge kept|results of the analysis|forecastMax =
      BoilerConstants|stopPresent\) \{|stopCount = 0|pumpThroughputP =
      confirmed|private void updatePum…)
  ⎿  (No output)

  Searched for 1 pattern (ctrl+o to expand)

⏺ Bash(SP=/private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3c16-run5-steam-boi
      ler/3a122a7e-8db3-44c1-a904-27351f4e1506/scratchpad && sed -i ''
      's/t(SENS, "647-674",…)
  ⎿  (No output)

⏺ post_codegen is written. Final trace refresh and compile check before writing
  the summary:

⏺ Bash(cd
      /Users/ranwei/Gitee/forge-v3c16-run5-steam_boiler/java.generated.project
      && SP=/private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3c16-run5-steam-b
      oiler/3a12…)  ⎿  (No output)

⏺ Now ITER_SUMMARY.md (300-word limit).

⏺ Write(~/Gitee/forge-v3c16-run5-steam_boiler/forge.assets/corrections/ITER_SUMM
       ARY.md)
  ⎿  Wrote 24 lines to ~/Gitee/forge-v3c16-run5-steam_boiler/forge.assets/c
     orrections/ITER_SUMMARY.md
       1 # Steam Boiler, pass 5, iteration 1
       2
       3 **Written.** 14 Java files under `steam_boiler/`, plus `result_c
         odegen.json` (198 entries; all 53 requirements covered) and `pos
         t_codegen.{md,json}`. No Java existed beforehand.
       4
       5 - `SteamBoilerController` has one mode field. Its `step` covers
         five modes, each transition fired by the per-cycle `Transmission
         Received` event.
       6   - Every guard conjoins the negations of the guards above it.
     … +18 lines (ctrl+o to expand)

  Read 1 file (ctrl+o to expand)

⏺ Everything's written up—Java, trace, post_codegen, and a 299-word summary. Now
  I'll run the runner once in the background (passing unavailable since no
  token figure is shown) and log its output to the scratchpad.

⏺ Bash(cd /Users/ranwei/Gitee/forge-v3c16-run5-steam_boiler && export
      FORGE_ARM_DIR=/Users/ranwei/Gitee/formal_method_guided_vibe_coding/experim
      ents/v3-cold && export…)
  ⎿  Running in the background (↓ to manage)

⏺ The runner is running in the background. I'll wait for it to finish and won't
  touch the Java or any report until then.

⏺ Background command "Run the pipeline runner once for iteration 1" completed
(exit code 0)

⏺ Bash(L=/private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3c16-run5-steam-boil
      er/3a122a7e-8db3-44c1-a904-27351f4e1506/scratchpad/runner.log; wc -l $L;
      grep -n -A3 '=…)
  ⎿  (No output)

⏺ The pass is finished, but the pipeline did not pass: at least one phase
  failed. Following the protocol I stopped there, so I haven't opened any
  post_*.md report, haven't touched the Java, and don't know which phase failed
  or why. The runner exited with code 1.

  The line the runner printed after === iteration 1 : what to do next ===,
  verbatim:

    Some phase failed. Read ONLY these, inside your worktree:

  What I did before running it:
  - Pre-flight: all five checks printed OK. No Java was present, so nothing was
    deleted.
  - Java: 14 files under java.generated.project/src/main/java/steam_boiler/,
    built through the five layers. One controller handles the five modes, and
    every transition fires on a once-per-cycle event. Emergency stop is a
    terminal mode.
  - Checks I could run myself: the code compiles with javac and with Gradle
    offline; I removed the build output Gradle left behind. Checking every
    combination of the guard conditions showed exactly one branch can fire in
    each non-terminal mode.
  - Smoke test: a scratch plant simulation, kept outside the project, ran
    initialization → normal → pump regulation → level failure → rescue → repair
    → normal as intended. It also caught a false level-failure alarm in my lower
    level bound, which I fixed before the run.
  - Trace: result_codegen.json has 198 entries. Every requirement, every Java
    file and every public element is covered.
  - Review and summary:
    - forge.assets/corrections/post_codegen.{md,json} has status "uncertain" and
      lists 14 decisions the requirements left open, for example the constant
      values (none are given) and how a pump is chosen each cycle. Nothing is
      unimplemented.
    - forge.assets/corrections/ITER_SUMMARY.md is 299 words.

  Two of those decisions limit what the verifiers can catch. The
  failure-announcement and repair-acknowledgement messages are sent outside the
  controller, so they are not in the extracted model. And the model sees the
  sensor's judgements only as uninterpreted functions, so the verifiers check
  the mode logic, not the failure-detection arithmetic.

  I passed unavailable as the token figure, because my session shows none.

  ITER_1_COMPLETE: 18 files written