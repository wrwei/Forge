You are doing **Layer 4** of the top-down codegen sequence
documented in `forge.assets/vibe-coding-prompts/README.md`.

# Layer 4 — Controller (mode-nested if-else `step()`)

Implement the controller's `step()` method using the single-method,
mode-nested if-else pattern. The reference shape is in
`forge.assets/prompts/few_shot_codegen.txt` — read it before
starting.

Required structure:

1. One public method: `void step(InputEvent event)`.
2. Track current mode as an enum field; assign it directly inside
   each branch.
3. **Named boolean predicates declared BEFORE the if-else chain.**
   Every guard condition gets a named variable. Each predicate RHS
   is a simple comparison or boolean expression — no ternary, no
   sentinel checks (the sensor layer already returns safe defaults).
4. Pure two-level if-else:
   - **Outer**: exactly one block per mode (`currentMode == X`).
     Never `currentMode != X`.
   - **Inner**: transitions ordered by priority. High-priority
     transitions (operator overrides, emergency stops) duplicated as
     the first inner branches in each applicable mode block.
5. Use traditional `instanceof` + a separate cast line — never
   pattern-matching `instanceof`.
6. Entry actions inline at the moment the mode is assigned.

Cross-check against `forge.assets/prompts/chain_of_thought_codegen.txt`
phase 3 (steps 12-16) before declaring this layer done.

## Wrap-up

Follow `forge.assets/prompts/codegen_trace_rules.txt` to refresh
`java.generated.project/result_codegen.json` — the requirement → Java
traceability map that the dashboard's Coverage phase consumes.

## Stop condition

Project compiles; `step()` implemented end-to-end; all transitions
from the requirements are covered; `result_codegen.json` regenerated.
Wait for me to review before Layer 5.
