# Mutation-testing experiment (RQ6, Section 4.8)

This folder is the complete evidence set for the mutation-testing experiment
reported in Section 4.8 (RQ6) of the paper. Every number in that section can
be recomputed from `kill_table_all_backends.csv`.

## Summary tables
- `kill_table_all_backends.csv` — one row per mutant (186 rows): defect class,
  injection file/line, before/after text, whether each generated artefact
  (.thy / .dfy / .rct) changed, and the per-backend verdict
  (`dafny_killed`, `fdr4_killed`, `isabelle_killed`, `isabelle_outcome`).
  Headline counts: 142 mutants change at least one artefact, 44 are invisible;
  Dafny kills 13, FDR4 kills 18, Isabelle kills 1, no mutant is killed by both
  Dafny and FDR4; 11 Isabelle builds exceed the per-goal budget and are
  reported as inconclusive, not as kills.
- `kill_table_with_fdr4.csv`, `kill_table.csv` — earlier cuts of the same
  table (Dafny-only, then +FDR4), kept for provenance.
- `mutants_manifest.csv` — the full mutant list as generated
  (deterministic, seed 20260815), before any pipeline run.

## Reproduction inputs
- `mutants/` — every mutated Java source tree, one folder per mutant.
- `baseline/` — the unmutated subjects and their generated artefacts.
- `outputs/` — per-mutant generated artefacts (.thy / .dfy / .rct / .csp /
  model .xmi) produced by the pipeline, diffed against `baseline/` to decide
  `model_changed_*`.
- `driver/` — the experiment driver (T2M -> M2M -> Dafny/RCT/Isabelle per mutant).
- `scripts/` — the FDR4 and Isabelle arm runners and the Isabelle timeout
  re-checks, as run.
- `prefix-snapshot/` — the extractor exactly as it was when the experiment ran
  (the pipeline sources and templates, staged read-only for the runs). The
  experiment measures THIS extractor, which is the one the paper's Table 6 runs
  used; it predates the C1/C2/T1 repairs described in Section 5.5.
- `fdr4-arm/`, `isabelle-arm/`, `isabelle-timeouts/` — per-backend logs and
  verdicts.
- `subj_*.txt` / `subj_*.err`, `*_changed.txt`, `classpath.txt`, `srcs.txt`,
  `all_jars.txt`, `rest_jars.txt` — run logs and build inputs.
- `EVIDENCE_README.md` — the archive note written when this set was packaged,
  including provenance detail.

## Relation to the paper
Section 4.8 (RQ6) reports the experiment; Section 5.5 explains why the 44
invisible mutants fall exactly where the correspondence table (Table 9)
predicts. The experiment ran on the pre-repair extractor deliberately: it
measures the system whose runs the paper reports in Table 6.
