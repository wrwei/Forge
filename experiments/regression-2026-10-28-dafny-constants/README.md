# Regression check: Dafny rendering of `Double` constants (28 October 2026)

The Dafny generator (`forge.transformations/.../java2dafny.egl`) used to emit
`Double.MAX_VALUE` as the bare identifier `MAX_VALUE`, and any `double` that Java
prints in scientific notation (`1.0E-9`) verbatim; Dafny resolves neither. It now
writes the decimal Java prints, in plain notation (`0.000000001`), and
`Double.MAX_VALUE` / `Double.MIN_VALUE` the same way; that decimal converts back
to the same double. The vacuity audit's Dafny-constant reader
(`forge.dashboard/web/vacuity_sat.py`) now also accepts an exponent. The change
was pre-registered before any scoring: Amendment 2 of the cold-baseline-v2
pre-registration in the paper repository.

## Method

Every Java source behind a result scored with this repository's pipeline was run
through T2M → M2M → Dafny/RCT/Isabelle generation
(`experiments/mutation-testing/driver/CampaignDriver.java`) twice, with the
templates before and after the change, and the generated `.dfy` files were
compared byte for byte: the three reference runs, the 186 mutants, and every
iteration of the convergence, ablation, Diagnosis-only, multi-controller and
cold-baseline collections (339 sources). Folders that ship Dafny but no Java
(the Steam Boiler, 3 files; the cold-baseline replay, 60 files) were scanned for
`MAX_VALUE`, `MIN_VALUE`, an exponent or the int-clamp value `2147483647.0`.

## Result

- 331 of 339 sources: identical Dafny. Both generations completed for all 339
  (`regression.tsv`: source, driver OK before, driver OK after, `.dfy` files,
  same/changed).
- 8 sources change, each in one constant only (`changed.diff`). All are first
  iterations of loop runs, not reported converged code: Table 7 LRE runs 1–3, and
  Diagnosis-only (D2) LRE runs 1–4 and SRanger run 5. With the change, each of
  the eight Dafny files verifies (12 verified, 0 errors for the LRE files; 7 for
  the SRanger file).
- The reference runs, all 186 mutants and every converged iteration are
  unchanged, so no mutation-campaign or converged-code result changes.
- No Dafny file in the Steam Boiler or cold-baseline-replay folders contains an
  affected constant.
