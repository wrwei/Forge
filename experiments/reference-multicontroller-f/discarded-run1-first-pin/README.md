# Condition F, run 1 at pin 6b78ebe8 — discarded (instrument defect)

Date: 2026-10-25. Actor: Claude Code, autonomous, cold start. Outcome: **no verdict**.

The run reached iteration 1. Every Gradle-backed phase (preflight, T2M, M2M, CSP,
Dafny and Isabelle generation) failed at once with `PermissionError: Permission
denied: forge.transformations/gradlew`; the verifiers and the vacuity audit then
had no input. The runner recorded 2 phases passed (compile, coverage), 2 failed,
8 without verdict. The actor stopped after iteration 1 without editing the
instrument and reported the cause (`actor-final-message.md`).

Cause: the pin copied the pipeline trees from the published Forge repository,
which has stored `forge.transformations/gradlew` without the executable bit
(mode 100644) since June 2026; the copy preserved that mode. The four pin checks
did not catch it because check 3 ran the phases with `java -cp` directly.

Per the pre-registration's stop rule the collection was stopped. The mode was
corrected in Forge and on the research branch, the condition was re-pinned
(see the amendment in `../prereg-reference-multicontroller-f.md`), and run 1
was started again cold under the new pin. Nothing in this directory contributes
to any reported figure; it is kept as the record of the defect.

Contents: the actor's iteration-1 reports (`corrections/`), its requirement map
(`result_codegen.json`), the staging record (`staging/iter-1/`), `actor.json`,
`setup_ok.json`, and the actor's final message.
