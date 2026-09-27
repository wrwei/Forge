# FORGE convergence experiment — iteration 1 (cold codegen)

You are doing **iteration 1** of the FORGE convergence experiment for the
**{study}** case study. This is the cold-codegen step: no prior feedback
exists yet. You must produce the initial Java source from the specification
alone.

## What to read

**Required inputs:**

1. `forge.assets/case-studies/{study}/system/system_description.txt` — natural-language description of the system
2. `forge.assets/case-studies/{study}/requirements/requirement_all.json` — structured requirements specification
3. `CLAUDE.md` — project-level codegen rules and conventions (at the repo root)
4. `forge.assets/prompts/java_codegen_rules.txt` — the full Java rule set the M2M depends on
5. `forge.assets/prompts/chain_of_thought_codegen.txt` — the step-by-step reasoning guide
6. `forge.assets/prompts/few_shot_codegen.txt` — a worked example of the required code shape

**Do NOT read** (these would leak the converged structure and invalidate the cold run):

- `experiments/cold-baseline/{study}/run-*/` — prior cold-baseline outputs for this study
- `experiments/convergence/{study}/` — any prior iter's output, including iters 1..N-1 if this is a retry
- The repo-root `reference-runs/{study}/java/` — the paper-companion converged source (the "answer"; the experiment must not reach for it)
- The git history of any of the above paths

## What to do

1. Execute the five-layer prompt sequence (Architecture → Skeleton → Leaves → Controller → Review) in order. Do not halt for review between layers — proceed straight through.
2. Write the resulting Java source tree to `java.generated.project/src/main/java/{study}/<sub-package>/...`. The target package is `{study}`. Subpackages follow CLAUDE.md conventions (`actuator`, `annotation`, `constants`, `controller`, `event`, `mode`, `operation`, `sensor`, plus any others your architecture layer decides on).
3. Wipe any existing files under `java.generated.project/src/main/java/{study}/` before writing your output, so the cold codegen result is exactly what your reasoning produced (not a merge with prior state).

## What to output

Write the Java files via the Edit/Write tools. **Do not output Java in the chat.** End your response with a single line `CODEGEN_COMPLETE: <N> files written` and nothing else.

## Constraints (most important)

- Follow `java_codegen_rules.txt` rigidly. The M2M extracts a formal model from this Java; violations break the pipeline.
- Single-method, mode-nested if-else controller; named boolean predicates declared before the if-else chain.
- No lambdas, streams, method references, ternary operators, pattern-matching instanceof, switch expressions over patterns.
- `compute()` methods in Operation classes: direct `this.field = expr` assignments only, no locals.
- Use Java 17 records for value types, sealed interfaces for event hierarchies, `var` for locals.
- `@RoboChartType("nat")` / `@RoboChartType("real")` on fields/parameters where Java's primitive type doesn't directly convey the RoboChart type.
- Reserved-word collisions to avoid: don't name fields `clock`, `event`, `state`, `wait`, etc. (see `java_codegen_rules.txt` for the full list).
