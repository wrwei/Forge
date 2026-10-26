# Requirements-conformance checking: SRanger and the Chemical Detector

This folder extends the LRE requirements-conformance comparison (the parent
folder) to the other two case studies, covering both of the Chemical
Detector's controllers. For each controller, the behavioural requirements were
translated into a mode-level transition relation and compared, cell by cell,
against the extracted Isabelle theory; the mutants of the mutation-testing
experiment (`../../mutation-testing`) that survive every verifier were then
scored against the same relations.

## What was compiled, and by whom

The three relations (`requirements_model_*.json`: 7 SRanger entries, 7
GasAnalysis entries, 23 Movement entries) were translated by the authors
during the revision, with the implementations already in existence. Unlike the
LRE relation in the parent folder, no ordering attestation is claimed for
them. What stands in its place is the audit trail: every entry carries the
requirement sentence verbatim beside the compiled formula
(`prose_formula_table_*.md`), every interpretive decision is logged
(`ambiguity_log_multi.md`), and the comparison itself is a published program a
reader can re-run.

## Results

Conformance of the extracted models:

| controller | cells | agreement |
|---|---|---|
| SRanger | 60 | 60/60 (100%) |
| GasAnalysis | 100 | 100/100 (100%) |
| Movement | 140 | 140/140 (100%) |

The Movement figure rests on one logged interpretive decision (MV-A1 in the
ambiguity log): the requirements read "once in Found, the subsystem stays
halted", and the generated implementation realises this as a silent,
observably equivalent step from Found into a terminal sink state, which the
extracted model represents faithfully. The relation therefore admits both
encodings at Found, following the set-valued convention used wherever the
wording admits more than one reading. This is an encoding convention, not an
extraction error; the strict single-successor reading (120/140) is recorded in
the log and recoverable by dropping the entry's `also_admits` field. No other
interpretive decision affects any verdict.

Scoring the 80 SRanger and Chemical Detector mutants that survive every verifier
(`../../mutation-testing/kill_table.csv`, `disposition == survived`):

| study | survivors | killed by the relation |
|---|---|---|
| SRanger | 21 | 8 |
| Chemical Detector | 59 | 23 (17 via Movement cell violations, 5 via GasAnalysis cell violations, 1 via a guard-bound divergence) |

By defect class: trigger-event swaps 16/17, comparison swaps 7/9,
relational-operator flips 4/4, dropped negations 1/1, off-by-one
thresholds 1/12 (the one whose mutated bound reaches the generated guard text),
branch-priority swaps 2/37 (the set-valued relation admits either order where the
requirements state no priority; the two kills change the gas-analysis outcome in
cells where the requirements fix it). The kill counts are identical under the
strict Found reading, so no kill depends on decision MV-A1.

Example kill: `chemical_detector_trigger_swap_005` replaces the Stop trigger
with Tick in the Waiting state, so on a stop event the mutant stays in Waiting
instead of halting in Found; the witness cell is Waiting+stop, expected Found.
Every backend passes this mutant.

## Files

| file | contents |
|---|---|
| `requirements_model_sranger.json`, `requirements_model_cd_ga.json`, `requirements_model_cd_mv.json` | the compiled relations |
| `prose_formula_table_*.md` | requirement sentence beside compiled formula, one row per entry |
| `ambiguity_log_multi.md` | the six logged interpretive decisions |
| `run_reqconf_multi.py` | the cell-by-cell comparison (also `--check`) |
| `score_survivors.py` | survivor scoring against the relations (also `--check`, `--idle-check`) |
| `multi_conformance_baseline.csv` | the 300 per-cell verdicts |
| `multi_survivor_scoring.csv` | per-mutant verdict, mechanism, witness |
| `idle_check.csv` | per theory, the check that each `Idle_<State>` operation is the complement of that state's transitions |

## Reproduction

From the repository root, with Python 3 (the theories are the campaign's, in
`experiments/mutation-testing/outputs/`):

    python3 experiments/requirements-conformance/multi-study/run_reqconf_multi.py --out /tmp/base.csv --check
    python3 experiments/requirements-conformance/multi-study/score_survivors.py --out /tmp/score.csv --check

Both `--check` runs verify the recomputed rows byte-identical against the
shipped CSVs. `Idle_<State>` operations (the Java else branch) are not
transitions and are excluded by the parser wrapper in `run_reqconf_multi.py`;
`score_survivors.py --idle-check FILE` verifies for every theory that each is
the complement of its state's transitions (`idle_check.csv`: holds for all).
