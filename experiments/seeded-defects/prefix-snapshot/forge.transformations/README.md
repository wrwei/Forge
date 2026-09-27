# forge.transformations

A text-to-model (T2M) transformation tool that parses Java source code into EMF models using [Spoon](https://spoon.gforge.inria.fr/), extracts [RoboChart](https://www.cs.york.ac.uk/circus/RoboCalc/) state machine models via Epsilon ETL, and generates tock-CSP for [FDR4](https://cocotec.io/fdr/) verification via Epsilon EGL.

## Purpose

This project bridges the gap between Java source code and formal verification. It takes raw `.java` files as input and produces both structured EMF models and tock-CSP text suitable for FDR4 model checking.

### Pipeline

```
Java source (.java)
  |
  v  Phase 1: Discovery (Spoon parsing -> EMF model)
  |
Spoon Java model (.xmi)
  |
  v  Phase 2: Transformation (Epsilon ETL)
  |
RoboChart state machine (.xmi)
  |
  +---> Phase 3: CSP Generation (Epsilon EGL) --> tock-CSP (.csp) -> FDR4
  |
  +---> Phase 4: RCT Generation (Epsilon EGL) --> complete .rct file
                                                    |
                                                    v  Phase 5: Standalone CSP Generation
                                                    |           (official RoboChart generator)
                                                    |
                                                  csp-gen/ (46 CSP files, untimed + timed)
```

## Architecture

| Component | Description |
|---|---|
| `SpoonDiscoverer` | Parses Java source with Spoon and produces an EMF model via reflective CtRole-based mapping |
| `SpoonJavaMetamodel` | Loads the auto-generated `spoon.ecore` metamodel at runtime |
| `SpoonEcoreGenerator` | One-time generator that produces `spoon.ecore` from Spoon's self-description API |
| `Java2RoboChartTransformer` | Delegates to `EtlTransformRunner` to execute `java2robochart.etl` |
| `EtlTransformRunner` | Generic runner for Epsilon ETL model-to-model transformations |
| `RoboChart2CspTransformer` | Delegates to `EglGenerationRunner` to execute `robochart2csp.egl` |
| `EglGenerationRunner` | Generic runner for Epsilon EGL model-to-text generation |
| `RoboChart2RctTransformer` | Delegates to `EglGenerationRunner` to execute `robochart2rct.egl` |
| `roboChartCspGen` (Gradle task) | Standalone RoboChart CSP generator using official RoboTool JARs |
| `App` | CLI entry point with per-phase subcommands |

### Discovery: Spoon to EMF

The discoverer uses Spoon's `CtRole`-based value access for reliable property mapping from Spoon's in-memory AST to dynamic EMF objects conforming to `spoon.ecore`. This reflective approach (~250 lines) replaces the previous hand-coded MoDisco AST mapper (~1200 lines) while supporting Java 17+ syntax.

### Transformation: ETL state machine extraction

The ETL script (`java2robochart.etl`) pattern-matches on a flat if-else state machine structure:

1. Finds an enum ending in `Mode` or `State` -- these become RoboChart **States**
2. Finds a class with a `step()` method -- the class name becomes the **StateMachine** name
3. Walks the if-else chain in `step()`, extracting from each branch:
   - **Source state** from `currentMode == MyMode.S1`
   - **Trigger event** from `event instanceof EventType`
   - **Guard condition** from any remaining conjuncts (see below)
   - **Target state** from `currentMode = MyMode.S2`

#### Guard extraction

Each if-condition is flattened into AND-conjuncts. Conjuncts matching known patterns (mode comparison, instanceof check) are consumed. Any **unconsumed conjuncts** represent guard conditions from the Java source (e.g. `sensor.inOpez()`, `sensor.cda() < constants.minSafeDist()`).

These are attached to the RoboChart transition as a named boolean constant (`guard_tN`), declared in the generated CSP-M as `guard_tN = true`. This approach:

- Preserves the **existence** of guards so triggerless transitions are no longer unconditional
- Produces **syntactically valid** CSP-M (guards are boolean constants, usable in `(guard) & P`)
- Eliminates **spurious divergence** -- tau cycles (e.g. MOM |~| HCM |~| MOM) are gated by guards
- Enables **scenario testing** -- set guards to `true`/`false` to explore reachability in FDR4

#### Scope

The pipeline targets a **single controller / state machine**. Multi-controller composition (E7, G2, G3, G4) is out of scope by design.

#### Limitations

The ETL is designed for a specific flat if-else state machine pattern. See [pipeline_limitations.md](../docs/fixes/pipeline_limitations.md) for the full detailed analysis. Key remaining limitations:

| ID | Status | Limitation | Detail |
|----|--------|------------|--------|
| E1 | Partial | **Opaque guards** | Named local variable predicates work; inline field reads and method calls still become synthetic `guard_tN`. |
| E11 | Partial | **Event data not captured** | Codegen enforces traditional instanceof (no pattern matching), abstract data flow (`?v`/`!v`), typed channels. Remaining: concrete type mapping, multi-field records, literal value extraction. |

Fixed: E2, E3, E4, E5, E6, E8, E9, E10, E12, E13, G1, G5, D3.

### Generation: EGL tock-CSP output

The EGL template (`robochart2csp.egl`) translates the RoboChart model into tock-CSP text for FDR4:

1. Declares **channels** for each event in the state machine
2. Generates **process definitions** for each state:
   - Triggered transitions become **external choice** (`[]`): `event -> TargetState`
   - Guard-only (triggerless) transitions become **internal choice** (`|~|`): `TargetState`
   - Mixed states wrap triggered transitions in parens: `(e1 -> T1 [] e2 -> T2) |~| T3`
3. Emits **FDR4 assertions**: deadlock freedom, divergence freedom, determinism
4. Includes transition name comments for traceability back to the RoboChart model

The template also supports guard conditions (`(guard) & event -> Target`) and expression formatting for the full RoboChart expression hierarchy (binary, unary, variable references, calls).

### Generation: EGL complete RCT output

The EGL template (`robochart2rct.egl`) generates a **complete** `.rct` file compatible with the standalone RoboChart CSP generator. The output includes all structural elements required by the RoboChart textual parser: `diagram`, `interface` declarations, `stm` with `uses`/`requires` clauses, a `controller` block, and a `module` block.

All needed information is derived from the RoboChart XMI model (`robochart_model.xmi`) through deterministic rule-based analysis — no LLM or agent is involved in this step.

#### Event classification

Events are classified by how they appear in transitions:

| Usage Pattern | Classification | Interface |
|---|---|---|
| Used in a transition `trigger` | Input event | `Inputs` |
| Used only in transition `action` (never as trigger) | Output event | `Outputs` |

#### Guard variable discovery and classification

Guard variables are identified by scanning all transition conditions for `CallExp` nodes (method-call references like `sensor.vel()`, `sensor.inOpez()`, `constants.minSafeDist()`). Each discovered variable name is then classified by **type** (`boolean` or `real`) and **kind** (`var` or `const`) based on how it is used across all conditions.

The classification rules analyse the *syntactic position* of each variable within the expression tree:

| Rule | Type | Kind | Rationale |
|---|---|---|---|
| Appears as direct operand of `Not(...)` | `boolean` | `var` | Boolean negation implies boolean type |
| Appears as bare child of `And`/`Or` (not wrapped in a comparison) | `boolean` | `var` | Used as boolean conjunct/disjunct |
| Appears on the **left** side of any comparison (`<`, `>`, `<=`, `>=`, `=`) | `real` | `var` | Left operand is typically the measured quantity |
| Appears **only** on the **right** side of comparisons (never left, never boolean) | `real` | `const` | Right operand is typically the threshold constant |

**Example** from the LRE controller:

```
condition not ( inOpez ) /\ cda < minSafeDist /\ tcpa >= 0
```

- `inOpez` → operand of `Not(...)` → **boolean, var**
- `cda` → left of `<` → **real, var**
- `minSafeDist` → right of `<`, never on left → **real, const**
- `tcpa` → left of `>=` → **real, var**

This produces four interface blocks:

```
interface Inputs    { event ReqVel : real ... }     -- trigger events
interface Outputs   { event apply }                  -- action-only events
interface Ctrl_State { var vel : real; var inOpez : boolean ... }  -- state variables
interface Constants  { const minSafeDist : real ... }              -- threshold constants
```

#### Float literal workaround

The standalone RoboChart CSP generator v3.0.0 cannot handle `FloatExp` nodes produced by the textual parser v3.1.0. The EGL works around this by converting fractional float values to their ceiling integer equivalents (e.g. `0.1` → `1`, `5.0` → `5`).

#### Naming conventions

| Element | Pattern | Example |
|---|---|---|
| Diagram | `<stm_name>` | `LreController` |
| Interfaces | Fixed names | `Inputs`, `Outputs`, `Ctrl_State`, `Constants` |
| Controller | `<stm_name>_Ctrl` | `LreController_Ctrl` |
| Module | `<stm_name>_Module` | `LreController_Module` |
| Robotic platform | Fixed name | `Vehicle` |

### Standalone RoboChart CSP generation

The standalone CSP generator uses the official RoboChart generator JARs (41 JARs from RoboTool, Strategy 1 from [robochart_csp_generator.md](../docs/research/robochart_csp_generator.md)) via Xtext standalone setup. It takes the complete `.rct` file as input and produces the full CSP semantics.

```
gradle roboChartCspGen -ProboChartProject=output
```

Output:
- `output/csp-gen/` — untimed CSP-M files
- `output/csp-gen/timed/` — tock-CSP files (with `tock` event for discrete time)

The generator JARs are located in `lib/robochart-csp-gen/`. The entry point is `circus.robocalc.robochart.generator.csp.Main`, invoked via Xtext's `IGenerator2` interface with `RoboChartStandaloneSetup.createInjectorAndDoEMFRegistration()`.

## Usage

### Run all phases

```
gradle run --args="<java-source-path> [output-dir]"
```

Output directory defaults to `output/`.

### Per-phase subcommands

Each pipeline phase can be run independently:

| Command | Phases | Description |
|---------|--------|-------------|
| `all <src> [out]` | 1-4 | Run the full pipeline (default) |
| `discover <src> [out]` | 1 | Spoon discovery only |
| `transform <src> [out]` | 1+2 | Discovery + ETL extraction |
| `csp [out]` | 3 | Generate CSP-M from `robochart_model.xmi` |
| `rct [out]` | 4 | Generate RCT from `robochart_model.xmi` |

The `csp` and `rct` subcommands load `robochart_model.xmi` from the output directory, so they can be re-run without repeating discovery and transformation. The `transform` subcommand re-runs discovery internally because the ETL needs resolved-value and record metadata that are computed during Spoon parsing.

```bash
# Full pipeline (phases 1-4)
gradle run --args="../java.generated.project/src/main/java build/output"

# Just discovery
gradle run --args="discover ../java.generated.project/src/main/java build/output"

# Discovery + ETL, then iterate on CSP generation
gradle run --args="transform ../java.generated.project/src/main/java build/output"
gradle run --args="csp build/output"

# Regenerate RCT from existing model
gradle run --args="rct build/output"

# Phase 5: Standalone RoboChart CSP generation (requires .rct from phase 4)
gradle roboChartCspGen -ProboChartProject=output
```

### Running phases via the pipeline manifest

The canonical way to invoke any phase is through the pipeline manifest at the repo root (`pipeline.yaml`). Either drive it interactively through the dashboard (`python forge.dashboard/web/server.py`), or invoke a single phase directly:

```bash
./gradlew.bat run --args="m2m source=../java.generated.project/src/main/java output=output"
./gradlew.bat run --args="dafny_gen source=../java.generated.project/src/main/java output=output"
./gradlew.bat run --args="isabelle_gen output=output"
```

The phase ids match the `phases:` keys in `pipeline.yaml`. See [CLAUDE.md](../CLAUDE.md) for the full phase list, and the repo-root `scripts/regression_test.sh` for an end-to-end M2M → RctPhase → CSP-gen invocation against both case studies.

### Example

```
gradle run --args="../java.generated.project/src/main/java build/output"
```

Output:
```
Discovering Java sources in: ..\java.generated.project\src\main\java
Model saved to: ...\build\output\discovered_model.xmi
Summary: 9 types, 75 methods, 39 fields
Extracting RoboChart state machine...
RoboChart model saved to: ...\build\output\robochart_model.xmi
RoboChart: 4 states, 18 transitions, 4 events
Generating tock-CSP from RoboChart model...
CSP-M written to: ...\build\output\robochart_controller.csp
Generating RoboChart textual notation...
RCT written to: ...\build\output\robochart_controller.rct
```

## Metamodels

### Spoon Java metamodel (`spoon.ecore`)

Auto-generated from Spoon's `Metamodel` API by `SpoonEcoreGenerator`.

- **nsURI**: `http://spoon.gforge.inria.fr/spoon`
- **Scope**: 127 EClasses, 5 EEnums, 132 features -- full Java language representation (Java 17+)
- **Regenerate**: `gradle generateSpoonEcore` (only needed when upgrading Spoon version)

### RoboChart metamodel (`robochart.ecore`)

Subset of the RoboChart metamodel sufficient for state machine extraction.

- **nsURI**: `http://www.robocalc.circus/RoboChart`
- **Scope**: `RCPackage`, `StateMachineDef`, `State`, `Initial`, `Transition`, `Trigger`, `Event`

### Key model structure (Spoon)

```
CtPackage
+-- declaredType: CtClass | CtInterface | CtEnum | ...
|   +-- typeMember: CtMethod | CtConstructor | CtField | ...
|   |   +-- body: CtBlock
|   |       +-- statement: CtIf | CtAssignment | CtReturn | ...
|   +-- value: CtEnumValue* (for CtEnum)
+-- pack: CtPackage* (sub-packages)
```

### Key model structure (RoboChart output)

```
RCPackage
+-- machines: StateMachineDef*
    +-- nodes: State* | Initial*
    +-- transitions: Transition*
    |   +-- trigger: Trigger -> Event
    |   +-- condition: Expression?
    |   +-- action: Statement?
    +-- events: Event*
```

## Model output locations

| Context | Path | Format |
|---|---|---|
| CLI (`gradle run`) | `<output>/discovered_model.xmi` | XMI |
| CLI (`gradle run`) | `<output>/robochart_model.xmi` | XMI |
| CLI (`gradle run`) | `<output>/robochart_controller.csp` | tock-CSP text |
| CLI (`gradle run`) | `<output>/robochart_controller.rct` | RoboChart textual notation |
| CLI (`gradle roboChartCspGen`) | `<output>/csp-gen/*.csp` | Untimed CSP-M |
| CLI (`gradle roboChartCspGen`) | `<output>/csp-gen/timed/*.csp` | Tock-CSP |
| Tests (`gradle test`) | `build/test-output/<testName>_robochart.xmi` | XMI |
| Tests (`gradle test`) | `build/test-output/robochart2csp_test.csp` | tock-CSP text |

## Building

```
gradle clean build
```

Requires Java 17+.

## Dependencies

- **Spoon** (`fr.inria.gforge.spoon:spoon-core:11.3.0`) -- Java source parsing and metamodel introspection
- **Eclipse EMF** -- Ecore metamodeling and XMI serialization
- **Epsilon ETL** (`2.8.0`) -- Model-to-model transformation engine
- **Epsilon EGL** (`2.8.0`) -- Model-to-text generation engine
- **JUnit 5** -- Testing

## Design decisions

### Why Spoon over MoDisco?

MoDisco's Java metamodel is frozen at JDK 5 (126 EClasses) and the project is effectively unmaintained. Spoon supports Java 20+, is actively maintained (400+ contributors), and exposes a self-description API that enables auto-generation of the Ecore metamodel.

### Why auto-generate `spoon.ecore`?

Spoon has ~127 metaclasses with ~900 properties. Hand-crafting an Ecore metamodel would be error-prone and need manual updates on every Spoon version bump. The `SpoonEcoreGenerator` produces a correct metamodel in seconds from Spoon's `Metamodel.getInstance()` API.

### Why CtRole-based mapping?

Spoon's getter names don't always match property names (e.g., the `leftOperand` property uses `getLeftHandOperand()`). The `CtRole` enum provides a stable mapping between property names and values via `element.getValueByRole(role)`.

### Why Epsilon EGL for CSP generation (not RoboTool)?

The official RoboChart-to-CSP generator is an Eclipse/OSGi plugin (`circus.robocalc.robochart.generator.csp`) written in Xtend. It lives in a private GitHub repository, has deep Eclipse dependencies, and offers no standalone CLI. Three alternative approaches were evaluated:

| Approach | Verdict |
|----------|---------|
| Xtext standalone setup (reuse official generator JARs) | Feasible but fragile -- requires reverse-engineering the dependency set from P2 update sites |
| Eclipse headless application | Complex -- requires bundling a significant portion of the Eclipse runtime |
| **Custom EGL templates** | **Chosen** -- stays in the Epsilon ecosystem already used for ETL, no new framework dependencies, tractable for flat state machines |

The EGL template follows the published tock-CSP semantics from Miyazawa et al. (SoSyM 2019) and the RoboChart Reference Manual. The `cspm-textual` Xtext grammar (from `UoY-RoboStar/cspm-textual`) and the `robocert-textual` source code (from `UoY-RoboStar/robocert-textual`) were used as references for correct CSP-M syntax and tock-CSP patterns.
