# Run 1 — transcript audit for forbidden reads

Source: `transcript.md` (Claude Code `/export` of the run-1 session, 856 lines,
33 tool calls with result markers). Audited 2026-10-25 by the operator's assistant.

Forbidden for the actor (prompt, "Do not read"): `forge.transformations/`,
`forge.dashboard/`, `docs/fixes|design|research|experiments`, `reference-runs/`,
`.remember`.

Findings: no tool call read, listed, searched or executed anything under a
forbidden path.
- 12 `Bash` calls: three runner/preflight invocations; a listing of
  `forge.assets/vibe-coding-prompts/`; `ls forge.assets/corrections/` with
  `javac -version`; creating the Java package directories; a throwaway test
  harness under the Claude scratchpad in `/private/tmp`; two `grep` passes and
  three `python3` scripts over the actor's own Java and reports; one `sed` on
  the scratchpad iteration log.
- 21 `Write` + 1 `Update`: the 18 Java files under
  `java.generated.project/src/main/java/chemical_detector/` and
  `forge.assets/corrections/ITER_SUMMARY.md`.
- 7 `Read` calls (the export collapses their arguments to "Read 1 file"):
  from the immediately surrounding narration, the requirements input and the
  prompts directory; the actor's own `ITER_SUMMARY.md` after each write; the
  runner's result after each iteration; and `post_isabelle_verify.md` after
  iteration 1 ("I'll read its feedback file and nothing else").
- The strings `forge.transformations/` and `forge.dashboard/` occur four
  times: in the prompt's prohibition (L86), in the actor declining to read
  `forge.transformations/output/` (L652), and in the operator's resume message
  (L694-695).

Limitation: because the export elides `Read` arguments, the seven read paths are
established from narration, not literal arguments. Nothing in the session's
shape contradicts them.
