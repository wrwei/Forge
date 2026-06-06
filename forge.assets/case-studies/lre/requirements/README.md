# LRE requirements — ID prefix conventions

The LRE case study uses 51 structured requirements organised by
prefix. Every requirement carries the schema documented in CLAUDE.md
(`kind`, `name`, `id`, `description`, `priority`, `types`); the
prefix tells you which architectural layer the requirement belongs to.

## Prefix conventions

| Prefix      | Layer                          | Examples |
|-------------|--------------------------------|----------|
| `LRE-ARCH*` | System & controller architecture | `LRE-ARCH1` (system overview), `LRE-ARCH2` (controller composition) |
| `LRE-DM*`   | Data types                     | `LRE-DM1` (LreMode enum), `LRE-DM2` (Obstacle record) |
| `LRE-SF*`   | Sensor functions               | derived sensor quantities |
| `LRE-OP*`   | Operation classes              | per-step computation classes (CalcVel, CalcCStc, …) |
| `LRE-Var*`  | State variables                | shared variables read by guards (vel, hvel, vvel, cda, …) |
| `LRE-FR*`   | Functional requirements        | observable functional behaviour |
| `LRE-GP*`   | Guard predicates               | named boolean predicates in `step()` |
| `LRE-Beh*`  | Behavioural transitions        | transitions between modes (the actual state machine) |

## Files in this directory

- `requirement_all.json` — canonical source of truth, all 51
  requirements. **Read this** when implementing.
- `tier1_foundation.json` … `tier7_behavior.json` — the same
  requirements grouped by dependency tier. **Documentation only**;
  not consumed by the current top-down codegen workflow (see
  `forge.assets/vibe-coding-prompts/`).
- `requirement_all.txt` — human-readable text rendering of
  `requirement_all.json`.

## Adding a new prefix

If a new architectural layer is needed, add a row to the table above
and use the same `<PROJECT>-<LAYER>n` convention (uppercase project
slug + uppercase layer prefix + integer).

## For other case studies

This README is LRE-specific. Other case studies maintain their own
ID prefix conventions in their own `requirements/README.md`. The
`<PROJECT>-` prefix typically matches the case-study slug (so
e.g. a `chemical_detector` case study would use `CD-` prefixes).
