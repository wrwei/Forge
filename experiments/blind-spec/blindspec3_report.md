# Round-3 blind LRE reference spec — conformance of the shipped system and scoring of the 62 survivors

Inputs: `LRE-reference-spec.md` (artifact vfa5c89c9, drafted 2026-09-08,
READ-ONLY, never modified); shipped Java `reference-runs/lre/java` (read-only);
the 62-row survivor population and the archived compiled oracle's per-mutant
verdicts from `archived_survivor_scoring.csv` (artifact v8cbaf1f7, 23/62
killed); the round-2 spec's verdicts from `blindspec2_survivor_scoring.csv`
(internal comparison only). Method: the spec's SET-VALUED relation
`R(m,e,x) = {(destination(t), outputs(t)) : t ∈ E(m,e,x)}` with explicit hold
row, implemented directly from the document and validated against 11
boundary/overlap consequences from the spec's own tables before any comparison
(all pass, including the CAM/reqOCM two-member set, the MOM triple overlap, the
tcpa<0 CAM-stuck case, and the D>V alternation). The Java `step()` chain and
every mutant's chain parsed statically (brace-matching + guard ASTs) — no Java
executed; data-plane edits re-simulated with Java `double` semantics
(`Double.MAX_VALUE` sentinels, 0/0 = NaN, all-comparisons-false on NaN).
Machinery reused from the round-2 run (same parser, same enumeration scaffold);
the relation, kill rule, and attribution were re-implemented for this spec's
set-valued, no-priority semantics.

## 1. Conformance of the shipped system

**Membership semantics.** Per the spec, a single-step observation conforms
exactly when its `(destination, output sequence)` is a MEMBER of `R(m,e,x)`;
a fixed-priority implementation selects a subset of alternatives and is
expected to REFINE R. Kill/violation = outcome NOT IN R.

**Abstract enumeration** (2^11 atom valuations × 4 modes × 7 events = 57,344
cells; R computed as the full enabled-row set per cell):

| | cells | share |
|---|---|---|
| Java outcome ∈ R(m,e,x) | **57,344** | **100.00%** |
| violations | 0 | 0% |

R's structure over these cells: 20,576 hold cells (E empty; Java falls off the
chain and holds — the unique allowed outcome), 20,968 singleton cells, 15,800
multi-row cells (Java's priority chain picks one member). On the realizable
subspace (14,112 cells) likewise 100%, 0 violations. **The shipped controller
refines the round-3 spec's relation exactly: membership-agreement 100%, with
no priority substitution needed** — the round-2 spec's 16.96% A03
(priority-order) disagreement class is eliminated by this spec's explicitly
nondeterministic A02 resolution.

**Concrete grid** (8,928 sensor scenarios × 28 mode-event pairs = 249,984
cells; quantities derived independently on each side — spec state/observation
conventions incl. the F sentinel and A05 smallest-id tie rule vs Java
data-plane semantics; grid extended with two equidistant-obstacle tie
configurations for A05):

| verdict | cells |
|---|---|
| Java outcome ∈ R | 228,576 (91.44%) |
| non-member, ledger **A07** (obstacle-absence CPA: spec `cda=F, tcpa=0` vs Java `MAX_VALUE` arithmetic / 0/0=NaN) | 17,496 |
| non-member, ledger **A08/A14** (zero-relative-velocity CPA for an existing dynamic obstacle: outside the spec's declared input domain; Java yields NaN) | 3,912 |
| non-member, **unambiguous** | **0** |

Every one of the 21,408 non-member cells is a NaN cell of the Java CPA
pipeline (all Java comparisons false ⇒ chain falls through to hold where R
requires a transition, or misses T18's release). Substituting the Java CPA
conventions into the spec's atom derivation (the disclosed-alternative test)
leaves exactly the NaN cells and 0 others; every residual is within the
ledger's A07/A08/A14 scope, which the spec itself marks as
convention-dependent ("A raw-sensor-level checker must separately identify its
CPA convention; this reference does not independently validate that
convention"). **There are no unambiguous disagreements between the shipped
system and the round-3 spec, and hence no paper-grade defect findings against
the shipped controller.** T01 startup also conforms (Java initializes
`currentMode = LreMode.OCM`, LreController.java:70, constructor emits nothing).

## 2. Survivor scoring (62 all-backend survivors)

Population: exactly the 62 `mutant_id` rows of `archived_survivor_scoring.csv`
(verified: the 62 mutant source trees each differ from the baseline in exactly
the manifest-named file). Kill rule for a set-valued R: the mutant's
`(dest, outs)` is NOT a member of `R(m,e,x)` at a concrete cell where the
baseline's outcome IS a member. Attribution: a kill cell is *ledger-attributed*
when the deciding difference is a ledger choice (A07/A08/A14 CPA-edge cells,
incl. all NaN cells, and A12 threshold binding); *unambiguous* when the mutant's
outcome is outside every enabled row and the hold case under both the spec's own
and the Java-convention-substituted atom derivations, on a non-NaN cell.

| class | n | spec3 kills | unambiguous | ledger-attr | archived oracle | both | spec3-only | oracle-only |
|---|---|---|---|---|---|---|---|---|
| relop_flip | 10 | 10 | 10 | 0 | 9 | 9 | 1 | 0 |
| cmp_swap | 10 | 10 | 10 | 0 | 3 | 3 | 7 | 0 |
| trigger_swap | 8 | 6 | 6 | 0 | 6 | 6 | 0 | 0 |
| off_by_one | 10 | 5 | 5 | 0 | 4 | 4 | 1 | 0 |
| branch_swap | 14 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
| drop_negation | 2 | 2 | 2 | 0 | 1 | 1 | 1 | 0 |
| sentinel_init | 8 | 2 | 2 | 0 | 0 | 0 | 2 | 0 |
| **total** | **62** | **35** | **35** | **0** | **23** | **23** | **12** | **0** |

**Every kill is unambiguous** — each killed mutant has at least one non-NaN
concrete cell where its outcome lies outside the entire requirements-eligible
set (all enabled rows + hold) under BOTH atom-derivation conventions, while the
baseline's outcome is a member. Zero kills depend on a ledger choice.

**Complementarity vs the archived compiled oracle (15/62 — paper-facing):**
union **35**, intersection **15**, spec3-only **20**, oracle-only **0** — the
round-3 spec's kills strictly contain the archived oracle's. (CORRECTION,
parent-verified: the delegate's `archived_oracle_killed` column carried the
CURRENT-TREE oracle verdicts (23), not the archived arm's; the archived
scoring CSV itself records 15 kills, and the paper reports 15. Recomputed
from the primary CSVs.) The 20 spec3-only kills are mutants below the
compiled oracle's abstraction, reached because the spec's
State-and-observations section fixes the sensor-to-guard semantics that the
Beh-prose model leaves uninterpreted. Spec3-only ids:
lre_cmp_swap_003, lre_cmp_swap_004, lre_cmp_swap_005, lre_cmp_swap_006, lre_cmp_swap_007, lre_cmp_swap_008, lre_cmp_swap_009, lre_drop_negation_007, lre_off_by_one_004, lre_off_by_one_009, lre_relop_flip_000, lre_relop_flip_001, lre_relop_flip_002, lre_relop_flip_003, lre_relop_flip_004, lre_relop_flip_005, lre_relop_flip_009, lre_sentinel_init_006, lre_sentinel_init_007, lre_trigger_swap_004.

**Comparison vs the round-2 blind spec (40/62 — INTERNAL, not for the paper):**
intersection 35, spec2-only 5, spec3-only 0. The 5 mutants the round-2 spec
killed and this one does not are exactly the priority/threshold cases:
- `branch_swap_014` (CAM exit-priority swap): both CAM exits target OCM; under
  set semantics both `(OCM,[])` and `(OCM,[advVel(0)])` are members whenever
  T17 and T18 are both enabled, and where only one is enabled the mutant takes
  that one. 0 membership violations — the spec's own closing section discloses
  this: "Priority-only differences may therefore remain undetected."
- `off_by_one_000..003` (`LreConstants` thresholds 1→2): scored with the spec
  bound to the mutant's declared constants (DM4 default-as-configuration
  reading, per A12's "no added ordering precondition"); the mutant then refines
  its own re-bound relation — 0 violations. Sensitivity under the alternative
  literal-default binding (spec thresholds fixed at 1): all four become kills
  with 144–18,178 outside-eligible-set cells each (off_by_one_000: 22,084
  violation cells / 18,178 unambiguous; _001: 576/144; _002: 2,268/660;
  _003: 1,728/396). We report the conservative attribution.

**Misses (27):**
- 13 branch_swaps (001–012, 014): guard-reorderings the round-2 run verified
  relation-equivalent (disjoint guards) or, for 014, priority-only — no
  behavioural difference reaches a set-valued R with no priority.
- 5 off_by_one non-kills: 000–003 threshold-binding (above) and 005
  (`vvel>=1`→`vvel>=2` in T11's disjunct, subsumed by T10's `vs ≤ D` disjunct
  under default constants D=V — relation-equivalent; no behavioural oracle can
  kill it).
- 6 sentinel_init (000–005): constructor/field initializations overwritten by
  the `compute()`/`step()` refresh before any guard read; T01 fixes only the
  initial mode, and A09 leaves initial actuator records unspecified.
- 3 actuator-internal (branch_swap_000, trigger_swap_000/001): storage of
  received advice inside `Actuator`; R records emitted advice events, and the
  spec states the mode relation does not depend on the `lastVel`/`lastHdng`
  records (A09) — below the relation's interface.

**Representative unambiguous witnesses** (full list in
`blindspec3_survivor_scoring.csv`):
- **lre_cmp_swap_003** (`isStatic`: `vn==0 && ve==0` → `vn!=0 && ve==0`):
  MOM/reqVel, depth=-0.5, v=(1,0), roc=-1.5, one static obstacle 0.3m ahead
  (`s_close`). Mutant classifies it dynamic → CPA against it → `camActive` →
  CAM; R's enabled set is {T05 (OPEZ, depth≤0), T09/T10 (static caution)} —
  CAM ∉ R; baseline takes HCM+advVel(0) ∈ R.
- **lre_sentinel_init_007** (`closestDynamicIndex` best=-1→0): same cell;
  mutant computes CPA against the static obstacle id 0 → CAM ∉ R.
- **lre_relop_flip_009** (`CheckOPEZ`: `depth<=0` → `depth>0`): OCM/reqMOM,
  depth=-0.5 (surfaced), approaching dynamic obstacle at safe range
  (`d_approach`). Mutant: `inOpez=false` → T04 accepted → MOM+advVel(1);
  R = {(OCM,[])} (T04's guard requires ¬P and P holds) — violation; baseline
  holds ∈ R.
- **lre_off_by_one_009** (`inOpez`: `depth<=0` → `depth<=1`): OCM/reqMOM,
  depth=0.5, `d_approach`: mutant rejects a request whose T04 guard holds;
  R = {(MOM,[advVel(1)])} and hold ∉ R ("no optional stuttering when a row is
  enabled") — violation; baseline enters MOM ∈ R.
- **lre_cmp_swap_009** (`obsEwVel` accessor zeroed): MOM/reqVel, depth=-0.5,
  v=(1,1), roc=-1.5, side-crossing dynamic obstacle (`d_side_t0`): mutant's
  corrupted relative velocity yields `camActive` → CAM ∉ R = {(OCM,[]) via
  T05}; baseline OCM ∈ R.

## 3. Method notes and limitations

1. Static analysis throughout; identical parser and grid machinery as the
   round-2 run (restored from its transcript), with the relation, kill rule,
   and attribution re-implemented for set-valued membership semantics. The
   concrete grid was extended (8,928 scenarios: +2 equidistant-obstacle tie
   configurations for A05; 29→31 obstacle configs) — ties produced no
   divergence (Java's strict-< ascending scan = spec's smallest-id rule).
2. Kill verdicts rest on the concrete grid straddling every threshold at 1 and
   2 including equality points, CPA geometries with tcpa <0/=0/>0, cda
   <1/=1/<2/=2/>2, absence, zero-relative-velocity, and tie/ordering cases.
   Guards on both sides are single-variable threshold comparisons over the
   derived quantities, so per sign pattern the grid is exhaustive for the
   mode-relation vocabulary.
3. The A08 ledger entry leaves CPA for existing dynamic obstacles abstract
   ("the transition relation accepts them as observations"). For concrete
   scoring we supplied `cda`/`tcpa` from the audited system's own declared
   convention (the baseline CalcCPA formula) — the reading most favourable to
   the implementation; zero-relative-velocity cells, where that convention is
   undefined (0/0), are A14 domain edges and never decide a verdict. This is
   exactly the sensitivity the spec's A08 text instructs a checker to disclose.
4. Output comparison is name+value (`advVel:0/1/v`, `advHdng:h`), including
   the spec's rule that destination-entry advice is part of the row's sequence
   and never re-emitted (matches Java inline emission; A17 row identity is
   recorded in the conformance CSV's enabled-row column).
5. The four LreConstants mutants are scored under DM4's
   default-as-configuration reading (conservative); the literal-1 sensitivity
   is reported above rather than silently chosen.

## 4. Provenance

The reference specification's own attestation, quoted verbatim:

> "Drafted by me from lre_requirement_all.txt (SHA-256 above) alone; I did not
> consult the FORGE repository, any implementation artefact, or any prior spec
> draft, and the interpretive decisions are my own."

> "This is a requirements-level mode-transition reference, drafted on
> 2026-09-08. The digest was verified before the source was read. The source
> contains 51 distinct LRE entries, declares exactly four operating modes, and
> contains no SB-prefixed identifiers or five-mode declaration. Only the
> supplied requirements file was used as evidence. The output was created in a
> new directory outside the workspace without inspecting any repository or
> existing draft. Exposure disclosure: no prohibited material was present in
> the conversation context available to me; I cannot audit exposure in
> conversations outside that context."

Nothing stronger is claimed here. This scoring run (spec-to-system comparison)
was performed by a different session with full repository access, after the
spec was frozen as the artifact cited above; the spec was never modified. The
spec's closing section states that no extracted model, mutation population,
verifier result, or measured kill rate was consulted during drafting.

## 5. Files

- `spec3_guard_mapping.md` — symbol/row-to-system vocabulary mapping, the
  set-valued semantics deltas, and the documented representational deltas.
- `blindspec3_conformance.csv` — all 57,344 abstract cells: atoms, the full
  R set (destinations|outputs), enabled row ids, Java destination/output,
  membership verdict.
- `blindspec3_survivor_scoring.csv` — 62 rows: killed, attribution, mechanism,
  witness, archived-oracle verdict + overlap class (paper-facing), round-2
  spec verdict (internal).
- `blindspec3_report.md` — this report.
