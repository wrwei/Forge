# Requirements-conformance prototype — LRE (R3-2 oracle)

## 1. Ordering attestation

The requirements model, prose→formula table, and ambiguity log were compiled
**before any implementation artefact was opened** in this session. The only
repository file read up to that point was
`forge.assets/case-studies/lre/requirements/requirement_all.json`.
Hashes were recorded at **2026-09-02T09:20:15.918661+00:00** (see
`ordering_attestation.txt`, written in the same kernel cell that produced the model):

```
sha256(requirements_model_lre.json) = d2491f4e6ce75f4b2b0bfafae99664b589948fa5abf46a2a3d5bce94db8d7a4b
sha256(prose_formula_table.md)      = 61c635f75c45a69d24bde02d41ffbf0e794d6ea9ce0038cb740df29f4429d932
sha256(ambiguity_log.md)            = 9575486033cb28eac63b556cddd66cdf8ad148e569902df1563d84f353b566b6
combined                            = 78d7006508342441f83df3fc9db6b7d56ea95108d6c34bab8daa79486fda0aee
```

The first implementation read (the regenerated theory) occurred strictly after
this cell in the transcript, which is the ordering proof.

## 2. Scope and translation defensibility

Compiled from **LRE-Beh1..Beh19** (behavioural prose) plus the variable
declarations LRE-Var1..Var8. **LRE-GP\* entries were hard-excluded** — the
repo's own guide calls the baseline requirements 'extremely prescriptive'
*for the GP entries*, which contain literal Java guard code; the Beh entries
are prose with thresholds, which is the defensible middle for this exercise.
LRE-DM4 (constant *values*) is also outside scope, so named thresholds stay
symbolic in the requirements model and are bound to the implementation's
declared constants only at diff time.

18 transitions + 1 closure constraint (Beh19) compiled; 11 atomic guard
predicates; **8 ambiguities logged, all resolved from prose alone;
0 conjuncts UNDERDETERMINED** (none excluded from scoring).

## 3. Conformance diff — extracted model vs requirements model

Extracted side: the current regenerated theory
(`/tmp/i1int/lre/output/isabelle/LreController_Beh.thy`, fixed extractor,
regenerated Sep 1 with the standard harness), parsed exactly as the Tier B
analysis parses it (same `parse_thy.py`).

**Abstract enumeration** (the Tier B method, over the requirements-model
vocabulary): 2^11 valuations x 4 modes x 7 events (6 operator events + tick) =
**57,344 cells** (`conformance_diff.csv`). Per-cell comparison of the
requirements-enabled transition set vs the theory-enabled operation set:

| verdict | cells |
|---|---|
| agree_exact (same nonempty target set) | 24,320 |
| agree_stay (both: no transition) | 20,576 |
| agree_refinement (theory set is a nonempty subset of the req set) | 12,448 |
| REQ-says-transition-CODE-doesn't | **0** |
| CODE-says-REQ-doesn't | **0** |
| guard-boundary (comparator/bound mismatch) | **0** |
| UNDERDETERMINED-excluded | **0** |

Every refinement cell (12,448) has **multiple requirements transitions
enabled** (the Beh prose states no priority); the extracted model resolves
that nondeterminism with the priority chain. Zero cells have the theory
enabled where the requirements forbid, or vice versa. Transition-local
outputs (Beh2/3/7/18) also check out (0 violations).

Guard boundaries were additionally checked concretely: a numeric grid over
{0.5,1,1.5,2,2.5}^8 x tcpa {-0.5,0,0.5} x inOpez (2,343,750 cells per
mode-event pair) straddles every threshold including equality points; the
baseline Java relation, the theory relation, and the requirements relation
agree on all of it (0 disagreement cells). This also proves baseline-Java ≡
current-theory relation, which licenses the mutation-scoring method below.

**Headline: the extracted model conforms to the behavioural requirements —
100% of the enumeration, with 21.7% of cells conforming via
priority-refinement of requirements nondeterminism.** The value of the
oracle is therefore in what it catches when the relation is *wrong*, which
the survivor scoring measures.

## 4. Survivor scoring

Survivor set: `kill_table_all_backends.csv` (the campaign's definitive
table) has **155 mutants killed by no backend (Dafny+FDR4+Isabelle), of
which 62 are LRE**. (The task brief said 56; no filter of the definitive
table produces 56 — 56 is the branch_swap class population. All 62 LRE
survivors were scored; only LRE mutants are scoreable at all, since the
requirements model exists only for LRE — chemical_detector (71) and
sranger (22) survivors are out of scope by construction.)

Method: for each LRE survivor, the mutated relation was obtained by
re-simulating the mutant's `step()` semantics (campaign evidence tree,
`campaign/mutants/<id>/java`) on the concrete grid — licensed by the proven
baseline Java≡theory equivalence — and diffed against the requirements
relation. A kill = at least one cell where the mutated relation disagrees
(target set or mandatory transition-local output) where the baseline agreed.

| class | LRE survivors | scoreable | killed | kill rate (scoreable) |
|---|---|---|---|---|
| relop_flip | 10 | 9 | 9 | 100% |
| trigger_swap | 8 | 6 | 6 | 100% |
| cmp_swap | 10 | 3 | 3 | 100% |
| drop_negation | 2 | 1 | 1 | 100% |
| off_by_one | 10 | 5 | 4 | 80% |
| branch_swap | 14 | 13 | 0 | 0% |
| sentinel_init | 8 | 0 | — | — |
| **total** | **62** | **37** | **23** | **62.2%** |

**23/62 = 37.1% of all LRE survivors; 23/37 = 62.2% of scoreable ones.**

### The 14 scoreable survivors the oracle still misses, and why

- **lre_branch_swap_001** (branch_swap, line 199): EQUIVALENT-UNDER-RELATION: swap of whole branches whose guards are mutually exclusive or whose overlap maps to the same target; transition relation identical to baseline (verified by exhaustive grid), so no oracle can kill without observing outputs under multi-enabled req transitions (Beh17/Beh18 share target OCM)
- **lre_branch_swap_002** (branch_swap, line 222): EQUIVALENT-UNDER-RELATION: swap of whole branches whose guards are mutually exclusive or whose overlap maps to the same target; transition relation identical to baseline (verified by exhaustive grid), so no oracle can kill without observing outputs under multi-enabled req transitions (Beh17/Beh18 share target OCM)
- **lre_branch_swap_003** (branch_swap, line 253): EQUIVALENT-UNDER-RELATION: swap of whole branches whose guards are mutually exclusive or whose overlap maps to the same target; transition relation identical to baseline (verified by exhaustive grid), so no oracle can kill without observing outputs under multi-enabled req transitions (Beh17/Beh18 share target OCM)
- **lre_branch_swap_004** (branch_swap, line 201): EQUIVALENT-UNDER-RELATION: swap of whole branches whose guards are mutually exclusive or whose overlap maps to the same target; transition relation identical to baseline (verified by exhaustive grid), so no oracle can kill without observing outputs under multi-enabled req transitions (Beh17/Beh18 share target OCM)
- **lre_branch_swap_005** (branch_swap, line 207): EQUIVALENT-UNDER-RELATION: swap of whole branches whose guards are mutually exclusive or whose overlap maps to the same target; transition relation identical to baseline (verified by exhaustive grid), so no oracle can kill without observing outputs under multi-enabled req transitions (Beh17/Beh18 share target OCM)
- **lre_branch_swap_006** (branch_swap, line 224): EQUIVALENT-UNDER-RELATION: swap of whole branches whose guards are mutually exclusive or whose overlap maps to the same target; transition relation identical to baseline (verified by exhaustive grid), so no oracle can kill without observing outputs under multi-enabled req transitions (Beh17/Beh18 share target OCM)
- **lre_branch_swap_007** (branch_swap, line 230): EQUIVALENT-UNDER-RELATION: swap of whole branches whose guards are mutually exclusive or whose overlap maps to the same target; transition relation identical to baseline (verified by exhaustive grid), so no oracle can kill without observing outputs under multi-enabled req transitions (Beh17/Beh18 share target OCM)
- **lre_branch_swap_008** (branch_swap, line 235): EQUIVALENT-UNDER-RELATION: swap of whole branches whose guards are mutually exclusive or whose overlap maps to the same target; transition relation identical to baseline (verified by exhaustive grid), so no oracle can kill without observing outputs under multi-enabled req transitions (Beh17/Beh18 share target OCM)
- **lre_branch_swap_009** (branch_swap, line 239): EQUIVALENT-UNDER-RELATION: swap of whole branches whose guards are mutually exclusive or whose overlap maps to the same target; transition relation identical to baseline (verified by exhaustive grid), so no oracle can kill without observing outputs under multi-enabled req transitions (Beh17/Beh18 share target OCM)
- **lre_branch_swap_010** (branch_swap, line 243): EQUIVALENT-UNDER-RELATION: swap of whole branches whose guards are mutually exclusive or whose overlap maps to the same target; transition relation identical to baseline (verified by exhaustive grid), so no oracle can kill without observing outputs under multi-enabled req transitions (Beh17/Beh18 share target OCM)
- **lre_branch_swap_011** (branch_swap, line 255): EQUIVALENT-UNDER-RELATION: swap of whole branches whose guards are mutually exclusive or whose overlap maps to the same target; transition relation identical to baseline (verified by exhaustive grid), so no oracle can kill without observing outputs under multi-enabled req transitions (Beh17/Beh18 share target OCM)
- **lre_branch_swap_012** (branch_swap, line 259): EQUIVALENT-UNDER-RELATION: swap of whole branches whose guards are mutually exclusive or whose overlap maps to the same target; transition relation identical to baseline (verified by exhaustive grid), so no oracle can kill without observing outputs under multi-enabled req transitions (Beh17/Beh18 share target OCM)
- **lre_branch_swap_014** (branch_swap, line 274): PERMITTED-BY-REQ-NONDETERMINISM: swaps priority of CAM's two exits (Beh17 reqOCM, no output vs Beh18 cda>=minSafeDist, advVel(0)). Where both req transitions are enabled they share target OCM and the Beh prose states no priority, so either output behaviour conforms; relation differs from baseline only in output choice inside the req-nondeterministic region (verified: 0 target diffs, output diff cells only)
- **lre_off_by_one_005** (off_by_one, line 180): EQUIVALENT-UNDER-DEFAULTS: strengthens vvel>=1.0 to >=2.0 inside hcmActive's third disjunct, but with staticObsVertDist == staticObsDfltVertDist (=1.0) the vdist<=sv conjunct implies the unconditional vdist<=sdv disjunct (Beh10 subsumes Beh11); relation identical to baseline on the grid

The 13 branch_swap misses were **verified equivalent**: each swaps adjacent
guarded branches whose guards are mutually exclusive by construction (the
explicit `!camActive`/`!hcmActive` negations added for Dafny), so the
induced relation is bit-identical to baseline on the full grid — no
behavioural oracle can kill them. They are equivalent mutants, not oracle
failures. `lre_branch_swap_014` differs only inside a region where the
requirements themselves are nondeterministic (Beh17 vs Beh18, same target),
so either behaviour conforms. `lre_off_by_one_005` is equivalent under the
default constants (Beh10's unconditional vdist disjunct subsumes Beh11's).

### Unscoreable (25): why

- 8 sentinel_init + 3 constructor inits: initial *values* are not in the Beh
  transition relation (Beh1 fixes only the initial mode).
- 4 constants/LreConstants.java: threshold value changes; values live in
  LRE-DM4 (excluded scope), thresholds bind symbolically on both sides.
- 10 sensor/operation/actuator internals (incl. CheckOPEZ line 32): below
  the guard abstraction — odist/hdist/vdist/closest*Index are uninterpreted
  functions and inOpez is an abstract variable in both the requirements
  model and the extracted relation.

### Pipeline-path caveat

Of the 23 kills, **16 are visible in the pre-fix extractor's `.thy`**
(the campaign's extractor); the other 7 (6 relop_flip + 1 off_by_one, all in
the camActive/hcmActive/momReturn definition lines 176–183) are invisible to
the pre-fix extraction and reach the extracted relation only via the FIXED
extractor, which emits the flag-definition update formulas (the current
regenerated theory contains them; direct re-extraction per mutant was not
re-run here). So as a pipeline phase: 16 kills with the shipped extractor,
23 with the fixed one.

## 5. Limitations (for the paper)

1. The prose→formula translation was performed by the same agent that later
   ran the diff — not a blind human translator. The ordering attestation
   proves the translation *preceded sight of the implementation*, but not
   independence of authorship. A referee-proof version needs an independent
   translator or the published table (provided) for inspection.
2. LRE only. The other two case studies have no requirements documents of
   comparable structure in the repo; per-class rates here (n≤13) are small.
3. The requirements document itself was written to describe this system
   (same project); it is prose, but not adversarially independent.
4. Output conformance is checked only for transition-local outputs stated in
   Beh prose (Beh2/3/7/18); mode-entry actions (advVel on entering MOM/HCM)
   are stated in LRE-FR2/FR3, outside the Beh scope, and were not scored.
5. Mutation scoring re-simulates mutated Java semantics rather than
   re-running the extractor per mutant (licensed by the proven baseline
   equivalence, but extraction-stage defects on mutants are out of frame;
   the pre-fix/fixed split above bounds this).
6. Equivalence/kill verdicts rest on the concrete grid straddling every
   threshold; grids can in principle miss regions, though here every guard
   is a conjunction of single-variable threshold comparisons, for which the
   grid is exhaustive per sign pattern.

## 6. Verdict on R3-2

The oracle is material: it kills **23 of the 62 LRE all-backend survivors
(37%)**, including **all 9 scoreable relop_flips** — the class the campaign
flagged as surviving every backend — and every scoreable trigger_swap. The
survivors it cannot kill are (verified) equivalent mutants, below-abstraction
mutations, or initial-value/constant-value changes that no transition-relation
oracle over the Beh scope can see. This supports adding a
requirements-conformance phase; wiring into pipeline.yaml is a separate
decision as instructed.
