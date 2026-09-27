# Ambiguity log — LRE requirements compilation

Rule: every ambiguity resolved from prose alone, or marked UNDERDETERMINED and excluded from scoring. No implementation artefact was consulted.

## AMB-1 (LRE-Beh5 / LRE-Beh16) — RESOLVED

**Issue.** No trigger event named ('when inOpez is true').

**Resolution.** Interpreted as eventless, condition-triggered transitions evaluated each controller step (consistent with Var1: inOpez recalculated by CheckOPEZ each step). Resolved from prose alone.

## AMB-2 (All eventless MOM/HCM/CAM transitions) — RESOLVED

**Issue.** Beh prose states no priority or mutual exclusion among simultaneously-enabled transitions (e.g. in MOM both inOpez (Beh5) and cda<minSafeDist AND tcpa>=0 (Beh8) can hold).

**Resolution.** Requirements model is a transition RELATION: per (mode, event/step, valuation) it yields the set of enabled transitions. Conformance is judged per-transition enabledness, not on a deterministic successor. Resolved from prose alone (LRE-DC1 uniqueness is per (source, trigger) pair, which is satisfied).

## AMB-3 (LRE-Beh4) — RESOLVED

**Issue.** 'greater than 1' for odist(cdyn)/odist(cstc): literal 1 vs the minSafeDist threshold (default 1) — prose says the literal.

**Resolution.** Kept literal bound 1 exactly as prose states. Any extracted-side use of a named constant with value 1 is semantically equal under default parameters; flagged for the diff as bound-name difference only if numeric values diverge.

## AMB-4 (LRE-Beh2/Beh3 vs Beh4) — RESOLVED

**Issue.** In OCM with reqMOM and guards FALSE: does the LRE stay silently in OCM, or is there any output? Prose says nothing.

**Resolution.** Beh19 says OCM 'handles' reqMOM as 'transition to MOM if guards allow'; when guards do not allow, no transition and no output. Resolved from Beh19 prose.

## AMB-5 (LRE-Beh10 vs LRE-Beh13) — RESOLVED (modelled verbatim)

**Issue.** HCM entry via vdist<=staticObsDfltVertDist (Beh10) but HCM exit (Beh13) tests vdist>staticObsVertDist — a different named threshold. If the two constants differ, entry/exit are not complementary (possible chattering).

**Resolution.** Not an ambiguity to resolve: modelled exactly as prose states, with two distinct named bounds. Recorded as a prose fact relevant to the diff.

## AMB-6 (Atoms vdist_le_dfltvert vs vdist_le_vert) — RESOLVED

**Issue.** If staticObsVertDist == staticObsDfltVertDist (both default 1 — but defaults live in LRE-DM4, OUTSIDE the allowed scope), the two atoms are semantically identical and independent enumeration over them visits infeasible valuations.

**Resolution.** Atoms kept independent (bounds symbolic, per Beh prose). Enumeration is over abstract valuations; infeasibility under specific constant instantiations does not affect per-cell verdict comparison since both sides are evaluated over the same abstract valuation.

## AMB-7 (LRE-Beh2/Beh3 event parameters) — RESOLVED

**Issue.** reqVel/reqHdng carry real parameters v/h; abstract enumeration cannot range over reals.

**Resolution.** Outputs modelled symbolically as advVel(v)/advHdng(h) (identity pass-through). Parameter values are irrelevant to mode-transition verdicts.

## AMB-8 (Entry actions of MOM/HCM (advVel(1), advVel(0))) — RESOLVED (scope-limited)

**Issue.** Mode-entry outputs are stated in LRE-FR2/LRE-FR3, which are OUTSIDE the allowed scope (Beh*/Var* only). Beh4/Beh12 etc. state no outputs themselves.

**Resolution.** Outputs recorded only where Beh prose states them (Beh2, Beh3, Beh7, Beh18). Entry-action outputs are NOT part of the requirements model; output comparison is therefore limited to transition-local outputs. Recorded as scope limitation, not resolved by peeking.

