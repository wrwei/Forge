# Formal-Method-Guided Vibe Coding

## Project Overview

This project implements a formal-methods pipeline for safety controllers. Java source code is written interactively with LLM assistance, then transformed through a formal verification toolchain (Spoon → ETL → RoboChart → CSP / Dafny / Isabelle/UTP Z-Machine). Each case study under `forge.assets/case-studies/<study>/` provides its own system description and requirements; this CLAUDE.md is intentionally domain-independent.

## Active case study

The active case study is whatever `pipeline.yaml` `agent.active_case_study`
says — **read it, do not assume**. That key is the single authority: the
dashboard and its Requirements tab both read it, and this file previously
restated a value that drifted out of sync with it. Substitute that name for
`<study>` in any path that uses the placeholder, unless the user names a
different one. The three studies present are `sranger`, `lre` and
`chemical_detector`. The system description, modes, sensors, and actuators
live under `forge.assets/case-studies/<study>/system/system_description.txt`
— do **not** assume those domain details from this file; read the case-study
description when the user points you at it (e.g., during Layer 1 of
`forge.assets/vibe-coding-prompts/`).

To switch case studies, change `agent.active_case_study` in `pipeline.yaml`
(or use the dashboard's Requirements-tab dropdown, which writes that key).

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
read this file directly. See
[docs/superpowers/specs/2026-05-11-pipeline-manifest-design.md](docs/superpowers/specs/2026-05-11-pipeline-manifest-design.md)
for the design rationale.

### Key Directories

- `java.generated.project/` — Generated Java project (the code you produce goes here)
- `forge.assets/` — Shared assets (domain-independent prompts, skills, corrections)
- `forge.assets/prompts/` — Codegen rules, chain-of-thought, few-shot examples
- `forge.assets/corrections/` — Verification feedback files
- `forge.assets/case-studies/<study>/` — Per-case-study assets. Each has its own `system_description.txt` and `requirements/` directory. The active study is named in the "Active case study" section above.
- `docs/corpus/` — **Vendored Isabelle theory references** (the Z-machine RAG corpus): `zmachine-framework/` (5 files — `Z_Machine.thy`, `Z_Operations.thy`, `Z_Testing.thy`, `Z_Animator.thy`, `Show_Record.thy`) and `zmachine-examples/` (19 canonical Z-method textbook examples: BirthdayBook, BoxOffice, DwarfSignal, FileSystem, Incubator, TelephoneExchange, Ring_Buffer, Dining_Philosophers, etc.). Consult these when diagnosing an Isabelle proof failure or looking up the canonical pattern for a Z-machine construct. See [docs/corpus/README.md](docs/corpus/README.md) for the full inventory and provenance.
- `docs/archive/` — Additional vendored references: ICECCS2023 paper supplements (`theory generation for GasAnalysis/`, `theory generation for LRE/`) and the `RoboChart_project4Z-Machine-transformation` archive. Same RAG-corpus status as `docs/corpus/` — read-only reference material the agents may consult.
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
- **Do NOT use `Optional` / `OptionalInt`** on any sensor or operation method
  the controller's guards or actions reference. The ETL has no mapping for
  them and silently degrades the type to `real`
  (`java2robochart.etl` `mapJavaTypeToRcTypeRef`, fallback branch). Return a
  primitive safe default instead — which is what the "no sentinel checks"
  rule above already requires. `Optional` is acceptable only on methods the
  formal model never touches.

### `@RoboChartType` Annotation

Generate a `@RoboChartType` annotation in the `annotation` subpackage:
```java
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
public @interface RoboChartType { String value(); }
```

Apply on field declarations, method parameters, and return types:
- `int` representing natural numbers/indices/counts: `@RoboChartType("nat")`
- `double` fields: `@RoboChartType("real")` — **mandatory**; preflight
  reports a missing one as an `error` (`rule4_double_missing_real_annotation`)
- Never on local variables or generic type arguments

**What the annotation actually does.** It is consumed by the *preflight
structural linter* (`StructuralLinter.java`, Phase 2c) and by `coverage.py`.
It is **not** read by the ETL or any EGL template — grep the transformation
sources and it does not appear. It documents intent and keeps preflight
green; it does not influence the extracted RoboChart type.

**How types are actually mapped** (`mapJavaTypeToRcTypeRef`), which matters
for what the formal model can express:
- `int` / `Integer` / `long` / `Long` → **`nat`, unconditionally**. There is
  no code path producing RoboChart `int`. Do not rely on a Java `int` to
  model a signed quantity or a negative sentinel: the extracted model treats
  it as a natural number regardless of annotation.
- `double` / `float` → `real`; `boolean` → `boolean`
- declared enums and records → their own types; `List`/`Set`/`Collection` →
  `Seq(...)`
- **anything else** (including `String`, `Optional`) → silently falls back to
  `real`, losing semantic precision.

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

Requirements are provided incrementally by the user in each conversation. The
canonical — and only — source of truth is
`forge.assets/case-studies/<study>/requirements/requirement_all.json`. Read it
as the input; each study's directory contains just that file, its `.txt`
rendering, and a `README.md` describing the ID-prefix conventions.

**No tier files exist.** Earlier revisions of this document referred to
`tier1.md`…`tier7.md` as a dependency decomposition; no such files are
present under any case study. The unrelated `docs/requirements/tier1-3.md`
are quarantined historical LRE material whose requirement IDs **conflict with
the canonical ones** — do not read them for requirement content. If the user
names a subset, treat it as a focus narrowing, not a hard isolation: still
consult `requirement_all.json` for context, but only **implement** the named
items.

Each requirement has the schema: `kind`, `name`, `id`, `description`, `priority`, `types`. ID prefix conventions are case-study-specific — see `forge.assets/case-studies/<study>/requirements/README.md` for the active study's ID pattern.

---

## Pipeline Commands

All transformation commands run from `forge.transformations/`. Use
`./gradlew` on macOS/Linux and `./gradlew.bat` on Windows. `<Stm>`
is the state-machine name the pipeline derives from the controller
class — it's resolved automatically by the runners and appears in
`output/robochart_model.xmi`. Substitute it in the verifier filenames
below if you're invoking them outside the dashboard.

The first argument must be a phase id from [pipeline.yaml](pipeline.yaml)
and every other argument must be `key=value` — `App.java` silently drops
bare tokens and rejects unknown phase ids. Only `kind: java` phases can be
run this way; `sequence`, `gradle` and `python` phases are expanded by the
dashboard, not by `App.java`.

```bash
# Phase 2c — Preflight (Structural Lint)
cd forge.transformations && ./gradlew run --args="preflight source=../java.generated.project/src/main/java output=output"

# Phase 3 — T2M: Spoon Discovery (Java → EMF model)
cd forge.transformations && ./gradlew run --args="t2m source=../java.generated.project/src/main/java output=output"

# Phase 4 — M2M: ETL Transformation (Java EMF → RoboChart EMF)
cd forge.transformations && ./gradlew run --args="m2m source=../java.generated.project/src/main/java output=output"

# Phase 5b — M2T: RCT + CSP Generation
# `m2t` is a SEQUENCE phase — App.java refuses it. Run the two steps directly,
# or run the m2t phase from the dashboard, which expands the sequence.
cd forge.transformations && ./gradlew run --args="forge.transformations.m2t.RctPhase output=output"
cd forge.transformations && ./gradlew roboChartCspGen -ProboChartProject=output

# Phase 5a — Dafny Generation (Java EMF → Dafny)
cd forge.transformations && ./gradlew run --args="dafny_gen source=../java.generated.project/src/main/java output=output"

# Phase 5c — Isabelle Theory Generation
cd forge.transformations && ./gradlew run --args="isabelle_gen output=output"

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
cd java.generated.project && ./gradlew build
```

`<FDR4_PATH>` is configured in [pipeline.yaml](pipeline.yaml) under
`phases.fdr4.fdr4_path`, per OS. The macOS default is the bare name
`refines` (resolved via `PATH`), not an `/Applications` path.
(`forge.dashboard/config.yaml` no longer exists — its contents were folded
into `pipeline.yaml` during the May 2026 manifest migration.)

See [docs/design/isabelle_wsl_setup.md](docs/design/isabelle_wsl_setup.md) for the WSL setup procedure and [docs/fixes/I1_deadlock_free_proof.md](docs/fixes/I1_deadlock_free_proof.md) for the proof tactic used in the deadlock-freedom lemma.

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
- `interface <Machine>_State { var <field> : <Type> }` — controller
  state variables, **one interface per state machine**. Type is inferred
  from the Java field type; classifier-derived predicates that don't
  correspond to declared fields are added as `var <name> : boolean`.
  A multi-machine package additionally gets `Ctrl_State_Shared` for
  variables more than one machine reads. (The old single union interface
  literally named `Ctrl_State` was removed — per-machine interfaces let
  each machine's CSP process range only over its own variables, which was
  the dominant FDR4 cost lever. `Ctrl_State` survives only as an
  intermediate ETL model name and never appears in emitted `.rct`.)
- `interface Constants { const <name> : <Type> = <N> }` — values
  resolved from a constants class. Fractional values are ceiled to
  the nearest integer (CSP-gen v3.0.0 limitation).
- `interface LOperations { <op>(p0 : T, p1 : T, ...) }` — emitted
  when any transition action contains a multi-arg invocation; the
  invocation becomes a `Call` referencing the OperationSig.
  stms/controllers gain `requires LOperations` when non-empty.

**Per state machine**:
- `stm <ControllerName> { uses Inputs uses Outputs [uses Shared]
  requires <Machine>_State requires Constants [requires Sensors]
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
- `<sv> = <expr>` — Assignment to a `<Machine>_State` variable.
- `<event> ! <expr>` — Communication on a typed event.
- `send <event>` — Communication on an untyped event.
- `<op>(arg1, arg2, ...)` — operation `Call` (multi-arg).
- `wait(<duration>)` — emitted from `vehicle.pause(N)` /
  `wait(N)` / `delay(N)` calls.
- `# <clock>` — clock reset (lives on `transition.reset`, not
  `transition.action`).

**Linter output**:
- The M2M phase emits `[deadlock-lint] <stm>.<state>: ...` to
  stdout for states it flags. The dashboard surfaces these in
  `post_m2m.json` as advisory Issues (status remains "passed").

  ABLATION (condition D2). The text that stood here defined which
  Java shapes satisfy the check, ranked two of them, explained the
  CSP consequence of the weaker one, and stated when to add the
  fallback. All of that is removed: it prescribes the construct to
  write and the shape the prover requires.

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
- The deadlock-freedom proof is `apply deadlock_free` followed by a closer the
  generator **selects automatically** from `hasPayloadDomain`
  (`thy_generation_rule.egl` ~:1367). Both branches are needed; neither works
  everywhere:
  - machine emits a typed-payload domain set (e.g. chemical_detector's
    gas-analysis `Reading`, whose only transition consumes `gs_input ∈ SeqGs`)
    → **`using St.exhaust_disc by auto`**. `metis` *hangs* (>10 min) here,
    because the enabledness disjunct is an existential over the payload set.
  - no payload domain (e.g. LRE) → **`by (metis St.exhaust_disc)`**. `auto`
    times out (>6 min) on LRE's real-arithmetic guards.

  So do not hard-code either closer, and do not "fix" a hang by swapping
  tactics — check which branch the machine should be taking. `cases st;
  simp_all` fails in both cases. See
  [docs/fixes/I1_deadlock_free_proof.md](docs/fixes/I1_deadlock_free_proof.md),
  which still documents only the `metis` branch.
- ABLATION (condition D2). Three bullets stood here. The first enumerated the Java patterns that give a state a bare-precondition operation and ranked them; the second prescribed a total guard cover for autonomous-only modes and laid out the divergence/deadlock/determinism trilemma with the construct that resolves each; the third instructed that the theory-generated controller must carry no `Final` mode and how to reroute its terminal transition. All three are removed: each states the Java shape the prover requires, which is the answer rather than the requirement. The proof-tactic selection above is retained -- that is generator-internal documentation, not codegen guidance.
- The EGL template for theory generation is at [forge.transformations/src/main/resources/transformations/thy_generation_rule.egl](forge.transformations/src/main/resources/transformations/thy_generation_rule.egl) (loaded from the runtime classpath, like the other Epsilon templates). Forked from the ICECCS2023 archive vendored under [docs/archive/ICECCS2023/](docs/archive/ICECCS2023/); see [docs/fixes/I2_template_fork.md](docs/fixes/I2_template_fork.md) for the patch audit.

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
- **Isabelle/UTP Z-Machine `deadlock_free` proof**: closed by one of two tactics the generator picks via `hasPayloadDomain` — `using St.exhaust_disc by auto` for payload-domain machines, `by (metis St.exhaust_disc)` otherwise. Each *hangs or times out* in the other's case, so the choice is not cosmetic. `cases st; simp_all` fails on the mixed bare/guarded disjunction in both. Either closer only succeeds when the theory-generated controller has **no `Final` state** — a `Final` mode leaves an unprovable residual (no `st = Final` disjunct) and makes every closing tactic *hang*; see "Interpreting Isabelle Results". See [docs/fixes/I1_deadlock_free_proof.md](docs/fixes/I1_deadlock_free_proof.md) (documents only the `metis` branch) and [.claude/memory/isabelle-zmachine-proofs.md](.claude/memory/isabelle-zmachine-proofs.md)
- **Isabelle build is cross-OS on Windows**: theory generation runs in Gradle on Windows (`gradlew.bat run --args="isabelle_gen output=output"` — note the phase id is `isabelle_gen`, and bare arguments without `=` are silently dropped); `isabelle build` runs in WSL (`isabelle` distribution is Linux-only). The dashboard's `isabelle_verify` phase bridges this via `wsl.exe`. See [docs/design/isabelle_wsl_setup.md](docs/design/isabelle_wsl_setup.md)

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
