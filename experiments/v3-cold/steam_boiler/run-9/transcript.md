
❯ # Single cold pass — steam_boiler, pass 9

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
  /Users/ranwei/Gitee/forge-v3c16-run9-steam_boiler
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
  cd /Users/ranwei/Gitee/forge-v3c16-run9-steam_boiler 2>/dev/null || { echo
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
  ude_iteration.sh steam_boiler 9 1 <tokens|unavailable>`
  2. When it finishes, **stop**. Report the line it prints after
     `=== iteration 1 : what to do next ===`, verbatim. Do **not** open any
     `forge.assets/corrections/post_*.md` file, do **not** change the Java, and
  do
     **not** run the runner again: a pass whose Java changes after the runner
  starts,
     or that reads a report, is void.

  Then finish with the line `ITER_1_COMPLETE: <N> files written`.

⏺ No earlier run appears in this conversation, so this pass is clean. I'll start
  with the pre-write check.

⏺ Bash(cd /Users/ranwei/Gitee/forge-v3c16-run9-steam_boiler 2>/dev/null || {
      echo "FAIL no worktree"; exit 1; }
      export FORGE_ARM_DIR=/Users/ranwei/Gitee/formal_method_…)
  ⎿  (file diff unavailable for this command)

⏺ The check passed on every line. Next I'll read the inputs.

  Listed 1 directory (ctrl+o to expand)

⏺ There's no system/system_description.txt for this study, so the requirements
  are the only input. Reading them now.

  Read 7 files (ctrl+o to expand)

⏺ Next, the five layer prompts and their README.

⏺ Bash(cd /Users/ranwei/Gitee/forge-v3c16-run9-steam_boiler/forge.assets/vibe-co
      ding-prompts && for f in README.md 01_architecture.md 02_skeleton.md
      03_leaves.md 04_co…)
  ⎿  (file diff unavailable for this command)

⏺ The shell moved into the prompts folder, so I'll use absolute paths from here
  on. Next I'll check the Gradle scaffold and the tool versions.

⏺ Bash(cd
      /Users/ranwei/Gitee/forge-v3c16-run9-steam_boiler/java.generated.project
      && cat build.gradle settings.gradle gradle.properties; ls -la; ls -la
      ../forge.asset…)
  ⎿  (No output)

⏺ Java goes under src/main/java/steam_boiler/. Before designing, I'll list
  docs/corpus/, which I'm allowed to read for Isabelle background.

⏺ Bash(cd /Users/ranwei/Gitee/forge-v3c16-run9-steam_boiler && find docs/corpus
      -maxdepth 3 | head -50; du -sh docs/corpus)
  ⎿  (No output)

⏺ I've laid out the Layer 1 architecture for the steam_boiler package, defining
  the mode enum, domain types, input/output events, controller, sensor, and
  actuator components, each mapped to specific requirements and designed to keep
  the controller's state minimal while isolating per-unit logic in the leaves.
  Next, I'll move on to building the Layer 2 skeleton.

⏺ Bash(cd
      /Users/ranwei/Gitee/forge-v3c16-run9-steam_boiler/java.generated.project
      && mkdir -p
      src/main/java/steam_boiler/{annotation,mode,domain,event,controller,sens…)
  ⎿  (No output)

⏺ Bash(B=src/main/java/steam_boiler
      cat > $B/event/InputEvent.java <<'EOF'…)
  ⎿  (No output)

⏺ Next, the skeletons of the sensor, actuator and controller, with empty bodies.

⏺ Bash(B=src/main/java/steam_boiler
      cat > $B/sensor/BoilerSensor.java <<'EOF'…)
  ⎿  (No output)

⏺ The skeleton compiles. Next I'll write the trace generator in the scratchpad
  so it can be re-run at the end of each layer.

  Made 1 scratchpad edit +157 (ctrl+o to expand)

⏺ Bash(cd /Users/ranwei/Gitee/forge-v3c16-run9-steam_boiler && python3 -I
      /private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3c16-run9-steam-boiler/
      85130cee-6d8e-4dbe-…)
  ⎿  (No output)

⏺ The trace covers all 53 requirements and all 11 files.

  Layer 3: leaves

  Next, the full sensor: reception, the SB-FR1–5 detection, and the bookkeeping
  for SB-V5–V10.

⏺ Write(java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.ja
       va)
  ⎿  Added 575 lines, removed 28 lines
        3  import java.util.ArrayList;
        4  import java.util.List;
        5  import steam_boiler.annotation.RoboChartType;
        6 +import steam_boiler.controller.BoilerConstants;               
        7 +import steam_boiler.domain.PumpControlReading;                
        8 +import steam_boiler.domain.PumpReading;                       
        9  import steam_boiler.event.InputEvent;
       10  import steam_boiler.event.OutputEvent;
       11  import steam_boiler.mode.BoilerMode;
       12  
       10 -/** Reception and analysis stage (skeleton). */               
       13 +/**                                                           
       14 + * Reception and analysis stage of the program (SB-V3). Each c
          +ycle the                                                      
       15 + * transmission system calls {@link #startCycle()}, then {@lin
          +k #receive} once                                              
       16 + * per incoming message, then {@link #analyse()}. The analysis
          + judges unit                                                  
       17 + * failures (SB-FR1..SB-FR5), keeps the per-unit status (SB-V9
          +), the STOP count                                             
       18 + * (SB-V10), the quantities q, v, p (SB-V5..SB-V7) and the lev
          +el bounds (SB-V8).                                            
       19 + * Pumps are numbered 1..PUMP_COUNT.                          
       20 + */                                                           
       21  public final class BoilerSensor {
       12 -    public void startCycle() { }                              
       13 -    public void receive(InputEvent message) { }               
       14 -    public void analyse() { }                                 
       15 -    public void notePumpOrder(@RoboChartType("nat") int pump, 
          -boolean switchOn) { }                                         
       16 -    public void noteModeSent(BoilerMode mode) { }             
       17 -    public void noteProgramReadySent() { }                    
       18 -    public void noteValve(boolean open) { }                   
       19 -    public boolean pumpCommandedOn(@RoboChartType("nat") int p
          -ump) { return false; }                                        
       20 -    public List<OutputEvent> statusMessagesDue() { return new 
          -ArrayList<>(); }                                              
       21 -    @RoboChartType("nat") public int successiveStops() { retur
          -n 0; }                                                        
       22 -    public boolean transmissionFailure() { return false; }    
       23 -    public boolean steamBoilerWaitingReceived() { return false
          -; }                                                           
       24 -    public boolean physicalUnitsReadyReceived() { return false
          -; }                                                           
       25 -    @RoboChartType("real") public double levelReading() { retu
          -rn 0.0; }                                                     
       26 -    @RoboChartType("real") public double steamReading() { retu
          -rn 0.0; }                                                     
       27 -    @RoboChartType("real") public double pumpThroughput() { re
          -turn 0.0; }                                                   
       28 -    @RoboChartType("real") public double levelLowerBound() { r
          -eturn 0.0; }                                                  
       29 -    @RoboChartType("real") public double levelUpperBound() { r
          -eturn 0.0; }                                                  
       30 -    @RoboChartType("real") public double levelEstimate() { ret
          -urn 0.0; }                                                    
       31 -    @RoboChartType("real") public double predictedLevelLow() {
          - return 0.0; }                                                
       32 -    @RoboChartType("real") public double predictedLevelHigh() 
          -{ return 0.0; }                                               
       33 -    public boolean levelDeviceFailed() { return false; }      
       34 -    public boolean steamDeviceFailed() { return false; }      
       35 -    public boolean pumpFailed(@RoboChartType("nat") int pump) 
          -{ return false; }                                             
       36 -    public boolean pumpControllerFailed(@RoboChartType("nat") 
          -int pump) { return false; }                                   
       37 -    public boolean anyPumpFailed() { return false; }          
       38 -    public boolean anyPumpControllerFailed() { return false; }
       22 +                                                              
       23 +    private static final int PUMPS = BoilerConstants.PUMP_COUN
          +T;                                                            
       24 +    private static final int SETTLED = 3;                     
       25 +                                                              
       26 +    /** Status of one physical unit (SB-V9). */               
       27 +    private static final class UnitStatus {                   
       28 +        private boolean outOfOrder = false;                   
       29 +        private boolean failureAcknowledged = true;           
       30 +        private boolean repairAcknowledgementDue = false;     
       31 +        private boolean repairedReceived = false;             
       32 +        private boolean failureAckReceived = false;           
       33 +                                                              
       34 +        private void clearCycle() {                           
       35 +            repairAcknowledgementDue = false;                 
       36 +            repairedReceived = false;                         
       37 +            failureAckReceived = false;                       
       38 +        }                                                     
       39 +                                                              
       40 +        private void markFailed() {                           
       41 +            outOfOrder = true;                                
       42 +            failureAcknowledged = false;                      
       43 +        }                                                     
       44 +                                                              
       45 +        private boolean announcementPending() {               
       46 +            return outOfOrder && !failureAcknowledged;        
       47 +        }                                                     
       48 +    }                                                         
       49 +                                                              
       50 +    private final UnitStatus[] pumpUnits = new UnitStatus[PUMP
          +S];                                                           
       51 +    private final UnitStatus[] controlUnits = new UnitStatus[P
          +UMPS];                                                        
       52 +    private final UnitStatus levelUnit = new UnitStatus();    
       53 +    private final UnitStatus steamUnit = new UnitStatus();    
       54 +                                                              
       55 +    // Messages of the current transmission.                  
       56 +    private boolean stopReceived = false;                     
       57 +    private boolean waitingReceived = false;                  
       58 +    private boolean unitsReadyReceived = false;               
       59 +    private boolean aberrantMessage = false;                  
       60 +    @RoboChartType("nat") private int levelMessages = 0;      
       61 +    @RoboChartType("nat") private int steamMessages = 0;      
       62 +    @RoboChartType("real") private double levelValue = 0.0;   
       63 +    @RoboChartType("real") private double steamValue = 0.0;   
       64 +    private final int[] pumpStateMessages = new int[PUMPS];   
       65 +    private final boolean[] pumpReportedOn = new boolean[PUMPS
          +];                                                            
       66 +    private final int[] controlStateMessages = new int[PUMPS];
       67 +    private final boolean[] controlReportedFlow = new boolean[
          +PUMPS];                                                       
       68 +                                                              
       69 +    // What the program itself transmitted (recorded by the ac
          +tuator).                                                      
       70 +    private BoilerMode modeSentPrevious = BoilerMode.INITIALIZ
          +ATION;                                                        
       71 +    private BoilerMode modeSentCurrent = BoilerMode.INITIALIZA
          +TION;                                                         
       72 +    private boolean programReadySentPrevious = false;         
       73 +    private boolean programReadySentCurrent = false;          
       74 +    private boolean valveOpen = false;                        
       75 +    private boolean valveOpenDuringCycle = false;             
       76 +    private final boolean[] pumpCommanded = new boolean[PUMPS]
          +;                                                             
       77 +    private final boolean[] pumpOrderedCurrent = new boolean[P
          +UMPS];                                                        
       78 +    private final int[] cyclesSinceOrder = new int[PUMPS];    
       79 +                                                              
       80 +    // Derived quantities.                                    
       81 +    /** q: water level most recently reported (SB-V5). */     
       82 +    @RoboChartType("real") private double q = 0.0;            
       83 +    /** v: steam rate most recently reported (SB-V6). */      
       84 +    @RoboChartType("real") private double v = 0.0;            
       85 +    /** p: current throughput of the pumps taken together (SB-
          +V7). */                                                       
       86 +    @RoboChartType("real") private double p = 0.0;            
       87 +    /** Lower bound on the true water level (SB-V8). */       
       88 +    @RoboChartType("real") private double levelLow = 0.0;     
       89 +    /** Upper bound on the true water level (SB-V8). */       
       90 +    @RoboChartType("real") private double levelHigh = 0.0;    
       91 +    @RoboChartType("real") private double steamRateLow = 0.0; 
       92 +    @RoboChartType("real") private double steamRateHigh = 0.0;
       93 +    @RoboChartType("real") private double predictedLow = 0.0; 
       94 +    @RoboChartType("real") private double predictedHigh = 0.0;
       95 +    /** Number of successive transmissions that carried STOP (
          +SB-V10). */                                                   
       96 +    @RoboChartType("nat") private int stopCount = 0;          
       97 +    private boolean transmissionFailed = false;               
       98 +    private boolean boundsKnown = false;                      
       99 +    private final boolean[] certainFlowPrevious = new boolean[
          +PUMPS];                                                       
      100 +    private final boolean[] couldPourPrevious = new boolean[PU
          +MPS];                                                         
      101 +                                                              
      102 +    /** Creates the sensor stage with every unit sound and eve
          +ry pump commanded off. */                                     
      103 +    public BoilerSensor() {                                   
      104 +        for (int i = 0; i < PUMPS; i++) {                     
      105 +            pumpUnits[i] = new UnitStatus();                  
      106 +            controlUnits[i] = new UnitStatus();               
      107 +            cyclesSinceOrder[i] = SETTLED;                    
      108 +        }                                                     
      109 +        steamRateHigh = BoilerConstants.MAX_STEAM_W;          
      110 +        levelHigh = BoilerConstants.CAPACITY_C;               
      111 +    }                                                         
      112 +                                                              
      113 +    /** Begins the reception stage of a new cycle. */         
      114 +    public void startCycle() {                                
      115 +        modeSentPrevious = modeSentCurrent;                   
      116 +        programReadySentPrevious = programReadySentCurrent;   
      117 +        programReadySentCurrent = false;                      
      118 +        valveOpenDuringCycle = valveOpen;                     
      119 +        for (int i = 0; i < PUMPS; i++) {                     
      120 +            if (pumpOrderedCurrent[i]) {                      
      121 +                cyclesSinceOrder[i] = 1;                      
      122 +            } else if (cyclesSinceOrder[i] < SETTLED) {       
      123 +                cyclesSinceOrder[i] = cyclesSinceOrder[i] + 1;
      124 +            }                                                 
      125 +            pumpOrderedCurrent[i] = false;                    
      126 +            pumpStateMessages[i] = 0;                         
      127 +            controlStateMessages[i] = 0;                      
      128 +            pumpUnits[i].clearCycle();                        
      129 +            controlUnits[i].clearCycle();                     
      130 +        }                                                     
      131 +        levelUnit.clearCycle();                               
      132 +        steamUnit.clearCycle();                               
      133 +        stopReceived = false;                                 
      134 +        waitingReceived = false;                              
      135 +        unitsReadyReceived = false;                           
      136 +        aberrantMessage = false;                              
      137 +        levelMessages = 0;                                    
      138 +        steamMessages = 0;                                    
      139 +    }                                                         
      140 +                                                              
      141 +    /** Records one incoming message of the current transmissi
          +on. */                                                        
      142 +    public void receive(InputEvent message) {                 
      143 +        if (message instanceof InputEvent.StopRequest) {      
      144 +            stopReceived = true;                              
      145 +        } else if (message instanceof InputEvent.SteamBoilerWa
          +iting) {                                                      
      146 +            waitingReceived = true;                           
      147 +        } else if (message instanceof InputEvent.PhysicalUnits
          +Ready) {                                                      
      148 +            unitsReadyReceived = true;                        
      149 +        } else if (message instanceof InputEvent.PumpState) { 
      150 +            InputEvent.PumpState ps = (InputEvent.PumpState) m
          +essage;                                                       
      151 +            PumpReading reading = ps.reading();               
      152 +            if (validPump(reading.pumpNumber())) {            
      153 +                int i = reading.pumpNumber() - 1;             
      154 +                pumpStateMessages[i] = pumpStateMessages[i] + 
          +1;                                                            
      155 +                pumpReportedOn[i] = reading.switchedOn();     
      156 +            } else {                                          
      157 +                aberrantMessage = true;                       
      158 +            }                                                 
      159 +        } else if (message instanceof InputEvent.PumpControlSt
          +ate) {                                                        
      160 +            InputEvent.PumpControlState pcs = (InputEvent.Pump
          +ControlState) message;                                        
      161 +            PumpControlReading reading = pcs.reading();       
      162 +            if (validPump(reading.controlledPump())) {        
      163 +                int i = reading.controlledPump() - 1;         
      164 +                controlStateMessages[i] = controlStateMessages
          +[i] + 1;                                                      
      165 +                controlReportedFlow[i] = reading.waterFlowing(
          +);                                                            
      166 +            } else {                                          
      167 +                aberrantMessage = true;                       
      168 +            }                                                 
      169 +        } else if (message instanceof InputEvent.Level) {     
      170 +            InputEvent.Level lv = (InputEvent.Level) message; 
      171 +            levelMessages = levelMessages + 1;                
      172 +            levelValue = lv.levelValue();                     
      173 +        } else if (message instanceof InputEvent.Steam) {     
      174 +            InputEvent.Steam st = (InputEvent.Steam) message; 
      175 +            steamMessages = steamMessages + 1;                
      176 +            steamValue = st.steamValue();                     
      177 +        } else if (message instanceof InputEvent.PumpRepaired)
          + {                                                            
      178 +            InputEvent.PumpRepaired pr = (InputEvent.PumpRepai
          +red) message;                                                 
      179 +            noteRepaired(pumpUnits, pr.pump());               
      180 +        } else if (message instanceof InputEvent.PumpControlRe
          +paired) {                                                     
      181 +            InputEvent.PumpControlRepaired pcr = (InputEvent.P
          +umpControlRepaired) message;                                  
      182 +            noteRepaired(controlUnits, pcr.pump());           
      183 +        } else if (message instanceof InputEvent.LevelRepaired
          +) {                                                           
      184 +            levelUnit.repairedReceived = true;                
      185 +        } else if (message instanceof InputEvent.SteamRepaired
          +) {                                                           
      186 +            steamUnit.repairedReceived = true;                
      187 +        } else if (message instanceof InputEvent.PumpFailureAc
          +knowledgement) {                                              
      188 +            InputEvent.PumpFailureAcknowledgement pfa = (Input
          +Event.PumpFailureAcknowledgement) message;                    
      189 +            noteFailureAck(pumpUnits, pfa.pump());            
      190 +        } else if (message instanceof InputEvent.PumpControlFa
          +ilureAcknowledgement) {                                       
      191 +            InputEvent.PumpControlFailureAcknowledgement pcfa 
          +=                                                             
      192 +                    (InputEvent.PumpControlFailureAcknowledgem
          +ent) message;                                                 
      193 +            noteFailureAck(controlUnits, pcfa.pump());        
      194 +        } else if (message instanceof InputEvent.LevelFailureA
          +cknowledgement) {                                             
      195 +            levelUnit.failureAckReceived = true;              
      196 +        } else if (message instanceof InputEvent.SteamOutcomeF
          +ailureAcknowledgement) {                                      
      197 +            steamUnit.failureAckReceived = true;              
      198 +        }                                                     
      199 +    }                                                         
      200 +                                                              
      201 +    /** Analyses the messages of the current transmission (SB-
          +FR1..SB-FR5, SB-V5..SB-V10). */                               
      202 +    public void analyse() {                                   
      203 +        // SB-V10: successive transmissions carrying STOP.    
      204 +        if (stopReceived) {                                   
      205 +            if (stopCount < BoilerConstants.STOP_REPETITIONS) 
          +{                                                             
      206 +                stopCount = stopCount + 1;                    
      207 +            }                                                 
      208 +        } else {                                              
      209 +            stopCount = 0;                                    
      210 +        }                                                     
      211 +                                                              
      212 +        // SB-FR5: aberrant presence of a message.            
      213 +        if (waitingReceived && modeSentPrevious != BoilerMode.
          +INITIALIZATION) {                                             
      214 +            aberrantMessage = true;                           
      215 +        }                                                     
      216 +        if (unitsReadyReceived                                
      217 +                && !(modeSentPrevious == BoilerMode.INITIALIZA
          +TION && programReadySentPrevious)) {                          
      218 +            aberrantMessage = true;                           
      219 +        }                                                     
      220 +                                                              
      221 +        // SB-FR6 acknowledgements, then SB-FR7 repairs.      
      222 +        acknowledge(levelUnit);                               
      223 +        acknowledge(steamUnit);                               
      224 +        for (int i = 0; i < PUMPS; i++) {                     
      225 +            acknowledge(pumpUnits[i]);                        
      226 +            acknowledge(controlUnits[i]);                     
      227 +        }                                                     
      228 +        repair(levelUnit);                                    
      229 +        repair(steamUnit);                                    
      230 +        for (int i = 0; i < PUMPS; i++) {                     
      231 +            boolean pumpBackInService = pumpUnits[i].repairedR
          +eceived && pumpUnits[i].outOfOrder;                           
      232 +            repair(pumpUnits[i]);                             
      233 +            repair(controlUnits[i]);                          
      234 +            if (pumpBackInService && pumpStateMessages[i] == 1
          +) {                                                           
      235 +                // A repaired pump's reported state becomes th
          +e new commanded state.                                        
      236 +                pumpCommanded[i] = pumpReportedOn[i];         
      237 +                cyclesSinceOrder[i] = 1;                      
      238 +            }                                                 
      239 +        }                                                     
      240 +                                                              
      241 +        // SB-FR5: indispensable messages present exactly once
          +.                                                             
      242 +        boolean dataComplete = levelMessages == 1 && steamMess
          +ages == 1;                                                    
      243 +        for (int i = 0; i < PUMPS; i++) {                     
      244 +            dataComplete = dataComplete && pumpStateMessages[i
          +] == 1 && controlStateMessages[i] == 1;                       
      245 +        }                                                     
      246 +        transmissionFailed = aberrantMessage || !dataComplete;
      247 +        if (dataComplete) {                                   
      248 +            analyseReadings();                                
      249 +        }                                                     
      250 +    }                                                         
      251 +                                                              
      252 +    private void analyseReadings() {                          
      253 +        double dt = BoilerConstants.CYCLE_SECONDS;            
      254 +        double previousSteamLow = steamRateLow;               
      255 +        double previousSteamHigh = steamRateHigh;             
      256 +                                                              
      257 +        // SB-FR4: steam device, static range and gradients U1
          +, U2.                                                         
      258 +        v = steamValue;                                       
      259 +        boolean steamStaticBad = v < 0.0 || v > BoilerConstant
          +s.MAX_STEAM_W;                                                
      260 +        boolean steamDynamicBad = v > previousSteamHigh + Boil
          +erConstants.STEAM_INCREASE_U1 * dt                            
      261 +                || v < previousSteamLow - BoilerConstants.STEA
          +M_DECREASE_U2 * dt;                                           
      262 +        if (!steamUnit.outOfOrder && (steamStaticBad || steamD
          +ynamicBad)) {                                                 
      263 +            steamUnit.markFailed();                           
      264 +        }                                                     
      265 +        if (!steamUnit.outOfOrder) {                          
      266 +            steamRateLow = v;                                 
      267 +            steamRateHigh = v;                                
      268 +        } else {                                              
      269 +            steamRateLow = Math.max(0.0, previousSteamLow - Bo
          +ilerConstants.STEAM_DECREASE_U2 * dt);                        
      270 +            steamRateHigh = Math.min(BoilerConstants.MAX_STEAM
          +_W,                                                           
      271 +                    previousSteamHigh + BoilerConstants.STEAM_
          +INCREASE_U1 * dt);                                            
      272 +        }                                                     
      273 +                                                              
      274 +        // SB-FR1: a pump whose state differs from the last or
          +der (or changed spontaneously).                               
      275 +        for (int i = 0; i < PUMPS; i++) {                     
      276 +            if (!pumpUnits[i].outOfOrder && pumpReportedOn[i] 
          +!= pumpCommanded[i]) {                                        
      277 +                pumpUnits[i].markFailed();                    
      278 +            }                                                 
      279 +        }                                                     
      280 +        // SB-FR2: a controller judged from the second transmi
          +ssion after an order on.                                      
      281 +        for (int i = 0; i < PUMPS; i++) {                     
      282 +            if (!controlUnits[i].outOfOrder && !pumpUnits[i].o
          +utOfOrder                                                     
      283 +                    && cyclesSinceOrder[i] >= 2 && controlRepo
          +rtedFlow[i] != pumpCommanded[i]) {                            
      284 +                controlUnits[i].markFailed();                 
      285 +            }                                                 
      286 +        }                                                     
      287 +                                                              
      288 +        // SB-V7 and SB-Beh25: which pumps pour water, honouri
          +ng the start-up latency.                                      
      289 +        int certainCount = 0;                                 
      290 +        int bothCycleCount = 0;                               
      291 +        int couldPourPreviousCount = 0;                       
      292 +        int couldPourNowCount = 0;                            
      293 +        for (int i = 0; i < PUMPS; i++) {                     
      294 +            boolean known = true;                             
      295 +            boolean flowing = false;                          
      296 +            if (!controlUnits[i].outOfOrder) {                
      297 +                flowing = controlReportedFlow[i];             
      298 +            } else if (!pumpUnits[i].outOfOrder) {            
      299 +                flowing = pumpReportedOn[i] && cyclesSinceOrde
          +r[i] >= 2;                                                    
      300 +            } else {                                          
      301 +                known = false;                                
      302 +            }                                                 
      303 +            boolean certainNow = known && flowing;            
      304 +            boolean couldPourNow = !known || flowing || pumpRe
          +portedOn[i];                                                  
      305 +            if (certainNow) {                                 
      306 +                certainCount = certainCount + 1;              
      307 +            }                                                 
      308 +            if (certainNow && certainFlowPrevious[i]) {       
      309 +                bothCycleCount = bothCycleCount + 1;          
      310 +            }                                                 
      311 +            if (couldPourPrevious[i] || couldPourNow) {       
      312 +                couldPourPreviousCount = couldPourPreviousCoun
          +t + 1;                                                        
      313 +            }                                                 
      314 +            if (couldPourNow) {                               
      315 +                couldPourNowCount = couldPourNowCount + 1;    
      316 +            }                                                 
      317 +            certainFlowPrevious[i] = certainNow;              
      318 +            couldPourPrevious[i] = couldPourNow;              
      319 +        }                                                     
      320 +        p = BoilerConstants.PUMP_CAPACITY_P * certainCount;   
      321 +        double pumpVolumeMin = BoilerConstants.PUMP_CAPACITY_P
          + * bothCycleCount * dt;                                       
      322 +        double pumpVolumeMax = BoilerConstants.PUMP_CAPACITY_P
          + * couldPourPreviousCount * dt;                               
      323 +                                                              
      324 +        // SB-FR3: level device, static range and dynamics sin
          +ce the previous cycle.                                        
      325 +        q = levelValue;                                       
      326 +        double expectedLow = levelLow - maxSteamVolume(previou
          +sSteamHigh) + pumpVolumeMin;                                  
      327 +        double expectedHigh = levelHigh - minSteamVolume(previ
          +ousSteamLow) + pumpVolumeMax;                                 
      328 +        boolean levelStaticBad = q < 0.0 || q > BoilerConstant
          +s.CAPACITY_C;                                                 
      329 +        boolean levelDynamicBad = boundsKnown && !valveOpenDur
          +ingCycle                                                      
      330 +                && (q < expectedLow || q > expectedHigh);     
      331 +        if (!levelUnit.outOfOrder && (levelStaticBad || levelD
          +ynamicBad)) {                                                 
      332 +            levelUnit.markFailed();                           
      333 +        }                                                     
      334 +                                                              
      335 +        // SB-V8: level bounds, from the reading or from worst
          +-case dynamics.                                               
      336 +        if (!levelUnit.outOfOrder) {                          
      337 +            levelLow = q;                                     
      338 +            levelHigh = q;                                    
      339 +        } else if (boundsKnown) {                             
      340 +            levelLow = Math.max(0.0, expectedLow);            
      341 +            levelHigh = Math.min(BoilerConstants.CAPACITY_C, e
          +xpectedHigh);                                                 
      342 +        } else {                                              
      343 +            levelLow = 0.0;                                   
      344 +            levelHigh = BoilerConstants.CAPACITY_C;           
      345 +        }                                                     
      346 +        boundsKnown = true;                                   
      347 +                                                              
      348 +        // SB-V4, SB-Beh13: worst-case level at the next cycle
          +. Pumps may stop                                              
      349 +        // (none counted for the low bound); pumps that are on
          + or uncertain may pour.                                       
      350 +        predictedLow = levelLow - maxSteamVolume(steamRateHigh
          +);                                                            
      351 +        predictedHigh = levelHigh - minSteamVolume(steamRateLo
          +w)                                                            
      352 +                + BoilerConstants.PUMP_CAPACITY_P * couldPourN
          +owCount * dt;                                                 
      353 +    }                                                         
      354 +                                                              
      355 +    private static double maxSteamVolume(double rate) {       
      356 +        double dt = BoilerConstants.CYCLE_SECONDS;            
      357 +        return Math.min(BoilerConstants.MAX_STEAM_W * dt,     
      358 +                rate * dt + 0.5 * BoilerConstants.STEAM_INCREA
          +SE_U1 * dt * dt);                                             
      359 +    }                                                         
      360 +                                                              
      361 +    private static double minSteamVolume(double rate) {       
      362 +        double dt = BoilerConstants.CYCLE_SECONDS;            
      363 +        return Math.max(0.0, rate * dt - 0.5 * BoilerConstants
          +.STEAM_DECREASE_U2 * dt * dt);                                
      364 +    }                                                         
      365 +                                                              
      366 +    private static boolean validPump(int pump) {              
      367 +        return pump >= 1 && pump <= PUMPS;                    
      368 +    }                                                         
      369 +                                                              
      370 +    private void noteRepaired(UnitStatus[] units, int pump) { 
      371 +        if (validPump(pump)) {                                
      372 +            units[pump - 1].repairedReceived = true;          
      373 +        } else {                                              
      374 +            aberrantMessage = true;                           
      375 +        }                                                     
      376 +    }                                                         
      377 +                                                              
      378 +    private void noteFailureAck(UnitStatus[] units, int pump) 
          +{                                                             
      379 +        if (validPump(pump)) {                                
      380 +            units[pump - 1].failureAckReceived = true;        
      381 +        } else {                                              
      382 +            aberrantMessage = true;                           
      383 +        }                                                     
      384 +    }                                                         
      385 +                                                              
      386 +    private void acknowledge(UnitStatus unit) {               
      387 +        if (unit.failureAckReceived) {                        
      388 +            if (unit.announcementPending()) {                 
      389 +                unit.failureAcknowledged = true;              
      390 +            } else {                                          
      391 +                aberrantMessage = true;                       
      392 +            }                                                 
      393 +        }                                                     
      394 +    }                                                         
      395 +                                                              
      396 +    private void repair(UnitStatus unit) {                    
      397 +        if (unit.repairedReceived) {                          
      398 +            if (unit.outOfOrder) {                            
      399 +                unit.outOfOrder = false;                      
      400 +                unit.failureAcknowledged = true;              
      401 +                unit.repairAcknowledgementDue = true;         
      402 +            } else {                                          
      403 +                aberrantMessage = true;                       
      404 +            }                                                 
      405 +        }                                                     
      406 +    }                                                         
      407 +                                                              
      408 +    /** Records that the program ordered pump {@code pump} on 
          +or off in this cycle. */                                      
      409 +    public void notePumpOrder(@RoboChartType("nat") int pump, 
          +boolean switchOn) {                                           
      410 +        if (validPump(pump)) {                                
      411 +            pumpCommanded[pump - 1] = switchOn;               
      412 +            pumpOrderedCurrent[pump - 1] = true;              
      413 +        }                                                     
      414 +    }                                                         
      415 +                                                              
      416 +    /** Records the MODE message sent in this cycle. */       
      417 +    public void noteModeSent(BoilerMode mode) {               
      418 +        modeSentCurrent = mode;                               
      419 +    }                                                         
      420 +                                                              
      421 +    /** Records that PROGRAM_READY was sent in this cycle. */ 
      422 +    public void noteProgramReadySent() {                      
      423 +        programReadySentCurrent = true;                       
      424 +    }                                                         
      425 +                                                              
      426 +    /** Records the evacuation valve position requested in thi
          +s cycle. */                                                   
      427 +    public void noteValve(boolean open) {                     
      428 +        valveOpen = open;                                     
      429 +    }                                                         
      430 +                                                              
      431 +    /** Whether the last order to pump {@code pump} was to swi
          +tch it on. */                                                 
      432 +    public boolean pumpCommandedOn(@RoboChartType("nat") int p
          +ump) {                                                        
      433 +        boolean commanded = false;                            
      434 +        if (validPump(pump)) {                                
      435 +            commanded = pumpCommanded[pump - 1];              
      436 +        }                                                     
      437 +        return commanded;                                     
      438 +    }                                                         
      439 +                                                              
      440 +    /** Failure announcements still unacknowledged (SB-FR6) an
          +d repair acknowledgements due (SB-FR7). */                    
      441 +    public List<OutputEvent> statusMessagesDue() {            
      442 +        List<OutputEvent> due = new ArrayList<>();            
      443 +        for (int i = 0; i < PUMPS; i++) {                     
      444 +            if (pumpUnits[i].announcementPending()) {         
      445 +                due.add(new OutputEvent.PumpFailureDetection(i
          + + 1));                                                       
      446 +            }                                                 
      447 +            if (controlUnits[i].announcementPending()) {      
      448 +                due.add(new OutputEvent.PumpControlFailureDete
          +ction(i + 1));                                                
      449 +            }                                                 
      450 +            if (pumpUnits[i].repairAcknowledgementDue) {      
      451 +                due.add(new OutputEvent.PumpRepairedAcknowledg
          +ement(i + 1));                                                
      452 +            }                                                 
      453 +            if (controlUnits[i].repairAcknowledgementDue) {   
      454 +                due.add(new OutputEvent.PumpControlRepairedAck
          +nowledgement(i + 1));                                         
      455 +            }                                                 
      456 +        }                                                     
      457 +        if (levelUnit.announcementPending()) {                
      458 +            due.add(new OutputEvent.LevelFailureDetection()); 
      459 +        }                                                     
      460 +        if (steamUnit.announcementPending()) {                
      461 +            due.add(new OutputEvent.SteamFailureDetection()); 
      462 +        }                                                     
      463 +        if (levelUnit.repairAcknowledgementDue) {             
      464 +            due.add(new OutputEvent.LevelRepairedAcknowledgeme
          +nt());                                                        
      465 +        }                                                     
      466 +        if (steamUnit.repairAcknowledgementDue) {             
      467 +            due.add(new OutputEvent.SteamRepairedAcknowledgeme
          +nt());                                                        
      468 +        }                                                     
      469 +        return due;                                           
      470 +    }                                                         
      471 +                                                              
      472 +    /** Number of successive transmissions that carried STOP (
          +SB-V10). */                                                   
      473 +    @RoboChartType("nat")                                     
      474 +    public int successiveStops() {                            
      475 +        return stopCount;                                     
      476 +    }                                                         
      477 +                                                              
      478 +    /** Whether the current transmission failed: an aberrant o
          +r a missing message (SB-FR5). */                              
      479 +    public boolean transmissionFailure() {                    
      480 +        return transmissionFailed;                            
      481 +    }                                                         
      482 +                                                              
      483 +    /** Whether STEAM_BOILER_WAITING arrived in the current tr
          +ansmission. */                                                
      484 +    public boolean steamBoilerWaitingReceived() {             
      485 +        return waitingReceived;                               
      486 +    }                                                         
      487 +                                                              
      488 +    /** Whether PHYSICAL_UNITS_READY arrived in the current tr
          +ansmission. */                                                
      489 +    public boolean physicalUnitsReadyReceived() {             
      490 +        return unitsReadyReceived;                            
      491 +    }                                                         
      492 +                                                              
      493 +    /** q: water level most recently reported (SB-V5). */     
      494 +    @RoboChartType("real")                                    
      495 +    public double levelReading() {                            
      496 +        return q;                                             
      497 +    }                                                         
      498 +                                                              
      499 +    /** v: steam rate most recently reported (SB-V6). */      
      500 +    @RoboChartType("real")                                    
      501 +    public double steamReading() {                            
      502 +        return v;                                             
      503 +    }                                                         
      504 +                                                              
      505 +    /** p: current throughput of the pumps (SB-V7). */        
      506 +    @RoboChartType("real")                                    
      507 +    public double pumpThroughput() {                          
      508 +        return p;                                             
      509 +    }                                                         
      510 +                                                              
      511 +    /** Lower bound on the water level (SB-V8). */            
      512 +    @RoboChartType("real")                                    
      513 +    public double levelLowerBound() {                         
      514 +        return levelLow;                                      
      515 +    }                                                         
      516 +                                                              
      517 +    /** Upper bound on the water level (SB-V8). */            
      518 +    @RoboChartType("real")                                    
      519 +    public double levelUpperBound() {                         
      520 +        return levelHigh;                                     
      521 +    }                                                         
      522 +                                                              
      523 +    /** Level used for regulation: the reading, or the middle 
          +of the estimate (SB-V8, SB-Beh19). */                         
      524 +    @RoboChartType("real")                                    
      525 +    public double levelEstimate() {                           
      526 +        return (levelLow + levelHigh) / 2.0;                  
      527 +    }                                                         
      528 +                                                              
      529 +    /** Worst-case lowest level at the next cycle (SB-Beh13). 
          +*/                                                            
      530 +    @RoboChartType("real")                                    
      531 +    public double predictedLevelLow() {                       
      532 +        return predictedLow;                                  
      533 +    }                                                         
      534 +                                                              
      535 +    /** Worst-case highest level at the next cycle (SB-Beh13).
          + */                                                           
      536 +    @RoboChartType("real")                                    
      537 +    public double predictedLevelHigh() {                      
      538 +        return predictedHigh;                                 
      539 +    }                                                         
      540 +                                                              
      541 +    /** Whether the level measuring device is out of order (SB
          +-FR3, SB-V9). */                                              
      542 +    public boolean levelDeviceFailed() {                      
      543 +        return levelUnit.outOfOrder;                          
      544 +    }                                                         
      545 +                                                              
      546 +    /** Whether the steam measuring device is out of order (SB
          +-FR4, SB-V9). */                                              
      547 +    public boolean steamDeviceFailed() {                      
      548 +        return steamUnit.outOfOrder;                          
      549 +    }                                                         
      550 +                                                              
      551 +    /** Whether pump {@code pump} is out of order (SB-FR1, SB-
          +V9). */                                                       
      552 +    public boolean pumpFailed(@RoboChartType("nat") int pump) 
          +{                                                             
      553 +        boolean failed = false;                               
      554 +        if (validPump(pump)) {                                
      555 +            failed = pumpUnits[pump - 1].outOfOrder;          
      556 +        }                                                     
      557 +        return failed;                                        
      558 +    }                                                         
      559 +                                                              
      560 +    /** Whether the controller of pump {@code pump} is out of 
          +order (SB-FR2, SB-V9). */                                     
      561 +    public boolean pumpControllerFailed(@RoboChartType("nat") 
          +int pump) {                                                   
      562 +        boolean failed = false;                               
      563 +        if (validPump(pump)) {                                
      564 +            failed = controlUnits[pump - 1].outOfOrder;       
      565 +        }                                                     
      566 +        return failed;                                        
      567 +    }                                                         
      568 +                                                              
      569 +    /** Whether some pump is out of order. */                 
      570 +    public boolean anyPumpFailed() {                          
      571 +        boolean failed = false;                               
      572 +        for (int i = 0; i < PUMPS; i++) {                     
      573 +            failed = failed || pumpUnits[i].outOfOrder;       
      574 +        }                                                     
      575 +        return failed;                                        
      576 +    }                                                         
      577 +                                                              
      578 +    /** Whether some pump controller is out of order. */      
      579 +    public boolean anyPumpControllerFailed() {                
      580 +        boolean failed = false;                               
      581 +        for (int i = 0; i < PUMPS; i++) {                     
      582 +            failed = failed || controlUnits[i].outOfOrder;    
      583 +        }                                                     
      584 +        return failed;                                        
      585 +    }                                                         
      586  }

⏺ Next, the actuator.

⏺ Write(java.generated.project/src/main/java/steam_boiler/actuator/BoilerActuato
       r.java)
  ⎿  Added 84 lines, removed 12 lines
       8  import steam_boiler.mode.BoilerMode;
       9  import steam_boiler.sensor.BoilerSensor;
      10  
      11 -/** Transmission stage (skeleton). */                          
      11 +/**                                                            
      12 + * Transmission stage of the program (SB-V3, SB-DM8). The contr
         +oller's commands                                               
      13 + * are collected into the outgoing transmission of the current 
         +cycle; every                                                   
      14 + * order is recorded with the sensor stage so that the next cyc
         +le's analysis can                                              
      15 + * judge the units' answers (SB-FR1, SB-FR2, SB-FR5). Pump and 
         +valve orders are                                               
      16 + * sent only when they change the commanded state.             
      17 + */                                                            
      18  public final class BoilerActuator {
      19 +                                                               
      20      private final BoilerSensor sensor;
      14 -    public BoilerActuator(BoilerSensor sensor) { this.sensor = 
         -sensor; }                                                      
      15 -    public void beginTransmission() { }                        
      16 -    public List<OutputEvent> transmission() { return new ArrayL
         -ist<>(); }                                                     
      17 -    public void broadcastMode(BoilerMode mode) { }             
      18 -    public void announceProgramReady() { }                     
      19 -    public void commandValveOpen() { }                         
      20 -    public void commandValveClosed() { }                       
      21 -    public void commandPumpOn(@RoboChartType("nat") int pump) {
         - }                                                             
      22 -    public void commandPumpOff(@RoboChartType("nat") int pump) 
         -{ }                                                            
      23 -    public void reportUnitStatus() { }                         
      24 -    public ValvePosition valvePosition() { return ValvePosition
         -.CLOSED; }                                                     
      21 +    private final List<OutputEvent> outgoing = new ArrayList<>(
         +);                                                             
      22 +    private ValvePosition valve = ValvePosition.CLOSED;        
      23 +                                                               
      24 +    /** Creates the transmission stage that reports to the give
         +n sensor stage. */                                             
      25 +    public BoilerActuator(BoilerSensor sensor) {               
      26 +        this.sensor = sensor;                                  
      27 +    }                                                          
      28 +                                                               
      29 +    /** Starts the outgoing transmission of a new cycle. */    
      30 +    public void beginTransmission() {                          
      31 +        outgoing.clear();                                      
      32 +    }                                                          
      33 +                                                               
      34 +    /** The messages of the current outgoing transmission, all 
         +emitted together. */                                           
      35 +    public List<OutputEvent> transmission() {                  
      36 +        return new ArrayList<>(outgoing);                      
      37 +    }                                                          
      38 +                                                               
      39 +    /** MODE(m): the current operating mode, sent on every cycl
         +e (SB-Beh24). */                                               
      40 +    public void broadcastMode(BoilerMode mode) {               
      41 +        outgoing.add(new OutputEvent.ModeMessage(mode));       
      42 +        sensor.noteModeSent(mode);                             
      43 +    }                                                          
      44 +                                                               
      45 +    /** PROGRAM_READY (SB-Beh7). */                            
      46 +    public void announceProgramReady() {                       
      47 +        outgoing.add(new OutputEvent.ProgramReady());          
      48 +        sensor.noteProgramReadySent();                         
      49 +    }                                                          
      50 +                                                               
      51 +    /** VALVE, to open the evacuation valve if it is closed (SB
         +-Beh4). */                                                     
      52 +    public void commandValveOpen() {                           
      53 +        if (valve == ValvePosition.CLOSED) {                   
      54 +            outgoing.add(new OutputEvent.Valve());             
      55 +            valve = ValvePosition.OPEN;                        
      56 +            sensor.noteValve(true);                            
      57 +        }                                                      
      58 +    }                                                          
      59 +                                                               
      60 +    /** VALVE, to close the evacuation valve if it is open (SB-
         +DM7). */                                                       
      61 +    public void commandValveClosed() {                         
      62 +        if (valve == ValvePosition.OPEN) {                     
      63 +            outgoing.add(new OutputEvent.Valve());             
      64 +            valve = ValvePosition.CLOSED;                      
      65 +            sensor.noteValve(false);                           
      66 +        }                                                      
      67 +    }                                                          
      68 +                                                               
      69 +    /** OPEN_PUMP(n), if pump n is not already commanded on. */
      70 +    public void commandPumpOn(@RoboChartType("nat") int pump) {
      71 +        if (!sensor.pumpCommandedOn(pump)) {                   
      72 +            outgoing.add(new OutputEvent.OpenPump(pump));      
      73 +            sensor.notePumpOrder(pump, true);                  
      74 +        }                                                      
      75 +    }                                                          
      76 +                                                               
      77 +    /** CLOSE_PUMP(n), if pump n is commanded on. */           
      78 +    public void commandPumpOff(@RoboChartType("nat") int pump) 
         +{                                                              
      79 +        if (sensor.pumpCommandedOn(pump)) {                    
      80 +            outgoing.add(new OutputEvent.ClosePump(pump));     
      81 +            sensor.notePumpOrder(pump, false);                 
      82 +        }                                                      
      83 +    }                                                          
      84 +                                                               
      85 +    /** Failure detections not yet acknowledged and repair ackn
         +owledgements due (SB-FR6, SB-FR7). */                          
      86 +    public void reportUnitStatus() {                           
      87 +        List<OutputEvent> due = sensor.statusMessagesDue();    
      88 +        for (int i = 0; i < due.size(); i++) {                 
      89 +            outgoing.add(due.get(i));                          
      90 +        }                                                      
      91 +    }                                                          
      92 +                                                               
      93 +    /** The commanded position of the evacuation valve. */     
      94 +    public ValvePosition valvePosition() {                     
      95 +        return valve;                                          
      96 +    }                                                          
      97  }

⏺ Bash(cd
      /Users/ranwei/Gitee/forge-v3c16-run9-steam_boiler/java.generated.project
      && S=/private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3c16-run9-steam-bo
      iler/85130…)
  ⎿  (No output)

⏺ The leaves compile and the trace is refreshed.

  Layer 4: controller

  Next, step(), built as the mode-nested if-else with named predicates.
⏺ Write(java.generated.project/src/main/java/steam_boiler/controller/SteamBoiler
       Controller.java)
  ⎿  Added 215 lines, removed 3 lines
        5  import steam_boiler.mode.BoilerMode;
        6  import steam_boiler.sensor.BoilerSensor;
        7  
        8 -/** Steam-boiler mode controller (skeleton). */               
        8 +/**                                                           
        9 + * The steam-boiler control program (SB-V1). Once per five-sec
          +ond cycle, after                                              
       10 + * the sensor stage has received and analysed the incoming tra
          +nsmission, the                                                
       11 + * transmission system calls {@link #step} with               
       12 + * {@link InputEvent.TransmissionComplete}; the controller dec
          +ides the                                                      
       13 + * operating mode (SB-V2) and issues the cycle's outgoing mess
          +ages.                                                         
       14 + */                                                           
       15  public final class SteamBoilerController {
       16 +                                                              
       17      private BoilerMode currentMode = BoilerMode.INITIALIZATION
           ;
       18 +    /** STEAM_BOILER_WAITING has arrived (SB-Beh2). */        
       19      private boolean plantWaiting = false;
       20 +    /** PROGRAM_READY has been announced (SB-Beh7). */        
       21      private boolean readyAnnounced = false;
       22      private final BoilerSensor sensor;
       23      private final BoilerActuator actuator;
       24  
       25 +    /** Creates the controller in initialization mode (SB-Beh1
          +). */                                                         
       26      public SteamBoilerController(BoilerSensor sensor, BoilerAc
           tuator actuator) {
       27          this.sensor = sensor;
       28          this.actuator = actuator;
       29      }
       30  
       21 -    public BoilerMode currentMode() { return currentMode; }   
       31 +    /** The current operating mode. */                        
       32 +    public BoilerMode currentMode() {                         
       33 +        return currentMode;                                   
       34 +    }                                                         
       35  
       23 -    public void step(InputEvent event) { }                    
       36 +    /** One control cycle: analyse the received information an
          +d transmit. */                                                
       37 +    public void step(InputEvent event) {                      
       38 +        // --- Named predicates ---                           
       39 +        boolean operatorStop = sensor.successiveStops() >= Boi
          +lerConstants.STOP_REPETITIONS;                                
       40 +        boolean transmissionFault = sensor.transmissionFailure
          +();                                                           
       41 +        boolean waitingNow = sensor.steamBoilerWaitingReceived
          +();                                                           
       42 +        boolean plantStarted = plantWaiting || waitingNow;    
       43 +        boolean steamNotZero = sensor.steamReading() != 0.0;  
       44 +        boolean levelFault = sensor.levelDeviceFailed();      
       45 +        boolean steamFault = sensor.steamDeviceFailed();      
       46 +        boolean pumpFault = sensor.anyPumpFailed();           
       47 +        boolean pumpControlFault = sensor.anyPumpControllerFai
          +led();                                                        
       48 +        boolean otherUnitFault = steamFault || pumpFault || pu
          +mpControlFault;                                               
       49 +        boolean unitsReady = sensor.physicalUnitsReadyReceived
          +();                                                           
       50 +        boolean handshakeDone = readyAnnounced && unitsReady; 
       51 +        boolean initAbort = operatorStop                      
       52 +                || (plantStarted && (transmissionFault || stea
          +mNotZero || levelFault));                                     
       53 +        boolean readingAboveNormal = sensor.levelReading() > B
          +oilerConstants.MAX_NORMAL_N2;                                 
       54 +        boolean readingBelowNormal = sensor.levelReading() < B
          +oilerConstants.MIN_NORMAL_N1;                                 
       55 +        boolean levelRiskLow = sensor.predictedLevelLow() < Bo
          +ilerConstants.MIN_LIMIT_M1;                                   
       56 +        boolean levelRiskHigh = sensor.predictedLevelHigh() > 
          +BoilerConstants.MAX_LIMIT_M2;                                 
       57 +        boolean emergencyNeeded = operatorStop || transmission
          +Fault || levelRiskLow || levelRiskHigh;                       
       58 +        boolean rescueEmergency = emergencyNeeded || steamFaul
          +t || pumpControlFault;                                        
       59 +        boolean waterLow = sensor.levelEstimate() < BoilerCons
          +tants.MIN_NORMAL_N1;                                          
       60 +        boolean waterHigh = sensor.levelEstimate() > BoilerCon
          +stants.MAX_NORMAL_N2;                                         
       61 +                                                              
       62 +        // --- Mode-nested transitions ---                    
       63 +        if (currentMode == BoilerMode.INITIALIZATION) {       
       64 +            if (event instanceof InputEvent.TransmissionComple
          +te && initAbort) {                                            
       65 +                currentMode = BoilerMode.EMERGENCY_STOP;      
       66 +                actuator.broadcastMode(BoilerMode.EMERGENCY_ST
          +OP);                                                          
       67 +            } else if (event instanceof InputEvent.Transmissio
          +nComplete && !initAbort && !plantStarted) {                   
       68 +                currentMode = BoilerMode.INITIALIZATION;      
       69 +                actuator.broadcastMode(BoilerMode.INITIALIZATI
          +ON);                                                          
       70 +            } else if (event instanceof InputEvent.Transmissio
          +nComplete && !initAbort && plantStarted                       
       71 +                    && handshakeDone && !otherUnitFault) {    
       72 +                currentMode = BoilerMode.NORMAL;              
       73 +                actuator.broadcastMode(BoilerMode.NORMAL);    
       74 +                actuator.commandValveClosed();                
       75 +                actuator.reportUnitStatus();                  
       76 +            } else if (event instanceof InputEvent.Transmissio
          +nComplete && !initAbort && plantStarted                       
       77 +                    && handshakeDone && otherUnitFault) {     
       78 +                currentMode = BoilerMode.DEGRADED;            
       79 +                actuator.broadcastMode(BoilerMode.DEGRADED);  
       80 +                actuator.commandValveClosed();                
       81 +                actuator.reportUnitStatus();                  
       82 +            } else if (event instanceof InputEvent.Transmissio
          +nComplete && !initAbort && plantStarted                       
       83 +                    && readyAnnounced && !unitsReady) {       
       84 +                currentMode = BoilerMode.INITIALIZATION;      
       85 +                actuator.broadcastMode(BoilerMode.INITIALIZATI
          +ON);                                                          
       86 +                plantWaiting = true;                          
       87 +                actuator.announceProgramReady();              
       88 +                actuator.reportUnitStatus();                  
       89 +            } else if (event instanceof InputEvent.Transmissio
          +nComplete && !initAbort && plantStarted                       
       90 +                    && !readyAnnounced && readingAboveNormal) 
          +{                                                             
       91 +                currentMode = BoilerMode.INITIALIZATION;      
       92 +                actuator.broadcastMode(BoilerMode.INITIALIZATI
          +ON);                                                          
       93 +                plantWaiting = true;                          
       94 +                actuator.commandPumpOff(1);                   
       95 +                actuator.commandValveOpen();                  
       96 +                actuator.reportUnitStatus();                  
       97 +            } else if (event instanceof InputEvent.Transmissio
          +nComplete && !initAbort && plantStarted                       
       98 +                    && !readyAnnounced && readingBelowNormal) 
          +{                                                             
       99 +                currentMode = BoilerMode.INITIALIZATION;      
      100 +                actuator.broadcastMode(BoilerMode.INITIALIZATI
          +ON);                                                          
      101 +                plantWaiting = true;                          
      102 +                actuator.commandValveClosed();                
      103 +                actuator.commandPumpOn(1);                    
      104 +                actuator.reportUnitStatus();                  
      105 +            } else if (event instanceof InputEvent.Transmissio
          +nComplete && !initAbort && plantStarted                       
      106 +                    && !readyAnnounced && !readingAboveNormal 
          +&& !readingBelowNormal) {                                     
      107 +                currentMode = BoilerMode.INITIALIZATION;      
      108 +                actuator.broadcastMode(BoilerMode.INITIALIZATI
          +ON);                                                          
      109 +                plantWaiting = true;                          
      110 +                readyAnnounced = true;                        
      111 +                actuator.commandValveClosed();                
      112 +                actuator.commandPumpOff(1);                   
      113 +                actuator.announceProgramReady();              
      114 +                actuator.reportUnitStatus();                  
      115 +            }                                                 
      116 +                                                              
      117 +        } else if (currentMode == BoilerMode.NORMAL) {        
      118 +            if (event instanceof InputEvent.TransmissionComple
          +te && emergencyNeeded) {                                      
      119 +                currentMode = BoilerMode.EMERGENCY_STOP;      
      120 +                actuator.broadcastMode(BoilerMode.EMERGENCY_ST
          +OP);                                                          
      121 +            } else if (event instanceof InputEvent.Transmissio
          +nComplete && !emergencyNeeded && levelFault) {                
      122 +                currentMode = BoilerMode.RESCUE;              
      123 +                actuator.broadcastMode(BoilerMode.RESCUE);    
      124 +                actuator.reportUnitStatus();                  
      125 +            } else if (event instanceof InputEvent.Transmissio
          +nComplete && !emergencyNeeded && !levelFault                  
      126 +                    && otherUnitFault) {                      
      127 +                currentMode = BoilerMode.DEGRADED;            
      128 +                actuator.broadcastMode(BoilerMode.DEGRADED);  
      129 +                actuator.reportUnitStatus();                  
      130 +            } else if (event instanceof InputEvent.Transmissio
          +nComplete && !emergencyNeeded && !levelFault                  
      131 +                    && !otherUnitFault && waterLow) {         
      132 +                currentMode = BoilerMode.NORMAL;              
      133 +                actuator.broadcastMode(BoilerMode.NORMAL);    
      134 +                actuator.commandPumpOn(1);                    
      135 +                actuator.commandPumpOn(2);                    
      136 +                actuator.commandPumpOn(3);                    
      137 +                actuator.commandPumpOn(4);                    
      138 +                actuator.reportUnitStatus();                  
      139 +            } else if (event instanceof InputEvent.Transmissio
          +nComplete && !emergencyNeeded && !levelFault                  
      140 +                    && !otherUnitFault && waterHigh) {        
      141 +                currentMode = BoilerMode.NORMAL;              
      142 +                actuator.broadcastMode(BoilerMode.NORMAL);    
      143 +                actuator.commandPumpOff(1);                   
      144 +                actuator.commandPumpOff(2);                   
      145 +                actuator.commandPumpOff(3);                   
      146 +                actuator.commandPumpOff(4);                   
      147 +                actuator.reportUnitStatus();                  
      148 +            } else if (event instanceof InputEvent.Transmissio
          +nComplete && !emergencyNeeded && !levelFault                  
      149 +                    && !otherUnitFault && !waterLow && !waterH
          +igh) {                                                        
      150 +                currentMode = BoilerMode.NORMAL;              
      151 +                actuator.broadcastMode(BoilerMode.NORMAL);    
      152 +                actuator.reportUnitStatus();                  
      153 +            }                                                 
      154 +                                                              
      155 +        } else if (currentMode == BoilerMode.DEGRADED) {      
      156 +            if (event instanceof InputEvent.TransmissionComple
          +te && emergencyNeeded) {                                      
      157 +                currentMode = BoilerMode.EMERGENCY_STOP;      
      158 +                actuator.broadcastMode(BoilerMode.EMERGENCY_ST
          +OP);                                                          
      159 +            } else if (event instanceof InputEvent.Transmissio
          +nComplete && !emergencyNeeded && levelFault) {                
      160 +                currentMode = BoilerMode.RESCUE;              
      161 +                actuator.broadcastMode(BoilerMode.RESCUE);    
      162 +                actuator.reportUnitStatus();                  
      163 +            } else if (event instanceof InputEvent.Transmissio
          +nComplete && !emergencyNeeded && !levelFault                  
      164 +                    && !otherUnitFault) {                     
      165 +                currentMode = BoilerMode.NORMAL;              
      166 +                actuator.broadcastMode(BoilerMode.NORMAL);    
      167 +                actuator.reportUnitStatus();                  
      168 +            } else if (event instanceof InputEvent.Transmissio
          +nComplete && !emergencyNeeded && !levelFault                  
      169 +                    && otherUnitFault && waterLow) {          
      170 +                currentMode = BoilerMode.DEGRADED;            
      171 +                actuator.broadcastMode(BoilerMode.DEGRADED);  
      172 +                actuator.commandPumpOn(1);                    
      173 +                actuator.commandPumpOn(2);                    
      174 +                actuator.commandPumpOn(3);                    
      175 +                actuator.commandPumpOn(4);                    
      176 +                actuator.reportUnitStatus();                  
      177 +            } else if (event instanceof InputEvent.Transmissio
          +nComplete && !emergencyNeeded && !levelFault                  
      178 +                    && otherUnitFault && waterHigh) {         
      179 +                currentMode = BoilerMode.DEGRADED;            
      180 +                actuator.broadcastMode(BoilerMode.DEGRADED);  
      181 +                actuator.commandPumpOff(1);                   
      182 +                actuator.commandPumpOff(2);                   
      183 +                actuator.commandPumpOff(3);                   
      184 +                actuator.commandPumpOff(4);                   
      185 +                actuator.reportUnitStatus();                  
      186 +            } else if (event instanceof InputEvent.Transmissio
          +nComplete && !emergencyNeeded && !levelFault                  
      187 +                    && otherUnitFault && !waterLow && !waterHi
          +gh) {                                                         
      188 +                currentMode = BoilerMode.DEGRADED;            
      189 +                actuator.broadcastMode(BoilerMode.DEGRADED);  
      190 +                actuator.reportUnitStatus();                  
      191 +            }                                                 
      192 +                                                              
      193 +        } else if (currentMode == BoilerMode.RESCUE) {        
      194 +            if (event instanceof InputEvent.TransmissionComple
          +te && rescueEmergency) {                                      
      195 +                currentMode = BoilerMode.EMERGENCY_STOP;      
      196 +                actuator.broadcastMode(BoilerMode.EMERGENCY_ST
          +OP);                                                          
      197 +            } else if (event instanceof InputEvent.Transmissio
          +nComplete && !rescueEmergency && !levelFault                  
      198 +                    && !otherUnitFault) {                     
      199 +                currentMode = BoilerMode.NORMAL;              
      200 +                actuator.broadcastMode(BoilerMode.NORMAL);    
      201 +                actuator.reportUnitStatus();                  
      202 +            } else if (event instanceof InputEvent.Transmissio
          +nComplete && !rescueEmergency && !levelFault                  
      203 +                    && otherUnitFault) {                      
      204 +                currentMode = BoilerMode.DEGRADED;            
      205 +                actuator.broadcastMode(BoilerMode.DEGRADED);  
      206 +                actuator.reportUnitStatus();                  
      207 +            } else if (event instanceof InputEvent.Transmissio
          +nComplete && !rescueEmergency && levelFault                   
      208 +                    && waterLow) {                            
      209 +                currentMode = BoilerMode.RESCUE;              
      210 +                actuator.broadcastMode(BoilerMode.RESCUE);    
      211 +                actuator.commandPumpOn(1);                    
      212 +                actuator.commandPumpOn(2);                    
      213 +                actuator.commandPumpOn(3);                    
      214 +                actuator.commandPumpOn(4);                    
      215 +                actuator.reportUnitStatus();                  
      216 +            } else if (event instanceof InputEvent.Transmissio
          +nComplete && !rescueEmergency && levelFault                   
      217 +                    && waterHigh) {                           
      218 +                currentMode = BoilerMode.RESCUE;              
      219 +                actuator.broadcastMode(BoilerMode.RESCUE);    
      220 +                actuator.commandPumpOff(1);                   
      221 +                actuator.commandPumpOff(2);                   
      222 +                actuator.commandPumpOff(3);                   
      223 +                actuator.commandPumpOff(4);                   
      224 +                actuator.reportUnitStatus();                  
      225 +            } else if (event instanceof InputEvent.Transmissio
          +nComplete && !rescueEmergency && levelFault                   
      226 +                    && !waterLow && !waterHigh) {             
      227 +                currentMode = BoilerMode.RESCUE;              
      228 +                actuator.broadcastMode(BoilerMode.RESCUE);    
      229 +                actuator.reportUnitStatus();                  
      230 +            }                                                 
      231 +                                                              
      232 +        } else if (currentMode == BoilerMode.EMERGENCY_STOP) {
      233 +            // Terminal mode (SB-Beh22): the program has stopp
          +ed; no transition leaves it.                                  
      234 +        }                                                     
      235 +    }                                                         
      236  }

⏺ Next, compile with Gradle and run a scenario smoke test from the scratchpad,
  kept outside the project.

⏺ Bash(cd
      /Users/ranwei/Gitee/forge-v3c16-run9-steam_boiler/java.generated.project
      && S=/private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3c16-run9-steam-bo
      iler/85130…)
  ⎿  (No output)

⏺ Bash(./gradlew -q compileJava --offline; echo "gradle exit $?"; ls
      build/classes/java/main/steam_boiler 2>/dev/null)
  ⎿  (No output)

⏺ Gradle compiles cleanly. Next, the scenario harness in the scratchpad.

  Made 1 scratchpad edit +93 (ctrl+o to expand)

⏺ Bash(S=/private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3c16-run9-steam-boil
      er/85130cee-6d8e-4dbe-8bbc-3711205f82cb/scratchpad && javac --release 17
      -cp $S/classes…)
  ⎿  (No output)

⏺ The bang-bang policy overshoots to 700 L and the M2 check lands right at the
  900 L boundary, exposing a weakness. I'll fix this by basing the close
  decision on the worst-case predicted level against N2 so pumps stop before
  overshooting.

⏺ Bash(F=src/main/java/steam_boiler/controller/SteamBoilerController.java &&
      perl -0pi -e 's/boolean waterLow = sensor\.levelEstimate\(\) <
      BoilerConstants\.MIN_NORMAL…)
  ⎿  (No output)

⏺ Bash(S=/private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3c16-run9-steam-boil
      er/85130cee-6d8e-4dbe-8bbc-3711205f82cb/scratchpad && rm -rf $S/classes
      $S/harness/out…)
  ⎿  (No output)

⏺ Normal mode now holds the level between 300-525 L, while rescue mode's
  widening estimate causes alternating pump cycles — conservative but worth
  noting in the review. I'll refresh the trace, clean up the Gradle build/
  output, and gather line numbers for the review.

⏺ Bash(cd /Users/ranwei/Gitee/forge-v3c16-run9-steam_boiler &&
      S=/private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3c16-run9-steam-boile
      r/85130cee-6d8e-4dbe-8bbc-3711…)
  ⎿  (No output)

⏺ Layer 5: review

  Next, I'll write post_codegen.json and post_codegen.md from a single issue
  table.

  Made 1 scratchpad edit +131 (ctrl+o to expand)

⏺ Bash(S=/private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3c16-run9-steam-boil
      er/85130cee-6d8e-4dbe-8bbc-3711205f82cb/scratchpad && python3 -I
      $S/gen_review.py "$PWD…)
  ⎿  (No output)

⏺ Next, the iteration summary.

⏺ Write(forge.assets/corrections/ITER_SUMMARY.md)
  ⎿  Wrote 26 lines to forge.assets/corrections/ITER_SUMMARY.md
      1 # Steam boiler, pass 9, iteration 1
      2
      3 ## What I wrote
      4
      5 There are 11 files under `steam_boiler/`. No Java was present bef
        orehand.
      6
      7 - **`SteamBoilerController`** holds the mode (initialization firs
        t) and two handshake flags. Its `step` reacts to `InputEvent.Tran
        smissionComplete`, which marks the end of each cycle's reception
     … +20 lines (~2 KB) (ctrl+o to expand)

⏺ Before running the pipeline I'll check the summary's word count, the JSON
  files and a final compile.

⏺ Bash(wc -w forge.assets/corrections/ITER_SUMMARY.md && python3 -I -c "import
      json;json.load(open('java.generated.project/result_codegen.json'));json.lo
      ad(open('forge…)
  ⎿  (No output)

⏺ The summary is 320 words, over the 300 limit, so I'll trim it.

⏺ Write(forge.assets/corrections/ITER_SUMMARY.md)
  ⎿  Added 10 lines, removed 10 lines
       4  
       5  There are 11 files under `steam_boiler/`. No Java was present b
          eforehand.
       6  
       7 -- **`SteamBoilerController`** holds the mode (initialization fi
         -rst) and two handshake flags. Its `step` reacts to `InputEvent.
         -TransmissionComplete`, which marks the end of each cycle's rece
         -ption stage. The guards within each mode exclude one another an
         -d together cover every case. Priority runs emergency stop, then
         - rescue, then degraded, then regulation (SB-FR8). Emergency sto
         -p is terminal.                                                 
       8 -- **`BoilerSensor`** collects each cycle's messages. It judges 
         -pump, controller, level, steam and transmission failures (SB-FR
         -1–5). It maintains q, v, p, the level bounds, the unit status a
         -nd the STOP count (SB-V5–10).                                  
       9 -- **`BoilerActuator`** builds the outgoing transmission. Pump a
         -nd valve orders are sent only when they change the commanded st
         -ate. Failure announcements and repair acknowledgements are emit
         -ted on every cycle (SB-FR6/7).                                 
       7 +- **`SteamBoilerController`** holds the mode (initialization fi
         +rst) and two handshake flags. Its `step` reacts to `InputEvent.
         +TransmissionComplete`, which ends each cycle's reception. The g
         +uards within each mode exclude one another and together cover e
         +very case. Priority runs emergency stop, then rescue, then degr
         +aded, then regulation (SB-FR8). Emergency stop is terminal.    
       8 +- **`BoilerSensor`** collects each cycle's messages. It judges 
         +unit and transmission failures (SB-FR1–5) and maintains q, v, p
         +, the level bounds, the unit status and the STOP count (SB-V5–1
         +0).                                                            
       9 +- **`BoilerActuator`** builds the outgoing transmission. Pump a
         +nd valve orders are sent only on change. Failure announcements 
         +and repair acknowledgements are emitted on every cycle (SB-FR6/
         +7).                                                            
      10  
      11 -Why: per-unit history cannot be written as simple predicates in
         - a two-level if-else. Keeping it in the leaves keeps the contro
         -ller's model state small. Every transition is triggered by an e
         -vent, which avoids divergence.                                 
      11 +Why: per-unit history cannot be written as simple predicates in
         + a two-level if-else. Keeping it in the leaves keeps the contro
         +ller's model small. Every transition is triggered by an event, 
         +which avoids divergence.                                       
      12  
      13  ## Decisions the requirements did not settle
      14  
      15  - **Constants:** C=1000, M1=100, M2=900, N1=400, N2=600, W=25, 
          U1=U2=2, P=10, cycle 5 s, STOP threshold 3.
      16 -- **Regulation:** all four pumps on below N1; all off when the 
         -worst-case next-cycle level exceeds N2.                        
      17 -- **Level risk:** for the low limit, steam is at its maximum an
         -d no pump pours. For the high limit, every pump that is on, flo
         -wing or uncertain pours.                                       
      18 -- **Initialization:** checks begin only after STEAM_BOILER_WAIT
         -ING arrives. Pump 1 fills. The level-dynamics check is skipped 
         -while the valve is open, because no evacuation rate is given.  
      16 +- **Regulation:** all pumps on below N1; all off when the worst
         +-case next-cycle level exceeds N2.                             
      17 +- **Level risk:** low limit assumes maximal steam and no pumpin
         +g; high limit assumes every pump that is on, flowing or uncerta
         +in pours.                                                      
      18 +- **Initialization:** checks begin after STEAM_BOILER_WAITING. 
         +Pump 1 fills. Level dynamics are not checked while the valve is
         + open (no evacuation rate given).                              
      19  - **Rescue:** any pump-controller failure means emergency stop.
      20 -- **After emergency stop:** no MODE message is sent.           
      21 -- **Aberrant messages:** the list is my own choice, given in `p
         -ost_codegen.md`.                                               
      20 +- **After emergency stop:** no MODE message.                   
      21 +- **Aberrant messages:** my own list, in `post_codegen.md`.    
      22  
      23  ## Not resolved
      24  
      25  - The constants are placeholders.
      26 -- In rescue mode the widening estimate makes the pumps alternat
         -e on and off every cycle.                                      
      26 +- In rescue mode the widening estimate makes the pumps alternat
         +e on and off.                                                  

  Listed 1 directory (ctrl+o to expand)

⏺ Everything is written. Now the single runner invocation. I can't see a token
  figure for this session, so I'm passing unavailable.

⏺ Bash(export FORGE_ARM_DIR=/Users/ranwei/Gitee/formal_method_guided_vibe_coding
      /experiments/v3-cold && export FORGE_WT_PREFIX=forge-v3c16 && export
      FORGE_STAGE_DIR=/U…)
  ⎿  Running in the background (↓ to manage)

⏺ The runner is going in the background. I'll wait for it to finish before
  reading its output.

⏺ The pipeline runner is still going in the background. I'll report back when it
  finishes.
⏺ Background command "Run the pipeline runner once" completed (exit code 0)

  Searched for 1 pattern (ctrl+o to expand)

⏺ The pipeline ran once and some phase failed. I don't know which phase, or why,
  because I'm not allowed to open the correction reports. The runner exited
  with code 1. The line it printed after === iteration 1 : what to do next ===,
  verbatim:

    Some phase failed. Read ONLY these, inside your worktree:

  As instructed, I stopped there: the Java is unchanged and the runner was not
  run again.

  Pre-write check. Every line read OK: pinned commit, no Java present, study is
  steam_boiler, only this study's inputs, no archive. There was no Java to
  delete. The study has no system/system_description.txt, so the requirements
  were the whole input.

  What I wrote. The five layers were done in order. The code compiles with
  Gradle and javac, and a scenario run outside the project behaved as intended.
  It went through the start-up handshake, normal regulation, a level-device
  failure into rescue and repair, and three STOPs into emergency stop.
  - 11 Java files under java.generated.project/src/main/java/steam_boiler/:
    - The controller holds only the mode and two start-up flags. Every
      transition fires on one end-of-cycle event, which keeps the model small
      and avoids divergence.
    - Failure detection and the per-unit bookkeeping are in the sensor class.
    - Outgoing messages are built by the actuator class.
  - java.generated.project/result_codegen.json: 152 entries covering all 53
    requirements and all 11 files.
  - forge.assets/corrections/post_codegen.md and .json: status uncertain, 12
    issues.
  - forge.assets/corrections/ITER_SUMMARY.md: 289 words.

  Main open points:
  - Constants: the requirements give no values for C, M1, M2, N1, N2, W, U1, U2
    or P, so I chose placeholders.
  - Rescue mode: the estimated level widens every cycle, so the pumps switch on
    and off on alternate cycles.

  I passed unavailable as the token figure because this session showed none.

  ITER_1_COMPLETE: 15 files written