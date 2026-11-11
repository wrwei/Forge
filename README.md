# Formal-Method-Guided Vibe Coding

An end-to-end workflow combining AI-assisted code generation with formal verification to produce trustworthy Java software. A developer writes Java source code interactively with LLM assistance (Claude Code / Cline in VS Code), following strict structural constraints. The code is then transformed into formal RoboChart models and verified using FDR4 (CSP model checker) and Dafny (deductive verifier). A Python pipeline runs the deterministic transformation and verification phases.

## Version

The `main` branch is the version of the pipeline whose results the accompanying paper reports: every recorded run, the mutation campaign and the requirements-level checks under `experiments/` were produced by, or re-derived with, this code. After the collections closed, the Python code that runs the phases (`forge.pipeline/`, formerly `forge.dashboard/`) was restructured without re-running them; the transformations, generators and verifier invocations it drives are unchanged. Earlier versions are in the history.

## Architecture Overview

```
Requirements (51 structured requirements in forge.assets/case-studies/lre/requirements/)
        │
        ▼
┌────────────────────────────────────────────────┐
│  Developer + Claude Code / Cline (VS Code)     │  Phase 1–2: Interactive code generation
│  Guided by CLAUDE.md constraints               │  Writes Java directly to project;
│  (forge.assets/prompts/ for rules/examples) │  Layer 5 of vibe-coding-prompts
│                                                │  post_codegen.* (uncertainty register)
└───────────────────┬────────────────────────────┘
                    │  java.generated.project/src/ (Java classes)
                    ▼
┌────────────────────────────────────────────────┐
│  forge.pipeline  (python -m forge_pipeline)    │  Orchestrates Phases 2a–6c
│  Runs deterministic phases as subprocesses     │  No LLM — pure tool execution
└───────────────────┬────────────────────────────┘
                    │
                    ▼
┌────────────────────────────────────────────────┐
│  Pre-transformation gates                      │  Phase 2a / 2b / 2c
│  2a Compile (gradle build)                     │  catches compile errors early
│  2b Coverage (req ↔ Java trace)                │  missing / over-implementation
│  2c Preflight (structural lint)                │  CLAUDE.md rule violations
└───────────────────┬────────────────────────────┘
                    │
                    ▼
┌────────────────────────────────────────────────┐
│  forge.transformations                       │  Phase 3–5: Model extraction
│  14 Java classes + 4 Epsilon transformations   │  Spoon 11.3.0 + Epsilon 2.8.0
│  t2m → m2m → {csp-gen, dafny, isabelle-thy}    │  + EMF 2.41.0
└───────────────────┬────────────────────────────┘
                    │  robochart_model.xmi, robochart_controller.rct
                    │  csp-gen/ (71 CSP-M files), LreController.dfy
                    │  isabelle/<Stm>_Beh.thy + ROOT, 4 traceability JSON files
                    ▼
┌────────────────────────────────────────────────┐
│  Formal Verification                           │  Phase 6a / 6b / 6c
│  FDR4    4.2.7  (CSP refinement checker)       │  deadlock / divergence
│  Dafny   4.11.0 (SMT deductive verifier, Z3)   │  design-by-contract correctness
│  Isabelle 2023-CyPhyAssure (Linux/WSL)         │  Z-Machine deadlock-freedom + invariants
└───────────────────┬────────────────────────────┘
                    │  Verification feedback
                    ▼
┌───────────────────────────────────────────────┐
│  Developer + Claude Code / Cline (VS Code)    │  Phase 7: Closed-loop refinement
│  Reads verification errors, traces to Java    │  Makes surgical fixes, re-verifies
│  (forge.assets/corrections/ for feedback)  │
└───────────────────────────────────────────────┘
```

> **Note:** The previous AutoGen + DeepSeek multi-agent pipeline was removed from main
> in May 2026. The snapshot is preserved at git tag `v1.0-autogen-pipeline`; recover
> via `git checkout v1.0-autogen-pipeline -- java.codegen.pipeline/`.

## Case Study: LRE Safety Controller

The running case study is the **Last Response Engine (LRE)** — the safety controller for an Autonomous Underwater Vehicle (AUV) — based on:

1. Foster et al., *"Formal Model-Based Assurance Cases in Isabelle/SACM"*, FormaliSE 2020
2. Wei et al., *"REMEDIATE"*, JSS 2024 (arXiv:2403.15236)

The LRE implements a four-mode safety state machine:

| Mode | Description |
|------|-------------|
| **OCM** | Operator Control Mode — passthrough: reqVel/reqHdng forwarded directly to autopilot |
| **MOM** | Main Operating Mode — LRE takes autonomous control at 1 m/s |
| **HCM** | High Caution Mode — reduced speed (0 m/s) when near a static obstacle |
| **CAM** | Collision Avoidance Mode — evasive manoeuvre when CDA < minSafeDist |

### Key state machine properties (formally verified)

- **18 transitions** between 4 modes, **4 trigger events** (reqOCM, endTask, reqMOM, reqHCM)
- **5 operations** computed each step: CalcVel, CalcCStc, CalcCDyn, CalcCPA, CheckOPEZ
- **8 shared variables**: vel, inOpez, cdyn, cstc, cda, tcpa, hvel, vvel
- **4 safety constants**: minSafeDist, staticObsHorizDist, staticObsVertDist, staticObsDfltVertDist
- **15 Java source files**: controller, mode enum, 6 event types (sealed), 5 operations, sensor, actuator, constants, types
- FDR4: all checked assertions **PASS** (deadlock-freedom, divergence-freedom) — 5 compressed states, 89 transitions.
  Determinism is **not** checked: `run_fdr4` strips the generator's `:[deterministic]`
  assertions before invoking FDR, because RoboChart gives a state's transitions as
  concurrent choices and the ETL does not encode Java's else-if priority, so the
  extracted model is non-deterministic by construction.
- Dafny: **8 verified, 0 errors**
- Isabelle/UTP Z-Machine: `LreController_deadlock_free` proved — see [experiments/environment-model/](experiments/environment-model/) for the environment model the proof is stated under and the measured build times

---

## Projects

### [forge.assets/](forge.assets/)

Shared assets used by both the developer (via `CLAUDE.md` `@file` references) and the pipeline:

- `prompts/` — Domain-independent LLM-context material: codegen rules, chain-of-thought reasoning, few-shot examples, FDR4 feedback guide (auto-loaded into Claude's context via CLAUDE.md `@file` references)
- `vibe-coding-prompts/` — User-driven workflow prompts (the top-down five-layer codegen sequence). Open a layer file and copy or reference its content to start that layer.
- `corrections/` — Verification feedback files (populated by pipeline runs, read by developer during refinement)
- `case-studies/lre/` — LRE case study: system description + 51 structured requirements (26 functional, 18 non-functional, 7 design constraints)

The Claude Code slash commands (`/gen-trace`, `/run-pipeline`, `/verify`, `/fix-from-feedback`) live in [`.claude/skills/`](.claude/skills/) and load automatically on clone — no install step.

#### Java code rules enforced

The `prompts/java_codegen_rules.txt` asset enforces strict patterns required for formal model extraction:

- **Mode-nested if-else** — two-level structure: outer = `currentMode` switch, inner = transition conditions
- **Named boolean predicates** — no inline compound conditions, no ternary operators
- **Sealed event interfaces** — `InputEvent`, `OutputEvent` as sealed super-types with record subtypes
- **No lambdas/streams/pattern-matching/ternary** — these constructs cannot be reliably mapped to RoboChart by the ETL
- **Records for immutable types** — immutable value types as Java records
- **`@RoboChartType` annotations** — type hints (`"nat"`, `"real"`) on fields/parameters for RoboChart type mapping
- **Operation `compute()` methods** — only direct `this.field = expression` assignments (no local variables)

### [forge.pipeline/](forge.pipeline/)

The deterministic pipeline phases (2a–6c) and the vacuity audit, as a Python package with a command line. No LLM dependencies: code generation is done interactively (Claude Code / Cline), and this package only runs tools and writes reports.

```bash
cd forge.pipeline
pip install -r requirements.txt   # pyyaml, jsonschema, ruamel.yaml
python -m forge_pipeline run-iteration        # every phase in order
python -m forge_pipeline run-phase fdr4       # one phase
python -m pytest tests
```

`experiments/scripts/run_experiment_iteration.py` is a thin wrapper around `run-iteration`. Each phase writes `forge.assets/corrections/post_<phase>.{md,json}`; the phase's own report, not the exit status, is the verdict.

| Phase | Id | Description |
|---|---|---|
| **2a — Compile** | `compile` | `gradlew build` against `java.generated.project/`; surfaces Java compile errors before any model-extraction phase runs |
| **2b — Coverage** | `coverage` | Bidirectional requirement ↔ Java trace check; flags missing implementations and over-implementations against `requirement_all.json` |
| **2c — Preflight** | `preflight` | Structural lint of the generated Java (no transformation); catches CLAUDE.md rule violations early |
| **3 — T2M** | `t2m` | Spoon discovery: Java → EMF model |
| **4 — M2M** | `m2m` | ETL transformation: Java EMF → RoboChart EMF |
| **5a — CSP Gen** | `m2t` | RCT generation + RoboChart CSP generator |
| **5b — Dafny Gen** | `dafny_gen` | Dafny code generation from Spoon EMF |
| **5c — Isabelle Gen** | `isabelle_gen` | Isabelle/UTP Z-Machine theory generation (forked EGL template) |
| **6a — FDR4** | `fdr4` | CSP refinement checking (deadlock, divergence; determinism assertions are stripped) |
| **6b — Dafny** | `dafny_verify` | Design-by-contract verification (Z3 SMT) |
| **6c — Isabelle** | `isabelle_verify` | Deductive deadlock-freedom + invariants via `isabelle build` (Linux native; WSL on Windows) |

Phases 1–2 (requirements + code generation) and Phase 7 (refinement) are interactive — performed by the developer with Claude Code / Cline in VS Code, guided by `CLAUDE.md`.

### `java.codegen.pipeline/` *(removed)*

The previous AutoGen + DeepSeek multi-agent pipeline. Removed from main in May 2026; preserved at git tag `v1.0-autogen-pipeline`. Recover with:

```bash
git checkout v1.0-autogen-pipeline -- java.codegen.pipeline/
```

The CSP corrections logic (`phases/apply_csp_corrections.py`) and its `csp_overrides.csp` / `type_ranges.json` configuration files lived under that directory; the equivalent overrides path in the current architecture is hand-maintained CSP-M overrides at `forge.assets/corrections/csp_overrides.csp` (consumed by the pipeline's CSP corrections step, not by an autogen pre-run).

---

### [forge.transformations/](forge.transformations/)

A six-phase model extraction and generation tool (Gradle project, Java 17+). 16 Java source classes + 4 Epsilon transformation scripts.

```
Phase 1 (t2m):     Java source  →  Spoon EMF model  →  discovered_model.xmi
Phase 2 (m2m):     Spoon EMF    →  RoboChart EMF    →  robochart_model.xmi
Phase 3 (m2t):     RoboChart    →  .rct text file   →  robochart_controller.rct
Phase 4 (csp-gen): .rct         →  CSP-M            →  output/csp-gen/ (71 files)
Phase 5 (dafny):   Spoon EMF    →  Dafny            →  LreController.dfy
Phase 6 (isabelle): RoboChart EMF →  Isabelle/UTP Z-Machine  →  output/isabelle/<Stm>_Beh.thy + ROOT
```

#### Running

```bash
cd forge.transformations

# Phase 3 — T2M: Spoon Discovery (Java → EMF model)
./gradlew run --args="t2m source=../java.generated.project/src/main/java output=output"

# Phase 4 — M2M: ETL Transformation (Java EMF → RoboChart EMF)
./gradlew run --args="m2m source=../java.generated.project/src/main/java output=output"

# Phase 5a — M2T: RCT + CSP Generation (RctPhase then roboChartCspGen)
./gradlew run --args="m2t output=output"

# Phase 5b — Dafny Generation
./gradlew run --args="dafny_gen source=../java.generated.project/src/main/java output=output"

# Phase 5c — Isabelle Theory Generation
./gradlew run --args="isabelle_gen output=output"

# Regenerate spoon.ecore (only needed when Spoon version changes)
./gradlew generateSpoonEcore
```

Phase IDs and their full invocation details are defined in [pipeline.yaml](pipeline.yaml) at the repo root.

#### Technology stack

| Component | Technology | Version |
|---|---|---|
| Java parser | [Spoon](https://spoon.gforge.inria.fr/) (no-classpath mode, Java 17 compliance) | 11.3.0 |
| EMF framework | Eclipse EMF (ecore, ecore.xmi, common) | 2.41.0 / 2.42.0 |
| M2M transformation | [Epsilon ETL](https://eclipse.dev/epsilon/) — `java2robochart.etl` (3,060 lines) | 2.8.0 |
| M2T generation | [Epsilon EGL](https://eclipse.dev/epsilon/) — `robochart2rct.egl`, `java2dafny.egl`, `thy_generation_rule.egl` (forked from ICECCS2023) | 2.8.0 |
| CSP-M generation | Official RoboChart CSP generator (`circus.robocalc.robochart.generator.csp`) | 3.1.0 |
| Dafny generation | `Java2DafnyEglTransformer.java` driving `java2dafny.egl` against the Spoon EMF model | — |
| Isabelle/UTP Z-Machine generation | `IsabelleEgxRunner.java` driving forked `thy_generation_rule.egl` against the RoboChart EMF model | — |
| Isabelle proof checker | Isabelle2023-CyPhyAssure (Linux/WSL) — `Z_Machines` session heap | — |
| Metamodels | `spoon.ecore` (auto-generated, 127 EClasses, 5 EEnums), `robochart.ecore` (hand-crafted subset) | — |

#### Java source classes

**Discovery (5 classes)**:
| Class | Lines | Purpose |
|---|---|---|
| `SpoonDiscoverer` | 480 | Parses Java → Spoon EMF model via `CtRole`-based value access; contains inner `SpoonToEmfMapper` |
| `SpoonEcoreGenerator` | 270 | Auto-generates `spoon.ecore` from Spoon's `Metamodel` API (4-pass: create EClasses → wire supertypes → create features → deduplicate) |
| `SpoonJavaMetamodel` | 90 | Thread-safe singleton for runtime `spoon.ecore` loading (nsURI: `http://spoon.gforge.inria.fr/spoon`) |
| `ValueResolver` | 109 | Resolves `CtLiteral` values and `static final` constant references from live Spoon AST |
| `RecordMetadataResolver` | 63 | Extracts Java record component metadata (field name + type) for event data typing |

**Transform (8 classes — App in root package, rest in `transform/`)**:
| Class | Purpose |
|---|---|
| `App` | CLI entry point — reads `pipeline.yaml`; subcommands `t2m`, `m2m`, `m2t`, `dafny_gen`, `isabelle_gen`, `preflight`; writes trace JSON files |
| `Java2RoboChartTransformer` | ETL wrapper — injects `resolvedValues`, `recordMetadata`, `traceEntries`, configurable naming (enumSuffixes, stepMethodName, modeFieldName) |
| `EtlTransformRunner` | Generic Epsilon ETL executor — wraps EMF models in `InMemoryEmfModel`, injects frame stack variables |
| `Java2DafnyEglTransformer` | EGL wrapper for `.dfy` generation from Spoon EMF — drives `java2dafny.egl` to emit mode datatype, abstracted functions, transition methods with requires/ensures, determinism lemmas |
| `RoboChart2RctTransformer` | EGL wrapper for `.rct` generation; injects `traceEntries` and `constantDefaults` |
| `IsabelleEgxRunner` | EGL wrapper for `.thy` generation from RoboChart EMF — drives forked `transformations/thy_generation_rule.egl` (loaded from classpath), also writes the `ROOT` session file |
| `EglGenerationRunner` | Generic Epsilon EGL executor — `module.execute()` returns `Object`, must cast to `String` |
| `RoboChartMetamodel` | Thread-safe singleton for runtime `robochart.ecore` loading (nsURI: `http://www.robocalc.circus/RoboChart`) |

#### ETL transformation (`java2robochart.etl`, 3,060 lines)

Pattern-matching extraction of RoboChart state machine from Spoon Java model:

1. **Mode enum detection** — finds `CtEnum` with suffix "Mode" or "State"; extracts enum values as RoboChart `State` nodes
2. **Controller class detection** — finds `CtClass` containing `step()` method
3. **Constants class detection** — heuristics: not step() container, not nested, ≥2 `static final` primitive fields → builds `constantFieldMap` (SCREAMING_SNAKE → camelCase), `constantTypeMap`, `constantValueMap`
4. **State creation** — one RoboChart `State` per enum value; first value = initial state
5. **If-else chain walking** — collects local variable predicate definitions, then walks outer if-else:
   - `currentMode == MyMode.X` → source state
   - `event instanceof EventType` → trigger event
   - Remaining conjuncts → guard condition (with predicate inlining)
   - Assignment in then-block → target state
6. **Trace generation** — appends entries to injected `traceEntries` list (Java element → RoboChart element mappings)

Key injected variables: `enumSuffixes`, `stepMethodName`, `modeFieldName`, `traceEntries`, `resolvedValues`, `recordMetadata`

#### EGL templates

**`robochart2rct.egl`** — generates `.rct` textual notation:
- Event classification: input (used in triggers) vs output (used only in actions)
- Guard variable classification via `classifyGuardVar()`: boolean/real × var/const
- Entry action promotion: if all incoming transitions share same action, promote to state entry
- Generates: diagram, datatypes, functions, interfaces (Inputs, Outputs, Ctrl_State, Constants), controller with STM, module with event bindings
- Traceability: maps each rendered element to RCT line range → `trace_m2t_rct.json`

**`java2dafny.egl`** — generates Dafny directly from the Spoon EMF model:
- Mode datatype, InputEvent datatype, controller class with `Valid()` ghost predicate
- Sensor/constant methods as abstract functions with `reads this`
- Per-mode transition methods with `requires`/`ensures` contracts
- Determinism lemmas for guard-only (triggerless) transitions

**`thy_generation_rule.egl`** — generates the Isabelle/UTP Z-Machine
theory file (`<Stm>_Beh.thy`) from the RoboChart EMF model. Forked from
the ICECCS2023 archive's template. The environment model the generated
invariant encodes, and the deadlock-freedom (totality) proof tactic, are
described in the template's comments and in
[experiments/environment-model/README.md](experiments/environment-model/README.md).

#### Traceability chain

Four JSON trace files provide end-to-end traceability:

```
trace_t2m.json          Spoon AST → source file/line positions (Phase 1)
trace_m2m.json          Java element → RoboChart element (Phase 2)
trace_m2t_rct.json      RoboChart element → RCT line range (Phase 3)
trace_full.json         Merged: requirement → Java → RoboChart → RCT (cross-phase)
```

`App.java` post-processes traces via `writeM2MTrace()`, `writeRctTrace()`, and `enrichWithJavaSource()` to cross-reference RoboChart elements with Java file/line positions.

#### Official CSP generator output structure

The RoboChart CSP generator (41 JARs in `lib/robochart-csp-gen/`) produces two semantics:

```
output/csp-gen/
  instantiations.csp          -- type ranges and function stubs (patched by apply_csp_corrections)
  file_robochart_controller.csp
  file_robochart_controller_coreassertions.csp
  defs/                       -- untimed CSP-M (33 files)
    LreController.csp         -- STM, D__, O__, sharedVarMemory, Memory_* processes
    LreController_coreassertions.csp  -- P_LreController + 4 assertions
    CalcVel.csp, CalcCDyn.csp, CalcCPA.csp, CalcCStc.csp, CheckOPEZ.csp  -- per-operation CSP
    core_defs.csp, robochart_defs.csp, state_defs.csp   -- toolkit definitions
    function_toolkit_defs.csp, relation_toolkit_defs.csp, sequence_toolkit_defs.csp, set_toolkit_defs.csp
    ...
  timed/defs/                 -- tock-CSP semantics (35 files; unconditional output of the generator, retained for a future tock-CSP verification path — current FDR4 verification uses defs/ only)
```

Key CSP processes:
- `STM(id__, ...)` — parallel composition of state machine body with `sharedVarMemory`
- `D__(id__, ...) = STM(...) \ internal_events` — hides internal events
- `O__(id__, ...) = sbisim(diamond(D__(...)))` — strong bisimulation + diamond compression
- `sharedVarMemory(id__, ...)` — parallel composition of `Memory_<var>` cell processes
- `P_LreController` — top-level assertion process (uses `O__`)

#### FDR4 verification results

```
P_LreController :[deadlock free]   PASS  5 states  89 transitions
P_LreController :[deterministic]   -- stripped by run_fdr4; not checked
P_LreController :[divergence free] PASS  5 states  89 transitions
```

The 5 states are `sbisim(diamond(...))` bisimulation equivalence classes. `sbisim` (strong bisimulation) compresses both states and transitions. Internally FDR4 enumerates ~5,600 states and ~39,000 transitions before compression. The compressed result is equally sound — FDR4 explores the full state space; the reported count is just the compressed representation.

#### Dafny verification results

```
Dafny program verifier finished with 8 verified, 0 errors
```

The generated `LreController.dfy` (from `Java2DafnyEglTransformer`, which drives `java2dafny.egl`):
- Mode enum as `datatype Mode = OCM | MOM | HCM | CAM`
- Sensor methods (`vel()`, `inOpez()`, `cdyn()`, etc.) as bodyless (uninterpreted) functions
- Constants object methods as bodyless functions
- Static final fields as top-level `const` declarations
- Guard predicates recursively inlined into transition guards (10-pass expansion)
- Per-mode transition methods with `requires`/`ensures` contracts
- Main `step()` dispatching to per-mode methods

#### Spoon EMF design decisions

- `spoon.ecore` is auto-generated via `gradlew generateSpoonEcore` using Spoon's `Metamodel` API (4-pass algorithm)
- **127 EClasses, 5 EEnums** (BinaryOperatorKind, UnaryOperatorKind, ModifierKind, etc.)
- All references are **containment** (`containment="true"`) — Spoon AST is a tree; non-contained objects break Epsilon property resolution
- `CtRole`-based value access: `element.getValueByRole(CtRole.NAME)` — robust across Spoon versions
- `CtLiteral` has no `value` attribute in ecore — use `resolvedValues` map from `SpoonDiscoverer` (which calls `CtLiteral.getValue()` on live AST)
- `CtUnaryOperator` uses `expression` feature (not `operand`)
- `CtLocalVariable` initializer is in `CtRHSReceiver.assignment` (not `CtVariable.defaultExpression`)
- `CtFieldWrite` for assignment targets (not `CtFieldRead`) — both must be checked in `isCurrentModeRef`

#### Known CSP generator limitations

- `.rct` function bodies must be empty (`{ }`) — even `{ 0 }` fails the parser
- Function-typed variable application (e.g. `obs(cstc)` where `obs : nat -> Obstacle`) crashes the generator: "Other types of callees not yet supported"
- Uninterpreted functions produce stubs in `instantiations.csp` (default: return 0)
- `instantiations.csp` type ranges can be too large for FDR4 (OOM) — use `apply_csp_corrections`
- CSP-M `{-` opens a multiline comment — write `{ -1..1}` (space after `{`) for negative ranges

---

### [java.generated.project/](java.generated.project/)

The LLM-generated Java project, produced interactively in VS Code with Claude Code / Cline (guided by [CLAUDE.md](CLAUDE.md)). Contains 15 Java source files implementing the LRE safety controller as a flat mode-nested if-else state machine in `LreController.step()`. No T2M output included.

### [forge.transformations/ref-rct-project/](forge.transformations/ref-rct-project/)

Hand-authored reference `.rct` model and its CSP-M output (generated by the official RoboChart CSP generator). Used as ground-truth comparison when the generated RCT diverges.

---

## Correction feedback loop

The developer drives an iterative refinement cycle using verification feedback:

```
 Developer + Claude Code / Cline (VS Code)
       │  writes/refines Java code
       ▼
 gradlew build  ──fail──▶  Developer fixes compilation errors
       │
      pass
       │
       ▼
 Pipeline: T2M → M2M → M2T → FDR4 / Dafny / Isabelle
       │
      fail ──▶  forge.assets/corrections/
       │                    │
       │           Developer reads feedback,
       │           traces errors to Java source
       │           via trace_full.json
       │                    │
       ▼                    ▼
     pass          Developer makes surgical fixes
       │           in VS Code, re-runs pipeline
       ▼
   Verified ✓
```

Unlike the previous AutoGen pipeline (which regenerated all files each iteration), the developer can make **targeted fixes** — reading specific verification errors, tracing them through the transformation chain, and editing only the affected code.

---

### The experiments

The code on `main` is the pipeline at the revision the paper reports. The 75 LLM
sessions of Section 4 (`experiments/v3-cold/`, `experiments/v3-reference/`,
`experiments/v3-compile-only/`) all ran against this pipeline under the study-free
code-generation instructions in `forge.assets/prompts/`; the design of the collection,
the prompt every session received and the scripts that set up, ran, recorded and audited
each session are in `experiments/v3-preregistration/`. The mutation campaign
(`experiments/mutation-testing/`), the requirements-conformance checks
(`experiments/requirements-conformance/`, `experiments/reference-specification/`) and the guard-level
enumeration (`experiments/guard-level-enumeration/`) were run at the same revision.
`experiments/README.md` maps each folder to the section and table it supports.

**Checks that run from this repository.**

```
bash scripts/regression_test.sh                    # extraction determinism (RQ1)
cd forge.pipeline && python -m pytest tests        # includes the Phase 6d positive controls
```

Phase 6d (the vacuity audit) reports blocking findings (an unsatisfiable precondition, an antecedent that never holds, an empty domain, an initial state outside the invariant, a mode or transition unreachable in the symbolic model, a constant that differs between backends) and advisory ones (unreachability under the narrowed FDR4 instantiation, narrowed singleton domains, terminal states that offer nothing, and the two generator placeholders: the Dafny class invariant `Valid()` being `true` while the per-transition `ensures` clauses carry the obligations, and the Isabelle requirement predicate `R1` being `True` with one lemma per operation preserving it, each reported with the number of obligations it accounts for).

```
bash scripts/generate_isabelle.sh /tmp/enum-thy
python experiments/guard-level-enumeration/run_enumeration.py --thy-root /tmp/enum-thy   # guard-level enumeration (Section 5.5)
```

---

## Installation

### Prerequisites

| Tool | Version | Install |
|---|---|---|
| Java | 17+ | System package manager |
| Gradle | via wrapper | Included (`gradlew`) |
| Python | 3.9+ | System package manager |
| FDR4 | 4.2.7 | [cocotec.io/fdr](https://cocotec.io/fdr/) |
| Dafny | 4.11.0 | See below |
| Isabelle2023-CyPhyAssure | 2023 fork | Linux container via `scripts/isabelle-docker.sh` (native Linux or WSL also work) |

### Installing Dafny

Install Dafny 4.11.0 from its GitHub releases page and put `dafny` on the `PATH`; the
pipeline invokes it as `dafny verify` with the bundled Z3.

### Pipeline setup

```bash
cd forge.pipeline
pip install -r requirements.txt   # pyyaml, jsonschema, ruamel.yaml
```

---

## Quick Start

```bash
# 1. Write Java code interactively with Claude Code / Cline in VS Code.
#    Guided by CLAUDE.md and the five-layer top-down prompts under
#    forge.assets/vibe-coding-prompts/.

# 2. Run the pipeline, every phase from 2a (Compile) through 6c (Isabelle).
python experiments/scripts/run_experiment_iteration.py
# (or: cd forge.pipeline && python -m forge_pipeline run-iteration)
# Each phase writes forge.assets/corrections/post_<phase>.{md,json}.

# 3. Read any post_<phase>.md that reports failure, then in Claude Code:
#    /fix-from-feedback
# Apply the suggested fix, re-run the failed phase, repeat.

# Phase-by-phase alternative from Claude Code:
#   /run-pipeline   — runs the gradle-driven phases sequentially
#   /verify         — runs FDR4 + Dafny + Isabelle
#   /gen-trace      — produces result_codegen.json (input to coverage)
```

The Phase 6c (Isabelle) verifier runs in the Linux container that
`scripts/isabelle-docker.sh` starts; on Windows, run it under WSL.

---

## Directory structure

```
Forge/
├── README.md
├── LICENSE
├── CLAUDE.md                        # Project rules read by the Claude Code sessions (references forge.assets/prompts/)
├── pipeline.yaml                    # Phase manifest: order, commands, time and memory limits
├── .gitignore
│
├── docker/                          # Isabelle/UTP (CyPhyAssure 2023) container used by the Isabelle phase
├── reference-runs/                  # The three RoboStar case studies as recorded before the evaluation: Java and artefacts
├── experiments/                     # Records behind Section 4 (see experiments/README.md)
│
├── forge.assets/                 # Shared assets (domain-independent + case studies)
│   ├── prompts/                     #   LLM-context material (auto-loaded via CLAUDE.md @file)
│   │   ├── java_codegen_rules.txt   #     Strict patterns for formal model extraction
│   │   ├── chain_of_thought_codegen.txt  # Step-by-step reasoning guide
│   │   ├── few_shot_codegen.txt     #     Complete worked example (traffic controller)
│   │   └── fdr4_system.txt          #     FDR4 feedback interpretation guide
│   ├── vibe-coding-prompts/         #   User-driven workflow prompts (top-down 5 layers)
│   │   ├── README.md                #     Index + usage notes
│   │   └── 0N_*.md                  #     One file per layer (architecture, skeleton, ...)
│   ├── corrections/                 #   Verification feedback files
│   └── case-studies/
│       └── lre/                     #   LRE case study (AUV safety controller)
│           ├── system_description.txt
│           └── requirements/        #     51 requirements (JSON + TXT)
│
├── forge.pipeline/               # Deterministic phases + vacuity audit (Python)
│   ├── README.md
│   ├── requirements.txt             #   pyyaml, jsonschema, ruamel.yaml
│   ├── tests/
│   └── forge_pipeline/
│       ├── engine.py                #     Runs one phase of any kind, writes its report
│       ├── iteration.py             #     Phase order and authoritative verdicts
│       ├── cli.py                   #     python -m forge_pipeline ...
│       ├── phases/                  #     One module per Python phase
│       ├── feedback/                #     Report schema and per-backend classifiers
│       ├── audit/                   #     Vacuity audit (checks/, sat.py)
│       └── csp/                     #     instantiations.csp type-range corrections
│
├── forge.transformations/         # Gradle model extraction tool
│   ├── build.gradle                 #   Dependencies: Spoon 11.3.0, Epsilon 2.8.0, EMF 2.41.0
│   ├── src/main/java/.../
│   │   ├── App.java                 #     CLI entry point (487 lines)
│   │   ├── discovery/
│   │   │   ├── SpoonDiscoverer.java         #  Java → Spoon EMF (480 lines)
│   │   │   ├── SpoonEcoreGenerator.java     #  Auto-gen spoon.ecore (270 lines)
│   │   │   ├── SpoonJavaMetamodel.java      #  Metamodel singleton (90 lines)
│   │   │   ├── ValueResolver.java           #  Literal/constant resolution (109 lines)
│   │   │   └── RecordMetadataResolver.java  #  Record component metadata (63 lines)
│   │   ├── transform/
│   │   │   ├── Java2DafnyEglTransformer.java # EGL wrapper (Spoon EMF → Dafny via java2dafny.egl)
│   │   │   ├── Java2RoboChartTransformer.java # ETL wrapper (Spoon EMF → RoboChart EMF)
│   │   │   ├── RoboChart2RctTransformer.java  # EGL wrapper for RCT
│   │   │   ├── IsabelleEgxRunner.java         # EGL wrapper for Isabelle .thy + ROOT (drives transformations/thy_generation_rule.egl)
│   │   │   ├── EtlTransformRunner.java        # Generic ETL executor
│   │   │   ├── EglGenerationRunner.java       # Generic EGL executor
│   │   │   └── RoboChartMetamodel.java        # Metamodel singleton
│   │   └── lint/
│   │       └── StructuralLinter.java          # Pre-transformation Java structural lint
│   ├── src/main/resources/
│   │   ├── metamodels/
│   │   │   ├── spoon.ecore          #   Auto-generated: 127 EClasses, 5 EEnums
│   │   │   └── robochart.ecore      #   Hand-crafted RoboChart subset
│   │   └── transformations/
│   │       ├── java2robochart.etl       #   M2M: Spoon EMF → RoboChart EMF
│   │       ├── robochart2rct.egl        #   M2T: RoboChart → RCT
│   │       ├── java2dafny.egl           #   M2T: Spoon EMF → Dafny
│   │       └── thy_generation_rule.egl  #   M2T: RoboChart EMF → Isabelle .thy (forked from ICECCS2023)
│   ├── lib/robochart-csp-gen/       #   Official RoboChart CSP generator v3.1.0 (41 JARs)
│   ├── output/                      #   T2M pipeline output
│   └── ref-rct-project/             #   Hand-authored reference .rct + CSP ground truth
│
├── java.generated.project/          # LLM-generated Java (15 classes)
│   └── src/main/java/com/example/lre/
│       ├── controller/LreController.java  # Main state machine (130 lines)
│       ├── mode/LreMode.java              # 4-mode enum (OCM, MOM, HCM, CAM)
│       ├── event/{InputEvent,OutputEvent}.java  # Sealed event hierarchy
│       ├── sensor/Sensor.java             # Environment data interface
│       ├── actuator/Actuator.java         # Velocity/heading output
│       ├── constant/LreConstants.java     # 4 safety thresholds
│       ├── operation/{CalcVel,CalcCStc,CalcCDyn,CalcCPA,CheckOPEZ}.java
│       ├── type/{Obstacle,ObstacleRegister}.java  # Records
│       └── annotation/RoboChartType.java  # Type hint annotation
│
│
└── scripts/                         # Regression test and the Isabelle container wrapper
```

---

## Development

`bash scripts/regression_test.sh` re-extracts the fixtures under `scripts/regression-fixtures/` and
checks the extracted state-machine structure and generated CSP against the recorded output.

## References

- Miyazawa et al., *"RoboChart: modelling and verification of the functional behaviour of robotic applications"*, SoSyM 2019
- Foster et al., *"Formal Model-Based Assurance Cases in Isabelle/SACM"*, FormaliSE 2020
- Wei et al., *"REMEDIATE"*, JSS 2024 (arXiv:2403.15236)
- [RoboChart Reference Manual](https://robostar.cs.york.ac.uk/publications/techreports/reports/robochart-reference.pdf)
- [RoboTool](https://robostar.cs.york.ac.uk/robotool/) — Eclipse IDE for RoboChart
- [FDR4](https://cocotec.io/fdr/) — CSP refinement checker (free for academic use)
- [Spoon](https://spoon.gforge.inria.fr/) — Java source analysis and transformation framework
- [Epsilon](https://eclipse.dev/epsilon/) — model management (ETL, EGL, EOL)
- [Dafny](https://dafny.org/) — formal verification language (Z3 SMT backend)

## Licence

Forge is released under the [PolyForm Noncommercial License 1.0.0](LICENSE):
free for research, teaching and other noncommercial use. Commercial use needs a
separate licence; see [COMMERCIAL-LICENSE.md](COMMERCIAL-LICENSE.md).
Third-party components in `forge.transformations/lib/` keep their own licences
(`forge.transformations/lib/THIRD_PARTY_LICENSES.md`).

