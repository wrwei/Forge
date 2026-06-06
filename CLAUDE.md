# Formal-Method-Guided Vibe Coding

## Project Overview

This project implements a formal-methods pipeline for safety controllers. Java source code is written interactively with LLM assistance, then transformed through a formal verification toolchain (Spoon → ETL → RoboChart → CSP / Dafny / Isabelle/UTP Z-Machine). Each case study under `forge.assets/case-studies/<study>/` provides its own system description and requirements; this CLAUDE.md is intentionally domain-independent.

## Active case study

The active case study is **lre**. Substitute `lre` for `<study>` in any path that uses the placeholder, unless the user names a different one. The system description, modes, sensors, and actuators live under `forge.assets/case-studies/lre/system/system_description.txt` — do **not** assume those domain details from this file; read the case-study description when the user points you at it (e.g., during Layer 1 of `forge.assets/vibe-coding-prompts/`).

To switch case studies, change the name in the line above to a different `<study>` directory.

### Pipeline Architecture

```
Java source code (this is what you write/refine)
    → [SpoonDiscoverer] → Spoon EMF model (.xmi)
    → [ETL: java2robochart.etl] → RoboChart model (.xmi)
    → [EGL: robochart2rct.egl] → RoboChart textual notation (.rct)
    → [RoboChart CSP generator] → CSP-M files
    → [FDR4] → Deadlock/divergence/determinism verification
    → [Dafny generator] → Dafny code
    → [Dafny verifier] → Design-by-contract verification
    → [EGL: thy_generation_rule.egl] → Isabelle/UTP Z-Machine theory (.thy)
    → [Isabelle build] → deadlock-freedom + invariant proofs
```

### Pipeline phases

The canonical phase manifest is [pipeline.yaml](pipeline.yaml). Each entry
declares the runner kind (`java`/`python`/`gradle`/`sequence`), its
implementation class or function, dependencies, output files, and any
runtime tuning (timeouts, tool paths). Both the Python dashboard
([forge.dashboard/web/bridge.py](forge.dashboard/web/bridge.py))
and the Java CLI ([forge.transformations/.../App.java](forge.transformations/src/main/java/forge/transformations/core/App.java))
read this file directly.

### Key Directories

- `java.generated.project/` — Generated Java project (the code you produce goes here)
- `forge.assets/` — Shared assets (domain-independent prompts, skills, corrections)
- `forge.assets/prompts/` — Codegen rules, chain-of-thought, few-shot examples
- `forge.assets/corrections/` — Verification feedback files
- `forge.assets/case-studies/<study>/` — Per-case-study assets. Each has its own `system_description.txt` and `requirements/` directory. The active study is named in the "Active case study" section above.
- `forge.dashboard/` — Web dashboard for running deterministic pipeline phases
- `java.codegen.pipeline/` — *removed in May 2026*. Was the legacy AutoGen + DeepSeek codegen pipeline. Recover via `git checkout v1.0-autogen-pipeline -- java.codegen.pipeline/` if needed.
- `forge.transformations/` — Spoon discovery + ETL/EGL transformations + Dafny generation
- `forge.transformations/output/` — Transformation outputs (XMI, RCT, CSP, Dafny)

---

## Java Code Generation Constraints

**These constraints are CRITICAL.** The generated Java code must be structured so that the Spoon/Epsilon transformation pipeline can extract a correct RoboChart formal model. Violating these rules will cause transformation failures or incorrect formal models.

### Detailed Rules

See @forge.assets/prompts/java_codegen_rules.txt for the full ruleset.

Key highlights:

### Controller State Machine Structure

The controller MUST use a **single-method, mode-nested if-else** pattern:

1. One public method: `void step(InputEvent event)`
2. Track current mode as an enum field
3. **Named boolean predicates** declared BEFORE the if-else chain — all guard conditions captured as named variables
4. Pure two-level if-else:
   - **Outer**: exactly one block per mode (`currentMode == X`), never `currentMode != X`
   - **Inner**: transitions ordered by priority
5. High-priority transitions DUPLICATED in each applicable mode block
6. Entry actions inline at mode transitions

**Named predicate rules:**
- Simple comparisons only (sensor calls, constants, arithmetic, boolean operators)
- NO ternary expressions: ~~`boolean safe = idx == -1 ? true : expr;`~~
- NO sentinel checks: ~~`boolean exists = idx != -1;`~~
- Sensor layer must return safe defaults for missing data
- Reuse same variable name when same condition appears in multiple modes

### Operation `compute()` Methods

- ONLY direct `this.field = expression` assignments
- NO local intermediate variables
- Every RHS must be a single expression (sensor calls, Math.sqrt, arithmetic, boolean ops, previously-assigned fields)

### Banned Language Features (Required for Formal Model Extraction)

- **NO** lambdas, streams, `.stream()`, `.filter()`, `.map()`, `.anyMatch()`, method references (`::`)
- **NO** functional interfaces
- **NO** pattern-matching instanceof — use traditional `instanceof` + separate cast
- **NO** switch expressions with pattern matching
- **NO** ternary conditional expressions (`condition ? a : b`) — use plain if-else

### Required Language Features

- Java 17, Gradle 8.12+, JUnit Jupiter 5.11+
- Records for immutable value types
- Sealed interfaces for algebraic data types (events)
- `var` for local variable type inference
- `Optional` / `OptionalInt` for nullable return values

### `@RoboChartType` Annotation

Generate a `@RoboChartType` annotation in the `annotation` subpackage:
```java
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
public @interface RoboChartType { String value(); }
```

Apply on field declarations, method parameters, and return types:
- `int` representing natural numbers/indices/counts: `@RoboChartType("nat")`
- `double` fields: `@RoboChartType("real")`
- Never on local variables or generic type arguments

### Package Structure

- Derive base package from the active case study's system domain
- Sub-packages: `sensor`, `actuator`, `event`, `controller`, `operation`, `annotation`
- No wildcard imports; final fields by default; package-private unless public needed

### Naming conventions consumed by the M2M (ETL)

The M2M is generic — it does NOT hardcode case-study names — but it
does pattern-match a few Java naming/annotation conventions. Either
follow the names or apply the matching annotation; the ETL accepts
both routes. Each convention's defaults are centralised at the top of
[forge.transformations/src/main/resources/transformations/java2robochart.etl](forge.transformations/src/main/resources/transformations/java2robochart.etl)
under "Configurable conventions".

| Construct | Convention default | Annotation alternative |
|-----------|--------------------|--------------------------|
| Controller class | has `step(...)` method + an enum-typed mode field | — (auto-detected; field name `mode` or `currentMode`) |
| Operation class | has a method named `compute()` | — |
| Clock dependency | class named `Clock` (referenced from a controller field) | `@Clock` on the field declaration OR on the dependency class/interface |
| Sensor service class | class name contains substring `"sensor"` (case-insensitive); used by Pass-3 sensor-class fallback | `@SensorService` on the class declaration |
| Timing primitives | method name is one of `pause` / `wait` / `delay` | `@RoboChartWait` on the method declaration |

The annotation route is preferred for non-obvious Java names (e.g. a
clock class called `SystemTime`, a sensor service called `Telemetry`,
or a method `sleep`). Annotation types may live in any package; the
M2M matches by SIMPLE NAME so `@chemdetector.annotation.Clock` and
`@com.acme.markers.Clock` both work.

---

## Thinking Process for Code Generation

When generating or substantially refactoring the Java code, follow this structured thinking process:

See @forge.assets/prompts/chain_of_thought_codegen.txt for the full step-by-step reasoning guide.

For top-down codegen pacing — architecture first, then skeleton, then leaves, then controller — see [forge.assets/vibe-coding-prompts/](forge.assets/vibe-coding-prompts/). Each of the five layers lives in its own self-contained file (`01_architecture.md` through `05_review.md`); copy a file's content into chat to start that layer. The README in the directory is the index. Each layer halts and waits for user review before the next.

---

## Reference Examples

See @forge.assets/prompts/few_shot_codegen.txt for a complete worked example (traffic controller) showing the required code structure.

---

## Requirements

Requirements are provided incrementally by the user in each conversation. The default source of truth is `forge.assets/case-studies/<study>/requirements/requirement_all.json` — read it as the canonical input. The tier files (`tier1.md`...`tier7.md`) are documentation of how the requirements decompose by dependency; they are **not** consumed as codegen drivers in the current top-down workflow. If the user names a specific tier file or other subset, treat that as a focus narrowing rather than a hard isolation: still consult `requirement_all.json` for context (so you don't paint into a corner with names that conflict with later tiers), but only **implement** the items in the named subset.

Each requirement has the schema: `kind`, `name`, `id`, `description`, `priority`, `types`. ID prefix conventions are case-study-specific — see `forge.assets/case-studies/<study>/requirements/README.md` for the active study's ID pattern.

---

## Pipeline Commands (Windows)

All transformation commands run from `forge.transformations/`. `<Stm>`
is the state-machine name the pipeline derives from the controller
class — it's resolved automatically by the runners and appears in
`output/robochart_model.xmi`. Substitute it in the verifier filenames
below if you're invoking them outside the dashboard.

```bash
# Phase 2c — Preflight (Structural Lint)
cd forge.transformations && ./gradlew.bat run --args="lint source=../java.generated.project/src/main/java output=output"

# Phase 3 — T2M: Spoon Discovery (Java → EMF model)
cd forge.transformations && ./gradlew.bat run --args="t2m source=../java.generated.project/src/main/java output=output"

# Phase 4 — M2M: ETL Transformation (Java EMF → RoboChart EMF)
cd forge.transformations && ./gradlew.bat run --args="m2m source=../java.generated.project/src/main/java output=output"

# Phase 5b — M2T: RCT + CSP Generation (sequence: RctPhase then roboChartCspGen)
cd forge.transformations && ./gradlew.bat run --args="m2t output=output"
# (The sequence step runs RctPhase then the gradle roboChartCspGen task internally.)

# Phase 5a — Dafny Generation (Java EMF → Dafny)
cd forge.transformations && ./gradlew.bat run --args="dafny_gen source=../java.generated.project/src/main/java output=output"

# Phase 5c — Isabelle Theory Generation
cd forge.transformations && ./gradlew.bat run --args="isabelle_gen output=output"

# Phase 6b — FDR4 Verification
"<FDR4_PATH>/refines.exe" forge.transformations/output/csp-gen/defs/<Stm>_coreassertions.csp

# Phase 6a — Dafny Verification
dafny verify forge.transformations/output/<Stm>.dfy

# Phase 6c — Isabelle Proof — runs in WSL on Windows; native on Linux/macOS
#    The session dir lives on /mnt/c/... for cross-OS access.
~/isabelle/Isabelle2023-CyPhyAssure/bin/isabelle build \
  -D "$(wslpath '<REPO>\forge.transformations\output\isabelle')" \
  -v -o timeout=600

# Build the generated Java project (Phase 2a — Compile)
cd java.generated.project && ./gradlew.bat build
```

`<FDR4_PATH>` defaults to `C:\Program Files\fdr\bin\` on Windows or
`/Applications/FDR4.app/Contents/MacOS/` on macOS — see
`forge.dashboard/config.yaml` `phases.fdr4.fdr4_path` for the
configured location.

---

## Generated RoboChart structure

A summary of the constructs the ETL/EGL produce, as of the May 2026
model-fidelity work. The exact shape depends on the
Java source — items below are emitted only when applicable.

**Per-package**:
- `enumeration <Name> { ... }` — emitted for every Java domain enum
  (excludes mode enums, which drive state machines).
- `datatype <Name> { ... }` — emitted for every Java record except
  event records (which become events) and the sensor record
  (becomes the Sensors interface).
- `function <name>(p : <Type>) : <Ret> { }` — emitted for sensor
  methods referenced from guards or actions; the parameter and
  return types are inferred from the Java method signature
  (`List<X>` → `Seq(X)`, enums → enum names, records → record names).

**Per-package interfaces**:
- `interface Inputs { ... }` — boundary inputs only (events received
  from the environment).
- `interface Outputs { ... }` — boundary outputs only (events sent
  to the environment).
- `interface Shared { ... }` — inter-controller events (a trigger in
  one machine and an action in another). stms/controllers gain
  `uses Shared` when this interface is non-empty.
- `interface Sensors { var <method> : <Ret> }` — zero-arg sensor
  methods referenced from actions. `interface Actuators { }` always
  emitted (empty by default).
- `interface Ctrl_State { var <field> : <Type> }` — controller
  state variables. Type is inferred from the Java field type;
  classifier-derived predicates that don't correspond to declared
  fields are added as `var <name> : boolean`.
- `interface Constants { const <name> : <Type> = <N> }` — values
  resolved from a constants class. Fractional values are ceiled to
  the nearest integer (CSP-gen v3.0.0 limitation).
- `interface LOperations { <op>(p0 : T, p1 : T, ...) }` — emitted
  when any transition action contains a multi-arg invocation; the
  invocation becomes a `Call` referencing the OperationSig.
  stms/controllers gain `requires LOperations` when non-empty.

**Per state machine**:
- `stm <ControllerName> { uses Inputs uses Outputs [uses Shared]
  requires Ctrl_State requires Constants [requires Sensors]
  [requires LOperations] var v : real [clock <name> ...] ... }`.
- `var v : real` — typed-trigger local. When a transition's first
  action is `<sv> = v` (capturing the payload into a state var of
  matching type), the trigger is rebound to `<sv>` directly and the
  assignment is dropped — required for non-real event payloads
  (`event obstacle : Loc` → `trigger obstacle ? l`).
- `clock <name>` — promoted from Java fields assigned via
  `clock.nowMs()`. The reset action becomes `# <name>` on the
  containing transition. Time predicates of the form
  `clock.nowMs() - <clockField> < CONST` are rewritten to
  `since(<clockField>) < CONST`.
- `state <Name> { [entry <action>] }` — entry actions come from
  either ETL-extracted top-of-mode statements (e.g.
  `vehicle.randomWalk()` at the head of a Waiting mode block) or
  from the EGL's "common incoming action" lifting heuristic.
- `transition <name> { from <S> to <T> [trigger <evt>[ ? <var>]]
  [# <clock>] [condition <expr>] [action <statements>] }`.

**Per package — modules**:
- Single-machine package: one `module <Stm>_Module` with the lone
  controller, an `InputEnv`/`OutputEnv` pair, and connections
  through them (legacy single-machine layout).
- Multi-machine package: each controller still gets its own
  `<Name>_Ctrl` block, but ONE unified
  `<Diagram>_System_Module` aggregates all controllers as
  `ctrl_ref0..N` with direct `cref → cref` connections for
  inter-controller events.

**Action statements** in transitions / state entries:
- `<sv> = <expr>` — Assignment to a Ctrl_State variable.
- `<event> ! <expr>` — Communication on a typed event.
- `send <event>` — Communication on an untyped event.
- `<op>(arg1, arg2, ...)` — operation `Call` (multi-arg).
- `wait(<duration>)` — emitted from `vehicle.pause(N)` /
  `wait(N)` / `delay(N)` calls.
- `# <clock>` — clock reset (lives on `transition.reset`, not
  `transition.action`).

**Linter output**:
- The M2M phase emits `[deadlock-lint] <stm>.<state>: ...` to
  stdout when a non-Final state has no bare-precondition outgoing
  transition (one whose only Isabelle precondition is
  `st = <SourceMode>`, no extra guards conjoined). The dashboard
  surfaces these in `post_m2m.json` as advisory Issues (status
  remains "passed").

  **Both** `else { mode = <SameMode>; }` fallbacks AND
  event-triggered branches without extra guards (e.g.,
  `if (event instanceof InputEvent.Reset) { mode = Idle; }` —
  becomes a zoperation with precondition just `st = <SourceMode>`) qualify
  as bare-precondition. **Prefer the event-triggered form when
  applicable.** The unconditional `else` fallback becomes a
  τ-transition in tock-CSP that competes with every enabled
  guarded autonomous transition, producing FDR4 `:[deterministic]`
  failures.
  Add `else { mode = <SameMode>; }` only when no event-triggered
  branch in that mode block already provides bare-precondition
  cover — the M2M lint is currently too broad and may advise
  self-loops on states that already have them.

  `/lint` runs M2M and filters for these lines only.

A regression test script lives at
[scripts/regression_test.sh](scripts/regression_test.sh) — it runs
M2M → RctPhase → CSP-gen against both chemical_detector and a
git-extracted LRE source, asserting both produce module CSP files.

---

## Formal Verification Feedback

### Interpreting FDR4 Results

See @forge.assets/prompts/fdr4_system.txt for the full FDR4 feedback interpretation guide.

Key points:
- **Determinism is NOT verified** — `run_fdr4` strips the official generator's `:[deterministic]` assertions before invoking FDR. RoboChart models a state's transitions as concurrent choices (not a prioritised list), and an autonomous guard-only transition compiles to a hidden internal event, so the extracted models are non-deterministic by construction even though the generated Java is deterministic. The pipeline therefore verifies only deadlock-freedom and divergence-freedom. (Historically this was instead tolerated via `expected_failures: ["deterministic"]`; that entry is now empty.)
- Only fix genuinely unexpected failures: deadlocks, divergences, parse errors.
- Deadlock = state with no outgoing transitions (missing transition logic in Java)
- Divergence = infinite loop of internal events (guard-only transitions forming a cycle)
- Parse errors = CSP-M syntax issue (may be EGL template bug, not Java code)

### Interpreting Dafny Results

- Dafny verifies design-by-contract properties (preconditions, postconditions)
- Verification errors trace back to specific Java methods via the transformation chain
- Fix by adjusting Java code logic, not Dafny output

### Interpreting Isabelle Results

- Isabelle/UTP Z-Machine verifies deadlock-freedom and structural invariants of the controller as a Z-machine
- Failures show up as `*** Failed to apply proof method ...` in the `isabelle build` output, with the residual goal printed
- The deadlock-freedom proof tactic is **`apply deadlock_free` then `by (metis St.exhaust_disc)`**. Do NOT replace it with `cases st; simp_all` (fails) or `auto` (10-min timeout)
- The proof relies on every state having at least one bare-precondition operation — a zoperation whose precondition is just `st = <SourceMode>`, with no extra guards conjoined. Three patterns satisfy this: (a) an unconditional `else { mode = <SameMode>; }` fallback; (b) any event-triggered branch with no extra guard, e.g., `if (event instanceof InputEvent.Reset) { mode = Idle; }`; (c) any autonomous branch whose only condition is the source mode. **Prefer (b) when applicable** — pattern (a) introduces a τ-self-loop that breaks FDR4 determinism (see the "Linter output" note under "Generated RoboChart structure" above for the trade-off)
- **Best of all, where the mode is "autonomous-only" (consumes no input event — its outgoing transitions are all guard-only), prefer a TOTAL GUARD COVER over any self-loop.** If a mode's autonomous guards are jointly exhaustive (e.g. a mode whose two outgoing guards split on a two-valued enum — `s == valA` and `s == valB` where the enum declares exactly those two literals), then one outgoing transition is *always* enabled — the mode is deadlock-free with NO self-loop, and stays deterministic and divergence-free. This is how a well-formed RoboChart model with such a mode passes `:[deterministic]`. If codegen instead adds a `Tick`-event self-loop to such a mode (pattern (b)) to get a bare precondition, that self-loop then *competes* with the autonomous transitions and is the sole cause of a determinism failure (FDR witness event `tick`). The trilemma for autonomous-only modes: τ-self-loop breaks divergence; deleting the self-loop breaks deadlock; `Tick`-self-loop breaks determinism — **a total guard cover satisfies all three at once and needs no self-loop.** Determinism is not currently verified by the pipeline (stripped in `run_fdr4`), so this is a quality/fidelity improvement, not a convergence blocker.
- **The primary controller — the one whose Isabelle theory is generated — must NOT contain a `Final` state.** This refines the bare-precondition rule above: a `Final` mode cannot be given a bare-precondition operation, and (critically) the current FORKed theory generator emits the **weak** store invariant `where inv: "tr ≠ []"` instead of the reference's `wf_rcstore tr st (Some final)`, so it never *designates* a terminal state. `deadlock_free` then treats `Final` as an ordinary state that needs an enabled operation — it has none — and the residual goal lacks a `st = Final` disjunct, leaving it **unprovable**. The symptom is deceptive: every closing tactic (`by (metis St.exhaust_disc)`, `auto`, `metis`, `blast`) **hangs** (indistinguishable from a hard proof) rather than failing fast; and adding an operation *on* `Final` instead makes `apply deadlock_free` itself fail. **Fix:** give the theory-generated controller no `Final` mode — reroute its terminal transition back to a live state while still emitting the terminating event (in a multi-controller study only the primary/last-discovered controller gets a theory, so a `Final` on the *secondary* controller is harmless). *Diagnostic, no source access needed:* set the proof to `apply deadlock_free` then `done` and read the printed residual goal — a missing disjunct for some `st` means that state has no enabled op; `apply deadlock_free; sorry` (under `-o quick_and_dirty`) finishing in seconds confirms it is the *closer*, not the reduction, that hangs.
- The EGL template for theory generation is at [forge.transformations/src/main/resources/transformations/thy_generation_rule.egl](forge.transformations/src/main/resources/transformations/thy_generation_rule.egl) (loaded from the runtime classpath, like the other Epsilon templates).

### Traceability

The pipeline produces `trace_full.json` linking:
```
Requirement ID → Java file/element → RoboChart element → CSP-M line
```

Use this to trace verification failures back to the originating Java code and requirement.

---

## Corrections Workflow

Every pipeline phase (2a-6c) writes a pair of files to
`forge.assets/corrections/` on each run — both on success and on
failure. The pair is always `post_<phase>.md` (human-readable) +
`post_<phase>.json` (structured, same content). Phase 2 (interactive
codegen) also writes `post_codegen.*`, but that file is authored by
Claude per the Layer 5 prompt rather than by the dashboard:

- `post_codegen.*` — written by Claude at the end of every Phase 2
  (interactive code generation) turn, per the Layer 5 prompt in
  `forge.assets/vibe-coding-prompts/05_review.md`. Surfaces design choices,
  invented defaults, ambiguous requirements, and unimplemented items
  the user should review. Issue kinds: `invented_default`,
  `design_choice`, `ambiguous_requirement`, `scope_question`,
  `unimplemented`. `status: "complete"` with `issues: []` is the
  no-uncertainty case.
- `post_compile.*` — `gradlew build` against `java.generated.project/`.
  Phase 2a. Catches Java compile errors before any model-extraction
  phase runs. Issue kinds: `java_compile_error`,
  `etl_unsupported_construct` (the gradle classifier is shared with
  the other gradle phases).
- `post_coverage.*` — bidirectional requirement ↔ Java trace check
  (Phase 2b). Reads `requirement_all.json` + `java.generated.project/result_codegen.json`
  + the Java source tree. Issue kinds: `missing_implementation`
  (requirement has no trace entry), `over_implementation` (public
  Java element with no requirement mapping), `missing_codegen_trace`
  (the result_codegen.json artifact is itself missing — run
  `/gen-trace` first).
- `post_t2m.*`, `post_m2m.*`, `post_m2t.*`, `post_dafny_gen.*`,
  `post_isabelle_gen.*` — gradle phases (Spoon discovery, ETL, CSP
  generation, Dafny generation, Isabelle theory generation).
  Failures classify common ETL/EGL errors (e.g. `case not treated:
  CtLambda` → CLAUDE.md rule violation).
- `post_fdr4.*` — FDR4 results. Classifies assertion failures as
  `deadlock` / `divergence` / `nondeterminism` / `parse_error`, with
  counterexample traces and Java-source links via `trace_full.json`.
  Nondeterminism from overlapping guards is usually EXPECTED
  (see `forge.assets/prompts/fdr4_system.txt`).
- `post_dafny_verify.*` — Dafny results. Classifies errors as
  `dafny_postcondition` / `dafny_precondition` / `dafny_assertion` /
  `dafny_bounds` / `dafny_termination`.
- `post_isabelle_verify.*` — Isabelle/UTP Z-Machine `isabelle build`
  results. Classifies failures as `proof_method_failed` / `tactic_timeout`
  / `parse_error` / `theory_load_error`, with the residual goal printed
  for proof-method failures (see I1 fix doc for tactics that work and
  ones that don't).
- `post_vacuity.*` — Phase 6d. Vacuity audit on the generated Dafny +
  Isabelle artefacts. Catches two specific "verifier passes nothing
  because the obligation was True" patterns: `dafny_valid_vacuous`
  (Dafny `Valid()` body is `true` AND no other behavioural `ensures`
  clauses are active on transition methods) and `isabelle_inv_vacuous`
  (Isabelle zstore `where inv:` is `"True"`). Pure-Python check,
  no subprocess. Implementation: [forge.dashboard/web/vacuity.py](forge.dashboard/web/vacuity.py).
  Without this phase, "Dafny verifies; Isabelle proves 24 lemmas" can
  be entirely vacuous even on broken Java — the vacuity audit
  that motivated these signals is preserved in the first-round experiment
  write-up in git history.
- `csp_overrides.csp` — hand-maintained CSP-M overrides. Not feedback.
- `human_codegen.txt` — free-form notes; never overwritten by the
  pipeline.

Each file has: `status`, `summary`, per-issue `fix_directive`,
`java_trace` entries (Java file + line range + element + requirement
IDs), and a `next_step` recommendation.

When refining code, invoke `/fix-from-feedback` or read the most recent
`post_<phase>.md` for the failed phase. Files are overwritten on every
phase run — treat them as current state only.

NOTE: `forge.dashboard/corrections/` is a **separate** directory
holding user CSP type-range configuration. It is not the feedback
directory.

---

## Known Gotchas

- **Spoon EMF**: `CtLiteral` has no `value` attribute — the pipeline uses a `resolvedValues` map from `SpoonDiscoverer`
- **Spoon EMF**: `CtUnaryOperator` uses `expression` feature, not `operand`
- **Epsilon ETL**: `pre` block local variables are NOT visible to operations — pass data as explicit parameters
- **EGL templates**: Use `//` comments inside `[% %]` blocks (EOL syntax), NOT `--` (CSP-M syntax)
- **CSP-M**: Use `{ -1..1}` (space after `{`) for negative ranges — `{-` opens a multiline comment
- **RoboChart CSP generator**: `.rct` textual format does NOT support function bodies (must be empty `{ }`)
- **RoboChart CSP generator**: Does NOT support function-typed variable application (crashes with "Other types of callees not yet supported")
- **Isabelle/UTP Z-Machine `deadlock_free` proof**: must be closed with `by (metis St.exhaust_disc)` — `cases st; simp_all` fails on the mixed bare/guarded disjunction; `(deadlock_free; cases st; auto)` chains incorrectly and times out. This closer only succeeds when the theory-generated controller has **no `Final` state** — a `Final` mode leaves an unprovable residual (no `st = Final` disjunct) and makes every closing tactic *hang*; see "Interpreting Isabelle Results".
- **Isabelle build is cross-OS on Windows**: theory generation runs in Gradle on Windows (`gradlew.bat run --args="isabelle output"`); `isabelle build` runs in WSL (`isabelle` distribution is Linux-only). The dashboard's `isabelle_verify` phase bridges this via `wsl.exe`.

---

## Dashboard Claude chat

The pipeline dashboard embeds Claude Code as a chat input at the bottom
of the log panel. Each user message spawns a `claude -p` subprocess that
resumes a persistent session id stored at
`forge.dashboard/agent/session.json`. Destructive tool calls (Edit /
Write / Bash) trigger an in-browser approval modal via a PreToolUse hook
registered in a dashboard-owned settings file (`--settings` flag), which
keeps the dashboard's hook isolated from your VS Code Claude Code
sessions.

The `agent:` block in [pipeline.yaml](pipeline.yaml) configures the
`claude` binary path per-OS and the approval timeout.

#### Selecting requirements from the dashboard

The right panel has a **Requirements** tab. The active case study is set
in `pipeline.yaml` (`agent.active_case_study`) and surfaced as a
dropdown that writes back to the file on change. The tab lists all
`*.json` files under `forge.assets/case-studies/<study>/requirements/`;
checking any and clicking **Load selected → Claude** sends a single
chat message asking Claude to read those files plus the always-relevant
`<study>/system/system_description.txt`.
