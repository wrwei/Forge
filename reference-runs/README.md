# Case Studies — Companion Artefacts

This directory contains the **converged paper-companion artefacts** for the three
case studies reported in the FORGE paper:

| Study                                                  | Controllers | Modes | Requirements | Snapshot from iteration\* |
| ------------------------------------------------------ | ----------- | ----- | ------------ | ---------------------- |
| [`lre/`](lre/) — Last Response Engine (AUV)            | 1           | 4     | 51           | 4                      |
| [`chemical_detector/`](chemical_detector/) — Chemical Detector | 2           | 13    | 81           | 9                      |
| [`sranger/`](sranger/) — SRanger (ground robot)        | 1           | 3     | 23           | 5                      |

\* Each artefact is a snapshot from one converged trajectory; the iteration
index is that trajectory's converging iteration, **not** a headline convergence
result. The paper reports convergence as a distribution over nine independent
runs (three per study; median 3, range 2--4).

> **Scope.** This directory is curated for paper readers. It is **not** the
> pipeline's working state — that lives under `forge.assets/case-studies/`
> (tool input) and `forge.transformations/output/` (tool output). Files here
> are snapshots taken from each study's converged-iteration git branch
> (`lre-iter4-converged`, `chemdetector-iter9-converged`, `sranger-iter5-converged`)
> and regenerated end-to-end so artefacts match source.

## Layout per study

```
<study>/
  README.md                          ← per-study summary (LOC, modes, verification result)
  system-description/                ← the natural-language description handed to the LLM
    system_description.md
  requirements/                      ← the structured requirements specification
  java/                              ← the converged Java controller source
  formal-artefacts/
    dafny/                           ← Dafny specification (.dfy)
    csp/                             ← RoboChart concrete syntax (.rct) + CSP-M scripts (.csp)
    isabelle/                        ← Isabelle/UTP Z-machine theory (.thy) + ROOT
```

## Reproducing verification

The formal artefacts can be checked locally without running the upstream
generation pipeline:

```bash
# Dafny
dafny verify <study>/formal-artefacts/dafny/*.dfy

# FDR4 (CSP-M) — the shipped instantiations.csp already has [0..1] type ranges
# applied (required: wider ranges either won't compile or exhaust Windows' page file)
"<FDR4_PATH>/refines.exe" <study>/formal-artefacts/csp/csp-gen/defs/<TopController>_coreassertions.csp

# Isabelle/UTP — runs in WSL on Windows, native on Linux/macOS
isabelle build -D <study>/formal-artefacts/isabelle -v -o timeout=600
```

All three are expected to pass: Dafny with 0 errors, FDR4 deadlock-free and
divergence-free (the `:[deterministic]` assertions fail as expected, where
overlapping `else if` guards become concurrent transitions — a known, fixable
M2M limitation), and Isabelle with all lemmas (including `deadlock_free`) proved.

## License

MIT — see [`../LICENSE`](../LICENSE).
