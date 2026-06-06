# Top-down codegen — five-layer prompt sequence

A reusable conversational pacing for Phase 2 (interactive Java code
generation). The point is to make the *architecture* decision first —
while it's still cheap to redirect — then fill leaves to satisfy the
architecture. Bottom-up generation works, but inverts the importance
of the decisions you're making (you only get to the controller after
dozens of small choices in tiers 1-6 have already constrained you).

This directory is **prompt source**, not skill source. Each layer
lives in its own file so you can copy or reference one at a time
without scrolling through a long doc.

## How to use

For each round, either:

- **Copy-paste**: open the layer file, copy the entire content,
  paste into chat. The file IS the prompt.
- **Reference**: tell Claude "read `forge.assets/vibe-coding-prompts/03_leaves.md`
  and execute that layer." Same effect, less copying.

Each layer halts and waits for your review before the next. After
Layer 5 you run the dashboard pipeline (compile → coverage →
preflight → T2M → M2M → M2T → verifiers) and feed any failures back
via `/fix-from-feedback`.

## The layers

| File | Goal | Output |
|---|---|---|
| [01_architecture.md](01_architecture.md) | Architecture sketch | Markdown response describing packages, classes, public surfaces, requirement-to-class mapping. **No code.** |
| [02_skeleton.md](02_skeleton.md) | Compilable skeleton | Java records, enums, sealed events, classes with empty-body public methods. Project compiles. |
| [03_leaves.md](03_leaves.md) | Sensor + actuator + operations | Leaf logic: sensor derived quantities, actuator setters, operation `compute()` methods. Controller `step()` still empty. |
| [04_controller.md](04_controller.md) | Controller `step()` | Mode-nested if-else with named predicates. All transitions from the requirements covered. |
| [05_review.md](05_review.md) | Self-review | Writes `forge.assets/corrections/post_codegen.{md,json}` flagging any uncertainty (invented defaults, design choices, ambiguous requirements, unimplemented items). |

## Notes

- **Don't skip layers.** Each layer's stop condition is the user's
  cue to review. Skipping breaks the review pacing.
- **Layer 1 is non-negotiable.** If the architecture is wrong, every
  later layer compounds the mistake. Treat the user's confirmation
  of Layer 1 as a gate.
- **You can re-enter at any layer.** If `/fix-from-feedback` says the
  controller is broken, paste `04_controller.md` again with the
  failure context — no need to start over at Layer 1.
- **The prompts assume `requirement_all.json` is the source of
  truth.** The tier files (`tier1.md`...`tier7.md`) are documentation
  of how the requirements decompose by dependency; they are not
  consumed by this top-down sequence.
- **Pure natural language outputs.** No machine-readable artifacts
  at the design stage — this matches the vibe-coding spirit.
