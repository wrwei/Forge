
❯ # Reference run — sranger, run 4

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
  /Users/ranwei/Gitee/forge-v3r7-run4-sranger
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
  cd /Users/ranwei/Gitee/forge-v3r7-run4-sranger 2>/dev/null || { echo "FAIL no
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
  _claude_iteration.sh sranger 4 <iteration> <tokens|unavailable>`.
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

⏺ Bash(cd /Users/ranwei/Gitee/forge-v3r7-run4-sranger 2>/dev/null || { echo
      "FAIL no worktree"; exit 1; }
      export FORGE_ARM_DIR=/Users/ranwei/Gitee/formal_method_guided…)
  ⎿  (file diff unavailable for this command)

⏺ All checks passed (output above). Now reading the inputs.

⏺ Bash(cd /Users/ranwei/Gitee/forge-v3r7-run4-sranger; cat
      forge.assets/case-studies/sranger/system/system_description.txt; echo
      ----; cat forge.assets/case-studies/sr…)
  ⎿  (No output)

⏺ Bash(cd /Users/ranwei/Gitee/forge-v3r7-run4-sranger; cat
      forge.assets/case-studies/sranger/requirements/requirement_all.json)
  ⎿  (No output)

⏺ Bash(cd /Users/ranwei/Gitee/forge-v3r7-run4-sranger; cat
      forge.assets/prompts/java_codegen_rules.txt)
  ⎿  (No output)

⏺ Bash(cd /Users/ranwei/Gitee/forge-v3r7-run4-sranger; cat
      forge.assets/prompts/chain_of_thought_codegen.txt; echo -----; cat
      forge.assets/prompts/codegen_trace_rules.…)
  ⎿  (No output)

⏺ Bash(cd /Users/ranwei/Gitee/forge-v3r7-run4-sranger; cat
      forge.assets/prompts/few_shot_codegen.txt)
  ⎿  (No output)

⏺ Bash(cd /Users/ranwei/Gitee/forge-v3r7-run4-sranger; for f in
      forge.assets/vibe-coding-prompts/*.md; do echo "===== $f"; cat "$f"; done)
  ⎿  (file diff unavailable for this command)

⏺ Bash(cd /Users/ranwei/Gitee/forge-v3r7-run4-sranger; cat
      forge.assets/prompts/fdr4_system.txt; echo ----; cat
      java.generated.project/build.gradle java.generated.proj…)
  ⎿  (No output)

⏺ Bash(cd /Users/ranwei/Gitee/forge-v3r7-run4-sranger; ls -la
      forge.assets/corrections/ 2>&1 | head; ls java.generated.project/src 2>&1;
      java -version 2>&1 | head -2;…)
  ⎿  (No output)

⏺ Bash(cd /Users/ranwei/Gitee/forge-v3r7-run4-sranger; cat
      forge.assets/corrections/csp_overrides.csp)
  ⎿  (No output)

⏺ Pre-flight output:

  OK   pinned commit
  OK   no Java present
  OK   study is sranger
  OK   only this study's inputs
  OK   no archive

  No Java was present, so there was nothing to delete. One thing I noticed:
  forge.assets/corrections/csp_overrides.csp is a file at the pinned commit
  whose header says it is for the LRE controller, a different study. I haven't
  changed it. I'll mention it in the summary.

  Layer 1: Architecture

  Base package sranger:
  - annotation: RoboChartType. Supports the architecture and the real-typed
    constants (SR-ARCH2, SR-DM2).
  - event: InputEvent is a sealed interface with the records Obstacle, Tick and
    EndTask, all signals (SR-DM3). OutputEvent is sealed with one record,
    Move(double lv, double av) (SR-DM4).
  - sensor: Sensor. distance() returns real; update(double) and clearReading()
    feed it. With no reading it returns a large default (SR-DM5, SR-SF1).
  - actuator: Actuator. apply(OutputEvent) stores the last command;
    lastLinearVelocity() and lastAngularVelocity() read it back (SR-DM6).
  - time: Clock, with now() returning seconds. This is the profile's clock
    convention, used for clockResetTime and since (SR-Var1, SR-GP2).
  - controller:
    - SRangerMode {MOVING, TURNING, FINAL} (SR-DM1, SR-FR1–3).
    - SRangerConstants with the 4 real constants (SR-DM2).
    - SRangerController(Sensor, Actuator, Clock), holding currentMode,
      clockResetTime and a single step(InputEvent) (SR-ARCH1/2, SR-GP1/2,
      SR-Beh1–7, SR-DC1).

  Design choices:
  - The clock field is named timer because clock is a reserved word.
  - In Turning the priority is endTask, then the autonomous elapsed-time
    transition, then tick. Tick is guarded by !turnDurationElapsed. This keeps a
    stream of ticks from blocking the return to Moving.
  - Moving→Turning needs the obstacle event and obstacleDetected. SR-Beh2 and
    SR-GP1 together call for both.

  Layers 2–4 (skeleton, leaves, controller) follow.

⏺ Bash(cd /Users/ranwei/Gitee/forge-v3r7-run4-sranger/java.generated.project &&
      mkdir -p
      src/main/java/sranger/{annotation,event,sensor,actuator,time,controller}
      && ls…)
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
       9  * Declares the RoboChart type ("real", "nat") of a field, param
         eter or
     … +7 lines (ctrl+o to expand)

⏺ Write(java.generated.project/src/main/java/sranger/event/InputEvent.java)
  ⎿  Wrote 17 lines to
     java.generated.project/src/main/java/sranger/event/InputEvent.java
       1 package sranger.event;
       2
       3 /**
       4  * Input events of the SRanger controller. All three are signals
          without a
       5  * payload.
       6  */
       7 public sealed interface InputEvent {
       8
       9     /** Emitted by the IR sensor framework when the distance cro
     … +9 lines (ctrl+o to expand)

⏺ Write(java.generated.project/src/main/java/sranger/event/OutputEvent.java)
  ⎿  Wrote 18 lines to
     java.generated.project/src/main/java/sranger/event/OutputEvent.java
      1 package sranger.event;
      2
      3 import sranger.annotation.RoboChartType;
      4
      5 /**
      6  * Output events of the SRanger controller. Move is the only comm
        and sent to
      7  * the differential-drive layer.
      8  */
      9 public sealed interface OutputEvent {
     … +9 lines (ctrl+o to expand)

⏺ Write(java.generated.project/src/main/java/sranger/controller/SRangerMode.java
       )
  ⎿  Wrote 10 lines to java.generated.project/src/main/java/sranger/control
     ler/SRangerMode.java
       1 package sranger.controller;
       2
       3 /**
       4  * Operating modes of the SRanger controller; MOVING is the init
         ial mode.
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
  ⎿  Wrote 41 lines to
     java.generated.project/src/main/java/sranger/sensor/Sensor.java
       1 package sranger.sensor;
       2
       3 import sranger.annotation.RoboChartType;
       4
       5 /**
       6  * Exposes the latest forward-facing IR distance reading.
       7  */
       8 public final class Sensor {
       9
      10     // Returned while no reading is available, far above the obs
     … +32 lines (ctrl+o to expand)

⏺ Write(java.generated.project/src/main/java/sranger/actuator/Actuator.java)  ⎿  Wrote 39 lines to
     java.generated.project/src/main/java/sranger/actuator/Actuator.java
       1 package sranger.actuator;
       2
       3 import sranger.annotation.RoboChartType;
       4 import sranger.event.OutputEvent;
       5
       6 /**
       7  * Differential-drive actuator: receives the controller's Move c
         ommands and
       8  * stores the last one issued.
       9  */
     … +30 lines (ctrl+o to expand)

⏺ Write(java.generated.project/src/main/java/sranger/time/Clock.java)
  ⎿  Wrote 26 lines to
     java.generated.project/src/main/java/sranger/time/Clock.java
       1 package sranger.time;
       2
       3 import sranger.annotation.RoboChartType;
       4
       5 /**
       6  * Monotonic time source for the controller's timed transitions.
       7  */
       8 public final class Clock {
       9
      10     @RoboChartType("real") private final double originSeconds;
     … +16 lines (ctrl+o to expand)

⏺ Write(java.generated.project/src/main/java/sranger/controller/SRangerControlle
       r.java)
  ⎿  Wrote 82 lines to java.generated.project/src/main/java/sranger/control
     ler/SRangerController.java
       1 package sranger.controller;
       2
       3 import sranger.actuator.Actuator;
       4 import sranger.annotation.RoboChartType;
       5 import sranger.event.InputEvent;
       6 import sranger.event.OutputEvent;
       7 import sranger.sensor.Sensor;
       8 import sranger.time.Clock;
       9
      10 /**
     … +72 lines (ctrl+o to expand)

⏺ Bash(OUT=/private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3r7-run4-sranger/c
      921a302-2ecc-4ddd-a7de-3b824331ca2b/scratchpad/classes && mkdir -p $OUT &&
      javac --rele…)
  ⎿  (No output)

⏺ Compiles cleanly with -Xlint:all. A quick behaviour smoke test from the
  scratchpad (outside the project):

⏺ Bash(SP=/private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3r7-run4-sranger/c9
      21a302-2ecc-4ddd-a7de-3b824331ca2b/scratchpad; mkdir -p $SP/smoke && cat >
      $SP/smoke/Sm…)
  ⎿  (No output)

⏺ Behaviour matches the requirements. Now Layer 5: the trace, post_codegen, and
  the summary.

⏺ Bash(cat > result_codegen.json <<'EOF'
      {…)
  ⎿  (No output)

⏺ Bash(cd /Users/ranwei/Gitee/forge-v3r7-run4-sranger/java.generated.project/src
      /main/java/sranger && grep -n "NO_READING_DISTANCE\|public void
      update\|reading >= 0" s…)
  ⎿  (No output)

⏺ Bash(cd /Users/ranwei/Gitee/forge-v3r7-run4-sranger && cat >
      /private/tmp/claude-501/-Users-ranwei-Gitee-forge-v3r7-run4-sranger/c921a3
      02-2ecc-4ddd-a7de-3b824331ca2b…)
  ⎿  (No output)

⏺ Write(forge.assets/corrections/ITER_SUMMARY.md)
  ⎿  Wrote 24 lines to forge.assets/corrections/ITER_SUMMARY.md
      1 # ITER_SUMMARY — sranger, run 4
      2
      3 ## Iteration 1
      4
      5 **Written.** Nine files under `sranger/`. `annotation.RoboChartTy
        pe` and `event.InputEvent` (signals `Obstacle`, `Tick`, `EndTask`
        ). `event.OutputEvent` (one record, `Move(lv, av)`). `sensor.Sens
        or` (`distance()`), `actuator.Actuator` (stores the last Move) an
        d `time.Clock` (`now()`, in seconds). Under `controller`: `SRange
        rMode {MOVING, TURNING, FINAL}`, `SRangerConstants` (the four rea
     … +20 lines (~2 KB) (ctrl+o to expand)

  Listed 1 directory (ctrl+o to expand)

⏺ Iteration 1 is ready. Running the runner. My session doesn't show a token
  figure, so I'm passing unavailable.

⏺ Bash(export FORGE_ARM_DIR=/Users/ranwei/Gitee/formal_method_guided_vibe_coding
      /experiments/v3-reference FORGE_WT_PREFIX=forge-v3r7
      FORGE_STAGE_DIR=/Users/ranwei/Gite…)
  ⎿  (No output)
  ⎿  (timeout 10m)

⏺ The run converged at iteration 1: the runner printed ALL PHASES PASSED, so I
  stopped there.

  ┌───────────────────────────┬─────────────────────────────────────────────┐
  │           Phase           │                   Result                    │
  ├───────────────────────────┼─────────────────────────────────────────────┤
  │ compile, coverage,        │ passed                                      │
  │ preflight                 │                                             │
  ├───────────────────────────┼─────────────────────────────────────────────┤
  │ t2m, m2m, m2t, dafny_gen, │ passed                                      │
  │  isabelle_gen             │                                             │
  ├───────────────────────────┼─────────────────────────────────────────────┤
  │ fdr4                      │ 3/3 assertions passed (determinism was      │
  │                           │ commented out by the pipeline, as designed) │
  ├───────────────────────────┼─────────────────────────────────────────────┤
  │ dafny_verify              │ all files verified                          │
  ├───────────────────────────┼─────────────────────────────────────────────┤
  │ isabelle_verify           │ 21 lemmas proved, deadlock freedom proved   │
  ├───────────────────────────┼─────────────────────────────────────────────┤
  │ vacuity                   │ passed: 0 blocking, 2 advisory              │
  └───────────────────────────┴─────────────────────────────────────────────┘

  I didn't open the vacuity report to see what the two advisory notes say,
  because the loop rules only allow reading reports for failing phases.

  What I wrote. No Java was there before I started, so nothing was deleted.
  There are nine files under java.generated.project/src/main/java/sranger/:
  - the three input events, Move(lv, av) as the only output, a sensor, an
    actuator and a Clock;
  - the mode enum and the four constants;
  - SRangerController with a single step.

  I also wrote result_codegen.json (java.generated.project/result_codegen.json),
  which covers all 23 requirements and every Java file, plus
  post_codegen.md/.json and ITER_SUMMARY.md
  (forge.assets/corrections/ITER_SUMMARY.md) (279 words). I made no commits and
  passed unavailable for tokens.

  Decisions the requirements left open:
  - Moving → Turning needs both the obstacle event and the obstacleDetected
    guard, because SR-Beh2 and SR-GP1 name one each.
  - Priority in Turning is endTask, then the timed return, then tick. Otherwise
    a tick every cycle could keep the robot turning forever.
  - Start-up: the constructor sends the first forward Move command, which SR-FR1
    requires on power-up.
  - Sensor: with no reading it returns 1000.0 m, a value I chose. Negative or
    NaN readings are ignored.
  - Actuator: it reads 0/0 until the first command.
  - Clock field is named timer, because clock is a reserved word.
  - Threshold rounding: OBSTACLE_THRESHOLD (0.5) is rounded up to 1 in the
    model's constants, so the model checks the obstacle guard against 1 m, not
    0.5 m.

  Things the operator should look at:
  - forge.assets/corrections/csp_overrides.csp came with the pinned commit, and
    its header says it is for the LRE study. The fdr4 log shows it was applied
    to this study ("applied csp corrections to instantiations.csp"), and FDR4
    passed anyway. I left it unchanged. It may be a leftover from another study
    in the instrument.
  - The runner's record step printed "fill in: converged, failure_summary,
    actor_codegen_summary". I took that as a field for the operator and didn't
    fill it in.

  ITER_1_COMPLETE: 13 files edited