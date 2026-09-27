# LRE run 5 — iteration 3

Iteration 2 took the run from 6 passing phases to 11 of 12. The one remaining
failure was Isabelle: `exit 142 ... *** Timeout` on `LreController_deadlock_free`
(`LreController_Beh.thy:549`), 629s.

## Diagnosis

My iteration-2 edit caused it. To satisfy preflight rule 6 I had given *every*
branch in a mode the negation of all preceding triggerless guards, including the
event-triggered ones. In MOM that made eight transitions whose preconditions
carry up to five negated conjuncts over real-arithmetic atoms. The deadlock
disjunction is valid by construction — the chain partitions the state space —
but proving it that way is a propositional blow-up over arithmetic atoms, and
the tactic ran out of time.

## Change

One edit, in `LreController.step()`: the unconditional operator-request branches
(reqOCM in MOM/HCM/CAM, endTask and reqHCM in MOM) no longer carry the negation
conjuncts. They come last, after the triggerless branches, so each mode again has
a transition enabled on its mode alone and the deadlock goal has a trivially true
disjunct. The triggerless branches keep their negations, so rule 8 stays clean
and the autonomous transitions stay mutually exclusive.

## The trade-off, deliberately taken

This reintroduces preflight rule 6 warnings on those event branches. Rule 6's
stated consequence is that the extracted preconditions are not mutually
exclusive, i.e. nondeterminism — which this pipeline explicitly does not verify
(`:[deterministic]` assertions are stripped before FDR). Rule 6 is a warning, not
an error, so preflight should still pass. Against that, the change restores the
requirements' own reading of LRE-Beh6/Beh7/Beh12/Beh15, which state the operator
transitions unconditionally.

In the Java the operator requests are still preempted by an active safety guard,
because rule 8 forbids a triggerless branch following an event-triggered one
("RoboChart has no event-absence guard"). That divergence between the Java's
priority and the model's is not removable; it is worth a human decision.
