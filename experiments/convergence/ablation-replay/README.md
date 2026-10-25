# Compile-only ablation stop points, re-scored by the published extractor

The Java at the stop point of each of the 15 compile-only ablation runs of
`../ablation/` (the last iteration of each run, the point at which all eight
visible phases passed and the developer, seeing no verifier output, stopped),
each run through the published pipeline exactly once, verifiers included. This
is what the paper's Section 4.7 (RQ5) and Table 9 report. The submitted paper had
scored the same stop points with the extractor of that time; those verdicts
remain in `../ablation/` and are discussed in the response letter.

- `<study>/run-N/` — one pass, laid out as in `../../cold-baseline-replay/`;
  `source.json` names the stop iteration the Java was taken from.
- `superseded-d88ad2a1/` — first-pass records of the five Chemical Detector
  passes rerun after the pre-registration's amendment 1
  (`../../cold-baseline-replay/preregistration.md`).
- `RESULTS.md`, `results.csv` — the aggregate.

Result: 12 of 15 stop points pass every verifier and the vacuity audit; the
other three stop at a generation failure of the pipeline (one extractor crash,
two ill-typed CSP modules); no stop point fails a verifier on the code.
