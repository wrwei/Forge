# Round-3 blind LRE reference spec — conformance of the shipped system and scoring of the 54 survivors

Inputs: `LRE-reference-spec.md` (drafted 2026-09-08, READ-ONLY, never modified);
the LRE Java `reference-runs/lre/java` (read-only); the 54 LRE mutants that survive
every verifier of the mutation-testing experiment (`../mutation-testing/kill_table.csv`,
`study == lre`, `disposition == survived`), with their sources under
`../mutation-testing/mutants/`; and, for the complementarity columns, the compiled
oracle's per-mutant verdicts (`../requirements-conformance/survivor_scoring.csv`).
Method: the spec's SET-VALUED relation `R(m,e,x) = {(destination(t), outputs(t)) : t ∈
E(m,e,x)}` with explicit hold row, implemented directly from the document and
validated against 11 boundary/overlap consequences from the spec's own tables
before any comparison (all pass, including the CAM/reqOCM two-member set, the MOM
triple overlap, the tcpa<0 CAM-stuck case, and the D>V alternation). The Java
`step()` chain and every mutant's chain parsed statically (brace-matching + guard
ASTs) — no Java executed; data-plane edits re-simulated with Java `double`
semantics (`Double.MAX_VALUE` sentinels, 0/0 = NaN, all-comparisons-false on NaN).
The program is `blindspec3_score.py`; `--check` reproduces the shipped scoring.

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
refines the spec's relation exactly: membership-agreement 100%, with
no priority substitution needed** — the spec's explicitly nondeterministic A02
resolution leaves no priority-order disagreement class.

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

## 2. Survivor scoring (54 all-backend survivors)

Population: the 54 `mutant_id` rows of `../mutation-testing/kill_table.csv` with
`study == lre` and `disposition == survived` (verified: each mutant source tree
differs from the unmutated one in exactly the manifest-named file). Kill rule for a
set-valued R: the mutant's `(dest, outs)` is NOT a member of `R(m,e,x)` at a concrete
cell where the baseline's outcome IS a member. Attribution: a kill cell is
*ledger-attributed* when the deciding difference is a ledger choice (A07/A08/A14
CPA-edge cells, incl. all NaN cells, and A12 threshold binding); *unambiguous* when
the mutant's outcome is outside every enabled row and the hold case under both the
spec's own and the Java-convention-substituted atom derivations, on a non-NaN cell.

| class | n | spec kills | unambiguous | ledger-attr | compiled oracle | both | spec-only | oracle-only |
|---|---|---|---|---|---|---|---|---|
| relop_flip | 10 | 10 | 10 | 0 | 3 | 3 | 7 | 0 |
| cmp_swap | 3 | 3 | 3 | 0 | 3 | 3 | 0 | 0 |
| trigger_swap | 7 | 7 | 7 | 0 | 7 | 7 | 0 | 0 |
| off_by_one | 10 | 5 | 5 | 0 | 3 | 3 | 2 | 0 |
| branch_swap | 14 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
| drop_negation | 7 | 7 | 7 | 0 | 7 | 7 | 0 | 0 |
| sentinel_init | 3 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
| **total** | **54** | **32** | **32** | **0** | **23** | **23** | **9** | **0** |

**Every kill is unambiguous** — each killed mutant has at least one non-NaN
concrete cell where its outcome lies outside the entire requirements-eligible
set (all enabled rows + hold) under BOTH atom-derivation conventions, while the
baseline's outcome is a member. Zero kills depend on a ledger choice.

**Complementarity vs the compiled oracle (23/54, `../requirements-conformance`):**
union **32**, intersection **23**, spec-only **9**, oracle-only **0** — the spec's
kills strictly contain the compiled oracle's. The 9 spec-only kills are mutants
below the compiled oracle's abstraction (relational-operator flips and off-by-one
changes inside the computation of a guard value), reached because the spec's
State-and-observations section fixes the sensor-to-guard semantics that the
compiled relation leaves uninterpreted. Spec-only ids:
lre_off_by_one_004, lre_off_by_one_009, lre_relop_flip_000, lre_relop_flip_001, lre_relop_flip_002, lre_relop_flip_003, lre_relop_flip_004, lre_relop_flip_005, lre_relop_flip_009.

**Misses (22):**
- 14 branch_swaps (001, 002, 003, 004, 005, 006, 007, 008, 009, 010, 011, 012, 013, 014): guard-reorderings that are relation-equivalent
  (disjoint guards) or priority-only — no behavioural difference reaches a
  set-valued R with no priority. The spec's own closing section discloses this:
  "Priority-only differences may therefore remain undetected."
- 5 off_by_one non-kills: 000–003 (`LreConstants` thresholds 1→2) are scored with
  the spec bound to the mutant's declared constants (DM4 default-as-configuration
  reading, per A12's "no added ordering precondition"); the mutant then refines
  its own re-bound relation — 0 violations. Sensitivity under the alternative
  literal-default binding (spec thresholds fixed at 1): all four become kills.
  We report the conservative attribution. 005 (`vvel>=1`→`vvel>=2` in T11's
  disjunct) is subsumed by T10's `vs ≤ D` disjunct under default constants D=V —
  relation-equivalent; no behavioural oracle can kill it.
- 3 sentinel_init (000–002): constructor/field initialisations overwritten by the
  `compute()`/`step()` refresh before any guard read; T01 fixes only the initial
  mode, and A09 leaves initial actuator records unspecified.

**Representative unambiguous witnesses** (full list in
`blindspec3_survivor_scoring.csv`):
- **lre_relop_flip_009** (`CheckOPEZ`: `depth<=0` → `depth>0`): OCM/reqMOM,
  depth=-0.5 (surfaced), approaching dynamic obstacle at safe range
  (`d_approach`). Mutant: `inOpez=false` → T04 accepted → MOM+advVel(1);
  R = {(OCM,[])} (T04's guard requires ¬P and P holds) — violation; baseline
  holds ∈ R.
- **lre_off_by_one_009** (`inOpez`: `depth<=0` → `depth<=1`): OCM/reqMOM,
  depth=0.5, `d_approach`: mutant rejects a request whose T04 guard holds;
  R = {(MOM,[advVel(1)])} and hold ∉ R ("no optional stuttering when a row is
  enabled") — violation; baseline enters MOM ∈ R.
## 3. Method notes and limitations

1. Static analysis throughout; the relation, kill rule and attribution are
   implemented for set-valued membership semantics. The concrete grid has
   8,928 scenarios, including 2 equidistant-obstacle tie configurations for
   A05 (31 obstacle configurations) — ties produced no divergence (Java's
   strict-< ascending scan = spec's smallest-id rule).
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
- `blindspec3_survivor_scoring.csv` — 54 rows: killed, attribution, mechanism,
  witness.
- `blindspec3_score.py` — the program that produces it (`--check` reproduces the
  shipped file).
- `blindspec3_report.md` — this report.
