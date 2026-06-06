You are doing **Layer 5** of the top-down codegen sequence
documented in `forge.assets/vibe-coding-prompts/README.md`.

# Layer 5 — Self-review and `post_codegen.md`

Walk through the requirements file one more time. For each
requirement ID, identify the Java element(s) implementing it. Then
write `forge.assets/corrections/post_codegen.md` (and its mirror
`post_codegen.json`) using the schema in CLAUDE.md's "Corrections
Workflow" section.

Surface every requirement where you:

- Had to invent a default the requirement didn't specify
  (`kind: "invented_default"`).
- Made a design choice between equally valid options
  (`kind: "design_choice"`).
- Found ambiguity in the requirement
  (`kind: "ambiguous_requirement"`).
- Generated something whose scope you're unsure about
  (`kind: "scope_question"`).
- Could not place the requirement into the implementation
  (`kind: "unimplemented"`).

If everything is clean, write `status: "complete"` with `issues: []`.
If there's anything to flag, write `status: "uncertain"` and populate
the issue list. Each issue carries the same fields used by other
phases (`title`, `raw`, `fix_directive`,
`java_trace[].requirement_ids`), so `/fix-from-feedback` consumes
it without protocol changes.

This is the last codegen layer. Hand control back to me; I'll run
the dashboard pipeline (compile → coverage → preflight → T2M → M2M →
M2T → verifiers) and feed any failures back via `/fix-from-feedback`.

## Wrap-up

Follow `forge.assets/prompts/codegen_trace_rules.txt` to refresh
`java.generated.project/result_codegen.json` one more time so the
trace reflects any tweaks the review surfaced. The dashboard's
Coverage phase consumes it.

## Stop condition

`post_codegen.{md,json}` written, `result_codegen.json` regenerated.
Hand back.
