# Mutation campaign (Section 4.8, RQ6)

The 186 mutant sources (`mutants/<id>/java/`, generated deterministically from the three unmutated
sources `reference-runs/<study>/java` by `mutant_generator.py`, manifest in `mutants_manifest.csv`)
and the three unmutated sources were run end to end through the extractor at Forge commit `44c1492e` (T2M, M2M, Dafny, RCT, Isabelle, then the RoboChart CSP generator), and every
generated artefact was checked by its verifier.

## Files

- `kill_table.csv` — one row per mutant: which artefacts changed, each verifier's outcome, and the
  disposition (`outside`, `killed`, `rejected`, `survived`). This is Table 10 of the paper.
- `mutants/<id>/java/`, `mutants_manifest.csv` — the 186 mutant sources and, per mutant, the file,
  line, original and mutated text; `scripts/mutant_generator.py` produces them from the unmutated
  sources with seed 20260815.
- `outputs/<id>/` — every subject's generated artefacts (Dafny program, RoboChart model and `.rct`,
  Isabelle theory, the CSP tree under `rct-project/csp-gen/`), the extractor's trace files, driver
  log, Dafny logs and `record.json`; `outputs/baseline-<study>/` the three unmutated subjects;
  `outputs/results.csv` the per-subject record. (The T2M model `discovered_model.xmi` is omitted; the
  driver regenerates it.)
- `fdr4/` — `results.csv` and the `refines --format framed_json` output per checked subject.
- `isabelle/` — `results.csv` and a `build.log` per built theory.
- `requirements-scoring/` — the three requirements-level checks of Section 4.9 applied to the 134
  survivors, combined (`combined_verdicts.csv`); the per-instrument programs and files live with
  their relations in `../requirements-conformance/` and `../blind-spec/`.
- `driver/CampaignDriver.java` — runs the extractor's phases headlessly on one source tree.
- `scripts/regen_campaign.py` — runs the driver and the CSP generator on all 189 subjects and the Dafny
  column; `scripts/campaign_verifiers.sh` — the FDR4 and Isabelle columns (FDR4 licence and the
  pipeline's Docker Isabelle required; resumable); `scripts/canon.py` — the artefact comparison;
  `scripts/apply_corrections_standalone.py` — the pipeline's CSP range corrections, applied to a
  staged copy before FDR4 runs.

## How the columns were read

- **Boundary.** A mutant is inside the abstraction boundary if its Isabelle theory, Dafny program or
  RoboChart model differs from the unmutated one after comments are removed and the order of set
  elements and disjuncts is normalised (`canon.py`): the generator's set iteration order varies from
  run to run, so a raw text comparison would count ordering noise as a change. Seven mutants (six
  sentinel initialisers and one dropped negation) differ only in that ordering and are outside.
- **Dafny.** `dafny verify` on every generated program, 120 s cap as in the pipeline. Killed = at least
  one error. 276 of 276 programs verify.
- **FDR4.** The pipeline's type-range corrections applied to a staged copy of `csp-gen/`, determinism
  assertions set aside as the pipeline does, 1,200 s cap. Killed = an assertion fails. A subject whose
  CSP is identical to the baseline's inherits the baseline verdict (all three baselines pass). A subject
  whose CSP FDR4 refuses to load (ill-typed channel communication or out-of-range value) is **rejected**:
  the toolchain could not represent the mutated code, and no verifier verdict exists. 15 mutants.
- **Isabelle.** The pipeline's Isabelle (Docker), `-o timeout=600`, on every theory that differs from
  the baseline's, one session name per (subject, theory). 166 builds verified, 0 timed out, 1 theory
  (`chemical_detector_trigger_swap_000`, also an FDR4 rejection) is ill-typed and is counted as rejected.

## Reproducing

From the repository root, with JDK 21, the extractor built (`cd forge.transformations && ./gradlew
classes`), Dafny, FDR4 and Docker:

    python3 experiments/mutation-testing/scripts/regen_campaign.py        # artefacts + Dafny column
    bash experiments/mutation-testing/scripts/campaign_verifiers.sh       # FDR4 + Isabelle columns

Both are resumable (a subject with a record is skipped). The generator's set iteration order varies
between JVM runs, so regenerated theories may differ from the shipped ones in element order only;
`scripts/canon.py` is the comparison that ignores it.

## Result

| disposition | mutants |
|---|---:|
| outside the boundary | 34 |
| inside, killed (all by FDR4 assertion failure) | 3 |
| inside, rejected by the toolchain | 15 |
| inside, survived every verifier | 134 |
| **total** | **186** |
