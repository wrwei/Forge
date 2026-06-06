You are doing **Layer 1** of the top-down codegen sequence
documented in `forge.assets/vibe-coding-prompts/README.md`.

# Layer 1 — Architecture sketch (no code)

Read `CLAUDE.md` and
`forge.assets/case-studies/<study>/requirements/requirement_all.json`
(replace `<study>` with the actual case-study slug — e.g. `lre`).

Without writing any Java code, design the package and class structure
for this system. Produce a markdown response listing:

- The packages and the responsibility of each.
- The classes per package, with each class's public surface (method
  names + parameter/return types, public fields, sealed hierarchy
  members).
- Class relationships: composition, inheritance, sealed `permits`,
  whose constructor takes whom.
- For every class, which requirement IDs that class serves.

For each non-obvious design choice, justify it briefly against the
requirements. Flag any requirement you cannot place into the
architecture — that signal is more valuable than a fabricated home.

## Stop condition

The design is laid out as text. Do **not** generate any `.java`
files. Wait for me to confirm the architecture (or ask you to
revise it) before proceeding to Layer 2.
