# Mutation campaign, Isabelle column under environment model B

Re-run of the Isabelle column of `../../mutation-testing/` (186 mutants) with the
repaired generator (environment model B, see `../preregistration.md`, Follow-up 3).
Input: the campaign's archived per-mutant `robochart_model.xmi`. Built: every theory
that differs from its study's baseline (131 builds, 125 mutants; `plan.tsv`) plus
the three baselines (four theories). Pre-registration was written before the run.

## Result

| | archived run (Table 9) | model-B re-run |
|---|---|---|
| theory changed | 86 | 125 (both Chemical Detector controllers now built; data-variable updates added to the generator since) |
| verified | 74 | 124 |
| killed | 1 (`chemical_detector_trigger_swap_000`, ill-typed theory) | 1 (the same mutant, same cause) |
| no verdict (timeout) | 11 | 0 |

No verdict changed in either direction (`isabelle_column_archived_vs_modelB.csv`,
cross-tab: every archived `verified` stays verified, every archived `timeout`
becomes verified, the archived kill stays a kill). The four pre-registered
expectations all held: baselines verify; the 11 timeouts resolve to verdicts;
no kill arose from the totality lemma; kills >= the archived 1 (equal).

Per-build logs are in `logs/` (`Finished <session>` present in every verified
build; `quick_and_dirty=false`; no `sorry` in any theory). Median build 186 s,
maximum 1106 s (`MovementController_Beh`, 4 threads).

## Environment outcomes during the run (not verdicts)

* 4 baseline rows `proof_failed` in the first pass: a hyphen in the generated
  session name, rejected by Isabelle's ROOT parser before any proof. Fixed; rebuilt.
* 14 builds `unknown_rc=137`/`OOMKILL` across two passes: Poly/ML killed by the
  Docker VM's out-of-memory killer. Cause: the heap ceiling in
  `~/.isabelle-docker/etc/settings` was never read (the distribution's
  `etc/settings` points `ISABELLE_HOME_USER` at `/root/.isabelle/Isabelle2023`
  inside the container). `isabelle-docker-maxheap.sh` mounts the settings where
  Isabelle reads them; the runner refuses to start unless `--maxheap` is in
  effect (`B6_REQUIRE_MAXHEAP=1`). All affected builds then verified.
* `SUMMARY-interleaved-runs.txt` is the raw log of all passes; `results.csv`
  keeps the last row per build.
