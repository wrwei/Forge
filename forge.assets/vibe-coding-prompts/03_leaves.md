You are doing **Layer 3** of the top-down codegen sequence
documented in `forge.assets/vibe-coding-prompts/README.md`.

# Layer 3 — Sensor, actuator, and operation classes

Using the skeleton from Layer 2, implement the leaf logic:

- **Sensor** class: derived quantities as pure methods or cached
  fields updated on each cycle. Where a requirement does not say what
  a quantity is when no reading is available, choose a value of the
  declared type within the quantity's range (an integer is never
  negative; `null` is not a value), and list the choice as an
  `invented_default` in Layer 5.
- **Actuator** class: setters for the controller's output commands;
  track last-issued values for inspection.
- **One `Operation` class per requirement of `types: ["operation"]`**,
  in an `operation` subpackage. Each class:
  - Takes `Sensor` (and constants if needed) as constructor parameters.
  - Has a single public `void compute()` method.
  - Stores results in private fields exposed via getters whose names
    match the state-variable names from the requirements.
  - The `compute()` body uses **only** direct `this.field = expression`
    assignments. No local intermediate variables. Each RHS is a single
    expression using sensor calls, `Math.sqrt`, arithmetic, boolean
    operators, comparisons, and previously-assigned fields of the
    same class.

## Wrap-up

Follow `forge.assets/prompts/codegen_trace_rules.txt` to refresh
`java.generated.project/result_codegen.json` — the requirement → Java
traceability map that the dashboard's Coverage phase consumes.

## Stop condition

Project compiles; sensor / actuator / operations all implemented;
controller still has empty `step()`; `result_codegen.json` regenerated.
Wait for me to review before Layer 4.
