# Steam Boiler run — pre-registration of expected outcomes

**Recorded:** 2026-09-03T03:45:51.390204+00:00 — BEFORE any Java
generation for the steam boiler. Companion to review/steamboiler-profile-freeze.json.

## Purpose
Fixing, in advance, which verifier outcomes a FAITHFUL implementation is expected
to produce, so that a failure matching this list is evidence of fidelity rather
than defect, and anything off this list is a genuine finding.

## Pre-registered expectations

1. **Deadlock-freedom (FDR4 + Isabelle deadlock_free lemma): expected to FAIL
   on a faithful implementation.** Abrial's emergency-stop mode is terminal —
   the program halts (SB-Beh24/25; mapping notes sect 4.5). A correct model of a
   halting controller deadlocks at the terminal state by design. Protocol: this
   specific failure, localized to the emergency-stop state, is recorded as
   EXPECTED-FAITHFUL; the convergence loop must not "repair" it by making
   emergency stop escapable — that repair would be a requirements violation
   (caught by SB-Beh24). Acceptable faithful encodings: terminal self-loop
   emitting nothing (making deadlock-freedom pass vacuously) IF AND ONLY IF the
   self-loop emits no output messages; otherwise report the deadlock as expected.
2. **Determinism checks: no expectation of failure.** The spec's mode dispatch
   is priority-ordered; ties are resolved by the ambiguity log's resolutions.
3. **All other obligations (invariant preservation, refinement, Dafny contracts):
   expected to PASS on a converged run**, as for the three admitted studies.
4. **Lint pressure predicted** (from sb_profile_check.md, recorded pre-run):
   rule6 (priority negations on long per-mode chains) and rule8 (batched
   per-cycle input vs single-event step) are the two rules most likely to fire
   during iteration. Firing is not failure; it is the linter doing its job.

## What would count as a generality failure (also fixed now)
- Profile rejection that no iteration can clear (construct outside the admitted
  class, e.g. the pump family proving unencodable under the frozen rules)
- Extraction producing structurally broken models on constructs from the
  8-item unexercised list (sb_profile_check.md)
- Non-convergence within the same iteration budget the three studies used
- Any verifier failure NOT on this pre-registered list that persists at convergence

## Provenance chain for the run
profile freeze (hashed) -> this pre-registration -> requirements mapping
(sb_requirement_all.*, sha256 below) -> generation (unchanged prompt bundle,
deviations logged) -> pipeline -> verifier battery.

sha256(sb_requirement_all.json) = 2e6290093c960e2142ea0dae025de1d9d550105dbcc46b6165ecef81cf1b63ab
sha256(sb_requirement_all.txt)  = f7d0672fefdf865ff5506a19280d3488fad121566247dfda45eb9c0429da7eea
