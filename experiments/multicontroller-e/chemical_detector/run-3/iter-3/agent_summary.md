# Iteration 3 — chemical_detector run 3 (condition E)

**Iteration 2 feedback:**
- **M2T failed.** The generated model referenced `makingProgress` without declaring it. Only the two guard references appear, so the extractor dropped the assignment, which contained a clock read. Clock reads inside assignments are an extraction limit.
- **FDR4 was skipped** because M2T failed.
- **Isabelle was killed at 843 s.** Movement's proof still did not finish with propositional guards, so arithmetic in the guards was not the cause.

**Diagnosis:** CLAUDE.md says a hang means an unprovable residual. The gas analyser, which passes, gets every state's enabledness from its boundary input `gas` or from autonomous transitions whose guards cover every case. In Movement, `Waiting`, `Avoiding`, `GettingOut` and `Found` are enabled only by `turn`/`stop`/`resume`, the inter-controller events. Iteration 1's Isabelle run was the first time those reached the prover in a per-controller theory. The likeliest cause is that those states have no provable enabled operation.

**Changed (Movement, `InputEvent`):**
1. **New boundary input.** `InputEvent.tick` is a payload-free platform heartbeat. Every movement mode except `AvoidingAgain` accepts it as a lowest-priority self-loop with no action. `AvoidingAgain` already has complementary autonomous guards. Java behaviour is unchanged: modes already ignored inputs they did not handle.
2. **Reverted guards.** `AvoidingAgain` goes back to iteration 1's complementary clock/distance predicates, which extracted cleanly and passed FDR4. `makingProgress` is removed.
3. **`Found`'s self-loop.** `Found`'s outgoing transition is now the `tick` self-loop; the `stop` self-loop is gone.

The trace (`tick` → CD-ARCH1, the continuous sense-act loop) and `post_codegen` are refreshed.

**Spec deviation:** `tick` is invented. It is flagged in `post_codegen` for review.

**For the operator:** iteration 2's Isabelle feedback suggested extending the feedback classifier in `forge.dashboard`. That is instrument maintenance, not a Java fix; not acted on.
